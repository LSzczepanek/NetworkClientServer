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
import java.net.UnknownHostException;
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
	private static int attempts = 0;

	public static void main(String[] args) throws IOException {

		if (tryToConnectToServer()) {
				do {
					if (serverStatusCheck(ClientHelper.getServerAddress(), clientSocket.getPort())) {
						startSendingRandomNumbers();
					} else {
						chooseAfterLostConnection();
					}
				} while (!(msg.equals("close")));

				fromServer.close();
				output.close();
				clientSocket.close();
			
		}
	}

	
	
	
	private static boolean tryToConnectToServer(){
		if (makeConnection(checkSave())) {
			System.out.println("Connecting to saved server succeed!!");
			return true;
		
		}
		else if(makeConnection(false)){
			System.out.println("Connected with the server!!");
			return true;
		}else{
			System.out.println("All connections failed, closing the Client!");
			return false;
		}
	}
	
	
	private static boolean checkSave() {
		String[] lastSave = ClientHelper.readFromFile();

		if (lastSave != null) {

			System.out.println("Saved nickname is: " + lastSave[0]);
			System.out.println("Saved SocketAddress is: " + lastSave[1] + ":" + lastSave[2]);
			try {
				if (ClientHelper.loadSave(lastSave)) {
					nickname = lastSave[0];
					return true;
				}
			} catch (IOException e) {
				System.out.println("Something went wrong while loading settings...");
				return false;
			}
		}
		return false;
	}

	private static void chooseAfterLostConnection() throws IOException {

		System.out.println("Do you want try to recconnect to the last sever type y?"
				+ "\nIf you want look for new servers type n");
		ClientHelper.cleanSystemInStream();
		int choice = ((int) System.in.read());
		if (choice == ClientHelper.Y) {
			clientSocket.close();
			System.out.println("Tries to reconnect...");
			makeConnection(true);
		} else if (choice == ClientHelper.N) {
			ClientHelper.cleanSystemInStream();
			if (!makeConnection(false)) {
				attempts++;
			} else {
				attempts = 0;
			}
			if (attempts >= 3) {
				System.out.println(
						"After 3rd failed attempt clinet is going to be closed!!! \nSeems like there are not anymore any servers!!!");
				msg = "close";
				return;
			}
		} else {
			System.out.println("You typed wrong!! Choose again!!");
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

	/**
	 * 
	 * Making connection.
	 * 
	 * @param isReconnect
	 *            when true connection will try to reconnect insted of making
	 *            new connection
	 * @return true if connection will pass
	 */
	private static boolean makeConnection(boolean isReconnect) {

		Scanner input = new Scanner(System.in);
		clientSocket = new Socket();

		boolean serverAcceptance;

		if (isReconnect) {
			connectionAddressToServer = ClientHelper.reconnectToLastServer();
			serverAcceptance = false;

		} else {
			ClientHelper.clearConnections();
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

			if (isReconnect) {
				System.out.println("Reconnect succeed");
			}
			if (!isReconnect) {
				ClientHelper.saveLastServer(nickname);
			}
			return true;
		} catch (Exception e) {
			if (isReconnect) {
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
				ClientHelper.cleanSystemInStream();
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
			ClientHelper.cleanSystemInStream();
			frequency = input.nextInt();
			if (frequency < 10 || frequency > 10000) {
				System.out.println("Czestotliwosc spoza zakresu!");
			}
		} while (frequency < 10 || frequency > 10000);
	}

	private static void startSendingRandomNumbers() {
		msg = Integer.toString(((int) (Math.random() * 100)));
		output.println(msg);
		output.flush();
		System.out.println("Msg sent: " + msg);

		try {
			Thread.sleep(frequency);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
