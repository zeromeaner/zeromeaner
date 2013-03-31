package org.zeromeaner.gui.applet;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;

import org.zeromeaner.util.MusicList;

public class MusicVolumeInternalFrame extends JInternalFrame {
	public MusicVolumeInternalFrame() {
		super("Music", false, false, false, false);
		setLayout(new BorderLayout());
		JCheckBox cb = new JCheckBox(new AbstractAction("Play Music") {
			@Override
			public void actionPerformed(ActionEvent e) {
				JCheckBox cb = (JCheckBox) e.getSource();
				if(cb.isSelected())
					MusicList.getInstance().setVolume(1f);
				else
					MusicList.getInstance().setVolume(0f);
				CookieAccess.put("bgm.enable", "" + cb.isSelected());
			}
		});
		boolean sel = true;
		if(CookieAccess.get("bgm.enable") != null)
			sel = Boolean.parseBoolean(CookieAccess.get("bgm.enable"));
		cb.setSelected(sel);
		add(cb, BorderLayout.CENTER);
		JLabel l;
		add(l = new JLabel("<html><center>Music<br>courtesy<br>of<br>10X</center></html>"), BorderLayout.SOUTH);
		l.setHorizontalAlignment(JLabel.CENTER);
		pack();
	}
}
