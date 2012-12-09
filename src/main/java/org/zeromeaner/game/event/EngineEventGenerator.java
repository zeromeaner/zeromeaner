package org.zeromeaner.game.event;

public interface EngineEventGenerator {
	public void addEngineListener(EngineListener l);
	public void removeEngineListener(EngineListener l);
}
