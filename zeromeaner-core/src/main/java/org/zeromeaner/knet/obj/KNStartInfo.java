package org.zeromeaner.knet.obj;

import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.KryoSerializable;
import org.kryomq.kryo.io.Input;
import org.kryomq.kryo.io.Output;

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
