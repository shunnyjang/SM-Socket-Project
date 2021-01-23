package multiChatRoom;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class RestRoomUI extends JFrame implements ActionListener {

   public int lastRoomNum = 100;
   public JButton makeRoomBtn, getInRoomBtn, whisperBtn, sendBtn, makeFrBtn, delFrBtn;
   public JTree userTree, friendTree;
   public JList roomList, friendList;
   public JTextField chatField;
   public JTextArea restRoomArea;
   public JLabel lb_id, lb_nick;
   public JTextField lb_ip;

   private SixClient client;
   public ArrayList<User> userArray; // 사용자 목록 배열
   public ArrayList<String> friendArray;
   public String currentSelectedTreeNode;
   public DefaultListModel model, model_2;
   public DefaultMutableTreeNode level_1, friend_level_1;
   public DefaultMutableTreeNode level_2_1;
   public DefaultMutableTreeNode level_2_2;

   public RestRoomUI(SixClient sixClient) {
      setTitle("\uBBA4\uBBA4 Chat");
      userArray = new ArrayList<User>();
      friendArray = new ArrayList<String>();
      client = sixClient;
      initialize();
   }

   private void initialize() {
      setBounds(100, 100, 950, 500);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      JMenuBar menuBar = new JMenuBar();
      setJMenuBar(menuBar);

      JMenu basicMenus = new JMenu("일반");
      basicMenus.addActionListener(this);
      menuBar.add(basicMenus);

      JMenuItem changeNickItem = new JMenuItem(
            "닉네임 바꾸기");
      changeNickItem.addActionListener(this);
      basicMenus.add(changeNickItem);

      JMenuItem exitItem = new JMenuItem("끝내기");
      exitItem.addActionListener(this);
      basicMenus.add(exitItem);

      JMenu helpMenus = new JMenu("도움말");
      helpMenus.addActionListener(this);
      menuBar.add(helpMenus);

      JMenuItem proInfoItem = new JMenuItem(
            "프로그램 정보");
      proInfoItem.addActionListener(this);
      helpMenus.add(proInfoItem);
      getContentPane().setLayout(null);

      JPanel room = new JPanel();
      room.setBorder(new TitledBorder(UIManager
            .getBorder("TitledBorder.border"), "채팅방",
            TitledBorder.CENTER, TitledBorder.TOP, null, null));
      room.setBounds(12, 10, 477, 215);
      getContentPane().add(room);
      room.setLayout(new BorderLayout(0, 0));

      JScrollPane scrollPane = new JScrollPane();
      room.add(scrollPane, BorderLayout.CENTER);

      // 리스트 객체와 모델 생성
      roomList = new JList(new DefaultListModel());
      roomList.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
            // 채팅방 목록 중 하나를 선택한 경우,
            // 선택한 방의 방번호를 전송
            String temp = (String) roomList.getSelectedValue();

            try {
               client.getUser()
                     .getDos()
                     .writeUTF(
                           User.UPDATE_SELECTEDROOM_USERLIST + "/"
                                 + temp.substring(0, 3));
            } catch (IOException e1) {
               e1.printStackTrace();
            }
         }
      });
      model = (DefaultListModel) roomList.getModel();
      roomList.setBackground(new Color(224, 255, 255));
      scrollPane.setViewportView(roomList);

      JPanel panel_2 = new JPanel();
      room.add(panel_2, BorderLayout.SOUTH);

      makeRoomBtn = new JButton("방 만들기");
      makeRoomBtn.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
         }
      });
      makeRoomBtn.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent arg0) {
            // 방만들기 버튼 클릭
            createRoom();
         }
      });
      panel_2.setLayout(new GridLayout(0, 2, 0, 0));
      panel_2.add(makeRoomBtn);

      getInRoomBtn = new JButton("방 들어가기");
      getInRoomBtn.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent arg0) {
            // 방 들어가기
            getIn();
         }
      });
      panel_2.add(getInRoomBtn);

      JPanel user = new JPanel();
      user.setBorder(new TitledBorder(UIManager
            .getBorder("TitledBorder.border"),
            "사용자 목록", TitledBorder.CENTER,
            TitledBorder.TOP, null, null));
      user.setBounds(501, 10, 171, 215);
      getContentPane().add(user);
      user.setLayout(new BorderLayout(0, 0));

      JScrollPane scrollPane_1 = new JScrollPane();
      user.add(scrollPane_1, BorderLayout.CENTER);

      // 사용자목록, 트리구조
      userTree = new JTree();
      userTree.addTreeSelectionListener(new TreeSelectionListener() {
         public void valueChanged(TreeSelectionEvent arg0) {
            currentSelectedTreeNode = arg0.getPath().getLastPathComponent()
                  .toString();
         }
      });
      level_1 = new DefaultMutableTreeNode("참여자");
      level_2_1 = new DefaultMutableTreeNode("채팅방");
      level_2_2 = new DefaultMutableTreeNode("대기실");
      level_1.add(level_2_1);
      level_1.add(level_2_2);

      DefaultTreeModel model = new DefaultTreeModel(level_1);
      userTree.setModel(model);

      scrollPane_1.setViewportView(userTree);

      JPanel panel_1 = new JPanel();
      user.add(panel_1, BorderLayout.SOUTH);
      panel_1.setLayout(new GridLayout(1, 0, 0, 0));

      whisperBtn = new JButton("귓속말");
      whisperBtn.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent arg0) {
            StringTokenizer token = new StringTokenizer(
                  currentSelectedTreeNode, "("); // 토큰 생성
            String temp = token.nextToken(); // 토큰으로 분리된 스트링
            temp = token.nextToken();

            // 닉네임 제외하고 아이디만 따옴
            chatField.setText("/" + temp.substring(0, temp.length() - 1)
                  + " ");
            chatField.requestFocus();
         }
      });
      panel_1.add(whisperBtn);
      
      /* 친구 목록 갱신 */
      JPanel friend = new JPanel();
      friend.setBorder(new TitledBorder(UIManager
            .getBorder("TitledBorder.border"), "친구 목록",
            TitledBorder.CENTER, TitledBorder.TOP, null, null));
      friend.setBounds(684, 10, 231, 405);
      getContentPane().add(friend);
      friend.setLayout(new BorderLayout(0, 0));
      
      friend_level_1 = new DefaultMutableTreeNode("친구목록");
      DefaultTreeModel fModel = new DefaultTreeModel(friend_level_1);
      
      friendTree = new JTree();
      friendTree.setModel(fModel);

      JScrollPane scrollPane_friend = new JScrollPane();
      friend.add(scrollPane_friend, BorderLayout.CENTER);

      // 리스트 객체와 모델 생성
      friendList = new JList(new DefaultListModel());
      friendTree.setBackground(new Color(224, 255, 255));
      scrollPane_friend.setViewportView(friendTree);

      JPanel panel_frlist = new JPanel();
      friend.add(panel_frlist, BorderLayout.SOUTH);

      makeFrBtn = new JButton("친구 추가");
      makeFrBtn.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent arg0) {
        	 // 친구 추가
        	 addFriend();
         }
      });
      panel_frlist.setLayout(new GridLayout(0, 2, 0, 0));
      panel_frlist.add(makeFrBtn);

      delFrBtn = new JButton("친구 삭제");
      delFrBtn.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent arg0) {
            // 방 들어가기
            //친구삭제 만들기!!!
            ////
        	 deleteFriend();
         }
      });
      panel_frlist.add(delFrBtn);
      
      JPanel restroom = new JPanel();
      restroom.setBorder(new TitledBorder(UIManager
            .getBorder("TitledBorder.border"), "대 기 실",
            TitledBorder.CENTER, TitledBorder.TOP, null, Color.DARK_GRAY));
      restroom.setBounds(12, 235, 477, 185);
      getContentPane().add(restroom);
      restroom.setLayout(new BorderLayout(0, 0));

      JPanel panel = new JPanel();
      restroom.add(panel, BorderLayout.SOUTH);
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

      JScrollPane scrollPane_4 = new JScrollPane();
      panel.add(scrollPane_4);

      chatField = new JTextField();

      chatField.addKeyListener(new KeyAdapter() {
         @Override
         public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
               msgSummit();
            }
         }

      });
      scrollPane_4.setViewportView(chatField);
      chatField.setColumns(10);

      sendBtn = new JButton("보내기");
      sendBtn.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent arg0) {
            msgSummit();
            chatField.requestFocus();
         }
      });
      panel.add(sendBtn);

      JScrollPane scrollPane_2 = new JScrollPane();
      restroom.add(scrollPane_2, BorderLayout.CENTER);

      restRoomArea = new JTextArea();
      restRoomArea.setBackground(new Color(224, 255, 255));
      restRoomArea.setEditable(false);
      scrollPane_2.setViewportView(restRoomArea);

      JPanel info = new JPanel();
      info.setBorder(new TitledBorder(UIManager
            .getBorder("TitledBorder.border"), "내 정보",
            TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
      info.setBounds(501, 235, 171, 185);
      getContentPane().add(info);
      info.setLayout(null);

      JLabel lblNewLabel = new JLabel("IP");
      lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      lblNewLabel.setBounds(-25, 54, 57, 15);
      info.add(lblNewLabel);

      JLabel lblNewLabel_2 = new JLabel("아이디");
      lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
      lblNewLabel_2.setBounds(0, 89, 57, 15);
      info.add(lblNewLabel_2);

      JLabel lblNewLabel_3 = new JLabel("닉네임");
      lblNewLabel_3.setHorizontalAlignment(SwingConstants.CENTER);
      lblNewLabel_3.setBounds(0, 129, 57, 15);
      info.add(lblNewLabel_3);

      lb_id = new JLabel("-");
      lb_id.setBounds(57, 89, 102, 15);
      info.add(lb_id);

      lb_nick = new JLabel("-");
      lb_nick.setBounds(57, 129, 102, 15);
      info.add(lb_nick);

      lb_ip = new JTextField();
      lb_ip.setBounds(57, 51, 102, 21);
      lb_ip.setEditable(false);
      info.add(lb_ip);
      lb_ip.setColumns(10);
      chatField.requestFocus();
      setVisible(true);
      chatField.requestFocus();

      
      this.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            exit();
         }
      });
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      switch (e.getActionCommand()) {
      // 메뉴1 파일 메뉴
      case "닉네임 바꾸기":
         changeNick();
         break;
      case "끝내기":
         exit();
         break;
      case "프로그램 정보":
         maker();
         System.out.println("토스트");
         break;
      }
   }

   private void changeNick() {
      String temp = JOptionPane.showInputDialog(this, "변경할 닉네임을 입력하세요.");
      if (temp != null && !temp.equals("")) {
         try {
            client.getUser().getDos()
                  .writeUTF(User.CHANGE_NICK + "/" + temp);
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }
   
   private void addFriend() {
	      String temp = JOptionPane.showInputDialog(this, "친구의 아이디를 입력하세요.");
	      if (temp != null && !temp.equals("")) {
	         try {
	            client.getUser().getDos()
	                  .writeUTF(User.NEWFRIEND + "/" + temp);
	         } catch (IOException e) {
	            e.printStackTrace();
	         }
	      }
   }
   
   private void deleteFriend() {
	   String temp = JOptionPane.showInputDialog(this, "삭제할 친구의 아이디를 입력하세요.");
	      if (temp != null && !temp.equals("")) {
	         try {
	            client.getUser().getDos()
	                  .writeUTF(User.DELFRIEND + "/" + temp);
	         } catch (IOException e) {
	            e.printStackTrace();
	         }
	      } 
   }

   private void msgSummit() {
      // 메시지전송
      String string = chatField.getText();

      if (!string.equals("")) {
         if (string.substring(0, 1).equals("/")) {
            StringTokenizer token = new StringTokenizer(string, " "); // 토큰
                                                         // 생성
            String id = token.nextToken(); // 토큰으로 분리된 스트링
            String msg = token.nextToken();

            try {
               client.getDos().writeUTF(User.WHISPER + id + "/" + msg);
               restRoomArea.append(id + "님에게 귓속말 : " + msg + "\n");
            } catch (IOException e) {
               e.printStackTrace();
            }
            chatField.setText("");
         } else {
            try {
               // 대기실에 메시지 보냄
               client.getDos().writeUTF(User.ECHO01 + "/" + string + "/true");
            } catch (IOException e) {
               e.printStackTrace();
            }
            chatField.setText("");
         }
      }
   }

   private void exit() {
      try {
         client.getUser().getDos().writeUTF(User.LOGOUT);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private void createRoom() {
      String roomname = "함께 놀아보아요";
      Room newRoom = new Room(roomname); // 방 객체 생성
      newRoom.setRoomNum(lastRoomNum);
      newRoom.setrUI(new RoomUI(client, newRoom)); // UI

      // 클라이언트가 접속한 방 목록에 추가
      client.getUser().getRoomArray().add(newRoom);

      try {
         client.getDos().writeUTF(
               User.CREATE_ROOM + "/" + newRoom.toProtocol());
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private void getIn() {
      // 선택한 방 정보
      String selectedRoom = (String) roomList.getSelectedValue();
      StringTokenizer token = new StringTokenizer(selectedRoom, "/"); // 토큰 생성
      String rNum = token.nextToken();
      String rName = token.nextToken();

      Room theRoom = new Room(rName); // 방 객체 생성
      theRoom.setRoomNum(Integer.parseInt(rNum)); // 방번호 설정
      theRoom.setrUI(new RoomUI(client, theRoom)); // UI

      // 클라이언트가 접속한 방 목록에 추가
      client.getUser().getRoomArray().add(theRoom);

      try {
         client.getDos().writeUTF(
               User.GETIN_ROOM + "/" + theRoom.getRoomNum());
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public void maker() {
      JDialog maker = new JDialog();
      Maker m = new Maker();
      maker.setTitle("프로그램 정보");
      maker.getContentPane().add(m);
      maker.setSize(400, 170);
      maker.setVisible(true);
      maker.setLocation(400, 350);
      maker.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
   }
}

class Maker extends JPanel {
   public Maker() {
      super();
      initialize();
   }

   private void initialize() {
      this.setLayout(new GridLayout(3, 1));

      JLabel j1 = new JLabel("       19-2 네트워크 과제");
      JLabel j2 = new JLabel("       이소진, 장성은");
      JLabel j3 = new JLabel("       채팅 프로그램 만들기");

      this.add(j1);
      this.add(j2);
      this.add(j3);
   }
}
