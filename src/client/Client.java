package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.rmi.ServerRuntimeException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Client {

	private static String msg = null;
	private static String nickname = null;
	private static Socket clientSocket = null;
	private static BufferedReader fromServer = null;
	private static PrintWriter output = null;
	private static int frequency;
	static String serverAnswer;
	private static SocketAddress connectionAddressToServer;

	public static void main(String[] args) throws IOException {

		int attempts = 0;
		// newConnection();
		if (connection(false)) {

			do {
				if (serverStatusCheck(ClientHelper.getServerAddress(), clientSocket.getPort())) {

					startSendingRandomNumbers();

				} else {
					System.out.println("Do you want try to recconnect to the last sever type y?"
							+ "\nIf you want look for new servers type something n");
					System.in.read(new byte[System.in.available()]);
					int choice = ((int) System.in.read());
					if (choice == 121) {
						clientSocket.close();
						System.out.println("Tries to reconnect...");

						 connection(true);
//						reconnect();

					} else if (choice == 110) {
						System.in.read(new byte[System.in.available()]);
						// newConnection();
						if (!connection(false)) {
							attempts++;
						} else {
							attempts = 0;
						}

						if (attempts > 3) {
							System.out.println(
									"After 3rd failed attempt clinet is going to be closed!!! \nSeems like there are not anymore any servers!!!");
							return;
						}
					} else {
						System.out.println("You typed wrong!! Choose again!!");
					}
				}

			} while (!(msg.equals("close")));

			fromServer.close();
			output.close();
			clientSocket.close();
		}
	}

	public static boolean serverStatusCheck(InetAddress serverAddress, int port) {
		boolean isOnline = true;

		try {

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

	private static boolean connection(boolean isReconnect) {

		Scanner input = new Scanner(System.in);
		clientSocket = new Socket();

		boolean serverAcceptance;

		if (isReconnect) {
			connectionAddressToServer = ClientHelper.reconnectToLastServer();
			serverAcceptance = false;

		} else {
			System.out.println("Client starts searching for servers...");
			connectionAddressToServer = ClientHelper.lookForServer();
			serverAcceptance = true;
		}

		try {
			clientSocket.connect(connectionAddressToServer);

			fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			output = new PrintWriter(clientSocket.getOutputStream());

			setNickname(input, serverAcceptance);
			setFrequency(input);

			if(isReconnect){
				System.out.println("Reconnect succeed");
			}
			return true;
		} catch (Exception e) {
			if(isReconnect){
				System.out.println("Reconnect failed");
			}
			System.out.println("Error while making a connection!!!");
			return false;
		}

	}

	static void setNickname(Scanner input, boolean serverAcceptance) throws IOException {
		do {

			if (serverAcceptance) {
				System.out.print("Type your nickname: ");
				System.in.read(new byte[System.in.available()]);
				nickname = input.nextLine();
			}
			output.println(nickname);
			output.flush();
			serverAnswer = fromServer.readLine();

			if (serverAnswer.equalsIgnoreCase("Succed")) {
				System.out.println("Nickname accepted");
				serverAcceptance = false;
			} else {
				System.out.println("Nickname rejected\nPut another nickname");
				serverAcceptance = true;
			}
		} while (serverAcceptance);
	}

	static void setFrequency(Scanner input) throws IOException {
		do {

			System.out.println("Wybierz czestotliwosc wysylania liczb od 10-10 000: ");
			System.in.read(new byte[System.in.available()]);
			frequency = input.nextInt();
			if (frequency < 10 || frequency > 10000) {
				System.out.println("Czestotliwosc spoza zakresu!");
			}
		} while (frequency < 10 || frequency > 10000);
	}

	private static void newConnection() {
		ClientHelper.clearConnections();
		Scanner input = new Scanner(System.in);
		boolean serverAcceptance;
		// input.reset();

		clientSocket = new Socket();

		System.out.println("Client starts searching for servers...");
		SocketAddress connectionAddressToServer = ClientHelper.lookForServer();

		try {
			clientSocket.connect(connectionAddressToServer);
			System.out.println("Nawiazalem polaczenie: " + clientSocket);

			fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			output = new PrintWriter(clientSocket.getOutputStream());

			do {
				System.out.print("Podaj nickname: ");
				System.in.read(new byte[System.in.available()]);
				nickname = input.nextLine();

				output.println(nickname);
				output.flush();
				serverAnswer = fromServer.readLine();

				if (serverAnswer.equalsIgnoreCase("Succed")) {
					System.out.println("Nickname accepted");
					serverAcceptance = false;
				} else {
					System.out.println("Nickname rejected\nPut another nickname");
					serverAcceptance = true;
				}
			} while (serverAcceptance);
			System.out.println(serverAnswer);

		} catch (IOException | IllegalArgumentException e) {
			System.out.println("Couldn't connect to server!");
		}

		do {

			System.out.println("Wybierz czestotliwosc wysylania liczb od 10-10 000: ");

			frequency = input.nextInt();
			if (frequency < 10 || frequency > 10000) {
				System.out.println("Czestotliwosc spoza zakresu!");
			}
		} while (frequency < 10 || frequency > 10000);
	}

	private static void reconnect() {

		Scanner input = new Scanner(System.in);
		boolean serverAcceptance = false;
		clientSocket = new Socket();
		SocketAddress connectionAddressToServer = ClientHelper.reconnectToLastServer();
		System.out.println("Socket Address: " + connectionAddressToServer);
		try {
			// clientSocket.bind(null);
			try {
				clientSocket.connect(connectionAddressToServer);
			} catch (ConnectException e) {
				System.out.println("Error while reconnecting!!!");
			}

			System.out.println("Nawiazalem polaczenie: " + clientSocket);
			if (clientSocket.isConnected()) {
				fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

				output = new PrintWriter(clientSocket.getOutputStream());

				do {

					if (serverAcceptance) {
						System.out.print("Podaj nickname: ");
						System.in.read(new byte[System.in.available()]);
						nickname = input.nextLine();
					}
					output.println(nickname);
					output.flush();
					serverAnswer = fromServer.readLine();

					if (serverAnswer.equalsIgnoreCase("Succed")) {
						System.out.println("Nickname accepted");
						serverAcceptance = false;
					} else {
						System.out.println("Nickname rejected\nPut another nickname");
						serverAcceptance = true;
					}
				} while (serverAcceptance);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void startSendingRandomNumbers() {
		msg = Integer.toString(((int) (Math.random() * 100)));
		output.println(msg);
		output.flush();
		System.out.println("Msg sent: "+msg);

		try {
			Thread.sleep(frequency);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
