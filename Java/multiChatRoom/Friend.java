import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;


class Friend {
	private static String userID;
	private static String usernick;
	private static List friendArray = new ArrayList(); //
	
	Friend() {
		
	}
	
	Friend(String id) {
		this.userID = id;
	}
	
	public String getUser() {
		return userID;
	}
	
	public void setFriendArray() {	
		File file = new File("C:\\NetworkHW\\chatText\\" + userID + ".txt");
		String str = null;
		
		try {
			FileReader freader1 = new FileReader(file);
			BufferedReader breader1 = new BufferedReader(freader1);
			str = breader1.readLine();
			StringTokenizer token = new StringTokenizer(str, "/");
			str = token.nextToken(); str = token.nextToken();
			while (token.hasMoreElements()) {
				str = token.nextToken();
				this.setFriend(str);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setFriend(String friendID) {
		this.friendArray.add(friendID);
	}
	
	public String getFriend() {
		StringBuffer sb = null;
		while (friendArray.iterator().hasNext()) {
			sb.append(friendArray.iterator().next().toString() + "/");
		}
			return sb.toString();
	}
	
	public String toProtocol() {
		String list = "";
		for (int i = 0; i < friendArray.size(); i++) {
			list += "/" + friendArray.get(i);
		}
		return userID + "/" + list;
	}
}
