package org.zeromeaner.gui.knet;

import java.util.EventListener;

import org.zeromeaner.game.knet.KNetClient;

public interface KNetPanelListener extends EventListener{
	public void knetPanelConnected(KNetPanelEvent e);
	public void knetPanelDisconnected(KNetPanelEvent e);
}
