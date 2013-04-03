package org.zeromeaner.knet.srv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.text.html.HTMLDocument.HTMLReader.SpecialAction;

import org.zeromeaner.knet.KNetClient;
import org.zeromeaner.knet.KNetEvent;
import org.zeromeaner.knet.KNetEventArgs;
import org.zeromeaner.knet.KNetEventSource;
import org.zeromeaner.knet.KNetListener;
import org.zeromeaner.knet.obj.KNStartInfo;
import org.zeromeaner.knet.obj.KNetChannelInfo;
import org.zeromeaner.knet.obj.KNetPlayerInfo;


import static org.zeromeaner.knet.KNetEventArgs.*;

public class KNetChannelManager extends KNetClient implements KNetListener {
	protected class ChannelState {
		protected KNetChannelInfo channel;
		protected Set<KNetEventSource> requiredAutostartResponses = new HashSet<KNetEventSource>();
		protected Set<KNetEventSource> living = new HashSet<KNetEventSource>();
		
		public ChannelState(KNetChannelInfo channel) {
			this.channel = channel;
		}
	}
	
	protected Map<Integer, KNetChannelInfo> channels = new HashMap<Integer, KNetChannelInfo>();
	protected Map<KNetChannelInfo, ChannelState> states = new HashMap<KNetChannelInfo, ChannelState>();
	protected AtomicInteger nextChannelId = new AtomicInteger(-1);
	protected KNetChannelInfo lobby;
	
	public KNetChannelManager(int port) {
		this("localhost", port);
	}
	
	public KNetChannelManager(String host, int port) {
		super("RoomManager", host, port);
		
		lobby = new KNetChannelInfo(nextChannelId.incrementAndGet(), "lobby");
		
		channels.put(lobby.getId(), lobby);
		states.put(lobby, new ChannelState(lobby));
		
		addKNetListener(this);
	}

	@Override
	public KNetChannelManager start() throws IOException, InterruptedException {
		return (KNetChannelManager) super.start();
	}
	
	public synchronized List<KNetEventSource> getMembers(int channelId) {
		List<KNetEventSource> ret = new ArrayList<KNetEventSource>();
		if(channels.containsKey(channelId))
			ret.addAll(channels.get(channelId).getMembers());
		return ret;
	}
	
	@Override
	public synchronized void knetEvented(KNetClient client, KNetEvent e) {
		if(client.isLocal(e))
			return;
		try {
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
					boolean alreadyJoined = false;
					if(e.is(CHANNEL_SPECTATE) && !info.getPlayers().contains(e.getSource()))
						alreadyJoined = true;
					else if(!e.is(CHANNEL_SPECTATE) && info.getPlayers().contains(e.getSource()))
						alreadyJoined = true;
					if(alreadyJoined) {
						client.reply(e, ERROR, "Already joined channel with id " + id);
						return;
					}
				}
				join(info, e);
			}
			if(e.is(CHANNEL_CREATE)) {
				KNetChannelInfo request = (KNetChannelInfo) e.get(CHANNEL_CREATE);
				for(KNetChannelInfo ci : channels.values()) {
					if(request.getName().equals(ci.getName())) {
						client.reply(e, ERROR, "Cannot create duplicate channel named " + request.getName());
						return;
					}
				}
				request.setId(nextChannelId.incrementAndGet());

				channels.put(request.getId(), request);
				states.put(request, new ChannelState(request));
				client.fireTCP(CHANNEL_LIST, CHANNEL_INFO, channels.values().toArray(new KNetChannelInfo[0]));
				join(request, e);
			}
			if(e.is(CHANNEL_DELETE)) {
				int id = (Integer) e.get(CHANNEL_DELETE);
				KNetChannelInfo info = channels.get(id);
				if(id == KNetChannelInfo.LOBBY_CHANNEL_ID) {
					client.reply(e, ERROR, "Cannot delete lobby");
				} else if(info.getMembers().size() == 0) {
					channels.remove(id);
					states.remove(info);
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
				depart(info, e.getSource(), false);
			}
			if(e.is(DISCONNECTED)) {
				for(KNetChannelInfo info : channels.values().toArray(new KNetChannelInfo[channels.size()])) {
					if(!info.getMembers().contains(e.getSource()))
						continue;
					depart(info, e.getSource(), true);
				}
			}
			if(e.is(AUTOSTART) || e.is(READY, Boolean.class)) {
				//			client.fireTCP(START, CHANNEL_ID, e.get(CHANNEL_ID));
				KNetChannelInfo c = channels.get(e.get(CHANNEL_ID));
				ChannelState s = states.get(c);
				if(s.requiredAutostartResponses.remove(e.getSource()) && s.requiredAutostartResponses.size() == 0) {
					c.setPlaying(true);
					client.fireTCP(CHANNEL_UPDATE, c);
					KNStartInfo startInfo = new KNStartInfo();
					startInfo.setPlayerCount(c.getPlayers().size());
					startInfo.setSeed(Double.doubleToRawLongBits(Math.random()));
					client.fireTCP(START, startInfo, CHANNEL_ID, c.getId());
					s.living.clear();
					s.living.addAll(c.getPlayers());
				}
			} else if(e.is(READY) && !e.is(READY, Boolean.class)) {
				KNetChannelInfo c = channels.get(e.get(CHANNEL_ID));
				ChannelState s = states.get(c);
				s.requiredAutostartResponses.add(e.getSource());
			}
			if(e.is(DEAD)) {
				KNetChannelInfo c = channels.get(e.get(CHANNEL_ID));
				dead(c, c.getPlayers().get(e.get(DEAD, Integer.class)));
			}
			if(e.is(GAME_ENDING)) {
				KNetChannelInfo c = channels.get(e.get(CHANNEL_ID));
				c.setPlaying(false);
				client.fireTCP(CHANNEL_UPDATE, c);
				if(c.getPlayers().size() >= 2 && c.isAutoStart()) {
					client.fireTCP(AUTOSTART_BEGIN, 10, CHANNEL_ID, c.getId());
					states.get(c).requiredAutostartResponses.addAll(c.getPlayers());
				}
			}
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
	
	protected void join(KNetChannelInfo channel, KNetEvent e) {
		if(!channel.getMembers().contains(e.getSource()))
			channel.getMembers().add(e.getSource());
		KNetPlayerInfo newPlayer = null;
		if(channel.getPlayers().size() < channel.getMaxPlayers() && !channel.isPlaying() && !e.is(KNetEventArgs.CHANNEL_SPECTATE)) {
			channel.getPlayers().add(e.getSource());
			newPlayer = new KNetPlayerInfo();
			newPlayer.setChannel(channel);
			newPlayer.setPlayer(e.getSource());
			newPlayer.setTeam(e.getSource().getName() + e.getSource().getId());
			channel.getPlayerInfo().add(newPlayer);
		} else if(e.is(CHANNEL_SPECTATE)) {
			channel.getPlayers().remove(e.getSource());
			ChannelState s = states.get(channel);
			KNetEventSource user = e.getSource();
			boolean isPlayer = channel.getPlayers().contains(user);
			// declare the player dead
			if(isPlayer) {
				fireTCP(DEAD, channel.getPlayers().indexOf(user), CHANNEL_ID, channel.getId(), DEAD_PLACE, channel.getPlayers().size());
				dead(channel, user);
				maybeAutostart(channel);
			}
		}
		reply(e, 
				CHANNEL_JOIN,
				CHANNEL_ID, channel.getId(),
				PAYLOAD, e.getSource(),
				CHANNEL_INFO, new KNetChannelInfo[] { channel });
		if(newPlayer != null) {
			fireTCP(PLAYER_ENTER, newPlayer, CHANNEL_ID, channel.getId());
			maybeAutostart(channel);
		}
	}
	
	protected void maybeAutostart(KNetChannelInfo channel) {
		if(channel.getPlayers().size() == channel.getMaxPlayers() && channel.isAutoStart()) {
			fireTCP(AUTOSTART_BEGIN, 10, CHANNEL_ID, channel.getId());
			states.get(channel).requiredAutostartResponses.addAll(channel.getPlayers());
		} else {
			fireTCP(AUTOSTART_STOP, CHANNEL_ID, channel.getId());
		}
	}
	
	protected void dead(KNetChannelInfo channel, KNetEventSource user) {
		ChannelState s = states.get(channel);
		s.living.remove(user);
		if(s.living.size() == 1) {
			fireTCP(FINISH, false, FINISH_WINNER, s.living.iterator().next(), CHANNEL_ID, channel.getId());
			channel.setPlaying(false);
			fireTCP(CHANNEL_UPDATE, channel);
			maybeAutostart(channel);
		}
	}
	
	protected void depart(KNetChannelInfo channel, KNetEventSource user, boolean force) {
		ChannelState s = states.get(channel);
		boolean isPlayer = channel.getPlayers().contains(user);
		// declare the player dead
		if(isPlayer) {
			fireTCP(DEAD, channel.getPlayers().indexOf(user), CHANNEL_ID, channel.getId(), DEAD_PLACE, channel.getPlayers().size());
			dead(channel, user);
		}
		if(channel.getId() != KNetChannelInfo.LOBBY_CHANNEL_ID || force)
			channel.depart(user);
		fireTCP(CHANNEL_LEAVE, CHANNEL_ID, channel.getId(), PAYLOAD, user, CHANNEL_INFO, new KNetChannelInfo[] {channel});
		if(isPlayer) {
			maybeAutostart(channel);
		}
		if(channel.getId() != KNetChannelInfo.LOBBY_CHANNEL_ID && channel.getMembers().size() == 0) {
			channels.remove(channel.getId());
			states.remove(channel);
			fireTCP(CHANNEL_LIST, CHANNEL_INFO, channels.values().toArray(new KNetChannelInfo[0]));
		}
		
	}

}
