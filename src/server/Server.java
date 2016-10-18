package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Server implements Runnable {

	protected int serverPort = 0;
	protected ServerSocket serverSocket = null;
	protected boolean isStopped = false;
	protected Thread runningThread = null;
	protected String serverName;

	public Server(int port, String serverName) {
		this.serverPort = port;
		this.serverName = serverName;
	}

	
	
	public void run() {
		synchronized (this) {
			this.runningThread = Thread.currentThread();
		}
		openServerSocket();
		openMulticastSocket();
		waitForClient();
		//openMulticastSocket();	
		

		System.out.println("Server Stopped.");
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
			System.out.println("Serwer running at: "+serverSocket);
		} catch (IOException e) {
			throw new RuntimeException("Cannot open port: " + this.serverPort, e);
		}
	}
	
	private void waitForClient(){
		while (!isStopped()) {
			Socket clientSocket = null;
			try {
				System.out.println("Waiting for client");
				clientSocket = this.serverSocket.accept();
			} catch (IOException e) {
				if (isStopped()) {
					System.out.println("Server Stopped.");
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
	}

