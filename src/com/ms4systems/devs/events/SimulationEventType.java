package com.ms4systems.devs.events;

import java.io.Serializable;

public enum SimulationEventType implements Serializable {
	/**
	 * Issued when a message is received, before any events.
	 * Implemented in Simulator.processInput
	 */
	MESSAGE_RECEIVED,
	/**
	 * Issued when a message is sent, before internal events.
	 * Note that this may occur some time after the model has calculated output
	 * Implemented in Simulator.processInput and Simulator.executeNextEvent
	 */
	MESSAGE_SENT,
	/***
	 * Issued when a simulation finished
	 */
	SIMULATION_FINISHED,
	/***
	 * Issued when a simulation is starting, after model initialization
	 */
	SIMULATION_STARTED,
	/***
	 * Issued when a simulation is starting, before model initialization
	 */
	SIMULATION_STARTING, 
	/***
	 * Issued after a simulation has simulated an iteration, after model events
	 */
	SIMULATION_STEP_FINISHED,
	/***
	 * Issued when a simulation is simulating an iteration, before model events
	 */
	SIMULATION_STEP_STARTED,
	
	STRUCTURE_COUPLING_ADDED,
	STRUCTURE_COUPLING_REMOVED,
	STRUCTURE_MODEL_ADDED,
	STRUCTURE_MODEL_REMOVED,
	
	/**
	 * Issued before a confluent transition. 
	 * Implemented in Simulator.processInput
	 */
	TRANSITION_CONFLUENT,
	
	/**
	 * Issued before an external transition. 
	 * Implemented in Simulator.processInput
	 */
	TRANSITION_EXTERNAL,
	
	/**
	 * Issued before an internal transition. 
	 * Implemented in Simulator.executeNextEvent
	 */
	TRANSITION_INTERNAL, 
	
	MODEL_MESSAGE,
	
	ERROR
}
