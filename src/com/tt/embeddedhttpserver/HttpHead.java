package com.tt.embeddedhttpserver;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class HttpHead {
	
	// common: protocol (e.g. "HTTP/1.1")
	private String protocol;
	
	// request: method (e.g. "GET")
	private String requestMethod;
	
	// request: path (e.g. "/test/index.html")
	private String requestPath;
	
	// request: query string (e.g. "?a=b&c=d")
	private String requestQueryString;
	
	// response: status (e.g. "200")
	private int responseStatus;

	// response: message (e.g. "OK")
	private String responseMessage;
	
	// common: headers
	private Map<String, String> headers;
	
	public HttpHead() {
		headers = new HashMap<String, String>();
	}
	
	public Date parseIfModifiedSince() {
		String ifModifiedSince = getHeaderValue("If-Modified-Since");
		if (ifModifiedSince == null) {
			return (null);
		}
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			return (sdf.parse(ifModifiedSince));
		} catch (Exception e) {
			Logger.w("error parsing date from IMS header value " + ifModifiedSince, e);
			return (null);
		}
	}
	
	public long[] parseRange(long fileSize) {
        String range = getHeaderValue("Range");
        long start = -1;
        long end = -1;
        if (range != null) {
            if (range.toLowerCase().startsWith("bytes=")) {
            	String s = range.substring(6);
            	int i = s.indexOf("-");
        		if (i == -1) {
        			return (null);
        		} else if (i == 0) {
        			start = fileSize - Long.parseLong(s.substring(1));
        			end = fileSize - 1;
        			return (new long[]{start, end});
        		} else if (i == s.length() - 1) {
        			start = Long.parseLong(s.substring(0, s.length() - 1));
        			end = fileSize - 1;
        			return (new long[]{start, end});
        		} else {
        			start = Long.parseLong(s.substring(0, i));
        			end = Long.parseLong(s.substring(i + 1));
        			return (new long[]{start, end});
        		}
            } else {
            	return (null);
            }
        } else {
        	return (null);
        }
	}
	
	public boolean closeConnection() {
		String connection = getHeaderValue("Connection");
		return (connection != null && "close".equalsIgnoreCase(connection));
	}
	
	public String getHeaderValue(String headerName) {
		for (String key : headers.keySet()) {
			if (key.equalsIgnoreCase(headerName)) {
				return (headers.get(key));
			}
		}
		return (null);
	}
	
	public static class Validator {
		
		public static boolean isValidRequestHead(HttpHead head) {
			if (head == null) {
				return (false);
			}
			// method: only GET supported for now
			if (head.getRequestMethod() == null || !head.getRequestMethod().equals("GET")) {
				return (false);
			}
			// path: must start with "/"
			if (head.getRequestPath() == null || !head.getRequestPath().startsWith("/")) {
				return (false);
			}
			// protocol: must be HTTP/1.0 or HTTP/1.1
			if (head.getProtocol() == null && !(head.getProtocol().equals("HTTP/1.0") || head.getProtocol().equals("HTTP/1.1"))) {
				return (false);
			}
			return (true);
		}
		
	}
	
	public static class Factory {
		
		public static HttpHead handle200(String str, String contentType, boolean closeConnection) {
			HttpHead head = new HttpHead();
			head.setProtocol("HTTP/1.1");
			head.setResponseStatus(200);
			head.setResponseMessage("Ok");
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Server", getServerHeader());
			headers.put("Date", getDateHeader(new Date()));
			headers.put("Last-Modified", getDateHeader(new Date()));
			headers.put("Content-Length", "" + str.length());
			if (contentType != null) {
				headers.put("Content-Type", contentType);
			}
			if (closeConnection) {
				headers.put("Connection", "close");
			}
			head.setHeaders(headers);
			return (head);
		}
		
		public static HttpHead handle200(File file, String contentType, boolean closeConnection) {
			HttpHead head = new HttpHead();
			head.setProtocol("HTTP/1.1");
			head.setResponseStatus(200);
			head.setResponseMessage("Ok");
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Server", getServerHeader());
			headers.put("Date", getDateHeader(new Date()));
			headers.put("Last-Modified", getDateHeader(new Date(file.lastModified())));
			headers.put("Content-Length", "" + file.length());
			if (contentType != null) {
				headers.put("Content-Type", contentType);
			}
			if (closeConnection) {
				headers.put("Connection", "close");
			}
			head.setHeaders(headers);
			return (head);
		}
		
		public static HttpHead handle206(File file, String contentType, boolean closeConnection, long start, long end) {
			HttpHead head = new HttpHead();
			head.setProtocol("HTTP/1.1");
			head.setResponseStatus(206);
			head.setResponseMessage("Partial content");
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Server", getServerHeader());
			headers.put("Date", getDateHeader(new Date()));
			headers.put("Last-Modified", getDateHeader(new Date(file.lastModified())));
			headers.put("Content-Length", "" + (end - start + 1));
			headers.put("Content-Range", "bytes " + start + "-" + end + "/" + file.length());
			headers.put("Accept-Ranges", "bytes");
			if (contentType != null) {
				headers.put("Content-Type", contentType);
			}
			if (closeConnection) {
				headers.put("Connection", "close");
			}
			head.setHeaders(headers);
			return (head);
		}
		
		public static HttpHead handle304(boolean closeConnection) {
			HttpHead head = new HttpHead();
			head.setProtocol("HTTP/1.1");
			head.setResponseStatus(304);
			head.setResponseMessage("Not modified");
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Server", getServerHeader());
			headers.put("Date", getDateHeader(new Date()));
			headers.put("Content-Length", "0");
			if (closeConnection) {
				headers.put("Connection", "close");
			}
			head.setHeaders(headers);
			return (head);
		}
		
		public static HttpHead handle400(boolean closeConnection) {
			HttpHead head = new HttpHead();
			head.setProtocol("HTTP/1.1");
			head.setResponseStatus(400);
			head.setResponseMessage("Bad request");
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Server", getServerHeader());
			headers.put("Date", getDateHeader(new Date()));
			headers.put("Content-Length", "0");
			if (closeConnection) {
				headers.put("Connection", "close");
			}
			head.setHeaders(headers);
			return (head);
		}
		
		public static HttpHead handle401(String realm, boolean closeConnection) {
			HttpHead head = new HttpHead();
			head.setProtocol("HTTP/1.1");
			head.setResponseStatus(401);
			head.setResponseMessage("Not authorized");
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Server", getServerHeader());
			headers.put("Date", getDateHeader(new Date()));
			headers.put("WWW-Authenticate", "basic");
			headers.put("Content-Length", "0");
			if (closeConnection) {
				headers.put("Connection", "close");
			}
			head.setHeaders(headers);
			return (head);
		}
		
		public static HttpHead handle403(boolean closeConnection) {
			HttpHead head = new HttpHead();
			head.setProtocol("HTTP/1.1");
			head.setResponseStatus(403);
			head.setResponseMessage("Forbidden");
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Server", getServerHeader());
			headers.put("Date", getDateHeader(new Date()));
			headers.put("Content-Length", "0");
			if (closeConnection) {
				headers.put("Connection", "close");
			}
			head.setHeaders(headers);
			return (head);
		}
		
		public static HttpHead handle404(boolean closeConnection) {
			HttpHead head = new HttpHead();
			head.setProtocol("HTTP/1.1");
			head.setResponseStatus(404);
			head.setResponseMessage("Not found");
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Server", getServerHeader());
			headers.put("Date", getDateHeader(new Date()));
			headers.put("Content-Length", "0");
			if (closeConnection) {
				headers.put("Connection", "close");
			}
			head.setHeaders(headers);
			return (head);
		}
		
		public static HttpHead handle500(boolean closeConnection) {
			HttpHead head = new HttpHead();
			head.setProtocol("HTTP/1.1");
			head.setResponseStatus(500);
			head.setResponseMessage("Internal server error");
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Server", getServerHeader());
			headers.put("Date", getDateHeader(new Date()));
			headers.put("Content-Length", "0");
			if (closeConnection) {
				headers.put("Connection", "close");
			}
			head.setHeaders(headers);
			return (head);
		}
		
	}
	
	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

	public String getRequestPath() {
		return requestPath;
	}

	public void setRequestPath(String requestPath) {
		this.requestPath = requestPath;
	}

	public String getRequestQueryString() {
		return requestQueryString;
	}

	public void setRequestQueryString(String requestQueryString) {
		this.requestQueryString = requestQueryString;
	}

	public int getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(int responseStatus) {
		this.responseStatus = responseStatus;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	
	private static String getDateHeader(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return (sdf.format(date));
	}
	
	private static String getServerHeader() {
		return ("EmbeddedHttpServer/" + Constants.VERSION + " (Java)");
	}
	
}
