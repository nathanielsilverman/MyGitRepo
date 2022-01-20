package edu.yu.cs.com3800.stage5;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.*;

public class Client {

	private int hostPort;
	private String hostName;
	private ExecutorService executor;
	private static boolean verbose = false;

	public Client(String hostName, int hostPort) {
		this.hostName = hostName;
		this.hostPort = hostPort;
		this.executor = Executors.newCachedThreadPool();
	}

	public Client(int hostPort) {
		this.hostName = "localhost";
		this.hostPort = hostPort;
		this.executor = Executors.newCachedThreadPool();
	}

	public Future<HttpResponse<String>> submitRequest(String src) {
		Future<HttpResponse<String>> responseFuture = this.executor.submit(new Callable<HttpResponse<String>>() {
			@Override
			public HttpResponse<String> call() throws Exception {
				try {
					HttpRequest request = HttpRequest.newBuilder()
							.POST(HttpRequest.BodyPublishers.ofString(src))
							.version(HttpClient.Version.HTTP_1_1)
							.uri(new URI("http://" + hostName + ":" + hostPort + "/compileandrun"))
							.header("Content-Type", "text/x-java-source")
							.build();
					HttpClient client = HttpClient.newBuilder().build();

					try { if (verbose) System.out.println("Request: " + request.toString() + " " + request.headers().map().toString());
					} catch (Exception ignored){}

					return client.send(request, HttpResponse.BodyHandlers.ofString());
				} catch (InterruptedException | IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					System.out.println("Illegal URI");
				}
				return null;
			}
		});
		return responseFuture;
	}

	public static void main(String[] args) {

		int numRequest;
		int httpPort;
		try { numRequest = Integer.valueOf(args[0]); } catch (Exception e) { numRequest = 9; }
		try { httpPort = Integer.valueOf(args[1]); } catch (Exception e) { httpPort = 9; }
		try {
			if (args[2].equals("-v")) verbose = true;
		} catch (Exception e) {}

		Client c = new Client(httpPort);
		Queue<Future<HttpResponse<String>>> futures = new ArrayDeque<>();
		for (int i = 0; i < numRequest; i++) {
			futures.add(c.submitRequest(
					   "public class Factorial { \n" +
							"	public String run() { \n" +
							"		int n = " + (i+6) + "; \n" +
							"		int mul = 1; \n" +
							"		for (int i = 1; i <= n; i++) { \n" +
							"			mul = mul * i; \n" +
							"		}\n" +
							"		return \"Factorial of \" + n + \" : \" + mul; \n" +
							"	}\n" +
							"}"
			));
		}

		for (Future<HttpResponse<String>> future: futures) {
			try {
				HttpResponse<String> response = future.get();
				Response r = new Response(response.statusCode(), response.body());
				System.out.println(r.toString());
			} catch (ExecutionException | InterruptedException e) {}
		}
	}
}
