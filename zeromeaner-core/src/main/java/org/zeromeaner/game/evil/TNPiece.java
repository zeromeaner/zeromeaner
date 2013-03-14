package org.zeromeaner.game.evil;

import org.zeromeaner.game.component.Piece;

import org.eviline.Shape;
import org.eviline.ShapeType;

public class TNPiece {
	public static ShapeType fromNullpo(int pieceId) {
		switch(pieceId) {
		case Piece.PIECE_I: return ShapeType.I;
		case Piece.PIECE_L: return ShapeType.L;
		case Piece.PIECE_O: return ShapeType.O;
		case Piece.PIECE_Z: return ShapeType.Z;
		case Piece.PIECE_T: return ShapeType.T;
		case Piece.PIECE_J: return ShapeType.J;
		case Piece.PIECE_S: return ShapeType.S;
		}
		return null;
	}
	
	public static Shape fromNullpo(Piece piece) {
		if(piece == null)
			return null;
		ShapeType type = fromNullpo(piece.id);
		switch(piece.direction) {
		case Piece.DIRECTION_UP: return type.up();
		case Piece.DIRECTION_RIGHT: return type.right();
		case Piece.DIRECTION_DOWN: return type.down();
		case Piece.DIRECTION_LEFT: return type.left();
		}
		return null;
	}
	
	public static int toNullpo(ShapeType type) {
		int pieceId = -1;
		switch(type) {
		case I: pieceId = Piece.PIECE_I; break;
		case L: pieceId = Piece.PIECE_L; break;
		case O: pieceId = Piece.PIECE_O; break;
		case Z: pieceId = Piece.PIECE_Z; break;
		case T: pieceId = Piece.PIECE_T; break;
		case J: pieceId = Piece.PIECE_J; break;
		case S: pieceId = Piece.PIECE_S; break;
		}
		return pieceId;
	}
	
	public static Piece toNullpo(Shape shape) {
		Piece piece = new Piece(toNullpo(shape.type()));
		switch(shape.direction()) {
		case UP: piece.direction = Piece.DIRECTION_UP;
		case RIGHT: piece.direction = Piece.DIRECTION_RIGHT;
		case DOWN: piece.direction = Piece.DIRECTION_DOWN;
		case LEFT: piece.direction = Piece.DIRECTION_LEFT;
		}
		return piece;
	}
}
