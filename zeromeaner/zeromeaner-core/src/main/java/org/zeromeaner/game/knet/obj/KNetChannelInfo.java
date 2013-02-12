package org.zeromeaner.game.knet.obj;

import java.util.ArrayList;
import java.util.List;

import org.zeromeaner.game.component.RuleOptions;
import org.zeromeaner.game.knet.KNetEventSource;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KNetChannelInfo implements KryoSerializable {
	private int id;
	private String name;
	private List<KNetEventSource> members = new ArrayList<KNetEventSource>();
	private List<KNetEventSource> players = new ArrayList<KNetEventSource>();
	private List<KNetPlayerInfo> playerInfo = new ArrayList<KNetPlayerInfo>();
	private int maxPlayers;
	private boolean ruleLock;
	private RuleOptions rule;
	private boolean playing;
	private KNetGameInfo game;
	
	public KNetChannelInfo() {}
	
	public KNetChannelInfo(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "[" + id + ":" + name + "]";
	}
	
	@Override
	public int hashCode() {
		return id;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof KNetChannelInfo) {
			return id == ((KNetChannelInfo) obj).id;
		}
		return false;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public List<KNetEventSource> getMembers() {
		return members;
	}
	
	public void setMembers(List<KNetEventSource> members) {
		this.members = members;
	}
	
	public List<KNetEventSource> getPlayers() {
		return players;
	}
	
	public void setPlayers(List<KNetEventSource> players) {
		this.players = players;
	}
	
	@Override
	public void write(Kryo kryo, Output output) {
		output.writeInt(id, true);
		output.writeString(name);
		output.writeInt(members.size(), true);
		for(KNetEventSource m : members) {
			kryo.writeObject(output, m);
		}
		output.writeInt(players.size(), true);
		for(KNetEventSource p : players) {
			kryo.writeObject(output, p);
		}
		output.writeBoolean(ruleLock);
		kryo.writeObjectOrNull(output, rule, RuleOptions.class);
		output.writeInt(playerInfo.size(), true);
		for(KNetPlayerInfo pi : playerInfo) {
			kryo.writeObject(output, pi);
		}
		output.writeBoolean(playing);
		output.writeInt(maxPlayers, true);
		kryo.writeObjectOrNull(output, game, KNetGameInfo.class);
	}
	
	@Override
	public void read(Kryo kryo, Input input) {
		id = input.readInt(true);
		name = input.readString();
		int msize = input.readInt(true);
		for(int i = 0; i < msize; i++)
			members.add(kryo.readObject(input, KNetEventSource.class));
		int psize = input.readInt(true);
		for(int i = 0; i < psize; i++)
			players.add(kryo.readObject(input, KNetEventSource.class));
		ruleLock = input.readBoolean();
		rule = kryo.readObjectOrNull(input, RuleOptions.class);
		int pisize = input.readInt(true);
		for(int i = 0; i < pisize; i++)
			playerInfo.add(kryo.readObject(input, KNetPlayerInfo.class));
		playing = input.readBoolean();
		maxPlayers = input.readInt(true);
		game = kryo.readObjectOrNull(input, KNetGameInfo.class);
	}

	public boolean isRuleLock() {
		return ruleLock;
	}

	public void setRuleLock(boolean ruleLock) {
		this.ruleLock = ruleLock;
	}

	public RuleOptions getRule() {
		return rule;
	}

	public void setRule(RuleOptions rule) {
		this.rule = rule;
	}

	public List<KNetPlayerInfo> getPlayerInfo() {
		return playerInfo;
	}

	public void setPlayerInfo(List<KNetPlayerInfo> playerInfo) {
		this.playerInfo = playerInfo;
	}

	public boolean isPlaying() {
		return playing;
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}

	public KNetGameInfo getGame() {
		return game;
	}

	public void setGame(KNetGameInfo game) {
		this.game = game;
	}
}