package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

/**

 */
public class ClientService implements Runnable {

	protected Socket clientSocket = null;
	protected String serverText = null;

	public ClientService(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public void run() {
		BufferedReader input;
		try {
			input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			// PrintWriter output = new
			// PrintWriter(clientSocket.getOutputStream());

			String str = "close";
			String clientNickname = null;
			clientNickname = input.readLine();
			do {
				while (isTheClient(clientNickname)) {
					try {
						str = input.readLine();
						System.out.println(String.format("<%s:> " + str, clientNickname));
					} catch (SocketException e) {
						System.out.println(String.format("<%s:> Lost connection...", clientNickname));
						str = "close";
						break;
					}
				}
			} while (!(str.equals("close")));
			// System.out.println("Request processed: " + time);
			clientSocket.close();
			input.close();
		} catch (IOException e) {
			// report exception somewhere.
			e.printStackTrace();

		}
	}

	boolean isTheClient(String nickname) {
		if (nickname == null) {
			return false;
		} else {
			return true;
		}

	}
}
