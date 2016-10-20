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
		waitForClient();
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
			new Thread(new ServerServiceForClient(clientSocket)).start();
		}
	}

	/**
	 * Opens MulticastSocket on another Thread and listening for DISCOVER message
	 * 
	 */
	
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
				System.out.println(nickname+" connected to the server!");
				success = true;
			}else{
				success = false;
			}
		}
		return success;
	}

	
	/**
     * If Client will dc his nickname will be realeased
     * 
     * 
     */
	static protected void removeNickname(String nickname) {
		synchronized (listOfNicknames) {
			Server.listOfNicknames.remove(nickname);
			System.out.println("Nick: "+nickname+" has been released");
		}

	}

	
	/**
	 * Checks client nickname already exist on server
	 * @param nickname
	 * @return true if nickname is already taken on server
	 */
	
	static private boolean checkNickname(String nickname) {
		if (listOfNicknames.contains(nickname)) {

			return true;
		}
		return false;
	}
}
