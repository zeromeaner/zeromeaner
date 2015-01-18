package org.zeromeaner.game.randomizer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.zeromeaner.game.component.Block;
import org.zeromeaner.game.component.Piece;
import org.zeromeaner.game.component.ReplayData;
import org.zeromeaner.game.play.GameEngine;

public class ReplayDataRandomizer extends Randomizer {

	
	protected TreeMap<Integer, Integer> ids;
	protected GameEngine engine;
	
	public ReplayDataRandomizer(ReplayData rd) {
		ids = new TreeMap<>();
		for(int c = 0; c < rd.max(); c++) {
			Integer id = rd.getAdditionalData(ReplayData.PIECE_SPAWN, c);
			if(id != null)
				ids.put(c, id);
		}
	}
	
	protected void initEngine() {
		engine.nextPieceArraySize = ids.lastKey() + 1;
		engine.nextPieceArrayID = new int[engine.nextPieceArraySize];
		engine.nextPieceArrayObject = new Piece[engine.nextPieceArraySize];
		for(Map.Entry<Integer, Integer> c : ids.entrySet()) {
			engine.nextPieceArrayID[c.getKey()] = c.getValue();
		}
		for(int i = 0; i < engine.nextPieceArrayObject.length; i++) {
			engine.nextPieceArrayObject[i] = new Piece(engine.nextPieceArrayID[i]);
			engine.nextPieceArrayObject[i].direction = engine.ruleopt.pieceDefaultDirection[engine.nextPieceArrayObject[i].id];
			if(engine.nextPieceArrayObject[i].direction >= Piece.DIRECTION_COUNT) {
				engine.nextPieceArrayObject[i].direction = engine.random.nextInt(Piece.DIRECTION_COUNT);
			}
			engine.nextPieceArrayObject[i].connectBlocks = engine.connectBlocks;
			engine.nextPieceArrayObject[i].setColor(engine.ruleopt.pieceColor[engine.nextPieceArrayObject[i].id]);
			engine.nextPieceArrayObject[i].setSkin(engine.getSkin());
			engine.nextPieceArrayObject[i].updateConnectData();
			engine.nextPieceArrayObject[i].setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
			engine.nextPieceArrayObject[i].setAttribute(Block.BLOCK_ATTRIBUTE_BONE, engine.bone);
		}
	}
	
	@Override
	public void setEngine(GameEngine e) {
		engine = e;
		initEngine();
	}
	
	public void init() {
		initEngine();
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
