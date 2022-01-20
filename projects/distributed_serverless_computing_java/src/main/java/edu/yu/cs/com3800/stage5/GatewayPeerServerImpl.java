package edu.yu.cs.com3800.stage5;


import java.net.*;
import java.util.*;
import edu.yu.cs.com3800.*;
import java.util.Map.Entry;

public class GatewayPeerServerImpl extends ZooKeeperPeerServerImpl {

	private static final ServerState state = ServerState.OBSERVER;

	public GatewayPeerServerImpl(int myPort, Long peerEpoch, Long id, Map<Long, InetSocketAddress> peerIDtoAddress, int numObservers) {
		super(myPort, peerEpoch, id, peerIDtoAddress, numObservers);
		super.setPeerState(state);
	}

	@Override
	public ServerState getPeerState() { return ServerState.OBSERVER; }

	@Override
	public void setPeerState(ServerState newState) { /* No-op because GatewayPeerServerImpl will always be in Observing state */ }

	protected boolean hasStates() { return getCurrentLeader() != null; }

	protected String getStates() {
		StringBuilder builder = new StringBuilder();
		builder.append("\nZooKeeper PeerServer States:\n");
		builder.append("============================\n");

		Vote leader = getCurrentLeader();

		long leaderID = (leader == null ? -1 : leader.getProposedLeaderID());

		for (Entry<Long,InetSocketAddress> entry: getPeers().entrySet()) {
			long id = entry.getKey();
			InetSocketAddress address = entry.getValue();
			builder.append("ZooKeeperPeerServerImpl-ID-" + id + " on-udp-port-" + address.getPort() + " STATE: ");
			builder.append((leaderID != -1 ? // If leaderId != -1 then Leader exists
							(id == leaderID ? ServerState.LEADING : ServerState.FOLLOWING) // if id == leaderID -> LEADING, else FOLLOWING
							: ServerState.LOOKING)); // if leaderID == -1 -> LOOKING
			builder.append("\n");
		}
		builder.append(getClass().getSimpleName() + "-ID-" + getServerId() + " on-udp-port-" + getUdpPort() + " STATE: " + state + "\n");
		return builder.toString();
	}
}
