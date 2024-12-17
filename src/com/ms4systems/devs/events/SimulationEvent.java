package com.ms4systems.devs.events;

import java.io.Serializable;

import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.simulation.Simulation;
import com.ms4systems.devs.core.simulation.Simulator;

public class SimulationEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	private SimulationEventType eventType;
	private AtomicModel model;
	private Simulation simulation;
	private Simulator simulator;
	private MessageBag message;
	private double time;
	private Object[] parameters;
	private String modelMessage;
	
	public SimulationEvent(SimulationEventType eventType) {
		this.setEventType(eventType);
	}
	
	public Simulation getSimulation() {
		return simulation;
	}
	public void setSimulation(Simulation simulation) {
		this.simulation = simulation;
	}
	public Simulator getSimulator() {
		return simulator;
	}
	public void setSimulator(Simulator simulator) {
		this.simulator = simulator;
	}
	public void setMessage(MessageBag message) {
		this.message = message;
	}
	public MessageBag getMessage() {
		return message;
	}
	public void setTime(double time) {
		this.time = time;
	}
	public double getTime() {
		return time;
	}
	public void setModel(AtomicModel model) {
		this.model = model;
	}
	public AtomicModel getModel() {
		return model;
	}


	public void setEventType(SimulationEventType eventType) {
		this.eventType = eventType;
	}


	public SimulationEventType getEventType() {
		return eventType;
	}


	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}


	public Object[] getParameters() {
		return parameters;
	}


	public String getModelMessage() {
		return modelMessage;
	}


	public void setModelMessage(String modelMessage) {
		this.modelMessage = modelMessage;
	}
	
	
}
