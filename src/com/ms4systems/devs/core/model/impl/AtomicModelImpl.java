package com.ms4systems.devs.core.model.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.core.message.impl.PortImpl;
import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.model.CoupledModel;
import com.ms4systems.devs.core.simulation.Simulator;
import com.ms4systems.devs.core.simulation.impl.SimulatorImpl;
import com.ms4systems.devs.exception.AccessException;
import com.ms4systems.devs.visitor.ModelVisitor;



public class AtomicModelImpl implements AtomicModel {

	private static final long serialVersionUID = 1L;
	private ArrayList<Port<? extends Serializable>> inputPorts;
	private ArrayList<Port<? extends Serializable>> outputPorts;
	private String name;
	private Simulator simulator;
	private boolean initialized;
	private CoupledModel parent = null;
	
	public AtomicModelImpl() {
		this("AtomicModel");
	}
	
	public AtomicModelImpl(String name) {
		this(name,null);
	}
	
	public AtomicModelImpl(String name, Simulator simulator) {
		this.setName(name);
		setInputPorts(new ArrayList<Port<? extends Serializable>>());
		setOutputPorts(new ArrayList<Port<? extends Serializable>>());
		setInitialized(false);
		
		if (simulator==null) {
			this.setSimulator(new SimulatorImpl(this));
		}
		else this.setSimulator(simulator);
	}
	
	@Override
	public void accept(ModelVisitor visitor){
		visitor.visit(this);
	}
	
	@Override
	public Double getTimeAdvance() {return Double.POSITIVE_INFINITY;};

	/// Default to doing nothing
	@Override
	public void internalTransition() {};

	/// Default to ignoring input
	@Override
	public void externalTransition(double timeElapsed, MessageBag input) {};

	/// Default to processing internal event, then external event
	@Override
	public void confluentTransition(MessageBag input) {
		internalTransition();
		externalTransition(0,input);
	}

	@Override
	public MessageBag getOutput() {
		return MessageBag.EMPTY;
	}

	@Override
	public void initialize() {
		setInitialized(true);
	}
	
	@Override
	public void setSimulator(Simulator simulator) {
		if (isInitialized()) throw new AccessException("Cannot set simulator after model initialization");
		this.simulator = simulator;
	}
	
	@Override
	public boolean isInitialized() {
		return initialized;
	}

	protected void setInputPorts(ArrayList<Port<? extends Serializable>> inputPorts) {
		this.inputPorts = inputPorts;
	}

	@Override
	public ArrayList<Port<? extends Serializable>> getInputPorts() {
		return inputPorts;
	}

	@Override
	public Simulator getSimulator() {
		return simulator;
	}

	protected void setOutputPorts(ArrayList<Port<? extends Serializable>> outputPorts) {
		this.outputPorts = outputPorts;
	}

	@Override
	public ArrayList<Port<? extends Serializable>> getOutputPorts() {
		return outputPorts;
	}

	public void setName(String name) {
		if (isInitialized()) throw new AccessException("Cannot change model name after initialization");
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	protected void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}
	
	public  Port<Serializable> addPort(String name, Port.Direction direction) {
		return addPort(name, Serializable.class, direction);
	}
	
	public <T extends Serializable> Port<T> addPort(Port<T> port) {
		switch (port.getDirection()) {
		case INPUT:
			HashSet<String> inputSet = new HashSet<String>();
			for(Object obj : getInputPorts()){
				String portName = ((Port<?>)obj).getName();
				inputSet.add(portName);
			}
			if(!inputSet.contains(port.getName())) getInputPorts().add(port);
			break;
		case OUTPUT:
			HashSet<String> outputSet = new HashSet<String>();
			for(Object obj : getOutputPorts()){
				String portName = ((Port<?>)obj).getName();
				outputSet.add(portName);
			}
			if(!outputSet.contains(port.getName())) getOutputPorts().add(port);
			break;
		}
		return port;
	}

	public <T extends Serializable> Port<T> addPort(String name, Class<T> portType, Port.Direction direction) {
		return addPort( new PortImpl<T>(this, name, portType, direction) );
	}
	
	public Port<Serializable> addInputPort(String name) {
		return addInputPort(name,Serializable.class);
	}
	
	public <T extends Serializable> Port<T> addInputPort(String name, Class<T> portType) {
		return addPort(name, portType, Port.Direction.INPUT);
	}
	
	public <T extends Serializable> Port<T> addInputPort(Port<T> inPort) {
		return addPort(inPort);
	}
	
	public Port<Serializable> addOutputPort(String name) {
		return addOutputPort(name,Serializable.class);
	}
	
	public <T extends Serializable> Port<T> addOutputPort(String name, Class<T> portType) {
		return addPort(name, portType, Port.Direction.OUTPUT);
	}
	
	public <T extends Serializable> Port<T> addOutputPort(Port<T> outPort) {
		return addPort(outPort);
	}

	public boolean hasPort(String name) {
		return getPort(name)!=null;
	}

	public boolean hasPort(String name, Class<? extends Serializable> klass) {
		return getPort(name,klass)!=null;
	}

	public Port<? extends Serializable> getPort(String name) {
		return getPort(name,Serializable.class);
	}

	@SuppressWarnings("unchecked")
	public <T extends Serializable> Port<T> getPort(String name, Class<T> portType) {
		final List<Port<? extends Serializable>> allPorts = new 
			ArrayList<Port<? extends Serializable>>(getInputPorts().size()+getOutputPorts().size());
		allPorts.addAll(getInputPorts()); allPorts.addAll(getOutputPorts());
		for (Port<? extends Serializable> p : allPorts) {
			if (p.getName().equalsIgnoreCase(name) && 
					(portType==null || portType.isAssignableFrom(p.getType())))
				return (Port<T>) p;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Serializable> Port<T> getPort(String name, Class<T> klass, Port.Direction direction) {
		if (direction==null) return getPort(name,klass);
		List <Port<? extends Serializable>> ports = Collections.emptyList();
		switch (direction) {
		case INPUT:
			ports = getInputPorts();
			break;
		case OUTPUT:
			ports = getOutputPorts();
			break;
		}
		
		for (Port<? extends Serializable> p : ports) {
			if (p.getName().equalsIgnoreCase(name) && 
					(klass==null || klass.isAssignableFrom(p.getType())))
				return (Port<T>) p;
		}
		return null;
	}

	@Override
	public boolean hasInputPort(String name) {
		return getInputPort(name)!=null;
	}

	@Override
	public boolean hasInputPort(String name, Class<? extends Serializable> portType) {
		return getPort(name,portType,Port.Direction.INPUT)!=null;
	}

	@Override
	public Port<? extends Serializable> getInputPort(String name) {
		return getPort(name, null, Port.Direction.INPUT);
	}

	@Override
	public <T extends Serializable> Port<T>  getInputPort(String name, Class<T> klass) {
		return getPort(name, klass, Port.Direction.INPUT);
	}

	@Override
	public boolean hasOutputPort(String name) {
		return getOutputPort(name)!=null;
	}

	@Override
	public boolean hasOutputPort(String name,
			Class<? extends Serializable> klass) {
		return getOutputPort(name, klass)!=null;
	}

	@Override
	public Port<? extends Serializable> getOutputPort(String name) {
		return getPort(name, null, Port.Direction.OUTPUT);
	}

	@Override
	public <T extends Serializable> Port<T>  getOutputPort(String name, Class<T> klass) {
		return getPort(name, klass, Port.Direction.OUTPUT);
	}

	@Override
	public boolean hasCompatibleInputPort(Class<? extends Serializable> portType) {
		for (Port<?> port : getInputPorts())
			if (port.getType().isAssignableFrom(portType)) return true;
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> ArrayList<Port<T>> getCompatibleInputPorts(
			Class<T> portType) {
		ArrayList<Port<T>> compatiblePorts = new ArrayList<Port<T>>();
		for (Port<?> port : getInputPorts())
			if (port.getType().isAssignableFrom(portType)) {
				compatiblePorts.add((Port<T>) port);
			}
		return compatiblePorts;
	}

	@Override
	public boolean hasCompatibleOutputPort(
			Class<? extends Serializable> portType) {
		for (Port<?> port : getOutputPorts())
			if (port.getType().isAssignableFrom(portType)) return true;
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> ArrayList<Port<T>> getCompatibleOutputPorts(
			Class<T> portType) {
		ArrayList<Port<T>> compatiblePorts = new ArrayList<Port<T>>();
		for (Port<?> port : getOutputPorts())
			if (port.getType().isAssignableFrom(portType)) {
				compatiblePorts.add((Port<T>) port);
			}
		return compatiblePorts;
	}
	
	@Override
	public CoupledModel getParent() {
		return parent;
	}

	@Override
	public void setParent(CoupledModel parent) {
		this.parent = parent;
	}
    
    public CoupledModelImpl getAncestor(AtomicModel m,int level){
    	if (level == 1)return (CoupledModelImpl)m.getParent();
    	return(getAncestor(m.getParent(),level-1));
    }
}
