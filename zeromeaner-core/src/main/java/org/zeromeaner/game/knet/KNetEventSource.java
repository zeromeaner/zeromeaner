package org.zeromeaner.game.knet;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KNetEventSource implements KryoSerializable {
	protected int id;
	protected String type;
	protected String name;
	
	@Deprecated
	public KNetEventSource() {}
	
	public KNetEventSource(int id) {
		this.id = id;
	}
	
	public void updateFrom(KNetEventSource source) {
		this.type = source.getType();
		this.name = source.getName();
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return "(" + id + "," + type + "," + name + ")";
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof KNetEventSource) {
			return id == ((KNetEventSource) obj).id;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return id;
	}
	
	public KNetEvent event(Object... args) {
		return new KNetEvent(this, args);
	}
	
	@Override
	public void write(Kryo kryo, Output output) {
		output.writeInt(id, true);
		output.writeString(type);
		output.writeString(name);
	}

	@Override
	public void read(Kryo kryo, Input input) {
		id = input.readInt(true);
		type = input.readString();
		name = input.readString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
