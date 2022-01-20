package edu.yu.cs.com3800.stage5;

import org.junit.*;
import java.util.*;
import edu.yu.cs.com3800.Util;
import java.util.concurrent.*;
import java.net.http.HttpResponse;
import static edu.yu.cs.com3800.stage5.HeartBeatGossipMonitor.*;

public class Stage5Test {

	private ZooKeeperServerCluster cluster;
	private final int httpPort = 8888;
	private final int waitDeadPeer = FAIL + 25000;

	@BeforeClass
	public static void beforeClass() {
		System.out.println(" ");
		System.out.println("= = = = = = = = = = = = = = = = = = = = = = = = = = = =");
		System.out.println("Starting JUNIT Stage 5 Test at: " + Util.getDate());
		System.out.println("= = = = = = = = = = = = = = = = = = = = = = = = = = = =");
		System.out.println(" ");
	}

	@AfterClass
	public static void afterClass() {
		System.out.println(" ");
		System.out.println("= = = = = = = = = = = = = = = = = = = = = = = = = = = =");
		System.out.println("Finished JUNIT Stage 5 Test at: " + Util.getDate());
		System.out.println("= = = = = = = = = = = = = = = = = = = = = = = = = = = =");
		System.out.println(" ");
	}

	@Before
	public void before() { System.out.println("---- Test Started at: " + Util.getDate() + " ----"); }

	@After
	public void after() {
		if (cluster != null) {
			cluster.shutDownCluster();
		}
		System.out.println("\n---- Test Ended at: " + Util.getDate() + " ----\n");
	}

	@Test(timeout = 20 * 60000)  // Starting Port: 7000 numberServers: 4
	public void testSendingSingleClientRequest() {
		int numberServers = 5;
		int startingPort = 6000;
		makeClusterAndStart(numberServers, startingPort, httpPort);
		try {
			Future<HttpResponse<String>> future = sendRequest();
			System.out.println(getResponse(future).toString());
		} catch (Exception e) {
		} finally {
			cluster.shutDownCluster();
		}
	}

	@Test(timeout = 30 * 60000)
	public void testGossipHeartBeatProtocolKillLeader() {
		int numberServers = 7;
		int startingPort = 6200;
		makeClusterAndStart(numberServers, startingPort, httpPort);
		try {
			long leaderID = killLeader(cluster);
			Thread.sleep(waitDeadPeer);
			AssertionError err = null;
			for (ZooKeeperPeerServerImpl server : cluster.getServers()) {
				try {
					assertDeadPeer(server, leaderID);
				} catch (AssertionError e){
					err = new AssertionError(e);
				}
			}
			if (err != null) Assert.fail();
			else passed();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cluster.printLeaders();
			cluster.shutDownCluster();
		}
	}

	@Test(timeout = 20 * 60000)
	public void testGossipHeartBeatProtocolKillFollower() {
		int numberServers = 7;
		int startingPort = 6500;
		makeClusterAndStart(numberServers, startingPort, httpPort);
		try {
			long id = killFollower(cluster);
			Thread.sleep(waitDeadPeer);
			for (ZooKeeperPeerServerImpl server: cluster.getServers()) assertDeadPeer(server, id);
			passed();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			cluster.printLeaders();
			cluster.shutDownCluster();
		}
	}

	@Test(timeout = 30 * 60000)
	public void testKillLeaderAndFewFollowers() {
		int numberServers = 15;
		int startingPort = 7600;
		int numberOfRequests = 20;
		makeClusterAndStart(numberServers, startingPort, httpPort);
		int killCount = 2;
		List<Long> killedServers = new ArrayList<>();
		Queue<Future<HttpResponse<String>>> futures = new ArrayDeque<>();

		try {
			for (int i = 0; i < killCount; i++) {
				killedServers.add(killFollower(cluster));
				futures.addAll(sendRequests(numberOfRequests/killCount));
				Thread.sleep(5000);
			}
			long leaderID = killLeader(cluster);
			sleep(FAIL * killCount);

			for (ZooKeeperPeerServerImpl server: cluster.getServers()) for (Long id: killedServers) assertDeadPeer(server, id);

			sleep(FAIL * 2);
			for (ZooKeeperPeerServerImpl server: cluster.getServers()) assertDeadPeer(server, leaderID);

			verifyRequests(numberOfRequests, futures);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cluster.shutDownCluster();
		}
	}

	//////////////////////////////////////////////////

	private long killLeader(ZooKeeperServerCluster cluster) {
		try {
			List<ZooKeeperPeerServerImpl> list = cluster.getServers();
			ZooKeeperPeerServerImpl leader = cluster.getLeader();
			list.remove(leader);
			Long id = leader.getServerId();
			System.out.println("\n ======== " + getDate() + " Force Shutting Down -- LEADER-ID-" + id + " ======== \n");
			leader.shutdown();
			return id;
		} catch (NullPointerException e) {
			return -1;
		}
	}

	private long killFollower(ZooKeeperServerCluster cluster) {
		List<ZooKeeperPeerServerImpl> list = cluster.getServers();
		ZooKeeperPeerServerImpl s = list.get(0);
		list.remove(s);

		Long id = s.getServerId();
		System.out.println("\n" + getDate() + " Force Shutting Down Server-ID-" + id + "\n");
		s.shutdown();
		return id;
	}

	private Queue<Future<HttpResponse<String>>> sendRequests(int numberOfRequests) {
		System.out.println("\n ======== Sending " + numberOfRequests + " requests ======== \n");
		Queue<Future<HttpResponse<String>>> futures = new LinkedList<>();
		for (int i = 1; i <= numberOfRequests; i++) futures.offer(new Client("localhost", httpPort).submitRequest(getValidClass(i)));
		Assert.assertEquals(futures.size(), numberOfRequests);
		return futures;
	}

	private Future<HttpResponse<String>> sendRequest() {
		System.out.println("Sending request");
		Client client = new Client("localhost", httpPort);
		return client.submitRequest(getValidClass(100));
	}

	private void verifyRequests(int numberOfRequests, Queue<Future<HttpResponse<String>>> futures) {
		Assert.assertEquals(futures.size(), numberOfRequests);
		int requestsFulfilled = 0;

		System.out.println("\n----------------------------------");
		System.out.println(  "- Trying to verify requests sent -" + timeStamp());
		System.out.println(  "----------------------------------");

		while(!futures.isEmpty()) {
			Future<HttpResponse<String>> future = futures.poll();
			Response response = null;
			if (futures.size() == 1) {
				try {
					response = getResponse(future, 5, TimeUnit.MINUTES);
					Assert.assertNotNull(response);
					Assert.assertEquals(200, response.getCode());
					requestsFulfilled++;
					System.out.println("Requests Fulfilled: " + requestsFulfilled + " --- [ " + getDate() + " ] " );
				} catch (AssertionError e) {
					if (response != null) System.out.println(response.toString());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} catch(TimeoutException e) {
					futures.offer(future);
				}
			}
			else if (future.isDone()) {
				try {
					response = getResponse(future);
					Assert.assertNotNull(response);
					Assert.assertEquals(200, response.getCode());
					requestsFulfilled++;
					System.out.println("Requests Fulfilled: " + requestsFulfilled + " --- [ " + getDate() + " ] " );
				} catch (AssertionError e) {
					if (response != null) System.out.println(response.toString());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
			else {
				futures.offer(future);
			}
		}
		if (requestsFulfilled == numberOfRequests) passed();
		else Assert.fail();
	}

	private Response getResponse(Future<HttpResponse<String>> future, long time, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
		HttpResponse<String> response = future.get(time, unit);
		return new Response(response.statusCode(), response.body());
	}

	private Response getResponse(Future<HttpResponse<String>> future) throws ExecutionException, InterruptedException {
		HttpResponse<String> response = future.get();
		return new Response(response.statusCode(), response.body());
	}

	private void makeClusterAndStart(int numberServers, int startingPort, int httpPort) {
		cluster = new ZooKeeperServerCluster(numberServers, startingPort, 1, httpPort);
		cluster.startCluster();
		try { Thread.sleep(3000); } catch (InterruptedException e){}
	}

	private String getValidClass(int i) {
		int sleep = (new Random().nextBoolean()) ? 5000 : 2500;
		return  "\n" +
				"\npublic class HelloWorld {" +
				"\n    public String run() {" +
				"\n        try {" +
				"\n        		Thread.sleep(" + sleep + ");" +
				"\n        		return \"Hello World! " + i + "\";" +
				"\n        } catch(Exception e) {" +
				"\n             return \"I woke up early: " + i + "\";" +
				"\n        }" +
				"\n    }" +
				"\n}" +
				"\n";
	}

	private String getDate() { return Util.getDate();}

	private void assertDeadPeer(ZooKeeperPeerServerImpl server, long peerID) throws AssertionError{
		try {
			Assert.assertTrue(server.isPeerDead(peerID));
		} catch (AssertionError e) {
			System.out.println("Server-" + server.getServerId() + " failed to recognize that Server-" + String.format("%02d", peerID) + " is dead [Is peerDead(" + String.format("%02d", peerID) + ") = " + server.isPeerDead(peerID) + "] " + "ServerState = " + server.getPeerState().name() + timeStamp() );
			throw new AssertionError(e);
		}
	}

	private String timeStamp() { return " --- [" + Util.getDate() + "]"; }

	private void passed() { System.out.print("\n -----------\n|  PASSED!  |\n -----------\n"); }

}
