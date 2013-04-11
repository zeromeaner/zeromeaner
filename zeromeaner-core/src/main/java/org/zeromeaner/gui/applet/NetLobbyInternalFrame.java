package org.zeromeaner.gui.applet;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JInternalFrame;

import org.zeromeaner.gui.knet.KNetPanel;
import org.zeromeaner.gui.knet.KNetPanelAdapter;
import org.zeromeaner.gui.knet.KNetPanelEvent;
import org.zeromeaner.knet.obj.KNetChannelInfo;

public class NetLobbyInternalFrame extends JInternalFrame {
	private KNetPanel knetPanel;
	
	public NetLobbyInternalFrame() {
		knetPanel = new KNetPanel(AppletMain.userId, !NullpoMinoInternalFrame.propGlobal.getProperty(0 + ".ai", "").isEmpty());
		
		setLayout(new BorderLayout());
		add(knetPanel, BorderLayout.CENTER);

		setSize(800, 300);
		
		setTitle("zeromeaner NetPlay");
		
		setLocation(0, 500);
		AppletMain.instance.desktop.add(this);
		setVisible(true);
		
	}
	
	public void init() {
		knetPanel.init();
		knetPanel.addKNetPanelListener(new KNetPanelAdapter() {
			@Override
			public void knetPanelJoined(KNetPanelEvent e) {
				KNetChannelInfo ci = e.getSource().getClient().getCurrentChannel();
				NullpoMinoInternalFrame.gameFrame.strModeToEnter = ci.getMode();
				// FIXME: busy-waiting is bad mojo
				while(!NullpoMinoInternalFrame.gameFrame.strModeToEnter.isEmpty())
					;
			}
			@Override
			public void knetPanelParted(KNetPanelEvent e) {
				if(e.getChannel().getId() == KNetChannelInfo.LOBBY_CHANNEL_ID)
					return;
				NullpoMinoInternalFrame.gameFrame.strModeToEnter = null;
			}
			
			@Override
			public void knetPanelDisconnected(KNetPanelEvent e) {
				NullpoMinoInternalFrame.gameFrame.strModeToEnter = null;
			}
			
			@Override
			public void knetPanelShutdown(KNetPanelEvent e) {
				setVisible(false);
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
