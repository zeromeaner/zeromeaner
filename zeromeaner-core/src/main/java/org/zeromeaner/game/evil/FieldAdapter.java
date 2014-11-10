package org.zeromeaner.game.evil;

import org.eviline.core.Block;
import org.eviline.core.Field;

public class FieldAdapter extends Field {
	protected static Block FILLED = new Block(0);
	
	public void update(org.zeromeaner.game.component.Field nullpo) {
		reset();
		for(int y = -BUFFER; y < HEIGHT; y++) {
			for(int x = 0; x < WIDTH; x++) {
				setBlock(x, y, nullpo.getBlockEmpty(x, y) ? null : FILLED);
			}
		}
	}
}
