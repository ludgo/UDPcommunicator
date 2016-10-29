package pks;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * A helper class with util methods
 */
public class Utilities {
	
	/**
	 * Choose file at system explorer
	 * @return A selected file
	 */
	public static File pickFile(JPanel panel) {
		
		final JFileChooser chooser = new JFileChooser();
		
		int userSelection = chooser.showOpenDialog(panel);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
        	
            return chooser.getSelectedFile();
        }
        return null;
	}
	
	/**
	 * Convert UTF-8 formatted string to byte array
	 */
	public static byte[] stringToBytes(String string) {
		if (string != null) {
	        return string.getBytes(Charset.forName("UTF-8"));
		}
        return null;
	}
	
	/**
	 * Convert byte array to UTF-8 formatted string
	 */
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
	
	/**
	 * Convert file to byte array
	 */
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
	
	/**
	 * Obtain file name (including extension)
	 */
	public static String getFileName(File file) {
		if (file != null) {
			Path path = Paths.get(file.getAbsolutePath());
	        return path.getFileName().toString();
		}
		return null;
	}
	
	/**
	 * Save file by system explorer
	 * @return A absolute file path on success, null otherwise
	 */
	public static String saveFile(JPanel panel, byte[] fileData, String fileName) {        
        
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(fileName));
         
        int userSelection = chooser.showSaveDialog(panel);
         
        if (userSelection == JFileChooser.APPROVE_OPTION) {
        	
            File file = chooser.getSelectedFile();

            if (file == null) return null;
            if (file.exists()) {
            	
                int response = JOptionPane.showConfirmDialog(panel,
                		file.getName() + " already exists. Do you want to replace it?",
                		"Replace", 
                		JOptionPane.YES_NO_OPTION,
                		JOptionPane.WARNING_MESSAGE);
                
                if (response == JOptionPane.NO_OPTION) {
                	return null;
                }                            
            }
            
            Path filePath = Paths.get(file.getAbsolutePath());
    		try {
    			Files.write(filePath, fileData);
    			return filePath.toString();
    			
    		} catch (IOException e) {

    			//e.printStackTrace();
    		}
        }
        return null;
	}
	
	/**
	 * Calculate checksum of byte array
	 */
	public static long calcChecksum(byte[] data) {
		Checksum checksum = new CRC32();		
		checksum.update(data, 0, data.length);				 
		return checksum.getValue();
	}
	
	/**
	 * Check whether specified byte array checksum is valid or not
	 */
	public static boolean validateChecksum(byte[] data, long suggested) {
		if (data == null) return false;
		long value = calcChecksum(data);
		return value == suggested;
	}
	
	/**
	 * Divide byte array into parts of maximal length and append them to array list of byte arrays
	 */
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
	
	/**
	 * Merge array of byte arrays
	 * @return A single joined array
	 */
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
	
	/**
	 * Create byte from specified part of integer
	 */
	public static byte intToByte(int value, int shiftRight) {
		return ((byte) ((value >> shiftRight) & 0xff));
	}
	
	/**
	 * Create integer from byte with specified bit shift
	 */
	public static int byteToInt(byte value, int shiftLeft) {
		return ((value & 0xff) << shiftLeft);
	}
	
	/**
	 * Create byte from specified part of long
	 */
	public static byte longToByte(long value, int shiftRight) {
		return ((byte) ((value >> shiftRight) & 0xffL));
	}
	
	/**
	 * Create long from byte with specified bit shift
	 */
	public static long byteToLong(byte value, int shiftLeft) {
		return ((value & 0xffL) << shiftLeft);
	}
	
	/**
	 * Format datagram packet sender info
	 * @return <ip address>:<port>
	 */
	public static String formatHost(DatagramPacket packet) {
		if (packet == null) return null;
		return packet.getAddress().getHostAddress() + ":" + packet.getPort();
	}
	
	/**
	 * Obtain formatted current time
	 * @return <year>-<month>-<day> <hours>:<minutes>:<seconds>
	 */
	public static String getCurrentTime() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}
	
}
