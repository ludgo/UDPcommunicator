package pks;


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

	public Gui() {

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1300, 700);
		
		panel = new MyCustomizedPanel();
		add(panel);
		
		setTitle("Chat");
		setVisible(true);

		
	}


	private class MyCustomizedPanel extends JPanel {
		
		private static final long serialVersionUID = 1L;

		private JLabel mGoalLabel = new JLabel("Goal address");
		private JTextField mGoalField = new JTextField(10);
		
		private JLabel mSizeLabel = new JLabel("Max. fragment size");
		private JTextField mSizeField = new JTextField(10);
		
		private JButton mSendButton = new JButton("Send file");

		private JTextArea mOutputArea = new JTextArea(30, 60);
		private JScrollPane mOutputPane = new JScrollPane(mOutputArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		public MyCustomizedPanel() {
			
			setLayout(null);
			
			mGoalLabel.setBounds(20, 20, 200, 50);
			add(mGoalLabel);
			mGoalField.setBounds(150, 20, 200, 50);
			add(mGoalField);
			
			mSizeLabel.setBounds(420, 20, 200, 50);
			add(mSizeLabel);
			mSizeField.setBounds(550, 20, 200, 50);
			add(mSizeField);
			
			mSendButton.setBounds(850, 20, 200, 50);
			add(mSendButton);
			
			// Output
			mOutputPane.setBounds(20, 100, 1000, 500);
			add(mOutputPane);
		}
		
	}
}
