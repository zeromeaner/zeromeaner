package org.zeromeaner.game.evil;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;

import org.eviline.core.Command;
import org.eviline.core.Configuration;
import org.eviline.core.Engine;
import org.eviline.core.ShapeType;
import org.eviline.core.XYShapes;
import org.eviline.core.ai.AIKernel;
import org.eviline.core.ai.AIPlayer;
import org.eviline.core.ai.CommandGraph;
import org.eviline.core.ai.DefaultAIKernel;
import org.eviline.core.ai.DefaultAIKernel.Best;
import org.eviline.core.ai.NextFitness;
import org.zeromeaner.game.component.Controller;
import org.zeromeaner.game.component.Field;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.game.subsystem.ai.AbstractAI;

public class EvilineAI extends AbstractAI {
	protected static final Runnable NOP = new Runnable() {
		@Override
		public void run() {
		}
	};

	protected class PathPipeline {
		public ExecutorService exec;
		public LinkedBlockingDeque<PathTask> pipe = new LinkedBlockingDeque<PathTask>();
		
		public PathPipeline() {
			exec = Executors.newSingleThreadExecutor();
		}
		
		public boolean extend(final GameEngine gameEngine) {
			if(pipe.size() == 0) {
				int xyshape = XYShapeAdapter.toXYShape(gameEngine);
				if(xyshape == -1)
					return false;
				FieldAdapter f = new FieldAdapter();
				f.update(gameEngine.field);
				final PathTask pt = new PathTask(
						this,
						gameEngine.nextPieceCount,
						f,
						XYShapeAdapter.toXYShape(gameEngine),
						createGameNext(gameEngine, gameEngine.nextPieceCount));
				pipe.offerLast(pt);
				exec.execute(new Runnable() {
					@Override
					public void run() {
						pt.task.run();
						exec.execute(new Runnable() {
							@Override
							public void run() {
								extend(gameEngine);
							}
						});
					}
				});
				return true;
			}
			
			PathTask tail = pipe.peekLast();
			if(tail.seq == gameEngine.nextPieceCount || !tail.task.isDone())
				return false;
			final PathTask pt = tail.extend(gameEngine);
			if(pt == null)
				return false;
			pipe.offerLast(pt);
			exec.execute(new Runnable() {
				@Override
				public void run() {
					pt.task.run();
					exec.execute(new Runnable() {
						@Override
						public void run() {
							extend(gameEngine);
						}
					});
				}
			});
			return true;
		}
		
		public PathTask discardUntil(GameEngine engine) {
			PathTask pt;
			for(pt = pipe.peekFirst(); pt != null && pt.seq < engine.nextPieceCount; pt = pipe.peekFirst())
				pipe.remove(pt);
			return pt;
		}
		
		public byte[] currentPath(GameEngine engine) {
			extend(engine);
			PathTask pt = discardUntil(engine);
			if(pt == null || !pt.task.isDone())
				return null;
			return pt.path;
		}
		
		public org.eviline.core.Field expectedField(GameEngine engine) {
			extend(engine);
			PathTask pt = discardUntil(engine);
			return pt.field;
		}
		
		public boolean isDirty(GameEngine engine) {
			FieldAdapter f = new FieldAdapter();
			f.copyFrom(expectedField(engine));
			return f.update(engine.field);
		}
		
		public void shutdown() {
			exec.shutdownNow();
			pipe.peekLast().task.cancel(true);
		}
	}
	
	protected class PathTask {
		public PathPipeline pipeline;
		public int seq;
		public org.eviline.core.Field field;
		public int xystart;
		public ShapeType[] next;
		public FutureTask<Best> task;
		public byte[] path;
		
		public PathTask(PathPipeline pipeline, int seq, org.eviline.core.Field field, int xystart, ShapeType[] next) {
			this.pipeline = pipeline;
			this.seq = seq;
			this.field = field;
			this.xystart = xystart;
			this.next = next;
			task = new FutureTask<>(new Callable<Best>() {
				@Override
				public Best call() throws Exception {
					Engine engine = new Engine(PathTask.this.field, new Configuration(null, 0));
					engine.setNext(PathTask.this.next);
					engine.setShape(PathTask.this.xystart);
					AIPlayer player = new AIPlayer(ai, engine, lookahead);
					player.tick();
					Best best = player.getBest();
					path = createCommandPath(best.graph);
					return best;
				}
			});
		}
		
		
		public Best get() {
			try {
				return task.get();
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		public PathTask extend(GameEngine gameEngine) {
			ShapeType[] extnext = createGameNext(gameEngine, seq);
			if(extnext == null || extnext.length < lookahead + 1)
				return null;
			return new PathTask(
					pipeline,
					seq+1,
					get().after,
					XYShapes.toXYShape(extnext[0].startX(), extnext[0].startY(), extnext[0].start()),
					Arrays.copyOfRange(extnext, 1, extnext.length));
		}
	}
	
	protected DefaultAIKernel ai;
	
	protected Command shifting = null;
	
	protected int lookahead = 2;
	
	protected PathPipeline pipeline;
	
	
	protected byte[] createCommandPath(CommandGraph g) {
		byte[] computingPaths = new byte[XYShapes.SHAPE_MAX];
		int xyshape = g.getSelectedShape();
				
		computingPaths[xyshape] = (byte) Command.HARD_DROP.ordinal();
		while(xyshape != CommandGraph.NULL_ORIGIN) {
			int parent = CommandGraph.originOf(g.getVertices(), xyshape);
			Command c = CommandGraph.commandOf(g.getVertices(), xyshape);
			if(parent >= 0 && parent < XYShapes.SHAPE_MAX)
				computingPaths[parent] = (byte) c.ordinal();
			if(c == Command.SOFT_DROP) {
				xyshape = XYShapes.shiftedUp(xyshape);
				while(xyshape != parent) {
					if(xyshape >= 0 && xyshape < XYShapes.SHAPE_MAX)
						computingPaths[xyshape] = (byte) Command.SOFT_DROP.ordinal();
					xyshape = XYShapes.shiftedUp(xyshape);
				}
			}
			xyshape = parent;
		}
		
		return computingPaths;
	}
	
	protected ShapeType[] createGameNext(GameEngine engine, int seq) {
		if(seq < engine.nextPieceCount || seq >= engine.nextPieceCount + engine.nextPieceArraySize)
			return null;
		ShapeType[] nextShapes = new ShapeType[engine.nextPieceArraySize];
		for(int i = 0; i < nextShapes.length; i++)
			nextShapes[i] = XYShapeAdapter.toShapeType(engine.getNextObject(engine.nextPieceCount + i));
		return nextShapes;
	}
	
	protected void resetPipeline() {
		pipeline.shutdown();
		pipeline = new PathPipeline();
	}
	
	@Override
	public String getName() {
		return "eviline2";
	}

	@Override
	public void init(GameEngine engine, int playerID) {
		ai = new DefaultAIKernel(new NextFitness());
		ai.setDropsOnly(true);
		pipeline = new PathPipeline();
	}

	@Override
	public void shutdown(GameEngine engine, int playerID) {
		if(pipeline != null)
			pipeline.shutdown();
	}

	@Override
	public void setControl(GameEngine engine, int playerID, Controller ctrl) {
		try {
		int xyshape = XYShapeAdapter.toXYShape(engine);
		int input = 0;
		
		if(xyshape == -1) {
			ctrl.setButtonBit(input);
			return;
		}

		if(shifting != null) {
			if(pipeline.isDirty(engine)) {
				shifting = null;
				ctrl.setButtonBit(input);
				resetPipeline();
				return;
			}
			
			EngineAdapter engineAdapter = new EngineAdapter();
			engineAdapter.update(engine);
			
			switch(shifting) {
			case AUTOSHIFT_LEFT:
				if(!engineAdapter.getField().intersects(XYShapes.shiftedLeft(xyshape)))
					input |= Controller.BUTTON_BIT_LEFT;
				else
					shifting = null;
				break;
			case AUTOSHIFT_RIGHT:
				if(!engineAdapter.getField().intersects(XYShapes.shiftedRight(xyshape)))
					input |= Controller.BUTTON_BIT_RIGHT;
				else
					shifting = null;
				break;
			case SOFT_DROP:
				if(!engineAdapter.getField().intersects(XYShapes.shiftedDown(xyshape)))
					input |= Controller.BUTTON_BIT_DOWN;
				else
					shifting = null;
				break;
			}
			if(shifting != null) {
				ctrl.setButtonBit(input);
				return;
			}
		}
		
		byte[] paths = pipeline.currentPath(engine);
		
		if(paths == null) {
			ctrl.setButtonBit(input);
			return;
		}
		
		Command c = Command.fromOrdinal(paths[xyshape]);
		if(c == null || pipeline.isDirty(engine)) {
			ctrl.setButtonBit(input);
			resetPipeline();
			return;
		} else
			paths[xyshape] = (byte) -1;
		
		switch(c) {
		case AUTOSHIFT_LEFT:
			if(ctrl.isPress(Controller.BUTTON_LEFT))
				break;
			shifting = Command.AUTOSHIFT_LEFT;
			input |= Controller.BUTTON_BIT_LEFT;
			break;
		case SHIFT_LEFT:
			if(!ctrl.isPress(Controller.BUTTON_LEFT))
				input |= Controller.BUTTON_BIT_LEFT;
			break;
		case AUTOSHIFT_RIGHT:
			if(ctrl.isPress(Controller.BUTTON_RIGHT))
				break;
			shifting = Command.AUTOSHIFT_RIGHT;
			input |= Controller.BUTTON_BIT_RIGHT;
			break;
		case SHIFT_RIGHT:
			if(!ctrl.isPress(Controller.BUTTON_RIGHT))
				input |= Controller.BUTTON_BIT_RIGHT;
			break;
		case ROTATE_LEFT:
			if(engine.isRotateButtonDefaultRight()) {
				if(!ctrl.isPress(Controller.BUTTON_B))
					input |= Controller.BUTTON_BIT_B;
			} else {
				if(!ctrl.isPress(Controller.BUTTON_A))
					input |= Controller.BUTTON_BIT_A;
			}
			break;
		case ROTATE_RIGHT:
			if(engine.isRotateButtonDefaultRight()) {
				if(!ctrl.isPress(Controller.BUTTON_A))
					input |= Controller.BUTTON_BIT_A;
			} else {
				if(!ctrl.isPress(Controller.BUTTON_B))
					input |= Controller.BUTTON_BIT_B;
			}
			break;
		case SOFT_DROP:
			if(ctrl.isPress(Controller.BUTTON_DOWN))
				break;
			shifting = Command.SOFT_DROP;
			input |= Controller.BUTTON_BIT_DOWN;
			break;
		case SHIFT_DOWN:
			if(!ctrl.isPress(Controller.BUTTON_DOWN))
				input |= Controller.BUTTON_BIT_DOWN;
			break;
		case HARD_DROP:
			if(!ctrl.isPress(Controller.BUTTON_UP))
				input |= Controller.BUTTON_BIT_UP;
			break;
		}
		
		ctrl.setButtonBit(input);
		} catch(RuntimeException re) {
			re.printStackTrace();
			throw re;
		}
	}

	@Override
	public void onFirst(GameEngine engine, int playerID) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLast(GameEngine engine, int playerID) {
		// TODO Auto-generated method stub
	}

	@Override
	public void renderState(GameEngine engine, int playerID) {
		// TODO Auto-generated method stub

	}

	@Override
	public void newPiece(GameEngine engine, int playerID) {
		try {
		pipeline.extend(engine);
		} catch(RuntimeException e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public void renderHint(GameEngine engine, int playerID) {
		// TODO Auto-generated method stub

	}

}
