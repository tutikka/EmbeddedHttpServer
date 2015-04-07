package com.tt.embeddedhttpserver;

import java.util.Base64;

public class BasicAccessAuthentication {

	public static final int AUTHENTICATION_OK = 0;
	
	public static final int AUTHENTICATION_REQUIRED = 1;
	
	public static final int AUTHENTICATION_FORBIDDEN = 2;
	
	private String realm;
	
	private String username;
	
	private String password;

	public BasicAccessAuthentication() {
	}
	
	public BasicAccessAuthentication(String realm, String username, String password) {
		this.realm = realm;
		this.username = username;
		this.password = password;
	}

	public int authenticate(HttpHead head) {
		String authorization = head.getHeaderValue("Authorization");
		if (authorization == null) {
			return (BasicAccessAuthentication.AUTHENTICATION_REQUIRED);
		} else {
			String server = "Basic ";
			try {
				server += Base64.getEncoder().encodeToString((username + ":" + password).getBytes("UTF-8"));
			} catch (Exception e) {
			}
			if (authorization.equals(server)) {
				return (BasicAccessAuthentication.AUTHENTICATION_OK);
			} else {
				return (BasicAccessAuthentication.AUTHENTICATION_FORBIDDEN);
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ realm = ");
		sb.append(realm);
		sb.append(", username = ");
		sb.append(username);
		sb.append(", password = ** }");
		return (sb.toString());
	}

	public String getRealm() {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
}
