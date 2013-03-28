package org.zeromeaner.game.knet.srv;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.zeromeaner.game.knet.KNetClient;
import org.zeromeaner.game.knet.KNetEvent;
import org.zeromeaner.game.knet.KNetListener;
import org.zeromeaner.game.knet.obj.KNetChannelInfo;
import org.zeromeaner.game.knet.obj.KNetPlayerInfo;


import static org.zeromeaner.game.knet.KNetEventArgs.*;

public class KNetChannelManager extends KNetClient implements KNetListener {
	protected Map<Integer, KNetChannelInfo> channels = new HashMap<Integer, KNetChannelInfo>();
	protected AtomicInteger nextChannelId = new AtomicInteger(-1);
	protected KNetChannelInfo lobby;
	
	public KNetChannelManager(int port) {
		this("localhost", port);
	}
	
	public KNetChannelManager(String host, int port) {
		super("RoomManager", host, port);
		
		lobby = new KNetChannelInfo(nextChannelId.incrementAndGet(), "lobby");
		
		channels.put(lobby.getId(), lobby);
		
		addKNetListener(this);
	}

	@Override
	public KNetChannelManager start() throws IOException, InterruptedException {
		return (KNetChannelManager) super.start();
	}
	
	@Override
	public void knetEvented(KNetClient client, KNetEvent e) {
		if(client.isLocal(e))
			return;
		if(e.is(CHANNEL_LIST)) {
			client.reply(e,
					CHANNEL_LIST,
					CHANNEL_INFO, channels.values().toArray(new KNetChannelInfo[0]));
		}
		if(e.is(CHANNEL_JOIN) && e.is(CHANNEL_ID)) {
			int id = (Integer) e.get(CHANNEL_ID);
			if(!channels.containsKey(id)) {
				client.reply(e, ERROR, "Unknown channel id " + id);
				return;
			}
			KNetChannelInfo info = channels.get(id);
			if(info.getMembers().contains(e.getSource())) {
				client.reply(e, ERROR, "Already joined channel with id " + id);
				return;
			}
			info.getMembers().add(e.getSource());
			KNetPlayerInfo newPlayer = null;
			if(info.getPlayers().size() < info.getMaxPlayers()) {
				info.getPlayers().add(e.getSource());
				newPlayer = new KNetPlayerInfo();
				newPlayer.setChannel(info);
				newPlayer.setPlayer(e.getSource());
				info.getPlayerInfo().add(newPlayer);
			}
			client.reply(e, 
					CHANNEL_JOIN,
					CHANNEL_ID, id,
					PAYLOAD, e.getSource(),
					CHANNEL_INFO, new KNetChannelInfo[] { info });
			if(newPlayer != null)
				client.fireTCP(PLAYER_ENTER, newPlayer, CHANNEL_ID, info.getId());
			if(info.getPlayers().size() >= 2 && info.isAutoStart())
				client.fireTCP(AUTOSTART_BEGIN, 10, CHANNEL_ID, info.getId());
		}
		if(e.is(CHANNEL_CREATE)) {
			KNetChannelInfo request = (KNetChannelInfo) e.get(CHANNEL_CREATE);
			for(KNetChannelInfo ci : channels.values()) {
				if(request.getName().equals(ci.getName())) {
					client.reply(e, ERROR, "Cannot create duplicate channel named " + request.getName());
					return;
				}
			}
//			KNetChannelInfo create = new KNetChannelInfo(nextChannelId.incrementAndGet(), request.getName());
			request.setId(nextChannelId.incrementAndGet());
			channels.put(request.getId(), request);
			client.fireTCP(CHANNEL_LIST, CHANNEL_INFO, channels.values().toArray(new KNetChannelInfo[0]));
		}
		if(e.is(CHANNEL_DELETE)) {
			int id = (Integer) e.get(CHANNEL_DELETE);
			KNetChannelInfo info = channels.get(id);
			if(id == KNetChannelInfo.LOBBY_CHANNEL_ID) {
				client.reply(e, ERROR, "Cannot delete lobby");
			} else if(info.getMembers().size() == 0) {
				channels.remove(id);
				client.fireTCP(CHANNEL_LIST, CHANNEL_INFO, channels.values().toArray(new KNetChannelInfo[0]));
			} else
				client.reply(e, ERROR, "Cannot delete channel with members");
		}
		if(e.is(CHANNEL_LEAVE) && e.is(CHANNEL_ID)) {
			int id = (Integer) e.get(CHANNEL_ID);
			if(!channels.containsKey(id)) {
				client.reply(e, ERROR, "Unknown channel id " + id);
				return;
			}
			KNetChannelInfo info = channels.get(id);
			if(!info.getMembers().contains(e.getSource())) {
				client.reply(e, ERROR, "Not in channel with id " + id);
				return;
			}
			if(info.getId() != KNetChannelInfo.LOBBY_CHANNEL_ID)
				info.depart(e.getSource());
			client.fireTCP(
					CHANNEL_LEAVE, 
					CHANNEL_ID, id,
					PAYLOAD, e.getSource(),
					CHANNEL_INFO, new KNetChannelInfo[] { info });
		}
		if(e.is(DISCONNECTED)) {
			for(KNetChannelInfo info : channels.values()) {
				if(!info.getMembers().contains(e.getSource()))
					continue;
				info.depart(e.getSource());
				client.fireTCP(
						CHANNEL_LEAVE,
						CHANNEL_ID, info.getId(),
						PAYLOAD, e.getSource(),
						CHANNEL_INFO, new KNetChannelInfo[] { info });
			}
		}
	}

}
