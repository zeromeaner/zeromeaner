package org.zeromeaner.dbo;

public class User {
	private Integer userId;
	private String email;
	private String sha1PwHex;
	
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getSha1PwHex() {
		return sha1PwHex;
	}
	public void setSha1PwHex(String sha1PwHex) {
		this.sha1PwHex = sha1PwHex;
	}
}
