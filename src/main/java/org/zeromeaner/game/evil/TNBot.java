package org.zeromeaner.game.evil;

import java.util.List;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eviline.AIKernel;
import org.eviline.AIKernel.Decision;
import org.eviline.AIKernel.QueueContext;
import org.eviline.Field;
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
		}
		throw new InternalError("switch fallthrough when all cases covered");
	}

	private static int MAX_RECOMPUTES = 20;
	
	private static ExecutorService POOL = Executors.newCachedThreadPool();

	private TNField field;
	private List<PlayerAction> actions;
	private Exchanger<Decision> nextActions;
	private boolean pressed = false;
	
	private int recomputes = 0;
	private String misdrop = null;
	
	@Override
	public String getName() {
		return "Eviline AI";
	}

	@Override
	public void init(GameEngine engine, int playerID) {
		field = new TNField(engine);
		engine.aiShowHint = false;
	}
	
	protected void recompute(GameEngine engine) {
		if(recomputes > MAX_RECOMPUTES)
			return;
		field.update();
		Shape shape = TNPiece.fromNullpo(engine.nowPieceObject);

		field.setShape(shape);
		field.setShapeX(engine.nowPieceX + Field.BUFFER);
		field.setShapeY(engine.nowPieceY + Field.BUFFER);

		if(engine.nextPieceArraySize == 1) {
			Decision best = AIKernel.getInstance().bestFor(field);
			actions = best.bestPath;
		} else /* if(engine.nextPieceArraySize == 2) */ {
			Shape next = TNPiece.fromNullpo(engine.getNextObject(engine.nextPieceCount + 1));
			QueueContext qc = new QueueContext(field, new ShapeType[] {shape.type(), next.type()});
			Decision best = AIKernel.getInstance().bestFor(qc);
			actions = best.bestPath;
		} /*else {
			Shape next = TNPiece.fromNullpo(engine.getNextObject(engine.nextPieceCount + 1));
			final Shape third = TNPiece.fromNullpo(engine.getNextObject(engine.nextPieceCount + 2));
			QueueContext qc = new QueueContext(field, new ShapeType[] {shape.type(), next.type()});
			final Decision best = AIKernel.getInstance().bestFor(qc);
			actions = best.bestPath;
			final Exchanger<Decision> nextActions = new Exchanger<AIKernel.Decision>();
			this.nextActions = nextActions;
			Runnable task = new Runnable() {
				@Override
				public void run() {
					Field f = best.field.copy();
//					ShapeType type = best.deeper.type;
					f.setShape(third.type().starter());
					f.setShapeX(Field.WIDTH / 2 + Field.BUFFER - 2 + third.type().starterX());
					f.setShapeY(third.type().starterY());
					Decision nextBest = AIKernel.getInstance().bestFor(f);
					try {
						nextActions.exchange(nextBest, 3, TimeUnit.SECONDS);
					} catch(InterruptedException ie) {
					} catch(TimeoutException te) {
					}
				}
			};
			if(recomputes == 0)
				POOL.execute(task);
		}
		 */
		
		actions.add(new PlayerAction(field, Type.DOWN_ONE));
		
		if(recomputes != 0)
			nextActions = null;
		recomputes++;
	}

	@Override
	public void newPiece(GameEngine engine, int playerID) {
		recomputes = 0;
		misdrop = null;
		/*
		if(nextActions != null) {
			Decision d = null;
			try {
				d = nextActions.exchange(null, 250, TimeUnit.MILLISECONDS);
			} catch(InterruptedException ie) {
			} catch(TimeoutException te) {
			}
			nextActions = null;
			if(d != null) {
				final Decision best = d;
				final Exchanger<Decision> nextActions = new Exchanger<AIKernel.Decision>();
				this.nextActions = nextActions;
				final ShapeType third = TNPiece.fromNullpo(engine.getNextObject(engine.nextPieceCount + 2)).type();
				Runnable task = new Runnable() {
					@Override
					public void run() {
						Field f = best.field.copy();
//						ShapeType type = best.deeper.type;
						f.setShape(third.starter());
						f.setShapeX(Field.WIDTH / 2 + Field.BUFFER - 2 + third.starterX());
						f.setShapeY(third.starterY());
						Decision nextBest = AIKernel.getInstance().bestFor(f);
						try {
							nextActions.exchange(nextBest, 3, TimeUnit.SECONDS);
						} catch(InterruptedException ie) {
						} catch(TimeoutException te) {
						}
					}
				};
				POOL.execute(task);
			}
		} else
		*/
			recompute(engine);
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
		
		if(actions == null)
			return;
		if(actions.size() == 0) {
			recompute(engine);
		}
		if(actions.size() == 0)
			return;

		if(!pressed) {
			if(engine.nowPieceY == -2)
				return;
			
//			if(requiredY != -1 && engine.nowPieceY > requiredY) {
//				boolean recompute = false;
//				for(int i = requiredY; i < engine.nowPieceY; i++) {
//					if(actions.size() == 0 || actions.remove(0).getType() != Type.DOWN_ONE) {
//						recompute = true;
//						break;
//					}
//				}
//				if(recompute)
//					recompute(engine);
//				
//				if(actions.size() == 0)
//					return;
//			}
			
			PlayerAction pa = actions.remove(0);
			
			if(pa.getStartX() - Field.BUFFER != engine.nowPieceX || pa.getStartY() - Field.BUFFER != engine.nowPieceY) {
				boolean recompute = false;
				if(pa.getStartX() - Field.BUFFER == engine.nowPieceX && pa.getStartY() - Field.BUFFER > engine.nowPieceY) {
					// We expected the piece to be lower than it is.  Odd, but just put back the current move and make a soft drop.
					// This can happen on the very first move.
					actions.add(0, pa);
					pa = new PlayerAction(field, Type.DOWN_ONE);
				} else if(pa.getStartX() - Field.BUFFER == engine.nowPieceX && pa.getStartY() - Field.BUFFER < engine.nowPieceY) {
					// We expected the piece to be higher than it is.
					// This can happen on the very first move, or because of gravity.
					// Discard soft-drop moves until we either catch up or need to recompute
					while(pa.getType() == Type.DOWN_ONE && pa.getStartY() - Field.BUFFER < engine.nowPieceY) {
						if(actions.size() == 0) {
							recompute = true;
							break;
						}
						pa = actions.remove(0);
					}
					if(pa.getStartY() - Field.BUFFER != engine.nowPieceY)
						recompute = true;
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
						if(actions.size() == 0)
							return;
						pa = actions.remove(0);
					} else
						misdrop = "GIVE UP";
				}
			}
			
			int buttonId;
			buttonId = controllerButtonId(pa.getType());
			
			boolean dropOnly = buttonId == Controller.BUTTON_DOWN;
			if(dropOnly) {
				for(PlayerAction npa : actions) {
					if(npa.getType() != Type.DOWN_ONE)
						dropOnly = false;
				}
			}
			
			if(dropOnly)
				buttonId = Controller.BUTTON_UP;
			
			ctrl.setButtonPressed(buttonId);

//			if(buttonId == Controller.BUTTON_DOWN && !dropOnly) {
//				requiredY = engine.nowPieceY + 1;
//			} else
//				requiredY = -1;
			
			pressed = true;
		} else {
			ctrl.clearButtonState();
			pressed = false;
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
