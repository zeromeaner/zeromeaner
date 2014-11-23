package org.zeromeaner.knet;

import org.zeromeaner.gui.knet.KNetPanel;

public class KNetPacket {
	public interface KNetFromClient {
		public final KNetPacket UPDATE_SOURCE = new KNetPacket("fc.source.update");
		public final KNetPacket AUTHENTICATE_USER = new KNetPacket("fc.user.authenticate");
		public final KNetPacket CREATE_USER = new KNetPacket("fc.user.create");
		public final KNetPacket UPDATE_PASSWORD = new KNetPacket("fc.user.password.update");

		public final KNetPacket LIST_CHANNELS = new KNetPacket("fc.channel.list");
		public final KNetPacket JOIN_CHANNEL = new KNetPacket("fc.channel.join");
		public final KNetPacket JOIN_CHANNEL_GAME = new KNetPacket("fc.channel.join.game");
		public final KNetPacket PART_CHANNEL = new KNetPacket("fc.channel.part");
		public final KNetPacket CREATE_CHANNEL = new KNetPacket("fc.channel.create");
		public final KNetPacket UPDATE_CHANNEL = new KNetPacket("fc.channel.update");
		public final KNetPacket DELETE_CHANNEL = new KNetPacket("fc.channel.delete");
		public final KNetPacket SEND_CHANNEL_MESSAGE = new KNetPacket("fc.channel.message.send");
	}
	
	public interface KNetFromServer {
		public final KNetPacket CONNECTED = new KNetPacket("fs.connected");
		public final KNetPacket DISCONNECTED = new KNetPacket("fs.disconnected");
		public final KNetPacket ASSIGN_SOURCE = new KNetPacket("fs.source.assign");
		
		public final KNetPacket CHANNELS_LISTED = new KNetPacket("fs.channel.listed");
		public final KNetPacket CHANNEL_JOINED = new KNetPacket("fs.channel.joined");
		public final KNetPacket CHANNEL_JOINED_GAME = new KNetPacket("fs.channel.joined.game");
		public final KNetPacket CHANNEL_PARTED = new KNetPacket("fs.channel.parted");
		public final KNetPacket CHANNEL_CREATED = new KNetPacket("fs.channel.created");
		public final KNetPacket CHANNEL_UPDATED = new KNetPacket("fs.channel.updated");
		public final KNetPacket CHANNEL_DELETED = new KNetPacket("fs.channel.deleted");
		public final KNetPacket CHANNEL_RECEIVED_MESSAGE = new KNetPacket("fs.channel.message.received");
	}
	
	protected String type;
	
	public KNetPacket(String type) {
		if(type == null)
			throw new IllegalArgumentException();
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this)
			return true;
		if(obj instanceof KNetPacket) {
			return type.equals(((KNetPacket) obj).type);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return type.hashCode();
	}
}
