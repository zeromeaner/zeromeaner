package org.zeromeaner.game.knet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.zeromeaner.game.component.Field;
import org.zeromeaner.game.knet.obj.KNetChannelInfo;

import static org.zeromeaner.game.knet.KNetEventArgs.*;

public class KNetGameClient extends KNetClient implements KNetListener {
	private List<Field> maps = new ArrayList<Field>();
	private Set<KNetChannelInfo> channels = new HashSet<KNetChannelInfo>();
	
	public KNetGameClient(String host, int port) {
		this(KNetGameClient.class.getSimpleName(), host, port);
	}

	public KNetGameClient(String type, String host, int port) {
		super(type, host, port);
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

}
