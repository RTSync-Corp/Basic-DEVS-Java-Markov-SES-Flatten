package com.ms4systems.devs.core.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;
import org.w3c.dom.*;
import java.util.*;

//returns the DOM of a given .xml file
public class XMLToDom {

    public static Document getDocument(String xmlFileString) {
        Document document = null;
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl",null);
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(new File(xmlFileString));
            return document;
        } catch (SAXException sxe) {
            // Error generated during parsing)
            Exception x = sxe;
            if (sxe.getException() != null) {
                x = sxe.getException();
            }
            x.printStackTrace();

        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();

        } catch (IOException ioe) {
            // I/O error
            ioe.printStackTrace();
        }
        return document;
    }

    public static void printChildrenOf(String xmlFileString, String tagName) {
        Document doc = getDocument(xmlFileString);
        NodeList nl = doc.getElementsByTagName(tagName);
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            System.out.println(tagName + " has children: " + getChildrenOf(el));
        }
    }

    public static void printAttributesAndValuesOf(String xmlFileString,
            String tagName) {
        Document doc = getDocument(xmlFileString);
        NodeList nl = doc.getElementsByTagName(tagName);
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            System.out.println(tagName + " has attributes:" + getAttributesAndValuesOf(el));
        }
    }

    public static String getAttributesAndValuesOf(Element el) {
        String s = "";
        NamedNodeMap m = el.getAttributes();
        if (m == null) {
            return "";
        }
        for (int i = 0; i < m.getLength(); i++) {
            Attr n = (Attr) m.item(i);
            s += " " + n.getName() + " = " + n.getNodeValue() + ",";
        }
        if (s.length() < 1) {
            return s;
        }
        s = s.substring(0, s.length() - 1);
        return s;
    }

    public static String getChildrenOf(Element el) {
        String s = "";
        NodeList nl = el.getChildNodes();
        if (nl.getLength() == 0) {
            return " none";
        }
        for (int i = 0; i < nl.getLength(); i++) {
            Node ch = nl.item(i);
            if (ch.getNodeType() != Node.TEXT_NODE) {
                s += ch.getNodeName() + ",";
            }
        }
        s = s.substring(0, s.length() - 1);
        return s;
    }

    public static Node getAncestor(Node n, int level) {
        if (level == 0) {
            return n;
        }
        return getAncestor(n.getParentNode(), level - 1);
    }

    public static Node getAncestorAlongPath(Node n, String path) {
        if (path.equals("")) {
            return n;
        }
        int indOfDot = path.indexOf(".");
        if (indOfDot == -1) {
            return getAncestorAlongPath(n.getParentNode(), "");
        } else {
            String lab = path.substring(0, indOfDot);
            if (labelStartsWithTag(lab, n.getParentNode().getNodeName())) {
                return getAncestorAlongPath(n.getParentNode(),
                        path.substring(indOfDot + 1,
                        path.length()));
            }
        }
        return null;
    }

    public static boolean labelStartsWithTag(String label, String tag) {
        return label.startsWith(tag);
    }

    public static Node getAncestorWChild(Document doc, Node n, String chd) {
        if (labelStartsWithTag(getLabel(n), chd)) {
            return n;
        }
        if (n.getParentNode() == null) {
            return null;
        }
        return getAncestorWChild(doc, n.getParentNode(), chd);
    }

    public static Node getAncestorWChildren(Document doc, Node n, LinkedList<Object> chds) {
    	LinkedList<Object> copy = new LinkedList<Object>();
        Iterator it = chds.iterator();
        while (it.hasNext()) {
            copy.addLast(it.next());
        }
        if (chds.isEmpty()) {
            return n;
        }
        Node p = getAncestorWChild(doc, n, (String) chds.getFirst());
        if (p == null) {
            return null;
        }
        copy.remove(0);
        return getAncestorWChildren(doc, p, copy);
    }

    public static String getLabel(Node n) {
        if (n instanceof Element) {
            Element el = (Element) n;
            if (!el.getAttribute("number").equals("")) {
                return el.getNodeName() + "[" + el.getAttribute("number") + "]";
            }
            if (!el.getAttribute("id").equals("")) {
                return el.getNodeName() + "[" + el.getAttribute("id") + "]";
            }
            return el.getNodeName();
        }
        return "";
    }

    public static String getParentLabel(Document doc, Node n) {
        if (n.equals(doc.getDocumentElement())) {
            return "";
        } else {
            Node par = n.getParentNode();
            Element el = (Element) par;
            if (!el.getAttribute("number").equals("")) {
                return par.getNodeName() + "[" + el.getAttribute("number")
                        + "]";
            }
            if (!el.getAttribute("id").equals("")) {
                return par.getNodeName() + "[" + el.getAttribute("id") + "]";
            }
            return par.getNodeName();
        }
    }

    public static HashSet<Object> getParentLabels(Document doc, String tag) {
    	HashSet<Object> res = new HashSet<Object>();
        NodeList nl = doc.getElementsByTagName(tag);
        for (int i = 0; i < nl.getLength(); i++) {
            res.add(getParentLabel(doc, nl.item(i)));
        }
        return res;
    }

    public static HashSet<Object> getTopAncestors(Document doc, String tag) {
    	HashSet<Object> paths = getUniqueIDs(doc, tag);
        return getTopAncestors(paths);
    }

    public static Hashtable<Object,Integer> getMultiplicityTopAncestors(HashSet<Object> paths) {
    	Hashtable<Object,Integer> res = new Hashtable<Object,Integer>();
        Iterator<Object> it = paths.iterator();
        while (it.hasNext()) {
            String path = (String) it.next();
            int indOfLastDot = path.lastIndexOf(".");
            if (indOfLastDot == -1) {
            	if(res.contains(path)){
            		int size = res.get(path).intValue();
            		res.put(path,new Integer(size+1));
            	}else{
            		res.put(path,new Integer(1));
            	}
            } else {
                String top = path.substring(indOfLastDot + 1, path.length());
                if(res.contains(top)){
            		int size = res.get(top).intValue();
            		res.put(top,new Integer(size+1));
            	}else{
            		res.put(top,new Integer(1));
            	}
            }
        }

        return res;
    }

    public static HashSet<Object> getTopAncestors(HashSet<Object> paths) {
    	HashSet<Object> res = new HashSet<Object>();
        Iterator it = paths.iterator();
        while (it.hasNext()) {
            String path = (String) it.next();
            int indOfLastDot = path.lastIndexOf(".");
            if (indOfLastDot == -1) {
                res.add(path);
            } else {
                String top = path.substring(indOfLastDot + 1, path.length());
                res.add(top);
            }
        }
        return res;
    }

    public static HashSet<Object> getNodesWTop(String top, Hashtable<Object,HashSet<Object>> r) {
    	HashSet<Object> res = new HashSet<Object>();
    	Enumeration<Object> e = r.keys();
    	while(e.hasMoreElements()){
    		Object key = e.nextElement();
    		HashSet<Object> val = r.get(key);
    		Iterator<Object> it = val.iterator();
    		while(it.hasNext()){
    			Object value = it.next();
    			String path = (String)key;
    			int indOfLastDot = path.lastIndexOf(".");
    			if (indOfLastDot == -1 & path.equals(top)) {
                    res.add(value);
                } else {
                    String topp = path.substring(indOfLastDot + 1, path.length());
                    if (topp.equals(top)) {
                        res.add(value);
                    }
                }
    		}
    	}
        
        return res;
    }

    public static sesRelation computeTopSes(Document doc, String tag) {
        sesRelation ses = new sesRelation();
        ses.rootEntityName = tag;
        NodeList nl = doc.getElementsByTagName(tag);
        if (nl.getLength() <= 1) {
            return ses;
        }
        ses.addAspectToEntity("topsOf" + tag, tag);
        Hashtable<Object,HashSet<Object>> r = XMLToDom.getUniqueIDsRelation(doc, tag);
        HashSet<Object> ids = XMLToDom.getPathsStartW(tag, r);
        //
        System.out.println(ids);
        //for bag
        Hashtable<Object,Integer> tops = XMLToDom.getMultiplicityTopAncestors(ids);
        HashSet<Object> keys = (HashSet<Object>)tops.keySet();
        //
        System.out.println(keys);
        Iterator<Object> it = keys.iterator();
        boolean repeat = false;
        while (it.hasNext()) {
            String top = (String) it.next();
            ses.addEntityToAspect(top, "topsOf" + tag);
            if(tops.get(top).intValue() > 1){
            	iterateRefine(doc, r, tag, top, ses);
            }
        }
        return ses;
    }

    public static void iterateRefine(Document doc, Hashtable<Object,HashSet<Object>> r, String tag,
            String top, sesRelation ses) {
        ses.addAspectToEntity("topsOf" + top, top);
        while (true) {
            boolean repeat = false;
            HashSet<Object> nodes = XMLToDom.getNodesWTop(top, r);
            Hashtable<Object,HashSet<Object>> nr = XMLToDom.getUniqueIDsRelation(doc, nodes);
            HashSet<Object> ids = XMLToDom.getPathsStartW(tag, nr);
            //For Bag
            Hashtable<Object,Integer> tops = XMLToDom.getMultiplicityTopAncestors(ids);
            HashSet<Object> keys = (HashSet<Object>)tops.keySet();
            Iterator<Object> it = keys.iterator();
            while (it.hasNext()) {
                String topp = (String) it.next();
                ses.addEntityToAspect(topp, "topsOf" + top);
                if(tops.get(topp).intValue() >1){
                	iterateRefine(doc,nr,tag,topp,ses);
                	repeat = true;
                }
            }
            if (!repeat) {
                break;
            }
        }
    }

    public static boolean topAncestorsAreDistinguishing(
            HashSet<Object> ancestors, HashSet<Object> paths) {
        return ancestors.size() == paths.size();
    }

    public static boolean topAncestorsAreDistinguishing(Document doc,
            String tag) {
    	HashSet<Object> es = getTopAncestors(doc, tag);
        return es.size() == doc.getElementsByTagName(tag).getLength();
    }

    public static boolean isRegular(Document doc) {
    	LinkedList<Object> q = getAllElementDescendents(doc);
        Iterator<Object> it = q.iterator();
        while (it.hasNext()) {
            Node n = (Node) it.next();
            String tag = n.getNodeName();
            if (!topAncestorsAreDistinguishing(doc, tag)) {
                return false;
            }
        }
        return true;
    }

    public static HashSet<Object> getUniqueIDs(Document doc, String tag) {
    	HashSet<Object> es = new HashSet<Object>();
        NodeList nl = doc.getElementsByTagName(tag);
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            es.add(el);
        }
        return getUniqueIDs(doc, es);
    }

    public static HashSet<Object> getUniqueIDs(Document doc, HashSet<Object> els) {
        Iterator<Object> is = els.iterator();
        while (is.hasNext()) {
            Element el = (Element) is.next();
            String tag = el.getNodeName();
            return getPathsStartW(tag, getUniqueIDsRelation(doc, els));
        }
        return new HashSet<Object>();
    }

    public static Hashtable<Object,HashSet<Object>> getUniqueIDsRelation(Document doc, String tag) {
    	HashSet<Object> es = new HashSet<Object>();
        NodeList nl = doc.getElementsByTagName(tag);
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            es.add(el);
        }
        return getUniqueIDsRelation(doc, es);
    }

    public static String getUniqueID(Document doc, Element el) {
    	Hashtable<Object,HashSet<Object>> r = getUniqueIDsRelation(doc, el.getNodeName());
    	Enumeration<Object> e = r.keys();
    	while(e.hasMoreElements()){
    		Object key = e.nextElement();
    		HashSet<Object> valSet = r.get(key);
    		Iterator it = valSet.iterator();
    		while(it.hasNext()){
    			Object value = it.next();
    			if (value.equals(el)) {
                    return (String) key;
                }
    		}
    	}
        
        return "";
    }

    public static Hashtable<Object,HashSet<Object>> getUniqueIDsRelation(Document doc, HashSet<Object> els) {
    	Hashtable<Object,HashSet<Object>> r = new Hashtable<Object,HashSet<Object>>();
        String tag = "";
        
        Iterator<Object> is = els.iterator();
        while (is.hasNext()) {
            Object el = (Object) is.next();
            if (el instanceof Node) {
                tag = ((Node) el).getNodeName();
                if(r.containsKey("")){
                	r.get("").add(el);
                }else{
                	HashSet<Object> value = new HashSet<Object>();
                	value.add(el);
                	r.put("", value);
                }
            }
        }
        return refine(r, doc);
    }

    public static HashSet<Object> getPathsStartW(String tag, Hashtable<Object,HashSet<Object>> r) {
        HashSet<Object> keys = new HashSet<Object>();
        for(Object o : r.keySet()){
        	keys.add(o);
        }
        HashSet<Object> res = new HashSet<Object>();
        Iterator<Object> it = keys.iterator();
        while (it.hasNext()) {
            String path = (String) it.next();
            if (path.equals("")) {
                res.add(tag);
            } else {
                res.add(tag + "." + path);
            }
        }
        return res;
    }

    public static Hashtable<Object,HashSet<Object>> refine(Hashtable<Object,HashSet<Object>> r, Document doc) {
    	Hashtable<Object,HashSet<Object>> nr = new Hashtable<Object,HashSet<Object>>();
    	
    	Enumeration<Object> e = r.keys();
    	while(e.hasMoreElements()){
    		Object key = e.nextElement();
    		HashSet<Object> val = r.get(key);
    		Iterator<Object> ir = val.iterator();
    		while(ir.hasNext()){
    			Object value = ir.next();
    			if(nr.containsKey(key)){
    				nr.get(key).add(value);
    			}else{
    				HashSet<Object> valueSet = new HashSet<Object>();
    				valueSet.add(value);
    				nr.put(key, valueSet);
    			}
    		}
    	}
        
        while (true) {
            HashSet<Object> domain = new HashSet<Object>();
            for(Object o : nr.keySet()){
            	domain.add(o);
            }
            HashSet<Object> range = new HashSet<Object>(); //nodes
            for(HashSet<Object> hSet : nr.values()){
            	range.addAll(hSet);
            }
            
            if (domain.size() == range.size()) {
                return nr;
            }
            Hashtable<Object,HashSet<Object>> rr = new Hashtable<Object,HashSet<Object>>();
            Iterator<Object> it = domain.iterator();
            while (it.hasNext()) {
                String path = (String) it.next();
                HashSet<Object> nodesStartPath = nr.get(path);
                Iterator<Object> ip = nodesStartPath.iterator();
                if (nodesStartPath.size() == 1) {
                	if(rr.containsKey(path)){
                		rr.get(path).add(ip.next());
                	}else{
                		HashSet<Object> valSet = new HashSet<Object>();
                		valSet.add(it.next());
                		rr.put(path, valSet);
                	}
                    continue;
                }
                while (ip.hasNext()) {
                    Node n = (Node) ip.next();
                    Node anc = getAncestorAlongPath(n, path);
                    if (anc == null) {
                        continue;
                    }
                    String label = getParentLabel(doc, anc);
                    if (path.equals("")) {
                    	if(rr.containsKey(label)){
                    		rr.get(label).add(n);
                    	}else{
                    		HashSet<Object> valSet = new HashSet<Object>();
                    		valSet.add(n);
                    		rr.put(label, valSet);
                    	}
                    } else {
                        String newp = path + "." + label;
                        if(rr.containsKey(newp)){
                    		rr.get(newp).add(n);
                    	}else{
                    		HashSet<Object> valSet = new HashSet<Object>();
                    		valSet.add(n);
                    		rr.put(newp, valSet);
                    	}
                    }
                }
            }
            nr = rr;
        }
    }

    public static LinkedList<Object> getElementOccurrences(Document doc, String tag) {
    	LinkedList<Object> q = new LinkedList<Object>();
        NodeList nl = doc.getElementsByTagName(tag);
        for (int i = 0; i < nl.getLength(); i++) {
            q.add(nl.item(i));
        }
        return q;
    }

    public static Element getElementOccurrence(Document doc, String tag) {
    	LinkedList<Object> q = getElementOccurrences(doc, tag);
        if (q.isEmpty()) {
            return null;
        }
        return (Element) q.getFirst();
    }

    public static LinkedList<Object> getElementOccurrences(Document doc, String tag,
    		LinkedList<Object> chds) {
    	LinkedList<Object> res = new LinkedList<Object>();
        NodeList nl = doc.getElementsByTagName(tag);
        for (int i = 0; i < nl.getLength(); i++) {
            Node anc = getAncestorWChildren(doc, nl.item(i), chds);
            if (anc != null) {
                res.add(nl.item(i));
            }
        }

        int maxId = 0;
        Element max = null;
        Iterator it = res.iterator();
        while (it.hasNext()) {
            Element el = (Element) it.next();
            String id = getUniqueID(doc, el);
            if (id.length() > maxId) {
                maxId = id.length();
                max = el;
            }
        }
        res = new LinkedList<Object>();
        res.add(max);
        return res;
    }

    public static Element getElementOccurrence(Document doc, String tag,
            String[] chds) {
    	LinkedList<Object> q = getElementOccurrences(doc, tag, chds);
        if (q.isEmpty()) {
            return null;
        }
        return (Element) q.getFirst();
    }

    public static LinkedList<Object> getElementOccurrences(Document doc, String tag,
            String[] chds) {
    	LinkedList<Object> q = new LinkedList<Object>();
        for (int i = 0; i < chds.length; i++) {
            q.add(chds[i]);
        }
        return getElementOccurrences(doc, tag, q);
    }

    public static String getAttrVal(Document doc, String tag, String attr,
            String[] chds) {

    	LinkedList<Object> q = getElementOccurrences(doc, tag, chds);
        if (q.isEmpty()) {
            return "";
        }
        if (q.size() == 1) {
            Element prim = (Element) q.getFirst();
            if (prim != null) {
                return prim.getAttribute(attr);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static String getAttrVal(Document doc, String tag, String attr) {
        Element el = getElementOccurrence(doc, tag);
        if (el == null) {
            return "";
        }
        return el.getAttribute(attr);
    }

    public static String getAttrVal(Document doc, String attr) {
    	LinkedList<Object> q = getElementOccurrences(doc, "*");
        Iterator it = q.iterator();
        while (it.hasNext()) {
            Element el = (Element) it.next();
            String val = el.getAttribute(attr);
            if (!val.equals("")) {
                return val;
            }
        }
        return "";
    }

    public static LinkedList<Object> getAttrVals(Document doc, String tag, String attr) {
    	LinkedList<Object> res = new LinkedList<Object>();
    	LinkedList<Object> q = getElementOccurrences(doc, tag);
        Iterator it = q.iterator();
        while (it.hasNext()) {
            Element el = (Element) it.next();
            res.add(el.getAttribute(attr));
        }
        return res;
    }

    public static boolean setAttrVal(Document doc, String tag, String attr,
            String val, String[] chds) {

    	LinkedList<Object> q = getElementOccurrences(doc, tag, chds);
        if (q.isEmpty()) {
            return false;
        }
        if (q.size() == 1) {
            Element prim = (Element) q.getFirst();
            prim.setAttribute(attr, val);
            return true;
        } else {
            return false;
        }
    }

    public static boolean setAttrVal(Document doc, String tag,
            String attr, String val) {
        Element el = getElementOccurrence(doc, tag);
        if (el == null) {
            return false;
        }
        el.setAttribute(attr, val);
        return true;
    }

    public static LinkedList<Object> getChildElements(Document doc, String tag,
            String[] chds) {
    	LinkedList<Object> q = getElementOccurrences(doc, tag, chds);
        if (q.isEmpty()) {
            return q;
        }
        if (q.size() == 1) {
            Element prim = (Element) q.getFirst();
            LinkedList<Object> chs = getChildElements(prim);
            return chs;
        } else {
            return null;
        }
    }

    public static LinkedList<Object> getChildElements(Element el) {
    	LinkedList<Object> es = new LinkedList<Object>();
        NodeList nlc = el.getChildNodes();
        for (int j = 0; j < nlc.getLength(); j++) {
            Node nc = nlc.item(j);
            if (nc instanceof Element) {
                es.add(nc);
            }
        }
        return es;
    }

    public static LinkedList<Object>  getElementDescendentsAtLevel(Element n, int level) {
    	LinkedList<Object>  q = new LinkedList<Object> ();
        if (level == 0) {
            q.add(n);
            return q;
        }
        NodeList nl = n.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node cl = nl.item(i);
            if (cl instanceof Element) {
                q.addAll(getElementDescendentsAtLevel((Element) cl, level - 1));
            }
        }
        return q;
    }

    public static LinkedList<Object>  getAllElementDescendents(Document doc) {
        return getAllElementDescendents(doc.getDocumentElement());
    }

    public static LinkedList<Object>  getAllElementDescendents(Element n) {
        return getAllElementDescendents(n, 0, new HashSet<Object>());
    }

    public static LinkedList<Object>  getAllElementDescendents(Element n, int level,
            Set<Object> accumulation) {
        int lastSize = accumulation.size();
        LinkedList<Object>  q = getElementDescendentsAtLevel(n, level);
        accumulation.addAll(q);
        if (lastSize == accumulation.size()) {
        	LinkedList<Object> queue = new LinkedList<Object>();
        	queue.addAll(accumulation);
        	return queue;
        }
        return getAllElementDescendents(n, level + 1, accumulation);
    }

    public static LinkedList<Object>  getChildrenWNodeName(Node el, String nodeNm) {
    	LinkedList<Object>  res = new LinkedList<Object> ();
        NodeList nlc = el.getChildNodes();
        for (int j = 0; j < nlc.getLength(); j++) {
            Node nc = nlc.item(j);
            if (nc.getNodeName().equals(nodeNm)) {
                res.add(nc);
            }
        }
        return res;
    }

    public static void printDocStats(Document doc) {
        if (doc == null) {
            return;
        }
        System.out.println("For root: "
                + doc.getDocumentElement().getNodeName() + ", "
                + doc.getDocumentElement().getAttribute("name"));
        System.out.println("Number of elements "
                + doc.getElementsByTagName("*").getLength());
        System.out.println("Depth of root node "
                + getDepth(doc.getDocumentElement()));
        System.out.println("Maximum breadth "
                + getMaxBreadth(doc.getDocumentElement()));
        System.out.println("Elements and Occurrences " + "\n"
                + elementsNOccurrences(doc));
        System.out.println("nonVariable Names and Multiple Occurrences "
                + "\n"
                + nonVarNamesMultipleOccurrences(doc));
        System.out.println("Names  " + "\n" + names(doc));
    }
    // For Bag
    public static Hashtable<Object,Integer> elementsNOccurrences(Document doc) {
    	Hashtable<Object,Integer> b = new Hashtable<Object,Integer>();
        NodeList nl = doc.getElementsByTagName("*");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            String tag = el.getNodeName();
            if(b.containsKey(tag)){
            	int size = b.get(tag).intValue();
            	b.put(tag, new Integer(size+1));
            	
            }else {
            	b.put(tag, new Integer(1));
            }
        }
        return b;
    }

    public static Hashtable<Object,Object> namesMultipleOccurrences(Document doc) {
    	Hashtable<Object,HashSet<Object>> b = new Hashtable<Object,HashSet<Object>>();
        NodeList nl = doc.getElementsByTagName("*");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            String name = el.getAttribute("name");
            if(b.containsKey(name)){
            	int size = b.get(name).size();
            	b.get(name).add(new Integer(size+1));
            }else {
            	HashSet<Object> valSet = new HashSet<Object>();
            	valSet.add(new Integer(1));
            	b.put(name, valSet);
            }
        }
        Hashtable<Object,Object> res = new Hashtable<Object,Object>();
        HashSet<Object> s = (HashSet<Object>)b.keySet();
        Iterator<Object> it = s.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            int val = b.get(o).size();
            if (val > 1) {
                res.put(o, new Integer(val));
            }
        }
        return res;
    }

    public static Hashtable<Object,Object> nonVarNamesMultipleOccurrences(Document doc) {
    	Hashtable<Object,HashSet<Object>> b = new Hashtable<Object,HashSet<Object>>();
        NodeList nl = doc.getElementsByTagName("*");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            if (!el.getNodeName().toLowerCase().endsWith("var")) {
                String name = el.getAttribute("name");
                if(b.containsKey(name)){
                	int size = b.get(name).size();
                	b.get(name).add(new Integer(size+1));
                }else {
                	HashSet<Object> valSet = new HashSet<Object>();
                	valSet.add(new Integer(1));
                	b.put(name, valSet);
                }
            }
        }
        Hashtable<Object,Object> res = new Hashtable<Object,Object>();
        Set<Object> s = b.keySet();
        Iterator<Object> it = s.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            int val = b.get(o).size();
            if (val > 1) {
                res.put(o, new Integer(val));
            }
        }
        return res;
    }

    public static HashSet<Object> names(Document doc) {
    	HashSet<Object> es = new HashSet<Object>();
        NodeList nl = doc.getElementsByTagName("*");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            String name = el.getAttribute("name");
            es.add(name);
        }
        return es;
    }

    public static int countNodeContains(Document doc, String attr, String part) {
    	HashSet<Object> es = new HashSet<Object>();
        NodeList nl = doc.getElementsByTagName("*");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            if (el.getAttribute(attr).toLowerCase().contains(part.toLowerCase())) {
                es.add(el);
            }
        }
        return es.size();
    }

    public static int getDepth(Element n) {
        int maxDepth = 1;
        NodeList nl = n.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node cl = nl.item(i);
            if (cl instanceof Element) {
                int depth = getDepth((Element) cl);
                if (depth + 1 > maxDepth) {
                    maxDepth = depth + 1;
                }
            }
        }
        return maxDepth;
    }

    public static int getBreadth(Element n) {
        return getChildElements(n).size();
    }

    public static int getMaxBreadth(Element n) {
    	LinkedList<Object> q = getChildElements(n);
        int maxBreadth = q.size();
        Iterator it = q.iterator();
        while (it.hasNext()) {
            Node el = (Node) it.next();
            if (el instanceof Element) {
                int breadth = getMaxBreadth((Element) el);
                if (breadth > maxBreadth) {
                    maxBreadth = breadth;
                }
            }
        }
        return maxBreadth;
    }

    public static Set<Object> getElementsWName(Element n, String type) {
        Set<Object> res = new HashSet<Object>();
        if (n.getNodeName().startsWith(type)) {
            res.add(n);
            return res;
        }
        LinkedList<Object> q = getChildElements(n);
        Iterator it = q.iterator();
        while (it.hasNext()) {
            Node el = (Node) it.next();
            if (el instanceof Element) {
                res.addAll(getElementsWName((Element) el, type));
            }
        }
        return res;
    }

    public static int getVolumeWOName(Element n, String type) {
        if (n.getNodeName().startsWith(type)) {
            return 0;
        }
        LinkedList<Object> q = getChildElements(n);
        int total = 0;
        Iterator it = q.iterator();
        while (it.hasNext()) {
            Node el = (Node) it.next();
            if (el instanceof Element) {
                total += 1 + getVolumeWOName((Element) el, type);
            }
        }
        return total;
    }

    public static int getRecursionLevel(Element n, String type) {
        int maxDepth = 0;
        Set<Object> es = getElementsWName(n, type);
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Element el = (Element) it.next();
            NodeList nl = el.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i) instanceof Element) {
                    Element ch = (Element) nl.item(i);
                    int depth = getRecursionLevel(ch, type);
                    if (depth + 1 > maxDepth) {
                        maxDepth = depth + 1;
                    }
                }
            }
        }
        return maxDepth;
    }

    public static int getVolume(Element n, String type) {
        int total = getVolumeWOName(n, type);
        Set<Object> es = getElementsWName(n, type);
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Element el = (Element) it.next();
            NodeList nl = el.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i) instanceof Element) {
                    Element ch = (Element) nl.item(i);
                    if (getElementsWName(ch, type).size() > 0) {
                        total += getVolume(ch, type);
                    }
                }
            }
        }
        return total;
    }

    public static Set<Object> copyNExtend(String label, HashSet<Object> paths) {
    	Set<Object> res = new HashSet<Object>();
        Iterator<Object> it = paths.iterator();
        while (it.hasNext()) {
            String path = (String) it.next();
            res.add(path + "." + label);
        }
        return res;
    }

    public static Set<Object> getPathsHavingType(Element n,
            String type, String path) {
        return getPathsHavingType(n, type, "!", path);
    }

    public static Set<Object> getPathsHavingType(Element n,
            String type, String skip,
            String path) {
        String newpath = path;
        Set<Object> res = new HashSet<Object>();
        if (n.getNodeName().startsWith(type)) {
            if (!n.getAttribute("name").toLowerCase().endsWith(skip.toLowerCase())) {
                newpath = path + "." + n.getAttribute("name");
                res.add(newpath);
            } else {
                res.add(newpath);
            }
        }

        LinkedList<Object> q = getChildElements(n);
        Iterator it = q.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof Element) {
                Element el = (Element) o;
                res.addAll(getPathsHavingType(el, type, skip, newpath));
            }
        }
        return res;
    }

    public static LinkedList<Object> getElementOccurrenceNames(Document doc, String tag) {
    	LinkedList<Object> q = new LinkedList<Object>();
        NodeList nl = doc.getElementsByTagName(tag);
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            q.add(el.getAttribute("name"));
        }
        return q;
    }

    public static void printElementsAndNames(Document doc) {
        Hashtable<Object,Integer> b = elementsNOccurrences(doc);
        Set<Object> s = b.keySet();
        Iterator<Object> it = s.iterator();
        while (it.hasNext()) {
            String tag = (String) it.next();
            System.out.println("Element :" + tag);
            System.out.println("----- names " + getElementOccurrenceNames(doc, tag));
        }
    }

    public static void printElementsAttrVals(Document doc) {
        NodeList nl = doc.getElementsByTagName("*");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            System.out.println("Element :" + el.getTagName());
            System.out.println("-----  " + getAttributesAndValuesOf(el));
        }
    }

    public static void printElementsTextVals(Document doc, String tag) {
        NodeList nl = doc.getElementsByTagName(tag);
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            System.out.println(el.getAttribute("text"));
        }
    }

    public static void printElementsChildrenTextVals(Document doc, String tag, String keywd) {
        NodeList nl = doc.getElementsByTagName(tag);
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            LinkedList<Object> q = getChildElements(el);
            Iterator it = q.iterator();
            String DSR = "";
            String DSRDescr = "";
            boolean found = false;
            while (it.hasNext()) {
                Element ent = (Element) it.next();
                if (ent.getNodeName().equals("DSR")) {
                    DSR = ent.getAttribute("text");
                } else if (ent.getNodeName().equals("DSRName-Description")) {
                    DSRDescr = ent.getAttribute("text");
                    if (DSRDescr.contains(keywd)) {
                        found = true;
                    }
                }
            }
            if (found && !DSR.equals("")) {
                System.out.println("For keyword: " + keywd + " DSR:" + DSR + ": " + DSRDescr);
            }
        }
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
                        path.substring(indOfDot + 1,
                        path.length()));
            } else {
                return null;
            }
        }
    }

    public static Element getSpecifiedElement(String entity, String path) {
    	LinkedList<Object> q = XMLToDom.getElementOccurrences(pruneOps.getPruneDoc(), entity);
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

    public static void main(String argv[]) {
        Document doc = getDocument("C:/Users/bernie/Documents/NetBeansProjects/NLPFDDEVS2/src/ARS/Sheet1MineSurvey_RequirementsRowsInstance.xml");
    }
}
