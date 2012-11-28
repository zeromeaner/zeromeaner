package org.zeromeaner.game.evil;

import org.zeromeaner.game.play.GameEngine;

import org.eviline.randomizer.AngelRandomizer;
import org.eviline.randomizer.MaliciousRandomizer.MaliciousRandomizerProperties;
import org.eviline.randomizer.ThreadedMaliciousRandomizer;

public class TNAngelRandomizer extends TNRandomizer {
	@Override
	public void setEngine(GameEngine engine) {
		super.setEngine(engine);
		MaliciousRandomizerProperties mp = new MaliciousRandomizerProperties(3, 0.01, true, 15);
		AngelRandomizer t = new AngelRandomizer(mp);
		t.setRandom(engine.random);
		field.setProvider(t);
	}
	
	@Override
	public String getName() {
		return "ANGELIC";
	}

}
