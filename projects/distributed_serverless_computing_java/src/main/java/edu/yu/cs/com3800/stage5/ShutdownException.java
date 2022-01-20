package edu.yu.cs.com3800.stage5;

public class ShutdownException extends Exception {

	public ShutdownException() { super(); }
	public ShutdownException(String message) { super(message); }
	public ShutdownException(String message, Throwable cause) { super(message, cause); }
	public ShutdownException(Throwable cause) { super(cause); }
	protected ShutdownException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) { super(message, cause, enableSuppression, writableStackTrace); }

}
