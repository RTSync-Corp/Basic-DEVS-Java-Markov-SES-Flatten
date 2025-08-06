package com.ms4systems.devs.core.message.impl;

import java.io.Serializable;

import com.ms4systems.devs.core.message.Coupling;
import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.model.CoupledModel;
import com.ms4systems.devs.core.model.impl.CoupledModelImpl;

public class CouplingImpl implements Coupling {

    private static final long serialVersionUID = 1L;
    private CoupledModel parent;
    private AtomicModel source, destination;
    private Port<? extends Serializable> sourcePort, destinationPort;
    private static CoupledModelImpl DummyParent = new CoupledModelImpl();
//BPX: added to keep the equals method (which checks parent) as it is whil
    //relaxing the constructor to not check for validity

    public CouplingImpl(Port<? extends Serializable> sourcePort, Port<? extends Serializable> destinationPort) {
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
        source = sourcePort.getModel();
        destination = destinationPort.getModel();
        parent = DummyParent; //see note
    }

    public CoupledModel getParent() {
        return parent;
    }

    public void setParent(CoupledModel parent) {
        this.parent = parent;
    }

    public AtomicModel getSource() {
        return source;
    }

    public void setSource(AtomicModel source) {
        this.source = source;
    }

    public AtomicModel getDestination() {
        return destination;
    }

    public void setDestination(AtomicModel destination) {
        this.destination = destination;
    }

    public Port<? extends Serializable> getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(Port<? extends Serializable> sourcePort) {
        this.sourcePort = sourcePort;
    }

    public Port<? extends Serializable> getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(Port<? extends Serializable> destinationPort) {
        this.destinationPort = destinationPort;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CouplingImpl)) {
            return false;
        }
        final Coupling other = (CouplingImpl) obj;
        return (getSourcePort().equals(other.getSourcePort())
                && getDestinationPort().equals(other.getDestinationPort())
                //RAC: 	Don't think we need this if we're comparing ports directly
                //                && getParent().equals(other.getParent())  
                );
    }

    @Override
    public int hashCode() {
        return (Integer.toString(sourcePort.hashCode()) + Integer.toString(destinationPort.hashCode())).hashCode();
    }
    
    @Override
    public String toString() {
    	
    	return source.getName() + " (" + sourcePort.getName() + ") --> " + destination.getName() + " (" + destinationPort.getName() + ")";
    }
}
