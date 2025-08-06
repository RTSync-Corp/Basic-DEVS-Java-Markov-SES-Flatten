package com.ms4systems.devs.core.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;


import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.model.CoupledModel;
import com.ms4systems.devs.core.model.impl.CoupledModelImpl;
import com.ms4systems.devs.core.util.sesRelationExtend.pruningRule;
import com.ms4systems.devs.core.util.sesRelationExtend.pruningRules;
import com.ms4systems.devs.util.SimulationClassLoader;


@SuppressWarnings({"rawtypes","unused"})
public class BasicExecution extends contextPrune{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public sesRelationExtend rses;
	
    public BasicExecution() {
        workspace = System.getProperty("user.dir") +File.separator+ "src"+File.separator;
        projectNm = "";
        folderJava = workspace + projectNm +File.separator+ "Models"+File.separator+"java"+File.separator;
        packageNm = projectNm + ".Models.java.";
        folderTxt = workspace + projectNm +File.separator+ "Models"+File.separator+"txt"+File.separator;
        folderXml = workspace + projectNm +File.separator+ "Models"+File.separator+"xml"+File.separator;    
        
    }

    public BasicExecution(String proName) {

        
		System.out.println("workspace : " + workspace);
		projectNm = proName;

		packageNm = "Models.java.";

    }
    public BasicExecution(String proName, boolean isAnimation) {
    	
		System.out.println("workspace : " + workspace);
		projectNm = proName;

		if(isAnimation) packageNm = "Models.animation.";
		else packageNm = "Models.java.";
    }

    public BasicExecution(String sesfile, String pesfile)throws ClassNotFoundException {
        this();
        doWork(sesfile, pesfile);
    }
    public BasicExecution(String proName,String sesfile, String pesfile)throws ClassNotFoundException {
        this(proName);
        doWork(sesfile, pesfile);
    }
    public BasicExecution(String proName,String sesfile, String pesfile,boolean isAnimation) throws ClassNotFoundException{
        this(proName, isAnimation);
        doWork(sesfile, pesfile,isAnimation);
    }
    public void doWork(String sesfile, String pesfile) throws ClassNotFoundException{
    	hasError = false;
    	entityToPrune="";
    	sesRelationExtend.multiplicityFn = new Hashtable<Object, Object>();
    	sesRelationExtend.multNumtoEnt = new Hashtable<Object,Object>();
    	sesRelationExtend.nameGen=new Hashtable<Object, Integer>();
    	sesRelationExtend.subSeSList=new ArrayList<String>();
    	PESToDEVSOnTheFly.inheritanceSelections = new HashSet<Object>();
    	// save all entity names to use to make couplings (6/16/2015 cs)
    	PESToDEVSOnTheFly.entitiesInPrune = new HashSet<String>();
    	sesRelationExtend.allActionEnt = new HashSet<String>();
    	sesRelationExtend.specToEnt = new Hashtable<Object,Object>();
    	natLangToSes.entList = new ArrayList<String>();
    	
    	
    	generatePruningsWInher.newRoot = null;
    	generatePruningsWInher.newPrefix ="";
    	
    	generateMethodSummary.nameBag = new Hashtable<Object,Integer>();
    	
    	sesRelationExtend rses = mergeAllForSeS(sesfile,pesfile);
    	rses.printTree();
    	String name = rses.getRootEntityName();
    	rses = restructureNSave(rses,sesfile,pesfile);
    	
    	String sesDoc = natLangToSes.convertEntityNL(rses.getRootEntityName(), rses);

        rses.doExtendedCoupling(folderSes+sesfile);
        for(String subSesFile : sesRelationExtend.subSeSList){
        	rses.doExtendedCoupling(folderSes+subSesFile);
        }
        // if there is a multiple aspect with specialization, pruning rules are created with "all" keyword in a ses file
        // CS 11/01/12
        pruningRuleForMultiplicity(rses);
        

        deleteFiles(rses,false);

        com.ms4systems.devs.core.util.pruningTable pruningTable = getPruneInfoNL(pesfile, rses);
                
        pruneAndTransform(rses, pruningTable);
       
       doPostProcessing(pesfile);        

    }
    public sesRelationExtend doWorkInstance(String sesfile, String pesfile){
    	hasError = false;
    	entityToPrune="";
    	sesRelationExtend.multiplicityFn = new Hashtable<Object, Object>();
    	sesRelationExtend.multNumtoEnt = new Hashtable<Object,Object>();
    	sesRelationExtend.nameGen=new Hashtable<Object, Integer>();
    	sesRelationExtend.subSeSList=new ArrayList<String>();
    	PESToDEVSOnTheFly.inheritanceSelections = new HashSet<Object>();
    	PESToDEVSOnTheFly.entitiesInPrune = new HashSet<String>();
    	sesRelationExtend.allActionEnt = new HashSet<String>();
    	sesRelationExtend.specToEnt = new Hashtable<Object,Object>();
    	natLangToSes.entList = new ArrayList<String>();
    	
    	
    	generatePruningsWInher.newRoot = null;
    	generatePruningsWInher.newPrefix ="";
    	
    	generateMethodSummary.nameBag = new Hashtable<Object,Integer>();
    	
    	sesRelationExtend rses = mergeAllForSeSInstance(sesfile,pesfile);
    	rses.printTree();
    	String name = rses.getRootEntityName();
    	rses = restructureNSave(rses,sesfile,pesfile);
    	
    	String sesDoc = natLangToSes.convertEntityNL(rses.getRootEntityName(), rses);

        rses.doExtendedCoupling(folderSes+sesfile);
        for(String subSesFile : sesRelationExtend.subSeSList){
        	rses.doExtendedCoupling(folderSes+subSesFile);
        }
        // if there is a multiple aspect with specialization, pruning rules are created with "all" keyword in a ses file
        // CS 11/01/12
        pruningRuleForMultiplicity(rses);
        
        return rses;
        
        
    }
    // Generate sesRelationExtend with ses and pes contents (cseo 6/18/2018)
    public sesRelationExtend doWorkInstanceWContent(String sesContent, String pesContent){
    	hasError = false;
    	entityToPrune="";
    	sesRelationExtend.multiplicityFn = new Hashtable<Object, Object>();
    	sesRelationExtend.multNumtoEnt = new Hashtable<Object,Object>();
    	sesRelationExtend.nameGen=new Hashtable<Object, Integer>();
    	sesRelationExtend.subSeSList=new ArrayList<String>();
    	PESToDEVSOnTheFly.inheritanceSelections = new HashSet<Object>();
    	// save all entity names to use to make couplings (6/16/2015 cs)
    	PESToDEVSOnTheFly.entitiesInPrune = new HashSet<String>();
    	sesRelationExtend.allActionEnt = new HashSet<String>();
    	sesRelationExtend.specToEnt = new Hashtable<Object,Object>();
    	natLangToSes.entList = new ArrayList<String>();
    	
    	
    	generatePruningsWInher.newRoot = null;
    	generatePruningsWInher.newPrefix ="";
    	
    	generateMethodSummary.nameBag = new Hashtable<Object,Integer>();
    	
    	sesRelationExtend rses = mergeAllForSeSWContents(sesContent,pesContent);
    	rses.printTree();
    	String name = rses.getRootEntityName();
    	rses = restructureNSaveWContent(rses,sesContent,pesContent);
    	
    	String sesDoc = natLangToSes.convertEntityNL(rses.getRootEntityName(), rses);
        rses.doExtendedCouplingWContent(sesContent);
        for(String subSesFile : sesRelationExtend.subSeSList){
        	rses.doExtendedCoupling(folderSes+subSesFile);
        }
        // if there is a multiple aspect with specialization, pruning rules are created with "all" keyword in a ses file
        // CS 11/01/12
        pruningRuleForMultiplicity(rses);
        
        return rses;
        
        
    }
    public void doWork(String sesfile, String pesfile,boolean isAnimation) throws ClassNotFoundException{

    	hasError = false;
    	entityToPrune="";
    	sesRelationExtend.multiplicityFn = new Hashtable<Object, Object>();
    	sesRelationExtend.multNumtoEnt = new Hashtable<Object,Object>();
    	sesRelationExtend.nameGen=new Hashtable<Object, Integer>();
    	sesRelationExtend.subSeSList=new ArrayList<String>();
    	PESToDEVSOnTheFly.inheritanceSelections = new HashSet<Object>();
    	sesRelationExtend.allActionEnt = new HashSet<String>();
    	sesRelationExtend.specToEnt = new Hashtable<Object,Object>();
    	natLangToSes.entList = new ArrayList<String>();
    	
    	
    	generatePruningsWInher.newRoot = null;
    	generatePruningsWInher.newPrefix ="";
    	
    	generateMethodSummary.nameBag = new Hashtable<Object,Integer>();
    	
    	sesRelationExtend rses = mergeAllForSeS(sesfile,pesfile);
    	rses.printTree();
    	String name = rses.getRootEntityName();
    	rses = restructureNSave(rses,sesfile,pesfile);
    	
    	String sesDoc = natLangToSes.convertEntityNL(rses.getRootEntityName(), rses);

        rses.doExtendedCoupling(folderSes+sesfile);
        for(String subSesFile : sesRelationExtend.subSeSList){
        	rses.doExtendedCoupling(folderSes+subSesFile);
        }
        // if there is a multiple aspect with specialization, pruning rules are created with "all" keyword in a ses file
        // CS 11/01/12
        pruningRuleForMultiplicity(rses);
        
        deleteFiles(rses,false);
        
        com.ms4systems.devs.core.util.pruningTable pruningTable = getPruneInfoNL(pesfile, rses);
        pruneAndTransform(rses, pruningTable,isAnimation);
        doPostProcessing(pesfile,isAnimation);
        
        this.rses = rses;

    }
    public sesRelationExtend restructureNSave(String sesfile, String pesfile) {

    	extractingMergeAll(folderSes,pesfile);
    	extractSubSeS();
        
    	sesRelationExtend rses = new sesRelationExtend(folderSes, sesfile,"HandlePESOperation");
        
        sesRelationExtend.pruningRules pr = rses.PruningRules;
        doRestructureMultiplicityInfoNL(pesfile);
        if (sesRelationExtend.multiplicityFn.size() == 0) {
            sesRelation ses = new sesRelation();
            Vector<String> vRestructure = getRestructureInfoNL(pesfile);
            if(vRestructure.size()==0){ // There is no restructuring information
            	String spec = "";
                ses = restructureAndSave(sesfile, spec);
            }else { // handle multi restructuring information
	            for(String specPlusThenSeq : vRestructure){
		            int ind = specPlusThenSeq.indexOf(":");
		            if (ind < 0) {
		                String spec = specPlusThenSeq;
		                 ses = restructureAndSave(sesfile, spec);
		            } else {
		                String spec = specPlusThenSeq.substring(0, ind);
		                ses = restructureAndSave(sesfile, spec);
		                String thenSeq = specPlusThenSeq.substring(ind);
		                java.util.HashSet<String> hs = restructureInfo.getList(thenSeq);
		                for (String sp : hs) {
		                    ses = restructureAndSave(ses, sesfile, sp);
		                }
		            }
	            }
            }
            
            rses = new sesRelationExtend(ses);
            rses.PruningRules = pr;
            
        } else {
        	Enumeration<Object> e = sesRelationExtend.multiplicityFn.keys();
        	while(e.hasMoreElements()){
        		Object key = e.nextElement();
        		Object val = sesRelationExtend.multiplicityFn.get(key);
        		if (rses.isSpecOfMultiAsp((String) key)) {
                    continue;
                }
                rses.expandSpec((String) key,
                        (int[]) val);
        	}
            
            doRestructureMultiAspects(pesfile, rses);
        }
        addCommonVariables(rses, folderSes + sesfile);
        for(String subSesFile : sesRelationExtend.subSeSList){
        	addCommonVariables(rses, folderSes + subSesFile);
        }
        return rses;
    }
    // using a sesRelationExtend class for restructureNSave
    public sesRelationExtend restructureNSave(sesRelationExtend rses, String sesfile, String pesfile){
    	sesRelationExtend.pruningRules pr = rses.PruningRules;
        doRestructureMultiplicityInfoNL(pesfile);
        if (sesRelationExtend.multiplicityFn.size() == 0) {
        	
            sesRelation ses = new sesRelation();
            Vector<String> vRestructure = getRestructureInfoNL(pesfile);
            if(vRestructure.size()==0){ // There is no restructuring information
            	String spec = "";
                ses = restructureAndSave(sesfile, spec);
            }else { // handle multi restructuring information
            	int i=0;
	            for(String specPlusThenSeq : vRestructure){
		            int ind = specPlusThenSeq.indexOf(":");
		            if (ind < 0) {
		                String spec = specPlusThenSeq;
		                if(i==0){
		                	ses = restructureAndSave(sesfile, spec);
		                }else{
		                	ses = restructureAndSave(ses, sesfile, spec);
		                }
		            } else {
		                String spec = specPlusThenSeq.substring(0, ind);
		                //ses = restructureAndSave(sesfile, spec);
		                if(i==0){
		                	ses = restructureAndSave(sesfile, spec);
		                }
		                String thenSeq = specPlusThenSeq.substring(ind);
		                java.util.HashSet<String> hs = restructureInfo.getList(thenSeq);
		                for (String sp : hs) {
		                    ses = restructureAndSave(ses, sesfile, sp);
		                }
		            }
		            i++;
	            }
            }
		
            rses = new sesRelationExtend(ses);
            rses.PruningRules = pr;
        } else {
        	// get specToEntity (cs 11/01/12)
        	Enumeration en = sesRelationExtend.multiplicityFn.keys();
        	while(en.hasMoreElements()){
        		String key = (String)en.nextElement();
        		String ent ="";
        		if(rses.specHasEntity.containsKey(key)){
        			HashSet<Object> entSet = rses.specHasEntity.get(key);
        			Iterator<Object> it = entSet.iterator();
        			while(it.hasNext()){
        				ent = (String)it.next();
        				break;
        			} 
        			sesRelationExtend.specToEnt.put(key, ent);
        		}        		
        	}
        	// ------------------------------------------------------------
        	Enumeration<Object> e = sesRelationExtend.multiplicityFn.keys();
        	while(e.hasMoreElements()){
        		Object key = e.nextElement();
        		Object val = sesRelationExtend.multiplicityFn.get(key);
        		if (rses.isSpecOfMultiAsp((String) key)) {
                    continue;
                }
                rses.expandSpec((String) key,
                        (int[]) val);
        	}
            
            doRestructureMultiAspects(pesfile, rses);
        }
    	return rses;
    }
    
    // using a sesRelationExtend class for restructureNSave
    public sesRelationExtend restructureNSaveWContent(sesRelationExtend rses, String sesContent, String pesContent){
    	sesRelationExtend.pruningRules pr = rses.PruningRules;
    	doRestructureMultiplicityInfoNLWContent(pesContent);
        if (sesRelationExtend.multiplicityFn.size() == 0) {
        	
            sesRelation ses = new sesRelation();
            Vector<String> vRestructure = getRestructureInfoNLWContent(pesContent);
            if(vRestructure.size()==0){ // There is no restructuring information
            	String spec = "";
                ses = restructureAndSaveWContent(sesContent, spec);
            }else { // handle multi restructuring information
            	int i=0;
	            for(String specPlusThenSeq : vRestructure){
		            int ind = specPlusThenSeq.indexOf(":");
		            if (ind < 0) {
		                String spec = specPlusThenSeq;
		                if(i==0){
		                	ses = restructureAndSaveWContent(sesContent, spec);
		                }else{
		                	ses = restructureAndSave(ses, sesContent, spec);
		                }
		            } else {
		                String spec = specPlusThenSeq.substring(0, ind);
		                //ses = restructureAndSave(sesfile, spec);
		                if(i==0){
		                	ses = restructureAndSaveWContent(sesContent, spec);
		                }
		                String thenSeq = specPlusThenSeq.substring(ind);
		                java.util.HashSet<String> hs = restructureInfo.getList(thenSeq);
		                for (String sp : hs) {
		                    ses = restructureAndSave(ses, sesContent, sp);
		                }
		            }
		            i++;
	            }
            }
            rses = new sesRelationExtend(ses);
            rses.PruningRules = pr;
        } else {
        	// get specToEntity (cs 11/01/12)
        	Enumeration en = sesRelationExtend.multiplicityFn.keys();
        	while(en.hasMoreElements()){
        		String key = (String)en.nextElement();
        		String ent ="";
        		if(rses.specHasEntity.containsKey(key)){
        			HashSet<Object> entSet = rses.specHasEntity.get(key);
        			Iterator<Object> it = entSet.iterator();
        			while(it.hasNext()){
        				ent = (String)it.next();
        				break;
        			} 
        			sesRelationExtend.specToEnt.put(key, ent);
        		}        		
        	}
        	// ------------------------------------------------------------
        	Enumeration<Object> e = sesRelationExtend.multiplicityFn.keys();
        	while(e.hasMoreElements()){
        		Object key = e.nextElement();
        		Object val = sesRelationExtend.multiplicityFn.get(key);
        		if (rses.isSpecOfMultiAsp((String) key)) {
                    continue;
                }
                rses.expandSpec((String) key,
                        (int[]) val);
        	}
            
        	doRestructureMultiAspectsWContent(pesContent, rses);
        }
    	return rses;
    }
    public sesRelationExtend genMergedSeSRelationExtend(String sesfile) {
    	sesRelationExtend rses = new sesRelationExtend(folderSes, sesfile,"HandlePESOperation");
    	
    	addCommonVariables(rses, folderSes + sesfile);
        for(String subSesFile : sesRelationExtend.subSeSList){
        	addCommonVariables(rses, folderSes + subSesFile);
        }
    	return rses;
    }
    // use a ses content (cseo 6/18/2018)
    public sesRelationExtend genMergedSeSRelationExtendWContent(String sesContent) {
    	sesRelationExtend rses = new sesRelationExtend(folderSes, sesContent,true);
    	
    	addCommonVariablesWContent(rses, sesContent);
        for(String subSesFile : sesRelationExtend.subSeSList){
        	addCommonVariables(rses, folderSes + subSesFile);
        }
    	return rses;
    }

    public static void addCommonVariables(sesRelationExtend rses, String folderPlusesesfile) {
        String contents = fileHandler.getContentsAsString(folderPlusesesfile);
        restructureInfo.parseNAdd(contents, rses);
    }

    // with a content (cseo 6/18/2018)
    public static void addCommonVariablesWContent(sesRelationExtend rses, String sesContent) {
        restructureInfo.parseNAdd(sesContent, rses);
    }

    public static void createFolders(String project) {
        workspace = System.getProperty("user.dir") +File.separator+ "src"+File.separator;
        createDirectory(workspace, project);
        initialize(workspace + project + File.separator);
    }

    public static void createDirectory(String workspace, String relativePath) {
        File file = new File(workspace + relativePath);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public static void initialize(String workspace) {
        System.out.println("***** workspace : " + workspace);

        String folderModels = workspace + "Models" + File.separator;
        createDirectory(workspace, "Models");

        String folderModelsJava = folderModels + "java"
                + File.separator;
        createDirectory(workspace, "Models" + File.separator + "java");
        
        String folderModelsXml = folderModels + "xml" + File.separator;
        createDirectory(workspace, "Models" + File.separator + "xml");

        String folderModelsTxt = folderModels + "txt" + File.separator;
        createDirectory(workspace, "Models" + File.separator + "txt");
        if (!new File(folderModelsTxt + "DummyFDDEVS.txt").exists()) {
            fileHandler.writeToFile(
                    folderModelsTxt + "DummyFDDEVS.txt", "");
            fileHandler.writeToFile(folderModelsTxt + "DummySeS.txt",
                    "");
        }
        String folderModelsTest = folderModels + "test"
                + File.separator;
        createDirectory(workspace, "Models" + File.separator + "test");

        String folderModelsFeedback = folderModels + "feedback"
                + File.separator;
        createDirectory(workspace, "Models" + File.separator
                + "feedback");

        
        String folderModelsDoc = folderModels + "doc" + File.separator;
        createDirectory(workspace, "Models" + File.separator + "doc");
    }

    public sesRelation restructureAndSave(String sesFileNm, String specNm) {
        return restructureAndSave(sesFileNm, folderSes, specNm);
    }
    // With a ses content (cseo 6/19/2018)
    public sesRelation restructureAndSaveWContent(String sesContent, String specNm) {
        return restructureAndSaveWithContent(sesContent, folderSes, specNm);
    }
    public sesRelation restructureAndSave(sesRelation ses, String sesFileNm, String specNm) {
        return restructureAndSave(ses, sesFileNm, folderSes, specNm);
    }
    
    public sesRelation restructureAndSave(String sesFileNm) {
        return restructureAndSave(sesFileNm, folderSes, "");
    }



    public void pruneAndTransform(sesRelation ses, com.ms4systems.devs.core.util.pruningTable pruningTable) throws ClassNotFoundException{
        pruneNTransform((sesRelationExtend)ses, (CoupledModelImpl)this, pruningTable, folderJava, packageNm);
    }

    public void pruneAndTransform(sesRelationExtend ses, com.ms4systems.devs.core.util.pruningTable pruningTable) throws ClassNotFoundException{
        pruneAndTransform(ses, pruningTable, ses.PruningRules);
    }
    public void pruneAndTransform(sesRelationExtend ses, com.ms4systems.devs.core.util.pruningTable pruningTable, boolean isAnimation)throws ClassNotFoundException {
        pruneAndTransform(ses, pruningTable, ses.PruningRules, isAnimation);
    }
    public void pruneAndTransform(sesRelationExtend ses, com.ms4systems.devs.core.util.pruningTable pruningTable, sesRelationExtend.pruningRules pr) throws ClassNotFoundException{
    	// handling multSpec information
    	if(pruningTable.getMultiSpec().size()!=0){
    		for(String parent : pruningTable.getMultiSpec()){
    			
    			for(Object o : ses.getEnsembleSet("entityNames")){
    				String entity = o.toString();
    				if(entity.contains("_"+parent)){
    					String[] pairs = pruningTable.getPairs(parent);
    					String[] newPairs = new String[pairs.length];
    					for(int i = 0; i < pairs.length ; i++){
    						String newPair =pairs[i].replace(parent, entity);
    						newPairs[i]=newPair;
    					}
    					pruningTable.addPairs(entity, newPairs);
    				} 
    			}    			
    		}
    	}
        HashSet<Object> toAdd = new HashSet<Object>();
        for (Object o : ses.getEnsembleSet("entityNames")) {
            String entity = o.toString();
            String[] pairs = pruningTable.getPairs(entity);
            if (pairs != null && pairs.length > 0) {
                for (String str : pairs) {
                    HashSet<Object> es = pr.getAction(entity, str);
                    for (Object oo:es){
	                    Pair act = (Pair)oo;
	                    if (act != null) {
	                        toAdd.add(act);
	                    }
                    }
                }
            }
            for (Object oo : toAdd) {
                Pair act = (Pair) oo;
                pruningTable.addPair((String) act.getKey(), (String) act.getValue());
            }
        }
        pruneNTransform((sesRelationExtend) ses, this, pruningTable, folderJava, packageNm);
    }
	 public void pruneAndTransform(sesRelationExtend ses, com.ms4systems.devs.core.util.pruningTable pruningTable, sesRelationExtend.pruningRules pr, boolean isAnimation) throws ClassNotFoundException{
	     HashSet<Object> toAdd = new HashSet<Object>();
	     for (Object o : ses.getEnsembleSet("entityNames")) {
	         String entity = o.toString();
	         String[] pairs = pruningTable.getPairs(entity);
	         if (pairs != null && pairs.length > 0) {
	             for (String str : pairs) {
	                 HashSet<Object> es = pr.getAction(entity, str);
	                 for (Object oo:es){
	                 Pair act = (Pair)oo;
	                 if (act != null) {
	                     toAdd.add(act);
	                 }
	                   }
	             }
	         }
	         for (Object oo : toAdd) {
	             Pair act = (Pair) oo;
	             pruningTable.addPair((String) act.getKey(), (String) act.getValue());
	         }
	     }
	     if(isAnimation)pruneNTransform((sesRelationExtend) ses, this, pruningTable, folderAnimation, packageNm);
	     else pruneNTransform((sesRelationExtend) ses, this, pruningTable, folderJava, packageNm);
	 }

    public com.ms4systems.devs.core.util.pruningTable getPruneInfoNL(String natLangFile, sesRelationExtend ses) {
        if (natLangFile.equals("")) {
            return new com.ms4systems.devs.core.util.pruningTable();
        }
        String contents = fileHandler.getContentsAsString(folderPes
                + natLangFile);
        if (contents == null) {
            System.out.println("wrong file path");
            return null;
        }
        return parseNInterpret(contents, ses);
    }

    public Vector<String> getRestructureInfoNL(String natLangFile) {
        if (natLangFile.equals("")) {
            return new Vector<String>();
        }
        String contents = fileHandler.getContentsAsString(folderPes
                + natLangFile);
        if (contents == null) {
            System.out.println("wrong file path");
            return null;
        }
        return restructureInfo.parseNReturn(contents);
    }
    // With a pes content (cseo 6/18/2018)
    public Vector<String> getRestructureInfoNLWContent(String pesContent) {
       
        if (pesContent == null) {
            System.out.println("No pes content");
            return null;
        }
        return restructureInfo.parseNReturn(pesContent);
    }
    public void doRestructureMultiAspects(String natLangFile, sesRelationExtend ses) {
        if (natLangFile.equals("")) {
            return;
        }
        String contents = fileHandler.getContentsAsString(folderPes
                + natLangFile);
        if (contents == null) {
            System.out.println("wrong file path");
            return;
        }
        restructureInfo.parseNRestructure(contents, ses);
    }
    
    public void doRestructureMultiAspectsWContent(String pesContent, sesRelationExtend ses) {
        
        if (pesContent == null) {
            System.out.println("wrong file path");
            return;
        }
        restructureInfo.parseNRestructure(pesContent, ses);
    }
    public void extractingMergeAll(String folderSeS,String pesFile){
    	if (pesFile.equals("")) {
        }
        String contents = fileHandler.getContentsAsString(folderPes
                + pesFile);
        if (contents == null) {
            System.out.println("wrong file path");
            return;
        }
        restructureInfo.parseForMergeAll(folderSeS,contents);
    }
    public void extractingMergeAllWContent(String folderSeS,String pesContent){
    	
        if (pesContent == null) {
            System.out.println("wrong file path");
            return;
        }
        restructureInfo.parseForMergeAll(folderSeS,pesContent);
    }
    public void extractingSubSeSFiles(String pesFile){
    	if (pesFile.equals("")) {
        }
        String contents = fileHandler.getContentsAsString(folderPes
                + pesFile);
        if (contents == null) {
            System.out.println("wrong file path");
            return;
        }
        restructureInfo.parseForSubSeS(contents);
    }
    public void extractingSubStructure(String pesFile){
    	if (pesFile.equals("")) {
        }
        String contents = fileHandler.getContentsAsString(folderPes
                + pesFile);
        if (contents == null) {
            System.out.println("wrong file path");
            return;
        }
        restructureInfo.parseForSubStructure(contents);
    }
    public void extractingSubtractSeSFiles(String pesFile){
    	if (pesFile.equals("")) {
        }
        String contents = fileHandler.getContentsAsString(folderPes
                + pesFile);
        if (contents == null) {
            System.out.println("wrong file path");
            return;
        }
        restructureInfo.parseForSubtractSeS(contents);
    }
    public void doRestructureMultiplicityInfoNL(String natLangFile) {
        if (natLangFile.equals("")) {
        }
        String contents = fileHandler.getContentsAsString(folderPes
                + natLangFile);
        if (contents == null) {
            System.out.println("wrong file path");
            return;
        }
        restructureInfo.parseForMultiplicity(contents);
    }
    // With a pes Content (cseo 6/18/2018)
    public void doRestructureMultiplicityInfoNLWContent(String pesContent) {
        
        if (pesContent == null) {
            System.out.println("No pes content");
            return;
        }
        restructureInfo.parseForMultiplicity(pesContent);
    }
    public String doPostProcessing(String natLangFile) {
        if (natLangFile.equals("")) {
            return "";
        }
        String contents = fileHandler.getContentsAsString(folderPes
                + natLangFile);
        if (contents == null) {
            System.out.println("wrong file path");
            return null;
        }
        return parseNPerform(contents, this);
    }
    public String doPostProcessing(String natLangFile, boolean isAnimation) {
        if (natLangFile.equals("")) {
            return "";
        }
        String contents = fileHandler.getContentsAsString(folderPes
                + natLangFile);
        if (contents == null) {
            System.out.println("wrong file path");
            return null;
        }
        return parseNPerform(contents, this, isAnimation);
    }
    public static String doPostProcessing(String folderPlusPesfile, BasicWork pe) {
        String contents = fileHandler.getContentsAsString(folderPlusPesfile);
        return parseNPerform(contents, pe);
    }


    static class pruningTable extends com.ms4systems.devs.core.util.pruningTable {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		pruningTable() {
            super();
        }

        pruningTable(sesRelation ses) {
            super(ses);
        }
	}
    
	public void deleteFiles(sesRelationExtend rses, boolean isAnimation){
		if(isAnimation){
			File animationFolder = new File(folderAnimation);
			List<String> animationfileSet = null;
			if(animationFolder.isDirectory()){
				String[] fileList = animationFolder.list();
				animationfileSet = Arrays.asList(fileList);
			}
			for(Object en : rses.getEntitySet()){
				String entityName = en.toString();
				
				if(animationfileSet.contains(entityName+".java")){
					 File f = new File(folderAnimation + entityName + ".java");
			         f.delete();
			         System.out.println("deleted file : "+entityName+".java");
				}
	        	System.out.println("entity name : "+en.toString());
	        }
			
		}else {
			File dnlfolder = new File(folderDnl);
			File javaFolder = new File(folderJava);
			List<String> dnlfileSet = null;
			List<String> javafileSet = null;
			
			if(dnlfolder.isDirectory()){
				String[] fileList = dnlfolder.list();
				dnlfileSet = Arrays.asList(fileList);
			}
			if(javaFolder.isDirectory()){
				String[] fileList = javaFolder.list();
				javafileSet = Arrays.asList(fileList);
			}
			for(Object en : rses.getEntitySet()){
				String entityName = en.toString();
				if(detetableEntities(rses,entityName)){
					if(dnlfileSet.contains(entityName+".dnl")) continue;
					if(javafileSet.contains(entityName+".java")){
						
						 File f = new File(folderJava + entityName + ".java");
				         f.delete();
				         System.out.println("deleted file : "+entityName+".java");
					}
				}
	        	System.out.println("entity name : "+en.toString());
	        }
		}
	}
	public boolean detetableEntities(sesRelationExtend rses, String entity){
		sesRelation sesrel = rses.substructure(entity);
		if(sesrel.getAspectNames().isEmpty()&& sesrel.getMultiAspectNames().isEmpty()){
			return true;
		}		
		
		return false;
	}
	public static void deleteAtomicFiles(AtomicModel iod, String projectName, String sesfile, String pesfile, boolean isAnimation) {
        if (iod == null) {
            return;
        }
        ContentsWork pe = new ContentsWork(projectName);

        if (!(iod instanceof CoupledModel)) {
            String nm = iod.getName();
            if(isAnimation){
            	File f = new File(pe.folderAnimation + nm + ".java");
	            f.delete();
            }else{
	            File f = new File(BasicWork.folderJava + nm + ".java");
	            f.delete();
            }
            return;
        }
        CoupledModel dig = (CoupledModel) iod;
        Iterator<AtomicModel> it = dig.getChildren().iterator();
        while (it.hasNext()) {
            AtomicModel iod1 = (AtomicModel) it.next();
            deleteAtomicFiles(iod1, projectName, sesfile, pesfile,isAnimation);
        }
    }
	public void extractSubSeS(){
		Queue<String> sesQueue = new LinkedList<String>(sesRelationExtend.subSeSList);
		String sesName = sesQueue.poll();
		if(sesName!= null){
			sesRelationExtend ses = new sesRelationExtend(folderSes, sesName);
	        HashSet<Object> set = ses.getEntitySet();
	        File sesFolder = new File(folderSes);
	        List<String> sesList = new ArrayList<String>();
	        if(sesFolder.isDirectory()){
	        	String[] fileList = sesFolder.list();
	        	sesList = Arrays.asList(fileList);
	        }
	        for(Object ent : set){
            	String entName = ent.toString();
            	if(ses.getRootEntityName().equals(entName))continue; // Not add root entity
            	for(String fileName : sesList){
            		if(fileName.equals(sesName)||fileName.contains("Merged"))continue; // Not add a parent SeS file
            		sesRelationExtend subSeS = new sesRelationExtend(folderSes,fileName);
            		String rootEntity = subSeS.getRootEntityName();
            		if(rootEntity.equals(entName)){
            			if(!sesQueue.contains(fileName)&&!sesRelationExtend.subSeSList.contains(fileName)){
                    		sesRelationExtend.addSubSeSFile(fileName);
                    		sesQueue.add(fileName);
                    		System.out.println("merged ses file : "+fileName);
                		}
            		}
            		
            	}
            }
	        sesName = sesQueue.poll();
	        if(sesName!=null){
	        	getSubSeS(sesQueue, sesName);
	        }
		}
	}
	public void getSubSeS(Queue<String> sesQueue,String sesName){
		sesRelationExtend ses = new sesRelationExtend(folderSes, sesName);
        HashSet<Object> set = ses.getEntitySet();
        File sesFolder = new File(folderSes);
        List<String> sesList = new ArrayList<String>();
        if(sesFolder.isDirectory()){
        	String[] fileList = sesFolder.list();
        	sesList = Arrays.asList(fileList);
        }
        for(Object ent : set){
        	String entName = ent.toString();
        	if(ses.getRootEntityName().equals(entName))continue; // Not add root entity
        	for(String fileName : sesList){
        		if(fileName.equals(sesName))continue; // Not add a parent SeS file
        		sesRelationExtend subSeS = new sesRelationExtend(folderSes,fileName);
        		String rootEntity = subSeS.getRootEntityName();
        		if(rootEntity.equals(entName)){
        			if(!sesQueue.contains(fileName)&&!sesRelationExtend.subSeSList.contains(fileName)){
                		sesRelationExtend.addSubSeSFile(fileName);
                		sesQueue.add(fileName);
                		System.out.println("merged ses file : "+fileName);
            		}
        		}
        		
        	}
        }
        sesName = sesQueue.poll();
        if(sesName!=null){
        	getSubSeS(sesQueue, sesName);
        }
	}
	public void writeSeSDoc(String name,String s){
		try {
            OutputStream stream = new FileOutputStream(folderSes + name + ".ses");
            stream.write(s.getBytes());
            stream.close();
            System.out.println("File was created/rewritten: "
                    + folderSes + name + ".ses");
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	public void mergeAllForSeS(String sesfile){
		
    	sesRelationExtend.subSeSList=new ArrayList<String>();    	
    	natLangToSes.entList = new ArrayList<String>();
    	
		populateSubSeSList(sesfile);
		if(sesRelationExtend.subSeSList.size()== 0){

			return;
		}
    	extractSubSeS();
    	
		sesRelationExtend rses = genMergedSeSRelationExtend(sesfile);
        String name = rses.getRootEntityName();
		System.out.println(" Root Model Name : "+name);
        rses.doExtendedCoupling(folderSes+sesfile);
        for(String subSesFile : sesRelationExtend.subSeSList){
        	rses.doExtendedCoupling(folderSes+subSesFile);
        }
      
        if(sesRelationExtend.subSeSList.size()!=0){
	        String sesDoc = natLangToSes.convertEntityNL(rses.getRootEntityName(), rses);
	        sesDoc += natLangToSes.convertPruningRulesToNL(rses);
	        System.out.println("merged SeS Doc : \n"+sesDoc);
	        writeSeSDoc(rses.getRootEntityName()+"Merged",sesDoc);
        }        
        
 
	}
	public sesRelationExtend mergeAllForSeS(String sesfile,String pesfile){
		
		extractingMergeAll(folderSes,pesfile);
    	extractSubSeS();
    	
		sesRelationExtend rses = genMergedSeSRelationExtend(sesfile);
        String name = rses.getRootEntityName();
		System.out.println(" Root Model Name : "+name);
        rses.doExtendedCoupling(folderSes+sesfile);
        for(String subSesFile : sesRelationExtend.subSeSList){
        	rses.doExtendedCoupling(folderSes+subSesFile);
        }
      
        if(sesRelationExtend.subSeSList.size()!=0){
        	//Initialize entity list
        	natLangToSes.entList= new ArrayList<String>();
	        String sesDoc = natLangToSes.convertEntityNL(rses.getRootEntityName(), rses);
	        sesDoc += natLangToSes.convertPruningRulesToNL(rses);
	        System.out.println("merged SeS Doc : \n"+sesDoc);
	        writeSeSDoc(rses.getRootEntityName()+"Merged",sesDoc);
        }        
        
        return rses;
	}
public sesRelationExtend mergeAllForSeSInstance(String sesfile,String pesfile){
		
		extractingMergeAll(folderSes,pesfile);
    	extractSubSeS();
    	
		sesRelationExtend rses = genMergedSeSRelationExtend(sesfile);
        String name = rses.getRootEntityName();
		System.out.println(" Root Model Name : "+name);
        rses.doExtendedCoupling(folderSes+sesfile);
        for(String subSesFile : sesRelationExtend.subSeSList){
        	rses.doExtendedCoupling(folderSes+subSesFile);
        }
      
        if(sesRelationExtend.subSeSList.size()!=0){
        	//Initialize entity list
        	natLangToSes.entList= new ArrayList<String>();
	        String sesDoc = natLangToSes.convertEntityNL(rses.getRootEntityName(), rses);
	        sesDoc += natLangToSes.convertPruningRulesToNL(rses);
	        System.out.println("merged SeS Doc : \n"+sesDoc);
        }        
        
        return rses;
	}
	// using ses and pes contents (cseo 6/18/2018) 
	public sesRelationExtend mergeAllForSeSWContents(String sesContent,String pesContent){
		
		extractingMergeAllWContent(folderSes,pesContent);
		extractSubSeS();
		
		sesRelationExtend rses = genMergedSeSRelationExtendWContent(sesContent);
	    String name = rses.getRootEntityName();
		System.out.println(" Root Model Name : "+name);
	    rses.doExtendedCouplingWContent(sesContent);
	    for(String subSesFile : sesRelationExtend.subSeSList){
	    	rses.doExtendedCoupling(folderSes+subSesFile);
	    }
	  
	    if(sesRelationExtend.subSeSList.size()!=0){
	    	//Initialize entity list
	    	natLangToSes.entList= new ArrayList<String>();
	        String sesDoc = natLangToSes.convertEntityNL(rses.getRootEntityName(), rses);
	        sesDoc += natLangToSes.convertPruningRulesToNL(rses);
	        System.out.println("merged SeS Doc : \n"+sesDoc);
	    }        
	    
	    return rses;
	}
	public void populateSubSeSList(String sesfile){
		sesRelationExtend ses = new sesRelationExtend(folderSes, sesfile);
        HashSet<Object> set = ses.getEntitySet();
        File sesFolder = new File(folderSes);
        List<String> sesList = new ArrayList<String>();
        if(sesFolder.isDirectory()){
        	String[] fileList = sesFolder.list();
        	sesList = Arrays.asList(fileList);
        }
        for(Object ent : set){
        	String entName = ent.toString();
        	if(ses.getRootEntityName().equals(entName))continue; // Not add root entity
        	for(String fileName : sesList){
        		if(fileName.equals(sesfile)||fileName.contains("Merged"))continue; // Not add a parent SeS file
        		sesRelationExtend subSeS = new sesRelationExtend(folderSes,fileName);
        		String rootEntity = subSeS.getRootEntityName();
        		if(rootEntity.equals(entName)){
        			if(!sesRelationExtend.subSeSList.contains(fileName)){
                		sesRelationExtend.addSubSeSFile(fileName);
                		System.out.println("merged ses file : "+fileName);
            		}
        		}
        		
        	}

        }
	}
	public static void pruningRuleForMultiplicity(sesRelationExtend rses){
		for(String entSpec : sesRelationExtend.allActionEnt){
        	Enumeration en = sesRelationExtend.multiplicityFn.keys();
        	while(en.hasMoreElements()){
        		String key = (String)en.nextElement();
        		if(key.contains(entSpec)){
        			int[] mult = (int[])sesRelationExtend.multiplicityFn.get(key);
            		int num = mult[0];
            		System.out.println("number of multiplicity : "+num);
            		String entName = (String)sesRelationExtend.specToEnt.get(key);
            		System.out.println("SpecToEnt : "+entName);
            		pruningRules pruningRules = new pruningRules();
            		Iterator it = rses.PruningRules.iterator();
            		while(it.hasNext()){
            			pruningRule pr = (pruningRule)it.next();
            			System.out.println("entity : "+pr.entity);
            			System.out.println("condition : "+pr.condition);
            			System.out.println("condition : "+pr.action.key.toString());
            			System.out.println("action : "+pr.action.value.toString());
            			if(pr.action.key.toString().equals(entSpec)){
            				
            				for(int i = 0; i < num; i++){
            					String oldKey = pr.action.key.toString();
            					String keyEnt = entName+i+"_"+oldKey;
            					String value = pr.action.value.toString();
            					value.replace(oldKey, keyEnt);
            					
            					pruningRules.add(pr.entity, pr.condition, new Pair(keyEnt,value));
            				}
            			}else {
            				pruningRules.add(pr.entity, pr.condition, new Pair(pr.action.key,pr.action.value));
            			}
            		}
            		rses.PruningRules = pruningRules;
        		}
        	}        	
        }		
	}
	
}
