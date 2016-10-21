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
public class ServerServiceForClient implements Runnable {

	protected Socket clientSocket = null;
	protected String serverText = null;
	private BufferedReader input;
	private PrintWriter output;

	private String clientNickname = null;

	private static final long SECOND_IN_MILISECONDS = 1000;

	public ServerServiceForClient(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public void run() {

		try {
			input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			output = new PrintWriter(clientSocket.getOutputStream());

			clientNickname = input.readLine();

			if (clientNickname != null) {
				getClientName();
				communicationWithClient();
				
				Server.removeNickname(clientNickname);
			}
			clientSocket.close();
			input.close();
		} catch (IOException e ) {
			System.out.println("Someone interrupted connection!!");

		}
	}

	boolean isTheRealClient(String nickname) {
		if (nickname == null) {
			return false;
		} else {
			return true;
		}

	}

	private void getClientName() throws IOException {
		boolean isClientNeedNewNickname = false;
		do {
			if (isClientNeedNewNickname) {
				clientNickname = input.readLine();
			}
			if (Server.addNickname(clientNickname)) {
				output.println("Succed");
				output.flush();
				isClientNeedNewNickname = false;
			} else {
				output.println("Failed");
				output.flush();
				isClientNeedNewNickname = true;
			}
		} while (isClientNeedNewNickname);
	}

	
	private void communicationWithClient(){
		String msgFromClient = "close";
		do {
			while (isTheRealClient(clientNickname)) {
				try {
					msgFromClient = input.readLine();
					System.out.println(String.format("<%s:> " + msgFromClient, clientNickname));

				} catch (SocketException e) {
					long start = System.currentTimeMillis();
					long estimated;
					do {
						System.out.println(String.format("<%s:> Lost connection...", clientNickname));
						estimated = System.currentTimeMillis() - start;
						try {
							Thread.sleep(SECOND_IN_MILISECONDS);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					} while (estimated < SECOND_IN_MILISECONDS * 5);
					msgFromClient = "close";
					break;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} while (!(msgFromClient.equals("close")));
	}
}
