package resources;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastReceiver {

	/* ww w .j av a 2 s .c o m */
	public static void main(String[] args) throws Exception {
		int mcPort = 12345;
		String mcIPStr = "230.1.1.1"; //adres grupy multicastowej
		MulticastSocket mcSocket = null;
		InetAddress mcIPAddress = null;
		mcIPAddress = InetAddress.getByName(mcIPStr);
		mcSocket = new MulticastSocket(mcPort);
		System.out.println("Multicast Receiver running at:" + mcSocket.getLocalSocketAddress());
		mcSocket.joinGroup(mcIPAddress);
		mcSocket.setReuseAddress(true);

		DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);

		System.out.println("Waiting for a  multicast message...");
		int time = 0;
		do {
			mcSocket.receive(packet);
			String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
			System.out.println("[Multicast  Receiver] Received:" + msg);
			time++;
		}while(time < 100);
		mcSocket.leaveGroup(mcIPAddress);
		mcSocket.close();
	}
}
