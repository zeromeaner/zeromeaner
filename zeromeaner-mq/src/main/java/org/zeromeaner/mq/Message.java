package org.zeromeaner.mq;

import java.io.ByteArrayOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
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
		this(null, buf, reliable);
	}
	
	public Message(String topic, boolean reliable) {
		this(topic, null, reliable);
	}
	
	public Message(String topic, byte[] buf, boolean reliable) {
		this.topic = topic;
		this.buf = buf;
		this.reliable = reliable;
	}
	
	public Object get(Kryo kryo) {
		Input input = new Input(buf);
		try {
			return kryo.readClassAndObject(input);
		} finally {
			input.close();
		}
	}
	
	public Message set(Kryo kryo, Object value) {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		Output output = new Output(buf);
		try {
			kryo.writeClassAndObject(output, value);
		} finally {
			output.close();
		}
		this.buf = buf.toByteArray();
		return this;
	}
}
