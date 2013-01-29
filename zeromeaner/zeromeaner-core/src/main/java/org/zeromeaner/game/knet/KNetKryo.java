package org.zeromeaner.game.knet;

import com.esotericsoftware.kryo.Kryo;

public class KNetKryo {
	public static void configure(Kryo kryo) {
		kryo.register(KNetEvent.class);
		kryo.register(KNetEventSource.class);
	}
}
