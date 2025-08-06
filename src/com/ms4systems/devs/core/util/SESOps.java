package com.ms4systems.devs.core.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.util.regex.*;
import org.w3c.dom.*;

import java.util.*;

@SuppressWarnings({"rawtypes","unchecked","unused"})
public class SESOps {

    static protected Document sesDoc;
    static protected HashSet<Object> entityNames, aspectNames, specNames, multiAspectNames, varNames, allNames, declared;
    static protected Element sesRoot;
    public static Pair topPair = new Pair();

    public static void createSesDoc() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl",null);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            sesDoc = builder.newDocument(); // Create from whole cloth
            sesRoot = (Element) sesDoc.createElement("entity");
            sesDoc.appendChild(sesRoot);
        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();
        }
    }

    public static Document getSesDoc() {
        return sesDoc;
    }

    public static Element getSesRoot() {
        return sesRoot;
    }

    public static void addSES(String fn) {
        if (sesDoc == null) {
            createSesDoc();
        }
        Document document = XMLToDom.getDocument(fn);
        NodeList nl = document.getElementsByTagName("entity");
        for (int i = 0; i < nl.getLength(); i++) {
            Element newNode = (Element) sesDoc.importNode(nl.item(i), true);
            sesRoot.appendChild(newNode);
        }
    }


    static public void restoreSesDoc(String xmlFile) {
        sesDoc = XMLToDom.getDocument(xmlFile);
        sesRoot = sesDoc.getDocumentElement();
        String rootElemName = sesRoot.getAttribute("name");
        if (rootElemName.equals("")) {
            NodeList nl = sesRoot.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i).getNodeName().equals("entity")) {
                    sesRoot = (Element) nl.item(i);
                    break;
                }
            }
        }

        allNames = new HashSet();
        entityNames = getNames("entity");
        allNames.addAll(entityNames);
        aspectNames = getNames("aspect");
        allNames.addAll(aspectNames);
        specNames = getNames("specialization");
        allNames.addAll(specNames);
        multiAspectNames = getNames("multiAspect");
        allNames.addAll(multiAspectNames);
        varNames = getNames("var");

    }

    public static HashSet getNames(String type, Document doc) {
        HashSet names = new HashSet();
        NodeList nl = doc.getElementsByTagName(type);
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            names.add(getNodeNameAttrib(n, type, "name"));
        }
        return names;
    }

    public static HashSet<Object> getNames(String type) {
        return getNames(type, sesDoc);
    }

    public static HashSet<Object> getNamesOfElements(LinkedList<Object> q, String parent) {
        HashSet<Object> names = new HashSet<Object>();
        for (int i = 0; i < q.size(); i++) {
            Element n = (Element) q.get(i);
            names.add(n.getAttribute("name") + "_" + parent);
        }
        return names;
    }

    public static String checkIntersect(HashSet<Object> es, HashSet<Object> et) {
        HashSet<Object> result = new HashSet<Object>(es);
        result.retainAll(et);
        return result.toString();
    }

    public static String hasSpace(String s) {
        if (s.indexOf(" ") > -1) {
            return s;
        }

        return "";
    }

    public static HashSet haveSpace(HashSet et) {
        HashSet res = new HashSet();
        Iterator it = et.iterator();
        while (it.hasNext()) {
            Object o = (Object) it.next();
            String s = o.toString();
            if (o instanceof String) {
                res.add(hasSpace(s));
            }
        }
        return res;
    }

    public static Element firstInstWithChildren(Document doc, String tagName) {
        Node res = null;
        NodeList nlc = doc.getElementsByTagName(tagName);
        for (int j = 0; j < nlc.getLength(); j++) {
            Node nc = nlc.item(j);
            if (!getText(nc).equals("")) {
                continue;
            }
            if (res == null) {
                res = nc;
            } else if (nc.getChildNodes().getLength() > 0) {
                res = nc;
            }
            return (Element) res;
        }
        return (Element) res;
    }

    public static Element firstInstWithChildren(Document doc, String type,
            String name) {
        Node res = null;
        NodeList nlc = doc.getElementsByTagName(type);
        for (int j = 0; j < nlc.getLength(); j++) {
            Node nc = nlc.item(j);
            if (!getText(nc).equals("")) {
                continue;
            }
            if (!getNodeNameAttrib(nc, type, "name").equals(name)) {
                continue;
            }
            if (res == null) {
                res = nc;
            } else if (getChildrenOfElement((Element) nc, type).size() > 0) {

                return (Element) nc;
            }
        }

        return (Element) res;
    }

    public static HashSet getAttributeValues(String tag, String attr, String fn) {
        Document document = XMLToDom.getDocument(fn);
        document.normalize();
        HashSet attrvals = new HashSet();
        NodeList nl = document.getElementsByTagName(tag);
        for (int i = 0; i < nl.getLength(); i++) {
            Element n = (Element) nl.item(i);
            String an = n.getAttribute(attr);
            attrvals.add(an);
        }
        return attrvals;
    }

    // go from an element node to the attribute value of one of its attributes
    public static String getNodeNameAttrib(Node n, String nodeName, String attr) {
        if (n.getNodeName().equals(nodeName)) {
            Element e = (Element) n;
            return e.getAttribute(attr);
        } else {
            return "";
        }
    }

    public static String getText(Node n) {
        if (n.getNodeName().equals("#text")) {
            return n.getNodeValue();
        } else {
            return "";
        }
    }

    public static Element getElementFrom(Element root, String tagName) {
        Node res = null;
        NodeList nlc = root.getChildNodes();
        for (int j = 0; j < nlc.getLength(); j++) {
            Node nc = nlc.item(j);
            if (nc.getNodeName().equals(tagName)) {
                return (Element) nc;
            } else {
                return getElementFrom((Element) nc, tagName);
            }
        }
        return (Element) res;
    }

    public static LinkedList<Object> allSpecsFrom(Node root) {
    	LinkedList<Object> q = new LinkedList<Object>();
        NodeList nlc = root.getChildNodes();
        for (int j = 0; j < nlc.getLength(); j++) {
            Node nc = nlc.item(j);
            if (nc.getNodeName().endsWith("Type") || nc.getNodeName().endsWith("Spec")) {
                q.add(nc);
            }
            q.addAll(allSpecsFrom(nc));
        }
        return q;
    }

    public static LinkedList<Object> getSpecEntNames(String multaspNm) {
        NodeList nl = sesDoc.getElementsByTagName("multiAspect");
        for (int i = 0; i < nl.getLength(); i++) {
            Element multasp = (Element) nl.item(i);
            if (multasp == null) {
                continue;
            }
            if (multasp.getAttribute("name").equals(multaspNm)) {
                Node parEnt = multasp.getParentNode();
                LinkedList<Object> et = getElementChildrenOfElement(multasp, "entity");
                if (!et.isEmpty()) {
                    Element ent = (Element) et.getFirst();
                    String entNm = ent.getAttribute("name");
                    LinkedList<Object> q = getElementChildrenOfElement(ent, "specialization");
                    if (q.isEmpty()) {
                        continue;
                    }
                    Element spec = (Element) q.getFirst();
                    return getChildrenOfElement(spec, "entity");
                }
            }
        }
        return new LinkedList<Object>();
    }

    public static Element getElement(String type, String name) {
        return firstInstWithChildren(sesDoc, type, name);
    }

    public static LinkedList<Object> getAllElements(String type, String name) {
    	LinkedList<Object> q = new LinkedList<Object>();
        NodeList nl = sesDoc.getElementsByTagName(type);
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (getNodeNameAttrib(n, type, "name").equals(name)) {
                q.add(n);
            }
        }
        return q;
    }

    public static Element getChildElement(Element parent, String type) {
        NodeList nlc = parent.getChildNodes(); // for entity:
        // aspect,spec,multiAsp,var,#text
        for (int j = 0; j < nlc.getLength(); j++) {
            Node nc = nlc.item(j);
            if (nc.getNodeName().equals(type)) {
                return (Element) nc;
            }
        }
        return null;
    }

    public static Element getChildElement(Element parent, String type,
            String name) {
        NodeList nlc = parent.getChildNodes(); // for entity:
        // aspect,spec,multiAsp,var,#text
        for (int j = 0; j < nlc.getLength(); j++) {
            Node nc = nlc.item(j);
            if (nc.getNodeName().equals(type) && ((Element) nc).getAttribute("name").equals(name)) {
                return (Element) nc;
            }
        }
        return null;
    }

    public static LinkedList<Object> getChildrenOf(String name, String type) {
        return getChildrenOfElement(getElement("entity", name), type);
    }

    // return names of children
    public static LinkedList<Object> getChildrenOfElement(Element namedElem, String type) {
    	LinkedList<Object> es = new LinkedList<Object>();
        NodeList nlc = namedElem.getChildNodes(); // for entity:
        // aspect,spec,multiAsp,var,#text
        for (int j = 0; j < nlc.getLength(); j++) {
            Node nc = nlc.item(j);
            if (nc.getNodeName().equals(type) && getNodeNameAttrib(nc, type, "name").length() > 0) {
                es.add(getNodeNameAttrib(nc, type, "name"));

            }
        }
        return es;
    }

    // return children
    public static LinkedList<Object> getElementChildrenOfElement(Element namedElem,
            String type) {
    	LinkedList<Object> es = new LinkedList<Object>();
        NodeList nlc = namedElem.getChildNodes(); // for entity:
        // aspect,spec,multiAsp,var,#text
        for (int j = 0; j < nlc.getLength(); j++) {
            Node nc = nlc.item(j);
            if (nc.getNodeName().equals(type) && getNodeNameAttrib(nc, type, "name").length() > 0) {
                es.add((Element) nc);
            }
        }
        return es;
    }

    public static LinkedList<Object> collectAspectsOf(Element entity) {
    	LinkedList<Object> es = getChildrenOfElement(entity, "aspect");
        es.addAll(getChildrenOfElement(entity, "multiAspect"));
        return es;
    }

    public static LinkedList<Object> getEntityChildrenOf(String type, String name) {
        return getEntityChildrenOf(getElement(type, name));
    }

    public static LinkedList<Object> getEntityChildrenOf(Element e) {
    	LinkedList<Object> es = new LinkedList<Object>();
        NodeList nlc = e.getChildNodes(); // entity,#text
        for (int j = 0; j < nlc.getLength(); j++) {
            Node nc = nlc.item(j);
            if (nc.getNodeName().equals("entity")) {
                es.add(getNodeNameAttrib(nc, "entity", "name"));
            }
        }
        return es;
    }

    public static void printNames(String type) {
        System.out.println(getNames(type));
    }

    public static void writeDTDToXML(String myFileName, Element root) {
        fileHandler.writeToFile(myFileName + ".dtd", writeRoot(root));
    }

    public static void writeDTDToXML(String myFileName) {
        fileHandler.writeToFile(myFileName + ".dtd", writeDTDDoc());
    }

    public static void writeDTDToXMLForSingleAspect(String myFileName) {
        fileHandler.writeToFile(myFileName + ".dtd",
                writeDTDDocForSingleAspect());
    }

    public static void writeDTDToXMLForSingleAspectOrSpec(String myFileName) {
        fileHandler.writeToFile(myFileName + ".dtd",
                writeDTDDocForSingleAspectOrSpec());
    }

    public static void writeSesDoc(String xmlFile) {
        fileHandler.writeToCleanFile(xmlFile, DomToXML.write(sesDoc.getDocumentElement(), sesDoc.getDocumentElement().getNodeName(), "ses.dtd"));
    }
    public static void writeSesDoc(String xmlFile,String folderFordtd) {
        fileHandler.writeToCleanFile(xmlFile, DomToXML.write(sesDoc.getDocumentElement(), sesDoc.getDocumentElement().getNodeName(), folderFordtd+"ses.dtd"));
    }
    public static boolean isDeclared(String nm) {
        return !declared.add(nm);
    }

    public static String writeDTDDoc() {
        declared = new HashSet<Object>();

        return writeRoot(sesDoc.getDocumentElement());
    }

    public static String writeDTDDocForSingleAspect() {
        declared = new HashSet();

        return writeRootForSingleAspect(sesDoc.getDocumentElement());
    }

    public static String writeDTDDocForSingleAspectOrSpec() {
        declared = new HashSet();

        return writeRootForSingleAspectOrSpec(sesDoc.getDocumentElement());
    }

    public static String writeRoot(Element root) {
        return "<?xml version='1.0' encoding='us-ascii'?> \\n" + "<!--  DTD for a " + getNodeNameAttrib(sesRoot, "entity", "name") + " -->" + " \\n" + writeEntity(sesRoot);
    }

    public static String writeRootForSingleAspect(Element root) {
        return "<?xml version='1.0' encoding='us-ascii'?> \\n" + "<!--  DTD for a " + getNodeNameAttrib(sesRoot, "entity", "name") + " -->" + " \\n" + writeEntityForSingleAspect(sesRoot);
    }

    public static String writeRootForSingleAspectOrSpec(Element root) {
        return "<?xml version='1.0' encoding='us-ascii'?> \\n" + "<!--  DTD for a " + getNodeNameAttrib(sesRoot, "entity", "name") + " -->" + " \\n" + writeEntityForSingleAspectOrSpec(sesRoot);
    }

    public static String writeEntity(Element entity) {
        if (entity == null) {
            return "";
        }
        if (isDeclared(getNodeNameAttrib(entity, "entity", "name"))) {
            return "";
        }
        String s = " \\n" + "<!ELEMENT " + getNodeNameAttrib(entity, "entity", "name") + " (";
        String el = "";
        el += writeChildrenOfEntity(entity, "specialization");
        LinkedList<Object> es = collectAspectsOf(entity);
        if (!es.isEmpty()) {
            if (el.equals("")) {
                el += "aspectsOf" + getNodeNameAttrib(entity, "entity", "name");
            } else {
                el += ",aspectsOf" + getNodeNameAttrib(entity, "entity", "name");
            }
        }
        if (el.equals("")) {
            el += " #PCDATA";
        }
        s += el + ")>";

        es = getChildrenOfElement(entity, "specialization");
        Iterator<Object> it = es.iterator();
        s += " \\n";
        while (it.hasNext()) {
            s += writeSpec((String) it.next());

        }
        es = collectAspectsOf(entity);
        if (!es.isEmpty()) {
            it = es.iterator();
            s += " \\n <!ELEMENT aspectsOf" + getNodeNameAttrib(entity, "entity", "name") + " (";
            while (it.hasNext()) {
                String aspNm = (String) it.next();
                s += aspNm + " | ";
            }
            s = s.substring(0, s.length() - 2);
            s += ")>";

        }
        es = collectAspectsOf(entity);
        it = es.iterator();
        if (!es.isEmpty()) {
            while (it.hasNext()) {
                String aspNm = (String) it.next();

                s += writeAsp(getNodeNameAttrib(entity, "entity", "name"),
                        aspNm);
            }

        }

        LinkedList<Object> et = getElementChildrenOfElement(entity, "var");

        if (et.isEmpty()) {
            return s;
        } else {
            it = et.iterator();

            s += " \\n" + "<!ATTLIST " + getNodeNameAttrib(entity, "entity", "name");

            while (it.hasNext()) {

                Element ch = (Element) it.next();

                s += " \\n" + ch.getAttribute("name") + " CDATA " + quote(ch.getAttribute("rangeSpec")) + " \\n";
            }
            s += ">";
        }
        return s;
    }

    public static String writeEntityForSingleAspect(Element entity) {
        if (entity == null) {
            return "";
        }
        if (isDeclared(getNodeNameAttrib(entity, "entity", "name"))) {
            return "";
        }
        String aspName = "";
        LinkedList<Object> eq = collectAspectsOf(entity);
        Iterator iq = eq.iterator();
        if (!eq.isEmpty()) {
            while (iq.hasNext()) {
                aspName = (String) iq.next();
                break;
            }
        }

        String s = " \\n" + "<!ELEMENT " + getNodeNameAttrib(entity, "entity", "name") + " (";
        String el = "";
        el += writeChildrenOfEntity(entity, "specialization");
        LinkedList<Object> es = collectAspectsOf(entity);
        if (!es.isEmpty()) {
            if (el.equals("")) {
                el += aspName;
            } else {
                el += "," + aspName;
            }
        }

        if (!el.equals("")) {
            s += el + ")>";
        }

        es = getChildrenOfElement(entity, "specialization");
        Iterator it = es.iterator();
        if (!es.isEmpty()) {
            s += " \\n";
            while (it.hasNext()) {
                s += " \\n" + writeSpecForSingleAspect((String) it.next());
            }
        }

        es = collectAspectsOf(entity);
        Iterator is = es.iterator();
        if (!es.isEmpty()) {
            s += " \\n";
            while (is.hasNext()) {
                String aspNm = (String) is.next();

                s += writeAspForSingleAspect(getNodeNameAttrib(entity,
                        "entity", "name"), aspNm);
            }
        }
        if (el.equals("")) {
            el += " #PCDATA";
            s += el + ")>";
        }

        LinkedList<Object> et = getChildrenOfElement(entity, "var");

        if (et.isEmpty()) {
            return s;
        } else {
            s += " \\n" + " \\n" + "<!ATTLIST " + getNodeNameAttrib(entity, "entity", "name");

            Iterator itq = et.iterator();
            s += " \\n";
            while (itq.hasNext()) {
                s += (String) itq.next() + " CDATA #REQUIRED \\n";
            }
            s += ">";
        }
        return s;
    }

    public static String writeEntityForSingleAspectOrSpec(Element entity) {
        if (entity == null) {
            return "";
        }
        if (isDeclared(getNodeNameAttrib(entity, "entity", "name"))) {
            return "";
        }
        String aspName = "", el = "", s = "";
        LinkedList<Object> es = collectAspectsOf(entity);
        Iterator is = es.iterator();
        if (!es.isEmpty()) {
            el += " \\n";
            while (is.hasNext()) {
                aspName = (String) is.next();

                el += writeAspForSingleAspectOrSpec(getNodeNameAttrib(entity,
                        "entity", "name"), aspName);
            }
        }

        s += el;
        el = "";

        LinkedList<Object> ec = getChildrenOfElement(entity, "specialization");
        Iterator it = ec.iterator();
        if (!ec.isEmpty()) {

            while (it.hasNext()) {
                el += " \\n" + writeSpecForSingleAspectOrSpec((String) it.next(),
                        getNodeNameAttrib(entity, "entity", "name"));
            }
        }
        s += el;

        if (s.equals("")) {
            s += " \\n" + "<!ELEMENT " + getNodeNameAttrib(entity, "entity", "name") + " (#PCDATA)>";
        }

        LinkedList<Object> et = getChildrenOfElement(entity, "var");

        if (et.isEmpty()) {
            return s;
        } else {
            s += " \\n" + " \\n" + "<!ATTLIST " + getNodeNameAttrib(entity, "entity", "name");

            Iterator itq = et.iterator();
            s += " \\n";
            while (itq.hasNext()) {
                s += (String) itq.next() + " CDATA #REQUIRED \\n";
            }
            s += ">";
        }
        return s;
    }

    public static String writeChildrenOfEntity(Element entity, String type) {
        if (entity == null) {
            return "";
        }
        LinkedList<Object> es = getChildrenOfElement(entity, type);
        if (es.isEmpty()) {
            return "";
        }
        String s = "";
        Iterator it = es.iterator();
        while (it.hasNext()) {
            s += (String) it.next() + ",";
        }
        s = s.substring(0, s.length() - 1);
        return s;
    }

    public static String writeSpec(String specNm) {
        if (specNm == null) {
            return " ";
        }
        if (isDeclared(specNm)) {
            return " ";
        }
        String s = "<!ELEMENT " + specNm + " (";
        LinkedList<Object> es = getEntityChildrenOf(getElement("specialization", specNm));
        Iterator it = es.iterator();
        while (it.hasNext()) {
            s += (String) it.next() + " | ";
        }
        s = s.substring(0, s.length() - 2);
        s += ")>";

        it = es.iterator();
        while (it.hasNext()) {
            s += writeEntity(getElement("entity", (String) it.next()));
        }
        return s;
    }

    public static String writeSpecForSingleAspect(String specNm) {
        if (specNm == null) {
            return " ";
        }
        if (isDeclared(specNm)) {
            return " ";
        }
        String s = "<!ELEMENT " + specNm + " (";
        LinkedList<Object> es = getEntityChildrenOf(getElement("specialization", specNm));
        Iterator it = es.iterator();
        while (it.hasNext()) {
            s += (String) it.next() + " | ";
        }
        s = s.substring(0, s.length() - 2);
        s += ")>";

        it = es.iterator();
        while (it.hasNext()) {
            s += writeEntityForSingleAspect(getElement("entity", (String) it.next()));
        }
        return s;
    }

    public static String writeSpecForSingleAspectOrSpec(String specNm,
            String parEntNm) {
        if (specNm == null) {
            return " ";
        }
        if (isDeclared(specNm)) {
            return " ";
        }
        String s = "<!ELEMENT " + parEntNm + " (";
        LinkedList<Object> es = getEntityChildrenOf(getElement("specialization", specNm));
        Iterator it = es.iterator();
        while (it.hasNext()) {
            s += (String) it.next() + " | ";
        }
        s = s.substring(0, s.length() - 2);
        s += ")>";

        it = es.iterator();
        while (it.hasNext()) {
            s += writeEntityForSingleAspectOrSpec(getElement("entity",
                    (String) it.next()));
        }

        return s;
    }

    public static String writeAsp(String parEntNm, String aspNm) {

        if (aspNm == null) {
            return "";
        }
        if (isDeclared(aspNm)) {
            return "";
        }
        String s = "";
        if (aspectNames.contains(aspNm)) {

            Element asp = getElement("aspect", aspNm);

            if (asp != null) {
                boolean attlist = false;
                String cop = asp.getAttribute("coupling");
                if (!cop.equals("")) {
                    attlist = true;
                    s += " \\n" + "<!ATTLIST " + aspNm;
                    s += " \\n" + cop + " CDATA #IMPLIED \\n";
                }
                LinkedList<Object> mcs = getChildrenOfElement(asp, "numberComponentsVar");
                if (!mcs.isEmpty()) {
                    if (!attlist) {
                        s += " \\n" + "<!ATTLIST " + aspNm;
                        attlist = true;
                    }
                    Iterator mit = mcs.iterator();
                    Element el = getElement("numberComponentsVar", (String) mit.next());
                    String ncvar = getNodeNameAttrib(el, "numberComponentsVar",
                            "name");
                    s += " \\n" + ncvar + " CDATA #IMPLIED \\n";
                }
                if (attlist) {
                    s += ">";
                }
                LinkedList<Object> es = getEntityChildrenOf(asp);
                s += " \\n<!ELEMENT " + aspNm + " ( ";
                Iterator it = es.iterator();
                while (it.hasNext()) {
                    s += (String) it.next() + " , ";

                }
                s = s.substring(0, s.length() - 2);
                s += ")>";

                es = getEntityChildrenOf(asp);
                it = es.iterator();
                while (it.hasNext()) {
                    s += writeEntity(getElement("entity", (String) it.next()));
                }
            }
        } else if (multiAspectNames.contains(aspNm)) {
            Element mult = getElement("multiAspect", aspNm);

            Queue<Object> mcs = getChildrenOfElement(mult, "entity");
            if (!mcs.isEmpty()) {
                Iterator mit = mcs.iterator();
                String entNm = getNodeNameAttrib(getElement("entity", (String) mit.next()), "entity", "name");
                if (!entNm.equals("")) {
                    s += " \\n<!ELEMENT " + aspNm + " (" + entNm + "*)>";
                    s += writeEntity(getElement("entity", entNm));

                    boolean attlist = false;

                    String cop = mult.getAttribute("coupling");
                    if (!cop.equals("")) {
                        attlist = true;
                        s += " \\n" + "<!ATTLIST " + aspNm;
                        s += " \\n" + cop + " CDATA #IMPLIED \\n";
                    }

                    mcs = getChildrenOfElement(mult, "numberComponentsVar");
                    if (mcs.isEmpty()) {
                        return s;
                    }
                    mit = mcs.iterator();
                    Element nv = getElement("numberComponentsVar", (String) mit.next());
                    String ncvar = getNodeNameAttrib(nv, "numberComponentsVar",
                            "name");
                    if (!attlist) {
                        s += " \\n" + "<!ATTLIST " + aspNm;
                        attlist = true;
                    }
                    s += " \\n" + ncvar + " CDATA #IMPLIED \\n";

                    String num = mult.getAttribute("number");
                    if (!num.equals("")) {
                        s += " \\n" + num + " CDATA #IMPLIED \\n";
                    }
                    if (attlist) {
                        s += ">";
                    }
                }
            }

        }
        return s;
    }

    public static String writeAspForSingleAspect(String parEntNm, String aspNm) {

        if (aspNm == null) {
            return "";
        }
        if (isDeclared(aspNm)) {
            return "";
        }
        String s = "";
        if (aspectNames.contains(aspNm)) {

            Element asp = getElement("aspect", aspNm);

            if (asp != null) {
                boolean attlist = false;
                String cop = asp.getAttribute("coupling");
                if (!cop.equals("")) {
                    attlist = true;
                    s += " \\n" + "<!ATTLIST " + aspNm;
                    s += " \\n" + cop + " CDATA #IMPLIED \\n";
                }
                LinkedList<Object> mcs = getChildrenOfElement(asp, "numberComponentsVar");
                if (!mcs.isEmpty()) {
                    if (!attlist) {
                        s += " \\n" + "<!ATTLIST " + aspNm;
                        attlist = true;
                    }
                    Iterator mit = mcs.iterator();
                    Element el = getElement("numberComponentsVar", (String) mit.next());
                    String ncvar = getNodeNameAttrib(el, "numberComponentsVar",
                            "name");
                    s += " \\n" + ncvar + " CDATA #IMPLIED \\n";
                }
                if (attlist) {
                    s += ">";
                }
                LinkedList<Object> es = getEntityChildrenOf(asp);
                s += " \\n<!ELEMENT " + aspNm + " ( ";
                Iterator it = es.iterator();
                while (it.hasNext()) {
                    s += (String) it.next() + " , ";

                }
                s = s.substring(0, s.length() - 2);
                s += ")>";

                es = getEntityChildrenOf(asp);
                it = es.iterator();
                while (it.hasNext()) {
                    s += writeEntityForSingleAspect(getElement("entity",
                            (String) it.next()));
                }
            }
        } else if (multiAspectNames.contains(aspNm)) {
            Element mult = getElement("multiAspect", aspNm);

            LinkedList<Object> mcs = getChildrenOfElement(mult, "entity");
            Iterator mit = mcs.iterator();
            String entNm = getNodeNameAttrib(getElement("entity", (String) mit.next()), "entity", "name");
            if (!entNm.equals("")) {
                s += " \\n<!ELEMENT " + aspNm + " (" + entNm + "*)>";
                s += writeEntityForSingleAspect(getElement("entity", entNm));

                boolean attlist = false;

                String cop = mult.getAttribute("coupling");
                if (!cop.equals("")) {
                    attlist = true;
                    s += " \\n" + "<!ATTLIST " + aspNm;
                    s += " \\n" + cop + " CDATA #IMPLIED \\n";
                }

                mcs = getChildrenOfElement(mult, "numberComponentsVar");
                mit = mcs.iterator();
                String ncvar = getNodeNameAttrib(getElement(
                        "numberComponentsVar", (String) mit.next()),
                        "numberComponentsVar", "name");
                if (!attlist) {
                    s += " \\n" + "<!ATTLIST " + aspNm;
                    attlist = true;
                }
                s += " \\n" + ncvar + " CDATA #IMPLIED \\n";

                String num = mult.getAttribute("number");
                if (!num.equals("")) {
                    s += " \\n" + num + " CDATA #IMPLIED \\n";
                }
                if (attlist) {
                    s += ">";
                }
            }

        }
        return s;
    }

    public static String writeAspForSingleAspectOrSpec(String parEntNm,
            String aspNm) {

        if (aspNm == null) {
            return "";
        }
        if (isDeclared(aspNm)) {
            return "";
        }
        String s = "";
        if (aspectNames.contains(aspNm)) {

            Element asp = getElement("aspect", aspNm);

            if (asp != null) {
                boolean attlist = false;
                String cop = asp.getAttribute("coupling");
                if (!cop.equals("")) {
                    attlist = true;
                    s += " \\n" + "<!ATTLIST " + aspNm;
                    s += " \\n" + cop + " CDATA #IMPLIED \\n";
                }
                Queue<Object> mcs = getChildrenOfElement(asp, "numberComponentsVar");
                if (!mcs.isEmpty()) {
                    if (!attlist) {
                        s += " \\n" + "<!ATTLIST " + aspNm;
                        attlist = true;
                    }
                    Iterator mit = mcs.iterator();
                    Element el = getElement("numberComponentsVar", (String) mit.next());
                    String ncvar = getNodeNameAttrib(el, "numberComponentsVar",
                            "name");
                    s += " \\n" + ncvar + " CDATA #IMPLIED \\n";
                }
                if (attlist) {
                    s += ">";
                }
                LinkedList<Object> es = getEntityChildrenOf(asp);
                s += " \\n<!ELEMENT " + parEntNm + " ( ";
                Iterator it = es.iterator();
                while (it.hasNext()) {
                    s += (String) it.next() + " , ";

                }
                s = s.substring(0, s.length() - 2);
                s += ")>";

                es = getEntityChildrenOf(asp);
                it = es.iterator();
                while (it.hasNext()) {
                    s += writeEntityForSingleAspectOrSpec(getElement("entity",
                            (String) it.next()));
                }
            }
        } else if (multiAspectNames.contains(aspNm)) {
            Element mult = getElement("multiAspect", aspNm);

            LinkedList<Object> mcs = getChildrenOfElement(mult, "entity");
            Iterator mit = mcs.iterator();
            String entNm = getNodeNameAttrib(getElement("entity", (String) mit.next()), "entity", "name");
            if (!entNm.equals("")) {
                s += " \\n<!ELEMENT " + parEntNm + " (" + entNm + "*)>";
                s += writeEntityForSingleAspectOrSpec(getElement("entity",
                        entNm));

                boolean attlist = false;

                String cop = mult.getAttribute("coupling");
                if (!cop.equals("")) {
                    attlist = true;
                    s += " \\n" + "<!ATTLIST " + aspNm;
                    s += " \\n" + cop + " CDATA #IMPLIED \\n";
                }

                mcs = getChildrenOfElement(mult, "numberComponentsVar");
                mit = mcs.iterator();
                String ncvar = getNodeNameAttrib(getElement(
                        "numberComponentsVar", (String) mit.next()),
                        "numberComponentsVar", "name");
                if (!attlist) {
                    s += " \\n" + "<!ATTLIST " + aspNm;
                    attlist = true;
                }
                s += " \\n" + ncvar + " CDATA #IMPLIED \\n";

                String num = mult.getAttribute("number");
                if (!num.equals("")) {
                    s += " \\n" + num + " CDATA #IMPLIED \\n";
                }
                if (attlist) {
                    s += ">";
                }
            }

        }
        return s;
    }

    public static String quote(String s) {
        String ss = " " + s + " ";
        char[] ca = ss.toCharArray();
        ca[0] = ca[ca.length - 1] = '"';
        String r = "";
        for (int i = 0; i < ca.length; i++) {
            r += ca[i];
        }
        return r;
    }

    public static String writeAttributes(Element e) {
        String s = "";
        NamedNodeMap m = e.getAttributes();

        if (m == null) {
            return "";
        }
        for (int i = 0; i < m.getLength(); i++) {
            Attr n = (Attr) m.item(i);
            s += n.getName() + " = " + quote(n.getNodeValue()) + " ";
        }
        return s;
    }

    public static String writeTextChildren(Node n) {
        if (n == null) {
            return " ";
        }
        String s = " ";
        NodeList nl = n.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node nc = nl.item(i);
            if (!nc.hasChildNodes()) {
                if (nc.getNodeName().equals("#text")) {
                    s += " " + nc.getNodeValue();
                }
            }
        }
        return s;
    }

    public static void createTree() {
        topPair = new Pair("entity: " + getNodeNameAttrib(sesRoot, "entity", "name"), new LinkedList<Object>()); // (name,
        createEntitySubTree(sesRoot, topPair);
    }

    public static void printTree() {
        createTree();
        LinkedList<Object> q = hierTree.StringHier(topPair, (int) Double.POSITIVE_INFINITY);
        hierTree.printQueue(q);
    }

    public static String printTreeString() {
        createTree();
        LinkedList<Object> q = hierTree.StringHier(topPair, (int) Double.POSITIVE_INFINITY);
        return hierTree.QueueToString(q);
    }
// CS.
    public static void printSubTree(String entNm, int maxLevel) {
        Pair p = new Pair("entity: " + entNm, new LinkedList<Object>());
        createEntitySubTree(getElement("entity", entNm), p);
        LinkedList<Object> q = hierTree.StringHier(p, maxLevel);
    }

    public static void extractSesFromEntity(String entName, String folder) {
    	LinkedList<Object> q = SESOps.getAllElements("entity", entName);
        if (q.isEmpty()) {
            return;
        }
        Node n = ((Node) q.getFirst()).cloneNode(true);
        fileHandler.writeToCleanFile(folder + "sesFor" + ((Element) n).getAttribute("name") + ".xml", DomToXML.write(
                n, n.getNodeName(), "ses.dtd"));
    }

    public static void extractSesFromChildren(String entName, String folder) {
        Element entity = getElement("entity", entName);
        extractSesFromChildren(entity, folder);
    }

    public static void extractSesFromChildren(Element entity, String folder) {
    	LinkedList<Object> q = SESOps.getElementChildrenOfElement(entity, "aspect");
        if (q.isEmpty()) {
            return;
        }
        Element asp = (Element) q.peek();
        q = SESOps.getChildrenOfElement(asp, "entity");
        Iterator it = q.iterator();
        while (it.hasNext()) {
            extractSesFromEntity((String) it.next(), folder);
        }
    }

    public static void extractSesFromFirstEntities(String folder) {
        extractSesFromChildren(sesRoot, folder);
    }

    public static void extractSesFromAllEntitiesWSuffix(String suffix,
            String folder) {
        Iterator it = entityNames.iterator();
        while (it.hasNext()) {
            String entNm = (String) it.next();
            if (entNm.toLowerCase().endsWith(suffix.toLowerCase())) {
                extractSesFromEntity(entNm, folder);
            }
        }
    }

    public static void extractSesFromAllEntitiesWPrefix(String prefix,
            String folder) {
        Iterator it = entityNames.iterator();
        while (it.hasNext()) {
            String entNm = (String) it.next();
            if (entNm.toLowerCase().startsWith(prefix.toLowerCase())) {
                extractSesFromEntity(entNm, folder);
            }
        }
    }

    public static void printDocStatsOfChildren(Element entity, String folder) {
    	LinkedList<Object> q = SESOps.getElementChildrenOfElement(entity, "aspect");
        if (q.isEmpty()) {
            return;
        }
        Element ent = (Element) q.peek();
        q = SESOps.getChildrenOfElement(ent, "entity");
        Iterator it = q.iterator();
        while (it.hasNext()) {
            String entNm = (String) it.next();
            restoreSesDoc(folder + "sesFor" + entNm + ".xml");
            XMLToDom.printDocStats(sesDoc);
        }
    }

    public static void printDocStatsOfFirstEntities(String folder) {
        printDocStatsOfChildren(sesRoot, folder);
    }

    public static void printSesTextsOfChildren(Element entity, String folder) {
    	LinkedList<Object> q = SESOps.getElementChildrenOfElement(entity, "aspect");
        if (q.isEmpty()) {
            return;
        }
        Element ent = (Element) q.peek();
        q = SESOps.getChildrenOfElement(ent, "entity");
        Iterator it = q.iterator();
        while (it.hasNext()) {
            String entNm = (String) it.next();
            sesRelation ses = new sesRelation(folder + "sesFor" + entNm + ".xml");
            ses.backToNatLang(folder);
        }
    }

    public static void printSesTextsOfFirstEntities(String folder) {
        printSesTextsOfChildren(sesRoot, folder);
    }

    // /////////////////////////////////////
    public static String createEntitySubString(Element entity) {
        String s = getNodeNameAttrib(entity, "entity", "name");

        Queue<Object> es = getChildrenOfElement(entity, "specialization");
        if (!es.isEmpty()) {
            Iterator it = es.iterator();
            while (it.hasNext()) {
                String spec = (String) it.next();
                s += createSpecSubString(getElement("specialization", spec));
            }
        }

        es = getChildrenOfElement(entity, "aspect");

        if (!es.isEmpty()) {
            s += "(";
            Iterator it = es.iterator();
            while (it.hasNext()) {
                String asp = (String) it.next();
                s += createAspSubString(getElement("aspect", asp));
            }
            s += ")";
        }

        return s;
    }

    public static String createSpecSubString(Element spec) {
        String s = "_";
        Queue<Object> es = getEntityChildrenOf(spec);
        Iterator it = es.iterator();
        while (it.hasNext()) {
            String entNm = (String) it.next();
            s += "_" + createEntitySubString(getElement("entity", entNm));
        }
        return s;
    }

    public static String createAspSubString(Element asp) {
        String s = "";

        Queue<Object> es = getEntityChildrenOf(asp);
        if (!es.isEmpty()) {
            Iterator it = es.iterator();
            while (it.hasNext()) {
                String entNm = (String) it.next();
                s += createEntitySubString(getElement("entity", entNm)) + ",";
            }
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    public static String createSESString() {
        String sesRootNm = getNodeNameAttrib(sesRoot, "entity", "name");
        return createEntitySubString(sesRoot);
    }

    public static Element transformMultiAspect(Element multiAsp) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl",null);
        Document transMultSesDoc = null;
        Element transMultSesRoot = null;

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            transMultSesDoc = builder.newDocument(); // Create from whole
            // cloth
            transMultSesRoot = (Element) transMultSesDoc.createElement("aspect");
            transMultSesDoc.appendChild(transMultSesRoot);
            transMultSesRoot.setAttribute("name", getNodeNameAttrib(multiAsp,
                    "multiAspect", "name") + "Dec");
            transMultSesRoot.setAttribute("coupling", getNodeNameAttrib(
                    multiAsp, "multiAspect", "coupling"));
            Element el = getChildElement(multiAsp, "numberComponentsVar");
            Element newNode = (Element) transMultSesDoc.importNode(el, true);
            transMultSesRoot.appendChild(newNode);
            aspectNames.add(getNodeNameAttrib(multiAsp, "multiAspect", "name") + "Dec");
        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();
        }
        Element ent = getChildElement(multiAsp, "entity");
        String entNm = getNodeNameAttrib(ent, "entity", "name");
        String specNm = (String) getChildrenOfElement(
                getElement("entity", entNm), "specialization").peek();
        Queue<Object> q = getChildrenOfElement(getElement("specialization", specNm),
                "entity");
        Iterator it = q.iterator();
        while (it.hasNext()) {
            String chNm = (String) it.next();
            inheritAll(ent, getElement("entity", chNm), specNm);

            Element chs = transMultSesDoc.createElement("entity");
            chs.setAttribute("name", chNm + "_" + entNm + "Comp");
            entityNames.add(chNm + "_" + entNm + "Comp");
            transMultSesRoot.appendChild(chs);
            Element multasp = transMultSesDoc.createElement("multiAspect");
            multasp.setAttribute("name", chNm + "_" + entNm + "s");
            multiAspectNames.add(chNm + "_" + entNm + "s");
            chs.appendChild(multasp);
            Element numVars = transMultSesDoc.createElement("numberComponentsVar");
            multasp.appendChild(numVars);
            numVars.setAttribute("name", "N" + chNm + "_" + entNm + "s");
            numVars.setAttribute("number", "");
            multasp.setAttribute("coupling", chNm + "_" + entNm + "s_" + getNodeNameAttrib(multiAsp, "multiAspect", "coupling"));

            Element newNode = (Element) transMultSesDoc.importNode(getElement(
                    "entity", chNm), true);
            // ///////
            entityNames.add(chNm);
            // //////
            multasp.appendChild(newNode);
        }

        allNames.addAll(entityNames);
        allNames.addAll(aspectNames);
        allNames.addAll(multiAspectNames);
        return transMultSesRoot;

    }

    public static void transformAdd(String multiAspNm) {
        transformAdd(getElement("multiAspect", multiAspNm));
    }

    public static void transformAdd(Element multiAsp) {
        Element newRoot = transformMultiAspect(multiAsp);
        Element parent = (Element) multiAsp.getParentNode();
        Element newNode = (Element) sesDoc.importNode(newRoot, true);
        parent.appendChild(newNode);

    }

    public static void copyAttributes(Element from, Element to) {
        NamedNodeMap m = from.getAttributes();

        if (m == null) {
            return;
        }
        for (int i = 0; i < m.getLength(); i++) {
            Attr n = (Attr) m.item(i);
            to.setAttribute(n.getName(), n.getNodeValue());
        }
    }

    public static void copyAspectsOf(Element from, Element to) {
    	Queue<Object> es = getChildrenOfElement(from, "aspect");
        Iterator it = es.iterator();
        while (it.hasNext()) {
            String aspNm = (String) it.next();
            Element asp = sesDoc.createElement("aspect");
            String toNm = to.getAttribute("name");
             String fromNm = from.getAttribute("name");
            asp.setAttribute("name", aspNm);
            Element aspf = getElement("aspect", aspNm);
            copyEntitiesOf(aspf, asp);
            copyCouplingOf(aspf, asp);
            to.appendChild(asp);

        }
    }

    public static void copyMultiAspectsOf(Element from, Element to) {

    	Queue<Object> es = getChildrenOfElement(from, "multiAspect");
        Iterator it = es.iterator();
        while (it.hasNext()) {
            String aspNm = (String) it.next();
            Element asp = sesDoc.createElement("multiAspect");
            asp.setAttribute("name", aspNm);
            copyVarsOf(getElement("multiAspect", aspNm), asp);
            copyEntitiesOf(getElement("multiAspect", aspNm), asp);
            copyCouplingOf(getElement("multiAspect", aspNm), asp);
            to.appendChild(asp);
        }

    }

    public static void copyCouplingOf(Element from, Element to) {
        String fromCoupling = from.getAttribute("coupling");
        String toCoupling = to.getAttribute("coupling");
        if (toCoupling.equals("unknown")) {
            to.setAttribute("coupling", fromCoupling);
        } else {
            to.setAttribute("coupling", toCoupling + fromCoupling);
        }
    }

    public static void copyEntitiesOf(Element from, Element to) {
    	Queue<Object> es = getChildrenOfElement(from, "entity");
        Iterator it = es.iterator();
        while (it.hasNext()) {
            String entNm = (String) it.next();
            Element ent = sesDoc.createElement("entity");
            ent.setAttribute("name", entNm);
            copyVarsOf(getElement("entity", entNm), ent);
            copyAspectsOf(getElement("entity", entNm), ent);
            copyMultiAspectsOf(getElement("entity", entNm), ent);
            copySpecsOf(getElement("entity", entNm), ent, "");
            to.appendChild(ent);

        }
    }

    public static void copySpecsOf(Element from, Element to, String specNm) {
    	Queue<Object> es = getChildrenOfElement(from, "specialization");
        Iterator it = es.iterator();
        while (it.hasNext()) {
            String specNmMy = (String) it.next();
            if (specNmMy.equals(specNm)) {
                continue;
            }
            Element spec = sesDoc.createElement("specialization");
            spec.setAttribute("name", specNmMy);
            copyVarsOf(getElement("specialization", specNmMy), spec);
            copyEntitiesOf(getElement("specialization", specNmMy), spec);
            to.appendChild(spec);

        }
    }

    public static void copyVarsOf(Element entity, Element specEnt) {

        NodeList nlc = entity.getChildNodes(); // for entity:
        // aspect,spec,multiAsp,var,#text
        for (int j = 0; j < nlc.getLength(); j++) {
            Node nc = nlc.item(j);
            if (nc.getNodeName().equals("var")) {
                Element newNode = sesDoc.createElement("var");
                copyAttributes((Element) nc, newNode);
                specEnt.appendChild(newNode);
            }
        }
    }

    public static void inheritAll(Element entity, Element specEnt, String specNm) {
        copySpecsOf(entity, specEnt, specNm);
        copyAspectsOf(entity, specEnt);
        copyMultiAspectsOf(entity, specEnt);
        copyVarsOf(entity, specEnt);
    }

    // ////////////////////////////////////////
    // ////////////////////////////////////////
    public static void createEntitySubTree(Element entity, Pair node) {
        NodeList nlc = entity.getChildNodes(); // for entity:
        // aspect,spec,multiAsp,var,#text

        for (int j = 0; j < nlc.getLength(); j++) {
            Node nc = nlc.item(j);
            if (nc.getNodeName().equals("var") && getNodeNameAttrib(nc, "var", "name").length() > 0) {
                String info = getNodeNameAttrib(nc, "var", "name");
                info += ",rangeSpec: " + getNodeNameAttrib(nc, "var", "rangeSpec");
                Pair chd = hierTree.addChild(node, "var: " + info);
            }
        }

        LinkedList<Object> es = getElementChildrenOfElement(entity, "specialization");
        if (!es.isEmpty()) {
            Iterator it = es.iterator();
            while (it.hasNext()) {
                Element spec = (Element) it.next();
                String specnm = spec.getAttribute("name");
                Pair chd = hierTree.addChild(node, "specialization: " + specnm);
                createSpecSubTree(spec, chd);
            }
        }
        es = getElementChildrenOfElement(entity, "aspect");
        if (!es.isEmpty()) {
            Iterator it = es.iterator();
            while (it.hasNext()) {
                Element asp = (Element) it.next();
                String aspnm = asp.getAttribute("name");
                Pair chd = hierTree.addChild(node, "aspect: " + aspnm);
                createAspSubTree(asp, chd);
            }

        }

        es = getElementChildrenOfElement(entity, "multiAspect");
        if (!es.isEmpty()) {
            Iterator it = es.iterator();
            while (it.hasNext()) {
                Element multiasp = (Element) it.next();
                String aspnm = multiasp.getAttribute("name");
                Pair chd = hierTree.addChild(node, "multiAspect: " + aspnm);
                createMultiAspSubTree(multiasp, chd);
            }

        }
    }

    
	public static void createSpecSubTree(Element spec, Pair node) {
    	LinkedList<Object> es = getElementChildrenOfElement(spec, "entity");
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Element ent = (Element) it.next();
            String entNm = ent.getAttribute("name");
            Pair chd = hierTree.addChild(node, "entity: " + entNm);
            createEntitySubTree(ent, chd);
        }
    }

    public static void createAspSubTree(Element asp, Pair node) {
        String info = asp.getAttribute("coupling");
        if (!info.equals("")) {
            hierTree.addChild(node, "coupling: " + info);
        }

        LinkedList<Object> es = getElementChildrenOfElement(asp, "entity");
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Element ent = (Element) it.next();
            String entNm = ent.getAttribute("name");
            Pair chd = hierTree.addChild(node, "entity: " + entNm);
            createEntitySubTree(ent, chd);
        }
    }

    // bpz Dec 2005
    public static void createMultiAspSubTree(Element multasp, Pair node) {
        String info = multasp.getAttribute("coupling");
        if (!info.equals("")) {
            hierTree.addChild(node, "coupling: " + info);
        }

        LinkedList<Object> es = getElementChildrenOfElement(multasp, "numberComponentsVar");
        if (!es.isEmpty()) {
            Element ncv = (Element) es.peek();
            String ncvar = ncv.getAttribute("name");
            String s = "numComponentsVar: " + ncvar + ",";
            String min = ncv.getAttribute("min");
            s += " min: " + min + ",";
            String max = ncv.getAttribute("max");
            s += " max: " + max + ",";
            hierTree.addChild(node, s);
        }
        es = getElementChildrenOfElement(multasp, "entity");
        if (!es.isEmpty()) {
            Element ent = (Element) es.peek();
            String entNm = ent.getAttribute("name");
            Pair chd = hierTree.addChild(node, "entity: " + entNm);
            createEntitySubTree(ent, chd);
        }
    }

    // //////////////////////
    public static LinkedList<Object> expandAllMultiAsp() {
    	LinkedList<Object> specs = new LinkedList<Object>();
        NodeList nl = sesDoc.getElementsByTagName("multiAspect");
        for (int i = 0; i < nl.getLength(); i++) {
            Element multasp = (Element) nl.item(i);
            if (multasp == null) {
                continue;
            }
            specs.addAll(getSpecEntNames(multasp.getAttribute("name")));
        }

        for (int i = 0; i < nl.getLength(); i++) {
            Element multasp = (Element) nl.item(i);
            if (multasp == null) {
                continue;
            }
            expandMultiAsp(multasp);
        }
        return specs;
    }

    public static void expandMultiAsp(String multaspNm) {

        NodeList nl = sesDoc.getElementsByTagName("multiAspect");
        for (int i = 0; i < nl.getLength(); i++) {

            Element multasp = (Element) nl.item(i);

            if (multasp == null) {
                continue;
            }
            if (multasp.getAttribute("name").equals(multaspNm)) {
                expandMultiAsp(multasp);
            }
        }
    }

    public static void expandMultiAsp(Element multasp) {
        Node parEnt = multasp.getParentNode();
        LinkedList<Object> et = getElementChildrenOfElement(multasp, "entity");
        if (!et.isEmpty()) {
            Element ent = (Element) et.peek();
            String entNm = ent.getAttribute("name");
            LinkedList<Object> es = getElementChildrenOfElement(ent, "specialization");
            Iterator it = es.iterator();
            if (it.hasNext()) {
                Element spec = (Element) it.next();
                ent.removeChild(spec);
                String specNmMy = spec.getAttribute("name");
                Element asp = sesDoc.createElement("aspect");
                asp.setAttribute("name", specNmMy + "Dec");
                aspectNames.add(specNmMy + "Dec");
                parEnt.removeChild(multasp);
                parEnt.appendChild(asp);
                expandEntitiesOf(ent, spec, asp);
            }
        }
    }

    public static void expandEntitiesOf(Element parent, Element from,
            Element asp) {

    	LinkedList<Object> es = getChildrenOfElement(from, "entity");
        Iterator it = es.iterator();
        while (it.hasNext()) {
            String entNm = (String) it.next();
            Element ents = sesDoc.createElement("entity");
            ents.setAttribute("name", entNm + "s");
            asp.appendChild(ents);
            Element multasp = sesDoc.createElement("multiAspect");
            ents.appendChild(multasp);
            multasp.setAttribute("name", entNm + "multAsp");
            multiAspectNames.add(entNm + "multAsp");
            Element numcom = sesDoc.createElement("numberComponentsVar");
            numcom.setAttribute("name", "num" + entNm + "s");
            numcom.setAttribute("min", "0");
            numcom.setAttribute("max", "10");
            multasp.appendChild(numcom);

            Element ent = sesDoc.createElement("entity");
            multasp.appendChild(ent);
            ent.setAttribute("name", entNm);
            NodeList nl = parent.getChildNodes();
            boolean first = true;
            for (int i = 0; i < nl.getLength(); i++) {
                Node nc = nl.item(i);
                Node n;
                if (first) {
                    n = nc.cloneNode(true);
                } else {
                    n = nc.cloneNode(false);
                }
                ent.appendChild(n);
            }
        }
    }

    public static int getMultiAspectVolume() {
        int total = 0;
        NodeList nl = SESOps.getSesDoc().getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node nc = nl.item(i);

            if (nc instanceof Element) {
                Element ch = (Element) nc;
                total += XMLToDom.getVolume(ch, "multiAspec");
            }
        }
        return total;
    }
////////

    public static Element crossProduct(Element spec1, Element spec2) {
        String firstNm = spec1.getAttribute("name");
        String secondNm = spec2.getAttribute("name");
        String crossNm = firstNm + "_" + secondNm;
        Element cross = sesDoc.createElement("specialization");
        cross.setAttribute("name", crossNm);
        LinkedList<Object> es = getElementChildrenOfElement(spec1, "entity");
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Element ent = (Element) it.next();
            String entNm = ent.getAttribute("name");
            LinkedList<Object> ess = getElementChildrenOfElement(spec2, "entity");
            Iterator its = ess.iterator();
            while (its.hasNext()) {
                Element ents = (Element) its.next();
                String entNms = ents.getAttribute("name");
                String crossEntNm = entNm + "_" + entNms;
                Element crossent = sesDoc.createElement("entity");
                crossent.setAttribute("name", crossEntNm);
                cross.appendChild(crossent);

                copyVarsOf(ent, crossent);
                copyAspectsOf(ent, crossent);
                copyMultiAspectsOf(ent, crossent);
                copySpecsOf(ent, crossent, "");

                copyVarsOf(ents, crossent);
                copyAspectsOf(ents, crossent);
                copyMultiAspectsOf(ents, crossent);
                copySpecsOf(ents, crossent, "");
            }
        }
        return cross;
    }

    public static void crossProduct(Element parent) {
        if (parent == null) {
            return;
        }
        Element cross = null;
        LinkedList<Object> q = getElementChildrenOfElement(parent, "specialization");
        if (q.size() < 2) {
            return;
        }
        int qsize = q.size();
        Element first = (Element) q.poll();
        Element second = (Element) q.poll();
        cross = crossProduct(first, second);
        for (int i = 2; i < qsize; i++) {
            cross = crossProduct(cross, (Element) q.poll());
            String crossName = cross.getAttribute("name");
        }
        q = getElementChildrenOfElement(parent, "specialization");
        Iterator it = q.iterator();
        while (it.hasNext()) {
            Element chd = (Element) it.next();
            parent.removeChild(chd);
        }
        parent.appendChild(cross);
    }

    public static void doAllCrossProduct() {
        if (sesDoc == null) {
            return;
        }
        NodeList nl = sesDoc.getElementsByTagName("entity");
        for (int i = 0; i < nl.getLength(); i++) {
            Element ent = (Element) nl.item(i);
            String entName = ent.getAttribute("name");
            crossProduct(ent);
        }
    }

    public static int selectRandomFrom(int universesize, double rnum) {
        return (int) Math.ceil(rnum * universesize);
    }

    public static void removeAllExcept(String elname, Element spec) {
        LinkedList<Object> q = SESOps.getElementChildrenOfElement(spec, "entity");
        int qsize = q.size();
        for (int i = 0; i < qsize; i++) {
            Element ch = (Element) q.poll();
            if (!ch.getAttribute("name").equals(elname)) {
                spec.removeChild(ch);
            }
        }
    }

    public static void pruneAllSpecs(Random rand) {
        if (sesDoc == null) {
            return;
        }
        NodeList nl = sesDoc.getElementsByTagName("specialization");
        for (int i = 0; i < nl.getLength(); i++) {
            Element spec = (Element) nl.item(i);
            String specnm = spec.getAttribute("name");
            LinkedList<Object> q = SESOps.getElementChildrenOfElement(spec, "entity");
            double rnum = rand.nextDouble();
            int pick = selectRandomFrom(q.size(), rnum);
            Element el = (Element) q.get(pick - 1);
            String elname = el.getAttribute("name");
            removeAllExcept(elname, spec);
        }
    }

    public static void pruneAllSpecs() {
        if (sesDoc == null) {
            return;
        }
        NodeList nl = sesDoc.getElementsByTagName("specialization");
        for (int i = 0; i < nl.getLength(); i++) {
            Element spec = (Element) nl.item(i);
            String specnm = spec.getAttribute("name");
            LinkedList<Object> q = SESOps.getElementChildrenOfElement(spec, "entity");
            Element el = generatePruningsWInher.pickFrom(q);
            String elname = el.getAttribute("name");
            removeAllExcept(elname, spec);
        }
    }

    public static void inherit(Element parSpec, Element parent, Element chdSpec) {
    	LinkedList<Object> es = getElementChildrenOfElement(chdSpec, "entity");
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Element ent = (Element) it.next();
            String entNm = ent.getAttribute("name");

            String crossEntNm = entNm + "_" + parent.getAttribute("name");
            LinkedList<Object> ep = getChildrenOfElement(parSpec, "entity");
            if (ep.contains(crossEntNm)) {
                continue;
            }
            Element crossent = sesDoc.createElement("entity");
            crossent.setAttribute("name", crossEntNm);

            parSpec.appendChild(crossent);

            copyVarsOf(parent, crossent);
            copyAspectsOf(parent, crossent);
            copyMultiAspectsOf(parent, crossent);
            copySpecsOf(parent, crossent, chdSpec.getAttribute("name"));

            copyVarsOf(ent, crossent);
            copyAspectsOf(ent, crossent);
            copyMultiAspectsOf(ent, crossent);
            copySpecsOf(ent, crossent, "");
        }
    }
    public static int lastNumSpecs = Integer.MAX_VALUE;

    public static boolean specsLeft() {
        if (sesDoc == null) {
            return false;
        }
        if (lastNumSpecs == Integer.MAX_VALUE) {
            lastNumSpecs = Integer.MAX_VALUE - 1;
            return true;
        }
        NodeList nl = sesDoc.getElementsByTagName("specialization");
        int size = nl.getLength();
        if (size < lastNumSpecs) {
            lastNumSpecs = size;
            return true;
        } else {
            return false;
        }
    }

    public static void normalizeSpecs() {
        if (sesDoc == null) {
            return;
        }
        Hashtable<Object,HashSet<Object>> r = new Hashtable<Object,HashSet<Object>>();
        NodeList nl = sesDoc.getElementsByTagName("specialization");
        for (int i = 0; i < nl.getLength(); i++) {
            Element parSpec = (Element) nl.item(i);
            String parSpecNm = parSpec.getAttribute("name");
            if(r.containsKey(parSpecNm)){
            	r.get(parSpecNm).add(parSpec);
            }else {
            	HashSet<Object> valSet = new HashSet<Object>();
            	valSet.add(parSpec);
            	r.put(parSpecNm, valSet);
            }
        }
        Set<Object> keys = r.keySet();
        Iterator<Object> it = keys.iterator();
        while (it.hasNext()) {
            String parSpecNm = (String) it.next();
            Set<Object> parSpecs = r.get(parSpecNm);
            if (parSpecs.size() > 1) {
                int count = 0;
                Iterator<Object> ik = parSpecs.iterator();
                while (ik.hasNext()) {
                    Element parSpec = (Element) ik.next();
                    parSpec.setAttribute("name", parSpecNm + count);
                    count++;
                }
            }
        }
    }

    public static void restoreSpecs() {
        if (sesDoc == null) {
            return;
        }
        NodeList nl = sesDoc.getElementsByTagName("specialization");
        for (int i = 0; i < nl.getLength(); i++) {
            Element parSpec = (Element) nl.item(i);
            String parSpecNm = parSpec.getAttribute("name");
            int end = endsWithNumber(parSpecNm);
            if (end > 0) {
                String parSpecNmOrig =
                        parSpecNm.substring(0, parSpecNm.length() - end);
                parSpec.setAttribute("name", parSpecNmOrig);
            }
        }
    }

    public static boolean repsInt(String s) {
        for (int i = 0; i < 100; i++) {
            if (s.equals("" + i)) {
                return true;
            }
        }
        return false;
    }

    public static int endsWithNumber(String s) {
        for (int i = 1; i < 5; i++) {
            String lastpart = s.substring(s.length() - i);
            if (repsInt(lastpart)) {
                return i;
            }
        }
        return 0;
    }

    public static void doExpand() {
        int count = 100;
        while (count > 0 && specsLeft()) {
            normalizeSpecs();
            doAllCrossProduct();
            doInheritAll();
            count--;
        }
        restoreSpecs();
    }

    public static LinkedList<Object> copyQueue(LinkedList<Object> q) {
    	LinkedList<Object> res = new LinkedList<Object>();
        Iterator<Object> it = q.iterator();
        while (it.hasNext()) {
            Element el = (Element) it.next();
            res.add(el);
        }
        return res;
    }

    public static void printDepthQueue(LinkedList<Object> q) {
        Iterator<Object> it = q.iterator();
        while (it.hasNext()) {
            Element el = (Element) it.next();
            System.out.println(el.getAttribute("name") + " " + XMLToDom.getDepth(el));
        }

    }

    public static LinkedList<Object> putInPlace(LinkedList<Object> q, Element myel, int mydepth) {
        if (q.isEmpty()) {
            q.add(myel);
            return q;
        }

        int place = 0;
        Iterator<Object> it = q.iterator();
        while (it.hasNext()) {
            Element el = (Element) it.next();
            int depth = XMLToDom.getDepth(el);
            if (mydepth < depth) {
                continue;
            } else {
                place++;
            }

        }
        q.add(place, myel);
        return q;
    }

    public static LinkedList<Object> sortByDepth(LinkedList<Object> q) {
    	LinkedList<Object> res = new LinkedList<Object>();//copyQueue(q); 

        Iterator<Object> it = q.iterator();
        while (it.hasNext()) {
            Element el = (Element) it.next();
            int depth = XMLToDom.getDepth(el);
            res =
                    putInPlace(res, el, depth);
        }

        return res;
    }

    public static void doInheritAll() {
        HashSet<Object> specsToRemove = new HashSet<Object>();
        HashSet<Object> specsDone = new HashSet<Object>();
        int numSpec = sesDoc.getElementsByTagName("specialization").getLength();
        while (numSpec > 0) {
        	LinkedList<Object> specs = new LinkedList<Object>();
            NodeList nl = sesDoc.getElementsByTagName("specialization");
            for (int i = 0; i <
                    nl.getLength(); i++) {
                Element parSpec = (Element) nl.item(i);
                if (!specsDone.contains(parSpec.getAttribute("name"))) {
                    specs.add(parSpec);
                }

            }
            if (specs.isEmpty()) {
                return;
            }

            LinkedList<Object> p = sortByDepth(specs);
            Element parSpec = (Element) p.getFirst();
            numSpec--;

            String parSpecNm = parSpec.getAttribute("name");
            specsDone.add(parSpecNm);
            LinkedList<Object> q = getElementChildrenOfElement(parSpec, "entity");
            Iterator it = q.iterator();
            while (it.hasNext()) {
                Element parent = (Element) it.next();
                String parentNm = parent.getAttribute("name");
                LinkedList<Object> qq = getElementChildrenOfElement(parent, "specialization");
                Iterator itt = qq.iterator();
                while (itt.hasNext()) {
                    Element chd = (Element) itt.next();
                    String chdNm = chd.getAttribute("name");
                    inherit(parSpec, parent, chd);
                    specsToRemove.add(chd);
                }

            }
        }

        NodeList nl = sesDoc.getElementsByTagName("entity");
        for (int i = 0; i <
                nl.getLength(); i++) {
            Element ent = (Element) nl.item(i);
            if (ent.equals(sesRoot)) {
                continue;
            }

            String entNm = ent.getAttribute("name");
            Node parrSpec = XMLToDom.getAncestor(ent, 1);
            if (parrSpec == null) {
                continue;
            }

            if (!entsToRemain((Element) parrSpec).contains(entNm)) {
                parrSpec.removeChild(ent);
            }

        }
        Iterator<Object> is = specsToRemove.iterator();
        while (is.hasNext()) {
            Element spec = (Element) is.next();
            Node parEnt = XMLToDom.getAncestor(spec, 1);
            if (parEnt == null) {
                continue;
            }

            parEnt.removeChild(spec);
        }

    }

    public static HashSet<Object> getParts(
            String nm) {
        HashSet<Object> hs = new HashSet<Object>();
        Pattern p = Pattern.compile("_");
        String[] groups = p.split(nm);
        for (int i = 0; i <
                groups.length; i++) {
            hs.add(groups[i]);
        }

        return hs;
    }

    public static boolean containsMatch(HashSet<Object> es, String nm) {
        HashSet<Object> nmparts = getParts(nm);
        Iterator<Object> it = es.iterator();
        while (it.hasNext()) {
            String esnm = (String) it.next();
            if (getParts(esnm).containsAll(nmparts) && esnm.length() != nm.length()) {
                return true;
            }

        }
        return false;
    }

    public static HashSet<Object> distintEntityNames() {
        HashSet<Object> h = new HashSet<Object>();
        LinkedList<Object> q = new LinkedList<Object>();
        NodeList nl = sesDoc.getElementsByTagName("entity");
        for (int i = 0; i <
                nl.getLength(); i++) {
            Element ent = (Element) nl.item(i);
            String entNm = ent.getAttribute("name");
            q.add(entNm);
        }

        q = sortByLengthString(q);
        Iterator<Object> it = q.iterator();
        while (it.hasNext()) {
            String entnm = (String) it.next();
            if (containsMatch(h, entnm)) {
                h.add(entnm);
            }

        }
        return h;
    }

    public static HashSet<Object> entsToRemain(
            Element parspec) {

    	LinkedList<Object> q = getChildrenOfElement(parspec, "entity");
        HashSet<Object> enttorem = new HashSet<Object>();

        q = sortByLengthString(q);
        Iterator<Object> it = q.iterator();
        while (it.hasNext()) {
            String entnm = (String) it.next();
            if (!containsMatch(enttorem, entnm)) {
                enttorem.add(entnm);
            }

        }
        return enttorem;
    }

    public static LinkedList<Object> sortByLengthString(LinkedList<Object> q) {
    	LinkedList<Object> res = new LinkedList<Object>();//copyQueue(q); 
        Iterator<Object> it = q.iterator();
        while (it.hasNext()) {
            String el = (String) it.next();
            res =
                    putInPlaceString(res, el, getParts(el).size());
        }

        return res;
    }

    public static LinkedList<Object> putInPlaceString(LinkedList<Object> q, String myel, int mydepth) {
        if (q.isEmpty()) {
            q.add(myel);
            return q;
        }

        int place = 0;
        Iterator it = q.iterator();
        while (it.hasNext()) {
            String el = (String) it.next();
            if (mydepth > getParts(el).size()) {
                continue;
            } else {
                place++;
            }

        }
        q.add(place, myel);
        return q;
    }

// /////////////////////////////////////
    public static void main(String argv[]) {
        String folder = "C: \\Users \\bernie \\Documparent \\NetBeansProjects \\NLPFDDEVS2\\";
        restoreSesDoc("SesForHier.xml");
        doAllCrossProduct();
        doInheritAll();
        printTree();
        System.exit(3);
    } // main
}
