package org.zeromeaner.knet.obj;

import java.util.ArrayList;
import java.util.List;

import org.zeromeaner.game.component.RuleOptions;
import org.zeromeaner.knet.KNetEvent;
import org.zeromeaner.knet.KNetEventSource;
import org.zeromeaner.knet.KNetTopics;
import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.KryoSerializable;
import org.kryomq.kryo.io.Input;
import org.kryomq.kryo.io.Output;

public class KNetChannelInfo implements KryoSerializable {
	public static final int LOBBY_CHANNEL_ID = 0;
	
	private int id;
	private String name;
	private List<KNetEventSource> members = new ArrayList<KNetEventSource>();
	private List<KNetEventSource> players = new ArrayList<KNetEventSource>();
	private List<KNetPlayerInfo> playerInfo = new ArrayList<KNetPlayerInfo>();
	private int maxPlayers;
	private String mode;
	private boolean ruleLock = true;
	private RuleOptions rule;
	private boolean playing;
	private boolean autoStart = true;
	private KNetGameInfo game;
	
	public KNetChannelInfo() {}
	
	public KNetChannelInfo(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public synchronized KNetPlayerInfo getPlayerInfo(KNetEventSource source) {
		int index = players.indexOf(source);
		if(index == -1)
			return null;
		return playerInfo.get(index);
	}
	
	public String getTopic() {
		return KNetTopics.CHANNEL + id;
	}
	
	public synchronized int getSeatId(KNetEvent e) {
		return players.indexOf(e.getSource());
	}
	
	public synchronized void depart(KNetEventSource source) {
		members.remove(source);
		int index = players.indexOf(source);
		players.remove(source);
		if(index != -1)
			playerInfo.remove(source);
	}
	
	@Override
	public synchronized String toString() {
		return "[" + id + ":" + name + members + "]";
	}
	
	@Override
	public synchronized int hashCode() {
		return id;
	}
	
	@Override
	public synchronized boolean equals(Object obj) {
		if(obj instanceof KNetChannelInfo) {
			return id == ((KNetChannelInfo) obj).id;
		}
		return false;
	}
	
	public synchronized int getId() {
		return id;
	}
	public synchronized void setId(int id) {
		this.id = id;
	}
	public synchronized String getName() {
		return name;
	}
	public synchronized void setName(String name) {
		this.name = name;
	}
	
	public synchronized List<KNetEventSource> getMembers() {
		return members;
	}
	
	public synchronized void setMembers(List<KNetEventSource> members) {
		this.members = members;
	}
	
	public synchronized List<KNetEventSource> getPlayers() {
		return players;
	}
	
	public synchronized void setPlayers(List<KNetEventSource> players) {
		this.players = players;
	}
	
	@Override
	public synchronized void write(Kryo kryo, Output output) {
		output.writeInt(id, true);
		output.writeString(name);
		output.writeInt(members.size(), true);
		for(KNetEventSource m : members) {
			kryo.writeObjectOrNull(output, m, KNetEventSource.class);
		}
		output.writeInt(players.size(), true);
		for(KNetEventSource p : players) {
			kryo.writeObjectOrNull(output, p, KNetEventSource.class);
		}
		output.writeString(mode);
		output.writeBoolean(ruleLock);
		kryo.writeObjectOrNull(output, rule, RuleOptions.class);
		output.writeInt(playerInfo.size(), true);
		for(KNetPlayerInfo pi : playerInfo) {
			kryo.writeObjectOrNull(output, pi, KNetPlayerInfo.class);
		}
		output.writeBoolean(playing);
		output.writeInt(maxPlayers, true);
		kryo.writeObjectOrNull(output, game, KNetGameInfo.class);
		output.writeBoolean(autoStart);
	}
	
	@Override
	public synchronized void read(Kryo kryo, Input input) {
		id = input.readInt(true);
		name = input.readString();
		int msize = input.readInt(true);
		members.clear();
		for(int i = 0; i < msize; i++)
			members.add(kryo.readObjectOrNull(input, KNetEventSource.class));
		int psize = input.readInt(true);
		players.clear();
		for(int i = 0; i < psize; i++)
			players.add(kryo.readObjectOrNull(input, KNetEventSource.class));
		mode = input.readString();
		ruleLock = input.readBoolean();
		rule = kryo.readObjectOrNull(input, RuleOptions.class);
		int pisize = input.readInt(true);
		playerInfo.clear();
		for(int i = 0; i < pisize; i++)
			playerInfo.add(kryo.readObjectOrNull(input, KNetPlayerInfo.class));
		playing = input.readBoolean();
		maxPlayers = input.readInt(true);
		game = kryo.readObjectOrNull(input, KNetGameInfo.class);
		autoStart = input.readBoolean();
	}

	public synchronized boolean isRuleLock() {
		return ruleLock;
	}

	public synchronized void setRuleLock(boolean ruleLock) {
		this.ruleLock = ruleLock;
	}

	public synchronized RuleOptions getRule() {
		return rule;
	}

	public synchronized void setRule(RuleOptions rule) {
		this.rule = rule;
	}

	public synchronized List<KNetPlayerInfo> getPlayerInfo() {
		return playerInfo;
	}

	public synchronized void setPlayerInfo(List<KNetPlayerInfo> playerInfo) {
		this.playerInfo = playerInfo;
	}

	public synchronized boolean isPlaying() {
		return playing;
	}

	public synchronized void setPlaying(boolean playing) {
		this.playing = playing;
	}

	public synchronized int getMaxPlayers() {
		return maxPlayers;
	}

	public synchronized void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}

	public synchronized KNetGameInfo getGame() {
		return game;
	}

	public synchronized void setGame(KNetGameInfo game) {
		this.game = game;
	}

	public synchronized boolean isAutoStart() {
		return autoStart;
	}

	public synchronized void setAutoStart(boolean autoStart) {
		this.autoStart = autoStart;
	}

	public synchronized String getMode() {
		return mode;
	}

	public synchronized void setMode(String mode) {
		this.mode = mode;
	}
}