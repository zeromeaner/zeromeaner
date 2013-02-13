package org.zeromeaner.gui.knet;

import java.util.EventListener;

import org.zeromeaner.game.knet.KNetClient;

public interface KNetPanelListener extends EventListener {
	public void knetPanelInit(KNetPanelEvent e);
	public void knetPanelConnected(KNetPanelEvent e);
	public void knetPanelDisconnected(KNetPanelEvent e);
	public void knetPanelJoined(KNetPanelEvent e);
	public void knetPanelParted(KNetPanelEvent e);
	public void knetPanelShutdown(KNetPanelEvent e);
}
