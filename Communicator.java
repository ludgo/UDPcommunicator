package pks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Observer;

import javax.swing.JFileChooser;
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
	
	public void pickFile() {
		final JFileChooser fc = new JFileChooser();
		
		int returnVal = fc.showOpenDialog(Communicator.this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            System.out.println(file.getPath());
            
            byte[] data;
            Path path = Paths.get(file.getPath());
            try {
				data = Files.readAllBytes(path);
	            System.out.println(Arrays.toString(data));
	            
	            
	            
	            
	            Path newPath = Paths.get("C:\\Users\\lajos\\Desktop\\New Text Document2.txt");
	            Files.write(newPath, data);
	            
	            
	            
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        } else {

        }
	}
	
	public boolean launchServer(int port, Observer observer) {
		if (mServer == null) {
			mServer = new Server(port, observer);
			mServer.start();
			return true;
		} else {
			return false;
		}
	}
	
	public boolean stopServer() {
		if (mServer != null) {
			mServer.halt();
			mServer = null;
			return true;
		}
		return false;
	}
	
	public boolean connectClient(String ipAddress, int port, Observer observer) {
		if (mClient == null) {
			mClient = new Client(ipAddress, port, observer);
			mClient.start();
			return true;
		} else {
			return false;
		}
	}
	
	public boolean sendMessage(String message) {
		if (mClient != null) {
			mClient.setMessage(message);
			return true;
		}
		return false;
	}
	
	public boolean disconnectClient() {
		if (mClient != null) {
			mClient.halt();
			mClient = null;
			return true;
		}
		return false;
	}
	
	

}
