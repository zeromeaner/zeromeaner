package org.zeromeaner.game.evil;

import org.eviline.randomizer.ThreadedMaliciousRandomizer;
import org.eviline.randomizer.MaliciousRandomizer.MaliciousRandomizerProperties;

import org.zeromeaner.game.play.GameEngine;

public class TNAggressiveRandomizer extends TNRandomizer {
	@Override
	public void setEngine(GameEngine engine) {
		super.setEngine(engine);
		MaliciousRandomizerProperties mp = new MaliciousRandomizerProperties(3, 0.05, true, 30);
		ThreadedMaliciousRandomizer t = new ThreadedMaliciousRandomizer(mp);
		t.setRandom(engine.random);
		field.setProvider(t);
	}
	
	@Override
	public String getName() {
		return "AGGRESSIVE";
	}
}
