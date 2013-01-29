package org.zeromeaner.game.knet;

import java.util.ArrayList;

import org.zeromeaner.game.component.Block;
import org.zeromeaner.game.component.Field;
import org.zeromeaner.game.component.Piece;
import org.zeromeaner.game.knet.ser.BlockSerializer;
import org.zeromeaner.game.knet.ser.FieldSerializer;
import org.zeromeaner.game.knet.ser.PieceSerializer;
import org.zeromeaner.game.knet.srv.KSChannelManager.ChannelInfo;

import com.esotericsoftware.kryo.Kryo;

public class KNetKryo {
	public static void configure(Kryo kryo) {
		kryo.register(KNetEvent.class);
		kryo.register(KNetEventSource.class);
		kryo.register(ChannelInfo.class);
		kryo.register(ChannelInfo[].class);
		kryo.register(Field.class, new FieldSerializer());
		kryo.register(Block[][].class);
		kryo.register(Block[].class);
		kryo.register(Block.class, new BlockSerializer());
		kryo.register(boolean[].class);
		kryo.register(ArrayList.class);
		kryo.register(Piece.class, new PieceSerializer());
		kryo.register(int[][].class);
		kryo.register(int[].class);
		kryo.register(Piece[].class);
	}
}
