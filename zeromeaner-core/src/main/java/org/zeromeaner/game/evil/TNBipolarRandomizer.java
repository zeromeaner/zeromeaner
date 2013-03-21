package org.zeromeaner.game.evil;

import org.zeromeaner.game.play.GameEngine;

import org.eviline.randomizer.BipolarRandomizer;
import org.eviline.randomizer.MaliciousRandomizer.MaliciousRandomizerProperties;

public class TNBipolarRandomizer extends TNRandomizer {
	@Override
	public void setEngine(GameEngine engine) {
		super.setEngine(engine);
		MaliciousRandomizerProperties mp = new MaliciousRandomizerProperties(3, .01, true, 15);
		BipolarRandomizer t = new BipolarRandomizer(mp);
		t.setRandom(engine.random);
		field.setProvider(t);
	}
	
	@Override
	public String getName() {
		return "BIPOLAR";
	}

}
