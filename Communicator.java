package pks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

public class Communicator extends JPanel {
	
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
	
	void launchServer(String ipAddress, String port) {
		
	}
	
	void connectClient(String port) {
		
	}

}
