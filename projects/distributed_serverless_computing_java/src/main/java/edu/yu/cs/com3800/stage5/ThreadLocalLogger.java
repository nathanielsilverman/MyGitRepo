package edu.yu.cs.com3800.stage5;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.yu.cs.com3800.LoggingServer;

public class ThreadLocalLogger extends ThreadLocal<Logger> implements LoggingServer {

	private String path;
	private String fileName;

	public ThreadLocalLogger(String path, String fileName) {
		this.path = path;
		this.fileName = fileName;
	}

	@Override
	protected Logger initialValue() {
		try {
			String s = fileName + "-Thread-ID-" + Thread.currentThread().getId();
			return initializeLogging(path, s);
		} catch (IOException e) {}
		return null;
	}

	@Override
	public Logger getLogger() {
		return get();
	}

	protected void log(Level level, String message) {
		get().log(level, message);
	}

//	protected void log(Level level, String message, GatewayServer.Protocol protocol) {
//
//	}
}