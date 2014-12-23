package org.zeromeaner.game.randomizer;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.eviline.core.Field;
import org.eviline.core.ShapeSource;
import org.eviline.core.ShapeType;
import org.eviline.core.ai.DefaultAIKernel;
import org.eviline.core.ai.NextFitness;
import org.eviline.core.ai.ScoreFitness;
import org.eviline.core.conc.SubtaskExecutor;
import org.eviline.core.ss.Bag7NShapeSource;
import org.zeromeaner.game.component.Piece;
import org.zeromeaner.game.eviline.EngineAdapter;
import org.zeromeaner.game.eviline.FieldAdapter;
import org.zeromeaner.game.eviline.XYShapeAdapter;
import org.zeromeaner.game.play.GameEngine;

public class Eviline2Randomizer extends Randomizer {
	
	protected static final ExecutorService EXEC = Executors.newFixedThreadPool(
			Math.max(2, Runtime.getRuntime().availableProcessors()-1),
			new ThreadFactory() {
				private ThreadFactory f = Executors.defaultThreadFactory();
				@Override
				public Thread newThread(Runnable r) {
					Thread t = f.newThread(r);
					t.setName(t.getName() + ": " + Eviline2Randomizer.class.getSimpleName());
					return t;
				}
			});
	protected static final SubtaskExecutor subtasks = new SubtaskExecutor(EXEC, 0);
	
	protected GameEngine engine;
	protected EngineAdapter evilEngine;
	protected FieldAdapter evilField;

	protected Bag7NShapeSource shapes;
	protected DefaultAIKernel ai;
	
	protected int count;
	
	public Eviline2Randomizer() {
	}

	public Eviline2Randomizer(boolean[] pieceEnable, long seed) {
		super(pieceEnable, seed);
	}
	
	@Override
	public void init() {
		evilEngine = new EngineAdapter();
		evilField = (FieldAdapter) evilEngine.getField();
		
		shapes = new Bag7NShapeSource(4);
		ai = new DefaultAIKernel(subtasks, new NextFitness());
	}
	
	@Override
	public void setEngine(GameEngine e) {
		engine = e;
		engine.nextPieceArraySize = 1;
		engine.nextPieceArrayID = new int[engine.nextPieceArraySize];
		engine.nextPieceArrayObject = new Piece[engine.nextPieceArraySize];
		count = 0;
	}

	@Override
	public int next() {
		if(count++ == 0) {
			List<Integer> noSZO = Arrays.asList(Piece.PIECE_I, Piece.PIECE_J, Piece.PIECE_L, Piece.PIECE_T);
			int id = noSZO.get((int)(Math.random() * noSZO.size()));
			ShapeType type = XYShapeAdapter.toShapeType(new Piece(id));
			shapes.remove(type);
			return id;
		}
		
		engine.nextPieceArraySize = 1;
		evilEngine.update(engine);
		Field field = evilEngine.getField();
		if(evilEngine.getShape() != -1) {
			DefaultAIKernel.Best best = ai.bestPlacement(field, field, evilEngine.getShape(), ShapeType.NONE, 0, 0);
			field.blit(best.shape, 0);
			field.clearLines();
		}
		ShapeType worst = ai.worstNext(field, shapes, ShapeType.NONE, 3);
		shapes.remove(worst);
		int id = XYShapeAdapter.fromShapeType(worst);
		return id;
	}

}
