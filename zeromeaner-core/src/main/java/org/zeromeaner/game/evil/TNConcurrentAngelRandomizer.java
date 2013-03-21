package org.zeromeaner.game.evil;

import org.zeromeaner.game.play.GameEngine;

import org.eviline.Shape;
import org.eviline.randomizer.Randomizer;
import org.eviline.randomizer.RandomizerFactory;
import org.eviline.randomizer.RandomizerPresets;

public class TNConcurrentAngelRandomizer extends TNRandomizer {
	@Override
	public void setEngine(GameEngine engine) {
		super.setEngine(engine);
//		MaliciousRandomizerProperties mp = new MaliciousRandomizerProperties(3, .01, true, 15);
//		AngelRandomizer t = new AngelRandomizer(mp);
		Randomizer t = new RandomizerFactory().newRandomizer(RandomizerPresets.ANGELIC);
		// FIXME be able to set the random
//		t.setRandom(engine.random);
		field.setProvider(t);
	}
	
	@Override
	public String getName() {
		return "FAST ANGELIC";
	}
	
	@Override
	public synchronized int next() {
		if(regenerate)
			field.setShape(Shape.O_DOWN);
		return super.next();
	}

}
