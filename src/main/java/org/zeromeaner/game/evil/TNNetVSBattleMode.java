package org.zeromeaner.game.evil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.zeromeaner.game.component.Block;
import org.zeromeaner.game.component.Piece;
import org.zeromeaner.game.event.EventReceiver;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.game.subsystem.mode.NetVSBattleMode;
import org.zeromeaner.game.subsystem.wallkick.StandardWallkick;
import org.zeromeaner.util.GeneralUtil;

public class TNNetVSBattleMode extends NetVSBattleMode {
	protected boolean waiting = false;
	
	protected EventReceiver receiver;
	
	protected Map<GameEngine, TNRandomizer> randomizers = new HashMap<GameEngine, TNRandomizer>();
	
	@Override
	public String getName() {
		return "NET-EVILINE-VS-BATTLE";
	}

	@Override
	public void playerInit(GameEngine engine, int playerID) {
		super.playerInit(engine, playerID);
		receiver = engine.owner.receiver;
		engine.ruleopt = new TNRuleOptions(engine.ruleopt);
		engine.randomizer = new TNConcurrentAggressiveRandomizer();
		randomizers.put(engine, (TNRandomizer) engine.randomizer);
		((TNRandomizer) engine.randomizer).setEngine(engine);
		engine.wallkick = new StandardWallkick();
	}
	
	@Override
	public boolean onSetting(GameEngine engine, int playerID) {
		boolean ret = super.onSetting(engine, playerID);
		engine.randomizer = randomizers.get(engine);
		if(engine.randomizer == null)
			return ret;
		engine.nextPieceArraySize = 1;
		if(engine.nextPieceArrayID != null)
			engine.nextPieceArrayID = Arrays.copyOf(engine.nextPieceArrayID, 1);
		if(engine.nextPieceArrayObject != null)
			engine.nextPieceArrayObject = Arrays.copyOf(engine.nextPieceArrayObject, 1);
		return ret;
	}
	
	public static Piece newPiece(int id) {
		if(id != Piece.PIECE_NONE)
			return new Piece(id);
		Piece p = new Piece();
//		p.id = Piece.PIECE_NONE;
		p.dataX = new int[0][0];
		p.dataY = new int[0][0];
		return p;
	}
	
	@Override
	public boolean onMove(GameEngine engine, int playerID) {
		engine.randomizer = randomizers.get(engine);
		if(!waiting)
			return super.onMove(engine, playerID);
		
		int next = engine.randomizer.next();
		if(next == -1)
			return true;
		
		regenerate(engine);
//		engine.statARE();
		waiting = false;
		
		return true;
	}

	public void regenerate(GameEngine engine) {
		int next = engine.randomizer.next();
		
		engine.nextPieceArrayID = new int[1];
		engine.nextPieceArrayObject = new Piece[1];
		engine.nextPieceArrayID[0] = next;
		
		
		try {
			for(int i = 0; i < engine.nextPieceArrayObject.length; i++) {
				engine.nextPieceArrayObject[i] = newPiece(engine.nextPieceArrayID[i]);
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
			if (engine.randomBlockColor)
			{
				if (engine.blockColors.length < engine.numColors || engine.numColors < 1)
					engine.numColors = engine.blockColors.length;
				for(int i = 0; i < engine.nextPieceArrayObject.length; i++) {
					int size = engine.nextPieceArrayObject[i].getMaxBlock();
					int[] colors = new int[size];
					for (int j = 0; j < size; j++)
						colors[j] = engine.blockColors[engine.random.nextInt(engine.numColors)];
					engine.nextPieceArrayObject[i].setColor(colors);
					engine.nextPieceArrayObject[i].updateConnectData();
				}
			}
		} catch(RuntimeException re) {
		}
	}
	
	@Override
	public void pieceLocked(GameEngine engine, int playerID, int lines) {
		engine.randomizer = randomizers.get(engine);
		((TNRandomizer) engine.randomizer).regenerate = true;

		regenerate(engine);
		
		waiting = true;
		
		super.pieceLocked(engine, playerID, lines);
	}
	
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		engine.randomizer = randomizers.get(engine);
		super.renderLast(engine, playerID);
		if(engine.randomizer == null)
			return;
		double[] evil = ((TNRandomizer) engine.randomizer).score();
		receiver.drawScoreFont(engine, playerID, 0, 17, ((TNRandomizer) engine.randomizer).getName(), EventReceiver.COLOR_BLUE);
		receiver.drawScoreFont(engine, playerID, 0, 18, "" + ((int) evil[0]) + "(" + ((int) evil[1]) + ")", EventReceiver.COLOR_WHITE);
	}

}
