package com.tt.embeddedhttpserver;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerThread {

	private HttpServer httpServer;
	
	private ServerSocket serverSocket;
	
	private ServerWorker serverWorker;
	
	private static ServerThread instance;
	
	public static ServerThread getInstance() {
		if (instance == null) {
			instance = new ServerThread();
		}
		return (instance);
	}
	
	private ServerThread() {
	}
	
	public boolean isRunning() {
		return (serverWorker != null && serverWorker.isAlive());
	}
	
	public boolean requestStart(HttpServer httpServer) {
		if (isRunning()) {
			Logger.w("server already running, please stop first", null);
			return (false);
		}
		this.httpServer = httpServer;
		int port = httpServer.getPortNumber();
		try {
			serverSocket = new ServerSocket(port);
			Logger.i("accepting connections on port " + port);
			serverWorker = new ServerWorker();
			serverWorker.start();
			return (true);
		} catch (Exception e) {
			Logger.e("error accepting connections on port " + port, e);
			return (false);
		}
	}
	
	public boolean requestStop() {
		if (!isRunning()) {
			Logger.w("server not running, please start first", null);
			return (false);
		}
		try {
			serverWorker.interrupt();
			serverSocket.close();
			Logger.i("closed server socket");
			return (true);
		} catch (Exception e) {
			Logger.e("error closing server socket", e);
			return (false);
		}
	}
	
	private class ServerWorker extends Thread {
		
		private ExecutorService executorService;
		
		private long clientCount = 0;
		
		private boolean running = true;
		
		public void interrupt() {
			running = false;
		}
		
		@Override
		public void run() {
			executorService = Executors.newFixedThreadPool(httpServer.getMaxParallelConnections());
			Logger.i("client executor service created");
			while (running) {
				try {
					executorService.submit(new ClientThread(httpServer, serverSocket.accept(), clientCount++));
				} catch (Exception e) {
					if (running) {
						Logger.e("error accepting connection", e);
					}
				}
			}
			executorService.shutdown();
			Logger.i("client executor service shut down");
		}	
		
	}

}
