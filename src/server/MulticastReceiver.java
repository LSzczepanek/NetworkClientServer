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
	/* ww w .j av a 2 s .c o m */
	public MulticastReceiver(int serverPort, String serverName, InetAddress serverSocket) {
		this.serverPort = serverPort;
		this.serverName = serverName;
		this.serverSocket = serverSocket;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		int mcPort = 12345;
		String mcIPStr = "230.1.1.1"; // adres grupy multicastowej
		MulticastSocket mcSocket = null;
		InetAddress mcIPAddress = null;
		try {
			mcIPAddress = InetAddress.getByName(mcIPStr);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Ustawilem IP na: " + mcIPAddress);
		try {
			mcSocket = new MulticastSocket(mcPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Postawilem Mullticast Socket");
		System.out.println("Multicast Receiver running at:" + mcSocket.getLocalSocketAddress());
		try {
			mcSocket.joinGroup(mcIPAddress);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Dolaczylem do grupy");
		try {
			mcSocket.setReuseAddress(true);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Adres moze byc uzyty ponownie");

		DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);

		System.out.println("Waiting for a  multicast message...");
		int time = 0;
		do {
			try {
				mcSocket.receive(packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
			
			if (msg.equals("DISCOVER")) {
			System.out.println("[Multicast  Receiver] Client nas szuka: " + msg);
			

			DatagramSocket udpSocket = null;
			try {
				udpSocket = new DatagramSocket();
			} catch (SocketException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String testAddress = serverName +","+serverSocket.getHostAddress()+","+serverPort;
			byte[] msg2 = testAddress.getBytes();
			DatagramPacket packet2 = new DatagramPacket(msg2, msg2.length);
			packet2.setAddress(mcIPAddress);
			packet2.setPort(mcPort);
			try {
				udpSocket.send(packet2);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			udpSocket.close();
			
			}
			time++;
		} while (time < 100);
		try {
			mcSocket.leaveGroup(mcIPAddress);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mcSocket.close();
	}
} 
