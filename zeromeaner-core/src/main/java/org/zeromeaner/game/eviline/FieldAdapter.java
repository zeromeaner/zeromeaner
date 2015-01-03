package org.zeromeaner.game.eviline;

import org.eviline.core.Block;
import org.eviline.core.Field;

public class FieldAdapter extends Field {
	protected static Block FILLED = new Block(0);
	
	public FieldAdapter() {
		setNoBlocks(true);
	}
	
	public boolean update(org.zeromeaner.game.component.Field nullpo) {
		boolean changed = false;
		for(int y = -BUFFER; y < HEIGHT; y++) {
			for(int x = 0; x < WIDTH; x++) {
				org.zeromeaner.game.component.Block npb = nullpo.getBlock(x, y); 
				boolean empty = npb.color == org.zeromeaner.game.component.Block.BLOCK_COLOR_NONE;
				
				boolean wasEmpty = block(x, y) == null;
				if(empty ^ wasEmpty) {
					setBlock(x, y, empty ? null : FILLED);
					changed = true;
				}
			}
		}
		return changed;
	}
}
