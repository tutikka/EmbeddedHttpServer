package com.tt.embeddedhttpserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientThread implements Runnable {
	
	private HttpServer httpServer;
	
	private Socket socket;
	
	private String id;
	
	public ClientThread(HttpServer httpServer, Socket socket, long clientCount) {
		this.httpServer = httpServer;
		this.socket = socket;
		StringBuilder sb = new StringBuilder();
		sb.append(clientCount);
		id = sb.toString();
		Logger.i("new connection from " + socket.getInetAddress().getHostAddress(), id);
	}
	
	@Override
	public void run() {
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		boolean closeConnection = false;
		try {
			
			in = new BufferedInputStream(socket.getInputStream());
			out = new BufferedOutputStream(socket.getOutputStream());
			
			// request head
			HttpHead head = HttpIO.readRequestHead(in);
			if (!HttpHead.Validator.isValidRequestHead(head)) {
				Logger.w("invalid http head found, please check logs", id, null); 
				HttpIO.writeResponseHead(HttpHead.Factory.handle400(closeConnection), out);
				return;
			}
			
			// request authentication
			if (httpServer.getBasicAccessAuthentication() != null) {
				int result = httpServer.getBasicAccessAuthentication().authenticate(head);
				if (result == BasicAccessAuthentication.AUTHENTICATION_REQUIRED) {
					HttpIO.writeResponseHead(HttpHead.Factory.handle401(httpServer.getBasicAccessAuthentication().getRealm(), closeConnection), out);
					return;
				}
				if (result == BasicAccessAuthentication.AUTHENTICATION_FORBIDDEN) {
					HttpIO.writeResponseHead(HttpHead.Factory.handle403(closeConnection), out);
					return;
				}
			}

			// tiny path
			String tinyPath = head.getRequestPath();
			String fullPath = httpServer.getTinyPaths().get(tinyPath);
			if (fullPath != null) {
				head.setRequestPath(fullPath);
				Logger.i("translated tiny path " + tinyPath + " to full path " + fullPath, id);
			}

			// request connection close
			closeConnection = head.closeConnection();
			Logger.i("client requested to close connection: " + closeConnection, id);
			
			// request filename
			String filename = head.getRequestPath();
			Logger.i("using filename " + filename, id);
			
			File file = new File(httpServer.getPageRoot(), filename);			
			Logger.i("mapped filename to absolute path " + file.getAbsolutePath(), id);
			if (file.exists() && file.canRead()) {
				if (file.isDirectory()) {
					for (String welcomeFile : httpServer.getWelcomeFiles()) {
						File f = new File(file.getAbsolutePath(), welcomeFile);
						if (f.exists() && f.canRead()) {
							Logger.i("found welcome file " + welcomeFile + ", handle file content", id);
							handleFileContent(head, f, closeConnection, out);
							return;
						}
					}
					if (httpServer.isAllowDirectoryIndexes()) {
						Logger.i(filename + " is a directory, handle directory list", id);
						handleDirectoryList(head, filename, file, closeConnection, out);
						return;
					} else {
						Logger.i(filename + " is a directory, but directory lists are not allowed", id);
						HttpIO.writeResponseHead(HttpHead.Factory.handle403(closeConnection), out);
						return;
					}
				} else {
					Logger.i(filename + " is a file, handle file content", id);
					handleFileContent(head, file, closeConnection, out);
					return;
				}
			} else {
				Logger.w("file " + filename + " does not exist or is not readable", id, null);
				HttpIO.writeResponseHead(HttpHead.Factory.handle404(closeConnection), out);
				return;
			}
		} catch (Exception e) {
			Logger.e("error processing connection", id, e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					Logger.w("error closing client output stream", e);
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					Logger.w("error closing client input stream", e);
				}
			}
			if (closeConnection) {
				closeSocket();
			}
		}
	}
	
	private void handleDirectoryList(HttpHead head, String path, File directory, boolean closeConnection, OutputStream out) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<head>");
		sb.append("</head>");
		sb.append("<body>");
		sb.append("<h1>Directory Index for " + path + "</h1>");
		sb.append("<table width='100%'>");
		File[] files = directory.listFiles();
		for (File file : files) {
			sb.append("<tr>");
			sb.append("<td>");
			sb.append("<a href='" + directory.getName() + "/" + file.getName() + "'>" + file.getName() + "</a>");
			sb.append("</td>");
			sb.append("<td>");
			sb.append(file.isDirectory() ? "-" : file.length() + " bytes");
			sb.append("</td>");
			sb.append("<td>");
			sb.append(file.isDirectory() ? "directory" : "file");
			sb.append("</td>");
			sb.append("<td>");
			sb.append(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(file.lastModified())));
			sb.append("</td>");			
			sb.append("</tr>");
		}
		sb.append("</table>");
		sb.append("</body>");
		sb.append("</html>");
		Logger.i("serving directory list from " + directory.getName() + " with " + files.length + " files", id);
		HttpIO.writeResponseHead(HttpHead.Factory.handle200(sb.toString(), "text/html", closeConnection), out);
		HttpIO.writeResponseContent(sb.toString(), out);
	}
	
	private void handleFileContent(HttpHead head, File file, boolean closeConnection, OutputStream out) throws Exception {
		
		// if-modified-since
		Date ims = head.parseIfModifiedSince();
		if (ims != null) {
			Logger.i("found IMS request with time " + ims, id);
			Date lm = new Date(file.lastModified());
			if (ims.after(lm)) {
				Logger.i("IMS after " + lm + ", respond with head only", id);
				HttpIO.writeResponseHead(HttpHead.Factory.handle304(closeConnection), out);
				return;
			} else {
				Logger.i("IMS not after " + lm + ", respond with full body", id);
			}
		}
		
		// content-type
		String extension = null;
		String contentType = null;
		if (file.getName().indexOf(".") != -1) {
			extension = file.getName().substring(file.getName().indexOf(".") + 1);
			Logger.i("parsed extension " + extension + " from filename", id);
			contentType = httpServer.getContentTypes().get(extension);
			if (contentType != null) {
				Logger.i("mapped content type " + contentType + " to extension", id);
			} else {
				Logger.w("no content type defined for extension " + extension, id, null);
			}
		} else {
			Logger.w("could not parse extension from filename", id, null);
		}
		
		// range
		long[] range = head.parseRange(file.length());
		if (range != null) {
			long start = range[0];
			long end = range[1];
			Logger.i("found range request " + start + "-" + end, id);
			Logger.i("serving range file content from " + file.getName() + " with size " + (end - start + 1), id);
			HttpIO.writeResponseHead(HttpHead.Factory.handle206(file, contentType, closeConnection, start, end), out);
			HttpIO.writeResponseContent(file, out, start, end);
		} else {
			Logger.i("serving file content from " + file.getName() + " with size " + file.length(), id);
			HttpIO.writeResponseHead(HttpHead.Factory.handle200(file, contentType, closeConnection), out);
			HttpIO.writeResponseContent(file, out);
		}

	}
	
	private void closeSocket() {
		if (socket != null) {
			try {
				socket.close();
				Logger.i("closed connection", id);
			} catch (Exception e) {
				Logger.e("error closing connection", id, e);
			}
		}
	}
	
}
