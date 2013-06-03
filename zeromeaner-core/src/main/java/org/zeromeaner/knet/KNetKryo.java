package org.zeromeaner.knet;

import java.util.ArrayList;
import java.util.Properties;

import org.zeromeaner.game.component.Block;
import org.zeromeaner.game.component.Field;
import org.zeromeaner.game.component.Piece;
import org.zeromeaner.game.component.RuleOptions;
import org.zeromeaner.game.component.SpeedParam;
import org.zeromeaner.game.component.Statistics;
import org.zeromeaner.game.subsystem.mode.AbstractNetMode;
import org.zeromeaner.game.subsystem.mode.ComboRaceMode;
import org.zeromeaner.game.subsystem.mode.DigChallengeMode;
import org.zeromeaner.game.subsystem.mode.DigRaceMode;
import org.zeromeaner.game.subsystem.mode.ExtremeMode;
import org.zeromeaner.game.subsystem.mode.MarathonMode;
import org.zeromeaner.game.subsystem.mode.MarathonPlusMode;
import org.zeromeaner.game.subsystem.mode.NetVSBattleMode;
import org.zeromeaner.knet.obj.KNStartInfo;
import org.zeromeaner.knet.obj.KNetChannelInfo;
import org.zeromeaner.knet.obj.KNetGameInfo;
import org.zeromeaner.knet.obj.KNetPlayerInfo;
import org.zeromeaner.knet.obj.PieceHold;
import org.zeromeaner.knet.obj.PieceMovement;
import org.zeromeaner.knet.ser.BlockSerializer;
import org.zeromeaner.knet.ser.DiffFieldSerializer;
import org.zeromeaner.knet.ser.PieceSerializer;
import org.zeromeaner.knet.ser.PropertiesSerializer;
import org.zeromeaner.knet.ser.SpeedParamSerializer;
import org.zeromeaner.knet.ser.StatisticsSerializer;
import org.zeromeaner.util.CustomProperties;
import org.zeromeaner.util.GeneralUtil;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer;

public class KNetKryo {
	public static void configure(Kryo kryo) {
		kryo.setReferences(true);
		kryo.setAutoReset(true);
		
		kryo.register(String[].class);
		
		kryo.register(KNetEvent.class);
		kryo.register(KNetEventSource.class);
		kryo.register(KNetChannelInfo.class);
		kryo.register(KNetChannelInfo[].class);
		fieldSerializer(kryo, KNetGameInfo.class);
		kryo.register(KNetGameInfo.TSpinEnableType.class);
		kryo.register(KNetPlayerInfo.class);
//		kryo.register(Field.class, new org.zeromeaner.game.knet.ser.FieldSerializer());
		fieldSerializer(kryo, Field.class);
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
//		fieldSerializer(kryo, RuleOptions.class);
		kryo.register(RuleOptions.class, new DiffFieldSerializer<RuleOptions>(kryo, RuleOptions.class, GeneralUtil.loadRule("config/rule/Standard.rul")));
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
