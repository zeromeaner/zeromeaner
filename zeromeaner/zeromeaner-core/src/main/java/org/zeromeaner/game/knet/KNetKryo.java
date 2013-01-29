package org.zeromeaner.game.knet;

import org.zeromeaner.game.knet.srv.KSChannelManager.ChannelInfo;

import com.esotericsoftware.kryo.Kryo;

public class KNetKryo {
	public static void configure(Kryo kryo) {
		kryo.register(KNetEvent.class);
		kryo.register(KNetEventSource.class);
		kryo.register(ChannelInfo.class);
		kryo.register(ChannelInfo[].class);
		
	}
}
