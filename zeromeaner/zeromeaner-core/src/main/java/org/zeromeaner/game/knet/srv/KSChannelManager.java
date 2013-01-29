package org.zeromeaner.game.knet.srv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.zeromeaner.game.knet.KNetClient;
import org.zeromeaner.game.knet.KNetEvent;
import org.zeromeaner.game.knet.KNetEventSource;
import org.zeromeaner.game.knet.KNetListener;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import static org.zeromeaner.game.knet.KNetEvent.NetEventArgs.*;

public class KSChannelManager extends KNetClient implements KNetListener {
	public static class ChannelInfo implements KryoSerializable {
		private int id;
		private String name;
		private List<KNetEventSource> members = new ArrayList<KNetEventSource>();
		
		public ChannelInfo() {}
		
		public ChannelInfo(int id, String name) {
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
			if(obj instanceof ChannelInfo) {
				return id == ((ChannelInfo) obj).id;
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
	
	protected Map<Integer, ChannelInfo> channels = new HashMap<Integer, ChannelInfo>();
	protected AtomicInteger nextChannelId = new AtomicInteger(-1);
	protected ChannelInfo lobby;
	
	public KSChannelManager(int port) {
		this("localhost", port);
	}
	
	public KSChannelManager(String host, int port) {
		super("RoomManager", host, port);
		
		lobby = new ChannelInfo(nextChannelId.incrementAndGet(), "lobby");
		
		channels.put(lobby.getId(), lobby);
		
		addKNetListener(this);
	}

	@Override
	public KSChannelManager start() throws IOException, InterruptedException {
		return (KSChannelManager) super.start();
	}
	
	@Override
	public void knetEvented(KNetClient client, KNetEvent e) {
		if(client.isLocal(e))
			return;
		if(e.is(CHANNEL_LIST)) {
			client.reply(e,
					CHANNEL_LIST, true,
					PAYLOAD, channels.values().toArray(new ChannelInfo[0]));
		}
		if(e.is(CHANNEL_JOIN) && e.is(CHANNEL_ID)) {
			int id = (Integer) e.get(CHANNEL_ID);
			if(!channels.containsKey(id)) {
				client.reply(e, ERROR, "Unknown channel id " + id);
				return;
			}
			ChannelInfo info = channels.get(id);
			if(info.getMembers().contains(e.getSource())) {
				client.reply(e, ERROR, "Already joined channel with id " + id);
				return;
			}
			info.getMembers().add(e.getSource());
			client.reply(e, 
					CHANNEL_JOIN, true,
					CHANNEL_ID, id,
					PAYLOAD, info);
		}
		if(e.is(CHANNEL_LEAVE) && e.is(CHANNEL_ID)) {
			int id = (Integer) e.get(CHANNEL_ID);
			if(!channels.containsKey(id)) {
				client.reply(e, ERROR, "Unknown channel id " + id);
				return;
			}
			ChannelInfo info = channels.get(id);
			if(lobby.equals(info)) {
				client.reply(e, ERROR, "Cannot leave lobby");
				return;
			}
			if(!info.getMembers().contains(e.getSource())) {
				client.reply(e, ERROR, "Not in channel with id " + id);
				return;
			}
			info.getMembers().remove(e.getSource());
			client.reply(e,
					CHANNEL_LEAVE, true,
					CHANNEL_ID, id,
					PAYLOAD, info);
		}
	}

}
