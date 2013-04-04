package org.zeromeaner.rest;

import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_INFO;
import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_LIST;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.zeromeaner.knet.KNetClient;
import org.zeromeaner.knet.KNetEvent;
import org.zeromeaner.knet.KNetEventSource;
import org.zeromeaner.knet.KNetListener;
import org.zeromeaner.knet.obj.KNetChannelInfo;
import org.zeromeaner.knet.srv.KNetServer;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("users")
@Produces("application/json")
public class UsersRS extends RS {
	@GET
	public String list() throws Exception {
		List<KNetEventSource> users = new ArrayList<KNetEventSource>();
		for(KNetChannelInfo c : ChannelRS.getChannels()) {
			if(c.getId() == KNetChannelInfo.LOBBY_CHANNEL_ID) {
				users.addAll(c.getMembers());
				break;
			}
		}
		return createMapper().writerWithView(View.class).writeValueAsString(users);
	}
}
