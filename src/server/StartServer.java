package server;

public class StartServer {

	public static void main(String[] args) {

		Server server = null;
		for (int i = 9000; i < 9005; i++) {
			String serverName = "server"+(i-9000);
			server = new Server(i, serverName);
			new Thread(server).start();
		
		try {
			Thread.sleep(5 * 60 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("Stopping Server");
		server.stop();
		}
	}

}
