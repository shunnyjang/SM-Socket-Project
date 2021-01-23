package multiChatRoom;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JTextArea;

public class ServerThread implements Runnable {

	private ArrayList<User> userArray; // ������ ������ ����ڵ�
	private ArrayList<Room> roomArray; // ������ ������� ä�ù��
	private List friendArray = new ArrayList();
	private User user; // ���� ������� �����(������ ������) �����
	private JTextArea jta;
	private boolean onLine = true;

	private DataOutputStream thisUser;

	ServerThread(JTextArea jta, User person, ArrayList<User> userArray,
			ArrayList<Room> roomArray) {
		this.roomArray = roomArray;
		this.userArray = userArray;
		this.userArray.add(person); // �迭�� ����� �߰�
		this.user = person;
		this.jta = jta;
		this.thisUser = person.getDos();
	}

	@Override
	public void run() {
		DataInputStream dis = user.getDis(); // �Է� ��Ʈ�� ���

		while (onLine) {
			try {
				String receivedMsg = dis.readUTF(); // �޽��� �ޱ�(���)
				dataParsing(receivedMsg); // �޽��� �ؼ�
				jta.append("���� : �޽��� ���� -" + receivedMsg + "\n");
				jta.setCaretPosition(jta.getText().length());
			} catch (IOException e) {
				try {
					user.getDis().close();
				} catch (IOException e1) {
					e1.printStackTrace();
				} finally {
					jta.append("���� : ����������-�б� ����\n");
					break;
				}
			}
		}
	}

	// �����͸� ����
	public synchronized void dataParsing(String data) {
		StringTokenizer token = new StringTokenizer(data, "/"); // ��ū ����
		String protocol = token.nextToken(); // ��ū���� �и��� ��Ʈ���� ���ڷ�
		String id, pw, rNum, nick, rName, rUser, msg, friend, flag;
		System.out.println("������ ���� ������ : " + data);

		switch (protocol) {
		case User.LOGIN: // �α���
			// ����ڰ� �Է���(������) ���̵�� �н�����
			id = token.nextToken();
			pw = token.nextToken();
			login(id, pw);
			break;
		case User.LOGOUT: // �α׾ƿ�
			logout();
			break;
		case User.MEMBERSHIP: // ȸ������
			id = token.nextToken();
			pw = token.nextToken();
			member(id, pw);
			break;
		case User.INVITE: // �ʴ��ϱ�
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
			userList(thisUser);
			break;
		case User.UPDATE_ROOM_USERLIST: // ä�ù� ����� ���
			// ���ȣ�б�
			rNum = token.nextToken();
			flag = "";
			while (token.hasMoreElements()) {
				flag = token.nextToken();
			}
			if (flag.equals("mobile")) {
				
			} else {
				userList(rNum, thisUser);
			}
			break;
		case User.UPDATE_SELECTEDROOM_USERLIST: // ���ǿ��� ������ ä�ù��� ����� ���
			// ���ȣ�б�
			rNum = token.nextToken();
			selectedRoomUserList(rNum, thisUser);
			break;
		case User.UPDATE_FRIENDLIST:
			friendList(thisUser);
			break;
		case User.UPDATE_ROOMLIST: // �� ���
			if (token.hasMoreTokens()) {
				flag = token.nextToken();
			} else {
				flag = "true";
			}
			
			if (token.equals("true")) {
				roomList(thisUser);
			} else {
				// �ȵ���̵� handler ����
			}
			break;
		case User.CHANGE_NICK: // �г��� ����(����)
			nick = token.nextToken();
			changeNick(nick);
			break;
		case User.CREATE_ROOM: // �游���
			rNum = token.nextToken();
			rName = token.nextToken();
			createRoom(rNum, rName);
			break;
		case User.GETIN_ROOM:
			rNum = token.nextToken();
			getInRoom(rNum);
			break;
		case User.GETOUT_ROOM:
			rNum = token.nextToken();
			getOutRoom(rNum);
			break;
		case User.MOBILE_IN_ROOM:
			rUser = token.nextToken();
			rNum = token.nextToken();
			
			if (token.hasMoreElements())	// flag = "mobile"
				flag = token.nextToken();
			else 
				flag = null;
			
			if (flag.equals("mobile"))
				getInRoom(rUser, rNum);
			break;
		case User.ECHO01: // ���� ����
			msg = token.nextToken();
			flag = token.nextToken();
			echoMsg(User.ECHO01 + "/" + user.toString() + msg + "/" + flag);
			break;
		case User.ECHO02: // ä�ù� ����
			rNum = token.nextToken();
			msg = token.nextToken();
			flag = "";
			if (token.hasMoreElements()) {
				flag = token.nextToken();
			}
			
			if (flag.equals("mobile")) {
				//
			} else {
				echoMsg(rNum, msg);
			}
			break;
		case User.WHISPER: // �ӼӸ�
			id = token.nextToken();
			msg = token.nextToken();
			whisper(id, msg);
			break;
		case User.NEWFRIEND:  // ģ���߰�
			friend = token.nextToken();
			addFriend(friend);
			break; 
		case User.DELFRIEND:
			friend = token.nextToken();
			deleteFriend(friend);
			break;
		}
	}

	public void alarm() {

	}
	
	private void deleteMobileRoom(String rNum) {
		try {
			for (int i = 0; i < userArray.size(); i++) {
				userArray
					.get(i)
					.getDos()
					.writeUTF(User.MOBILE_OUT_ROOM + "/" + rNum + "/server");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// �ڹ� ä�ù� ����
	private void getOutRoom(String rNum) {
		for (int i = 0; i < roomArray.size(); i++) {
			if (Integer.parseInt(rNum) == roomArray.get(i).getRoomNum()) {
				// �濡�� ������
				// ä�ù��� ��������Ʈ���� ����� ����
				for (int j = 0; j < roomArray.get(i).getUserArray().size(); j++) {
					if (user.getId().equals(
							roomArray.get(i).getUserArray().get(j).getId())) {
						roomArray.get(i).getUserArray().remove(j);
					}
				}

				// ������� �渮��Ʈ���� ���� ����
				for (int j = 0; j < user.getRoomArray().size(); j++) {
					if (Integer.parseInt(rNum) == user.getRoomArray().get(j)
							.getRoomNum()) {
						user.getRoomArray().remove(j);
					}
				}
				echoMsg(roomArray.get(i), user.toString() + "���� �����ϼ̽��ϴ�.");
				userList(rNum);

				if (roomArray.get(i).getUserArray().size() <= 0) {
					roomArray.remove(i);
					deleteMobileRoom(rNum);
					roomList();
				}
			}
		}
	}
	
	// �ڹ� ä�ù� ����
	private void getInRoom(String rNum) {
		for (int i = 0; i < roomArray.size(); i++) {
			if (Integer.parseInt(rNum) == roomArray.get(i).getRoomNum()) {
				// �� ��ü�� �ִ� ���, �濡 ������߰�
				roomArray.get(i).getUserArray().add(user);
				// ����� ��ü�� �� �߰�
				user.getRoomArray().add(roomArray.get(i));
				echoMsg(roomArray.get(i), user.toString() + "���� �����ϼ̽��ϴ�.");
				userList(rNum);
			}
		}
	}
	
	// ����� �̿��� ä�ù� ����
	private void getInRoom(String rUser, String rNum) {
		User mobileUser = new User();
		for (int i = 0; i < userArray.size(); i++) {
			if (rUser.equals(userArray.get(i).getId())) {
				mobileUser = userArray.get(i);
			}
		}
		
		for (int i = 0; i < roomArray.size(); i++) {
			if (Integer.parseInt(rNum) == roomArray.get(i).getRoomNum()) {
				// �� ��ü�� ����� �߰�
				roomArray.get(i).getUserArray().add(mobileUser);
				// ����� ��ü�� �� �߰�
				mobileUser.getRoomArray().add(roomArray.get(i));
				echoMsg(roomArray.get(i), mobileUser.toString() + "���� �����ϼ̽��ϴ�.");
				userList(rNum);
			}
		}
	}

	private void createRoom(String rNum, String rName) {
		Room rm = new Room(rName); // ������ �������� ä�ù� ����
		rm.setMaker(user); // ���� ����
		rm.setRoomNum(Integer.parseInt(rNum)); // ���ȣ ����

		rm.getUserArray().add(user); // ä�ù濡 ����(����) �߰�
		roomArray.add(rm); // �븮��Ʈ�� ���� ä�ù� �߰�
		user.getRoomArray().add(rm); // ����� ��ü�� ������ ä�ù��� ����

		echoMsg(User.ECHO01 + "/" + user.toString() + "���� " + rm.getRoomNum()
				+ "�� ä�ù��� �����ϼ̽��ϴ�.");
		echoMsg(rm, user.toString() + "���� �����ϼ̽��ϴ�.");
		roomList();
		userList(rNum, thisUser);
		jta.append("���� : " + userArray.toString() + "�� ä�ù����\n");
	}

	private void whisper(String id, String msg) {
		for (int i = 0; i < userArray.size(); i++) {
			if (id.equals(userArray.get(i).getId())) {
				// �ӼӸ� ��븦 ã����
				try {
					userArray
							.get(i)
							.getDos()
							.writeUTF(
									User.WHISPER + "/" + user.toProtocol()
											+ "/" + msg);
					jta.append("���� : �ӼӸ����� : " + user.toString() + "�� "
							+ userArray.get(i).toString() + "����" + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// ���� ����
	private void echoMsg(String msg) {
		for (int i = 0; i < userArray.size(); i++) {
			try {
				userArray.get(i).getDos().writeUTF(msg);
				jta.append(user.toString() + " - " + msg + "\n");
			} catch (IOException e) {
				e.printStackTrace();
				jta.append("���� : ���� ����\n");
			}
		}
	}

	// �� ���� (�� ��ȣ�� �ƴ� ���)
	private void echoMsg(String rNum, String msg) {
		for (int i = 0; i < roomArray.size(); i++) {
			if (Integer.parseInt(rNum) == roomArray.get(i).getRoomNum()) {
				echoMsg(roomArray.get(i), msg);
			}
		}
	}

	// �� ���� (�水ü�� �ִ� ���)
	private void echoMsg(Room room, String msg) {
		for (int i = 0; i < room.getUserArray().size(); i++) {
			try {
				// �濡 ������ �����鿡�� ���� �޽��� ����
				room.getUserArray()
						.get(i)
						.getDos()
						.writeUTF(
								User.ECHO02 + "/" + room.getRoomNum() + "/"
										+ msg);
				jta.append("���� : �޽������� : " + msg + "\n");
			} catch (IOException e) {
				e.printStackTrace();
				jta.append("���� : ���� ����\n");
			}
		}
	}

	// ���Ǵг��� ����
	private void changeNick(String nick) {
		File file = new File("C:\\NetworkHW\\chatText\\" + user.getId() + ".txt");
		FileWriter f;
		try {
			f = new FileWriter(file);
			// ���Ͽ� ȸ���������� (���̵�+�н�����+�г���)
			f.write(user.getId() + "/" + user.getPw() + "/" + nick);
			f.close();
			thisUser.writeUTF(User.MEMBERSHIP + "/OK");
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// �г����� �ߺ������
		jta.append("���� : �г��Ӻ��� : " + user.getId() + "���� �г��� +"
				+ user.getNickName() + "�� " + nick + "�� ����");
		user.setNickName(nick);

		try {
			user.getDos().writeUTF(User.CHANGE_NICK + "/" + nick);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < userArray.size(); i++) {
			userList(userArray.get(i).getDos());
		}
		// �� ����� ������ ��� ���� ����ڸ���Ʈ�� ������Ʈ
		for (int i = 0; i < user.getRoomArray().size(); i++) {
			userList(String.valueOf(user.getRoomArray().get(i).getRoomNum()));
		}
	}
	
	private void addFriend(String nick) {
		
		if (nick.equals(user.getId())) {
			try {
				thisUser.writeUTF(User.DELFRIEND + "/fail");
				jta.append("ģ�� �߰� ����");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			File file = new File("C:\\NetworkHW\\chatText\\" + user.getId() + ".txt");
			File file2 = new File("C:\\NetworkHW\\chatText\\" + nick + ".txt");
			FileWriter f1, f2;
			
			try {
				if (file2.isFile()) {
					
					boolean flag = true;
					
					FileReader reader1 = new FileReader("C:\\NetworkHW\\chatText\\" + user.getId() + ".txt");
					BufferedReader reader = new BufferedReader(reader1);
					String str = null;
					str = reader.readLine();
					StringTokenizer token = new StringTokenizer(str, "/");
					while (token.hasMoreTokens()) {
						String temp = token.nextToken();
						if (nick.equals(temp)) {
							flag = false;
							break;
						}
					}
					reader1.close();
					
					if (flag) {
						f1 = new FileWriter(file, true);
						f2 = new FileWriter(file2, true);
						// ���Ͽ� ȸ���������� (���̵�+�н�����+�г���)
						f1.write("/"+ nick);
						f2.write("/"+ user.getId());
						f1.close();
						f2.close();
						thisUser.writeUTF(User.NEWFRIEND + "/OK");
					} else {
						thisUser.writeUTF(User.NEWFRIEND + "/already");
						jta.append("���� : �̹� ģ���� ȸ��");
					}
				} else {
					thisUser.writeUTF(User.NEWFRIEND + "/fail");
					jta.append("���� : ���� ȸ��");
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		friendList(user.getDos());
	}
	
	private void deleteFriend(String nick) {
		
		if (nick.equals(user.getId())) {
			try {
				thisUser.writeUTF(User.DELFRIEND + "/fail");
				jta.append("ģ�� ���� ����");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			File file = new File("C:\\NetworkHW\\chatText\\" + user.getId() + ".txt");
			File file2 = new File("C:\\NetworkHW\\chatText\\" + nick + ".txt");
			FileWriter f1, f2;
			
			if (file2.isFile()) {
				try {
					FileReader freader1 = new FileReader(file);
					FileReader freader2 = new FileReader(file2);
					BufferedReader breader1 = new BufferedReader(freader1);
					BufferedReader breader2 = new BufferedReader(freader2);
					
					String str1 = null, str2 = null;
					str1 = breader1.readLine(); str2 = breader2.readLine();
					StringTokenizer token1 = new StringTokenizer(str1, "/");
					StringTokenizer token2 = new StringTokenizer(str2, "/");
					
					boolean flag = false;
					StringBuffer sbUser = new StringBuffer();
					StringBuffer sbNick = new StringBuffer();
					
					while (token1.hasMoreTokens() && token2.hasMoreTokens()) {
						String tempUser = token1.nextToken();
						String tempNick = token2.nextToken();
						
						if (nick.equals(tempUser))
							flag = true;
						else {
							sbUser.append(tempUser + "/");
							sbNick.append(tempNick + "/");
						}
					}
					
					if (flag) {
						f1 = new FileWriter("C:\\NetworkHW\\chatText\\" + user.getId() + ".txt");
						f2 = new FileWriter("C:\\NetworkHW\\chatText\\" + nick + ".txt");
						f1.write(sbUser.toString());
						f2.write(sbNick.toString());
						thisUser.writeUTF(User.DELFRIEND + "/OK");
						jta.append(user.getId() + "��" + nick + "ģ�� ���� ����");
						f1.close(); f2.close();
					} else {
						thisUser.writeUTF(User.DELFRIEND + "/fail");
						jta.append("ģ�� ���� ����");
					}
					
					breader1.close(); breader2.close();
					freader1.close(); freader2.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} else {
				try {
					thisUser.writeUTF(User.DELFRIEND + "/fail");
					jta.append("ģ�� ���� ����");
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
		}
		
		friendList(user.getDos());
	}

	private void invite(String id, String rNum) {
		for (int i = 0; i < userArray.size(); i++) {
			// �ʴ��һ���� ã�Ƽ� �ʴ�޽��� ����
			if (id.equals(userArray.get(i).getId())) {
				try {
					// �ʴ��� ����� ���̵�� ���ȣ�� ����
					userArray
							.get(i)
							.getDos()
							.writeUTF(
									User.INVITE + "/" + user.getId() + "/"
											+ rNum);
				} catch (IOException e) {
					e.printStackTrace();
					jta.append("���� : �ʴ����-" + userArray.toString() + "\n");
				}
			}
		}
	}

	private void member(String id, String pw) {
		User newUser = new User();
		newUser.setId(id);
		newUser.setPw(pw);

		try {
			File file = new File("C:\\NetworkHW\\chatText\\" + id + ".txt");
			if (!file.isFile()) {
				FileWriter f = new FileWriter(file);

				// ���Ͽ� ȸ���������� (���̵�+�н�����+�г���)
				f.write(newUser.toStringforLogin());
				f.close();
				thisUser.writeUTF(User.MEMBERSHIP + "/OK");
				jta.append("���� : ȸ������ ���ϻ���\n");
			} else {
				// ������ �����ϴ� ���
				thisUser.writeUTF(User.MEMBERSHIP + "/fail");
				jta.append("���� : �̹� ���Ե� ȸ��\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				thisUser.writeUTF(User.MEMBERSHIP + "/fail");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			jta.append("���� : ȸ������ ���ϻ���\n");
		}
	}

	public boolean login(String id, String pw) {

		FileReader reader = null;
		int inputValue = 0;
		StringBuffer str = new StringBuffer();
		try {
			// ���� ����
			reader = new FileReader("C:\\NetworkHW\\chatText\\" + id + ".txt");
			while ((inputValue = reader.read()) != -1) {
				// ���� ����
				str.append((char) inputValue);
			}
			jta.append("���� : ���� �б� : C:\\NetworkHW\\chatText\\" + id + ".txt\n");
			reader.close();
			StringTokenizer token = new StringTokenizer(str.toString(), "/"); // ��ū
			// ����

			try {
				if (id.equals(token.nextToken())) {
					if (pw.equals(token.nextToken())) {
						for (int i = 0; i < userArray.size(); i++) {
							if (id.equals(userArray.get(i).getId())) {
								try {
									System.out.println("������");
									thisUser.writeUTF(User.LOGIN
											+ "/fail/�̹� ���� ���Դϴ�.");
									return false;
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}

						// �α��� OK
						user.setId(id);
						user.setPw(pw);
						user.setNickName(token.nextToken());
						thisUser.writeUTF(User.LOGIN + "/OK/"
								+ user.getNickName());
						this.user.setOnline(true);

						// ���ǿ� ����
						echoMsg(User.ECHO01 + "/" + user.toString()
								+ "���� �����ϼ̽��ϴ�." + "/false");
						jta.append(id + " : ���� �����ϼ̽��ϴ�.\n");
						
						roomList(thisUser);
						for (int i = 0; i < userArray.size(); i++) {
							userList(userArray.get(i).getDos());
						}
						friendList(thisUser);
						
						return true;
					} else {
						thisUser.writeUTF(User.LOGIN + "/fail/�н����尡 ��ġ���� �ʽ��ϴ�.");
						jta.append("���� : �α���-�н����尡 ��ġ���� �ʽ��ϴ�. : " + pw + "\n");
						return false;
					}
				} else {
					thisUser.writeUTF(User.LOGIN + "/fail/���̵� �������� �ʽ��ϴ�.");
					jta.append("���� : �α���-���̵� ��ġ���� �ʽ��ϴ�. : " + id + "\n");
					return false;
				}
			} catch (IOException e) {
				e.printStackTrace();
				thisUser.writeUTF(User.LOGIN + "/fail/�α��ν���");
				jta.append("���� : �α��� ����" + pw + "\n");
				return false;
			}
		} catch (Exception e) {
			try {
				thisUser.writeUTF(User.LOGIN + "/fail/���̵� �������� �ʽ��ϴ�.");
				return false;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			jta.append("���� : ���� �б�\n");
			return false;
		}

	}

	private void logout() {
		System.out.println("�α׾ƿ�");

		// ������������ �ٲ�
		user.setOnline(false);
		// ����ڹ迭���� ����
		for (int i = 0; i < userArray.size(); i++) {
			if (user.getId().equals(userArray.get(i).getId())) {
				System.out.println(userArray.get(i).getId() + "������.");
				userArray.remove(i);
			}
		}
		// room Ŭ������ ��������� ����ڹ迭���� ����
		for (int i = 0; i < roomArray.size(); i++) {
			for (int j = 0; j < roomArray.get(i).getUserArray().size(); j++) {
				if (user.getId().equals(
						roomArray.get(i).getUserArray().get(j).getId())) {
					roomArray.get(i).getUserArray().remove(j);
				}
			}
		}
		echoMsg(User.ECHO01 + "/" + user.toString() + "���� �����ϼ̽��ϴ�.");

		for (int i = 0; i < userArray.size(); i++) {
			userList(userArray.get(i).getDos());
		}

		jta.append(user.getId() + " : ���� �����ϼ̽��ϴ�.\n");

		try {
			user.getDos().writeUTF(User.LOGOUT);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			user.getDis().close();
			user.getDos().close();
			user = null;
			jta.append("���� : ��Ʈ�� �ݱ�\n");
		} catch (IOException e) {
			e.printStackTrace();
			jta.append("���� : ��Ʈ�� �ݱ�\n");
		}
	}

	// ����� ����Ʈ (������ ä�ù�)
	public void selectedRoomUserList(String rNum, DataOutputStream target) {
		String ul = "";

		for (int i = 0; i < roomArray.size(); i++) {
			if (Integer.parseInt(rNum) == roomArray.get(i).getRoomNum()) {
				for (int j = 0; j < roomArray.get(i).getUserArray().size(); j++) {
					// ä�ù濡 ���ӵǾ� �ִ� �������� ���̵�+�г���
					ul += "/"
							+ roomArray.get(i).getUserArray().get(j)
									.toProtocol();
				}
			}
		}
		try {
			// ������ ����
			target.writeUTF(User.UPDATE_SELECTEDROOM_USERLIST + ul);
			jta.append("���� : ���(�����)-" + ul + "\n");
		} catch (IOException e) {
			jta.append("���� : ���(�����) ���� ����\n");
		}
	}

	// ����� ����Ʈ (����)
	public String userList(DataOutputStream target) {
		String ul = "";

		for (int i = 0; i < userArray.size(); i++) {
			// ���ӵǾ� �ִ� �������� ���̵�+�г���
			ul += "/" + userArray.get(i).toProtocol();
		}

		try {
			// ������ ����
			target.writeUTF(User.UPDATE_USERLIST + ul);
			jta.append("���� : ���(�����)-" + ul + "\n");
		} catch (IOException e) {
			jta.append("���� : ���(�����) ���� ����\n");
		}
		return ul;
	}

	// ����� ����Ʈ (ä�ù� ����)
	public void userList(String rNum, DataOutputStream target) {
		String ul = "/" + rNum;

		for (int i = 0; i < roomArray.size(); i++) {
			if (Integer.parseInt(rNum) == roomArray.get(i).getRoomNum()) {
				for (int j = 0; j < roomArray.get(i).getUserArray().size(); j++) {
					// ä�ù濡 ���ӵǾ� �ִ� �������� ���̵�+�г���
					ul += "/"
							+ roomArray.get(i).getUserArray().get(j)
									.toProtocol();
				}
			}
		}
		try {
			// ������ ����
			target.writeUTF(User.UPDATE_ROOM_USERLIST + ul);
			jta.append("���� : ���(�����)-" + ul + "\n");
		} catch (IOException e) {
			jta.append("���� : ���(�����) ���� ����\n");
		}
	}

	// ����� ����Ʈ (ä�ù� ���� ��� ����ڵ鿡�� ����)
	public void userList(String rNum) {
		String ul = "/" + rNum;
		Room temp = null;
		for (int i = 0; i < roomArray.size(); i++) {
			if (Integer.parseInt(rNum) == roomArray.get(i).getRoomNum()) {
				temp = roomArray.get(i);
				for (int j = 0; j < roomArray.get(i).getUserArray().size(); j++) {
					// ä�ù濡 ���ӵǾ� �ִ� �������� ���̵�+�г���
					ul += "/"
							+ roomArray.get(i).getUserArray().get(j)
									.toProtocol();
				}
			}
		}
		for (int i = 0; i < temp.getUserArray().size(); i++) {
			try {
				// ������ ����
				temp.getUserArray().get(i).getDos()
						.writeUTF(User.UPDATE_ROOM_USERLIST + ul);
				jta.append("���� : ���(�����)-" + ul + "\n");
			} catch (IOException e) {
				jta.append("���� : ���(�����) ���� ����\n");
			}
		}
	}

	// ä�� �渮��Ʈ
	public void roomList(DataOutputStream target) {
		String rl = "";

		for (int i = 0; i < roomArray.size(); i++) {
			// ������� ä�ù���� ����
			rl += "/" + roomArray.get(i).toProtocol();
		}

		//jta.append("test/target\n");

		try {
			// ������ ����
			target.writeUTF(User.UPDATE_ROOMLIST + rl);
			jta.append("���� : ���(��)-" + rl + "\n");
		} catch (IOException e) {
			jta.append("���� : ���(��) ���� ����\n");
		}
	}

	// ä�� �渮��Ʈ
	public void roomList() {
		String rl = "";

		for (int i = 0; i < roomArray.size(); i++) {
			// ������� ä�ù���� ����
			rl += "/" + roomArray.get(i).toProtocol();
		}

		//jta.append("test\n");

		for (int i = 0; i < userArray.size(); i++) {

			try {
				// ������ ����
				userArray.get(i).getDos().writeUTF(User.UPDATE_ROOMLIST + rl);
				jta.append("���� : ���(��)-" + rl + "\n");
			} catch (IOException e) {
				jta.append("���� : ���(��) ���� ����\n");
			}
		}
	}
	
	// ģ������Ʈ (ģ�� ���)
	public String friendList(DataOutputStream target) {
		
		String fl = "";
		
		File file = new File("C:\\NetworkHW\\chatText\\" + user.getId() + ".txt");
		String str = null;
		
		fl += "/" + user.getId() + "/";
		try {
			FileReader freader1 = new FileReader(file);
			BufferedReader breader1 = new BufferedReader(freader1);
			str = breader1.readLine();
			StringTokenizer token = new StringTokenizer(str, "/");
			str = token.nextToken(); str = token.nextToken();
			while (token.hasMoreElements()) {
				str = token.nextToken();
				fl += str + "/";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		try {
			target.writeUTF(User.UPDATE_FRIENDLIST + fl);
			jta.append("���� : ���(ģ��) target -" + fl + "\n");
		} catch (IOException e) {
			jta.append("���� : ���(ģ��) ���� ����\n");
		}
		return fl;
	}
	
}

