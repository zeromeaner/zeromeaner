package org.zeromeaner.game.evil;

import org.eviline.randomizer.Randomizer;
import org.eviline.randomizer.RandomizerFactory;
import org.eviline.randomizer.MaliciousRandomizer.MaliciousRandomizerProperties;
import org.zeromeaner.game.play.GameEngine;

public class TNNetplayRandomizer extends TNConcurrentRandomizer {
	public TNNetplayRandomizer() {
	}
	
	public TNNetplayRandomizer(GameEngine e) {
		setEngine(e);
	}
	
	@Override
	public void setEngine(GameEngine engine) {
		super.setEngine(engine);
		MaliciousRandomizerProperties mp = new MaliciousRandomizerProperties(3, .01, true, 45);
		mp.put(RandomizerFactory.CONCURRENT, "true");
		mp.put(RandomizerFactory.NEXT, "1");
		Randomizer r = new RandomizerFactory().newRandomizer(mp);
		field.setProvider(r);
	}
	
	@Override
	public String getName() {
		return "NETPLAY RUDE";
	}
}
