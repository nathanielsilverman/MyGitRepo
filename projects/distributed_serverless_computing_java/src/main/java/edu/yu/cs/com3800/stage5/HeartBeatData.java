package edu.yu.cs.com3800.stage5;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

public class HeartBeatData {

	public final static int SIZE = 16; // Size of HearBeatData Object used for serializing
	private final long id;
	private AtomicLong heartbeatCounter;
	private long timeReceived;

	protected HeartBeatData(final long serverID, AtomicLong heartbeatCounter) {
		this.id = serverID;
		this.heartbeatCounter = heartbeatCounter;
	}

	protected HeartBeatData(final long serverID, long heartbeatCounter, long timeReceived) {
		this.id = serverID;
		this.heartbeatCounter = new AtomicLong(heartbeatCounter);
		this.timeReceived = timeReceived;
	}

	protected long getID() { return id; }
	protected long getHeartbeatCount() { return heartbeatCounter.get(); }
	protected long getTimeReceived() { return this.timeReceived;}
	protected void incrementHeartBeat() { this.heartbeatCounter.incrementAndGet(); }
	protected void setHeartbeatCount(long count) { this.heartbeatCounter.set(count);}
	protected void setTimeReceived(long timeReceived) { this.timeReceived = timeReceived; }
	protected boolean hasFailed(long time) { return (time- this.timeReceived >= HeartBeatGossipMonitor.FAIL); }
	protected boolean readyCleanup(long time) { return (time - this.timeReceived >= HeartBeatGossipMonitor.CLEANUP); }
	protected HeartBeatData copy() { return new HeartBeatData(id, heartbeatCounter); }

	/*
	 *	Size of buffer =
	 *	1 long (serverID) = 8 bytes
	 *	1 long (heartBeatCounter)  = 8 bytes
	 *	= 16 bytes
	 */
	public byte[] getBytes() {
		ByteBuffer buffer = ByteBuffer.allocate(SIZE);
		buffer.putLong(id);
		buffer.putLong(heartbeatCounter.get());
		return buffer.array();
	}

	@Override
	public String toString() {
		return "ID: " + id + " | HB: " + heartbeatCounter.get() + "\n";
	}
}
