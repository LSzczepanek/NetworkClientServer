package server;

public class StartServer {

	public static void main(String[] args) {
		
		
		
		Server server = new Server(9000, "server1");
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
