import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

public class DSRSenderNode {
	public static void main(String[] args) throws SocketException, IOException {

		System.out.println("Node started");
		int port = 5005;
		DatagramSocket socket = new DatagramSocket(port);
		socket.setBroadcast(true);
		InetAddress remoteAddress;

		// RREQ message and adding Source IP
		String destination = "192.168.210.175,";
		String RREQ = "F," + destination+ socket.getLocalAddress();
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

		while (true) {

			try {
				// receive packets
				socket.receive(packet);
				// latency = System.currentTimeMillis() - startTime;
				// System.out.println("Message Received from: " + packet.getAddress().toString()
				// + " Latency: "+latency);
				buffer = packet.getData();
				String msg = new String(buffer);
				String[] ipArr = msg.split(",");

				
				if (ipArr[0].equals("R")) {
					path = Arrays.copyOfRange(ipArr, 2, ipArr.length - 1);
					System.out.println("Path is "+path+"\n\n");
															
				}

			} catch (IOException e) {
				System.out.println(e);
				break;
			}
		}

		socket.close();

	}
}