package ite.smu.socketmanager_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ChatRoomUserAdapter extends ArrayAdapter {

    private ArrayList<ChatUserItem> chatUserItems = new ArrayList<ChatUserItem>();

    public ChatRoomUserAdapter(Context context, ArrayList userList) {
        super(context, 0);
    }

    public void add(ChatUserItem item) {
        chatUserItems.add(item);
        super.add(item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        View row = convertView;

        if (row == null) {
            // inflator를 생성하여, chatting_message.xml을 읽어서 View객체로 생성한다.
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.room_user_item, parent, false);
        }

        ChatUserItem chatUserItem = chatUserItems.get(position);
        TextView username = (TextView)row.findViewById(R.id.roomUserTextView);
        username.setText(chatUserItem.getUserString());

        return row;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public Object getItem(int position) {
        return super.getItem(position);
    }
}
