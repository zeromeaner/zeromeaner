package org.zeromeaner.rest;

import java.util.List;

import org.zeromeaner.knet.KNetEventSource;
import org.zeromeaner.knet.obj.KNetPlayerInfo;
import org.zeromeaner.rest.View.Detail;

import com.fasterxml.jackson.annotation.JsonView;

public class Mixin {
	public static class Channel {
		@JsonView(View.class) private int id;
		@JsonView(View.class) private String name;
		@JsonView(View.class) private List<KNetEventSource> members;
		@JsonView(View.class) private List<KNetEventSource> players;
		@JsonView(Detail.class) private List<KNetPlayerInfo> playerInfo;
	}
	public static class PlayerInfo {
		@JsonView(View.class) private KNetEventSource player;
		@JsonView(View.class) private boolean ready;
		@JsonView(View.class) private boolean playing;
		@JsonView(Detail.class) private int playCount;
		@JsonView(Detail.class) private int winCount;
		@JsonView(View.class) private String team;
	}
	public static class Source {
		@JsonView(View.class) private int id;
		@JsonView(View.class) private String type;
		@JsonView(View.class) private String name;
	}
}
