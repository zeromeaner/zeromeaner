package org.zeromeaner.game.knet.obj;

import org.zeromeaner.game.component.Statistics;
import org.zeromeaner.util.CustomProperties;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class Replay implements KryoSerializable {
	private CustomProperties replay;
	private Statistics statistics;
	private int gameType;
	
	@Override
	public void write(Kryo kryo, Output output) {
		kryo.writeObject(output, replay);
		kryo.writeObject(output, statistics);
		output.writeInt(gameType, true);
	}
	
	@Override
	public void read(Kryo kryo, Input input) {
		replay = kryo.readObject(input, CustomProperties.class);
		statistics = kryo.readObject(input, Statistics.class);
		gameType = input.readInt(true);
	}
	
	public CustomProperties getReplay() {
		return replay;
	}
	public void setReplay(CustomProperties replay) {
		this.replay = replay;
	}
	public Statistics getStatistics() {
		return statistics;
	}
	public void setStatistics(Statistics statistics) {
		this.statistics = statistics;
	}
	public int getGameType() {
		return gameType;
	}
	public void setGameType(int gameType) {
		this.gameType = gameType;
	}
}
