package edu.yu.cs.com3800.stage5;

public class NoLeaderException extends Exception {

	public NoLeaderException() { super(); }
	public NoLeaderException(String message) { super(message); }
	public NoLeaderException(String message, Throwable cause) { super(message, cause); }
	public NoLeaderException(Throwable cause) { super(cause); }
	protected NoLeaderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) { super(message, cause, enableSuppression, writableStackTrace); }
}
