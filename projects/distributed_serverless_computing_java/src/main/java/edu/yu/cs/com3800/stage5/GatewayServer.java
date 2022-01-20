package edu.yu.cs.com3800.stage5;

import java.io.*;
import java.net.*;
import java.util.*;
import edu.yu.cs.com3800.*;
import java.util.logging.*;
import java.util.concurrent.*;
import com.sun.net.httpserver.*;
import java.util.concurrent.atomic.*;
import static java.util.logging.Level.*;

public class GatewayServer extends Thread implements LoggingServer {

	enum Protocol { HTTP, TCP, UDP }

	private HttpServer httpServer;
	private Logger logger;
	private final int httpPort;
	private final int tcpPort;
	private final String host;
	private final GatewayPeerServerImpl gatewayPeerServer;
	private final InetSocketAddress gatewayPeerServerAddress;
	private String gatewayServerDir;
	private ThreadLocal<Logger> loggers;
	private ExecutorService executorService;

	// Assign request Messages a new unique request ID using an atomic counter
	private AtomicLong requestCounter;
	private AtomicLong responseCounter;

	private ConcurrentHashMap<Long, byte[]> requestsIDs;

	public GatewayServer(int httpPort, GatewayPeerServerImpl gatewayPeerServer) {
		if (gatewayPeerServer == null) throw new IllegalArgumentException();
		this.httpPort = httpPort;
		this.gatewayPeerServer = gatewayPeerServer;
		this.gatewayPeerServerAddress = this.gatewayPeerServer.getAddress();
		this.host = this.gatewayPeerServerAddress.getHostName();
		this.tcpPort = this.gatewayPeerServerAddress.getPort() + 2;
		this.setName(GatewayServer.class.getSimpleName()+"-on-http-port-" + this.httpPort);
		initLogging();

	}

	public GatewayServer(int httpPort, int myPort, Long peerEpoch, Long id, Map<Long, InetSocketAddress> peerIDtoAddress, int numObservers) {
		this.httpPort = httpPort;
		this.gatewayPeerServer = new GatewayPeerServerImpl(myPort, peerEpoch, id, peerIDtoAddress, numObservers);
		if (!this.gatewayPeerServer.isAlive()) this.gatewayPeerServer.start();
		this.gatewayPeerServerAddress = this.gatewayPeerServer.getAddress();
		this.host = this.gatewayPeerServerAddress.getHostName();
		this.tcpPort = this.gatewayPeerServerAddress.getPort() + 2;
		this.setName(GatewayServer.class.getSimpleName()+"-on-http-port-" + this.httpPort);
		initLogging();
	}

	@Override
	public void run() {
		if (gatewayPeerServer != null && !gatewayPeerServer.isAlive()) gatewayPeerServer.start();
		if (logger == null) return;
		try { initHttpServer(); } catch (IOException e) { return; }
		startHttpServer();
		while (!this.isInterrupted()) {
			try {} catch (Exception e) {
				if (!this.isInterrupted()) this.logger.severe(this.logger.getName() + " is offline \n" + Util.getStackTrace(e));
			}
		}
	}

	public void shutdown() {
		this.executorService.shutdownNow();
		stopHttpServer();
		this.interrupt();
		this.logger.warning(this.logger.getName() + " is shutting down");
		if(this.gatewayPeerServer != null && this.gatewayPeerServer.isAlive()) this.gatewayPeerServer.shutdown();
	}

	public int getTcpPort() { return this.tcpPort; }
	public int getHttpPort() { return this.httpPort; }

	protected GatewayPeerServerImpl getGatewayPeerServer() { return this.gatewayPeerServer; }
	protected String getServerDir() { return this.gatewayServerDir; }
	protected long getRequestID() { return requestCounter.incrementAndGet(); }

	private void initLogging() {
		try {
			this.gatewayServerDir = initializeGatewayServerDir(this);
			this.logger = initializeLogging(this.gatewayServerDir, "GatewayServer-HTTP-port-" + this.httpPort);
		} catch (IOException e) {
			this.logger = null;
		}
	}

	private void initHttpServer() throws IOException {
		try {
			this.httpServer = HttpServer.create(new InetSocketAddress("localhost", httpPort), 0);
		} catch (IOException e) {
			this.logger.severe("Failed to create HTTP Server \n" + Util.getStackTrace(e));
			throw new IOException();
		}
		HttpContext context = this.httpServer.createContext("/compileandrun", new HttpHandler() {
			@Override
			public void handle(HttpExchange httpExchange) throws IOException {
				if (httpExchange == null) {
					log(Level.SEVERE, "Error HTTP Exchange is null", Protocol.HTTP);
					return;
				}
				new HttpRequestHandler(httpExchange).run();
			}
		});

		HttpContext states = this.httpServer.createContext("/get-states", new HttpHandler() {
			@Override
			public void handle(HttpExchange httpExchange) throws IOException {
				if (httpExchange == null) {
					log(Level.SEVERE, "Error HTTP Exchange is null", Protocol.HTTP);
					return;
				}

				int resCode = (gatewayPeerServer.hasStates() ? 200 : 400);
				byte[] res = gatewayPeerServer.hasStates() ? gatewayPeerServer.getStates().getBytes() : new byte[0];
				httpExchange.sendResponseHeaders(resCode, res.length);
				OutputStream os = httpExchange.getResponseBody();
				os.write(res);
				os.close();
			}
		});

		HttpContext leader = this.httpServer.createContext("/leader", new HttpHandler() {
			@Override
			public void handle(HttpExchange httpExchange) throws IOException {
				if (httpExchange == null) {
					log(Level.SEVERE, "Error HTTP Exchange is null", Protocol.HTTP);
					return;
				}
				Vote v = gatewayPeerServer.getCurrentLeader();
				int resCode = (v != null ? 200 : 400);
				byte[] res = v != null ? Long.toString(v.getProposedLeaderID()).getBytes() : new byte[0];
				httpExchange.sendResponseHeaders(resCode, res.length);
				OutputStream os = httpExchange.getResponseBody();
				os.write(res);
				os.close();
			}
		});

		this.logger.info("Created HTTP Server context: " + context.getPath());
		this.logger.info("Created HTTP Server context: " + states.getPath());
		this.logger.info("Created HTTP Server context: " + leader.getPath());
		this.executorService = Executors.newCachedThreadPool();
		this.httpServer.setExecutor(this.executorService);
		this.requestCounter = new AtomicLong();
		this.responseCounter = new AtomicLong();

		this.requestsIDs = new ConcurrentHashMap<>();

		String path = getServerDir() + File.separator + "GatewayServer-HttpRequestHandlers";
		String fileName = HttpRequestHandler.class.getSimpleName();

		this.loggers = new ThreadLocalLogger(path, fileName);
	}

	private void startHttpServer() {
		this.httpServer.start();
		this.logger.info("Starting HTTP Server...");
	}

	private void stopHttpServer() {
		this.httpServer.stop(0);
		this.logger.warning("Stopping HTTP Server...");
	}

	class HttpRequestHandler implements Runnable {

		private HttpExchange httpExchange;
		private byte[] request;
		private Long requestID;

		private InetSocketAddress leader;
		private String leaderHost;
		private int leaderTcpPort;
		private long leaderID;
		private Socket socket;

		private Message requestMessage; // Request Message Gateway Sends to Leader
		private Message responseMessage; // Response Message Gateway Receives from Leader

		public HttpRequestHandler(HttpExchange httpExchange) {
			this.httpExchange = httpExchange;
		}

		@Override
		public void run() {
			if (!validRequest()) return;
			this.requestID = getRequestID();
			requestsIDs.putIfAbsent(this.requestID, request); // Map requestID to request
			log(INFO, "Request-" + String.format("%02d", this.requestID) + " received from client", Protocol.HTTP);
			while (true) {
				try {
					locateLeader();
					connectToLeader();
					sendRequest();
					getResponseFromLeader();
					if (requestMessage != null) break;
				} catch (Exception e) {
					if (e instanceof DeadPeerException) {
						try {
							socket.close();
						} catch (IOException io) {}
					}
				}
			}
			sendResponse();
		}

		///// Methods to Read and Verify HTTP Request /////

		private boolean validRequest() {
			try {
				verifyContentType();
				readHttpRequest();
				return true;
			} catch (IllegalContentTypeException | IOException e) {
				sendResponse(400, Util.getStackTrace(e).getBytes());
				return false;
			}
		}

		private void verifyContentType() throws IllegalContentTypeException {
			Headers headers = this.httpExchange.getRequestHeaders();
			if (!headers.get("Content-Type").contains("text/x-java-source")) {
				log(Level.WARNING, "IllegalContentTypeException: Content-Type must be of type (text/x-java-source)", Protocol.HTTP);
				throw new IllegalContentTypeException();
			}
		}

		private void readHttpRequest() throws IOException {
			try {
				request = httpExchange.getRequestBody().readAllBytes(); // Read All bytes from http request
			} catch (IOException e) {
				log(Level.SEVERE, ("IOException occurred while trying to read bytes from client HTTP request: \n" + Util.getStackTrace(e)), Protocol.HTTP);
				throw new IOException(e);
			}
		}

		///// Methods to Properly Connect to Leader and Send Leader Request /////

		private void locateLeader() throws DeadPeerException {
			log(INFO, "trying to locate leader", Protocol.UDP);
			while (!knowsLeader()) try {
				Thread.sleep(2000);
			} catch (InterruptedException ignored) {}

			if (gatewayPeerServer.isPeerDead(gatewayPeerServer.getCurrentLeader().getProposedLeaderID())) throw new DeadPeerException("DeadPeerException: Leader was marked dead");
			log(INFO, "located leader", Protocol.UDP);
		}

		private boolean knowsLeader() { return gatewayPeerServer.getCurrentLeader() != null; }

		private void connectToLeader() throws NoLeaderException {
			if (knowsLeader()) {
				try {
					InetSocketAddress address = gatewayPeerServer.getAddressOfLeader();
					this.leaderID = gatewayPeerServer.getLeaderID();
					this.leaderHost = address.getHostName();
					this.leaderTcpPort = address.getPort() + 2;
					this.leader = new InetSocketAddress(leaderHost, leaderTcpPort);
					this.socket = new Socket();
					this.socket.connect(this.leader);
					log(Level.INFO, "Established connection to RoundRobinLeader-on-port-" + this.leaderTcpPort, Protocol.TCP);
				} catch (NoLeaderException e) {
					throw new NoLeaderException("No Leader Could be Found", e);
				} catch (IOException e) {}
			} else throw new NoLeaderException("No Leader Could be Found");
		}

		private void sendRequest() throws DeadPeerException, IOException {
			requestMessage = new Message(Message.MessageType.WORK, request, host, tcpPort, this.leaderHost, this.leaderTcpPort, this.requestID); // Create a request message to send to leader
			if (!gatewayPeerServer.isPeerDead(this.leader)) {
				Util.writeToSocket(this.socket, requestMessage.getNetworkPayload()); // Send to master thread to assign work to compute
				log(Level.INFO, "Request: " + requestMessage.getRequestID() + " sent to LeaderLeader-on-port-" + this.leaderTcpPort, Protocol.TCP);
			} else throw new DeadPeerException("DeadPeerException: Leader was marked dead");
		}

		//// Methods to Receive Response from Leader ////
		private void getResponseFromLeader() throws DeadPeerException, IOException, NoLeaderException {
			if (!knowsLeader()) throw new NoLeaderException();
			log(INFO, "Waiting to receive response from Leader...", Protocol.TCP);
			responseMessage = new Message(Util.readAllBytesFromNetwork(gatewayPeerServer, socket, gatewayPeerServer.getLeaderID()));
			if (gatewayPeerServer.isPeerDead(leader)) {
				logger.warning("Received response from leader: " + Util.getDate() + " ERROR: (received from failed peer)");
				throw new DeadPeerException("DeadPeerException: Leader was marked dead");
			}
			log(Level.INFO, "Response message (to request: " + requestMessage.getRequestID() + ") received from Leader-on-port-" + responseMessage.getSenderPort() + " -- RequestID: " + responseMessage.getRequestID() + (responseMessage.getErrorOccurred() ? "  -- Error Occurred" : ""), Protocol.TCP);
			logger.info("Response message (to request: " + requestMessage.getRequestID() + ") received from Leader-on-port-" + responseMessage.getSenderPort() + " -- RequestID: " + responseMessage.getRequestID() + (responseMessage.getErrorOccurred() ? "  -- Error Occurred" : ""));

		}

		//// Methods to send Response to Client ////
		private void sendResponse(int responseCode, byte[] bytes) {
			try {
				httpExchange.sendResponseHeaders(responseCode, bytes.length);
				OutputStream os = httpExchange.getResponseBody();
				os.write(bytes);
				os.close();
				log(FINE, "Response message sent to client " + (responseCode == 200 ? "200 OK" : "400 Bad Request"), Protocol.HTTP);
				logger.fine("Response message sent to client " + (responseCode == 200 ? "200 OK" : "400 Bad Request"));
			} catch (IOException e) {
				log(Level.SEVERE, "IOException occurred while trying to send HTTP response to Client", Protocol.HTTP);
			}
		}

		private void sendResponse() {
			try {
				int responseCode = (!responseMessage.getErrorOccurred()) ? 200 : 400;
				byte[] response = responseMessage.getMessageContents();
				httpExchange.sendResponseHeaders(responseCode, response.length);
				OutputStream os = httpExchange.getResponseBody();
				os.write(response);
				os.close();
				long n = responseCounter.incrementAndGet();
				log(Level.INFO, "Response (Request ID: " + String.format("%02d", responseMessage.getRequestID())+ ") message sent to client " + (responseCode == 200 ? "200 OK" : "400 Bad Request") + " # of Responses: " + n, Protocol.HTTP);
				logger.info(loggers.get().getName() + " - Response (Request ID: " + String.format("%02d", responseMessage.getRequestID())+ ") message sent to client " + (responseCode == 200 ? "200 OK" : "400 Bad Request") + " # of Responses: " + n);
				requestsIDs.remove(responseMessage.getRequestID());
			} catch (IOException e) {
				log(Level.SEVERE, "IOException occurred while trying to send HTTP response to Client", Protocol.HTTP);
			}
		}
	}

	private void log(Level level, String logMessage, Protocol protocol) {
		if (loggers.get() != null) {
			loggers.get().log(level,  "[Type: " + String.format("%4s", protocol.name()) + "] -- " + logMessage);
		}
		if (level == WARNING || level == SEVERE) {
			logger.log(level,  "[Type: " + String.format("%4s", protocol.name()) + "] -- occurred in " + loggers.get().getName() + " >>> " + logMessage);
		}
	}

	@Override
	public Logger getLogger() { return this.logger; }

	@Override
	public void setLoggerLevel(Level level) { this.logger.setLevel(level); }

	private String timeStamp() { return " --- [" + Util.getDate() + "]"; }

}

