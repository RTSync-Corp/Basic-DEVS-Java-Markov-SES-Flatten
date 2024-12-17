package com.ms4systems.devs.extensions;

public interface PhaseBased {
	String[] getPhaseNames();
	String getPhase();
	boolean phaseIs(String phase);
}
