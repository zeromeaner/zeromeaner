package org.zeromeaner.game.knet;

import java.util.EnumMap;
import java.util.EventObject;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KNetEvent extends EventObject implements KryoSerializable {
	public static enum NetEventArgs {
		ASSIGN_SOURCE,
		
		;
		
		public void write(Kryo kryo, Output output, Object argValue) {
			
		}
		
		public Object read(Kryo kryo, Input input) {
			return null;
		}
	}
	
	private Map<NetEventArgs, Object> args = new EnumMap<NetEventArgs, Object>(NetEventArgs.class);
	
	@Deprecated
	public KNetEvent() {
		super(new Object());
	}
	
	public KNetEvent(KNetEventSource source, Object... args) {
		super(source);
		for(int i = 0; i < args.length; i += 2) {
			this.args.put((NetEventArgs) args[i], args[i+1]);
		}
	}
	
	@Override
	public KNetEventSource getSource() {
		return (KNetEventSource) super.getSource();
	}
	
	public Object get(NetEventArgs arg) {
		return args.get(arg);
	}
	
	public boolean is(NetEventArgs arg) {
		return args.containsKey(arg);
	}
	
	public Map<NetEventArgs, Object> getArgs() {
		return args;
	}

	@Override
	public void write(Kryo kryo, Output output) {
		kryo.writeObject(output, getSource());
		output.writeInt(args.size(), true);
		for(Map.Entry<NetEventArgs, Object> e : args.entrySet()) {
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
			NetEventArgs arg = NetEventArgs.values()[ordinal];
			Object val = arg.read(kryo, input);
			args.put(arg, val);
		}
	}
}
