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
		//openMulticastSocket();	
		
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
			new Thread(new WorkerRunnable(clientSocket)).start();
		}
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
		} catch (IOException e) {
			throw new RuntimeException("Cannot open port: " + this.serverPort, e);
		}
	}
	
	private void openMulticastSocket() throws IOException{
		int mcPort = 7;
		String mcIPStr = "230.1.1.1"; //adres grupy multicastowej
		MulticastSocket mcSocket = null;
		InetAddress mcIPAddress = null;
		mcIPAddress = InetAddress.getByName(mcIPStr);
		mcSocket = new MulticastSocket(mcPort);
		System.out.println("Multicast Receiver running at:" + mcSocket.getLocalSocketAddress());
		mcSocket.joinGroup(mcIPAddress);
		mcSocket.setReuseAddress(true);

		DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);

		System.out.println("Waiting for a  multicast message...");
		int time = 0;
		do {
			mcSocket.receive(packet);
			String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
			System.out.println("[Multicast  Receiver] Received:" + msg);
			time++;
		}while(time < 100);
		mcSocket.leaveGroup(mcIPAddress);
		mcSocket.close();
	}
	}

