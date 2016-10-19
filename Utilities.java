package pks;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
	
	public static ArrayList<byte[]> addDataParts(ArrayList<byte[]> parts, byte[] data, int maxLength) {
		if (parts == null) {
			parts = new ArrayList<>();			
		}
		int dataLength = data.length;
		int rider = 0;
		while (true) {
			byte[] part;
			int length;
			if (dataLength - rider <= maxLength) {
				length = dataLength - rider;
				part = new byte[length];
				System.arraycopy(data, rider, part, 0, length);
				parts.add(part);
				break;
			} else {
				length = maxLength;
				part = new byte[length];
				System.arraycopy(data, rider, part, 0, length);
				parts.add(part);
				rider += maxLength;
			}
		}
		return parts;
	}
	
	public static byte[] joinArrays(byte[][] arrays) {
		int sumLength = 0;
		for (int i = 0; i < arrays.length; i++) {
			sumLength += arrays[i].length;
		}
		byte[] newArray = new byte[sumLength];
		int rider = 0;
		for (int i = 0; i < arrays.length; i++) {
			System.arraycopy(arrays[i], 0, newArray, rider, arrays[i].length);
			rider += arrays[i].length;
		}
		return newArray;
	}
	
	public static byte numToByte(long value, int shiftRight) {
		return ((byte) ((value >> shiftRight) & 0xFF));
	}
	
	public static byte numToByte(long value) {
		return numToByte(value, 0);
	}
	
	public static long byteToNum(byte value, long shiftLeft) {
		return ((value & 0xFF) << shiftLeft);
	}
	
	public static long byteToNum(byte value) {
		return byteToNum(value, 0);
	}
	
}
