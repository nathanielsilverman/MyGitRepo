package edu.yu.cs.com3800.stage5;

import edu.yu.cs.com3800.Util;

import java.util.*;

public class GossipMessage {

	private final long senderId, id;
	private final Map<Long, HeartBeatData> heartBeats;
	private String wallClockTime;

	public GossipMessage(long senderId, long id,  Map<Long, HeartBeatData> heartBeats, String wallClockTime) {
		this.senderId = senderId;
		this.id = id;
		this.heartBeats = new HashMap<>(heartBeats);
		this.wallClockTime = wallClockTime;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		sb.append("\nGossip Message Sent From Server - " + String.format("%02d", senderId));
		sb.append("\nReceived By Server - " + String.format("%02d", id));
		sb.append("\nTime: " + wallClockTime);
		sb.append("\n");
		sb.append(" -------------");
		sb.append("\n");
		sb.append("|IDs|HeartBeat|");
		sb.append("\n");
		sb.append(" -------------");
		sb.append("\n");
		for (HeartBeatData hb: heartBeats.values()) {
			long id = hb.getID();
			long hbCount = hb.getHeartbeatCount();
			sb.append("|");
			sb.append(String.format("% 3d", id));
			sb.append("|");
			sb.append(String.format("% 9d", hbCount));
			sb.append("|\n");
		}
		sb.append(" -------------\n");
		return sb.toString();
	}
}
