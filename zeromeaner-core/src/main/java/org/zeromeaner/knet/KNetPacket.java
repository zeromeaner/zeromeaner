package org.zeromeaner.knet;

import org.zeromeaner.gui.knet.KNetPanel;

public class KNetPacket {
	public interface KNetFromClient {
		public final KNetPacket UPDATE_SOURCE = new KNetPacket("fc.source.update");
		public final KNetPacket AUTHENTICATE_USER = new KNetPacket("fc.user.authenticate", KNetEventArgs.PASSWORD);
		public final KNetPacket CREATE_USER = new KNetPacket("fc.user.create");
		public final KNetPacket UPDATE_PASSWORD = new KNetPacket("fc.user.password.update", KNetEventArgs.PASSWORD);

		public final KNetPacket LIST_CHANNELS = new KNetPacket("fc.channel.list");
		public final KNetPacket JOIN_CHANNEL = new KNetPacket("fc.channel.join", KNetEventArgs.CHANNEL_ID);
		public final KNetPacket JOIN_CHANNEL_GAME = new KNetPacket("fc.channel.join.game", KNetEventArgs.CHANNEL_ID);
		public final KNetPacket PART_CHANNEL = new KNetPacket("fc.channel.part", KNetEventArgs.CHANNEL_ID);
		public final KNetPacket PART_CHANNEL_GAME = new KNetPacket("fc.channel.part.game", KNetEventArgs.CHANNEL_ID);
		public final KNetPacket CREATE_CHANNEL = new KNetPacket("fc.channel.create", KNetEventArgs.CHANNEL_INFO);
		public final KNetPacket UPDATE_CHANNEL = new KNetPacket("fc.channel.update", KNetEventArgs.CHANNEL_ID, KNetEventArgs.CHANNEL_INFO);
		public final KNetPacket DELETE_CHANNEL = new KNetPacket("fc.channel.delete", KNetEventArgs.CHANNEL_ID);
		public final KNetPacket SEND_CHANNEL_MESSAGE = new KNetPacket("fc.channel.message.send", KNetEventArgs.CHANNEL_ID, KNetEventArgs.CHANNEL_CHAT_MESSAGE);
	}
	
	public interface KNetFromServer {
		public final KNetPacket CONNECTED = new KNetPacket("fs.connected");
		public final KNetPacket DISCONNECTED = new KNetPacket("fs.disconnected");
		public final KNetPacket ASSIGN_SOURCE = new KNetPacket("fs.source.assign", KNetEventArgs.USER);
		
		public final KNetPacket USER_CREATED = new KNetPacket("fs.user.created");
		public final KNetPacket USER_AUTHENTICATED = new KNetPacket("fs.user.authenticated");
		public final KNetPacket USER_NOT_AUTHENTICATED = new KNetPacket("fs.user.authenticated.no");
		public final KNetPacket USER_PASSWORD_UPDATED = new KNetPacket("fs.user.passed.updated");
		public final KNetPacket USER_PASSWORD_NOT_UPDATED = new KNetPacket("fs.user.passed.updated.no");
		
		public final KNetPacket CHANNELS_LISTED = new KNetPacket("fs.channel.listed", KNetEventArgs.CHANNEL_LISTING);
		public final KNetPacket CHANNEL_JOINED = new KNetPacket("fs.channel.joined", KNetEventArgs.CHANNEL_ID, KNetEventArgs.USER);
		public final KNetPacket CHANNEL_JOINED_GAME = new KNetPacket("fs.channel.joined.game", KNetEventArgs.CHANNEL_ID, KNetEventArgs.USER);
		public final KNetPacket CHANNEL_PARTED = new KNetPacket("fs.channel.parted", KNetEventArgs.CHANNEL_ID, KNetEventArgs.USER);
		public final KNetPacket CHANNEL_CREATED = new KNetPacket("fs.channel.created", KNetEventArgs.CHANNEL_ID, KNetEventArgs.CHANNEL_INFO);
		public final KNetPacket CHANNEL_UPDATED = new KNetPacket("fs.channel.updated", KNetEventArgs.CHANNEL_ID, KNetEventArgs.CHANNEL_INFO);
		public final KNetPacket CHANNEL_DELETED = new KNetPacket("fs.channel.deleted", KNetEventArgs.CHANNEL_ID);
		public final KNetPacket CHANNEL_RECEIVED_MESSAGE = new KNetPacket("fs.channel.message.received", KNetEventArgs.CHANNEL_ID, KNetEventArgs.CHANNEL_CHAT_MESSAGE);
	}
	
	protected String type;
	protected KNetEventArgs[] args;
	
	public KNetPacket(String type, KNetEventArgs... args) {
		if(type == null)
			throw new IllegalArgumentException();
		this.type = type;
		this.args = args;
	}
	
	public String getType() {
		return type;
	}
	
	public KNetEventArgs[] getArgs() {
		return args;
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
