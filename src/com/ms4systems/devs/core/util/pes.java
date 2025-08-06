package com.ms4systems.devs.core.util;

import org.w3c.dom.*;

import java.util.*;
import java.util.regex.*;

public class pes extends ses {

    protected Document pruneDoc;
    protected Element pruneRoot;
    protected String dtdFile, id;

    public pes(String xmlFile, String id) {
        this(xmlFile);
        this.id = id;
    }

    public pes(String xmlFile) {
        this(XMLToDom.getDocument(xmlFile));
        this.dtdFile = getDTDFileName(xmlFile);
    }

    public pes(Document pruneDoc) {
        this.pruneDoc = pruneDoc;
        pruneRoot = pruneDoc.getDocumentElement();
        pruneOps.pruneDoc = pruneDoc;
        pruneOps.pruneRoot = pruneRoot;
    }

    public Document getPruneDoc() {
        return pruneDoc;
    }

    public Element getPruneRoot() {
        return pruneRoot;
    }

    public void setPruneDoc(Document doc) {
        pruneDoc = doc;
    }

    public void setPruneDoc() {
        setPruneDoc(pruneOps.getPruneDoc());
    }

    public static String getDTDFileName(String xmlFile) {
        String contents = fileHandler.readFromFile(xmlFile);
        String REGEX = "\"";

        Pattern p = Pattern.compile(REGEX);
        String[] items = p.split(contents);

        return items[1];
    }

    public String makePruneString() {
        pruneOps.pruneDoc = pruneDoc;
        pruneOps.pruneRoot = pruneRoot;
        return pruneOps.makePruneString();
    }

    public void writeMetaDataPruneDoc(String myFileName) {
        writePesToSes();
        SESOps.writeDTDToXML(myFileName + "DTD");
        SESOps.writeSesDoc(myFileName + "Ses.xml");
        writePruneDoc(myFileName, myFileName + "DTD.dtd");
    }

    public void writePruneDoc(String xmlFile, String dtdFile) {
        pruneOps.pruneDoc = pruneDoc;
        pruneOps.pruneRoot = pruneRoot;
        pruneOps.writePruneDoc(xmlFile, dtdFile);
    }

    public void writePruneDoc(String xmlFile) {
        writePruneDoc(xmlFile, dtdFile);
    }

//////////////////////
    public void copyAttributesFrom(Element el, Element pesEl) {
        NamedNodeMap m = pesEl.getAttributes();
        if (m != null) {
            for (int i = 0; i < m.getLength(); i++) {
                Attr n = (Attr) m.item(i);
                String attr = n.getName();
                String val = n.getNodeValue();
                if (!"".equals(val)) {
                    el.setAttribute(attr, val);
                }
            }
        }
    }

    public void copyFrom(pes p) {
        copyFrom(pruneRoot, p.pruneRoot);
    }

    public void copyFrom(Element el, Element pesEl) {
        copyAttributesFrom(el, pesEl);
        LinkedList<Object> q = pruneOps.getActualChildren(el);
        LinkedList<Object> qel = pruneOps.getActualChildren(pesEl);
        for (int i = 0; i < q.size(); i++) {
            copyFrom((Element) q.get(i), (Element) qel.get(i));
        }
    }

    public void removeElementAttributes(Element el, String[] attrs) {
        for (int i = 0; i < attrs.length; i++) {
            String attr = attrs[i];
            el.removeAttribute(attr);
        }
    }

    public void removeAttributes(String[] attrs) {
        removeAttributes(pruneRoot, attrs);
    }

    public void removeAttributes(Element pesEl, String[] attrs) {
        removeElementAttributes(pesEl, attrs);
        LinkedList<Object> qel = pruneOps.getActualChildren(pesEl);
        for (int i = 0; i < qel.size(); i++) {
            removeAttributes((Element) qel.get(i), attrs);
        }
    }

    public String getValueOfAttributeOfFirst(String tagNm, String attr) {
        NodeList ln = pruneDoc.getElementsByTagName(tagNm);
        if (ln.getLength() == 0) {
            System.out.println("attr " + attr + " is not an attribute of "
                    + tagNm + " in " + pruneRoot.getNodeName());
            return "";
        }
        Element ent = (Element) ln.item(0);
        return ent.getAttribute(attr);
    }

    public LinkedList<Object> getValueOfAttributesOfAll(String tagNm, String attr) {
        LinkedList<Object> q = new LinkedList<Object>();
        NodeList ln = pruneDoc.getElementsByTagName(tagNm);
        for (int i = 0; i < ln.getLength(); i++) {
            Element ent = (Element) ln.item(i);
            q.add(ent.getAttribute(attr));
        }
        return q;
    }

    public Hashtable<Object,Object> getTextValuesOf(Element ent) {
        Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        LinkedList<Object> q = pruneOps.getActualChildren(ent);
        Iterator it = q.iterator();
        while (it.hasNext()) {
            Element ch = (Element) it.next();
            f.put(ch.getNodeName(), ch.getAttribute("text"));
        }
        return f;
    }

    public void setValueOfAttribute(String entNm, String attr, String val) {
        NodeList ln = pruneDoc.getElementsByTagName(entNm);
        if (ln.getLength() == 0) {
            System.out.println("attr " + attr + " is not an attribute of "
                    + entNm + " in " + pruneRoot.getNodeName());
            return;
        }

        Element ent = (Element) ln.item(0);
        ent.setAttribute(attr, val);
    }

    public LinkedList<Object> getValuesOfAttribute(String attr, Element parent) {
        LinkedList<Object> res = new LinkedList<Object>();
        pruneOps.pruneDoc = pruneDoc;
        LinkedList<Object> chds = pruneOps.getActualChildren(parent);
        Iterator it = chds.iterator();
        while (it.hasNext()) {
            Element el = (Element) it.next();
            res.add(el.getAttribute(attr));
        }
        return res;
    }

    public LinkedList<Object> getValuesOfAttribute(String attr, String parent) {
        NodeList ln = pruneDoc.getElementsByTagName(parent);
        if (ln.getLength() == 1) {
            return getValuesOfAttribute(attr, (Element) ln.item(0));
        }
        return new LinkedList<Object>();
    }

    public Hashtable<Object,HashSet<Object>> getPairsOfAttributes(String key, String value,
            Element parent) {
        Hashtable<Object,HashSet<Object>> res = new Hashtable<Object,HashSet<Object>>();
        pruneOps.pruneDoc = pruneDoc;
        LinkedList<Object> chds = pruneOps.getActualChildren(parent);
        Iterator<Object> it = chds.iterator();
        while (it.hasNext()) {
            Element el = (Element) it.next();
            String elkey = el.getAttribute(key);
            String elvalue = el.getAttribute(value);
            if(res.containsKey(elkey)){
            	res.get(elkey).add(elvalue);
            }else{
            	HashSet<Object> valSet = new HashSet<Object>();
            	valSet.add(elvalue);
            	res.put(elkey, valSet);
            }
        }
        return res;
    }

    public Hashtable<Object,HashSet<Object>> getPairsOfAttributes(String key, String value,
            String parent) {
        NodeList ln = pruneDoc.getElementsByTagName(parent);
        if (ln.getLength() == 1) {
            return getPairsOfAttributes(key, value, (Element) ln.item(0));
        }
        return new Hashtable<Object,HashSet<Object>>();
    }

    public void getRelations(sesRelation ses) {
        pruneOps.pruneDoc = pruneDoc;
        NodeList nl = pruneDoc.getElementsByTagName("Hashtable<Object,HashSet<Object>>");
        for (int i = 0; i < nl.getLength(); i++) {
            Element rel = (Element) nl.item(i);
            restrictRelation r = ses.makeRelationForSpecs(
                    rel.getAttribute("domainSpec"),
                    rel.getAttribute("rangeSpec"));
            String useConverse = rel.getAttribute("useConverse");
            Element aspofRel = (Element) pruneOps.getActualChildren(rel).getFirst();
            Element relStr = (Element) pruneOps.getActualChildren(aspofRel).getFirst();
            Element prs = (Element) pruneOps.getActualChildren(relStr).getFirst();
            Element asprs = (Element) pruneOps.getActualChildren(prs).getFirst();
            Element prsma = (Element) pruneOps.getActualChildren(asprs).getFirst();
            Hashtable<Object,HashSet<Object>> pairs = getPairsOfAttributes("key", "value", prsma);
            r.setRelation(pairs);
            if (useConverse.equals("true")) {
                ses.replaceByConverse(r);
            }
        }
    }

    public NodeList getNodeList(String tag) {
        pruneOps.pruneDoc = pruneDoc;
        return pruneDoc.getElementsByTagName(tag);
    }

////////////////////////
    public Element getMetaDataElement(String entity, String suffix) {
        pruneOps.pruneDoc = pruneDoc;
        pruneOps.pruneRoot = pruneRoot;
        Element entMeta = pruneOps.getPruneElement(entity + "MetaData");
        if (entMeta == null) {
            return null;
        }
        LinkedList<Object> q = pruneOps.getActualChildren(entMeta);
        if (q.isEmpty()) {
            return null;
        }
        q = pruneOps.getActualChildren((Element) q.getFirst());
        if (q.isEmpty()) {
            return null;
        }
        q = pruneOps.getActualChildren((Element) q.getFirst());
        if (q.isEmpty()) {
            return null;
        }
        Element ent = (Element) q.getFirst();
        ent.setAttribute("sourceEntity", entity + suffix);
        return ent;
    }

    public void compileAllMetatDataFor(String[] entities, pes[] ps) {
        for (int i = 0; i < entities.length; i++) {
            compileAllMetatDataFor(entities[i], ps);
        }
    }

    public void compileAllMetatDataFor(String entity, pes[] ps) {
        Element myEntity = getMetaDataElement(entity, "");
        if (myEntity == null) {
            return;
        }
        prepareMyAttributes(myEntity);
        for (int i = 0; i < ps.length; i++) {
            Element otherEntity = ps[i].getMetaDataElement(entity, ps[i].id);
            if (otherEntity != null) {
                copyOverFrom(myEntity, otherEntity, ps[i]);
            }
        }
    }

    public void copyOverFrom(Element el, Element pesEl, pes p) {
        copyAttributesOverFrom(el, pesEl, p);
        LinkedList<Object> q = pruneOps.getActualChildren(el);
        LinkedList<Object> qel = pruneOps.getActualChildren(pesEl);
        for (int i = 0; i < q.size(); i++) {
            copyOverFrom((Element) q.get(i), (Element) qel.get(i), p);
        }
    }

    public void copyAttributesOverFrom(Element el, Element pesEl, pes p) {
        String other = p.id;
        NamedNodeMap m = pesEl.getAttributes();
        if (m != null) {
            for (int i = 0; i < m.getLength(); i++) {
                Attr n = (Attr) m.item(i);
                String attr = n.getName();
                if (!attr.equals("sourceEntity")) {
                    String myval = el.getAttribute(attr);
                    String otherval = n.getNodeValue();
                    el.setAttribute(other + "." + attr, otherval);
                }
            }
        }
    }

    public void prepareMyAttributes(Element el) {
        prepareMyAttributesBase(el);
        LinkedList<Object> q = pruneOps.getActualChildren(el);
        for (int i = 0; i < q.size(); i++) {
            prepareMyAttributes((Element) q.get(i));
        }
    }

    public void prepareMyAttributesBase(Element el) {
        NamedNodeMap m = el.getAttributes();
        HashSet<Object> cp = new HashSet<Object>();
        if (m != null) {
            for (int i = 0; i < m.getLength(); i++) {
                Attr n = (Attr) m.item(i);
                String attr = n.getName();
                if (!attr.equals("sourceEntity")) {
                    cp.add(n);

                }
            }
            Iterator it = cp.iterator();
            while (it.hasNext()) {
                Attr n = (Attr) it.next();
                String attr = n.getName();
                String myval = el.getAttribute(attr);
                el.setAttribute(attr, "ListOf:" + attr);
                el.setAttribute(id + "." + attr, myval);
            }
        }
    }

    public Hashtable<Object,Object> getCompiledValuesFor(String entity, String metaEnt) {
        Hashtable<Object,Object> f = getCompiledValuesFor(entity);
        Hashtable<Object,Object> res = new Hashtable<Object,Object>();
        Enumeration<Object> e = f.keys();
        while(e.hasMoreElements()){
        	Object key = e.nextElement();
        	Object value = f.get(key);
        	String name = (String) key;
            if (name.startsWith(metaEnt)) {
                res.put(name.substring(metaEnt.length() + 1), value);
            }
        }
        
        return res;
    }

    public Hashtable<Object,Object> getCompiledValuesFor(String entity) {
        Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        Element el = getMetaDataElement(entity, "");
        getCompiledValues(el, f);
        return f;
    }

    public void getCompiledValues(Element el, Hashtable<Object,Object> f) {
        getCompiledValuesBase(el, f);
        LinkedList<Object> q = pruneOps.getActualChildren(el);
        for (int i = 0; i < q.size(); i++) {
            getCompiledValues((Element) q.get(i), f);
        }
    }

    public void getCompiledValuesBase(Element el, Hashtable<Object,Object> f) {
        String elNm = el.getNodeName();
        NamedNodeMap m = el.getAttributes();
        if (m != null) {
            for (int i = 0; i < m.getLength(); i++) {
                Attr n = (Attr) m.item(i);
                String attr = n.getName();
                if (!attr.equals("sourceEntity")
                        && !n.getValue().startsWith("ListOf:")) {
                    f.put(elNm + "." + attr, n.getValue());
                }
            }
        }
    }

//////
    public void writePesToSes() {
        pruneOps.pruneDoc = pruneDoc;
        pruneOps.pruneRoot = pruneRoot;
        pruneOps.mapPesToSes();
    }

    public void printTree() {
        writePesToSes();
        SESOps.printTree();
    }

////////
    public sesRelation aggregate() {
        writePesToSes();
        sesRelation ses = new sesRelation(SESOps.sesDoc);
        return ses;
    }

    public boolean includedIn(sesRelation ses) {
        sesRelation mySes = aggregate();
        // return mySes.conformsTo(ses);
        return ses.include(mySes);
    }

    public boolean includedIn(String sesFile) {
        return includedIn(new sesRelation(sesFile));
    }

    public boolean conformsTo(String sesFile) {
        pruneOps.pruneDoc = pruneDoc;
        pruneOps.pruneRoot = pruneRoot;
        validateSes.restoreSesDoc(sesFile);
        return validatePruning.conformsTo();
    }

    ////////////////////////////
    public static void main(String argv[]) {
        System.exit(3);

    } // main
}
