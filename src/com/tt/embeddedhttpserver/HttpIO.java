package com.tt.embeddedhttpserver;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.StandardOpenOption;
import java.util.StringTokenizer;

public class HttpIO {
	
	private static boolean isHeadTerminated(String line) {
		return (line == null || line.isEmpty());
	}
	
	public static HttpHead readRequestHead(InputStream in) throws Exception {
		HttpHead head = new HttpHead();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line = null;
		int i = 0;
		while (!isHeadTerminated(line = br.readLine())) {
			System.out.println("> " + line);
			if (i == 0) {
				StringTokenizer st = new StringTokenizer(line, " ");
				if (st.countTokens() == 3) {
					head.setRequestMethod(st.nextToken().trim());
					String uri = st.nextToken().trim();
					int j = uri.indexOf("?");
					if (j == -1) {
						head.setRequestPath(uri);
					} else {
						head.setRequestPath(uri.substring(0, j));
						head.setRequestQueryString(uri.substring(j));
					}
					head.setProtocol(st.nextToken().trim());
				}
			} else {
				int j = line.indexOf(":");
				if (j != -1) {
					String name = line.substring(0, j);
					String value = line.substring(j + 2);
					head.getHeaders().put(name, value);
				}
			}
			i++;
		}
		return (head);
	}
	
	public static void writeResponseHead(HttpHead head, OutputStream out) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(head.getProtocol());
		sb.append(" ");
		sb.append(head.getResponseStatus());
		sb.append(" ");
		sb.append(head.getResponseMessage());
		sb.append("\n");
		System.out.println("< " + head.getProtocol() + " " + head.getResponseStatus() + " " + head.getResponseMessage());
		for (String key : head.getHeaders().keySet()) {
			sb.append(key);
			sb.append(": ");
			sb.append(head.getHeaders().get(key));
			sb.append("\n");
			System.out.println("< " + key + ": " + head.getHeaders().get(key));
		}
		sb.append("\n");
		out.write(sb.toString().getBytes("UTF-8"));
	}
	
	public static void writeResponseContent(File file, OutputStream out) throws Exception {
		FileChannel fc = new FileInputStream(file).getChannel();
		ByteBuffer buffer = ByteBuffer.allocate(32 * 1024);
		WritableByteChannel wbc = Channels.newChannel(out);
		while (fc.read(buffer) != -1) {
			buffer.flip();
			wbc.write(buffer);
			buffer.clear();
		}
		fc.close();
	}
	
	public static void writeResponseContent(File file, OutputStream out, long start, long end) throws Exception {
		FileChannel fc = new FileInputStream(file).getChannel();
		ByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, start, end - start + 1);
		WritableByteChannel wbc = Channels.newChannel(out);
		wbc.write(buffer);
		fc.close();
	}
	
	public static void writeResponseContent(String str, OutputStream out) throws Exception {
		out.write(str.getBytes("UTF-8"));
	}
	
}
