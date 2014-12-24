package org.zeromeaner.util;

public abstract class Session {
	
	public static final String ANONYMOUS_USER = "anonymous";

	private static String user = ANONYMOUS_USER;
	
	public static String getUser() {
		return user;
	}
	
	public static void setUser(String user) {
		Session.user = user;
	}
	
	private Session() {}
}
