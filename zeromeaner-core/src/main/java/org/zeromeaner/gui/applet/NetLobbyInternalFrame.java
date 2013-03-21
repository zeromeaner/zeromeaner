package org.zeromeaner.gui.applet;

import java.awt.BorderLayout;

import javax.swing.JInternalFrame;

import org.zeromeaner.gui.knet.KNetPanel;
import org.zeromeaner.gui.knet.KNetPanelAdapter;
import org.zeromeaner.gui.knet.KNetPanelEvent;
import org.zeromeaner.gui.knet.KNetPanelListener;

public class NetLobbyInternalFrame extends JInternalFrame {
	private KNetPanel knetPanel;
	
	public NetLobbyInternalFrame() {
		knetPanel = new KNetPanel();
		
		setLayout(new BorderLayout());
		add(knetPanel, BorderLayout.CENTER);

		setSize(800, 300);
		
		AppletMain.instance.desktop.add(this);
		setVisible(true);
		
		setLocation(0, 510);
	}
	
	public void init() {
		knetPanel.init();
		knetPanel.addKNetPanelListener(new KNetPanelAdapter() {
			@Override
			public void knetPanelJoined(KNetPanelEvent e) {
				NullpoMinoInternalFrame.gameFrame.strModeToEnter = e.getChannel().getMode();
			}
			@Override
			public void knetPanelParted(KNetPanelEvent e) {
				NullpoMinoInternalFrame.gameFrame.strModeToEnter = null;
			}
		});
	}
	
	public void shutdown() {
		knetPanel.shutdown();
	}
	
	public KNetPanel getKnetPanel() {
		return knetPanel;
	}
}
