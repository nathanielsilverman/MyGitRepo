package edu.yu.cs.com3800;

import java.io.IOException;
import java.net.InetSocketAddress;

public interface ZooKeeperPeerServer extends LoggingServer {

    void shutdown();

    void setCurrentLeader(Vote v) throws IOException;

    Vote getCurrentLeader();

    void sendMessage(Message.MessageType type, byte[] messageContents, InetSocketAddress target) throws IllegalArgumentException;

    void sendBroadcast(Message.MessageType type, byte[] messageContents);

    ServerState getPeerState();

    void setPeerState(ServerState newState);

    Long getServerId();

    long getPeerEpoch();

    InetSocketAddress getAddress();

    int getUdpPort();

    InetSocketAddress getPeerByID(long peerId);

    int getQuorumSize();

    default void reportFailedPeer(long peerID){
    }

    default boolean isPeerDead(long peerID){
        return false;
    }

    default boolean isPeerDead(InetSocketAddress address){
        return false;
    }

    enum ServerState {
        LOOKING, // Trying to identify the leader. If all servers are in this state, a leader must be elected.
        FOLLOWING, // I am following the leader
        LEADING, // I am the leader
        OBSERVER; // I am neither the leader nor a follower who gets to vote on state changes - serving a secondary purpose

        public char getChar() {
            switch (this) {
                case LOOKING:
                    return 'O';
                case LEADING:
                    return 'E';
                case FOLLOWING:
                    return 'F';
                case OBSERVER:
                    return 'B';
            }
            return 'z';
        }

        public static ZooKeeperPeerServer.ServerState getServerState(char c) {
            switch (c) {
                case 'O':
                    return LOOKING;
                case 'E':
                    return LEADING;
                case 'F':
                    return FOLLOWING;
                case 'B':
                    return OBSERVER;
            }
            return null;
        }
    }
}