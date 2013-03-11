package org.zeromeaner.game.evil;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eviline.Field;
import org.eviline.Shape;
import org.eviline.ai.AI;
import org.eviline.ai.AIKernel;
import org.eviline.fitness.Fitness;

import org.zeromeaner.contrib.net.omegaboshi.nullpomino.game.subsystem.randomizer.Randomizer;
import org.zeromeaner.game.component.Block;
import org.zeromeaner.game.component.Piece;
import org.zeromeaner.game.play.GameEngine;

public class TNRandomizer extends Randomizer {
	private static final Logger log = Logger.getLogger(TNRandomizer.class);

	public static Map<GameEngine, TNRandomizer> preload = new HashMap<GameEngine, TNRandomizer>();

	public GameEngine engine;
	public TNField field;
	public int next;
	public boolean regenerate = true;

	public TNRandomizer() {
	}

	public TNRandomizer(GameEngine e) {
		setEngine(e);
	}

	public String getName() {
		return "EVIL";
	}
	
	public GameEngine getEngine() {
		return engine;
	}

	public void setEngine(GameEngine engine) {
		this.engine = engine;
		if(preload.containsKey(engine)) {
			this.field = preload.get(engine).field;
			this.next = preload.get(engine).next;
			this.regenerate = preload.get(engine).regenerate;
		} else {
			preload.put(engine, this);
			this.field = new TNField(engine);
			this.next = 0;
			this.regenerate = true;
		}
	}

	@Override
	public synchronized int next() {
		if(regenerate) {

			field.update();

			Shape shape = field.getProvider().provideShape(field);
			
			if(shape == null)
				next = Piece.PIECE_NONE;
			else {
				Logger.getLogger(getClass()).debug("Generating piece");
				next = TNPiece.toNullpo(shape.type());

				regenerate = false;
			}
		}
		return next;
	}
	
	public double[] score() {
		if(field == null)
			return new double[2];
		field.update();
		Field f;
		f = field.copyInto(new Field());
		AI.getInstance().getFitness().prepareField(f);
		double base = AI.getInstance().getFitness().score(f);

		f = field.copyInto(new Field());
		f.setShape(TNPiece.fromNullpo(engine.nowPieceObject));
		f.setShapeX(engine.nowPieceX + Field.BUFFER);
		f.setShapeY(engine.nowPieceBottomY + Field.BUFFER);
		if(f.getShape() != null)
			f.clockTick();
		AI.getInstance().getFitness().prepareField(f);
		double withMove = AI.getInstance().getFitness().score(f);
		
		return new double[] {base, withMove - base};
//		return 0;
	}
}
