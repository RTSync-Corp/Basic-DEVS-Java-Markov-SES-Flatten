package com.ms4systems.devs.core.simulation.impl;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


import com.ms4systems.devs.core.message.Coupling;
import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.model.CoupledModel;
import com.ms4systems.devs.core.simulation.Simulation;
import com.ms4systems.devs.core.simulation.Simulator;
import com.ms4systems.devs.events.SimulationEvent;
import com.ms4systems.devs.events.SimulationEventListener;
import com.ms4systems.devs.events.SimulationEventType;
import com.ms4systems.devs.exception.DEVSRuntimeException;
import com.ms4systems.devs.exception.SynchronizationException;
import com.ms4systems.devs.log.SimulationLogger;
import com.ms4systems.devs.util.DevsUtil;
import com.ms4systems.devs.visitor.ModelVisitor;

public class SimulationImpl implements Simulation {
	private final class SimulationThreadFactory implements ThreadFactory {
		// Implementation from java.util.concurrent.Executors.DefaultThreadFactory
		// Modified to set thread context classloader to the same as the root model's classloader
		final AtomicInteger poolNumber = new AtomicInteger(1);
		final ThreadGroup group;
		final AtomicInteger threadNumber = new AtomicInteger(1);
		final String namePrefix;
		ClassLoader contextClassLoader = null;
		
		SimulationThreadFactory() {
			SecurityManager s = System.getSecurityManager();
			group = (s != null)? s.getThreadGroup() :
				Thread.currentThread().getThreadGroup();
			namePrefix = "simulationPool-" +
					poolNumber.getAndIncrement() +
					"-thread-";
		}

		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r,
					namePrefix + threadNumber.getAndIncrement(),
					0);
			if (t.isDaemon())
				t.setDaemon(false);
			if (t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);
			if (getRootModel()!=null) 
				t.setContextClassLoader(contextClassLoader);
			return t;
		}
	}

	private class PlotThread implements Runnable, Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void run() {
		}

	}

	/**
	 * 
	 */
//	private static final long serialVersionUID = 1L;

	private long currentIterationCount = 0;

	private double currentSimulationTime = 0;

	private ArrayList<SimulationEventListener> eventListeners = new ArrayList<SimulationEventListener>();

	private boolean finished = false;
	private long finishTime = -1;
	private double initialSimulationTime = 0;
	private MessageBag lastOutput = MessageBag.EMPTY;
	private long maxElapsedTime = -1;
	private long maxIterationCount = -1;
	private long maxSimulationTime = -1;
	private String name;
	private boolean plotting;
	
	//cseo (1/7/2021) for logging
	private boolean logging;

	private Simulator rootSimulator;

	private boolean running = false;
	private final SimulationLogger simulationLogger = new SimulationLogger();
	private long startTime = -1;
	private final SimulationThreadFactory threadFactory = new SimulationThreadFactory();
	private ThreadPoolExecutor simulationThreadPool;


	
	public SimulationImpl() {
		this("Simulation", null);
	}

	public SimulationImpl(String name) {
		this(name, null);
	}
//BPZ
	public SimulationImpl(String name, AtomicModel rootModel) {
	setRootModel(rootModel);
	setName(name);
	}


	@Override
	public void addEventListener(SimulationEventListener eventListener,
			boolean addRecursively) {
		if (!eventListeners.contains(eventListener))
			eventListeners.add(eventListener);

		if (addRecursively)
			rootSimulator.addSimulationEventListener(eventListener, true);
	}

	protected void createPlots() {
		Thread plotThread = new Thread(new PlotThread());
		plotThread.start();
	}

	protected void fireEvent(final SimulationEvent event) {
		if (eventListeners.isEmpty())
			return;
		try {
			Future<?> future = simulationThreadPool.submit(new Runnable() {
				@Override
				public void run() {
					for (final SimulationEventListener eventListener : eventListeners)
						if (eventListener.isForEvent(event.getEventType()))
							eventListener.eventOccurred(event);
				}
			});
			future.get();
		}
		catch (Throwable t) {
			fireNewEvent(SimulationEventType.ERROR, t);
			throw new DEVSRuntimeException(
					"Error while performing simulation iteration", t);
		}
	}

	protected void fireNewEvent(SimulationEventType eventType) {
		fireNewEvent(eventType, null);
	}

	protected void fireNewEvent(SimulationEventType eventType, Object obj) {
		if (needEvents()) {
			final SimulationEvent event = new SimulationEvent(eventType);

			event.setTime(getCurrentSimulationTime());
			event.setSimulation(this);
			if (obj != null)
				event.setParameters(new Object[] { obj });

			fireEvent(event);
		}
	}

	@Override
	public ArrayList<URI> getAllContents() {
		if (getRootSimulator() == null)
			return null;
		return getRootSimulator().getAllContents();
	}

	@Override
	public long getCurrentElapsedTime() {
		return System.currentTimeMillis() - getStartTime();
	}

	@Override
	public long getCurrentIterationCount() {
		return currentIterationCount;
	}

	@Override
	public double getCurrentSimulationTime() {
		return currentSimulationTime;
	}

	@Override
	public long getFinishTime() {
		return finishTime;
	}

	@Override
	public double getInitialSimulationTime() {
		return initialSimulationTime;
	}

	@Override
	public double getLastEventTime() {
		return getRootSimulator().getLastEventTime();
	}

	@Override
	public MessageBag getLastOutput() {
		return lastOutput;
	}

	@Override
	public long getMaxElapsedTime() {
		return maxElapsedTime;
	}

	@Override
	public long getMaxIterationCount() {
		return maxIterationCount;
	}

	@Override
	public double getMaxSimulationTime() {
		return maxSimulationTime;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public double getNextEventTime() {
		return getRootSimulator().getNextEventTime();
	}

	@Override
	public MessageBag getNextOutput() {
		return getRootSimulator().computeOutput();
	}

	@Override
	public AtomicModel getRootModel() {
		if (getRootSimulator() == null)
			return null;
		return getRootSimulator().getAtomicModel();
	}

	@Override
	public Simulator getRootSimulator() {
		return rootSimulator;
	}


	/**
	 * @return A hash string that uniquely identifies this simulation by its
	 *         configuration
	 */
	@Override
	public int getSimulationHash() {
		if (rootSimulator == null)
			return -1;

		final StringBuilder builder = new StringBuilder();
		// Start with simulation name
		builder.append(getURI().getPath());

		// Add all names of components and a hash of couplings
		ModelVisitor visitor = new ModelVisitor() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void visit(AtomicModel atomicModel) {
				builder.append(atomicModel.getSimulator().getURI().getPath());
			}

			@Override
			public void visit(CoupledModel coupledModel) {
				builder.append(coupledModel.getCoordinator().getURI().getPath());
				String couplingStr = "";
				for (Coupling coupling : coupledModel.getCouplings())
					couplingStr += coupling.toString();
				builder.append(couplingStr.hashCode());
			}
		};
		rootSimulator.getAtomicModel().accept(visitor);

		return builder.toString().hashCode();
	}

	@Override
	public SimulationLogger getSimulationLogger() {
		return simulationLogger;
	}


	@Override
	public long getStartTime() {
		return startTime;
	}

	@Override
	public URI getURI() {
		try {
			final InetAddress addr = InetAddress.getLocalHost();
			final String hostname = addr.getCanonicalHostName();
			return new URI("devs", hostname, "/"
					+ URLEncoder.encode(getName(), "UTF-8") + "/", null);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected void initPlotSupport() {
			}

//			@Override
			public void visit(AtomicModel atomicModel) {
			}

//			@Override
			public void visit(CoupledModel coupledModel) {
			}


	@Override
	public void injectInput(final double simulationTime, final MessageBag injectedInput) {
		if (simulationTime < getCurrentSimulationTime())
			throw new SynchronizationException(
					"Attempted to inject input occurring before current time");

		if (isFinished()) {
			setRunning(true);
			setFinished(false);
		}

		// Advance simulation until input occurs before next event
		while (getNextEventTime() < simulationTime)
			simulateIterations(1);

		if (isFinished()) {
			setRunning(true);
			setFinished(false);
		}

		fireNewEvent(SimulationEventType.SIMULATION_STEP_STARTED);

		// Record output if this will be a confluent event
		if (Double.compare(getCurrentSimulationTime(), simulationTime) == 0)
			setLastOutput(getRootSimulator().computeOutput());

		try {
			Future<?> future = simulationThreadPool.submit(new Runnable() {
				@Override
				public void run() {
					// Inject
					getRootSimulator().injectInput(simulationTime, MessageBag.EMPTY,
							injectedInput);
				}
			});
			future.get();
		}
		catch (Throwable t) {
			fireNewEvent(SimulationEventType.ERROR, t);
			throw new DEVSRuntimeException(
					"Error while performing simulation iteration", t);
		}
		
		// Update stats
		setCurrentSimulationTime(simulationTime);
		setCurrentIterationCount(getCurrentIterationCount() + 1);

		fireNewEvent(SimulationEventType.SIMULATION_STEP_FINISHED);

	}

	@Override
	public boolean isFinished() {
		return finished;
	}

	public boolean isPlotting() {
		return plotting;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	protected boolean needEvents() {
		return !eventListeners.isEmpty();
	}

	protected void processSimulationOptions() {
	}

	@Override
	public void restartSimulation(double initialSimulationTime) {
		stopSimulation();
		startSimulation(initialSimulationTime);
	}

	protected void setCurrentIterationCount(long currentIterationCount) {
		this.currentIterationCount = currentIterationCount;
	}

	protected void setCurrentSimulationTime(double currentSimulationTime) {
		this.currentSimulationTime = currentSimulationTime;
	}

	protected void setFinished(boolean finished) {
		this.finished = finished;
	}

	protected void setFinishTime(long finishTime) {
		this.finishTime = finishTime;
	}

	protected void setInitialSimulationTime(double initialSimulationTime) {
		this.initialSimulationTime = initialSimulationTime;
	}

	protected void setLastOutput(MessageBag lastOutput) {
		this.lastOutput = lastOutput;
	}

	@Override
	public void setMaxElapsedTime(long maxElapsedTime) {
		this.maxElapsedTime = maxElapsedTime;
	}

	@Override
	public void setMaxIterationCount(long maxIterationCount) {
		this.maxIterationCount = maxIterationCount;
	}

	@Override
	public void setMaxSimulationTime(long maxSimulationTime) {
		this.maxSimulationTime = maxSimulationTime;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public void setPlotting(boolean plotting) {
	}
	// cseo (1/7/2021) 
	public void setLogging(boolean logging) {
		this.logging = logging;
	}
	
	@Override
	public void setRootModel(AtomicModel rootModel) {
		if (isRunning())
			throw new IllegalStateException(
					"Cannot set root model once simulation has started");
		setRootSimulator(rootModel.getSimulator());

		if (isPlotting())
			initPlotSupport();
	}

	@Override
	public void setRootSimulator(Simulator rootSimulator) {
		if (isRunning())
			throw new IllegalStateException(
					"Cannot set root simulator once simulation has started");

		if (getRootModel() != null
				&& getRootModel().getSimulator() != rootSimulator)
			getRootModel().setSimulator(rootSimulator);

		this.rootSimulator = rootSimulator;
		rootSimulator.setSimulation(this);
	}

	protected void setRunning(boolean running) {
		this.running = running;
	}

	protected void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	// Check for termination conditions
	protected boolean shouldTerminate() {
		if (getMaxIterationCount() > 0
				&& getCurrentIterationCount() >= getMaxIterationCount())
			return true;
		if (getMaxElapsedTime() > 0
				&& getCurrentElapsedTime() >= getMaxElapsedTime())
			return true;
		if (getMaxSimulationTime() > 0
				&& getCurrentSimulationTime() >= getMaxSimulationTime())
			return true;
		if (getNextEventTime() == Double.POSITIVE_INFINITY)
			return true;
		return false;
	}

	@Override
	public boolean simulateIterations(long numberOfIterations) {
		if (!isRunning())
			throw new IllegalStateException("Simulation is not running");

		long iterationCount = 0;
		while (iterationCount < numberOfIterations) {
			if (shouldTerminate()) { // Check for termination conditions
				stopSimulation();
				return false;
			}

			// Run next event
			setCurrentSimulationTime(getNextEventTime());

			SimulationEventType eventType = SimulationEventType.SIMULATION_STEP_STARTED;
			fireNewEvent(eventType);

			
			try {
				Future<?> future = simulationThreadPool.submit(new Runnable() {
					@Override
					public void run() {
						setLastOutput(getRootSimulator().computeOutput());
						getRootSimulator().executeNextEvent(getCurrentSimulationTime());
					}
				});
				future.get();
			}
			catch (Throwable t) {
				fireNewEvent(SimulationEventType.ERROR, t);
				throw new DEVSRuntimeException(
						"Error while performing simulation iteration", t);
			}
			
			// Update status
			setCurrentIterationCount(getCurrentIterationCount() + 1);

			fireNewEvent(SimulationEventType.SIMULATION_STEP_FINISHED);

			iterationCount++;
		}

		// Return whether or not all iterations were completed
		return iterationCount == numberOfIterations;
	}

	@Override
	public void startSimulation(final double initialSimulationTime) {
		if (getRootSimulator() == null)
			throw new IllegalStateException(
					"Simulation cannot be started without a root simulator set");
		if (getRootModel() == null)
			throw new IllegalStateException(
					"Simulation cannot be started without a root model set");

		if (isRunning())
			throw new IllegalStateException("Simulation already started");

		threadFactory.contextClassLoader = DevsUtil.findContextClassLoader(getRootModel());
		simulationThreadPool = new ThreadPoolExecutor(0, 1, 10, TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(1), 
				threadFactory);

		setFinished(false);

		if (isPlotting())
			createPlots();

		setStartTime(System.currentTimeMillis());
		setCurrentIterationCount(0);
		setInitialSimulationTime(initialSimulationTime);
		setCurrentSimulationTime(initialSimulationTime);

		addEventListener(simulationLogger, true);

		fireNewEvent(SimulationEventType.SIMULATION_STARTING);

		try {
			Future<?> future = simulationThreadPool.submit(new Runnable() {
				@Override
				public void run() {
					// Inject
					getRootSimulator().initialize(initialSimulationTime);
				}
			});
			future.get();
		}
		catch (Throwable t) {
			fireNewEvent(SimulationEventType.ERROR, t);
			throw new DEVSRuntimeException(
					"Error while performing simulation iteration", t);
		}

		setRunning(true);

		fireNewEvent(SimulationEventType.SIMULATION_STARTED);
		//cseo (1/29/2021)
		if(!logging) {
			System.out.println("Simulation is started with no logging!");
		}

	}

	@Override
	public void stopSimulation() {
		setFinishTime(System.currentTimeMillis());
		setRunning(false);
		setFinished(true);

		fireNewEvent(SimulationEventType.SIMULATION_FINISHED);
		//cseo (1/29/2021)
		if(!logging) {
			System.out.println("Simulation is finished with no logging!");
		}

	}
}
