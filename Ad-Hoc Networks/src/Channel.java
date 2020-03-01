import java.io.*;
import java.net.*; 

public class Channel implements Runnable {
	private DatagramSocket socket;
	private boolean running;
    private InetAddress address;
    
    public void bind(int portNumber) throws SocketException
    {
    	socket = new DatagramSocket(portNumber) ;
    }
    
    public void start() {
    	Thread thread = new Thread(this);
    	thread.start();    	
    }
    
    public void close() {
    	running = false;
    	socket.close();
    }
    
	@Override
	public void run() {
		byte[] buffer = new byte[256];
		DatagramPacket packet= new DatagramPacket(buffer, buffer.length);
		running = true;
		
		while(running) {
			try
			{
				socket.receive(packet);
				
			}
			catch(IOException e) {
				break;
			}
		}
		
	}
}