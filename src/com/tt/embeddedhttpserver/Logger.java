package com.tt.embeddedhttpserver;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
	
	public static void i(String message) {
		i(message, null);
	}
	
	public static void i(String message, String id) {
		StringBuilder sb = new StringBuilder();
		sb.append("[INFO   ] ");
		sb.append(formatCurrentDateAndTime());
		sb.append(" ");
		if (id != null) {
			sb.append("[");
			sb.append(id);
			sb.append("] ");
		}
		sb.append(message);
		System.out.println(sb.toString());
	}
	
	public static void w(String message, Exception exception) {
		w(message, null, exception);
	}
	
	public static void w(String message, String id, Exception exception) {
		StringBuilder sb = new StringBuilder();
		sb.append("[WARNING] ");
		sb.append(formatCurrentDateAndTime());
		sb.append(" ");
		if (id != null) {
			sb.append("[");
			sb.append(id);
			sb.append("] ");
		}
		sb.append(message);
		System.err.println(sb.toString());
		if (exception != null) {
			System.err.println(exception.getMessage());
			exception.printStackTrace(System.err);
		}
	}
	
	public static void e(String message, Exception exception) {
		e(message, null, exception);
	}
	
	public static void e(String message, String id, Exception exception) {
		StringBuilder sb = new StringBuilder();
		sb.append("[ERROR  ] ");
		sb.append(formatCurrentDateAndTime());
		sb.append(" ");
		if (id != null) {
			sb.append("[");
			sb.append(id);
			sb.append("] ");
		}
		sb.append(message);
		System.err.println(sb.toString());
		if (exception != null) {
			System.err.println(exception.getMessage());
			exception.printStackTrace(System.err);
		}
	}
	
	private static String formatCurrentDateAndTime() {
		return (DATE_FORMAT.format(new Date()));
	}
	
}
