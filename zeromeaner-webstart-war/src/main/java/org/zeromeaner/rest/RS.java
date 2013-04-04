package org.zeromeaner.rest;

import org.zeromeaner.knet.KNetEventSource;
import org.zeromeaner.knet.obj.KNetChannelInfo;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RS {
	protected ObjectMapper createMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
		mapper.addMixInAnnotations(KNetChannelInfo.class, Mixin.Channel.class);
		mapper.addMixInAnnotations(KNetEventSource.class, Mixin.Source.class);
		return mapper;
	}
}
