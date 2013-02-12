package org.zeromeaner.game.knet;

import java.util.ArrayList;
import java.util.Properties;

import org.zeromeaner.game.component.Block;
import org.zeromeaner.game.component.Field;
import org.zeromeaner.game.component.Piece;
import org.zeromeaner.game.component.RuleOptions;
import org.zeromeaner.game.component.SpeedParam;
import org.zeromeaner.game.component.Statistics;
import org.zeromeaner.game.knet.obj.KNStartInfo;
import org.zeromeaner.game.knet.obj.KNetChannelInfo;
import org.zeromeaner.game.knet.obj.KNetGameInfo;
import org.zeromeaner.game.knet.obj.KNetPlayerInfo;
import org.zeromeaner.game.knet.obj.PieceHold;
import org.zeromeaner.game.knet.obj.PieceMovement;
import org.zeromeaner.game.knet.ser.BlockSerializer;

import org.zeromeaner.game.knet.ser.PieceSerializer;
import org.zeromeaner.game.knet.ser.PropertiesSerializer;
import org.zeromeaner.game.knet.ser.SpeedParamSerializer;
import org.zeromeaner.game.knet.ser.StatisticsSerializer;
import org.zeromeaner.game.subsystem.mode.ComboRaceMode;
import org.zeromeaner.game.subsystem.mode.NetVSBattleMode;
import org.zeromeaner.util.CustomProperties;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.FieldSerializer;

public class KNetKryo {
	public static void configure(Kryo kryo) {
		kryo.register(KNetEvent.class);
		kryo.register(KNetEventSource.class);
		kryo.register(KNetChannelInfo.class);
		kryo.register(KNetChannelInfo[].class);
		fieldSerializer(kryo, KNetGameInfo.class);
		kryo.register(KNetPlayerInfo.class);
		kryo.register(Field.class, new org.zeromeaner.game.knet.ser.FieldSerializer());
		kryo.register(Block[][].class);
		kryo.register(Block[].class);
		kryo.register(Block.class, new BlockSerializer());
		kryo.register(SpeedParam.class, new SpeedParamSerializer());
		kryo.register(boolean[].class);
		kryo.register(ArrayList.class);
		kryo.register(Piece.class, new PieceSerializer());
		kryo.register(int[][].class);
		kryo.register(int[].class);
		kryo.register(Piece[].class);
		kryo.register(PieceHold.class);
		kryo.register(PieceMovement.class);
		kryo.register(Properties.class, new PropertiesSerializer());
		kryo.register(CustomProperties.class, new PropertiesSerializer());
		kryo.register(Statistics.class, new StatisticsSerializer());
		fieldSerializer(kryo, RuleOptions.class);
		kryo.register(KNStartInfo.class);
		
		
		kryo.register(NetVSBattleMode.AttackInfo.class);
		kryo.register(NetVSBattleMode.StatsInfo.class);
		fieldSerializer(kryo, NetVSBattleMode.EndGameStats.class);
		
		fieldSerializer(kryo, ComboRaceMode.Stats.class);
		fieldSerializer(kryo, ComboRaceMode.Options.class);
	}
	
	private static <T> void fieldSerializer(Kryo kryo, Class<T> clazz) {
		kryo.register(clazz, new FieldSerializer<T>(kryo, clazz));
	}
}
