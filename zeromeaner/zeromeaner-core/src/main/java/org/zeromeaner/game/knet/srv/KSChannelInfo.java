package org.zeromeaner.game.knet.srv;

import java.util.ArrayList;
import java.util.List;

import org.zeromeaner.game.knet.KNetEventSource;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KSChannelInfo implements KryoSerializable {
	private int id;
	private String name;
	private List<KNetEventSource> members = new ArrayList<KNetEventSource>();
	
	public KSChannelInfo() {}
	
	public KSChannelInfo(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "[" + id + ":" + name + "]";
	}
	
	@Override
	public int hashCode() {
		return id;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof KSChannelInfo) {
			return id == ((KSChannelInfo) obj).id;
		}
		return false;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public List<KNetEventSource> getMembers() {
		return members;
	}
	
	public void setMembers(List<KNetEventSource> members) {
		this.members = members;
	}
	
	@Override
	public void write(Kryo kryo, Output output) {
		output.writeInt(id, true);
		output.writeString(name);
		output.writeInt(members.size(), true);
		for(KNetEventSource m : members) {
			kryo.writeObject(output, m);
		}
	}
	
	@Override
	public void read(Kryo kryo, Input input) {
		id = input.readInt(true);
		name = input.readString();
		int msize = input.readInt(true);
		for(int i = 0; i < msize; i++)
			members.add(kryo.readObject(input, KNetEventSource.class));
	}
}