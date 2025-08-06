package com.ms4systems.devs.core.util;

import java.util.*;
import java.io.File;
import java.util.regex.Pattern;
import org.w3c.dom.*;


public class ContentsWork extends contextPrune {

    public ContentsWork() {

    }


    public ContentsWork(String proName) {     
        System.out.println("workspace : " + workspace);
		projectNm = proName;
		packageNm = "Models.java.";
    }

    public ContentsWork(String sesfile, String pesfile) throws ClassNotFoundException{
        this();
        doWork(sesfile, pesfile);
    }

    public void doWork(String sesfile, String pesfile) throws ClassNotFoundException{
        sesRelationExtend rses = restructureNSave(sesfile, pesfile);
        rses.printTree();
        com.ms4systems.devs.core.util.pruningTable pruningTable = getPruneInfoNL(pesfile, rses);
        pruneAndTransform(rses, pruningTable);
    }
//
    public void doWork(String sesfile, String pesfile, boolean isAnimation) throws ClassNotFoundException{
        sesRelationExtend rses = restructureNSave(sesfile, pesfile);
        rses.printTree();
        com.ms4systems.devs.core.util.pruningTable pruningTable = getPruneInfoNL(pesfile, rses);
        pruneAndTransform(rses, pruningTable, isAnimation);
    }
    public void doWork(String sesfile) throws ClassNotFoundException{
        sesRelationExtend rses = restructureNSave(sesfile);
        rses.printTree();
        pruneAndTransform(rses, new com.ms4systems.devs.core.util.pruningTable());
    }

    public sesRelationExtend restructureNSave(String sesfile, String pesfile) {
        sesRelationExtend rses = new sesRelationExtend(folderSes, sesfile);
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
            doRestructureMultiAspects(sesfile, rses);
        }
        addCommonVariables(rses, folderSes + sesfile);
        rses.doExtendedCoupling(folderSes + sesfile);
        return rses;
    }

    public void doWork(String sesfile, String pesfile, String node, String specToExpand, int[] multint) throws ClassNotFoundException{
        sesRelationExtend rses = restructureNSave(sesfile, pesfile, node, specToExpand, multint);
        rses.printTree();
        com.ms4systems.devs.core.util.pruningTable pruningTable = getPruneInfoNL(pesfile, rses);
        pruneAndTransform(rses, pruningTable);
    }

    public sesRelationExtend restructureNSave(String sesfile, String pesfile, String node, String specToExpand, int[] multint) {
        sesRelationExtend rses = new sesRelationExtend(folderSes, sesfile);
        sesRelationExtend.addSpecAndMultiplicity(node + "-" + specToExpand + "Spec", multint);
        rses.restructureAllMultiAspectsOverSpec(specToExpand);
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
        doRestructureMultiAspects(sesfile, rses);
        addCommonVariables(rses, folderSes + sesfile);
        return rses;
    }

    public sesRelationExtend restructureNSave(String sesfile) {
        sesRelation ses = restructureAndSave(sesfile, "");
        sesRelationExtend rses = new sesRelationExtend(ses);
        addCommonVariables(rses, folderSes + sesfile);
        return rses;
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
    public void doRestructureMultiAspects(String natLangFile, sesRelationExtend ses) {
        if (natLangFile.equals("")) {
            return;
        }
        // here is some problem
        String contents = "";
        if(natLangFile.contains("ses")) contents = fileHandler.getContentsAsString(folderSes + natLangFile);
        else
        	contents = fileHandler.getContentsAsString(folderPes + natLangFile);
        if (contents == null) {
            System.out.println("wrong file path");
            return;
        }
        restructureInfo.parseNRestructure(contents, ses);
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

    public static String doPostProcessing(String folderPlusPesfile, MainForExecution pe) {
        String contents = fileHandler.getContentsAsString(folderPlusPesfile);
        return parseNPerform(contents, pe);
    }
    
    public static String doPostProcessing(String folderPlusPesfile, MainForExecution pe, boolean isAnimation) {
        String contents = fileHandler.getContentsAsString(folderPlusPesfile);
        return parseNPerform(contents, pe,isAnimation);
    }

    public static void addCommonVariables(sesRelationExtend rses, String folderPlusesesfile) {
        String contents = fileHandler.getContentsAsString(folderPlusesesfile);
        restructureInfo.parseNAdd(contents, rses);
    }

    public sesRelation restructureAndSave(String sesFileNm, String specNm) {
        return restructureAndSave(sesFileNm, folderSes, specNm);
    }

    public sesRelation restructureAndSave(sesRelation ses, String sesFileNm, String specNm) {
        return restructureAndSave(ses, sesFileNm, folderSes, specNm);
    }

    public sesRelation restructureAndSave(String sesFileNm) {
        return restructureAndSave(sesFileNm, folderSes, "");
    }


    public void pruneAndTransform(sesRelationExtend ses, com.ms4systems.devs.core.util.pruningTable pruningTable)throws ClassNotFoundException {
        pruneAndTransform(ses, pruningTable, ses.PruningRules);
    }
    public void pruneAndTransform(sesRelationExtend ses, com.ms4systems.devs.core.util.pruningTable pruningTable,boolean isAnimation) throws ClassNotFoundException{
        pruneAndTransform(ses, pruningTable, ses.PruningRules, isAnimation);
    }
    public void pruneWithNoTransform(sesRelationExtend ses, com.ms4systems.devs.core.util.pruningTable pruningTable) {
        pruneWithNoTransform(ses, pruningTable, ses.PruningRules);
    }

    public void pruneAndTransform(sesRelationExtend ses, com.ms4systems.devs.core.util.pruningTable pruningTable, sesRelationExtend.pruningRules pr) throws ClassNotFoundException{
        HashSet<Object> toAdd = new HashSet<Object>();
        for (Object o : ses.getEnsembleSet("entityNames")) {
            String entity = o.toString();
            String[] pairs = pruningTable.getPairs(entity);
            if (pairs != null && pairs.length > 0) {
                for (String str : pairs) {
                	HashSet<Object> es = pr.getAction(entity, str);
                    for (Object oo : es) {
                    	com.ms4systems.devs.core.util.Pair act = (com.ms4systems.devs.core.util.Pair) oo;
                        if (act != null) {
                            toAdd.add(act);
                        }
                    }
                }
            }
            for (Object oo : toAdd) {
            	com.ms4systems.devs.core.util.Pair act = (com.ms4systems.devs.core.util.Pair) oo;
                pruningTable.addPair((String) act.getKey(), (String) act.getValue());
            }
        }
        pruneNTransform((sesRelationExtend) ses, this, pruningTable, folderJava, packageNm);
    }
    public void pruneAndTransform(sesRelationExtend ses, com.ms4systems.devs.core.util.pruningTable pruningTable, sesRelationExtend.pruningRules pr,boolean isAnimation)throws ClassNotFoundException {
        HashSet<Object> toAdd = new HashSet<Object>();
        for (Object o : ses.getEnsembleSet("entityNames")) {
            String entity = o.toString();
            String[] pairs = pruningTable.getPairs(entity);
            if (pairs != null && pairs.length > 0) {
                for (String str : pairs) {
                	HashSet<Object> es = pr.getAction(entity, str);
                    for (Object oo : es) {
                    	com.ms4systems.devs.core.util.Pair act = (com.ms4systems.devs.core.util.Pair) oo;
                        if (act != null) {
                            toAdd.add(act);
                        }
                    }
                }
            }
            for (Object oo : toAdd) {
            	com.ms4systems.devs.core.util.Pair act = (com.ms4systems.devs.core.util.Pair) oo;
                pruningTable.addPair((String) act.getKey(), (String) act.getValue());
            }
        }
        if(isAnimation)pruneNTransform((sesRelationExtend) ses, this, pruningTable, folderAnimation, packageNm);
        else pruneNTransform((sesRelationExtend) ses, this, pruningTable, folderJava, packageNm);
    }
    public void pruneWithNoTransform(sesRelationExtend ses, com.ms4systems.devs.core.util.pruningTable pruningTable, sesRelationExtend.pruningRules pr) {
    	HashSet<Object> toAdd = new HashSet<Object>();
        for (Object o : ses.getEnsembleSet("entityNames")) {
            String entity = o.toString();
            String[] pairs = pruningTable.getPairs(entity);
            if (pairs != null && pairs.length > 0) {
                for (String str : pairs) {
                	HashSet<Object> es = pr.getAction(entity, str);
                    for (Object oo : es) {
                        com.ms4systems.devs.core.util.Pair act = (com.ms4systems.devs.core.util.Pair) oo;
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
        pruneWithNoTransform((sesRelationExtend) ses, this, pruningTable, folderJava, packageNm);
    }

    public String doPostProcessing(String natLangFile) {
        if (natLangFile.equals("")) {
            return "";
        }
        String contents = fileHandler.getContentsAsString(folderTxt
                + natLangFile);
        if (contents == null) {
            System.out.println("wrong file path");
            return null;
        }
        return parseNPerform(contents, this);
    }

    public static String doPostProcessing(String folderPlusPesfile, BasicWork pe) {
        String contents = fileHandler.getContentsAsString(folderPlusPesfile);
        return parseNPerform(contents, pe);
    }

    public static String getCountOf(String comp, String XMLFile) {
        pruneOps.restorePruneDoc(XMLFile);
        return pruneOps.countNodeContains("pruneName", comp) + "";
    }

    public static void setNumberOf(String DataEntity, String NumberOfSubComp, String numString, String XMLFile) {
        pruneOps.restorePruneDoc(XMLFile);
        Element el = pruneOps.getElementOccurrence(DataEntity);
        el.removeAttribute("path");
        el.removeAttribute("pruneName");
        pruneOps.setAttrVal(DataEntity, NumberOfSubComp, numString);
        generatePruningsWInher.writePruneSchemaDoc(XMLFile, "");
    }

    public static String getAttrVal(String attr, String XMLFile) {
        pruneOps.restorePruneDoc(XMLFile);
        String attrVal = pruneOps.getAttrVal(attr + "DefaultValue");
        if (attrVal.endsWith("Value")) {
            return attrVal.substring(0, attrVal.length() - "value".length());
        } else {
            return attrVal;
        }
    }

    public static String getAttrVal(String tag, String attr, String XMLFile) {
        pruneOps.restorePruneDoc(XMLFile);
        String attrVal = pruneOps.getAttrVal(tag, attr);
        if (attrVal.endsWith("Value")) {
            return attrVal.substring(0, attrVal.length() - "value".length());
        } else {
            return attrVal;
        }
    }

    public static void setAttrVal(String DataEntity, String attr, String valString, String XMLFile) {
        pruneOps.restorePruneDoc(XMLFile);
        Element el = pruneOps.getElementOccurrence(DataEntity);
        el.removeAttribute("path");
        el.removeAttribute("pruneName");
        pruneOps.setAttrVal(DataEntity, attr, valString);
        generatePruningsWInher.writePruneSchemaDoc(XMLFile, "");
    }

    public static String getSpecializationChoice(String spec, String Schemaxsd, String XMLFile) {
        Document doc = XMLToDom.getDocument(Schemaxsd);
        NodeList nl = doc.getElementsByTagName("xs:choice");
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            Node par = n.getParentNode();
            Node gp = par.getParentNode();
            if (!((Element) gp).getAttribute("name").contains(spec)) {
                continue;
            }
            NodeList nnl = n.getChildNodes();
            for (int ii = 0; ii < nnl.getLength(); ii++) {
                Node nn = nnl.item(ii);
                if (!(nn instanceof Element)) {
                    continue;
                }
                String name = ((Element) nn).getAttribute("name");
                String numString = getCountOf(name, XMLFile);
                if (numString.equals(1 + "")) {
                    return name;
                }
            }
        }
        return "";
    }

    public static int getCountOfViaPath(Document doc, String path, String pathpart, String attr, String attrpart) {
        HashSet<Object> es = new HashSet();
        NodeList nl = doc.getElementsByTagName("*");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            if (el.getAttribute(path).contains(pathpart)
                    && el.getAttribute(attr).toLowerCase().contains(attrpart.toLowerCase())) {
                es.add(el);
            }
        }
        return es.size();
    }

    public static String getSpecializationChoice(String pathpart, String spec, String Schemaxsd, String XMLFile) {
        Document doc = XMLToDom.getDocument(Schemaxsd);
        NodeList nl = doc.getElementsByTagName("xs:choice");
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            Node par = n.getParentNode();
            Node gp = par.getParentNode();
            if (!((Element) gp).getAttribute("name").contains(spec)) {
                continue;
            }
            NodeList nnl = n.getChildNodes();
            for (int ii = 0; ii < nnl.getLength(); ii++) {
                Node nn = nnl.item(ii);
                if (!(nn instanceof Element)) {
                    continue;
                }
                String attrpart = ((Element) nn).getAttribute("name");
                Document doc1 = XMLToDom.getDocument(XMLFile);
                int count = getCountOfViaPath(doc1, "path", pathpart, "pruneName", attrpart);
                //   String numString = getCountOf(name, XMLFile);
                if (count == 1) {
                    return attrpart;
                }
            }
        }
        return "";
    }

    public static String getAttrValueViaPath(String pathpart, String attr, String XMLFile) {
        pruneOps.restorePruneDoc(XMLFile);
        NodeList nl = pruneOps.getPruneDoc().getElementsByTagName("*");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            if (el.getAttribute("path").contains(pathpart)
                    && el.hasAttribute(attr)) {
                String attrVal = el.getAttribute(attr + "DefaultValue");
                if (attrVal.endsWith("Value")) {
                    return attrVal.substring(0, attrVal.length() - "value".length());
                } else {
                    return attrVal;
                }
            }
        }
        return "";
    }

    public static String getSpecChoiceViaPathNPrune(String pathpart, String XMLFile) {
        pruneOps.restorePruneDoc(XMLFile);
        NodeList nl = pruneOps.getPruneDoc().getElementsByTagName("*");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            if (el.getAttribute("path").contains(pathpart)
                    && el.hasAttribute("pruneName")) {
                String pruneVal = el.getAttribute("pruneName");
                int ind = pruneVal.indexOf(pathpart);
                if (ind > 0) {
                    pruneVal = pruneVal.substring(0, ind - 1);
                }
                return pruneVal;
            }
        }
        return "";
    }

    public static void setAttrValueViaPath(String path, String pathpart, String attr, String val, String XMLFile) {
        pruneOps.restorePruneDoc(XMLFile);
        NodeList nl = pruneOps.getPruneDoc().getElementsByTagName("*");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            if (el.getAttribute(path).contains(pathpart)
                    && el.hasAttribute(attr)) {
                el.setAttribute(attr + "DefaultValue", val);
            }
        }
        generatePruningsWInher.writePruneSchemaDoc(XMLFile, "");
    }
///////////////////////////

    public static double getFuzzyVal(String tag, String attr) {
        if (attr.equals("simulation_time")) {
            double factor = 1;
            if (tag.equals("quick")) {
                factor = 10;
            }
            if (tag.equals("normal")) {
                factor = 100;
            }
            if (tag.equals("lengthy")) {
                factor = 1000;
            }
            return factor * generatePrunings.getNextRandom();
        }
        return 1;
    }

    public static String getInferredAttrVal(String tag, String attr, String XMLFile) {
        pruneOps.restorePruneDoc(XMLFile);
        Element el = pruneOps.getPruneElement(tag);
        LinkedList<Object> q = getAllChildrenAndSelf(el);
        for (Object o : q) {
            Element chd = (Element) o;
            if (chd.hasAttribute(attr + "DefaultValue")) {
                String val = chd.getAttribute(attr + "DefaultValue");
                if (!val.equals("")) {
                    if (val.equals("fuzzy")) {
                        return "" + getFuzzyVal(chd.getNodeName(), attr);
                    }
                    return val;
                }
            }
        }
        String attrVal = pruneOps.getAttrVal(tag, attr);
        if (!attrVal.equals("")) {
            return attrVal;
        } else {
            attrVal = pruneOps.getAttrVal(tag, attr + "DefaultValue");
            if (!attrVal.equals("")) {
                return attrVal;
            }
        }
        return "";
    }

    public static String getAttrValueInMulti(String tag, String id, String idval, String attr, String XMLFile) {
        pruneOps.restorePruneDoc(XMLFile);
        NodeList nl = pruneOps.getPruneDoc().getElementsByTagName(tag);
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            if (el.getAttribute(id).equals(idval)) {
            	LinkedList<Object> q = getAllChildrenOf(el);
                for (Object o : q) {
                    Element ch = (Element) o;
                    if (ch.hasAttribute(attr)) {
                        return ch.getAttribute(attr);
                    }
                }
            }
        }
        return "";
    }

    public static void setAttrValueInMulti(String tag, String id, String idval, String attr, String attrval, String XMLFile) {
        pruneOps.restorePruneDoc(XMLFile);
        NodeList nl = pruneOps.getPruneDoc().getElementsByTagName(tag);
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            if (el.getAttribute(id).equals(idval)) {
            	LinkedList<Object> q = getAllChildrenAndSelf(el);
                for (Object o : q) {
                    Element ch = (Element) o;
                    if (ch.hasAttribute(attr)) {
                        ch.setAttribute(attr, attrval);
                    }
                }
            }
        }
        generatePruningsWInher.writePruneSchemaDoc(XMLFile, "");
    }

    public static void setSpecInMulti(String tag, String id, String idval, String spec, String specVal, String XMLFile) {
        pruneOps.restorePruneDoc(XMLFile);
        NodeList nl = pruneOps.getPruneDoc().getElementsByTagName(tag);
        String specNm = tag + "-" + spec + "Spec";
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            if (el.getAttribute(id).equals(idval)) {
            	LinkedList<Object> q = getAllChildrenAndSelf(el);
                for (Object o : q) {
                    Element ch = (Element) o;
                    if (ch.getNodeName().equals(specNm)) {
                        generatePrunings.selectEntityFromSpec(ch, specVal);
                    }
                }
            }
        }
        generatePruningsWInher.writePruneSchemaDoc(XMLFile, "");
    }

    public static LinkedList<Object> getAllChildrenOf(Element el) {
    	LinkedList<Object> q = new LinkedList<Object>();
        NodeList nl = el.getChildNodes();
        if (nl.getLength() == 0) {
            return q;
        }
        for (int i = 0; i < nl.getLength(); i++) {
            Node ch = nl.item(i);
            if (ch.getNodeType() != Node.TEXT_NODE) {
                q.add(ch);
                if (ch instanceof Element) {
                    q.addAll(getAllChildrenOf((Element) ch));
                }
            }
        }
        return q;
    }

    public static LinkedList<Object> getAllChildrenAndSelf(Element el) {
    	LinkedList<Object> q = getAllChildrenOf(el);
        q.add(el);
        return q;
    }

    public static void setIds(String id, String XMLFile) {
    	LinkedList<Object> q = getAllChildrenOf(pruneOps.getPruneDoc().getDocumentElement());
        int i = 0;
        for (Object o : q) {
            Element ch = (Element) o;
            if (ch.hasAttribute(id)) {
                ch.setAttribute(id, i + "");
                i++;
            }
        }
        generatePruningsWInher.writePruneSchemaDoc(XMLFile, "");
    }

    public static void addMultipleEnts(String multiAsp, String id, int numEnts, String XMLFile) {
        pruneOps.restorePruneDoc(XMLFile);
        Element asp = pruneOps.getElementOccurrence(multiAsp + "-multMultiAsp");
        if (asp == null) {
            return;
        }
        Element mult = SESOps.getElement("multiAspect", asp.getNodeName());
        if (mult == null) {
            return;
        }
        LinkedList<Object> mcs = SESOps.getChildrenOfElement(mult, "entity");
        if (mcs.isEmpty()) {
            return;
        }
        Iterator mit = mcs.iterator();
        Element se = SESOps.getElement("entity", (String) mit.next());
        String cop = mult.getAttribute("coupling");
        mcs = pruneOps.getElementChildrenOfElement(mult, "numberComponentsVar");
        mit = mcs.iterator();
        Element ncv = (Element) mit.next();
        String ncvar = ncv.getAttribute("name");
        LinkedList<Object> entities = SESOps.getChildrenOfElement(mult, "entity");
        if (entities.isEmpty()) {
            return;
        }
        asp.setAttribute(ncvar, "" + numEnts);
        String entNm = (String) entities.getFirst();
        Element sen = SESOps.getElement("entity", entNm);
        for (int i = 0; i < numEnts; i++) {
            Element ent = pruneOps.getPruneDoc().createElement(entNm);
            asp.appendChild(ent);
            ent.setAttribute(id, i + "");
            generatePrunings.genericPESEntityFull(ent, sen, "");
        }
        setIds(id, XMLFile);
        removeAllPathNPruneName(XMLFile);
        Node mchd = null;
        NodeList nl = asp.getChildNodes();
        for (int ii = 0; ii < nl.getLength(); ii++) {
            Node n = nl.item(ii);
            if (!(n instanceof Element)) {
                continue;
            }
            Element e = (Element) n;
            if (!e.hasAttribute(id)) {
                mchd = n;
                break;
            }
        }
        if (mchd != null) {
            asp.removeChild(mchd);
        }
    }

    public static void removeAllPathNPruneName(String XMLFile) {
        Element root = pruneOps.getPruneDoc().getDocumentElement();
        LinkedList<Object> q = getAllChildrenOf(root);
        q.add(root);
        for (Object o : q) {
            Element ch = (Element) o;
            ch.removeAttribute("path");
            ch.removeAttribute("pruneName");
        }
        generatePruningsWInher.writePruneSchemaDoc(XMLFile, "");
    }

    public String createInst(String projectName, String sesfile, String pesfile) throws ClassNotFoundException{
        new ContentsWork(projectName);
        String root = doInheritWork(sesfile, pesfile);
        String SchemaFile = folderXml + root + "Schema.xsd";
        String xmlFile = folderXml + root + "Inst.xml";
        generatePruningsWInher.writePruneSchemaDoc(xmlFile, SchemaFile);
        return xmlFile;
    }

    public String doInheritWork(String sesfile, String pesfile) throws ClassNotFoundException{
        sesRelationExtend rses = restructureNSave(sesfile, pesfile);
        rses.printTree();
        com.ms4systems.devs.core.util.pruningTable pruningTable = getPruneInfoNL(pesfile, rses);
        pruneAndTransform(rses, pruningTable);
        doPostProcessing(pesfile);
        return rses.getRootEntityName();
    }

    public String doNoInheritWork(String sesfile, String pesfile) {
        sesRelationExtend rses = restructureNSave(sesfile, pesfile);
        rses.printTree();
        com.ms4systems.devs.core.util.pruningTable pruningTable = getPruneInfoNL(pesfile, rses);
        pruneWithNoTransform(rses, pruningTable);
        doPostProcessing(pesfile);
        return rses.getRootEntityName();
    }

    public static void writeSchemaToXMLChangeComplexType(String myFileName) {
        String s = sesToGenericSchema.writeComplexSchemaDoc();
        s = s.replaceAll("<xs:complexType>",
                "<xs:complexType mixed=" + DomToXML.quote("true") + ">");
        fileHandler.writeToFile(myFileName + ".xsd", s);
    }

    public static void writeXSD(String projectNm, String sesfile, String pesfile) {
        ContentsWork pe = new ContentsWork(projectNm);
        sesRelationExtend rses = new sesRelationExtend(pe.folderSes, sesfile);

        ContentsWork.addCommonVariables(rses, pe.folderSes + sesfile);
        ContentsWork.addCommonVariables(rses, pe.folderPes + pesfile);

        rses.printTree();
        rses.toDOM();
        writeSchemaToXMLChangeComplexType(pe.folderXml + rses.getRootEntityName() + "Schema");
    }

    public static String getItemPresent(String pathpart, String item, String XMLFile) {
        pruneOps.restorePruneDoc(XMLFile);
        NodeList nl = pruneOps.getPruneDoc().getElementsByTagName("*");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            if (el.getAttribute("path").contains(pathpart)
                    && el.getAttribute("pruneName").contains(item)) {

                return 1 + "";
            }
        }
        return 0 + "";
    }

    public String doNoRestructuringWork(String sesfile, String pesfile) {
        sesRelationExtend rses = new sesRelationExtend(folderSes, sesfile);
        rses.printTree();
        addCommonVariables(rses, folderSes + sesfile);
        addCommonVariables(rses, folderPes + pesfile);
        com.ms4systems.devs.core.util.pruningTable pruningTable = getPruneInfoNL(pesfile, rses);
        pruneWithNoTransform(rses, pruningTable);
        return rses.getRootEntityName();
    }

    public String doNoRestructuringWork(String sesfile, String pesfile, boolean prunelater) {
        sesRelationExtend rses = new sesRelationExtend(folderSes, sesfile);
        rses.printTree();
        addCommonVariables(rses, folderSes + sesfile);
        addCommonVariables(rses, folderPes + pesfile);


        String entToPrune = rses.getRootEntityName();
        createPruneDoc(rses);
        if (!entityToPrune.equals("")) {
            entToPrune = entityToPrune;
            rses = new sesRelationExtend(rses.substructure(entToPrune));
            createPruneDoc(rses);
        }

        return rses.getRootEntityName();
    }

    public int[] getMultiplicity(String sesfile) {
        String contents = fileHandler.getContentsAsString(folderSes + sesfile);
        Pattern p = Pattern.compile("!");
        String[] sentences = p.split(contents);
        Hashtable<Object,Object> f = null;
        for (int i = 0; i < sentences.length; i++) {
            f = restructureInfo.parse(sentences[i]);
            if (f == null) {
                continue;
            }
            if (f.get("multiplicity") != null) {
                String specToExpand = (String) f.get("specToExpand");
                String mult = (String) f.get("multiplicity");
                String node = (String) f.get("node");
                mult = mult.substring(1, mult.length() - 1).trim();
                int ind = mult.indexOf(",");
                int[] multint = new int[]{3, 3};
                if (ind == -1) {
                    multint = new int[]{Integer.parseInt(mult)};
                } else {
                    String first = mult.substring(0, ind).trim();
                    String second = mult.substring(ind + 1, mult.length()).trim();
                    multint[0] = Integer.parseInt(first);
                    ind = second.indexOf(",");
                    if (ind == -1) {
                        multint[1] = Integer.parseInt(second);
                    } else {
                        multint = new int[]{3, 3, 3};//do later
                    }
                }
                return multint;
            }
        }
        return new int[]{};
    }

    public int[] getMultiplicityForNode(String sesfile, String node) {
        String contents = fileHandler.getContentsAsString(folderSes + sesfile);
        Pattern p = Pattern.compile("!");
        String[] sentences = p.split(contents);
        Hashtable<Object,Object> f = null;
        for (int i = 0; i < sentences.length; i++) {
            f = restructureInfo.parse(sentences[i]);
            if (f == null) {
                continue;
            }
            if (f.get("multiplicity") != null
                    && f.get("node").equals(node)) {
                String mult = (String) f.get("multiplicity");
                mult = mult.substring(1, mult.length() - 1).trim();
                int ind = mult.indexOf(",");
                int[] multint = new int[]{3, 3};
                if (ind == -1) {
                    multint = new int[]{Integer.parseInt(mult)};
                } else {
                    String first = mult.substring(0, ind).trim();
                    String second = mult.substring(ind + 1, mult.length()).trim();
                    multint[0] = Integer.parseInt(first);
                    ind = second.indexOf(",");
                    if (ind == -1) {
                        multint[1] = Integer.parseInt(second);
                    } else {
                        multint = new int[]{3, 3, 3};//do later
                    }
                }
                return multint;
            }
        }
        return new int[]{};
    }

    public void getCountBounds(String sesfile) {
        String contents = fileHandler.getContentsAsString(folderPes + sesfile);
        Pattern p = Pattern.compile("!");
        String[] sentences = p.split(contents);
        Hashtable<Object,Object> f = null;
        for (int i = 0; i < sentences.length; i++) {
            f = restructureInfo.parse(sentences[i]);
            if (f == null) {
                continue;
            }
            if (f.get("bounds") != null) {
                String entity = (String) f.get("entity");
                String bounds = (String) f.get("bounds");
                bounds = bounds.substring(1, bounds.length() - 1).trim();
                int lower = 1;
                int upper = 1000;
                int ind = bounds.indexOf(",");
                if (ind > -1) {
                    lower = Integer.parseInt(bounds.substring(0, ind).trim());
                    upper = Integer.parseInt(bounds.substring(ind + 1, bounds.length()).trim());
                }
                String parent = (String) f.get("parent");
                ContentsWork.addCountBound(entity, parent, lower, upper);
            }
        }
    }

    public static String createNewXMLInst(
            String projectNm,
            String sesfile, String pesfile) {
        ContentsWork pe = new ContentsWork(projectNm);
        String rootName = pe.doNoRestructuringWork(sesfile, pesfile);
        long timeStamp = System.currentTimeMillis();
        String XMLfile = pe.folderXml + rootName + timeStamp + "Inst.xml";
        String SchemaFile = pe.folderXml + rootName + "Schema.xsd";
        generatePruningsWInher.writePruneSchemaDoc(XMLfile, SchemaFile);

        addCopiesToAllMultAsps(
                projectNm, sesfile, pesfile,
                XMLfile,
                SchemaFile);
        countBoundsSet = new HashSet<Object>();
        pe.getCountBounds(pesfile);
        removeAllNotPresents(XMLfile);
        boolean countSatisfied = true;
        for (Object o : countBoundsSet) {
            countBounds cb = (countBounds) o;
            int count = 0;
            if (cb.parent == null) {
                count = getNumberOf(cb.entity, XMLfile);
                if (count <= cb.upper && count >= cb.lower) {
                    continue;
                } else {
                    countSatisfied = false;
                    break;
                }
            } else {
                countSatisfied = parentCountSatisfied(cb, XMLfile);
            }
        }
        if (countSatisfied) {
            ContentsWork.removeAllPathNPruneName(XMLfile);
            generatePruningsWInher.writePruneSchemaDoc(XMLfile, SchemaFile);
        } else {
            File f = new File(XMLfile);
            f.delete();
            return "";
        }
        return XMLfile;
    }

    public static String createNewXMLInst(
            String projectNm,
            String sesfile, String pesfile, boolean prunelater) {
        ContentsWork pe = new ContentsWork(projectNm);
        String rootName = pe.doNoRestructuringWork(sesfile, pesfile, prunelater);

        long timeStamp = System.currentTimeMillis();
        String XMLfile = pe.folderXml + rootName + timeStamp + "Inst.xml";
        String SchemaFile = pe.folderXml + rootName + "Schema.xsd";
        generatePruningsWInher.writePruneSchemaDoc(XMLfile, SchemaFile);

        addCopiesToAllMultAsps(
                projectNm, sesfile, pesfile,
                XMLfile,
                SchemaFile, prunelater);

        removeAllPathNPruneName(XMLfile);
        generatePruningsWInher.writePruneSchemaDoc(XMLfile, SchemaFile);
        return XMLfile;
    }

    public static LinkedList<Object> makeMultCops(String ProjectNm, String sesfile, String pesfile, String multiAsp, String multent, int numEnts) {
    	LinkedList<Object> q = new LinkedList<Object>();
        ContentsWork pe = new ContentsWork(ProjectNm);
        sesRelationExtend rses = new sesRelationExtend(pe.folderSes, sesfile);

        com.ms4systems.devs.core.util.pruningTable pruningTable =
                pe.getPruneInfoNL(pesfile, rses);
        pe.pruneWithNoTransform(rses, pruningTable);

        sesRelationExtend cop = rses.makeCopyExtend();

        for (int i = 0; i < numEnts; i++) {
            sesRelation multentCop = cop.substructure(multent);
            sesRelationExtend multExtend = new sesRelationExtend(multentCop);

            ContentsWork.addCommonVariables(multExtend, pe.folderSes + sesfile);
            ContentsWork.addCommonVariables(multExtend, pe.folderPes + pesfile);

            pe.pruneWithNoTransform(multExtend, pruningTable);
            pe.doPostProcessing(pesfile);
            q.add(pruneOps.getPruneDoc());
        }
        return q;
    }

    public static LinkedList<Object> makeMultCops(String ProjectNm,
            String sesfile, String pesfile, String multiAsp,
            String multent, int numEnts, boolean prunelater) {
    	LinkedList<Object> q = new LinkedList<Object>();
        ContentsWork pe = new ContentsWork(ProjectNm);
        sesRelationExtend rses = new sesRelationExtend(pe.folderSes, sesfile);


        sesRelationExtend cop = rses.makeCopyExtend();
        for (int i = 0; i < numEnts; i++) {
            sesRelation multentCop = cop.substructure(multent);
            sesRelationExtend multExtend = new sesRelationExtend(multentCop);

            ContentsWork.addCommonVariables(multExtend, pe.folderSes + sesfile);
            ContentsWork.addCommonVariables(multExtend, pe.folderPes + pesfile);

            pe.doPostProcessing(pesfile);
            q.add(pruneOps.getPruneDoc());
        }
        return q;
    }

    public static void addMultCop(
            String multiAsp, String multComp, String mult,
            String projectNm, String sesfile, String pesfile,
            String XMLfile,
            String SchemaFile) {
        int numSegs = 0;
        if (!multiAsp.equals("")) {
            Element multc = pruneOps.getPruneElement(multComp);
            if (multc == null) {
                return;
            }
            Element multAspc = pruneOps.getPruneElement(multiAsp + "-" + mult + "MultiAsp");
            if (multAspc == null) {
                return;
            }
            multAspc.removeChild(multc);
            int[] multint = new ContentsWork(projectNm).getMultiplicityForNode(pesfile, multComp);
            if (multint != null && multint.length > 0) {
                numSegs = multint[0];
            }
            multAspc.setAttribute("numContainedIn" + multiAsp, "" + numSegs);
            generatePruningsWInher.writePruneSchemaDoc(XMLfile, SchemaFile);
        }
        LinkedList<Object> q = makeMultCops(projectNm, sesfile, pesfile, multiAsp, multComp, numSegs);
        int i = 0;
        for (Object o : q) {
            Document curr = (Document) o;
            Element root = curr.getDocumentElement();
            pruneOps.restorePruneDoc(XMLfile);
            Element newNode = (Element) pruneOps.pruneDoc.importNode(root, true);
            Element multAsp = pruneOps.getPruneElement(multiAsp + "-" + mult + "MultiAsp");

            multAsp.appendChild(newNode);
            newNode.setAttribute("ID", i + "");
            generatePruningsWInher.writePruneSchemaDoc(XMLfile, SchemaFile);
            i++;
        }
    }

    public static int getNumberOf(String comp, String xml) {
        int count = 0;
        int ind = comp.lastIndexOf("_");
        if (ind < 0) {
            return countNodeEquals(pruneOps.getPruneDoc(), comp);
        }
        String parentOfComp = comp.substring(ind + 1);
        comp = comp.substring(0, ind);
        NodeList nl = pruneOps.getPruneDoc().getElementsByTagName("*");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            if (el.getNodeName().equals(parentOfComp)
                    && entityExistsInSubstructure(pruneOps.getPruneDoc(), el, comp)) {
                count++;
            }
        }
        return count;
    }

    public static int getNumberOf(String comp, Element parent, String XMLFile) {
        int count = 0;
        int ind = comp.lastIndexOf("_");
        if (ind < 0) {
            return countNodeEqualsInSubstructure(pruneOps.getPruneDoc(), parent, comp);
        }
        String parOfComp = comp.substring(ind + 1);
        comp = comp.substring(0, ind);
        LinkedList<Object> q = getAllChildrenOf(parent);
        for (Object o : q) {
            Element elem = (Element) o;
            if (elem.getNodeName().equals(parOfComp)) {
                boolean ex = entityExistsInSubstructure(pruneOps.getPruneDoc(), parent, comp);
                count++;
            }
        }
        return count;
    }

    public static int countNodeContains(Document doc, String part) {
        HashSet<Object> es = new HashSet<Object>();
        NodeList nl = doc.getElementsByTagName("*");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            String nodenm = el.getNodeName();
            if (nodenm.contains(part)) {
                es.add(el);
            }
        }
        return es.size();
    }

    public static int countNodeEquals(Document doc, String elemName) {
    	HashSet<Object> es = new HashSet<Object>();
        NodeList nl = doc.getElementsByTagName("*");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            String nodenm = el.getNodeName();
            if (nodenm.equals(elemName)) {
                es.add(el);
            }
        }
        return es.size();
    }

    public static int countNodeEqualsInSubstructure(Document doc, Element par, String elemName) {
    	HashSet<Object> es = new HashSet<Object>();
        LinkedList<Object> q = getAllChildrenAndSelf(par);
        for (int i = 0; i < q.size(); i++) {
            Element el = (Element) q.get(i);
            String nodenm = el.getNodeName();
            if (nodenm.equals(elemName)) {
                es.add(el);
            }
        }
        return es.size();
    }

    public static boolean entityExistsInSubstructure(Document doc, Element par, String entity) {
        Pattern p = Pattern.compile("_");
        String[] groups = p.split(entity);
        int count = 0;
        LinkedList<Object> q = getAllChildrenAndSelf(par);
        for (int i = 0; i < q.size(); i++) {
            Element el = (Element) q.get(i);
            String nodenm = el.getNodeName();
            for (String subent : groups) {
                if (nodenm.equals(subent)) {
                    count++;
                }
            }
        }
        return (count == groups.length);
    }

    public static boolean allEntitiesPresentUnderElement(String[] entities, Element SatMod) {
        for (String ent : entities) {
            int n = ContentsWork.countNodeEqualsInSubstructure(
                    pruneOps.getPruneDoc(), SatMod, ent);
            if (n == 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean entityPresentUnderElement(String entity, Element SatMod) {
        return ContentsWork.countNodeEqualsInSubstructure(
                pruneOps.getPruneDoc(), SatMod, entity) > 0;
    }

    public static HashSet<Object> getAllPresenceParentsInSubstructure(sesRelation ses, String parent) {
    	HashSet<Object> es = new HashSet<Object>();
        sesRelation sub = ses.substructure(parent);
        Hashtable<Object,HashSet<Object>> entityHasSpec = sub.entityHasSpec();
        Enumeration<Object> e = entityHasSpec.keys();
        while(e.hasMoreElements()){
        	Object key = e.nextElement();
        	HashSet<Object> value = entityHasSpec.get(key);
        	Iterator<Object> it = value.iterator();
        	while(it.hasNext()){
        		Object val = it.next();
        		if(val.toString().contains("-presenceSpec")){
        			es.add(key);
        		}
        	}
        }
        
        return es;
    }

    public static String startWithSamePrefix(String comp, String comp1) {
        if (comp.equals(comp1)) {
            return "";
        }
        for (int i = comp.length() - 5; i < comp.length() - 1; i++) {
            String prefix = comp.substring(0, i);
            if (comp1.startsWith(prefix)) {
                return prefix;
            }
        }
        return "";
    }

    public static HashSet<Object> startWithSamePrefix(HashSet<Object> comps, String comp) {
    	HashSet<Object> es = new HashSet<Object>();
        for (Object o : comps) {
            String other = (String) o;
            String prefix = startWithSamePrefix(other, comp);
            if (!prefix.equals("")) {
                es.add(prefix);
            }
        }
        return es;
    }

    public static boolean containedInOthers(HashSet<Object> prefixes, String prefix) {
        for (Object o : prefixes) {
            String other = (String) o;
            if (other.equals(prefix)) {
                continue;
            }
            if (other.contains(prefix)) {
                return true;
            }
        }
        return false;
    }

    public static HashSet<Object> commonPrefixes(HashSet<Object> comps) {
    	HashSet<Object> es = new HashSet<Object>();
        for (Object o : comps) {
            String comp = (String) o;
            HashSet<Object> res = startWithSamePrefix(comps, comp);
            es.addAll(res);
        }
        es.removeAll(minimalPrefixes(es));
        return es;
    }

    public static HashSet<Object> minimalPrefixes(HashSet<Object> prefixes) {
    	HashSet<Object> es = new HashSet<Object>();
        for (Object o : prefixes) {
            String prefix = (String) o;
            if (containedInOthers(prefixes, prefix)) {
                es.add(prefix);
            }
        }
        return es;
    }
    ////////////////////////////////////////////////////////
    public static HashSet<Object> countBoundsSet = new HashSet<Object>();

    public static void addCountBound(String entity, String parent, int lower, int upper) {
        countBoundsSet.add(new countBounds(entity, parent, lower, upper));
    }

    public static boolean parentCountSatisfied(countBounds cb, String XMLfile) {
        NodeList nl = pruneOps.getPruneDoc().getElementsByTagName(cb.parent);
        for (int ii = 0; ii < nl.getLength(); ii++) {
            Element par = (Element) nl.item(ii);
            int count = getNumberOf(cb.entity, par, XMLfile);
            if (count <= cb.upper && count >= cb.lower) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    public static void removeAllNotPresents(String XMLFile) {
    	HashSet<Object> es = new HashSet<Object>();
        NodeList nl = pruneOps.getPruneDoc().getElementsByTagName("NotPresent");
        for (int i = 0; i < nl.getLength(); i++) {
            Element notPres = (Element) nl.item(i);
            Element presence = (Element) notPres.getParentNode();
            Element par = (Element) presence.getParentNode();
            es.add(par);
        }
        for (Object o : es) {
            Element par = (Element) o;
            Element gpar = (Element) par.getParentNode();
            gpar.removeChild(par);
        }
    }

    public static long mappedNewXMLInstWFilter(
            String projectNm,
            String sesfile, String pesfile) {
        ContentsWork pe = new ContentsWork(projectNm);
        String rootName = pe.doNoRestructuringWork(sesfile, pesfile);
        long timeStamp = System.currentTimeMillis();
        String XMLfile = pe.folderXml + rootName + timeStamp + "Inst.xml";
        String SchemaFile = pe.folderXml + rootName + "Schema.xsd";

        addMultCop(
                "PointsOfInterest", "PointOfInterest", "point",
                projectNm, sesfile, pesfile,
                XMLfile,
                SchemaFile);
        generatePruningsWInher.writePruneSchemaDoc(XMLfile, SchemaFile);
        return timeStamp;
    }

    public static void addOneCop(
            long timeStamp, int i,
            String pruneEnt, String moduleType,
            String multiAsp, String multComp, String mult,
            String projectNm, String sesfile, String pesfile) {

        ContentsWork pe = new ContentsWork(projectNm);
        sesRelationExtend rses = new sesRelationExtend(pe.folderSes, sesfile);
        String rootName = rses.getRootEntityName();
        String XMLfile = pe.folderXml + rootName + timeStamp + "Inst.xml";
        String SchemaFile = pe.folderXml + rootName + "Schema.xsd";
        pruneOps.restorePruneDoc(XMLfile);
        if (multiAsp.equals("")) {
            return;
        }
        Element multc = pruneOps.getPruneElement(multComp);
        if (multc == null) {
            return;
        }
        Element multAspc = pruneOps.getPruneElement(multiAsp + "-" + mult + "MultiAsp");
        if (multAspc == null) {
            return;
        }
        if (i == 0) {
            multAspc.removeChild(multc);
        }
        generatePruningsWInher.writePruneSchemaDoc(XMLfile, SchemaFile);

        Document curr = makeOneCop(timeStamp, pruneEnt, moduleType, projectNm, sesfile, pesfile, multiAsp, multComp);

        Element root = curr.getDocumentElement();
        pruneOps.restorePruneDoc(XMLfile);
        Element newNode = (Element) pruneOps.pruneDoc.importNode(root, true);

        Element multAsp = pruneOps.getPruneElement(multiAsp + "-" + mult + "MultiAsp");

        multAsp.appendChild(newNode);
        newNode.setAttribute("ID", i + "");
        ContentsWork.removeAllPathNPruneName(XMLfile);
        generatePruningsWInher.writePruneSchemaDoc(XMLfile, SchemaFile);
    }

    public static Document makeOneCop(
            long timeStamp,
            String pruneEnt, String moduleType,
            String ProjectNm, String sesfile, String pesfile,
            String multiAsp, String multent) {

        ContentsWork pe = new ContentsWork(ProjectNm);


        sesRelationExtend rses = new sesRelationExtend(pe.folderSes, sesfile);
        String rootName = rses.getRootEntityName();
        String XMLfile = pe.folderXml + rootName + timeStamp + "Inst.xml";

        pruneOps.restorePruneDoc(XMLfile);

        com.ms4systems.devs.core.util.pruningTable pruningTable =
                pe.getPruneInfoNL(pesfile, rses);
        pruningTable.selectEntityFromSpec(pruneEnt, moduleType, multent);
        pe.pruneWithNoTransform(rses, pruningTable);


        sesRelationExtend cop = rses.makeCopyExtend();

        sesRelation multentCop = cop.substructure(multent);
        sesRelationExtend multExtend = new sesRelationExtend(multentCop);

        ContentsWork.addCommonVariables(multExtend, pe.folderSes + sesfile);
        ContentsWork.addCommonVariables(multExtend, pe.folderPes + pesfile);

        pe.pruneWithNoTransform(multExtend, pruningTable);
        pe.doPostProcessing(pesfile);
        return pruneOps.getPruneDoc();
    }

    public static void createNewXMLInstAndMap(
            String projectNm, String multComp, String sesfile, String pesfile) {
        ContentsWork.createNewXMLInst(projectNm,
                sesfile, pesfile);
        NodeList nl = pruneOps.getPruneDoc().getElementsByTagName(multComp);
        long timeStamp = ContentsWork.mappedNewXMLInstWFilter(
                projectNm,
                sesfile, pesfile);
        for (int ii = 0; ii < nl.getLength(); ii++) {
            Element SatMod = (Element) nl.item(ii);
            boolean SensorPresent = ContentsWork.entityPresentUnderElement(
                    "Sensor", SatMod);
            boolean InterComPresent = ContentsWork.entityPresentUnderElement(
                    "CommInterSatelliteLink", SatMod);
            boolean UpDownPresent = ContentsWork.entityPresentUnderElement(
                    "CommUpNDownlink", SatMod);
            String satType = "RelaySat";
            if (SensorPresent && InterComPresent) {
                System.out.println("ImageSat");
                satType = "ImageSat";
            }
            if (!SensorPresent && InterComPresent && UpDownPresent) {
                System.out.println("RelaySat");
                satType = "RelaySat";
            }
            if (satType.equals("")) {
                continue;
            }
            ContentsWork.addOneCop(
                    timeStamp, ii,
                    satType, "moduleType",
                    "SatelliteModules", "SatelliteModule", "mult",
                    "F6Project", "JimExpFrameSeS.txt", "JimExpFramePrune.txt");
        }
    }

    public static void addCopiesToAllMultAsps(
            String projectNm, String sesfile, String pesfile,
            String XMLfile, String SchemaFile) {
        ContentsWork pe = new ContentsWork(projectNm);
        sesRelationExtend ses = new sesRelationExtend(pe.folderSes, sesfile);
        Hashtable<Object,HashSet<Object>> multrel = ses.getRelation("multiAspectHasEntity");
        Enumeration<Object> e = multrel.keys();
        while(e.hasMoreElements()){
        	Object key = e.nextElement();
        	HashSet<Object> val = multrel.get(key);
        	Iterator<Object> it = val.iterator();
        	while(it.hasNext()){
        		Object value = it.next();
        		String multiAsp = (String) key;
        		String multComp = (String) value;
        		int numSegs = 0;
        		int[] multint = new ContentsWork(projectNm).getMultiplicityForNode(pesfile, multComp);
                if (multint != null && multint.length > 0) {
                    numSegs = multint[0];
                }
                LinkedList<Object> copyRecord = new LinkedList<Object>();
                LinkedList<Object> multoccurAsp = pruneOps.getElementOccurrences(multiAsp);
                for (int ii = 0; ii < multoccurAsp.size(); ii++) {
                	LinkedList<Object> copies = makeMultCops(projectNm, sesfile, pesfile, multiAsp, multComp, numSegs);
                    copyRecord.add(copies);
                }
                pruneOps.restorePruneDoc(XMLfile);
                LinkedList<Object> occursOfMultiAsp = pruneOps.getElementOccurrences(multiAsp);
                int numOccurs = 0;
                for (Object oo : occursOfMultiAsp) {
                    Element multAspc = (Element) oo;

                    if (multAspc == null) {
                        return;
                    }
                    if (!multiAsp.equals("")) {
                    	LinkedList<Object> chds = pruneOps.getActualChildren(multAspc);
                        Element multc = (Element) chds.getFirst();
                        if (multc == null) {
                            return;
                        }
                        multAspc.removeChild(multc);

                        multAspc.setAttribute("numContainedIn" + multiAsp, "" + numSegs);

                        int i = 0;
                        LinkedList<Object> q = (LinkedList<Object>) copyRecord.get(numOccurs);
                        for (Object o : q) {
                            Document curr = (Document) o;
                            Element root = curr.getDocumentElement();
                            Element newNode = (Element) pruneOps.pruneDoc.importNode(root, true);
                            multAspc.appendChild(newNode);
                            newNode.setAttribute("ID", i + "");
                            i++;
                        }
                    }
                    numOccurs++;
                }
                generatePruningsWInher.writePruneSchemaDoc(XMLfile, SchemaFile);
        	}
        	
        }
    }

      public static void addCopiesToAllMultAsps(
            String projectNm, String sesfile, String pesfile,
            String XMLfile, String SchemaFile,boolean prunelater) {
        ContentsWork pe = new ContentsWork(projectNm);
        sesRelationExtend ses = new sesRelationExtend(pe.folderSes, sesfile);
        Hashtable<Object,HashSet<Object>> multrel = ses.getRelation("multiAspectHasEntity");
        Enumeration<Object> e = multrel.keys();
        while(e.hasMoreElements()){
        	Object key = e.nextElement();
        	HashSet<Object> valSet = multrel.get(key);
        	Iterator<Object> it = valSet.iterator();
        	while(it.hasNext()){
        		int numSegs = 0;
                
                String multiAsp = (String)key;
                String multComp = (String)it.next();
                int[] multint = new ContentsWork(projectNm).getMultiplicityForNode(pesfile, multComp);
                if (multint != null && multint.length > 0) {
                    numSegs = multint[0];
                }
                LinkedList<Object> copyRecord = new LinkedList<Object>();
                LinkedList<Object> multoccurAsp = pruneOps.getElementOccurrences(multiAsp);
                for (int ii = 0; ii < multoccurAsp.size(); ii++) {
                	LinkedList<Object> copies = makeMultCops(projectNm, sesfile, pesfile, multiAsp, multComp, numSegs,prunelater);
                    copyRecord.add(copies);
                }
                pruneOps.restorePruneDoc(XMLfile);
                LinkedList<Object> occursOfMultiAsp = pruneOps.getElementOccurrences(multiAsp);
                int numOccurs = 0;
                for (Object oo : occursOfMultiAsp) {
                    Element multAspc = (Element) oo;

                    if (multAspc == null) {
                        return;
                    }
                    if (!multiAsp.equals("")) {
                    	LinkedList<Object> chds = pruneOps.getActualChildren(multAspc);
                        Element multc = (Element) chds.getFirst();
                        if (multc == null) {
                            return;
                        }
                        multAspc.removeChild(multc);

                        multAspc.setAttribute("numContainedIn" + multiAsp, "" + numSegs);

                        int i = 0;
                        LinkedList<Object> q = (LinkedList<Object>) copyRecord.get(numOccurs);
                        for (Object o : q) {
                            Document curr = (Document) o;
                            Element root = curr.getDocumentElement();
                            Element newNode = (Element) pruneOps.pruneDoc.importNode(root, true);
                            multAspc.appendChild(newNode);
                            newNode.setAttribute("ID", i + "");
                            i++;
                        }
                    }
                    numOccurs++;
                }
                generatePruningsWInher.writePruneSchemaDoc(XMLfile, SchemaFile);
        	}
        }
        
       
    }
      // Partition
    public static Hashtable<Object,Hashtable<Object,Object>> makePartition(
            HashSet<Object> components, HashSet<Object> stripIds, String XMLFile, String multEnt) {
    	Hashtable<Object,Hashtable<Object,Object>> res = new Hashtable<Object,Hashtable<Object,Object>>();
        pruneOps.restorePruneDoc(XMLFile);
        NodeList nl = pruneOps.getPruneDoc().getElementsByTagName(multEnt);
        for (int i = 0; i < nl.getLength(); i++) {
            Element SatMod = (Element) nl.item(i);
            for (Object o : components) {
                String comp = o.toString();
                boolean ComponentPresent = ContentsWork.entityPresentUnderElement(
                        comp, SatMod);
                if (ComponentPresent) {
                    for (Object oo : stripIds) {
                        String stripId = oo.toString();
                        if (comp.startsWith(stripId)) {
                            comp = stripId;
                        }
                    }
                    String key = "ID" + SatMod.getAttribute("ID");
                    if(res.containsKey(key)){
                    	Hashtable<Object,Object> valHT = res.get(key);
                    	if(valHT.containsKey(comp)){
                    		Integer value = (Integer)valHT.get(comp);
                    		valHT.put(comp, new Integer(value.intValue()+1));
                    	}else{
                    		valHT.put(comp, new Integer(1));
                    	}
                    }else{
                    	Hashtable<Object,Object> valHT = new Hashtable<Object,Object>();
                    	valHT.put(comp, new Integer(1));
                    	res.put(key, valHT);
                    }
                    //res.add("ID" + SatMod.getAttribute("ID"), comp);
                }
            }
        }
        return res;
    }

//////////////////////////////////////////////////

    public static class countBounds {

        public String entity, parent;
        public int lower, upper;

        public countBounds(String entity, String parent, int lower, int upper) {
            this.entity = entity;
            this.parent = parent;
            this.lower = lower;
            this.upper = upper;

        }
    }

    public static class collectPartitions {

        protected HashSet<Object> partitionsFound, components, commonPrefixes;
        protected String multEnt;


        public collectPartitions(
                String folderSes, String sesfile, String multEnt, HashSet<Object> commonPrefixes) {
            partitionsFound = new HashSet<Object>();
            sesRelationExtend ses = new sesRelationExtend(folderSes, sesfile);
            components = getAllPresenceParentsInSubstructure(ses, multEnt);
            if (commonPrefixes.isEmpty()) {
                commonPrefixes = commonPrefixes(components);
            }
            this.commonPrefixes = commonPrefixes;
            this.multEnt = multEnt;
        }

        public boolean checkAndAddNewPartition(String XMLFile) {
            if (XMLFile.equals("")) {
                return false;
            }
            Hashtable<Object,Hashtable<Object,Object>> newContents =
                    makePartition(components, commonPrefixes, XMLFile, multEnt);

            Hashtable<Object,Hashtable<Object,Object>> newone = checkAddablity(newContents, partitionsFound);
            if (newone != null) {
                partitionsFound.add(newone);
                System.out.println("Number of Satellite distributions so far : " + partitionsFound.size());
                System.out.println("Satellite distributions so far : " + partitionsFound);
                return true;
            }
            return false;
        }
        // Modified by CS 4/24/12 for a Partition class
        public Hashtable<Object,Hashtable<Object,Object>> checkAddablity(Hashtable<Object,Hashtable<Object,Object>> newContents, HashSet<Object> partitionSet){
        	boolean isContain = false;
        	for(Object o : partitionSet){
        		Hashtable<Object,Hashtable<Object,Object>> partition = (Hashtable<Object,Hashtable<Object,Object>>) o;
        		if(isEqualPartitions(partition,newContents)){
        			isContain = true;
        			break;
        		}
        	}
        	if(isContain){
        		return null;
        	}else {
        		return newContents;
        	}
        	
        }
        public boolean isEqualPartitions(Hashtable<Object,Hashtable<Object,Object>> pt, Hashtable<Object,Hashtable<Object,Object>> pt2){
        	boolean equal = true;
        	if(pt.size() == pt2.size()){
        		Enumeration<Object> e = pt.keys();
        		while(e.hasMoreElements()){
        			Object key = e.nextElement();
        			if(pt2.containsKey(key)){
        				if(!isEqual(pt.get(key), pt2.get(key))){
        					equal = false;
        					break;
        				}
        			}else{
        				equal = false;
        				break;
        			}
        		}
        	}else {
        		equal = false;
        	}
        	return equal;
        }
        public boolean isEqual(Hashtable<Object,Object> ht, Hashtable<Object,Object> ht2){
        	boolean equal = true;
        	if(ht.size() == ht2.size()){
        		Enumeration<Object> e = ht.keys();
        		while(e.hasMoreElements()){
        			Object key = e.nextElement();
        			if(ht2.containsKey(key)){
        				Integer val = (Integer)ht.get(key);
        				Integer val2= (Integer)ht2.get(key);
        				if(val.intValue()!= val2.intValue()){
        					equal = false;
        					break;
        				}
        			}else {
        				equal = false;
        				break;
        			}
        		}
        	}else {
        		equal = false;
        	}
        	return equal;
        }
    }
}
