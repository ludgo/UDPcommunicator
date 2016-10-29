package pks;

/*
 * A protocol description and methods as stated in documentation specification
 */
public class CustomProtocol {
	
	// Type types
	public static final int TYPE_CONNECT = 1;
	public static final int TYPE_CONFIRM = 2;
	public static final int TYPE_MESSAGE = 3;
	public static final int TYPE_FILE = 4;
	public static final int TYPE_OK = 6;
	public static final int TYPE_RETRY = 7;
	public static final int TYPE_SUCCESS = 8;
	public static final int TYPE_FAIL = 9;

	public static final int CUSTOM_HEADER_LENGTH = 12;

	public static final int FRAGMENT_SIZE_MIN = CUSTOM_HEADER_LENGTH + 1;
	public static final int FRAGMENT_SIZE_MAX = 65507;

	public static final int DATA_LENGTH_MIN = 1;
	public static final int DATA_LENGTH_MAX = FRAGMENT_SIZE_MAX - CUSTOM_HEADER_LENGTH;

	// Max. value in 2 bytes
	public static final int B2_MAX = 65535;
	
	public static final int ACCEPTED_PORTS_MIN = 8000;
	public static final int ACCEPTED_PORTS_MAX = 8080;

	/**
	 * Validate whether a protocol is to be used properly
	 */
	private boolean checkParams(int packetOrder, int totalPackets, int length, int type) {

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
					totalPackets > 0 &&
					totalPackets <= B2_MAX &&
					length >= DATA_LENGTH_MIN &&
					length <= DATA_LENGTH_MAX;
		}
		default:
			return false;
			
		}
	}
	
	/**
	 * Add header to data byte array
	 * @return A byte array with prepended header if correct params, null otherwise
	 */
	public byte[] addHeader(int packetOrder, int totalPackets, int type, byte[] data) {
		
		if (data == null) return null;
		int length = data.length;
		
		if (checkParams(packetOrder, totalPackets, length, type)) {
			
			int udpLength = CUSTOM_HEADER_LENGTH + length;
			byte[] udpData = new byte[udpLength];
			
			// packet order
			udpData[0] = Utilities.intToByte(packetOrder, 8);
			udpData[1] = Utilities.intToByte(packetOrder, 0);
			// total packets
			udpData[2] = Utilities.intToByte(totalPackets, 8);
			udpData[3] = Utilities.intToByte(totalPackets, 0);
			// length
			udpData[4] = Utilities.intToByte(length, 8);
			udpData[5] = Utilities.intToByte(length, 0);
			// type
			udpData[6] = Utilities.intToByte(type, 8);
			udpData[7] = Utilities.intToByte(type, 0);
			// data
			System.arraycopy(data, 0, udpData, CUSTOM_HEADER_LENGTH, length);

			return setChecksum(removeChecksum(udpData));
		}
		return null;
	}

	public int getPacketOrder(byte[] udpData) {
		return (int) (Utilities.byteToInt(udpData[0], 8) | Utilities.byteToInt(udpData[1], 0));
	}
	
	public int getTotalPackets(byte[] udpData) {
		return (int) (Utilities.byteToInt(udpData[2], 8) | Utilities.byteToInt(udpData[3], 0));
	}
	
	public int getLength(byte[] udpData) {
		return (int) (Utilities.byteToInt(udpData[4], 8) | Utilities.byteToInt(udpData[5], 0));
	}
	
	public int getType(byte[] udpData) {
		return (int) (Utilities.byteToInt(udpData[6], 8) | Utilities.byteToInt(udpData[7], 0));
	}
	
	public long getChecksum(byte[] udpData) {
		return (long) (Utilities.byteToLong(udpData[8], 24) | Utilities.byteToLong(udpData[9], 16) | 
				Utilities.byteToLong(udpData[10], 8) | Utilities.byteToLong(udpData[11], 0));
	}
	
	public byte[] getData(byte[] udpData) {
		int dataLength = getLength(udpData);
		byte[] data = new byte[dataLength];
		System.arraycopy(udpData, CUSTOM_HEADER_LENGTH, data, 0, dataLength);
		return data;
	}
	
	public byte[] buildSignalMessage(int packetOrder, int totalPackets, int type) {
		return addHeader(packetOrder, totalPackets, type, new byte[DATA_LENGTH_MIN]);
	}
	
	public byte[] buildSignalMessage(int type) {
		return buildSignalMessage(1, 1, type);
	}
	
	private byte[] removeChecksum(byte[] udpData) {
		
		long checksum = 0;
		// checksum
		udpData[8] = Utilities.longToByte(checksum, 24);
		udpData[9] = Utilities.longToByte(checksum, 16);
		udpData[10] = Utilities.longToByte(checksum, 8);
		udpData[11] = Utilities.longToByte(checksum, 0);
		
		return udpData;
	}
	
	/**
	 * Add custom protocol checksum to prepared UDP data
	 */
	private byte[] setChecksum(byte[] udpData) {
		
		long checksum = Utilities.calcChecksum(udpData);
		// checksum
		udpData[8] = Utilities.longToByte(checksum, 24);
		udpData[9] = Utilities.longToByte(checksum, 16);
		udpData[10] = Utilities.longToByte(checksum, 8);
		udpData[11] = Utilities.longToByte(checksum, 0);
		
		return udpData;
	}
	
	/**
	 * Check custom protocol checksum from received UDP data
	 */
	public byte[] checkChecksum(byte[] udpData) {
    	long checksum = getChecksum(udpData);
    	udpData = removeChecksum(udpData);
    	if (Utilities.validateChecksum(udpData, checksum)) {
    		return udpData;
    	}
		return null;
	}

}
