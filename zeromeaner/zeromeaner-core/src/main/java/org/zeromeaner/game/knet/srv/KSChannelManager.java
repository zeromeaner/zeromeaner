package org.zeromeaner.game.knet.srv;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.zeromeaner.game.knet.KNetClient;
import org.zeromeaner.game.knet.KNetEvent;
import org.zeromeaner.game.knet.KNetEventSource;
import org.zeromeaner.game.knet.KNetListener;


import static org.zeromeaner.game.knet.KNetEventArgs.*;

public class KSChannelManager extends KNetClient implements KNetListener {
	protected Map<Integer, KSChannelInfo> channels = new HashMap<Integer, KSChannelInfo>();
	protected AtomicInteger nextChannelId = new AtomicInteger(-1);
	protected KSChannelInfo lobby;
	
	public KSChannelManager(int port) {
		this("localhost", port);
	}
	
	public KSChannelManager(String host, int port) {
		super("RoomManager", host, port);
		
		lobby = new KSChannelInfo(nextChannelId.incrementAndGet(), "lobby");
		
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
					PAYLOAD, channels.values().toArray(new KSChannelInfo[0]));
		}
		if(e.is(CHANNEL_JOIN) && e.is(CHANNEL_ID)) {
			int id = (Integer) e.get(CHANNEL_ID);
			if(!channels.containsKey(id)) {
				client.reply(e, ERROR, "Unknown channel id " + id);
				return;
			}
			KSChannelInfo info = channels.get(id);
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
			KSChannelInfo info = channels.get(id);
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
