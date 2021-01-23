package multiChatRoom;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ServerAddress extends JFrame {

	public JButton confirmBtn;
	public JTextField ipText;
	private LoginUI loginUI;

	public ServerAddress(LoginUI loginUI) {
		this.loginUI = loginUI;
		initialize();
	}

	private void initialize() {
		setTitle("\uC11C\uBC84 \uC544\uC774\uD53C \uC8FC\uC18C \uC785\uB825");
		setBounds(100, 100, 306, 95);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().setLayout(null);

		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.setBounds(12, 10, 266, 37);
		getContentPane().add(panel);
		panel.setLayout(new BorderLayout(0, 0));

		ipText = new JTextField();
		ipText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					loginUI.ipBtn.setText(ipText.getText());
					loginUI.setVisible(true);
					dispose();
					loginUI.idText.requestFocus();
				}
			}
		});
		//ipText.setText("10.101.44.54");
		//ipText.setText("172.30.1.29");
		//ipText.setText("172.30.1.3");
		ipText.setText("192.168.0.12");
		//ipText.setText("192.168.42.198");
		panel.add(ipText, BorderLayout.CENTER);
		ipText.setColumns(10);
		confirmBtn = new JButton("\uD655\uC778");
		confirmBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				loginUI.ipBtn.setText(ipText.getText());
				loginUI.setVisible(true);
				dispose();
				loginUI.idText.requestFocus();
			}
		});
		panel.add(confirmBtn, BorderLayout.EAST);
		setVisible(true);
	}

}
