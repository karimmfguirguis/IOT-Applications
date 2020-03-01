import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

public class DSRNode {
	public static void main(String[] args) throws SocketException, IOException {

		// array to save sequence of Backward propagation
		String[] path = new String[5];

		System.out.println("Node started..");
		int port = 5005;
		DatagramSocket socket = new DatagramSocket(port);
		socket.setBroadcast(true);
		// InetAddress remoteAddress;

		boolean receivedMessageFlag = false;

		byte[] buffer = new byte[256];

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
				System.out.println("Message Received from: " + packet.getAddress().toString());
				System.out.println("Received Message is: "+msg);
				
				String[] ipArr = msg.split(",");
				System.out.println("Flag is: " + ipArr[0]);
				
				// check if Flooding
				if (ipArr[0].equals("F")) {
					// Broadcast message if it was never received before
					if (!receivedMessageFlag) {
						receivedMessageFlag = true;
						// Add my own IP
						msg = msg + "," + socket.getLocalAddress();
						System.out.println("New message received and added my address: "+msg);
						// check if I am destination to initiate reply to sender
						if (ipArr[1].equals(socket.getLocalAddress())) {
							System.out.println("Destination reached..replying to sender");
							msg = "R," + msg.substring(18, msg.length() - 1); // destination address field(16 character+
																				// comma=17) is removed
							System.out.println("Reply message is "+msg);
							int index = 0;
							while (!ipArr[index].equals(socket.getLocalAddress())) {
								index++;
							}

							// Save path cache
							path = Arrays.copyOfRange(ipArr, 2, ipArr.length - 1);
							System.out.println("Path is "+path);
							socket.setBroadcast(false);
							//update message and destination IP
							buffer = msg.getBytes();
							DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length,
									InetAddress.getByName(ipArr[index - 1]), port);
							socket.send(sendPacket);
						} else {
							System.out.println("Continue route discovery"+"\n\n");
							buffer = msg.getBytes();
							DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length,
									InetAddress.getByName("192.168.210.255"), port);
							socket.send(sendPacket);
						}
					}
				} else if (ipArr[0].equals("R")) {
					
					socket.setBroadcast(false);

					// Save path cache
					path = Arrays.copyOfRange(ipArr, 2, ipArr.length - 1);
					System.out.println("Replying to sender");
					System.out.println("Path is "+path+"\n\n");
					int index = 0;
					while (!ipArr[index].equals(socket.getLocalAddress())) {
						index++;
					}
					buffer = msg.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length,
							InetAddress.getByName(ipArr[index - 1]), port);
					socket.send(sendPacket);
				} else if (ipArr[0].equals("M")) {
					/*TODO
					 * implement the message response
					 */
				}

			} catch (IOException e) {
				System.out.println(e);
				break;
			}
		}

		socket.close();

	}
}