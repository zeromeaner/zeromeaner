package org.zeromeaner.knet;

import java.util.EventObject;

import org.zeromeaner.knet.obj.KNetChannelInfo;

public class KNetChannelEvent extends EventObject {
	private KNetChannelInfo channel;
	private KNetEvent event;
	
	public KNetChannelEvent(KNetGameClient source, KNetEvent event, KNetChannelInfo channel) {
		super(source);
		this.channel = channel;
		this.event = event;
	}
	
	@Override
	public KNetGameClient getSource() {
		return (KNetGameClient) super.getSource();
	}
	
	public KNetChannelInfo getChannel() {
		return channel;
	}
	
	public KNetEvent getEvent() {
		return event;
	}
}
