package pks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Utilities {
	
	static String aaa;	
	
	public static File pickFile(JPanel panel) {
		
		final JFileChooser chooser = new JFileChooser();
		
		int userSelection = chooser.showOpenDialog(panel);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
        	
            return chooser.getSelectedFile();
        }
        return null;
	}
	
	public static byte[] packMessage(String message) {
		if (message != null) {
	        return message.getBytes();
		}
        return null;
	}
	
	public static byte[] packFile(File file) {
		if (file != null) {
			byte[] data;
	        Path path = Paths.get(file.getAbsolutePath());
	        String name = path.getFileName().toString();
	        aaa = name;

	        try {
				data = Files.readAllBytes(path);
	            return data;
	
	        } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        return null;
	}
	
	public static void saveFile(JPanel panel, byte[] data, String name) {        
        
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(aaa));
         
        int userSelection = chooser.showSaveDialog(panel);
         
        if (userSelection == JFileChooser.APPROVE_OPTION) {
        	
            File file = chooser.getSelectedFile();

            if ((file != null) && file.exists()) {
                int response = JOptionPane.showConfirmDialog(panel,
                		file.getName() + " already exists. Do you want to replace it?",
                		"Replace", 
                		JOptionPane.YES_NO_OPTION,
                		JOptionPane.WARNING_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                	Path newPath = Paths.get(file.getAbsolutePath());
            		try {
            			Files.write(newPath, data);
            		} catch (IOException e) {
            			// TODO Auto-generated catch block
            			e.printStackTrace();
            		}
                }
            }
        }
	}


}
