package org.zeromeaner.game.subsystem.mode;

import org.apache.log4j.Logger;
import org.zeromeaner.contrib.net.omegaboshi.nullpomino.game.subsystem.randomizer.Randomizer;
import org.zeromeaner.game.component.Block;
import org.zeromeaner.game.component.Controller;
import org.zeromeaner.game.component.Field;
import org.zeromeaner.game.component.Piece;
import org.zeromeaner.game.component.RuleOptions;
import org.zeromeaner.game.component.SpeedParam;
import org.zeromeaner.game.component.Statistics;
import org.zeromeaner.game.event.EventRenderer;
import org.zeromeaner.game.knet.KNetClient;
import org.zeromeaner.game.knet.KNetEvent;
import org.zeromeaner.game.knet.KNetEventSource;
import org.zeromeaner.game.knet.KNetGameClient;
import org.zeromeaner.game.knet.KNetListener;
import org.zeromeaner.game.knet.obj.KNetChannelInfo;
import org.zeromeaner.game.knet.obj.KNetGameInfo;
import org.zeromeaner.game.knet.obj.KNetPlayerInfo;
import org.zeromeaner.game.knet.obj.PieceHold;
import org.zeromeaner.game.knet.obj.PieceMovement;
import org.zeromeaner.game.knet.obj.Replay;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.game.play.GameManager;
import org.zeromeaner.game.subsystem.mode.ModeTypes.ModeType;
import org.zeromeaner.game.subsystem.wallkick.Wallkick;
import org.zeromeaner.gui.knet.KNetPanel;
import org.zeromeaner.gui.knet.KNetPanelEvent;
import org.zeromeaner.gui.knet.KNetPanelListener;
import org.zeromeaner.util.CustomProperties;
import org.zeromeaner.util.GeneralUtil;

import static org.zeromeaner.game.knet.KNetEventArgs.*;

/**
 * Special base class for netplay
 */
@ModeTypes(ModeType.HIDDEN)
public class AbstractNetMode extends AbstractMode implements KNetListener, KNetPanelListener {
	public static class DefaultStats {
		private Statistics statistics;
		private int goalType;
		private boolean gameActive;
		private boolean timerActive;
		private int lastScore;
		private int scGetTime;
		private int lastEvent;
		private boolean lastB2b;
		private int lastCombo;
		private int lastPiece;
		private int bg;
		
		public Statistics getStatistics() {
			return statistics;
		}
		public void setStatistics(Statistics statistics) {
			this.statistics = statistics;
		}
		public int getGoalType() {
			return goalType;
		}
		public void setGoalType(int goalType) {
			this.goalType = goalType;
		}
		public boolean isGameActive() {
			return gameActive;
		}
		public void setGameActive(boolean gameActive) {
			this.gameActive = gameActive;
		}
		public boolean isTimerActive() {
			return timerActive;
		}
		public void setTimerActive(boolean timerActive) {
			this.timerActive = timerActive;
		}
		public int getLastScore() {
			return lastScore;
		}
		public void setLastScore(int lastScore) {
			this.lastScore = lastScore;
		}
		public int getScGetTime() {
			return scGetTime;
		}
		public void setScGetTime(int scGetTime) {
			this.scGetTime = scGetTime;
		}
		public int getLastEvent() {
			return lastEvent;
		}
		public void setLastEvent(int lastEvent) {
			this.lastEvent = lastEvent;
		}
		public boolean isLastB2b() {
			return lastB2b;
		}
		public void setLastB2b(boolean lastB2b) {
			this.lastB2b = lastB2b;
		}
		public int getLastCombo() {
			return lastCombo;
		}
		public void setLastCombo(int lastCombo) {
			this.lastCombo = lastCombo;
		}
		public int getLastPiece() {
			return lastPiece;
		}
		public void setLastPiece(int lastPiece) {
			this.lastPiece = lastPiece;
		}
		public int getBg() {
			return bg;
		}
		public void setBg(int bg) {
			this.bg = bg;
		}
	}
	
	public static class DefaultOptions {
		private SpeedParam speed;
		private int bgmno;
		private boolean big;
		private int goalType;
		private int presetNumber;
		public SpeedParam getSpeed() {
			return speed;
		}
		public void setSpeed(SpeedParam speed) {
			this.speed = speed;
		}
		public int getBgmno() {
			return bgmno;
		}
		public void setBgmno(int bgmno) {
			this.bgmno = bgmno;
		}
		public boolean isBig() {
			return big;
		}
		public void setBig(boolean big) {
			this.big = big;
		}
		public int getGoalType() {
			return goalType;
		}
		public void setGoalType(int goalType) {
			this.goalType = goalType;
		}
		public int getPresetNumber() {
			return presetNumber;
		}
		public void setPresetNumber(int presetNumber) {
			this.presetNumber = presetNumber;
		}
	}
	
	/** Log (Declared in NetDummyMode) */
	static Logger log = Logger.getLogger(AbstractNetMode.class);

	protected KNetPanel knetPanel;
	
	/** NET: Lobby (Declared in NetDummyMode) */
	protected KNetGameClient knetClient() {
		if(knetPanel == null)
			return null;
		return knetPanel.getClient();
	}
	
	protected KNetChannelInfo channelInfo() {
		if(knetClient() == null)
			return null;
		return knetPanel.getClient().getCurrentChannel();
	}

	/** NET: GameManager (Declared in NetDummyMode; Don't override it!) */
	protected GameManager owner;

	/** NET: true if netplay (Declared in NetDummyMode) */
	protected boolean netIsNetPlay;

	/** NET: true if watch mode (Declared in NetDummyMode) */
	protected boolean netIsWatch;

	/** NET: Number of spectators (Declared in NetDummyMode) */
	protected int netNumSpectators;

	/** NET: Send all movements even if there are no spectators */
	protected boolean netForceSendMovements;

	/** NET: Previous piece informations (Declared in NetDummyMode) */
	protected int netPrevPieceID, netPrevPieceX, netPrevPieceY, netPrevPieceDir;

	/** NET: The skin player using (Declared in NetDummyMode) */
	protected int netPlayerSkin;

	/** NET: If true, NetDummyMode will always send attributes when sending the field (Declared in NetDummyMode) */
	protected boolean netAlwaysSendFieldAttributes;

	/** NET: Player name (Declared in NetDummyMode) */
	protected String netPlayerName;

	/** NET: Replay send status (0:Before Send 1:Sending 2:Sent) (Declared in NetDummyMode) */
	protected int netReplaySendStatus;

	/** NET: Current round's online ranking rank (Declared in NetDummyMode) */
	protected int[] netRankingRank;

	/** NET: True if new personal record (Declared in NetDummyMode) */
	protected boolean netIsPB;

	/** NET-VS: Local player's seat ID (-1:Spectator) */
	protected int netvsMySeatID() {
		if(channelInfo() == null)
			return -1;
		return channelInfo().getPlayers().indexOf(knetClient().getSource());
	}

	protected boolean synchronousPlay;
	
	public boolean isSynchronousPlay() {
		return synchronousPlay;
	}
	
	protected KNetPlayerInfo currentPlayer() {
		int index = channelInfo().getPlayers().indexOf(knetClient().getSource());
		if(index == -1)
			return null;
		return channelInfo().getPlayerInfo().get(index);
	}

	protected KNetGameInfo currentGame() {
		if(channelInfo() == null)
			return null;
		return channelInfo().getGame();
	}
	
	/*
	 * NET: Mode name
	 */
	@Override
	public String getName() {
		return "NET-DUMMY";
	}

	/**
	 * NET: Netplay Initialization. NetDummyMode will set the lobby's current mode to this.
	 */
	@Override
	public void netplayInit(KNetPanel obj) {
		knetPanel = obj;
		knetPanel.addKNetPanelListener(this);
		if(knetPanel.getClient() != null)
			knetPanel.getClient().addKNetListener(this);
	}

	/**
	 * NET: Netplay Unload. NetDummyMode will set the lobby's current mode to null.
	 */
	@Override
	public void netplayUnload(KNetPanel obj) {
		obj.removeKNetPanelListener(this);
	}

	/**
	 * NET: Mode Initialization. NetDummyMode will set the "owner" variable.
	 */
	@Override
	public void modeInit(GameManager manager) {
		log.debug("modeInit() on NetDummyMode");
		owner = manager;
		netIsNetPlay = false;
		netIsWatch = false;
		netNumSpectators = 0;
		netForceSendMovements = false;
		netPlayerName = "";
		if(knetClient() != null)
			netIsNetPlay = true;
	}

	/**
	 * NET: Initialization for each player. NetDummyMode will stop and hide all players.
	 * Call netPlayerInit if you want to init NetPlay variables.
	 */
	@Override
	public void playerInit(GameEngine engine, int playerID) {
		engine.stat = GameEngine.STAT_NOTHING;
		engine.isVisible = false;
	}

	/**
	 * NET: Initialize various NetPlay variables. Usually called from playerInit.
	 * @param engine GameEngine
	 * @param playerID Player ID
	 */
	protected void netPlayerInit(GameEngine engine, int playerID) {
		netPrevPieceID = Piece.PIECE_NONE;
		netPrevPieceX = 0;
		netPrevPieceY = 0;
		netPrevPieceDir = 0;
		netPlayerSkin = 0;
		netReplaySendStatus = 0;
		netRankingRank = new int[2];
		netRankingRank[0] = -1;
		netRankingRank[1] = -1;
		netIsPB = false;
		netAlwaysSendFieldAttributes = false;

		if(netIsWatch) {
			engine.isNextVisible = false;
			engine.isHoldVisible = false;
		}
	}

	/**
	 * NET: When the pieces can move. NetDummyMode will send field/next/stats/piece movements.
	 */
	@Override
	public boolean onMove(GameEngine engine, int playerID) {
		// NET: Send field, next, and stats
		if((engine.ending == 0) && (engine.statc[0] == 0) && (engine.holdDisable == false) &&
		   (netIsNetPlay) && (!netIsWatch) && ((netNumSpectators > 0) || (netForceSendMovements)))
		{
			netSendField(engine, true);
			netSendStats(engine);
		}
		// NET: Send piece movement
		if((engine.ending == 0) && (netIsNetPlay) && (!netIsWatch) && (engine.nowPieceObject != null) &&
		   ((netNumSpectators > 0) || (netForceSendMovements)))
		{
			if(netSendPieceMovement(engine, false)) {
				netSendNextAndHold(engine);
			}
		}
		// NET: Stop game in watch mode
		if(netIsWatch) {
			return true;
		}

		return false;
	}

	@Override
	public boolean onReady(GameEngine engine, int playerID) {
		return super.onReady(engine, playerID);
	}
	
	/**
	 * NET: When the piece locked. NetDummyMode will send field and stats.
	 */
	@Override
	public void pieceLocked(GameEngine engine, int playerID, int lines) {
		// NET: Send field and stats
		if((engine.ending == 0) && (netIsNetPlay) && (!netIsWatch) && ((netNumSpectators > 0) || (netForceSendMovements))) {
			netSendField(engine, false);
			netSendStats(engine);
			knetClient().fireTCP(GAME, GAME_SYNCHRONOUS, GAME_SYNCHRONOUS_LOCKED, true);
		}
	}

	/**
	 * NET: Line clear. NetDummyMode will send field and stats.
	 */
	@Override
	public boolean onLineClear(GameEngine engine, int playerID) {
		// NET: Send field and stats
		if((engine.statc[0] == 1) && (engine.ending == 0) && (netIsNetPlay) && (!netIsWatch) && ((netNumSpectators > 0) || (netForceSendMovements))) {
			netSendField(engine, false);
			netSendStats(engine);
		}
		return false;
	}

	/**
	 * NET: ARE. NetDummyMode will send field, next and stats.
	 */
	@Override
	public boolean onARE(GameEngine engine, int playerID) {
		// NET: Send field, next, and stats
		if((engine.statc[0] == 0) && (engine.ending == 0) && (netIsNetPlay) && (!netIsWatch) && ((netNumSpectators > 0) || (netForceSendMovements))) {
			netSendField(engine, true);
			netSendNextAndHold(engine);
			netSendStats(engine);
		}
		return false;
	}

	/**
	 * NET: Ending start. NetDummyMode will send ending start messages.
	 */
	@Override
	public boolean onEndingStart(GameEngine engine, int playerID) {
		if(engine.statc[2] == 0) {
			// NET: Send game completed messages
			if(netIsNetPlay && !netIsWatch && ((netNumSpectators > 0) || (netForceSendMovements))) {
				netSendField(engine, false);
				netSendNextAndHold(engine);
				netSendStats(engine);
				knetClient().fire(GAME, true, GAME_ENDING, true, CHANNEL_ID, channelInfo().getId());
			}
		}
		return false;
	}

	/**
	 * NET: "Excellent!" screen
	 */
	@Override
	public boolean onExcellent(GameEngine engine, int playerID) {
		if(engine.statc[0] == 0) {
			// NET: Send game completed messages
			if(netIsNetPlay && !netIsWatch && ((netNumSpectators > 0) || (netForceSendMovements))) {
				netSendField(engine, false);
				netSendNextAndHold(engine);
				netSendStats(engine);
//				netLobby.netPlayerClient.send("game\texcellent\n");
				knetClient().fire(GAME, true, GAME_EXCELLENT, true);
			}
		}
		return false;
	}

	/**
	 * NET: Game Over
	 */
	@Override
	public boolean onGameOver(GameEngine engine, int playerID) {
		// NET: Send messages / Wait for messages
		if(netIsNetPlay){
			if(!netIsWatch) {
				if(engine.statc[0] == 0) {
					// Send end-of-game messages
					if((netNumSpectators > 0) || (netForceSendMovements)) {
						netSendField(engine, false);
						netSendNextAndHold(engine);
						netSendStats(engine);
					}
					netSendEndGameStats(engine);
					knetClient().fireTCP(DEAD, true, DEAD_PLACE, 0);
				} else if(engine.statc[0] >= engine.field.getHeight() + 1 + 180) {
					// To results screen
					knetClient().fireTCP(GAME, true, GAME_RESULTS_SCREEN, true);
				}
			} else {
				if(engine.statc[0] < engine.field.getHeight() + 1 + 180) {
					return false;
				} else {
					engine.field.reset();
					engine.stat = GameEngine.STAT_RESULT;
					engine.resetStatc();
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * NET: Results screen
	 */
	@Override
	public boolean onResult(GameEngine engine, int playerID) {
		// NET: Retry
		if(netIsNetPlay) {
			engine.allowTextRenderByReceiver = false;

			// Replay Send
			if(netIsWatch || owner.replayMode) {
				netReplaySendStatus = 2;
			} else if(netReplaySendStatus == 0) {
				netReplaySendStatus = 1;
				netSendReplay(engine);
			}

			// Retry
			if(engine.ctrl.isPush(Controller.BUTTON_A) && !netIsWatch && (netReplaySendStatus == 2)) {
				engine.playSE("decide");
				if((netNumSpectators > 0) || (netForceSendMovements)) {
					knetClient().fire(GAME, true, GAME_RETRY, true);
					netSendOptions(engine);
				}
				owner.reset();
			}

			return true;
		}

		return false;
	}

	/**
	 * NET: Update menu cursor. NetDummyMode will signal cursor movement to all spectators.
	 */
	@Override
	protected int updateCursor(GameEngine engine, int maxCursor, int playerID) {
		// NET: Don't execute in watch mode
		if(netIsWatch) return 0;

		int change = super.updateCursor(engine, maxCursor, playerID);

		// NET: Signal cursor change
		if((engine.ctrl.isMenuRepeatKey(Controller.BUTTON_UP) || engine.ctrl.isMenuRepeatKey(Controller.BUTTON_DOWN)) &&
			netIsNetPlay && ((netNumSpectators > 0) || (netForceSendMovements)))
		{
			knetClient().fire(GAME, true, GAME_CURSOR, engine.statc[2]);
		}

		return change;
	}

	/**
	 * NET: Retry key
	 */
	@Override
	public void netplayOnRetryKey(GameEngine engine, int playerID) {
		if(netIsNetPlay && !netIsWatch) {
			owner.reset();
//			netLobby.netPlayerClient.send("reset1p\n");
			knetClient().fire(RESET_1P, true);
			netSendOptions(engine);
		}
	}

	/*
	 * NET: Message received
	 */
	@Override
	public void knetEvented(KNetClient client, KNetEvent e) {
		if(knetClient().getSource().equals(e.getSource()))
			return;
		
		if(e.is(DISCONNECTED))
			netlobbyOnDisconnect(client, e);
		
		// Player status update
		if(e.is(PLAYER_UPDATE)) {
			netUpdatePlayerExist();
		}
		// When someone logout
		if(e.is(PLAYER_LOGOUT)) {
			KNetEventSource pInfo = (KNetEventSource) e.get(PLAYER_LOGOUT);

			if(channelInfo() != null && channelInfo().getMembers().contains(pInfo)) {
				netUpdatePlayerExist();
			}
		}
		// Game started
		if(e.is(START)) {
			log.debug("NET: Game started");

			if(netIsWatch) {
				owner.reset();
				owner.engine[0].stat = GameEngine.STAT_READY;
				owner.engine[0].resetStatc();
			}
		}
		// Dead
		if(e.is(DEAD)) {
			log.debug("NET: Dead");

			if(netIsWatch) {
				owner.engine[0].gameEnded();

				if((owner.engine[0].stat != GameEngine.STAT_GAMEOVER) && (owner.engine[0].stat != GameEngine.STAT_RESULT)) {
					owner.engine[0].stat = GameEngine.STAT_GAMEOVER;
					owner.engine[0].resetStatc();
				}
			}
		}
		// Replay send fail
		if(e.is(REPLAY_NOT_RECEIVED)) {
			netReplaySendStatus = 1;
			netSendReplay(owner.engine[0]);
		}
		// Replay send complete
		if(e.is(REPLAY_RECEIVED)) {
			netReplaySendStatus = 2;
		}
		// Reset
		if(e.is(RESET_1P)) {
			if(netIsWatch) {
				owner.reset();
			}
		}
		// Game messages
		if(e.is(GAME)) {
			if(netIsWatch) {
				GameEngine engine = owner.engine[0];
				if(engine.field == null) {
					engine.field = new Field();
				}

				// Move cursor
				if(e.is(GAME_CURSOR)) {
					if(engine.stat == GameEngine.STAT_SETTING) {
						engine.statc[2] = (Integer) e.get(GAME_CURSOR);
						engine.playSE("cursor");
					}
				}
				// Change game options
				if(e.is(GAME_OPTIONS)) {
					netRecvOptions(engine, e);
				}
				// Field
				if(e.is(GAME_FIELD)) {
					netRecvField(engine, e);
				}
				// Stats
				if(e.is(GAME_STATS)) {
					netRecvStats(engine, e);
				}
				// Current Piece
				if(e.is(GAME_PIECE_MOVEMENT)) {
					netRecvPieceMovement(engine, e);
				}
				// Next and Hold
				if(e.is(GAME_NEXT_PIECE)) {
					netRecvNextAndHold(engine, e);
				}
				// Ending
				if(e.is(GAME_ENDING)) {
					engine.ending = 1;
					if(!engine.staffrollEnable) engine.gameEnded();
					engine.stat = GameEngine.STAT_ENDINGSTART;
					engine.resetStatc();
				}
				// Excellent
				if(e.is(GAME_EXCELLENT)) {
					engine.stat = GameEngine.STAT_EXCELLENT;
					engine.resetStatc();
				}
				// Retry
				if(e.is(GAME_RETRY)) {
					engine.ending = 0;
					engine.gameEnded();
					engine.stat = GameEngine.STAT_SETTING;
					engine.resetStatc();
					engine.playSE("decide");
				}
				// Display results screen
				if(e.is(GAME_RESULTS_SCREEN)) {
					engine.field.reset();
					engine.stat = GameEngine.STAT_RESULT;
					engine.resetStatc();
				}
			}
		}
		
		if(isSynchronousPlay()) {
			GameEngine eng = owner.engine[0];
			eng.synchronousIncrement = getPlayers() - 1;
			if(e.is(GAME)) {
				if(e.is(GAME_SYNCHRONOUS)) {
					if(e.is(GAME_SYNCHRONOUS_LOCKED)) {
						eng.synchronousSync.decrementAndGet();
					}
				}
				if(e.is(GAME_RESULTS_SCREEN)) {
					eng.synchronousSync.set(0);
				}
			}
			if(e.is(PLAYER_LOGOUT)) {
				eng.synchronousSync.decrementAndGet();
			}
			if(e.is(DEAD)) {
				eng.synchronousSync.decrementAndGet();
			}
		}
	}

	/*
	 * NET: When the lobby window is closed
	 */
	public void netlobbyOnExit(KNetPanel lobby) {
		try {
			for(int i = 0; i < owner.engine.length; i++) {
				owner.engine[i].quitflag = true;
			}
		} catch (Exception e) {}
	}

	/**
	 * NET-VS: Disconnected
	 */
	public void netlobbyOnDisconnect(KNetClient client, KNetEvent e) {
	}

	/**
	 * NET: When you join the room
	 * @param lobby NetLobbyFrame
	 * @param client NetPlayerClient
	 * @param roomInfo NetRoomInfo
	 */
	protected void netOnJoin(KNetClient client, KNetChannelInfo roomInfo) {
		log.debug("onJoin on NetDummyMode");

		netIsNetPlay = true;
		netIsWatch = !channelInfo().getPlayers().contains(knetClient().getSource());
		netNumSpectators = 0;
		netUpdatePlayerExist();

		if(netIsWatch) {
			owner.engine[0].isNextVisible = false;
			owner.engine[0].isHoldVisible = false;
		}

		if(roomInfo != null) {
			// Set to locked rule
			if(channelInfo().isRuleLock()) {
				log.info("Set locked rule");
				Randomizer randomizer = GeneralUtil.loadRandomizer(channelInfo().getRule().strRandomizer, owner.engine[0]);
				Wallkick wallkick = GeneralUtil.loadWallkick(channelInfo().getRule().strWallkick);
				if(owner == null)
					return;
				owner.engine[0].ruleopt.copy(channelInfo().getRule());
				owner.engine[0].randomizer = randomizer;
				owner.engine[0].wallkick = wallkick;
				loadRanking(owner.modeConfig, owner.engine[0].ruleopt.strRuleName);
			}
		}
	}

	/**
	 * NET: Read rankings from property file. This is used from netOnJoin.
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	protected void loadRanking(CustomProperties prop, String ruleName) {
	}

	/**
	 * NET: Update player count
	 */
	protected void netUpdatePlayerExist() {
		netNumSpectators = 0;
		netPlayerName = "";

//		if((channelInfo != null) && (channelInfo().roomID != -1) && (netLobby != null)) {
//			for(NetPlayerInfo pInfo: netLobby.updateSameRoomPlayerInfoList()) {
//				if(pInfo.roomID == channelInfo().roomID) {
//					if(pInfo.seatID == 0) {
//						netPlayerName = pInfo.strName;
//					} else if(pInfo.seatID == -1) {
//						netNumSpectators++;
//					}
//				}
//			}
//		}
		
		if(channelInfo() != null)
			netNumSpectators = channelInfo().getMembers().size();
		if(knetClient() != null)
			netPlayerName = knetClient().getSource().getName();
	}

	/**
	 * NET: Draw game-rate to bottom-right of screen.
	 * @param engine GameEngine
	 */
	protected void netDrawGameRate(GameEngine engine) {
		if(netIsNetPlay && !netIsWatch && engine.gameStarted && (engine.startTime != 0)) {
			float gamerate = 0f;
			if(engine.endTime != 0) {
				gamerate = engine.statistics.gamerate;
			} else {
				long nowtime = System.nanoTime();
				gamerate = (float)(engine.replayTimer / (0.00000006*(nowtime-engine.startTime)));
			}

			String strTemp = String.format("%.0f%%", (float)(gamerate * 100f));
			String strTemp2 = String.format("%40s", strTemp);

			int fontcolor = EventRenderer.COLOR_BLUE;
			if(gamerate < 1f) fontcolor = EventRenderer.COLOR_YELLOW;
			if(gamerate < 0.9f) fontcolor = EventRenderer.COLOR_ORANGE;
			if(gamerate < 0.8f) fontcolor = EventRenderer.COLOR_RED;
			owner.receiver.drawDirectFont(engine, 0, 0, 480-32, strTemp2, fontcolor);
		}
	}

	/**
	 * NET: Draw spectator count in score area.
	 * @param engine GameEngine
	 * @param x X offset
	 * @param y Y offset
	 */
	protected void netDrawSpectatorsCount(GameEngine engine, int x, int y) {
		if(netIsNetPlay) {
			int fontcolor = netIsWatch ? EventRenderer.COLOR_GREEN : EventRenderer.COLOR_RED;
			owner.receiver.drawScoreFont(engine, engine.getPlayerID(), x, y+0, "SPECTATORS", fontcolor);
			owner.receiver.drawScoreFont(engine, engine.getPlayerID(), x, y+1, "" + netNumSpectators, EventRenderer.COLOR_WHITE);

			if(engine.stat == GameEngine.STAT_SETTING && !netIsWatch && netIsNetRankingViewOK(engine)) {
				int y2 = y + 2;
				if(y2 > 24) y2 = 24;
				String strBtnD = engine.getOwner().receiver.getKeyNameByButtonID(engine, Controller.BUTTON_D);
				owner.receiver.drawScoreFont(engine, engine.getPlayerID(), x, y2, "D(" + strBtnD + " KEY):\n NET RANKING", EventRenderer.COLOR_GREEN);
			}
		}
	}

	/**
	 * NET: Draw player's name. It may also appear in offline replay.
	 * @param engine GameEngine
	 */
	protected void netDrawPlayerName(GameEngine engine) {
		if((netPlayerName != null) && (netPlayerName.length() > 0)) {
			String name = netPlayerName;
			owner.receiver.drawTTFDirectFont(
					engine, engine.getPlayerID(),
					owner.receiver.getFieldDisplayPositionX(engine, engine.getPlayerID()),
					owner.receiver.getFieldDisplayPositionY(engine, engine.getPlayerID()) - 20,
					name);
		}
	}

	/**
	 * NET: Send the current piece's movement to all spectators.
	 * @param engine GameEngine
	 * @param forceSend <code>true</code> to force send a message
	 *        (if <code>false</code>, it won't send a message unless there is a movement)
	 * @return <code>true</code> if the message is sent
	 */
	protected boolean netSendPieceMovement(GameEngine engine, boolean forceSend) {
		if( ((engine.nowPieceObject == null) && (netPrevPieceID != Piece.PIECE_NONE)) || (engine.manualLock) )
		{
			
			PieceMovement pm = new PieceMovement();
			pm.setPieceId(netPrevPieceID);
			pm.setX(netPrevPieceX);
			pm.setY(netPrevPieceY);
			pm.setDirection(netPrevPieceDir);
			pm.setSkin(engine.getSkin());
			
			knetClient().fireUDP(knetClient().event(
					GAME, true,
					GAME_PIECE_MOVEMENT, pm));
			
			return true;
		}
		else if((engine.nowPieceObject.id != netPrevPieceID) || (engine.nowPieceX != netPrevPieceX) ||
				(engine.nowPieceY != netPrevPieceY) || (engine.nowPieceObject.direction != netPrevPieceDir) ||
				(forceSend))
		{
			netPrevPieceID = engine.nowPieceObject.id;
			netPrevPieceX = engine.nowPieceX;
			netPrevPieceY = engine.nowPieceY;
			netPrevPieceDir = engine.nowPieceObject.direction;

			int x = netPrevPieceX + engine.nowPieceObject.dataOffsetX[netPrevPieceDir];
			int y = netPrevPieceY + engine.nowPieceObject.dataOffsetY[netPrevPieceDir];
			
			PieceMovement pm = new PieceMovement();
			pm.setPieceId(netPrevPieceID);
			pm.setX(x);
			pm.setY(y);
			pm.setDirection(netPrevPieceDir);
			pm.setBottomY(engine.nowPieceBottomY);
			pm.setColor(engine.ruleopt.pieceColor[netPrevPieceID]);
			pm.setSkin(engine.getSkin());
			pm.setBig(engine.nowPieceObject.big);
			
			knetClient().fireUDP(knetClient().event(
					GAME, true,
					GAME_PIECE_MOVEMENT, pm));
			
			return true;
		}
		return false;
	}

	/**
	 * NET: Receive the current piece's movement. You can override it if you customize "piece" message.
	 * @param engine GameEngine
	 * @param message Message array
	 */
	protected void netRecvPieceMovement(GameEngine engine, KNetEvent e) {
		if(!e.is(GAME) || !e.is(GAME_PIECE_MOVEMENT))
			return;
		
		PieceMovement pm = (PieceMovement) e.get(GAME_PIECE_MOVEMENT);
		
		int id = pm.getPieceId();

		if(id >= 0) {
			int pieceX = pm.getX();;
			int pieceY = pm.getY();
			int pieceDir = pm.getDirection();
			int pieceBottomY = pm.getBottomY();
			int pieceColor = pm.getColor();
			int pieceSkin = pm.getSkin();
			boolean pieceBig = pm.isBig();

			engine.nowPieceObject = new Piece(id);
			engine.nowPieceObject.direction = pieceDir;
			engine.nowPieceObject.setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
			engine.nowPieceObject.setColor(pieceColor);
			engine.nowPieceObject.setSkin(pieceSkin);
			engine.nowPieceX = pieceX;
			engine.nowPieceY = pieceY;
			//engine.nowPieceBottomY = pieceBottomY;
			engine.nowPieceObject.big = pieceBig;
			engine.nowPieceObject.updateConnectData();
			engine.nowPieceBottomY =
				engine.nowPieceObject.getBottom(pieceX, pieceY, engine.field);

			if((engine.stat != GameEngine.STAT_EXCELLENT) && (engine.stat != GameEngine.STAT_GAMEOVER) &&
			   (engine.stat != GameEngine.STAT_RESULT))
			{
				engine.gameActive = true;
				engine.timerActive = true;
				engine.stat = GameEngine.STAT_MOVE;
				engine.statc[0] = 2;
			}

			netPlayerSkin = pieceSkin;
		} else {
			engine.nowPieceObject = null;
		}
	}

	/**
	 * NET: Send field to all spectators
	 * @param engine GameEngine
	 */
	protected void netSendField(GameEngine engine, boolean udp) {
		Field f = engine.field;
		if(f == null)
			return;
		if(udp)
			knetClient().fireUDP(GAME, GAME_FIELD, f);
		else
			knetClient().fireTCP(GAME, GAME_FIELD, f);
	}

	/**
	 * NET: Receive field message
	 * @param engine GameEngine
	 * @param message Message array
	 */
	protected void netRecvField(GameEngine engine, KNetEvent e) {
		engine.field = (Field) e.get(GAME_FIELD);
//		engine.field.copy(e.get(GAME_FIELD, Field.class));
	}

	/**
	 * NET: Send next and hold piece informations to all spectators
	 * @param engine GameEngine
	 */
	protected void netSendNextAndHold(GameEngine engine) {
		PieceHold hold = new PieceHold();
		hold.setPiece(engine.holdPieceObject);
		hold.setDisableHold(engine.holdDisable);
		
		Piece[] next = new Piece[engine.ruleopt.nextDisplay];
		for(int i = 0; i < next.length; i++) {
			next[i] = engine.getNextObject(engine.nextPieceCount + i);
		}

		knetClient().fireUDP(GAME, true, GAME_HOLD_PIECE, hold, GAME_NEXT_PIECE, next);
	}

	/**
	 * NET: Receive next and hold piece informations
	 * @param engine GameEngine
	 * @param message Message array
	 */
	protected void netRecvNextAndHold(GameEngine engine, KNetEvent e) {
		if(!e.is(GAME) || !e.is(GAME_HOLD_PIECE) || !e.is(GAME_NEXT_PIECE))
			return;
		
		PieceHold hold = (PieceHold) e.get(GAME_HOLD_PIECE);
		Piece[] next = (Piece[]) e.get(GAME_NEXT_PIECE);
		
		int maxNext = next.length;
		engine.ruleopt.nextDisplay = maxNext;
		engine.holdDisable = hold.isDisableHold();

		engine.holdPieceObject = hold.getPiece();
		
		if(engine.nextPieceArrayObject == null || engine.nextPieceArrayObject.length < maxNext)
			engine.nextPieceArrayObject = new Piece[maxNext];
		System.arraycopy(next, 0, engine.nextPieceArrayObject, 0, maxNext);

		engine.isNextVisible = true;
		engine.isHoldVisible = true;
	}


	/**
	 * NET: Send various in-game stats (as well as goaltype)<br>
	 * Game modes should implement this.
	 * @param engine GameEngine
	 */
	protected void netSendStats(GameEngine engine) {
	}

	/**
	 * NET: Receive various in-game stats (as well as goaltype)<br>
	 * Game modes should implement this.
	 * @param engine GameEngine
	 * @param message Message array
	 */
	protected void netRecvStats(GameEngine engine, KNetEvent e) {
	}

	/**
	 * NET: Send end-of-game stats<br>
	 * Game modes should implement this.
	 * @param engine GameEngine
	 */
	protected void netSendEndGameStats(GameEngine engine) {
	}

	/**
	 * NET: Send game options to all spectators<br>
	 * Game modes should implement this.
	 * @param engine GameEngine
	 */
	protected void netSendOptions(GameEngine engine) {
	}

	/**
	 * NET: Receive game options.<br>
	 * Game modes should implement this.
	 * @param engine GameEngine
	 * @param message Message array
	 */
	protected void netRecvOptions(GameEngine engine, KNetEvent e) {
	}

	/**
	 * NET: Send replay data<br>
	 * Game modes should implement this. However, some basic codes are already implemented in NetDummyMode.
	 * @param engine GameEngine
	 */
	protected void netSendReplay(GameEngine engine) {
		if(netIsNetRankingSendOK(engine)) {
			Replay replay = new Replay();
			replay.setReplay(owner.replayProp);
			replay.setStatistics(engine.statistics);
			replay.setGameType(netGetGoalType());
			
			knetClient().fireTCP(REPLAY_DATA, replay);
			
		} else {
			// TODO wtf does this magic constant mean?
			netReplaySendStatus = 2;
		}
	}

	/**
	 * NET: Get goal type (used from the default implementation of netSendReplay)<br>
	 * Game modes should implement this, unless there is only 1 goal type.
	 * @return Goal type (default implementation will return 0)
	 */
	protected int netGetGoalType() {
		return 0;
	}

	/**
	 * NET: It returns <code>true</code> when the current settings doesn't prevent leaderboard screen from showing.
	 * Game modes should implement this. By default, this always returns false.
	 * @param engine GameEngine
	 * @return <code>true</code> when the current settings doesn't prevent leaderboard screen from showing.
	 */
	protected boolean netIsNetRankingViewOK(GameEngine engine) {
		return false;
	}

	/**
	 * NET: It returns <code>true</code> when the current settings doesn't prevent replay data from sending.
	 * By default, it just calls netIsNetRankingViewOK, but you should override it if you make "race" modes.
	 * @param engine GameEngine
	 * @return <code>true</code> when the current settings doesn't prevent replay data from sending.
	 */
	protected boolean netIsNetRankingSendOK(GameEngine engine) {
		return netIsNetRankingViewOK(engine);
	}

	@Override
	public void knetPanelInit(KNetPanelEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void knetPanelConnected(KNetPanelEvent e) {
	}

	@Override
	public void knetPanelDisconnected(KNetPanelEvent e) {
	}

	@Override
	public void knetPanelJoined(KNetPanelEvent e) {
		netOnJoin(e.getClient(), e.getChannel());
	}

	@Override
	public void knetPanelParted(KNetPanelEvent e) {
	}

	@Override
	public void knetPanelShutdown(KNetPanelEvent e) {
		// TODO Auto-generated method stub
		
	}
}
