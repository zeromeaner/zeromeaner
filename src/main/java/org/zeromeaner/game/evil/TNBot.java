package org.zeromeaner.game.evil;

import java.util.List;

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

	private TNField field;
	private List<PlayerAction> actions;
	private boolean pressed = false;
	
	private int requiredY = -1;
	
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
		field.update();
		Shape shape = TNPiece.fromNullpo(engine.nowPieceObject);


		//		if(engine.nextPieceArraySize == 1) {
		field.setShape(shape);
		field.setShapeX(engine.nowPieceX + Field.BUFFER);
		field.setShapeY(engine.nowPieceY + Field.BUFFER);

		Decision best = AIKernel.getInstance().bestFor(field);
		actions = best.bestPath;
		//		} else {
		//			Shape next = TNPiece.fromNullpo(engine.getNextObject(engine.nextPieceCount + 1));
		//			QueueContext qc = new QueueContext(field, new ShapeType[] {shape.type(), next.type()});
		//			Decision best = AIKernel.getInstance().bestFor(qc);
		//			actions = best.bestPath;
		//		}
		requiredY = -1;
	}

	@Override
	public void newPiece(GameEngine engine, int playerID) {
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
			
			if(requiredY != -1 && engine.nowPieceY < requiredY)
				return;
			if(requiredY != -1 && engine.nowPieceY > requiredY)
				recompute(engine);
			
			PlayerAction pa = actions.remove(0);
			
			if(pa.getStartX() - Field.BUFFER != engine.nowPieceX || pa.getStartY() - Field.BUFFER != engine.nowPieceY) {
				// FIXME: Why is this possible?  Strange inconsistencies in the kick tables I guess.
				System.out.println("Strange inconsistency in actions.  Recomputing.");
				recompute(engine);
				if(actions.size() == 0)
					return;
				pa = actions.remove(0);
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
			
			if(buttonId == Controller.BUTTON_DOWN && !dropOnly) {
				requiredY = engine.nowPieceY + 1;
			} else {
				requiredY = -1;
			
				ctrl.setButtonPressed(buttonId);
				pressed = true;
			}
		} else {
			ctrl.clearButtonState();
			pressed = false;
		}
	}

	@Override
	public void shutdown(GameEngine engine, int playerID) {
	}

}
