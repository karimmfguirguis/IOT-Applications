import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class node {
	public static void main(String[] args) throws SocketException, IOException {
		
		int port = 5030;
		DatagramSocket socket = new DatagramSocket(port);
		InetAddress remoteAddress;
		byte[] buffer = new byte[256];
		DatagramPacket packet= new DatagramPacket(buffer, buffer.length);
		socket.setBroadcast(true);
		ArrayList<String> receivedMessages = new ArrayList<String>();
		System.out.println("Node started");
		
		
		while (true) {
			
			try{
				//receive packets
				socket.receive(packet);
				System.out.println("Message Received from: " + packet.getAddress().toString());
				buffer = packet.getData();
				String msg = new String(buffer);
				
				//Broadcast message if it was never received before
				if(!receivedMessages.contains( msg )) {
					
					System.out.println(msg);
					receivedMessages.add(msg);
					
					DatagramPacket sendPacket = new DatagramPacket( buffer, buffer.length, InetAddress.getByName("192.168.210.255"), port); 
					socket.send(sendPacket);
				}
				
				
				
			}
			catch(Exception e){
				System.out.println(e);
				break;
			}
		}
		
		socket.close();
		
		
	}
}