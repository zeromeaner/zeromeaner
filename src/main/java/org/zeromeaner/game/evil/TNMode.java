package org.zeromeaner.game.evil;

import java.util.Arrays;

import org.apache.log4j.Logger;

import org.zeromeaner.game.component.Block;
import org.zeromeaner.game.component.Piece;
import org.zeromeaner.game.event.EventRenderer;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.game.play.GameManager;
import org.zeromeaner.game.subsystem.mode.MarathonMode;
import org.zeromeaner.util.GeneralUtil;

public class TNMode extends MarathonMode {
	private static final Logger log = Logger.getLogger(TNMode.class);

	protected boolean waiting = false;
	
	protected EventRenderer receiver;
	
	protected Integer lastScoreX;
	protected double[] lastScore;
	
	@Override
	public String getName() {
		return "EVILINE";
	}

	@Override
	public void playerInit(GameEngine engine, int playerID) {
		super.playerInit(engine, playerID);
		receiver = engine.getOwner().receiver;
		engine.ruleopt = new TNRuleOptions(engine.ruleopt);
		engine.randomizer = GeneralUtil.loadRandomizer(engine.ruleopt.strRandomizer);
		if(!(engine.randomizer instanceof TNRandomizer))
			engine.randomizer = new TNConcurrentBipolarRandomizer();
		engine.wallkick = GeneralUtil.loadWallkick(engine.ruleopt.strWallkick);
		lastScoreX = null;
		lastScore = null;
	}
	
	@Override
	public boolean onSetting(GameEngine engine, int playerID) {
		boolean ret = super.onSetting(engine, playerID);
		((TNRandomizer) engine.randomizer).setEngine(engine);
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
		
		retaunt(engine);
		
		if(lastScoreX == null || lastScoreX != engine.nowPieceX) {
			lastScoreX = engine.nowPieceX;
			lastScore = null;
		}
		
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
	
	public void retaunt(GameEngine engine) {
		String taunt = ((TNRandomizer) engine.randomizer).field.getProvider().getTaunt();
		if(taunt == null || taunt.isEmpty())
			taunt = " ";
		
		engine.nextPieceArraySize = taunt.length();
		if(engine.nextPieceArrayID != null)
			engine.nextPieceArrayID = Arrays.copyOf(engine.nextPieceArrayID, taunt.length());
		if(engine.nextPieceArrayObject != null)
			engine.nextPieceArrayObject = Arrays.copyOf(engine.nextPieceArrayObject, taunt.length());
		
		for(int i = 1; i < taunt.length(); i++) {
			switch(taunt.charAt(i)) {
			case 'T': 
				engine.nextPieceArrayID[i] = Piece.PIECE_T;
				break;
			case 'S':
				engine.nextPieceArrayID[i] = Piece.PIECE_S;
				break;
			case 'Z':
				engine.nextPieceArrayID[i] = Piece.PIECE_Z;
				break;
			case 'L':
				engine.nextPieceArrayID[i] = Piece.PIECE_L;
				break;
			case 'J':
				engine.nextPieceArrayID[i] = Piece.PIECE_J;
				break;
			case 'O':
				engine.nextPieceArrayID[i] = Piece.PIECE_O;
				break;
			case 'I':
				engine.nextPieceArrayID[i] = Piece.PIECE_I;
				break;
			}
		}
		
		engine.ruleopt.nextDisplay = taunt.length() - 1;
	
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

	public void regenerate(GameEngine engine) {
		int next = engine.randomizer.next();

		engine.nextPieceArrayID[0] = next;
		engine.nextPieceCount = 0;

		retaunt(engine);
		
		
	}
	
	@Override
	public void pieceLocked(GameEngine engine, int playerID, int lines) {
		((TNRandomizer) engine.randomizer).regenerate = true;

		regenerate(engine);
		
		waiting = true;
		lastScoreX = null;
		lastScore = null;
		
		super.pieceLocked(engine, playerID, lines);
	}
	
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		super.renderLast(engine, playerID);
		if(lastScore == null)
			lastScore = ((TNRandomizer) engine.randomizer).score();
		receiver.drawScoreFont(engine, playerID, 0, 17, ((TNRandomizer) engine.randomizer).getName(), EventRenderer.COLOR_BLUE);
		String score = String.format("%1.0f(%s%1.0f)", lastScore[0], lastScore[1] > 0 ? "+" : "", lastScore[1]).toUpperCase();
		receiver.drawScoreFont(engine, playerID, 0, 18, score, EventRenderer.COLOR_WHITE);
	}

	@Override
	public boolean onLineClear(GameEngine engine, int playerID) {
		 int lines = engine.statistics.lines;
		 ((TNRandomizer) engine.randomizer).field.setLines(lines);
		 return super.onLineClear(engine, playerID);
	}
}
