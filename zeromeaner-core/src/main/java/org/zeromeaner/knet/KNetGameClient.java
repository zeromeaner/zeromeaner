package org.zeromeaner.knet;

import static org.zeromeaner.knet.KNetEventArgs.ADDRESS;
import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_CHAT;
import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_ID;
import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_INFO;
import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_JOIN;
import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_LEAVE;
import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_LIST;
import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_SPECTATE;
import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_UPDATE;
import static org.zeromeaner.knet.KNetEventArgs.CONNECTED;
import static org.zeromeaner.knet.KNetEventArgs.GAME;
import static org.zeromeaner.knet.KNetEventArgs.MAPS;
import static org.zeromeaner.knet.KNetEventArgs.PAYLOAD;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.funcish.core.Predicates;
import org.funcish.core.fn.Predicate;
import org.funcish.core.impl.AbstractPredicate;
import org.mmmq.Topic;
import org.zeromeaner.game.component.Field;
import org.zeromeaner.knet.obj.KNetChannelInfo;

public class KNetGameClient extends KNetClient implements KNetListener {
	private final Predicate<KNetEvent> CHANNEL_LISTING = new AbstractPredicate<KNetEvent>(KNetEvent.class) {
		@Override
		public boolean test0(KNetEvent value, Integer index) throws Exception {
			return value.is(CHANNEL_LIST) && (value.is(CHANNEL_INFO, KNetChannelInfo[].class));
		}
	};
	
	private final Predicate<KNetEvent> CURRENT_CHANNEL = new AbstractPredicate<KNetEvent>(KNetEvent.class) {
		@Override
		public boolean test0(KNetEvent value, Integer index) throws Exception {
			return currentChannel != null && value.is(CHANNEL_ID, Integer.class) && currentChannel.getId() == value.get(CHANNEL_ID, Integer.class);
		}
	};
	
	private final Predicate<KNetEvent> JOIN_CHANNEL = new AbstractPredicate<KNetEvent>(KNetEvent.class) {
		@Override
		public boolean test0(KNetEvent value, Integer index) throws Exception {
			return value.is(CHANNEL_JOIN) && value.is(PAYLOAD);
		}
	};
	
	private final Predicate<KNetEvent> LEAVE_CHANNEL = new AbstractPredicate<KNetEvent>(KNetEvent.class) {
		@Override
		public boolean test0(KNetEvent value, Integer index) throws Exception {
			return value.is(CHANNEL_LEAVE) && value.is(PAYLOAD);
		}
	};
	
	private final Predicate<KNetEvent> I_JOINED_OR_LEFT = new AbstractPredicate<KNetEvent>(KNetEvent.class) {
		@Override
		public boolean test0(KNetEvent value, Integer index) throws Exception {
			return getSource().equals(value.get(PAYLOAD)) && isMine(value);
		}
	};
	
	private final Predicate<KNetEvent> I_JOINED_CHANNEL = Predicates.and(JOIN_CHANNEL, I_JOINED_OR_LEFT);
	private final Predicate<KNetEvent> I_LEFT_CHANNEL = Predicates.and(LEAVE_CHANNEL, I_JOINED_OR_LEFT);
	
	private List<Field> maps = new ArrayList<Field>();
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
		if(!e.is(CHANNEL_ID) && currentChannel != null)
			e.set(CHANNEL_ID, currentChannel.getId());
		return super.process(e);
	}
	
	@Override
	protected void issue(KNetEvent e) {
		boolean issue = false;
		for(KNetEventArgs arg : e.getArgs().keySet()) {
			if(arg.isGlobal())
				issue = true;
		}
		if(e.is(CHANNEL_ID) && currentChannel != null && currentChannel.getId() == (Integer) e.get(CHANNEL_ID))
			issue = true;
		if(getSource().equals(e.get(ADDRESS)))
			issue = true;
		if(issue) {
			knetEvented(this, e);
			super.issue(e);
		}
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

		if(e.is(MAPS))
			maps = Arrays.asList((Field[]) e.get(MAPS));
		else if(e.is(CONNECTED)) {
			client.fireTCP(CHANNEL_LIST, true);
		} else if(e.is(CHANNEL_LISTING)) {
			List<KNetChannelInfo> chl = Arrays.asList((KNetChannelInfo[]) e.get(CHANNEL_INFO));
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
		} else if(e.is(JOIN_CHANNEL)) {
			if(!e.is(CHANNEL_INFO)) {
				int channelId = e.get(CHANNEL_ID, Integer.class);
				KNetGameClient.this.client.subscribe(new Topic(KNetTopics.CHANNEL + channelId), KNetGameClient.this);
				fireTCP(CHANNEL_JOIN, CHANNEL_ID, channelId);
			} else {
				KNetChannelInfo c = e.get(CHANNEL_INFO, KNetChannelInfo[].class)[0];
				c = updateChannel(e, c);
				if(e.is(I_JOINED_CHANNEL)) {
					currentChannel = c;
					fireChannelJoined(e, c);
				}
			}
		} else if(e.is(LEAVE_CHANNEL)) {
			KNetChannelInfo c = e.get(CHANNEL_INFO, KNetChannelInfo[].class)[0];
			c = updateChannel(e, c);
			if(e.is(I_LEFT_CHANNEL)) {
				currentChannel = null;
				fireChannelLeft(e, c);
			}
		} else if(e.is(CHANNEL_CHAT)) {
			KNetChannelInfo c = channels.get(e.get(CHANNEL_ID, Integer.class));
			fireChannelChat(e, c);
		} else if(e.is(CHANNEL_UPDATE)) {
			KNetChannelInfo c = updateChannel(e, e.get(KNetEventArgs.CHANNEL_UPDATE, KNetChannelInfo.class));
			fireChannelUpdated(e, c);
		}
	}
	
	public KNetChannelInfo getChannel(int channelId) {
		return channels.get(channelId);
	}
	
	public List<Field> getMaps() {
		return maps;
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
		fireTCP(CHANNEL_JOIN, CHANNEL_ID, channelId);
	}
	
	public void spectateChannel(int channelId) {
		if(currentChannel != null && currentChannel.getId() != channelId)
			leaveChannel();
		fireTCP(CHANNEL_JOIN, CHANNEL_SPECTATE, CHANNEL_ID, channelId);
	}
	
	public void leaveChannel() {
		if(currentChannel == null)
			return;
		if(currentChannel.getId() == KNetChannelInfo.LOBBY_CHANNEL_ID)
			return; // don't even try to leave the lobby
		fireTCP(CHANNEL_LEAVE, CHANNEL_ID, currentChannel.getId());
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
