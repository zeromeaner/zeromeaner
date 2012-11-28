package org.zeromeaner.game.evil;

import org.eviline.randomizer.ConcurrentRandomizer;
import org.eviline.randomizer.MaliciousRandomizer.MaliciousRandomizerProperties;
import org.eviline.randomizer.ThreadedMaliciousRandomizer;

import org.zeromeaner.game.play.GameEngine;

public class TNSadisticRandomizer extends TNRandomizer {
	@Override
	public void setEngine(GameEngine engine) {
		super.setEngine(engine);
		MaliciousRandomizerProperties mp = new MaliciousRandomizerProperties(5, 0, false, 30);
		ThreadedMaliciousRandomizer rr = new ThreadedMaliciousRandomizer(mp);
		rr.setRandom(engine.random);
		field.setProvider(rr);
	}
}
