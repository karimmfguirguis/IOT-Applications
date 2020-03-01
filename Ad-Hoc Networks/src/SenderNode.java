import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class SenderNode {
	public static void main(String[] args) throws SocketException, IOException {
		System.out.println("Node started");
		int port = 5030;
		DatagramSocket socket = new DatagramSocket(port);
		InetAddress remoteAddress;
		byte[] buffer = "HelloWorld!".getBytes();
		DatagramPacket packet= new DatagramPacket(buffer, buffer.length);
		socket.setBroadcast(true);
		ArrayList<String> receivedMessages = new ArrayList<String>();
		receivedMessages.add("HelloWorld!");
		
		//initiates flooding
		System.out.println("initiating flooding");
		DatagramPacket floodingPacket = new DatagramPacket( buffer, buffer.length, InetAddress.getByName("192.168.210.255"), port); 
		long startTime = System.currentTimeMillis();
		long latency;
		socket.send(floodingPacket);
		while (true) {
			
			try{
				//receive packets
				socket.receive(packet);
				latency = System.currentTimeMillis() - startTime;
				System.out.println("Message Received from: " + packet.getAddress().toString() + " Latency: "+latency);
				buffer = packet.getData(); 
				String msg = new String(buffer);
				
				//Broadcast message if it was never received before
				if(!receivedMessages.contains( msg ) ) {
					receivedMessages.add(msg);
					DatagramPacket sendPacket = new DatagramPacket( buffer, buffer.length, InetAddress.getByName("192.168.210.255"), port); 
					socket.send(sendPacket);
				}
				
				
				
			}
			catch(IOException e){
				System.out.println(e);
				break;
			}
		}
		
		socket.close();
		
		
	}
}