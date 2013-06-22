package org.zeromeaner.game.evil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.eviline.Field;
import org.eviline.PlayerAction;
import org.eviline.PlayerActionType;
import org.eviline.ai.Decision;
import org.eviline.ai.DefaultAIKernel;
import org.eviline.ai.QueueContext;
import org.eviline.fitness.EvilineFitness;
import org.eviline.fitness.EvilineFitness2;
import org.eviline.Shape;
import org.eviline.ShapeType;
import org.zeromeaner.game.component.Controller;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.game.subsystem.ai.AbstractAI;


public class TNBot extends AbstractAI {

	public static int controllerButtonId(PlayerActionType paType) {
		switch(paType) {
		case DOWN_ONE:
			return Controller.BUTTON_DOWN;
		case ROTATE_LEFT:
			return Controller.BUTTON_B;
		case ROTATE_RIGHT:
			return Controller.BUTTON_A;
		case SHIFT_LEFT:
		case DAS_LEFT:
			return Controller.BUTTON_LEFT;
		case SHIFT_RIGHT:
		case DAS_RIGHT:
			return Controller.BUTTON_RIGHT;
		case HOLD:
			return Controller.BUTTON_D;
		case HARD_DROP:
			return Controller.BUTTON_UP;
		}
		throw new InternalError("switch fallthrough when all cases covered");
	}

	public static class Race extends TNBot {
		public Race() {
		}
		
		@Override
		public String getName() {
			return super.getName() + " [Race]";
		}
		
		@Override
		public void init(GameEngine engine, int playerID) {
			super.init(engine, playerID);
			kernel.setHardDropOnly(false);
			highGravity = false;
			skipHold = true;
			skipLookahead = false;
		}
	}
	
	public static class SlowRace extends Race {
		@Override
		public String getName() {
			return super.getName() + " [Slow]";
		}
		
		@Override
		public void init(GameEngine engine, int playerID) {
			super.init(engine, playerID);
			skipLookahead = false;
			lookahead = 3;
		}
	}
	
	public static class Dig extends TNBot {
		@Override
		public String getName() {
			return super.getName() + " [Dig]";
		}
		
		@Override
		public void init(GameEngine engine, int playerID) {
			super.init(engine, playerID);
			lookahead = 2;
			/*
			double[] fp = kernel.getFitness().getParams();
			fp[EvilineFitness.Weights.TRANSITION_EXP] += 2;
			fp[EvilineFitness.Weights.IMPOSSIBLE_POWER] += 2;
			fp[EvilineFitness.Weights.SMOOTHNESS_MULT] *= 3;
			*/
		}
	}
	
	public static class Clairevoyant extends TNBot {
		@Override
		public String getName() {
			return super.getName() + " [Clairevoyant]";
		}
		
		@Override
		public void init(GameEngine engine, int playerID) {
			super.init(engine, playerID);
			skipLookahead = false;
			skipHold = false;
//			kernel.setFitness(new HybridFitness());
		}
		
		@Override
		protected void recompute(GameEngine engine) {
			super.recompute(engine);
		}
	}
	
	protected static int MAX_RECOMPUTES = 5;

	protected static ExecutorService POOL = Executors.newCachedThreadPool();

	protected TNField field;
	protected DefaultAIKernel kernel = new DefaultAIKernel();
	protected boolean pressed = false;

	protected Future<List<PlayerAction>> futureActions;
	protected List<List<PlayerAction>> pipeline = new ArrayList<List<PlayerAction>>();

	protected int recomputes = 0;
	protected String misdrop = null;

	protected boolean held;
	protected boolean swapping;
	protected boolean computeHold;


	protected boolean highGravity = false;
	protected int lookahead = 1;
	protected boolean skipLookahead = false;
	protected boolean skipHold = false;
	protected int das = 0;
	protected Integer buttonId;
	protected double worst = 0;
	
	@Override
	public String getName() {
		return "Eviline AI";
	}

	@Override
	public void init(GameEngine engine, int playerID) {
		kernel.setHardDropOnly(false);
		highGravity = false;
		skipHold = false;
		skipLookahead = false;
		field = new TNField(engine);
		engine.aiShowHint = false;
		held = false;
		swapping = false;
		highGravity = false;
		worst = 0;
	}

	protected List<PlayerAction> actions() {
		if(futureActions == null || futureActions.isCancelled() || !futureActions.isDone())
			return null;
		try {
			return futureActions.get();
		} catch(Exception ex) {
			ex.printStackTrace();
			return null;
			//			throw new RuntimeException(ex);
		}
	}
	
	protected void recompute(final GameEngine engine) {
//		if(recomputes > MAX_RECOMPUTES)
//			return;

		final Shape shape;
		final Field field;
		this.field.update();
		this.field.updateShape();
		field = this.field;
		shape = field.getShape();
		double currentScore = kernel.getFitness().score(field);
		worst = Math.max(worst * 0.9, currentScore);

		Callable<List<PlayerAction>> task = new Callable<List<PlayerAction>>() {
			@Override
			public List<PlayerAction> call() throws Exception {
				if(!skipHold && !held && engine.isHoldOK()) {
					held = true;
					return new ArrayList<PlayerAction>(Arrays.asList(new PlayerAction(field, PlayerActionType.HOLD)));
				}
				
//				synchronized(pipeline) {
//					if(pipeline.size() > 0) {
//						List<PlayerAction> pla = pipeline.remove(0);
////						System.out.println("Returning pipelined computation " + pla);
//						return pla;
//					}
//				}
				
				kernel.setHighGravity(engine.statistics.level >= 10 || highGravity);

				
				Decision best;
				
				if(engine.nextPieceArraySize <= lookahead /*|| kernel.isHighGravity()*/ || skipLookahead) {
					// best for the current shape
					best = kernel.bestFor(field);

					// best for the hold shape
					if(
							!skipHold 
							&& computeHold
							&& !engine.holdDisable 
							&& engine.holdPieceObject != null 
							&& engine.isHoldOK()) {
						computeHold = false;
						Shape heldShape = TNPiece.fromNullpo(engine.holdPieceObject);
						if(heldShape.type() != shape.type()) {
							Field f = field.copy();
							f.setShape(heldShape);
							f.setShapeX(heldShape.type().starterX());
							f.setShapeY(heldShape.type().starterY());
							QueueContext qc = new QueueContext(kernel, f, new ShapeType[] {heldShape.type()});
							Decision heldBest = kernel.bestFor(qc);
							if(heldBest.score < best.score) {
								List<PlayerAction> hp = new ArrayList<PlayerAction>(Arrays.asList(new PlayerAction(field, PlayerActionType.HOLD)));
								hp.addAll(heldBest.bestPath);
								heldBest.bestPath = hp;
								best = heldBest;
							}
						}
					}
					
				} else {
					// best for the current shape
					ShapeType[] types = new ShapeType[lookahead + 1];
					types[0] = shape.type();
					for(int i = 1; i < types.length; i++) {
						types[i] = TNPiece.fromNullpo(engine.getNextID(engine.nextPieceCount + i));
					}
					QueueContext qc = new QueueContext(kernel, field, types);
					best = kernel.bestFor(qc);

					// best for the hold shape
					if(
							!skipHold 
							&& computeHold 
							&& !engine.holdDisable 
							&& engine.holdPieceObject != null 
							&& engine.isHoldOK()) {
						computeHold = false;
						Shape heldShape = TNPiece.fromNullpo(engine.holdPieceObject);
						if(heldShape.type() != shape.type()) {
							Field f = field.copy();
							f.setShape(heldShape);
							f.setShapeX(heldShape.type().starterX());
							f.setShapeY(heldShape.type().starterY());
							types = Arrays.copyOf(types, types.length);
							types[0] = heldShape.type();
							qc = new QueueContext(kernel, f, types);
							Decision heldBest = kernel.bestFor(qc);
							if(heldBest.score < best.score) {
								List<PlayerAction> hp = new ArrayList<PlayerAction>(Arrays.asList(new PlayerAction(field, PlayerActionType.HOLD)));
								hp.addAll(heldBest.bestPath);
//								return hp;
								heldBest.bestPath = hp;
								best = heldBest;
							}
						}
					}
				}
				
//				synchronized(pipeline) {
//					pipeline.clear();
//					Decision d = best.deeper;
//					while(d.bestPath != null) {
////						System.out.println("Pipelining computation " + d.bestPath);
//						pipeline.add(d.bestPath);
//						if(d.deeper == null || d.deeper == d)
//							break;
//						d = d.deeper;
//					}
//				}
				
				return best.bestPath;
			}
		};

		if(engine.aiUseThread) {
			futureActions = POOL.submit(task);
		} else {
			FutureTask<List<PlayerAction>> ft = new FutureTask<List<PlayerAction>>(task);
			ft.run();
			futureActions = ft;
		}

		recomputes++;
	}

	@Override
	public void newPiece(GameEngine engine, int playerID) {
		misdrop = null;
		if(!swapping || pressed) {
			recomputes = 0;
			computeHold = true;
			swapping = false;
			recompute(engine);
		}
	}

	@Override
	public void onFirst(GameEngine engine, int playerID) {
	}

	@Override
	public void onLast(GameEngine engine, int playerID) {
	}

	@Override
	public void setControl(GameEngine engine, int playerID, Controller ctrl) {
		if((engine.nowPieceObject != null) && (engine.stat == GameEngine.Status.MOVE) && (engine.statc[0] > 0))
			;
		else
			return;

		//		if(buttonId != null) {
		//			ctrl.setButtonPressed(buttonId);
		//			buttonId = null;
		//			return;
		//		}

		//		pressed = false;

		if(buttonId != null) {
			buttonId = null;
			return;
		}

		if(actions() == null)
			return;

		//		if((!pressed || ctrl.buttonPress[Controller.BUTTON_DOWN]) && das <= 0) {
		//			if(engine.nowPieceY == -2)
		//				return;

		swapping = false;
		if(actions().size() == 0) {
			recompute(engine);
			return;
		}

		PlayerAction pa = actions().remove(0);
		
//		if(pa.getStartShape().type() != TNPiece.fromNullpo(engine.nowPieceObject.id)) {
//			synchronized(pipeline) {
//				pipeline.clear();
//				recompute(engine);
//				return;
//			}
//		}

		if(actions().size() == 0 && pa.getType() != PlayerActionType.HARD_DROP && pa.getType() != PlayerActionType.HOLD) {
			actions().add(new PlayerAction(pa.getEndField(), PlayerActionType.HARD_DROP));
		}

		if(pa.getType() != PlayerActionType.HOLD && pa.getType() != PlayerActionType.HARD_DROP) {
			if(pa.getStartX() - Field.BUFFER != engine.nowPieceX || pa.getStartY() - Field.BUFFER != engine.nowPieceY) {
				boolean recompute = false;
				if(pa.getStartX() - Field.BUFFER == engine.nowPieceX && pa.getStartY() - Field.BUFFER > engine.nowPieceY) {
					// We expected the piece to be lower than it is.  Odd, but just put back the current move and make a soft drop.
					// This can happen on the very first move.
					actions().add(0, pa);
					pa = new PlayerAction(field, PlayerActionType.DOWN_ONE);
				} else if(pa.getStartX() - Field.BUFFER == engine.nowPieceX && pa.getStartY() - Field.BUFFER < engine.nowPieceY) {
					// We expected the piece to be higher than it is.
					// This can happen on the very first move, or because of gravity.
					// Discard soft-drop moves until we either catch up or need to recompute
					while(pa.getType() == PlayerActionType.DOWN_ONE && pa.getStartY() - Field.BUFFER < engine.nowPieceY) {
						if(actions().size() == 0) {
							recompute = true;
							break;
						}
						pa = actions().remove(0);
					}
					if(pa.getStartY() - Field.BUFFER != engine.nowPieceY) {
						recompute = true;
//						highGravity = true;
					}
				} else
					recompute = true;
				if(recompute) {
					// FIXME: Why is this possible?  Strange inconsistencies in the kick tables I guess.
					System.out.println("Misdrop");
					System.out.println("Expected shape at (" + (pa.getStartX() - Field.BUFFER) + "," + (pa.getStartY() - Field.BUFFER) + " but found at (" + engine.nowPieceX + "," + engine.nowPieceY + ")");
					if(recomputes > 1) {// 1 recompute is the initial computation
						//						System.out.println("Strange inconsistency in actions.  Recomputing.");
						misdrop = "RECOMPUTE";
						synchronized(pipeline) {
							pipeline.clear();
						}
					}
					if(recomputes <= MAX_RECOMPUTES) {
						recompute(engine);
						return;
					} else
						misdrop = "GIVE UP";
				}
			}
		} else
			swapping = true;

//		if(pa.getType() == Type.ROTATE_LEFT && actions().size() >= 2) {
//			if(actions().get(0).getType() == Type.ROTATE_LEFT && actions().get(1).getType() == Type.ROTATE_LEFT) {
//				actions().remove(0);
//				actions().remove(0);
//				pa = new PlayerAction(field, Type.ROTATE_RIGHT);
//			}
//		}

		//		int buttonId;
		buttonId = controllerButtonId(pa.getType());

		//		if(pa.getType() == Type.DAS_LEFT || pa.getType() == Type.DAS_RIGHT)
		//			das = 1;

		boolean dropOnly = buttonId == Controller.BUTTON_DOWN;
		if(dropOnly) {
			for(PlayerAction npa : actions()) {
				if(npa.getType() != PlayerActionType.DOWN_ONE && npa.getType() != PlayerActionType.HARD_DROP)
					dropOnly = false;
			}
		}

		if(dropOnly)
			buttonId = Controller.BUTTON_UP;

		//		if(ctrl.getButtonBit() == 0) {
//		if(buttonId != Controller.BUTTON_DOWN || misdrop == null)
			ctrl.setButtonPressed(buttonId);
		//		} else {
		//			if(ctrl.isPress(buttonId)) {
		//				ctrl.clearButtonState();
		//				return;
		//			}
		//			ctrl.clearButtonState();
		//			if(buttonId != Controller.BUTTON_DOWN || misdrop == null)
		//				ctrl.setButtonPressed(buttonId);
		//		}

		//		pressed = true;
		//		buttonId = null;

//		if(actions().size() == 0)
//			recompute(engine);

	}

	@Override
	public void shutdown(GameEngine engine, int playerID) {
	}

	@Override
	public void renderState(GameEngine engine, int playerID) {
		super.renderState(engine, playerID);
		if(misdrop != null)
			engine.getOwner().receiver.drawScoreFont(engine, playerID, 0, 16, misdrop);
	}

}
