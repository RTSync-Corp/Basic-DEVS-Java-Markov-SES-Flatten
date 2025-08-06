package com.ms4systems.devs.core.util;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;

import com.ms4systems.devs.analytics.InternalUseSeS;
import com.ms4systems.devs.core.message.Coupling;
import com.ms4systems.devs.core.message.impl.CouplingImpl;
import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.model.impl.CoupledModelImpl;
import com.ms4systems.devs.core.simulation.Simulation;
import com.ms4systems.devs.core.simulation.impl.SimulationImpl;

import com.ms4systems.devs.markov.ContinuousTimeMarkov;
 
public class SimulateModel {
	static double buildTime;
	static double simulationTime;
	
	public  AtomicModel model = null;
	 String folder = System.getProperty("user.dir") + File.separator;
	 
	 String foldertxt = folder + "src\\Models\\txt"+ File.separator;
	 String folderpes = folder + "src\\Models\\pes"+ File.separator;
	 String folderses = folder + "src\\Models\\ses"+ File.separator;	
	 
	 public  String resultsFile = foldertxt+"RunSims.txt";
	 public Simulation sim;
	 
	 public void setMaxSimulationTime(long simTime) {
		    if (this.sim != null)this.sim.setMaxSimulationTime(simTime);
	 }
	 public SimulateModel(){
	  		
		}
	public SimulateModel(AtomicModel myModel){
		setModel(myModel);
	}
	
	public void setSim(AtomicModel myModel){
		setModel(myModel);


  		sim = new com.ms4systems.devs.core.simulation.impl.
 			     SimulationImpl("Simulation",model);
	}
	public void setModelAndSim() {
		setSim(model);
	}
	////////////////////////////
	  public  ArrayList<AtomicModel> pruneNTransformInstanceWContents(CoupledModelImpl parent,String sesContents,String pesContents,String commonName){			
		  int numInstances = 0;
		  for (AtomicModel child : parent.getChildren()){
			    if (!child.getName().contains(commonName))continue;
              numInstances++;
	          }
		  String[] pesContentsAr = new String[numInstances];
		  for (int i = 0;i<numInstances;i++)
			  pesContentsAr[i]= pesContents;
			  return pruneNTransformInstancesWContents(parent,sesContents,pesContentsAr,commonName);
		  }
	  public  ArrayList<AtomicModel> 
	  pruneNTransformInstancesWContents(CoupledModelImpl parent,String sesContents,String[] pesContents,String commonName){			
		  TreeSet<String> ts = new TreeSet<String>();
		  for (AtomicModel child : parent.getChildren()){
			    if (!child.getName().contains(commonName))continue;
              ts.add(child.getName());
	          }
		  int i =0;
		  ArrayList<AtomicModel> lst = new ArrayList<AtomicModel>();
		  for (String child : ts){
			    sesRelationExtend rses = InternalUseSeS.getSesFromContents(sesContents,pesContents[i]);
		        AtomicModel am = (CoupledModelImpl)InternalUseSeS.pruneNTransToGetModelInstanceWContents(rses, pesContents[i]);
				i++;
				am.setName( child);
				lst.add(am);
	          }
			  return lst;
		  }		
	  
	////////////////////////////////////////////////////////////////////////
	  public  ArrayList<AtomicModel> pruneNTransformInstances(CoupledModelImpl parent,String sesfile,String pesfile){			
		  String clnm = sesfile.substring(0,sesfile.indexOf("."));
		  return pruneNTransformInstances(parent,sesfile,pesfile,clnm);
	  }
	  public  ArrayList<AtomicModel> pruneNTransformInstances(CoupledModelImpl parent,String sesfile,String pesfile,String commonName){			
		  int numInstances = 0;
		  for (AtomicModel child : parent.getChildren()){
			    if (!child.getName().contains(commonName))continue;
              numInstances++;
	          }
		  String[] pesfiles = new String[numInstances];
		  for (int i = 0;i<numInstances;i++)
			  pesfiles[i]= pesfile;
			  return pruneNTransformInstances(parent,sesfile,pesfiles,commonName);
		  }

	  public  ArrayList<AtomicModel> pruneNTransformInstances(CoupledModelImpl parent,String sesfile,String[] pesfiles,String commonName){			
		 
		  String sesContents = fileHandler.getContentsAsString(folderses+sesfile);
		  String[] pesContentsAr = new String[pesfiles.length];
		  for (int i = 0;i<pesfiles.length;i++)
			  pesContentsAr[i]= fileHandler.getContentsAsString(folderpes+pesfiles[i]);
		  return pruneNTransformInstancesWContents(parent,sesContents,pesContentsAr,commonName);
		  }		 
	  ///////////////////////////////////////////////////////////
	 
	  public  void replaceAllComponents(CoupledModelImpl parent,ArrayList<AtomicModel> reps){
  	  ArrayList<String> lst = new ArrayList<String>();
        for (AtomicModel child : parent.getChildren()){
      	  lst.add(child.getName());
        }
        for (String nm:lst){
      	  AtomicModel ch = parent.getComponentWithName(nm);
      	  AtomicModel rep = null;
      	  for (AtomicModel am:reps){
      		  if (am.getName().equals(nm)){
      			  rep = am;
      			  break;
      		  }
      	  }
      if (rep == null)continue;
      transferInternalCoupling(parent,ch,rep); 
      transferExternalCoupling(parent,ch,rep);
        }
  }
	  public  String idName(String nm){
		  int ind = nm.indexOf("_");
		  if (ind == -1)return "";
		  return nm.substring(0,ind);
	  }
	  public  void replaceAllComponents(CoupledModelImpl parent,ArrayList<AtomicModel> reps,String commonName){
	  	  ArrayList<String> lst = new ArrayList<String>();
	        for (AtomicModel child : parent.getChildren()){
	      	  lst.add(child.getName());
	        }
	        for (String nm:lst){
	      	  AtomicModel ch = parent.getComponentWithName(nm);
	      	  AtomicModel rep = null;
	      	  for (AtomicModel am:reps){
	      		  if (
	      				  am.getName().contains(commonName)
	      				  && nm.contains(commonName)
	      				  && idName(nm).equals(idName(am.getName()))
	      		       ){
	      			  rep = am;
	      			  break;
	      		  }
	      	  }
	      if (rep == null)continue;
	      transferInternalCoupling(parent,ch,rep); 
	      transferExternalCoupling(parent,ch,rep);
	        }
	  }
  public  void transferInternalCoupling(CoupledModelImpl parent,AtomicModel ch,AtomicModel rep){
  	  ArrayList coup1 = (ArrayList)parent.getCouplingsFor(ch);
	       for (Object co1:coup1){
	    	   CouplingImpl c1= (CouplingImpl)co1;
                    if (     
                    		!(c1.getDestination().equals(parent))
                    		&&
                    		c1.getSource().equals(ch)
                    		&&
                    		ch.hasOutputPort( c1.getSourcePort().getName())
                    		&&
                    		rep.hasOutputPort( c1.getSourcePort().getName())
                    		)		  	  
                     {
	    		   parent.removeCoupling(c1);
	    	       parent.addCoupling(
	    	    		   rep.getOutputPort(c1.getSourcePort().getName(),Serializable.class),
	    	    		   c1.getDestinationPort());	    		   
	               }
                    else if (
                    		!(c1.getSource().equals(parent))
                    		&&
                    		c1.getDestination().equals(ch)
                    		&&
                    		ch.hasInputPort( c1.getDestinationPort().getName())
                    		&&
                    		rep.hasInputPort( c1.getDestinationPort().getName())
                    		)		  	  
                    {
	    		   parent.removeCoupling(c1);
	    	       parent.addCoupling(
	    	    		   c1.getSourcePort(),
	    	    		   rep.getInputPort(c1.getDestinationPort().getName(),Serializable.class)
	    	    		   );		   
	               }
                    ArrayList coup1r = (ArrayList)parent.getCouplingsFor(rep);
                    coup1r = coup1r;
	    	   }
	    	ArrayList<Coupling> al = null;
	    	al =	parent.getCouplings();
	    	 parent.removeChildModel(ch);
	    	  parent.addChildModel(rep);

  }
    public  void transferExternalCoupling(CoupledModelImpl parent,AtomicModel ch,AtomicModel rep){
  	  CoupledModelImpl temp = new CoupledModelImpl();
  	  ArrayList coup1 = (ArrayList)parent.getCouplings();
  	       for (Object co1:coup1){
  	    	   CouplingImpl c1= (CouplingImpl)co1;
                      if (
                      	c1.getDestination().equals(ch)	
                      &&
                      rep.hasInputPort( c1.getDestinationPort().getName())
                    	&&
                    	parent.equals(c1.getSource())
                    	&&
                    	parent.hasInputPort(c1.getSourcePort().getName())	  
                      )	
                      
                       {
  	    	       temp.addCoupling(
  	    	    		 parent.getInputPort(c1.getDestinationPort().getName(),Serializable.class),
  	    	    		 rep.getInputPort(c1.getSourcePort().getName(),Serializable.class)
  	    	    		   );	    		   
  	               }
                      else  if (
                          	c1.getSource().equals(ch)	
                            &&
                            rep.hasOutputPort( c1.getSourcePort().getName())
                            	&&
                            	parent.equals(c1.getDestination())
                             	&&
                           	parent.hasOutputPort(c1.getDestinationPort().getName())	  
                            )
         	     temp.addCoupling(
         	    	rep.getOutputPort(c1.getSourcePort().getName(),Serializable.class),
         	  	     parent.getOutputPort(c1.getDestinationPort().getName(),Serializable.class)
         	  	     
         	  	     );
  	    	   }
  	       for (Object o:temp.getCouplings()){
  	    	   CouplingImpl cl= (CouplingImpl)o;
  	    	   if (cl.getSource().equals(parent))
  	    		   parent.addCoupling(cl.getSourcePort(),cl.getDestinationPort());
  	       }
  	    	ArrayList<Coupling> al = null;
    }
    /////////////////////////////
  
    public static ArrayList<AtomicModel> getAtomicModels(CoupledModelImpl cm){
    	ArrayList<AtomicModel> al = new ArrayList<AtomicModel>();
    	ArrayList<AtomicModel> cds = cm.getChildren();
    	for (AtomicModel cd:cds) {
    	if (cd instanceof CoupledModelImpl) {
    		al.addAll(getAtomicModels((CoupledModelImpl)cd));}
	         else al.add(cd);
    	}
    return al;
}
    		
    		
    public static AtomicModel getAtomicModelWithName(CoupledModelImpl cm,String compName) {
    	ArrayList<AtomicModel> al = getAtomicModels(cm);
    	for (AtomicModel am:al) {
    		if (am.getName().equals(compName))
    			return am;
    	}
      return null;
    }

	public static void setValue(AtomicModel model,String varName,Object varVal,Class classOfVar) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class cl = Class.forName("com.ms4systems.devs.examples.efp." + model.getName());
		
		String first = varName.substring(0,1).toUpperCase();
		varName = first+varName.substring(1);
		Method me = cl.getMethod("set"+varName,classOfVar);
		me.invoke(model,varVal);
	}
	public static Object getValue(AtomicModel model,String varName) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class cl = Class.forName("com.ms4systems.devs.examples.efp." + model.getName());
		
		String first = varName.substring(0,1).toUpperCase();
		varName = first+varName.substring(1);
		Method me = cl.getMethod("get"+varName);
		return me.invoke(model);
	}

	public static void setValue(CoupledModelImpl cm,String modelName,String varName,Object varVal,Class classOfVar) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		AtomicModel model = getAtomicModelWithName(cm,modelName);
		setValue(model,varName,varVal,classOfVar);
	}
	public static Object getValue(CoupledModelImpl cm,String modelName,String varName) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		AtomicModel model = getAtomicModelWithName(cm,modelName);
		return getValue(model,varName);
	}
	
	public  void SetValue(String modelName,String varName,Object varVal,Class classOfVar) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		AtomicModel model = getAtomicModelWithName((CoupledModelImpl)this.model,modelName);
		setValue(model,varName,varVal,classOfVar);
	}
	public  Object GetValue(String modelName,String varName) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		AtomicModel model = getAtomicModelWithName((CoupledModelImpl)this.model,modelName);
		return getValue(model,varName);
	}
	
	public  void setValue(String modelName,String varName,Object varVal,Class classOfVar)  {
		AtomicModel model = getAtomicModelWithName((CoupledModelImpl)this.model,modelName);
		try {
			setValue(model,varName,varVal,classOfVar);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public  Object getValue(String modelName,String varName) {
		AtomicModel model = getAtomicModelWithName((CoupledModelImpl)this.model,modelName);
		try {
			return getValue(model,varName);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return null;
	}
	///////////////////////////////
    public static ArrayList<AtomicModel> getAtomicModelsContainName(CoupledModelImpl cm,String compName) {
    	ArrayList<AtomicModel> res = new ArrayList<AtomicModel>();
    	ArrayList<AtomicModel> al = getAtomicModels(cm);
    	for (AtomicModel am:al) {
    		if (am.getName().contains(compName))
    			res.add(am);
    	}
      return res;
    }
	public  void setValuesForAll(String modelName,String varName,Object varVal,Class classOfVar)  {
		ArrayList<AtomicModel> al = getAtomicModelsContainName((CoupledModelImpl)this.model, modelName);
        for (AtomicModel am:al) {
		try {
			setValue(am,varName,varVal,classOfVar);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        }
	}
	public  ArrayList getValuesForAll(String modelName,String varName) {
		ArrayList<AtomicModel> al = getAtomicModelsContainName((CoupledModelImpl)this.model, modelName);
    	ArrayList res = new ArrayList();
		for (AtomicModel am:al) {
		try {
			res.add(getValue(am,varName));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		}
		return res;
	}
	///////////////////////////////
	public static String selectFrom(String[] choices) {
		if (choices.length == 0)
			return "";
		return choices[ContinuousTimeMarkov.Rand.nextInt(choices.length)];
	}
	public static String[] getChoices(sesRelationExtend ses, String specPart) {
		Hashtable hs = ses.specHasEntity();
		Set keys = hs.keySet();
		HashSet ents = new HashSet();
		for (Object k : keys) {
			String ke = (String) k;
			if (ke.contains(specPart))
				ents = (HashSet) hs.get(k);
		}
		String[] res = new String[ents.size()];
		int i = 0;
		for (Object o : ents) {
			res[i] = o.toString();
			i++;
		}
		return res;
	}
	///////////////////////////////
	public void setModel(AtomicModel myModel){
		model = myModel;
	}
	public void setResultsFile(String file){
		resultsFile = foldertxt+file;
	}
	public  void executeWView(AtomicModel model) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		execute( model,true);
	}
	public  void executeWView() throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		defineModel();
		execute( model,true);
	}
	public  void executeNoView(AtomicModel model) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		execute( model,false);
	}
	public  void execute(AtomicModel model) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		executeNoView( model);
	}
	public  void execute() throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		defineModel();
		if (model==null)
		System.out.println("No model defined");
		else executeNoView( model);
	}
	public  void execute(AtomicModel model,Boolean view) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

  	//	Simulation 

  		sim = new    SimulationImpl("Simulation",model);
  		//set the parameters for each run assuming non-default settings		
  		setParams();
  		//initialize the simulation
  			  			sim.startSimulation(0);
  		//do the simulation		
			 		sim.simulateIterations(Long.MAX_VALUE);
  					sim.stopSimulation();

  	//get and analyze the results
  	getAndAnalyzeResults();
}
	
	public void executeBuildOnce() throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		// only build first time
		if (model == null) {
			defineModel();
		}
		
		sim = new com.ms4systems.devs.core.simulation.impl.
 			     SimulationImpl("Simulation",model);
 		//set the parameters for each run assuming non-default settings	
 		setParams();
 		double start = System.currentTimeMillis();
 		//initialize the simulation
 		sim.startSimulation(0);
 		//do the simulation		
 		sim.simulateIterations(Long.MAX_VALUE);
 		sim.stopSimulation();
 		double end = System.currentTimeMillis();
 		
 		simulationTime = end - start;
		
 		//get and analyze the results
 		getAndAnalyzeResults();
	}
	
	public void doIt(int depth) {
		
	}
	public void executeMult(int depth) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		
		// only build first time
		if (model == null) {
			defineModel();
		}
		
		sim = new com.ms4systems.devs.core.simulation.impl.
 			     SimulationImpl("Simulation",model);
 		//set the parameters for each run assuming non-default settings	
 		setParams();
 		double start = System.currentTimeMillis();
//////////////////////////////////////////////////////////
 		doIt(depth);
 		double end = System.currentTimeMillis();
 		
 		simulationTime = end - start;
		
 		//get and analyze the results
 		getAndAnalyzeResults();
	}
	/////////////////definitions
	
	public void defineModel(){
    System.out.println("Define model");
	}

  	public  void setParams() throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
  	    System.out.println("Set parameters");
	}
  	public  void getAndAnalyzeResults() throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
  	   System.out.println("Get and analyze results");	
  	 }	  

	public void defineModel(String[] defaults){
	    System.out.println("Define model");
		}
	public  void executeExternal(String[] defaults) {
		defineModel(defaults);
		System.out.println("No model defined");
	}
	
	
	/// get times
	public double getSimulationTime() {
		return simulationTime;
	}
	
	public double getBuildTime() {
		return buildTime;
	}
	
	public void setBuildTime(double t) {
		buildTime = t;
	}
    public String[] getPhaseNames() {
        return new String[] { "requestState", "waitForState", "sendUpdate" };
    }
public static void main(String[] args) {
          
  	}

}
