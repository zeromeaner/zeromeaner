package org.zeromeaner.game.knet.ser;

import java.util.ArrayList;

import org.zeromeaner.game.component.Block;
import org.zeromeaner.game.component.Field;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class FieldSerializer extends Serializer<Field> {

	@Override
	public void write(Kryo kryo, Output output, Field f) {
		kryo.writeObjectOrNull(output, f.getBlock_field(), Block[][].class);
		kryo.writeObjectOrNull(output, f.getBlock_hidden(), Block[][].class);
		output.writeBoolean(f.isCeiling());
		output.writeInt(f.getColorClearExtraCount(), true);
		output.writeInt(f.getColorsCleared(), true);
		output.writeInt(f.getGarbageCleared(), true);
		output.writeInt(f.getGemsCleared(), true);
		output.writeInt(f.getHeight(), true);
		output.writeInt(f.getHidden_height(), true);
		output.writeInt(f.getHurryupFloorLines(), true);
		kryo.writeObjectOrNull(output, f.getLastLinesCleared(), ArrayList.class);
		kryo.writeObjectOrNull(output, f.getLineColorsCleared(), ArrayList.class);
		kryo.writeObjectOrNull(output, f.getLineflag_field(), boolean[].class);
		kryo.writeObjectOrNull(output, f.getLineflag_hidden(), boolean[].class);
		output.writeInt(f.getWidth(), true);
	}

	@Override
	public Field read(Kryo kryo, Input input, Class<Field> type) {
		Field f = new Field();
		
		f.setBlock_field(kryo.readObjectOrNull(input, Block[][].class));
		f.setBlock_hidden(kryo.readObjectOrNull(input, Block[][].class));
		f.setCeiling(input.readBoolean());
		f.setColorClearExtraCount(input.readInt(true));
		f.setColorsCleared(input.readInt(true));
		f.setGarbageCleared(input.readInt(true));
		f.setGemsCleared(input.readInt(true));
		f.setHeight(input.readInt(true));
		f.setHidden_height(input.readInt(true));
		f.setHurryupFloorLines(input.readInt(true));
		f.setLastLinesCleared(kryo.readObjectOrNull(input, ArrayList.class));
		f.setLineColorsCleared(kryo.readObjectOrNull(input, ArrayList.class));
		f.setLineflag_field(kryo.readObjectOrNull(input, boolean[].class));
		f.setLineflag_hidden(kryo.readObjectOrNull(input, boolean[].class));
		f.setWidth(input.readInt(true));

		return f;
	}

}
