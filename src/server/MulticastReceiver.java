package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MulticastReceiver implements Runnable {

	private int serverPort = 0;
	private String serverName = null;
	private InetAddress serverSocket = null;

	int groupPort = 12345;
	String groupIPStr = "230.1.1.1"; // adres grupy multicastowej
	MulticastSocket mcSocket = null;
	InetAddress groupIPAddress = null;

	/* ww w .j av a 2 s .c o m */
	public MulticastReceiver(int serverPort, String serverName, InetAddress serverSocket) {
		this.serverPort = serverPort;
		this.serverName = serverName;
		this.serverSocket = serverSocket;
	}

	@Override
	public void run() {

		try {
			createAndRunMulticastSocket();
			waitForDiscover();
			mcSocket.leaveGroup(groupIPAddress);
		} catch (IOException e) {
			System.out.println("Error!!! We can't listen on group!");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mcSocket.close();
	}

	void createAndRunMulticastSocket() throws IOException {

		groupIPAddress = InetAddress.getByName(groupIPStr);
		System.out.println("Address IP of multicast group: " + groupIPAddress);
		mcSocket = new MulticastSocket(groupPort);
		System.out.println("Multicast Socket is running");
		mcSocket.joinGroup(groupIPAddress);
		mcSocket.setReuseAddress(true);
		System.out.println("Multicast Receiver running at:" + mcSocket.getLocalSocketAddress());

	}

	void waitForDiscover() throws IOException {
		
		DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
		System.out.println("Waiting for a  multicast message...");
		int time = 0;
		do {
			mcSocket.receive(packet);
			String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
			if (msg.equals("DISCOVER")) {
				System.out.println("[Multicast  Receiver] We get msg: " + msg);
				DatagramSocket udpSocket = null;
				try {
					udpSocket = new DatagramSocket();
				} catch (SocketException e1) {
					System.out.println("Error while making DatagramSocket");
					e1.printStackTrace();
				}
				String testAddress = serverName + "," + serverSocket.getHostAddress() + "," + serverPort;
				byte[] msg2 = testAddress.getBytes();
				DatagramPacket packet2 = new DatagramPacket(msg2, msg2.length);
				packet2.setAddress(groupIPAddress);
				packet2.setPort(groupPort);
				udpSocket.send(packet2);
				udpSocket.close();
			}
			time++;
		} while (time < 100);
	}
}
