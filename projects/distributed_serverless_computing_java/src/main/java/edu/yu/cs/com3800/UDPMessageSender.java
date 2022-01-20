package edu.yu.cs.com3800;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UDPMessageSender extends Thread implements LoggingServer {
    private int serverUdpPort;
    private LinkedBlockingQueue<Message> outgoingMessages;
    private Logger logger;

    public UDPMessageSender(LinkedBlockingQueue<Message> outgoingMessages, int serverUdpPort) {
        this.outgoingMessages = outgoingMessages;
        this.serverUdpPort = serverUdpPort;
        setDaemon(true);
        setName("UDPMessageSender-port-" + this.serverUdpPort);
    }

    public UDPMessageSender(LinkedBlockingQueue<Message> outgoingMessages, int serverUdpPort, String logFilePath) throws IOException {
        this.outgoingMessages = outgoingMessages;
        this.serverUdpPort = serverUdpPort;
        setDaemon(true);
        setName("UDPMessageSender-port-" + this.serverUdpPort);
        initLogging(logFilePath);
    }

    public void shutdown() { interrupt(); }

    @Override
    public void run() {

        while (!this.isInterrupted()) {
            try {
                Message messageToSend = this.outgoingMessages.poll();
                if (messageToSend != null) {
                    DatagramSocket socket = new DatagramSocket();
                    byte[] payload = messageToSend.getNetworkPayload();
                    DatagramPacket sendPacket = new DatagramPacket(payload, payload.length, new InetSocketAddress(messageToSend.getReceiverHost(), messageToSend.getReceiverPort()));
                    socket.send(sendPacket);
                    socket.close();
                    this.logger.fine("Message sent:\n" + messageToSend.toString());
                }
            } catch (IOException e) {
                this.logger.log(Level.WARNING,"failed to send packet", e);
            } catch (Exception e) {
                if (this.isInterrupted()) {
                    this.logger.log(Level.WARNING,"Shutting down...");
                }
            }
        }
        this.logger.log(Level.SEVERE,"Exiting UDPMessageSender.run()");
    }

    /**
     * initialize Logger
     */
    private void initLogging(String path) throws IOException {
        if (this.logger == null) this.logger = initializeLogging(path, this.getClass().getSimpleName() + "-on-udp-port-" + this.serverUdpPort);
    }

    @Override
    public Logger getLogger() { return this.logger; }
}