package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Runnable {

	protected int serverPort = 0;
	protected ServerSocket serverSocket = null;
	protected boolean isStopped = false;
	protected Thread runningThread = null;
	protected String serverName;
	protected static ArrayList<String> listOfNicknames;

	public Server(int port, String serverName) {
		this.serverPort = port;
		this.serverName = serverName;
		Server.listOfNicknames = new ArrayList<String>();
	}

	public void run() {
		synchronized (this) {
			this.runningThread = Thread.currentThread();
		}
		openServerSocket();
		openMulticastSocket();
		// System.out.println("Adres serwera: "+serverSocket.getInetAddress());
		waitForClient();
		// openMulticastSocket();

		System.out.println("Server Stopped in run.");
	}

	private synchronized boolean isStopped() {
		return this.isStopped;
	}

	public synchronized void stop() {
		this.isStopped = true;
		try {
			this.serverSocket.close();
		} catch (IOException e) {
			throw new RuntimeException("Error closing server", e);
		}
	}

	private void openServerSocket() {
		try {
			this.serverSocket = new ServerSocket(this.serverPort);
			System.out.println("Serwer running at: " + serverSocket);
		} catch (IOException e) {
			throw new RuntimeException("Cannot open port: " + this.serverPort, e);
		}
	}

	private void waitForClient() {
		while (!isStopped()) {
			Socket clientSocket = null;
			try {
				// System.out.println("Waiting for client");
				clientSocket = this.serverSocket.accept();
			} catch (IOException e) {
				if (isStopped()) {
					System.out.println("Server Stopped in waitForClient.");
					return;
				}
				throw new RuntimeException("Error accepting client connection", e);
			}
			new Thread(new ClientService(clientSocket)).start();
		}
	}

	private void openMulticastSocket() {
		new Thread(new MulticastReceiver(this.serverPort, this.serverName, this.serverSocket.getInetAddress())).start();
	}

	
	
	/**
     * Adds nickname to list of current online nicknames
     * 
     * @return true if succed and false when failed
     */
	static protected boolean addNickname(String nickname) {
		
		boolean success;
		synchronized (Server.listOfNicknames) {
			if (!checkNickname(nickname)) {
				Server.listOfNicknames.add(nickname);
				for (String x : Server.listOfNicknames) {
					System.out.println("Nickname add: " + x);
					
				}
				success = true;
			}else{
				success = false;
			}
		}
		return success;
	}

	static protected void removeNickname(String nickname) {
		synchronized (listOfNicknames) {
			Server.listOfNicknames.remove(nickname);
			for (String x : Server.listOfNicknames) {
				System.out.println("Nickname remove: " + x);
			}
		}

	}

	static private boolean checkNickname(String nickname) {
		if (listOfNicknames.contains(nickname)) {

			return true;
		}
		return false;
	}
}
