package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;

import client.ClientHelper;

/**

 */
public class ClientService implements Runnable {

	protected Socket clientSocket = null;
	protected String serverText = null;
	private static final long SECOND_IN_MILISECONDS = 1000;

	public ClientService(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public void run() {
		BufferedReader input;
		PrintWriter output;
		try {
			input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			output = new PrintWriter(clientSocket.getOutputStream());

			String str = "close";
			String clientNickname = null;

			clientNickname = input.readLine();
			if (clientNickname != null) {
				if(Server.addNickname(clientNickname)){
					System.out.println("Succed");
				}else{
					System.out.println("failed");
				}

				do {
					while (isTheClient(clientNickname)) {
						try {
							str = input.readLine();
							System.out.println(String.format("<%s:> " + str, clientNickname));
						} catch (SocketException e) {
							long start = System.currentTimeMillis();
							long estimated;
							do {
								System.out.println(String.format("<%s:> Lost connection...", clientNickname));
								estimated = System.currentTimeMillis() - start;
								try {
									Thread.sleep(SECOND_IN_MILISECONDS);
								} catch (InterruptedException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							} while (estimated < SECOND_IN_MILISECONDS * 5);
							str = "close";
							break;
						}
					}
				} while (!(str.equals("close")));
				// System.out.println("Request processed: " + time);
				Server.removeNickname(clientNickname);
			}
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
