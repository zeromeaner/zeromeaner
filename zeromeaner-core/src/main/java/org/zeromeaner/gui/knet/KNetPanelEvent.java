package org.zeromeaner.gui.knet;

import java.util.EventObject;

import org.zeromeaner.game.knet.KNetClient;
import org.zeromeaner.game.knet.obj.KNetChannelInfo;

public class KNetPanelEvent extends EventObject {
	
	private KNetClient client;
	private KNetChannelInfo channel;
	
	public KNetPanelEvent(KNetPanel knetPanel) {
		super(knetPanel);
	}
	
	public KNetPanelEvent(KNetPanel knetPanel, KNetClient client) {
		super(knetPanel);
		this.client = client;
	}
	
	public KNetPanelEvent(KNetPanel knetPanel, KNetClient client, KNetChannelInfo channel) {
		super(knetPanel);
		this.client = client;
		this.channel = channel;
	}
	
	@Override
	public KNetPanel getSource() {
		return (KNetPanel) super.getSource();
	}
	
	public KNetClient getClient() {
		return client;
	}
	
	public KNetChannelInfo getChannel() {
		return channel;
	}
}
