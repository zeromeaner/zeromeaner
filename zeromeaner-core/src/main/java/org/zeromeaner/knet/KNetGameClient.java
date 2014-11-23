package org.zeromeaner.knet;

import static org.zeromeaner.knet.KNetEventArgs.ADDRESS;
import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_ID;
import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_INFO;
import static org.zeromeaner.knet.KNetEventArgs.GAME;
import static org.zeromeaner.knet.KNetEventArgs.MAPS;
import static org.zeromeaner.knet.KNetEventArgs.PAYLOAD;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.funcish.core.fn.Predicate;
import org.funcish.core.impl.AbstractPredicate;
import org.funcish.core.util.Predicates;
import org.mmmq.Topic;
import org.zeromeaner.game.component.Field;
import org.zeromeaner.knet.KNetPacket.KNetFromClient;
import org.zeromeaner.knet.KNetPacket.KNetFromServer;
import org.zeromeaner.knet.obj.KNetChannelInfo;

public class KNetGameClient extends KNetClient implements KNetListener {
	private final Predicate<KNetEvent> CHANNEL_LISTING = new AbstractPredicate<KNetEvent>(KNetEvent.class) {
		@Override
		public boolean test0(KNetEvent value, Integer index) throws Exception {
			return value.isType(KNetFromServer.CHANNELS_LISTED);
		}
	};
	
	private final Predicate<KNetEvent> CURRENT_CHANNEL = new AbstractPredicate<KNetEvent>(KNetEvent.class) {
		@Override
		public boolean test0(KNetEvent value, Integer index) throws Exception {
			return currentChannel != null && value.has(CHANNEL_ID, Integer.class) && currentChannel.getId() == value.get(CHANNEL_ID, Integer.class);
		}
	};
	
	private final Predicate<KNetEvent> CHANNEL_JOINED = new AbstractPredicate<KNetEvent>(KNetEvent.class) {
		@Override
		public boolean test0(KNetEvent value, Integer index) throws Exception {
			return value.isType(KNetFromServer.CHANNEL_JOINED);
		}
	};
	
	private final Predicate<KNetEvent> CHANNEL_PARTED = new AbstractPredicate<KNetEvent>(KNetEvent.class) {
		@Override
		public boolean test0(KNetEvent value, Integer index) throws Exception {
			return value.isType(KNetFromServer.CHANNEL_PARTED);
		}
	};
	
	private final Predicate<KNetEvent> USER_IS_ME = new AbstractPredicate<KNetEvent>(KNetEvent.class) {
		@Override
		public boolean test0(KNetEvent value, Integer index) throws Exception {
			return getSource().equals(value.get(KNetEventArgs.USER));
		}
	};
	
	private final Predicate<KNetEvent> I_JOINED_CHANNEL = Predicates.and(CHANNEL_JOINED, USER_IS_ME);
	private final Predicate<KNetEvent> I_LEFT_CHANNEL = Predicates.and(CHANNEL_PARTED, USER_IS_ME);
	
	private Map<Integer, KNetChannelInfo> channels = new HashMap<Integer, KNetChannelInfo>();
	private volatile KNetChannelInfo currentChannel;
	
	public KNetGameClient(String host, int port) {
		this(KNetGameClient.class.getSimpleName(), host, port);
	}

	public KNetGameClient(String type, String host, int port) {
		super(type, host, port);
//		addKNetListener(this);
	}
	
	@Override
	public KNetClient start() throws IOException, InterruptedException {
		super.start();
		
		client.subscribe(new Topic(KNetTopics.CHANNEL), this);
		
		return this;
	}
	
	@Override
	protected KNetEvent process(KNetEvent e) {
		for(Map.Entry<KNetEventArgs, Object> en : e.getArgs().entrySet()) {
			if(en.getKey().name().startsWith("GAME_")) {
				e.set(GAME, true);
				break;
			}
		}
		return super.process(e);
	}
	
	@Override
	protected void issue(KNetEvent e) {
		knetEvented(this, e);
		super.issue(e);
	}
	
	protected KNetChannelInfo updateChannel(KNetEvent ke, KNetChannelInfo src) {
		KNetChannelInfo dst = channels.get(src.getId());
		if(dst == null)
			return null;
//		KryoCopy.overwrite(src, channels.get(src.getId()));
		dst.setName(src.getName());
		dst.setMembers(src.getMembers());
		dst.setPlayers(src.getPlayers());
		dst.setPlayerInfo(src.getPlayerInfo());
		dst.setMaxPlayers(src.getMaxPlayers());
		dst.setMode(src.getMode());
		dst.setRuleLock(src.isRuleLock());
		dst.setRule(src.getRule());
		dst.setPlaying(src.isPlaying());
		dst.setAutoStart(src.isAutoStart());
		dst.setGame(src.getGame());
		fireChannelUpdated(ke, channels.get(src.getId()));
		return channels.get(src.getId());
	}

	@Override
	public void knetEvented(KNetClient client, KNetEvent e) {
		if(e.isType(KNetFromServer.CONNECTED)) {
			client.fireTCP(KNetFromClient.LIST_CHANNELS);
		} else if(e.isType(KNetFromServer.CHANNELS_LISTED)) {
			List<KNetChannelInfo> chl = Arrays.asList(e.get(KNetEventArgs.CHANNEL_LISTING, KNetChannelInfo[].class));
			for(KNetChannelInfo c : chl) {
				if(channels.containsKey(c.getId())) {
					updateChannel(e, c);
					continue;
				}
				channels.put(c.getId(), c);
				fireChannelCreated(e, c);
			}
			List<KNetChannelInfo> deleted = new ArrayList<KNetChannelInfo>(channels.values());
			deleted.removeAll(chl);
			for(KNetChannelInfo c : deleted) {
				fireChannelDeleted(e, channels.remove(c.getId()));
			}
		} else if(e.is(I_JOINED_CHANNEL)) {
			KNetChannelInfo c = e.get(CHANNEL_INFO, KNetChannelInfo.class);
			c = updateChannel(e, c);
			if(e.is(I_JOINED_CHANNEL)) {
				currentChannel = c;
				fireChannelJoined(e, c);
			}
		} else if(e.is(CHANNEL_PARTED)) {
			KNetChannelInfo c = e.get(CHANNEL_INFO, KNetChannelInfo.class);
			c = updateChannel(e, c);
			if(e.is(I_LEFT_CHANNEL)) {
				currentChannel = null;
				fireChannelLeft(e, c);
			}
		} else if(e.isType(KNetFromServer.CHANNEL_RECEIVED_MESSAGE)) {
			KNetChannelInfo c = channels.get(e.get(CHANNEL_ID, Integer.class));
			fireChannelChat(e, c);
		} else if(e.isType(KNetFromServer.CHANNEL_UPDATED)) {
			KNetChannelInfo c = updateChannel(e, e.get(KNetEventArgs.CHANNEL_INFO, KNetChannelInfo.class));
			fireChannelUpdated(e, c);
		}
	}
	
	public KNetChannelInfo getChannel(int channelId) {
		return channels.get(channelId);
	}
	
	public Map<Integer, KNetChannelInfo> getChannels() {
		return channels;
	}
	
	public KNetChannelInfo getCurrentChannel() {
		return currentChannel;
	}
	
	public void joinChannel(int channelId) {
		if(currentChannel != null && currentChannel.getId() != channelId)
			leaveChannel();
		client.subscribe(new Topic(KNetTopics.CHANNEL + channelId), this);
		fireTCP(KNetFromClient.JOIN_CHANNEL, CHANNEL_ID, channelId);
		fireTCP(KNetFromClient.JOIN_CHANNEL_GAME, CHANNEL_ID, channelId);
	}
	
	public void spectateChannel(int channelId) {
		if(currentChannel != null && currentChannel.getId() != channelId)
			leaveChannel();
		client.subscribe(new Topic(KNetTopics.CHANNEL + channelId), this);
		fireTCP(KNetFromClient.JOIN_CHANNEL, CHANNEL_ID, channelId);
		fireTCP(KNetFromClient.PART_CHANNEL_GAME, CHANNEL_ID, channelId);
	}
	
	public void leaveChannel() {
		if(currentChannel == null)
			return;
		if(currentChannel.getId() == KNetChannelInfo.LOBBY_CHANNEL_ID)
			return; // don't even try to leave the lobby
		fireTCP(KNetFromClient.PART_CHANNEL, CHANNEL_ID, currentChannel.getId());
		client.unsubscribe(new Topic(KNetTopics.CHANNEL + currentChannel.getId()), this);
	}
	
	public void addKNetChannelListener(KNetChannelListener l) {
		listenerList.add(KNetChannelListener.class, l);
	}
	
	public void removeKNetChannelListener(KNetChannelListener l) {
		listenerList.remove(KNetChannelListener.class, l);
	}

	protected void fireChannelJoined(KNetEvent ke, KNetChannelInfo c) {
		Object[] ll = listenerList.getListenerList();
		KNetChannelEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == KNetChannelListener.class) {
				if(e == null)
					e = new KNetChannelEvent(this, ke, c);
				((KNetChannelListener) ll[i+1]).channelJoined(e);
			}
		}
	}

	protected void fireChannelUpdated(KNetEvent ke, KNetChannelInfo c) {
		Object[] ll = listenerList.getListenerList();
		KNetChannelEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == KNetChannelListener.class) {
				if(e == null)
					e = new KNetChannelEvent(this, ke, c);
				((KNetChannelListener) ll[i+1]).channelUpdated(e);
			}
		}
	}
	
	protected void fireChannelLeft(KNetEvent ke, KNetChannelInfo c) {
		Object[] ll = listenerList.getListenerList();
		KNetChannelEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == KNetChannelListener.class) {
				if(e == null)
					e = new KNetChannelEvent(this, ke, c);
				((KNetChannelListener) ll[i+1]).channelLeft(e);
			}
		}
	}
	
	protected void fireChannelCreated(KNetEvent ke, KNetChannelInfo c) {
		Object[] ll = listenerList.getListenerList();
		KNetChannelEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == KNetChannelListener.class) {
				if(e == null)
					e = new KNetChannelEvent(this, ke, c);
				((KNetChannelListener) ll[i+1]).channelCreated(e);
			}
		}
	}
	
	protected void fireChannelDeleted(KNetEvent ke, KNetChannelInfo c) {
		Object[] ll = listenerList.getListenerList();
		KNetChannelEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == KNetChannelListener.class) {
				if(e == null)
					e = new KNetChannelEvent(this, ke, c);
				((KNetChannelListener) ll[i+1]).channelDeleted(e);
			}
		}
	}

	protected void fireChannelChat(KNetEvent ke, KNetChannelInfo c) {
		Object[] ll = listenerList.getListenerList();
		KNetChannelEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == KNetChannelListener.class) {
				if(e == null)
					e = new KNetChannelEvent(this, ke, c);
				((KNetChannelListener) ll[i+1]).channelChat(e);
			}
		}
	}

	public void setCurrentChannel(KNetChannelInfo currentChannel) {
		this.currentChannel = currentChannel;
	}
}
