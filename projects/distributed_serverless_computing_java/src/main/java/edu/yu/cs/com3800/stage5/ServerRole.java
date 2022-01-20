package edu.yu.cs.com3800.stage5;

public class ServerRole extends Thread {

	public void shutdown(){ interrupt(); }

	@Override
	public void interrupt() { super.interrupt(); }
}
