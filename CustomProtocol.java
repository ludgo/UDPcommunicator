package pks;

import java.util.Arrays;

public class CustomProtocol {
	
	public static final int TYPE_INFO = 1;
	public static final int TYPE_MESSAGE = 2;
	public static final int TYPE_FILE = 3;
	
	private static final int NAME_LENGTH_MIN = 0;
	private static final int NAME_LENGTH_MAX = 255;
	private static final int DATA_LENGTH_MIN = 0;
	private static final int DATA_LENGTH_MAX = 65459;
	
	private static final int CUSTOM_HEADER_LENGTH = 8;

	private static boolean correctParams(int type, int nameLength, int dataLength) {
		switch(type) {
		case TYPE_MESSAGE: {
			return nameLength == NAME_LENGTH_MIN &&
					dataLength > DATA_LENGTH_MIN &&
					dataLength <= DATA_LENGTH_MAX;
		}
		case TYPE_FILE: {
			return nameLength > NAME_LENGTH_MIN &&
					nameLength <= NAME_LENGTH_MAX &&
					dataLength > DATA_LENGTH_MIN &&
					dataLength <= DATA_LENGTH_MAX;
		}
		default:
			return false;
		}
	}
	
	public static byte[] addCustomHeader(int type, byte[] data, String name) {
		
		if (data == null) return null;
		if (name == null) name = "";
		byte[] nameData = Utilities.stringToBytes(name);

		int nameLength = nameData.length;
		int dataLength = data.length;
		
		if (correctParams(type, nameLength, dataLength)) {
			int packetOrder = 1;
			int totalPackets = 1;
			
			int udpLength = CUSTOM_HEADER_LENGTH + nameLength + dataLength;
			byte[] udpData = new byte[udpLength];
			
			udpData[0] = (byte) (packetOrder >> 8);
			udpData[1] = (byte) packetOrder;
			udpData[2] = (byte) (totalPackets >> 8);
			udpData[3] = (byte) totalPackets;
			udpData[4] = (byte) type;
			udpData[5] = (byte) nameLength;
			udpData[6] = (byte) (dataLength >> 8);
			udpData[7] = (byte) dataLength;
			System.arraycopy(nameData, 0, udpData, CUSTOM_HEADER_LENGTH, nameLength);
			System.arraycopy(data, 0, udpData, CUSTOM_HEADER_LENGTH + nameLength, dataLength);

			return udpData;
		}
		return null;
	}
	
	public static int getPacketOrder(byte[] udpData) {
		return (udpData[0] << 8) | udpData[1];
	}
	
	public static int getTotalPackets(byte[] udpData) {
		return (udpData[2] << 8) | udpData[3];
	}
	
	public static int getType(byte[] udpData) {
		return udpData[4];
	}
	
	public static int getNameLength(byte[] udpData) {
		return udpData[5];
	}
	
	public static int getDataLength(byte[] udpData) {
		return (udpData[6] << 8) | udpData[7];
	}
	
	public static String getName(byte[] udpData) {
		int nameLength = getNameLength(udpData);
		byte[] nameData = new byte[nameLength];
		System.arraycopy(udpData, CUSTOM_HEADER_LENGTH, nameData, 0, nameLength);
		return Utilities.bytesToString(nameData);
	}
	
	public static byte[] getData(byte[] udpData) {
		int nameLength = getNameLength(udpData);
		int dataLength = getDataLength(udpData);
		byte[] data = new byte[dataLength];
		System.arraycopy(udpData, CUSTOM_HEADER_LENGTH + nameLength, data, 0, dataLength);
		return data;
	}

}
