package org.zeromeaner.dbo;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class User {
	private Integer usedId;
	private String email;
	private String sha1PwHex;
	
	public Integer getUsedId() {
		return usedId;
	}
	public void setUsedId(Integer usedId) {
		this.usedId = usedId;
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
	public byte[] getSha1Pw() {
		if(sha1PwHex == null)
			return null;
		try {
			return Hex.decodeHex(sha1PwHex.toCharArray());
		} catch(DecoderException e) {
			throw new RuntimeException(e);
		}
	}
	public void setSha1Pw(byte[] sha1Pw) {
		if(sha1Pw == null)
			sha1PwHex = null;
		else
			sha1PwHex = Hex.encodeHexString(sha1Pw);
	}
}
