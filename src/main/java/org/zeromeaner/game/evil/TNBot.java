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
			return Controller.BUTTON_LEFT;
		case SHIFT_RIGHT:
			return Controller.BUTTON_RIGHT;
		case HOLD:
			return Controller.BUTTON_D;
		case HARD_DROP:
			return Controller.BUTTON_UP;
		}
		throw new InternalError("switch fallthrough when all cases covered");
	}

	private static int MAX_RECOMPUTES = 20;
	
	private static ExecutorService POOL = Executors.newCachedThreadPool();

	private TNField field;
//	private List<PlayerAction> actions;
	private boolean pressed = false;
	
	private Future<List<PlayerAction>> futureActions;
	
	private int recomputes = 0;
	private String misdrop = null;
	
	private boolean held;
	private boolean swapping;
	private boolean computeHold;
	private boolean highGravity = false;
	
	@Override
	public String getName() {
		return "Eviline AI";
	}

	@Override
	public void init(GameEngine engine, int playerID) {
		field = new TNField(engine);
		engine.aiShowHint = false;
		held = false;
		swapping = false;
		highGravity = false;
	}
	
	private List<PlayerAction> actions() {
		if(futureActions == null || futureActions.isCancelled() || !futureActions.isDone())
			return null;
		try {
			return futureActions.get();
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private void recompute(final GameEngine engine) {
		if(recomputes > MAX_RECOMPUTES)
			return;
		
		field.update();
		final Shape shape = TNPiece.fromNullpo(engine.nowPieceObject);
		
		field.setShape(shape);
		field.setShapeX(engine.nowPieceX + Field.BUFFER);
		field.setShapeY(engine.nowPieceY + Field.BUFFER);
		
		Callable<List<PlayerAction>> task = new Callable<List<PlayerAction>>() {
			@Override
			public List<PlayerAction> call() throws Exception {
				
				if(!held && engine.isHoldOK()) {
					held = true;
					return new ArrayList<PlayerAction>(Arrays.asList(new PlayerAction(field, Type.HOLD)));
				}
				
				AIKernel kernel = new AIKernel();
				kernel.setHighGravity(engine.statistics.level >= 10 || highGravity);
				
				kernel.setFitness(new ElTetrisFitness());
				
				if(engine.nextPieceArraySize == 1 /*|| kernel.isHighGravity()*/ ||true) {
					// best for the current shape
					Decision best = kernel.bestFor(field);
					
					// best for the hold shape
					if(computeHold && !engine.holdDisable && engine.holdPieceObject != null && engine.isHoldOK()) {
						computeHold = false;
						Shape heldShape = TNPiece.fromNullpo(engine.holdPieceObject);
						if(heldShape.type() != shape.type()) {
							Field f = field.copy();
							f.setShape(null);
							QueueContext qc = kernel.new QueueContext(f, new ShapeType[] {heldShape.type()});
							Decision heldBest = kernel.bestFor(qc);
							if(heldBest.score < best.score) {
								List<PlayerAction> hp = new ArrayList<PlayerAction>(Arrays.asList(new PlayerAction(field, Type.HOLD)));
								hp.addAll(heldBest.bestPath);
								return hp;
							}
						}
					}
					
					return best.bestPath;
				} else {
					// best for the current shape
					Shape next = TNPiece.fromNullpo(engine.getNextObject(engine.nextPieceCount + 1));
					QueueContext qc = kernel.new QueueContext(field, new ShapeType[] {shape.type(), next.type()});
					Decision best = kernel.bestFor(qc);
					
					// best for the hold shape
					if(computeHold && !engine.holdDisable && engine.holdPieceObject != null && engine.isHoldOK()) {
						computeHold = false;
						Shape heldShape = TNPiece.fromNullpo(engine.holdPieceObject);
						if(heldShape.type() != shape.type()) {
							Field f = field.copy();
							f.setShape(null);
							qc = kernel.new QueueContext(f, new ShapeType[] {heldShape.type(), next.type()});
							Decision heldBest = kernel.bestFor(qc);
							if(heldBest.score < best.score) {
								List<PlayerAction> hp = new ArrayList<PlayerAction>(Arrays.asList(new PlayerAction(field, Type.HOLD)));
								hp.addAll(heldBest.bestPath);
								return hp;
							}
						}
					}
					
					return best.bestPath;
				}
			}
		};
		
		if(engine.aiUseThread)
			futureActions = POOL.submit(task);
		else {
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
		
		if(actions() == null)
			return;

		if(!pressed || ctrl.buttonPress[Controller.BUTTON_DOWN]) {
//			if(engine.nowPieceY == -2)
//				return;
			
			swapping = false;
			if(actions().size() == 0) {
				recompute(engine);
				return;
			}
			
			PlayerAction pa = actions().remove(0);
			
			if(actions().size() == 0 && pa.getType() != Type.HARD_DROP) {
				actions().add(new PlayerAction(field, Type.HARD_DROP));
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
							highGravity = true;
						}
					} else
						recompute = true;
					if(recompute) {
						// FIXME: Why is this possible?  Strange inconsistencies in the kick tables I guess.
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
			
			int buttonId;
			buttonId = controllerButtonId(pa.getType());
			
			boolean dropOnly = buttonId == Controller.BUTTON_DOWN;
			if(dropOnly) {
				for(PlayerAction npa : actions()) {
					if(npa.getType() != Type.DOWN_ONE)
						dropOnly = false;
				}
			}
			
			if(dropOnly)
				buttonId = Controller.BUTTON_UP;
			
			if(ctrl.buttonPress[Controller.BUTTON_DOWN] && buttonId != Controller.BUTTON_DOWN)
				ctrl.clearButtonState();
			
			if(buttonId != Controller.BUTTON_DOWN || misdrop == null)
				ctrl.setButtonPressed(buttonId);

			pressed = true;
		} else {
			ctrl.clearButtonState();
			pressed = false;
			if(actions().size() == 0)
				recompute(engine);
		}
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
