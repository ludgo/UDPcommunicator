package pks;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Observer;

public class Client extends Thread {
	
	private int mType;
	private byte[] mData;
	private String mName;

	private DatagramSocket mSocket = null;
	private InetAddress mHost = null;
	private String mIpAddress;
	private int mPort;
	private MyObservable mObservable;
	
	private boolean mConnected;
	
	public Client(String ipAddress, int port, Observer o) {
		mIpAddress = ipAddress;
		mPort = port;
		mObservable = new MyObservable(o);
	}
	
	public void setData(int type, byte[] data, String name) {
		mType = type;
		mData = data;
		mName = name;
	}
		
	public void halt() {
		mConnected = false;
		mSocket.close();
		
	}
	
	public void run() {
		connect();
    	mObservable.informUser("Client disconnected.\n");
   }
	
	private void connect() {   
        try
        {
        	mSocket = new DatagramSocket();
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
        
        while (mConnected) {
        	try {
				sleep(3000);
			} catch (InterruptedException e) {
				
				//mObservable.informUser(e.toString());
			}
        	
        	send();
        	mType = 0;
        	mData = null;
        	mName = null;
        }
	}
	
	private void send() {
		
		
		if (mSocket != null && mType > 0 && mData != null) {
			
			byte[] udpData = CustomProtocol.addCustomHeader(mType, mData, mName);
			if (udpData == null) return;
			
			try {
	            		        
		        DatagramPacket dp = new DatagramPacket(
		        		udpData , udpData.length , mHost , mPort);
		        mSocket.send(dp);
		         
		        //now receive reply
		        //buffer to receive incoming data
		        byte[] buffer = new byte[65536];
		        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
		        mSocket.receive(reply);
		         
		        byte[] data = reply.getData();
		        String response = new String(data, 0, reply.getLength());
		         
		        //echo the details of incoming data - client ip : client port - client message
		        mObservable.informUser(reply.getAddress().getHostAddress() + " : " + reply.getPort() + " - " + response + "\n");

	        }
	         
	        catch(IOException e)
	        {
				//mObservable.informUser(e.toString());
	        }
		}
				
	}

}
