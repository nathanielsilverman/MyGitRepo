package edu.yu.cs.com3800.stage5;

import java.io.*;
import java.net.*;
import java.util.*;
import edu.yu.cs.com3800.*;
import java.util.logging.*;
import java.util.concurrent.*;
import com.sun.net.httpserver.*;
import static java.util.logging.Level.*;
import static edu.yu.cs.com3800.ZooKeeperPeerServer.ServerState.*;

public class ZooKeeperPeerServerImpl extends Thread implements ZooKeeperPeerServer  {

    private final InetSocketAddress myAddress; // Address of Server
    private final int myPort; // Port # server is running on for UDP message senders/receivers
    private final Long id; // Server ID in cluster
    private Long peerEpoch;

    private volatile boolean shutdown;
    private volatile ServerState state;
    private volatile Vote currentLeader;
    private Map<Long,InetSocketAddress> peerIDtoAddress;
    private ServerRole role; // LEADER/FOLLOWER/OBSERVER
    private int numObservers;
    private Set<Long> observers = new HashSet<>();

    // UDP Message Sender/Receiver
    private UDPMessageSender udpSender;
    private UDPMessageReceiver udpReceiver;

    // Incoming and Outgoing Message Queues
    private LinkedBlockingQueue<Message> outgoingMessages, incomingElectionMessages, incomingGossipMessages;
    // Heartbeat Gossip Monitor
    private HeartBeatGossipMonitor heartBeatMonitor;
    private Set<InetSocketAddress> deadPeers;
    private Set<Long> deadPeersIDs;

    // Logger-related variables
    private Logger logger;
    private String logFileDir;

    private HttpServer gossipMsgAPI;

    public ZooKeeperPeerServerImpl(int myPort, long peerEpoch, Long id, Map<Long,InetSocketAddress> peerIDtoAddress) {
        this.myPort = myPort;
        this.peerEpoch = peerEpoch;
        this.id = id;
        this.peerIDtoAddress = new ConcurrentHashMap<>(peerIDtoAddress);
        this.myAddress = new InetSocketAddress("localhost", myPort);
        this.numObservers = 1;
        init();
    }

    public ZooKeeperPeerServerImpl(int myPort, long peerEpoch, Long id, Map<Long,InetSocketAddress> peerIDtoAddress, int numObservers) {
        this.myPort = myPort;
        this.peerEpoch = peerEpoch;
        this.id = id;
        this.peerIDtoAddress = new ConcurrentHashMap<>(peerIDtoAddress);
        this.myAddress = new InetSocketAddress("localhost", myPort);
        this.numObservers = numObservers;
        init();
    }

    public ZooKeeperPeerServerImpl(int myPort, long peerEpoch, Long id, Map<Long,InetSocketAddress> peerIDtoAddress, long gatewayID, int numObservers) {
        this.myPort = myPort;
        this.peerEpoch = peerEpoch;
        this.id = id;
        this.peerIDtoAddress = new ConcurrentHashMap<>(peerIDtoAddress);
        this.myAddress = new InetSocketAddress("localhost", myPort);
        this.observers = new HashSet<>();
        this.observers.add(gatewayID);
        this.numObservers = numObservers;
        init();
    }

    public ZooKeeperPeerServerImpl(int myPort, long peerEpoch, Long id, Map<Long,InetSocketAddress> peerIDtoAddress, Set<Long> observers) {
        this.myPort = myPort;
        this.peerEpoch = peerEpoch;
        this.id = id;
        this.peerIDtoAddress = new ConcurrentHashMap<>(peerIDtoAddress);
        this.myAddress = new InetSocketAddress("localhost", myPort);
        this.observers = observers;
        this.numObservers = this.observers.size();
        init();
    }

    @Override
    public void run(){
        if (!initLoggers()) return;
        this.shutdown = false;
        startThreads();
        this.logger.fine("Peers:" + peerIDtoAddress.keySet().toString());

        try {
            while (!shutdown){
                try {
                    switch (getPeerState()) {
                        case LOOKING: // Start leader election, set leader to the election winner
                            electLeader();
                            break;
                        case LEADING: // Assume the responsibility as LEADER - Master Thread assigning tasks to worker threads (FOLLOWING)
                            lead(); // RoundRobinLeader
                            break;
                        case FOLLOWING: // Assume the role of worker thread. Run task assigned by LEADER
                            follow(); // JavaRunnerFollower
                            break;
                        case OBSERVER:
                            if (getCurrentLeader() == null) electLeader();
                            break;
                        default:
                    }
                } catch (Exception e){
                    if (!isInterrupted()) logger.log(WARNING, Util.getStackTrace(e));

                }
            }
        }
        catch (Exception e) {
            this.logger.severe(this.logger.getName() + " is offline \n" + Util.getStackTrace(e));
        }
        this.logger.log(Level.WARNING,"Shutting down...");
        this.logger.log(Level.SEVERE,"Exiting ZooKeeperPeerServerImpl.run()...");
    }

    @Override
    public void shutdown(){
        this.shutdown = true;
        if (this.heartBeatMonitor != null) this.heartBeatMonitor.shutdown();
        this.interrupt();
        if (this.udpSender != null) this.udpSender.shutdown();
        if (this.udpReceiver != null) this.udpReceiver.shutdown();
        if (this.role != null) role.shutdown();
        if (this.gossipMsgAPI != null) this.gossipMsgAPI.stop(0);
    }

    @Override
    public synchronized void setCurrentLeader(Vote v) {
        this.currentLeader = v;
        if (this.currentLeader != null) this.logger.info(this.getClass().getSimpleName() + "-ID-" + getServerId() + " found current Leader: " + v.toString());
    }

    @Override
    public Vote getCurrentLeader() { return this.currentLeader; }

    @Override
    public void sendMessage(Message.MessageType type, byte[] messageContents, InetSocketAddress target) {
        if (type != null && messageContents != null && target != null && !target.equals(myAddress)) this.outgoingMessages.offer(new Message(type, messageContents, this.myAddress.getHostString(), myPort, target.getHostName(), target.getPort()));
    }

    @Override
    public void sendBroadcast(Message.MessageType type, byte[] messageContents) {
        this.logger.info(this.logger.getName() + " is broadcasting " + type + " messages to all ZooKeeperPeerServers in the cluster");
        for (InetSocketAddress target: this.peerIDtoAddress.values())
            if (!isPeerDead(target)) sendMessage(type, messageContents, target);
    }

    // ****************************** //

    @Override
    public Long getServerId() { return this.id; }

    @Override
    public long getPeerEpoch() { return this.peerEpoch; }

    @Override
    public ServerState getPeerState() { return this.state; }

    @Override
    public void setPeerState(ServerState newState) { this.state = newState; }

    @Override
    public InetSocketAddress getAddress() { return this.myAddress; }

    @Override
    public int getUdpPort() { return this.myPort; }

    @Override
    public InetSocketAddress getPeerByID(long peerId) { return this.peerIDtoAddress.get(peerId); }

    @Override // Number of peerServers, plus itself, minus number of observers
    public int getQuorumSize() { return ((this.peerIDtoAddress.size() - this.numObservers + 1) / 2) + 1; } // accurately return quorum size accounting for servers in Observing State

    @Override
    public synchronized boolean isPeerDead(long peerID) { return this.deadPeersIDs.contains(peerID); }

    @Override
    public synchronized boolean isPeerDead(InetSocketAddress address) { return this.deadPeers.contains(address); }

    @Override
    public Logger getLogger() { return this.logger; }

    @Override
    public void setLoggerLevel(Level level) { this.logger.setLevel(level); }

    // ****************************** //

    protected void addObserverID(Long id) { if (this.peerIDtoAddress.containsKey(id)) observers.add(id); }

    protected boolean isObserver(Long id) { return observers.contains(id); }

    @Override
    public synchronized void reportFailedPeer(long peerID) {
        if (this.peerIDtoAddress.containsKey(peerID)) {
            String failureMessage = String.format("%02d", id) + ": no heartbeat from server " + String.format("%02d", peerID) + " - server failed";
            if (this.heartBeatMonitor != null) this.heartBeatMonitor.getLogger().log(Level.SEVERE, failureMessage);
            this.logger.warning(failureMessage);
            System.out.print(failureMessage + " -- [" + Util.getDate() + "]\n");
            hasLeaderFailed(peerID); // If reported failed peer is leader. switch state and log appropriately
            InetSocketAddress addr = this.peerIDtoAddress.remove(peerID);
            this.deadPeers.add(addr);
            this.deadPeersIDs.add(peerID);
        }
    }

    private void hasLeaderFailed(long reportedFailedPeer) {
        if (currentLeader != null) {
            long leaderID = currentLeader.getProposedLeaderID();
            if (leaderID == reportedFailedPeer) {
                setCurrentLeader(null);
                this.peerEpoch += 1;
                ServerState oldState = getPeerState();
                setPeerState(LOOKING);
                if (oldState != this.state) {
                    String leaderFailed = String.format("%02d", id) + ": switching from " + oldState + " to " + this.state;
                    this.logger.warning(leaderFailed);
                    System.out.print(leaderFailed + " --- [" + Util.getDate() + "]\n");
                }
            }
        }
    }

    protected Map<Long,InetSocketAddress> getPeers() { return this.peerIDtoAddress; }

    public String getLogFileDir() { return this.logFileDir; }

    ////  Added for Leader-Follower Thread Roles  ////
    private void lead() throws IOException {
        if (currentLeader != null && currentLeader.getProposedLeaderID() == getServerId()) {
            if (role == null) {
                try {
                    role = new RoundRobinLeader(this);
                    role.start();
                } catch (IOException e) {
                    logger.severe("Failed to create and start RoundRobinLeader \n" + Util.getStackTrace(e));
                    throw new IOException("Failed to create and start RoundRobinLeader", e);
                }
            }
            // If switching roles from Follower to Leader, i.e. this server won the Leader Election after the previous Leader failed. Close down
            if (role instanceof JavaRunnerFollower) {
                role.shutdown();
                try {
                    role = new RoundRobinLeader(this);
                    role.start();
                } catch (IOException e) {
                    logger.severe("Failed to take over as RoundRobinLeader \n" + Util.getStackTrace(e));
                    throw new IOException("Failed to take over as RoundRobinLeader", e);
                }
            }
        }
    }

    private void follow() throws IOException {
        if (currentLeader != null && currentLeader.getProposedLeaderID() != getServerId()) {
            if (role == null) {
                try {
                    role = new JavaRunnerFollower(this);
                    role.start();
                } catch (IOException e) {
                    logger.severe("Failed to create and start JavaRunnerFollower \n" + Util.getStackTrace(e));
                    throw new IOException("Failed to create and start JavaRunnerFollower", e);
                }
            }
        }
    }

    ///// ZookeeperServer Leader Election ////
    private void electLeader() {
        try {
            ZooKeeperLeaderElection leaderElection = new ZooKeeperLeaderElection(this, incomingElectionMessages);
            Vote vote = leaderElection.lookForLeader();
            if (vote == null) this.logger.warning(this.logger.getName() + " couldn't find leader");
            else setCurrentLeader(vote);
        } catch(Exception e) {
            this.logger.severe(this.logger.getName() + "\n" + Util.getStackTrace(e));
        }
    }

    // Returns Address of Leader on UDP port
    protected InetSocketAddress getAddressOfLeader() throws NoLeaderException {
        if (state == LEADING) return myAddress;
        if (this.state != LOOKING && currentLeader != null) return peerIDtoAddress.get(getLeaderID());
        throw new NoLeaderException();
    }

    protected Long getLeaderID() throws NoLeaderException {
        if (this.state != LOOKING && currentLeader != null) return currentLeader.getProposedLeaderID();
        throw new NoLeaderException();
    }

    //////// Methods for initializing ZookeeperPeerServer and Loggers ///////

    private void init() {
        this.peerIDtoAddress.remove(this.id); // If Server exists in its own map, Remove self
        setPeerState(ServerState.LOOKING);
        this.deadPeers = ConcurrentHashMap.newKeySet();
        this.deadPeersIDs = ConcurrentHashMap.newKeySet();
        this.outgoingMessages = new LinkedBlockingQueue<>();
        this.incomingElectionMessages = new LinkedBlockingQueue<>();
        this.incomingGossipMessages = new LinkedBlockingQueue<>();
    }

    private boolean initLoggers() {
        try {
            initLogging();
            String path = this.logFileDir + File.separator + "UDP-Messengers-on-port-" + this.myPort;
            initMessageReceiver(path);
            initMessageSender(path);
            initHeartBeatMonitor();
        } catch (IOException e){
            return false;
        }
        return this.logger != null;
    }

    private void initLogging() throws IOException {
        this.logFileDir = initializeServerDir(this);
        String fileName = this.getClass().getSimpleName() + "-with-ID-" + this.id + "-udp-port-" + this.myPort;
        this.logger = initializeLogging(this.logFileDir, fileName);
    }

    private void initMessageReceiver(String path) throws IOException {
        try {
            this.udpReceiver = new UDPMessageReceiver(this.incomingElectionMessages, this.incomingGossipMessages, this.myAddress, this.myPort, this, path);
        } catch (IOException e) {
            this.logger.warning(this.logger.getName() + " failed to create UDP message receiver thread");
            throw new IOException();
        }
    }

    private void initMessageSender(String path) throws IOException {
        try {
            this.udpSender = new UDPMessageSender(this.outgoingMessages, myPort, path);
        } catch (IOException e) {
            this.logger.warning(this.logger.getName() + " failed to created UDP message sender thread");
            throw new IOException();
        }
    }

    private void initHeartBeatMonitor() throws IOException {
        try {
            this.heartBeatMonitor = new HeartBeatGossipMonitor(this, incomingGossipMessages);
        } catch (IOException e) {
            this.logger.warning(this.logger.getName() + " failed to created HeartbeatMonitor thread");
            throw new IOException();
        }
    }

    private void initApiGossipService() throws IOException {
        this.gossipMsgAPI = HttpServer.create(new InetSocketAddress("localhost", this.myPort+1), 0);
        this.gossipMsgAPI.createContext("/gossip-log-file", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                if (httpExchange != null) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(heartBeatMonitor.getGossipMessagesFile().getPath());
                    builder.append('\n');
                    String s = builder.toString();
                    byte[] bytes = s.getBytes();
                    httpExchange.sendResponseHeaders(200, bytes.length);
                    OutputStream os = httpExchange.getResponseBody();
                    os.write(bytes);
                    os.close();
                }
            }
        });
        this.gossipMsgAPI.createContext("/gossip-messages", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                if (httpExchange != null) {
                    StringBuilder builder = new StringBuilder();
                    builder.append("\n");
                    builder.append(heartBeatMonitor.getGossipMessages().toString());
                    String s = builder.toString();
                    byte[] bytes = s.getBytes();
                    httpExchange.sendResponseHeaders(200, bytes.length);
                    OutputStream os = httpExchange.getResponseBody();
                    os.write(bytes);
                    os.close();
                }
            }
        });
        if (this.gossipMsgAPI != null) this.gossipMsgAPI.start();
    }

    private void startThreads() {
        this.logger.info("Starting up " + this.getClass().getSimpleName() + " UDP (Message Receiver)/(Message Sender) threads");
        if (this.udpReceiver != null) this.udpReceiver.start();
        if (this.udpSender != null) this.udpSender.start();
        this.logger.info("Starting up " + this.getClass().getSimpleName() + " HeartBeat Monitor threads");
        if (this.heartBeatMonitor != null) this.heartBeatMonitor.start();
        try { initApiGossipService(); } catch (IOException e) { this.logger.warning(this.getClass().getSimpleName() + " failed to start Gossip Service API"); }
    }
}
