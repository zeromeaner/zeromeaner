package org.zeromeaner.game.subsystem.mode;

import java.util.ArrayDeque;
import java.util.Deque;

import org.zeromeaner.game.component.Block;
import org.zeromeaner.game.component.Piece;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.knet.KNetEvent;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import static org.zeromeaner.knet.KNetEventArgs.*;

public class TGMNetVSBattleMode extends NetVSBattleMode {
	@Override
	public String getName() {
		return "NET-TGM-BATTLE";
	}
	
	public static class TGMAttackInfo extends NetVSBattleMode.AttackInfo {
		private Piece piece;
		private int x;
		
		@Override
		public void write(Kryo kryo, Output output) {
			super.write(kryo, output);
			kryo.writeObject(output, piece);
			output.writeInt(x, true);
		}
		
		@Override
		public void read(Kryo kryo, Input input) {
			super.read(kryo, input);
			piece = kryo.readObject(input, Piece.class);
			x = input.readInt(true);
		}

		public Piece getPiece() {
			return piece;
		}

		public void setPiece(Piece piece) {
			this.piece = piece;
		}

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}
	}
	
	protected Piece lastPiece;
	protected int lastPieceX;
	protected Deque<TGMAttackInfo> pendingGarbage = new ArrayDeque<TGMAttackInfo>();
	
	@Override
	public boolean onMove(GameEngine engine, int playerID) {
		boolean ret = super.onMove(engine, playerID);
		if(playerID == 0) {
			lastPiece = engine.nowPieceObject;
			lastPieceX = engine.nowPieceX;
		}
		return ret;
	}
	
	@Override
	protected void sendGarbage(int playerID, int targetSeatID, int[] pts) {
		TGMAttackInfo attack = new TGMAttackInfo();
		attack.setPoints(pts);
		attack.setLastEvent(lastevent[playerID]);
		attack.setLastB2b(lastb2b[playerID]);
		attack.setLastCombo(lastcombo[playerID]);
		attack.setGarbage(garbage[playerID]);
		attack.setLastPiece(lastpiece[playerID]);
		attack.setTargetSeatId(targetSeatID);
		
		attack.setPiece(lastPiece);
		attack.setX(lastPieceX);
		knetClient().fireTCP(GAME, TGMNETVSBATTLE_GAME_ATTACK, attack);
	}
	
	@Override
	protected void spawnGarbage(GameEngine engine, int playerID, int lines) {
		
	}
	
	@Override
	protected void receiveGarbage(KNetEvent e, int uid, int seatID, int playerID) {
		TGMAttackInfo attack = (TGMAttackInfo) e.get(TGMNETVSBATTLE_GAME_ATTACK);
		if(netvsPlayerSeatID[0] != attack.getTargetSeatId())
			return;
		synchronized(pendingGarbage) {
			pendingGarbage.offer(attack);
		}
	}
	
	@Override
	public void onLast(GameEngine engine, int playerID) {
		super.onLast(engine, playerID);
		
		synchronized(pendingGarbage) {
			for(TGMAttackInfo attack : pendingGarbage) {
				Piece g = attack.getPiece();
				engine.field.pushUp(g.getHeight() - 1);
				int h = 0;
				for(
						int y = engine.field.getHeightWithoutHurryupFloor() - g.getHeight() + 1; 
						y < engine.field.getHeightWithoutHurryupFloor(); 
						y++) 
				{
					for(int x = 0; x < engine.field.getWidth(); x++) {
						boolean hole = false;
						for(int i = 0; i < g.dataX[g.direction].length; i++) {
							int gx = attack.getX() + g.dataX[g.direction][i];
							int gy = engine.field.getHeightWithoutHurryupFloor() - g.getHeight() + 1 + g.dataY[g.direction][i];
							if(gx == x && gy == y)
								hole = true;
						}
						if(hole)
							continue;
						Block blk = new Block();
						blk.color = Block.BLOCK_COLOR_GRAY;
						blk.skin = engine.getSkin();
						blk.attribute = Block.BLOCK_ATTRIBUTE_VISIBLE | Block.BLOCK_ATTRIBUTE_OUTLINE | Block.BLOCK_ATTRIBUTE_GARBAGE;
						engine.field.setBlock(x, y, blk);
					}
					h++;
				}
			}
			pendingGarbage.clear();
		}
	}
}
