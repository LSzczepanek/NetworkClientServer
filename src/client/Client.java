package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
import java.util.Scanner;

public class Client {

	public static void main(String[] args) throws IOException {
		String msg = null;
		Scanner input = new Scanner(System.in);
		System.out.print("Podaj nickname: ");
		String nickname = input.nextLine();
		Socket clientSocket = new Socket();
		int frequency;
		clientSocket.bind(null);
		System.out.println("Nawiazalem polaczenie: " + clientSocket.getLocalPort());
		SocketAddress test = ClientHelper.lookForServer(clientSocket);
		System.out.println("Socket Address: " + test);
		clientSocket.connect(test);
		// clientSocket = new Socket(HOST, PORT);
		System.out.println("Nawiazalem polaczenie: " + clientSocket);

		BufferedReader fromServer;
		fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		PrintWriter output;
		output = new PrintWriter(clientSocket.getOutputStream());

		output.println(nickname);
		do {
			System.out.println("Wybierz czestotliwosc wysylania liczb od 10-10 000: ");

			frequency = input.nextInt();
			if (frequency < 10 || frequency > 10000) {
				System.out.println("Czestotliwosc spoza zakresu!");
			}
		} while (frequency < 10 || frequency > 10000);

		do {

			// System.out.print("<Wysylamy:> ");
			msg = Integer.toString(((int) (Math.random() * 100)));
			// msg = input.readLine();
			output.println(msg);
			output.flush();

			try {
				Thread.sleep(frequency);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while (!(msg.equals("close")));

		input.close();
		fromServer.close();
		output.close();
		clientSocket.close();
	}

}
