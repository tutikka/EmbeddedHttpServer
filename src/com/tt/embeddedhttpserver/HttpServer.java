package com.tt.embeddedhttpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HttpServer {

	private String pageRoot;
	
	private int portNumber;
	
	private int maxParallelConnections;
	
	private boolean allowDirectoryIndexes;
	
	private Map<String, String> contentTypes;
	
	private Set<String> welcomeFiles;

	private BasicAccessAuthentication basicAccessAuthentication;

	private Map<String, String> aliases;

	private boolean allowOnlyAliases;

	public static class Builder {
		
		private String pageRoot = "/tmp";
		
		private int portNumber = 10001;
		
		private int maxParallelConnections = 10;
		
		private boolean allowDirectoryIndexes = false;
		
		private Map<String, String> contentTypes = new HashMap<String, String>();
		
		private Set<String> welcomeFiles = new HashSet<String>();
		
		private BasicAccessAuthentication basicAccessAuthentication = null;

		private Map<String, String> aliases = new HashMap<String, String>();

		private boolean allowOnlyAliases = false;

		public Builder() {
			// default mime types: web
			contentTypes.put("html", "text/html");
			contentTypes.put("htm", "text/html");
			contentTypes.put("css", "text/css");
			contentTypes.put("js", "text/javascript");
			contentTypes.put("txt", "text/plain");
			contentTypes.put("xml", "text/xml");
			contentTypes.put("log", "text/plain");
			
			// default mime types: application
			contentTypes.put("ttf", "application/x-font-ttf");
			contentTypes.put("otf", "application/x-font-opentype");
			contentTypes.put("woff", "application/font-woff");
			contentTypes.put("eot", "application/vnd.ms-fontobject");
			
			// default mime types: image
			contentTypes.put("jpg", "image/jpeg");
			contentTypes.put("jpeg", "image/jpeg");
			contentTypes.put("png", "image/png");
			contentTypes.put("gif", "image/gif");
			contentTypes.put("svg", "image/svg+xml");
			contentTypes.put("ico", "image/x-icon");
			
			// default mime types: video
			contentTypes.put("mov", "video/quicktime");
			contentTypes.put("qt", "video/quicktime");
			contentTypes.put("mpeg", "video/mpeg");
			contentTypes.put("mpg", "video/mpeg");
			contentTypes.put("mp4", "video/mp4");
			
			// default mime types: audio
			contentTypes.put("mid", "audio/mid");
			contentTypes.put("mp3", "audio/mpeg");
			contentTypes.put("wav", "audio/x-wav");
			
			// default welcome files
			welcomeFiles.add("index.html");
			welcomeFiles.add("index.htm");
		}
		
		public Builder setPageRoot(String pageRoot) {
			this.pageRoot = pageRoot;
			return (this);
		}
		
		public Builder setPortNumber(int portNumber) {
			this.portNumber = portNumber;
			return (this);
		}
		
		public Builder setMaxParallelConnections(int maxParallelConnections) {
			this.maxParallelConnections = maxParallelConnections;
			return (this);
		}
		
		public Builder setAllowDirectoryIndexes(boolean allowDirectoryIndexes) {
			this.allowDirectoryIndexes = allowDirectoryIndexes;
			return (this);
		}
		
		public Builder addContentType(String extension, String type) {
			this.contentTypes.put(extension, type);
			return (this);
		}
		
		public Builder setContentTypes(Map<String, String> contentTypes) {
			this.contentTypes = contentTypes;
			return (this);
		}
		
		public Builder addWelcomeFile(String filename) {
			this.welcomeFiles.add(filename);
			return (this);
		}
		
		public Builder setWelcomeFiles(Set<String> welcomeFiles) {
			this.welcomeFiles = welcomeFiles;
			return (this);
		}

		public Builder setBasicAccessAuthentication(BasicAccessAuthentication basicAccessAuthentication) {
			this.basicAccessAuthentication = basicAccessAuthentication;
			return (this);
		}

		public Builder setAliases(Map<String, String> aliases) {
			this.aliases = aliases;
			return (this);
		}

		public Builder addAlias(String alias, String path) {
			this.aliases.put(alias, path);
			return (this);
		}

		public Builder setAllowOnlyAliases(boolean allowOnlyAliases) {
			this.allowOnlyAliases = allowOnlyAliases;
			return (this);
		}

		public HttpServer build() {
			return (new HttpServer(pageRoot, portNumber, maxParallelConnections, allowDirectoryIndexes, contentTypes, welcomeFiles, basicAccessAuthentication, aliases, allowOnlyAliases));
		}
		
	}
	
	private HttpServer(String pageRoot, int portNumber, int maxParallelConnections, boolean allowDirectoryIndexes, Map<String, String> contentTypes, Set<String> welcomeFiles, BasicAccessAuthentication basicAccessAuthentication, Map<String, String> aliases, boolean allowOnlyAliases) {
		this.pageRoot = pageRoot;
		this.portNumber = portNumber;
		this.maxParallelConnections = maxParallelConnections;
		this.allowDirectoryIndexes = allowDirectoryIndexes;
		this.contentTypes = contentTypes;
		this.welcomeFiles = welcomeFiles;
		this.basicAccessAuthentication = basicAccessAuthentication;
		this.aliases = aliases;
		this.allowOnlyAliases = allowOnlyAliases;
		Logger.i("config: pageRoot = " + pageRoot);
		Logger.i("config: portNumber = " + portNumber);
		Logger.i("config: maxParallelConnections = " + maxParallelConnections);
		Logger.i("config: allowDirectoryIndexes = " + allowDirectoryIndexes);
		Logger.i("config: contentTypes = " + contentTypes);
		Logger.i("config: welcomeFiles = " + welcomeFiles);
		Logger.i("config: basicAccessAuthentication = " + basicAccessAuthentication);
		Logger.i("config: aliases = " + aliases);
		Logger.i("config: allowOnlyAliases = " + allowOnlyAliases);
	}

	public String getPageRoot() {
		return pageRoot;
	}

	public int getPortNumber() {
		return portNumber;
	}

	public int getMaxParallelConnections() {
		return maxParallelConnections;
	}

	public boolean isAllowDirectoryIndexes() {
		return allowDirectoryIndexes;
	}

	public Map<String, String> getContentTypes() {
		return contentTypes;
	}

	public Set<String> getWelcomeFiles() {
		return welcomeFiles;
	}

	public BasicAccessAuthentication getBasicAccessAuthentication() {
		return basicAccessAuthentication;
	}

	public void startServer() {
		ServerThread.getInstance().requestStart(this);
	}

	public void stopServer() {
		ServerThread.getInstance().requestStop();
	}

	public boolean isAllowOnlyAliases() {
		return allowOnlyAliases;
	}

	public void addAlias(String alias, String path) {
		if (alias == null || alias.length() == 0) {
			Logger.w("error setting alias: null or empty alias", null);
			return;
		}
		if (!alias.startsWith("/")) {
			alias = "/" + alias;
			Logger.i("prepended '/' to alias");
		}
		if (path == null || path.length() == 0) {
			Logger.w("error setting alias: null or empty full path", null);
			return;
		}
		if (!path.startsWith("/")) {
			path = "/" + path;
			Logger.i("prepended '/' to full path");
		}
		aliases.put(alias, path);
		Logger.i("set alias " + alias + " -> " + path);
	}

	public void removeAlias(String alias) {
		aliases.remove(alias);
		Logger.i("remove alias " + alias);
	}

	public Map<String, String> getAliases() {
		return (aliases);
	}

	public static void printHelp() {
		System.out.println("-----------------------------");
		System.out.println("| Embedded Http Server Shell |");
		System.out.println("------------------------------");
		System.out.println("| start |   Start the server |");
		System.out.println("| stop  |    Stop the server |");
		System.out.println("| help  |    Print this help |");
		System.out.println("| quit  |         Exit shell |");
		System.out.println("------------------------------");
	}
	
	public static void main(String[] args) throws IOException {
		printHelp();
		HttpServer httpServer = new HttpServer.Builder()
			.setPageRoot("/tmp")
			.setPortNumber(10001)
			.setMaxParallelConnections(10)
			.setAllowDirectoryIndexes(true)
			.setBasicAccessAuthentication(new BasicAccessAuthentication("realm", "username", "password"))
			.addWelcomeFile("index.html")
			.addContentType("mp4", "video/mp4")
			.addAlias("/abc", "/index.html")
			.setAllowOnlyAliases(true)
			.build();
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			String line = in.readLine();
			if ("start".equals(line)) {
				httpServer.startServer();
			}
			if ("stop".equals(line)) {
				httpServer.stopServer();
			}
			if ("help".equals(line)) {
				printHelp();
			}
			if ("quit".equals(line)) {
				if (ServerThread.getInstance().isRunning()) {
					ServerThread.getInstance().requestStop();
				}
				break;
			}
		}
	}
	
}
