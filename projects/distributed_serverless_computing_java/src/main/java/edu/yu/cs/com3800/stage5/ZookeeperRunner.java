package edu.yu.cs.com3800.stage5;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ZookeeperRunner {

	public static final long gatewayServerID = 7L;
	public static final int[] ports = {8000, 8010, 8020, 8030, 8040, 8050, 8060, 8070};
	public static final long[] ids = {0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L};
	public static final int httpPort = 8888;

	private final long id;
	private final int port;

	private Map<Long,InetSocketAddress> peerIDsToAddress;

	public static void main(String[] args) {
		try {
			ZookeeperRunner runner = new ZookeeperRunner(Integer.parseInt(args[0]), Long.parseLong(args[1]));
			Thread.sleep(5000);
			runner.createRunner();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	private ZookeeperRunner(int port, long id) {
		this.id = id;
		this.port = port;
		this.peerIDsToAddress = new HashMap<>();
		for (int i = 0; i < ports.length; i++) {
			peerIDsToAddress.put(Integer.valueOf(i).longValue(), new InetSocketAddress("localhost", ports[i]));
		}
		this.peerIDsToAddress.remove(this.id);
	}

	private void createRunner() throws IOException {
		if (this.id != gatewayServerID) {
			ZooKeeperPeerServerImpl server = new ZooKeeperPeerServerImpl(port, 0, id, this.peerIDsToAddress, gatewayServerID, 1);
			server.start();
		}
		else {
			GatewayPeerServerImpl gatewayPeerServer = new GatewayPeerServerImpl(port, 0L, gatewayServerID, peerIDsToAddress, 1);
			gatewayPeerServer.start();

			GatewayServer server = new GatewayServer(httpPort, gatewayPeerServer);
			server.start();
		}
	}

	public static Process exec(int index) throws IOException {
		String javaHome = System.getProperty("java.home");
		String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
		String classpath = System.getProperty("java.class.path");
		String className = ZookeeperRunner.class.getName();
		List<String> command = new LinkedList<>();
		command.add(javaBin);
		command.add("-cp");
		command.add(classpath);
		command.add(className);

		int port = ports[index];
		long id = ids[index];

		command.add(Integer.toString(port));
		command.add(Long.toString(id));

		ProcessBuilder builder = new ProcessBuilder(command);
		Process zookeeper = builder.inheritIO().start();

		if (id == gatewayServerID)System.out.println("executing process: " + ZookeeperRunner.class.getSimpleName() + " GatewayServer-ID-" + id + "-on-port-" + port);
		else System.out.println("executing process: " + ZookeeperRunner.class.getSimpleName() + " ZooKeeperPeerServer-ID-" + id + "-on-port-" + port);
		pause();
		return zookeeper;
	}

	private static void pause(){
		try { Thread.sleep(200); } catch (InterruptedException e) {}
	}
}
