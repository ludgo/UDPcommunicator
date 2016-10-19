package pks;

import java.util.Arrays;

public class CustomProtocol {
	
	public static final int TYPE_CONNECT = 1;
	public static final int TYPE_CONFIRM = 2;
	public static final int TYPE_MESSAGE = 3;
	public static final int TYPE_FILE = 4;
	public static final int TYPE_OK = 6;
	public static final int TYPE_RETRY = 7;
	public static final int TYPE_SUCCESS = 8;
	public static final int TYPE_FAIL = 9;
	
	public static final int DATA_LENGTH_MIN = 0;
	public static final int DATA_LENGTH_MAX = 65455;
	
	private static final int CUSTOM_HEADER_LENGTH = 12;

	private boolean checkParams(int packetOrder, int totalPackets, int length, int type) {
		// TODO
		switch(type) {
		case TYPE_CONNECT:
		case TYPE_CONFIRM:
		case TYPE_MESSAGE:
		case TYPE_FILE:
		case TYPE_OK:
		case TYPE_RETRY:
		case TYPE_SUCCESS:
		case TYPE_FAIL:{
			return packetOrder > 0 &&
					packetOrder <= totalPackets &&
					length > DATA_LENGTH_MIN &&
					length <= DATA_LENGTH_MAX;
		}
		default:
			return false;
			
		}
	}
	
	public byte[] addHeader(int packetOrder, int totalPackets, int type, byte[] data) {
		
		if (data == null) return null;
		int length = data.length;
		System.out.println(length + "\n");
		long checksum = 0;
		//long checksum = Utilities.calcChecksum(data);
		
		if (checkParams(packetOrder, totalPackets, length, type)) {
			
			int udpLength = CUSTOM_HEADER_LENGTH + length;
			byte[] udpData = new byte[udpLength];
			
			// packet order
			udpData[0] = Utilities.numToByte(packetOrder, 8);
			udpData[1] = Utilities.numToByte(packetOrder);
			// total packets
			udpData[2] = Utilities.numToByte(totalPackets, 8);
			udpData[3] = Utilities.numToByte(totalPackets);
			// length
			udpData[4] = Utilities.numToByte(length, 8);
			udpData[5] = Utilities.numToByte(length);
			// type
			udpData[6] = Utilities.numToByte(type, 8);
			udpData[7] = Utilities.numToByte(type);
			// checksum
			udpData[8] = Utilities.numToByte(checksum, 24);
			udpData[9] = Utilities.numToByte(checksum, 16);
			udpData[10] = Utilities.numToByte(checksum, 8);
			udpData[11] = Utilities.numToByte(checksum);
			System.arraycopy(data, 0, udpData, CUSTOM_HEADER_LENGTH, length);

			return udpData;
		}
		return null;
	}
	
	public int getPacketOrder(byte[] udpData) {
		return (int) (Utilities.byteToNum(udpData[0], 8) | Utilities.byteToNum(udpData[1]));
	}
	
	public int getTotalPackets(byte[] udpData) {
		return (int) (Utilities.byteToNum(udpData[2], 8) | Utilities.byteToNum(udpData[3]));
	}
	
	public int getLength(byte[] udpData) {
		int aaa = (int) (Utilities.byteToNum(udpData[4], 8) | Utilities.byteToNum(udpData[5]));
		System.out.println(aaa + "\n");
		return (int) (Utilities.byteToNum(udpData[4], 8) | Utilities.byteToNum(udpData[5]));
	}
	
	public int getType(byte[] udpData) {
		return (int) (Utilities.byteToNum(udpData[6], 8) | Utilities.byteToNum(udpData[7]));
	}
	
	public long getChecksum(byte[] udpData) {
		return (int) (Utilities.byteToNum(udpData[8], 24) | Utilities.byteToNum(udpData[9], 16) | 
				Utilities.byteToNum(udpData[10], 8) | Utilities.byteToNum(udpData[11]));
	}
	
	public byte[] getData(byte[] udpData) {
		int dataLength = getLength(udpData);
		byte[] data = new byte[dataLength];
		System.arraycopy(udpData, CUSTOM_HEADER_LENGTH, data, 0, dataLength);
		return data;
	}
	
	public byte[] buildSignalMessage(int packetOrder, int totalPackets, int type) {
		return addHeader(packetOrder, totalPackets, type, new byte[]{0,0,0,0});
	}
	
	public byte[] buildSignalMessage(int type) {
		return buildSignalMessage(1, 1, type);
	}

}
