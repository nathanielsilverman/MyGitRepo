package edu.yu.cs.com3800;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.Date;
import edu.yu.cs.com3800.stage5.*;

public class Util {

	private static String dateFormat = "hh:mm:ss.S aa";
	private static DateFormat formatter = new SimpleDateFormat(dateFormat);

	public static byte[] readAllBytesFromNetwork(InputStream in) throws IOException {
		try {
			while (in.available() == 0) {
				try {
					Thread.currentThread().sleep(500);
				}
				catch (InterruptedException e) {}
			}
		} catch(IOException e){ throw new IOException(); }
		return readAllBytes(in);
	}

	public static byte[] readAllBytesFromNetwork(ZooKeeperPeerServerImpl server, Socket socket, Long connectedTo) throws DeadPeerException, IOException {
		InputStream in = socket.getInputStream();
		try {

			while (in.available() == 0) {
				try {
					Thread.currentThread().sleep(750);
				}
				catch (InterruptedException e) {}
				if (server.isPeerDead(connectedTo)) throw new DeadPeerException();
			}
		} catch(IOException e){ throw new IOException(); }
		return readAllBytes(in);
	}

	public static byte[] readAllBytesFromNetwork(ZooKeeperPeerServerImpl server, Socket socket, InetSocketAddress address) throws DeadPeerException, IOException {
		InputStream in = socket.getInputStream();
		try {

			while (in.available() == 0) {
				try {
					Thread.currentThread().sleep(750);
				}
				catch (InterruptedException e) {}
				if (server.isPeerDead(getUdpAddressFromTcpAddress(address)) || server.isPeerDead(address)) throw new DeadPeerException();
			}
		} catch(IOException e){ throw new IOException(); }
		return readAllBytes(in);
	}

	public static byte[] readAllBytes(InputStream in) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int numberRead;
		byte[] data = new byte[40960];
		try {
			while (in.available() > 0 && (numberRead = in.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, numberRead);
			}
		} catch(IOException e){ throw new IOException(); }

		return buffer.toByteArray();
	}

	public static Thread startAsDaemon(Runnable run, String name) {
		Thread thread = new Thread(run, name);
		thread.setDaemon(true);
		thread.start();
		return thread;
	}

	public static String getStackTrace(Exception e){
		ByteArrayOutputStream bas = new ByteArrayOutputStream();
		PrintStream myErr = new PrintStream(bas,true);
		e.printStackTrace(myErr);
		myErr.flush();
		myErr.close();
		return bas.toString();
	}

	public static void writeToSocket(Socket socket, byte[] bytes) throws IOException {
		if (socket.isClosed()) throw new IOException();
		OutputStream os = socket.getOutputStream();
		os.write(bytes);
	}

	public static byte[] readFromSocket(Socket socket) throws IOException {
		if (socket.isClosed()) throw new IOException();
		InputStream is = socket.getInputStream();
		return readAllBytesFromNetwork(is);
	}

	public static String getDate() { return formatter.format(new Date()); }

	public static InetSocketAddress getSenderAddressFromMessage(Message message) {
		return (message != null) ? new InetSocketAddress(message.getSenderHost(), message.getSenderPort()) : null;
	}

	public static InetSocketAddress getTcpAddressFromUdpAddress(InetSocketAddress address) {
		return new InetSocketAddress(address.getHostName(), address.getPort()+2);
	}

	public static InetSocketAddress getUdpAddressFromTcpAddress(InetSocketAddress address) {
		return new InetSocketAddress(address.getHostName(), address.getPort()-2);
	}

	public static Message changeMessageRecipient(Message m, String newTargetHost, int newTargetPort) {
		return new Message(m.getMessageType(), m.getMessageContents(), m.getSenderHost(), m.getSenderPort(), newTargetHost, newTargetPort, m.getRequestID(), m.getErrorOccurred());
	}
}
