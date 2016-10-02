package pks;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

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
		
		private JLabel mClientIpLabel = new JLabel("IP address");
		private JTextField mClientIpField = new JTextField(10);
		
		private JLabel mClientPortLabel = new JLabel("port");
		private JTextField mClientPortField = new JTextField(10);
		
		private JLabel mSizeLabel = new JLabel("Max. fragment Bytes");
		private JTextField mSizeField = new JTextField(10);
		
		private JButton mSendButton = new JButton("Send file");

		private JLabel mInputLabel = new JLabel("Input");
		private JTextArea mInputArea = new JTextArea(30, 60);
		private JScrollPane mInputPane = new JScrollPane(mInputArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

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
					
					communicator.stopServer();
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
					
					String ipAddress = mClientIpField.getText();
					if (ipAddress == null) return;
					String port = mClientPortField.getText();
					if (port == null) return;
					if (!communicator.connectClient(ipAddress, port)) {
						mOutputArea.append("Server already connected!\n");
					}
				}
			});
			
			mClientIpLabel.setBounds(430, 40, 100, 30);
			add(mClientIpLabel);
			mClientIpField.setBounds(530, 40, 200, 30);
			add(mClientIpField);
			
			mClientPortLabel.setBounds(430, 70, 100, 30);
			add(mClientPortLabel);
			mClientPortField.setBounds(530, 70, 200, 30);
			add(mClientPortField);
			
			mSizeLabel.setBounds(830, 10, 200, 30);
			add(mSizeLabel);
			mSizeField.setBounds(1030, 10, 100, 30);
			add(mSizeField);
			
			mSendButton.setBounds(830, 40, 100, 30);
			add(mSendButton);
			mSendButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {

					communicator.pickFile();
				}
			});
			
			mInputLabel.setBounds(20, 140, 50, 100);
			add(mInputLabel);
			mInputPane.setBounds(100, 140, 1000, 100);
			add(mInputPane);
			
			mOutputLabel.setBounds(20, 250, 50, 100);
			add(mOutputLabel);
			mOutputPane.setBounds(100, 250, 1000, 300);
			add(mOutputPane);
		}

		@Override
		public void update(Observable arg0, Object arg1) {
			if (arg1 instanceof String) {
				mOutputArea.append((String) arg1);
			}
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
		
	}
}
