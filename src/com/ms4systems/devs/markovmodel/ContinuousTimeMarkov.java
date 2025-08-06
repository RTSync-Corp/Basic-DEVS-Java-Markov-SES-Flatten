/* Do not remove or modify this comment!  It is required for file identification!
DNL
platform:/resource/Markov/src/Models/dnl/ContinuousTimeMarkov.dnl
976238775
 Do not remove or modify this comment!  It is required for file identification! */
package com.ms4systems.devs.markovmodel;

import org.w3c.dom.*;

import org.xml.sax.SAXException;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.*;

// Custom library code
//ID:LIB:0
import java.util.Random;
import java.util.Set;

import javax.xml.parsers.*;

import com.ms4systems.devs.analytics.*;
import com.ms4systems.devs.core.message.Message;
import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.core.message.impl.MessageBagImpl;
import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.model.CoupledModel;
import com.ms4systems.devs.core.model.CoupledModel;
import com.ms4systems.devs.core.model.impl.AtomicModelImpl;
import com.ms4systems.devs.core.model.impl.CoupledModelImpl;
import com.ms4systems.devs.core.model.impl.CoupledModelImpl;
import com.ms4systems.devs.core.simulation.Simulation;
import com.ms4systems.devs.core.simulation.Simulator;
import com.ms4systems.devs.extensions.PhaseBased;
import com.ms4systems.devs.extensions.StateVariableBased;
import com.ms4systems.devs.markov.*;

//ENDID
// End custom library code
@SuppressWarnings("unused")
public class ContinuousTimeMarkov extends AtomicModelImpl implements PhaseBased,
    StateVariableBased {
    private static final long serialVersionUID = 1L;

    //ID:SVAR:0
    private static final int ID_RAND = 0;

    //ENDID
    //ID:SVAR:1
    private static final int ID_ACCLIFETIME = 1;

    //ENDID
    //ID:SVAR:2
    private static final int ID_TIMEINSTATELIST = 2;

    //ENDID
    //ID:SVAR:3
    private static final int ID_TRANSITIONINFOLIST = 3;

    //ENDID
    //ID:SVAR:4
    private static final int ID_AVGLIFETIME = 4;

    //ENDID
    //ID:SVAR:5
    private static final int ID_SEED = 5;

    // Declare state variables
    private PropertyChangeSupport propertyChangeSupport =
        new PropertyChangeSupport(this);
    protected Random Rand = new Random();
    protected double AccLifeTime = 0;
    protected ArrayList<TimeInState> TimeInStateList;
    protected ArrayList<TransitionInfo> TransitionInfoList;
    protected double AvgLifeTime = 0;
    protected long Seed = 2349991;
    protected MarkovMat mm;
    //ENDID
    String phase = "";
    String previousPhase = null;
    Double sigma = 10.0;
    Double previousSigma = Double.NaN;

    // End state variables

    // Input ports
    //ID:INP:0
    public final Port<Serializable> inCreateLumpedModel =
        addInputPort("inCreateLumpedModel", Serializable.class);

    //ENDID
    //ID:INP:1
   
    //ENDID
    // End input ports

    // Output ports
    //ID:OUTP:0
      //ENDID
    //ID:OUTP:1
    public final Port<String> outTimeInState =
        addOutputPort("outTimeInState", String.class);
    
    public final Port<ArrayList> outTimeInStateList =
        addOutputPort("outTimeInStateList", ArrayList.class);

    //ENDID
    // End output ports
    protected double currentTime;

    // Custom function definitions

    //ID:CUST:0

    String nextState = "";
    double timeToNextEvent;

    // This variable is just here so we can use @SuppressWarnings("unused")
    private final int unusedIntVariableForWarnings = 0;

    public ContinuousTimeMarkov() {
        this("ContinuousTimeMarkov");
    }

    public ContinuousTimeMarkov(String name) {
        this(name, null);
    }

    public ContinuousTimeMarkov(String name, Simulator simulator) {
        super(name, simulator);
        TimeInStateList = new ArrayList<TimeInState>();
    }

    public void initialize() {
        super.initialize();

        currentTime = 0;

        // Default state variable initialization
    //    Rand = new Random();
        AccLifeTime = 0;
        AvgLifeTime = 0;
        Seed = 2349991;
  //      holdIn("FirstS", 0.0);
        // Initialize Variables
        //ID:INIT
        Rand = new Random(Seed);
        timeToNextEvent = 0.;
        TimeInStateList = new ArrayList<TimeInState>();
 //       fillTransitionInfoList("ContinuousTimeMarkov.xml");

        //ENDID
        // End initialize variables
    }

    @Override
    public void internalTransition() {
        currentTime += sigma;
     getSimulator().modelMessage("Internal transition from "+phase);


            internalTransitionFor(phase);

              // End internal event code
            return;
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

        //ID:EXT::inCreateLumpedModel
        if (input.hasMessages(inCreateLumpedModel)) {
            ArrayList<Message<Serializable>> messageList =
                inCreateLumpedModel.getMessages(input);

            // ctm = new ("HelloHi");
            //setInitialStateAndTransitionList(ctm);
             mm = new MarkovMat();
            setInitialStateAndTransitionMatrix(mm);

        }

        //ENDID

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
      output.add(outTimeInState,phase);
      output.add(outTimeInStateList,TimeInStateList);
            //ENDID
            // End output event code
        
        return output;
    }

    public void internalTransitionFor(String state) {
        ArrayList<String> successors = getSuccs(state);
        if (timeToNextEvent == 0) {
            timeToNextEvent = Double.POSITIVE_INFINITY;
            int i = 0;
            for (String succ : successors) {
                TransitionInfo ti = getTransitionInfoFor(state, succ);
                double prob = ti.getProbValue();
                double time = timeToNextEvent(prob);
                if (time < timeToNextEvent) {
                    timeToNextEvent = time;
                    nextState = succ;
                }
                i++;
            }
        }
        if (previousPhase != null && previousPhase.equals(state)) {
            timeToNextEvent = 0.;
            holdIn(nextState, 0.);
        } else {
            TimeInState tm = getTimeInState(state);
            if (tm == null) {
                tm = new TimeInState(); //state,0,0.)
                tm.setStateName(state);
                tm.setCountInState(0);
                tm.setElapsedTime(0.);
                TimeInStateList.add(tm);
            }
            holdIn(state, timeToNextEvent);
            previousPhase = state;
            incCount(tm);
            updateElapsedTime(tm, timeToNextEvent);
            AccLifeTime += timeToNextEvent;
            printTimeInState();
        }
    }

    public void internalTransitionFor(String state, String[] successors,
        double[] probabilities) {
        if (timeToNextEvent == 0) {
            timeToNextEvent = Double.POSITIVE_INFINITY;
            int i = 0;
            for (String succ : successors) {
                double time = timeToNextEvent(probabilities[i]);
                if (time < timeToNextEvent) {
                    timeToNextEvent = time;
                    nextState = succ;
                }
                i++;
            }
        }
        if (previousPhase != null && previousPhase.equals(state)) {
            timeToNextEvent = 0.;
            holdIn(nextState, 0.);
        } else {
            TimeInState tm = getTimeInState(state);
            if (tm == null) {
                tm = new TimeInState(); //state,0,0.)
                tm.setStateName(state);
                tm.setCountInState(0);
                tm.setElapsedTime(0.);
                TimeInStateList.add(tm);
            }
            holdIn(state, timeToNextEvent);
            previousPhase = state;
            incCount(tm);
            updateElapsedTime(tm, timeToNextEvent);
            AccLifeTime += timeToNextEvent;
            printTimeInState();
        }
    }

    public double timeToNextEvent(double lambda) {
        double sample = Rand.nextDouble();
        return -(1 / lambda) * Math.log(sample);
    }

    public static void incCount(TimeInState tm) {
        int ct = tm.getCountInState() + 1;
        tm.setCountInState(ct);
    }

    public static void updateElapsedTime(TimeInState tm, double e) {
        double ct = tm.getElapsedTime() + e;
        tm.setElapsedTime(ct);
    }

    public TimeInState getTimeInState(String state) {
        for (TimeInState tm : TimeInStateList) {
            if (tm.getStateName().equals(state)) {
                return tm;
            }
        }
        return null;
    }

    public void printTimeInState() {
    	HashSet hs = getStates();
        for (TimeInState tm : TimeInStateList) {
        	hs.remove(tm.getStateName());
            System.out.println(tm.getStateName() + " " + tm.getElapsedTime() +
                " " + tm.getCountInState());
        }
        System.out.println("Unreached states "+hs);
    }

    public String TimeInStateString() {
        String s = "" + getName() + " ";
        for (TimeInState tm : TimeInStateList) {
            s += (tm.getStateName() + " " + tm.getElapsedTime() + " " +
            tm.getCountInState());
        }
        return s;
    }

    public void addTransitionInfo(String state, String[] successors,
        double[] probabilities) {
        int i = 0;
        for (String succ : successors) {
            TransitionInfo ti = new TransitionInfo();
            ti.setStartState(state);
            ti.setEndState(succ);
            ti.setProbValue(probabilities[i]);
            TransitionInfoList.add(ti);
            i++;
        }
    }

    public void replaceTransitions(ArrayList<TransitionInfo> tis) {
        for (TransitionInfo ti : tis) {
            replaceTransition(ti);
        }
    }

    public void replaceTransition(TransitionInfo ti) {
        if (ti.getStartState() != null && ti.getEndState() != null) {
            replaceTransition(ti.getStartState(), ti.getEndState(),
                ti.getProbValue());
        }
    }

    public void replaceTransition(String state, String succ, double prob) {
        TransitionInfo ti = this.getTransitionInfoFor(state, succ);
        if (ti != null) {
            double p = ti.getProbValue();
            ti.setProbValue(prob);
            return;
        }
        ti = new TransitionInfo();
        ti.setStartState(state);
        ti.setEndState(succ);
        ti.setProbValue(prob);
        TransitionInfoList.add(ti);
    }

    public ArrayList<TransitionInfo> lumpTransitionInfoList(String state,
        String endState, TransitionInfo[] tis, double[] occurProbs) {
        TransitionInfo tbase = getTransitionInfoFor(state, endState);
        if (tbase == null) {
            return TransitionInfoList;
        }
        double pbase = tbase.getProbValue();
        double sumOfOccur = 0;
        double sumOfWeightedTransProb = 0;
        int i = 0;
        for (TransitionInfo ti : tis) {
            double pv = ti.getProbValue();
            double pt = occurProbs[i];
            sumOfWeightedTransProb += pt * pv;
            sumOfOccur += pt;
            i++;
        }
        double pbleft = 1 - sumOfOccur;
        sumOfWeightedTransProb += pbleft * pbase;
        ArrayList<TransitionInfo> newtl = new ArrayList<TransitionInfo>();
        TransitionInfo tn = new TransitionInfo();
        tn.setStartState(state);
        tn.setEndState(endState);
        tn.setProbValue(sumOfWeightedTransProb);
        for (TransitionInfo ti : TransitionInfoList) {
            if (!ti.getStartState().equals(state)) {
                newtl.add(ti);
            } else {
                newtl.add(tn);
            }
        }
        return newtl;
    }

    public ArrayList<TransitionInfo> getTransitionInfoFor(String state) {
        ArrayList<TransitionInfo> sublist = new ArrayList<TransitionInfo>();
        ArrayList<TransitionInfo> l = getTransitionInfoList();
        for (TransitionInfo ti : l) {
            if (ti.getStartState().equals(state)) {
                sublist.add(ti);
            }
        }
        return sublist;
    }

    public TransitionInfo getTransitionInfoFor(String state, String succ) {
        ArrayList<TransitionInfo> l = getTransitionInfoFor(state);
        for (TransitionInfo ti : l) {
            if (ti.getEndState().equals(succ)) {
                return ti;
            }
        }
        return null;
    }

    public HashSet<String> getStates() {
        HashSet<String> str = new HashSet<String>();
        for (int i = 0; i < TransitionInfoList.size(); i++) {
            TransitionInfo ti = TransitionInfoList.get(i);
            str.add(ti.getStartState());
        }
        return str;
    }

    public ArrayList<String> indexStates() {
        HashSet<String> h = getStates();
        ArrayList<String> al = new ArrayList<String>();
        for (String str : h) {
            al.add(str);
        }
        return al;
    }

    public int getIndex(String state) {
        ArrayList<String> al = indexStates();
        for (int i = 0; i < al.size(); i++) {
            if (al.get(i).equals(state)) {
                return i;
            }
        }
        return -1;
    }

    public double[] getProbs(String state) {
        double[] probs = new double[getStates().size()];
        for (int i = 0; i < probs.length; i++) {
            probs[i] = 0;
        }
        for (int i = 0; i < TransitionInfoList.size(); i++) {
            TransitionInfo ti = TransitionInfoList.get(i);
            if (ti.getStartState().equals(state)) {
                int index = getIndex(ti.getEndState());
                probs[index] = ti.getProbValue();
            }
        }
        double sum = 0;
        for (int i = 0; i < probs.length; i++) {
            sum += probs[i];
        }
        if (sum <1)
        probs[getIndex(state)] = 1 - sum;
        return probs;
    }

    public ArrayList<String> getSuccs(String state) {
        ArrayList<String> ar = new ArrayList<String>();
        ArrayList<TransitionInfo> l = getTransitionInfoFor(state);
        for (int i = 0; i < l.size(); i++) {
            TransitionInfo ti = l.get(i);
            ar.add(ti.getEndState());
        }
        return ar;
    }

    public void setInitialStateAndTransitionMatrix(MarkovMat devs) {
        devs.stateVector = new double[getStates().size()];
        devs.stateVector[0] = 1;
        devs.lastStateVector = devs.stateVector;
        devs.TransitionMatrix = new Matrix(getStates().size());
        ArrayList<String> states = indexStates();
        for (int i = 0; i < states.size(); i++) {
            devs.TransitionMatrix.setColumn(i, getProbs(states.get(i)));
        }
        devs.TransitionMatrix.print();
       

    }

    public void setInitialStateAndTransitionList(ContinuousTimeMarkov devs) {
        devs.fillTransitionInfoList(devs.getName() + ".xml");
        devs.TimeInStateList = new ArrayList<TimeInState>();

       
    }
    
    public static ContinuousTimeMarkov setInitialStateAndTransitionList(int size,
            double prob) {
            ContinuousTimeMarkov ctm = new ContinuousTimeMarkov();
            MarkovMat mm = new MarkovMat();
            mm.setRand(new Random(234567));
            mm.setInitialStateAndTransitionMatrix(size, prob);
            ctm.TransitionInfoList = new ArrayList<TransitionInfo>();
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
                        probs[j] = p;
                    }
                ctm.addTransitionInfo(state, states, probs);
            }
            ctm.setTimeInStateList(new ArrayList<TimeInState>());
            return ctm;
        }

    //////////////////////////////// xml
    public static Document parseXmlFile(String fname) {

        //get the factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl",null);
        Document dom = null;
        try {

            //Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            //parse using builder to get DOM representation of the XML file
            File f = getDataFile(fname);
            dom = db.parse(f);

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return dom;
    }

    public void fillTransitionInfoList(String fname) {
        if (TransitionInfoList == null) {
            TransitionInfoList = new ArrayList<TransitionInfo>();
        }
        Document dom = parseXmlFile(fname);

        //get the root element
        Element docEle = dom.getDocumentElement();

        //get a nodelist of elements
        NodeList nl = docEle.getElementsByTagName("TransitionInfo");
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {

                //get the TransitionInfo element
                Element el = (Element) nl.item(i);

                //get the Employee object
                TransitionInfo ti = getTransitionInfo(el);

                //add it to list
                TransitionInfoList.add(ti);
            }
        }
    }

    public static TransitionInfo getTransitionInfo(Element TransEl) {
        String start = getTextValue(TransEl, "StartState");
        String end = getTextValue(TransEl, "EndState");
        double p = getDoubleValue(TransEl, "ProbValue");

        TransitionInfo ti = new TransitionInfo();
        ti.setStartState(start);
        ti.setEndState(end);
        ti.setProbValue(p);        
        return ti;
    }

    public static String getTextValue(Element ele, String tagName) {
        String textVal = null;
        NodeList nl = ele.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0) {
            Element el = (Element) nl.item(0);
            textVal = el.getFirstChild().getNodeValue();
        }

        return textVal;
    }

    public static Double getDoubleValue(Element ele, String tagName) {
        return Double.parseDouble(getTextValue(ele, tagName));
    }

    //ENDID

    // End custom function definitions
    public static void main(String[] args) {
        ContinuousTimeMarkov model = new ContinuousTimeMarkov();
    
    }

    public void addPropertyChangeListener(String propertyName,
        PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    // Getter/setter for Rand
    public void setRand(Random Rand) {
        propertyChangeSupport.firePropertyChange("Rand", this.Rand,
            this.Rand = Rand);
    }

    public Random getRand() {
        return this.Rand;
    }

    // End getter/setter for Rand

    // Getter/setter for AccLifeTime
    public void setAccLifeTime(double AccLifeTime) {
        propertyChangeSupport.firePropertyChange("AccLifeTime",
            this.AccLifeTime, this.AccLifeTime = AccLifeTime);
    }

    public double getAccLifeTime() {
        return this.AccLifeTime;
    }

    // End getter/setter for AccLifeTime

    // Getter/setter for TimeInStateList
    public void setTimeInStateList(ArrayList<TimeInState> TimeInStateList) {
        propertyChangeSupport.firePropertyChange("TimeInStateList",
            this.TimeInStateList, this.TimeInStateList = TimeInStateList);
    }

    public ArrayList<TimeInState> getTimeInStateList() {
        return this.TimeInStateList;
    }

    // End getter/setter for TimeInStateList

    // Getter/setter for TransitionInfoList
    public void setTransitionInfoList(
        ArrayList<TransitionInfo> TransitionInfoList) {
        propertyChangeSupport.firePropertyChange("TransitionInfoList",
            this.TransitionInfoList,
            this.TransitionInfoList = TransitionInfoList);
    }

    public ArrayList<TransitionInfo> getTransitionInfoList() {
        return this.TransitionInfoList;
    }

    // End getter/setter for TransitionInfoList

    // Getter/setter for AvgLifeTime
    public void setAvgLifeTime(double AvgLifeTime) {
        propertyChangeSupport.firePropertyChange("AvgLifeTime",
            this.AvgLifeTime, this.AvgLifeTime = AvgLifeTime);
    }

    public double getAvgLifeTime() {
        return this.AvgLifeTime;
    }

    // End getter/setter for AvgLifeTime

    // Getter/setter for Seed
    public void setSeed(long Seed) {
        propertyChangeSupport.firePropertyChange("Seed", this.Seed,
            this.Seed = Seed);
    }

    public long getSeed() {
        return this.Seed;
    }

    // End getter/setter for Seed

    // State variables
    public String[] getStateVariableNames() {
        return new String[] {
            "Rand", "AccLifeTime", "TimeInStateList", "TransitionInfoList",
            "AvgLifeTime", "Seed"
        };
    }

    public Object[] getStateVariableValues() {
        return new Object[] {
            Rand, AccLifeTime, TimeInStateList, TransitionInfoList, AvgLifeTime,
            Seed
        };
    }

    public Class<?>[] getStateVariableTypes() {
        return new Class<?>[] {
            Random.class, Double.class, ArrayList.class, ArrayList.class,
            Double.class, Long.class
        };
    }

    @SuppressWarnings("unchecked")
    public void setStateVariableValue(int index, Object value) {
        switch (index) {

            case ID_RAND:
                setRand((Random) value);
                return;

            case ID_ACCLIFETIME:
                setAccLifeTime((Double) value);
                return;

            case ID_TIMEINSTATELIST:
                setTimeInStateList((ArrayList<TimeInState>) value);
                return;

            case ID_TRANSITIONINFOLIST:
                setTransitionInfoList((ArrayList<TransitionInfo>) value);
                return;

            case ID_AVGLIFETIME:
                setAvgLifeTime((Double) value);
                return;

            case ID_SEED:
                setSeed((Long) value);
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
            dirUri = ContinuousTimeMarkov.class.getResource(".").toURI();
            dir = new File(dirUri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(
                "Could not find Models directory. Invalid model URL: " +
                ContinuousTimeMarkov.class.getResource(".").toString());
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
        return new String[] { "WaitForHello", "SayHi" };
    }
}
