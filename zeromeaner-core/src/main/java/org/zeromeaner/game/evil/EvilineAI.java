package org.zeromeaner.game.evil;

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
import org.zeromeaner.game.component.Controller;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.game.subsystem.ai.AbstractAI;
import org.zeromeaner.gui.common.Configurable;
import org.zeromeaner.util.CustomProperties;
import org.zeromeaner.util.Options;
import org.zeromeaner.util.PropertyConstant;
import org.zeromeaner.util.PropertyConstant.Constant;
import org.zeromeaner.util.PropertyConstant.ConstantParser;

public class EvilineAI extends AbstractAI implements Configurable {
	
	protected static final Constant<Boolean> DROPS_ONLY = new Constant<>(PropertyConstant.BOOLEAN, ".eviline.drops_only", true);
	protected static final Constant<Integer> LOOKAHEAD = new Constant<>(PropertyConstant.INTEGER, ".eviline.lookahead", 3);
	protected static final Constant<Integer> PRUNE_TOP = new Constant<>(PropertyConstant.INTEGER, ".eviline.prune_top", 5);
	protected static final Constant<Integer> CPU_CORES = new Constant<>(PropertyConstant.INTEGER, ".eviline.cpu_cores", 8);
	protected static final Constant<String> FITNESS = new Constant<String>(PropertyConstant.STRING, ".eviline.fitness", "NextFitness");
	
	protected static class EvilineAIConfigurator implements Configurable.Configurator {
		
		private JCheckBox dropsOnly;
		private JSpinner lookahead;
		private JSpinner pruneTop;
		private JSpinner cpuCores;
		private JComboBox<String> fitness;
		private JPanel panel;
		
		public EvilineAIConfigurator() {
			dropsOnly = new JCheckBox();
			lookahead = new JSpinner(new SpinnerNumberModel(3, 0, 6, 1));
			pruneTop = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
			cpuCores = new JSpinner(new SpinnerNumberModel(8, 1, 16, 1));
			fitness = new JComboBox<>(new String[] {"DefaultFitness", "NextFitness", "ScoreFitness"});
			panel = new JPanel(new GridLayout(0, 2));
			panel.add(new JLabel("Maximum Lookahead: "));
			panel.add(lookahead);
			panel.add(new JLabel("Lookahead Choices: "));
			panel.add(pruneTop);
			panel.add(new JLabel("Number of AI CPU cores: "));
			panel.add(cpuCores);
			panel.add(new JLabel("Only use drops (don't shift down)"));
			panel.add(dropsOnly);
			panel.add(new JLabel("AI fitness function: "));
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
			CPU_CORES.set(p, (Integer) cpuCores.getValue());
			FITNESS.set(p, (String) fitness.getSelectedItem());
		}

		@Override
		public void reloadConfiguration(CustomProperties p) {
			lookahead.setValue(LOOKAHEAD.value(p));
			pruneTop.setValue(PRUNE_TOP.value(p));
			dropsOnly.setSelected(DROPS_ONLY.value(p));
			cpuCores.setValue(CPU_CORES.value(p));
			fitness.setSelectedItem(FITNESS.value(p));
		}
		
	}
	
	private static EvilineAIConfigurator configurator = new EvilineAIConfigurator();

	protected static final ThreadPoolExecutor POOL = new ThreadPoolExecutor(1, 1, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	
	protected static ExecutorService pipelineExec = Executors.newSingleThreadExecutor();
	
	protected class PathPipeline {
		public ExecutorService exec;
		public LinkedBlockingDeque<PathTask> pipe = new LinkedBlockingDeque<PathTask>();
		
		public PathPipeline() {
			exec = pipelineExec;
		}
		
		public void exec(Runnable task) {
			try {
				exec.execute(task);
			} catch(RejectedExecutionException e) {
			}
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
				exec(new Runnable() {
					@Override
					public void run() {
						pt.task.run();
						exec(new Runnable() {
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
			if(tail.seq >= gameEngine.nextPieceCount + gameEngine.nextPieceArraySize - lookahead - 3 || !tail.task.isDone())
				return false;
			final PathTask pt = tail.extend(gameEngine);
			if(pt == null)
				return false;
			pipe.offerLast(pt);
			exec(new Runnable() {
				@Override
				public void run() {
					pt.task.run();
					exec(new Runnable() {
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
				pipe.pollFirst();
			return pt;
		}
		
		public byte[] currentPath(GameEngine engine) {
			PathTask pt = discardUntil(engine);
			if(pt == null) {
				extend(engine);
				pt = discardUntil(engine);
			}
			if(pt == null || !pt.task.isDone())
				return null;
			return pt.path;
		}
		
		public org.eviline.core.Field expectedField(GameEngine engine) {
			extend(engine);
			PathTask pt = discardUntil(engine);
			if(pt == null)
				return null;
			return pt.field;
		}
		
		public boolean isDirty(GameEngine engine) {
			PathTask pt = pipe.peekFirst();
			if(pt != null && pt.task.isDone()) {
				try {
					pt.task.get();
				} catch(Exception e) {
					return true;
				}
			}
			FieldAdapter f = new FieldAdapter();
			org.eviline.core.Field expected = expectedField(engine);
			if(expected == null)
				return true;
			f.copyFrom(expected);
			return f.update(engine.field);
		}
		
		public void shutdown() {
//			exec.shutdownNow();
			PathTask pt = pipe.peekLast();
			if(pt != null)
				pt.task.cancel(true);
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
				return null;
			}
		}
		
		public PathTask extend(GameEngine gameEngine) {
			ShapeType[] extnext = createGameNext(gameEngine, seq);
			if(extnext == null || extnext.length < lookahead + 2)
				return null;
			Best best = get();
			if(best == null)
				return null;
			return new PathTask(
					pipeline,
					seq+1,
					best.after,
					XYShapes.toXYShape(extnext[0].startX(), extnext[0].startY(), extnext[0].start()),
					Arrays.copyOfRange(extnext, 1, extnext.length));
		}
	}
	
	protected DefaultAIKernel ai;
	
	protected Command shifting = null;
	
	protected int lookahead = 3;
	
	protected PathPipeline pipeline;
	
	
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
		if(seq < engine.nextPieceCount || seq >= engine.nextPieceCount + engine.nextPieceArraySize)
			return null;
		int size = engine.nextPieceArraySize - (seq - engine.nextPieceCount);
		ShapeType[] nextShapes = new ShapeType[size];
		for(int i = 1; i <= size; i++)
			nextShapes[size - i] = XYShapeAdapter.toShapeType(engine.getNextObject(engine.nextPieceCount + engine.nextPieceArraySize - i));
		return nextShapes;
	}
	
	public EvilineAI() {}
	
	
	
	protected void resetPipeline() {
		pipeline.shutdown();
		pipeline = new PathPipeline();
		lastxy = -1;
	}
	
	@Override
	public String getName() {
		return "eviline2";
	}

	@Override
	public void init(GameEngine engine, int playerID) {
		CustomProperties opt = Options.player(playerID).ai.BACKING;
		
		int cores = CPU_CORES.value(opt);
		if(cores > POOL.getCorePoolSize()) {
			POOL.setMaximumPoolSize(cores);
			POOL.setCorePoolSize(cores);
		} else if(cores < POOL.getCorePoolSize()) {
			POOL.setCorePoolSize(cores);
			POOL.setMaximumPoolSize(cores);
		}
		
		Fitness fitness;
		try {
			fitness = Class.forName("org.eviline.core.ai." + FITNESS.value(opt)).asSubclass(Fitness.class).newInstance();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		ai = new DefaultAIKernel(POOL, fitness);
		ai.setDropsOnly(DROPS_ONLY.value(opt));
		ai.setPruneTop(PRUNE_TOP.value(opt));
		pipeline = new PathPipeline();
		lookahead = LOOKAHEAD.value(opt);
	}

	@Override
	public void shutdown(GameEngine engine, int playerID) {
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
		
		if(paths == null) {
			if(pipeline.isDirty(engine)) {
				resetPipeline();
				pipeline.extend(engine);
			}
			ctrl.setButtonBit(input);
			return;
		}
		
		Command c = Command.fromOrdinal(paths[xyshape]);
		if(c == null || pipeline.isDirty(engine)) {
			ctrl.setButtonBit(input);
			resetPipeline();
			pipeline.extend(engine);
			return;
		} else if(xyshape != lastxy && lastxy >= 0)
			paths[lastxy] = -1;
		lastxy = xyshape;
		
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
		if(pipeline.isDirty(engine))
			resetPipeline();
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
		// TODO Auto-generated method stub

	}

	@Override
	public Configurator getConfigurator() {
		return configurator;
	}

}
