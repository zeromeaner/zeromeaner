package org.zeromeaner.game.evil;

import org.zeromeaner.game.component.Block;
import org.zeromeaner.game.play.GameEngine;

import org.eviline.Field;
import org.eviline.randomizer.Randomizer;
import org.eviline.randomizer.RandomizerFactory;
import org.eviline.randomizer.RandomizerPresets;

public class TNField extends Field {
	
	protected GameEngine engine;
	
	public TNField(GameEngine engine) {
		super();
		this.engine = engine;
//		MaliciousRandomizerProperties mp = new MaliciousRandomizerProperties(3, 0, false, 30);
//		ThreadedMaliciousRandomizer r = new ThreadedMaliciousRandomizer(mp);
		Randomizer r = new RandomizerFactory().newRandomizer(RandomizerPresets.EVIL);
		// FIXME be able to set the random
//		r.setRandom(engine.random);
		provider = r;
	}
	
	public void update() {
		if(engine.field == null)
			return;
		for(int y = 0; y < BUFFER; y++) {
			for(int x = BUFFER;  x < BUFFER + WIDTH; x++)
				field[y][x] = null;
		}
		for(int y = 0; y < 20; y++) {
			for(int x = 0; x < 10; x++) {
				org.zeromeaner.game.component.Block npblock = engine.field.getBlock(x, y);
//				field[y + BUFFER][x + BUFFER] = npblock.color == 0 ? null : Block.values()[npblock.color];
				org.eviline.Block b = null;
				switch(npblock.color) {
				case Block.BLOCK_COLOR_NONE: b = null; break;
				case Block.BLOCK_COLOR_YELLOW: b = org.eviline.Block.O; break;
				case Block.BLOCK_COLOR_CYAN: b = org.eviline.Block.I; break;
				case Block.BLOCK_COLOR_GREEN: b = org.eviline.Block.S; break;
				case Block.BLOCK_COLOR_BLUE: b = org.eviline.Block.J; break;
				case Block.BLOCK_COLOR_PURPLE: b = org.eviline.Block.T; break;
				case Block.BLOCK_COLOR_RED: b = org.eviline.Block.Z; break;
				case Block.BLOCK_COLOR_ORANGE: b = org.eviline.Block.L; break;
				default:
					b = org.eviline.Block.X;
				}
				field[y + BUFFER][x + BUFFER] = b;
			}
		}
	}
	
	public void updateShape() {
		shape = TNPiece.fromNullpo(engine.nowPieceObject);
		shapeX = BUFFER + engine.nowPieceX;
		shapeY = BUFFER + engine.nowPieceY;
		
//		switch(shape) {
//		case S_LEFT: shapeX--; break;
//		case S_RIGHT: shapeX--; break;
//		case J_LEFT: shapeX--; break;
//		case L_DOWN: shapeX--; break;
//		case O_UP: shapeX--; break;
//		case Z_LEFT: shapeX--; break;
//		}
		
	}
	
	public Object writeReplace() {
		return copyInto(new Field());
	}
}
