package multiChatRoom;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginUI extends JFrame {

	public boolean confirm = false;
	public JTextField idText;
	public JTextField pwText;
	public JButton loginBtn, signUpBtn;
	public MemberUI mem;
	public JButton ipBtn;
	private SixClient client;

	public LoginUI(SixClient sixClient) {
		setTitle("2019 네트워크-로그인");
		ServerAddress sd = new ServerAddress(this);
		this.client = sixClient;
		loginUIInitialize();
	}

	private void loginUIInitialize() {
		setBounds(100, 100, 335, 218);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(null);

		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.setBounds(12, 10, 295, 160);
		getContentPane().add(panel);
		panel.setLayout(null);

		JLabel lblNewLabel = new JLabel("아이디");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(60, 55, 57, 15);
		panel.add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("비밀번호");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setBounds(60, 86, 57, 15);
		panel.add(lblNewLabel_1);

		idText = new JTextField();
		idText.setBounds(129, 52, 116, 21);
		panel.add(idText);
		idText.setColumns(10);

		pwText = new JTextField();
		pwText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					msgSummit();
				}
			}

		});
		pwText.setBounds(129, 83, 116, 21);
		panel.add(pwText);
		pwText.setColumns(10);

		loginBtn = new JButton("로그인");
		loginBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		loginBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				msgSummit();
			}
		});
		loginBtn.setBounds(50, 111, 97, 23);
		panel.add(loginBtn);

		signUpBtn = new JButton("회원가입");
		signUpBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		signUpBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// 회원가입
				mem = new MemberUI(client);
			}
		});
		signUpBtn.setBounds(149, 111, 97, 23);
		panel.add(signUpBtn);

		JLabel lblNewLabel_2 = new JLabel("서버 아이피");
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_2.setBounds(12, 10, 78, 15);
		panel.add(lblNewLabel_2);

		ipBtn = new JButton("아이피 버튼");
		ipBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ServerAddress sd = new ServerAddress(LoginUI.this);
				setVisible(false);
			}
		});
		ipBtn.setBounds(93, 6, 120, 23);
		panel.add(ipBtn);
	}

	private void msgSummit() {
		new Thread(new Runnable() {
			public void run() {

				// 소켓생성
				if (client.serverAccess()) {
					try {
						// 로그인정보(아이디+패스워드) 전송
						client.getDos().writeUTF(
								User.LOGIN + "/" + idText.getText() + "/"
										+ pwText.getText());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}).start();
	}
}
