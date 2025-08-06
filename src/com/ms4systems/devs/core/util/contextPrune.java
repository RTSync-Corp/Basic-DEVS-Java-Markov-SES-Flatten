/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ms4systems.devs.core.util;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.*;
import org.w3c.dom.Attr;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.ms4systems.devs.core.message.Coupling;
import com.ms4systems.devs.core.message.Message;
import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.core.message.impl.MessageBagImpl;
import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.model.CoupledModel;
import com.ms4systems.devs.core.model.impl.AtomicModelImpl;
import com.ms4systems.devs.core.model.impl.CoupledModelImpl;
import com.ms4systems.devs.core.simulation.Coordinator;
import com.ms4systems.devs.core.simulation.Simulation;
import com.ms4systems.devs.core.simulation.Simulator;
import com.ms4systems.devs.core.util.sesRelationExtend.pruningRules;

/**
 *
 * @author Bernie
 */
@SuppressWarnings({"rawtypes","unchecked","unused"})
public class contextPrune extends BasicWork {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static Hashtable<Object,Object> parse(String sentance) {
    	//if(sentance.contains("//")) return null;
        sesParse par = new sesParse();
        String[] groups = par.getParts(sentance);
        if (checkShow(groups) != null) {
            return checkShow(groups);
        } else if (checkInherit(groups) != null) {
            return checkInherit(groups);
        } else if (checkSelectAspect(groups) != null) {
            return checkSelectAspect(groups);
        } else if (checkSelectSpecialization(groups) != null) {
            return checkSelectSpecialization(groups);
        } else if (checkEntityToPrune(groups) != null) {
            return checkEntityToPrune(groups);
        } else if (checkDoNotSelect(groups) != null) {
            return checkDoNotSelect(groups);
        } else if (checkBound(groups) != null) {
            return checkBound(groups);
        }
        return null;
    }
	public static Hashtable<Object,Object> checkBound(String[] groups) {
        Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (groups.length >= 7
                && groups[0].toLowerCase().equals("set")
                && groups[1].toLowerCase().equals("count")
                && groups[2].toLowerCase().equals("bounds")) {
            f.put("entity", groups[4]);
            f.put("bounds", groups[6]);
            if (groups.length==9){
                f.put("parent",groups[8]);
            }
            return f;
        }
        return null;
    }
    public static Hashtable<Object,Object> checkDoNotSelect(String[] groups) {
        Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (groups.length == 5
                && groups[0].toLowerCase().equals("don't")
                && groups[1].toLowerCase().equals("select")) {
            f.put("donotselect", groups[0]);
            f.put("specEnt", groups[2]);
            f.put("entity", groups[4]);
            return f;
        }
        return null;
    }

    public static Hashtable<Object,Object> checkShow(String[] groups) {
        Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (groups.length >= 2 && groups[0].toLowerCase().equals("show")) {
            f.put("show", groups[0]);
            f.put("entity", groups[1]);
            return f;
        }
        return null;
    }

    public static Hashtable<Object,Object> checkInherit(String[] groups) {
        Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (groups.length >= 3 && groups[0].toLowerCase().equals("inherit")) {
            f.put("inherit", groups[0]);
            f.put("entity", groups[2]);
            return f;
        }
        return null;
    }

    public static Hashtable<Object,Object> checkEntityToPrune(String[] groups) {
        Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (groups.length == 6
                && groups[0].toLowerCase().equals("set")
                && groups[1].toLowerCase().equals("entity")) {
            f.put("setEntity", "set");
            f.put("entity", groups[5]);
            return f;
        }
        return null;
    }

    public static Hashtable<Object,Object> checkSelectSpecialization(String[] groups) {
        Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (groups.length >= 7 && groups[0].toLowerCase().equals("select") && groups[5].toLowerCase().equals("all")) {
            f.put("select", groups[0]);
            f.put("entToSelect", groups[1]);
            
            f.put("specialization", groups[3]);
            f.put("parent", groups[6]);
            f.put("multSpec", groups[6]);
            String underSeq = "";
            for (int i = 6; i < groups.length; i += 2) {
                if (groups[i].equals("under")) {
                    underSeq += groups[i + 1] + ".";
                }
            }
            f.put("underSequence", underSeq);
            return f;
        }else
        if (groups.length >= 6 && groups[0].toLowerCase().equals("select")) {
            f.put("select", groups[0]);
            f.put("entToSelect", groups[1]);
            f.put("specialization", groups[3]);
            f.put("parent", groups[5]);
            String underSeq = "";
            for (int i = 6; i < groups.length; i += 2) {
                if (groups[i].equals("under")) {
                    underSeq += groups[i + 1] + ".";
                }
            }
            f.put("underSequence", underSeq);
            return f;
        }
        return null;
    }

    public static Hashtable<Object,Object> checkSelectAspect(String[] groups) {
        Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (groups.length >= 6 && groups[0].toLowerCase().equals("select")
                && groups[3].toLowerCase().equals("aspects")) {
            f.put("selectAspect", groups[0]);
            f.put("aspToSelect", groups[1]);
            f.put("parent", groups[5]);
            String underSeq = "";
            for (int i = 6; i < groups.length; i += 2) {
                if (groups[i].equals("under")) {
                    underSeq += groups[i + 1] + ".";
                }
            }
            f.put("underSequence", underSeq);
            return f;
        }
        return null;
    }

    public static Node getAncestorEntity(Node n) {
        Node par = n.getParentNode();
        if (n instanceof Element) {
            String nodeNm = par.getNodeName();
            if (nodeNm.contains("-") || nodeNm.contains("aspectsOf")) {
                return getAncestorEntity(par);
            } else {
                return par;
            }
        }
        return null;
    }

    public static Node getAncestorAlongEntityPath(Node n, String path) {
        if (path.equals("")) {
            return n;
        }
        int indOfDot = path.indexOf(".");
        if (indOfDot == -1) {
            return n;
        } else {
            String lab = path.substring(0, indOfDot);
            if (getAncestorEntity(n).getNodeName().equals(lab)) {
                return getAncestorAlongEntityPath(getAncestorEntity(n),
                        path.substring(indOfDot + 1, path.length()));
            } else {
                return null;
            }
        }
    }

    public static boolean isElementInContext(Element n, String path) {
        if (path.equals("")) {
            return true;
        }
        int indOfDot = path.indexOf(".");
        if (indOfDot == -1) {
            return true;
        } else {
            String lab = path.substring(0, indOfDot);
            if (getAncestorEntity(n).getNodeName().equals(lab)) {
                return isElementInContext((Element) getAncestorEntity(n),
                        path.substring(indOfDot + 1, path.length()));
            } else {
                return false;
            }
        }
    }

    public static void createPruneDoc(sesRelation ses) {
        ses.toDOM();// creates SESOps.sesDoc
        ses.printTree();
        String rootElemName = SESOps.getSesRoot().getAttribute("name");
        pruneOps.createPesDoc(rootElemName);
        generatePrunings.genericPESEntityFull(pruneOps.pruneRoot,
                SESOps.getSesRoot(), pruneOps.pruneRoot.getNodeName());
        
        try{
	        Transformer transformer = TransformerFactory.newInstance().newTransformer();
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	
	        StreamResult result = new StreamResult(new StringWriter());
	        DOMSource source = new DOMSource(ses.sesRoot);
	        transformer.transform(source, result);
	
	        String xmlString = result.getWriter().toString();
	        System.out.println("ses.sesRoot : \n"+xmlString);
	        
	        
	        DOMSource source1 = new DOMSource(pruneOps.pruneRoot);
	        transformer.transform(source1, result);
	
	        String xmlString1 = result.getWriter().toString();
	        System.out.println("pruneOps.pruneRoot : \n"+xmlString1);
	        
        }catch(Exception e){
        	e.printStackTrace();
        }
    }

    public static Element getSpecifiedElement(String entity, String path) {
        LinkedList<Object> q = XMLToDom.getElementOccurrences(pruneOps.getPruneDoc(),
                entity);
        Iterator it = q.iterator();
        Element rightElem = null;
        Node par = null;
        while (it.hasNext()) {
            Element el = (Element) it.next();
            par = getAncestorAlongEntityPath(el, path);
            if (par != null) {
                rightElem = el;
                break;
            }
        }
        return rightElem;
    }

    public static String reducePathToEntityPath(String fullPath) {
        return reducePathToEntityPath(fullPath, "");
    }

    public static String reducePathToEntityPath(String fullPath, String reducedPath) {
        if (fullPath.equals("")) {
            return "";
        }
        int indOfDot = fullPath.indexOf(".");
        if (indOfDot == -1) {
            if (fullPath.contains("-")) {
                int indOfDash = fullPath.indexOf("-");//assuming ends with dec or spec

                String ent = fullPath.substring(0, indOfDash);
                return reducedPath + "." + ent;
            } else {
                return reducedPath + "." + fullPath;
            }
        } else {
            String lab = fullPath.substring(0, indOfDot);
            if (lab.contains("-") || lab.contains("aspectsOf")) {
                return reducePathToEntityPath(fullPath.substring(indOfDot + 1), reducedPath);
            }
            return reducePathToEntityPath(fullPath.substring(indOfDot + 1), reducedPath + "." + lab);
        }
    }

    public static HashSet<Object> getUniqueEntityPaths(String entity) {
        HashSet<Object> res = new HashSet<Object>();
        if (XMLToDom.getElementOccurrences(pruneOps.getPruneDoc(), entity).size() <= 1) {
            return res;
        }
        HashSet<Object> fullpaths = XMLToDom.getUniqueIDs(pruneOps.getPruneDoc(), entity);

        Iterator it = fullpaths.iterator();
        while (it.hasNext()) {
            String fullpath = (String) it.next();
            res.add(reducePathToEntityPath(fullpath));
        }
        HashSet<Object> rest = new HashSet<Object>();
        Iterator ir = res.iterator();
        while (ir.hasNext()) {
            String path = (String) ir.next();
            rest.add(expressAsUnderSequence(path));
        }
        return rest;
    }

    public static String expressAsUnderSequence(String path) {
        if (path.startsWith(".")) {
            path = path.substring(1);
        }
        int indOfDot = path.indexOf(".");
        if (indOfDot == -1) {
            return path;
        }
        int lndOfDot = path.indexOf(".");
        if (indOfDot >= -1) {
            return expressAsUnderSequence(path.substring(indOfDot + 1), "");
        }
        return path;
    }

    public static String expressAsUnderSequence(String path, String exp) {
        int indOfDot = path.indexOf(".");
        if (indOfDot > -1) {
            String rempath = path.substring(indOfDot + 1);
            return expressAsUnderSequence(rempath,
                    exp + " under " + path.substring(0, indOfDot));
        }
        return exp + " under " + path;
    }


    
    public static void pruneNTransform(sesRelationExtend ses, CoupledModelImpl cm,
            pruningTable pruningTable, String folder, String packageNm)throws ClassNotFoundException {
        String entToPrune = ses.getRootEntityName();
        createPruneDoc(ses);
        if (!entityToPrune.equals("")) {
            entToPrune = entityToPrune;
            ses = new sesRelationExtend(ses.substructure(entToPrune));
            createPruneDoc(ses);
        }
        pruneEntity(ses, pruningTable, folder, packageNm,
                entToPrune, pruneOps.pruneRoot);
        generatePruningsWInherAuto.doInherit();
     // need to print the pruned doc.
        Transformer transformer;
        try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

	        StreamResult result = new StreamResult(new StringWriter());
	        DOMSource source = new DOMSource(pruneOps.pruneRoot);
	       
			transformer.transform(source, result);			

	        String xmlString = result.getWriter().toString();
	        System.out.println("pruning done : \n"+xmlString);
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        //removeAllNotPresents(ses);
        accumulateAllCommonVars(ses, entToPrune, pruneOps.pruneRoot);
        
        // save restructured entities to PESToDEVSOnTheFly.entitiesInPrune (6/16/2015)
        saveEntities(ses);
        
        PESToDEVSOnTheFly.folder = folder;
        PESToDEVSOnTheFly.packageNm = packageNm;
        PESToDEVSOnTheFly.toDEVS(cm);       
        if(hasError)return;
        PESToDEVSOnTheFly.removeSelfCoupling(cm);
        transferPairCoupling(cm);
        
		
    }
    public static void pruneNTransformForData(sesRelationExtend ses, CoupledModelImpl cm,
            pruningTable pruningTable, String folder, String packageNm)throws ClassNotFoundException {
        String entToPrune = ses.getRootEntityName();
        createPruneDoc(ses);
        if (!entityToPrune.equals("")) {
            entToPrune = entityToPrune;
            ses = new sesRelationExtend(ses.substructure(entToPrune));
            createPruneDoc(ses);
        }
        pruneEntity(ses, pruningTable, folder, packageNm,
                entToPrune, pruneOps.pruneRoot);
        generatePruningsWInherAuto.doInherit();
        accumulateAllCommonVars(ses, entToPrune, pruneOps.pruneRoot);
        PESToDEVSOnTheFly.folder = folder;
        PESToDEVSOnTheFly.packageNm = packageNm;
        PESToDEVSOnTheFly.toDEVSForData(cm);       
        if(hasError)return;
        PESToDEVSOnTheFly.removeSelfCoupling(cm);
        transferPairCoupling(cm);
        
		
    }
    public static void pruneNTransformForInstance(sesRelationExtend ses, CoupledModelImpl cm,
            pruningTable pruningTable, String folder, String packageNm)throws ClassNotFoundException {
        String entToPrune = ses.getRootEntityName();
        createPruneDoc(ses);
        if (!entityToPrune.equals("")) {
            entToPrune = entityToPrune;
            ses = new sesRelationExtend(ses.substructure(entToPrune));
            createPruneDoc(ses);
        }
        pruneEntity(ses, pruningTable, folder, packageNm,
                entToPrune, pruneOps.pruneRoot);
        generatePruningsWInherAuto.doInherit();
        accumulateAllCommonVars(ses, entToPrune, pruneOps.pruneRoot);
        PESToDEVSOnTheFly.folder = folder;
        PESToDEVSOnTheFly.packageNm = packageNm;
        PESToDEVSOnTheFly.toDEVS(cm);       
        if(hasError)return;
        PESToDEVSOnTheFly.removeSelfCoupling(cm);
        transferPairCoupling(cm);
        
		
    }
    public static void pruneWithNoTransform(sesRelationExtend ses, CoupledModelImpl cm,
            pruningTable pruningTable, String folder, String packageNm) {
        String entToPrune = ses.getRootEntityName();
        createPruneDoc(ses);
        if (!entityToPrune.equals("")) {
            entToPrune = entityToPrune;
            ses = new sesRelationExtend(ses.substructure(entToPrune));
            createPruneDoc(ses);
        }
        pruneEntity(ses, pruningTable, folder, packageNm,
                entToPrune, pruneOps.pruneRoot);
        accumulateAllCommonVars(ses, entToPrune, pruneOps.pruneRoot);
    }

    public static void pruneEntity(sesRelation ses, pruningTable pruningTable,
            String folder, String packageNm, String entity, Element el, pruningRules pr) {
    }

    public static void pruneEntity(sesRelation ses, pruningTable pruningTable,
            String folder, String packageNm, String entity, Element el) {
        if (ses.isLeaf(entity)) {
            return;
        }
        String[] pairs = pruningTable.getPairs(entity);
        if (pairs == null) {
            continuePruning(ses, pruningTable, entity, folder, packageNm, el);
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
                                System.out.println(left + " context " + context);
                                throw new java.lang.UnsupportedOperationException(left + " context " + context);
                            }
                            if (!leftel.getNodeName().equals(left)) {
                                System.out.println("Existing selection " + leftel.getNodeName()
                                        + " not changed by " + left);
                            }
                            pruneEntity(ses, pruningTable, folder, packageNm, left, leftel);
                        }
                        LinkedList<Object> aspps = generatePrunings.getAspectElementsOfEntity(el);
                        if (right.equals(entity) && !aspps.isEmpty()) {

                            Element asp = generatePrunings.selectNGetAspectFromEntity(left, el);
                            if (asp == null) {
                            	 String aspName = asp.getNodeName();
                                 if (aspName.endsWith("Dec"))aspName = aspName.substring(0, aspName.length()-4);
                                System.out.println(aspName+ " does not contain "+left);
                                throw new java.lang.UnsupportedOperationException(left + " context " + context);
                            }
                            if (asp != null) {
                                if (!asp.getNodeName().equals(left)) {
                                    System.out.println("Existing selection " + asp.getNodeName()
                                            + " not changed by " + left);
                                }
                                LinkedList<Object> ents = pruneOps.getActualChildren(asp);
                                for (Object o : ents) {
                                    pruneEntity(ses, pruningTable, folder,
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
                        if (leftel == null) {
                            System.out.println("Does not exist " + left);
                            String specName = spec.getNodeName();
                            if (specName.endsWith("Spec"))specName = specName.substring(0, specName.length()-4);
                            throw new java.lang.UnsupportedOperationException(specName+ " does not contain "+left);//spec.getNodeName()+ " does not contain "+left);
                            
                        } else if (!leftel.getNodeName().equals(left)) {
                            System.out.println("Existing selection " + leftel.getNodeName()
                                    + " not changed by " + left);

                        }
                        pruneEntity(ses, pruningTable, folder, packageNm, left,
                                leftel);
                    } else {//this was for asp not for spec
                        if (!asps.isEmpty()) {
                            Element asp = generatePrunings.selectNGetAspectFromEntity(left, el);
                            if (!asp.getNodeName().equals(left)) {
                                System.out.println("Existing selection " + asp.getNodeName()
                                        + " not changed by " + left);
                            }
                            LinkedList<Object> ents = pruneOps.getActualChildren(asp);
                            for (Object o : ents) {
                                pruneEntity(ses, pruningTable, folder,
                                        packageNm, ((Element) o).getNodeName(), (Element) o);
                            }

                        }
                    }
                }
            }
        }
        continuePruning(ses, pruningTable, entity, folder, packageNm, el);
    }

    public static void continuePruning(sesRelation ses,
            pruningTable pruningTable, String entity, String folder,
            String packageNm, Element el) {
        LinkedList<Object> specs = generatePrunings.getSpecializationElementsOfEntity(el);
        LinkedList<Object> asps = generatePrunings.getAspectElementsOfEntity(el);
        if (!asps.isEmpty()) {
            Element asp = generatePrunings.selectElementFrom(asps);
            String aspect = asp.getNodeName();
            generatePrunings.selectNGetAspectFromEntity(aspect, el);
            LinkedList<Object> ents = pruneOps.getActualChildren(asp);
            for (Object o : ents) {
                pruneEntity(ses, pruningTable, folder,
                        packageNm, ((Element) o).getNodeName(), (Element) o);
            }
        }

        for (Object s : specs) {
            Element spec = (Element) s;
 /////////////////////////////////  Added BPZ 2024
            String[] pairs = pruningTable.getPairs(entity);
            if (pairs != null && pairs.length>0) {
            LinkedList<Object> q = new LinkedList<Object>();
                       for (String str : pairs) {
                           q.add(str);
                       }
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
            generatePrunings.selectNGetEntityFromSpec(spec, left);
                    }
            }else {
   /////////////////////////////////////////////////////

            Element entel = generatePrunings.selectNGetEntityFromSpec(spec);
            if (entel != null) {
                generatePrunings.selectNGetEntityFromSpec(spec, entel.getNodeName());
                pruneEntity(ses, pruningTable, folder, packageNm, entel.getNodeName(), entel);
            } else {
                System.out.println("Trying to select entity not in specialization " + spec.getNodeName());
            }
        }
    }
   } //BPZ 2024

    public static String getMax(LinkedList<Object> q) {
        int max = -1;
        String maxString = "";
        for (Object o : q) {
            String str = o.toString();
            String context = "";
            int indc = str.indexOf(":");
            int contextSize = 0;
            if (indc > -1) {// if there is a context
                context = str.substring(0, indc).trim();
                while (context.indexOf(".") > -1) {
                    contextSize++;
                    context = context.substring(context.indexOf(".") + 1);
                }
            }
            if (contextSize > max) {
                max = contextSize;
                maxString = str;
            }
        }
        return maxString;
    }

    public static LinkedList<Object> reorderByContextLength(LinkedList<Object> q) {
        int origSize = q.size();
        LinkedList<Object> res = new LinkedList<Object>();
        while (!q.isEmpty()) {
            String maxString = getMax(q);
            res.add(maxString);
            q.remove(maxString);
        }
        if (res.size() != origSize) {
            System.out.println("Error: reorder did not preserve size");
            System.exit(3);
        }
        return res;
    }

///////////////////////////////////
    public pruningTable parseNInterpret(String contents, sesRelationExtend ses) {

        String s = "";
        s = addNeighborhoodSpecification(contents, ses);

        pruningTable pt = parseNInterpret(s + contents, ses, new pruningTable(ses));
//two passes are needed to get external couplings into the contents
        if (!s.equals("")) {
            return parseNInterpret(s, ses, pt);
        }
        return pt;
    }

    public pruningTable parseNInterpret(String contents, sesRelationExtend ses, pruningTable pt) {
        addCouplingSpecification(contents, ses);

        String addCouples = preProcessAddCouple(contents, ses);

        Pattern p = Pattern.compile("!");
        String[] sentences = p.split(contents + addCouples);

        Hashtable<Object,Object> f = null;

        for (int i = 0; i < sentences.length; i++) {
            f = contextPrune.parse(sentences[i]);
            if (f == null) {
                continue;
            }
            if (f.get("donotselect") != null) {
                String specEnt = (String) f.get("specEnt");
                String entity = (String) f.get("entity");
                ses.downSelectFromEntity(specEnt, entity);
                System.out.println(specEnt + " removed  from " + entity);
                //
                ses.printTree();
            } else if (f.get("setEntity") != null) {
                entityToPrune = (String) f.get("entity");
            } else if (f.get("show") != null) {
                String enttoShow = (String) f.get("entity");
                if (enttoShow.equals("all")) {
                    this.addAllEntitiesToShow(ses);
                } else {
                    this.addEntityToShow(enttoShow);
                }
            } else if (f.get("selectAspect") != null) {
                String aspToSelect = (String) f.get("aspToSelect");
                String parent = (String) f.get("parent");
                String underSeq = (String) f.get("underSequence");
                if (underSeq.length() == 0) {
                    pt.selectAspectFromEntity(aspToSelect, parent);
                } else {
                    pt.addContext(underSeq);
                    pt.selectAspectFromEntityInContext(aspToSelect, parent,
                            underSeq);
                }
            } else if (f.get("select") != null) {
                String entToSelect = (String) f.get("entToSelect");
                String specialization = (String) f.get("specialization");
                String parent = (String) f.get("parent");
                String underSeq = (String) f.get("underSequence");
                if (underSeq.length() == 0) {
                    pt.selectEntityFromSpec(entToSelect, specialization, parent);
                } else {
                    pt.addContext(underSeq);
                    pt.selectEntityFromSpecInContext(entToSelect,
                            specialization, parent, underSeq);
                }
                String multiSpec = (String)f.get("multSpec");
                if(multiSpec!= null){
                	pt.addMultiSpec(multiSpec);
                }
            } else if (f.get("inherit") != null) {
                String enttoInherit = (String) f.get("entity");
                if (enttoInherit.equals("all")) {
                    this.addAllInheritanceSelections(ses);
                } else {
                    this.addInheritanceSelection(enttoInherit);
                }
            } // }
            else if (f.get("setEntity") != null) {
                String enttoShow = (String) f.get("entity");
                if (enttoShow.equals("all")) {
                    this.addAllEntitiesToShow(ses);
                } else {
                    this.addEntityToShow(enttoShow);
                }
            }
        }
        return pt;
    }

    public static Hashtable<Object,Object> preparse(String sentance) {
        sesParse par = new sesParse();
        String[] groups = par.getParts(sentance);
        if (checkWriteCouplingSpecification(groups) != null) {
            return checkWriteCouplingSpecification(groups);
        } else if (checkCellularSpecification(groups) != null) {
            return checkCellularSpecification(groups);
        } else if (checkTreeSpecification(groups) != null) {
            return checkTreeSpecification(groups);
        } else if (checkForAddPairCoupling(groups) != null) {
            return checkForAddPairCoupling(groups);
        }

        return null;
    }

    public static Hashtable<Object,Object> checkWriteCouplingSpecification(String[] groups) {
        Hashtable<Object,Object> g = checkCellularSpecification(groups);
        if (g != null) {
            g.put("writeCoupling", groups[0]);
            return g;
        }
        g = checkTreeSpecification(groups);
        if (g != null) {
            g.put("writeCoupling", groups[0]);
            return g;
        }
        Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (groups.length == 10
                && groups[0].toLowerCase().equals("write")
                && groups[1].toLowerCase().equals("coupling")
                && groups[7].toLowerCase().equals("based")) {
            f.put("writeCoupling", groups[0]);
            f.put("node", groups[4]);
            f.put("nodeid", groups[6]);
            f.put("name", groups[9]);
            return f;
        }
        return null;
    }

    public static Hashtable<Object,Object> checkCellularSpecification(String[] groups) {
        Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (groups.length == 10
                && groups[0].toLowerCase().equals("write")
                && groups[1].toLowerCase().equals("cellular")
                && groups[7].toLowerCase().equals("based")) {
            f.put("cellular", groups[1]);
            f.put("node", groups[4]);
            f.put("nodeid", groups[6]);
            f.put("name", groups[9]);
            return f;
        }
        if (groups.length == 10
                && groups[0].toLowerCase().equals("write")
                && groups[1].toLowerCase().equals("cyclic")
                && groups[7].toLowerCase().equals("based")) {
            f.put("cellular", groups[1]);
            f.put("node", groups[4]);
            f.put("nodeid", groups[6]);
            f.put("name", groups[9]);
            return f;
        }
        return null;
    }

    public static Hashtable<Object,Object> checkTreeSpecification(String[] groups) {
        Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (groups.length == 10
                && groups[0].toLowerCase().equals("write")
                && groups[1].toLowerCase().equals("tree")
                && groups[7].toLowerCase().equals("based")) {
            f.put("tree", groups[1]);
            f.put("node", groups[4]);
            f.put("nodeid", groups[6]);
            f.put("name", groups[9]);
            return f;
        }
        return null;
    }

    public static boolean countAddCouplingForPair(String[] groups, String name) {
        if (groups.length == 9
                && groups[0].toLowerCase().equals("add")
                && groups[1].toLowerCase().equals("coupling")
                && groups[3].toLowerCase().equals("from")
                && groups[2].equals(name + "pair")) {
            return true;
        } else {
            return false;
        }
    }

    public static Hashtable<Object,Object> checkForAddPairCoupling(String[] groups) {
        Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (groups.length == 7
                && groups[0].toLowerCase().equals("for")
                && groups[3].toLowerCase().equals("sends")
                && groups[5].toLowerCase().equals("to")) {
            f.put("addCoupling", groups[0]);
            String first = groups[2];
            if (first.equals("leftnode")) {
                first = "left" + groups[1] + "node";
            } else if (first.equals("rightnode")) {
                first = "right" + groups[1] + "node";
            }
            f.put("from", first);
            f.put("outport", groups[4]);
            String second = groups[6];
            if (second.equals("leftnode")) {
                second = "left" + groups[1] + "node";
            } else if (second.equals("rightnode")) {
                second = "right" + groups[1] + "node";
            }
            f.put("to", second);
            f.put("name", groups[1]);
            return f;
        }
        return null;
    }

    public void addCouplingSpecification(String contents, sesRelation ses) {
        for (Object o : getCouplingSpecificationNames(ses)) {
            addCouplingSpecification(o.toString(), contents, ses);
        }
    }


    public String defineNeighborhood(sesRelation ses, String name, String node, String nodeid) {
        String spec = node + "-" + nodeid + "Spec";
        Set nodeids = ses.specHasEntity().get(spec);
        nodeids = getBackEnums(nodeids);
        String separator = "";
        String s = "";
        int[] max = (int[]) sesRelationExtend.multiplicityFn.get(node + "-" + nodeid + "Spec");
        int imax = 0;
        int iimax = 0;
        int iiimax = 0;
        if (max.length == 1) {
            imax = max[0];
        } else if (max.length == 2) {
            imax = max[0];
            iimax = max[1];
        } else if (max.length == 3) {
            imax = max[0];
            iimax = max[1];
            iiimax = max[2];
        }
        HashSet<Object> ents = new HashSet<Object>();
        TreeSet oents = new TreeSet(nodeids);
        int count = oents.size();
        if (count == 0) {
            ents = new HashSet<Object>(oents);
        } else if (count == 1) {
            Iterator oie = oents.iterator();
            String ospecEnt = (String) oie.next();
            sesRelationExtend.reset(ospecEnt);
            for (int i = 0; i < imax; i++) {
                ents.add(sesRelationExtend.getNext(ospecEnt));
            }
        } else if (count == 2) {
            Iterator oie = oents.iterator();
            String ospecEnti = (String) oie.next();
            sesRelationExtend.reset(ospecEnti);
            String ospecEntii = (String) oie.next();
            for (int i = 0; i < imax; i++) {
                String ent = sesRelationExtend.getNext(ospecEnti);
                sesRelationExtend.reset(ospecEntii);
                for (int ii = 0; ii < iimax; ii++) {
                    ents.add(ent
                            + separator + sesRelationExtend.getNext(ospecEntii));
                }
            }
        } else if (count == 3) {
            Iterator oie = oents.iterator();
            String ospecEnti = (String) oie.next();
            String ospecEntii = (String) oie.next();
            String ospecEntiii = (String) oie.next();
            sesRelationExtend.reset(ospecEnti);
            for (int i = 0; i < imax; i++) {
                String ent = sesRelationExtend.getNext(ospecEnti);
                sesRelationExtend.reset(ospecEntii);
                for (int ii = 0; ii < iimax; ii++) {
                    String ient = ent + separator + sesRelationExtend.getNext(ospecEntii);
                    sesRelationExtend.reset(ospecEntiii);
                    for (int iii = 0; iii < iiimax; iii++) {
                        ents.add(ient
                                + separator + sesRelationExtend.getNext(ospecEntiii));
                    }
                }
            }
        }
        if (count == 1) {
            Iterator it = oents.iterator();
            String first = it.next().toString();
            for (Object o : ents) {
                HashSet<Object> neighbors = new HashSet<Object>();
                String Enum = o.toString();
                int[] coords = extractCoords(Enum, oents);
                neighbors = getNeigbors(name, coords, max);
                for (Object n : neighbors) {
                    int[] ncoords = (int[]) n;
                    String x1 = first + ncoords[0];
                    s += "add coupling " + name + "pair from " + Enum + " to " + x1 + " in " + nodeid + "!";
                }
            }
        } else if (count == 2) {
            Iterator it = oents.iterator();
            String first = it.next().toString();
            String second = it.next().toString();
            for (Object o : ents) {
                HashSet<Object> neighbors = new HashSet<Object>();
                String Enum = o.toString();
                int[] coords = extractCoords(Enum, oents);
                neighbors = getNeigbors(name, coords, max);
                for (Object n : neighbors) {
                    int[] ncoords = (int[]) n;
                    String x1y0 = first + ncoords[0] + second + ncoords[1];
                    s += "add coupling " + name + "pair from " + Enum + " to " + x1y0 + " in " + nodeid + "!";
                }
            }


        }
        return s;
    }

    public String defineTree(sesRelation ses, String name, String node, String nodeid) {
        String spec = node + "-" + nodeid + "Spec";
        Set nodeids = ses.specHasEntity().get(spec);
        nodeids = getBackEnums(nodeids);
        String s = "";
        int[] max = (int[]) sesRelationExtend.multiplicityFn.get(node + "-" + nodeid + "Spec");
        int imax = 0;
        if (max.length == 1) {
            imax = max[0];
        }
        HashSet<Object> ents = new HashSet<Object>();
        TreeSet oents = new TreeSet(nodeids);
        int count = oents.size();
        if (count == 1) {
            Iterator oie = oents.iterator();
            String ospecEnt = (String) oie.next();
            sesRelationExtend.reset(ospecEnt);
            for (int i = 0; i < imax; i++) {
                ents.add(sesRelationExtend.getNext(ospecEnt));
            }
            Iterator it = oents.iterator();
            String first = it.next().toString();
            for (Object o : ents) {
                HashSet<Object> neighbors = new HashSet<Object>();
                String Enum = o.toString();
                int[] coords = extractCoords(Enum, oents);
                neighbors = getChildren(coords, max);
                for (Object n : neighbors) {
                    int[] ncoords = (int[]) n;
                    String x1 = first + ncoords[0];
                    s += "add coupling " + name + "pair from " + Enum + " to " + x1 + " in " + nodeid + "!";

                }
            }
        }
        return s;
    }

    public String defineTreeD(sesRelation ses, String name, String node, String nodeid) {
        String spec = node + "-" + nodeid + "Spec";
        Set nodeids = ses.specHasEntity().get(spec);
        nodeids = getBackEnums(nodeids);
        String s = "";
        int[] max = (int[]) sesRelationExtend.multiplicityFn.get(node + "-" + nodeid + "Spec");
        int imax = 0;
        if (max.length == 1) {
            imax = max[0];
        }
        HashSet<Object> ents = new HashSet<Object>();
        TreeSet oents = new TreeSet(nodeids);
        int count = oents.size();
        if (count == 1) {
            Iterator oie = oents.iterator();
            String ospecEnt = (String) oie.next();
            sesRelationExtend.reset(ospecEnt);
            for (int i = 0; i < imax; i++) {
                ents.add(sesRelationExtend.getNext(ospecEnt));
            }
            Iterator it = oents.iterator();
            String first = it.next().toString();
            for (Object o : ents) {
                HashSet<Object> neighbors = new HashSet<Object>();
                String Enum = o.toString();
                int[] coords = extractCoords(Enum, oents);
                neighbors = getChildren(coords, max);
                for (Object n : neighbors) {
                    int[] ncoords = (int[]) n;
                    String x1 = first + ncoords[0];
                    s += "add coupling " + name + "pair from " + Enum + " to " + x1 + " in " + nodeid + "!";

                }
            }
        }
        return s;
    }

    public int[] extractCoords(String Enum, TreeSet ents) {
        int[] coords = new int[ents.size()];
        if (ents.size() == 1) {
            Iterator it = ents.iterator();
            String first = it.next().toString();
            int ind = Enum.indexOf(first) + first.length();
            Enum = Enum.substring(ind);
            coords[0] = Integer.parseInt(Enum);
        }
        if (ents.size() == 2) {
            Iterator it = ents.iterator();
            String first = it.next().toString();
            String second = it.next().toString();
            int ind = Enum.indexOf(first) + first.length();
            Enum = Enum.substring(ind);
            String coord = Enum.substring(0, Enum.indexOf(second));
            coords[0] = Integer.parseInt(coord);
            ind = Enum.indexOf(second) + second.length();
            coord = Enum.substring(ind);
            coords[1] = Integer.parseInt(coord);
        }
        return coords;
    }

    public HashSet<Object> getBackEnums(Set ents) {
    	return sesRelationExtend.getBackEnums(ents);
    }

    public HashSet<Object> getNeigbors(String name, int[] coords, int[] max) {
        HashSet<Object> es = new HashSet<Object>();
        if (coords.length == 1) {
            int j = coords[0] + 1;
            if (j < 0) {
                j = max[0] - 1;
            }
            if (j >= max[0]) {
                j = 0;
            }
            es.add(new int[]{j});
        } 
        else if (coords.length == 2) {
            if (name.contains("EW")) {
                int j = coords[0] + 1;
                if (j < 0) {
                    j = max[0] - 1;
                }
                if (j >= max[0]) {
                    j = 0;
                }

                es.add(new int[]{j, coords[1]});

            } else {//name = cellNS
                int j = coords[1] + 1;
                if (j < 0) {
                    j = max[1] - 1;
                }
                if (j >= max[1]) {
                    j = 0;
                }
                es.add(new int[]{coords[0], j});
            }
        }
        return es;
    }

    public HashSet<Object> getChildren(int[] coords, int[] max) {
        HashSet<Object> es = new HashSet<Object>();
        if (coords.length == 1) {
            int j = 2 * coords[0];
            if (j > 0 && j < max[0]) {
                es.add(new int[]{j});
            }

            int k = 2 * coords[0] + 1;
            if (k > 0 && k < max[0]) {
                es.add(new int[]{k});
            }
        }
        return es;
    }

    public void addCouplingSpecification(String name, String contents, sesRelation ses) {
        Pattern p = Pattern.compile("!");
        String[] sentences = p.split(contents);
        int countOfPairs = 0;
        for (int i = 0; i < sentences.length; i++) {
            sesParse par = new sesParse();
            String[] groups = par.getParts(sentences[i]);
            if (countAddCouplingForPair(groups, name)) {
                countOfPairs++;
            }
        }
        Hashtable<Object,Object> f = null;
        for (int i = 0; i < sentences.length; i++) {
            f = contextPrune.preparse(sentences[i]);
            if (f == null) {
                continue;
            }
            if (f.get("writeCoupling") != null
                    || f.get("writeCellular") != null
                    )
            {
                String node = (String) f.get("node");
                String nodeid = (String) f.get("nodeid");
                if (name.equals((String) f.get("name"))) {
                    String coupSpec = writeCouplingSpecification(name, node, nodeid, countOfPairs);
                    sesRelation sesforcoup = Mappings.toSesRelationFromTxt(coupSpec);
                    String asp = name + "pair-" + name + "paircompDec";
                    HashSet<Object> couplings = getCouplings(name, contents);
                    for (Object o : couplings) {
                        Hashtable<Object,Object> fc = (Hashtable<Object,Object>) o;
                        sesforcoup.addCouplingToAspect(fc, asp);
                    }
                    sesforcoup.restructureAllMultiAspectsOverSpec(name + "pair-" + name + "pairidSpec");
                    ses.mergeSeS(sesforcoup);
                }
            }
        }
    }

    public String addNeighborhoodSpecification(String contents, sesRelation ses) {
        String s = "";
        Pattern p = Pattern.compile("!");
        String[] sentences = p.split(contents);
        Hashtable<Object,Object> f = null;
        for (int i = 0; i < sentences.length; i++) {
            f = contextPrune.preparse(sentences[i]);
            if (f == null) {
                continue;
            }
            if (f.get("cellular") != null) {
                String node = (String) f.get("node");
                String nodeid = (String) f.get("nodeid");
                String name = (String) f.get("name");
                s += defineNeighborhood(ses, name, node, nodeid);
            } else if (f.get("tree") != null) {
                String node = (String) f.get("node");
                String nodeid = (String) f.get("nodeid");
                String name = (String) f.get("name");
                s += defineTree(ses, name, node, nodeid);
            }
        }
        return s;
    }

    public HashSet<Object> getCouplings(String name, String contents) {
        HashSet<Object> res = new HashSet<Object>();
        Pattern p = Pattern.compile("!");
        String[] sentences = p.split(contents);
        Hashtable<Object,Object> f = null;
        for (int i = 0; i < sentences.length; i++) {
            f = contextPrune.preparse(sentences[i]);
            if (f == null) {
                continue;
            }
            if (f.get("addCoupling") != null) {
                if (name.equals((String) f.get("name"))) {
                    String from = (String) f.get("from");
                    String to = (String) f.get("to");
                    String outport = (String) f.get("outport");
                    Hashtable<Object,Object> fc = new Hashtable<Object,Object>();
                    fc.put("source", from);
                    fc.put("destination", to);
                    fc.put("outport", "out" + outport);
                    fc.put("inport", "in" + outport);
                    res.add(fc);
                }
            }
        }
        return res;
    }

    public static HashSet<Object> getCouplingSpecificationNames(sesRelation ses) {
        HashSet<Object> res = new HashSet<Object>();
        HashSet<Object> allNames = ses.getEnsembleSet("entityNames");
        for (Object o : allNames) {
            String nm = o.toString();
            if (nm.endsWith("CouplingSpecification")) {
                res.add(nm.substring(0, nm.length() - "CouplingSpecification".length()));
            }
        }
        return res;
    }

    public static String writeMultipleEnums(String name, int number) {
        String s = "\n";
        for (int i = 0; i < number; i++) {
            s += "\n" + name + "pair can be enum" + i + " in " + name + "pairid!!";
        }
        return s;
    }

    public static String writeCouplingSpecification(String name, String node, String nodeid, int countOfPairs) {
        String s = "\n";
        s += "\nFrom the coupling perspective," + name + "CouplingSpecification is made of " + name + "pairs!";
        s += "\nFrom the " + name + "pairmult perspective, " + name + "pairs are made of more than one " + name + "pair!";
        s += "\nFrom the " + name + "paircomp perspective, " + name + "pair is made of left" + name + "node and right" + name + "node !";
        s += writeMultipleEnums(name, countOfPairs);
        s += "\nleft" + name + "node is like " + node + " in " + nodeid + "!";
        s += "\nright" + name + "node is like " + node + " in " + nodeid + "!";
        return s;
    }

    public static sesRelation sesForCouplingSpecification(String coupSpec) {
        sesRelation ses = Mappings.toSesRelationFromTxt(coupSpec);
        return ses;
    }

    public String preProcessAddCouple(String contents, sesRelation ses) {
        HashSet<Object> pairs = getPairEnts(ses);
        String s = "";
        Pattern p = Pattern.compile("!");
        String[] sentences = p.split(contents);

        for (Object o : pairs) {
            String pair = o.toString();
            Set asps = ses.entityHasAspect().get(pair);
            if ( asps == null || asps.isEmpty()) {
                continue;
            }
            String pairasp = (String) asps.iterator().next();
            if (pairasp == null) {
                continue;
            }
            Set chds = ses.aspectHasEntity.get(pairasp);
            Iterator it = chds.iterator();
            String leftnode = (String) it.next();
            String rightnode = (String) it.next();
            if (leftnode.startsWith("right")) {
                String oldleft = leftnode;
                leftnode = rightnode;
                rightnode = oldleft;
            }
            Set pairids = ses.entityHasSpec().get(pair);
            if (pairids != null){
            if (pairids.isEmpty()) {
                continue;
            }
            String pairid = (String) pairids.iterator().next();

            Set enums = ses.specHasEntity.get(pairid);
            Iterator ie = enums.iterator();
            String enumVal = (String) ie.next();
            boolean fresh = true;
            for (int i = 0; i < sentences.length; i++) {
                sesParse par = new sesParse();
                String sent = sentences[i];
                String[] groups = par.getParts(sent);
                String res = checkAddCouplingForPair(groups, pair, enumVal, leftnode, rightnode);
                if (!res.equals("")) {
                    s += res;
                    fresh = false;
                    if (ie.hasNext()) {
                        enumVal = (String) ie.next();
                        fresh = true;
                    }
                }
            }
            if (fresh) {
                String parasp = (String) ses.getAspectParents(enumVal + "_" + pair).iterator().next();
                if(ses.aspectHasEntity().containsKey(parasp)){
                	HashSet<Object> valSet = ses.aspectHasEntity().get(parasp);
                	valSet.remove(enumVal + "_" + pair);
                }
            }
            while (ie.hasNext()) {
                enumVal = (String) ie.next();
                String parasp = (String) ses.getAspectParents(enumVal + "_" + pair).iterator().next();
                if(ses.aspectHasEntity().containsKey(parasp)){
                	HashSet<Object> valSet = ses.aspectHasEntity().get(parasp);
                	valSet.remove(enumVal + "_" + pair);
                }
            }
        }
        }
        return s;
    }

    public static HashSet<Object> getPairEnts(sesRelation ses) {
        Set mults = ses.getEnsembleSet("entityNames");
        HashSet<Object> res = new HashSet<Object>();
        for (Object o : mults) {
            String mult = o.toString();
            if (mult.endsWith("CouplingSpecification")) {
                res.add(mult.substring(0, mult.length() - "CouplingSpecification".length()) + "pair");
            }
        }
        return res;
    }

    public static String getPairEntAddCouple(String[] groups) {
        if (groups.length == 9
                && groups[0].toLowerCase().equals("add")
                && groups[1].toLowerCase().equals("coupling")
                && groups[3].toLowerCase().equals("from")) {
            String pair = groups[2].trim();
            return pair;
        }
        return null;
    }

    public static String checkAddCouplingForPair(String[] groups, String pair, String enumVal, String leftnode, String rightnode) {
        String res = "";
        if (groups.length == 9
                && groups[0].toLowerCase().equals("add")
                && groups[1].toLowerCase().equals("coupling")
                && groups[3].toLowerCase().equals("from")
                && groups[2].equals(pair)) {
            String from = groups[4].trim();
            String to = groups[6].trim();
            String nodeid = groups[8].trim();

            res = "select " + from + " from " + nodeid + " for " + leftnode + " under " + enumVal + "_" + pair + "!";
            res += "select " + to + " from " + nodeid + " for " + rightnode + " under " + enumVal + "_" + pair + "!";
        }
        return res;
    }
    //////////////////////////////// postProcess

    public static Hashtable<Object,Object> postParse(String sentance) {
        sesParse par = new sesParse();
        String[] groups = par.getParts(sentance);
        if (checkModelName(groups) != null) {
            return checkModelName(groups);
        } else if (checkWriteDoc(groups) != null) {
            return checkWriteDoc(groups);
        } else if (checkWriteFiles(groups) != null) {
            return checkWriteFiles(groups);
        } else if (checkWriteXml(groups) != null) {
            return checkWriteXml(groups);
        }
        return null;
    }

    public static Hashtable<Object,Object> checkModelName(String[] groups) {
        Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (groups.length == 6
                && groups[0].toLowerCase().equals("set")
                && groups[1].toLowerCase().equals("name")) {
            f.put("setName", "set");
            f.put("name", groups[5]);
            return f;
        }
        return null;
    }

    public static Hashtable<Object,Object> checkWriteDoc(String[] groups) {
        Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (groups.length == 2
                && groups[0].toLowerCase().equals("write")
                && groups[1].toLowerCase().equals("documentation")) {
            f.put("writeDoc", "write");
            return f;
        }
        return null;
    }

    public static Hashtable<Object,Object> checkWriteXml(String[] groups) {
        Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (groups.length == 2
                && groups[0].toLowerCase().equals("write")
                && groups[1].toLowerCase().equals("xml")) {
            f.put("writeXml", "write");
            return f;
        }
        return null;
    }

    public static Hashtable<Object,Object> checkWriteFiles(String[] groups) {
        Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (groups.length == 3
                && groups[0].toLowerCase().equals("write")
                && groups[1].toLowerCase().equals("class")) {
            f.put("writeFiles", "write");
            return f;
        }
        return null;
    }

    public static String parseNPerform(String contents, BasicWork dig) {
        Pattern p = Pattern.compile("!");
        String[] sentences = p.split(contents);

        Hashtable<Object,Object> f = null;

        for (int i = 0; i < sentences.length; i++) {
            f = postParse(sentences[i]);
            if (f == null) {
                continue;
            }
            if (f.get("setName") != null) {
                String name = (String) f.get("name");
                //no change in name
                PESToDEVSOnTheFly.changeDigName(dig, name);
            }
            else if (f.get("writeDoc") != null) {
                dig.writeDoc();
            } else if (f.get("writeFiles") != null) {
            	WriteClassFiles.hashAtomicPorts = new Hashtable<String,Vector<AtomicModelImpl>>();
            	
                WriteClassFiles.mapDEVSToFileWExceptions(dig,
                        dig.packageNm, BasicWork.folderJava, new HashSet<Object>());
            } else if (f.get("writeXml") != null) {
                generatePruningsWInher.writePruneSchemaDoc(dig.folderXml + dig.getName() + "Inst.xml", dig.folderXml + dig.getName() + "Schema.xsd");
            }
        }
        return "";
    }
    public static String parseNPerform(String contents, BasicWork dig, boolean isAnimation) {
        Pattern p = Pattern.compile("!");
        String[] sentences = p.split(contents);

        Hashtable<Object,Object> f = null;

        for (int i = 0; i < sentences.length; i++) {
            f = postParse(sentences[i]);
            if (f == null) {
                continue;
            }
            if (f.get("setName") != null) {
                String name = (String) f.get("name");
                //no change in name
                PESToDEVSOnTheFly.changeDigName(dig, name);
            }
            else if (f.get("writeDoc") != null) {
                dig.writeDoc();
            } else if (f.get("writeFiles") != null) {
  //need to change dig to CoupledModelImpl
            	if(isAnimation)
            		WriteClassFiles.mapDEVSToFileWExceptions(dig,
                            dig.packageNm, dig.folderAnimation, new HashSet<Object>());
            	else             		
            		WriteClassFiles.mapDEVSToFileWExceptions(dig,
            				dig.packageNm, BasicWork.folderJava, new HashSet<Object>());
            } else if (f.get("writeXml") != null) {
                generatePruningsWInher.writePruneSchemaDoc(dig.folderXml + dig.getName() + "Inst.xml", dig.folderXml + dig.getName() + "Schema.xsd");
            }
        }
        return "";
    }
    public static HashSet<Object> getColors(String nm) {
        HashSet<Object> res = new HashSet<Object>();
        String rem = nm;
        while (true) {
            int ind = rem.indexOf("_");
            if (ind > 0) {
                res.add(rem.substring(0, ind));
                rem = rem.substring(ind + 1, rem.length());
            } else {
                break;
            }
        }
        return res;
    }

    public static boolean haveSameColor(String nm1, String nm2) {
        int ind = nm1.lastIndexOf("_");
        int ind2 = nm2.lastIndexOf("_");
        if (ind < 0 || ind2 < 0) {
            return false;
        }
        String col1 = nm1.substring(0, ind);
        String col2 = nm2.substring(0, ind);
        return col1.equals(col2);

    }

    public static AtomicModelImpl findMatch(AtomicModel pnode, CoupledModelImpl dig) {
        for (AtomicModel comp : dig.getChildren()) {
            HashSet<Object> es1 = getColors(comp.getName());
            HashSet<Object> es2 = getColors(pnode.getName());
            es1.retainAll(es2);
            if (!es1.isEmpty() && !es2.isEmpty() && !es1.isEmpty()) {
                return (AtomicModelImpl)comp;
            }

        }
        return null;
    }

    public static void transferPairCoupling(CoupledModelImpl dig, CoupledModelImpl pairdig) {
        ArrayList<AtomicModel> al = pairdig.getChildren();
        if(al.size()!=2)return;
        AtomicModel leftnode = al.get(0);
        AtomicModel rightnode = al.get(1);
        AtomicModelImpl comp1 = findMatch(leftnode, dig);
        AtomicModelImpl comp2 = findMatch(rightnode, dig);
        if (comp1 == null || comp2 == null) {
            return;
        }
        ArrayList<Coupling> ac = pairdig.getCouplings();
        for (Coupling c:ac){
        	AtomicModel src = c.getSource();
        	Port srcPrt = c.getSourcePort();
        	AtomicModel dest = c.getDestination();
        	Port destPrt = c.getDestinationPort();
        	if (src.getName().equals(leftnode.getName())){
        		srcPrt.setModel(comp1);
        		comp1.addOutputPort(srcPrt);
        		destPrt.setModel(comp2);
        		comp2.addInputPort(destPrt);
        		dig.addCoupling(srcPrt, destPrt);	
        	}
        	if (src.getName().equals(rightnode.getName())){
                        srcPrt.setModel(comp2);
            		comp2.addOutputPort(srcPrt);
            		destPrt.setModel(comp1);
            		comp1.addInputPort(destPrt);
            		dig.addCoupling(srcPrt, destPrt);
            	}
        }
    }
    

    public static HashSet<Object> getCouplingSpecifications(CoupledModelImpl dig) {
        HashSet<Object> res = new HashSet<Object>();
        ArrayList<AtomicModel> al = dig.getChildren();
       for (AtomicModel am:al) {
            if (am.getName().endsWith("CouplingSpecification")) {
                res.add(am);
            }
        }
        return res;
    }
    public static void removeAllSpecifications(CoupledModelImpl dig) {
        HashSet<Object> es = getCouplingSpecifications(dig);
        for (Object o : es) {
            AtomicModel d = (AtomicModel) o;
            if (!(d instanceof CoupledModel)){
                System.out.println("Not well specified: "+d.getName());
                return;
                }
            CoupledModel parent = (CoupledModel) d.getParent();
            parent.removeChildModel(d);
        }
    }


    public static void transferPairCoupling(CoupledModelImpl dig) {
        Iterator it = getCouplingSpecifications(dig).iterator();
        while (it.hasNext()) {
            AtomicModel iod = (AtomicModel) it.next();
            if (iod instanceof CoupledModel) {
                CoupledModel coupSpec = (CoupledModel) iod;
                for (Object o : coupSpec.getChildren()) {
                    CoupledModelImpl pairdig = (CoupledModelImpl) o;
                    transferPairCoupling(dig,  pairdig);
                }
            }
        }
        ArrayList<AtomicModel> al = dig.getChildren();
         for (AtomicModel iod:al)
            {
            
            if (iod.getName().endsWith("CouplingSpecification")) {
                continue;
            }
            if (iod instanceof CoupledModel) {
                transferPairCoupling((CoupledModelImpl) iod);
            }
        }
        PESToDEVSOnTheFly.removeSelfCoupling(dig);
        removeAllSpecifications(dig);
    }
    public static void removeNotPresents(CoupledModel dig) {
        HashSet<Object> removes = new HashSet<Object>();
   ArrayList<AtomicModel> al = dig.getChildren();
     for (AtomicModel iod:al)
        {
            if (iod.getName().toLowerCase().contains("notpresent")) {
                removes.add(iod);
            } 
            else if (iod instanceof CoupledModel) {
            	removeNotPresents((CoupledModel)iod);
            }
        }

        for (Object o : removes) {
        	AtomicModel am = (AtomicModel)o;
        	CoupledModel par =(CoupledModel)am.getParent();
            
            par.removeChildModel(am);
        
        }
    }

    public static LinkedList<Object> accumulate(String var, sesRelation ses, String entity, Element el) {
        LinkedList<Object> res = new LinkedList<Object>();
        return accumulate(var, ses, entity, el, res);
    }

    public static LinkedList<Object> accumulate(String var, sesRelation ses, String entity, Element el, LinkedList<Object> res) {
        Attr attr = el.getAttributeNode(var + "DefaultValue");
        String ss = el.getAttribute("pruneName") + " " + attr.getName() + "," + attr.getNodeValue().substring(0, attr.getNodeValue().length() - "value".length());
        res.add(ss);
        if (ses.isLeaf(entity)) {
            return res;
        }
        LinkedList<Object> specs = generatePrunings.getSpecializationElementsOfEntity(el);
        LinkedList<Object> asps = generatePrunings.getAspectElementsOfEntity(el);
        if (!asps.isEmpty()) {
            Element asp = generatePrunings.selectElementFrom(asps);
            LinkedList<Object> ents = pruneOps.getActualChildren(asp);
            for (Object o : ents) {
                res.addAll(accumulate(var, ses, ((Element) o).getNodeName(), (Element) o));
            }
        }
        for (Object s : specs) {
            Element spec = (Element) s;
            Element entel = generatePrunings.selectNGetEntityFromSpec(spec);
            if (entel != null) {
                res.addAll(accumulate(var, ses, entel.getNodeName(), entel));
            }
        }
        return res;
    }

    public static void accumulateAllCommonVars(sesRelationExtend ses, String entity, Element el) {
        for (Object var : ses.commonVariables) {
            accumulateValue(var.toString(), ses, entity, el);
        }
    }

    public static double accumulateValue(String var, sesRelationExtend ses, String entity, Element el) {
        Attr attr = el.getAttributeNode(var + "DefaultValue");
        if (attr == null) {
            return 0;
        }
        String value = attr.getNodeValue().substring(0, attr.getNodeValue().length() - "value".length());

        if (ses.isLeaf(entity)) {
            return Double.parseDouble(value);
        }
        double sum = 0;
        LinkedList<Object> specs = generatePrunings.getSpecializationElementsOfEntity(el);
        LinkedList<Object> asps = generatePrunings.getAspectElementsOfEntity(el);
        if (!asps.isEmpty()) {
            Element asp = generatePrunings.selectElementFrom(asps);
            LinkedList<Object> ents = pruneOps.getActualChildren(asp);
            for (Object o : ents) {
                sum += accumulateValue(var, ses, ((Element) o).getNodeName(), (Element) o);
            }
        }
        for (Object s : specs) {
            Element spec = (Element) s;
            Element entel = generatePrunings.selectNGetEntityFromSpec(spec);
            if (entel != null) {
                sum += accumulateValue(var, ses, entel.getNodeName(), entel);
            }
        }
        el.setAttribute(var + "DefaultValue", Double.toString(sum) + "value");
        return sum;
    }

	public static void saveEntities(sesRelationExtend rses){
		
		NodeList nodes = rses.sesRoot.getElementsByTagName("entity");
		if(nodes!=null){
			for(int i=0; i< nodes.getLength(); i++){
				Node entity = nodes.item(i);
				Element ent = (Element) entity;
				String entName = ent.getAttribute("name");
				if(entName != null){
					System.out.println("entity.name : "+entName);
					PESToDEVSOnTheFly.entitiesInPrune.add(entName);
				}
			}
			
		}
		
	}
}
