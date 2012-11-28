package org.zeromeaner.game.evil;

import org.zeromeaner.game.play.GameEngine;

import org.eviline.Shape;
import org.eviline.randomizer.ConcurrentRandomizer;
import org.eviline.randomizer.MaliciousRandomizer.MaliciousRandomizerProperties;
import org.eviline.randomizer.ThreadedMaliciousRandomizer;

public class TNConcurrentAggressiveRandomizer extends TNRandomizer {
	@Override
	public void setEngine(GameEngine engine) {
		super.setEngine(engine);
		MaliciousRandomizerProperties mp = new MaliciousRandomizerProperties(3, .05, true, 30);
		ThreadedMaliciousRandomizer t = new ThreadedMaliciousRandomizer(mp);
		t.setRandom(engine.random);
		field.setProvider(new ConcurrentRandomizer(t));
	}
	
	@Override
	public String getName() {
		return "FAST AGGRESSIVE";
	}
	
	@Override
	public synchronized int next() {
		if(regenerate)
			field.setShape(Shape.O_DOWN);
		return super.next();
	}

}
