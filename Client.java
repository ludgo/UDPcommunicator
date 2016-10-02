package pks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client extends Thread {
	
	DatagramSocket mSocket = null;
	String mIpAddress;
	String mPort;
    String s;

	public Client(String ipAddress, String port) {
		mIpAddress = ipAddress;
		mPort = port;
	}
	
	public void run() {
		connect();
    }
	
	public void connect() {
		BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));
        
        try
        {
        	mSocket = new DatagramSocket();
             
            InetAddress host = InetAddress.getByName(mIpAddress);
             
            while(true)
            {
                //take input and send the packet
            	System.out.println("Enter message to send : ");
                s = (String)cin.readLine();
                byte[] b = s.getBytes();
                 
                DatagramPacket  dp = new DatagramPacket(
                		b , b.length , host , Integer.parseInt(mPort));
                mSocket.send(dp);
                 
                //now receive reply
                //buffer to receive incoming data
                byte[] buffer = new byte[65536];
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                mSocket.receive(reply);
                 
                byte[] data = reply.getData();
                s = new String(data, 0, reply.getLength());
                 
                //echo the details of incoming data - client ip : client port - client message
                System.out.println(reply.getAddress().getHostAddress() + " : " + reply.getPort() + " - " + s);
            }
        }
         
        catch(IOException e)
        {
            System.err.println("IOException " + e);
        }
	}

}
