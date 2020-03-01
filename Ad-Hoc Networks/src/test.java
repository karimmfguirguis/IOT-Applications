import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class test {
	public static void main(String[] args) throws SocketException, IOException {
		System.out.println("Node started");
		int port = 5005;
		DatagramSocket socket = new DatagramSocket(port);
		InetAddress remoteAddress;
		byte[] buffer = "HelloWorld!".getBytes();
		DatagramPacket packet= new DatagramPacket(buffer, buffer.length);
		socket.setBroadcast(false);
		ArrayList<String> receivedMessages = new ArrayList<String>();
		receivedMessages.add("HelloWorld!");
		
		//initiates flooding
		System.out.println("initiating testing");
		long startTime = System.currentTimeMillis();
		long latency;
		DatagramPacket Packet1 = new DatagramPacket( buffer, buffer.length, InetAddress.getByName("192.168.210.152"), port);
		DatagramPacket Packet2 = new DatagramPacket( buffer, buffer.length, InetAddress.getByName("192.168.210.177"), port);
		DatagramPacket Packet3 = new DatagramPacket( buffer, buffer.length, InetAddress.getByName("192.168.210.162"), port);
		DatagramPacket Packet4 = new DatagramPacket( buffer, buffer.length, InetAddress.getByName("192.168.210.168"), port);
		DatagramPacket Packet5 = new DatagramPacket( buffer, buffer.length, InetAddress.getByName("192.168.210.175"), port);
		socket.send(Packet1);
		socket.send(Packet2);
		socket.send(Packet3);
		socket.send(Packet4);
		socket.send(Packet5);
		while(true) {	
			try{
				//receive packets
				socket.receive(packet);
				latency = System.currentTimeMillis() - startTime;
				System.out.println("Message Received from: " + packet.getAddress().toString() + " Latency: "+latency);
				buffer = packet.getData(); 
				String msg = new String(buffer);
				System.out.println(msg);
				
			}
						
				
			
			catch(Exception e){
				System.out.println(e);
				break;
			}
		}
		
		
		socket.close();
		
		
	}
}