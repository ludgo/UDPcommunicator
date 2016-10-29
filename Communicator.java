package pks;

import java.io.File;
import java.util.Observer;

import javax.swing.JPanel;

/**
 * A UDP communicator controller
 */
public class Communicator extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private Server mServer = null;
	private Client mClient = null;
	
	// Run program
	public static void main(String[] args) {
		Communicator c = new Communicator();
		new Gui(c);		
	}
	
	/**
	 * Launch server if not already and port not used by client
	 */
	public boolean launchServer(int port, Observer observer) {
		
		if (!isServerLaunched() &&
				(!isClientConnected() || mClient.getUsedPort() != port)) {
			
			mServer = new Server(port, observer, this);
			mServer.start();
			return true;
		}
		return false;
	}
	
	public boolean stopServer() {
		
		if (isServerLaunched()) {
			mServer.halt();
			mServer = null;
			return true;
		}
		return false;
	}
	
	private boolean isServerLaunched() {
		return mServer != null;
	}
	
	/**
	 * Launch client if not already and port not used by server
	 */
	public boolean connectClient(String serverIpAddress, int serverPort, int clientPort, Observer observer) {
		
		if (!isClientConnected() &&
				(!isServerLaunched() || mServer.getUsedPort() != clientPort)) {
			
			mClient = new Client(serverIpAddress, serverPort, clientPort, observer);
			mClient.start();
			return true;
		}
		return false;
	}
	
	public boolean disconnectClient() {
		
		if (isClientConnected()) {
			mClient.halt();
			mClient = null;
			return true;
		}
		return false;
	}
	
	private boolean isClientConnected() {
		return mClient != null;
	}
	
	/**
	 * Pass message and sending parameters to client
	 */
	public boolean sendMessage(String message, int maxFragmentSize, boolean corrupted) {
		
		if (isClientConnected()) {
			if (mClient.isAvailable()) {
				byte[] messageData = Utilities.stringToBytes(message);
				mClient.setData(CustomProtocol.TYPE_MESSAGE, messageData, null, maxFragmentSize, corrupted);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Prompt user with pick file option and pass reference to file and sending parameters to client
	 */
	public boolean sendFile(int maxFragmentSize, boolean corrupted) {
		
		if (isClientConnected()) {
			File file = Utilities.pickFile(this);
			if (file != null) {
				if (mClient.isAvailable()) {
					byte[] fileData = Utilities.fileToBytes(file);
					String fileName = Utilities.getFileName(file);
					if (fileData != null) {
						mClient.setData(CustomProtocol.TYPE_FILE, fileData, fileName, maxFragmentSize, corrupted);
					} else {
						return false;
					}
				}
				return true;	
			}
		}
		return false;
	}
	
	

}
