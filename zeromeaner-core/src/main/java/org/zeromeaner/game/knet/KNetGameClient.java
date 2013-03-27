package org.zeromeaner.game.knet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zeromeaner.game.component.Field;
import org.zeromeaner.game.knet.obj.KNetChannelInfo;

import static org.zeromeaner.game.knet.KNetEventArgs.*;

public class KNetGameClient extends KNetClient implements KNetListener {
	private List<Field> maps = new ArrayList<Field>();
	private Set<KNetChannelInfo> channels = new HashSet<KNetChannelInfo>();
	private Integer joinedId = -1;
	
	public KNetGameClient(String host, int port) {
		this(KNetGameClient.class.getSimpleName(), host, port);
	}

	public KNetGameClient(String type, String host, int port) {
		super(type, host, port);
	}
	
	@Override
	protected KNetEvent process(KNetEvent e) {
		for(Map.Entry<KNetEventArgs, Object> en : e.getArgs().entrySet()) {
			if(en.getKey().name().startsWith("GAME_")) {
				e.set(GAME, true);
				break;
			}
		}
		if(!e.is(CHANNEL_ID) && joinedId != null)
			e.set(CHANNEL_ID, joinedId);
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
		if(e.is(CHANNEL_ID) && joinedId != null && (int) joinedId == (Integer) e.get(CHANNEL_ID))
			issue = true;
		if(issue)
			super.issue(e);
	}
	
	@Override
	public void knetEvented(KNetClient client, KNetEvent e) {
		if(e.is(MAPS)) {
			maps = Arrays.asList((Field[]) e.get(MAPS));
		}
	}
	
	public List<Field> getMaps() {
		return maps;
	}

	public void setMaps(List<Field> maps) {
		this.maps = maps;
	}
	
	public Set<KNetChannelInfo> getChannels() {
		return channels;
	}

	public void setChannels(Set<KNetChannelInfo> channels) {
		this.channels = channels;
	}

	public Integer getJoinedId() {
		return joinedId;
	}

	public void setJoinedId(Integer joinedId) {
		this.joinedId = joinedId;
	}

}
