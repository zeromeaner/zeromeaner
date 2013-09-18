package org.zeromeaner.knet.ser;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.Callable;

import org.badiff.ByteArrayDiffs;
import org.badiff.Diff;
import org.badiff.alg.InertialGraph;
import org.badiff.imp.MemoryDiff;
import org.badiff.io.DefaultSerialization;
import org.badiff.q.ChunkingOpQueue;
import org.badiff.q.CoalescingOpQueue;
import org.badiff.q.GraphOpQueue;
import org.badiff.q.OpQueue;
import org.badiff.util.Diffs;
import org.badiff.util.Serials;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.InputChunked;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.io.OutputChunked;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.util.ObjectMap;

public class DiffFieldSerializer<T> extends FieldSerializer<T> {
	protected T typical;
	protected byte[] typicalBytes;
	
	public DiffFieldSerializer(Kryo kryo, Class<T> type, T typical) {
		super(kryo, type);
		this.typical = typical;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		Output output = new Output(bout);
		super.write(kryo, output, typical);
		output.flush();
		typicalBytes = bout.toByteArray();
	}

	@Override
	public void write(Kryo kryo, Output output, T object) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		Output boutput = new Output(bout);
		super.write(kryo, boutput, object);
		boutput.flush();
		byte[] objectBytes = bout.toByteArray();
		OpQueue q = Diffs.queue(typicalBytes, objectBytes);
		q = new ChunkingOpQueue(q);
		q = new GraphOpQueue(q, new InertialGraph((Diff.DEFAULT_CHUNK + 1) * (Diff.DEFAULT_CHUNK + 1)));
		q = new CoalescingOpQueue(q);
		MemoryDiff md = new MemoryDiff(q);
		byte[] diffBytes = Serials.serialize(DefaultSerialization.newInstance(), MemoryDiff.class, md);
		kryo.writeObject(output, diffBytes);
	}

	@Override
	public T read(Kryo kryo, Input input, Class<T> type) {
		byte[] diffBytes = kryo.readObject(input, byte[].class);
		MemoryDiff md = Serials.deserialize(DefaultSerialization.newInstance(), MemoryDiff.class, diffBytes);
		byte[] objectBytes = Diffs.apply(md, typicalBytes);
		Input binput = new Input(objectBytes);
		return super.read(kryo, binput, type);
	}

}
