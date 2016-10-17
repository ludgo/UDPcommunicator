package pks;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.Observer;

import javax.swing.JPanel;

public class Server extends Thread {
	
	private DatagramSocket mSocket = null;
	private int mPort;
	private MyObservable mObservable;
	
	private JPanel mPanel;
	
	public Server(int port, Observer o, JPanel panel) {
		mPort = port;
		mObservable = new MyObservable(o);
		mPanel = panel;
	}
	
	public void halt() {
		mSocket.close();
	}
	
	public void run() {
		launch();
		mObservable.informUser("Shutting down server...\n");
    }
	
	private void launch() {
		
		try {			
            //1. creating a server socket, parameter is local port number
			mSocket = new DatagramSocket(mPort);
        	mObservable.informUser("Server socket created. Waiting for incoming data at port " + mPort + "...\n");  
        }    
        catch(IOException e)
        {
        	mObservable.informUser("Server not launched.\n");  
        }
		
		if (mSocket != null) {
		
			try {	            
	        	//buffer to receive incoming data
	        	byte[] buffer = new byte[65536];
	        	DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
	         
	        	//2. Wait for an incoming data
	        	//communication loop
	            while(true)
	            {
	            	mSocket.receive(incoming);
					String s = "";

		        	byte[] data = incoming.getData();
		        	
		        	byte[] udpData = Arrays.copyOf(data, incoming.getLength());	 
		        	int type = CustomProtocol.getType(udpData);

		            switch(type) {
		    		case CustomProtocol.TYPE_MESSAGE: {
		    			byte[] messageData = CustomProtocol.getData(udpData);
		    			s = Utilities.bytesToString(messageData);
		    			
		    	        mObservable.informUser(incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + s);
			            
		                s = "OK : " + s;
		                DatagramPacket dp = new DatagramPacket(s.getBytes() , s.getBytes().length , incoming.getAddress() , incoming.getPort());
		                mSocket.send(dp);
		                break;
		    		}
		    		case CustomProtocol.TYPE_FILE: {
		    			byte[] fileData = CustomProtocol.getData(udpData);
		    			String fileName = CustomProtocol.getName(udpData);
		    			Utilities.saveFile(mPanel, fileData, fileName);
		    			
		    	        mObservable.informUser(incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + s);
			            
		                s = "OK : " + s;
		                DatagramPacket dp = new DatagramPacket(s.getBytes() , s.getBytes().length , incoming.getAddress() , incoming.getPort());
		                mSocket.send(dp);
		                break;
		    		}
		    		default:{
		    			break;
		    		}
		    		}
	            }
	        }
	         
	        catch(IOException e)
	        {
				//mObservable.informUser(e.toString());
	        }
		}
	}
}
