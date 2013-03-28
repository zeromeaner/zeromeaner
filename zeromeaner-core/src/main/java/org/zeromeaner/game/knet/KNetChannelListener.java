package org.zeromeaner.game.knet;

import java.util.EventListener;

public interface KNetChannelListener extends EventListener {
	public void channelJoined(KNetChannelEvent e);
	public void channelUpdated(KNetChannelEvent e);
	public void channelLeft(KNetChannelEvent e);
	public void channelCreated(KNetChannelEvent e);
	public void channelDeleted(KNetChannelEvent e);
	public void channelChat(KNetChannelEvent e);
}
