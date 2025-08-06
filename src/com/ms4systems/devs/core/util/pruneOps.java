package com.ms4systems.devs.core.util;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
@SuppressWarnings({"rawtypes","unused"})
public class pruneOps extends SESOps {

    static public Document pruneDoc;
    static public Element pruneRoot;

    static public void restorePruneDoc(String xmlFile) {
        pruneDoc = XMLToDom.getDocument(xmlFile);
        pruneRoot = pruneDoc.getDocumentElement();
    }

    // returns queue of nodes
    // identical to getChildElements
    public static LinkedList<Object> getActualChildren(Element el) {
        return XMLToDom.getChildElements(el);
    }

    public static LinkedList<Object> getActualChildren(String tag, String[] chds) {
        return XMLToDom.getChildElements(pruneDoc, tag, chds);
    }

    public static LinkedList<Object> toNames(LinkedList<Object> tags) {
    	LinkedList<Object> res = new LinkedList<Object>();
        Iterator<Object> it = tags.iterator();
        while (it.hasNext()) {
            res.add(((Node) it.next()).getNodeName());
        }
        return res;
    }

    public static LinkedList<Object> getChildTagNames(String tagName) {
        return toNames(XMLToDom.getChildElements(getPruneElement(tagName)));
    }

    public static boolean isSESSpecOf(String tagNm, String entName) {
        return getChildrenOfElement(getElement("entity", entName),
                "specialization").contains(tagNm);
    }

    public static boolean isSESAspectOf(String tagNm, String entName) {
        return getChildrenOfElement(getElement("entity", entName), "aspect").contains(tagNm);
    }

    public static boolean isSESMultiAspectOf(String tagNm, String entName) {
        return getChildrenOfElement(getElement("entity", entName),
                "multiAspect").contains(tagNm);
    }

    public static boolean hasUniqueSelection(String specOraspName) {
        return getChildTagNames(specOraspName).size() == 1;
    }

    public static boolean isChoiceOf(String tagName, String specOraspName) {
        return getChildTagNames(specOraspName).contains(tagName);
    }

    public static boolean isUniqueChoiceOf(String tagNm, String specOraspName) {
        return isChoiceOf(tagNm, specOraspName) && hasUniqueSelection(specOraspName);
    }

    public static boolean isAspChoiceOf(String aspName, String entName) {
        return getChildTagNames("aspectsOf" + entName).contains(aspName);
    }

    // /////////////////////////////////////
    public static String makeEntitySubString(Element e) {

        String entNm = e.getNodeName();
        String s = entNm + attribString(e);
        LinkedList<Object> es = getActualChildren(e);
        if (!es.isEmpty()) {
            Iterator<Object> it = es.iterator();
            while (it.hasNext()) {
                Element ch = (Element) it.next();
                String chNm = ch.getNodeName();
                if (chNm.endsWith("Spec") || chNm.endsWith("Type")) {
                    s += makeSpecSubString(ch);
                } else if (chNm.startsWith("aspectsOf")) {
                	LinkedList<Object> et = getActualChildren(ch);
                    if (!et.isEmpty()) {
                        Iterator<Object> itt = et.iterator();
                        Element gch = (Element) itt.next(); // only one
                        s += "(";
                        s += makeAspSubString(gch);
                        s += ")";
                    }
                }
            }
        }
        return s;
    }

    public static String attribString(Element e) {
        String s = "[";
        s += writeAttributes(e);
        s += "]";
        return s;
    }

    public static String makeSpecSubString(Element sp) {
        String s = "_";
        LinkedList<Object> es = getActualChildren(sp);
        Iterator<Object> it = es.iterator();
        while (it.hasNext()) {
            Element ent = (Element) it.next();
            s += "_" + makeEntitySubString(ent);
        }
        return s;
    }

    public static boolean sameNames(LinkedList<Object> es) {
    	LinkedList<Object> res = new LinkedList<Object>();
        Iterator<Object> it = es.iterator();
        Element e = (Element) it.next();
        res.add(e);
        String entNm = e.getNodeName();
        while (it.hasNext()) {
            e = (Element) it.next();
            if (e.getNodeName().equals(entNm)) {
                res.add(e);
            }
        }
        return res.size() == es.size();
    }

    public static String makeAspSubString(Element asp) {
        String s = "";
        LinkedList<Object> es = getActualChildren(asp);
        if (es.size() > 1 && sameNames(es)) { // bpz Dec 05
            s += " MULTIPLE ASPECT: ";
            s += attribString(asp);
        }
        if (!es.isEmpty()) {
            if (sameNames(es)) {

                Iterator<Object> it = es.iterator();
                while (it.hasNext()) {
                    Element e = (Element) it.next();
                    s += writeAttributes(e);
                    // s += e.getNodeName();//bpz Dec 05
                    s += makeEntitySubString(e) + ",";

                }
                s = s.substring(0, s.length() - 1);
            } else { // not multiple aspect
                es = getActualChildren(asp);
                if (!es.isEmpty()) {
                    Iterator<Object> it = es.iterator();
                    while (it.hasNext()) {
                        Element el = (Element) it.next();
                        s += makeEntitySubString(el) + ",";
                    }

                    s = s.substring(0, s.length() - 1);
                }
            }
        }

        return s;
    }

    public static String makePruneString() {
        return makeEntitySubString(pruneRoot);
    }

    // //////////////////////
    public static String makeEntitySubStringForSingleAspect(Element e) {

        String entNm = e.getNodeName();
        String s = entNm + attribString(e);
        LinkedList<Object> es = getActualChildren(e);
        if (!es.isEmpty()) {
            Iterator<Object> it = es.iterator();
            while (it.hasNext()) {
                Element ch = (Element) it.next();
                String chNm = ch.getNodeName();
                if (chNm.endsWith("Spec") || chNm.endsWith("Type")) {
                    s += makeSpecSubStringForSingleAspect(ch);
                } else if (chNm.endsWith("Aspect")) {

                    s += makeAspSubStringForSingleAspect(ch);
                }
            }
        }
        return s;

    }

    public static String makeAspSubStringForSingleAspect(Element asp) {
        String s = "";
        LinkedList<Object> es = getActualChildren(asp);
        if (!es.isEmpty() && sameNames(es)) {
            s += " MULTIPLE ASPECT: ";
            s += attribString(asp);
        }
        if (!es.isEmpty()) {
            if (sameNames(es)) {
                Iterator<Object> it = es.iterator();
                while (it.hasNext()) {
                    Element e = (Element) it.next();
                    s += writeAttributes(e);
                    s += makeEntitySubStringForSingleAspect(e) + ",";

                }
                s = s.substring(0, s.length() - 1);
            } else { // not multiple aspect
                es = getActualChildren(asp);
                if (!es.isEmpty()) {
                    Iterator<Object> it = es.iterator();
                    while (it.hasNext()) {
                        Element el = (Element) it.next();
                        s += makeEntitySubStringForSingleAspect(el) + ",";
                    }

                    s = s.substring(0, s.length() - 1);
                }
            }
        }

        return s;
    }

    public static String makeSpecSubStringForSingleAspect(Element sp) {
        String s = "_";
        LinkedList<Object> es = getActualChildren(sp);
        Iterator<Object> it = es.iterator();
        while (it.hasNext()) {
            Element ent = (Element) it.next();
            s += "_" + makeEntitySubStringForSingleAspect(ent);
        }
        return s;
    }

    public static String makePruneStringForSingleAspect() {
        return makeEntitySubStringForSingleAspect(pruneRoot);
    }

    // //////////
    public static void mapPesToSes() {
        SESOps.createSesDoc();
        SESOps.sesRoot.setAttribute("name", pruneRoot.getNodeName());
        PesToSesEntity(pruneRoot, SESOps.sesRoot);
        allNames = new HashSet<Object>();
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

    public static void PesToSesEntity(Element e, Element se) {
        se.setAttribute("name", e.getNodeName());
        Element asp = null;
        LinkedList<Object> es = getActualChildren(e);
        if (!es.isEmpty()) {
            Iterator it = es.iterator();
            while (it.hasNext()) {
                Element ch = (Element) it.next();
                String chNm = ch.getNodeName();
                if (chNm.endsWith("Spec") || chNm.endsWith("Type")) {
                    PesToSeSSpec(ch, se);

                } else if (chNm.startsWith("aspectsOf")) {
                	LinkedList<Object> et = getActualChildren(ch);
                    if (!et.isEmpty()) {
                        Iterator itt = et.iterator();
                        asp = (Element) itt.next();
                    }
                }
            }
        }
        if (asp != null) {
            PesToSeSAsp(asp, se);
        }
        LinkedList<Object> pesAttrs = generatePrunings.getAttributes(e);
        Iterator it = pesAttrs.iterator();
        while (it.hasNext()) {
            String attrNm = (String) it.next();
            String val = e.getAttribute(attrNm);
            Element var = sesDoc.createElement("var");
            se.appendChild(var);
            var.setAttribute("name", attrNm);
        }

    }

    public static void PesToSeSSpec(Element sp, Element se) {
        String nm = sp.getNodeName();
        LinkedList<Object> es = getActualChildren(sp);
        if (es.isEmpty()) {
            return;
        }

        Element spe = sesDoc.createElement("specialization");
        se.appendChild(spe);
        spe.setAttribute("name", sp.getNodeName());
        LinkedList<Object> specChilds = new LinkedList<Object>();
        if (es.isEmpty()) {
            return;
        }
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Element ent = (Element) it.next();
            if (specChilds.contains(ent.getNodeName())) {
                continue;
            }
            specChilds.add(ent.getNodeName());
            Element sen = sesDoc.createElement("entity");
            sen.setAttribute("name", ent.getNodeName());
            spe.appendChild(sen);
            PesToSesEntity(ent, sen);
        }
    }

    public static void PesToSeSAsp(Element asp, Element sem) {
        String nm = asp.getNodeName();
        if (!(nm.endsWith("MA") || nm.endsWith("multiAsp"))) {
            Element sasp = sesDoc.createElement("aspect");
            // only aspects for now
            sem.appendChild(sasp);
            sasp.setAttribute("name", nm);
            LinkedList<Object> es = getActualChildren(asp);
            HashSet<Object> saspChilds = new HashSet<Object>();
            if (es.isEmpty()) {
                return;
            }
            Iterator it = es.iterator();
            while (it.hasNext()) {
                Element el = (Element) it.next();
                if (saspChilds.contains(el.getNodeName())) {
                    continue;
                }
                saspChilds.add(el.getNodeName());
                Element els = sesDoc.createElement("entity");
                sasp.appendChild(els);
                els.setAttribute("name", el.getNodeName());
                PesToSesEntity(el, els);
            }
        } else { // multiAspect
            Element sasp = sesDoc.createElement("multiAspect");
            sem.appendChild(sasp);
            sasp.setAttribute("name", nm);
            LinkedList<Object> es = getActualChildren(asp);
            if (es.isEmpty()) {
                return;
            }
            Iterator it = es.iterator();
            while (it.hasNext()) {
                Element el = (Element) it.next();
                Element els = sesDoc.createElement("entity");
                sasp.appendChild(els);
                els.setAttribute("name", el.getNodeName());
                PesToSesEntity(el, els);
            }

        }
    }

    // ///////////////////////////////////
    public static Document getPruneDoc() {
        return pruneDoc;
    }

    public static void writePruneDoc(String xmlFile, String dtdFile) {
        fileHandler.writeToCleanFile(xmlFile, DomToXML.write(pruneDoc.getDocumentElement(), pruneDoc.getDocumentElement().getNodeName(), dtdFile));
    }

    public static void writePruneSchemaDoc(String xmlFile, String schFile,
            String fromEntity) {
        Element el = getPruneElement(fromEntity);
        fileHandler.writeToCleanFile(xmlFile, DomToXML.writeSchema(el,
                fromEntity, schFile));
    }

    public static void writePruneSchemaDoc(String xmlFile, String schFile) {
        fileHandler.writeToCleanFile(xmlFile, DomToXML.writeSchema(pruneDoc.getDocumentElement(), pruneDoc.getDocumentElement().getNodeName(), schFile));
    }


    public static void createPesDoc(String rootName) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl",null);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            pruneDoc = builder.newDocument(); // Create from whole cloth
            pruneRoot = (Element) pruneDoc.createElement(rootName);
            pruneDoc.appendChild(pruneRoot);
        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();
        }
    }

    public static void createPesDoc() {
        createPesDoc(SESOps.getSesDoc().getDocumentElement().getAttribute(
                "name"));
    }

    // /////////////////
    public static HashSet<Object> getUniqueIDs(String tag) {
        return XMLToDom.getUniqueIDs(pruneDoc, tag);
    }


    public static sesRelation computeTopSes(String tag) {
        return XMLToDom.computeTopSes(pruneDoc, tag);
    }

    public static boolean isRegular() {
        return XMLToDom.isRegular(pruneDoc);
    }

    // now same as getElementOccurrence
    public static Element getPruneElement(String tagName) {
        return getElementOccurrence(tagName);
    }

    public static Element getElementOccurrence(String tag) {
        return XMLToDom.getElementOccurrence(pruneDoc, tag);
    }

    public static Element getElementOccurrence(String tag, String[] chds) {
        return XMLToDom.getElementOccurrence(pruneDoc, tag, chds);
    }

    public static LinkedList<Object> getElementOccurrences(String tag) {
        return XMLToDom.getElementOccurrences(pruneDoc, tag);
    }

    public static LinkedList<Object> getElementOccurrences(String tag, String[] chds) {
        return XMLToDom.getElementOccurrences(pruneDoc, tag, chds);
    }

    public static String getAttrVal(String tag, String attr, String[] chds) {
        return XMLToDom.getAttrVal(pruneDoc, tag, attr, chds);
    }

    public static String getAttrVal(String tag, String attr) {
        return XMLToDom.getAttrVal(pruneDoc, tag, attr);
    }

    public static LinkedList<Object> getAttrVals(String tag, String attr) {
        return XMLToDom.getAttrVals(pruneDoc, tag, attr);
    }

    public static String getAttrVal(String attr) {
        return XMLToDom.getAttrVal(pruneDoc, attr);
    }

    public static boolean setAttrVal(String tag, String attr, String val,
            String[] chds) {

        return XMLToDom.setAttrVal(pruneDoc, tag, attr, val, chds);
    }

    public static boolean setAttrVal(String tag, String attr, String val) {
        return XMLToDom.setAttrVal(pruneDoc, tag, attr, val);
    }

    public static int countNodeContains(String attr, String part) {
        return XMLToDom.countNodeContains(pruneDoc, attr, part);
    }

    // //////
    public static Element getSubClass(Element entity) {
        Element spec = null;
        LinkedList<Object> q = getActualChildren(entity);
        Iterator it = q.iterator();
        while (it.hasNext()) {
            Element ch = (Element) it.next();
            if (ch.getNodeName().endsWith("Spec") || ch.getNodeName().endsWith("Type")) {
                spec = ch;
            }
        }
        if (spec == null) {
            return entity;
        }
        LinkedList<Object> qq = getActualChildren(spec);
        if (!qq.isEmpty()) {
            return (Element) qq.getFirst();
        }
        return entity;
    }

    public static Element getIterSubClass(Element entity) {
        Element lastent = entity;
        while (true) {
            Element ent = getSubClass(lastent);
            if (ent.equals(lastent)) {
                return lastent;
            }
            lastent = ent;
        }
    }

    public static LinkedList<Object> getComponents(Element entity) {
        Element aspectsOf = null;
        LinkedList<Object> q = pruneOps.getActualChildren(entity);
        Iterator it = q.iterator();
        while (it.hasNext()) {
            Element ch = (Element) it.next();
            if (ch.getNodeName().startsWith("aspectsOf")) {
                aspectsOf = ch;
            }
            break;
        }
        if (aspectsOf == null) {
            return new LinkedList<Object>();
        }
        Element aspect = (Element) pruneOps.getActualChildren(aspectsOf).getFirst();
        return pruneOps.getActualChildren(aspect);
    }

    public static Hashtable<Object,Object> getAttrValsOfChildren(Element ent, String attr) {
    	Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        LinkedList<Object> q = getActualChildren(ent);
        Iterator it = q.iterator();
        while (it.hasNext()) {
            Element ch = (Element) it.next();
            f.put(ch.getNodeName(), ch.getAttribute(attr));
        }
        return f;
    }

    public static LinkedList<Object> getElementsContainingValue(String tagNm, String attr, String val) {
    	LinkedList<Object> q = new LinkedList<Object>();
        NodeList ln = pruneDoc.getElementsByTagName(tagNm);
        for (int i = 0; i < ln.getLength(); i++) {
            Element ent = (Element) ln.item(i);
            if (ent.getAttribute(attr).contains(val)) {
                q.add(ent);
            }
        }
        return q;
    }

    public static LinkedList<Object> getParentsOf(String tagNm, String attr, String val) {
    	LinkedList<Object> res = new LinkedList<Object>();
    	LinkedList<Object> q = getElementsContainingValue(tagNm, attr, val);
        Iterator it = q.iterator();
        while (it.hasNext()) {
            Element ent = (Element) it.next();
            Element par = (Element) ent.getParentNode();
            res.add(par);
        }
        return res;
    }

    public static void main(String argv[]) {
        restorePruneDoc("WaitDecideCMMasterInst.xml");
        mapPesToSes();
        SESOps.printTree();
        System.exit(3);
        // main
    }
}
