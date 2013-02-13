package org.zeromeaner.gui.knet;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.zeromeaner.game.knet.obj.KNetGameInfo;

public class KNetGameInfoPanel extends JPanel {
	private KNetGameInfo game;
	
	public KNetGameInfoPanel(KNetGameInfo game) {
		this.game = game;
		
//		setLayout(new BorderLayout());
//		add(new JLabel("Game Configuration Panel"), BorderLayout.CENTER);
		setBorder(BorderFactory.createTitledBorder("Game Configuration Panel"));
	}
}
