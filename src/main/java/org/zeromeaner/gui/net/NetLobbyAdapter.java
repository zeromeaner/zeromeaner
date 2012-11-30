package org.zeromeaner.gui.net;

import java.io.IOException;

import org.zeromeaner.game.net.NetPlayerClient;
import org.zeromeaner.game.net.NetRoomInfo;

public abstract class NetLobbyAdapter implements NetLobbyListener {

	@Override
	public void netlobbyOnInit(NetLobbyFrame lobby) {
	}

	@Override
	public void netlobbyOnLoginOK(NetLobbyFrame lobby, NetPlayerClient client) {
	}

	@Override
	public void netlobbyOnRoomJoin(NetLobbyFrame lobby, NetPlayerClient client, NetRoomInfo roomInfo) {
	}

	@Override
	public void netlobbyOnRoomLeave(NetLobbyFrame lobby, NetPlayerClient client) {
	}

	@Override
	public void netlobbyOnDisconnect(NetLobbyFrame lobby, NetPlayerClient client, Throwable ex) {
	}

	@Override
	public void netlobbyOnMessage(NetLobbyFrame lobby, NetPlayerClient client, String[] message) throws IOException {
	}

	@Override
	public void netlobbyOnExit(NetLobbyFrame lobby) {
	}

}
