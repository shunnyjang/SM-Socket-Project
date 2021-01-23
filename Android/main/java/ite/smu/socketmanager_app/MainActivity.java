package ite.smu.socketmanager_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    public static String msgID, msgSend, msgReceive; // 대기실 채팅 목록 id, send, receive
    public static String roomInfo, roomID, roomName;
    public static Handler mainHandler, roomHandler;
    public static String whisperID;
    public static Boolean isWhispered = false;

    private static final int left = 1;
    private static final int right = 0;
    public static SocketListener socketListener;
    public static SocketManager socketManager;
    public static ArrayList<User> userArray;  // 현재 접속자 목록
    public static ArrayList<Room> roomArray;  // 생성된 채팅방 목록

    Socket socket = null;
    EditText chatText;
    TextView idTxt, chatTextView;
    ListView chatListView, roomListView;
    Button sendBtn, whisperBtn;
    ChatMessageAdapter chatMessageAdapter;  // 대기실 메세지 목록 어댑터
    ChatRoomAdapter chatRoomAdapter;        // 챙팅방 목록 어댑터

    String host, id, pw;
    public static Boolean sendBtnClicked = false;

    public Handler handler_L;
    public Handler handler_R;
    public Handler handler_W;

    //@SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        socketManager = SocketManager.getSocketManager();

        if(Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        final Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        host = bundle.getString("host");
        id = bundle.getString("id");
        pw = bundle.getString("pw");

        userArray = new ArrayList<User>(); // 접속자 목록 객체 선언
        roomArray = new ArrayList<Room>(); // 채팅방 목록 객체 선언

        idTxt = (TextView)findViewById(R.id.idTxt);
        chatText = (EditText)findViewById(R.id.chatText);   // 대기실 채팅 메세지 입력창
        chatTextView = (TextView)findViewById(R.id.chatTextView);
        chatListView = (ListView)findViewById(R.id.listView);   // 대기실 채팅 목록 리스트뷰
        roomListView = (ListView)findViewById(R.id.chatRoomListView);   // 채팅방 목록 리스트뷰

        sendBtn = (Button)findViewById(R.id.sendBtn);
        whisperBtn = (Button)findViewById(R.id.whisperBtn);

        chatText.setText("");
        chatMessageAdapter = new ChatMessageAdapter(this, 0);   // 대기실 채팅 어댑터 객체 생성
        chatRoomAdapter = new ChatRoomAdapter(this, roomArray);                  // 채팅방 목록 어댑터 객체 생성
        chatListView.setAdapter(chatMessageAdapter);
        roomListView.setAdapter(chatRoomAdapter);

        socketManager.setIdv(id);   // 로그인
        socketManager.setPwv(pw);   // 비밀번호
        socketManager.setHostv(host);

        // 로그인
        try {
            socket = socketManager.getSocket();
            socketManager.login();
            Log.i("CHECK","socket login 시작");
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.getMessage();//위치 바꾸면 안됨

        // 대기실 채팅 핸들러
        mainHandler = new Handler(){
            public void handleMessage(Message msg){
                Log.i("CHECK", "handler 작동");
                switch(msg.what){
                    case left:
                        msgReceive = msgID + msgReceive;
                        chatMessageAdapter.add(new ChatMessage(true, msgReceive));
                        msgReceive = "";
                        break;
                    case right:
                        msgSend = msgID + msgSend;
                        chatMessageAdapter.add(new ChatMessage(false, msgSend));
                        msgSend = "";
                        break;
                }
            }
        };

        // 대기실 메세지 전송 버튼
        sendBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                sendBtnClicked = true;
                String msg = chatText.getText().toString();
                socketListener.rstroomflag = "me";
                try {
                    socketListener.restRoomMsg
                            (msg + "/false", id);
                    chatText.setText("");
                } catch (IOException e) {
                    e.printStackTrace();
                }
				sendBtnClicked = false;
                chatText.requestFocus();
                socketListener.rstroomflag = "you";
            }
        });

        //귓속말 버튼
        whisperBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                sendBtnClicked = true;
                String msg = chatText.getText().toString();
                try {
                    isWhispered = true;
                    socketListener.restRoomMsg(msg + "/false" , id);
                    chatText.setText("");
                    isWhispered = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
				sendBtnClicked = false;
                chatText.requestFocus();
            }
        });

        // 채팅방 핸들러
        roomHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case left:  // 채팅방 생성
                        chatRoomAdapter.add(new ChatRoomItem(roomID, roomName));
                        chatRoomAdapter.notifyDataSetChanged();
                        break;
                    case right: // 채팅방 삭제
                        Log.i("CHECK", "채팅방 삭제 - adapter clear");
                        chatRoomAdapter.clearAllItems();
                        chatRoomAdapter.notifyDataSetChanged();
                        break;
                }
            }
        };

        // 채팅방 입장
        roomListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent1 = new Intent(MainActivity.this, ChatRoomActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("number", chatRoomAdapter.getNum(position));
                intent1.putExtras(bundle);
                startActivity(intent1);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        chatMessageAdapter = new ChatMessageAdapter(this.getApplicationContext(),R.layout.chatting_message);
        final ListView listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(chatMessageAdapter);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL); // 이게 필수

        // When message is added, it makes listview to scroll last message
        chatMessageAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatMessageAdapter.getCount()-1);
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings)
            return true;

        return super.onOptionsItemSelected(item);
    }

    public void getMessage(){
        socketListener = new SocketListener(MainActivity.this, mainHandler, socketManager, userArray, roomArray);
        socketListener.start();
    }
}



