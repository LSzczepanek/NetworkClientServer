package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Client {

	private static String msg = null;
	private static String nickname = null;
	private static Socket clientSocket = null;
	private static BufferedReader fromServer = null;
	private static PrintWriter output = null;
	private static int frequency;

	public static void main(String[] args) throws IOException {

		makeConnection();

		do {
			if (serverStatusCheck(ClientHelper.getServerAddress(), clientSocket.getPort())) {

				msg = Integer.toString(((int) (Math.random() * 100)));
				output.println(msg);
				output.flush();

				try {
					Thread.sleep(frequency);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				System.out.println("Do you want try to recconnect to the last sever?");
				System.in.read(new byte[System.in.available()]);
				if (((int) System.in.read()) == 121) {
					clientSocket.close();
					System.out.println("Tries to reconnect...");
					if (reconnect()) {
						System.out.println("Succeed");
					} else {
						System.out.println("Failed");
					}
				} else {
					System.in.read(new byte[System.in.available()]);
					makeConnection();
				}
			}

		} while (!(msg.equals("close")));

		fromServer.close();
		output.close();
		clientSocket.close();
	}

	public static boolean serverStatusCheck(InetAddress serverAddress, int port) {
		boolean isOnline = true;

		try {
			System.out.println("Address to: " + serverAddress);

			Socket sock = new Socket();
			final int timeOut = (int) TimeUnit.SECONDS.toMillis(2); // 5 sec
																	// wait
																	// period
			sock.connect(new InetSocketAddress(serverAddress.getHostAddress(), port), timeOut);
			sock.close();
		} catch (Exception e) {
			System.out.println("Server is dead");
			isOnline = false;

		}
		return isOnline;
	}

	private static void makeConnection() {
		ClientHelper.clearConnections();
		Scanner input = new Scanner(System.in);
		//input.reset();
		System.out.print("Podaj nickname: ");
		nickname = input.nextLine();
		
		clientSocket = new Socket();

		System.out.println("Nawiazalem polaczenie: " + clientSocket.getLocalPort());
		SocketAddress connectionAddressToServer = ClientHelper.lookForServer();
		System.out.println("Socket Address: " + connectionAddressToServer);
		try {
			// clientSocket.bind(null);
			clientSocket.connect(connectionAddressToServer);
			System.out.println("Nawiazalem polaczenie: " + clientSocket);

			fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			output = new PrintWriter(clientSocket.getOutputStream());

			output.println(nickname);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// clientSocket = new Socket(HOST, PORT);
		do {

			System.out.println("Wybierz czestotliwosc wysylania liczb od 10-10 000: ");

			frequency = input.nextInt();
			if (frequency < 10 || frequency > 10000) {
				System.out.println("Czestotliwosc spoza zakresu!");
			}
		} while (frequency < 10 || frequency > 10000);

		//input.reset();
	}

	private static boolean reconnect() {

		clientSocket = new Socket();
		SocketAddress connectionAddressToServer = ClientHelper.reconnectToLastServer();
		System.out.println("Socket Address: " + connectionAddressToServer);
		try {
			// clientSocket.bind(null);
			clientSocket.connect(connectionAddressToServer);
			System.out.println("Nawiazalem polaczenie: " + clientSocket);

			fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			output = new PrintWriter(clientSocket.getOutputStream());

			output.println(nickname);

			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
