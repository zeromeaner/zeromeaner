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
import java.util.concurrent.ScheduledExecutorService;
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
import org.eviline.core.Field;
import org.eviline.core.ShapeType;
import org.eviline.core.XYShapes;
import org.eviline.core.ai.AIPlayer;
import org.eviline.core.ai.CommandGraph;
import org.eviline.core.ai.DefaultAIKernel;
import org.eviline.core.ai.DefaultAIKernel.Best;
import org.eviline.core.ai.Fitness;
import org.eviline.core.ai.NextFitness;
import org.eviline.core.conc.ActiveCompletor;
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
	private static final ScheduledExecutorService gc = Executors.newSingleThreadScheduledExecutor();
	static {
		Runnable gcTask = new Runnable() {
			@Override
			public void run() {
				System.gc();
			}
		};
		gc.scheduleWithFixedDelay(gcTask, 250, 250, TimeUnit.MILLISECONDS);
	}

	protected static final Constant<Boolean> DROPS_ONLY = new Constant<>(PropertyConstant.BOOLEAN, ".eviline.drops_only", true);
	protected static final Constant<Integer> LOOKAHEAD = new Constant<>(PropertyConstant.INTEGER, ".eviline.lookahead", 3);
	protected static final Constant<Integer> PRUNE_TOP = new Constant<>(PropertyConstant.INTEGER, ".eviline.prune_top", 5);
	protected static final Constant<Integer> MAX_THREADS = new Constant<>(PropertyConstant.INTEGER, ".eviline.max_threads", Runtime.getRuntime().availableProcessors());
	protected static final Constant<Boolean> TWENTY_G = new Constant<>(PropertyConstant.BOOLEAN, ".eviline.20g", false);
	protected static final Constant<String> FITNESS = new Constant<String>(PropertyConstant.STRING, ".eviline.fitness", "NextFitness");
	
	protected static class EvilineAIConfigurator implements Configurable.Configurator {

		private JCheckBox dropsOnly;
		private JSpinner lookahead;
		private JSpinner pruneTop;
		private JSpinner maxThreads;
		private JCheckBox twentyG;
		private JComboBox<String> fitness;
		private JPanel panel;

		public EvilineAIConfigurator() {
			dropsOnly = new JCheckBox();
			lookahead = new JSpinner(new SpinnerNumberModel(3, 0, 6, 1));
			pruneTop = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
			maxThreads = new JSpinner(new SpinnerNumberModel(8, 1, 128, 1));
			twentyG = new JCheckBox();
			fitness = new JComboBox<>(new String[] {"DefaultFitness", "NextFitness", "ScoreFitness", "TwentyGFitness", "DigFitness"});
			panel = new JPanel(new GridLayout(0, 2));
			panel.add(new JLabel("Maximum Lookahead: "));
			panel.add(lookahead);
			panel.add(new JLabel("Lookahead Choices: "));
			panel.add(pruneTop);
			panel.add(new JLabel("Max AI threads: "));
			panel.add(maxThreads);
			panel.add(new JLabel("Only use drops: "));
			panel.add(dropsOnly);
			panel.add(new JLabel("20G Mode: "));
			panel.add(twentyG);
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
			TWENTY_G.set(p, twentyG.isSelected());
		}

		@Override
		public void reloadConfiguration(CustomProperties p) {
			lookahead.setValue(LOOKAHEAD.value(p));
			pruneTop.setValue(PRUNE_TOP.value(p));
			dropsOnly.setSelected(DROPS_ONLY.value(p));
			maxThreads.setValue(MAX_THREADS.value(p));
			fitness.setSelectedItem(FITNESS.value(p));
			twentyG.setSelected(TWENTY_G.value(p));
		}

	}

	private static EvilineAIConfigurator configurator = new EvilineAIConfigurator();

//	protected static final 
//	ThreadPoolExecutor
////	ExecutorService
//	POOL =
////			Executors.newCachedThreadPool();
//			new ThreadPoolExecutor(
//			1, 1, 
//			10, TimeUnit.SECONDS, 
//			new SynchronousQueue<Runnable>(),
//			new ThreadPoolExecutor.DiscardPolicy());
	
	protected static final ExecutorService pipelineExecutor = Executors.newSingleThreadExecutor();

	protected ExecutorService pool;
	
	protected class PathPipeline {
		protected PathTask task;
		protected boolean holdable;
		protected ShapeType held;
		protected int holdWait;
		
		public PathPipeline() {
			log.trace("Created new pipeline " + this);
		}

		public synchronized void extend(final GameEngine gameEngine) {
			PathTask pt = discardUntil(gameEngine);
			if(pt == null) {
				int xyshape = XYShapeAdapter.toXYShape(gameEngine);
				if(xyshape == -1)
					return;
				holdable = gameEngine.isHoldOK() && --holdWait < 0;
				FieldAdapter f = new FieldAdapter();
				f.update(gameEngine.field);
				if(twentyG) {
					while(!f.intersects(xyshape))
						xyshape = XYShapes.shiftedDown(xyshape);
					xyshape = XYShapes.shiftedUp(xyshape);
				}
				task = new PathTask(
						gameEngine,
						this,
						gameEngine.nextPieceCount - 1,
						f,
						xyshape,
						createGameNext(gameEngine, gameEngine.nextPieceCount));
				pipelineExecutor.execute(task.task);
			}
		}

		public synchronized PathTask discardUntil(GameEngine engine) {
			if(task != null && task.seq < engine.nextPieceCount - 1)
				task = null;
			return task;
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
		
		public synchronized Boolean currentHold(GameEngine engine) {
			PathTask pt = discardUntil(engine);
			if(pt == null) {
				extend(engine);
				pt = discardUntil(engine);
			}
			if(pt == null || !pt.task.isDone())
				return null;
			return pt.hold;
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
//			if(engine.lockDelayNow > 0)
//				return false;
			extend(engine);
			PathTask pt = discardUntil(engine);
			if(pt == null)
				return false;
			org.eviline.core.Field expected1 = pt.field;
//			org.eviline.core.Field expected2 = pt.after;
			if(expected1 == null)// || expected2 == null)
				return false;
			FieldAdapter f1 = new FieldAdapter();
			f1.copyFrom(expected1);
//			FieldAdapter f2 = new FieldAdapter();
//			f2.copyFrom(expected2);
			boolean dirty = f1.update(engine.field);// && f2.update(engine.field);
//			if(dirty)
//				System.out.println("dirty field");
			if(dirty)
				log.trace("dirty field");
			return dirty;
		}

		public synchronized void shutdown() {
			log.trace("shutting down pipeline " + this);
			PathTask pt = task;
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
		public boolean hold;

		public PathTask(final GameEngine gameEngine, final PathPipeline pipeline, int seq, org.eviline.core.Field field, int xystart, ShapeType[] next) {
			this.pipeline = pipeline;
			this.seq = seq;
			this.field = field;
			this.xystart = xystart;
			this.next = next;
			final boolean holdable = pipeline.holdable;
			final ShapeType held = pipeline.held;
			task = new FutureTask<>(new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					Engine engine = new Engine(PathTask.this.field, new Configuration(null, 0));
					engine.setNext(PathTask.this.next);
					engine.setShape(PathTask.this.xystart);
					engine.setHold(held);
					if(holdable) {
						engine.setHoldable(true);
						engine.setHoldEnabled(true);
					}
					AIPlayer player = new AIPlayer(ai, engine, lookahead);
					Command c = player.tick();
					if(c == Command.HOLD) {
						hold = true;
						xydest = -1;
					} else {
						Best best = player.getBest();
						path = createCommandPath(best.graph);
						xydest = player.getDest();
					}
					after = player.getAfter();
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
	}

	protected ActiveCompletor subtasks;
	
	protected DefaultAIKernel ai;

	protected Command shifting = null;

	protected int lookahead = 3;

	protected PathPipeline pipeline;

	protected boolean twentyG;
	
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
			// propagate upwards, needed in cases of high gravity
			if((c == Command.SOFT_DROP || c == Command.HARD_DROP) && parent != CommandGraph.NULL_ORIGIN) {
				xyshape = XYShapes.shiftedUp(xyshape);
				while(xyshape != parent) {
					if(xyshape >= 0 && xyshape < XYShapes.SHAPE_MAX)
						computingPaths[xyshape] = (byte) c.ordinal();
					xyshape = XYShapes.shiftedUp(xyshape);
				}
			}
			if(parent == CommandGraph.NULL_ORIGIN && twentyG) {
				xyshape = XYShapes.shiftedUp(xyshape);
				while((xyshape & XYShapes.MASK_Y) != 0) {
					computingPaths[xyshape] = (byte) Command.SOFT_DROP.ordinal();
					xyshape = XYShapes.shiftedUp(xyshape);
				}
				computingPaths[xyshape] = (byte) Command.SOFT_DROP.ordinal();
			}
			xyshape = parent;
		}
		
		return computingPaths;
	}

	protected ShapeType[] createGameNext(GameEngine engine, int seq) {
		int size = lookahead + 2;
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
//		lastxy = -1;
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
//		POOL.setMaximumPoolSize(threads);
		if(pool != null)
			pool.shutdown();
		pool = Executors.newFixedThreadPool(threads);

		Fitness fitness;
		try {
			fitness = Class.forName("org.eviline.core.ai." + FITNESS.value(opt)).asSubclass(Fitness.class).newInstance();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}

		subtasks = new ActiveCompletor(pool);
		ai = new DefaultAIKernel(subtasks, fitness) {
			@Override
			protected int starter(Field field, ShapeType type) {
				int shape = super.starter(field, type);
				if(twentyG) {
					while(!field.intersects(shape))
						shape = XYShapes.shiftedDown(shape);
					shape = XYShapes.shiftedUp(shape);
				}
				return shape;
			}
			
			@Override
			protected CommandGraph graph(Field field, int start, boolean dropsOnly) {
				if(!twentyG)
					return super.graph(field, start, dropsOnly);
				return new CommandGraph(field, start, dropsOnly) {
					protected void searchRoot(int shape, Field f) {
						setVertex(shape, NULL_ORIGIN, NULL_COMMAND, 0);
						search(shape, f);
						while(pendingHead != pendingTail) {
							shape = pending.get()[pendingHead++];
							enqueued.get()[shape & XYShapes.MASK_TYPE_POS] = false;
							pendingHead %= XYShapes.SIZE_TYPE_POS;
							search(shape, f);
						}
					}

					@Override
					protected void maybeUpdate(int shape, int origin, Command command, int pathLength, Field f) {
						while(!f.intersects(shape))
							shape = XYShapes.shiftedDown(shape);
						shape = XYShapes.shiftedUp(shape);
						super.maybeUpdate(shape, origin, command, pathLength, f);
					}
				};
			}
		};
		ai.setDropsOnly(DROPS_ONLY.value(opt));
		ai.setPruneTop(PRUNE_TOP.value(opt));
		pipeline = new PathPipeline();
		lookahead = LOOKAHEAD.value(opt);
		twentyG = TWENTY_G.value(opt);
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

			if(pipeline.isDirty(engine)) {
				log.trace("dirty pipeline, needs flushing");
				flushPipeline(engine);
				ctrl.setButtonBit(input);
				return;
			}

			Boolean hold = pipeline.currentHold(engine);
			if(hold != null) {
				if(hold) {
					if(!engine.isHoldOK()) {
						log.warn("Supposed to hold when not OK");
						flushPipeline(engine);
					} else {
						pipeline.holdWait = 0;
						input = Controller.BUTTON_BIT_D;
					}
					ctrl.setButtonBit(input);
					if(engine.holdPieceObject != null) {
						pipeline.task = null;
					}
					return;
				}
			}
			
			if(paths == null) {
				ctrl.setButtonBit(input);
				return;
			}

			Command c = Command.fromOrdinal(paths[xyshape]);

			if(c == null) {
				log.trace("dirty pipeline, needs flushing");
				flushPipeline(engine);
				ctrl.setButtonBit(input);
				return;
			}
			
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
			pipeline.held = XYShapeAdapter.toShapeType(engine.holdPieceObject);
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
