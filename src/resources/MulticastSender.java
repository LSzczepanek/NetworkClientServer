package resources;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

public class MulticastSender {

	public static void main(String[] args) {

		InetAddress ia = null;
		int port = 12345;
		byte ttl = 1;
		String characters = "My msg\n";
		byte[] data; //= new byte[characters.length()];
		data = characters.getBytes(Charset.forName("UTF-8"));

		// read the address from the command line
		try {
			try {
				ia = InetAddress.getByName("230.1.1.1");
			} catch (UnknownHostException e) {
				// ia = InetAddressFactory.newInetAddress(args[0]);
			}
			
		} catch (Exception e) {
			
			e.printStackTrace();
			System.err.println("Usage: java MulticastSender MulticastAddress port");
			System.exit(1);
		}

		//characters.getBytes(0, characters.length(), data, 0);
		//DatagramPacket dp = new DatagramPacket(data, data.length, ia, port);

		try {
			MulticastSocket ms = new MulticastSocket();
			ms.joinGroup(ia);
			for (int i = 0; i < 10; i++) {
				String characters1 = ("My msg number: "+i);
				System.out.println(characters1);
				byte[] data1 = characters1.getBytes(Charset.forName("UTF-8"));
				DatagramPacket dp = new DatagramPacket(data1, data1.length, ia, port);
				ms.send(dp, ttl);
				System.out.println("Msg sent, closing");
			}
			//System.out.println("Msg sent, closing");
			ms.leaveGroup(ia);
			ms.close();
		} catch (SocketException se) {
			System.err.println(se);
			se.printStackTrace();
		} catch (IOException ie) {
			System.err.println(ie);
			ie.printStackTrace();
		}

	}

}
	