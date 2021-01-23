package ite.smu.socketmanager_app;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

public class SocketManager {
    public final static String HOST = "192.168.0.12";
    //public final static String HOST = "192.168.42.198";//서버주소설정
    //public final static String HOST = "10.101.44.54";//서버주소설정
    //public final static String HOST = "172.30.1.29";//서버주소설정

    public final static int PORT = 5555;
    //public final static int PORT = 5656;
    public static boolean ready = false;
    private static Socket socket = null;
    private static DataInputStream dis = null;
    private static DataOutputStream dos = null;
    public static User user; // 사용자
    private String idv, pwv, hostv;
    private static SocketManager socketManager;

    static {
        try {
            socketManager = new SocketManager();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SocketManager() throws IOException {
      // this.socket = getSocket();
        //getSocket();

    }

    public static SocketManager getSocketManager(){
        return socketManager;
    }

    //기존의 serverAccess()함수의 역할을 하는 함수 getSocket()
    public static Socket getSocket() throws IOException
    {
        if (!ready) {
            // 소켓이 연결이 이루어지지 않은 경우에만 실행
            // 즉, 처음 연결시에만 실행
            socket = null;

            try {

                InetSocketAddress inetSockAddr = new InetSocketAddress(InetAddress.getByName(HOST), PORT);// 서버접속
                socket = new Socket();
                socket.connect(inetSockAddr, 7000);// 지정된 주소로 접속 시도 (3초동안)
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 접속이 되면 실행
        if (socket.isBound()) {
            // 입력, 출력 스트림 생성
            try {
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());

            } catch (IOException e) {
                e.printStackTrace();
            }
            ready = true;
        }
        return socket;
    }

    public static void closeSocket() throws IOException
    {
        if ( socket != null )
            socket.close();
    }

    public static void sendMsg(String msg) throws IOException
    {
        getSocket().getOutputStream().write((msg + '\n').getBytes());
    }

    public void login() throws IOException {
        if (ready) {
            try {
                // 로그인정보(아이디+패스워드) 전송
                getDos().writeUTF(
                        User.LOGIN + "/" + idv + "/"
                                + pwv);
                // 사용자가 객체 생성 및 아이피설정
                user = new User(dis, dos);
                user.setIP(socket.getInetAddress().getHostAddress());
                user.setId(idv);

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        Log.i("SocketManager", "login성공");
    }



    //getter, setter 설정. socket, idv, pwv, dis, dos, HOST, PORT, ready
    public Socket returnSocket(){
        return socket;
    }

    public void setIdv(String id){
        this.idv = id;
    }

    public void setPwv(String pw){
        this.pwv = pw;
    }

    public void setHostv(String host) { this.hostv = host; }

    public String getIdv(){
        return idv;
    }

    public String getPwv(){
        return pwv;
    }

    public static DataInputStream getDis(){
        return dis;
    }

    public DataOutputStream getDos(){
        return dos;
    }

    public boolean getReady(){
        return ready;
    }

    public String getHost(){
        return HOST;
    }

    public int getPORT(){
        return PORT;
    }
}

