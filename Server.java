package pks;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Observer;

import javax.swing.JPanel;

/**
 * A server class in UDP client-server communication on a separate Thread
 */
public class Server extends Thread {
	
	private int counterAck;
	private int counterNack;
	
	private static final int EMPTY_REQUEST_LIMIT = 5;
	
	private byte[][] mUdpPackets = null;
	private CustomProtocol mCustomProtocol;

	private DatagramSocket mSocket = null;
	private int mPort;
	private MyObservable mObservable;
	
	private JPanel mPanel;
	
	/**
	 * @param port A server port
	 * @param o An observer to inform user
	 */
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
		
		counterAck = counterNack = 0;
		
		try {
			// Tight socket to port
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
		        		// Reset
		        		type = -1;
		        		emptyRequestCount = 0;
		        	}
		        	
		        	// Wait for client
		        	mSocket.receive(incoming);
		        	byte[] data = incoming.getData();
		        	
		        	// Only single client at once
/*		        	if (client != null && 
		        			!client.equals(Utilities.formatHost(incoming))) {
		        		continue whileloop;
		        	}*/
		        	byte[] udpData = Arrays.copyOf(data, incoming.getLength());	 

		        	byte[] sendData;
		        	boolean ended = false;

		        	// Validate checksum
		        	udpData = mCustomProtocol.checkChecksum(udpData);
		        	if (udpData == null) {
		        		// Received corrupted data
		        		sendData = mCustomProtocol.buildSignalMessage(CustomProtocol.TYPE_RETRY);
		        		counterNack++;
		        		
		        	} else if (emptyRequestCount >= EMPTY_REQUEST_LIMIT) {
		        		// Too much bad requests
		        		client = null;
		        		sendData = mCustomProtocol.buildSignalMessage(CustomProtocol.TYPE_FAIL);
		        		mObservable.informUser("Server: Transport aborted. Waiting for client exceeded limit.\n");
		        		
		        	} else {
	    				client = Utilities.formatHost(incoming);
		        			        	
			        	int receivedType = mCustomProtocol.getType(udpData);
	
			        	// Determine response
			        	switchlabel: switch(receivedType) {
			    		case CustomProtocol.TYPE_CONNECT: {
			    			if (mUdpPackets == null) {
			    				// Client initialized communication, tight to it
				        		sendData = mCustomProtocol.buildSignalMessage(CustomProtocol.TYPE_CONFIRM);		    				
				        	} else {
				        		
				    			for (int i = 0; i < mUdpPackets.length; i++) {
				    				// Until all parts delivered, receive them
				    				if (mUdpPackets[i] == null) {
						        		emptyRequestCount++;
						        		sendData = mCustomProtocol.buildSignalMessage(CustomProtocol.TYPE_CONFIRM);		    				
						        		break switchlabel;
				    				}
				    			}
				    			
				    			// All parts delivered, respond end status
				    			switch(type) {
				    			case CustomProtocol.TYPE_MESSAGE: {
				    				sendData = mCustomProtocol.buildSignalMessage(CustomProtocol.TYPE_SUCCESS);
				    				break;
				    			}
				    			case CustomProtocol.TYPE_FILE: {
				    				sendData = mCustomProtocol.buildSignalMessage(CustomProtocol.TYPE_SUCCESS);
				    				break;
				    			}
				    			default: {
				    				sendData = mCustomProtocol.buildSignalMessage(CustomProtocol.TYPE_FAIL);
				    				break;
				    			}
				    			}
				    			
				    			mObservable.informUser("-------- Counter ACK: " + counterAck + "\n");
				    			mObservable.informUser("-------- Counter NACK: " + counterNack + "\n");
				    			
				    			ended = true;
				        	}
			        		break;
			    		}
			    		case CustomProtocol.TYPE_MESSAGE:
			    		case CustomProtocol.TYPE_FILE: {
			    			
			    			int packetOrder = mCustomProtocol.getPacketOrder(udpData);
			    			int totalPackets = mCustomProtocol.getTotalPackets(udpData);
			    			byte[] part = mCustomProtocol.getData(udpData);
	
			    			// Process data part
			    			if (mUdpPackets == null) {
			    				mUdpPackets = new byte[totalPackets][];
			    				type = receivedType;
			    			}
			    			if (mUdpPackets[packetOrder - 1] == null) {
			    				mUdpPackets[packetOrder - 1] = part;
		                		mObservable.informUser("Server: Received fragment " + packetOrder + "/" + totalPackets + 
		                				" (" + udpData.length + " B).\n");
			    			}			    			
	
			        		sendData = mCustomProtocol.buildSignalMessage(packetOrder, totalPackets, CustomProtocol.TYPE_OK);
			        		counterAck++;
			                break;
			            }
			    		default: {
			        		emptyRequestCount++;
			    			continue whileloop;
			    		}
			    		}
		        	}
		            
		        	// Respond in a proper way
		            send(sendData, incoming.getAddress(), incoming.getPort());
		            
		            if (ended) {
		            	// After end, pass processed, received data to user
		            	switch(type) {
		    			case CustomProtocol.TYPE_MESSAGE: {
		    				receiveMessage(client);
		    				break;
		    			}
		    			case CustomProtocol.TYPE_FILE: {
		    				receiveFile();
		    				break;
		    			}
		    			default: {
		    				break;
		    			}
		    			}
		            	
		    	        client = null;
		    	        mUdpPackets = null;
		            }
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
	
	/**
	 * Show message at GUI
	 */
	private void receiveMessage(String client) {
		
		byte[] messageData = Utilities.joinArrays(mUdpPackets);
		String message = Utilities.bytesToString(messageData);
		
		mObservable.informUser("Server: 1 new message\n");
		mObservable.informUser("\n");
		mObservable.informUser(client + " - " + Utilities.getCurrentTime() + "\n");
		mObservable.informUser(message + "\n");
		mObservable.informUser("\n");
	}
	
	/**
	 * Prompt user with save file option and show path to it at GUI
	 */
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
	
	public int getUsedPort() {
		return mPort;
	}
}
