package org.zeromeaner.game.evil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eviline.AIKernel;
import org.eviline.AIKernel.Decision;
import org.eviline.AIKernel.QueueContext;
import org.eviline.ElTetrisFitness;
import org.eviline.Field;
import org.eviline.Fitness;
import org.eviline.PlayerAction;
import org.eviline.PlayerAction.Type;
import org.eviline.Shape;
import org.eviline.ShapeType;
import org.zeromeaner.game.component.Controller;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.game.subsystem.ai.AbstractAI;
import org.zeromeaner.game.subsystem.mode.DigRaceMode;
import org.zeromeaner.game.subsystem.mode.LineRaceMode;

public class TNBot extends AbstractAI {

	public static int controllerButtonId(PlayerAction.Type paType) {
		switch(paType) {
		case DOWN_ONE:
			return Controller.BUTTON_DOWN;
		case ROTATE_LEFT:
			return Controller.BUTTON_A;
		case ROTATE_RIGHT:
			return Controller.BUTTON_B;
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
			skipLookahead = true;
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
			double[] fp = kernel.getFitness().getParams();
			fp[Fitness.Weights.TRANSITION_EXP] += 2;
			fp[Fitness.Weights.IMPOSSIBLE_POWER] += 2;
			fp[Fitness.Weights.SMOOTHNESS_MULT] *= 3;
		}
	}
	
	protected static int MAX_RECOMPUTES = 5;

	protected static ExecutorService POOL = Executors.newCachedThreadPool();

	protected TNField field;
	protected AIKernel kernel = new AIKernel();
	protected boolean pressed = false;

	protected Future<List<PlayerAction>> futureActions;

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

		Callable<List<PlayerAction>> task = new Callable<List<PlayerAction>>() {
			@Override
			public List<PlayerAction> call() throws Exception {
				if(!skipHold && !held && engine.isHoldOK()) {
					held = true;
					return new ArrayList<PlayerAction>(Arrays.asList(new PlayerAction(field, Type.HOLD)));
				}
				
				//				AIKernel kernel = new AIKernel();
				kernel.setHighGravity(engine.statistics.level >= 10 || highGravity);

				double currentScore = kernel.getFitness().score(field);
				worst = Math.max(worst * 0.9, currentScore);
				
				if(engine.nextPieceArraySize <= lookahead /*|| kernel.isHighGravity()*/ || skipLookahead) {
					// best for the current shape
					Decision best = kernel.bestFor(field);

					// best for the hold shape
					if(
							!skipHold 
							&& computeHold
							&& best.score >= worst * 0.8
							&& !engine.holdDisable 
							&& engine.holdPieceObject != null 
							&& engine.isHoldOK()) {
						computeHold = false;
						Shape heldShape = TNPiece.fromNullpo(engine.holdPieceObject);
						if(heldShape.type() != shape.type()) {
							Field f = field.copy();
							f.setShape(heldShape);
//							f.setShapeX(Field.BUFFER + Field.WIDTH / 2 - 2 + heldShape.type().starterX());
//							f.setShapeX(Field.BUFFER + engine.getSpawnPosX(engine.field, engine.holdPieceObject));
//							f.setShapeX(0 + (Field.WIDTH + Field.BUFFER * 2 - heldShape.width() + 0) / 2);
							f.setShapeX(heldShape.type().starterX());
							f.setShapeY(heldShape.type().starterY());
							QueueContext qc = kernel.new QueueContext(f, new ShapeType[] {heldShape.type()});
							Decision heldBest = kernel.bestFor(qc);
							if(heldBest.score < best.score) {
								List<PlayerAction> hp = new ArrayList<PlayerAction>(Arrays.asList(new PlayerAction(field, Type.HOLD)));
								hp.addAll(heldBest.bestPath);
//								hp.add(new PlayerAction(hp.get(hp.size() - 1).getEndField(), Type.HARD_DROP));
								return hp;
							}
						}
					}
//					best.bestPath.add(new PlayerAction(best.bestPath.get(best.bestPath.size() - 1).getEndField(), Type.HARD_DROP));
					return best.bestPath;
				} else {
					// best for the current shape
					ShapeType[] types = new ShapeType[lookahead + 1];
					types[0] = shape.type();
					for(int i = 1; i < types.length; i++) {
						types[i] = TNPiece.fromNullpo(engine.getNextID(engine.nextPieceCount + i));
					}
					QueueContext qc = kernel.new QueueContext(field, types);
					Decision best = kernel.bestFor(qc);

					// best for the hold shape
					if(
							!skipHold 
							&& computeHold 
							&& best.score >= worst * 0.8
							&& !engine.holdDisable 
							&& engine.holdPieceObject != null 
							&& engine.isHoldOK()) {
						computeHold = false;
						Shape heldShape = TNPiece.fromNullpo(engine.holdPieceObject);
						if(heldShape.type() != shape.type()) {
							Field f = field.copy();
							f.setShape(heldShape);
//							f.setShapeX(Field.BUFFER + Field.WIDTH / 2 - 2 + heldShape.type().starterX());
//							f.setShapeX(Field.BUFFER + engine.getSpawnPosX(engine.field, engine.holdPieceObject));
//							f.setShapeX(-0 + (Field.WIDTH + 2 * Field.BUFFER - heldShape.width() + 0) / 2);
							f.setShapeX(heldShape.type().starterX());
							f.setShapeY(heldShape.type().starterY());
							types = Arrays.copyOf(types, types.length);
							types[0] = heldShape.type();
							qc = kernel.new QueueContext(f, types);
							Decision heldBest = kernel.bestFor(qc);
							if(heldBest.score < best.score) {
								List<PlayerAction> hp = new ArrayList<PlayerAction>(Arrays.asList(new PlayerAction(field, Type.HOLD)));
								hp.addAll(heldBest.bestPath);
//								hp.add(new PlayerAction(hp.get(hp.size() - 1).getEndField(), Type.HARD_DROP));
								return hp;
							}
						}
					}
//					best.bestPath.add(new PlayerAction(best.bestPath.get(best.bestPath.size() - 1).getEndField(), Type.HARD_DROP));
					return best.bestPath;
				}
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
		if((engine.nowPieceObject != null) && (engine.stat == GameEngine.STAT_MOVE) && (engine.statc[0] > 0))
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

		if(actions().size() == 0 && pa.getType() != Type.HARD_DROP && pa.getType() != Type.HOLD) {
			actions().add(new PlayerAction(pa.getEndField(), Type.HARD_DROP));
		}

		if(pa.getType() != Type.HOLD && pa.getType() != Type.HARD_DROP) {
			if(pa.getStartX() - Field.BUFFER != engine.nowPieceX || pa.getStartY() - Field.BUFFER != engine.nowPieceY) {
				boolean recompute = false;
				if(pa.getStartX() - Field.BUFFER == engine.nowPieceX && pa.getStartY() - Field.BUFFER > engine.nowPieceY) {
					// We expected the piece to be lower than it is.  Odd, but just put back the current move and make a soft drop.
					// This can happen on the very first move.
					actions().add(0, pa);
					pa = new PlayerAction(field, Type.DOWN_ONE);
				} else if(pa.getStartX() - Field.BUFFER == engine.nowPieceX && pa.getStartY() - Field.BUFFER < engine.nowPieceY) {
					// We expected the piece to be higher than it is.
					// This can happen on the very first move, or because of gravity.
					// Discard soft-drop moves until we either catch up or need to recompute
					while(pa.getType() == Type.DOWN_ONE && pa.getStartY() - Field.BUFFER < engine.nowPieceY) {
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
				if(npa.getType() != Type.DOWN_ONE && npa.getType() != Type.HARD_DROP)
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
