package com.ms4systems.devs.core.model.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.ms4systems.devs.core.message.Coupling;
import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.model.CoupledModel;
import com.ms4systems.devs.core.simulation.Coordinator;
import com.ms4systems.devs.core.simulation.Simulator;
import com.ms4systems.devs.core.simulation.impl.CoordinatorImpl;
//import com.ms4systems.devs.core.util.DomToXML;
import com.ms4systems.devs.visitor.ModelVisitor;

public class CoupledModelImpl extends AtomicModelImpl implements CoupledModel {
	private ArrayList<AtomicModel> children;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CoupledModelImpl() {
		this("CoupledModel");
	}

	public CoupledModelImpl(String name) {
		this(name,null);
	}

	public CoupledModelImpl(String name, Coordinator coordinator) {
		this.setName(name);
		setInputPorts(new ArrayList<Port<? extends Serializable>>());
		setOutputPorts(new ArrayList<Port<? extends Serializable>>());
		setInitialized(false);
		setChildren(new ArrayList<AtomicModel>());
		
		if (coordinator==null) {
			this.setCoordinator(new CoordinatorImpl(this));
        } else {
            this.setCoordinator(coordinator);
		}
	}

	@Override
	public ArrayList<AtomicModel> getChildren() {
		return children;
	}

	@Override
	public Coordinator getCoordinator() {
		return (Coordinator)getSimulator();
	}

	@Override
	public void setCoordinator(Coordinator coordinator) {
		setSimulator((Simulator)coordinator);
	}

	@Override
	public void addChildModel(AtomicModel model) {
		getCoordinator().addModelChild(model);
	}

	@Override
	public void addCoupling(Port<?> fromPort, Port<?> toPort) {
		getCoordinator().addCoupling(fromPort, toPort);
	}

	/**
	 * Method is final and will never actually be called by the coordinator
	 */
	@Override
	final public Double getTimeAdvance() {
		return Double.POSITIVE_INFINITY;
	}

	/**
	 * Method is final and will never actually be called by the coordinator
	 */
	@Override
    final public void internalTransition() {
    }

	/**
	 * Method is final and will never actually be called by the coordinator
	 */
	@Override
    final public void externalTransition(double timeElapsed, MessageBag input) {
    }
	
	/**
	 * Method is final and will never actually be called by the coordinator
	 */
	@Override
    final public void confluentTransition(MessageBag input) {
    }
	
	/**
	 * Method is final and will never actually be called by the coordinator
	 */
	@Override
	final public MessageBag getOutput() {
		return MessageBag.EMPTY;
	}

	protected void setChildren(ArrayList<AtomicModel> children) {
		this.children = children;
	}
	
	@Override
	public ArrayList<Coupling> getCouplings() {
		return getCoordinator().getCouplings();
	}

    public List<Coupling> getCouplingsFor(AtomicModel am) {
        return getCoordinator().getRoutingTable().getCouplingsFor(am);
    }

	public void removeChildModel(AtomicModel modelToRemove) {
        getCoordinator().removeModelChild(modelToRemove);
    }

    public void removeCouplings(List<Coupling> couplingsToRemove) {
        getCoordinator().removeCouplings(couplingsToRemove);
    }
//BPZ: made remove coupling use remove couplings
//    public void removeCoupling(Port<?> sendingPort, Port<?> receivingPort) {
//        getCoordinator().getRoutingTable().removeCoupling(sendingPort, receivingPort);
    //   }
    public void removeCoupling(Port<?> sendingPort, Port<?> receivingPort) {
    	getCoordinator().removeCoupling(sendingPort, receivingPort);
    }

    public void removeCoupling(Coupling coupling) {
        getCoordinator().removeCoupling(coupling);
    }

    public AtomicModel getComponentWithName(String compNm) {
        ArrayList<AtomicModel> al = this.getChildren();
        for (AtomicModel am : al) {
            if (am.getName().equals(compNm)) {
                return am;
            }
        }
        return null;
    }

    public void addCoupling(String source, String srcpt, String dest,
            String destpt) {
        AtomicModel amSrc = this.getComponentWithName(source);
        AtomicModel amDest = this.getComponentWithName(dest);
        if (amSrc != null && amDest != null) {
            addInternalCoupling(source, srcpt, dest, destpt);
        } else if (amSrc == null && amDest != null) {
            addExternalInputCoupling(srcpt, dest, destpt);
        } else if (amSrc != null && amDest == null) {
            addExternalOutputCoupling(source, srcpt, destpt);
        }
    }

    public void addInternalCoupling(String source, String srcpt, String dest,
            String destpt) {
        AtomicModel amSrc = this.getComponentWithName(source);
        Port<? extends Serializable> amSrcpt = amSrc.getOutputPort(srcpt);
        AtomicModel amDest = this.getComponentWithName(dest);
        Port<? extends Serializable> amDestpt = amDest.getInputPort(destpt);
        this.addCoupling(amSrcpt, amDestpt);
    }

    public void addExternalInputCoupling(String srcpt, String dest,
            String destpt) {
        Port<? extends Serializable> cmSrcpt = this.getInputPort(srcpt);
        AtomicModel amDest = this.getComponentWithName(dest);
        Port<? extends Serializable> amDestpt = amDest.getInputPort(destpt);
        this.addCoupling(cmSrcpt, amDestpt);
    }

    public void addExternalOutputCoupling(String source, String srcpt,
            String destpt) {
        AtomicModel amSrc = this.getComponentWithName(source);
        Port<? extends Serializable> amSrcpt = amSrc.getOutputPort(srcpt);
        Port<? extends Serializable> cmDestpt = this.getOutputPort(destpt);
        this.addCoupling(amSrcpt, cmDestpt);
    }

    public static Port<? extends Serializable> addInputPort(
            CoupledModelImpl cm, String portNm) {
        Port<? extends Serializable> pt = cm.addInputPort(portNm,
                Serializable.class);
        return pt;
    }
		
    public static Port<? extends Serializable> addOutputPort(
            CoupledModelImpl cm, String portNm) {
        Port<? extends Serializable> pt = cm.addOutputPort(portNm,
                Serializable.class);
        return pt;
	}

    public String writeCoupling() {
        String s = "";
        ArrayList<Coupling> cp = this.getCouplings();
        for (Coupling coup : cp) {
            String source = coup.getSource().getName();
            String srcPrt = coup.getSourcePort().getName();
            String destination = coup.getDestination().getName();
            String destPrt = coup.getDestinationPort().getName();
            if (source.toLowerCase().contains("notpresent") || destination.toLowerCase().contains("notpresent")) {
                continue;
            }
            if (!source.equals(this.getName())
                    && !destination.equals(this.getName())) {
                s += "\n\t\taddCoupling(" + source + "." + srcPrt + ","
                        + destination + "." + destPrt + ");";
            }
            if (source.equals(this.getName())
                    && !destination.equals(this.getName())) {
                s += "\n\t\taddCoupling(this." + srcPrt + ","
                        + destination + "." + destPrt + ");";
            }
            if (!source.equals(this.getName())
                    && destination.equals(this.getName())) {
                s += "\n\t\taddCoupling(" + source + "." + srcPrt + ","
                        + "this." + destPrt + ");";
            }


        }
        return s;
    }
//
//    public String writePorts() {
//        String s = "";
//        ArrayList<Coupling> cp = this.getCouplings();
//        for (Coupling coup : cp) {
//            String source = coup.getSource().getName();
//            String srcPrt = coup.getSourcePort().getName();
//            String destination = coup.getDestination().getName();
//            String destPrt = coup.getDestinationPort().getName();
//            if (source.equals(this.getName())) {
//                s += "\npublic final Port<? extends Serializable> " + srcPrt
//                        + "= addInputPort(" + DomToXML.quote(srcPrt)
//                        + ",Serializable.class);";
//            }
//
//            if (destination.equals(this.getName())) {
//                s += "\npublic final Port<? extends Serializable> " + destPrt
//                        + "= addOutputPort(" + DomToXML.quote(destPrt)
//                        + ",Serializable.class);";
//            }
//        }
//        return s;
//    }
    public String writePorts() {

		String s = "";
		HashSet<Object> outports = new HashSet<Object>();
		HashSet<Object>  inports = new HashSet<Object>();
		ArrayList<Coupling> cp = this.getCouplings();
		for (Coupling coup : cp) {
			
			String source = coup.getSource().getName();
			String srcPrt = coup.getSourcePort().getName();
			String destination = coup.getDestination().getName();
			String destPrt = coup.getDestinationPort().getName();
			if (source.equals(this.getName()) && !(inports.contains(srcPrt))) {
				inports.add(srcPrt);
				// s += "\n\t\tpublic final Port<? extends Serializable> " + srcPrt
				// 		+ "= addInputPort(" + DomToXML.quote(srcPrt)
				// 		+ ",Serializable.class);";
			}
			if (destination.equals(this.getName()) && !(outports.contains(destPrt))) {
				outports.add(destPrt);
				// s += "\n\t\tpublic final Port<? extends Serializable> " + destPrt
				// 		+ "= addOutputPort(" + DomToXML.quote(destPrt)
				// 		+ ",Serializable.class);";
			}
		}
		return s;
	}

    @Override
	public void accept(ModelVisitor visitor){
		visitor.visit(this);
		for (AtomicModel child : getChildren())
			child.accept(visitor);
	}
    
    public ArrayList<AtomicModel>  getComponentsWithPartName( String nm){
		ArrayList<AtomicModel> al = getChildren();
		ArrayList<AtomicModel> res = new ArrayList<AtomicModel>();
		for (AtomicModel am:al){
			if (am.getName().contains(nm)){
				res.add(am);
			}
		}
		return res;
	}
	
	public AtomicModel getComponentWithPartName( String nm){
		ArrayList<AtomicModel> res = getComponentsWithPartName(nm);
		if (res.isEmpty()) {
			return null;
		}
		return res.get(0);
	}
}
