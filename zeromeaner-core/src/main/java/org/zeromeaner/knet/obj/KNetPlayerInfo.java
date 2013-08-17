package org.zeromeaner.knet.obj;

import org.zeromeaner.knet.KNetEventSource;

import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.KryoSerializable;
import org.kryomq.kryo.io.Input;
import org.kryomq.kryo.io.Output;

public class KNetPlayerInfo implements KryoSerializable {
	private KNetEventSource player;
//	private KNetChannelInfo channel;
	private int channelId;
	private boolean ready;
	private boolean playing;
	private int playCount;
	private int winCount;
	private String team;
	private boolean bravo;
	
	@Override
	public void write(Kryo kryo, Output output) {
		kryo.writeObject(output, player);
//		kryo.writeObject(output, channel);
		output.writeInt(channelId, true);
		output.writeBoolean(ready);
		output.writeBoolean(playing);
		output.writeInt(playCount, true);
		output.writeInt(winCount, true);
		output.writeString(team);
		output.writeBoolean(bravo);
	}
	
	@Override
	public void read(Kryo kryo, Input input) {
		player = kryo.readObject(input, KNetEventSource.class);
//		channel = kryo.readObject(input, KNetChannelInfo.class);
		channelId = input.readInt(true);
		ready = input.readBoolean();
		playing = input.readBoolean();
		playCount = input.readInt(true);
		winCount = input.readInt(true);
		team = input.readString();
		bravo = input.readBoolean();
	}
	
	public int getSeatId(KNetChannelInfo channel) {
		return channel.getPlayerInfo().indexOf(this);
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

//	public KNetChannelInfo getChannel() {
//		return channel;
//	}
//
//	public void setChannel(KNetChannelInfo channel) {
//		this.channel = channel;
//	}

	public boolean isBravo() {
		return bravo;
	}

	public void setBravo(boolean bravo) {
		this.bravo = bravo;
	}

	public int getChannelId() {
		return channelId;
	}

	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}
}
