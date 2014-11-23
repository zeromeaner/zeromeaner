package org.zeromeaner.knet;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import org.funcish.core.fn.Predicate;
import org.mmmq.Topic;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KNetEvent extends EventObject implements KryoSerializable {
	private KNetPacket type;
	private Topic topic;
	private Map<KNetEventArgs, Object> args = new HashMap<KNetEventArgs, Object>();
	
	@Deprecated
	public KNetEvent() {
		super(new Object());
	}
	
	public KNetEvent(KNetEventSource source, KNetPacket type, Topic topic, Object... args) {
		super(source);
		this.type = type;
		this.topic = topic;
		for(int i = 0; i < args.length; i += 2) {
			if(i+1 < args.length && !(args[i+1] instanceof KNetEventArgs))
				this.args.put((KNetEventArgs) args[i], args[i+1]);
			else
				this.args.put((KNetEventArgs) args[i--], true);
		}
	}
	
	public KNetPacket getType() {
		return type;
	}
	
	public Topic getTopic() {
		return topic;
	}
	
	public boolean has(KNetEventArgs arg) {
		return args.containsKey(arg);
	}
	
	public boolean has(KNetEventArgs arg, Class<?> cls) {
		return cls.isInstance(args.get(arg));
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[source=" + super.getSource() + ", args=" + args + "]";
	}
	
	@Override
	public KNetEventSource getSource() {
		return (KNetEventSource) super.getSource();
	}
	
	public Object get(KNetEventArgs arg) {
		return args.get(arg);
	}
	
	public boolean isType(KNetPacket type) {
		return this.type == null ? type == null : this.type.equals(type);
	}
	
	public boolean is(Predicate<KNetEvent> p) {
		return p.test(this, null);
	}
	
	public <T> T get(KNetEventArgs arg, Class<T> cls) {
		return cls.cast(get(arg));
	}
	
	public void set(KNetEventArgs arg, Object value) {
		args.put(arg, value);
	}
	
	public Map<KNetEventArgs, Object> getArgs() {
		return args;
	}

	@Override
	public void write(Kryo kryo, Output output) {
		kryo.writeObject(output, getSource());
		kryo.writeClassAndObject(output, type);
		output.writeInt(args.size(), true);
		for(Map.Entry<KNetEventArgs, Object> e : args.entrySet()) {
			kryo.writeObject(output, e.getKey());
			e.getKey().write(kryo, output, e.getValue());
		}
	}

	@Override
	public void read(Kryo kryo, Input input) {
		try {
			source = kryo.readObject(input, KNetEventSource.class);
			KNetEventSource.class.cast(source);
			type = (KNetPacket) kryo.readClassAndObject(input);
			int size = input.readInt(true);
			for(int i = 0; i < size; i++) {
				KNetEventArgs arg = kryo.readObject(input, KNetEventArgs.class);
				Object val = arg.read(kryo, input);
				args.put(arg, val);
			}
		} catch(RuntimeException re) {
			throw new KryoException("Error reading " + this, re);
		}
	}
}
