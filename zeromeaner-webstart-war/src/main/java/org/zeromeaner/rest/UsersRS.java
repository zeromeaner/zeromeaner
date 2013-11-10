package org.zeromeaner.rest;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.zeromeaner.knet.KNetEventSource;
import org.zeromeaner.knet.obj.KNetChannelInfo;

@Path("users")
@Produces(MediaType.APPLICATION_JSON)
public class UsersRS extends RS {
	public static List<KNetEventSource> getUsers() throws Exception {
		List<KNetEventSource> users = new ArrayList<KNetEventSource>();
		for(KNetChannelInfo c : ChannelRS.getChannels()) {
			if(c.getId() == KNetChannelInfo.LOBBY_CHANNEL_ID) {
				users.addAll(c.getMembers());
				break;
			}
		}
		return users;
	}
	
	@GET
	public String list() throws Exception {
		return createMapper().writerWithView(View.class).writeValueAsString(getUsers());
	}
	
	@GET
	@Path("/names")
	@Produces(MediaType.APPLICATION_JSON)
	public String names() throws Exception {
		List<String> names = new ArrayList<String>();
		for(KNetEventSource s : getUsers()) {
			names.add(s.getName());
		}
		return createMapper().writerWithView(View.class).writeValueAsString(names);
	}
}
