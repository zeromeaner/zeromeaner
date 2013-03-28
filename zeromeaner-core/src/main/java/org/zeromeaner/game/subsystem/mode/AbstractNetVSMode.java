package org.zeromeaner.game.subsystem.mode;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.zeromeaner.contrib.net.omegaboshi.nullpomino.game.subsystem.randomizer.Randomizer;
import org.zeromeaner.game.component.BGMStatus;
import org.zeromeaner.game.component.Block;
import org.zeromeaner.game.component.Controller;
import org.zeromeaner.game.event.EventRenderer;
import org.zeromeaner.game.knet.KNetClient;
import org.zeromeaner.game.knet.KNetEvent;
import org.zeromeaner.game.knet.KNetEventSource;
import org.zeromeaner.game.knet.obj.KNStartInfo;
import org.zeromeaner.game.knet.obj.KNetChannelInfo;
import org.zeromeaner.game.knet.obj.KNetGameInfo;
import org.zeromeaner.game.knet.obj.KNetPlayerInfo;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.game.play.GameManager;
import org.zeromeaner.game.subsystem.mode.ModeTypes.ModeType;
import org.zeromeaner.game.subsystem.wallkick.Wallkick;
import org.zeromeaner.util.GeneralUtil;

import static org.zeromeaner.game.knet.KNetEventArgs.*;

/**
 * Special base class for netplay VS modes. Up to 6 players supported.
 */
@ModeTypes(ModeType.HIDDEN)
public abstract class AbstractNetVSMode extends AbstractNetMode {
	/* -------------------- Constants -------------------- */
	/** NET-VS: Max number of players */
	protected static final int NETVS_MAX_PLAYERS = 6;

	/** NET-VS: Numbers of seats numbers corresponding to frames on player's screen */
	protected static final int[][] NETVS_GAME_SEAT_NUMBERS =
		{
		{0,1,2,3,4,5},
		{1,0,2,3,4,5},
		{1,2,0,3,4,5},
		{1,2,3,0,4,5},
		{1,2,3,4,0,5},
		{1,2,3,4,5,0},
		};

	/** NET-VS: Each player's garbage block color */
	protected static final int[] NETVS_PLAYER_COLOR_BLOCK = {
		Block.BLOCK_COLOR_RED, Block.BLOCK_COLOR_BLUE, Block.BLOCK_COLOR_GREEN,
		Block.BLOCK_COLOR_YELLOW, Block.BLOCK_COLOR_PURPLE, Block.BLOCK_COLOR_CYAN
	};

	/** NET-VS: Each player's frame color */
	protected static final int[] NETVS_PLAYER_COLOR_FRAME = {
		GameEngine.FRAME_COLOR_RED, GameEngine.FRAME_COLOR_BLUE, GameEngine.FRAME_COLOR_GREEN,
		GameEngine.FRAME_COLOR_YELLOW, GameEngine.FRAME_COLOR_PURPLE, GameEngine.FRAME_COLOR_CYAN
	};

	/** NET-VS: Team font colors */
	protected static final int[] NETVS_TEAM_FONT_COLORS = {
		EventRenderer.COLOR_WHITE,
		EventRenderer.COLOR_RED, EventRenderer.COLOR_GREEN, EventRenderer.COLOR_BLUE, EventRenderer.COLOR_YELLOW,
		EventRenderer.COLOR_PURPLE, EventRenderer.COLOR_CYAN
	};

	/** NET-VS: Default time before forced piece lock */
	protected static final int NETVS_PIECE_AUTO_LOCK_TIME = 30 * 60;

	/* -------------------- Variables -------------------- */

	/** NET-VS: Number of players */
	protected int netvsNumPlayers() {
		return channelInfo().getPlayers().size();
	}

	/** NET-VS: Number of players in current game */
	protected int netvsNumNowPlayers;

	/** NET-VS: Number of players still alive in current game */
	protected int netvsNumAlivePlayers;

	/** NET-VS: Player exist flag */
	protected boolean[] netvsPlayerExist;

	/** NET-VS: Player ready flag */
	protected boolean[] netvsPlayerReady;

	/** NET-VS: Player dead flag */
	protected boolean[] netvsPlayerDead;

	/** NET-VS: Player active flag (false if newcomer) */
	protected boolean[] netvsPlayerActive;

	/** NET-VS: Player's Seat ID array (-1:No Player) */
	protected int[] netvsPlayerSeatID;

	/** NET-VS: Player's UID array (-1:No Player) */
	protected int[] netvsPlayerUID;

	/** NET-VS: Player's place */
	protected int[] netvsPlayerPlace;

	/** NET-VS: Player's win count */
	protected int[] netvsPlayerWinCount;

	/** NET-VS: Player's game count */
	protected int[] netvsPlayerPlayCount;

	/** NET-VS: Player's team colors */
	protected int[] netvsPlayerTeamColor;

	/** NET-VS: Player names */
	protected String[] netvsPlayerName;

	/** NET-VS: Player team names */
	protected String[] netvsPlayerTeam;

	/** NET-VS: Player's skins */
	protected int[] netvsPlayerSkin;

	/** NET-VS: true if it's ready to show player's result */
	protected boolean[] netvsPlayerResultReceived;

	/** NET-VS: true if automatic start timer is activated */
	protected boolean netvsAutoStartTimerActive;

	/** NET-VS: Time left until the game starts automatically */
	protected int netvsAutoStartTimer;

	/** NET-VS: true if room game is in progress */
	protected boolean netvsIsGameActive;

	/** NET-VS: true if room game is finished */
	protected boolean netvsIsGameFinished;

	/** NET-VS: true if waiting for ready status change */
	protected boolean netvsIsReadyChangePending;

	/** NET-VS: true if waiting for dead status change */
	protected boolean netvsIsDeadPending;

	/** NET-VS: true if local player joined game in progress */
	protected boolean netvsIsNewcomer;

	/** NET-VS: Elapsed timer active flag */
	protected boolean netvsPlayTimerActive;

	/** NET-VS: Elapsed time */
	protected int netvsPlayTimer;

	/** NET-VS: true if practice mode */
	protected boolean netvsIsPractice;

	/** NET-VS: true if can exit from practice game */
	protected boolean netvsIsPracticeExitAllowed;

	/** NET-VS: How long current piece is active */
	protected int netvsPieceMoveTimer;

	/** NET-VS: Time before forced piece lock */
	protected int netvsPieceMoveTimerMax;

	/** NET-VS: Map number to use */
	protected int netvsMapNo;

	/** NET-VS: Random for selecting map in Practice mode */
	protected Random netvsRandMap;

	/** NET-VS: Practice mode last used map number */
	protected int netvsMapPreviousPracticeMap;

	/** NET-VS: UID of player who attacked local player last (-1: Suicide or Unknown) */
	protected int netvsLastAttackerUID;

	/*
	 * Mode Name
	 */
	@Override
	public String getName() {
		return "NET-VS-DUMMY";
	}

	@Override
	public boolean isVSMode() {
		return true;
	}

	/**
	 * NET-VS: Number of players
	 */
	@Override
	public int getPlayers() {
		if(channelInfo() == null)
			return 0;
		return channelInfo().getMaxPlayers();
	}

	/**
	 * NET-VS: This is netplay-only mode
	 */
	@Override
	public boolean isNetplayMode() {
		return true;
	}

	/**
	 * NET-VS: Mode Initialization
	 */
	@Override
	public void modeInit(GameManager manager) {
		super.modeInit(manager);
		log.debug("modeInit() on NetDummyVSMode");
		netForceSendMovements = true;
		netvsNumNowPlayers = 0;
		netvsNumAlivePlayers = 0;
		netvsPlayerExist = new boolean[NETVS_MAX_PLAYERS];
		netvsPlayerReady = new boolean[NETVS_MAX_PLAYERS];
		netvsPlayerActive = new boolean[NETVS_MAX_PLAYERS];
		netvsPlayerSeatID = new int[NETVS_MAX_PLAYERS];
		netvsPlayerUID = new int[NETVS_MAX_PLAYERS];
		netvsPlayerWinCount = new int[NETVS_MAX_PLAYERS];
		netvsPlayerPlayCount = new int[NETVS_MAX_PLAYERS];
		netvsPlayerTeamColor = new int[NETVS_MAX_PLAYERS];
		netvsPlayerName = new String[NETVS_MAX_PLAYERS];
		netvsPlayerTeam = new String[NETVS_MAX_PLAYERS];
		netvsPlayerSkin = new int[NETVS_MAX_PLAYERS];
		for(int i = 0; i < NETVS_MAX_PLAYERS; i++) netvsPlayerSkin[i] = -1;
		netvsAutoStartTimerActive = false;
		netvsAutoStartTimer = 0;
		netvsPieceMoveTimerMax = NETVS_PIECE_AUTO_LOCK_TIME;
		netvsMapPreviousPracticeMap = -1;
		netvsResetFlags();
	}

	/**
	 * NET-VS: Init some variables
	 */
	protected void netvsResetFlags() {
		netvsPlayerResultReceived = new boolean[NETVS_MAX_PLAYERS];
		netvsPlayerDead = new boolean[NETVS_MAX_PLAYERS];
		netvsPlayerPlace = new int[NETVS_MAX_PLAYERS];
		netvsIsGameActive = false;
		netvsIsGameFinished = false;
		netvsIsReadyChangePending = false;
		netvsIsDeadPending = false;
		netvsIsNewcomer = false;
		netvsPlayTimerActive = false;
		netvsPlayTimer = 0;
		netvsIsPractice = false;
		netvsIsPracticeExitAllowed = false;
		netvsPieceMoveTimer = 0;
	}

	/**
	 * NET-VS: Initialization for each player
	 */
	@Override
	public void playerInit(GameEngine engine, int playerID) {
		netPlayerInit(engine, playerID);
	}

	/**
	 * @return true if watch mode
	 */
	protected boolean netvsIsWatch() {
		if(channelInfo() == null || knetClient() == null)
			return true;
		return !channelInfo().getPlayers().contains(knetClient().getSource());
	}

	/**
	 * NET-VS: Update player variables
	 */
	@Override
	protected void netUpdatePlayerExist() {
		if(knetClient() == null)
			return;
		netNumSpectators = 0;
		netPlayerName = knetClient().getSource().getName();
		netIsWatch = netvsIsWatch();

		for(int i = 0; i < netvsNumPlayers(); i++) {
			netvsPlayerExist[i] = false;
			netvsPlayerReady[i] = false;
			netvsPlayerActive[i] = false;
			netvsPlayerSeatID[i] = -1;
			netvsPlayerUID[i] = -1;
			netvsPlayerWinCount[i] = 0;
			netvsPlayerPlayCount[i] = 0;
			netvsPlayerName[i] = "";
			netvsPlayerTeam[i] = "";
			owner.engine[i].framecolor = GameEngine.FRAME_COLOR_GRAY;
		}

		//		LinkedList<NetPlayerInfo> pList = netLobby.updateSameRoomPlayerInfoList();
		List<KNetEventSource> players = channelInfo().getPlayers();
		List<KNetPlayerInfo> playerInfo = channelInfo().getPlayerInfo();
		List<String> teamList = new LinkedList<String>();
		
		for(KNetEventSource player: players) {
			netNumSpectators++;
			if(!channelInfo().getPlayers().contains(player)) {
			} else {
				int playerID = netvsGetPlayerIDbySeatID(players.indexOf(player));
				KNetPlayerInfo info = playerInfo.get(playerID);
				netvsPlayerExist[playerID] = true;
				netvsPlayerReady[playerID] = info.isReady();
				netvsPlayerActive[playerID] = info.isPlaying();
				netvsPlayerSeatID[playerID] = players.indexOf(player);
				netvsPlayerUID[playerID] = player.getId();
				netvsPlayerWinCount[playerID] = info.getWinCount();
				netvsPlayerPlayCount[playerID] = info.getPlayCount();
				netvsPlayerName[playerID] = player.getName();
				netvsPlayerTeam[playerID] = info.getTeam();

				// Set frame color
				if(playerID < NETVS_PLAYER_COLOR_FRAME.length) {
					owner.engine[playerID].framecolor = NETVS_PLAYER_COLOR_FRAME[playerID];
				}

				// Set team color
				if(netvsPlayerTeam[playerID].length() > 0) {
					if(!teamList.contains(netvsPlayerTeam[playerID])) {
						teamList.add(netvsPlayerTeam[playerID]);
						netvsPlayerTeamColor[playerID] = teamList.size();
					} else {
						netvsPlayerTeamColor[playerID] = teamList.indexOf(netvsPlayerTeam[playerID]) + 1;
					}
				}
			}
		}
	}

	/**
	 * NET-VS: When you join the room
	 */
	@Override
	protected void netOnJoin(KNetClient client, KNetChannelInfo roomInfo) {
		log.debug("netOnJoin() on NetDummyVSMode");

		netIsNetPlay = true;
		netvsIsNewcomer = channelInfo().isPlaying();

		netUpdatePlayerExist();
		netvsSetLockedRule();
		netvsSetGameScreenLayout();

		if(netvsIsNewcomer) {
			netvsNumNowPlayers = netvsNumPlayers();
		}
	}

	/**
	 * NET-VS: Initialize various NetPlay variables. Usually called from playerInit.
	 */
	@Override
	protected void netPlayerInit(GameEngine engine, int playerID) {
		log.debug("netPlayerInit(engine, " + playerID + ") on NetDummyVSMode");


		super.netPlayerInit(engine, playerID);
		// Misc. variables
		engine.fieldWidth = 10;
		engine.fieldHeight = 20;
		engine.gameoverAll = false;
		engine.allowTextRenderByReceiver = true;
	}

	/**
	 * NET-VS: Draw player's name
	 */
	@Override
	protected void netDrawPlayerName(GameEngine engine) {
		int playerID = engine.getPlayerID();
		int x = owner.receiver.getFieldDisplayPositionX(engine, playerID);
		int y = owner.receiver.getFieldDisplayPositionY(engine, playerID);

		if((netvsPlayerName != null) && (netvsPlayerName[playerID] != null) && (netvsPlayerName[playerID].length() > 0)) {
			String name = netvsPlayerName[playerID];
			int fontcolorNum = netvsPlayerTeamColor[playerID];
			if(fontcolorNum < 0) fontcolorNum = 0;
			if(fontcolorNum > NETVS_TEAM_FONT_COLORS.length - 1) fontcolorNum = NETVS_TEAM_FONT_COLORS.length - 1;
			int fontcolor = NETVS_TEAM_FONT_COLORS[fontcolorNum];

			if(engine.displaysize == -1) {
				if(name.length() > 7) name = name.substring(0, 7) + "..";
				owner.receiver.drawTTFDirectFont(engine, playerID, x, y - 16, name, fontcolor);
			} else if(playerID == 0) {
				if(name.length() > 14) name = name.substring(0, 14) + "..";
				owner.receiver.drawTTFDirectFont(engine, playerID, x, y - 20, name, fontcolor);
			} else {
				owner.receiver.drawTTFDirectFont(engine, playerID, x, y - 20, name, fontcolor);
			}
		}
	}

	/**
	 * NET-VS: Send field to everyone. It won't do anything in practice game.
	 */
	@Override
	protected void netSendField(GameEngine engine) {
		if(!netvsIsPractice && (engine.getPlayerID() == 0) && (!netIsWatch)) {
			super.netSendField(engine);
		}
	}

	/**
	 * NET-VS: Send next and hold piece informations to everyone. It won't do anything in practice game.
	 */
	@Override
	protected void netSendNextAndHold(GameEngine engine) {
		if(!netvsIsPractice && (engine.getPlayerID() == 0) && (!netIsWatch)) {
			super.netSendNextAndHold(engine);
		}
	}

	/**
	 * NET-VS: Send the current piece's movement to everyone. It won't do anything in practice game.
	 */
	@Override
	protected boolean netSendPieceMovement(GameEngine engine, boolean forceSend) {
		if(!netvsIsPractice && (engine.getPlayerID() == 0) && (!netIsWatch)) {
			return super.netSendPieceMovement(engine, forceSend);
		}
		return false;
	}

	/**
	 * NET-VS: Set locked rule/Revert to user rule
	 */
	protected void netvsSetLockedRule() {
		if((channelInfo() != null) && (channelInfo().isRuleLock())) {
			// Set to locked rule
			for(int i = 0; i < getPlayers(); i++) {
				Randomizer randomizer = GeneralUtil.loadRandomizer(channelInfo().getRule().strRandomizer, owner.engine[i]);
				Wallkick wallkick = GeneralUtil.loadWallkick(channelInfo().getRule().strWallkick);
				owner.engine[i].ruleopt.copy(channelInfo().getRule());
				owner.engine[i].randomizer = randomizer;
				owner.engine[i].wallkick = wallkick;
			}
		} else if(!netvsIsWatch()) {
			// Revert rules
			owner.engine[0].ruleopt.copy(channelInfo().getRule());
			owner.engine[0].randomizer = GeneralUtil.loadRandomizer(owner.engine[0].ruleopt.strRandomizer, owner.engine[0]);
			owner.engine[0].wallkick = GeneralUtil.loadWallkick(owner.engine[0].ruleopt.strWallkick);
		}
	}

	/**
	 * Set game screen layout
	 */
	protected void netvsSetGameScreenLayout() {
		for(int i = 0; i < getPlayers(); i++) {
			netvsSetGameScreenLayout(owner.engine[i]);
		}
	}

	/**
	 * Set game screen layout
	 * @param engine GameEngine
	 */
	protected void netvsSetGameScreenLayout(GameEngine engine) {
		// Set display size
		if( ((engine.getPlayerID() == 0) && !netvsIsWatch()) ||
				((channelInfo() != null) && (channelInfo().getMaxPlayers() == 2) && (engine.getPlayerID() <= 1)) )
		{
			engine.displaysize = 0;
			engine.enableSE = true;
		} else {
			engine.displaysize = -1;
			engine.enableSE = false;
		}

		// Set visible flag
		if((channelInfo() != null) && (engine.getPlayerID() >= channelInfo().getMaxPlayers())) {
			engine.isVisible = false;
		}

		// Set frame color
		int seatID = netvsPlayerSeatID[engine.getPlayerID()];
		if((seatID >= 0) && (seatID < NETVS_PLAYER_COLOR_FRAME.length)) {
			engine.framecolor = NETVS_PLAYER_COLOR_FRAME[seatID];
		} else {
			engine.framecolor = GameEngine.FRAME_COLOR_GRAY;
		}
	}

	/**
	 * NET-VS: Apply room's settings (such as gravity) to all GameEngine
	 */
	protected void netvsApplyRoomSettings() {
		for(int i = 0; i < getPlayers(); i++) {
			netvsApplyRoomSettings(owner.engine[i]);
		}
	}

	/**
	 * NET-VS: Apply room's settings (such as gravity) to the specific GameEngine
	 * @param engine GameEngine to apply settings
	 */
	protected void netvsApplyRoomSettings(GameEngine engine) {
		if(channelInfo() != null) {
			KNetGameInfo game = channelInfo().getGame();

			engine.speed.gravity = game.getGravity();
			engine.speed.denominator = game.getDenominator();
			engine.speed.are = game.getAre();
			engine.speed.areLine = game.getAreLine();
			engine.speed.lineDelay = game.getLineDelay();
			engine.speed.lockDelay = game.getLockDelay();
			engine.speed.das = game.getDas();
			
			engine.b2bEnable = game.isB2bEnable();
			engine.comboType = game.getComboType();
			
			switch(game.getTspinEnableType()) {
			case DISABLE:
				engine.tspinEnable = false;
				engine.useAllSpinBonus = false;
				break;
			case ENABLE:
				engine.tspinEnable = true;
				engine.useAllSpinBonus = false;
				break;
			case ENABLE_WITH_BONUSES:
				engine.tspinEnable = true;
				engine.useAllSpinBonus = true;
			}
			
			synchronousPlay = game.isSynchronousPlay();
		}
	}

	/**
	 * NET-VS: Get player field number by seat ID
	 * @param seat The seat ID want to know
	 * @return Player number
	 */
	protected int netvsGetPlayerIDbySeatID(int seat) {
		return netvsGetPlayerIDbySeatID(seat, netvsMySeatID());
	}

	/**
	 * NET-VS: Get player field number by seat ID
	 * @param seat The seat ID want to know
	 * @param myseat Your seat number (-1 if spectator)
	 * @return Player number
	 */
	protected int netvsGetPlayerIDbySeatID(int seat, int myseat) {
		int myseat2 = myseat;
		if(myseat2 < 0) myseat2 = 0;
		return NETVS_GAME_SEAT_NUMBERS[myseat2][seat];
	}

	/**
	 * NET-VS: Start a practice game
	 * @param engine GameEngine
	 */
	protected void netvsStartPractice(GameEngine engine) {
		netvsIsPractice = true;
		netvsIsPracticeExitAllowed = false;

		engine.init();
		engine.stat = GameEngine.STAT_READY;
		engine.resetStatc();
		netUpdatePlayerExist();
		netvsSetGameScreenLayout();

		// Map
		if(channelInfo().getGame().getMap() != null && (knetClient().getMaps().size() > 0)) {
			if(netvsRandMap == null) netvsRandMap = new Random();

			int map = 0;
			int maxMap = knetClient().getMaps().size();
			do {
				map = netvsRandMap.nextInt(maxMap);
			} while ((map == netvsMapPreviousPracticeMap) && (maxMap >= 2));
			netvsMapPreviousPracticeMap = map;

			engine.createFieldIfNeeded();
			engine.field.copy(knetClient().getMaps().get(map));
			engine.field.setAllSkin(engine.getSkin());
			engine.field.setAllAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
			engine.field.setAllAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
			engine.field.setAllAttribute(Block.BLOCK_ATTRIBUTE_SELFPLACED, false);
		}
	}

	/**
	 * NET-VS: Receive end-of-game stats.<br>
	 * Game modes should implement this. However, there are some sample codes in NetDummyVSMode.
	 * @param message Message
	 */
	protected void netvsRecvEndGameStats(KNetEvent e) {
		int seatID = channelInfo().getPlayers().indexOf(e.getSource());
		int playerID = netvsGetPlayerIDbySeatID(seatID);

		if((playerID != 0) || (netvsIsWatch())) {
			netvsPlayerResultReceived[playerID] = true;
		}
	}

	/**
	 * Get number of teams alive (Each independence player will also count as a team)
	 * @return Number of teams alive
	 */
	protected int netvsGetNumberOfTeamsAlive() {
		LinkedList<String> listTeamName = new LinkedList<String>();
		int noTeamCount = 0;

		for(int i = 0; i < getPlayers(); i++) {
			if(netvsPlayerExist[i] && !netvsPlayerDead[i] && owner.engine[i].gameActive) {
				if(netvsPlayerTeam[i].length() > 0) {
					if(!listTeamName.contains(netvsPlayerTeam[i])) {
						listTeamName.add(netvsPlayerTeam[i]);
					}
				} else {
					noTeamCount++;
				}
			}
		}

		return noTeamCount + listTeamName.size();
	}

	/**
	 * Check if the given playerID can be attacked
	 * @param playerID Player ID (to attack)
	 * @return true if playerID can be attacked
	 */
	protected boolean netvsIsAttackable(int playerID) {
		// Can't attack self
		if(playerID <= 0) return false;

		// Doesn't exist?
		if(!netvsPlayerExist[playerID]) return false;
		// Dead?
		if(netvsPlayerDead[playerID]) return false;
		// Newcomer?
		if(!netvsPlayerActive[playerID]) return false;

		// Is teammate?
		String myTeam = netvsPlayerTeam[0];
		String thisTeam = netvsPlayerTeam[playerID];
		if((myTeam.length() > 0) && (thisTeam.length() > 0) && myTeam.equals(thisTeam)) {
			return false;
		}

		return true;
	}

	/**
	 * Draw room info box (number of players, number of spectators, etc) to somewhere on the screen
	 * @param engine GameEngine
	 * @param x X position
	 * @param y Y position
	 */
	protected void netvsDrawRoomInfoBox(GameEngine engine, int x, int y) {
		if(channelInfo() != null) {
			owner.receiver.drawDirectFont(engine, 0, x, y +  0, "PLAYERS", EventRenderer.COLOR_CYAN, 0.5f);
			owner.receiver.drawDirectFont(engine, 0, x, y +  8, "" + netvsNumPlayers(), EventRenderer.COLOR_WHITE, 0.5f);
			owner.receiver.drawDirectFont(engine, 0, x, y + 16, "SPECTATORS", EventRenderer.COLOR_CYAN, 0.5f);
			owner.receiver.drawDirectFont(engine, 0, x, y + 24, "" + netNumSpectators, EventRenderer.COLOR_WHITE, 0.5f);

			if(!netvsIsWatch()) {
				owner.receiver.drawDirectFont(engine, 0, x, y + 32, "MATCHES", EventRenderer.COLOR_CYAN, 0.5f);
				owner.receiver.drawDirectFont(engine, 0, x, y + 40, "" + netvsPlayerPlayCount[0], EventRenderer.COLOR_WHITE, 0.5f);
				owner.receiver.drawDirectFont(engine, 0, x, y + 48, "WINS", EventRenderer.COLOR_CYAN, 0.5f);
				owner.receiver.drawDirectFont(engine, 0, x, y + 56, "" + netvsPlayerWinCount[0], EventRenderer.COLOR_WHITE, 0.5f);
			}
		}
		owner.receiver.drawDirectFont(engine, 0, x, y + 72, "ALL ROOMS", EventRenderer.COLOR_GREEN, 0.5f);
		if(knetClient() != null)
			owner.receiver.drawDirectFont(engine, 0, x, y + 80, "" + knetClient().getChannels().size(), EventRenderer.COLOR_WHITE, 0.5f);
	}

	/**
	 * NET-VS: Settings screen
	 */
	@Override
	public boolean onSetting(GameEngine engine, int playerID) {
		if((channelInfo() != null) && (playerID == 0) && (!netvsIsWatch())) {
			netvsPlayerExist[0] = true;

			engine.displaysize = 0;
			engine.enableSE = true;
			engine.isVisible = true;

			if((!netvsIsReadyChangePending) && (netvsNumPlayers() >= 2) && (!netvsIsNewcomer) && (engine.statc[3] >= 5)) {
				// Ready ON
				if(engine.ctrl.isPush(Controller.BUTTON_A) && !netvsPlayerReady[0]) {
					engine.playSE("decide");
					netvsIsReadyChangePending = true;
					knetClient().fireTCP(READY, true);
				}
				// Ready OFF
				if(engine.ctrl.isPush(Controller.BUTTON_B) && netvsPlayerReady[0]) {
					engine.playSE("decide");
					netvsIsReadyChangePending = true;
					knetClient().fireTCP(READY, false);
				}
			}

			// Practice Mode
			if(engine.ctrl.isPush(Controller.BUTTON_F) && (engine.statc[3] >= 5)) {
				engine.playSE("decide");
				netvsStartPractice(engine);
				return true;
			}
		}

		// Random Map Preview
		if((channelInfo() != null) && channelInfo().getGame().getMap() != null && !knetClient().getMaps().isEmpty()) {
			if(netvsPlayerExist[playerID]) {
				if(engine.statc[3] % 30 == 0) {
					engine.statc[5]++;
					if(engine.statc[5] >= knetClient().getMaps().size()) engine.statc[5] = 0;
					engine.createFieldIfNeeded();
					engine.field.copy(knetClient().getMaps().get(engine.statc[5]));
					engine.field.setAllSkin(engine.getSkin());
					engine.field.setAllAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
					engine.field.setAllAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
					engine.field.setAllAttribute(Block.BLOCK_ATTRIBUTE_SELFPLACED, false);
				}
			} else if((engine.field != null) && !engine.field.isEmpty()) {
				engine.field.reset();
			}
		}

		engine.statc[3]++;

		return true;
	}

	/**
	 * NET-VS: Render the settings screen
	 */
	@Override
	public void renderSetting(GameEngine engine, int playerID) {
		if(engine.isVisible == false) return;

		int x = owner.receiver.getFieldDisplayPositionX(engine, playerID);
		int y = owner.receiver.getFieldDisplayPositionY(engine, playerID);

		if(channelInfo() != null) {
			if(netvsPlayerReady[playerID] && netvsPlayerExist[playerID]) {
				if(engine.displaysize != -1)
					owner.receiver.drawDirectFont(engine, playerID, x + 68, y + 204, "OK", EventRenderer.COLOR_YELLOW);
				else
					owner.receiver.drawDirectFont(engine, playerID, x + 36, y + 80, "OK", EventRenderer.COLOR_YELLOW, 0.5f);
			}

			if((playerID == 0) && !netvsIsWatch() && (!netvsIsReadyChangePending) && (netvsNumPlayers() >= 2) && !netvsIsNewcomer) {
				if(!netvsPlayerReady[playerID]) {
					String strTemp = "A(" + owner.receiver.getKeyNameByButtonID(engine, Controller.BUTTON_A) + " KEY):";
					if(strTemp.length() > 10) strTemp = strTemp.substring(0, 10);
					owner.receiver.drawMenuFont(engine, playerID, 0, 16, strTemp, EventRenderer.COLOR_CYAN);
					owner.receiver.drawMenuFont(engine, playerID, 1, 17, "READY", EventRenderer.COLOR_CYAN);
				} else {
					String strTemp = "B(" + owner.receiver.getKeyNameByButtonID(engine, Controller.BUTTON_B) + " KEY):";
					if(strTemp.length() > 10) strTemp = strTemp.substring(0, 10);
					owner.receiver.drawMenuFont(engine, playerID, 0, 16, strTemp, EventRenderer.COLOR_BLUE);
					owner.receiver.drawMenuFont(engine, playerID, 1, 17, "CANCEL", EventRenderer.COLOR_BLUE);
				}
			}
		}

		if((playerID == 0) && !netvsIsWatch() && (engine.statc[3] >= 5)) {
			String strTemp = "F(" + owner.receiver.getKeyNameByButtonID(engine, Controller.BUTTON_F) + " KEY):";
			if(strTemp.length() > 10) strTemp = strTemp.substring(0, 10);
			strTemp = strTemp.toUpperCase();
			owner.receiver.drawMenuFont(engine, playerID, 0, 18, strTemp, EventRenderer.COLOR_PURPLE);
			owner.receiver.drawMenuFont(engine, playerID, 1, 19, "PRACTICE", EventRenderer.COLOR_PURPLE);
		}
	}

	/**
	 * NET-VS: Ready
	 */
	@Override
	public boolean onReady(GameEngine engine, int playerID) {
		if(engine.statc[0] == 0) {
			// Map
			if(channelInfo().getGame().getMap() != null && (netvsMapNo < knetClient().getMaps().size()) && !netvsIsPractice) {
				engine.createFieldIfNeeded();
				engine.field.copy(knetClient().getMaps().get(netvsMapNo));
				if((playerID == 0) && (!netvsIsWatch())) {
					engine.field.setAllSkin(engine.getSkin());
				} else if(channelInfo().isRuleLock() && (channelInfo().getRule() != null)) {
					engine.field.setAllSkin(channelInfo().getRule().skin);
				} else if(netvsPlayerSkin[playerID] >= 0) {
					engine.field.setAllSkin(netvsPlayerSkin[playerID]);
				}
				engine.field.setAllAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
				engine.field.setAllAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
				engine.field.setAllAttribute(Block.BLOCK_ATTRIBUTE_SELFPLACED, false);
			}
		}

		if(netvsIsPractice && (engine.statc[0] >= 10)) {
			netvsIsPracticeExitAllowed = true;
		}

		return false;
	}

	/**
	 * NET-VS: Executed after Ready->Go, before the first piece appears.
	 */
	@Override
	public void startGame(GameEngine engine, int playerID) {
		netvsApplyRoomSettings(engine);

		if(playerID == 0) {
			// Set BGM
			if(netvsIsPractice) {
				owner.bgmStatus.bgm = BGMStatus.BGM_NOTHING;
			} else {
				owner.bgmStatus.bgm = BGMStatus.BGM_NORMAL1;
				owner.bgmStatus.fadesw = false;
			}

			// Init Variables
			netvsPieceMoveTimer = 0;
		}
	}

	/**
	 * NET-VS: When the pieces can move
	 */
	@Override
	public boolean onMove(GameEngine engine, int playerID) {
		// Stop game for remote players
		if((playerID != 0) || netvsIsWatch()) {
			return true;
		}

		// Timer start
		if((engine.ending == 0) && (engine.statc[0] == 0) && (engine.holdDisable == false) && (!netvsIsPractice))
			netvsPlayTimerActive = true;

		// Send movements
		super.onMove(engine, playerID);

		// Auto lock
		if((engine.ending == 0) && (engine.nowPieceObject != null) && (netvsPieceMoveTimerMax > 0)) {
			netvsPieceMoveTimer++;
			if(netvsPieceMoveTimer >= netvsPieceMoveTimerMax) {
				engine.nowPieceY = engine.nowPieceBottomY;
				engine.lockDelayNow = engine.getLockDelay();
				netvsPieceMoveTimer = 0;
			}
		}

		return false;
	}

	/**
	 * NET-VS: When the piece locked
	 */
	@Override
	public void pieceLocked(GameEngine engine, int playerID, int lines) {
		super.pieceLocked(engine, playerID, lines);
		netvsPieceMoveTimer = 0;
	}

	/**
	 * NET-VS: Executed at the end of each frame
	 */
	@Override
	public void onLast(GameEngine engine, int playerID) {
		super.onLast(engine, playerID);

		// Play Timer
		if((playerID == 0) && (netvsPlayTimerActive)) netvsPlayTimer++;

		// Automatic start timer
		if((playerID == 0) && (channelInfo() != null) && (netvsAutoStartTimerActive) && (!netvsIsGameActive)) {
			if(netvsNumPlayers() <= 1) {
				netvsAutoStartTimerActive = false;
			} else if(netvsAutoStartTimer > 0) {
				netvsAutoStartTimer--;
			} else {
				if(!netvsIsWatch()) {
//					netLobby.netPlayerClient.send("autostart\n");
					knetClient().fireTCP(AUTOSTART, true);
				}
				netvsAutoStartTimer = 0;
				netvsAutoStartTimerActive = false;
			}
		}

		// End practice mode
		if((playerID == 0) && (netvsIsPractice) && (netvsIsPracticeExitAllowed) && (engine.ctrl.isPush(Controller.BUTTON_F))) {
			netvsIsPractice = false;
			netvsIsPracticeExitAllowed = false;
			owner.bgmStatus.bgm = BGMStatus.BGM_NOTHING;
			engine.field.reset();
			engine.gameEnded();
			engine.stat = GameEngine.STAT_SETTING;
			engine.resetStatc();
		}
	}

	/**
	 * NET-VS: Render something such as HUD
	 */
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		// Room info box
		if(playerID == getPlayers() - 1) {
			int x2 = (owner.receiver.getNextDisplayType() == 2) ? 544 : 503;
			if((owner.receiver.getNextDisplayType() == 2) && (channelInfo().getMaxPlayers() == 2))
				x2 = 321;
			if((owner.receiver.getNextDisplayType() != 2) && channelInfo() != null && (channelInfo().getMaxPlayers() == 2))
				x2 = 351;

			netvsDrawRoomInfoBox(engine, x2, 286);
		}

		// Elapsed time
		if(playerID == 0) {
			owner.receiver.drawDirectFont(engine, 0, 256, 16, GeneralUtil.getTime(netvsPlayTimer));

			if(netvsIsPractice) {
				owner.receiver.drawDirectFont(engine, 0, 256, 32, GeneralUtil.getTime(engine.statistics.time), EventRenderer.COLOR_PURPLE);
			}
		}

		// Automatic start timer
		if((playerID == 0) && (channelInfo() != null) && (netvsAutoStartTimerActive) && (!netvsIsGameActive)) {
			owner.receiver.drawDirectFont(engine, 0, 496, 16, GeneralUtil.getTime(netvsAutoStartTimer), channelInfo().isAutoStart(),
					EventRenderer.COLOR_RED, EventRenderer.COLOR_YELLOW);
		}

		// Name
		netDrawPlayerName(engine);
	}

	/**
	 * NET-VS: Game Over
	 */
	@Override
	public boolean onGameOver(GameEngine engine, int playerID) {
		if(engine.statc[0] == 0) engine.gameEnded();
		engine.allowTextRenderByReceiver = false;
		owner.bgmStatus.bgm = BGMStatus.BGM_NOTHING;
		engine.resetFieldVisible();

		// Practice
		if((playerID == 0) && (netvsIsPractice)) {
			if(engine.statc[0] < engine.field.getHeight() + 1) {
				return false;
			} else {
				engine.field.reset();
				engine.stat = GameEngine.STAT_RESULT;
				engine.resetStatc();
				return true;
			}
		}

		// 1P died
		if((playerID == 0) && (!netvsPlayerDead[playerID]) && (!netvsIsDeadPending) && !netvsIsWatch()) {
			netSendField(engine);
			netSendNextAndHold(engine);
			netSendStats(engine);

			knetClient().fireTCP(DEAD, channelInfo().getPlayers().indexOf(knetClient().getSource()), DEAD_KO, netvsLastAttackerUID, DEAD_PLACE, 0);

			netvsPlayerResultReceived[playerID] = true;
			netvsIsDeadPending = true;
			return true;
		}

		// Player/Opponent died
		if(netvsPlayerDead[playerID]) {
			if(engine.field == null) {
				engine.stat = GameEngine.STAT_SETTING;
				engine.resetStatc();
				return true;
			}
			if((engine.statc[0] < engine.field.getHeight() + 1) || (netvsPlayerResultReceived[playerID])) {
				return false;
			}
		}

		return true;
	}

	/**
	 * NET-VS: Draw Game Over screen
	 */
	@Override
	public void renderGameOver(GameEngine engine, int playerID) {
		if((playerID == 0) && (netvsIsPractice)) return;
		if(!engine.isVisible) return;

		int x = owner.receiver.getFieldDisplayPositionX(engine, playerID);
		int y = owner.receiver.getFieldDisplayPositionY(engine, playerID);
		int place = netvsPlayerPlace[playerID];

		if(engine.displaysize != -1) {
			if(netvsPlayerReady[playerID] && !netvsIsGameActive) {
				owner.receiver.drawDirectFont(engine, playerID, x + 68, y + 204, "OK", EventRenderer.COLOR_YELLOW);
			} else if((netvsNumNowPlayers == 2) || (channelInfo().getMaxPlayers() == 2)) {
				owner.receiver.drawDirectFont(engine, playerID, x + 52, y + 204, "LOSE", EventRenderer.COLOR_WHITE);
			} else if(place == 1) {
				//owner.receiver.drawDirectFont(engine, playerID, x + 12, y + 204, "GAME OVER", EventReceiver.COLOR_WHITE);
			} else if(place == 2) {
				owner.receiver.drawDirectFont(engine, playerID, x + 12, y + 204, "2ND PLACE", EventRenderer.COLOR_WHITE);
			} else if(place == 3) {
				owner.receiver.drawDirectFont(engine, playerID, x + 12, y + 204, "3RD PLACE", EventRenderer.COLOR_RED);
			} else if(place == 4) {
				owner.receiver.drawDirectFont(engine, playerID, x + 12, y + 204, "4TH PLACE", EventRenderer.COLOR_GREEN);
			} else if(place == 5) {
				owner.receiver.drawDirectFont(engine, playerID, x + 12, y + 204, "5TH PLACE", EventRenderer.COLOR_BLUE);
			} else if(place == 6) {
				owner.receiver.drawDirectFont(engine, playerID, x + 12, y + 204, "6TH PLACE", EventRenderer.COLOR_PURPLE);
			}
		} else {
			if(netvsPlayerReady[playerID] && !netvsIsGameActive) {
				owner.receiver.drawDirectFont(engine, playerID, x + 36, y + 80, "OK", EventRenderer.COLOR_YELLOW, 0.5f);
			} else if((netvsNumNowPlayers == 2) || (channelInfo().getMaxPlayers() == 2)) {
				owner.receiver.drawDirectFont(engine, playerID, x + 28, y + 80, "LOSE", EventRenderer.COLOR_WHITE, 0.5f);
			} else if(place == 1) {
				//owner.receiver.drawDirectFont(engine, playerID, x + 8, y + 80, "GAME OVER", EventReceiver.COLOR_WHITE, 0.5f);
			} else if(place == 2) {
				owner.receiver.drawDirectFont(engine, playerID, x + 8, y + 80, "2ND PLACE", EventRenderer.COLOR_WHITE, 0.5f);
			} else if(place == 3) {
				owner.receiver.drawDirectFont(engine, playerID, x + 8, y + 80, "3RD PLACE", EventRenderer.COLOR_RED, 0.5f);
			} else if(place == 4) {
				owner.receiver.drawDirectFont(engine, playerID, x + 8, y + 80, "4TH PLACE", EventRenderer.COLOR_GREEN, 0.5f);
			} else if(place == 5) {
				owner.receiver.drawDirectFont(engine, playerID, x + 8, y + 80, "5TH PLACE", EventRenderer.COLOR_BLUE, 0.5f);
			} else if(place == 6) {
				owner.receiver.drawDirectFont(engine, playerID, x + 8, y + 80, "6TH PLACE", EventRenderer.COLOR_PURPLE, 0.5f);
			}
		}
	}

	/**
	 * NET-VS: Excellent screen
	 */
	@Override
	public boolean onExcellent(GameEngine engine, int playerID) {
		engine.allowTextRenderByReceiver = false;
		if(playerID == 0) netvsPlayerResultReceived[playerID] = true;

		if(engine.statc[0] == 0) {
			engine.gameEnded();
			owner.bgmStatus.bgm = BGMStatus.BGM_NOTHING;
			engine.resetFieldVisible();
			engine.playSE("excellent");
		}

		if((engine.statc[0] >= 120) && (engine.ctrl.isPush(Controller.BUTTON_A))) {
			engine.statc[0] = engine.field.getHeight() + 1 + 180;
		}

		if(engine.statc[0] >= engine.field.getHeight() + 1 + 180) {
			if((!netvsIsGameActive) && (netvsPlayerResultReceived[playerID])) {
				if(engine.field != null) engine.field.reset();
				engine.resetStatc();
				engine.stat = GameEngine.STAT_RESULT;
			}
		} else {
			engine.statc[0]++;
		}

		return true;
	}

	/**
	 * NET-VS: Draw Excellent screen
	 */
	@Override
	public void renderExcellent(GameEngine engine, int playerID) {
		if(!engine.isVisible) return;

		int x = owner.receiver.getFieldDisplayPositionX(engine, playerID);
		int y = owner.receiver.getFieldDisplayPositionY(engine, playerID);

		if(engine.displaysize != -1) {
			if((playerID == 0) && netvsIsPractice && !netvsIsWatch()) {
				owner.receiver.drawDirectFont(engine, playerID, x + 4, y + 204, "EXCELLENT!", EventRenderer.COLOR_YELLOW);
			} else if(netvsPlayerReady[playerID] && !netvsIsGameActive) {
				owner.receiver.drawDirectFont(engine, playerID, x + 68, y + 204, "OK", EventRenderer.COLOR_YELLOW);
			} else if((netvsNumNowPlayers == 2) || (channelInfo().getMaxPlayers() == 2)) {
				owner.receiver.drawDirectFont(engine, playerID, x + 52, y + 204, "WIN!", EventRenderer.COLOR_YELLOW);
			} else {
				owner.receiver.drawDirectFont(engine, playerID, x + 4, y + 204, "1ST PLACE!", EventRenderer.COLOR_YELLOW);
			}
		} else {
			if((playerID == 0) && netvsIsPractice && !netvsIsWatch()) {
				owner.receiver.drawDirectFont(engine, playerID, x + 4, y + 80, "EXCELLENT!", EventRenderer.COLOR_YELLOW, 0.5f);
			} else if(netvsPlayerReady[playerID] && !netvsIsGameActive) {
				owner.receiver.drawDirectFont(engine, playerID, x + 36, y + 80, "OK", EventRenderer.COLOR_YELLOW, 0.5f);
			} else if((netvsNumNowPlayers == 2) || (channelInfo().getMaxPlayers() == 2)) {
				owner.receiver.drawDirectFont(engine, playerID, x + 28, y + 80, "WIN!", EventRenderer.COLOR_YELLOW, 0.5f);
			} else {
				owner.receiver.drawDirectFont(engine, playerID, x + 4, y + 80, "1ST PLACE!", EventRenderer.COLOR_YELLOW, 0.5f);
			}
		}
	}

	/**
	 * NET-VS: Results screen
	 */
	@Override
	public boolean onResult(GameEngine engine, int playerID) {
		engine.allowTextRenderByReceiver = false;

		if((playerID == 0) && (!netvsIsWatch())) {
			// To the settings screen
			if(engine.ctrl.isPush(Controller.BUTTON_A)) {
				engine.playSE("decide");
				netvsIsPractice = false;
				engine.stat = GameEngine.STAT_SETTING;
				engine.resetStatc();
				return true;
			}
			// Start Practice
			if(engine.ctrl.isPush(Controller.BUTTON_F)) {
				engine.playSE("decide");
				netvsStartPractice(engine);
				return true;
			}
		}

		return true;
	}

	/**
	 * NET-VS: Draw results screen
	 */
	@Override
	public void renderResult(GameEngine engine, int playerID) {
		float scale = 1.0f;
		if(engine.displaysize == -1) scale = 0.5f;

		// Place
		if(!netvsIsPractice || (playerID != 0)) {
			owner.receiver.drawMenuFont(engine, playerID, 0, 0, "RESULT", EventRenderer.COLOR_ORANGE, scale);
			if(netvsPlayerPlace[playerID] == 1) {
				if(netvsNumNowPlayers == 2) {
					owner.receiver.drawMenuFont(engine, playerID, 6, 1, "WIN!", EventRenderer.COLOR_YELLOW, scale);
				} else {
					owner.receiver.drawMenuFont(engine, playerID, 6, 1, "1ST!", EventRenderer.COLOR_YELLOW, scale);
				}
			} else if(netvsPlayerPlace[playerID] == 2) {
				if(netvsNumNowPlayers == 2) {
					owner.receiver.drawMenuFont(engine, playerID, 6, 1, "LOSE", EventRenderer.COLOR_WHITE, scale);
				} else {
					owner.receiver.drawMenuFont(engine, playerID, 7, 1, "2ND", EventRenderer.COLOR_WHITE, scale);
				}
			} else if(netvsPlayerPlace[playerID] == 3) {
				owner.receiver.drawMenuFont(engine, playerID, 7, 1, "3RD", EventRenderer.COLOR_RED, scale);
			} else if(netvsPlayerPlace[playerID] == 4) {
				owner.receiver.drawMenuFont(engine, playerID, 7, 1, "4TH", EventRenderer.COLOR_GREEN, scale);
			} else if(netvsPlayerPlace[playerID] == 5) {
				owner.receiver.drawMenuFont(engine, playerID, 7, 1, "5TH", EventRenderer.COLOR_BLUE, scale);
			} else if(netvsPlayerPlace[playerID] == 6) {
				owner.receiver.drawMenuFont(engine, playerID, 7, 1, "6TH", EventRenderer.COLOR_DARKBLUE, scale);
			}
		} else {
			owner.receiver.drawMenuFont(engine, playerID, 0, 0, "PRACTICE", EventRenderer.COLOR_PINK, scale);
		}

		if((playerID == 0) && (!netvsIsWatch())) {
			// Restart/Practice
			String strTemp = "A(" + owner.receiver.getKeyNameByButtonID(engine, Controller.BUTTON_A) + " KEY):";
			if(strTemp.length() > 10) strTemp = strTemp.substring(0, 10);
			owner.receiver.drawMenuFont(engine, playerID, 0, 18, strTemp, EventRenderer.COLOR_RED);
			owner.receiver.drawMenuFont(engine, playerID, 1, 19, "RESTART", EventRenderer.COLOR_RED);

			String strTempF = "F(" + owner.receiver.getKeyNameByButtonID(engine, Controller.BUTTON_F) + " KEY):";
			if(strTempF.length() > 10) strTempF = strTempF.substring(0, 10);
			owner.receiver.drawMenuFont(engine, playerID, 0, 20, strTempF, EventRenderer.COLOR_PURPLE);
			if(!netvsIsPractice) {
				owner.receiver.drawMenuFont(engine, playerID, 1, 21, "PRACTICE", EventRenderer.COLOR_PURPLE);
			} else {
				owner.receiver.drawMenuFont(engine, playerID, 1, 21, "RETRY", EventRenderer.COLOR_PURPLE);
			}
		} else if(netvsPlayerReady[playerID] && netvsPlayerExist[playerID]) {
			// Player Ready
			int x = owner.receiver.getFieldDisplayPositionX(engine, playerID);
			int y = owner.receiver.getFieldDisplayPositionY(engine, playerID);

			if(engine.displaysize != -1)
				owner.receiver.drawDirectFont(engine, playerID, x + 68, y + 356, "OK", EventRenderer.COLOR_YELLOW);
			else
				owner.receiver.drawDirectFont(engine, playerID, x + 36, y + 156, "OK", EventRenderer.COLOR_YELLOW, 0.5f);
		}
	}

	/**
	 * NET-VS: No retry key.
	 */
	@Override
	public void netplayOnRetryKey(GameEngine engine, int playerID) {
	}

	/**
	 * NET-VS: Disconnected
	 */
	@Override
	public void netlobbyOnDisconnect(KNetClient lobby, KNetEvent e) {
		for(int i = 0; i < getPlayers(); i++) {
			owner.engine[i].stat = GameEngine.STAT_NOTHING;
		}
	}

	/**
	 * NET-VS: Message received
	 */
	
	@Override
	public void knetEvented(KNetClient client, KNetEvent e) {
		if(knetClient().getSource().equals(e.getSource()))
			return;
		
		if(isSynchronousPlay() && channelInfo() != null) {
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
		// Player status update
//		if(message[0].equals("playerupdate")) {
		if(e.is(PLAYER_UPDATE)) {
//			NetPlayerInfo pInfo = new NetPlayerInfo(message[1]);
			KNetPlayerInfo info = (KNetPlayerInfo) e.get(PLAYER_UPDATE);

			// Ready status change
			if(info.getChannel().getId() == channelInfo().getId() && info.getSeatId() != -1) {
				int playerID = netvsGetPlayerIDbySeatID(info.getSeatId());

				if(netvsPlayerReady[playerID] != info.isReady()) {
					netvsPlayerReady[playerID] = info.isReady();

					if((playerID == 0) && (!netvsIsWatch())) {
						netvsIsReadyChangePending = false;
					} else {
						if(info.isReady()) owner.receiver.playSE("decide");
						else if(!info.isPlaying()) owner.receiver.playSE("change");
					}
				}
			}

			netUpdatePlayerExist();
		}
		// When someone logout
		if(e.is(PLAYER_LOGOUT)) {
//			NetPlayerInfo pInfo = new NetPlayerInfo(message[1]);
			KNetEventSource info = (KNetEventSource) e.get(PLAYER_LOGOUT);
			KNetPlayerInfo player = channelInfo().getPlayerInfo(info);

			if((player.getChannel().getId() == channelInfo().getId()) && (player.getSeatId() != -1)) {
				netUpdatePlayerExist();
			}
		}
		// Player status change (Join/Watch)
//		if(message[0].equals("changestatus")) {
		if(e.is(CHANGE_STATUS)) {
//			int uid = Integer.parseInt(message[2]);
			KNetPlayerInfo player = (KNetPlayerInfo) e.get(CHANGE_STATUS);

			netUpdatePlayerExist();
			netvsSetGameScreenLayout();

			if(player.getPlayer().equals(knetClient().getSource())) {
				netvsIsPractice = false;
				if(netvsIsGameActive && !netvsIsWatch()) {
					netvsIsNewcomer = true;
				}

				owner.engine[0].stat = GameEngine.STAT_SETTING;

				for(int i = 0; i < getPlayers(); i++) {
					if(owner.engine[i].field != null) {
						owner.engine[i].field.reset();
					}
					owner.engine[i].nowPieceObject = null;

					if((owner.engine[i].stat == GameEngine.STAT_NOTHING) || (!netvsIsGameActive)) {
						owner.engine[i].stat = GameEngine.STAT_SETTING;
					}
					owner.engine[i].resetStatc();
				}
			}
		}
		// Someone entered here
		if(e.is(PLAYER_ENTER)) {
			KNetPlayerInfo player = (KNetPlayerInfo) e.get(PLAYER_ENTER);
			int seatID = player.getSeatId();
			if((seatID != -1) && (netvsNumPlayers() < 2)) {
				owner.receiver.playSE("levelstop");
			}
			netUpdatePlayerExist();
		}
		// Someone leave here
		if(e.is(PLAYER_LEAVE)) {
			netUpdatePlayerExist();

			if(netvsNumPlayers() < 2) {
				netvsAutoStartTimerActive = false;
			}
		}
		// Automatic timer start
		if(e.is(AUTOSTART_BEGIN)) {
			if(netvsNumPlayers() >= 2) {
				int seconds = (Integer) e.get(AUTOSTART_BEGIN);
				netvsAutoStartTimer = seconds * 60;
				netvsAutoStartTimerActive = true;
			}
		}
		// Automatic timer stop
		if(e.is(AUTOSTART_STOP)) {
			netvsAutoStartTimerActive = false;
		}
		// Game Started
//		if(message[0].equals("start")) {
		if(e.is(START)) {
			KNStartInfo start = (KNStartInfo) e.get(START);
			long randseed = start.getSeed();
			netvsNumNowPlayers = start.getPlayerCount();
			netvsNumAlivePlayers = netvsNumNowPlayers;
			netvsMapNo = start.getMapNumber();

			netvsResetFlags();
			netUpdatePlayerExist();

			owner.menuOnly = false;
			owner.bgmStatus.reset();
			owner.backgroundStatus.reset();
			owner.replayProp.clear();
			for(int i = 0; i < getPlayers(); i++) {
				if(netvsPlayerExist[i]) {
					owner.engine[i].init();
					netvsSetGameScreenLayout(owner.engine[i]);
				}
			}

			netvsAutoStartTimerActive = false;
			netvsIsGameActive = true;
			netvsIsGameFinished = false;
			netvsPlayTimer = 0;

			netvsApplyRoomSettings();
			netvsSetLockedRule();	// Set locked rule/Restore rule

			for(int i = 0; i < getPlayers(); i++) {
				GameEngine engine = owner.engine[i];
				engine.resetStatc();

				if(netvsPlayerExist[i]) {
					netvsPlayerActive[i] = true;
					engine.stat = GameEngine.STAT_READY;
					engine.randSeed = randseed;
					engine.random = new Random(randseed);

					if((channelInfo().getMaxPlayers() == 2) && (netvsNumPlayers() == 2)) {
						engine.isVisible = true;
						engine.displaysize = 0;

						if( (channelInfo().isRuleLock()) || ((i == 0) && (!netvsIsWatch())) ) {
							engine.isNextVisible = true;
							engine.isHoldVisible = true;

							if(i != 0) {
								engine.randomizer = owner.engine[0].randomizer;
							}
						} else {
							engine.isNextVisible = false;
							engine.isHoldVisible = false;
						}
					}
				} else if(i < channelInfo().getMaxPlayers()) {
					engine.stat = GameEngine.STAT_SETTING;
					engine.isVisible = true;
					engine.isNextVisible = false;
					engine.isHoldVisible = false;

					if((channelInfo().getMaxPlayers() == 2) && (netvsNumPlayers() == 2)) {
						engine.isVisible = false;
					}
				} else {
					engine.stat = GameEngine.STAT_SETTING;
					engine.isVisible = false;
				}

				netvsPlayerResultReceived[i] = false;
				netvsPlayerDead[i] = false;
				netvsPlayerReady[i] = false;
			}
		}
		// Dead
//		if(message[0].equals("dead")) {
		if(e.is(DEAD)) {
			int seatID = (Integer) e.get(DEAD);
			int playerID = netvsGetPlayerIDbySeatID(seatID);

			if(!netvsPlayerDead[playerID]) {
				netvsPlayerDead[playerID] = true;
				netvsPlayerPlace[playerID] = (Integer) e.get(DEAD_PLACE);
				owner.engine[playerID].stat = GameEngine.STAT_GAMEOVER;
				owner.engine[playerID].resetStatc();
				netvsNumAlivePlayers--;

				if(seatID == channelInfo().getPlayers().indexOf(knetClient().getSource())) {
					if(!netvsIsDeadPending) {
						// Forced death
						netSendField(owner.engine[0]);
						netSendNextAndHold(owner.engine[0]);
						netSendStats(owner.engine[0]);
						netvsPlayerResultReceived[0] = true;
					}

					// Send end game stats
					netSendEndGameStats(owner.engine[0]);
				}
			}
		}
		// End-of-game Stats
//		if(message[0].equals("gstat")) {
		if(e.is(GAME_END_STATS)) {
			netvsRecvEndGameStats(e);
		}
		// Game Finished
//		if(message[0].equals("finish")) {
		if(e.is(FINISH)) {
			netvsIsGameActive = false;
			netvsIsGameFinished = true;
			netvsPlayTimerActive = false;
			netvsIsNewcomer = false;

			// Stop practice game
			if(netvsIsPractice) {
				netvsIsPractice = false;
				netvsIsPracticeExitAllowed = false;
				owner.bgmStatus.bgm = BGMStatus.BGM_NOTHING;
				owner.engine[0].gameEnded();
				owner.engine[0].stat = GameEngine.STAT_SETTING;
				owner.engine[0].resetStatc();
			}

			boolean flagTeamWin = (Boolean) e.get(FINISH);

			if(flagTeamWin) {
				// Team won
				for(int i = 0; i < getPlayers(); i++) {
					if(netvsPlayerExist[i] && !netvsPlayerDead[i]) {
						netvsPlayerPlace[i] = 1;
						owner.engine[i].gameEnded();
						owner.engine[i].stat = GameEngine.STAT_EXCELLENT;
						owner.engine[i].resetStatc();
						owner.engine[i].statistics.time = netvsPlayTimer;
						netvsNumAlivePlayers--;

						if((i == 0) && (!netvsIsWatch())) {
							netSendEndGameStats(owner.engine[0]);
						}
					}
				}
			} else {
				// Normal player won
				int seatID = channelInfo().getPlayers().indexOf(e.get(FINISH_WINNER));
				if(seatID != -1) {
					int playerID = netvsGetPlayerIDbySeatID(seatID);
					if(netvsPlayerExist[playerID]) {
						netvsPlayerPlace[playerID] = 1;
						owner.engine[playerID].gameEnded();
						owner.engine[playerID].stat = GameEngine.STAT_EXCELLENT;
						owner.engine[playerID].resetStatc();
						owner.engine[playerID].statistics.time = netvsPlayTimer;
						netvsNumAlivePlayers--;

						if((seatID == channelInfo().getPlayers().indexOf(knetClient().getSource())) && (!netvsIsWatch())) {
							netSendEndGameStats(owner.engine[0]);
						}
					}
				}
			}

			if((netvsIsWatch()) || (netvsPlayerPlace[0] >= 3)) {
				owner.receiver.playSE("matchend");
			}

			netUpdatePlayerExist();
		}
		// Game messages
		if(e.is(GAME)) {
			int seatID = channelInfo().getPlayers().indexOf(e.getSource());
			int playerID = netvsGetPlayerIDbySeatID(seatID);
//			int playerID = seatID;
			GameEngine engine = owner.engine[playerID];

			if(engine.field == null) {
				engine.createFieldIfNeeded();
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

				// Play timer start
				if(netvsIsWatch() && !netvsIsNewcomer && !netvsPlayTimerActive && !netvsIsGameFinished) {
					netvsPlayTimerActive = true;
					netvsPlayTimer = 0;
				}

				// Force start
				if((!netvsIsWatch()) && (netvsPlayTimerActive) && (!netvsIsPractice) &&
						(engine.stat == GameEngine.STAT_READY) && (engine.statc[0] < engine.goEnd))
				{
					engine.statc[0] = engine.goEnd;
				}
			}
			// Next and Hold
			if(e.is(GAME_NEXT_PIECE)) {
				netRecvNextAndHold(engine, e);
			}
		}
	}
}
