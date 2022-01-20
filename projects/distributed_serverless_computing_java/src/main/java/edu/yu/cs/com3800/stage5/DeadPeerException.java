package edu.yu.cs.com3800.stage5;

public class DeadPeerException extends Exception {

	public DeadPeerException() { super(); }
	public DeadPeerException(String message) { super(message); }
	public DeadPeerException(String message, Throwable cause) { super(message, cause); }
	public DeadPeerException(Throwable cause) { super(cause); }
	public DeadPeerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) { super(message, cause, enableSuppression, writableStackTrace); }
}
