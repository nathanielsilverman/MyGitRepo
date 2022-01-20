package edu.yu.cs.com3800.stage5;

public class Demo {

	public static void main(String[] args) {
		try {
			int index = Integer.valueOf(args[0]);
			ZookeeperRunner.exec(index);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
