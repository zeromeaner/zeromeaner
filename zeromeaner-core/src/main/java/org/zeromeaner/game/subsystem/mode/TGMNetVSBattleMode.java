package org.zeromeaner.game.subsystem.mode;

import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_ID;
import static org.zeromeaner.knet.KNetEventArgs.GAME;
import static org.zeromeaner.knet.KNetEventArgs.TGMNETVSBATTLE_GAME_ATTACK;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import org.zeromeaner.game.component.Block;
import org.zeromeaner.game.component.Piece;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.knet.KNetClient;
import org.zeromeaner.knet.KNetEvent;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class TGMNetVSBattleMode extends NetVSBattleMode {
	@Override
	public String getName() {
		return "NET-TGM-BATTLE";
	}
	
	public static class TGMAttackInfo extends NetVSBattleMode.AttackInfo {
		private ArrayList<Block[]> cleared;
		
		@Override
		public void write(Kryo kryo, Output output) {
			super.write(kryo, output);
			kryo.writeObject(output, cleared);
		}
		
		@Override
		public void read(Kryo kryo, Input input) {
			super.read(kryo, input);
			cleared = kryo.readObject(input, ArrayList.class);
		}

		public ArrayList<Block[]> getCleared() {
			return cleared;
		}

		public void setCleared(ArrayList<Block[]> cleared) {
			this.cleared = cleared;
		}
	}
	
	protected Piece lastPiece;
	protected Piece sentPiece;
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
	protected void sendGarbage(GameEngine engine, int playerID, int targetSeatID, int[] pts) {
		if(lastPiece == sentPiece)
			return;
		TGMAttackInfo attack = new TGMAttackInfo();
		attack.setPoints(pts);
		attack.setLastEvent(lastevent[playerID]);
		attack.setLastB2b(lastb2b[playerID]);
		attack.setLastCombo(lastcombo[playerID]);
		attack.setGarbage(garbage[playerID]);
		attack.setLastPiece(lastpiece[playerID]);
		attack.setTargetSeatId(targetSeatID);
		
		ArrayList<Block[]> cleared = engine.field.getLastLinesAsTGMAttack();
		
		attack.setCleared(cleared);
		// Need to clear out lastPiece from the lines cleared
		
		knetClient().fireTCP(GAME, TGMNETVSBATTLE_GAME_ATTACK, attack, CHANNEL_ID, channelInfo().getId());
		sentPiece = lastPiece;
	}
	
	@Override
	protected void spawnGarbage(GameEngine engine, int playerID, int lines) {
		
	}
	
	@Override
	protected void receiveGarbage(KNetEvent e, int uid, int seatID, int playerID) {
		TGMAttackInfo attack = (TGMAttackInfo) e.get(TGMNETVSBATTLE_GAME_ATTACK);
		int mySeatId = channelInfo().getPlayers().indexOf(knetClient().getSource());
		System.out.println("Attack received.  My seat ID:" + mySeatId + " target ID:" + attack.getTargetSeatId());
		if(attack.getTargetSeatId() != channelInfo().getPlayers().indexOf(knetClient().getSource()))
			return;
		System.out.println("Attacking with " + attack);
		synchronized(pendingGarbage) {
			pendingGarbage.offer(attack);
		}
	}
	
	@Override
	protected void spawnPendingGarbage(GameEngine engine, int playerID) {
	}
	
	@Override
	public void onLast(GameEngine engine, int playerID) {
		super.onLast(engine, playerID);
		
		if(playerID == 0) {
			synchronized(pendingGarbage) {
				for(TGMAttackInfo attack : pendingGarbage) {
					int y = engine.field.getHeight() - 1;
					ArrayList<Block[]> cleared = attack.getCleared();
					cleared.remove(0);
					for(Block[] blocks : cleared) {
						engine.field.pushUp(1);
						for(int x = 0; x < blocks.length; x++) {
							engine.field.setBlock(x, y, blocks[x]);
						}
					}
				}
				pendingGarbage.clear();
			}
		}
	}
	
	@Override
	public void knetEvented(KNetClient client, KNetEvent e) {
		super.knetEvented(client, e);
		if(e.is(GAME) && channelInfo().getSeatId(e) != -1) {
			int uid = e.getSource().getId();
			int seatID = channelInfo().getPlayers().indexOf(e.getSource());
			int playerID = netvsGetPlayerIDbySeatID(seatID);

			// Attack
			if(e.is(TGMNETVSBATTLE_GAME_ATTACK)) { 
				receiveGarbage(e, uid, seatID, playerID);
			}
		}
	}
}
