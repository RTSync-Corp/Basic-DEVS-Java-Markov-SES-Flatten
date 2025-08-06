package com.ms4systems.devs.core.util;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import org.w3c.dom.*;

import com.ms4systems.devs.core.model.impl.AtomicModelImpl;
import com.ms4systems.devs.core.model.impl.CoupledModelImpl;


public class PESToDEVS extends generatePrunings {

    public static String packageNm = "", folder = "";
    
    // If this library is used in MS4Me Environment, isMS4MeEnv = true
    public static boolean isMS4MeEnv = true; 
 
    public PESToDEVS() {
    }
    
    public static String reconstructCoupling(String s, String orgName, String pruneName){
    	String couplings="";
    	while (true) {
			if (s.equals("")) {
				break;
			}
			int firstLeftParen = s.indexOf("{");
			int firstRightParen = s.indexOf("}");
			if (firstLeftParen > -1) {
				String fstring = s.substring(firstLeftParen + 1,
						firstRightParen);							
				Hashtable<Object,Object> f = parseCoupling(fstring);
				if(f.get("source").equals(orgName)){
					f.put("source", pruneName);
				}
				if(f.get("destination").equals(orgName)){
					f.put("destination", pruneName);
				}
				couplings += "{destination="+f.get("destination")+", outport="+f.get("outport")+", source="+
					f.get("source")+", inport="+f.get("inport")+"}\n";
				s = s.substring(firstRightParen + 1, s.length());
			}else{
				break;
			}
		}
		s = couplings;
	
    	return couplings;
    }
    public static void extractPorts(String s, CoupledModelImpl dig, String orgName) {
    	String digName = dig.getName();
    	if(!digName.equals(orgName)) s = reconstructCoupling(s,orgName,digName);
        while (true) {
            if (s.equals("{}")) {
                return;
            }
            int firstLeftParen = s.indexOf("{");
            int firstRightParen = s.indexOf("}");
            if (firstLeftParen > -1) {
                String fstring = s.substring(firstLeftParen + 1,
                        firstRightParen);
                Hashtable<Object,Object> f = parseCoupling(fstring);
                if (dig.getName().equals(//bpz 2010
                        (String) f.get("source"))) {
                //    dig.addInport((String) f.get("outport"));
dig.addInputPort((String) f.get("outport"));
                }
                if (dig.getName().equals(//bpz 2010
                        (String) f.get("destination"))) {
  dig.addOutputPort((String) f.get("inport"));
                }
                s = s.substring(firstRightParen + 1, s.length());
            } else {
                break;
            }
        }
    }
    public static void extractPorts(String s, CoupledModelImpl dig) {
        while (true) {
            if (s.equals("{}")) {
                return;
            }
            int firstLeftParen = s.indexOf("{");
            int firstRightParen = s.indexOf("}");
            if (firstLeftParen > -1) {
                String fstring = s.substring(firstLeftParen + 1,
                        firstRightParen);
                Hashtable<Object,Object> f = parseCoupling(fstring);
                if (dig.getName().equals(//bpz 2010
                        (String) f.get("source"))) {
dig.addInputPort((String) f.get("outport"));
                }
                if (dig.getName().equals(//bpz 2010
                        (String) f.get("destination"))) {
  dig.addOutputPort((String) f.get("inport"));
                }
                s = s.substring(firstRightParen + 1, s.length());
            } else {
                break;
            }
        }
    }
 

    public static Hashtable<Object,Object> parseCoupling(String fstring) {
        Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        String[] pairString = fstring.split(",");
        if(pairString.length==4){
        	for(String pair : pairString){
        		String[] element = pair.trim().split("=");
        		if(element.length==2){
        			f.put(element[0], element[1]);
        		}
        	}
        }
        return f;
    }



    public static boolean isLeaf(Element entity) {
        Element aspectsOf = null;
        LinkedList<Object> q = getActualChildren(entity);
        Iterator it = q.iterator();
        while (it.hasNext()) {
            Element ch = (Element) it.next();
            if (ch.getNodeName().startsWith("aspectsOf")) {
                aspectsOf = ch;
            }
            break;
        }
        if (aspectsOf == null) {
            return true;
        }
        Element aspect = (Element) getActualChildren(aspectsOf).getFirst();
        return (aspect == null);
    }



    public static String adapt(String coupling, String specNm, String digNm) {
        String s = coupling;

        if (!specNm.contains("_")) {
        	if(specNm.startsWith(digNm)){
            	int length = digNm.length();
                String temp = specNm.substring(length,specNm.length());
                boolean isNumeric = temp.matches("[0-9]*");

    	        if(isNumeric){
    	        	s= reconstructCoupling(s,digNm, specNm);
    	        }
    	        
    	    }
            return s;
        } //no rewrite unless this is a true spec
        
        if (digNm.equals(specNm)) { //allow spec having coupling
            String rs = myReplaceAllSpecCoupling(s, digNm, specNm);
            return rs;
        }
        
        if (specNm.endsWith(digNm)) { //allow for suffix at end of name
            return s;
        }
             
        
        if(specNm.startsWith(digNm)){
        	int length = digNm.length();
            String temp = specNm.substring(length,specNm.length());
            boolean isNumeric = temp.matches("[0-9]*");

	        if(isNumeric){
	        	String rs = myReplaceAllSpecCoupling(s, digNm, specNm);
	            return rs;
	        }
        }
                
        ///

        String rs = myReplaceAll(s, digNm, specNm);
        return rs;
    }

    public static String myReplaceAll(String s, String digNm, String specNm) {
        if (digNm.equals(specNm)) {
            return s;//bpz 2010
        }
        if(specNm.contains("_")){
        	String sourceDigNm = "";
        	// extract all entities' names from specNm to compare each name to coupling information (08/05/2015 cs)
            
            String[] entityArray = specNm.split("_");
            for(String entityName : entityArray){
            	sourceDigNm = entityName;
            	//check if sourceDigNm is an entity in the pruned ses(6/16/2015 cs)
              if(!PESToDEVSOnTheFly.entitiesInPrune.contains(sourceDigNm)){
              	//search a right entity from PESToDEVSOnTheFly.entitiesInPrune (6/16/2015 cs)
              	for(String entity : PESToDEVSOnTheFly.entitiesInPrune ){
              		if(specNm.contains(entity)){
              			sourceDigNm = entity;
              			break;
              		}
              	}
              } 
            }
            

            HashSet<Object> couplingSet = getCouplingSet(s);
            Iterator it = couplingSet.iterator();
            while (it.hasNext()) {
                Hashtable<Object,Object> f = (Hashtable<Object,Object>) it.next();
                //check all entity in the specNm (08/05/2015 cs)
                for(String entityName : entityArray){
                	sourceDigNm = entityName;
	                if (sourceDigNm.equals(f.get("source").toString())){
	                    f.put("source", specNm);
	                }
	                 if (sourceDigNm.equals(f.get("destination").toString())){
	                    f.put("destination", specNm);
	                }
                }
            }
            String res = couplingSet.toString();
            res = res.substring(1, res.length() - 1);
            return res;
        }else {
        	HashSet<Object> couplingSet = getCouplingSet(s);
            Iterator it = couplingSet.iterator();
            while (it.hasNext()) {
                Hashtable<Object,Object> f = (Hashtable<Object,Object>) it.next();
                
                if (specNm.endsWith(f.get("source").toString())){
                    f.put("source", specNm);
                }
                 if (specNm.endsWith(f.get("destination").toString())){
                    f.put("destination", specNm);
                }
            }
            String res = couplingSet.toString();
            res = res.substring(1, res.length() - 1);
            return res;
        }        
    }
   
    public static String myReplaceAllSpecCoupling(String s, String digNm, String specNm) {
        String sourceDigNm = digNm;
        int ind = specNm.lastIndexOf("_");
        if (ind > -1) {
        	if(specNm.startsWith(digNm)){
            	int length = digNm.length();
                String temp = specNm.substring(length,specNm.length());
                boolean isNumeric = temp.matches("[0-9]*");

    	        if(isNumeric){
    	        	sourceDigNm = digNm.substring(ind + 1);//sourceDigNm is the dig name
    	        }else{
    	        	sourceDigNm = specNm.substring(ind + 1);//sourceDigNm is the spec name
    	        }
            }else sourceDigNm = specNm.substring(ind + 1);//sourceDigNm is the spec name
        }

        HashSet<Object> couplingSet = getCouplingSet(s);
        Iterator it = couplingSet.iterator();
        while (it.hasNext()) {
            Hashtable<Object,Object> f = (Hashtable<Object,Object>) it.next();
            if (f.get("source").equals(sourceDigNm)) {
                f.put("source", specNm);
            }
            if (f.get("destination").equals(sourceDigNm)) {
                f.put("destination", specNm);
            }
        }
        String res = couplingSet.toString();
        res = res.substring(1, res.length() - 1);
        return res;
    }


    public static HashSet<Object> getCouplingSet(String s) {
        HashSet<Object> es = new HashSet<Object>();
        while (true) {
            if (s.equals("{}")) {
                return es;
            }
            int firstLeftParen = s.indexOf("{");
            int firstRightParen = s.indexOf("}");
            if (firstLeftParen > -1) {
                String fstring = s.substring(firstLeftParen + 1,
                        firstRightParen);
                Hashtable<Object,Object> f = parseCoupling(fstring);
                es.add(f);

                s = s.substring(firstRightParen + 1, s.length());
            } else {
                return es;
            }
        }
    }

    public static HashSet<Object> getInports(HashSet<Object> couplingSet,
            String compNm) {
        HashSet<Object> es = new HashSet<Object>();
        Iterator it = couplingSet.iterator();
        while (it.hasNext()) {
            Hashtable<Object,Object> f = (Hashtable<Object,Object>) it.next();
            if (f.get("destination").equals(compNm)) {
                es.add(f.get("inport"));
            }
        }
        return es;
    }

    public static HashSet<Object> getOutports(HashSet<Object> couplingSet,
            String compNm) {
        HashSet<Object> es = new HashSet<Object>();
        Iterator it = couplingSet.iterator();
        while (it.hasNext()) {
            Hashtable<Object,Object> f = (Hashtable<Object,Object>) it.next();
            if (f.get("source").equals(compNm)) {
                es.add(f.get("outport"));
            }
        }
        return es;
    }

    public static void addInports(AtomicModelImpl vc, String couplingString) {
        HashSet<Object> couplingSet = getCouplingSet(couplingString);
        HashSet<Object> inports = getInports(couplingSet, vc.getName());
        Iterator it = inports.iterator();
        while (it.hasNext()) {
            String inport = (String) it.next();
            vc.addInputPort(inport);
        }
    }

      public static void addOutports(AtomicModelImpl vc, String couplingString) {
    		       
    	HashSet<Object> couplingSet = getCouplingSet(couplingString);
        HashSet<Object> outports = getOutports(couplingSet, vc.getName());
        Iterator it = outports.iterator();
        while (it.hasNext()) {
            String outport = (String) it.next();
            vc.addOutputPort(outport);
        }
    }

    public static Pair getIOPair(String compNm, String couplingString) {
        HashSet<Object> couplingSet = getCouplingSet(couplingString);
        HashSet<Object> outports = getOutports(couplingSet, compNm);
        HashSet<Object> inports = getInports(couplingSet, compNm);
        return new Pair(inports, outports);
    }

    public static Hashtable<Object,HashSet<Object>> getIORelation(LinkedList<Object> comps,
            String couplingString, Hashtable<Object,HashSet<Object>> r) {
        Iterator it = comps.iterator();
        while (it.hasNext()) {
            String compNm = (String) it.next();
            if(r.containsKey(compNm)){
            	r.get(compNm).add(getIOPair(compNm, couplingString));
            }else{
            	HashSet<Object> valSet = new HashSet<Object>();
            	valSet.add(getIOPair(compNm, couplingString));
            	r.put(compNm, valSet);
            }
        }
        return r;
    }

    public static Hashtable<Object,HashSet<Object>> getIOInfo() {   //assumes com.ms4systems.devs.core.pes restored
        Element specCh = pruneOps.getIterSubClass(pruneRoot);
        String pruneName = specCh.getAttribute("pruneName");
        if (pruneName.equals("")) {
            pruneName = specCh.getNodeName();
        }
        return getIOInfoEntity(pruneRoot, new Hashtable<Object,HashSet<Object>>());
    }

    public static Hashtable<Object,HashSet<Object>> getIOInfoEntity(Element entity, Hashtable<Object,HashSet<Object>> r) {
        Element aspectsOf = null;
        LinkedList<Object> q = getActualChildren(entity);
        Iterator it = q.iterator();
        while (it.hasNext()) {
            Element ch = (Element) it.next();
            if (ch.getNodeName().startsWith("aspectsOf")) {
                aspectsOf = ch;
            }
            break;
        }
        if (aspectsOf == null) {
            return r;
        }
        Element aspect = (Element) getActualChildren(aspectsOf).getFirst();
        String aspNm = aspect.getNodeName();
        if (aspNm.endsWith("MA") || aspNm.endsWith("multiAsp") || aspNm.endsWith("MultiAsp")) {
            return r;
        } else {
            return getIOInfoAspect(aspect, r);
        }
    }

    public static Hashtable<Object,HashSet<Object>> getIOInfoAspect(Element aspect, Hashtable<Object,HashSet<Object>> r) {
        String s = aspect.getAttribute("coupling");
        s = s.trim();
        LinkedList<Object> components = getActualChildren(aspect);
        Iterator it = components.iterator();
        while (it.hasNext()) {
            Element ch = (Element) it.next();
            Element specCh = pruneOps.getIterSubClass(ch);
            String pruneName = specCh.getAttribute("pruneName");
            if (pruneName.equals("")) {
                pruneName = specCh.getNodeName();
            }
            s = adapt(s, pruneName, ch.getNodeName());
            if(r.containsKey(pruneName)){
            	r.get(pruneName).add(getIOPair(pruneName, s));
            }else{
            	HashSet<Object> valSet = new HashSet<Object>();
            	valSet.add(getIOPair(pruneName, s));
            	r.put(pruneName, valSet);
            }
            getIOInfoEntity(ch, r);
        }
        return r;
    }

    public static void main(String argv[]) {
    } // main
}

