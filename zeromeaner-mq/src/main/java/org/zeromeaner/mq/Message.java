package org.zeromeaner.mq;

import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Output;

public class Message {
	public boolean reliable;
	public String topic;
	public byte[] buf;

	public Message() {}
	
	public Message(byte[] buf) {
		this(buf, true);
	}

	public Message(byte[] buf, boolean reliable) {
		this.buf = buf;
		this.reliable = reliable;
	}
}
