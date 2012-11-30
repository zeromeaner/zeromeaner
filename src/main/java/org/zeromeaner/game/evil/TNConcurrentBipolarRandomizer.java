package org.zeromeaner.game.evil;

import org.zeromeaner.game.play.GameEngine;

import org.eviline.Shape;
import org.eviline.randomizer.AngelRandomizer;
import org.eviline.randomizer.BipolarRandomizer;
import org.eviline.randomizer.ConcurrentRandomizer;
import org.eviline.randomizer.Randomizer;
import org.eviline.randomizer.RandomizerFactory;
import org.eviline.randomizer.RandomizerPresets;
import org.eviline.randomizer.MaliciousRandomizer.MaliciousRandomizerProperties;

public class TNConcurrentBipolarRandomizer extends TNRandomizer {
	@Override
	public void setEngine(GameEngine engine) {
		super.setEngine(engine);
//		MaliciousRandomizerProperties mp = new MaliciousRandomizerProperties(3, .01, true, 15);
//		BipolarRandomizer t = new BipolarRandomizer(mp);

		Randomizer t = new RandomizerFactory().newRandomizer(RandomizerPresets.BIPOLAR);
		
		// FIXME: Be able to set the random
//		t.setRandom(engine.random);
		field.setProvider(new ConcurrentRandomizer(t));
	}
	
	@Override
	public String getName() {
		return "FAST BIPOLAR";
	}
	
	@Override
	public synchronized int next() {
		if(regenerate)
			field.setShape(Shape.O_DOWN);
		return super.next();
	}

}
