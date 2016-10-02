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
	
	Server mServer = null;
	Client mClient = null;
	
	// Run program
	public static void main(String[] args) {
		Communicator c = new Communicator();
		Gui g = new Gui(c);
	}
	
	void pickFile() {
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
	
	void launchServer(String port, Observer observer) {
		if (mServer == null && mClient == null) {
			mServer = new Server(port, observer);
			mServer.start();
		}
	}
	
	void stopServer() {
		if (mServer != null) {
			mServer.halt();
			mServer = null;
		}
	}
	
	void connectClient(String ipAddress, String port) {
		if (mServer == null && mClient == null) {
			mClient = new Client(ipAddress, port);
			mClient.start();
		}
	}

}
