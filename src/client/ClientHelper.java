package client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ClientHelper {

	private static int groupPort = 12345;
	private static String groupIPStr = "230.1.1.1"; // adres grupy multicastowej
	private static MulticastSocket mcSocket = null;
	private static InetAddress groupIPAddress = null;
	private static InetAddress serverAddress = null;
	private static int serverPort = 0;
	private static SocketAddress result;
	private static ArrayList<ListHelper<String, InetAddress, Integer>> serversResponses = new ArrayList<>();
	public final static long SECOND_IN_NANOSECONDS = 1000000000;
	static final int Y = 121;
	static final int N = 110;

	static InetAddress getServerAddress() {
		return serverAddress;
	}

	static void clearConnections() {
		mcSocket = null;
		groupIPAddress = null;
		serverAddress = null;
		serverPort = 0;
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

	private static void getListOfOnlineServers(DatagramPacket packet) {
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

	private static void chooseAndConnectToServer(DatagramPacket packet) {
		boolean exit = false;
		do {
			exit = true;
			if (serversResponses.size() != 0) {
				for (int i = 0; i < serversResponses.size(); i++) {
					System.out.printf("Serwer nr%d: " + serversResponses.get(i) + "\n", i + 1);
				}
				System.out.println("Choose server to connect by number: ");

				try {
					ClientHelper.cleanSystemInStream();
					byte option = (byte) ((byte) ((int) System.in.read()) - 49);
					serverAddress = serversResponses.get(option).getInetAddress();
					serverPort = serversResponses.get(option).getPort();
					result = new InetSocketAddress(serverAddress, serverPort);

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

	static void cleanSystemInStream() throws IOException {
		System.in.read(new byte[System.in.available()]);
	}

	static void saveLastServer(String nickname) throws IOException {

		String[] toSave = { nickname, serverAddress.toString(), String.valueOf(serverPort) };
		FileWriter lastConnection = new FileWriter("LastConnection.txt");
		BufferedWriter bufferedWriter = new BufferedWriter(lastConnection);

		try {
			for (String argument : toSave) {
				bufferedWriter.write(argument);
				bufferedWriter.newLine();
			}
		} finally {
			bufferedWriter.close();
		}

		System.out.println("File has been saved");
	}

	static String[] readFromFile() {
		try {
			File file = new File("LastConnection.txt");
			if (file.exists()) {

				System.out.println("Saved file with last server exist!");
				Scanner read = new Scanner(file);
				String[] lastSave = new String[3];

				for (int i = 0; i < lastSave.length; i++) {
					ClientHelper.cleanSystemInStream();
					lastSave[i] = read.nextLine();
				}
				return lastSave;
			} else {
				System.out.println("There is no last save connection!");
				return null;
			}
		} catch (IOException e) {
			System.out.println("Error while reading a file!");
			return null;
		}
	}

	static boolean loadSave(String[] lastSave) throws IOException {

		boolean isSucceed = false;
		int choice = 0;
		try {
			serverAddress = InetAddress.getByName(lastSave[1].replace("/", ""));
			serverPort = Integer.parseInt(lastSave[2]);
			result = new InetSocketAddress(serverAddress, serverPort);
		} catch (NumberFormatException | UnknownHostException e) {
			e.printStackTrace();
		}
		System.out.println("Last time you connected with this server");
		System.out.println("Do you want to reconnect to it? Type y or n");
		
		do {
			ClientHelper.cleanSystemInStream();
			choice = ((int) System.in.read());
			if (choice == ClientHelper.Y) {
				System.out.println("Tries to reconnect...");
				isSucceed = true;
			} else if (choice == ClientHelper.N) {
				ClientHelper.cleanSystemInStream();
				isSucceed = false;
			} else {
				System.out.println("You typed wrong!! Choose again!!");
			}
		} while (!(choice == ClientHelper.Y ^ choice == ClientHelper.N));
		return isSucceed;

	}

}
