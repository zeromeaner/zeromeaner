package org.zeromeaner.game.subsystem.mode;

import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.game.subsystem.mode.ModeTypes.ModeType;

/**
 * PREVIEW mode - A game mode for Tuning preview
 */
@ModeTypes(ModeType.HIDDEN)
public class PreviewMode extends AbstractMode {
	/*
	 * Mode name
	 */
	@Override
	public String getName() {
		return "PREVIEW";
	}

	/*
	 * Player init
	 */
	@Override
	public void playerInit(GameEngine engine, int playerID) {
		engine.allowTextRenderByReceiver = false;
		engine.readyStart = -1;
		engine.readyEnd = -1;
		engine.goStart = -1;
		engine.goEnd = 10;
	}

	/*
	 * Game Over - or is it?
	 */
	@Override
	public boolean onGameOver(GameEngine engine, int playerID) {
		engine.lives = 1;	// Let's give unlimited lives
		return false;
	}
}
