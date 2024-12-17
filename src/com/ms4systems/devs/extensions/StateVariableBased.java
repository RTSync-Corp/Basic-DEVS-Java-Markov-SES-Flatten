package com.ms4systems.devs.extensions;

public interface StateVariableBased {
	String[] getStateVariableNames();
	Object[] getStateVariableValues();
	
	Class<?>[] getStateVariableTypes();
	
	void setStateVariableValue(int index, Object value);
}
