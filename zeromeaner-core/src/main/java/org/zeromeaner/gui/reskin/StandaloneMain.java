package org.zeromeaner.gui.reskin;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;

import org.zeromeaner.plaf.ZeroMetalTheme;

public class StandaloneMain {
	public static void main(String[] args) {
		try {
			MetalLookAndFeel.setCurrentTheme(new ZeroMetalTheme());
			UIManager.setLookAndFeel(new MetalLookAndFeel());
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		new StandaloneFrame().setVisible(true);
	}
}
