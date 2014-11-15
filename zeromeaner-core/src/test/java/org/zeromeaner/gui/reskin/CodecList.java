package org.zeromeaner.gui.reskin;

import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.ICodec.ID;
import com.xuggle.xuggler.ICodec.Type;

public class CodecList {
	public static void main(String[] args) {
		for(ID id : ID.values()) {
			ICodec c;
			if((c = ICodec.findEncodingCodec(id)) != null) {
				if(c.getType() == Type.CODEC_TYPE_VIDEO)
					System.out.println(id);
			}
		}
	}
}
