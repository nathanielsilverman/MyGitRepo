package edu.yu.cs.com3800.stage5;

import java.io.*;
import java.nio.*;
import java.util.*;
import edu.yu.cs.com3800.*;
import java.util.logging.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static java.util.logging.Level.*;

public class HeartBeatGossipMonitor extends Thread implements LoggingServer {

	public static final int GOSSIP = 3000;
	public static final int FAIL = GOSSIP * 10;
	public static final int CLEANUP = FAIL * 2;
	public static final int LONG = 8; // long is 8 bytes
	public static final int INT = 4; // int is 4 bytes

	private final ZooKeeperPeerServerImpl server;
	private final Long serverID;
	private final HeartBeatData heartBeat;

	private final Map<Long, HeartBeatData> heartbeatsTable; // Maps id to HeartBeatData
	private LinkedBlockingQueue<Message> incomingGossipMessages;
	private LinkedList<GossipMessage> gossipMessages;
	private Queue<Long> peers;
	private File gossipMessagesDir;
	private Logger logger, gossipMessagesReceived;

	private volatile boolean shutdown;
	private Thread gossipSender;

	public HeartBeatGossipMonitor(ZooKeeperPeerServerImpl server, LinkedBlockingQueue<Message> incomingGossipMessages) throws IOException {
		this.server = server;
		this.serverID = server.getServerId();
		this.incomingGossipMessages = incomingGossipMessages;
		this.heartbeatsTable = new ConcurrentHashMap<>();
		this.heartBeat = new HeartBeatData(serverID, new AtomicLong());
		this.heartbeatsTable.put(serverID, heartBeat);
		this.gossipMessages = new LinkedList<>();
		this.setDaemon(true);
		initLogging();
		setName("HeartBeatMonitor-on-port-" + this.server.getUdpPort());
		randomizePeers();
		initSender();
	}

	private void initSender() {
		this.gossipSender = new Thread(new Runnable() {
			@Override
			public void run() {
				while(!shutdown) {
					try {
						Thread.sleep(GOSSIP);
						gossipTable();
					} catch (Exception e) {
						if (!shutdown) logger.warning(Util.getStackTrace(e));
					}
				}
			}
		});
	}

	public void shutdown() {
		this.shutdown = true;
		this.gossipSender.interrupt();
		interrupt();
		this.logger.log(Level.SEVERE,"Shutting down...");
	}

	@Override
	public void run() {
		this.shutdown = false;
		logger.info(HeartBeatGossipMonitor.class.getSimpleName() + "-on-server-with-ID-" + server.getServerId() + " starting GOSSIP HeartBeat Protocol...");
		this.gossipSender.start();
			while (!shutdown) {
				try {
					receiveGossipMessage();
				} catch (Exception e) {
					logger.severe(Util.getStackTrace(e));
					e.printStackTrace();
				}
			}
		this.logger.log(Level.SEVERE,"Exiting HeartBeatMonitor.run()");
		this.logger.log(INFO, "Current HeartBeat Table on Server-" + this.serverID + "\n" + heartBeatsTableToString());
	}


	private void gossipTable() {
		Long peerID;
		do {
			peerID = peers.poll();
		} while(this.server.isPeerDead(peerID));// && (!peersIDs.contains(peerID)));
		byte[] serialized = serialize();
		logger.info("Gossiping my table of heartbeats to Server-ID-" + peerID);
		this.server.sendMessage(Message.MessageType.GOSSIP, serialized, this.server.getPeerByID(peerID));
		peers.offer(peerID);
	}

	// 	Serialized HeartBeat Table Contents: ID of Server who sent Gossip Table Data, Number of HeartBeat Data Entries in the Table, serialized HeartBeat Data Entries (ServerID and HeartBeat Counter)
	private byte[] serialize() {
		/*
			Steps to serialize a Collection of Heart Beat Data into a byte[]
			1. (1 long | 8 bytes) ID of my server -> so receiving server can keep track who sent gossip data
			2. (1 int | 4 bytes) number of elements in gossip table
			3. (HeartBeatData serialized (HeartBeatData.SIZE * number elements in gossip table)
		*/

		Set<HeartBeatData> copy = new HashSet<>();
		synchronized (heartbeatsTable) {
			heartBeat.incrementHeartBeat();
			heartbeatsTable.values().forEach((v) -> copy.add(v.copy()));
		}

		int size = copy.size();
		// Long: ID of Sender,
		// (HeartBeatData.SIZE) my heartbeat data,
		// (size * HeartBeatData.SIZE) for the remaining heartbeatData entries
		ByteBuffer buffer = ByteBuffer.allocate(LONG + INT + (size * HeartBeatData.SIZE));

		buffer.putLong(this.serverID); // ID of Server who sent Gossip Table Data - ID
		buffer.putInt(size); // Number of HeartBeat Data Entries in the Table - N

		copy.forEach(hb -> buffer.put(hb.getBytes()));

		return buffer.array();
	}

	// process all received Gossip Messages
	private void receiveGossipMessage() {
		try {
			Message m = incomingGossipMessages.poll(100L, TimeUnit.MILLISECONDS);
			if (m != null) deserialize(m);
		} catch (InterruptedException e) {}
		searchForFailedPeers();
	}

	private void deserialize(Message msg) {
		/*
			Steps to deserialize a byte[] into a Collection of Heart Beat Data
			1. Read (1 long | 8 bytes) ID of my server -> so receiving server can keep track who sent gossip data
			2. Read (1 int | 4 bytes) number of elements in gossip table = N
			3. Iterate N times updating my gossip table with deserialize HeartBeatData from gossip message
		*/

		Map<Long, HeartBeatData> gossipTable = new HashMap<>();
		String wallClockTime = Util.getDate();
		ByteBuffer buffer = ByteBuffer.wrap(msg.getMessageContents());

		long senderID = buffer.getLong();
		logger.info("Received Gossip Message heartbeats from Server-ID-" + senderID);
		int numberEntries = buffer.getInt();

		for (int i = 0; i < numberEntries && buffer.remaining() >= (LONG * 2); i++) {
			long id = buffer.getLong();
			long hbCountReceived = buffer.getLong();
			gossipTable.put(id, new HeartBeatData(id, hbCountReceived, System.currentTimeMillis()));
		}

		synchronized (heartbeatsTable) {
			gossipTable.forEach((id,hb) -> {
				if (id != serverID && updatedHeartBeat(hb)) logger.log(INFO, (String.format("% 2d", this.serverID) + ": updated " + String.format("%02d", id) + "'s heartbeat sequence to " + heartbeatsTable.get(id).getHeartbeatCount() + " based on message from " + String.format("%02d", senderID) + " at node time " + hb.getTimeReceived()));
			});
		}
		GossipMessage gossipMessage = new GossipMessage(senderID, this.serverID, gossipTable, wallClockTime);
		this.gossipMessages.add(gossipMessage);
		this.gossipMessagesReceived.info(gossipMessage.toString());
	}

	private boolean updatedHeartBeat(HeartBeatData hb) {
		long id = hb.getID();
		long hbReceivedCount = hb.getHeartbeatCount();
		long timeReceived = hb.getTimeReceived();

		if (!this.server.isPeerDead(id) && id != serverID) {
			if (heartbeatsTable.containsKey(id)) {
				HeartBeatData heartBeatData = heartbeatsTable.get(id); // Get previously saved HeartBeatData Entry for that server.
				if (timedOut(heartBeatData, timeReceived)) return false;
				if (hbReceivedCount > heartBeatData.getHeartbeatCount()) { // If the entry for the node previously existed, and I received an updated HeartBeatCount for a node not marked as failed
					heartBeatData.setHeartbeatCount(hbReceivedCount);
					heartBeatData.setTimeReceived(timeReceived);
					return true;
				}
			}
			else {
				heartbeatsTable.put(id, new HeartBeatData(id, hbReceivedCount, timeReceived));
				return true;
			}
		}
		return false;
	}

	private boolean timedOut(HeartBeatData heartBeatData, long timeReceived) {
		boolean timedOut = false;
		long id = heartBeatData.getID();
		if (heartBeatData.hasFailed(timeReceived)) {
			this.logger.info("PeerID-" + id + " -- server failed");
			this.server.reportFailedPeer(id);
			timedOut = true;
		}
		if (heartBeatData.readyCleanup(timeReceived)) {
			cleanUp(id);
			timedOut = true;
		}
		return timedOut;
	}

	private void searchForFailedPeers() {
		synchronized (heartbeatsTable) {
			heartbeatsTable.forEach((id,hb) -> { if (id != serverID && !server.isPeerDead(id)) timedOut(hb, System.currentTimeMillis()); });
		}
	}

	private boolean cleanUp(long id) {
		logger.log(WARNING, ("Clean-up phase: " + String.format("%02d", serverID) + ": removed " + String.format("%02d", id) + " from its list - server failed"));
		synchronized (heartbeatsTable) {
			heartbeatsTable.remove(id);
		}
		return false;
	}

	@Override
	public Logger getLogger() { return this.logger; }

	protected synchronized LinkedList<GossipMessage> getGossipMessages() { return this.gossipMessages; }

	private void randomizePeers() {
		List<Long> list = new ArrayList<>(this.server.getPeers().keySet());
		Collections.shuffle(list);
		this.peers = new ArrayDeque<>(list);
	}

	/**
	 * initialize Logger
	 */
	private void initLogging() throws IOException {
		this.logger = initializeLogging(this.server.getLogFileDir(), HeartBeatGossipMonitor.class.getSimpleName() + "-on-server-with-ID-" + server.getServerId());
		this.gossipMessagesDir = new File(this.server.getLogFileDir() + File.separator + "GossipMessagesReceived.log");
		this.gossipMessagesReceived = initializeLogging(this.server.getLogFileDir(), "GossipMessagesReceived");
	}

	protected File getGossipMessagesFile() { return this.gossipMessagesDir; }

	private String heartBeatsTableToString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n---------------------------");
		sb.append("\n| ID |  HB  |Time Received|");
		sb.append("\n---------------------------\n");
		synchronized (heartbeatsTable) {
			heartbeatsTable.values().forEach((v) -> {
				sb.append("|");
				sb.append(String.format("% 4d", v.getID()));
				sb.append("|");
				sb.append(String.format("%06d", v.getHeartbeatCount()));
				sb.append("|");
				sb.append(((v.getID() != serverID) ? String.format("%013d", v.getTimeReceived()) : "- - - - - - -"));
				sb.append("|\n");
			});
		}
		sb.append(" ------------------\n");
		return sb.toString();
	}
}
