package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class ClientHelper {

	private static int mcPort = 12345;
	private static String mcIPStr = "230.1.1.1"; // adres grupy multicastowej
	private static MulticastSocket mcSocket = null;
	private static InetAddress mcIPAddress = null;
	private static SocketAddress result;
	private static ArrayList<ListHelper<String, InetAddress, Integer>> serversResponses = new ArrayList<>();

	static SocketAddress lookForServer(Socket clientSocket) {
		createAndOpenMulticastSocket();
		sendDiscover();
		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		waitForResponses(clientSocket);
		return result;
	}

	private static void createAndOpenMulticastSocket() {

		try {
			mcIPAddress = InetAddress.getByName(mcIPStr);
			mcSocket = new MulticastSocket(mcPort);
			mcSocket.joinGroup(mcIPAddress);
			mcSocket.setReuseAddress(true);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void sendDiscover() {
		DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
		DatagramSocket socket = null;

		try {
			socket = new DatagramSocket();
			socket.setBroadcast(true);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String characters = "DISCOVER";
		byte[] data; // = new byte[characters.length()];
		data = characters.getBytes(Charset.forName("UTF-8"));
		DatagramPacket dp = null;

		try {
			dp = new DatagramPacket(data, data.length, InetAddress.getByName("255.255.255.255"), 12345);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			socket.send(dp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private static void waitForResponses(Socket clientSocket) {
		int time = 0;
		String[] parametersFromServer;
		DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);

		do {
			try {
				mcSocket.receive(packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
			if (!msg.equalsIgnoreCase("discover")) {
				System.out.println("[Multicast  Receiver] Odpowiedz " + msg);
				
				parametersFromServer = msg.split(",");
				for (String x : parametersFromServer)
				System.out.println(x);
				try {
					serversResponses.add(new ListHelper<String, InetAddress, Integer>(parametersFromServer[0],
							InetAddress.getByName(parametersFromServer[1]), Integer.parseInt(parametersFromServer[2])));

				} catch (NumberFormatException | UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Check: " + serversResponses);
				time++;
			}
		} while (time < 100);

		try {
			mcSocket.leaveGroup(mcIPAddress);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		result = new InetSocketAddress(packet.getAddress(), 9000);

		mcSocket.close();
	}
	// return result;
}
