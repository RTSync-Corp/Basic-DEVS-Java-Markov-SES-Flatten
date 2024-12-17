package com.ms4systems.devs.core.simulation.impl;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import com.ms4systems.devs.core.message.Coupling;
import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.core.message.RoutingTable;
import com.ms4systems.devs.core.message.impl.CouplingImpl;
import com.ms4systems.devs.core.message.impl.MessageBagImpl;
import com.ms4systems.devs.core.message.impl.RoutingTableImpl;
import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.model.CoupledModel;
import com.ms4systems.devs.core.simulation.Coordinator;
import com.ms4systems.devs.core.simulation.Simulator;
import com.ms4systems.devs.events.SimulationEvent;
import com.ms4systems.devs.events.SimulationEventListener;
import com.ms4systems.devs.events.SimulationEventType;
import com.ms4systems.devs.exception.AccessException;
import com.ms4systems.devs.exception.InputSynchronizationException;
import com.ms4systems.devs.exception.InvalidUsageException;
import com.ms4systems.devs.exception.SynchronizationException;

public class CoordinatorImpl extends SimulatorImpl implements Coordinator {
	
	private final class SimulatorComparator implements Comparator<Simulator>, Serializable {
		private static final long serialVersionUID = 1L;

		@Override
		public int compare(Simulator o1, Simulator o2) {
			return Double.compare(o1.getNextEventTime(),o2.getNextEventTime());
		}
	}

	private static final long serialVersionUID = 1L;
	private ArrayList<Simulator> children;
	private ArrayList<AtomicModel> childModels;
	private PriorityQueue<Simulator> simulatorQueue;
	private final RoutingTable routingTable;
	
	public CoordinatorImpl() {
		this(null,null);
	}

	public CoordinatorImpl(CoupledModel model) {
		this(model,null);
	}
	
	public CoordinatorImpl(Coordinator parent) {
		this(null,parent);
	}
	
	public CoordinatorImpl(CoupledModel model, Coordinator parent) {
		this.setParent(parent);
		this.setAtomicModel(model);
		setInitialized(false);
		setLastEventTime(0);
		setNextEventTime(Double.POSITIVE_INFINITY);
		setChildren(new ArrayList<Simulator>());
		setChildModels(new ArrayList<AtomicModel>());
		setSimulatorQueue(new PriorityQueue<Simulator>(11,new SimulatorComparator()));
		
		routingTable = new RoutingTableImpl();
	}
	 
	@Override
	public URI getURI() {
		return URI.create(super.getURI().toString() + "/");
	}
	
	@Override
	public CoupledModel getCoupledModel() {
		return (CoupledModel)getAtomicModel();
	}

	@Override
	public void setCoupledModel(CoupledModel model) {
		if (isInitialized()) throw new AccessException("Cannot set model after simulator initialization");
		this.setAtomicModel((AtomicModel) model);
	}

	@Override
	public ArrayList<Simulator> getChildren() {
		return children;
	}

	@Override
	public void setAtomicModel(AtomicModel model) {
		if (isInitialized()) throw new AccessException("Cannot set model after simulator initialization");
		if (model instanceof CoupledModel) {
			super.setAtomicModel(model);
			return;
		}
		throw new InvalidUsageException("Cannot set an atomic model for a coordinator");
	}
	
	@Override
	public void initialize(double initialSimulationTime) {
		double maxTL = Double.NEGATIVE_INFINITY;
		double minTN = Double.POSITIVE_INFINITY;
		
		getSimulatorQueue().clear();
//		getChildren().clear();
//		getChildModels().clear();
		
		setCurrentTime(initialSimulationTime);
		
		for (AtomicModel a : getCoupledModel().getChildren()) {
			Simulator s = a.getSimulator();
			
			s.initialize(initialSimulationTime);
			if (s.getParent()==this) // Must check to make sure it hasn't been removed as part of the event
				getSimulatorQueue().add(s);
			
			if (s.getLastEventTime()>maxTL)
				maxTL=s.getLastEventTime();
			if (s.getNextEventTime()<minTN)
				minTN=s.getNextEventTime();
		}
		
		setLastEventTime(maxTL);
		setNextEventTime(minTN);
		setInitialized(true);
	}
	
	@Override
	public MessageBag computeOutput() {
		MessageBag output = null;
		setCurrentTime(getNextEventTime());
		
		// Loop through all my output ports
		for (Port<?> outputPort : getCoupledModel().getOutputPorts()) {
			for (Port<?> coupledPort : routingTable.getSendingPorts(outputPort)) {
				Simulator s = coupledPort.getModel().getSimulator();
				// Is model going to undergo an internal/confluent transition next?
				if (Double.compare(s.getNextEventTime(), getNextEventTime())==0) {
					final MessageBag simOutput = s.computeOutput();
					if (!simOutput.isEmpty()) {
						if (output==null) output = new MessageBagImpl();
						output.addAll(simOutput.getMessages(coupledPort));
					}
				}
			}
		}
		
		// Route output appropriately and get only messages that are coupled to this model
		if (output==null) return MessageBag.EMPTY;
		MessageBag routedBag = routeMessages(output).get(this);
		if (routedBag==null || routedBag.isEmpty()) return MessageBag.EMPTY;
		
		routedBag.setMessageTime(getNextEventTime());
		
		fireMessageSent(routedBag);
		
		return routedBag;
	}
	
	@Override
	public void processInput(double time, MessageBag input) {
		if (time>getNextEventTime())
			throw new SynchronizationException("Tried to process input scheduled for after next event");
		else if (time<getLastEventTime())
			throw new SynchronizationException("Tried to process input scheduled for before last event");

		List<AtomicModel> recvModel = input.getModelsWithMessages();
		if (recvModel.size()!=1 || recvModel.get(0)!=getCoupledModel())
			throw new InputSynchronizationException("Attempted to process input containing messages for other models");

		setCurrentTime(time);
				
		// Calculate imminent simulators and their output
		List<Simulator> immSimulator = new ArrayList<Simulator>();
		MessageBag immOutput = calcImminentSimulators(time, immSimulator);
		immOutput.addAll(input); // Add external input to messages for routing
		
		// Actually issue all needed executeNextEvent/processInput calls
		dispatchEvents(time, immSimulator, immOutput);
		
		// Update times
		double minTN = Double.POSITIVE_INFINITY;
		if (!getSimulatorQueue().isEmpty()) minTN = getSimulatorQueue().peek().getNextEventTime();
		
		setLastEventTime(time);
		setNextEventTime(minTN);
	}
	
	@Override
	public void injectInput(double time, MessageBag input, MessageBag injectedInput) {
		// Need to - trigger processInput for models with input in injection
		//		pass inject command down to child models of coupled models
		//   Process events appropriately to maintain simulation
		
		if (time>getNextEventTime())
			throw new SynchronizationException("Tried to process input scheduled for after next event");
		else if (time<getLastEventTime())
			throw new SynchronizationException("Tried to process input scheduled for before last event");

		List<AtomicModel> recvModel = input.getModelsWithMessages();
		if (recvModel.size()>1 || (recvModel.size()==1&&recvModel.get(0)!=getCoupledModel()))
			throw new InputSynchronizationException("Attempted to process input containing messages for other models");

		setCurrentTime(time);
				
		// Calculate imminent simulators and their output
		List<Simulator> immSimulator = new ArrayList<Simulator>();
		MessageBag immOutput = calcImminentSimulators(time, immSimulator);
		immOutput.addAll(input); // Add external input to messages for routing
		
		// Actually issue all needed executeNextEvent/processInput calls
		dispatchEventsWithInjections(time, immSimulator, immOutput, injectedInput);
		
		// Update times
		double minTN = Double.POSITIVE_INFINITY;
		if (!getSimulatorQueue().isEmpty()) minTN = getSimulatorQueue().peek().getNextEventTime();
		
		setLastEventTime(time);
		setNextEventTime(minTN);
	}
	

	/**
	 * @param time
	 * @param immSimulator
	 * @param immOutput
	 */
	protected void dispatchEvents(double time, List<Simulator> immSimulator,
			MessageBag immOutput) {
		final Map<Simulator, MessageBag>  routedMessages = routeMessages(immOutput);
		
		// Process events for imminent simulators
		for (final Simulator s : immSimulator) {
			if (s==this) { // Should never happen, just to be safe though
				continue;
			}
			
			final MessageBag inputMsg = routedMessages.get(s);
			if (inputMsg==null)
				s.executeNextEvent(time);
			else {
				s.processInput(time, inputMsg);
				routedMessages.remove(s);
			}
			
			if (s.getParent()==this) // Must check to make sure it hasn't been removed as part of the event 
				getSimulatorQueue().add(s);
		}
		
		// Process events for simulators that received input from imminent simulators or external source
		for (final Map.Entry<Simulator, MessageBag> entry : routedMessages.entrySet()) {
			final Simulator s = entry.getKey();
			if (s==this) continue; // This occurs if coordinator produces output, skip event processing
			final MessageBag simInput = entry.getValue();

			getSimulatorQueue().remove(s); // Remove old internal event
			s.processInput(time, simInput);
			if (s.getParent()==this) // Must check to make sure it hasn't been removed as part of the event
				getSimulatorQueue().add(s); // Add new internal event
		}
	}

	protected void dispatchEventsWithInjections(double time, List<Simulator> immSimulator,
			MessageBag immOutput, MessageBag injectedInput) {
		final Map<Simulator, MessageBag>  routedMessages = routeMessages(immOutput);
		for (AtomicModel m : injectedInput.getModelsWithMessages()) {
			if (getChildModels().contains(m)) {
				MessageBag bag = routedMessages.get(m.getSimulator());
				if (bag==null) {
					bag = new MessageBagImpl();
					bag.setMessageTime(time);
					routedMessages.put(m.getSimulator(), bag);
				}
				bag.addAll(injectedInput.getMessages(m));
			}
		}
		
		final List<Coordinator> coordList = new ArrayList<Coordinator>();
		
		// List coordinators 
		for (Simulator s : getChildren())
			if (s instanceof Coordinator) coordList.add((Coordinator) s);
		
		// Process events for imminent simulators
		for (final Simulator s : immSimulator) {
			if (s==this) { // Should never happen, just to be safe though
				continue;
			}
			
			final MessageBag inputMsg = routedMessages.get(s);
			
			// Coordinators get the full injected list to pass down to their children
			if (s instanceof Coordinator) {
				if (inputMsg==null)
					s.injectInput(time, MessageBag.EMPTY, injectedInput.getMessages(s.getAtomicModel()));
				else {
					s.injectInput(time, inputMsg, injectedInput.getMessages(s.getAtomicModel()));
					routedMessages.remove(s);
				}
				coordList.remove(s);
			}
			// Simulators get the injected input only if they need it
			else {
				final boolean hasInjectedMessages = injectedInput.hasMessages(s.getAtomicModel());
				if (inputMsg==null && !hasInjectedMessages)
					s.executeNextEvent(time);
				else if (inputMsg==null && hasInjectedMessages)
					s.injectInput(time, MessageBag.EMPTY, injectedInput.getMessages(s.getAtomicModel()));
				else if (hasInjectedMessages) {
					s.injectInput(time, inputMsg, injectedInput.getMessages(s.getAtomicModel()));
					routedMessages.remove(s);
				}
				else {
					s.processInput(time, inputMsg);
					routedMessages.remove(s);
				}
			}
			if (s.getParent()==this) // Must check to make sure it hasn't been removed as part of the event
				getSimulatorQueue().add(s);
		}
		
		// Process events for simulators that received input from imminent simulators or external source
		for (final Map.Entry<Simulator, MessageBag> entry : routedMessages.entrySet()) {
			final Simulator s = entry.getKey();
			if (s==this) continue; // This occurs if coordinator produces output, skip event processing
			final MessageBag simInput = entry.getValue();

			getSimulatorQueue().remove(s); // Remove old internal event
		
			// Coordinators get the full injected list to pass down to their children
			if (s instanceof Coordinator) {
					s.injectInput(time, simInput, injectedInput.getMessages(s.getAtomicModel()));
					coordList.remove(s);
			}
			// Simulators get the injected input only if they need it
			else {
				final boolean hasInjectedMessages = injectedInput.hasMessages(s.getAtomicModel());
				if (hasInjectedMessages) {
					s.injectInput(time, simInput, MessageBag.EMPTY);
				}
				else {
					s.processInput(time, simInput);
				}
			}
			if (s.getParent()==this) // Must check to make sure it hasn't been removed as part of the event
				getSimulatorQueue().add(s); // Add new internal event
		}
		
		// Inject input for remaining coordinators
		for (Coordinator c : coordList) {
			getSimulatorQueue().remove(c);
			c.injectInput(time, MessageBag.EMPTY, injectedInput);
			if (c.getParent()==this) // Must check to make sure it hasn't been removed as part of the event
				getSimulatorQueue().add(c);
		}
		
	}

	/**
	 * Combines routed message maps.
	 * @param fromSet Messages will be taken from this map
	 * @param toSet Messages will be added to this map
	 */
	protected void combineRoutedMessages(
			final Map<Simulator, MessageBag> fromSet,
			final Map<Simulator, MessageBag> toSet) {
		for (final Map.Entry<Simulator, MessageBag> entry : fromSet.entrySet()) {
			final Simulator s = entry.getKey();
			final MessageBag fromMsg = entry.getValue();

			final MessageBag toMsg = toSet.get(s);
			if (toMsg==null)
				toSet.put(s, fromMsg);
			else
				toMsg.addAll(fromMsg);
		}
	}

	/**
	 * Calculates all imminent simulators and retreives their output
	 * @param time Time of next event
	 * @param immSimulator list that will end up holding imminent simulators - cannot be null
	 * @return
	 */
	protected MessageBag calcImminentSimulators(double time,
			List<Simulator> immSimulator) {
		MessageBag immOutput = null;
		while (!getSimulatorQueue().isEmpty() && Double.compare(getSimulatorQueue().peek().getNextEventTime(),time)==0) {
			final Simulator s = getSimulatorQueue().poll();
			immSimulator.add(s);
			final MessageBag simOutput = s.computeOutput();
			if (!simOutput.isEmpty()) {
				if (immOutput==null) {
					immOutput = new MessageBagImpl();
					immOutput.setMessageTime(time);
				}
				immOutput.addAll(simOutput);
			}
			s.clearCachedOutput();
		}
		if (immOutput==null) {
			immOutput = new MessageBagImpl();
			immOutput.setMessageTime(time);
		}
		return immOutput;
	}
	
	@Override
	public void executeNextEvent(double time) {
		if (Double.compare(getNextEventTime(), time)!=0)
			throw new SynchronizationException("Attempted to execute internal event at wrong time");
		setCurrentTime(time);

		// Calculate imminent simulators and their output
		List<Simulator> immSimulator = new ArrayList<Simulator>();
		MessageBag immOutput = calcImminentSimulators(time, immSimulator);
		
		// Actually issue all needed executeNextEvent/processInput calls		
		dispatchEvents(time, immSimulator, immOutput);
		
		// Update times
		double minTN = Double.POSITIVE_INFINITY;
		if (!getSimulatorQueue().isEmpty()) minTN = getSimulatorQueue().peek().getNextEventTime();
		
		setLastEventTime(time);
		setNextEventTime(minTN);
	}
	
	/**
	 * route all messages in msgToRoute to the appropriate models, return map of models with messages
	 */
	protected Map<Simulator, MessageBag> routeMessages(MessageBag msgToRoute) {
		return routingTable.routeMessages(msgToRoute);
	}

	@Override
	public void addCoupling(Port<?> fromPort, Port<?> toPort) {
		routingTable.addCoupling(fromPort, toPort);
		fireCouplingAdded(new CouplingImpl(fromPort, toPort));
	}
	
	@Override
	public void addModelChild(AtomicModel model) {
		getCoupledModel().getChildren().add(model);
		model.setParent(getCoupledModel());
		getChildModels().add(model);
		
		addSimulator(model.getSimulator());

		fireModelAdded(model);
	}
	
	@Override
	public void removeModelChild(AtomicModel model) {
		getCoupledModel().getChildren().remove(model);
		model.setParent(null);
		getChildModels().remove(model);
		
		getRoutingTable().removeCouplingsFor(model);
        
		removeSimulator(model.getSimulator());
		
		fireModelRemoved(model);
	}
	
	@Override
	public void removeCoupling(Port<?> fromPort, Port<?> toPort) {
		routingTable.removeCoupling(fromPort, toPort);
		fireCouplingRemoved(new CouplingImpl(fromPort, toPort));
	}

	@Override
	public ArrayList<AtomicModel> getChildModels() {
		return childModels;
	}

	@Override
	public boolean isChildModel(AtomicModel model) {
		return getChildModels().contains(model);
	}

	protected void setChildren(ArrayList<Simulator> children) {
		this.children = children;
	}

	protected void setChildModels(ArrayList<AtomicModel> childModels) {
		this.childModels = childModels;
	}

	protected void setSimulatorQueue(PriorityQueue<Simulator> simulatorQueue) {
		this.simulatorQueue = simulatorQueue;
	}

	protected Queue<Simulator> getSimulatorQueue() {
		return simulatorQueue;
	}
	
	@Override
	public ArrayList<URI> getAllContents() {
		final ArrayList<URI> contents = super.getAllContents();
		for (Simulator s : getChildren())
			contents.addAll(s.getAllContents());
		return contents;
	}
	
	@Override
	public ArrayList<Coupling> getCouplings() {
		return routingTable.getCouplings();
	}
	
	@Override
	public RoutingTable getRoutingTable() {
		return routingTable;
	}
	
	@Override
	public void addSimulationEventListener(
			SimulationEventListener eventListener, boolean addRecursively) {
		super.addSimulationEventListener(eventListener, addRecursively);
		if (addRecursively) for (Simulator simulator : children)
			simulator.addSimulationEventListener(eventListener, true);
	}

	protected void fireModelAdded(AtomicModel model) {
		if (needEvents()) {
			final SimulationEvent event = new SimulationEvent(SimulationEventType.STRUCTURE_MODEL_ADDED);
			
			event.setTime(getCurrentTime());
			event.setModel(getAtomicModel());
			event.setSimulator(this);
			event.setSimulation(getSimulation());
			event.setParameters(new Object[] {model});
			fireEvent(event);
		}
	}
	
	protected void fireModelRemoved(AtomicModel model) {
		if (needEvents()) {
			final SimulationEvent event = new SimulationEvent(SimulationEventType.STRUCTURE_MODEL_REMOVED);
			
			event.setTime(getCurrentTime());
			event.setModel(getAtomicModel());
			event.setSimulator(this);
			event.setSimulation(getSimulation());
			event.setParameters(new Object[] {model});
			fireEvent(event);
		}
	}
	
	protected void fireCouplingAdded(Coupling coupling) {
		if (needEvents()) {
			final SimulationEvent event = new SimulationEvent(SimulationEventType.STRUCTURE_COUPLING_ADDED);
			
			event.setTime(getCurrentTime());
			event.setModel(getAtomicModel());
			event.setSimulator(this);
			event.setSimulation(getSimulation());
			event.setParameters(new Object[] {coupling});
			fireEvent(event);
		}
	}
	
	protected void fireCouplingRemoved(Coupling coupling) {
		if (needEvents()) {
			final SimulationEvent event = new SimulationEvent(SimulationEventType.STRUCTURE_COUPLING_REMOVED);
			
			event.setTime(getCurrentTime());
			event.setModel(getAtomicModel());
			event.setSimulator(this);
			event.setSimulation(getSimulation());
			event.setParameters(new Object[] {coupling});
			fireEvent(event);
		}
	}

	@Override
	public void refreshEventTime(Simulator simulator, boolean isNewSimulator) {
		double origNextEventTime = getNextEventTime();
		if (!isNewSimulator) {
			getSimulatorQueue().remove(simulator);
			getSimulatorQueue().add(simulator);
		}
		double newNextEventTime = simulator.getNextEventTime();
		if (newNextEventTime<origNextEventTime) {
			setNextEventTime(newNextEventTime);
			getParent().refreshEventTime(this, false);
		}
	}

	@Override
	public void addSimulator(Simulator simulator) {
		getChildren().add(simulator);
		simulator.setParent(this);
		
		// Initialize if we're running currently - needed for dynamic devs
		if (isInitialized()) {
			simulator.initialize(getCurrentTime());
			refreshEventTime(simulator,true);
			getSimulatorQueue().add(simulator);
		}
		
	}

	@Override
	public void removeSimulator(Simulator simulator) {
		getChildren().remove(simulator);
		simulator.setParent(null);
		
		if (isInitialized()) {
			getSimulatorQueue().remove(simulator);
			refreshEventTime(simulator, true);
		}
	}

	@Override
	public void removeCoupling(Coupling coupling) {
		removeCoupling(coupling.getSourcePort(), coupling.getDestinationPort());
	}

	@Override
	public void removeCouplings(List<Coupling> couplings) {
		for (Coupling c : couplings) 
			removeCoupling(c);
	}
	
}
