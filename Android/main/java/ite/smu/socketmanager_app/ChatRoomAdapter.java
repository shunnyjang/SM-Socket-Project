package ite.smu.socketmanager_app;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class ChatRoomAdapter extends ArrayAdapter {

    //List roomsList = new ArrayList();

    public static ArrayList<ChatRoomItem> chatRoomItemsList = new ArrayList<ChatRoomItem>();

    public ChatRoomAdapter(Context context, ArrayList room) {
        super(context, 0);
    }

    public void add(ChatRoomItem roomItem){
        chatRoomItemsList.add(roomItem);
        //super.add(roomItem);
    }

    public int getCount() {
        return chatRoomItemsList.size();
    }

    public void clearAllItems() {
        chatRoomItemsList.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Context context = parent.getContext();

        View row = convertView;

        if (row == null) {
            // inflator를 생성하여, chatting_message.xml을 읽어서 View객체로 생성한다.
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.room_list_item, parent, false);
        }

        //Room room = (Room)rooms.get(position);
        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획

        // Data Set(chatRoomItemsList)에서 position에 위치한 데이터 참조 획득
        ChatRoomItem chatRoomItem = chatRoomItemsList.get(position);
        TextView roomTitle = (TextView) row.findViewById(R.id.roomTitleTextView) ;
        roomTitle.setText(chatRoomItem.getRoomInfo());

        return row;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return chatRoomItemsList.get(position);
    }

    public String getNum(int position) {
        return chatRoomItemsList.get(position).getRoomNumber();
    }
}
