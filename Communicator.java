package pks;

import java.io.File;
import java.util.Observer;

import javax.swing.JPanel;

public class Communicator extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private Server mServer = null;
	private Client mClient = null;
	
	// Run program
	public static void main(String[] args) {
		Communicator c = new Communicator();
		Gui g = new Gui(c);
	}
	
	
	public boolean launchServer(int port, Observer observer) {
		if (!isServerLaunched()) {
			mServer = new Server(port, observer, this);
			mServer.start();
			return true;
		} else {
			return false;
		}
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
	
	public boolean connectClient(String ipAddress, int port, Observer observer) {
		if (!isCientConnected()) {
			mClient = new Client(ipAddress, port, observer);
			mClient.start();
			return true;
		} else {
			return false;
		}
	}
	
	public boolean disconnectClient() {
		if (isCientConnected()) {
			mClient.halt();
			mClient = null;
			return true;
		}
		return false;
	}
	
	private boolean isCientConnected() {
		return mClient != null;
	}
	
	public boolean sendMessage(String message) {
		if (isCientConnected()) {
			byte[] data = Utilities.packMessage(message);
			if (data != null) {
				mClient.setData(data, Client.TYPE_MESSAGE);
				return true;	
			}
		}
		return false;
	}
	
	public boolean sendFile() {
		if (isCientConnected()) {
			File file = Utilities.pickFile(this);
			if (file != null) {
				byte[] data = Utilities.packFile(file);
				if (data != null) {
					mClient.setData(data, Client.TYPE_FILE);
					return true;	
				}
			}
		}
		return false;
	}
	
	

}
