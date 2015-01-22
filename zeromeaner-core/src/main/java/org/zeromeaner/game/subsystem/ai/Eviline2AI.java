package org.zeromeaner.game.subsystem.ai;

import java.awt.GridLayout;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.eviline.core.Command;
import org.eviline.core.Configuration;
import org.eviline.core.Engine;
import org.eviline.core.ShapeType;
import org.eviline.core.XYShapes;
import org.eviline.core.ai.AIPlayer;
import org.eviline.core.ai.CommandGraph;
import org.eviline.core.ai.DefaultAIKernel;
import org.eviline.core.ai.DefaultAIKernel.Best;
import org.eviline.core.ai.Fitness;
import org.eviline.core.ai.NextFitness;
import org.eviline.core.conc.SubtaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromeaner.game.component.Controller;
import org.zeromeaner.game.eviline.EngineAdapter;
import org.zeromeaner.game.eviline.FieldAdapter;
import org.zeromeaner.game.eviline.XYShapeAdapter;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.gui.common.Configurable;
import org.zeromeaner.util.CustomProperties;
import org.zeromeaner.util.Options;
import org.zeromeaner.util.PropertyConstant;
import org.zeromeaner.util.PropertyConstant.Constant;
import org.zeromeaner.util.PropertyConstant.ConstantParser;

public class Eviline2AI extends AbstractAI implements Configurable {
	private static final Logger log = LoggerFactory.getLogger(Eviline2AI.class);

	protected static final Constant<Boolean> DROPS_ONLY = new Constant<>(PropertyConstant.BOOLEAN, ".eviline.drops_only", true);
	protected static final Constant<Integer> LOOKAHEAD = new Constant<>(PropertyConstant.INTEGER, ".eviline.lookahead", 3);
	protected static final Constant<Integer> PRUNE_TOP = new Constant<>(PropertyConstant.INTEGER, ".eviline.prune_top", 5);
	protected static final Constant<Integer> MAX_THREADS = new Constant<>(PropertyConstant.INTEGER, ".eviline.max_threads", 64);
	protected static final Constant<String> FITNESS = new Constant<String>(PropertyConstant.STRING, ".eviline.fitness", "NextFitness");
	protected static final Constant<Integer> PIPELINE_LENGTH = new Constant<>(PropertyConstant.INTEGER, ".eviline.pipeline_length", 3);
	
	protected static class EvilineAIConfigurator implements Configurable.Configurator {

		private JCheckBox dropsOnly;
		private JSpinner lookahead;
		private JSpinner pruneTop;
		private JSpinner maxThreads;
		private JSpinner pipelineLength;
		private JComboBox<String> fitness;
		private JPanel panel;

		public EvilineAIConfigurator() {
			dropsOnly = new JCheckBox();
			lookahead = new JSpinner(new SpinnerNumberModel(3, 0, 6, 1));
			pruneTop = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
			maxThreads = new JSpinner(new SpinnerNumberModel(8, 1, 128, 1));
			pipelineLength = new JSpinner(new SpinnerNumberModel(3, 0, 6, 1));
			fitness = new JComboBox<>(new String[] {"DefaultFitness", "NextFitness", "ScoreFitness"});
			panel = new JPanel(new GridLayout(0, 2));
			panel.add(new JLabel("Maximum Lookahead: "));
			panel.add(lookahead);
			panel.add(new JLabel("Lookahead Choices: "));
			panel.add(pruneTop);
			panel.add(new JLabel("Max AI threads: "));
			panel.add(maxThreads);
			panel.add(new JLabel("Only use drops: "));
			panel.add(dropsOnly);
			panel.add(new JLabel("Pipeline length: "));
			panel.add(pipelineLength);
			panel.add(new JLabel("Fitness function: "));
			panel.add(fitness);
		}

		@Override
		public JComponent getConfigurationComponent() {
			return panel;
		}

		@Override
		public void applyConfiguration(CustomProperties p) {
			LOOKAHEAD.set(p, (Integer) lookahead.getValue());
			PRUNE_TOP.set(p, (Integer) pruneTop.getValue());
			DROPS_ONLY.set(p, dropsOnly.isSelected());
			MAX_THREADS.set(p, (Integer) maxThreads.getValue());
			FITNESS.set(p, (String) fitness.getSelectedItem());
			PIPELINE_LENGTH.set(p, (Integer) pipelineLength.getValue());
		}

		@Override
		public void reloadConfiguration(CustomProperties p) {
			lookahead.setValue(LOOKAHEAD.value(p));
			pruneTop.setValue(PRUNE_TOP.value(p));
			dropsOnly.setSelected(DROPS_ONLY.value(p));
			maxThreads.setValue(MAX_THREADS.value(p));
			fitness.setSelectedItem(FITNESS.value(p));
			pipelineLength.setValue(PIPELINE_LENGTH.value(p));
		}

	}

	private static EvilineAIConfigurator configurator = new EvilineAIConfigurator();

	protected static final 
	ThreadPoolExecutor
//	ExecutorService
	POOL =
//			Executors.newCachedThreadPool();
			new ThreadPoolExecutor(
			1, 1, 
			10, TimeUnit.SECONDS, 
			new SynchronousQueue<Runnable>(),
			new ThreadPoolExecutor.DiscardPolicy());

	protected static final ExecutorService pipelineExecutor = Executors.newSingleThreadExecutor();
	
	protected class PathPipeline {
		public LinkedBlockingDeque<PathTask> pipe = new LinkedBlockingDeque<PathTask>();

		public PathPipeline() {
			log.trace("Created new pipeline " + this);
		}

		public synchronized boolean extend(final GameEngine gameEngine) {
			discardUntil(gameEngine);
			if(pipe.size() == 0) {
				log.trace("Extending empty pipeline");
				int xyshape = XYShapeAdapter.toXYShape(gameEngine);
				if(xyshape == -1)
					return false;
				FieldAdapter f = new FieldAdapter();
				f.update(gameEngine.field);
				final PathTask pt = new PathTask(
						gameEngine,
						this,
						gameEngine.nextPieceCount - 1,
						f,
						xyshape,
						createGameNext(gameEngine, gameEngine.nextPieceCount));
				pipe.offerLast(pt);
				pipelineExecutor.execute(new Runnable() {
					@Override
					public void run() {
						pt.task.run();
						pipelineExecutor.execute(new Runnable() {
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
			if(tail == null)
				return false;
			if(pipe.size() >= pipeLength || !tail.task.isDone())
				return false;
			final PathTask pt = tail.extend(gameEngine);
			log.trace("Extended pipeline to " + pt.seq);
			if(pt == null)
				return false;
			pipe.offerLast(pt);
			pipelineExecutor.execute(new Runnable() {
				@Override
				public void run() {
					pt.task.run();
					pipelineExecutor.execute(new Runnable() {
						@Override
						public void run() {
							extend(gameEngine);
						}
					});
				}
			});
			return true;
		}

		public synchronized PathTask discardUntil(GameEngine engine) {
			PathTask pt;
			for(pt = pipe.peekFirst(); pt != null && pt.seq < engine.nextPieceCount - 1; pt = pipe.peekFirst())
				pipe.pollFirst();
			return pt;
		}

		public synchronized byte[] currentPath(GameEngine engine) {
			PathTask pt = discardUntil(engine);
			if(pt == null) {
				extend(engine);
				pt = discardUntil(engine);
			}
			if(pt == null || !pt.task.isDone())
				return null;
			return pt.path;
		}

		public synchronized int desiredXYShape(GameEngine engine) {
			PathTask pt = discardUntil(engine);
			if(pt == null) {
				extend(engine);
				pt = discardUntil(engine);
			}
			if(pt == null || !pt.task.isDone())
				return -1;
			return pt.xydest;
		}

		public synchronized org.eviline.core.Field expectedField(GameEngine engine) {
			extend(engine);
			PathTask pt = discardUntil(engine);
			if(pt == null)
				return null;
			return pt.field;
		}

		public synchronized boolean isDirty(GameEngine engine) {
			if(engine.lockDelayNow > 0)
				return false;
			extend(engine);
			PathTask pt = discardUntil(engine);
			if(pt == null)
				return false;
			org.eviline.core.Field expected1 = pt.field;
			org.eviline.core.Field expected2 = pt.after;
			if(expected1 == null || expected2 == null)
				return false;
			FieldAdapter f1 = new FieldAdapter();
			f1.copyFrom(expected1);
			FieldAdapter f2 = new FieldAdapter();
			f2.copyFrom(expected2);
			boolean dirty = f1.update(engine.field) && f2.update(engine.field);
//			if(dirty)
//				System.out.println("dirty field");
			if(dirty)
				log.trace("dirty field");
			return dirty;
		}

		public synchronized void shutdown() {
			log.trace("shutting down pipeline " + this);
			PathTask pt = pipe.peekLast();
			if(pt != null)
				pt.task.cancel(true);
		}
	}

	protected class PathTask {
		public PathPipeline pipeline;
		public int seq;
		public org.eviline.core.Field field;
		public org.eviline.core.Field after;
		public int xystart;
		public int xydest;
		public ShapeType[] next;
		public FutureTask<Boolean> task;
		public byte[] path;

		public PathTask(final GameEngine gameEngine, final PathPipeline pipeline, int seq, org.eviline.core.Field field, int xystart, ShapeType[] next) {
			this.pipeline = pipeline;
			this.seq = seq;
			this.field = field;
			this.xystart = xystart;
			this.next = next;
			task = new FutureTask<>(new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					Engine engine = new Engine(PathTask.this.field, new Configuration(null, 0));
					engine.setNext(PathTask.this.next);
					engine.setShape(PathTask.this.xystart);
					AIPlayer player = new AIPlayer(ai, engine, lookahead);
					player.tick();
					Best best = player.getBest();
					path = createCommandPath(best.graph);
					after = best.after;
					xydest = player.getDest();
					pipeline.extend(gameEngine);
					return true;
				}
			});
		}


		public Boolean get() {
			try {
				return task.get();
			} catch(Exception e) {
				return null;
			}
		}

		public PathTask extend(GameEngine gameEngine) {
			for(PathTask existing : pipeline.pipe) {
				if(existing.seq == gameEngine.nextPieceCount - 1)
					return existing;
			}
			if(pipeline.pipe.size() >= pipeLength)
				return null;
			ShapeType[] extnext = createGameNext(gameEngine, seq);
			Boolean best = get();
			if(best == null) {
				flushPipeline(gameEngine);
				return null;
			}
			ShapeType en = extnext[0];
			return new PathTask(
					gameEngine,
					pipeline,
					seq+1,
					after,
					XYShapes.toXYShape(
							en.startX() + gameEngine.ruleopt.pieceOffsetX[en.ordinal()][0], 
							en.startY() + gameEngine.ruleopt.pieceOffsetY[en.ordinal()][0], 
							extnext[0].start()),
					Arrays.copyOfRange(extnext, 1, extnext.length));
		}
	}

	protected SubtaskExecutor subtasks;
	
	protected DefaultAIKernel ai;

	protected Command shifting = null;

	protected int lookahead = 3;

	protected PathPipeline pipeline;

	protected int pipeLength = 1;

	protected byte[] createCommandPath(CommandGraph g) {
		byte[] computingPaths = new byte[XYShapes.SHAPE_MAX];
		Arrays.fill(computingPaths, (byte) -1);
		int xyshape = g.getSelectedShape();

		boolean tail = true;

		computingPaths[xyshape] = (byte) Command.HARD_DROP.ordinal();
		while(xyshape != CommandGraph.NULL_ORIGIN) {
			int parent = CommandGraph.originOf(g.getVertices(), xyshape);
			Command c = CommandGraph.commandOf(g.getVertices(), xyshape);
			if(tail && c == Command.SOFT_DROP)
				c = Command.HARD_DROP;
			tail = false;
			if(parent >= 0 && parent < XYShapes.SHAPE_MAX)
				computingPaths[parent] = (byte) c.ordinal();
			if(c == Command.SOFT_DROP || c == Command.HARD_DROP) {
				xyshape = XYShapes.shiftedUp(xyshape);
				while(xyshape != parent) {
					if(xyshape >= 0 && xyshape < XYShapes.SHAPE_MAX)
						computingPaths[xyshape] = (byte) c.ordinal();
					xyshape = XYShapes.shiftedUp(xyshape);
				}
			}
			xyshape = parent;
		}

		return computingPaths;
	}

	protected ShapeType[] createGameNext(GameEngine engine, int seq) {
//		if(seq < engine.nextPieceCount - 1 || seq >= engine.nextPieceCount + engine.nextPieceArraySize - lookahead - 1)
//			return null;
		int size = Math.min(engine.nextPieceArraySize - (seq % engine.nextPieceArraySize), lookahead + pipeLength + 2);
		ShapeType[] nextShapes = new ShapeType[size];
		for(int i = 0; i < size; i++)
			nextShapes[i] = XYShapeAdapter.toShapeType(engine.getNextObject(seq + i));
		return nextShapes;
	}

	public Eviline2AI() {}



	protected void flushPipeline(GameEngine engine) {
//		System.out.println("resetting pipeline");
		log.trace("flushing pipeline");
		
		pipeline.shutdown();
		pipeline = new PathPipeline();
		pipeline.extend(engine);
		lastxy = -1;
		
		subtasks.shutdownNow();
		ai.setExec(subtasks = new SubtaskExecutor(POOL));
	}

	@Override
	public String getName() {
		return "eviline2";
	}

	@Override
	public void init(GameEngine engine, int playerID) {
		log.info("Initializing Eviline2 AI");
		CustomProperties opt = Options.player(playerID).ai.BACKING;

		int threads = MAX_THREADS.value(opt);
		POOL.setMaximumPoolSize(threads);

		Fitness fitness;
		try {
			fitness = Class.forName("org.eviline.core.ai." + FITNESS.value(opt)).asSubclass(Fitness.class).newInstance();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}

		pipeLength = PIPELINE_LENGTH.value(opt);
		
		subtasks = new SubtaskExecutor(POOL);
		ai = new DefaultAIKernel(subtasks, fitness);
		ai.setDropsOnly(DROPS_ONLY.value(opt));
		ai.setPruneTop(PRUNE_TOP.value(opt));
		pipeline = new PathPipeline();
		lookahead = LOOKAHEAD.value(opt);
	}

	@Override
	public void shutdown(GameEngine engine, int playerID) {
		log.info("Shutting down Eviline2 AI");
		if(pipeline != null)
			pipeline.shutdown();
	}

	protected int lastxy = -1;

	@Override
	public void setControl(GameEngine engine, int playerID, Controller ctrl) {
		try {
			int xyshape = XYShapeAdapter.toXYShape(engine);
			int input = 0;

			if(xyshape == -1) {
				ctrl.setButtonBit(input);
				return;
			}
			
			pipeline.extend(engine);

			if(shifting != null) {
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
				if(pipeline.isDirty(engine)) {
					log.trace("dirty pipeline, needs flushing");
					flushPipeline(engine);
				}
				ctrl.setButtonBit(input);
				return;
			}

			Command c = Command.fromOrdinal(paths[xyshape]);
			if(c == null && lastxy != xyshape && lastxy >= 0|| pipeline.isDirty(engine)) {
				log.trace("dirty pipeline, needs flushing");
				ctrl.setButtonBit(input);
				flushPipeline(engine);
				return;
			} else if(xyshape != lastxy && lastxy >= 0)
				paths[lastxy] = -1;
			lastxy = xyshape;
			
			if(pipeline.desiredXYShape(engine) == xyshape)
				c = Command.HARD_DROP;

			if(c != null) {
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
		lastxy = -1;
	}

	@Override
	public void renderHint(GameEngine engine, int playerID) {
		int xyshape = XYShapeAdapter.toXYShape(engine);

		if(xyshape == -1) {
			thinkComplete = false;
			engine.aiHintPiece = null;
			engine.aiHintReady = false;
			return;
		}

		int xydest = pipeline.desiredXYShape(engine);

		if(xydest == -1)
			return;
		
		if(pipeline.isDirty(engine)) {
			flushPipeline(engine);
			thinkComplete = false;
			engine.aiHintPiece = null;
			engine.aiHintReady = false;
			return;
		}

		engine.aiHintPiece = XYShapeAdapter.fromXYShape(xydest);
		bestX = XYShapes.xFromInt(xydest);
		bestY = XYShapes.yFromInt(xydest);
		bestHold = false;
		bestRt = XYShapes.shapeIdFromInt(xydest) & 0x3;
		engine.aiHintReady = true;
		thinkComplete = true;
	}

	@Override
	public Configurator getConfigurator() {
		return configurator;
	}

}
