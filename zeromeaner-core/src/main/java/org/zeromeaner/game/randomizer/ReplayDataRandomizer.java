package org.zeromeaner.game.randomizer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.zeromeaner.game.component.Piece;
import org.zeromeaner.game.component.ReplayData;
import org.zeromeaner.game.play.GameEngine;

public class ReplayDataRandomizer extends Randomizer {

	
	protected List<Integer> ids;
	protected GameEngine engine;
	
	public ReplayDataRandomizer(ReplayData rd) {
		ids = new ArrayList<>();
		for(int frame = 0; frame < rd.max(); frame++) {
			Integer id = rd.getAdditionalData(ReplayData.PIECE_SPAWN, frame);
			if(id != null)
				ids.add(id);
		}
	}
	
	@Override
	public void setEngine(GameEngine e) {
		engine = e;
		engine.nextPieceArraySize = ids.size();
		engine.nextPieceArrayID = new int[engine.nextPieceArraySize];
		engine.nextPieceArrayObject = new Piece[engine.nextPieceArraySize];
	}
	
	public void init() {
		engine.nextPieceArraySize = ids.size();
		engine.nextPieceArrayID = new int[engine.nextPieceArraySize];
		engine.nextPieceArrayObject = new Piece[engine.nextPieceArraySize];
	}

	@Override
	protected int next() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int next(int c) {
		return ids.get(c);
	}

}
