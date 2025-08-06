package com.ms4systems.devs.core.util;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author bernie
 */

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Iterator;



import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.model.CoupledModel;
import com.ms4systems.devs.core.simulation.Simulation;
import com.ms4systems.devs.util.SimulationClassLoader;
@SuppressWarnings({"rawtypes","unused"})
public class MainForExecution extends ContentsWork {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
    static sesRelationExtend ses = null;
    static com.ms4systems.devs.core.util.pruningTable pruningTable = null;

    public MainForExecution() {
        workspace = System.getProperty("user.dir") +File.separator+ "src"+File.separator;
        projectNm = "";
        folderJava = workspace + projectNm +File.separator+ "Models"+File.separator+"java"+File.separator;
        packageNm = projectNm + ".Models.java.";
        folderTxt = workspace + projectNm +File.separator+ "Models"+File.separator+"txt"+File.separator;
        folderXml = workspace + projectNm +File.separator+ "Models"+File.separator+"xml"+File.separator;
    }


    public MainForExecution(String proName) {
		System.out.println("workspace : " + workspace);
		projectNm = proName;
		packageNm = "Models.java.";
    }
    public MainForExecution(String proName, boolean isAnimation) {
		System.out.println("workspace : " + workspace);
		projectNm = proName;
		if(isAnimation) packageNm = "Models.animation.";
		else packageNm = "Models.java.";
    }
    public MainForExecution(String projectName, String sesfile, String pesfile) throws ClassNotFoundException{
        this(projectName);
        doWork(sesfile, pesfile);
        String classNm = getName();
        doPostProcessing(new MainForExecution(projectName).folderPes + pesfile, this);
        System.out.println("************* start *********************");
        simulation(classNm);
    }
    public MainForExecution(String projectName, String sesfile, String pesfile, boolean isAnimation)throws ClassNotFoundException {
        this(projectName,isAnimation);
        doWork(sesfile, pesfile, isAnimation);
        String classNm = getName();
        classNm = sesRelationExtend.getNextAfterFirst(classNm);
        PESToDEVSOnTheFly.changeDigName(this, classNm);
        doPostProcessing(new MainForExecution(projectName,true).folderPes + pesfile, this,isAnimation);
    }
    public MainForExecution(String projectName, String sesfile, boolean pesfile)throws ClassNotFoundException {
        this(projectName);
        doWork(sesfile);
        String classNm = getName();
        classNm = sesRelationExtend.getNextAfterFirst(classNm);
        PESToDEVSOnTheFly.changeDigName(this, classNm);
        this.addAllInheritanceSelections(ses);
        com.ms4systems.devs.core.util.WriteClassFiles.mapDEVSToFileWExceptions(this, packageNm, folderJava, new HashSet<Object>());
    }

    public MainForExecution(String sesfile, String pesfile) throws ClassNotFoundException{
        this();
        doWork(sesfile, pesfile);
    }

    public MainForExecution(String projectName, String sesfile, String pesfile, String node, String specToExpand, int[] multint)throws ClassNotFoundException {
        this(projectName);
        doWork(sesfile, pesfile, node, specToExpand, multint);
        doPostProcessing(new MainForExecution(projectName).folderPes + pesfile, this);
    }

///////////////////////////////////////
    public static void RandomIteratePrunings(
            String projectName,
            String sesfile,
            String pesfile,
            int numberOfReps,
            long seed) throws ClassNotFoundException{
        generatePrunings.setSeed(seed);
        for (int i = 0; i < numberOfReps; i++) {
            new MainForExecution(projectName, sesfile, pesfile);
            
        }
    }

    public static void RandomIteratePrunings(
            String projectName,
            String sesfile,
            String pesfile,
            int numberOfReps,
            long seed,
            String node,
            String specToExpand) throws ClassNotFoundException{
        generatePrunings.setSeed(seed);
        for (int i = 0; i < numberOfReps; i++) {
            int multint[] = new int[1];
            multint[0] = i + 1;
            new MainForExecution(projectName, sesfile, pesfile, node, specToExpand, multint);
        }
    }

    public static void randomPruneTransformAndRun(
            String projectName,
            String sesfile,
            int numberOfReps,
            long seed) throws ClassNotFoundException{
        generatePrunings.setSeed(seed);
        for (int i = 0; i < numberOfReps; i++) {
            new MainForExecution(projectName, sesfile, true);
        }
    }

    public static void RandomIteratePrunings(
            String projectName,
            String sesfile,
            String pesfile,
            int numberOfReps) throws ClassNotFoundException{
        RandomIteratePrunings(projectName, sesfile, pesfile, numberOfReps, 458008753);
    }

    public static void randomPruneTransformAndRun(
            String projectName,
            String sesfile,
            int numberOfReps)throws ClassNotFoundException {
        randomPruneTransformAndRun(projectName, sesfile, numberOfReps, 458008753);
    }

    public static void RandomIteratePrunings(
            String projectName,
            String sesfile,
            String pesfile)throws ClassNotFoundException {
        RandomIteratePrunings(projectName, sesfile, pesfile, 1);
    }

    public static void RandomPrunings(
            String projectName,
            String sesfile)throws ClassNotFoundException {
        randomPruneTransformAndRun(projectName, sesfile, 1);
    }

    public static void EnumeratePruningsIterate(
            String projectName,
            String sesfile,
            String pesfile)throws ClassNotFoundException {

        MainForExecution pe =
                new MainForExecution(projectName);
        ses = pe.restructureNSave(sesfile, pesfile);
        enumeratePrunings.assignEnumerate(ses);
        pruningTable = pe.getPruneInfoNL(pesfile, ses);
        int cycleLength = enumeratePrunings.getCycleLength();
        System.out.println("Number of PESs is bounded by :" + cycleLength);
        for (int i = 0; i < cycleLength; i++) {
            pe = new MainForExecution(projectName);
            enumeratePrunings.doFirstPart((sesRelationExtend) ses, BasicWork.folderJava, pe.packageNm);
            String selects = enumeratePrunings.getCurrent();
            System.out.println("Current selects are :" + selects);
            enumeratePrunings.doLastPart((sesRelationExtend) ses,
                    pruningTable,
                    pe, BasicWork.folderJava, pe.packageNm);
            selects = enumeratePrunings.getCurrentOfCopy();
            System.out.println("Final selects are :" + selects);

            String classNm = pe.getName();
            classNm = sesRelationExtend.getNextAfterFirst(classNm);
            PESToDEVSOnTheFly.changeDigName(pe, classNm);
            doPostProcessing(pe.folderPes + pesfile, pe);
            long timeStamp = System.currentTimeMillis();
            String XMLfile = pe.folderXml + pe.getName() + timeStamp + "Inst.xml";
            String SchemaFile = pe.folderXml + pe.getName() + "Schema.xsd";

            pe.getCountBounds(pesfile);
            boolean countSatisfied = true;
            for (Object o : countBoundsSet) {
                countBounds cb = (countBounds) o;
                int count = 0;
                if (cb.parent == null) {
                    count = getNumberOf(cb.entity, "");
                    if (count <= cb.upper && count >= cb.lower) {
                        continue;
                    } else {
                        countSatisfied = false;
                        break;
                    }
                } else {
                    countSatisfied = parentCountSatisfied(cb, "");
                }
            }
            if (countSatisfied) {
                removeAllPathNPruneName(XMLfile);
                generatePruningsWInher.writePruneSchemaDoc(XMLfile, SchemaFile);
            } else {
                File f = new File(XMLfile);
                f.delete();
                return;
            }
        }
    }

    public static void randomPruneNoTransformWPartition(
            String projectName,
            String sesfile,
            String pesfile,
            String multEnt,
            HashSet<Object> commonPrefixes,
            int numberOfReps,
            long seed) {
        collectPartitions cp = new collectPartitions(
                new ContentsWork(projectName).folderSes, sesfile, multEnt, commonPrefixes);

        for (int i = 0; i < numberOfReps; i++) {
            generatePrunings.setSeed(seed + i * 9);
            String XMLFile = ContentsWork.createNewXMLInst(projectName, sesfile, pesfile);
            if (XMLFile.equals("")) {
                continue;
            }
            boolean newone = cp.checkAndAddNewPartition(XMLFile);
            if (!newone) {
                File f = new File(XMLFile);
                f.delete();
            }
        }
    }

    public static void randomPruneNoTransformWPartition(
            String projectName,
            String sesfile,
            String pesfile,
            String multEnt,
            int numberOfReps) {
        randomPruneNoTransformWPartition(projectName, sesfile,
                pesfile, multEnt, new HashSet<Object>(), numberOfReps,
                22367965);
    }

    public static void randomPruneNoTransform(
            String projectName,
            String sesfile,
            String pesfile,
            int numberOfReps,
            long seed) {
        for (int i = 0; i < numberOfReps; i++) {
            generatePrunings.setSeed(seed + i * 9);
            String XMLFile = ContentsWork.createNewXMLInst(projectName, sesfile, pesfile);
        }
    }

    public static void randomPruneNoTransform(
            String projectName,
            String sesfile,
            String pesfile,
            int numberOfReps) {
        randomPruneNoTransform(projectName, sesfile, pesfile, numberOfReps, 22367965);
    }

    public static void transformAfterPruning(
            String projectName,
            String pesfile,
            String XMLFile)throws ClassNotFoundException { //full path to file
        pruneOps.restorePruneDoc(XMLFile);
        generatePruningsWInherAuto.doInherit();
        MainForExecution pe = new MainForExecution(projectName);
        PESToDEVSOnTheFly.folder = BasicWork.folderJava;
        PESToDEVSOnTheFly.packageNm = pe.packageNm;
        PESToDEVSOnTheFly.toDEVS(pe);
        transferPairCoupling(pe);
        pe.doPostProcessing(pesfile);
    }
    public static void deleteAtomicFiles(AtomicModel iod, String projectName, String sesfile, String pesfile) {
        if (iod == null) {
            return;
        }
        ContentsWork pe = new ContentsWork(projectName);

        if (!(iod instanceof CoupledModel)) {
            String nm = iod.getName();
            File f = new File(BasicWork.folderJava + nm + ".java");
            f.delete();
            return;
        }
        CoupledModel dig = (CoupledModel) iod;
        Iterator<AtomicModel> it = dig.getChildren().iterator();
        while (it.hasNext()) {
            AtomicModel iod1 = (AtomicModel) it.next();
            deleteAtomicFiles(iod1, projectName, sesfile, pesfile);
        }
    }
    public void runSimView(AtomicModel model) {
		try {
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
    public void simulation(String className){
        String coupled = packageNm+className;
        
        Class c = null;
		Object o = null;
        try{
	        		BasicExecution.class.getClassLoader();
			o = c.newInstance();
        }catch(Exception e){
        	e.printStackTrace();
        }catch(Throwable e){
        	e.printStackTrace();
        	String msg = e.getMessage();
        }
        if (o!=null){
        	Simulation sim = new com.ms4systems.devs.core.simulation.impl.SimulationImpl(className +" Simulation",(CoupledModel) o);
			sim.startSimulation(0);
			sim.simulateIterations(Long.MAX_VALUE);
        }
        	     
    }
    
	public void openSimViewer(String pesfile){
		String className = pesfile.substring(0,pesfile.length()-4);
        String coupled = packageNm+className;
        
        Class c = null;
		Object o = null;
        try{
			o = c.newInstance();
        }catch(Exception e){
        	e.printStackTrace();
        }catch(Throwable e){
        	e.printStackTrace();
        	String msg = e.getMessage();
        }
        if (o!=null)
        	runSimView((CoupledModel) o);
        
	}
    public static void main(String[] args) {
    	new MainForExecution();
 try {
	EnumeratePruningsIterate("Chapter1", "SendReceivePairSeS.txt", "SendReceivePairSeSPES.txt");
} catch (ClassNotFoundException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}

    }
}


