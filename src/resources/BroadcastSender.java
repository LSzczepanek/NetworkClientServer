package resources;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;

public class BroadcastSender {
	
	public static void main(String[] args) {
		DatagramSocket socket;
		
		try {
			socket = new DatagramSocket();
			socket.setBroadcast(true);
		
		
		String characters = "My broadcast message\n";
		byte[] data; //= new byte[characters.length()];
		data = characters.getBytes(Charset.forName("UTF-8"));
		DatagramPacket dp = null;
		
			dp = new DatagramPacket(data, data.length, InetAddress.getByName("255.255.255.255"), 12345);
		
		
			socket.send(dp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	

}
