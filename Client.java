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

	private static final int RETRY_LIMIT = 5;
	private static final int NO_RESPONSE_LIMIT = 3;
	private static final int SLEEP_TIME = 1000;
	private static final int RECEIVE_TIMEOUT = 3000;
	
	private DatagramSocket mSocket = null;
	private InetAddress mHost = null;
	private String mIpAddress;
	private int mPort;
	private MyObservable mObservable;
		
	public Client(String ipAddress, int port, Observer o) {
		mCustomProtocol = new CustomProtocol();
		mIpAddress = ipAddress;
		mPort = port;
		mObservable = new MyObservable(o);
		mStatus = STATUS_WAIT;
	}
	
	public void setData(int type, byte[] data, String name) {

		byte[] nameData = Utilities.stringToBytes(name);
		
		ArrayList<byte[]> packets = null;
		if (nameData != null) {
			packets = Utilities.addDataParts(packets, nameData, CustomProtocol.DATA_LENGTH_MAX);			
		}
		packets = Utilities.addDataParts(packets, data, CustomProtocol.DATA_LENGTH_MAX);
		
		int totalPackets = packets.size();
		mUdpPackets = new byte[totalPackets][];
		for (int i = 0; i < totalPackets; i++) {
			mObservable.informUser(Arrays.toString(packets.get(i)) + '\n');
			mUdpPackets[i] = mCustomProtocol.addHeader(i+1, totalPackets, type, packets.get(i));
			mObservable.informUser(Arrays.toString(mUdpPackets[i]) + '\n');
		}
		
		mStatus = STATUS_INIT;
	}
	
	public void run() {
		connect();
		mConnected = false;
    	mObservable.informUser("Client disconnected.\n");
    }
	
	private void connect() {
        try
        {
        	mSocket = new DatagramSocket(7778);
        } 
        catch(IOException e)
        {
        	mObservable.informUser("Client not connected.\n");
        	return;
        }
        
       	try {
			mHost = InetAddress.getByName(mIpAddress);
		} catch (UnknownHostException e) {
			halt();
	       	mObservable.informUser("Client not connected.\n");
        	return;
 		}
       	mConnected = true;
        
        mObservable.informUser("Client socket created for " + mIpAddress + ":" + mPort + "\n");  

        int retryCount = 0, noResponseCount = 0;
        loop: while (mConnected) {
        	
        	try {
				sleep(SLEEP_TIME);
			} catch (InterruptedException e) {				
				//mObservable.informUser(e.toString());
			}

//	        mObservable.informUser(reply.getAddress().getHostAddress() + " : " + reply.getPort() + " - " + response + "\n");
        	
        	byte[] sendData = null;
        	
        	switch(mStatus) {
        	case STATUS_WAIT: {
        		retryCount = noResponseCount = 0;
        		continue loop;
        	}
        	case STATUS_INIT: {
        		sendData = mCustomProtocol.buildSignalMessage(CustomProtocol.TYPE_CONNECT);
        		break;
        	}
        	case STATUS_SEND: {
    	        mObservable.informUser("send\n");
        		for (int i = 0; i < mUdpPackets.length; i++) {
        			if (mUdpPackets[i] != null) {
    					mObservable.informUser("break\n");
        				sendData = mUdpPackets[i];
        				break;
        			}
        			if (i == mUdpPackets.length - 1) {
    					mObservable.informUser("continue\n");
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
        	
        	byte[] replyData = send(sendData);
        	
        	if (replyData == null) {
        		noResponseCount++;
        		continue loop;
        	} else {
	        	int replyType = mCustomProtocol.getType(replyData);
	        	switch(replyType) {
	        	case CustomProtocol.TYPE_CONFIRM: {
	        		if (mStatus == STATUS_INIT) {
	        			mStatus = STATUS_SEND;
	        		}
	        		break;
	        	}
	        	case CustomProtocol.TYPE_OK: {
					mObservable.informUser("server ok\n");
	        		int packetOrder = mCustomProtocol.getPacketOrder(replyData);
	        		mUdpPackets[packetOrder - 1] = null;
	        		break;
	        	}
	        	case CustomProtocol.TYPE_RETRY: {
					mObservable.informUser("server retry\n");
	        		retryCount++;
	        		break;
	        	}
	        	case CustomProtocol.TYPE_SUCCESS: {
					mObservable.informUser("server success\n");
	        		mStatus = STATUS_WAIT;
	        		break;
	        	}
	        	case CustomProtocol.TYPE_FAIL: {
					mObservable.informUser("server fail\n");
	        		mStatus = STATUS_WAIT;
	        		break;
	        	}
	        	default:
	        		noResponseCount++;
	        		break;
	        	}
        	}
        	
        	if (retryCount > RETRY_LIMIT ||
        			noResponseCount > NO_RESPONSE_LIMIT) {
        		mStatus = STATUS_WAIT;
        	}
        }
	}
	
	private byte[] send(byte[] data) {
				
		if (mSocket != null) {
			if (data != null) {
				try {
					//mObservable.informUser(Arrays.toString(data) + '\n');
    		        
			        DatagramPacket dp = new DatagramPacket(
			        		data , data.length , mHost , 7777);
			        mSocket.send(dp);
			         
			        byte[] buffer = new byte[65536];
			        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			        mSocket.setSoTimeout(RECEIVE_TIMEOUT);
			        mSocket.receive(reply);
			        
			        return reply.getData();
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
		mConnected = false;
		mSocket.close();		
	}
}
