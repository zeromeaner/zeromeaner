package org.zeromeaner.gui.applet;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;

import org.zeromeaner.util.MusicList;

public class MusicVolumeInternalFrame extends JInternalFrame {
	public MusicVolumeInternalFrame() {
		super("Music", false, false, false, false);
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1,1,1,1), 0, 0);
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
		MusicList.getInstance().setVolume(sel ? 1f : 0f);
		add(cb, c);
		
		DefaultComboBoxModel smdl = new DefaultComboBoxModel(MusicList.getInstance().filesOnly().toArray());
		smdl.insertElementAt("Random", 0);
		smdl.setSelectedItem("boisterous little oscillator");
		String bgmSelection = CookieAccess.get("bgm.selection");
		if(bgmSelection != null && smdl.getIndexOf(bgmSelection) != -1)
			smdl.setSelectedItem(bgmSelection);
		JComboBox selection = new JComboBox(smdl);
		selection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox selection = (JComboBox) e.getSource();
				MusicList.getInstance().setSelection(selection.getSelectedIndex() - 1);
				CookieAccess.put("bgm.selection", (String) selection.getSelectedItem());
			}
		});
		MusicList.getInstance().setSelection(selection.getSelectedIndex() - 1);
		c.gridy++; add(selection, c);
		
		JLabel l;
		c.gridy++; add(l = new JLabel("<html><center>Music<br>courtesy<br>of<br>10X</center></html>"), c);
		l.setHorizontalAlignment(JLabel.CENTER);
		pack();
	}
}
