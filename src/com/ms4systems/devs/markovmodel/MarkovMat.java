/* Do not remove or modify this comment!  It is required for file identification!
DNL
platform:/resource/Markov/src/Models/dnl/MarkovMat.dnl
-1878518734
 Do not remove or modify this comment!  It is required for file identification! */
package com.ms4systems.devs.markovmodel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.File;
import java.io.Serializable;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.*;
import java.util.ArrayList;

import com.ms4systems.devs.core.message.Message;
import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.core.message.impl.MessageBagImpl;
import com.ms4systems.devs.core.model.impl.AtomicModelImpl;
import com.ms4systems.devs.core.simulation.Simulation;
import com.ms4systems.devs.core.simulation.Simulator;
import com.ms4systems.devs.extensions.PhaseBased;
import com.ms4systems.devs.extensions.StateVariableBased;
import com.ms4systems.devs.markov.*;
// Custom library code
//ID:LIB:0
import com.ms4systems.devs.util.Pair;

//ENDID
// End custom library code
@SuppressWarnings("unused")
public class MarkovMat extends AtomicModelImpl implements PhaseBased,
    StateVariableBased {
    private static final long serialVersionUID = 1L;

    //ID:SVAR:0
    private static final int ID_STATEVECTOR = 0;

    //ENDID
    //ID:SVAR:1
    private static final int ID_STATENAMES = 1;

    //ENDID
    //ID:SVAR:2
    private static final int ID_LASTSTATEVECTOR = 2;

    //ENDID
    //ID:SVAR:3
    private static final int ID_TRANSITIONMATRIX = 3;

    //ENDID
    //ID:SVAR:4
    private static final int ID_RAND = 4;

    //ENDID
    //ID:SVAR:5
    private static final int ID_COUNTOFTRANSITIONS = 5;

    // Declare state variables
    private PropertyChangeSupport propertyChangeSupport =
        new PropertyChangeSupport(this);
    protected double[] stateVector;
    protected String[] stateNames;
    protected double[] lastStateVector;
    protected Matrix TransitionMatrix;
    protected Random Rand = new Random();
    protected int countOfTransitions = 0;

    //ENDID
    String phase = "InternalTransition";
    String previousPhase = null;
    Double sigma = 1.0;
    Double previousSigma = Double.NaN;

    // End state variables

    // Input ports
    //ID:INP:0
    public final Port<Double> inIterate =
        addInputPort("inIterate", Double.class);

    //ENDID
    // End input ports

    // Output ports
    // End output ports
    protected double currentTime;

    // This variable is just here so we can use @SuppressWarnings("unused")
    private final int unusedIntVariableForWarnings = 0;

    public MarkovMat() {
        this("MarkovMat");
    }
    
    public MarkovMat(int size) {
        this("MarkovMat");
        stateVector = new   double[size] ;
        stateVector[1] = 1;
        stateNames = new  String[size] ;
        lastStateVector = stateVector ;
        TransitionMatrix = new Matrix(size);
    }

    public MarkovMat(String name) {
        this(name, null);
    }

    public MarkovMat(String name, Simulator simulator) {
        super(name, simulator);
        
    }

    public void initialize() {
        super.initialize();

        currentTime = 0;

        // Default state variable initialization
        Rand = new Random();
        countOfTransitions = 0;

        holdIn("InternalTransition", 1.0);

        // Initialize Variables
        //ID:INIT
   
          System.out.println(
            this.TransitionMatrix.matrixIsValid());   
 
          Rand = new Random(245789);
        lastStateVector = stateVector;
        countOfTransitions = 0;

        //ENDID
        // End initialize variables
    }

    @Override
    public void internalTransition() {
        currentTime += sigma;

        if (phaseIs("InternalTransition")) {
            getSimulator()
                .modelMessage("Internal transition from InternalTransition");

            //ID:TRA:InternalTransition
            holdIn("InternalTransition", 1.0);
            //ENDID
            // Internal event code
            //ID:INT:InternalTransition
            stateVector = TransitionMatrix.matrixMultiply(stateVector);
            double sum = 0;
            for (int i = 0; i < stateVector.length; i++) {
                sum += stateVector[i];
                System.out.println(i+" state : "+stateVector[i]);
            }
            System.out.println("CHECK " + getName() +" "+ sum);
            if (getEquilibriumState() ||
                  countOfTransitions >= 2 * TransitionMatrix.getSize()) {
                passivate();

                System.out.println(getName() +
                    " is conected from initial state " +
                    connectedFromInitialState());
                System.out.println(getName() + " most probable state: ");
                printMostProbableState();
                return;
            }
            countOfTransitions++;
            lastStateVector = stateVector;

            //ENDID
            // End internal event code
            return;
        }

        passivate();
    }

    @Override
    public void externalTransition(double timeElapsed, MessageBag input) {
        currentTime += timeElapsed;
        // Subtract time remaining until next internal transition (no effect if sigma == Infinity)
        sigma -= timeElapsed;

        // Store prior data
        previousPhase = phase;
        previousSigma = sigma;

        // Fire port specific external transition functions

        //ID:EXT::inIterate
        if (input.hasMessages(inIterate)) {
            ArrayList<Message<Double>> messageList =
                inIterate.getMessages(input);

            double prob = (Double) messageList.get(0).getData();
            MarkovMat mm = new MarkovMat();
            int size = 10;
            mm.repeat(size, prob, 2 * size);

        }

        //ENDID

        // Fire state transition functions
    }

    @Override
    public void confluentTransition(MessageBag input) {
        // confluentTransition with internalTransition first (by default)
        internalTransition();
        externalTransition(0, input);
    }

    @Override
    public Double getTimeAdvance() {
        return sigma;
    }

    @Override
    public MessageBag getOutput() {
        MessageBag output = new MessageBagImpl();

        return output;
    }

    // Custom function definitions

    //ID:CUST:0
    public void setStateName(int i, String name) {
        if (stateNames == null) {
            stateNames = new String[stateVector.length];
        }
        stateNames[i] = name;
    }

    public String getStateName(int i) {
        return stateNames[i];
    }

    public int getStateIndex(String nm) {
        for (int i = 0; i < stateVector.length; i++) {
            if (stateNames[i].equals(nm)) {
                return i;
            }
        }
        return -1;
    }

    public void printStateNames() {
        for (int i = 0; i < stateVector.length; i++) {
            System.out.println(getStateName(i));
        }
    }

    public double getProbValue(String state) {
        return stateVector[getStateIndex(state)];
    }

    public double getTransitionProb(String statei, String statej) {
        return TransitionMatrix.getM()[getStateIndex(statei)][getStateIndex(statej)];
    }

    public boolean getEquilibriumState() {
        for (int i = 0; i < stateVector.length; i++) {
            if (Math.abs(stateVector[i] - lastStateVector[i]) > .01) {
                return false;
            }
        }
        return true;
    }

    public void printReducable() {
        System.out.println(TransitionMatrix.irreducible());
    }

    public boolean connectedFromInitialState() {
        for (int i = 0; i < stateVector.length; i++) {
          
            if (stateVector[i] <= 0) {
                return false;
            }
        }
        return true;
    }

    public void printSortedStates() {
        TreeSet ts = new TreeSet();
        for (int i = 0; i < stateVector.length; i++) {
            ;
            ts.add(stateVector[i]);
        }
        System.out.println(ts);
    }

    public void printMostProbableState() {
        int state = 0;
        double prob = 0;
        for (int i = 0; i < stateVector.length; i++) {
            ;
            if (stateVector[i] > prob) {
                state = i;
                prob = stateVector[i];
            }
        }
        System.out.println(state + " " + prob);
    }

    public void setInitialStateAndTransitionMatrix(int size, double prob) {
        this.stateVector = new double[size];
        this.stateVector[0] = 1;
        this.lastStateVector = this.stateVector;
        this.TransitionMatrix = new Matrix(size);
        for (int i = 0; i < size; i++) {
            double[] p = new double[size];
            double sum = 0;
            for (int j = 0; j < size; j++) {
                if (Rand.nextDouble() < prob) {
                    p[j] = 1;
                    sum += p[j];
                }
            }
            if (sum <= 0) {
                p[Rand.nextInt(size - 1)] = 1;
                sum = 1;
            }
            for (int j = 0; j < size; j++) {
                p[j] = p[j] / sum;
            }
            this.TransitionMatrix.setColumn(i, p);
        }
        this.TransitionMatrix.print();
        System.out.println(" columns sum to 1 " +
            TransitionMatrix.matrixIsValid());
    }

    public void repeat(int size, double prob, int N) {
        ArrayList<Pair> al = new ArrayList<Pair>();
        for (int i = 0; i < N; i++) {
            setInitialStateAndTransitionMatrix(size, prob);
            Pair p = new Pair(0, 0);
            p.setFirst(TransitionMatrix.everyRowHasANonZero());
           
            p.setSecond(connectedFromInitialState());
            al.add(p);
            for (Pair pq : al) {
                System.out.println("                                        " +
                    pq.getFirst() + " " + pq.getSecond());
            }
        }
        for (Pair p : al) {
            System.out.println(p.getFirst() + " " + p.getSecond());
        }
    }

    public ContinuousTimeMarkov setInitialStateAndTransitionList(int size,
        double prob) {
        ContinuousTimeMarkov ctm = new ContinuousTimeMarkov();
        MarkovMat mm = new MarkovMat();
        mm.setInitialStateAndTransitionMatrix(size, prob);
        String[] states = new String[size];
        for (int i = 0; i < size; i++) {
            states[i] = "State" + i;
        }
        Matrix mat = mm.getTransitionMatrix();
        for (int i = 0; i < size; i++) {
            double[] probs = new double[size];
            String state = "State" + i;
            String[] successors = new String[size];
            for (int j = 0; j < size; j++) {
                double p = (mat.getM())[j][i];
                if (i != j) {
                    successors[j] = states[j];
                }
                ctm.addTransitionInfo(state, successors, probs);
                ctm.setTimeInStateList(new ArrayList<TimeInState>());
            }
        }
        return ctm;
    }

    public void writeXML() {
        TransitionMatrix.writeXML();
    }

    public void print() {
        TransitionMatrix.print();
    }

    //ENDID

    // End custom function definitions
    public static void main(String[] args) {
        MarkovMat model = new MarkovMat(4);
    }

    public void addPropertyChangeListener(String propertyName,
        PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    // Getter/setter for stateVector
    public void setStateVector(double[] stateVector) {
        propertyChangeSupport.firePropertyChange("stateVector",
            this.stateVector, this.stateVector = stateVector);
    }

    public double[] getStateVector() {
        return this.stateVector;
    }

    // End getter/setter for stateVector

    // Getter/setter for stateNames
    public void setStateNames(String[] stateNames) {
        propertyChangeSupport.firePropertyChange("stateNames", this.stateNames,
            this.stateNames = stateNames);
    }

    public String[] getStateNames() {
        return this.stateNames;
    }

    // End getter/setter for stateNames

    // Getter/setter for lastStateVector
    public void setLastStateVector(double[] lastStateVector) {
        propertyChangeSupport.firePropertyChange("lastStateVector",
            this.lastStateVector, this.lastStateVector = lastStateVector);
    }

    public double[] getLastStateVector() {
        return this.lastStateVector;
    }

    // End getter/setter for lastStateVector

    // Getter/setter for TransitionMatrix
    public void setTransitionMatrix(Matrix TransitionMatrix) {
        propertyChangeSupport.firePropertyChange("TransitionMatrix",
            this.TransitionMatrix, this.TransitionMatrix = TransitionMatrix);
    }

    public Matrix getTransitionMatrix() {
        return this.TransitionMatrix;
    }

    // End getter/setter for TransitionMatrix

    // Getter/setter for Rand
    public void setRand(Random Rand) {
        propertyChangeSupport.firePropertyChange("Rand", this.Rand,
            this.Rand = Rand);
    }

    public Random getRand() {
        return this.Rand;
    }

    // End getter/setter for Rand

    // Getter/setter for countOfTransitions
    public void setCountOfTransitions(int countOfTransitions) {
        propertyChangeSupport.firePropertyChange("countOfTransitions",
            this.countOfTransitions,
            this.countOfTransitions = countOfTransitions);
    }

    public int getCountOfTransitions() {
        return this.countOfTransitions;
    }

    // End getter/setter for countOfTransitions

    // State variables
    public String[] getStateVariableNames() {
        return new String[] {
            "stateVector", "stateNames", "lastStateVector", "TransitionMatrix",
            "Rand", "countOfTransitions"
        };
    }

    public Object[] getStateVariableValues() {
        return new Object[] {
            stateVector, stateNames, lastStateVector, TransitionMatrix, Rand,
            countOfTransitions
        };
    }

    public Class<?>[] getStateVariableTypes() {
        return new Class<?>[] {
            double[].class, String[].class, double[].class, Matrix.class,
            Random.class, Integer.class
        };
    }

    public void setStateVariableValue(int index, Object value) {
        switch (index) {

            case ID_STATEVECTOR:
                setStateVector((double[]) value);
                return;

            case ID_STATENAMES:
                setStateNames((String[]) value);
                return;

            case ID_LASTSTATEVECTOR:
                setLastStateVector((double[]) value);
                return;

            case ID_TRANSITIONMATRIX:
                setTransitionMatrix((Matrix) value);
                return;

            case ID_RAND:
                setRand((Random) value);
                return;

            case ID_COUNTOFTRANSITIONS:
                setCountOfTransitions((Integer) value);
                return;

            default:
                return;
        }
    }

    // Convenience functions
    protected void passivate() {
        passivateIn("passive");
    }

    protected void passivateIn(String phase) {
        holdIn(phase, Double.POSITIVE_INFINITY);
    }

    protected void holdIn(String phase, Double sigma) {
        this.phase = phase;
        this.sigma = sigma;
        getSimulator()
            .modelMessage("Holding in phase " + phase + " for time " + sigma);
    }

    protected static File getModelsDirectory() {
        URI dirUri;
        File dir;
        try {
            dirUri = MarkovMat.class.getResource(".").toURI();
            dir = new File(dirUri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(
                "Could not find Models directory. Invalid model URL: " +
                MarkovMat.class.getResource(".").toString());
        }
        boolean foundModels = false;
        while (dir != null && dir.getParentFile() != null) {
            if (dir.getName().equalsIgnoreCase("java") &&
                  dir.getParentFile().getName().equalsIgnoreCase("models")) {
                return dir.getParentFile();
            }
            dir = dir.getParentFile();
        }
        throw new RuntimeException(
            "Could not find Models directory from model path: " +
            dirUri.toASCIIString());
    }

    protected static File getDataFile(String fileName) {
        return getDataFile(fileName, "txt");
    }

    protected static File getDataFile(String fileName, String directoryName) {
        File modelDir = getModelsDirectory();
        File dir = new File(modelDir, directoryName);
        if (dir == null) {
            throw new RuntimeException("Could not find '" + directoryName +
                "' directory from model path: " + modelDir.getAbsolutePath());
        }
        File dataFile = new File(dir, fileName);
        if (dataFile == null) {
            throw new RuntimeException("Could not find '" + fileName +
                "' file in directory: " + dir.getAbsolutePath());
        }
        return dataFile;
    }

    protected void msg(String msg) {
        getSimulator().modelMessage(msg);
    }

    // Phase display
    public boolean phaseIs(String phase) {
        return this.phase.equals(phase);
    }

    public String getPhase() {
        return phase;
    }

    public String[] getPhaseNames() {
        return new String[] { "InternalTransition" };
    }
}
