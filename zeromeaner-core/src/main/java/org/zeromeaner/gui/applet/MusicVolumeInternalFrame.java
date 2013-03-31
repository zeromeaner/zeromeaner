package org.zeromeaner.gui.applet;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;

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
			}
		});
		cb.setSelected(true);
		add(cb, BorderLayout.CENTER);
		pack();
	}
}
