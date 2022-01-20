package edu.yu.cs.com3800.stage5;

import java.io.*;
import java.net.*;
import java.util.*;
import edu.yu.cs.com3800.*;
import java.util.logging.*;
import java.util.concurrent.*;
import static java.util.logging.Level.*;
import static edu.yu.cs.com3800.Message.MessageType.*;

public class RoundRobinLeader extends ServerRole implements LoggingServer {

	private ZooKeeperPeerServerImpl server;
	private final int tcpPort;
	private final String host;
	private ServerSocket serverSocket;
	private Logger logger;
	private String logFileDir;
	private static final int constant = 3;
	private LinkedBlockingQueue<InetSocketAddress> workers;
	private ThreadLocal<Logger> loggers;
	private ExecutorService executor;
	private final int parallelism;
	private Map<Long,Message> requestsIDsWorkCompleted;
	private volatile boolean shutdown;

	public RoundRobinLeader(ZooKeeperPeerServerImpl server) throws IOException {
		this.server = server;
		this.tcpPort = server.getUdpPort() + 2;
		this.host = server.getAddress().getHostName();
		this.parallelism = Runtime.getRuntime().availableProcessors();
		this.requestsIDsWorkCompleted = new ConcurrentHashMap<>();
		this.logFileDir = server.getLogFileDir();
		setName("RoundRobinLeader-tcp-port-" + this.tcpPort);
		setDaemon(true);
		initLogging((this.logFileDir + File.separator + "RoundRobinLeader-tcp-port-" + this.tcpPort), getName());
		initWorkers(server);
		initServerSocket();
		initThreadPool();
		shutdown = false;
	}

	@Override
	public void run() {
		this.logger.info("Starting RoundRobinLeader-tcp-port-" + this.tcpPort + "...");
		getLastWork();

		while(!this.isInterrupted() && !shutdown) {
			try {
				this.logger.info("Waiting for connection request from Gateway");
				while (!shutdown) {
					Socket gatewayConnection = this.serverSocket.accept();
					this.logger.info("RoundRobinLeader ServerSocket accepted new connection");
					executor.execute(new RoundRobinTaskScheduler(gatewayConnection));
				}
			} catch (Exception e) {
				if (shutdown) break;
				if (!this.isInterrupted()) this.logger.warning("Exception caught while trying to accept connection \n" + Util.getStackTrace(e));
			}
		}
		this.logger.warning("Exiting RoundRobinLeader...");
	}

	private void getLastWork() {
		workers.forEach(worker -> {
			if (!server.isPeerDead(Util.getUdpAddressFromTcpAddress(worker))) {
				executor.execute(requestCompletedWork(worker));
			}
		});
	}

	private Runnable requestCompletedWork(InetSocketAddress address) {
		final InetSocketAddress worker = address;
		return new Runnable() {
			@Override
			public void run() {
				if (!shutdown) {
					Socket socket = new Socket();
					try {
						try {
							socket.connect(worker);
							loggers.get().log(INFO, "NEW_LEADER established connection to JavaRunnerFollower-on-tcp-" + worker.getPort());
						} catch (IOException e) {
							throw new IOException("Unable to establish socket connection to JavaRunnerFollower-on-tcp-" + worker.getPort());
						}
						try {
							loggers.get().log(INFO, "NEW_LEADER REQUESTING COMPLETED_WORK from JavaRunnerFollower-on-tcp-" + worker.getPort());
							Util.writeToSocket(socket, new Message(NEW_LEADER_GETTING_LAST_WORK, new byte[0], host, tcpPort, worker.getHostName(), worker.getPort(), -1).getNetworkPayload());
						} catch (IOException e) {
							throw new IOException("Unable to write to JavaRunnerFollower-on-tcp-" + worker.getPort());
						}
						try {
							byte[] bytes = Util.readAllBytesFromNetwork(server, socket, Util.getUdpAddressFromTcpAddress(worker));
							if (!shutdown) {
								Message m = new Message(bytes);
								if (m.getRequestID() != -1) {
									requestsIDsWorkCompleted.put(m.getRequestID(), m);
									loggers.get().log(INFO, "Received completed work for Request: " + m.getRequestID() + " from JavaRunnerFollower-on-tcp-" + worker.getPort());
								} else {
									loggers.get().log(INFO, "NO Completed WORK from JavaRunnerFollower-on-tcp-" + worker.getPort());
								}
							}
						} catch (IOException | DeadPeerException e) {
							if (e instanceof IOException) throw new IOException("Unable to receive response from JavaRunnerFollower-on-tcp-" + worker.getPort());
						}
					} catch (IOException e) {
						loggers.get().log(WARNING, Util.getStackTrace(e));
					} finally {
						try {
							socket.close();
							loggers.get().log(INFO, "Closed socket connection to JavaRunnerFollower-on-tcp-" + worker.getPort());
						} catch (IOException e) {
							loggers.get().log(WARNING, Util.getStackTrace(e));
						} finally {
							try { workers.put(worker); } catch (InterruptedException ignored){}
						}
					}
				}
			}
		};
	}

	@Override
	public void shutdown() {
		this.shutdown = true;
		executor.shutdownNow();
		try {
			this.serverSocket.close();
			logger.info("Closing ServerSocket-on-port-" + this.tcpPort + "...");
		} catch (IOException e) {
			logger.warning("IOException occurred while trying to close ServerSocket: \n" + Util.getStackTrace(e));
		} finally {
			super.interrupt();
		}
		this.logger.warning("Shutting down...");
	}

	private class RoundRobinTaskScheduler implements Runnable {

		private Socket gatewaySocket, workerSocket; // socket connection to the GatewayServer, socket connection to the JavaRunnerFollower
		private InetSocketAddress gateway, worker; // address of the GatewayServer, address of the JavaRunnerFollower
		private Message gatewayRequest, gatewayResponse; // RequestMessage from Gateway, Response Message
		private Message request, response; // RequestMessage to JavaRunnerFollower, Response Message
		private String gatewayHost, workerHost; // GatewayServer Host name, Host name of the JavaRunnerFollower
		private int gatewayPort, workerPort; // GatewayServer port number, port number of the JavaRunnerFollower
		private long requestID;

		public RoundRobinTaskScheduler(Socket socket) { this.gatewaySocket = socket; }

		@Override
		public void run() {
			try {
				while(!shutdown) {
					try {
						readGatewayRequest(); // Read Request from GatewayServer
						if (hasCompletedWork()) sendCompletedWork(); // If RoundRobinLeader has stored results of completed work for the Gateway request return the saved results
						else scheduleWork(); // Otherwise assign work to JavaRunnerFollower to process request
						break;
					} catch (ShutdownException e) { break; }
				}
			} finally {
				close();
				if (worker != null) try { workers.put(worker); } catch (InterruptedException ignored) {}
			}
		}

		// Read all bytes from GatewayServer socket connection and construct request message from the network payload
		private void readGatewayRequest() throws ShutdownException {
			if (shutdown) throw new ShutdownException();
			while (!shutdown) {
				try {
					this.gatewayRequest = new Message(Util.readFromSocket(gatewaySocket));
					this.requestID = this.gatewayRequest.getRequestID();
					this.gatewayHost = this.gatewayRequest.getSenderHost();
					this.gatewayPort = this.gatewayRequest.getSenderPort();
					this.gateway = new InetSocketAddress(this.gatewayHost, this.gatewayPort);
					loggers.get().log(INFO, "RoundRobinLeader-on-tcp-port-" + tcpPort + " Received Request: " + this.gatewayRequest.getRequestID() + " From GatewayServer-on-tcp-port-" + gateway.getPort());
					break;
				} catch (IOException e) {
					if (shutdown) throw new ShutdownException();
					loggers.get().log(Level.SEVERE, "IOException occurred while reading Gateway Request message from socket " + Util.getStackTrace(e));
				}
			}
		}

		private boolean hasCompletedWork() { return requestsIDsWorkCompleted.containsKey(gatewayRequest.getRequestID()); }

		private void sendCompletedWork() throws ShutdownException {
			if (shutdown) throw new ShutdownException();
			this.gatewayResponse = requestsIDsWorkCompleted.get(gatewayRequest.getRequestID());
			writeGatewayResponse(); // Write response to Gateway
		}

		private void scheduleWork() throws ShutdownException {
			if (shutdown) throw new ShutdownException();
			while (!shutdown) {
				try {
					getNextWorker(); // Choose the next JavaRunnerFollower using RoundRobin Scheduling
					connectToWorkerSocket(); // Connect to worker Socket
					sendWorkerRequest(); // Send Request Message to assigned JavaRunnerFollower
					receiveWorkerResponse(); // Get response message from JavaRunnerFollower
					writeGatewayResponse(); // Write response to Gateway
					return; // If the peer isn't dead and it's after response was sent to Gateway, response successfully read from alive follower, break
				} catch (DeadPeerException | NullPointerException | ShutdownException e) { // Each step, periodically checks if peerServer of the JavaRunnerFollower is reported failed, if failed re-assign work
					if (shutdown) throw new ShutdownException();
					if (e instanceof DeadPeerException) loggers.get().log(Level.WARNING, Util.getStackTrace(e));
				}
			}
		}

		// Choose the next JavaRunnerFollower using RoundRobin Scheduling
		private void getNextWorker() throws ShutdownException {
			InetSocketAddress nextWorker;
			while(!isInterrupted() && !shutdown) {
				try {
					while (workers.isEmpty());
					nextWorker = workers.take();
					if (!nextWorker.equals(this.gateway)) {
						this.worker = nextWorker;
						this.workerHost = this.worker.getHostName();
						this.workerPort = this.worker.getPort();
						deadPeer();
						break;
					}
				} catch (DeadPeerException | InterruptedException e) {
					if (shutdown) throw new ShutdownException();
				}
			}
		}

		private void connectToWorkerSocket() throws DeadPeerException, ShutdownException {
			if (this.gatewayRequest == null || this.worker == null) return;
			if (shutdown) throw new ShutdownException();
			try {
				deadPeer(); // if worker is dead throws DeadPeerException
				this.workerSocket = new Socket(); // Establish a connection with a JavaRunnerFollower
				this.workerSocket.connect(worker);
				loggers.get().log(Level.INFO,"Connection established with next JavaRunnerFollower-on-port-" + this.workerPort);
				return;
			} catch (IOException e) {
				if (shutdown) throw new ShutdownException();
			 	if (server.isPeerDead(Util.getUdpAddressFromTcpAddress(worker))) throw new DeadPeerException();
				loggers.get().log(Level.WARNING, "Connection could not be established with JavaRunnerFollower-on-port-" + this.workerPort + "\n" + Util.getStackTrace(e));
			}
		}

		// Send request message to be processed JavaRunnerFollower
		private void sendWorkerRequest() throws DeadPeerException, ShutdownException {
			if (shutdown) throw new ShutdownException();
			if (this.gatewayRequest == null || this.worker == null || workerSocket == null) throw new DeadPeerException();

			this.request = new Message(WORK, gatewayRequest.getMessageContents(), host, tcpPort, this.workerHost, this.workerPort, requestID); // Construct Request Message with work to send to JavaRunnerFollower
			while (!shutdown) {
				try {
					deadPeer();
					Util.writeToSocket(workerSocket, request.getNetworkPayload());
					loggers.get().log(Level.INFO,"Sent (Request: " + request.getRequestID() + ") message send to JavaRunnerFollower-on-port-" + request.getReceiverPort());
					return;
				} catch (IOException e) {
					if (shutdown) throw new ShutdownException();
					loggers.get().log(Level.SEVERE, "IOException occurred while trying to connect and send request message to JavaRunnerFollower-on-tcp-port-" + this.workerPort + ": " + Util.getStackTrace(e));
				}
			}
		}

		// Receive response message from JavaRunnerFollower
		private void receiveWorkerResponse() throws DeadPeerException, ShutdownException {
			if (shutdown) throw new ShutdownException();
			loggers.get().log(Level.INFO, "Waiting for response message from JavaRunnerFollower-on-tcp-port-" + workerPort);
			while (!shutdown) {
				try {
					byte[] bytes = Util.readAllBytesFromNetwork(server, this.workerSocket, Util.getUdpAddressFromTcpAddress(this.worker));
					response = new Message(bytes);
					requestsIDsWorkCompleted.put(response.getRequestID(), response);
					loggers.get().log(Level.INFO,"Received response (Request: " + response.getRequestID() + ") from JavaRunnerFollower-on-port-" + response.getSenderPort());
					logger.log(INFO, loggers.get().getName() + " - Received response (Request: " + response.getRequestID() + ") from JavaRunnerFollower-on-port-" + response.getSenderPort());
					this.gatewayResponse = new Message(response.getMessageType(), response.getMessageContents(), host, tcpPort, gatewayHost, gatewayPort, response.getRequestID(), response.getErrorOccurred());

					cleanUpWorkerSocket();
					return;
				} catch (IOException | DeadPeerException e) {
					if (shutdown) throw new ShutdownException();
					deadPeer();
					loggers.get().log(Level.SEVERE, "IOException occurred while reading response from JavaRunnerFollower-on-tcp-port-" + this.workerPort + ": " + Util.getStackTrace(e));
				}
			}
		}

		private void cleanUpWorkerSocket() { try { this.workerSocket.close(); } catch (Exception ignored) {}}

		private void deadPeer() throws DeadPeerException { if (server.isPeerDead(Util.getUdpAddressFromTcpAddress(worker))) throw new DeadPeerException("During execution JavaRunnerFollower-on-tcp-port-" + worker.getPort() + " was reported dead. Re-assigning work to another JavaRunnerFollower"); }

		// Write response message to GatewayServer socket connection
		private void writeGatewayResponse() throws ShutdownException {
			if (shutdown) throw new ShutdownException();
			while (!shutdown) {
				try {
					Util.writeToSocket(gatewaySocket, this.gatewayResponse.getNetworkPayload());
					loggers.get().log(FINE, "Response (to Request:" + gatewayResponse.getRequestID() + ") Sent to GatewayServer-on-tcp-port-" + this.gatewayPort);
					logger.log(FINE, loggers.get().getName() + " - Response (to Request:" + gatewayResponse.getRequestID() + ") Sent to GatewayServer-on-tcp-port-" + this.gatewayPort);
					return;
				} catch (IOException e) {
					if (shutdown) throw new ShutdownException();
					loggers.get().log(SEVERE, "IOException occurred while writing response to GatewayServer-on-tcp-port-" + this.gatewayPort + ": " + Util.getStackTrace(e));
				}
			}
		}

		private void close() {
			try {
				workerSocket.close();
				loggers.get().log(FINE, "Closed socket Connection to JavaRunnerFollower-on-tcp-port-" + worker.getPort());
			} catch (Exception ignore) {}
			try {
				gatewaySocket.close();
				loggers.get().log(FINE, "Closed socket Connection to GatewayServer-on-tcp-port-" + gateway.getPort());
			} catch (Exception ignore) {}
		}
	}

	private void initWorkers(ZooKeeperPeerServerImpl server) {
		this.workers = new LinkedBlockingQueue<>();
		for (Map.Entry<Long,InetSocketAddress> entry : server.getPeers().entrySet()) {
			if (entry != null) {
				InetSocketAddress worker = entry.getValue();
				if (worker != null && !server.isPeerDead(worker) && !server.isObserver(entry.getKey())) workers.offer(Util.getTcpAddressFromUdpAddress(worker));
			}
		}
	}

	private void initThreadPool() {
		int poolSize = constant * parallelism;
		executor = Executors.newFixedThreadPool(poolSize);
		this.logger.info("created ThreadPool of RoundRobinTaskSchedulers");
		this.loggers = new ThreadLocalLogger(this.logFileDir + File.separator + "RoundRobinLeader-tcp-port-" + this.tcpPort + File.separator + "RoundRobinTaskSchedulerThreads-" + poolSize, RoundRobinTaskScheduler.class.getSimpleName() + "-tcp-port-" + this.tcpPort);
	}

	@Override
	public Logger getLogger() { return this.logger; }

	/**
	 * initialize Logger
	 * @Throws IOException is couldn't create Logger
	 */
	private void initLogging(String path, String fileName) throws IOException { this.logger = initializeLogging(path, fileName); }

	/**
	 * initialize ServerSocket if null or if serverSocket was closed
	 * @Throws IOException if ServerSocket failed to create ServerSocket bound to TCP port
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
				this.logger.severe( "IOException occurred while creating ServerSocket. exiting... \n" + Util.getStackTrace(e));
				throw new IOException("IOException occurred while creating ServerSocket. exiting...", e);
			}
		}
	}

}
