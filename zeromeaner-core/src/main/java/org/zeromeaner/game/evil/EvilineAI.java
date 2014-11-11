package org.zeromeaner.game.evil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.eviline.core.Command;
import org.eviline.core.Configuration;
import org.eviline.core.Engine;
import org.eviline.core.ShapeType;
import org.eviline.core.XYShapes;
import org.eviline.core.ai.AIKernel;
import org.eviline.core.ai.AIPlayer;
import org.eviline.core.ai.CommandGraph;
import org.eviline.core.ai.DefaultAIKernel;
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

	protected static final int LOOKAHEAD = 3;
	
	protected DefaultAIKernel ai;
	
	protected FutureTask<byte[]> pathified;
	
	protected Command shifting = null;
	
	protected FieldAdapter expectedField;
	protected int xyhold = -1;
	protected boolean swapHold = false;
	
	protected ExecutorService computePool;
	
	protected ExecutorService comparePool;
	
	protected void computeCommandPath(final Engine engine, final int xyhold) {
		Callable<byte[]> task = new Callable<byte[]>() {
			@Override
			public byte[] call() {
				byte[] computingPaths = new byte[XYShapes.SHAPE_MAX];
				Arrays.fill(computingPaths, (byte) -1);
				
				AIPlayer player;
				
				if(xyhold != -1) {
					Engine holdEngine = new Engine(engine.getField().clone(), new Configuration(null, 0));
					holdEngine.setNext(engine.getNext().clone());
					holdEngine.setShape(xyhold);

					final AIPlayer holdPlayer = new AIPlayer(ai, holdEngine, LOOKAHEAD);
					final AIPlayer currentPlayer = new AIPlayer(ai, engine, LOOKAHEAD);
					
					try {
						Future<?> hpf = comparePool.submit(new Runnable() {
							@Override
							public void run() {
								holdPlayer.tick();
							}
						});
						Future<?> cpf = comparePool.submit(new Runnable() {
							@Override
							public void run() {
								currentPlayer.tick();
							}
						});
						hpf.get();
						cpf.get();
					} catch(Exception e) {
						throw new RuntimeException(e);
					}

					if(holdPlayer.getBest().score < currentPlayer.getBest().score) {
						player = holdPlayer;
						swapHold = true;
					} else
						player = currentPlayer;
				} else {
					player = new AIPlayer(ai, engine, LOOKAHEAD);
					player.tick();
				}
				
				int xyshape = player.getDest();
				if(xyshape == -1)
					return computingPaths;
				
				computingPaths[xyshape] = (byte) Command.HARD_DROP.ordinal();
				CommandGraph g = player.getGraph();
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

				expectedField.copyFrom(engine.getField());
				
				return computingPaths;
			}
		};
		pathified = new FutureTask<>(task);
		computePool.execute(pathified);
	}
	
	protected byte[] commandPath() {
		if(pathified == null || !pathified.isDone())
			return null;
		try {
			return pathified.get();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getName() {
		return "eviline2";
	}

	@Override
	public void init(GameEngine engine, int playerID) {
		ai = new DefaultAIKernel(new NextFitness());
		computePool = Executors.newFixedThreadPool(1);
		comparePool = Executors.newFixedThreadPool(2);
		expectedField = new FieldAdapter();
		swapHold = false;
		xyhold = -1;
	}

	@Override
	public void shutdown(GameEngine engine, int playerID) {
		if(computePool != null)
			computePool.shutdown();
		if(comparePool != null)
			comparePool.shutdown();
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

		if(xyhold == -1 && engine.isHoldOK()) {
			input |= Controller.BUTTON_BIT_D;
			ctrl.setButtonBit(input);
			ShapeType ht = XYShapes.shapeFromInt(xyshape).type();
			xyhold = XYShapes.toXYShape(ht.startX(), ht.startY(), ht.start());
			return;
		}
		
		if(shifting != null) {
			if(expectedField.update(engine.field)) {
				shifting = null;
				ctrl.setButtonBit(input);
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
		
		byte[] paths = commandPath();
		
		if(paths == null) {
			ctrl.setButtonBit(input);
			return;
		}
		
		if(swapHold) {
			if(engine.isHoldOK()) {
				input |= Controller.BUTTON_BIT_D;
				ShapeType ht = XYShapes.shapeFromInt(xyshape).type();
				xyhold = XYShapes.toXYShape(ht.startX(), ht.startY(), ht.start());
			} else {
				EngineAdapter e = new EngineAdapter();
				e.update(engine);
				computeCommandPath(e, -1);
			}
			ctrl.setButtonBit(input);
			swapHold = false;
			return;
		}
		
		Command c = Command.fromOrdinal(paths[xyshape]);
		if(c == null || expectedField.update(engine.field)) {
			EngineAdapter e = new EngineAdapter();
			e.update(engine);
			computeCommandPath(e, engine.isHoldOK() ? xyhold : -1);
			ctrl.setButtonBit(input);
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
			org.eviline.core.Field next = expectedField.clone();
			int xyd = XYShapes.shiftedDown(xyshape);
			while(!next.intersects(xyd)) {
				xyshape = xyd;
				xyd = XYShapes.shiftedDown(xyshape);
			}
			Engine e = new Engine(next, new Configuration(null, 0));
			ShapeType[] nextShapes = new ShapeType[engine.nextPieceArraySize];
			for(int i = 0; i < nextShapes.length; i++)
				nextShapes[i] = XYShapeAdapter.toShapeType(engine.getNextObject(engine.nextPieceCount + i));
			e.setNext(nextShapes);
			e.setShape(xyshape);
			e.tick(Command.SHIFT_DOWN);
			while(e.getShape() == -1)
				e.tick(Command.NOP);
			computeCommandPath(e, engine.isHoldOK() ? xyhold : -1);
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
		if(pathified == null) {
			EngineAdapter e = new EngineAdapter();
			e.update(engine);
			computeCommandPath(e, engine.isHoldOK() ? xyhold : -1);
		}
	}

	@Override
	public void renderHint(GameEngine engine, int playerID) {
		// TODO Auto-generated method stub

	}

}
