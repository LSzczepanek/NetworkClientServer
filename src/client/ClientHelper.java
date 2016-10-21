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

	private static int groupPort = 12345;
	private static String groupIPStr = "230.1.1.1"; // adres grupy multicastowej
	private static MulticastSocket mcSocket = null;
	private static InetAddress groupIPAddress = null;
	private static InetAddress serverAddress = null;
	private static SocketAddress result;
	private static ArrayList<ListHelper<String, InetAddress, Integer>> serversResponses = new ArrayList<>();
	public final static long SECOND_IN_NANOSECONDS = 1000000000;

	static InetAddress getServerAddress() {
		return serverAddress;
	}

	static void clearConnections() {
		mcSocket = null;
		groupIPAddress = null;
		serverAddress = null;
		result = null;
		serversResponses.clear();
	}

	static SocketAddress reconnectToLastServer() {
		return result;
	}

	static SocketAddress lookForServer() {
		createAndOpenMulticastSocket();
		sendDiscover();
		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		waitForResponses();
		return result;
	}

	private static void createAndOpenMulticastSocket() {

		try {
			groupIPAddress = InetAddress.getByName(groupIPStr);
			mcSocket = new MulticastSocket(groupPort);
			mcSocket.joinGroup(groupIPAddress);
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
			String characters = "DISCOVER";
			byte[] data; 
			data = characters.getBytes(Charset.forName("UTF-8"));
			DatagramPacket dp = null;
			dp = new DatagramPacket(data, data.length, InetAddress.getByName("255.255.255.255"), groupPort);
			socket.send(dp);
		} catch (IOException e) {
			System.out.println("Something went wrong while sending DISCOVER message");
			e.printStackTrace();
		}
	}

	private static void waitForResponses() {

		
		DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
	
		getListOfOnlineServers(packet);
		chooseAndConnectToServer(packet);
		serversResponses.clear();
		try {
			mcSocket.leaveGroup(groupIPAddress);
		} catch (IOException e) {
			System.out.println("Problem with leaving multicast group");
			e.printStackTrace();
		}

		mcSocket.close();
	}
	
	
	private static void getListOfOnlineServers(DatagramPacket packet){
		String[] parametersFromServer;
		String msg;
		String olderMsg = "null";
		long startTime = System.nanoTime();
		long estimatedTime = 0;
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
				try {
					serversResponses.add(new ListHelper<String, InetAddress, Integer>(parametersFromServer[0],
							InetAddress.getByName(parametersFromServer[1]), Integer.parseInt(parametersFromServer[2])));

				} catch (NumberFormatException | UnknownHostException e) {
					System.out.println("Something went wrong while getting list of servers");
					e.printStackTrace();
				}

			}
			estimatedTime = System.nanoTime() - startTime;
			System.out.println("Actual Time used is: " + ((double) estimatedTime / SECOND_IN_NANOSECONDS));
		} while (estimatedTime < 5 * SECOND_IN_NANOSECONDS);
	}
	
	
	private static void chooseAndConnectToServer(DatagramPacket packet){
		boolean exit = false;
		do {
			exit = true;
			if (serversResponses.size() != 0) {
				for (int i = 0; i < serversResponses.size(); i++) {
					System.out.printf("Serwer nr%d: " + serversResponses.get(i) + "\n", i + 1);
				}
				System.out.println("Choose server to connect by number: ");

				try {
					System.in.read(new byte[System.in.available()]);
					byte option = (byte) ((byte) ((int) System.in.read()) - 49);
					result = new InetSocketAddress(packet.getAddress(), serversResponses.get(option).getPort());
					serverAddress = serversResponses.get(option).getInetAddress();
				} catch (IOException | IndexOutOfBoundsException e) {
					System.out.println("Wrong server number!\nTry once again!");
					exit = false;
					
				}
			} else {

				System.out.println("There is no servers avalaible at the moment");
				result = null;
			}

		} while (!exit);
	}
	// return result;
}
