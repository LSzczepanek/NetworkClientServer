package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
	public static final int PORT = 9000;
	public static final String HOST = "127.0.0.1";

	public static void main(String[] args) throws IOException {
		String msg = null;

		Socket clientSocket;
		clientSocket = new Socket(HOST, PORT);
		System.out.println("Nawiazalem polaczenie: " + clientSocket);

		BufferedReader input;
		input = new BufferedReader(new InputStreamReader(System.in));
		BufferedReader fromServer;
		fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		PrintWriter output;
		output = new PrintWriter(clientSocket.getOutputStream());
		
		System.out.print("Podaj nickname: ");
		String nickname = input.readLine();
		output.println(nickname);
		
		
		do {
			System.out.print("<Wysylamy:> ");
			msg = input.readLine();
			output.println(msg);
			output.flush();
		} while (!(msg.equals("close")));
		
		
		input.close();
		fromServer.close();
		output.close();
		clientSocket.close();
	}
}
