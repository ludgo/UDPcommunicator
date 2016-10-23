package pks;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observer;

public class Client extends Thread {
	
	public static final int STATUS_WAIT = 10;
	public static final int STATUS_INIT = 11;
	public static final int STATUS_SEND = 12;
	public static final int STATUS_DONE = 13;
	private int mStatus;
	
	private boolean mConnected;
	private byte[][] mUdpPackets;
	private CustomProtocol mCustomProtocol;

	private static final int RETRY_LIMIT = 30;
	private static final int NO_RESPONSE_LIMIT = 10;
	//private static final int SLEEP_TIME = 3000;
	private static final int RECEIVE_TIMEOUT = 3000;
	
	private DatagramSocket mSocket = null;
	private InetAddress mHost = null;
	private String mServerIpAddress;
	private int mServerPort;
	private int mClientPort;
	private MyObservable mObservable;
		
	public Client(String serverIpAddress, int serverPort, int clientPort, Observer o) {
		mCustomProtocol = new CustomProtocol();
		mServerIpAddress = serverIpAddress;
		mServerPort = serverPort;
		mClientPort = clientPort;
		mObservable = new MyObservable(o);
		mStatus = STATUS_WAIT;
	}
	
	public void setData(int type, byte[] data, String name, int maxFragmentSize) {

		byte[] nameData = Utilities.stringToBytes(name);
		
		ArrayList<byte[]> packets = null;
		if (nameData != null) {
			packets = Utilities.addDataParts(packets, nameData, maxFragmentSize - CustomProtocol.CUSTOM_HEADER_LENGTH);			
		}
		packets = Utilities.addDataParts(packets, data, maxFragmentSize - CustomProtocol.CUSTOM_HEADER_LENGTH);
		
		int totalPackets = packets.size();
		mUdpPackets = new byte[totalPackets][];
		for (int i = 0; i < totalPackets; i++) {
			mUdpPackets[i] = mCustomProtocol.addHeader(i+1, totalPackets, type, packets.get(i));
			if (mUdpPackets[i] == null) {
				mObservable.informUser("Client: Corrupted data.\n");
				return;
			}
		}
		
		mStatus = STATUS_INIT;
	}
	
	public void run() {
		connect();
		mConnected = false;
    	mObservable.informUser("Client disconnected.\n");
    }
	
	private void connect() {

		InetAddress localAddress; 
		try {			
			mSocket = new DatagramSocket(mClientPort);			
			mSocket.connect(InetAddress.getByName(mServerIpAddress), mServerPort);

	        localAddress = mSocket.getLocalAddress();

	        mSocket.disconnect();
	        mSocket.close();
	        mSocket = null;
        }    
        catch(IOException e)
        {
        	mObservable.informUser("Client not connected.\n");
        	return;
        }
		
		try {			
			mSocket = new DatagramSocket(mClientPort, localAddress);
        }    
        catch(IOException e)
        {
        	mObservable.informUser("Client not connected.\n");
        	return;
        }

		try {
			mHost = InetAddress.getByName(mServerIpAddress);
		} catch (UnknownHostException e) {
			halt();
	       	mObservable.informUser("Client not connected.\n");
        	return;
 		}
        mConnected = true;
        mObservable.informUser("Client socket created at " + localAddress.getHostAddress() + ":" + mClientPort + 
        		" and connected to server at " + mHost.getHostAddress() + ":" + mServerPort + ".\n");  
        
        int retryCount = 0, noResponseCount = 0;
        loop: while (mConnected) {
        	
        	/*try {
				sleep(SLEEP_TIME);
			} catch (InterruptedException e) {				
				//mObservable.informUser(e.toString());
			}*/
        	
        	byte[] sendData = null;
        	
        	switch(mStatus) {
        	case STATUS_WAIT: {
        		retryCount = noResponseCount = 0;
        		continue loop;
        	}
        	case STATUS_INIT: {
        		sendData = mCustomProtocol.buildSignalMessage(CustomProtocol.TYPE_CONNECT);
        		mObservable.informUser("Client: Initializing transport...\n");
        		break;
        	}
        	case STATUS_SEND: {
        		for (int i = 0; i < mUdpPackets.length; i++) {
        			if (mUdpPackets[i] != null) {
        				sendData = mUdpPackets[i];
                		mObservable.informUser("Client: Sending fragment " + (i+1) + "/" + mUdpPackets.length + 
                				" (" + mUdpPackets[i].length + " B) ...\n");
        				break;
        			}
        			if (i == mUdpPackets.length - 1) {
                		mStatus = STATUS_DONE;
            			continue loop;
        			}
        		}
        		break;
        	}
        	case STATUS_DONE: {
        		sendData = mCustomProtocol.buildSignalMessage(CustomProtocol.TYPE_CONNECT);
        		break;
        	}
        	default:
        		continue loop;
        	}
        	
        	if (noResponseCount > NO_RESPONSE_LIMIT) {
        		mUdpPackets = null;
        		mStatus = STATUS_WAIT;
        		mObservable.informUser("Server not responding or responding corrupted. Terminating...\n");
    			continue loop;
        	}      	
        	if (retryCount > RETRY_LIMIT) {
        		mUdpPackets = null;
        		mStatus = STATUS_WAIT;
        		mObservable.informUser("Sent data too corrupted. Terminating...\n");
    			continue loop;
        	}
        	
        	byte[] replyData = send(sendData);
        	
        	if (replyData == null) {
        		noResponseCount++;
        		continue loop;
        	} else {
        		
        		replyData = mCustomProtocol.checkChecksum(replyData);
	        	if (replyData == null) {
	        		noResponseCount++;
	        		continue loop;
	        	}
	        	
	        	int replyPacketOrder = mCustomProtocol.getPacketOrder(replyData);
	        	
	        	switch(mCustomProtocol.getType(replyData)) {
	        	case CustomProtocol.TYPE_CONFIRM: {
	        		if (mStatus == STATUS_INIT) {
	        			mStatus = STATUS_SEND;	        			
	        		}
	        		break;
	        	}
	        	case CustomProtocol.TYPE_OK: {
            		mObservable.informUser("Client: Fragment " + replyPacketOrder + "/" + mUdpPackets.length + 
            				" delivered.\n");
	        		int packetOrder = mCustomProtocol.getPacketOrder(replyData);
	        		mUdpPackets[packetOrder - 1] = null;
	        		break;
	        	}
	        	case CustomProtocol.TYPE_RETRY: {
            		mObservable.informUser("Client: Fragment " + replyPacketOrder + "/" + mUdpPackets.length + 
            				" delivered corrupted. Retry.\n");
	        		retryCount++;
	        		break;
	        	}
	        	case CustomProtocol.TYPE_SUCCESS: {
	    			mObservable.informUser("Client: Sending ended with success.\n");
	        		mStatus = STATUS_WAIT;
	        		break;
	        	}
	        	case CustomProtocol.TYPE_FAIL: {
            		mObservable.informUser("Client: Sending ended with failure.\n");
	        		mStatus = STATUS_WAIT;
	        		break;
	        	}
	        	default:
	        		noResponseCount++;
	        		break;
	        	}
        	}
        }
	}
	
	private byte[] send(byte[] data) {
				
		if (mSocket != null) {
			if (data != null) {
				try {
					
			        DatagramPacket dp = new DatagramPacket(
			        		data , data.length , mHost , mServerPort);
			        mSocket.send(dp);
			         
			        byte[] buffer = new byte[65536];
			        DatagramPacket reply = new DatagramPacket(
			        		buffer, buffer.length);
			        mSocket.setSoTimeout(RECEIVE_TIMEOUT);
			        mSocket.receive(reply);
			        
			        return Arrays.copyOf(reply.getData(), reply.getLength());	 
		        }
		         
		        catch(IOException e)
		        {
					//mObservable.informUser(e.toString());
		        }
			}
		}
		else {
			mConnected = false;
		}
		return null;
	}
	
	public void halt() {
		mSocket.close();		
		mSocket = null;
		mHost = null;
	}
	
	public boolean isAvailable() {
		if (mStatus == STATUS_WAIT) {
			return true;
		} else {
			mObservable.informUser("Client is busy.\n");
			return false;
		}
	}
	
	public int getUsedPort() {
		return mClientPort;
	}
}
