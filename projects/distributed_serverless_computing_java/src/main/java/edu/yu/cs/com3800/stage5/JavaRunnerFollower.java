package edu.yu.cs.com3800.stage5;

import java.io.*;
import java.net.*;
import java.util.logging.*;
import edu.yu.cs.com3800.*;
import static edu.yu.cs.com3800.Util.*;
import static java.util.logging.Level.*;
import static edu.yu.cs.com3800.Message.MessageType.*;

public class JavaRunnerFollower extends ServerRole implements LoggingServer {

	private final ZooKeeperPeerServerImpl server;
	private final int tcpPort;
	private final String host;
	private Logger logger;

	private ServerSocket serverSocket; // For accepting connections from RoundRobinLeader
	private Socket socket; // connection to the RoundRobinLeader
	private Message request; // request message received from RoundRobinLeader
	private Message response; // response message to send RoundRobinLeader
	private byte[] result; // results of Compiling and Running Request
	private volatile boolean shutdown;

	public JavaRunnerFollower(ZooKeeperPeerServerImpl server) throws IOException {
		this.server = server;
		this.tcpPort = server.getUdpPort() + 2;
		this.host = server.getAddress().getHostName();
		setDaemon(true);
		setName("JavaRunnerFollower-tcp-port-" + this.tcpPort);
		initLogging((server.getLogFileDir() + File.separator + "JavaRunnerFollower-tcp-port-" + this.tcpPort), getName());
		initServerSocket();
	}

	@Override
	public void run() {
		this.logger.info("Starting JavaRunnerFollower-on-tcp-port-" + this.tcpPort + "...");
		shutdown = false;
		while (!this.isInterrupted() && !shutdown) {
			try {
				acceptConnection();
				readRequest();
				if (!handleRequest()) continue;
				processRequest();
				writeResponse();
			} catch (ShutdownException | DeadPeerException | NoLeaderException e) {
				if (e instanceof DeadPeerException) logger.warning("Dead Peer \n" + Util.getStackTrace(e));
				if (e instanceof ShutdownException) break;
			}
		}
		close();
		this.logger.log(WARNING, "JavaRunnerFollower-on-tcp-port-" + this.tcpPort + " is offline...");
	}

	private void acceptConnection() throws ShutdownException {
		if (!shutdown) {
			this.logger.info("Waiting to accept connection");
			while (!shutdown) {
				try {
					socket = serverSocket.accept(); // Accept connection from RoundRobinLeader
					this.logger.info("Accepted connection");
					return;
				} catch (IOException e) {
					if (shutdown) throw new ShutdownException();
					this.logger.warning(e.getClass().getSimpleName() + " occurred while trying to accept socket connection from RoundRobinLeader. \n" + Util.getStackTrace(e));
				} catch (Exception e) {
					if (!(e instanceof NoLeaderException)) this.logger.warning(Util.getStackTrace(e));
				}
			}
		} else throw new ShutdownException();
	}

	// Read all bytes from socket connection and construct request message from the network payload
	private void readRequest() throws DeadPeerException, ShutdownException {
		if (!shutdown) {
			this.logger.info("Reading request from socket...");
			while (!shutdown) {
				try {
					this.request = new Message(Util.readAllBytesFromNetwork(server, socket, server.getAddressOfLeader()));
					logger.info("Read Request: " + request.getRequestID() + " from RoundRobinLeader-on-tcp-" + this.request.getSenderPort() + " [" + this.request.getMessageType() + "]");
					return;
				} catch (IOException | NullPointerException | NoLeaderException e) {
					if (shutdown) throw new ShutdownException();
					if (!(e instanceof NoLeaderException || e instanceof NullPointerException)) logger.severe(e.getClass().getSimpleName() + " occurred while reading request from socket \n" + Util.getStackTrace(e));
				}
			}
		} else throw new ShutdownException();
	}

	private boolean handleRequest() throws DeadPeerException, NoLeaderException, ShutdownException {
		if (!shutdown) {
			deadPeer();
			locateLeader();
			if (request.getMessageType() == NEW_LEADER_GETTING_LAST_WORK) { // Newly elected RoundRobinLeader requesting completed work
				if (response != null && result != null) { // I have completed work to send back to RoundRobinLeader
					sendLeaderCompletedWork();
				} else { // If I have no Completed Work return no work
					sendLeaderNoWork();
				}
				return false;
			}
			return true;
		} else throw new ShutdownException();
	}

	private void locateLeader() throws ShutdownException {
		if (!shutdown) {
			InetSocketAddress sender = new InetSocketAddress(request.getSenderHost(), (request.getSenderPort() - 2));
			while (!shutdown) {
				try {
					if (server.getAddressOfLeader() != null) {
						if (sender.equals(server.getAddressOfLeader())) break;
					}
				} catch (NoLeaderException e) {}
				try { Thread.sleep(10000); } catch (InterruptedException ignored) {}
			}
		} else throw new ShutdownException();
	}

	// Compile and Run request and construct a response message with results
	private void processRequest() {
		if (!shutdown) {
			logger.info("Request: " + request.getRequestID() + " received from RoundRobinLeader-on-tcp-" + this.request.getSenderPort() + " Processing request...");
			boolean errorOccurred = false;
			try {
				String r = compileAndRun(request.getMessageContents());
				result = r.getBytes();
				logger.info("Successfully compiled and ran code");
			} catch (IOException | ReflectiveOperationException | IllegalArgumentException e) {
				result = Util.getStackTrace(e).getBytes();
				errorOccurred = true;
				logger.warning(e.getClass().getSimpleName() + " occurred while attempting to compile and run code: \n Request-" + request.getRequestID() + ": " + new String(request.getMessageContents()) + " \n Response: " + new String(result));
			}
			this.response = new Message(COMPLETED_WORK, result, host, tcpPort, request.getSenderHost(), request.getSenderPort(), request.getRequestID(), errorOccurred);
		}
	}

	private void sendLeaderCompletedWork() throws DeadPeerException, NoLeaderException, ShutdownException {
		if (!shutdown) {
			String newLeaderHost = request.getSenderHost();
			int newLeaderPort = request.getSenderPort();
			this.response = new Message(COMPLETED_WORK, response.getMessageContents(), host, tcpPort, newLeaderHost, newLeaderPort, response.getRequestID(), response.getErrorOccurred());
			logger.info("JavaRunnerFollower-on-tcp-" + tcpPort + " sending completed work, to newly-elected RoundRobinLeader-on-tcp-" + newLeaderPort);
			writeResponse();
		} else throw new ShutdownException();
	}

	private void sendLeaderNoWork() throws DeadPeerException, NoLeaderException, ShutdownException {
		if (!shutdown) {
			String newLeaderHost = request.getSenderHost();
			int newLeaderPort = request.getSenderPort();
			this.response = new Message(COMPLETED_WORK, new byte[0], host, tcpPort, newLeaderHost, newLeaderPort, -1, true);
			logger.info("JavaRunnerFollower-on-tcp-" + tcpPort + " has no completed work to newly-elected RoundRobinLeader-on-tcp-" + newLeaderPort);
			writeResponse();
		} else throw new ShutdownException();
	}

	// Write response to socket connection
	private void writeResponse() throws DeadPeerException, NoLeaderException, ShutdownException {
		if (!shutdown) {
			while (!shutdown) {
				try {
					if (server.isPeerDead(server.getLeaderID())) throw new DeadPeerException();
					writeToSocket(socket, this.response.getNetworkPayload());
					if (this.response.getRequestID() == -1) {
						logger.info("NO_COMPLETED_WORK to be sent RoundRobinLeader-on-tcp-port-" + this.response.getReceiverPort());
					} else {
						logger.info("Response (to request " + this.response.getRequestID() + ") Sent Completed_Work to RoundRobinLeader-on-tcp-port-" + this.response.getReceiverPort() + " Error occurred: " + response.getErrorOccurred());
					}
					break;
				} catch (IOException e) {
					if (shutdown) throw new ShutdownException();
					if (server.isPeerDead(new InetSocketAddress(response.getReceiverHost(), (response.getReceiverPort() - 2)))) {
						// logJRF("DEAD_PEER");
						throw new DeadPeerException();
					}
					logger.severe("Error occurred while writing response to socket \n" + Util.getStackTrace(e) + "\nTrying Again");
				}
			}
		} else throw new ShutdownException();

	}

	private void deadPeer() throws DeadPeerException {
		if (request != null) {
			InetSocketAddress sender = new InetSocketAddress(request.getSenderHost(), (request.getSenderPort() - 2));
			if (this.server.isPeerDead(sender)) {
				logger.warning("Received Request message from DeadPeer");
				throw new DeadPeerException();
			}
		}
	}

	// Compile and Run java code
	private String compileAndRun(byte[] messageContent) throws IOException, ReflectiveOperationException, IllegalArgumentException {
		JavaRunner javaRunner = new JavaRunner();
		InputStream is = new ByteArrayInputStream(messageContent);
		is.close();
		logger.info("Compiling and running java code...");
		return javaRunner.compileAndRun(is);
	}

	/**
	 * initialize Logger
	 * @return true if Logger was successfully initialized, false if an IOException was thrown
	 */
	private void initLogging(String path, String fileName) throws IOException { this.logger = initializeLogging(path, fileName); }

	/**
	 * initialize ServerSocket if null or if serverSocket was closed
	 * @return true if ServerSocket was successfully created on TCP port, false if an IOException was thrown
	 */
	private void initServerSocket() throws IOException {
		if (this.serverSocket != null && this.serverSocket.isBound() && !this.serverSocket.isClosed()) {
			this.logger.info("SocketServer is already bound to port " + this.tcpPort);
		}
		else if (this.serverSocket == null) {
			try {
				this.serverSocket = new ServerSocket(tcpPort);
				this.logger.info("Created SocketServer bound to port " + this.tcpPort);
			} catch (IOException e) {
				this.logger.severe( "IOException occurred while creating ServerSocket. exiting..." + Util.getStackTrace(e));
				throw new IOException("IOException occurred while creating ServerSocket. exiting...", e);
			}
		}
	}

	@Override
	public void interrupt() {

	}

	@Override
	public void shutdown() {
		shutdown = true;
		if (this.socket != null) {
			try { this.socket.shutdownInput(); } catch (IOException ignored) {}
			try { this.socket.shutdownOutput(); } catch (IOException ignored) {}
			try {
				this.socket.close();
				logger.info("Socket Closed");
			} catch (IOException e) { logger.warning("Failed to close socket \n" + Util.getStackTrace(e)); }
		}
		try {
			this.serverSocket.close();
			logger.info("Closing ServerSocket-on-port-" + this.tcpPort + "...");
		} catch (IOException e) { logger.warning("IOException occurred while trying to close ServerSocket: \n" + Util.getStackTrace(e)); }
		finally {
			super.interrupt();
		}
		this.logger.warning("Shutting down...");
	}

	@Override
	public Logger getLogger() { return this.logger; }

	private void close() {
		try {
			if (socket != null && !socket.isClosed()){
				socket.close();
				logger.info("Closing socket");
			}
		} catch (Exception ignore) { }
		try {
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
				logger.info("Closing ServerSocket");
			}
		} catch (Exception ignore) { }
	}
}
