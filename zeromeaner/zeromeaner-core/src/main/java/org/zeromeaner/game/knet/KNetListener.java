package org.zeromeaner.game.knet;

import java.util.EventListener;

public interface KNetListener extends EventListener {
	public void knetEvented(KNetEvent e);
}
