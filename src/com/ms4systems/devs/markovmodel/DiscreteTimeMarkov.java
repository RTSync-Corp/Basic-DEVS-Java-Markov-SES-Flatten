package com.ms4systems.devs.markovmodel;

import java.util.HashSet;
import java.util.Random;

import com.ms4systems.devs.markov.TimeInState;

public class DiscreteTimeMarkov extends ContinuousTimeMarkov {
	protected double cycleLength = .1;
	protected MarkovMat mm;

	public DiscreteTimeMarkov(double CycleLength) {
		cycleLength = CycleLength;
		double prob = .5;
		int size = 4;
		mm = new MarkovMat();
		mm.setRand(new Random(234567));
		mm.setInitialStateAndTransitionMatrix(size, prob);
	}
	public DiscreteTimeMarkov(double CycleLength,int size,double prob) {
		cycleLength = CycleLength;
		mm = new MarkovMat();
		mm.setRand(new Random(234567));
		mm.setInitialStateAndTransitionMatrix(size, prob);
	}
    public static int makeSelectionFrom(Random Rand, double[] p) {
        double choice = Rand.nextDouble();
        double sum = 0;
        for (int i = 0; i < p.length; i++) {
            sum += p[i];
            if (choice < sum) {
                return i + 1;
            }
        }
        return p.length;
    }

	  @Override
	    public void internalTransition() {
	        currentTime += sigma;
	     getSimulator().modelMessage("Internal transition from "+phase);
	     int i;
	     for(   i = 0;i<mm.TransitionMatrix.getSize();i++){
        	   if (phase.equals("s"+i))break;
           }  
	            internalTransitionFor(i);

	              // End internal event code
	            return;
	    }
	public HashSet<String> getStates(){
		HashSet<String> hs = new HashSet<String>();
	     for (int i = 0;i<mm.TransitionMatrix.getSize();i++){
      	  hs.add("s"+i);
         } 
	     return hs;
	}
	
	public void internalTransitionFor(int i) {
		double sum = 0;
		double[] probs = new double[mm.TransitionMatrix.getSize()];
		for (int j = 0; j < mm.TransitionMatrix.getSize(); j++) {
			if (j == i)continue;
			probs[j] = 1 - Math.exp(-mm.TransitionMatrix.getM()[j][i] * cycleLength);
				//mm.TransitionMatrix.m[j][i] * cycleLength;
			sum += probs[j];
		}
		probs[i] = 	1-sum;
		int k = this.makeSelectionFrom(Rand, probs)-1;
		 TimeInState tm = getTimeInState("s" + k);
         if (tm == null) {
             tm = new TimeInState(); //state,0,0.)
             tm.setStateName("s" + k);
             tm.setCountInState(0);
             tm.setElapsedTime(0.);
             TimeInStateList.add(tm);
         }
 		 holdIn("s" + k, cycleLength);
		 previousPhase = "s" + i;
         incCount(tm);
         updateElapsedTime(tm, cycleLength);
         AccLifeTime += cycleLength;
         printTimeInState();
	}

	public static void main(String[] args) {
	
		DiscreteTimeMarkov model = new DiscreteTimeMarkov(.1,4,.5);

			model.holdIn("s0", 0.0);
	
	}

}
