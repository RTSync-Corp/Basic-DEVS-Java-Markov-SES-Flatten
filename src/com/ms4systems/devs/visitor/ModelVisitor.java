package com.ms4systems.devs.visitor;

import java.io.Serializable;

import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.model.CoupledModel;

public interface ModelVisitor extends Serializable {
	
	void visit(AtomicModel atomicModel);
	void visit(CoupledModel coupledModel);
	
}
