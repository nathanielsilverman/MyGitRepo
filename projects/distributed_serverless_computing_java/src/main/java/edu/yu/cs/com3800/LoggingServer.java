package edu.yu.cs.com3800;

import java.io.*;
import java.text.*;
import java.util.Date;
import java.nio.file.*;
import java.util.logging.*;
import edu.yu.cs.com3800.stage5.*;

public interface LoggingServer {

	String dateFormat = "yyyy-MM-dd-kk_mm";
	DateFormat formatter = new SimpleDateFormat(dateFormat);
	String slash = File.separator;
	String logsFilesDirectory = "logs" + slash;

	default String initializeServerDir(ZooKeeperPeerServer server) throws IOException {
		String date = getDate();
		String dir = logsFilesDirectory + date + slash + "ZooKeeperPeerServers" + slash + server.getClass().getSimpleName() + "-ID-" + server.getServerId() + "-on-port-" + server.getUdpPort();
		Files.createDirectories(Paths.get(dir));
		return dir;
	}

	default String initializeGatewayServerDir(GatewayServer gatewayServer) throws IOException {
		String dir = logsFilesDirectory + getDate() + slash + GatewayServer.class.getSimpleName() +  slash + GatewayServer.class.getSimpleName() + "-listening-on-http-port-" + gatewayServer.getHttpPort()+ "-tcp-port-" + gatewayServer.getTcpPort();
		Files.createDirectories(Paths.get(dir));
		return dir;
	}

	default Logger initializeLogging(String path, String fileName) throws IOException {
		Logger logger = Logger.getLogger(fileName);
		logger.setUseParentHandlers(false);
		logger.setLevel(Level.ALL);

		File dir = new File(path);
		if (!dir.exists()) {
			Files.createDirectories(Paths.get(path));
		}
		FileHandler fh = new FileHandler(path + slash + fileName + ".log", true);
		fh.setFormatter(new SimpleFormatter());
		logger.addHandler(fh);
		return logger;
	}

	default String getDate() {
		return formatter.format(new Date());
	}

	Logger getLogger();

	default void setLoggerLevel(Level level) {
		Logger logger = getLogger();
		if (logger != null) {
			logger.setLevel(level);
		}
	}
}
