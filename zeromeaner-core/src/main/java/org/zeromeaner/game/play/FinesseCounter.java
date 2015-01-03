package org.zeromeaner.game.play;

import org.eviline.core.Command;
import org.eviline.core.Field;
import org.eviline.core.XYShapes;
import org.eviline.core.ai.CommandGraph;
import org.zeromeaner.game.event.EngineAdapter;
import org.zeromeaner.game.event.EngineEvent;
import org.zeromeaner.game.eviline.FieldAdapter;
import org.zeromeaner.game.eviline.XYShapeAdapter;

public class FinesseCounter extends EngineAdapter {
	private static class FinesseGraph extends CommandGraph {

		public FinesseGraph(Field field, int start) {
			super(field, start, true);
		}

		@Override
		protected void searchShiftLeft(int shape, Field f) {
			int nextPathLength = pathLengthOf(vertices, shape) + 1;

			int next;
			
			next = XYShapes.shiftedLeft(shape);
			if(!f.intersects(next)) {
				maybeUpdate(next, shape, Command.SHIFT_LEFT, nextPathLength, f);

				int sl = next;
				while(!f.intersects(next))
					next = XYShapes.shiftedLeft(next);
				next = XYShapes.shiftedRight(next);
				if(next != sl)
					maybeUpdate(next, sl, Command.AUTOSHIFT_LEFT, nextPathLength + 1, f);
			}
		}
		
		@Override
		protected void searchShiftRight(int shape, Field f) {
			int nextPathLength = pathLengthOf(vertices, shape) + 1;

			int next;
			
			next = XYShapes.shiftedRight(shape);
			if(!f.intersects(next)) {
				maybeUpdate(next, shape, Command.SHIFT_RIGHT, nextPathLength, f);

				int sr = next;
				while(!f.intersects(next))
					next = XYShapes.shiftedRight(next);
				next = XYShapes.shiftedLeft(next);
				if(next != sr)
					maybeUpdate(next, sr, Command.AUTOSHIFT_RIGHT, nextPathLength + 1, f);
			}
		}
	}
	
	protected FieldAdapter field;
	protected int xystart;
	protected int xyshape;
	protected FinesseGraph graph;
	protected int moves;
	protected long finesse;
	protected int finesseDelta;
	
	public FinesseCounter() {
		field = new FieldAdapter();
		xystart = xyshape = -1;
	}
	
	@Override
	public boolean engineMove(EngineEvent e) {
		if(e.getSource().nowPieceMoveCount == 0) {
			xystart = XYShapeAdapter.toXYShape(e.getSource());
			if(xystart != -1) {
				field.update(e.getSource().field);
				graph = new FinesseGraph(field, xystart);
				moves = 0;
				xyshape = xystart;
			}
		}
		int s = XYShapeAdapter.toXYShape(e.getSource());
		if(xyshape != s)
			moves++;
		xyshape = s;
		return super.engineMove(e);
	}
	
	@Override
	public void enginePieceLocked(EngineEvent e) {
		int s = XYShapeAdapter.toXYShape(e.getSource());
		if(xyshape != s)
			moves++;
		xyshape = s;
		int actualMoves = moves;
		int idealMoves = CommandGraph.pathLengthOf(graph.getVertices(), xyshape);
		finesse += (finesseDelta = actualMoves - idealMoves);
		xystart = -1;
	}
	
	@Override
	public void engineRenderLast(EngineEvent e) {
		e.getSource().owner.receiver.drawDirectFont(e.getSource(), e.getPlayerId(), 1, 1, "FINESSE: " + finesse + " (" + finesseDelta + ")");
	}
}
