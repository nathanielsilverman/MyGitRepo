package edu.yu.cs.com3800;

import edu.yu.cs.com3800.Message.MessageType;
import edu.yu.cs.com3800.ZooKeeperPeerServer.ServerState;
import edu.yu.cs.com3800.stage5.ZooKeeperPeerServerImpl;

import java.util.concurrent.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Level;

import static edu.yu.cs.com3800.ZooKeeperPeerServer.ServerState.LOOKING;

public class ZooKeeperLeaderElection {

    /**
     * time to wait once we believe we've reached the end of leader election.
     */
    private final static int finalizeWait = 200;

    /**
     * Upper bound on the amount of time between two consecutive notification checks.
     * This impacts the amount of time to get the system up again after long partitions. Currently 60 seconds.
     */
    private final static int maxNotificationInterval = 60000;

    /////////////////////////////////////
    ///  ZooKeeper Election Algorithm ///
    /////////////////////////////////////
    
    private ZooKeeperPeerServerImpl myPeerServer;
    private LinkedBlockingQueue<Message> incomingMessages;
    private Map<Long,ElectionNotification> votesReceived;
    private Long proposedLeader;
    private Long proposedEpoch;
    private int timeOut;

    public ZooKeeperLeaderElection(ZooKeeperPeerServer server, LinkedBlockingQueue<Message> incomingMessages) {
        this.incomingMessages = incomingMessages;
        this.myPeerServer = (ZooKeeperPeerServerImpl) server;
        this.votesReceived = new HashMap<>();
        this.proposedLeader = server.getServerId();
        this.proposedEpoch =  server.getPeerEpoch();
        this.timeOut = finalizeWait;
    }

    private synchronized Vote getCurrentVote() {
        return new Vote(this.proposedLeader, this.proposedEpoch);
    }

    public synchronized Vote lookForLeader() {
        sendNotifications(); // send initial notifications to other peers to get things started
        while (isLooking() || isObserving()) { processMessage(); } // Loop, exchanging notifications with other servers until we find a leader
        return getCurrentVote(); // Return the vote of what this server believes is the LEADER
    }

    private boolean isLooking() {
        return this.myPeerServer.getPeerState() == ServerState.LOOKING;
    }

    private boolean isObserving() {
        return this.myPeerServer.getPeerState() == ServerState.OBSERVER && getCurrentVote().getProposedLeaderID() == this.myPeerServer.getServerId();
    }

    private Vote acceptElectionWinner(ElectionNotification n) {
        // Set my state to either LEADING or FOLLOWING
        ServerState oldState = myPeerServer.getPeerState();

        if (n.getProposedLeaderID() == this.myPeerServer.getServerId()) this.myPeerServer.setPeerState(ServerState.LEADING);
        else if (this.myPeerServer.getPeerState() == ServerState.LOOKING) this.myPeerServer.setPeerState(ServerState.FOLLOWING);

        if (!oldState.equals(myPeerServer.getPeerState())) {
            String stateChange = String.format("%02d", myPeerServer.getServerId()) + ": switching from " + oldState + " to " + myPeerServer.getPeerState();
            this.myPeerServer.getLogger().log(Level.FINE, stateChange);
            System.out.println(stateChange);
        }

        // Clear out the incoming queue before returning
        this.incomingMessages.clear();
        return new Vote(this.proposedLeader, this.proposedEpoch);
    }

    /*
     * We return true if one of the following two cases hold:
     * 1- New epoch is higher
     * 2- New epoch is the same as current epoch, but server id is higher.
     */
     protected boolean supersedesCurrentVote(long newId, long newEpoch) {
         return (newEpoch > this.proposedEpoch) || ((newEpoch == this.proposedEpoch) && (newId > this.proposedLeader));
     }

    /**
     * Termination predicate. Given a set of votes, determines if have sufficient support for the proposal to declare the end of the election round.
     * Who voted for who isn't relevant, we only care that each server has one current vote
     */
    protected boolean haveEnoughVotes(Map<Long, ElectionNotification > votes, Vote proposal) {
       //is the number of votes for the proposal >= the size of my peer server’s quorum?
       int voterCount = 0;
       for (ElectionNotification en : votes.values()) {
           if((en != null) && (en.getProposedLeaderID() == proposal.getProposedLeaderID())) voterCount++;
       }
       return voterCount >= this.myPeerServer.getQuorumSize();
    }

    private void updateVote(Long proposedLeaderID, Long proposedEpoch) {
        this.proposedLeader = proposedLeaderID;
        this.proposedEpoch = proposedEpoch;
    }

    // Retrieve the next message from the incoming queue. Implementing exponential back-off is no messages received.
    private void processMessage() {
        // //Remove next notification from queue, timing out after 2 times the termination time
        Message msg = null;
        try {
            msg = incomingMessages.poll(this.timeOut*2, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignore){}
        //if no notifications received..
        if (msg == null) {
            sendNotifications();  //..resend notifications to prompt a reply from others..
            exponentialBackOff(); //.and implement exponential back-off when notifications not received..
        }
        else {
            processVote(msg);
        }
    }

    private void exponentialBackOff() {
        this.timeOut = Math.min((this.timeOut*2), maxNotificationInterval);
    }

    private void processVote(Message msg) {
        ElectionNotification electionNotification = getNotificationFromMessage(msg);
        if (electionNotification.getPeerEpoch() >= this.proposedEpoch) {
            votesReceived.put(electionNotification.getSenderID(), electionNotification);
            switch (electionNotification.getState()) {
                case LOOKING:
                    senderLookingForLeader(electionNotification);
                    break;
                case FOLLOWING: case LEADING:
                    senderKnowsLeader(electionNotification);
                    break;
                default:
            }
        }
    }

    private void senderLookingForLeader(ElectionNotification electionNotification) {
        long enProposedLeaderID = electionNotification.getProposedLeaderID();
        long enPeerEpoch = electionNotification.getPeerEpoch();
        if (supersedesCurrentVote(enProposedLeaderID, enPeerEpoch)) { // if the received message has a vote for a leader which supersedes mine.
            updateVote(enProposedLeaderID, enPeerEpoch); //  change my vote
            sendNotifications(); // and tell all my peers what my new vote is.
        }
        if(haveEnoughVotes(this.votesReceived, electionNotification)) {
            try {
                Thread.sleep(finalizeWait);
            } catch (Exception ignore) {}
            if (this.incomingMessages.isEmpty()) {
                updateVoteAndAcceptElectionWinner(electionNotification);
            }
        }
    }

    private void senderKnowsLeader(ElectionNotification electionNotification) {
        //IF: see if the sender's vote allows me to reach a conclusion based on the election epoch that I'm in, i.e. it gives the majority to the vote of the FOLLOWING or LEADING peer whose vote I just received.
        if (electionNotification.getPeerEpoch() == this.proposedEpoch) {
            //if so, accept the election winner.
            //As, once someone declares a winner, we are done. We are not worried about / accounting for misbehaving peers.
            if(haveEnoughVotes(this.votesReceived, electionNotification)) {
                try {
                    Thread.sleep(finalizeWait);
                } catch (Exception ignore) {}
                if (this.incomingMessages.isEmpty()) {
                    updateVoteAndAcceptElectionWinner(electionNotification);
                }
            }
        }
        // n is from a LATER election epoch
        if (electionNotification.getPeerEpoch() > this.proposedEpoch) {
            //IF a quorum from that epoch are voting for the same peer as the vote of the FOLLOWING or LEADING peer whose vote I just received.
            //THEN accept their leader, and update my epoch to be their epoch
            updateVote(electionNotification.getProposedLeaderID(), electionNotification.getPeerEpoch());
            acceptElectionWinner(electionNotification);
        }
    }

    private void updateVoteAndAcceptElectionWinner(ElectionNotification electionNotification) {
        updateVote(electionNotification.getProposedLeaderID(), electionNotification.getPeerEpoch());
        acceptElectionWinner(electionNotification);
    }

    // Update all the other Servers in the cluster by sending them your current vote.
    private void sendNotifications() {
        ElectionNotification electionNotification = new ElectionNotification(this.proposedLeader, this.myPeerServer.getPeerState(), this.myPeerServer.getServerId(), this.proposedEpoch);
        byte[] msgContent = buildMsgContent(electionNotification);
        this.myPeerServer.sendBroadcast(MessageType.ELECTION, msgContent);
    }

    //////// Zookeeper Election Utility Methods /////////
    protected static ElectionNotification getNotificationFromMessage(Message msg) {
        // Build ElectionNotification from message content:
        byte[] messageContents = msg.getMessageContents();
        ByteBuffer byteBuffer = ByteBuffer.wrap(messageContents);
        long proposedLeaderID = byteBuffer.getLong();
        char stateChar = byteBuffer.getChar();
        long senderID = byteBuffer.getLong();
        long peerEpoch = byteBuffer.getLong();
        ZooKeeperPeerServer.ServerState state = ZooKeeperPeerServer.ServerState.getServerState(stateChar);
        return new ElectionNotification(proposedLeaderID,state,senderID,peerEpoch);
    }

    protected static byte[] buildMsgContent(ElectionNotification notification) {
        // Build Message content from Election Notifiction:
        // Allocate 8 bytes for each long: 8 * 3 = 24
        // Allocate 2 Bytes for each char: 2 * 1 = 02
        // Total bytes to build message content: = 26

        ByteBuffer byteBuffer = ByteBuffer.allocate(26);
        byteBuffer.clear();
        byteBuffer.putLong(notification.getProposedLeaderID());
        byteBuffer.putChar(notification.getState().getChar());
        byteBuffer.putLong(notification.getSenderID());
        byteBuffer.putLong(notification.getPeerEpoch());
        return byteBuffer.array();
    }
}