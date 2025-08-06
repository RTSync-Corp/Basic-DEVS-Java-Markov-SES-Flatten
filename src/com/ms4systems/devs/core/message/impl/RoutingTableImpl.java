package com.ms4systems.devs.core.message.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ms4systems.devs.core.message.Coupling;
import com.ms4systems.devs.core.message.Message;
import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.core.message.RoutingTable;
import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.simulation.Simulator;
import com.ms4systems.devs.exception.InvalidCouplingException;

public class RoutingTableImpl implements RoutingTable {
	
	private static final long serialVersionUID = 1L;
	
	/***
	 * This is the list of receiving input ports keyed on the sending output port
	 */
	protected HashMap<Port<?>, ArrayList<Port<?>>> sendReceiveMap;
	/***
	 * This is the list of sending output ports keyed on the receiving input port
	 */
	protected HashMap<Port<?>, ArrayList<Port<?>>> receiveSendMap;
	protected ArrayList<Coupling> couplings;
	public RoutingTableImpl() {
		sendReceiveMap = new HashMap<Port<?>, ArrayList<Port<?>>>();
		receiveSendMap = new HashMap<Port<?>, ArrayList<Port<?>>>();
		couplings = new ArrayList<Coupling>();
	}
	
	protected boolean isValidCoupling(Port<?> sendingPort,
			Port<?> receivingPort, boolean throwException) {
		try {
			new CouplingImpl(sendingPort, receivingPort); // This throws exception if coupling invalid
			return true;
		} catch (InvalidCouplingException e) {
			if (throwException) throw e;	// Rethrow exception if requested
		}
		return false; // This only returns if throwException==false
	}
	
	@Override
	public boolean isValidCoupling(Port<?> sendingPort,	Port<?> receivingPort) {
		return isValidCoupling(sendingPort, receivingPort, false);
	}

	@Override
	public void addCoupling(Port<?> sendingPort, Port<?> receivingPort) throws InvalidCouplingException {
		isValidCoupling(sendingPort, receivingPort, true); // Exception thrown if this coupling is invalid
		
		// Track list of ports that receive output from sending port
		ArrayList<Port<?>> receivingList = sendReceiveMap.get(sendingPort);
		if (receivingList==null) { 
			receivingList = new ArrayList<Port<?>>();
			sendReceiveMap.put(sendingPort,receivingList);
		}
		receivingList.add(receivingPort);
		
		// Track list of ports that send to receiving port
		ArrayList<Port<?>> sendingList = receiveSendMap.get(receivingPort);
		if (sendingList==null) { 
			sendingList = new ArrayList<Port<?>>();
			receiveSendMap.put(receivingPort,sendingList);
		}
		sendingList.add(sendingPort);
		
		// Track list of all couplings
		couplings.add(new CouplingImpl(sendingPort, receivingPort));
	}

	@Override
	public ArrayList<Port<?>> getReceivingPorts(Port<?> sendingPort) {
		final ArrayList<Port<?>> receivingList = sendReceiveMap.get(sendingPort);
		if (receivingList==null) return new ArrayList<Port<?>>();
		return receivingList;
	}
	
	@Override
	public ArrayList<Port<?>> getSendingPorts(Port<?> receivingPort) {
		final ArrayList<Port<?>> sendingList = receiveSendMap.get(receivingPort);
		if(sendingList==null) return new ArrayList<Port<?>>();
		return sendingList;
	}

	@Override
	public HashMap<Simulator, MessageBag> routeMessages(MessageBag input) {
		final HashMap<Simulator, MessageBag> routedMessages = new HashMap<Simulator, MessageBag>();
		if (input==null) return routedMessages;
		
		for (Message<?> inputMsg : input) { // Loop through all messages
			final Port<?> sendingPort = inputMsg.getPort();
			final List<Port<?>> receivingPorts = getReceivingPorts(sendingPort);
			
			for (Port<?> receivingPort : receivingPorts) {	// Loop through all ports receiving this message
				final Simulator recvSimulator = receivingPort.getModel().getSimulator();
				MessageBag messageBag = routedMessages.get(recvSimulator);
				if (messageBag==null) {
					messageBag = new MessageBagImpl();
					messageBag.setMessageTime(input.getMessageTime());
					routedMessages.put(recvSimulator, messageBag);
				}
				messageBag.add(inputMsg.replicateOnPort(receivingPort));  // Replicate message on receiving port
			}
		}
		
		return routedMessages; 
	}

	@Override
	public HashMap<Port<?>, ArrayList<Port<?>>> getSendReceiveMap() {
		return sendReceiveMap;
	}

	@Override
	public HashMap<Port<?>, ArrayList<Port<?>>> getReceiveSendMap() {
		return receiveSendMap;
	}

	public ArrayList<Coupling> getCouplings() {
		return couplings;
	}

	@Override
	public void setCouplings(ArrayList<Coupling> couplings)  throws InvalidCouplingException {
		clearAll();
		
		for (Coupling coupling : couplings) addCoupling(coupling);
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void removeCouplingsFor(Port<?> portToRemove) {
		if (sendReceiveMap.get(portToRemove)!=null) {
			final ArrayList<Port<?>> arrayList = (ArrayList<Port<?>>) sendReceiveMap.get(portToRemove).clone();
			for (Port<?> receivePort : arrayList)
				removeCoupling(portToRemove, receivePort);
		}
		
		if (receiveSendMap.get(portToRemove)!=null) {
			final ArrayList<Port<?>> arrayList = (ArrayList<Port<?>>) receiveSendMap.get(portToRemove).clone();
			for (Port<?> sendPort : arrayList)
				removeCoupling(sendPort, portToRemove);
		}
	}

	@Override
	public void removeCouplingsFor(AtomicModel modelToRemove) {
		ArrayList<Port<?>> portsToRemove = new ArrayList<Port<?>>(modelToRemove.getInputPorts().size() 
				+ modelToRemove.getOutputPorts().size());
		portsToRemove.addAll(modelToRemove.getInputPorts());
		portsToRemove.addAll(modelToRemove.getOutputPorts());
		for (Port<?> port : portsToRemove)
			removeCouplingsFor(port);
	}

	@Override
	public void clearAll() {
	//BPX: Redefined clearAll to use constructors instead of clear methods
    //thie allows remove couplings to first clear then copy back all
    //couplings in the complement of the specified removal set
	
        couplings = new ArrayList<Coupling>();
        sendReceiveMap = new HashMap<Port<?>, ArrayList<Port<?>>>();
        receiveSendMap = new HashMap<Port<?>, ArrayList<Port<?>>>();
	}

	@Override
	public ArrayList<Coupling> getCouplingsFor(AtomicModel model) {
		ArrayList<Coupling> lst = new ArrayList<Coupling>();
		for (Port<?> receivingPort : model.getInputPorts()) 
			lst.addAll(getCouplingsFor(receivingPort));
		for (Port<?> sendingPort : model.getOutputPorts()) 
			lst.addAll(getCouplingsFor(sendingPort));
		return lst;
	}

	@Override
	public ArrayList<Coupling> getCouplingsFor(Port<?> port) {
		ArrayList<Coupling> lst = new ArrayList<Coupling>();
		switch (port.getDirection()) {
		case INPUT:
			for (Port<?> sendingPort : getSendingPorts(port))
				lst.add(new CouplingImpl(sendingPort,port));
			break;
		case OUTPUT:
			for (Port<?> receivingPort : getReceivingPorts(port))
				lst.add(new CouplingImpl(port,receivingPort));
			break;
		default:
			throw new RuntimeException("Unknown or unspecified port direction");
		}
		return lst;
	}

	@Override
	public ArrayList<Coupling> getCouplingsFor(AtomicModel sendingModel,
			AtomicModel receivingModel) {
		ArrayList<Coupling> lst = new ArrayList<Coupling>();
		
		// Efficiency hack - loop through smallest port list
		final ArrayList<Port<?>> sendingPorts = sendingModel.getOutputPorts();
		final ArrayList<Port<?>> receivingPorts = receivingModel.getInputPorts();
		
		if (sendingPorts.size()<receivingPorts.size()) {
			for (Port<?> sendingPort : sendingPorts) {
				final ArrayList<Port<?>> portList = sendReceiveMap.get(sendingPort);
				if (portList!=null)	for (Port<?> receivingPort : portList) 
					if (receivingPort.getModel()==receivingModel)
						lst.add(new CouplingImpl(sendingPort, receivingPort));
			}
		}
		else {
			for (Port<?> receivingPort : receivingPorts) {
				final ArrayList<Port<?>> portList = receiveSendMap.get(receivingPort);
				if (portList!=null)	for (Port<?>  sendingPort : portList) 
					if (sendingPort.getModel()==sendingModel)
						lst.add(new CouplingImpl(sendingPort, receivingPort));
			}
		}
		return lst;
	}

	@Override
	public void addCoupling(Coupling coupling) {
		addCoupling(coupling.getSourcePort(),coupling.getDestinationPort());
	}

	@Override
	public void removeCoupling(Port<?> sendingPort, Port<?> receivingPort) {
		if (sendReceiveMap.get(sendingPort)!=null)
			sendReceiveMap.get(sendingPort).remove(receivingPort);
		if (receiveSendMap.get(receivingPort)!=null)
			receiveSendMap.get(receivingPort).remove(sendingPort);
		
		try {
			couplings.remove(new CouplingImpl(sendingPort, receivingPort));
		}
		catch (InvalidCouplingException e) {
			//log.warn("Tried to remove a coupling that was invalid", e);
		}
	}

	@Override
	public void removeCoupling(Coupling coupling) {
		removeCoupling(coupling.getSourcePort(),coupling.getDestinationPort());
	}

	@Override
    public void removeCouplings(ArrayList<Coupling> couplingsToRemove) {
        ArrayList<Coupling> existingcouplings = getCouplings();
        clearAll();
        for (Coupling cp : existingcouplings) {
        	if (!couplingsToRemove.contains(cp))
        		addCoupling(cp);
        }
    }
}
