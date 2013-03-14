package org.zeromeaner.game.knet.obj;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KNStartInfo implements KryoSerializable {
	private long seed;
	private int playerCount;
	private int mapNumber;
	
	@Override
	public void write(Kryo kryo, Output output) {
		output.writeLong(seed, true);
		output.writeInt(playerCount, true);
		output.writeInt(mapNumber, true);
	}
	
	@Override
	public void read(Kryo kryo, Input input) {
		seed = input.readLong(true);
		playerCount = input.readInt(true);
		mapNumber = input.readInt(true);
	}
	
	public long getSeed() {
		return seed;
	}
	public void setSeed(long seed) {
		this.seed = seed;
	}
	public int getPlayerCount() {
		return playerCount;
	}
	public void setPlayerCount(int playerCount) {
		this.playerCount = playerCount;
	}
	public int getMapNumber() {
		return mapNumber;
	}
	public void setMapNumber(int mapNumber) {
		this.mapNumber = mapNumber;
	}

}
