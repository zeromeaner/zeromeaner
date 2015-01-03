package org.zeromeaner.game.play;

import org.zeromeaner.game.event.EngineAdapter;
import org.zeromeaner.game.event.EngineEvent;
import org.zeromeaner.game.eviline.FieldAdapter;

public class FinesseCounter extends EngineAdapter {
	protected FieldAdapter field;
	protected int xyshape;
	
	public FinesseCounter() {
		field = new FieldAdapter();
		xyshape = -1;
	}
	
	@Override
	public void enginePieceLocked(EngineEvent e) {
		// TODO Auto-generated method stub
		super.enginePieceLocked(e);
	}
}
