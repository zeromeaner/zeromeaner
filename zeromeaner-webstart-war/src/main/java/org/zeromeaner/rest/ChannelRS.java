package org.zeromeaner.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.zeromeaner.game.play.GameManager;
import org.zeromeaner.knet.KNetClient;
import org.zeromeaner.knet.KNetEvent;
import org.zeromeaner.knet.KNetListener;
import org.zeromeaner.knet.obj.KNetChannelInfo;
import org.zeromeaner.knet.srv.KNetServer;

import static org.zeromeaner.knet.KNetEventArgs.*;

@Path("/channels")
@Produces(MediaType.APPLICATION_JSON)
public class ChannelRS extends RS {
	public static List<KNetChannelInfo> getChannels() throws Exception {
		final List<KNetChannelInfo> channels = new ArrayList<KNetChannelInfo>();
		final CountDownLatch latch = new CountDownLatch(1);
		KNetClient client = new KNetClient("localhost", KNetServer.DEFAULT_PORT + (GameManager.VERSION.isSnapshot() ? 1 : 0));
		client.addKNetListener(new KNetListener() {
			@Override
			public void knetEvented(KNetClient client, KNetEvent e) {
				if(e.is(CHANNEL_LIST) && e.is(CHANNEL_INFO, KNetChannelInfo[].class)) {
					channels.addAll(Arrays.asList(e.get(CHANNEL_INFO, KNetChannelInfo[].class)));
					client.removeKNetListener(this);
					latch.countDown();
				}
			}
		});
		client.start();
		client.fireTCP(CHANNEL_LIST);
		latch.await();
		client.stop();
		return channels;
	}
	
	@GET
	public String list() throws Exception {
		return createMapper().writerWithView(View.class).writeValueAsString(getChannels());
	}
	
	@GET
	@Path("/names")
	@Produces(MediaType.APPLICATION_JSON)
	public String names() throws Exception {
		List<String> names = new ArrayList<String>();
		for(KNetChannelInfo c : getChannels()) {
			names.add(c.getName());
		}
		return createMapper().writerWithView(View.class).writeValueAsString(names);
	}
	
	@GET
	@Path("/id/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public String get(@PathParam("id") String sid) throws Exception {
		int id = Integer.parseInt(sid);
		KNetChannelInfo channel = null;
		for(KNetChannelInfo c : getChannels()) {
			if(id == c.getId())
				channel = c;
		}
		return createMapper().writerWithView(View.Detail.class).writeValueAsString(channel);
	}
}
