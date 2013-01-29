package org.zeromeaner.game.knet;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KNetEventSource implements KryoSerializable {
	protected int id;
	
	@Deprecated
	public KNetEventSource() {}
	
	public KNetEventSource(int id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return String.valueOf(id);
	}
	
	public KNetEvent event(Object... args) {
		return new KNetEvent(this, args);
	}
	
	@Override
	public void write(Kryo kryo, Output output) {
		output.writeInt(id, true);
	}

	@Override
	public void read(Kryo kryo, Input input) {
		id = input.readInt(true);
	}

}
