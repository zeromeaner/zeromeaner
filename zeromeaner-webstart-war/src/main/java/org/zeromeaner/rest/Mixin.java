package org.zeromeaner.rest;

import java.util.List;

import org.zeromeaner.knet.KNetEventSource;

import com.fasterxml.jackson.annotation.JsonView;

public class Mixin {
	public static class Channel {
		@JsonView(View.class) private int id;
		@JsonView(View.class) private String name;
		@JsonView(View.class) private List<KNetEventSource> members;
		@JsonView(View.class) private List<KNetEventSource> players;
	}
	public static class Source {
		@JsonView(View.class) private int id;
		@JsonView(View.class) private String type;
		@JsonView(View.class) private String name;
	}
}
