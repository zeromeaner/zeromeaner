package org.zeromeaner.game.knet;

import java.util.EnumMap;
import java.util.EventObject;
import java.util.Map;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KNetEvent extends EventObject implements KryoSerializable {
	private Map<KNetEventArgs, Object> args = new EnumMap<KNetEventArgs, Object>(KNetEventArgs.class);
	
	@Deprecated
	public KNetEvent() {
		super(new Object());
	}
	
	public KNetEvent(KNetEventSource source, Object... args) {
		super(source);
		for(int i = 0; i < args.length; i += 2) {
			if(!(args[i+1] instanceof KNetEventArgs))
				this.args.put((KNetEventArgs) args[i], args[i+1]);
			else
				this.args.put((KNetEventArgs) args[i--], true);
		}
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[source=" + getSource() + ", args=" + args + "]";
	}
	
	@Override
	public KNetEventSource getSource() {
		return (KNetEventSource) super.getSource();
	}
	
	public Object get(KNetEventArgs arg) {
		return args.get(arg);
	}
	
	public boolean is(KNetEventArgs arg) {
		return args.containsKey(arg);
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
			output.writeInt(e.getKey().ordinal(), true);
			e.getKey().write(kryo, output, e.getValue());
		}
	}

	@Override
	public void read(Kryo kryo, Input input) {
		source = kryo.readObject(input, KNetEventSource.class);
		int size = input.readInt(true);
		for(int i = 0; i < size; i++) {
			int ordinal = input.readInt(true);
			KNetEventArgs arg = KNetEventArgs.values()[ordinal];
			Object val = arg.read(kryo, input);
			args.put(arg, val);
		}
	}
}
