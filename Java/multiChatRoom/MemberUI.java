package multiChatRoom;

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

public class MemberUI extends JFrame {

	public boolean confirm = false;
	public JTextField idText;
	public JTextField pwText;
	public JButton signUpBtn, cancelBtn;
	private SixClient client;

	public MemberUI(SixClient client) {
		setTitle("\uD68C\uC6D0\uAC00\uC785");

		this.client = client;
		initialize();
	}

	private void initialize() {
		setBounds(100, 100, 335, 197);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(null);

		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.setBounds(12, 10, 295, 138);
		getContentPane().add(panel);
		panel.setLayout(null);

		JLabel lblNewLabel = new JLabel("\uC544\uC774\uB514");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(60, 38, 57, 15);
		panel.add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("\uBE44\uBC00\uBC88\uD638");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setBounds(60, 63, 57, 15);
		panel.add(lblNewLabel_1);

		idText = new JTextField();
		idText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					setVisible(false);
				}
			}
		});
		idText.setBounds(129, 35, 116, 21);
		panel.add(idText);
		idText.setColumns(10);

		pwText = new JTextField();
		pwText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					msgSummit();
				} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					setVisible(false);
				}
			}
		});
		pwText.setBounds(129, 60, 116, 21);
		panel.add(pwText);
		pwText.setColumns(10);

		signUpBtn = new JButton("\uAC00\uC785");
		signUpBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						msgSummit();
					}
				}).start();
				dispose();
			}
		});
		signUpBtn.setBounds(50, 88, 97, 23);
		panel.add(signUpBtn);

		cancelBtn = new JButton("\uCDE8\uC18C");
		cancelBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				setVisible(false);
			}
		});
		cancelBtn.setBounds(148, 88, 97, 23);
		panel.add(cancelBtn);
		setVisible(true);
	}

	private void msgSummit() {// 소켓생성
		if (client.serverAccess()) {
			try {
				// 회원가입정보(아이디+패스워드) 전송
				client.getDos().writeUTF(
						User.MEMBERSHIP + "/" + idText.getText() + "/"
								+ pwText.getText());
				setVisible(false);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
