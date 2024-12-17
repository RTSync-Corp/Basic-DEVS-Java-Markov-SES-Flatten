package com.ms4systems.devs.log;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.ms4systems.devs.core.message.Coupling;
import com.ms4systems.devs.events.SimulationEvent;

public class SimulationLogRecord extends LogRecord {
	private static final long serialVersionUID = 1L;
	private final double eventTime;
	
	public double getEventTime() {
		return eventTime;
	}

	public SimulationLogRecord(SimulationEvent event) {
		super(Level.INFO,getEventMessage(event));
		setLoggerName(getEventLoggerName(event));
		setSourceClassName(getSourceClassName(event));
		setParameters(new Object[] {event});
		eventTime = event.getTime();
	}
	
	protected static String getEventLoggerName(SimulationEvent event) {
		URI eventURI = null;
		String pathStr = "";
		
		switch(event.getEventType()){
		case MESSAGE_RECEIVED:
		case MESSAGE_SENT:
		case STRUCTURE_COUPLING_ADDED:
		case STRUCTURE_COUPLING_REMOVED:
		case STRUCTURE_MODEL_ADDED:
		case STRUCTURE_MODEL_REMOVED:
		case TRANSITION_CONFLUENT:
		case TRANSITION_EXTERNAL:
		case TRANSITION_INTERNAL:
		case MODEL_MESSAGE:
			eventURI = event.getSimulator().getURI();
			pathStr = eventURI.getPath().replace("/", ".").substring(1);
			break;
		case SIMULATION_FINISHED:
		case SIMULATION_STARTED:
		case SIMULATION_STARTING:
		case SIMULATION_STEP_FINISHED:
		case SIMULATION_STEP_STARTED:
		case ERROR:
			eventURI = event.getSimulation().getURI();
			pathStr = eventURI.getPath().replace("/", ".").substring(1);
			pathStr = pathStr.substring(0, pathStr.length()-1);
			break;
		default:
			throw new IllegalArgumentException("Unknown event type: " + event);
		}
		
		return pathStr;
	}
	
	protected static String getSourceClassName(SimulationEvent event) {
		switch(event.getEventType()){
		case MESSAGE_RECEIVED:
		case MESSAGE_SENT:
		case STRUCTURE_COUPLING_ADDED:
		case STRUCTURE_COUPLING_REMOVED:
		case STRUCTURE_MODEL_ADDED:
		case STRUCTURE_MODEL_REMOVED:
		case TRANSITION_CONFLUENT:
		case TRANSITION_EXTERNAL:
		case TRANSITION_INTERNAL:
		case MODEL_MESSAGE:
			return event.getModel().getName();
		case SIMULATION_FINISHED:
		case SIMULATION_STARTED:
		case SIMULATION_STARTING:
		case SIMULATION_STEP_FINISHED:
		case SIMULATION_STEP_STARTED:
		case ERROR:
			return event.getSimulation().getName();
		default:
			throw new IllegalArgumentException("Unknown event type: " + event);
		}
	}
	
	
	protected static String getEventMessage(SimulationEvent event) {
		switch(event.getEventType()){
		case MESSAGE_RECEIVED:
			return "Received messages " + event.getMessage().toString();
		case MESSAGE_SENT:
			return "Sent messages " + event.getMessage().toString();
		case SIMULATION_FINISHED:
			return "Simulation finished";
		case SIMULATION_STARTED:
			return "Simulation started";
		case SIMULATION_STARTING:
			return "Simulation starting";
		case SIMULATION_STEP_FINISHED:
			return "Simulation step finished";
		case SIMULATION_STEP_STARTED:
			return "Simulation step started";
		case STRUCTURE_COUPLING_ADDED: {
			if (event.getParameters().length < 1 || 
					!(event.getParameters()[0] instanceof Coupling))
				return "Coupling added to " + event.getModel().getName();
			Coupling c = (Coupling) event.getParameters()[0];
			return "Coupling added to " + event.getModel().getName() + 
				": " + c.toString(); }
		case STRUCTURE_COUPLING_REMOVED: {
			if (event.getParameters().length < 1 || 
					!(event.getParameters()[0] instanceof Coupling))
				return "Coupling removed from " + event.getModel().getName();
			Coupling c = (Coupling) event.getParameters()[0];
			return "Coupling removed from " + event.getModel().getName() + 
				": " + c.toString(); }
		case STRUCTURE_MODEL_ADDED:
			return "Added child";
		case STRUCTURE_MODEL_REMOVED:
			return "Removed child";
		case TRANSITION_CONFLUENT:
			return "Confluent transition";
		case TRANSITION_EXTERNAL:
			return "External transition";
		case TRANSITION_INTERNAL:
			return "Internal transition";
		case MODEL_MESSAGE:
			return event.getModelMessage();
		case ERROR:
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			PrintWriter w = new PrintWriter(out);
			((Throwable)event.getParameters()[0]).printStackTrace(w);
			w.flush();
			return "Error occurred:\n" + out.toString();
		default:
			throw new IllegalArgumentException("Unknown event type: " + event);
		}
	}
}
