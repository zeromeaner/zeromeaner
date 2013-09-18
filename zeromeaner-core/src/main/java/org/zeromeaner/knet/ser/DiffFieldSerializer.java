package org.zeromeaner.knet.ser;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.Callable;

import org.badiff.ByteArrayDiffs;
import org.badiff.Diff;
import org.badiff.alg.EditGraph;
import org.badiff.alg.Graph;
import org.badiff.alg.InertialGraph;
import org.badiff.imp.MemoryDiff;
import org.badiff.io.DefaultSerialization;
import org.badiff.q.ChunkingOpQueue;
import org.badiff.q.CoalescingOpQueue;
import org.badiff.q.GraphOpQueue;
import org.badiff.q.OpQueue;
import org.badiff.util.Diffs;
import org.badiff.util.Serials;
import org.zeromeaner.knet.KNetKryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.InputChunked;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.io.OutputChunked;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.util.ObjectMap;

public class DiffFieldSerializer<T> extends Serializer<T> {
	protected T typical;
	protected byte[] typicalBytes;
	protected FieldSerializer<T> flds;
	
	protected Graph graph = new InertialGraph((Diff.DEFAULT_CHUNK + 1) * (Diff.DEFAULT_CHUNK + 1));
	
	public DiffFieldSerializer(Kryo kryo, Class<T> type, T typical) {
		this.typical = typical;
		
		flds = new FieldSerializer<T>(kryo, type);
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		Output boutput = new Output(bout);
		flds.write(kryo, boutput, typical);
		boutput.flush();
		typicalBytes = bout.toByteArray();
	}

	@Override
	public void write(Kryo kryo, Output output, T object) {
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		Output boutput = new Output(bout);
		flds.write(kryo, boutput, object);
		boutput.flush();
		byte[] objectBytes = bout.toByteArray();
		OpQueue q = Diffs.queue(typicalBytes, objectBytes);
		q = new ChunkingOpQueue(q);
		q = new GraphOpQueue(q, graph);
		q = new CoalescingOpQueue(q);
		MemoryDiff md = new MemoryDiff(q);
		byte[] diffBytes = Serials.serialize(DefaultSerialization.newInstance(), MemoryDiff.class, md);
		
		output.writeInt(diffBytes.length, true);
		output.write(diffBytes);
		
	}

	@Override
	public T read(Kryo kryo, Input input, Class<T> type) {
//		byte[] diffBytes = kryo.readObject(input, byte[].class);
		
		byte[] diffBytes = new byte[input.readInt(true)];
		input.read(diffBytes);
		
		MemoryDiff md = Serials.deserialize(DefaultSerialization.newInstance(), MemoryDiff.class, diffBytes);
		byte[] objectBytes = Diffs.apply(md, typicalBytes);
		Input binput = new Input(objectBytes);
//		return super.read(kryo, binput, type);
		return flds.read(kryo, binput, type);
	}

}
