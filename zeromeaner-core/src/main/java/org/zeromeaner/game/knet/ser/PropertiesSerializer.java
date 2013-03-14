package org.zeromeaner.game.knet.ser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class PropertiesSerializer extends Serializer<Properties> {

	@Override
	public void write(Kryo kryo, Output output, Properties object) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
			GZIPOutputStream zout = new GZIPOutputStream(bout);
			object.store(zout, "");
			zout.finish();
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		}
		byte[] bytes = bout.toByteArray();
		output.writeInt(bytes.length, true);
		output.writeBytes(bytes);
	}

	@Override
	public Properties read(Kryo kryo, Input input, Class<Properties> type) {
		Properties p = kryo.newInstance(type);
		int size = input.readInt(true);
		byte[] bytes = input.readBytes(size);
		ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
		try {
			GZIPInputStream zin = new GZIPInputStream(bin);
			p.load(zin);
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		}
		return p;
	}

}
