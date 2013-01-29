package org.zeromeaner.game.knet;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KNetChatMessage {
	private KNetEventSource source;
	private String username;
	private int room;
	private long timestamp;
	private String body;

	public KNetChatMessage() {
	}

	public KNetEventSource getSource() {
		return source;
	}

	public void setSource(KNetEventSource source) {
		this.source = source;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getRoom() {
		return room;
	}

	public void setRoom(int room) {
		this.room = room;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	
}
