package edu.yu.cs.com3800.stage5;

import edu.yu.cs.com3800.Vote;
import edu.yu.cs.com3800.ZooKeeperPeerServer;
import java.net.InetSocketAddress;
import java.util.*;

public class ZooKeeperServerCluster {

	private ArrayList<ZooKeeperPeerServerImpl> servers;
	private Map<Long,InetSocketAddress> idToPeerServer;
	private long numServers;
	private int numObservers;
	private InetSocketAddress leader = null;
	private List<GatewayPeerServerImpl> gatewayPeerServers;
	private GatewayServer gatewayServer;
	private int port;
	private boolean running;

	/**
	 * Create a cluster of i-ZooKeeperServers and have them elect a leader
	 * @param numServers - number of servers to start up
	 * @param port - starting port of first server. port numbers increase by 10 for each subsequent server
	 */
	public ZooKeeperServerCluster(long numServers, int port, int numberObservers, int httpPort) {
		if (numberObservers < 0 || numServers < 1) throw new IllegalArgumentException();
		this.running = false;
		this.numServers = numServers;
		this.port = port;
		this.servers = new ArrayList<>();
		this.idToPeerServer = new HashMap<>();
		this.numObservers = numberObservers;
		this.gatewayPeerServers = new ArrayList<>(numObservers);

		for (Long j = 0L; j < numServers; j++, this.port+=10) {
			InetSocketAddress address = new InetSocketAddress("localhost", this.port);
			idToPeerServer.put(j, address);
		}
		int serversCreated = 0;

		for (Map.Entry<Long,InetSocketAddress> entry: idToPeerServer.entrySet()) {
			InetSocketAddress address = entry.getValue();
			Long id = entry.getKey();
			HashMap<Long, InetSocketAddress> map = new HashMap<>(idToPeerServer);
			map.remove(id);

			if (serversCreated < numServers - numObservers) {
				ZooKeeperPeerServerImpl server = new ZooKeeperPeerServerImpl(address.getPort(), 0L, id, map, numObservers);
				this.servers.add(server);
				serversCreated++;
			}
			else if (serversCreated == numServers - 1){
				this.gatewayServer = new GatewayServer(httpPort, address.getPort(), 0L, id, map, numObservers);
				GatewayPeerServerImpl gatewayPeerServer = gatewayServer.getGatewayPeerServer();
				this.servers.add(gatewayPeerServer);
				this.gatewayPeerServers.add(gatewayPeerServer);
				serversCreated++;
			}
			else {
				GatewayPeerServerImpl gatewayPeerServer = new GatewayPeerServerImpl(address.getPort(), 0L, id, map, numObservers);
				this.servers.add(gatewayPeerServer);
				this.gatewayPeerServers.add(gatewayPeerServer);
				serversCreated++;
			}
		}

		servers.forEach(server -> gatewayPeerServers.forEach(g -> server.addObserverID(g.getServerId())));

		if (numObservers > 0) System.out.println("Cluster Created with " + numServers + " servers (Number of non-Observers: " + (numServers-numberObservers) + ". Number of votes to reach quorum: " + (((numServers-numberObservers)/2)+1) + ")\n");
	}

	protected List<ZooKeeperPeerServerImpl> getServers() {
		return this.servers;
	}

	protected List<GatewayPeerServerImpl> gatewayPeerServers() {
		return this.gatewayPeerServers;
	}

	protected void addServerToCluster(ZooKeeperPeerServerImpl server) {
		for(ZooKeeperPeerServerImpl s: servers) {
			if(s != null) {
				s.getPeers().put(server.getServerId(), server.getAddress());
			}
		}
		servers.add(server);
		numServers++;
		if (server instanceof GatewayPeerServerImpl) {
			gatewayPeerServers.add((GatewayPeerServerImpl) server);
			numObservers++;
		}
		if (!server.isAlive()) server.start();
	}

	public int getClusterSize() {
		return this.servers.size();
	}

	protected void startAndPrintLeaders() {
		startCluster();
		printLeaders();
	}

	protected void startCluster() {
		if (this.running) return;
		System.out.println("--- Starting Up ZooKeeperPeerServer Cluster ---");
		for (ZooKeeperPeerServerImpl server: this.servers) {
			if (!server.isAlive()) server.start();
		}
		this.running = true;
		if (!this.gatewayServer.isAlive()) this.gatewayServer.start();

		while(getLeader() == null) {
			try{
				Thread.sleep(250 * numServers);
			} catch (Exception e) {}
		}
	}

	protected void shutDownCluster() {
		if (!this.running) return;
		System.out.println("--- Shutting Down ZooKeeperPeerServer Cluster ---");
		for (ZooKeeperPeerServerImpl server : this.servers) {
			server.shutdown();
		}
		this.gatewayServer.shutdown();
		this.running = false;
	}

	protected void printLeaders() {

		try{ Thread.sleep(250 * numServers); } catch (Exception e) {}

		System.out.println("\n---------  ZooKeeper Server Cluster and Leader Election Results  ---------\n");
		ZooKeeperPeerServerImpl proposedLeader = null;
		for (ZooKeeperPeerServerImpl server : this.servers) {
			if (server != null) {
				Vote leader = server.getCurrentLeader();
				if (leader != null) {
					if (server.getPeerState() == ZooKeeperPeerServer.ServerState.LEADING) {
						if (server.getServerId() == leader.getProposedLeaderID()) {
							proposedLeader = server;
							this.leader = server.getAddress();
						}
					}
					if (server.getPeerState() == ZooKeeperPeerServer.ServerState.FOLLOWING) {
						if (proposedLeader != null && proposedLeader.getServerId() != leader.getProposedLeaderID()) {
							System.out.println("Proposed LeaderID: " + proposedLeader.getServerId() + " | Voted LeaderID: " + leader.getProposedLeaderID());
							System.out.println("Illegal Argument Exception: Split brain occurred during election.");
						}
					}
					System.out.println("Server on port " + server.getAddress().getPort() + " whose ID is " + String.format("%02d", server.getServerId()) + " has the following ID as its leader: " + String.format("%02d", leader.getProposedLeaderID()) + " and its state is " + server.getPeerState().name());
				}
			}
		}
		System.out.println("");
	}

	protected InetSocketAddress getLeaderAddress() {
		for (ZooKeeperPeerServerImpl server: servers) {
			if (server != null) {
				if (server.getPeerState() == ZooKeeperPeerServer.ServerState.LEADING) {
					return server.getAddress();
				}
			}
		}
		return null;
	}

	protected ZooKeeperPeerServerImpl getLeader() {
		for (ZooKeeperPeerServerImpl server: servers) {
			if (server != null) {
				if (server.getPeerState() == ZooKeeperPeerServer.ServerState.LEADING) {
					return server;
				}
			}
		}
		return null;
	}

}
