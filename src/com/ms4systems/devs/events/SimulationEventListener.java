package com.ms4systems.devs.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

abstract public class SimulationEventListener implements Serializable {
	private static final long serialVersionUID = 1L;

	protected ArrayList<SimulationEventType> eventTypes = new ArrayList<SimulationEventType>();

	public SimulationEventListener() {}
	
	public SimulationEventListener(SimulationEventType eventType) {
		this.eventTypes.add(eventType);
	}
	
	public SimulationEventListener(Collection<SimulationEventType> eventTypes) {
		this.eventTypes.addAll(eventTypes);
	}
	
	public SimulationEventListener(SimulationEventType ... eventTypes) {
		Collections.addAll(this.eventTypes, eventTypes);
	}
	
	public void addEventType(SimulationEventType eventType) {
		eventTypes.add(eventType);
	}

	public boolean isForEvent(SimulationEventType eventType) {
		return eventTypes.contains(eventType);
	}

	public ArrayList<SimulationEventType> getEventTypes() {
		return eventTypes;
	}

	public void setEventTypes(ArrayList<SimulationEventType> eventTypes) {
		this.eventTypes = eventTypes;
	}
	
	public void eventOccurred(SimulationEvent event) {
		switch(event.getEventType()){
		case MESSAGE_RECEIVED:
			messageReceived(event);
			break;
		case MESSAGE_SENT:
			messageSent(event);
			break;
		case SIMULATION_FINISHED:
			simulationFinished(event);
			break;
		case SIMULATION_STARTED:
			simulationStarted(event);
			break;
		case SIMULATION_STARTING:
			simulationStarting(event);
			break;
		case SIMULATION_STEP_FINISHED:
			simulationStepFinished(event);
			break;
		case SIMULATION_STEP_STARTED:
			simulationStepStarted(event);
			break;
		case STRUCTURE_COUPLING_ADDED:
			couplingAdded(event);
			break;
		case STRUCTURE_COUPLING_REMOVED:
			couplingRemoved(event);
			break;
		case STRUCTURE_MODEL_ADDED:
			modelAdded(event);
			break;
		case STRUCTURE_MODEL_REMOVED:
			modelRemoved(event);
			break;
		case TRANSITION_CONFLUENT:
			confluentTransition(event);
			break;
		case TRANSITION_EXTERNAL:
			externalTransition(event);
			break;
		case TRANSITION_INTERNAL:
			internalTransition(event);
			break;
		default:
			throw new IllegalArgumentException("Unknown event type: " + event);
		}
	}

	protected void internalTransition(SimulationEvent event) {}

	protected void externalTransition(SimulationEvent event) {}

	protected void confluentTransition(SimulationEvent event) {}

	protected void modelRemoved(SimulationEvent event) {}

	protected void modelAdded(SimulationEvent event) {}

	protected void couplingRemoved(SimulationEvent event) {}

	protected void couplingAdded(SimulationEvent event) {}

	protected void simulationStepStarted(SimulationEvent event) {}

	protected void simulationStepFinished(SimulationEvent event) {}

	protected void simulationStarting(SimulationEvent event) {}

	protected void simulationStarted(SimulationEvent event) {}

	protected void simulationFinished(SimulationEvent event) {}

	protected void messageSent(SimulationEvent event) {}

	protected void messageReceived(SimulationEvent event) {}
	
}
