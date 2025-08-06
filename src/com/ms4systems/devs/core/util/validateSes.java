package com.ms4systems.devs.core.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.w3c.dom.*;
import java.util.*;
import java.io.*;
import java.util.regex.Pattern;

public class validateSes extends SESOps {

    public static String pathSep = ">";

    public static boolean onPath(String path, String item) {
        Pattern p = Pattern.compile(pathSep);
        String[] items = p.split(path);
        Set<Object> itemSet = stringOps.toEnsembleSet(items);
        return itemSet.contains(item);
    }

    public static String getDocumentValidate(String xmlFileString) {
        Document document = null;
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl",null);
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(new File(xmlFileString));
            return "valid";
        } catch (SAXException sxe) {
            // Error generated during parsing)
            Exception x = sxe;
            if (sxe.getException() != null) {
                x = sxe.getException();
            }
            return x.toString();

        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();

        } catch (IOException ioe) {
            // I/O error
            ioe.printStackTrace();
        }
        return "valid";
    }

    static public void restoreSesDoc(String xmlFile) {
        String result = getDocumentValidate(xmlFile);
        if (result.equals("valid")) {
            SESOps.restoreSesDoc(xmlFile);
        } else {
            int colon = result.indexOf(":");
            result = result.substring(colon + 1, result.length());
            System.out.println("Ses Document is not well-formed, reason is: \n" +
                               result);
        }
    }

    public static void printTree() {
        if (!validate()) {
            System.out.println("Ses Document violates SES axioms");
            System.exit(3);
        }
        SESOps.printTree();
    }

    public static void writeDTDToXML(String myFileName) {
        fileHandler.writeToFile(myFileName + ".dtd", writeDTDDoc());
    }

    public static String writeDTDDoc() {
        if (!validate()) {
            System.out.println("Ses Document violates SES axioms");
            System.exit(3);
        }
        declared = new HashSet();
        return writeRoot(sesDoc.getDocumentElement());
    }

//return children w/o checking for empty names

    public static LinkedList<Object> getElementChildrenOfElement(Element namedElem,
            String type) {
        LinkedList<Object> es = new LinkedList<Object>();
        NodeList nlc = namedElem.getChildNodes(); //for entity: aspect,spec,multiAsp,var,#text
        for (int j = 0; j < nlc.getLength(); j++) {
            Node nc = nlc.item(j);
            if (nc.getNodeName().equals(type)
                    ) {
                es.add((Element) nc);
            }
        }
        return es;
    }

    public static LinkedList<Object> getChildrenOf(String name, String thisType, String type) {
        return getChildrenOfElement(getElement(thisType, name), type);
    }

    public static HashSet<Object> queueToEnsembleSet(LinkedList<Object> elements) {
        HashSet<Object> s = new HashSet<Object>();
        Iterator it = elements.iterator();
        while (it.hasNext()) {
            Element el = (Element) it.next();
            s.add(el.getAttribute("name"));
        }
        return s;
    }

    public static LinkedList<Object> allElementsWithName(Element namedElem,
                                            String type) {
        LinkedList<Object> es = new LinkedList<Object>();
        NodeList nlc = sesDoc.getElementsByTagName(type);
        for (int j = 0; j < nlc.getLength(); j++) {
            Node nc = nlc.item(j);
            if (((Element) nc).getAttribute("name").equals(namedElem.
                    getAttribute("name"))
                    ) {
                es.add(nc);
            }
        }
        return es;
    }

    public static boolean checkAllEmpty(LinkedList<Object> els, String type) {
        Iterator it = els.iterator();
        while (it.hasNext()) {
            Element el = (Element) it.next();
            LinkedList<Object> q = getElementChildrenOfElement(el, type);
            if (!q.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkValidBrothers(LinkedList<Object> els) {
        return els.size() == queueToEnsembleSet(els).size();
    }

    public static boolean checkOneDefn(LinkedList<Object> els) {
        boolean foundDef = false;
        Iterator it = els.iterator();
        while (it.hasNext()) {
            Element el = (Element) it.next();
            LinkedList<Object> q = getElementChildrenOfElement(el, "entity");
            if (q.size() == 0) {
                continue;
            } else
            if (!foundDef) {
                foundDef = true;
            } else {
                return false;
            }
        }
        return true;
    }

    public static boolean checkOneDefnForEntity(LinkedList<Object> ents) {
        boolean foundDef = false;
        Iterator it = ents.iterator();
        while (it.hasNext()) {
            Element el = (Element) it.next();
            LinkedList<Object> qs = getElementChildrenOfElement(el, "specialization");
            LinkedList<Object> qa = getElementChildrenOfElement(el, "aspect");
            LinkedList<Object> qma = getElementChildrenOfElement(el, "multiAspect");

            if (qs.size() + qa.size() + qma.size() == 0) {
                continue;
            } else
            if (!foundDef) {
                foundDef = true;
            } else {
                return false;
            }
        }
        return true;
    }

    public static boolean checkVarsDefn(Element se) {
        LinkedList<Object> vars = getElementChildrenOfElement(se, "var");
        Iterator it = vars.iterator();
        while (it.hasNext()) {
            Element var = (Element) it.next();
            if (var.getAttribute("name").equals("")) {
                System.out.println("a variable of  " +
                                   se.getAttribute("name") +
                                   " is undefined");
                return false;
            }
            if (!checkValidBrothers(vars)) {
                System.out.println("valid brothers axiom of " +
                                   se.getAttribute("name") +
                                   " is not satisfied");
                return false;
            }

        }
        return true;
    }

    public static boolean validateEntity(Element se) {
        if (!checkVarsDefn(se)) {
            return false;
        }
        if (!se.getNodeName().equals("entity")) {
            System.out.println(se.getNodeName() + " with name " +
                               se.getAttribute("name") +
                               " should be entity");
            return false;
        }

        if (se.getAttribute("name").equals("")) {
            System.out.println("entity's name is not defined ");
            return false;
        }

        LinkedList<Object> entities = getElementChildrenOfElement(se, "entity");
        if (!entities.isEmpty()) {
            System.out.println(se.getNodeName() + " with name " +
                               se.getAttribute("name") +
                               " must not have entity children");
            return false;
        }

        LinkedList<Object> aspects = getElementChildrenOfElement(se, "aspect");
        aspects.addAll(getElementChildrenOfElement(se, "multiAspect"));
        if (!checkValidBrothers(aspects)) {
            System.out.println("valid brothers axiom of " +
                               se.getAttribute("name") +
                               " is not satisfied");
            return false;
        }

        Iterator it = aspects.iterator();
        while (it.hasNext()) {
            Element asp = (Element) it.next();
            if (!validateAsp(asp, se)) {
                return false;
            }
        }

        LinkedList<Object> specs = getElementChildrenOfElement(se,
                                                  "specialization");
        if (!checkValidBrothers(specs)) {
            System.out.println("valid brothers axiom of " +
                               se.getAttribute("name") +
                               " is not satisfied");
            return false;
        }

        it = specs.iterator();
        while (it.hasNext()) {
            if (!validateSpec((Element) it.next(), se)) {
                return false;
            }
        }
        String name = se.getAttribute("name");
        LinkedList<Object> entsWithSameName = allElementsWithName(se, "entity");
        if (!checkOneDefnForEntity(entsWithSameName)) {
            System.out.println(se.getAttribute("name") +
                               " has multiple definitions: violates uniformity ");

            return false;
        }
        return true;
    }

    public static boolean validateSpec(Element sp, Element sem) {
        if (sp.getAttribute("name").equals("")) {
            System.out.println(sem.getNodeName() + " with name " +
                               sem.getAttribute("name") +
                               " has a specialization whose name is not defined ");
            return false;
        }
        LinkedList<Object> aspects = getElementChildrenOfElement(sp, "aspect");
        aspects.addAll(getElementChildrenOfElement(sp, "multiAspect"));
        if (!aspects.isEmpty()) {
            System.out.println(sp.getNodeName() + " with name " +
                               sp.getAttribute("name") +
                               " must not have aspect children");
            return false;
        }
        LinkedList<Object> specs = getElementChildrenOfElement(sp, "specialization");
        if (!specs.isEmpty()) {
            System.out.println(sp.getNodeName() + " with name " +
                               sp.getAttribute("name") +
                               " must not have specialization children");
            return false;
        }

        LinkedList<Object> spsWithSameName = allElementsWithName(sp, "specialization");
 
        if (checkAllEmpty(spsWithSameName, "entity")) {
            System.out.println(sp.getNodeName() + " with name " +
                               sp.getAttribute("name") +
                               " must  have entity children");
            return false;
        }

        if (!checkOneDefn(spsWithSameName)) {
            System.out.println(sp.getNodeName() + " with name " +
                               sp.getAttribute("name") +
                               " has multiple definitions: violates uniformity");
            return false;
        }

        LinkedList<Object> entities = getElementChildrenOfElement(sp, "entity");
        if (entities.size() == 0) {
            System.out.println("specialization " +
                               sp.getAttribute("name") + " has no children");
            return false;
        }

        if (!checkValidBrothers(entities)) {
            System.out.println("valid brothers axiom of " +
                               sp.getAttribute("name") +
                               " is not satisfied");
            return false;
        }

        Iterator is = entities.iterator();
        while (is.hasNext()) {
            Element sen = (Element) is.next();
            if (!validateEntity(sen)) {
                System.out.println("under specialization: " +
                                   sp.getAttribute("name"));
                return false;
            }
        }
        return true;
    }

    public static boolean validateAsp(Element asp, Element sem) {
        if (asp.getAttribute("name").equals("")) {
            System.out.println(sem.getNodeName() + " with name " +
                               sem.getAttribute("name") +
                               " has an aspect or multiaspect whose name is not defined ");
            return false;
        }
        LinkedList<Object> specs = getElementChildrenOfElement(asp, "specialization");
        if (!specs.isEmpty()) {
            System.out.println(asp.getNodeName() + " with name " +
                               asp.getAttribute("name") +
                               " must not have specialization children");
            return false;
        }
        LinkedList<Object> aspects = getElementChildrenOfElement(asp, "aspect");
        aspects.addAll(getElementChildrenOfElement(asp, "multiAspect"));
        if (!aspects.isEmpty()) {
            System.out.println(asp.getNodeName() + " with name " +
                               asp.getAttribute("name") +
                               " must not have aspect children");
            return false;
        }
        LinkedList<Object> aspsWithSameName = allElementsWithName(asp, "aspect");
        aspsWithSameName.addAll(allElementsWithName(asp, "multiAspect"));
        if (checkAllEmpty(aspsWithSameName, "entity")) {
            System.out.println(asp.getNodeName() + " with name " +
                               asp.getAttribute("name") +
                               " must  have entity children");
            return false;
        }
        if (!checkOneDefn(aspsWithSameName)) {
            System.out.println(asp.getNodeName() + " with name " +
                               asp.getAttribute("name") +
                               " has multiple definitions: violates uniformity");
            return false;
        }

        LinkedList<Object> entities = getElementChildrenOfElement(asp, "entity");
        if (!checkValidBrothers(entities)) {
            System.out.println("valid brothers axiom of " +
                               asp.getAttribute("name") +
                               " is not satisfied");
            return false;
        }

        Iterator is = entities.iterator();
        if (entities.size() == 0) {
            System.out.println("aspect " +
                               asp.getAttribute("name") + " has no children");
            return false;
        }

        while (is.hasNext()) {
            Element sen = (Element) is.next();
            if (!validateEntity(sen)) {
                System.out.println("under aspect or multiAspect: " +
                                   asp.getAttribute("name"));
                return false;
            }
        }

        if (multiAspectNames.contains(asp.getAttribute("name"))) {
            LinkedList<Object> multiaspects = getElementChildrenOfElement(sem, "multiAspect");
            Iterator it = multiaspects.iterator();
            while (it.hasNext()) {
                Element mult = (Element) it.next();

                LinkedList<Object> mcs = getElementChildrenOfElement(mult, "entity");
                Iterator mit = mcs.iterator();
                Element se = (Element) mit.next();
                String cop = mult.getAttribute("coupling");
                if (cop.equals("")) {
                    System.out.println("Warning: multiAspect: " +
                                       asp.getAttribute("name") +
                                       " coupling undefined");
                }
                mcs = getElementChildrenOfElement(mult, "numberComponentsVar");
                mit = mcs.iterator();
                Element ncv = (Element) mit.next();
                String nm = ncv.getAttribute("name");
                if (nm.equals("")) {
                    System.out.println("multiAspect: " +
                                       asp.getAttribute("name") +
                                       " numberComponentsVar's name undefined");
                    return false;
                }
                String min = ncv.getAttribute("min");
                int Min = 0;
                if (min.equals("")) {
                    System.out.println("Warning: multiAspect: " +
                                       asp.getAttribute("name") +
                                       " min undefined");
                } else {

                    Min = Integer.parseInt(min);
                    if (Min < 0) {
                        System.out.println("Warning: multiAspect: " +
                                           asp.getAttribute("name") +
                                           " min is negative");
                    }

                }

                String max = ncv.getAttribute("max");
                int Max = 0;
                if (max.equals("")) {
                    System.out.println("Warning: multiAspect: " +
                                       asp.getAttribute("name") +
                                       " max undefined");
                } else {

                    Max = Integer.parseInt(max);
                    if (Max < 0 || Max < Min) {
                        System.out.println("Warning: multiAspect: " +
                                           asp.getAttribute("name") +
                                           " max is negative or less than min");
                    }
                }

                if (!validateEntity(se)) {
                    System.out.println("under aspect  " + asp.getNodeName());
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean validate() {
        if (SESOps.sesRoot == null) {
            return false;
        }
        if (!checkStrictHier()) {
            return false;
        }

        return validateEntity(SESOps.sesRoot);
    }

    public static String getNmFrom(String path) {
        String nM = path;
        int lastDot = path.lastIndexOf(pathSep);
        if (lastDot > -1) {
            nM = path.substring(lastDot + 1, path.length());
        }
        return nM;
    }

    public static boolean checkStrictHier() {
        boolean result = checkHierEntity(sesRoot.getAttribute("name"));
        if (result) {
            System.out.println("Strict Hierarchy Axiom is satisfied");
            return true;
        }
        return false;
    }

    public static boolean checkHierEntity(String entityPath) {

        boolean aspHier = true;
        boolean multAspHier = true;
        boolean specHier = true;

        String entity = getNmFrom(entityPath);
        LinkedList<Object> aspects = getChildrenOf(entity, "entity", "aspect");
        Iterator ia = aspects.iterator();
        while (ia.hasNext()) {
            String aspNm = (String) ia.next();
            if (onPath(entityPath, aspNm)) {
                System.out.println("repeat aspect found on path: " + entityPath +
                                   pathSep + aspNm);
                return false;
            }
            aspHier = checkHierAspect(entityPath + pathSep + aspNm);
        }

        LinkedList<Object> multiAspects = getChildrenOf(entity, "entity", "multiAspect");
        ia = multiAspects.iterator();
        while (ia.hasNext()) {
            String aspNm = (String) ia.next();
            if (onPath(entityPath, aspNm)) {
                System.out.println("repeat multAspect found on path: " +
                                   entityPath + pathSep + aspNm);
                return false;
            }
            multAspHier = checkHierMultiAspect(entityPath + pathSep + aspNm);
        }
        LinkedList<Object> specs = getChildrenOf(entity, "entity", "specialization");
        Iterator is = specs.iterator();
        while (is.hasNext()) {
            String specNm = (String) is.next();
            if (onPath(entityPath, specNm)) {
                System.out.println("repeat spec found on path: " +
                                   entityPath + pathSep + specNm);
                return false;
            }

            specHier = checkHierSpec(entityPath + pathSep + specNm);
        }
        return aspHier && multAspHier && specHier;
    }

    public static boolean checkHierAspect(String aspectPath) {
        String aspect = getNmFrom(aspectPath);
        LinkedList<Object> entities = getChildrenOf(aspect, "aspect", "entity");
        Iterator ie = entities.iterator();
        boolean result = true;
        while (ie.hasNext()) {
            String entNm = (String) ie.next();
            if (onPath(aspectPath, entNm)) {
                System.out.println("repeat entity found on path: " +
                                   aspectPath + pathSep + entNm);
                return false;
            }
            result = result && checkHierEntity(aspectPath + pathSep + entNm);
        }
        return result;
    }


    public static boolean checkHierMultiAspect(String aspectPath) {
        String aspect = getNmFrom(aspectPath);
        LinkedList<Object> entities = getChildrenOf(aspect, "multiAspect", "entity");
        Iterator ie = entities.iterator();
        boolean result = true;
        while (ie.hasNext()) {
            String entNm = (String) ie.next();
            if (onPath(aspectPath, entNm)) {
                System.out.println("repeat entity found on path: " +
                                   aspectPath + pathSep + entNm);
                return false;
            }
            result = result && checkHierEntity(aspectPath + pathSep + entNm);
        }
        return result;
    }


    public static boolean checkHierSpec(String specPath) {
        String spec = getNmFrom(specPath);
        LinkedList<Object> entities = getChildrenOf(spec, "specialization", "entity");
        Iterator ie = entities.iterator();
        boolean result = true;
        while (ie.hasNext()) {
            String entNm = (String) ie.next();
            if (onPath(specPath, entNm)) {
                System.out.println("repeat entity found on path: " +
                                   specPath + pathSep + entNm);
                return false;
            }
            result = result && checkHierEntity(specPath + pathSep + entNm);
        }
        return result;
    }

    public static boolean checkSingleAspOrSpec() {
        Element topEnt = (Element) getElementChildrenOfElement(sesRoot,
                "entity").getFirst();
        return checkSingleAspOrSpec(topEnt);
    }

    public static boolean checkSingleAspOrSpec(Element se) {

        LinkedList<Object> aspects = getElementChildrenOfElement(se, "aspect");
        aspects.addAll(getElementChildrenOfElement(se, "multiAspect"));
        LinkedList<Object> specs = getElementChildrenOfElement(se,
                                                  "specialization");
        if (aspects.size() + specs.size() > 1) {
            System.out.println("entity has more than one aspect/spec child: " +
                               se.getAttribute("name"));
            return false;
        }
        if (aspects.size() > 0) {
            Element asp = (Element) aspects.getFirst();
            if (asp != null) {
                LinkedList<Object> entities = getElementChildrenOfElement(asp, "entity");
                Iterator it = entities.iterator();
                while (it.hasNext()) {
                    return checkSingleAspOrSpec((Element) it.next());
                }
            }
        }
        if (specs.size() > 0) {
            Element spec = (Element) specs.getFirst();
            if (spec != null) {
                LinkedList<Object> entities = getElementChildrenOfElement(spec, "entity");
                Iterator it = entities.iterator();
                while (it.hasNext()) {
                    return checkSingleAspOrSpec((Element) it.next());
                }
            }
        }
        return true;
    }


    public static void main(String argv[]) {
        System.exit(3);
    } // main

}
