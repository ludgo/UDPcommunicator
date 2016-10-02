package pks;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Server {
	
	DatagramSocket mSocket = null;
	String mPort;
	
	public Server(String port) {
		mPort = port;
	}
	
	public void launch() {
		try
        {
            //1. creating a server socket, parameter is local port number
			mSocket = new DatagramSocket(Integer.parseInt(mPort));
             
            //buffer to receive incoming data
            byte[] buffer = new byte[65536];
            DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
             
            //2. Wait for an incoming data
            System.out.println("Server socket created. Waiting for incoming data...");
             
            //communication loop
            while(true)
            {
            	mSocket.receive(incoming);
                byte[] data = incoming.getData();
                String s = new String(data, 0, incoming.getLength());
                 
                //echo the details of incoming data - client ip : client port - client message
                System.out.println(incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + s);
                 
                s = "OK : " + s;
                DatagramPacket dp = new DatagramPacket(s.getBytes() , s.getBytes().length , incoming.getAddress() , incoming.getPort());
                mSocket.send(dp);
            }
        }
         
        catch(IOException e)
        {
            System.err.println("IOException " + e);
        }
	}

}