package org.zeromeaner.gui.reskin;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.zeromeaner.applet.StandaloneApplet;
import org.zeromeaner.util.Localization;
import org.zeromeaner.util.MusicList;
import org.zeromeaner.util.Options;

public class StandaloneMusicVolumePanel extends JPanel {
	private static Localization lz = new Localization();
	
	public StandaloneMusicVolumePanel() {
		JPanel p = new JPanel();
		add(p);
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1,1,1,1), 0, 0);
		JCheckBox cb = new JCheckBox(new AbstractAction(lz.s("play")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				JCheckBox cb = (JCheckBox) e.getSource();
				if(cb.isSelected())
					MusicList.getInstance().setVolume(1f);
				else
					MusicList.getInstance().setVolume(0f);
//				CookieAccess.put("bgm.enable", "" + cb.isSelected());
				Options.standalone().BGM_ENABLE.set(cb.isSelected());
			}
		});
//		boolean sel = true;
//		if(CookieAccess.get("bgm.enable") != null)
//			sel = Boolean.parseBoolean(CookieAccess.get("bgm.enable"));
		boolean sel = Options.standalone().BGM_ENABLE.value();
		cb.setSelected(sel);
		MusicList.getInstance().setVolume(sel ? 1f : 0f);
		p.add(cb, c);
		
		DefaultComboBoxModel smdl = new DefaultComboBoxModel(MusicList.getInstance().filesOnly().toArray());
		smdl.insertElementAt(lz.s("random"), 0);
		smdl.setSelectedItem("boisterous little oscillator");
//		String bgmSelection = CookieAccess.get("bgm.selection");
		String bgmSelection = Options.standalone().BGM_SELECTION.value();
		if(bgmSelection != null && smdl.getIndexOf(bgmSelection) != -1)
			smdl.setSelectedItem(bgmSelection);
		JComboBox selection = new JComboBox(smdl);
		selection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox selection = (JComboBox) e.getSource();
				MusicList.getInstance().setSelection(selection.getSelectedIndex() - 1);
//				CookieAccess.put("bgm.selection", (String) selection.getSelectedItem());
				Options.standalone().BGM_SELECTION.set((String) selection.getSelectedItem());
			}
		});
		MusicList.getInstance().setSelection(selection.getSelectedIndex() - 1);
		c.gridy++; p.add(selection, c);
		
		JLabel l;
		c.gridy++; p.add(l = new JLabel(lz.s("courtesy")), c);
		JButton b;
		c.gridy++; p.add(b = new JButton(new AbstractAction("Tenfold") {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					URL tenfold = new URL("https://soundcloud.com/10x");
					StandaloneApplet.getInstance().getAppletContext().showDocument(tenfold, "_blank");
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		}), c);
		l.setHorizontalAlignment(JLabel.CENTER);
	}
}
