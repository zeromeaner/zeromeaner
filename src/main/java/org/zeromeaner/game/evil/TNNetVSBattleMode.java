package org.zeromeaner.game.evil;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.eviline.randomizer.MaliciousRandomizer.MaliciousRandomizerProperties;
import org.eviline.randomizer.Randomizer;
import org.eviline.randomizer.RandomizerFactory;
import org.zeromeaner.game.component.Block;
import org.zeromeaner.game.component.Piece;
import org.zeromeaner.game.event.EventRenderer;
import org.zeromeaner.game.net.NetPlayerClient;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.game.subsystem.mode.NetVSBattleMode;
import org.zeromeaner.game.subsystem.wallkick.StandardWallkick;
import org.zeromeaner.gui.net.NetLobbyFrame;
import org.zeromeaner.util.GeneralUtil;

public class TNNetVSBattleMode extends NetVSBattleMode {
	protected EventRenderer receiver;
	
	protected Map<GameEngine, TNNetplayRandomizer> randomizers = new HashMap<GameEngine, TNNetplayRandomizer>();
	
	protected NetLobbyFrame netLobby;
	
//	protected AtomicInteger sync;
	
	public TNNetVSBattleMode() {
		LINE_ATTACK_TABLE =
			new int[][] {
				// 1-2P, 3P, 4P, 5P, 6P
				{1, 1, 1, 1, 1},	// Single
				{2, 2, 2, 2, 2},	// Double
				{3, 3, 3, 3, 3},	// Triple
				{4, 4, 4, 4, 4},	// Four
				{1, 1, 1, 1, 1},	// T-Mini-S
				{1, 1, 1, 1, 1},	// T-Single
				{2, 2, 2, 2, 2},	// T-Double
				{3, 3, 3, 3, 3},	// T-Triple
				{2, 2, 2, 2, 2},	// T-Mini-D
				{0, 0, 0, 0, 0},	// EZ-T
			};
		LINE_ATTACK_TABLE_ALLSPIN =
			new int[][] {
				// 1-2P, 3P, 4P, 5P, 6P
				{1, 1, 1, 1, 1},	// Single
				{2, 2, 2, 2, 2},	// Double
				{3, 3, 3, 3, 3},	// Triple
				{4, 4, 4, 4, 4},	// Four
				{1, 1, 1, 1, 1},	// T-Mini-S
				{1, 1, 1, 1, 1},	// T-Single
				{2, 2, 2, 2, 2},	// T-Double
				{3, 3, 3, 3, 3},	// T-Triple
				{2, 2, 2, 2, 2},	// T-Mini-D
				{0, 0, 0, 0, 0},	// EZ-T
			};
		COMBO_ATTACK_TABLE = 
			new int[][]{
				{0,0,0,0,0,0,0,0,0,0,0,0}, // 1-2 Player(s)
				{0,0,0,0,0,0,0,0,0,0,0,0}, // 3 Player
				{0,0,0,0,0,0,0,0,0,0,0,0}, // 4 Player
				{0,0,0,0,0,0,0,0,0,0,0,0}, // 5 Player
				{0,0,0,0,0,0,0,0,0,0,0,0}, // 6 Payers
			};
	}
	
	@Override
	public boolean isSynchronousPlay() {
		return true;
	}
	
	@Override
	public String getName() {
		return "NET-EVILINE-VS-BATTLE";
	}

	@Override
	public void playerInit(GameEngine engine, int playerID) {
		super.playerInit(engine, playerID);
		receiver = engine.getOwner().receiver;
		engine.ruleopt = new TNRuleOptions(engine.ruleopt);
		engine.randomizer = new TNNetplayRandomizer();
		randomizers.put(engine, (TNNetplayRandomizer) engine.randomizer);
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
	public void netplayInit(Object obj) {
		netLobby = (NetLobbyFrame) obj;
		super.netplayInit(obj);
	}
	
	@Override
	public boolean onReady(GameEngine engine, int playerID) {
//		sync = new AtomicInteger(0);
		return super.onReady(engine, playerID);
	}
	
	@Override
	public boolean onMove(GameEngine engine, int playerID) {
//		if(sync.get() > 0)
//			return true;
		
		engine.randomizer = randomizers.get(engine);
		retaunt(engine);
		
		return super.onMove(engine, playerID);
	}
	
	@Override
	public void netlobbyOnMessage(NetLobbyFrame lobby, NetPlayerClient client, String[] message) throws IOException {
		super.netlobbyOnMessage(lobby, client, message);
//		System.out.println(Arrays.toString(message));
//		if("game".equals(message[0])) {
//			if("eviline".equals(message[3])) {
//				if("locked".equals(message[4])) {
//					int playerID = Integer.parseInt(message[5]);
//					sync.decrementAndGet();
//				}
//			}
//			if("resultsscreen".equals(message[3]))
//				sync.set(0);
//		}
//		if("playerlogout".equals(message[0]))
//			sync.decrementAndGet();
//		if("dead".equals(message[0]))
//			sync.decrementAndGet();
	}

	@Override
	public void pieceLocked(GameEngine engine, int playerID, int lines) {
//		netLobby.netPlayerClient.send("game\teviline\tlocked\t" + netvsMySeatID + "\n");
//	
//		sync.addAndGet(netLobby.netPlayerClient.getPlayerCount() - 1);
		
		regenerate(engine);
		super.pieceLocked(engine, playerID, lines);
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
		((TNRandomizer) engine.randomizer).regenerate = true;
		int next = engine.randomizer.next();

		engine.nextPieceArrayID[0] = next;
		engine.nextPieceCount = 0;

		retaunt(engine);
		
	}
	
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		engine.randomizer = randomizers.get(engine);
		super.renderLast(engine, playerID);
		if(engine.randomizer == null)
			return;
//		double[] evil = ((TNRandomizer) engine.randomizer).score();
//		receiver.drawScoreFont(engine, playerID, 0, 17, ((TNRandomizer) engine.randomizer).getName(), EventReceiver.COLOR_BLUE);
//		receiver.drawScoreFont(engine, playerID, 0, 18, "" + ((int) evil[0]) + "(" + ((int) evil[1]) + ")", EventReceiver.COLOR_WHITE);
	}

}
