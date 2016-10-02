package pks;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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


	private class MyCustomizedPanel extends JPanel {
		
		private static final long serialVersionUID = 1L;

		private JLabel mServerLabel = new JLabel("Server");
		private JButton mLaunchButton = new JButton("Launch");
		
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
					
					String port = mServerPortField.getText();
					communicator.launchServer(port);
				}
			});
			
			mServerPortLabel.setBounds(30, 40, 100, 30);
			add(mServerPortLabel);
			mServerPortField.setBounds(130, 40, 200, 30);
			add(mServerPortField);
			
			mClientLabel.setBounds(430, 10, 100, 30);
			add(mClientLabel);
			mConnectButton.setBounds(530, 10, 200, 30);
			add(mConnectButton);
			mConnectButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					
					String ipAddress = mClientIpField.getText();
					String port = mClientPortField.getText();
					communicator.connectClient(ipAddress, port);
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
			
			mOutputPane.setBounds(20, 100, 1000, 500);
			add(mOutputPane);
		}
		
	}
}
