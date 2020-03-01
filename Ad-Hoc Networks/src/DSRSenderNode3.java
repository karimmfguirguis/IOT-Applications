import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

public class DSRSenderNode3 {
	public static void main(String[] args) throws SocketException, IOException {

		System.out.println("Node started");
		int port = 5030;
		DatagramSocket socket = new DatagramSocket(port);
		socket.setBroadcast(true);
		InetAddress remoteAddress;

		// RREQ message and adding Source IP
		String destination = "192.168.210.175,";
		String RREQ = "F," + destination+ "192.168.210.162,";
		System.out.println("Initial Message " + RREQ);
		byte[] buffer = RREQ.getBytes();
		String[] path = new String[5];


		// initiates flooding
		System.out.println("initiating flooding");

		DatagramPacket floodingPacket = new DatagramPacket(buffer, buffer.length,
				InetAddress.getByName("192.168.210.255"), port);
		socket.send(floodingPacket);

		// Initialize packet received from other nodes
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		byte[] receivedBuffer= new byte[256];

		while (true) {

			try {
				// receive packets
				socket.receive(packet);
				receivedBuffer = packet.getData();
				System.out.println("Buffer2: "+receivedBuffer.toString());
				String msg = new String(receivedBuffer);
				msg = msg+",";
				String[] ipArr = msg.split(",");
				

				
				if (ipArr[0].equals("R")) {
					//path = Arrays.copyOfRange(ipArr, 2, ipArr.length - 1);
					for (int i = 1; i < ipArr.length; i++) {
						path[i - 1] = ipArr[i];
					}
					//System.out.println("Received Message Path is: "+msg);
					System.out.println("Path is ");
					for(int i =0;i<path.length;i++)
						System.out.println(path[i]);
					socket.setBroadcast(false);
					msg = "M," + "Hello!";
					buffer = msg.trim().getBytes();
					DatagramPacket messagePacket = new DatagramPacket(buffer, buffer.length,
							InetAddress.getByName(path[1]), port);					
															
				}

			} catch (IOException e) {
				System.out.println(e);
				break;
			}
		}

		socket.close();

	}
}