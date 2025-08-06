/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ms4systems.devs.core.util;

/**
 *
 * @author Bernie
 */

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.w3c.dom.Element;

import com.ms4systems.devs.core.model.impl.CoupledModelImpl;

/**
 *
 * @author Bernie
 */
@SuppressWarnings({"rawtypes","unused"})
public class enumeratePrunings extends contextPrune {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static SeriesOfEnumerate enumerateSeries = new SeriesOfEnumerate();
    public static SeriesOfEnumerate copyOfEnumerateSeries = new SeriesOfEnumerate();
    static Hashtable<Object,Object> pathToItem = new Hashtable<Object,Object>();
    public static Hashtable<Object,Object> itemToPath = new Hashtable<Object,Object>();

    public static void resetCounters() {
        enumerateSeries = new SeriesOfEnumerate();
    }

    public static void assignEnumerate(sesRelation ses) {
        String entToPrune = ses.getRootEntityName();
        createPruneDoc(ses);
        if (!entityToPrune.equals("")) {
            entToPrune = entityToPrune;
            ses = ses.substructure(entToPrune);
            createPruneDoc(ses);
        }
        assignEnumerate(ses, entToPrune, pruneOps.pruneRoot);
    }

    public static void assignEnumerateUniform(sesRelation ses) {
        String entToPrune = ses.getRootEntityName();
        createPruneDoc(ses);
        if (!entityToPrune.equals("")) {
            entToPrune = entityToPrune;
            ses = ses.substructure(entToPrune);
            createPruneDoc(ses);
        }
        assignEnumerateUniform(ses, entToPrune, pruneOps.pruneRoot);
    }

    public static void assignEnumerate(sesRelation ses, String entity, Element el) {
        if (ses.isLeaf(entity)) {
            return;
        }
        addEntEnumerate(el);
        LinkedList<Object> specs = generatePrunings.getSpecializationElementsOfEntity(el);
        LinkedList<Object> asps = generatePrunings.getAspectElementsOfEntity(el);
        for (Object a : asps) {
            Element asp = (Element) a;
            LinkedList<Object> chds = pruneOps.getActualChildren(asp);
            Iterator it = chds.iterator();
            while (it.hasNext()) {
                Element ch = (Element) it.next();
                assignEnumerate(ses, ch.getNodeName(), ch);
            }
        }
        for (Object s : specs) {
            Element spec = (Element) s;
            LinkedList<Object> cds = pruneOps.getActualChildren(spec);
            Iterator ic = cds.iterator();
            while (ic.hasNext()) {
                Element ch = (Element) ic.next();
                assignEnumerate(ses, ch.getNodeName(), ch);
            }

        }
    }

    public static void assignEnumerateUniform(sesRelation ses, String entity, Element el) {
        if (ses.isLeaf(entity)) {
            return;
        }
        addEntEnumerateUniform(el);
        LinkedList<Object> specs = generatePrunings.getSpecializationElementsOfEntity(el);
        LinkedList<Object> asps = generatePrunings.getAspectElementsOfEntity(el);
        for (Object a : asps) {
            Element asp = (Element) a;
            LinkedList<Object> chds = pruneOps.getActualChildren(asp);
            Iterator it = chds.iterator();
            while (it.hasNext()) {
                Element ch = (Element) it.next();
                assignEnumerateUniform(ses, ch.getNodeName(), ch);
            }
        }
        for (Object s : specs) {
            Element spec = (Element) s;
            LinkedList<Object> cds = pruneOps.getActualChildren(spec);
            Iterator ic = cds.iterator();
            while (ic.hasNext()) {
                Element ch = (Element) ic.next();
                assignEnumerateUniform(ses, ch.getNodeName(), ch);
            }

        }
    }

    public static void doFirstPart(sesRelation ses, 
            String folder, String packageNm) {
        String entToPrune = ses.getRootEntityName();
        createPruneDoc(ses);
        if (!entityToPrune.equals("")) {
            entToPrune = entityToPrune;
            ses = ses.substructure(entToPrune);
            createPruneDoc(ses);
        }
        String selects = doOneEntStep();
    }

    public static void doLastPart(sesRelationExtend ses,
            pruningTable pruningTable,
            CoupledModelImpl devs,
            String folder, String packageNm) throws ClassNotFoundException{
        String entToPrune = ses.getRootEntityName();
        copyOfEnumerateSeries = enumerateSeries.copy();

        pruneEntityWEnumerate(ses, pruningTable, folder, packageNm,
                entToPrune, pruneOps.pruneRoot);
        generatePruningsWInherAuto.doInherit();
        contextPrune.accumulateAllCommonVars(ses, entToPrune, pruneOps.pruneRoot);
        PESToDEVSOnTheFly.folder = folder;
        PESToDEVSOnTheFly.packageNm = packageNm;
        PESToDEVSOnTheFly.toDEVS(devs);
        transferPairCoupling(devs);
    }

    @SuppressWarnings("null")
	public static void pruneEntityWEnumerate(sesRelation ses, pruningTable pruningTable,
            String folder, String packageNm, String entity, Element el) {
        if (ses.isLeaf(entity)) {
            return;
        }
        String[] pairs = pruningTable.getPairs(entity);
        if (pairs == null) {
            continueEnumerate(ses, pruningTable, entity, folder, packageNm, el);
        } else if (pairs.length > 0) {
            LinkedList<Object> specs = generatePrunings.getSpecializationElementsOfEntity(el);
            LinkedList<Object> asps = generatePrunings.getAspectElementsOfEntity(el);
            LinkedList<Object> q = new LinkedList<Object>();
            for (String str : pairs) {
                q.add(str);
            }
            q = reorderByContextLength(q);//more specific contexts go first
            for (Object oo : q) {
                String str = oo.toString();
                String context = "";
                String rem = "";
                int indc = str.indexOf(":");
                if (indc > -1) {// if there is a context
                    context = str.substring(0, indc).trim();
                    rem = str.substring(indc + 1).trim();
                } else {
                    rem = str;
                }
                int ind = rem.indexOf(",");
                String left = rem.substring(0, ind).trim();
                String right = rem.substring(ind + 1).trim();
                if (!context.equals("")) {
                    if (isElementInContext(el, context)) {
                        Element leftel = null;
                        Element spec = generatePrunings.getSpecializationOfEntity(right, el);
                        if (spec != null) {
                            leftel = generatePrunings.selectNGetEntityFromSpec(spec, left);

                            if (leftel == null) {
                                System.out.println(left + "context " + context);
                            }
                            if (!leftel.getNodeName().equals(left)) {
                                System.out.println("Existing selection " + leftel.getNodeName()
                                        + " not changed by " + left);
                            } else {
                                String path = spec.getAttribute("path");
                                path = reducePathToEntityPath(path);
                                String specnm = (String) pathToItem.get(path);
                                if (specnm != null) {
                                    copyOfEnumerateSeries.setCurrent(specnm, left);
                                }
                            }
                            pruneEntityWEnumerate(ses, pruningTable, folder, packageNm, left, leftel);
                        }
                        // select from aspects
                        LinkedList<Object> aspps = generatePrunings.getAspectElementsOfEntity(el);
                        if (right.equals(entity) && !aspps.isEmpty()) {

                            Element asp = generatePrunings.selectNGetAspectFromEntity(left, el);
                            if (asp != null) {
                                if (!asp.getNodeName().equals(left)) {
                                    System.out.println("Existing selection " + asp.getNodeName()
                                            + " not changed by " + left);
                                } else {
                                    String path = el.getAttribute("path");
                                    path = reducePathToEntityPath(path);
                                    String entnm = (String) pathToItem.get(path);
                                    if (entnm != null) {
                                        copyOfEnumerateSeries.setCurrent(entnm, left);
                                    }
                                }
                                LinkedList<Object> ents = pruneOps.getActualChildren(asp);
                                for (Object o : ents) {
                                    pruneEntityWEnumerate(ses, pruningTable, folder,
                                            packageNm, ((Element) o).getNodeName(), (Element) o);
                                }
                            }
                        }
                    } else {//not at el with context
                    }
                } else {//context is empty
                    if (getUniqueEntityPaths(entity).size() > 0) {
                        System.out.println("Context unspecified: you can specify one of the context paths for " + entity + " :"
                                + getUniqueEntityPaths(entity));
                    }
                    //else {
                    Element spec = generatePrunings.getSpecializationOfEntity(right, el);
                    if (spec != null) {//this is for spec
                        Element leftel = generatePrunings.selectNGetEntityFromSpec(spec, left);
                        if (!leftel.getNodeName().equals(left)) {
                            System.out.println("Existing selection " + leftel.getNodeName()
                                    + " not changed by " + left);
                        } else {
                            String path = spec.getAttribute("path");
                            path = reducePathToEntityPath(path);
                            String specnm = (String) pathToItem.get(path);
                            if (specnm != null) {
                                copyOfEnumerateSeries.setCurrent(specnm, left);
                            }
                        }
                        pruneEntityWEnumerate(ses, pruningTable, folder, packageNm, left,
                                leftel);
                    } else {//this was for asp not for spec
                        if (!asps.isEmpty()) {
                            Element asp = generatePrunings.selectNGetAspectFromEntity(left, el);
                            if (!asp.getNodeName().equals(left)) {
                                System.out.println("Existing selection " + asp.getNodeName()
                                        + " not changed by " + left);
                            } else {
                                String path = spec.getAttribute("path");
                                path = reducePathToEntityPath(path);
                                String entnm = (String) pathToItem.get(path);
                                if (entnm != null) {
                                    copyOfEnumerateSeries.setCurrent(entnm, left);
                                }
                            }
                            LinkedList<Object> ents = pruneOps.getActualChildren(asp);
                            for (Object o : ents) {
                                pruneEntityWEnumerate(ses, pruningTable, folder,
                                        packageNm, ((Element) o).getNodeName(), (Element) o);
                            }

                        }
                    }
                }
            }
        }
        continueEnumerate(ses, pruningTable, entity, folder, packageNm, el);
    }

    public static void continueEnumerate(sesRelation ses,
            pruningTable pruningTable, String entity, String folder,
            String packageNm, Element el) {
        LinkedList<Object> specs = generatePrunings.getSpecializationElementsOfEntity(el);
        LinkedList<Object> asps = generatePrunings.getAspectElementsOfEntity(el);
        if (!asps.isEmpty()) {
            String path = el.getAttribute("path");
            path = reducePathToEntityPath(path);
            String asp = enumerateSeries.getCurrent((String) pathToItem.get(path));
            Element aspect = generatePrunings.selectNGetAspectFromEntity(asp, el);
            LinkedList<Object> ents = pruneOps.getActualChildren(aspect);
            for (Object o : ents) {
                pruneEntityWEnumerate(ses, pruningTable, folder,
                        packageNm, ((Element) o).getNodeName(), (Element) o);
            }
        }
        for (Object s : specs) {
            Element spec = (Element) s;
            String path = spec.getAttribute("path");
            path = reducePathToEntityPath(path);
            String specnm = (String) pathToItem.get(path);
            String ent = enumerateSeries.getCurrent(specnm);
            Element entel = generatePrunings.selectNGetEntityFromSpec(spec, ent);
            if (entel != null) {
                if (!entel.getNodeName().equals(ent)) {
                    System.out.println("Existing selection " + entel.getNodeName()
                            + " not changed by " + ent);
                } else {
                    if (specnm != null) {
                        copyOfEnumerateSeries.setCurrent(specnm, ent);
                    }
                }
                pruneEntityWEnumerate(ses, pruningTable, folder, packageNm, entel.getNodeName(), entel);
            }
        }
    }

    public static Enumerate makeSpecEnumerate(Element spec, Element parent) {//q of Strings
        LinkedList<Object> chds = pruneOps.getActualChildren(spec);
        String specnm = spec.getNodeName();
        int ind = specnm.lastIndexOf("-");
        if (ind > 0) {
            specnm = specnm.substring(ind + 1, specnm.length() - "Spec".length());
        }
        specnm = specnm + "-for-" + parent.getNodeName();
        specnm = sesRelationExtend.getNext(specnm);
        String path = spec.getAttribute("path");
        path = reducePathToEntityPath(path);
        pathToItem.put(path, specnm);
        itemToPath.put(specnm, path);
        Enumerate en = new Enumerate(specnm);
        for (int i = 0; i < chds.size(); i++) {
            String ent = ((Element) chds.get(i)).getNodeName();
            en.add(ent);
        }
        return en;
    }

    public static Enumerate makeSpecEnumerateUniform(Element spec, Element parent) {//q of Strings
        LinkedList<Object> chds = pruneOps.getActualChildren(spec);
        String specnm = spec.getNodeName();
        int ind = specnm.lastIndexOf("-");
        if (ind > 0) {
            specnm = specnm.substring(ind + 1, specnm.length() - "Spec".length());
        }
        specnm = specnm + "-for-" + parent.getNodeName();
        if (sesRelationExtend.getNextIntFor(specnm) == 0) {
            specnm = sesRelationExtend.getNext(specnm);
            String path = spec.getAttribute("path");
            path = reducePathToEntityPath(path);
            pathToItem.put(path, specnm);
            itemToPath.put(specnm, path);

            Enumerate en = new Enumerate(specnm);
            for (int i = 0; i < chds.size(); i++) {
                String ent = ((Element) chds.get(i)).getNodeName();
                en.add(ent);
            }
            return en;
        } else {
            specnm = specnm+"0";
            String path = spec.getAttribute("path");
            path = reducePathToEntityPath(path);
            pathToItem.put(path, specnm);
            itemToPath.put(specnm, path);
            return null;
        }
    }

    public static void makeAspsOfEnumerate(Element ent) {//q of Strings
        LinkedList<Object> asps = generatePrunings.getAspectElementsOfEntity(ent);
        String entnm = ent.getNodeName();
        entnm = sesRelationExtend.getNext(entnm);
        String path = ent.getAttribute("path");
        path = reducePathToEntityPath(path);
        pathToItem.put(path, entnm);
        itemToPath.put(entnm, path);
        Enumerate en = new Enumerate(entnm);
        for (int i = 0; i < asps.size(); i++) {
            Element asp = (Element) asps.get(i);
            String aspnm = asp.getNodeName();
            en.add(aspnm);
        }
        enumerateSeries.addComponent(en);
    }

    public static void addEntEnumerate(Element ent) {//q of Strings
        LinkedList<Object> asps = generatePrunings.getAspectElementsOfEntity(ent);
        if (asps.size() > 1) {//!asps.isEmpty()) {
            makeAspsOfEnumerate(ent);
        }
        LinkedList<Object> specs = generatePrunings.getSpecializationElementsOfEntity(ent);
        if (specs.isEmpty()) {
            return;
        }
        for (int i = 0; i < specs.size(); i++) {
            Element chd = (Element) specs.get(i);
            Enumerate en = (Enumerate) makeSpecEnumerate(chd, ent);
            if (en != null) {
                enumerateSeries.addComponent(en);
            }
        }
    }

    public static void addEntEnumerateUniform(Element ent) {//q of Strings
        LinkedList<Object> asps = generatePrunings.getAspectElementsOfEntity(ent);
        if (asps.size() > 1) {//!asps.isEmpty()) {
            makeAspsOfEnumerate(ent);
        }
        LinkedList<Object> specs = generatePrunings.getSpecializationElementsOfEntity(ent);
        if (specs.isEmpty()) {
            return;
        }
        for (int i = 0; i < specs.size(); i++) {
            Element chd = (Element) specs.get(i);
            Enumerate en = (Enumerate) makeSpecEnumerateUniform(chd, ent);
            if (en != null) {
                enumerateSeries.addComponent(en);
            }
        }
    }

    public static String doOneEntStep() {
        return enumerateSeries.doOneStep();
    }

    public static int getCycleLength() {
        return enumerateSeries.getCycleLength();
    }

    public static void resetNameGenForEnumerates() {
        enumerateSeries.resetNameGenForEnumerates();
    }

    public static String getCurrent() {
        return enumerateSeries.getCurrent();
    }

    public static String getCurrentOfCopy() {
        return copyOfEnumerateSeries.getCurrent();
    }

    public static Hashtable<Object,Object> getCurrentPairs() {
        return enumerateSeries.getCurrentPairs();
    }

    public static Hashtable<Object,Object> getCopyofCurrentPairs() {
        return copyOfEnumerateSeries.getCurrentPairs();
    }
    public static Hashtable<Object,Integer> numberOfType(String type, Hashtable<Object,Object> pairs) {
    	Enumeration<Object> e = pairs.keys();
    	Hashtable<Object,Integer> b = new Hashtable<Object,Integer>();
    	while(e.hasMoreElements()){
    		String key = (String)e.nextElement();
    		String val = (String)pairs.get(key);
    		if(key.contains(type)){
    			if(b.containsKey(val)){
    				Integer v = b.get(val);
    				b.put(val, new Integer(v.intValue()+1));
    			}else{
    				b.put(val, new Integer(1));
    			}
    		}
    	}
    	
        return b;
    }

    public static LinkedList<Object> getComponentsWValue(String type, String val, Hashtable<Object,Object> pairs) {
        LinkedList<Object> res = new LinkedList<Object>();
        Enumeration<Object> e = pairs.keys();
    	
    	while(e.hasMoreElements()){
    		String key = (String)e.nextElement();
    		String value = (String)pairs.get(key);
    		if(key.contains(type)&& value.equals(val)){
    			res.add(itemToPath.get(key));
    		}
    	}
       
        return res;
    }

    public static Hashtable<Object,HashSet<Object>> getContentsOfBuckets(HashSet<Object> components, String val, Hashtable<Object,Object> pairs) {
        Hashtable<Object,HashSet<Object>> res = new Hashtable<Object,HashSet<Object>>();
        
        Enumeration<Object> e = pairs.keys();    
    	while(e.hasMoreElements()){
    		String key = (String)e.nextElement();
    		String value = (String)pairs.get(key);
    		for(Object o : components){
    			if (key.contains(o.toString()) && value.equals(val)) {
                    String path = (String) itemToPath.get(key);
                    int ind = path.indexOf("_");
                    String buck = path.substring(0, ind);
                    if(res.containsKey(val)){
        				res.get(buck).add(o.toString());
        			}else{
        				HashSet<Object> valSet = new HashSet<Object>();
        				valSet.add(o.toString());
        				res.put(buck, valSet);
        			}
                    
                }
    		}
    	}    	
        
        return res;
    }

    public static Hashtable<Object,Hashtable<Object,Integer>> makePartition(HashSet<Object> components, HashSet<Object> stripIds, String val, Hashtable<Object,Object> pairs) {
    	Hashtable<Object,Hashtable<Object,Integer>> res = new Hashtable<Object,Hashtable<Object,Integer>>();
    	Enumeration<Object> e = pairs.keys();    
    	while(e.hasMoreElements()){
    		String key = (String)e.nextElement();
    		String value = (String)pairs.get(key);
    		for (Object o : components) {
                String comp = o.toString();
                if (key.contains(o.toString()) && value.equals(val)) {
                    String path = (String) itemToPath.get(key);
                    int ind = path.indexOf("_");
                    String buck = path.substring(0, ind);
                    for (Object oo : stripIds) {
                        String stripId = oo.toString();
                        if (comp.startsWith(stripId)) {
                            comp = stripId;
                        }
                    }
                    if(res.containsKey(buck)){
                    	Hashtable<Object,Integer> bigBag = res.get(buck);
                    	if(bigBag.containsKey(comp)){
                    		Integer num = bigBag.get(comp);
                    		bigBag.put(comp, new Integer(num.intValue()+1));
                    	}else{
                    		bigBag.put(comp, new Integer(1));                    		
                    	}
                    }else {
                    	Hashtable<Object,Integer> bigBag = new Hashtable<Object,Integer>();
                    	bigBag.put(comp, new Integer(1));
                    	res.put(comp, bigBag);
                    }
                    
                }
            }
    	}
        
        return res;
    }

    public static boolean pairIsInRelation(Pair p, Hashtable<Object,HashSet<Object>> r) {
    	Hashtable<Object,Hashtable<Object,Integer>> res = new Hashtable<Object,Hashtable<Object,Integer>>();
    	Enumeration<Object> e = r.keys();    
    	while(e.hasMoreElements()){
    		String key = (String)e.nextElement();
    		HashSet<Object> valSet = r.get(key);
    		Iterator<Object> it = valSet.iterator();
    		while(it.hasNext()){
    			Object val = it.next();
    			String value = (String)val;
    			if (p.getKey().equals(key) && p.getValue().equals(value)) {
                    return true;
                }
    		}    		
    	}       
        return false;
    }

    public static boolean relationContainedInOther(Hashtable<Object,HashSet<Object>> r, Hashtable<Object,HashSet<Object>> s) {
    	Enumeration<Object> e = r.keys();    
    	while(e.hasMoreElements()){
    		String key = (String)e.nextElement();
    		HashSet<Object> valSet = r.get(key);
    		Iterator<Object> it = valSet.iterator();
    		while(it.hasNext()){
    			Object val = it.next();
    			String value = (String)val;
    			Pair p = new Pair(key,value);
    			if(!pairIsInRelation(p,s)){
    				return false;
    			}
    		}	
    	}
    	
        return true;
    }

    public static boolean relationsEquality(Hashtable<Object,HashSet<Object>> r, Hashtable<Object,HashSet<Object>> s) {
        return relationContainedInOther(r, s) && relationContainedInOther(s, r);
    }

    public static Hashtable<Object,HashSet<Object>> removeBlockFromRelation(Set<Object> block, Hashtable<Object,HashSet<Object>> r) {
        Hashtable<Object,HashSet<Object>> cop = sesRelation.copyRelation(r);
        Set<Object> domainR = r.keySet();
        for (Object key : domainR) {
            Set<Object> contents = r.get(key);
            if (block.equals(contents)) {
                for (Object v : contents) {
                	if(cop.containsKey(key)){
                		cop.get(key).remove(v);
                	}
                }
                return cop;
            }
        }
        return null;
    }

    public static boolean partitionContainedInOther(Hashtable<Object,HashSet<Object>> r, Hashtable<Object,HashSet<Object>> s) {
        Hashtable<Object,HashSet<Object>> rem = sesRelation.copyRelation(s);
        Set<Object> domainR = r.keySet();
        for (Object key : domainR) {
            Set<Object> block = r.get(key);
            rem = removeBlockFromRelation(block, rem);
            if (rem == null) {
                return false;
            }
        }
        return true;
    }
}

/////////////////////////////////////////////
class Enumerate extends LinkedList<Object> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String name;
    int current;
    boolean firstTime = true;
    boolean completingCycle;

    public Enumerate() {
        reset();
        firstTime = true;
        completingCycle = false;
    }

    public Enumerate copyName() {
        Enumerate cop = new Enumerate();
        cop.name = name;
        return cop;
    }

    public Enumerate(String name) {
        this.name = name;
        reset();
        firstTime = true;
        completingCycle = false;
    }

    public void reset() {
        current = 0;
    }

    public String selectNext(boolean step) {
        completingCycle = false;
        if (step) {
            current++;
        }
        if (current == size()) {
            completingCycle = true;
            reset();
        }
        String sel = (String) get(current);
        return sel;
    }

    public String getCurrent() {
        if (current < size()) {
            return (String) get(current);
        } else {
            return "";
        }
    }

    public void setCurrent(String val) {
        add(current, val);
    }

    public boolean completeCycle() {
        return completingCycle;
    }
}

class SeriesOfEnumerate {

    java.util.LinkedList<Enumerate> q;

    public SeriesOfEnumerate() {
        q = new LinkedList<Enumerate>();

    }

    public SeriesOfEnumerate copy() {
        SeriesOfEnumerate cop = new SeriesOfEnumerate();
        cop.q = copyList();
        return cop;
    }

    public java.util.LinkedList<Enumerate> copyList() {
        java.util.LinkedList<Enumerate> cop = new java.util.LinkedList<Enumerate>();
        for (int i = 0; i < q.size(); i++) {
            Enumerate en = (Enumerate) q.get(i);
            cop.add(en.copyName());
        }
        return cop;
    }

    public void addComponent(Enumerate en) {
        q.add(en);
    }

    public String doOneStep() {
        String[] stra = new String[q.size()];
        stra[0] = q.get(0).selectNext(true);
        for (int i = 0; i < q.size() - 1; i++) {
            stra[i + 1] = q.get(i + 1).selectNext(q.get(i).completeCycle());
        }
        return toString(stra);
    }

    public String doOneStep(boolean step) {
        String[] stra = new String[q.size()];
        stra[0] = q.get(0).selectNext(step);
        for (int i = 0; i < q.size() - 1; i++) {
            stra[i + 1] = q.get(i + 1).selectNext(q.get(i).completeCycle());
        }
        return toString(stra);

    }

    public boolean completingCycle() {
        Enumerate en = q.getLast();
        return en.completingCycle;

    }

    public String toString(String[] sta) {
        String res = "";
        int count = 0;
        for (String s : sta) {
            count++;
            res += " " + count + ": " + s;
        }
        return res;
    }

    public String getCurrent() {
        String[] stra = new String[q.size()];
        for (int i = 0; i < q.size(); i++) {
            Enumerate en = (Enumerate) q.get(i);
            if (!en.isEmpty()) {
                stra[i] = en.name + ": " + en.getCurrent();
            } else {
                stra[i] = en.name + ": 0";
            }
        }
        return toString(stra);
    }

    public Hashtable<Object,Object> getCurrentPairs() {
        Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        for (int i = 0; i < q.size(); i++) {
            Enumerate en = (Enumerate) q.get(i);
            f.put(en.name, en.getCurrent());
        }
        return f;
    }

    public String getCurrent(String name) {
        for (int i = 0; i < q.size(); i++) {
            if (q.get(i).name.equals(name)) {
                return q.get(i).getCurrent();
            }
        }
        return "";
    }

    public void setCurrent(String name, String val) {
        for (int i = 0; i < q.size(); i++) {
            if (q.get(i).name.equals(name)) {
                q.get(i).setCurrent(val);
            }
        }
    }

    public void resetNameGenForEnumerates() {
        for (int i = 0; i < q.size(); i++) {
            String nm = q.get(i).name;
            sesRelationExtend.reset(sesRelationExtend.getRepresentative(nm));
        }
    }

    public int getCycleLength() {
        double cycleLength = 1.0;
        double maxcycleLength = new Double(Integer.MAX_VALUE).doubleValue();
        for (int i = 0; i < q.size(); i++) {
            cycleLength *= (double) q.get(i).size();
            if (cycleLength >= maxcycleLength) {
                System.out.println("CycleLength exceeds max integer " + cycleLength);
                System.exit(3);
            }
        }
        return (int) cycleLength;
    }

    public void doCycle() {
        System.out.println("0" + " :" + getCurrent());
        for (int i = 1; i <= getCycleLength(); i++) {
            System.out.println(i + " :" + doOneStep());
        }
    }
}
