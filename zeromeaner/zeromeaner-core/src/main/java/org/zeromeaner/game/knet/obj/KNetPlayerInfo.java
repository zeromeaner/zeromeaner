package org.zeromeaner.game.knet.obj;

import org.zeromeaner.game.knet.KNetEventSource;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KNetPlayerInfo implements KryoSerializable {
	private KNetEventSource player;
	private boolean ready;
	private boolean playing;
	private int playCount;
	private int winCount;
	private String team;
	
	@Override
	public void write(Kryo kryo, Output output) {
		kryo.writeObject(output, player);
		output.writeBoolean(ready);
		output.writeBoolean(playing);
		output.writeInt(playCount, true);
		output.writeInt(winCount, true);
		output.writeString(team);
	}
	
	@Override
	public void read(Kryo kryo, Input input) {
		player = kryo.readObject(input, KNetEventSource.class);
		ready = input.readBoolean();
		playing = input.readBoolean();
		playCount = input.readInt(true);
		winCount = input.readInt(true);
		team = input.readString();
	}
	
	public KNetEventSource getPlayer() {
		return player;
	}
	public void setPlayer(KNetEventSource player) {
		this.player = player;
	}
	public boolean isReady() {
		return ready;
	}
	public void setReady(boolean ready) {
		this.ready = ready;
	}
	public boolean isPlaying() {
		return playing;
	}
	public void setPlaying(boolean playing) {
		this.playing = playing;
	}
	public int getPlayCount() {
		return playCount;
	}
	public void setPlayCount(int playCount) {
		this.playCount = playCount;
	}
	public int getWinCount() {
		return winCount;
	}
	public void setWinCount(int winCount) {
		this.winCount = winCount;
	}
	public String getTeam() {
		return team;
	}
	public void setTeam(String team) {
		this.team = team;
	}
}
