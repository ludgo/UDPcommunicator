package pks;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Utilities {
		
	public static File pickFile(JPanel panel) {
		
		final JFileChooser chooser = new JFileChooser();
		
		int userSelection = chooser.showOpenDialog(panel);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
        	
            return chooser.getSelectedFile();
        }
        return null;
	}
	
	public static byte[] stringToBytes(String string) {
		if (string != null) {
	        return string.getBytes(Charset.forName("UTF-8"));
		}
        return null;
	}
	
	public static String bytesToString(byte[] data) {
		if (data != null) {
	        try {
				return new String(data, "UTF-8");
			} catch (UnsupportedEncodingException e) {

				//e.printStackTrace();
			}
		}
        return null;
	}
	
	public static byte[] fileToBytes(File file) {
		if (file != null) {
			byte[] fileData;
	        Path path = Paths.get(file.getAbsolutePath());
	        
	        try {
	        	fileData = Files.readAllBytes(path);
	            return fileData;
	
	        } catch (IOException e) {

				//e.printStackTrace();
			}
		}
        return null;
	}
	
	public static String getFileName(File file) {
		if (file != null) {
			Path path = Paths.get(file.getAbsolutePath());
	        return path.getFileName().toString();
		}
		return null;
	}
	
	public static void saveFile(JPanel panel, byte[] fileData, String fileName) {        
        
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(fileName));
         
        int userSelection = chooser.showSaveDialog(panel);
         
        if (userSelection == JFileChooser.APPROVE_OPTION) {
        	
            File file = chooser.getSelectedFile();

            if ((file != null) && file.exists()) {
            	
                int response = JOptionPane.showConfirmDialog(panel,
                		file.getName() + " already exists. Do you want to replace it?",
                		"Replace", 
                		JOptionPane.YES_NO_OPTION,
                		JOptionPane.WARNING_MESSAGE);
                
                if (response == JOptionPane.NO_OPTION) {
                	return;
                }                            
            }
            
            Path filePath = Paths.get(file.getAbsolutePath());
    		try {
    			Files.write(filePath, fileData);
    		} catch (IOException e) {

    			//e.printStackTrace();
    		}
        }
	}
	
	public static long calcChecksum(byte[] data) {
		Checksum checksum = new CRC32();		
		checksum.update(data, 0, data.length);				 
		return checksum.getValue();
	}
	
	public static boolean validateChecksum(byte[] data, long suggested) {
		long value = calcChecksum(data);
		return value == suggested;
	}
}
