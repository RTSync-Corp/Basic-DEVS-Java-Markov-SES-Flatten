package com.ms4systems.devs.core.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.*;


import java.lang.reflect.Constructor;

import java.util.regex.Pattern;
import java.lang.String;

import java.util.ArrayList;

//added
import java.util.HashSet;

import com.ms4systems.devs.core.message.Coupling;
import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.model.CoupledModel;
import com.ms4systems.devs.core.model.impl.AtomicModelImpl;
import com.ms4systems.devs.core.model.impl.CoupledModelImpl;


public class PESToDEVSOnTheFly extends PESToDEVS {

	public static HashSet<Object> entitiesToShow = new HashSet<Object>();
	public static HashSet<Object> inheritanceSelections = new HashSet<Object>();
	
	// to make right couplings from multiaspect with several specialization elements (6/16/2015 cs)
	public static HashSet<String> entitiesInPrune = new HashSet<String>();

	public PESToDEVSOnTheFly() {
	}

	public static void toDEVS(CoupledModelImpl dig) throws ClassNotFoundException{
		Element specCh = pruneOps.getIterSubClass(pruneOps.pruneRoot);
		String pruneName = specCh.getAttribute("pruneName");
		if (pruneName.equals("")) {
			pruneName = specCh.getNodeName();
		}
		dig.setName(pruneName);
		PESToDEVSOnTheFly.convertEntityDEVS(pruneOps.pruneRoot, dig);

		if (dig.getChildren().size() == 0) {
			System.out.println("There is no further pruning for "
					+ dig.getName());
		}
		System.out.println("Generated " + dig.getName());

	}
	public static void toDEVSForData(CoupledModelImpl dig) throws ClassNotFoundException{
		Element specCh = pruneOps.getIterSubClass(pruneOps.pruneRoot);
		String pruneName = specCh.getAttribute("pruneName");
		if (pruneName.equals("")) {
			pruneName = specCh.getNodeName();
		}
		dig.setName(pruneName);
		PESToDEVSOnTheFly.convertEntityDEVSForData(pruneOps.pruneRoot, dig);

		if (dig.getChildren().size() == 0) {
			System.out.println("There is no further pruning for "
					+ dig.getName());
		}
		System.out.println("Generated " + dig.getName());
	}

	public static String makeInheritanceSelection(String prunename) {
		Pattern p = Pattern.compile("_");
		String[] groups = p.split(prunename);
		for (int i = 0; i < groups.length; i++) {
			groups[i] = groups[i].trim();
			if (inheritanceSelections.contains(groups[i])) {
				return groups[i];
			}
		}
		return groups[groups.length - 1];// default is l astin pruneName
	}
	// check an atomic model (7/29/2018 cseo)
	public static boolean existFile(String classnm)throws InvocationTargetException, ClassNotFoundException{
		boolean isFile = false;
		String fileNm = folder + classnm + ".java";
		File file = new File(fileNm);
		if (file.exists()) {
			isFile=true;
		}		
		return isFile;
	}

	public static AtomicModelImpl getFromRepository(String classnm,
			String pruneName, boolean isleaf) throws InvocationTargetException, ClassNotFoundException {
		if (isMS4MeEnv){
			String fileNm = folder + classnm + ".java";
			File file = new File(fileNm);
			if (!file.exists()) {
				if (isleaf) {
					return new AtomicModelImpl(classnm);
				} else { // type = digraph
					return new CoupledModelImpl(classnm);
				}
			} else {// file exists
				if (isleaf) {// over write only if leaf
					return getDefiniteFromRepository(classnm, pruneName, isleaf);
				} else {
					return new CoupledModelImpl(classnm);
				}
			}
		}else{
			String fileNm = folder + classnm + ".java";
			File file = new File(fileNm);
			if (!file.exists()) {
				if (isleaf) {
					return new AtomicModelImpl(classnm);
				} else { // type = digraph
					return new CoupledModelImpl(classnm);
				}
			} else {
				if (isleaf) {// over write only if leaf
					return getDefiniteFromRepository(classnm, pruneName, isleaf);
				} else {
					return new CoupledModelImpl(classnm);
				}
			}
		}
	}
	public static AtomicModelImpl getFromRepositoryForData(String classnm,
			String pruneName, boolean isleaf) throws InvocationTargetException, ClassNotFoundException {
		if (isleaf) {
			return new AtomicModelImpl(classnm);
		} else { // type = digraph
			return new CoupledModelImpl(classnm);
		}

	}

	public static AtomicModelImpl getDefiniteFromRepository(String classnm,
			String pruneName, boolean isleaf) throws SecurityException,
			InvocationTargetException, ClassNotFoundException {

		Class<?>c = null;
		Object o = null;
		try {
			String repairPackName = packageNm;
			if (!packageNm.endsWith(".")) {
				repairPackName = repairPackName + ".";
			}
			String className = repairPackName + classnm;
			c = Class.forName(className);
			Constructor<?> con = null;
				try {
					Constructor<?>[] cons = c.getDeclaredConstructors();
					Class<?>stringcl = Class.forName("java.lang.String");
					if (hasStringConstructor(cons, stringcl)) {
						Class<?>[] args = new Class[] { stringcl };
						con = c.getConstructor(args);
						o = con.newInstance(pruneName);
					} else {// use zero arg constructor
						con = c.getConstructor();
						o = con.newInstance();
					}
				} catch (NoSuchMethodException ex) {
					Logger.getLogger(PESToDEVSOnTheFly.class.getName()).log(
							Level.SEVERE, null, ex);
				}
			} catch (InstantiationException ex) {
				Logger.getLogger(PESToDEVSOnTheFly.class.getName()).log(
						Level.SEVERE, null, ex);
			} catch (IllegalAccessException ex) {
				Logger.getLogger(PESToDEVSOnTheFly.class.getName()).log(
						Level.SEVERE, null, ex);
			}
		return (AtomicModelImpl) o;
	}

	public static boolean hasStringConstructor(Constructor<?>[] cons,
			Class<?>stringcl) {
		for (Object o : cons) {
			Constructor<?> con = (Constructor<?>) o;
			Class<?>[] params = con.getParameterTypes();
			if (params.length == 1 && params[0].equals(stringcl)) {
				return true;
			}
		}
		return false;
	}


	public static AtomicModel convertEntityDEVS(Element entity,
			CoupledModelImpl devs)throws ClassNotFoundException {

		Element aspectsOf = null;
		LinkedList<Object> q = getActualChildren(entity);
		Iterator<?> it = q.iterator();
		while (it.hasNext()) {
			Element ch = (Element) it.next();
			if (ch.getNodeName().startsWith("aspectsOf")) {
				aspectsOf = ch;
			}
			break;
		}
		if (aspectsOf == null) {
			return devs;
		}
		Element aspect = (Element) getActualChildren(aspectsOf).getFirst();
		String entityName = entity.getNodeName();
		extractPorts(aspect.getAttribute("coupling"), devs,entityName);
		convertAspectDEVS(entity, aspect, devs);
		return devs;
	}
	public static AtomicModel convertEntityDEVSForData(Element entity,
			CoupledModelImpl devs)throws ClassNotFoundException {

		Element aspectsOf = null;
		LinkedList<Object> q = getActualChildren(entity);
		Iterator<?> it = q.iterator();
		while (it.hasNext()) {
			Element ch = (Element) it.next();
			if (ch.getNodeName().startsWith("aspectsOf")) {
				aspectsOf = ch;
			}
			break;
		}
		if (aspectsOf == null) {
			return devs;
		}
		Element aspect = (Element) getActualChildren(aspectsOf).getFirst();
		String entityName = entity.getNodeName();
		extractPorts(aspect.getAttribute("coupling"), devs,entityName);
		convertAspectDEVSForData(entity, aspect, devs);
		return devs;
	}
	public static AtomicModel convertAspectDEVS(Element entity, Element aspect,
			AtomicModel devs) throws ClassNotFoundException{
		String s = aspect.getAttribute("coupling");
		s = s.replaceAll("outout", "out"); // no longer needed
		s = s.replaceAll("inin", "in");// no longer needed
		s = s.trim();
		CoupledModelImpl dig = (CoupledModelImpl) devs;
		LinkedList<Object> components = getActualChildren(aspect);
		Iterator<?> it = components.iterator();
		{
			while (it.hasNext()) {
				Element ch = (Element) it.next();
				Element specCh = pruneOps.getIterSubClass(ch);
				String pruneName = specCh.getAttribute("pruneName");
				if (pruneName.equals("")) {
					pruneName = specCh.getNodeName();
				}
				if(pruneName.contains("NotPresent")){
					continue;
				}
				if (dig.getComponentWithName(pruneName) != null) {
					continue;
				}
				s = adapt(s, pruneName, ch.getNodeName());
				AtomicModelImpl chDEVS = null;
				try {
					chDEVS = getFromRepository(
							makeInheritanceSelection(pruneName), pruneName,
							isLeaf(ch));
				} catch (InvocationTargetException ex) {
					Logger.getLogger(PESToDEVSOnTheFly.class.getName()).log(
							Level.SEVERE, null, ex);
				}
				if (chDEVS == null) {
					System.out.println(pruneName);
				}
				
				if(!isLeaf(ch)){
					String orgName = pruneName;
					pruneName = generateMethodSummary.nameGen(pruneName);
					
					if(!orgName.equals(pruneName)) s = reconstructCoupling(s,orgName,pruneName);
					
				}
				
				chDEVS.setName(pruneName);
				addInports(chDEVS, s);
				addOutports(chDEVS, s);
				if (chDEVS instanceof AtomicModelImpl
						&& !(chDEVS instanceof CoupledModelImpl)) {
					dig.addChildModel(chDEVS);
				} else {
					dig.addChildModel(chDEVS);
					if (isLeaf(ch) && !pruneName.equals(ch.getNodeName())) {
						adaptForPruneName((CoupledModelImpl) chDEVS);// adapt
					}
					if (!containsAPart(pruneName)) {
					}
					convertEntityDEVS(ch, (CoupledModelImpl) chDEVS);
				}
			}
		}
		String ads = adapt(s, dig.getName(), entity.getNodeName());
		doCoupling(ads, dig);
		return (CoupledModelImpl) dig;
	}
	public static AtomicModel convertAspectDEVSForData(Element entity, Element aspect,
			AtomicModel devs) throws ClassNotFoundException{
		String s = aspect.getAttribute("coupling");
		s = s.replaceAll("outout", "out"); // no longer needed
		s = s.replaceAll("inin", "in");// no longer needed
		s = s.trim();
		CoupledModelImpl dig = (CoupledModelImpl) devs;
		LinkedList<Object> components = getActualChildren(aspect);
		Iterator<?> it = components.iterator();
		{
			while (it.hasNext()) {
				Element ch = (Element) it.next();
				Element specCh = pruneOps.getIterSubClass(ch);
				String pruneName = specCh.getAttribute("pruneName");
				if (pruneName.equals("")) {
					pruneName = specCh.getNodeName();
				}
				if (dig.getComponentWithName(pruneName) != null) {
					continue;
				}
				s = adapt(s, pruneName, ch.getNodeName());
				AtomicModelImpl chDEVS = null;
				try {
					chDEVS = getFromRepositoryForData(
							makeInheritanceSelection(pruneName), pruneName,
							isLeaf(ch));
				} catch (InvocationTargetException ex) {
					Logger.getLogger(PESToDEVSOnTheFly.class.getName()).log(
							Level.SEVERE, null, ex);
				}
				if (chDEVS == null) {
					System.out.println(pruneName);
				}
				
				if(!isLeaf(ch)){
					String orgName = pruneName;
					pruneName = generateMethodSummary.nameGen(pruneName);
					
					// make change for each entity to a new name
					if(!orgName.equals(pruneName)) s = reconstructCoupling(s,orgName,pruneName);
					
				}
				
				chDEVS.setName(pruneName);
				addInports(chDEVS, s);
				addOutports(chDEVS, s);
				if (chDEVS instanceof AtomicModelImpl
						&& !(chDEVS instanceof CoupledModelImpl)) {
					dig.addChildModel(chDEVS);
				} else {
					dig.addChildModel(chDEVS);
					if (isLeaf(ch) && !pruneName.equals(ch.getNodeName())) {
						adaptForPruneName((CoupledModelImpl) chDEVS);// adapt
					}
					if (!containsAPart(pruneName)) {
					}
					convertEntityDEVS(ch, (CoupledModelImpl) chDEVS);
				}
			}
		}
		String ads = adapt(s, dig.getName(), entity.getNodeName());
		doCoupling(ads, dig);
		return (CoupledModelImpl) dig;
	}
	public static boolean containsAPart(String pruneName) {
		for (Object o : entitiesToShow) {
			if (pruneName.contains(o.toString())) {
				return true;
			}
		}
		return false;
	}


	public static void changeDigName(CoupledModelImpl dig,
			String newName, CoupledModelImpl parent) {
		dig.setName(newName);
		adaptForPruneName(dig);
		adaptForPruneName(dig, parent);
	}

	public static void changeDigName(CoupledModelImpl dig,
			String newName) {
		dig.setName(newName);
		adaptForPruneName(dig);
	}


	public static void adaptForPruneName(CoupledModelImpl dig,
			CoupledModelImpl parent) {
	}

	public static void adaptForPruneName(CoupledModelImpl dig) {
	}

	public static void doCoupling(String ads, CoupledModelImpl dig) {
		HashSet<Object> done = doSpecialCoupling(ads, dig);
		doMoreCoupling(ads, dig, done);
	}


	public static HashSet<Object> doSpecialCoupling(String s,
			CoupledModelImpl dig) {
		HashSet<Object> done = new HashSet<Object>();
		while (true) {
			if (s.equals("{}")) {
				return done;
			}
			int firstLeftParen = s.indexOf("{");
			int firstRightParen = s.indexOf("}");
			if (firstLeftParen > -1) {
				String fstring = s.substring(firstLeftParen + 1,
						firstRightParen);
				Hashtable<Object,Object> f = parseCoupling(fstring);
				boolean foundEachSpecialSource = ((String) f.get("source"))
						.startsWith("each_");
				boolean foundEachSpecialDest = ((String) f.get("destination"))
						.startsWith("each_");
				boolean foundAllSpecialSource = ((String) f.get("source"))
						.startsWith("all_");
				boolean foundAllSpecialDest = ((String) f.get("destination"))
						.startsWith("all_");
				if (foundEachSpecialSource && foundEachSpecialDest) {
					int ind = f.get("source").toString().indexOf("_");
					String classNm1 = f.get("source").toString()
							.substring(ind + 1);
					ind = f.get("destination").toString().indexOf("_");
					String classNm2 = f.get("destination").toString()
							.substring(ind + 1);
					addEachToEachCoupling(dig, (String) f.get("outport"),
							(String) f.get("inport"), classNm1, classNm2);
					done.add(fstring);
				} else if (foundAllSpecialSource && foundAllSpecialDest) {
					int ind = f.get("source").toString().indexOf("_");
					String classNm1 = f.get("source").toString()
							.substring(ind + 1);
					ind = f.get("destination").toString().indexOf("_");
					String classNm2 = f.get("destination").toString()
							.substring(ind + 1);
					addAllToAllCoupling(dig, (String) f.get("outport"),
							(String) f.get("inport"), classNm1, classNm2);
					done.add(fstring);
				} else if (foundEachSpecialSource) {
					int ind = f.get("source").toString().indexOf("_");
					String classNm1 = f.get("source").toString()
							.substring(ind + 1);
					addEachToThisCoupling(dig, (String) f.get("outport"),
							(String) f.get("inport"), classNm1,
							(String) f.get("destination"));
					done.add(fstring);
				} else if (foundEachSpecialDest) {
					int ind = f.get("destination").toString().indexOf("_");
					String classNm2 = f.get("destination").toString()
							.substring(ind + 1);
					addThisToEachCoupling(dig, (String) f.get("outport"),
							(String) f.get("inport"), (String) f.get("source"),
							classNm2);
					done.add(fstring);
				} else if (foundAllSpecialSource) {
					int ind = f.get("source").toString().indexOf("_");
					String classNm1 = f.get("source").toString()
							.substring(ind + 1);
					ind = f.get("destination").toString().indexOf("_");

					String classNm2 = f.get("destination").toString();

					addPortFromAllToOneInClassCoupling(dig,
							(String) f.get("outport"),
							(String) f.get("inport"), classNm1, classNm2);
					done.add(fstring);
				} else if (foundAllSpecialDest) {
					int ind = f.get("destination").toString().indexOf("_");
					String classNm2 = f.get("destination").toString()
							.substring(ind + 1);
					ind = f.get("source").toString().indexOf("_");

					String classNm1 = f.get("source").toString();

					addPortFromOneToAllInClassCoupling(dig,
							(String) f.get("outport"),
							(String) f.get("inport"), classNm1, classNm2);
					done.add(fstring);
				}
				s = s.substring(firstRightParen + 1, s.length());
			} else {
				break;
			}
		}
		return done;
	}

	public static void doMoreCoupling(String s, CoupledModelImpl dig,
			HashSet<Object> done) {
		while (true) {
			if (s.equals("{}")) {
				return;
			}
			int firstLeftParen = s.indexOf("{");
			int firstRightParen = s.indexOf("}");
			if (firstLeftParen > -1) {
				String fstring = s.substring(firstLeftParen + 1,
						firstRightParen);
				if (done.contains(fstring)) {
					s = s.substring(firstRightParen + 1, s.length());
					continue;
				}
				Hashtable<Object,Object> f = parseCoupling(fstring);
				AtomicModel source = null;


				source = dig.getComponentWithName((String) f.get("source"));

				AtomicModel destination = null;

				destination = dig.getComponentWithName((String) f.get("destination"));
				

				if (source != null && destination != null) {
					dig.addInternalCoupling((String) f.get("source"),
							(String) f.get("outport"),
							(String) f.get("destination"),
							(String) f.get("inport"));
				}else
				if (source == null && destination != null) {
					//if the source model is the dig (6/19/2015 cs)
					if(dig.getName().equals((String) f.get("source"))){
						source = dig;
						dig.addInputPort((String) f.get("outport"));
						if (destination != null) {
							dig.addExternalInputCoupling((String) f.get("outport"),
									(String) f.get("destination"),
									(String) f.get("inport"));
						}
					}
				}else 
				if (destination == null && source !=null) {
					//if the destination model is the dig (6/19/2015 cs)
					if(dig.getName().equals((String) f.get("destination"))){
						destination = dig;
						// dig.addOutport((String) f.get("outport"));
						dig.addOutputPort((String) f.get("inport"));
						if (source != null) {
							dig.addExternalOutputCoupling((String) f.get("source"),
									(String) f.get("outport"),
									(String) f.get("inport"));
						}
					}
				}
				s = s.substring(firstRightParen + 1, s.length());
			} else {
				break;
			}
		}
	}

	public static HashSet<Object> getComponentsOfClass(
			CoupledModelImpl dig, String classNm) {
		HashSet<Object> es = new HashSet<Object>();
		ArrayList<AtomicModel> cl = dig.getChildren();
		for (AtomicModel o : cl) {
			String name = o.getName();
			if(name.contains("_")){
				int index = name.lastIndexOf("_");
				String subName = name.substring(index+1,name.length());
				
				if(subName.startsWith(classNm)){
	            	int length = classNm.length();
	                String temp = subName.substring(length,subName.length());
	                boolean isNumeric = temp.matches("[0-9]*");
	
	    	        if(isNumeric){
	    	        	es.add(o);
	    	        }
	    	        
				}else 
					if (name.endsWith("_" + classNm)) {
						es.add(o);
					}
				
			}
			
		}
		return es;
	}
	  public static HashSet<Object> getColors(String nm1) {
	        HashSet<Object> hs = new HashSet<Object>();
	        Pattern p = Pattern.compile("_");
	        String[] groups = p.split(nm1);
	        for (int i = 0; i < groups.length; i++) {
	            groups[i] = groups[i].trim();
	            hs.add(groups[i]);
	        }
	        return hs;
	    }

	    public static boolean haveSameColor(String nm1, String nm2) {
	        HashSet<Object> hs1 = getColors(nm1);
	        HashSet<Object> hs2 = getColors(nm2);
	        hs1.retainAll(hs2);
	        return !hs1.isEmpty();
	    }

	public static void addEachToEachCoupling(CoupledModelImpl dig,
			String port, String classNm1, String classNm2) {
		addEachToEachCoupling(dig, "out" + port, "in" + port, classNm1,
				classNm2);
	}


	public static void addEachToEachCoupling(CoupledModelImpl dig,
			String srcPrt, String destPrt, String classNm1, String classNm2) {
		HashSet<Object> digs1 = getComponentsOfClass(dig, classNm1);
		HashSet<Object> digs2 = getComponentsOfClass(dig, classNm2);
		for (Object o : digs1) {
			AtomicModelImpl iod = (AtomicModelImpl) o;
			String nm = iod.getName();
			for (Object o2 : digs2) {
				AtomicModelImpl iod2 = (AtomicModelImpl) o2;
				String nm2 = iod2.getName();
				if (haveSameColor(nm, nm2)) {
					iod.addOutputPort(srcPrt);
					iod2.addInputPort(destPrt);
					dig.addCoupling(nm, srcPrt, nm2, destPrt);
				}
			}
		}
	}


	public static void addEachToThisCoupling(CoupledModelImpl dig,
			String srcPrt, String destPrt, String classNm1, String compNm) {
		HashSet<Object> digs1 = getComponentsOfClass(dig, classNm1);
		AtomicModel comp = dig.getComponentWithName(compNm);
		HashSet<Object> digs2 = new HashSet<Object>();
		if(comp==null)comp=dig;
		digs2.add(comp);
		for (Object o : digs1) {
			AtomicModelImpl iod = (AtomicModelImpl) o;
			iod.addOutputPort(srcPrt);
			String nm = iod.getName();
			for (Object o2 : digs2) {
				AtomicModelImpl iod2 = (AtomicModelImpl) o2;
				if (comp == dig) {
					iod2.addOutputPort(nm + "_" + destPrt);
					dig.addCoupling(iod.getName(), srcPrt, iod2.getName(), nm
							+ "_" + destPrt);
				} else {
					iod2.addInputPort(nm + "_" + destPrt);
					dig.addCoupling(iod.getName(), srcPrt, iod2.getName(), nm
							+ "_" + destPrt);
				}
			}
		}
	}


	public static void addThisToEachCoupling(CoupledModelImpl dig,
			String srcPrt, String destPrt, String compNm, String classNm2) {
		HashSet<Object> digs2 = getComponentsOfClass(dig, classNm2);
		HashSet<Object> digs1 = new HashSet<Object>();
		AtomicModel comp = dig.getComponentWithName(compNm);
		if(comp==null)comp=dig;
		digs1.add(comp);
		for (Object o : digs1) {
			AtomicModelImpl iod = (AtomicModelImpl) o;
			for (Object o2 : digs2) {
				AtomicModelImpl iod2 = (AtomicModelImpl) o2;
				String iod2nm = iod2.getName();
				iod2.addInputPort(destPrt);
				if (comp == dig) {
					iod.addInputPort(iod2nm + "_" + srcPrt);
					dig.addCoupling(iod.getName(), iod2nm + "_" + srcPrt,
							iod2.getName(), destPrt);
				} else {
					iod.addOutputPort(iod2nm + "_" + srcPrt);
					dig.addCoupling(iod.getName(), iod2nm + "_" + srcPrt,
							iod2.getName(), destPrt);
				}
			}
		}
	}


	public static void addPortFromOneToAllInClassCoupling(
			CoupledModelImpl dig, String destPrt, String srcPrt,
			String compNm, String classNm) {

		HashSet<Object> digs = getComponentsOfClass(dig, classNm);
		AtomicModelImpl comp = (AtomicModelImpl) dig
				.getComponentWithName(compNm);
		if (comp == null) {
			comp = dig;
		}
		for (Object o : digs) {
			AtomicModelImpl iod = (AtomicModelImpl) o;
			iod.addInputPort(srcPrt);
			if (comp != dig) {
				if (!comp.getName().equals(iod.getName())) {
					comp.addOutputPort(destPrt);
					dig.addCoupling(comp.getName(), destPrt, iod.getName(),
							srcPrt);
				}
			} else {
				comp.addInputPort(srcPrt);
				dig.addCoupling(comp.getName(), destPrt, iod.getName(), srcPrt);
			}
		}
	}


	public static void addPortFromOneToAllInClassCoupling(
			CoupledModelImpl dig, String port, String compNm,
			String classNm) {
		addPortFromOneToAllInClassCoupling(dig, "out" + port, "in" + port,
				compNm, classNm);
	}


	public static void addPortFromAllToOneInClassCoupling(
			CoupledModelImpl dig, String port, String classNm,
			String compNm) {
		addPortFromAllToOneInClassCoupling(dig, "out" + port, "in" + port,
				compNm, classNm);
	}


	public static void addPortFromAllToOneInClassCoupling(
			CoupledModelImpl dig, String srcPrt, String destPrt,
			String classNm, String compNm) {
		HashSet<Object> digs = getComponentsOfClass(dig, classNm);
		AtomicModelImpl comp = (AtomicModelImpl) dig
				.getComponentWithName(compNm);
		if (comp == null) {
			comp = dig;
		}
		for (Object o : digs) {
			AtomicModelImpl iod = (AtomicModelImpl) o;
			iod.addOutputPort(srcPrt);
			if (comp != dig) {
				comp.addInputPort(destPrt);
				dig.addCoupling(iod.getName(), srcPrt, comp.getName(), destPrt);
			} else {
				comp.addOutputPort(destPrt);
				dig.addCoupling(iod.getName(), srcPrt, comp.getName(), destPrt);
			}
		}
	}


	public static void addAllToAllCoupling(CoupledModelImpl dig,
			String destPrt, String srcPrt, String classNm1, String classNm2) {
		HashSet<Object> digs1 = getComponentsOfClass(dig, classNm1);
		for (Object o : digs1) {
			AtomicModel iod = (AtomicModel) o;
			String nm = iod.getName();
			addPortFromOneToAllInClassCoupling(dig, destPrt, srcPrt, nm,
					classNm2);
		}
	}


	public static void transferAttributes(Element ch, AtomicModel chDEVS) {
	}


	public static void removeSelfCoupling(CoupledModelImpl cm) {
		HashSet<Object> removeCouples = new HashSet<Object>();
		ArrayList<Coupling> alc = cm.getCouplings();
		for (Coupling cp:alc){
			AtomicModel src = cp.getSource();
			AtomicModel dest = cp.getDestination();
			if (src.equals(dest)) {
				removeCouples.add(cp);
			}
		}
		for (Object oo : removeCouples) {
			Coupling p = (Coupling) oo;
                        cm.removeCoupling(p);
		}
		ArrayList<AtomicModel> al = cm.getChildren();
		for (AtomicModel am:al){	
			if (am instanceof CoupledModel) {
				removeSelfCoupling((CoupledModelImpl) am);
			}
		}
	}

	public static void main(String argv[]) {
	} // main
}
