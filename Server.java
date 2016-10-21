package pks;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Observer;

import javax.swing.JPanel;

public class Server extends Thread {
	
	private static final int EMPTY_REQUEST_LIMIT = 5;
	
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
		mObservable.informUser("Server: Shutting down...\n");
    }
	
	private void launch() {
		
		try {			
			mSocket = new DatagramSocket(mPort);
        }    
        catch(IOException e)
        {
        	mObservable.informUser("Server not launched.\n");
        	return;
        }
    	mObservable.informUser("Server socket created and waiting for incoming data at port " + mPort + "...\n");  
		
		if (mSocket != null) {
			
			try {     
	        	byte[] buffer = new byte[65536];
	        	DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
	         	        	
				String client = null;
				int type = -1;
				int emptyRequestCount = 0;
				
	            whileloop: while(true)
	            {
		        	if (client == null) {
		        		type = -1;
		        		emptyRequestCount = 0;
		        	}
		        	
		        	mSocket.receive(incoming);
		        	byte[] data = incoming.getData();
		        	
		        	if (client != null && 
		        			!client.equals(Utilities.formatHost(incoming))) {
		        		continue whileloop;
		        	}
		        	byte[] udpData = Arrays.copyOf(data, incoming.getLength());	 

		        	byte[] sendData;

		        	udpData = mCustomProtocol.checkChecksum(udpData);
		        	if (udpData == null) {
		        		
		        		emptyRequestCount++;
		        		sendData = mCustomProtocol.buildSignalMessage(CustomProtocol.TYPE_RETRY);
		        		
		        	} else if (emptyRequestCount > EMPTY_REQUEST_LIMIT) {
		        		
		        		client = null;
		        		sendData = mCustomProtocol.buildSignalMessage(CustomProtocol.TYPE_FAIL);
		        		mObservable.informUser("Server: Transport aborted. Waiting for client exceeded limit.\n");
		        		
		        	} else {
		        			        	
			        	int receivedType = mCustomProtocol.getType(udpData);
	
			        	switchlabel: switch(receivedType) {
			    		case CustomProtocol.TYPE_CONNECT: {
			    			if (mUdpPackets == null) {
			    				client = Utilities.formatHost(incoming);
				        		sendData = mCustomProtocol.buildSignalMessage(CustomProtocol.TYPE_CONFIRM);		    				
				        	} else {
				        		
				    			for (int i = 0; i < mUdpPackets.length; i++) {
				    				if (mUdpPackets[i] == null) {
						        		emptyRequestCount++;
						        		sendData = mCustomProtocol.buildSignalMessage(CustomProtocol.TYPE_CONFIRM);		    				
						        		break switchlabel;
				    				}
				    			}
				    			
				    			switch(type) {
				    			case CustomProtocol.TYPE_MESSAGE: {
				    				receiveMessage(client);
				    				sendData = mCustomProtocol.buildSignalMessage(CustomProtocol.TYPE_SUCCESS);
				    				break;
				    			}
				    			case CustomProtocol.TYPE_FILE: {
				    				receiveFile();
				    				sendData = mCustomProtocol.buildSignalMessage(CustomProtocol.TYPE_SUCCESS);
				    				break;
				    			}
				    			default: {
				    				sendData = mCustomProtocol.buildSignalMessage(CustomProtocol.TYPE_FAIL);
				    				break;
				    			}
				    			}
				    			
				    	        client = null;
				    	        mUdpPackets = null;
				        	}
			        		break;
			    		}
			    		case CustomProtocol.TYPE_MESSAGE:
			    		case CustomProtocol.TYPE_FILE: {
			    			
			    			int packetOrder = mCustomProtocol.getPacketOrder(udpData);
			    			int totalPackets = mCustomProtocol.getTotalPackets(udpData);
			    			byte[] part = mCustomProtocol.getData(udpData);
	
			    			if (mUdpPackets == null) {
			    				mUdpPackets = new byte[totalPackets][];
			    				type = receivedType;
			    			}
			    			mUdpPackets[packetOrder - 1] = part;
			    			
	                		mObservable.informUser("Server: Received fragment " + packetOrder + "/" + totalPackets + 
	                				" (" + udpData.length + " B).\n");
	
			        		sendData = mCustomProtocol.buildSignalMessage(packetOrder, totalPackets, CustomProtocol.TYPE_OK);
			                break;
			            }
			    		default: {
			        		emptyRequestCount++;
			    			continue whileloop;
			    		}
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
	
	private void receiveMessage(String client) {
		
		byte[] messageData = Utilities.joinArrays(mUdpPackets);
		String message = Utilities.bytesToString(messageData);
		
		mObservable.informUser("Server: 1 new message\n");
		mObservable.informUser("\n");
		mObservable.informUser(client + " - " + Utilities.getCurrentTime() + "\n");
		mObservable.informUser(message + "\n");
		mObservable.informUser("\n");
	}
	
	private void receiveFile() {
		
		String fileName = Utilities.bytesToString(mUdpPackets[0]);
		mUdpPackets[0] = new byte[0];
		byte[] fileData = Utilities.joinArrays(mUdpPackets);
        
		String filePath = Utilities.saveFile(mPanel, fileData, fileName);		
        if (filePath == null) {
    		mObservable.informUser("Server: Received file not saved.\n");
        } else {
    		mObservable.informUser("Server: Received file saved.\n");
    		mObservable.informUser(filePath + "\n");
        }
	}
	
	public void halt() {
		mSocket.close();
		mSocket = null;
	}
	
	
}
