package org.zeromeaner.knet;

import java.util.EnumMap;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import org.funcish.core.fn.Predicate;
import org.kryomq.Topics;
import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.KryoException;
import org.kryomq.kryo.KryoSerializable;
import org.kryomq.kryo.io.Input;
import org.kryomq.kryo.io.Output;

public class KNetEvent extends EventObject implements KryoSerializable {
	private Map<KNetEventArgs, Object> args = new HashMap<KNetEventArgs, Object>();
	
	@Deprecated
	public KNetEvent() {
		super(new Object());
	}
	
	public KNetEvent(KNetEventSource source, Object... args) {
		super(source);
		for(int i = 0; i < args.length; i += 2) {
			if(i+1 < args.length && !(args[i+1] instanceof KNetEventArgs))
				this.args.put((KNetEventArgs) args[i], args[i+1]);
			else
				this.args.put((KNetEventArgs) args[i--], true);
		}
	}
	
	public String getTopic() {
		if(is(KNetEventArgs.USER_AUTHENTICATE) || is(KNetEventArgs.USER_CREATE) || is(KNetEventArgs.USER_UPDATE_PASSWORD))
			return Topics.PRIVILEGED + KNetTopics.AUTH;
		if(is(KNetEventArgs.ADDRESS))
			return get(KNetEventArgs.ADDRESS, KNetEventSource.class).getTopic();
		if(is(KNetEventArgs.CHANNEL_JOIN))
			return KNetTopics.CHANNEL;
		if(is(KNetEventArgs.CHANNEL_ID))
			return KNetTopics.CHANNEL + get(KNetEventArgs.CHANNEL_ID);
		return KNetTopics.GLOBAL;
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
	
	public boolean is(KNetEventArgs arg) {
		if(args.containsKey(arg) && arg.getType() != null && !arg.getType().isInstance(args.get(arg)) && args.get(arg) != null)
			new Throwable().printStackTrace();
		return args.containsKey(arg);
	}
	
	public boolean is(KNetEventArgs arg, Class<?> cls) {
		return cls.isInstance(get(arg));
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
		output.writeInt(args.size(), true);
		for(Map.Entry<KNetEventArgs, Object> e : args.entrySet()) {
//			output.writeInt(e.getKey().ordinal(), true);
			output.writeString(e.getKey().name());
			e.getKey().write(kryo, output, e.getValue());
		}
	}

	@Override
	public void read(Kryo kryo, Input input) {
		try {
			source = kryo.readObject(input, KNetEventSource.class);
			KNetEventSource.class.cast(source);
			int size = input.readInt(true);
			for(int i = 0; i < size; i++) {
//				int ordinal = input.readInt(true);
//				KNetEventArgs arg = KNetEventArgs.values()[ordinal];
				KNetEventArgs arg = KNetEventArgs.valueOf(input.readString());
				Object val = arg.read(kryo, input);
				args.put(arg, val);
			}
		} catch(RuntimeException re) {
			throw new KryoException("Error reading " + this, re);
		}
	}
}
