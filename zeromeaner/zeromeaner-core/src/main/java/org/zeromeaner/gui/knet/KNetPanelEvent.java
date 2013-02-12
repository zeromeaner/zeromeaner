package org.zeromeaner.gui.knet;

import java.util.EventObject;

import org.zeromeaner.game.knet.KNetClient;

public class KNetPanelEvent extends EventObject {
	
	private KNetClient client;
	
	public KNetPanelEvent(KNetPanel knetPanel, KNetClient client) {
		super(knetPanel);
		this.client = client;
	}
	
	@Override
	public KNetPanel getSource() {
		return (KNetPanel) super.getSource();
	}
	
	public KNetClient getClient() {
		return client;
	}
}
