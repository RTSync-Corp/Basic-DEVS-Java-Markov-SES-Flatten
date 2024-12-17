package com.ms4systems.devs.core.simulation.impl;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.simulation.Coordinator;
import com.ms4systems.devs.core.simulation.Simulation;
import com.ms4systems.devs.core.simulation.Simulator;
import com.ms4systems.devs.events.SimulationEvent;
import com.ms4systems.devs.events.SimulationEventListener;
import com.ms4systems.devs.events.SimulationEventType;
import com.ms4systems.devs.exception.AccessException;
import com.ms4systems.devs.exception.DEVSRuntimeException;
import com.ms4systems.devs.exception.InputSynchronizationException;
import com.ms4systems.devs.exception.SynchronizationException;


public class SimulatorImpl implements Simulator {
	private static final long serialVersionUID = 1L;
	
	private Simulation simulation;
	private AtomicModel model;
	private double lastEventTime;
	private double nextEventTime;
	private double currentTime;
	private double modelTimeAdvance;
	private Coordinator parent;
	private boolean initialized;

	private double cachedOutputTime = -1;
	private MessageBag cachedOutput = null;
	
	private ArrayList<SimulationEventListener> eventListeners = new ArrayList<SimulationEventListener>();
	
	public SimulatorImpl() { // Cannot pass off to this(null,null) due to coordinators inheriting
		setInitialized(false);
		setLastEventTime(0);
		setNextEventTime(Double.POSITIVE_INFINITY);
		setCurrentTime(0);
	}

	public SimulatorImpl(AtomicModel model) {
		this(model,null);
	}
	
	public SimulatorImpl(Coordinator parent) {
		this(null,parent);
	}
	
	public SimulatorImpl(AtomicModel model, Coordinator parent) {
		this.setParent(parent);
		this.setAtomicModel(model);
		setInitialized(false);
		setLastEventTime(0);
		setCurrentTime(0);
		setNextEventTime(Double.POSITIVE_INFINITY);
	}

	@Override
	public void initialize(double initialSimulationTime) {
		setCurrentTime(initialSimulationTime);
		try {
			getAtomicModel().initialize();
		} catch (Throwable t) {
			throw new DEVSRuntimeException("Error occurred in simulator for model " +
					getAtomicModel().getName() + " during initialize()",t);
		}
		setLastEventTime(initialSimulationTime);
		setModelTimeAdvance(getAtomicModel().getTimeAdvance());
		setNextEventTime(getLastEventTime() + getModelTimeAdvance());
		setInitialized(true);
	}

	@Override
	public void executeNextEvent(double time) {
		if (Double.compare(getNextEventTime(), time)!=0)
			throw new SynchronizationException("Attempted to execute internal event at wrong time");
		
		setCurrentTime(time);
		
		if (Double.compare(getCachedOutputTime(),time)==0 && !getCachedOutput().isEmpty()) {
			MessageBag output = getCachedOutput();
			fireMessageSent(output);
		}
		
		fireTransitionEvent(SimulationEventType.TRANSITION_INTERNAL);
		
		try {
			getAtomicModel().internalTransition();
		} catch (Throwable t) {
			throw new DEVSRuntimeException("Error occurred in simulator for model " +
					getAtomicModel().getName() + " during internalTransition()",t);
		}

		// Update times
		setLastEventTime(time);
		setModelTimeAdvance(getAtomicModel().getTimeAdvance()); 
		setNextEventTime(getLastEventTime() + getModelTimeAdvance());
	}

	@Override
	public MessageBag computeOutput() {
		setCurrentTime(getNextEventTime());
		
		// Check if we've already generated this output
		if (Double.compare(getNextEventTime(), getCachedOutputTime())==0) 
			return getCachedOutput();
		
		try {
			setCachedOutput(getAtomicModel().getOutput());
		} catch (Throwable t) {
			throw new DEVSRuntimeException("Error occurred in simulator for model " +
					getAtomicModel().getName() + " during getOutput()",t);
		}
		
		if (getCachedOutput()!=null) {  // Set time if needed 
			getCachedOutput().setMessageTime(getNextEventTime());
		}
		else  // Make sure we don't return null
			setCachedOutput(MessageBag.EMPTY); 
		
		
		
		setCachedOutputTime(getNextEventTime());
		return getCachedOutput();	
	}

	protected void fireTransitionEvent(SimulationEventType eventType) {
		fireTransitionEvent(eventType,null);
	}
	
	protected void fireTransitionEvent(SimulationEventType eventType, MessageBag message) {
		if (needEvents()) {
			final SimulationEvent event = new SimulationEvent(eventType);
			event.setTime(getCurrentTime());
			event.setModel(getAtomicModel());
			event.setSimulator(this);
			event.setSimulation(getSimulation());
			event.setMessage(message);
			fireEvent(event);
		}
	}
	
	protected void fireMessageSent(MessageBag output) {
		if (needEvents()) {
			final SimulationEvent event = new SimulationEvent(SimulationEventType.MESSAGE_SENT);
			event.setTime(getCurrentTime());
			event.setModel(getAtomicModel());
			event.setSimulator(this);
			event.setSimulation(getSimulation());
			event.setMessage(output);
			fireEvent(event);
		}
	}

	@Override
	public void processInput(double time, MessageBag input) {
		List<AtomicModel> recvModel = input.getModelsWithMessages();
		if (recvModel.size()!=1 || recvModel.get(0)!=getAtomicModel()) {
				throw new InputSynchronizationException("Attempted to process input containing messages for other models");
		}
		setCurrentTime(time);
		
		fireMessageReceived(input);
		
		int timeCompare = Double.compare(time, getNextEventTime());
		if (timeCompare==0) { // Next event and input have same time
			fireTransitionEvent(SimulationEventType.TRANSITION_CONFLUENT, input);
			if (Double.compare(getCachedOutputTime(),time)==0 && !getCachedOutput().isEmpty()) {
				MessageBag output = getCachedOutput();
				fireMessageSent(output);
			}
			try {
				getAtomicModel().confluentTransition(input);
			} catch (Throwable t) {
				throw new DEVSRuntimeException("Error occurred in simulator for model " +
						getAtomicModel().getName() + " during confluentTransition()",t);
			}
		}
		else if (timeCompare<0) { // Input scheduled before event 
			fireTransitionEvent(SimulationEventType.TRANSITION_EXTERNAL, input);
			try {
				getAtomicModel().externalTransition(time-getLastEventTime(),input);
			} catch (Throwable t) {
				throw new DEVSRuntimeException("Error occurred in simulator for model " +
						getAtomicModel().getName() + " during externalTransition()",t);
			}
		}
		else // Message scheduled for some time _after_ internal event
			throw new SynchronizationException("Attempted to send input scheduled after internal event");

		// Update times
		setLastEventTime(time);
		setModelTimeAdvance(getAtomicModel().getTimeAdvance()); 
		setNextEventTime(getLastEventTime() + getModelTimeAdvance());
	}

	protected void fireMessageReceived(MessageBag input) {
		if (needEvents()) {
			final SimulationEvent event = new SimulationEvent(SimulationEventType.MESSAGE_RECEIVED);
			
			event.setTime(getCurrentTime());
			event.setModel(getAtomicModel());
			event.setSimulator(this);
			event.setSimulation(getSimulation());
			event.setMessage(input);
			fireEvent(event);
		}
	}

	@Override
	public double getNextEventTime() {
		return nextEventTime;
	}

	@Override
	public double getLastEventTime() {
		return lastEventTime;
	}

	@Override
	public AtomicModel getAtomicModel() {
		return model;
	}
	
	@Override
	public void setAtomicModel(AtomicModel model) {
		if (isInitialized()) throw new AccessException("Cannot set model after simulator initialization");
		this.model = model;
	}

	@Override
	public Coordinator getParent() {
		return parent;
	}
	
	@Override
	public void setParent(Coordinator parent) {
		if (parent !=null && 
				isInitialized()) throw new AccessException("Cannot set parent coordinator after simulator initialization");
		this.parent = parent;
	}
	
	@Override
	public boolean isInitialized() {
		return initialized;
	}

	protected void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	protected void setLastEventTime(double lastEventTime) {
		this.lastEventTime = lastEventTime;
	}

	protected void setNextEventTime(double nextEventTime) {
		this.nextEventTime = nextEventTime;
	}

	protected void setModelTimeAdvance(double modelTimeAdvance) {
		this.modelTimeAdvance = modelTimeAdvance;
	}

	protected double getModelTimeAdvance() {
		return modelTimeAdvance;
	}

	protected void setCurrentTime(double currentTime) {
		this.currentTime = currentTime;
	}

	@Override
	public double getCurrentTime() {
		return currentTime;
	}

	protected void setCachedOutputTime(double cachedOutputTime) {
		this.cachedOutputTime = cachedOutputTime;
	}

	protected double getCachedOutputTime() {
		return cachedOutputTime;
	}

	protected void setCachedOutput(MessageBag cachedOutput) {
		this.cachedOutput = cachedOutput;
	}

	protected MessageBag getCachedOutput() {
		return cachedOutput;
	}
	
	@Override
	public void clearCachedOutput() {
		setCachedOutput(null);
		setCachedOutputTime(Double.NaN);
	}

	@Override
	public Simulation getSimulation() {
		if (getParent()!=null) return getParent().getSimulation();
		return simulation;
	}

	@Override
	public void setSimulation(Simulation simulation) {
		if (getParent()!=null) 
			throw new IllegalStateException("Tried to setSimulation for a simulator that is not the root");
		this.simulation = simulation;
	}
	
	@Override
	public URI getURI() {
		URI parentURI=null;
		if (getParent()==null) parentURI = getSimulation().getURI();
		else parentURI = getParent().getURI();
		if (parentURI==null) return null;
		
		try {
			final URI myURI = URI.create(URLEncoder.encode(getAtomicModel().getName(), "UTF-8"));
			return parentURI.resolve(myURI);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public ArrayList<URI> getAllContents() {
		final ArrayList<URI> contents = new ArrayList<URI>();
		final URI myURI = getURI();
		if (myURI==null) return contents;
		if (getAtomicModel()==null) return contents;
		
		contents.add(myURI);
		for (Port<?> p : getAtomicModel().getInputPorts())
			try {
				contents.add(URI.create(myURI + "#" + URLEncoder.encode(p.getName(), "UTF-8")));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
		for (Port<?> p : getAtomicModel().getOutputPorts())
			try {
				contents.add(URI.create(myURI + "#" + URLEncoder.encode(p.getName(), "UTF-8")));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

		return contents;
	}
	
	@Override
	public void modelMessage(String message) {
		SimulationEvent event = new SimulationEvent(SimulationEventType.MODEL_MESSAGE);
		event.setModel(getAtomicModel());
		event.setModelMessage(message);
		event.setSimulation(getSimulation());
		event.setSimulator(this);
		event.setTime(getCurrentTime());
		fireEvent(event);
	}
	
	@Override
	public void addSimulationEventListener(
			SimulationEventListener eventListener, boolean addRecursively) {
		if (!eventListeners.contains(eventListener))
			eventListeners.add(eventListener);
	}
	
	protected void fireEvent(SimulationEvent event) {
		if (eventListeners.isEmpty()) return;
		for (final SimulationEventListener eventListener : eventListeners)
			if (eventListener.isForEvent(event.getEventType()))
				eventListener.eventOccurred(event);
			
	}
	
	protected boolean needEvents() {
		return !eventListeners.isEmpty();
	}

	@Override
	public void injectInput(double time, MessageBag input, MessageBag injectedInput) {
		final boolean hasMessages = injectedInput.hasMessages(model);
		if (!hasMessages && input.isEmpty()) return;
		else if (!hasMessages) processInput(time,input);
		else {
			MessageBag myInput = injectedInput.getMessages(model);
			myInput.addAll(input);
			processInput(time, myInput);
		}
	}
	
	
}
