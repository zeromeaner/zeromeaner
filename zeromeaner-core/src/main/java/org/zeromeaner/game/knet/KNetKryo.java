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
import org.zeromeaner.game.subsystem.mode.AbstractNetMode;
import org.zeromeaner.game.subsystem.mode.ComboRaceMode;
import org.zeromeaner.game.subsystem.mode.DigChallengeMode;
import org.zeromeaner.game.subsystem.mode.DigRaceMode;
import org.zeromeaner.game.subsystem.mode.ExtremeMode;
import org.zeromeaner.game.subsystem.mode.MarathonMode;
import org.zeromeaner.game.subsystem.mode.MarathonPlusMode;
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
		kryo.register(KNetGameInfo.TSpinEnableType.class);
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
		
		fieldSerializer(kryo, AbstractNetMode.DefaultStats.class);
		fieldSerializer(kryo, AbstractNetMode.DefaultOptions.class);
		
		kryo.register(NetVSBattleMode.AttackInfo.class);
		kryo.register(NetVSBattleMode.StatsInfo.class);
		fieldSerializer(kryo, NetVSBattleMode.EndGameStats.class);
		
		fieldSerializer(kryo, ComboRaceMode.Stats.class);
		fieldSerializer(kryo, ComboRaceMode.Options.class);
		
		fieldSerializer(kryo, DigChallengeMode.Stats.class);
		fieldSerializer(kryo, DigChallengeMode.Options.class);
		
		fieldSerializer(kryo, DigRaceMode.Stats.class);
		fieldSerializer(kryo, DigRaceMode.EndGameStats.class);
		fieldSerializer(kryo, DigRaceMode.Options.class);
		
		fieldSerializer(kryo, ExtremeMode.Stats.class);
		fieldSerializer(kryo, ExtremeMode.Options.class);

		fieldSerializer(kryo, MarathonMode.Options.class);
		fieldSerializer(kryo, MarathonPlusMode.Stats.class);
		fieldSerializer(kryo, MarathonPlusMode.Options.class);
	}
	
	private static <T> void fieldSerializer(Kryo kryo, Class<T> clazz) {
		kryo.register(clazz, new FieldSerializer<T>(kryo, clazz));
	}
}
