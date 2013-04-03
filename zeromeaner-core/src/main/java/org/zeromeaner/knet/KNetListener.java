package org.zeromeaner.knet;

import java.util.EventListener;

public interface KNetListener extends EventListener {
	public void knetEvented(KNetClient client, KNetEvent e);
}
