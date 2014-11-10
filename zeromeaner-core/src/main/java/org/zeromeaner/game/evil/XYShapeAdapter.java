package org.zeromeaner.game.evil;

import org.eviline.core.Shape;
import org.eviline.core.ShapeType;
import org.eviline.core.XYShapes;
import org.zeromeaner.game.component.Piece;
import org.zeromeaner.game.play.GameEngine;

public abstract class XYShapeAdapter {

	public static ShapeType toShapeType(Piece p) {
		if(p == null)
			return null;
		
		ShapeType type = null;
		switch(p.id) {
			case Piece.PIECE_I:
				type = ShapeType.I;
				break;
			case Piece.PIECE_J:
				type = ShapeType.J;
				break;
			case Piece.PIECE_L:
				type = ShapeType.L;
				break;
			case Piece.PIECE_O:
				type = ShapeType.O;
				break;
			case Piece.PIECE_S:
				type = ShapeType.S;
				break;
			case Piece.PIECE_T:
				type = ShapeType.T;
				break;
			case Piece.PIECE_Z:
				type = ShapeType.Z;
				break;
		}
		return type;
	}
	
	public static int toXYShape(GameEngine engine) {
		Piece p = engine.nowPieceObject;
		ShapeType type = toShapeType(engine.nowPieceObject);
		
		if(type == null)
			return -1;
		
		Shape shape = null;
		
		switch(p.direction) {
		case Piece.DIRECTION_UP:
			shape = type.up();
			break;
		case Piece.DIRECTION_DOWN:
			shape = type.down();
			break;
		case Piece.DIRECTION_LEFT:
			shape = type.left();
			break;
		case Piece.DIRECTION_RIGHT:
			shape = type.right();
			break;
		}
		
		if(shape == null)
			return -1;
		
		return XYShapes.toXYShape(engine.nowPieceX, engine.nowPieceY, shape);
	}
	
	private XYShapeAdapter() {}
}
