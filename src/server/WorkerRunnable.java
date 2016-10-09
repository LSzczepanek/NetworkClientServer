package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**

 */
public class WorkerRunnable implements Runnable {

	protected Socket clientSocket = null;
	protected String serverText = null;

	public WorkerRunnable(Socket clientSocket, String serverText) {
		this.clientSocket = clientSocket;
		this.serverText = serverText;
	}

	public void run() {
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			PrintWriter output = new PrintWriter(clientSocket.getOutputStream());
			long time = System.currentTimeMillis();
			String str;
			String clientNickname = null;
			clientNickname = input.readLine();
			do
		      {                                                            
		      str=input.readLine();                                                    
		      System.out.println(String.format("<%s:> " + str, clientNickname)); 
		      }while(!(str.equals("close")));
			System.out.println("Request processed: " + time);
		} catch (IOException e) {
			// report exception somewhere.
			e.printStackTrace();
		}
	}
}
