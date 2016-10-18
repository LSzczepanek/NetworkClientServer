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
		Scanner odczyt = new Scanner(System.in);
		System.out.print("Podaj nickname: ");
		String nickname = odczyt.nextLine();
		Socket clientSocket = new Socket();
		clientSocket.bind(null);
		System.out.println("Nawiazalem polaczenie: " + clientSocket.getLocalPort());
		SocketAddress test = ClientHelper.lookForServer(clientSocket);
		System.out.println("Socket Address: "+ test);
		clientSocket.connect(test);
		//clientSocket = new Socket(HOST, PORT);
		System.out.println("Nawiazalem polaczenie: " + clientSocket);

		BufferedReader input;
		input = new BufferedReader(new InputStreamReader(System.in));
		BufferedReader fromServer;
		fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		PrintWriter output;
		output = new PrintWriter(clientSocket.getOutputStream());

		output.println(nickname);

		do {
			System.out.print("<Wysylamy:> ");
			msg = input.readLine();
			output.println(msg);
			output.flush();
		} while (!(msg.equals("close")));

		odczyt.close();
		input.close();
		fromServer.close();
		output.close();
		clientSocket.close();
	}


}
