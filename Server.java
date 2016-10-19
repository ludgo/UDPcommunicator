package pks;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Observer;

import javax.swing.JPanel;

public class Server extends Thread {
	
	private byte[][] mUdpPackets = null;
	private CustomProtocol mCustomProtocol;

	private DatagramSocket mSocket = null;
	private int mPort;
	private MyObservable mObservable;
	
	private JPanel mPanel;
	
	public Server(int port, Observer o, JPanel panel) {
		mCustomProtocol = new CustomProtocol();
		mPort = port;
		mObservable = new MyObservable(o);
		mPanel = panel;
	}
	
	public void run() {
		launch();
		mObservable.informUser("Shutting down server...\n");
    }
	
	private void launch() {
		/*InetAddress localAddress; 
		try {			
			mSocket = new DatagramSocket(mPort);			
			mSocket.connect(InetAddress.getByName("localhost"), mPort);

	        localAddress = mSocket.getInetAddress();

	        mSocket.disconnect();
	        mSocket.close();
	        mSocket = null;
        }    
        catch(IOException e)
        {
        	mObservable.informUser("Server not launched.\n");
        	return;
        }
		
		try {			
			mSocket = new DatagramSocket(mPort, localAddress);
        	mObservable.informUser("Server socket created for " + localAddress.getHostAddress() + 
        			". Waiting for incoming data at port " + mPort + "...\n");  
        }    
        catch(IOException e)
        {
        	mObservable.informUser("Server not launched.\n");
        	return;
        }*/
		
		try {			
			mSocket = new DatagramSocket(mPort);
        }    
        catch(IOException e)
        {
        	mObservable.informUser("Server not launched.\n");
        	return;
        }
    	mObservable.informUser("Server socket created. Waiting for incoming data at port " + mPort + "...\n");  
		
		if (mSocket != null) {
		
			try {     
	        	byte[] buffer = new byte[65536];
	        	DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
	         
	            whileloop: while(true)
	            {
	            	mSocket.receive(incoming);
		        	byte[] data = incoming.getData();
		        	
		        	byte[] udpData = Arrays.copyOf(data, incoming.getLength());	 
		        	int type = mCustomProtocol.getType(udpData);
		        	
		        	byte[] sendData;
		        	String s = "";

		            switchlabel: switch(type) {
		    		case CustomProtocol.TYPE_CONNECT: {
		    			if (mUdpPackets == null) {
			        		sendData = mCustomProtocol.buildSignalMessage(CustomProtocol.TYPE_CONFIRM);		    				
			        	} else {
			    			for (int i = 0; i < mUdpPackets.length; i++) {
			    				if (mUdpPackets[i] == null) {
					        		sendData = mCustomProtocol.buildSignalMessage(CustomProtocol.TYPE_CONFIRM);		    				
					        		break switchlabel;
			    				}
			    			}
			    			String fileName = Utilities.bytesToString(mUdpPackets[0]);
			    			mUdpPackets[0] = new byte[0];
			    			
			    			byte[] fileData = Utilities.joinArrays(mUdpPackets);
			    	        Utilities.saveFile(mPanel, fileData, fileName);
			    	        mUdpPackets = null;
			        		
			        		mObservable.informUser("fileName:" + fileName + "\n");

			        		sendData = mCustomProtocol.buildSignalMessage(CustomProtocol.TYPE_SUCCESS);			        				
			        	}
		        		break;
		    		}
		    		case CustomProtocol.TYPE_MESSAGE: {
		    			byte[] messageData = mCustomProtocol.getData(udpData);
		    			s = Utilities.bytesToString(messageData);
		    			
		    	        mObservable.informUser("OK " + incoming.getAddress().getHostAddress() + " " + incoming.getPort() + " - " + s + "\n");
			            
		        		sendData = mCustomProtocol.buildSignalMessage(CustomProtocol.TYPE_OK);			            
		                break;
		    		}
		    		case CustomProtocol.TYPE_FILE: {
		    			int packetOrder = mCustomProtocol.getPacketOrder(udpData);
		    			int totalPackets = mCustomProtocol.getTotalPackets(udpData);
		    			byte[] part = mCustomProtocol.getData(udpData);

		    			if (mUdpPackets == null) {
		    				mUdpPackets = new byte[totalPackets][];
		    			}
		    			mUdpPackets[packetOrder - 1] = part;

		        		sendData = mCustomProtocol.buildSignalMessage(packetOrder, totalPackets, CustomProtocol.TYPE_OK);
		                break;
		    		}
		    		default:{
		    			continue whileloop;
		    		}
		    		}
		            
		            send(sendData, incoming.getAddress(), incoming.getPort());
	            }
	        }
	         
	        catch(IOException e)
	        {
				//mObservable.informUser(e.toString());
	        }
		}
	}
	
	private void send(byte[] data, InetAddress host, int port) {
		
		try {	        
	        DatagramPacket dp = new DatagramPacket(
	        		data , data.length , host , port);
	        mSocket.send(dp);
       }
         
        catch(IOException e)
        {
			//mObservable.informUser(e.toString());
       }
	}
	
	public void halt() {
		mSocket.close();
		mSocket = null;
	}
}
