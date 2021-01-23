package ite.smu.socketmanager_app;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class SocketListener extends Thread{

    private InputStream im;
    private BufferedReader br;
    private SocketManager socketManager;
    private Handler mHandler = null;

    public ArrayList<Room> roomArray;
    public ArrayList<User> userArray;
    public ArrayList<User> roomUserArray;
    private static int left = 1;
    private static int right = 0;
    private static String whisperID = "", whisperFlag = "";
    String rstroomflag = "you";
    Context context;

    public SocketListener(Context context, Handler handler, SocketManager sockmng,
                          ArrayList<User> userArray, ArrayList<Room> roomArray) {
        this.mHandler = handler;
        this.context = context;
        this.socketManager = sockmng;
        this.roomArray = roomArray;
        this.userArray = userArray;
        this.userArray.add(socketManager.user);
    }

    @Override
    public void run() {
        super.run();
        // 메시지 리딩
        while (true) {
            try {
                String receivedMsg = socketManager.getDis().readUTF(); // 메시지 받기(대기)
                dataParsing(receivedMsg); // 메시지 해석
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    socketManager.user.getDis().close();
                    socketManager.user.getDos().close();
                    socketManager.getSocket().close();
                    break;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        Log.i("msgSummit", "서버프로그램이 먼저 종료되었습니다");
    }

    // 데이터를 구분
    public synchronized void dataParsing(String data) throws IOException {
        Log.i("CHECK","receive msg : " + data.toString());
        StringTokenizer token = new StringTokenizer(data, "/"); // 토큰 생성
        String protocol = token.nextToken(); // 토큰으로 분리된 스트링
        String id, pw, rNum, nick, rName, rUser, msg, result, flag;

        int idx1 = data.indexOf("(");
        int idx2 = data.indexOf(")");
        String msgReceive ="";
        String msgID = "";

        System.out.println("idx1 : " + idx1 + "idx2: " + idx2);

        if(idx1 >= 0 && idx2 >= 0) {
            msgID = data.substring(idx1 + 1,idx2);
            msgReceive = data.substring(idx2 + 1);
        } else {
            msgID = null;
        }
       // System.out.println("받은 데이터 : " + data + " msgID : " + msgID + " msgReceive " + msgReceive);

        switch (protocol) {
//            case User.LOGOUT:
//                logout();
//                break;
            case User.LOGIN:
                result = token.nextToken();
                if (result.equals("OK")) {
                    nick = token.nextToken();
                    socketManager.user.setNickName(nick.toString());
                } else {
                    msg = token.nextToken();
                }
                break;
            case User.UPDATE_USERLIST:
                userList(token);
                break;
            case User.UPDATE_ROOM_USERLIST: // 채팅방 사용자 목록
                // 방번호읽기
                rNum = token.nextToken();
                flag = null;
                StringBuilder builder = new StringBuilder();
                while (token.hasMoreTokens()) { // 사용자 목록 끝 flag를 찾음
                    flag = token.nextToken();
                    builder.append(flag + "/");
                }

                if (flag.equals("server")) {
                    // 서버가 반복되는 걸 막는다
                } else {
                    userList(rNum, builder);
                }
                break;
            case User.UPDATE_ROOMLIST: // 방 목록
                roomList(token);
                break;
            case User.ECHO01:           // 대기실 메세지 에코
                msg = token.nextToken();
                if (token.hasMoreTokens()) {
                    flag = token.nextToken();
                } else {
                    flag = "false";
                }
                if (flag.equals("true")) { // 내가 보낸 메세지
                    //whisperFlag = "true";
                    restRoomMsg(msg, msgID);
                }
                else if(rstroomflag.equals("me")){
                    restRoomMsg(msg, msgID);
                    rstroomflag = "you";
                }
                else if(rstroomflag.equals("you")){
                    restRoomMsg(msg, msgID);
                    // 처음 로그인했을 때 에코 / 내가 보낸 메세지 에코
                }
                break;
            case User.ECHO02: // 채팅방 에코
                rNum = token.nextToken();
                msg = token.nextToken();
                echoMsg(rNum, msg);
                break;
            case User.CREATE_ROOM:
                break;
            case User.GETOUT_ROOM:
                rNum = token.nextToken();
                getOutRoom(rNum);
                break;
            case User.MOBILE_IN_ROOM:
                rUser = token.nextToken();
                rNum = token.nextToken();
                getInRoom(rUser, rNum);
                break;
            case User.MOBILE_OUT_ROOM:
                rNum = token.nextToken();
                if (token.hasMoreTokens()) {
                    flag = token.nextToken();
                } else {
                    flag = "";
                }

                if (flag.equals("server")) {
                    deleteEmptyRoom(rNum);
                } else {
                    //
                }
                break;
            case User.WHISPER: //내가 상대방으로부터 귓속말을 받았을때
                whisperID = "";
                whisperID = token.nextToken();
                token.nextToken();
                msg = token.nextToken();
                if (token.hasMoreTokens()) {
                    flag = token.nextToken();
                    whisperID = msg;//나에게 귓속말하는 상대의 아이디 저장
                } else {
                    flag = "false";
                }
                flag = msg;
                if (!flag.equals("false")) { // 상대방이 빈스트링을 보내지 않는 이상 들어감
                    restRoomMsg(msg, whisperID);

                } else {
                    // 내가 귓속말 상대가 아닌 경우
                }
                break;
        }
    }

    public void restRoomMsg(String msg, String msgID) throws IOException {
        // 메세지 전송
        Log.i("CHECK", "flag :" + whisperFlag);
        if (msg.equals("/false")) {
            // 아무것도 입력되지 않은 메세지 - 리스트뷰에 false 찍히는 것 방지
        } else {
            if(!MainActivity.isWhispered && !whisperID.equals("")){ //귓속말 수신시
                if (whisperFlag.equals("true")) {
                    // 메세지 수신
                    Log.i("CHECK", "대기실 메시지 수신 성공");
                    //Message message = mHandler.obtainMessage();
                    Message message = MainActivity.mainHandler.obtainMessage();
                    message.what = left;
                    MainActivity.msgID = "";
                    MainActivity.msgReceive = msg;
                    MainActivity.mainHandler.sendMessage(message);
                    whisperFlag = "false";
                } else {
                    Log.i("CHECK", "대기실 귓속말 수신 성공");
                    //Message message = mHandler.obtainMessage();
                    Message message = MainActivity.mainHandler.obtainMessage();
                    message.what = left;
                    MainActivity.msgID = "";
                    msg = whisperID + "님으로부터 받은 귓속말: " + msg;
                    MainActivity.msgReceive = msg;
                    MainActivity.mainHandler.sendMessage(message);
                }
            } else if (!msgID.equals("null")) { // 정상적으로 로그인 된 계정
                if (!msg.equals("") && msgID.equals(socketManager.user.getId())) {
                    if (MainActivity.isWhispered) {
                        //귓속말
                        try {
                            // 대기실에 메시지 보냄
                            //.getDos().writeUTF(User.WHISPER + "/" + id + "/" + nickName + "/" + msg);
                            StringTokenizer tokenizer = new StringTokenizer(msg, "/");

                            MainActivity.msgID = socketManager.user.toString();
                            String targetWhisper = tokenizer.nextToken().toString();
                            MainActivity.msgSend =  targetWhisper + "님에게 보내는 귓속말 : " + tokenizer.nextToken().toString();;

                            socketManager.getDos().writeUTF(User.WHISPER + "/" + targetWhisper + "/" + MainActivity.msgSend);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.i("CHECK", "listener whisper: 귓속말 메시지 전송 성공");
                        Message message = new Message();
                        message.what = right;
                        MainActivity.mainHandler.sendMessage(message);
                    } else if(MainActivity.sendBtnClicked == true) {
                        try {
                            Log.i("CHECK", "listener : 대기실 전송시도 msg = " + msg);

                            // 대기실에 메시지 보냄
                            socketManager.getDos().writeUTF(User.ECHO01 + "/" + msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.i("CHECK", "listener : 대기실 메시지 전송 성공");
                        StringTokenizer tokenizer = new StringTokenizer(msg, "/");
                        Message message = new Message();
                        message.what = right;
                        MainActivity.msgID = socketManager.user.toString();
                        MainActivity.msgSend = tokenizer.nextToken().toString();
                        MainActivity.mainHandler.sendMessage(message);
                        MainActivity.sendBtnClicked = false;
                    }
                } else if (!msg.equals("") && MainActivity.sendBtnClicked == false) {
                    // 메세지 수신
                    Log.i("CHECK", "대기실 메시지 수신 성공");
                    //Message message = mHandler.obtainMessage();
                    Message message = MainActivity.mainHandler.obtainMessage();
                    message.what = left;
                    MainActivity.msgID = "";
                    MainActivity.msgReceive = msg;
                    MainActivity.mainHandler.sendMessage(message);
                }
            } else { // 로그인 거부 된 계정
                // 대기실 이용 불가
            }
        }
    }

    // 모바일 유저 채팅방 입장
    public void getInRoom(String rUser, String rNum) {
        User tempUser = new User();
        for (int i = 0; i < userArray.size(); i++) {
            if (userArray.get(i).getId().equals(rUser)) {
                tempUser = userArray.get(i);
                try {
                    tempUser.getDos().writeUTF(User.MOBILE_IN_ROOM + "/" + rUser + "/" + rNum + "/mobile");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }

        for (int i = 0; i < roomArray.size(); i++) {
            if (rNum.equals(roomArray.get(i).getRoomNum())) {
                roomArray.get(i).getUserArray().add(socketManager.user);
            }
        }
    }

    public void getOutRoom(String rNum) {
        for (int i = 0; i < roomArray.size(); i++) {
            if (rNum.equals(roomArray.get(i).getRoomNum())) {
                for (int j = 0; j < roomArray.get(j).getUserArray().size(); j++) {
                    if (socketManager.user.getId().equals(roomArray.get(i).getUserArray().get(j).getId())) {
                        roomArray.get(i).getUserArray().remove(j);
                    }
                }
            }

            for (int j = 0; j < socketManager.user.getRoomArray().size(); j++) {
                if (rNum.equals(socketManager.user.getRoomArray().get(j).getRoomNum())) {
                    socketManager.user.getRoomArray().remove(j);
                }
            }
            echoMsg(roomArray.get(i).getRoomNum(), socketManager.user.toString() + "님이 퇴장하셨습니다.");
            userList(rNum);

            if (roomArray.get(i).getUserArray().size() <= 0) {
                roomArray.remove(i);
                roomList();
            }
        }
    }

    private void deleteEmptyRoom(String rNum) {
        for (int i = 0; i < roomArray.size(); i++) {
            if (roomArray.get(i).getRoomNum().equals(rNum)) {
                roomArray.remove(i);
                Log.i("CHECK", rNum + "삭제");
                roomList();
            }
        }
    }

    public void echoMsg(String rNum, String msg) {
        // 서버 전송
        try {
            socketManager.user.getDos().writeUTF(User.ECHO02 + "/" + rNum + "/" + msg + "/mobile");
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringTokenizer token = new StringTokenizer(msg, "/");
        String uploadedMsg = token.nextToken();
        String flag = "";
        if (token.hasMoreTokens()) {
            flag = token.nextToken();
        }
        if (flag.equals("sendMobile")) {
            // 모바일에서 보내는 메세지 두 번 보내지는 것 방지
        } else {
            Message message = ChatRoomActivity.chatRoomHandler.obtainMessage();
            message.what = right;
            ChatRoomActivity.msgID = socketManager.user.toString();
            ChatRoomActivity.msgSend = uploadedMsg;
            ChatRoomActivity.chatRoomHandler.sendMessage(message);
        }
    }

    // 사용자 리스트 (채팅방 내부)
    public void userList(String rNum, DataOutputStream target) {
        String ul = "/" + rNum;

        for (int i = 0; i < roomArray.size(); i++) {
            if (rNum.equals(roomArray.get(i).getRoomNum())) {
                for (int j = 0; j < roomArray.get(i).getUserArray().size(); j++) {
                    // 채팅방에 접속되어 있는 유저들의 아이디+닉네임
                    ul += "/"
                            + roomArray.get(i).getUserArray().get(j)
                            .toProtocol();
                }
            }
        }
        try {
            // 데이터 전송
            target.writeUTF(User.UPDATE_ROOM_USERLIST + ul + "/mobile");
            Log.d("CHECK", "채팅방 유저 리스트 업데이트");
        } catch (IOException e) {
        }
    }

    // 사용자 리스트 (채팅방 내부 모든 사람들에게 전달)
    public void userList(String rNum) {
        String ul = "/" + rNum;
        Room temp = null;
        for (int i = 0; i < roomArray.size(); i++) {
            if (rNum.equals(roomArray.get(i).getRoomNum())) {
                temp = roomArray.get(i);
                for (int j = 0; j < roomArray.get(i).getUserArray().size(); j++) {
                    // 채팅방에 접속되어 있는 유저들의 아이디+닉네임
                    ul += "/"
                            + roomArray.get(i).getUserArray().get(j)
                            .toProtocol();
                }
            }
        }
        for (int i = 0; i < temp.getUserArray().size(); i++) {
            try {
                // 데이터 전송
                temp.getUserArray().get(i).getDos()
                        .writeUTF(User.UPDATE_ROOM_USERLIST + ul);
                Log.i("CHECK", "성공 : 목록(사용자)-" + ul);
            } catch (IOException e) {
                Log.i("CHECK", "에러 : 목록(사용자) 전송 실패");
            }
        }
    }

    // 유저리스트 업데이트
    public void userList(StringTokenizer token) {
        String uId, uNick;

        // EU/아이디/닉네임/아이디/닉네임... --> userArray에 업데이트
        while (token.hasMoreTokens()) {
            boolean flag = false;
            uId = token.nextToken();
            uNick = token.nextToken();
            for (int i = 0; i < userArray.size(); i++) {        // 이미 접속되어있는 유저인지 확인
                if (uId.equals(userArray.get(i).getId())) {
                    flag = true;
                    break;
                }
            }
            if (flag) {     // 이미 접속되어있는 유저라면 넘어간다
                continue;
            } else {        // 아니라면 userArray에 추가한다 (User 객체 생성)
                User user = new User(uId, uNick);
                userArray.add(user);
            }
        }
    }

    // 채팅방 유저리스트 업데이트
    public void userList(String rNum, StringBuilder builder) {
        Log.i("CHECK", "rnum : " + rNum);
        Log.i("CHECK", "builder : " + builder.toString());
        String uId, uNick;
        StringTokenizer token = new StringTokenizer(builder.toString(), "/");

        // ES/아이디/닉네임/아이디/닉네임... --> userArray에 업데이트
        while (token.hasMoreTokens()) {
            boolean flag = false;
            uId = token.nextToken();
            uNick = token.nextToken();
            int index = 0;
            for (int i = 0; i < userArray.size(); i++) {        // 이미 접속되어있는 유저인지 확인
                if (uId.equals(userArray.get(i).getId())) {
                    index = i;
                    break;
                }
            }
            for (int i = 0; i < roomArray.size(); i++) {
                if (roomArray.get(i).getRoomNum().equals(rNum)) {
                    roomArray.get(i).getUserArray().add(userArray.get(index));
                }
            }
        }
    }

    // 채팅 방리스트
    public void roomList() {
        String rl = "";

        for (int i = 0; i < roomArray.size(); i++) {
            // 만들어진 채팅방들의 제목
            rl += "/" + roomArray.get(i).toProtocol();
        }

        for (int i = 0; i < userArray.size(); i++) {
            try {
                // 데이터 전송
                userArray.get(i).getDos().writeUTF(User.UPDATE_ROOMLIST + rl);
                Log.i("CHECK", "성공 : 목록(방)-" + rl);
            } catch (IOException e) {
                Log.i("CHECK", "에러 : 목록(방) 전송 실패");
            }
        }
    }

    public void roomList(String rNum, String rName) {

        Log.i("CHECK", "채팅방 목록 : " + rNum + "/" + rName);
        Room room = new Room(rName);
        room.setRoomNum(rNum);
        roomArray.add(room);
        roomList();

        roomList(socketManager.user.getDos());
    }

    // 서버로부터 방리스트 업데이트하라는 명령을 받음
    private void roomList(StringTokenizer token) {

        String rNum, rName;

        Message msg = MainActivity.roomHandler.obtainMessage();
        msg.what = right;
        MainActivity.roomHandler.sendMessage(msg);

        while (token.hasMoreTokens()) {
            rNum = token.nextToken();
            rName = token.nextToken();

            Log.i("CHECK", "room number : "  + rNum + " room name : " + rName);

            Message message = MainActivity.roomHandler.obtainMessage();
            message.what = left;
            MainActivity.roomID = rNum;
            MainActivity.roomName = rName;
            MainActivity.roomHandler.sendMessage(message);
        }
    }

    // 채팅방 리스트
    public void roomList(DataOutputStream target) {
        Log.i("CHECK", "채팅방 목록 생성");
        String rl = "";

        for (int i = 0; i < roomArray.size(); i++) {
            // 만들어진 채팅방들의 제목
            rl += "/" + roomArray.get(i).toProtocol();
        }

        //jta.append("test/target\n");
        try {
            // 데이터 전송
            target.writeUTF(User.UPDATE_ROOMLIST + rl + "/false");
        } catch (IOException e) {
        }
    }
}
