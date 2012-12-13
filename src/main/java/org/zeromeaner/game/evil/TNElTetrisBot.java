package org.zeromeaner.game.evil;

import org.eviline.ElTetrisFitness;
import org.zeromeaner.game.play.GameEngine;

public class TNElTetrisBot extends TNBot {
	@Override
	public String getName() {
		return super.getName() + " [El-Tetris]";
	}
	
	@Override
	public void init(GameEngine engine, int playerID) {
		super.init(engine, playerID);
		kernel.setFitness(new ElTetrisFitness());
	}
}
