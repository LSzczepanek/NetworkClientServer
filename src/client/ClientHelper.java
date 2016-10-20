package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
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
	private static InetAddress serverAddress = null;
	private static SocketAddress result;
	private static ArrayList<ListHelper<String, InetAddress, Integer>> serversResponses = new ArrayList<>();
	public final static long SECOND_IN_NANOSECONDS = 1000000000;

	static InetAddress getServerAddress() {
		return serverAddress;
	}

	static void clearConnections(){
		mcSocket = null;
		mcIPAddress = null;
		serverAddress = null;
		result = null;
		serversResponses.clear();
	}
	
	static SocketAddress reconnectToLastServer(){
		return result;
	}
	
	static SocketAddress lookForServer() {
		createAndOpenMulticastSocket();
		sendDiscover();
		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		waitForResponses();
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
	private static void waitForResponses() {

		String[] parametersFromServer;
		DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
		String msg;
		String olderMsg = "null";

		long startTime = System.nanoTime();
		long estimatedTime = 0;
		boolean exit = false;

		do {
			try {
				mcSocket.setSoTimeout(1000);
				mcSocket.receive(packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Waiting for responses");
				msg = "DISCOVER";
			}

			msg = new String(packet.getData(), packet.getOffset(), packet.getLength());

			if (!msg.equalsIgnoreCase("discover") && !olderMsg.equals(msg)) {
				System.out.println("[Multicast  Receiver] Odpowiedz " + msg);
				olderMsg = msg;
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

			}
			estimatedTime = System.nanoTime() - startTime;
			System.out.println("Actual Time used is: " + ((double) estimatedTime / SECOND_IN_NANOSECONDS));
		} while (estimatedTime < 5 * SECOND_IN_NANOSECONDS);

		do {
			exit = true;
			if (serversResponses.size() != 0) {
				for (int i = 0; i < serversResponses.size(); i++) {
					System.out.printf("Serwer nr%d: " + serversResponses.get(i) + "\n", i + 1);
				}
				System.out.println("Choose server to connect by number: ");

				try {
					byte option = (byte) ((byte) ((int) System.in.read()) - 49);
					result = new InetSocketAddress(packet.getAddress(), serversResponses.get(option).getPort());
					serverAddress = serversResponses.get(option).getInetAddress();
				} catch (IOException | IndexOutOfBoundsException e) {
					System.out.println("Wrong server number!\nTry once again!");
					exit = false;
					e.printStackTrace();
				}
			} else {

				System.out.println("There is no servers avalaible at the moment");
				result = null;
			}

		} while (!exit);
		serversResponses.clear();
		try {
			mcSocket.leaveGroup(mcIPAddress);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mcSocket.close();
	}
	// return result;
}
