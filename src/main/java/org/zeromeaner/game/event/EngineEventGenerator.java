package org.zeromeaner.game.event;

import org.zeromeaner.game.subsystem.mode.GameMode;

public interface EngineEventGenerator {
	public void addEngineListener(EngineListener l);
	public void addGameMode(GameMode mode);
	public void addReceiver(EventReceiver receiver);
	public void removeEngineListener(EngineListener l);
}
