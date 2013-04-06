package org.zeromeaner.rest;

import java.io.StringWriter;
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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;

import static org.zeromeaner.knet.KNetEventArgs.*;

@Path("/channels")
@Produces("application/json")
public class ChannelRS extends RS {
	public static List<KNetChannelInfo> getChannels() throws Exception {
		final List<KNetChannelInfo> channels = new ArrayList<KNetChannelInfo>();
		final CountDownLatch latch = new CountDownLatch(1);
		KNetClient client = new KNetClient("localhost", KNetServer.DEFAULT_PORT);
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
	@Produces("application/json")
	public String names() throws Exception {
		List<String> names = new ArrayList<String>();
		for(KNetChannelInfo c : getChannels()) {
			names.add(c.getName());
		}
		return createMapper().writerWithView(View.class).writeValueAsString(names);
	}
}
