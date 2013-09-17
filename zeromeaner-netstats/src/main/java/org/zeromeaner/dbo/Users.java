package org.zeromeaner.dbo;

import java.security.MessageDigest;

import org.apache.commons.codec.binary.Hex;
import org.apache.ibatis.session.SqlSession;

public class Users {
	public static User select(int id) {
		SqlSession s = Mappers.getFactory().openSession();
		try {
			return s.getMapper(UserMapper.class).selectId(id);
		} finally {
			s.close();
		}
	}
	
	public static User select(String email) {
		SqlSession s = Mappers.getFactory().openSession();
		try {
			return s.getMapper(UserMapper.class).selectEmail(email);
		} finally {
			s.close();
		}
	}
	
	private static void insert(User user) {
		SqlSession s = Mappers.getFactory().openSession();
		try {
			s.getMapper(UserMapper.class).insert(user);
			s.commit();
		} finally {
			s.close();
		}
	}
	
	private static void update(User user) {
		SqlSession s = Mappers.getFactory().openSession();
		try {
			s.getMapper(UserMapper.class).update(user);
			s.commit();
		} finally {
			s.close();
		}
	}
	
	private static String sha1(String pw) {
		if(pw == null)
			return null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(pw.getBytes("UTF-8"));
			return new String(Hex.encodeHex(md.digest()));
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
		
	}
	
	public static User insert(String email, String password) {
		if(select(email) != null)
			throw new IllegalStateException("User with email \"" + email + "\" already exists");
		User u = new User();
		u.setEmail(email);
		u.setSha1PwHex(sha1(password));
		insert(u);
		return select(email);
	}
	
	public static boolean checkPassword(String email, String password) {
		return checkPassword(select(email), password);
	}
	
	public static boolean checkPassword(User user, String password) {
		return (user == null || user.getSha1PwHex() == null) && password == null || sha1(password).equals(select(user.getUserId()).getSha1PwHex());
	}
	
	public static void update(User user, String password) {
		if(!checkPassword(user, password))
			throw new IllegalArgumentException("Incorrect password");
		update(user);
	}
	
	public static User updatePassword(User user, String password, String newPassword) {
		if(!checkPassword(user, password))
			throw new IllegalArgumentException("Incorrect password");
		User u = select(user.getUserId());
		u.setSha1PwHex(sha1(newPassword));
		update(u);
		return select(user.getUserId());
	}
	
	private Users() {}
}
