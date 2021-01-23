package multiChatRoom;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

public class SixClient implements Runnable {

	private static int PORT = 5555; // ������Ʈ��ȣ
	//private static int PORT = 5656; // ������Ʈ��ȣ
	private static String IP = ""; // �����������ּ�
	private Socket socket; // ����
	private User user; // �����

	public LoginUI login;
	public RestRoomUI restRoom;
	private DataInputStream dis = null;
	private DataOutputStream dos = null;
	public boolean ready = false;

	SixClient() {
		login = new LoginUI(this);
		// ������ ����
		Thread thread = new Thread(this);
		thread.start();
	}

	public static void main(String[] args) {
		System.out.println("Client start...");
		new SixClient();
	}

   @Override
   public void run() {
      //
      // ���� ��� ����
      //
      while (!ready) {
         try {
            Thread.sleep(10);
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
      }

      // ����ڰ� ��ü ���� �� �����Ǽ���
      user = new User(dis, dos);
      user.setIP(socket.getInetAddress().getHostAddress());

      // �޽��� ����
      while (true) {
         try {
            String receivedMsg = dis.readUTF(); // �޽��� �ޱ�(���)
            dataParsing(receivedMsg); // �޽��� �ؼ�
         } catch (IOException e) {
            e.printStackTrace();
            try {
               user.getDis().close();
               user.getDos().close();
               socket.close();
               break;
            } catch (IOException e1) {
               e1.printStackTrace();
            }
         }
      }

      errorMsg("�������α׷��� ���� ����Ǿ����ϴ�.");
      // ä�����α׷� ����
      restRoom.dispose();
   }

   public boolean serverAccess() {
      if (!ready) {
         // ������ ������ �̷������ ���� ��쿡�� ����
         // ��, ó�� ����ÿ��� ����
         socket = null;
         IP = login.ipBtn.getText();
         try {
            // ��������
            InetSocketAddress inetSockAddr = new InetSocketAddress(
                  InetAddress.getByName(IP), PORT);
            socket = new Socket();

            // ������ �ּҷ� ���� �õ� (3�ʵ���)
            socket.connect(inetSockAddr, 3000);
         } catch (UnknownHostException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
         }
         

         // ������ �Ǹ� ����
         if (socket.isBound()) {
            // �Է�, ��� ��Ʈ�� ����
            try {
               dis = new DataInputStream(socket.getInputStream());
               dos = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
               e.printStackTrace();
            }
            ready = true;
         }
      }
      return ready;
   }

   // �����͸� ����
   public synchronized void dataParsing(String data) {
      StringTokenizer token = new StringTokenizer(data, "/"); // ��ū ����
      String protocol = token.nextToken(); // ��ū���� �и��� ��Ʈ��
      String id, pw, rNum, nick, rName, msg, result, flag;
      System.out.println("���� ������ : " + data);
      
      switch (protocol) {
      case User.LOGIN: // �α���
         // ����ڰ� �Է���(������) ���̵�� �н�����
         result = token.nextToken();

         if (result.equals("OK")) {
            nick = token.nextToken();
            login(nick);
         } else {
            msg = token.nextToken();
            errorMsg(msg);
         } 
         break;
      case User.LOGOUT:
         logout();
         break;
      case User.MEMBERSHIP: // ȸ������ ����
         result = token.nextToken();
         if (result.equals("OK")) {
            errorMsg("ȸ������ ����!");
         } else {
            errorMsg("�̹� ���ԵǾ� �ִ� ���̵��Դϴ�.");
         }
         break;
      case User.INVITE: // �ʴ�ޱ�
         id = null;
         // �Ѹ� �ʴ�
         while (token.hasMoreTokens()) {
            // �ʴ��� ����� ���̵�� ���ȣ
            id = token.nextToken();
            rNum = token.nextToken();
            invite(id, rNum);
         }
         break;
      case User.UPDATE_USERLIST: // ���� ����� ���
         userList(token);
         break;
      case User.UPDATE_ROOM_USERLIST: // ä�ù� ����� ���
         // ���ȣ�б�
         rNum = token.nextToken();
         userList(rNum, token);
         break;
      case User.UPDATE_SELECTEDROOM_USERLIST: // ���ǿ��� ������ ä�ù��� ����� ���
         selectedRoomUserList(token);
         break;
      case User.UPDATE_ROOMLIST: // �� ���
         roomList(token);
         break;
      case User.UPDATE_FRIENDLIST:
    	  friendList(token);
    	  break;
      case User.CHANGE_NICK: // �г��� ����(����)f
         nick = token.nextToken();
         changeNick(nick);
         break;
      case User.ECHO01: // ���� ����
         msg = token.nextToken();
         echoMsg(msg);
         break;
      case User.ECHO02: // ä�ù� ����
         rNum = token.nextToken();
         msg = token.nextToken();
         echoMsgToRoom(rNum, msg);
         break;
      case User.WHISPER: // �ӼӸ�
         id = token.nextToken();
         nick = token.nextToken();
         msg = token.nextToken();
         whisper(id, nick, msg);
         break;
      case User.NEWFRIEND:
    	  result = token.nextToken();
    	  if (result.equals("OK"))
    		  errorMsg("ģ�� �߰� ����!");
    	  else if (result.equals("already"))
    		  errorMsg("�̹� ģ���� ȸ���Դϴ�");
    	  else
    		  errorMsg("�������� �ʴ� ȸ���Դϴ�.");
    	  break;
      case User.DELFRIEND:
    	  result = token.nextToken();
    	  if (result.equals("OK"))
    		  errorMsg("ģ���� �����߽��ϴ�.");
    	  else
    		  errorMsg("ģ���� �ƴ� ȸ���Դϴ�.");
    	  break;
      }
   }

   private void logout() {
      try {
         restRoom.dispose();
         user.getDis().close();
         user.getDos().close();
         socket.close();
         restRoom = null;
         user = null;
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
   
   // ä�ù� ���� ����� ����Ʈ
   private void userList(String rNum, StringTokenizer token) {
      for (int i = 0; i < user.getRoomArray().size(); i++) {
         if (Integer.parseInt(rNum) == user.getRoomArray().get(i)
               .getRoomNum()) {

            // ������ ����Ʈ�� ���� ��� ������
            if (user.getRoomArray().get(i).getrUI().model != null)
               user.getRoomArray().get(i).getrUI().model
                     .removeAllElements();

            while (token.hasMoreTokens()) {
               // ���̵�� �г����� �о ���� ��ü �ϳ��� ����
               String id = token.nextToken();
               String nick = token.nextToken();
               User tempUser = new User(id, nick);

               user.getRoomArray().get(i).getrUI().model
                     .addElement(tempUser.toString());
            }
         }
      }
   }

   // ������ ä�ù��� ����� ����Ʈ
   private void selectedRoomUserList(StringTokenizer token) {
      // �����κ��� ��������Ʈ(ä�ù�)�� ������Ʈ�϶�� ����� ����

      if (!restRoom.level_2_1.isLeaf()) {
         // ������尡 �ƴϰ�, ���ϵ尡 �ִٸ� ��� ����
         restRoom.level_2_1.removeAllChildren();
      }
      while (token.hasMoreTokens()) {
         // ���̵�� �г����� �о ���� ��ü �ϳ��� ����
         String id = token.nextToken();
         String nick = token.nextToken();
         User tempUser = new User(id, nick);

         // ä�ù� ����ڳ�忡 �߰�
         restRoom.level_2_1.add(new DefaultMutableTreeNode(tempUser
               .toString()));
      }
      restRoom.userTree.updateUI();
   }

   // ���� ����� ����Ʈ
   private void userList(StringTokenizer token) {
      // �����κ��� ��������Ʈ(����)�� ������Ʈ�϶�� ����� ����

      if (restRoom == null) {
         return;
      }

      if (!restRoom.level_2_2.isLeaf()) {
         // ������尡 �ƴϰ�, ���ϵ尡 �ִٸ� ��� ����
         restRoom.level_2_2.removeAllChildren();
      }
      
      while (token.hasMoreTokens()) {
         // ���̵�� �г����� �о ���� ��ü �ϳ��� ����
         String id = token.nextToken();
         String nick = token.nextToken();
         User tempUser = new User(id, nick);

         for (int i = 0; i < restRoom.userArray.size(); i++) {
            if (tempUser.getId().equals(restRoom.userArray.get(i))) {
            }
            if (i == restRoom.userArray.size()) {
               // �迭�� ������ ������ �߰�����
               restRoom.userArray.add(tempUser);
            }
         }
         // ���� ����ڳ�忡 �߰�
         restRoom.level_2_2.add(new DefaultMutableTreeNode(tempUser
               .toString()));
      }
      restRoom.userTree.updateUI();
   }
   
   // ģ�� ��� ����Ʈ
   private void friendList(StringTokenizer token) {
	      // �����κ��� ��������Ʈ(ģ��)�� ������Ʈ�϶�� ����� ����
	      if (restRoom == null) {
	         return;
	      }

	      if (!restRoom.friend_level_1.isLeaf()) {
	         // ������尡 �ƴϰ�, ���ϵ尡 �ִٸ� ��� ����
	         restRoom.friend_level_1.removeAllChildren();
	      }

	      String id = token.nextToken();
	      String nick = token.nextToken();
	      //Friend usrFriend = new Friend(id);
	      //usrFriend.setFriendArray();
	      String tempFriend = null;
	      
	      while (token.hasMoreElements()) {
	    	  tempFriend = token.nextToken();
	    	  restRoom.friendArray.add(tempFriend);
	    	  restRoom.friend_level_1.add(new DefaultMutableTreeNode(tempFriend));
	      }
	      restRoom.friendTree.updateUI();
	   }

   // �����κ��� �渮��Ʈ�� ������Ʈ�϶�� ����� ����
   private void roomList(StringTokenizer token) {
      String rNum, rName;
      Room room = new Room();

      // ������ ����Ʈ�� ���� ��� ������
      if (restRoom.model != null) {
         restRoom.model.removeAllElements();
      }

      while (token.hasMoreTokens()) {
         rNum = token.nextToken();
         rName = token.nextToken();
         int num = Integer.parseInt(rNum);

         // ��Ʈ��ѹ��� ������Ʈ (�ִ밪+1)
         if (num >= restRoom.lastRoomNum) {
            restRoom.lastRoomNum = num + 1;
         }
         room.setRoomNum(num);
         room.setRoomName(rName);

         restRoom.model.addElement(room.toProtocol());
      }
   }

   private void errorMsg(String string) {
      int i = JOptionPane.showConfirmDialog(null, string, "�̺�Ʈ �߻�",
            JOptionPane.CLOSED_OPTION, JOptionPane.ERROR_MESSAGE);
      // Ȯ�� ������ ����
      if (i == 0) {

      }
   }

   private void login(String nick) {
      // �α������� ������
      user.setId(login.idText.getText());
      user.setNickName(nick);

      // �α���â �ݰ� ����â ����
      login.dispose();
      restRoom = new RestRoomUI(SixClient.this);
      restRoom.lb_id.setText(user.getId());
      restRoom.lb_ip.setText(user.getIP());
      restRoom.lb_nick.setText(user.getNickName());
   }
   
   private void whisper(String id, String nick, String msg) {
      restRoom.restRoomArea.append(nick+"("+id+")���� �ӼӸ� : "+msg+"\n");
   }

   private void invite(String id, String rNum) {
   }

   private void changeNick(String nick) {
      user.setNickName(nick);
      restRoom.lb_nick.setText(nick);
   }

   private void echoMsg(String msg) {
      // Ŀ�� ��ġ ����
      if (restRoom != null) {
         restRoom.restRoomArea.setCaretPosition(restRoom.restRoomArea
               .getText().length());
         restRoom.restRoomArea.append(msg + "\n");
      }
   }

   private void echoMsgToRoom(String rNum, String msg) {
      for (int i = 0; i < user.getRoomArray().size(); i++) {
         if (Integer.parseInt(rNum) == user.getRoomArray().get(i)
               .getRoomNum()) {

            // ����� -> ��迭 -> ������ -> �ؽ�Ʈ�����
            // Ŀ�� ��ġ ����
            user.getRoomArray().get(i).getrUI().chatArea
                  .setCaretPosition(user.getRoomArray().get(i).getrUI().chatArea
                        .getText().length());
            // ����
            user.getRoomArray().get(i).getrUI().chatArea.append(msg + "\n");
         }
      }
   }

   // getter, setter
   public static int getPORT() {
      return PORT;
   }

   public static void setPORT(int pORT) {
      PORT = pORT;
   }

   public static String getIP() {
      return IP;
   }

   public static void setIP(String iP) {
      IP = iP;
   }

   public Socket getSocket() {
      return socket;
   }

   public void setSocket(Socket socket) {
      this.socket = socket;
   }

   public User getUser() {
      return user;
   }

   public void setUser(User user) {
      this.user = user;
   }

   public LoginUI getLogin() {
      return login;
   }

   public void setLogin(LoginUI login) {
      this.login = login;
   }

   public RestRoomUI getRestRoom() {
      return restRoom;
   }

   public void setRestRoom(RestRoomUI restRoom) {
      this.restRoom = restRoom;
   }

   public DataInputStream getDis() {
      return dis;
   }

   public void setDis(DataInputStream dis) {
      this.dis = dis;
   }

   public DataOutputStream getDos() {
      return dos;
   }

   public void setDos(DataOutputStream dos) {
      this.dos = dos;
   }

   public boolean isReady() {
      return ready;
   }

   public void setReady(boolean ready) {
      this.ready = ready;
   }

}
