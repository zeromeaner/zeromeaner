package org.zeromeaner.game.knet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.funcish.core.Predicates;
import org.funcish.core.fn.Predicate;
import org.funcish.core.impl.AbstractPredicate;
import org.zeromeaner.game.component.Field;
import org.zeromeaner.game.knet.obj.KNetChannelInfo;
import org.zeromeaner.util.KryoCopy;

import static org.zeromeaner.game.knet.KNetEventArgs.*;

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
	private KNetChannelInfo currentChannel;
	
	public KNetGameClient(String host, int port) {
		this(KNetGameClient.class.getSimpleName(), host, port);
	}

	public KNetGameClient(String type, String host, int port) {
		super(type, host, port);
		addKNetListener(this);
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
			try {
				if(arg.getDeclaringClass().getField(arg.name()).isAnnotationPresent(Global.class))
					issue = true;
			} catch(Exception ex) {
			}
		}
		if(e.is(CHANNEL_ID) && currentChannel != null && currentChannel.getId() == (Integer) e.get(CHANNEL_ID))
			issue = true;
		if(issue)
			super.issue(e);
	}
	
	protected KNetChannelInfo updateChannel(KNetEvent ke, KNetChannelInfo c) {
		KryoCopy.overwrite(c, channels.get(c.getId()));
		fireChannelUpdated(ke, channels.get(c.getId()));
		return channels.get(c.getId());
	}
	
	@Override
	public void knetEvented(KNetClient client, KNetEvent e) {
		System.out.println(e);
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
			KNetChannelInfo c = e.get(CHANNEL_INFO, KNetChannelInfo[].class)[0];
			c = updateChannel(e, c);
			if(e.is(I_JOINED_CHANNEL)) {
				currentChannel = c;
				fireChannelJoined(e, c);
			}
		} else if(e.is(LEAVE_CHANNEL)) {
			KNetChannelInfo c = e.get(CHANNEL_INFO, KNetChannelInfo[].class)[0];
			c = updateChannel(e, c);
			if(e.is(I_LEFT_CHANNEL)) {
				currentChannel = null;
				fireChannelLeft(e, c);
			}
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
		if(currentChannel != null)
			leaveChannel();
		fireTCP(CHANNEL_JOIN, CHANNEL_ID, channelId);
	}
	
	public void leaveChannel() {
		if(currentChannel == null)
			return;
		fireTCP(CHANNEL_LEAVE, CHANNEL_ID, currentChannel.getId());
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
}
