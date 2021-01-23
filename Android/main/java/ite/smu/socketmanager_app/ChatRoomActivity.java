package ite.smu.socketmanager_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ChatRoomActivity extends AppCompatActivity {

    public static String userId, userNick;
    public static String msgID, msgSend, msgReceive;
    public static Handler roomUserHandler, chatRoomHandler;
    public Room room;

    private static final int left = 1;
    private static final int right = 0;
    private ArrayList<User> userArray;
    public static SocketManager socketManager;
    public static SocketListener socketListener;
    public static Socket socket = null;
    private String roomNumber;

    ListView userListView, roomChatListView;
    ChatRoomUserAdapter chatRoomUserAdapter;
    ChatMessageAdapter chatMessageAdapter;
    Button sendBtn, exitBtn;
    EditText chatEditText;
    TextView roomIdTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        // socket 연결
        socketManager = MainActivity.socketManager;
        socketListener = MainActivity.socketListener;
        try {
            socket = socketManager.getSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 채팅방 입장 사용자 목록 객체 초기화
        userArray = new ArrayList<User>();
        room = new Room();
        // 채팅방 번호 - MainActivity
        final Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        roomNumber = bundle.getString("number");

        socketListener.getInRoom(socketManager.user.getId(), roomNumber);
        Room room = new Room();

        roomIdTextView = (TextView)findViewById(R.id.roomId);
        userListView = (ListView)findViewById(R.id.roomUserListView);
        roomChatListView = (ListView)findViewById(R.id.roomChatting);
        sendBtn = (Button)findViewById(R.id.roomChattingSend);
        exitBtn = (Button)findViewById(R.id.exitButton);
        chatEditText = (EditText)findViewById(R.id.roomChattingEditText);

        roomIdTextView.setText(roomNumber);

        chatRoomUserAdapter = new ChatRoomUserAdapter(this, userArray);
        chatMessageAdapter = new ChatMessageAdapter(this, 0);
        userListView.setAdapter(chatRoomUserAdapter);
        roomChatListView.setAdapter(chatMessageAdapter);


        roomUserHandler = new Handler(){
            public void handleMessage(Message msg){
                switch(msg.what){
                    case left:
                        chatRoomUserAdapter.add(new ChatUserItem(userId, userNick));
                        userId = ""; userNick = "";
                        break;
                    case right:
                        break;
                }
            }
        };

        chatRoomHandler = new Handler(){
            public void handleMessage(Message msg){
                switch(msg.what){
                    case left:
                        break;
                    case right:
                        chatMessageAdapter.add(new ChatMessage(false, msgSend));
                        msgSend = "";
                        break;
                }
            }
        };

        sendBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = chatEditText.getText().toString();
                socketListener.echoMsg(roomNumber, socketManager.user.toString() + msg + "/sendMobile");
                chatEditText.setText("");
            }
        });

        exitBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    socketManager.getDos().writeUTF(User.GETOUT_ROOM + "/" + roomNumber);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                socketListener.getOutRoom(roomNumber);
                //Intent exitIntent = new Intent(ChatRoomActivity.this, MainActivity.class);
                //startActivityForResult(exitIntent, 1);
                finish();
            }
        });
    }

    // 뒤로 가기 버튼 없앰
    @Override
    public void onBackPressed() {
        // nothing
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

}
