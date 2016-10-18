package client;

public class ListHelper<String, InetAddress, Integer> {
	    public String serverName;
	    public InetAddress inetAddress;
	    public Integer port;
		
	    public ListHelper(String serverName, InetAddress inetAddress, Integer port) {
			super();
			this.serverName = serverName;
			this.inetAddress = inetAddress;
			this.port = port;
		}

		public String getServerName() {
			return serverName;
		}

		public void setServerName(String serverName) {
			this.serverName = serverName;
		}

		public InetAddress getInetAddress() {
			return inetAddress;
		}

		public void setInetAddress(InetAddress inetAddress) {
			this.inetAddress = inetAddress;
		}

		public Integer getPort() {
			return port;
		}

		public void setPort(Integer port) {
			this.port = port;
		}

		@Override
		public java.lang.String toString() {
			return "ListHelper [serverName=" + serverName + ", inetAddress=" + inetAddress + ", port=" + port + "]";
		}
	    
	   
	
}
