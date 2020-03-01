import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

public class DSRNode2 {
	public static void main(String[] args) throws SocketException, IOException {

		// array to save sequence of Backward propagation
		String[] path = new String[5];

		System.out.println("Node started..");
		int port = 5030;
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
				
			
				buffer = packet.getData();
				String msg = new String(buffer);
				System.out.println("Message Received from: " + packet.getAddress().toString());
				System.out.println("Received Message is: " + msg);

				String[] ipArr = msg.split(",");
				System.out.println("Flag is: " + ipArr[0]);

				// check if Flooding
				if (ipArr[0].equals("F")) {
					// Broadcast message if it was never received before
					if (!receivedMessageFlag) {
						receivedMessageFlag = true;
						// Add my own IP
						msg = msg.trim() + "192.168.210.177" + ",";
						
						System.out.println("New message received and added my address: " + msg);
						// check if I am destination to initiate reply to sender
						if (ipArr[1].equals("192.168.210.177")) {
							System.out.println("Destination reached..replying to sender");
							System.out.println("Message length "+msg.length());
							msg = "R," + msg.substring(18, msg.length()); // destination
																				// address
																				// field(16
																				// character+
																				// comma=17)
																				// is
																				// removed
							System.out.println("Reply message is " + msg);
							ipArr = new String[5];
							ipArr = msg.split(",");
//							for(String s: ipArr){
//								System.out.print(s);
//							}
							int index;
							for(index =0;index<ipArr.length;index++){
								if(ipArr[index].equals("192.168.210.177")){
									break;
								}
							}
								
							System.out.println("INDEX: "+index);
							
							
							// Save path cache
							for (int i = 1; i < ipArr.length; i++) {
								path[i - 1] = ipArr[i];
							}
							// path = Arrays.copyOfRange(ipArr, 2, ipArr.length
							// - 1);
							System.out.println("Path is ");
							for(int i =0;i<path.length;i++)
								System.out.print(path[i]+", ");
							System.out.println();
							socket.setBroadcast(false);
							// update message and destination IP
							System.out.println("Buffer1: "+buffer.toString());
							System.out.println("Before sending: msg: " + msg);
							buffer = msg.getBytes();
							index = 2;
							DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length,
									InetAddress.getByName(ipArr[index - 1]), port);
							socket.send(sendPacket);
						} else {
							System.out.println("Continue route discovery" + "\n\n");
							buffer = msg.getBytes();
							DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length,
									InetAddress.getByName("192.168.210.255"), port);
							socket.send(sendPacket);
						}
					}
				} else if (ipArr[0].equals("R")) {

					socket.setBroadcast(false);

					// Save path cache
					for (int i = 1; i < ipArr.length; i++) {
						path[i - 1] = ipArr[i];
					}

					System.out.println("Replying to sender");
					System.out.println("Path is ");
					for(int i =0;i<path.length;i++)
						System.out.print(path[i]+", ");
					int index = 0;
					while (!ipArr[index].equals("192.168.210.177")) {
						index++;
					}
					buffer = msg.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length,
							InetAddress.getByName(ipArr[index - 1]), port);
					socket.send(sendPacket);
				} else if (ipArr[0].equals("M")) {
					
					int index;
					for(index =0;index<path.length;index++){
						if(path[index].equals("192.168.210.177")){
							break;
						}
					}	
					// check if I am destination to initiate reply to sender
					if(index < path.length -1)
					{
						if (path[index+1]==null ) {
						System.out.println("Received Message: "+ipArr[1]);
					    } 
					else {
						buffer = msg.trim().getBytes();
					    DatagramPacket messagePacket = new DatagramPacket(buffer, buffer.length,
					    InetAddress.getByName(path[index+1]), port);
					    socket.send(messagePacket);
					     }
					}else {
						System.out.println("Received Message: "+ipArr[1]);
					}
			    }
					
					
		}

			 catch (IOException e) {
				System.out.println(e);
				socket.close();
				break;
			}
		}

		

	}
}