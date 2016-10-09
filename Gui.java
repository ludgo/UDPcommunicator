package pks;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;



public class Gui extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private MyCustomizedPanel panel;
	private Communicator communicator;

	public Gui(Communicator c) {
		
		communicator = c;

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1300, 700);
		
		panel = new MyCustomizedPanel();
		add(panel);
		
		setTitle("UDP communication");
		setVisible(true);

		
	}


	private class MyCustomizedPanel extends JPanel implements Observer {
		
		private static final long serialVersionUID = 1L;

		private JLabel mServerLabel = new JLabel("Server");
		private JButton mLaunchButton = new JButton("Launch");
		
		private JButton mStopButton = new JButton("Stop");
		
		private JLabel mServerPortLabel = new JLabel("port");
		private JTextField mServerPortField = new JTextField(10);
		
		private JLabel mClientLabel = new JLabel("Client");
		private JButton mConnectButton = new JButton("Connect");
		
		private JButton mDisconnectButton = new JButton("Disconnect");

		private JLabel mClientIpLabel = new JLabel("IP address");
		private JTextField mClientIpField = new JTextField(10);
		
		private JLabel mClientPortLabel = new JLabel("port");
		private JTextField mClientPortField = new JTextField(10);
		
		private JLabel mSizeLabel = new JLabel("Max. fragment Bytes");
		private JTextField mSizeField = new JTextField(10);
		
		private JButton mSendFileButton = new JButton("Send file");

		private JLabel mInputLabel = new JLabel("Input");
		private JTextArea mInputArea = new JTextArea(30, 60);
		private JScrollPane mInputPane = new JScrollPane(mInputArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		private JButton mSendMessageButton = new JButton("Send message");

		private JLabel mOutputLabel = new JLabel("Ouput");
		private JTextArea mOutputArea = new JTextArea(30, 60);
		private JScrollPane mOutputPane = new JScrollPane(mOutputArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		public MyCustomizedPanel() {
			
			setLayout(null);
			
			mServerLabel.setBounds(30, 10, 100, 30);
			add(mServerLabel);
			mLaunchButton.setBounds(130, 10, 200, 30);
			add(mLaunchButton);
			mLaunchButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					
					int port = getPort(mServerPortField);
					if (port == -1) {
						mOutputArea.append("Not a valid port!\n");
					} else if (!communicator.launchServer(port, panel)) {
						mOutputArea.append("Server already running!\n");
					}
				}
			});
			
			mStopButton.setBounds(130, 40, 200, 30);
			add(mStopButton);
			mStopButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
										
					if (!communicator.stopServer()) {
						mOutputArea.append("Server not running!\n");
					}
				}
			});
			
			mServerPortLabel.setBounds(30, 70, 100, 30);
			add(mServerPortLabel);
			mServerPortField.setBounds(130, 70, 200, 30);
			add(mServerPortField);
			
			mClientLabel.setBounds(430, 10, 100, 30);
			add(mClientLabel);
			mConnectButton.setBounds(530, 10, 200, 30);
			add(mConnectButton);
			mConnectButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					
					String ipAddress = getIpAddress(mClientIpField);
					int port = getPort(mClientPortField);
					if (ipAddress == null) {
						mOutputArea.append("Not a valid IP address!\n");
					} else if (port == -1) {
						mOutputArea.append("Not a valid port!\n");
					} else if (!communicator.connectClient(ipAddress, port, panel)) {
						mOutputArea.append("Client already created!\n");
					}
				}
			});
			
			mDisconnectButton.setBounds(530, 40, 200, 30);
			add(mDisconnectButton);
			mDisconnectButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					
					if (!communicator.disconnectClient()) {
						mOutputArea.append("Client not created!\n");
					}
				}
			});
			
			mClientIpLabel.setBounds(430, 70, 100, 30);
			add(mClientIpLabel);
			mClientIpField.setBounds(530, 70, 200, 30);
			add(mClientIpField);
			
			mClientPortLabel.setBounds(430, 100, 100, 30);
			add(mClientPortLabel);
			mClientPortField.setBounds(530, 100, 200, 30);
			add(mClientPortField);
			
			mSizeLabel.setBounds(830, 10, 200, 30);
			add(mSizeLabel);
			mSizeField.setBounds(1030, 10, 100, 30);
			add(mSizeField);
			
			mSendFileButton.setBounds(830, 40, 100, 30);
			add(mSendFileButton);
			mSendFileButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {

					communicator.sendFile();
				}
			});
			
			mInputLabel.setBounds(20, 140, 50, 100);
			add(mInputLabel);
			mInputPane.setBounds(100, 140, 1000, 100);
			add(mInputPane);
			mSendMessageButton.setBounds(100, 250, 200, 30);
			add(mSendMessageButton);
			mSendMessageButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					
					String message = getMessage(mInputArea);
					if (message == null) {
						mOutputArea.append("Message empty!\n");
					} else if (!communicator.sendMessage(message)) {
						//mOutputArea.append("Client already created!\n");
					}
				}
			});
			
						
			mOutputLabel.setBounds(20, 300, 50, 100);
			add(mOutputLabel);
			mOutputPane.setBounds(100, 300, 1000, 300);
			add(mOutputPane);
		}

		@Override
		public void update(Observable arg0, Object arg1) {
			if (arg1 instanceof String) {
				mOutputArea.append((String) arg1);
			}
		}
		
		private String getIpAddress(JTextField field) {
			String ipAddress;
			try {
				ipAddress = field.getText();
				if (ipAddress.equals("localhost")) {
					return "localhost";
				}
				String[] parts = ipAddress.split(Pattern.quote("."));
				if (parts.length != 4) return null;
				for (int i=0; i<4; i++) {
					int part = Integer.parseInt(parts[i]);
					if (part < 0 || part > 255) return null;
				}
			} catch (NullPointerException e) {
				// The field is null
				return null;
			} catch (NumberFormatException e) {
				// String in the field is not integer type
				return null;
			}
			return ipAddress;
		}
		
		private int getPort(JTextField field) {
			try {
				int number = Integer.parseInt(field.getText());
				if (number > 0 && number < 65535) {
					return number;
				}
				// Not positive
			} catch (NullPointerException e) {
				// The field is null
			} catch (NumberFormatException e) {
				// String in the field is not integer type
			}
			return -1;
		}
		
		private String getMessage(JTextArea field) {
			String message;
			try {
				message = field.getText();
				if (message != null && message.length() > 0) return message;
			} catch (NullPointerException e) {
				// The field is null
			}
			return null;
		}

		
	}
}
