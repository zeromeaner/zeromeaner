package org.zeromeaner.game.eviline;

import org.eviline.core.Configuration;
import org.eviline.core.Engine;
import org.eviline.core.ShapeType;
import org.zeromeaner.game.play.GameEngine;

public class EngineAdapter extends Engine {
	public EngineAdapter() {
		super(new FieldAdapter(), new Configuration());
	}
	
	public void update(GameEngine nullpo) {
		((FieldAdapter) field).update(nullpo.field);
		shape = XYShapeAdapter.toXYShape(nullpo);
		updateNext(nullpo);
	}
	
	protected void updateNext(GameEngine nullpo) {
		ShapeType[] next = new ShapeType[nullpo.nextPieceArraySize - 1];
		for(int i = 0; i < next.length; i++)
			next[i] = XYShapeAdapter.toShapeType(nullpo.getNextObject(nullpo.nextPieceCount + i));
		setNext(next);
	}
}
