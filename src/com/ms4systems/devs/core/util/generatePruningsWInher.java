package com.ms4systems.devs.core.util;

import org.w3c.dom.*;


import java.util.*;

import java.util.regex.Pattern;

@SuppressWarnings({"rawtypes","unused"})
public class generatePruningsWInher extends generatePrunings {

    static HashSet<Object> specNamesCopied = new HashSet<Object>();

    public static Element pickFrom(LinkedList<Object> q) {
        return (Element) q.get(r.nextInt(q.size()));
    }

    public static void copySesAttributesFrom(NamedNodeMap ma, Element pesEl) {
        if (ma == null) {
            return;
        }
        for (int i = 0; i < ma.getLength() / 2; i++) {
            Attr n = (Attr) ma.item(i);
            String AttrName = n.getName();
            if (AttrName.equals("name")) {
                String name = n.getNodeValue();
                if (name.startsWith("function")) {
                    int indOfDot = name.indexOf(".");
                    name = name.substring(indOfDot + 1, name.length());
                    int indEqu = name.indexOf("=");
                    String val = name.substring(indEqu + 1, name.length());
                    name = name.substring(0, indEqu).trim();
                    pesEl.setAttribute(name, "computed using: " + val);
                } else {
                    Attr m = (Attr) ma.item(i + 1);
                    String rangeSpec = m.getName();
                    if (rangeSpec.equals("rangeSpec")) {
                        String range = m.getNodeValue();
                        if (range.equals("")) {
                            pesEl.setAttribute(name, name + "Value"); // choice
                        } else {
                            pesEl.setAttribute(name, range + "Value"); // choice
                        }
                    }
                }
            }
        }
    }

    public static void generateEntity(Element e, Element se) {
        if (e == null || se == null) {
            return;
        }
        LinkedList<Object> vars = SESOps.getElementChildrenOfElement(se, "var");
        Iterator iv = vars.iterator();
        while (iv.hasNext()) {
            Element var = (Element) iv.next();
            NamedNodeMap m = var.getAttributes();
            copySesAttributesFrom(m, e);
        }

        LinkedList<Object> aspects = SESOps.getChildrenOf(se.getAttribute("name"),
                "aspect");
        aspects.addAll(SESOps.getChildrenOf(se.getAttribute("name"),
                "multiAspect"));
        LinkedList<Object> specs = SESOps.getChildrenOf(se.getAttribute("name"),
                "specialization");

        Iterator it = specs.iterator();
        while (it.hasNext()) {
            String chNm = (String) it.next();
            if (!selectLegal && r.nextDouble() < (errorPercent / 100)) {
                continue;
            }
            Element spec = pruneDoc.createElement(chNm);
            e.appendChild(spec);
            generateSpec(spec);
        }

        if (!aspects.isEmpty()) {
            String chNm = "aspectsOf" + se.getAttribute("name");
            Element aspof = pruneDoc.createElement(chNm);
            e.appendChild(aspof);
            String aspNm = selectFrom(aspects);
            Element asp = pruneDoc.createElement(aspNm);
            aspof.appendChild(asp);
            if (multiAspectNames.contains(asp.getNodeName())) {
                generateMultiAsp(asp);
            } else {
                generateAsp(asp);
            }
        }
    }

    public static void generateSpec(Element spec) {

        Element spe = SESOps.getElement("specialization", spec.getNodeName());
        if (spe == null) {
            return;
        }
        LinkedList<Object> entities = SESOps.getChildrenOfElement(spe, "entity");
        if (entities.isEmpty()) {
            return;
        }
        String entNm = selectFrom(entities);
        Element ent = pruneDoc.createElement(entNm);
        spec.appendChild(ent);
        Element sen = SESOps.getElement("entity", entNm);
        generateEntity(ent, sen);

    }

    public static void generateAsp(Element asp) {

        Element as = SESOps.getElement("aspect", asp.getNodeName());
        if (as == null) {
            return;
        }
        LinkedList<Object> entities = SESOps.getChildrenOfElement(as, "entity");
        if (entities.isEmpty()) {
            return;
        }
        Iterator it = entities.iterator();
        while (it.hasNext()) {
            String entNm = (String) it.next();
            Element ent = pruneDoc.createElement(entNm);
            asp.appendChild(ent);
            Element sen = SESOps.getElement("entity", entNm);
            generateEntity(ent, sen);
        }

    }

    public static void generateMultiAsp(Element asp) {

        Element mult = SESOps.getElement("multiAspect", asp.getNodeName());

        LinkedList<Object> mcs = SESOps.getChildrenOfElement(mult, "entity");
        Iterator mit = mcs.iterator();
        Element se = SESOps.getElement("entity", (String) mit.next());

        String cop = mult.getAttribute("coupling");

        mcs = getElementChildrenOfElement(mult, "numberComponentsVar");
        mit = mcs.iterator();
        Element ncv = (Element) mit.next();
        String ncvar = ncv.getAttribute("name");

        String min = ncv.getAttribute("min");
        int Min = 0;
        if (!min.equals("")) {
            Min = Integer.parseInt(min);
        }
        String max = ncv.getAttribute("max");
        int Max = 0;
        if (!max.equals("")) {
            Max = Integer.parseInt(max);
        }
        int NumEnts = Min + r.nextInt(Max - Min);
        if (NumEnts > Max) {
            NumEnts = Min;
        }

        asp.setAttribute(ncvar, "" + NumEnts);
        for (int i = 0; i < NumEnts; i++) {
        	LinkedList<Object> entities = SESOps.getChildrenOfElement(mult, "entity");
            if (entities.isEmpty()) {
                return;
            }

            String entNm = (String) entities.getFirst();
            if (!selectLegal && r.nextDouble() < (errorPercent / 100)) {
                entNm += "xxx";
            }
            Element ent = pruneDoc.createElement(entNm);
            asp.appendChild(ent);
            Element sen = SESOps.getElement("entity", entNm);
            generateEntity(ent, sen);
        }
    }

    public static void generate(String xmlFile, String dtdFile) {
        createPesDoc(SESOps.sesRoot.getAttribute("name"));
        generateEntity(pruneRoot, SESOps.sesRoot);
        writePruneDoc(xmlFile, dtdFile);
    }

    public static void generateMalformed(String xmlFile, String dtdFile,
            int numberInstances) {
        selectLegal = false;
        for (int i = 0; i < numberInstances; i++) {
            int dot = xmlFile.indexOf(".");
            String nextxmlFile = xmlFile.substring(0, dot) + i + ".xml";
            generate(nextxmlFile, dtdFile);
        }
    }

    public static void generate(String xmlFile, String dtdFile,
            int numberInstances) {
        selectLegal = true;
        for (int i = 0; i < numberInstances; i++) {
            int dot = xmlFile.indexOf(".");
            String nextxmlFile = xmlFile.substring(0, dot) + i + ".xml";
            generate(nextxmlFile, dtdFile);
        }
    }

    // //////////////////////////////
    public static void removeAllAttributes(Element e) {
        Set<Object> es = new HashSet<Object>();
        NamedNodeMap m = e.getAttributes();
        for (int i = 0; i < m.getLength(); i++) {
            Attr n = (Attr) m.item(i);
            String AttrName = n.getName();
            if (AttrName.startsWith("xsi") || AttrName.startsWith("xml")) {
                continue;
            }
            es.add(AttrName);
        }
        Iterator it = es.iterator();
        while (it.hasNext()) {
            removeAttribute(e, (String) it.next());
        }
    }

    public static void removeAttribute(Element e, String attrNm) {
        NamedNodeMap m = e.getAttributes();
        m.removeNamedItem(attrNm);
    }

    public static void copyAttributesFromTo(Element e, Element ent) {
        NamedNodeMap m = e.getAttributes();
        for (int i = 0; i < m.getLength(); i++) {
            Attr n = (Attr) m.item(i);
            String AttrName = n.getName();
            if (AttrName.startsWith("xsi") || AttrName.startsWith("xml")) {
                continue;
            }
            String val = n.getNodeValue();
            String defattr = AttrName+"DefaultValue";
            if (ent.hasAttribute(defattr)){
                String defval = ent.getAttribute(defattr);
                ent.setAttribute(AttrName, defval);
            }
            else if (!ent.hasAttribute(AttrName) || ent.getAttribute(AttrName).equals("unknownValue")) // special overrides general
            {
                ent.setAttribute(AttrName, val);
            }
        }
    }

    public static void copyAttributesFromRootTo(Element e, Element ent) {
        NamedNodeMap m = e.getAttributes();
        for (int i = 0; i < m.getLength(); i++) {
            Attr n = (Attr) m.item(i);
            String AttrName = n.getName();
            String val = n.getNodeValue();
            if (!ent.hasAttribute(AttrName) || ent.getAttribute(AttrName).equals("unknownValue")) // special overrides general
            {
                ent.setAttribute(AttrName, val);
            }
        }
    }

    public static void copyAllChildrenFromTo(Element e, Element ent) {
        LinkedList<Object> es = getActualChildren(e);
        if (!es.isEmpty()) {
            Iterator it = es.iterator();
            while (it.hasNext()) {
                Element ch = (Element) it.next();
                Node nch = ch.cloneNode(true);
                ent.appendChild(nch);
            }
        }
    }

    public static void copyOtherSpecsFromTo(String spNm, Element e, Element ent) {
    	LinkedList<Object> es = getActualChildren(e);
        if (!es.isEmpty()) {
            Iterator it = es.iterator();
            while (it.hasNext()) {
                Element ch = (Element) it.next();
                String chNm = ch.getNodeName();
                if (chNm.endsWith("Spec") || chNm.endsWith("Type")) {
                    if (!chNm.equals(spNm)) {
                        Node nch = ch.cloneNode(true);
                        ent.appendChild(nch);
                    }
                }
            }
        }
    }

    public static Element getAspectsOf(Element ent) {
    	LinkedList<Object> es = getActualChildren(ent);
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Element ch = (Element) it.next();
            String chNm = ch.getNodeName();
            if (chNm.startsWith("aspectsOf")) {
                return ch;
            }
        }
        return null;
    }

    public static void copyAspectsFromTo(Element e, Element ent) {
        Element aspsOf = getAspectsOf(e);
        if (aspsOf != null) {
        	LinkedList<Object> et = getActualChildren(aspsOf);
            Iterator itt = et.iterator();
            while (itt.hasNext()) {
                Element gch = (Element) itt.next();
                String gchNm = gch.getNodeName();
                if (gchNm.endsWith("Asp") || gchNm.endsWith("Dec")) {
                    Node gchc = gch.cloneNode(true);
                    Element aspsOfE = getAspectsOf(ent);
                    if (aspsOfE == null) {
                        aspsOfE = pruneDoc.createElement("aspectsOf" + ent.getNodeName());
                        ent.appendChild(aspsOfE);
                    }
                    aspsOfE.appendChild(gchc);
                }
            }
        }
    }

    public static void removeAllAspects(Element e) {
        Element aspsOf = getAspectsOf(e);
        if (aspsOf != null) {
            e.removeChild(aspsOf);
        }
    }

    public static void removeAllCopiedSpecs(Element e) {

        Iterator it = specNamesCopied.iterator();
        while (it.hasNext()) {
            String spNm = (String) it.next();
            LinkedList<Object> es = getActualChildren(e);
            if (!es.isEmpty()) {
                Iterator is = es.iterator();
                while (is.hasNext()) {
                    Element ch = (Element) is.next();
                    String chNm = ch.getNodeName();
                    if (chNm.endsWith("Spec") || chNm.endsWith("Type")) {
                        if (chNm.equals(spNm)) {
                            e.removeChild(ch);
                        }
                    }
                }
            }
        }
    }

    public static void changeRoot(Element e, Element ent, String prefix) {
        copyAttributesFromRootTo(e, ent);

        createPesDoc(prefix + "_" + e.getNodeName());

        Element impent = (Element) pruneDoc.importNode(ent, true);
        copyAttributesFromTo(impent, pruneRoot);
        copyAllChildrenFromTo(impent, pruneRoot);

        Element pel = pruneDoc.getDocumentElement();
        String pn = pruneDoc.getDocumentElement().getNodeName();
    }

    public static void doInherit() {
        doInheritFrom(pruneRoot, pruneRoot.getNodeName(), "", pruneRoot.getNodeName());//pruneRoot.getNodeName());
        if (newRoot != null) {
            changeRoot(pruneRoot, newRoot, newPrefix);
        }
        specNamesCopied = new HashSet<Object>();
    }

    public static void doInheritFrom(Element e, String topEntNm, String prefix, String path) {
    	LinkedList<Object> es = getActualChildren(e);
        if (es.isEmpty()) {
            if (prefix.equals("")) {
                e.setAttribute("pruneName", topEntNm);
            } else {
                e.setAttribute("pruneName", prefix + "_" + topEntNm);
            }
            return;
        }
        Iterator it = es.iterator();
        boolean someSpecs = false;
        while (it.hasNext()) {
            Element ch = (Element) it.next();
            String chNm = ch.getNodeName();
            if (chNm.endsWith("Spec") || chNm.endsWith("Type")) {
                doInheritFrom(ch, e, topEntNm, prefix, path);//e.getNodeName());
                someSpecs = true;
                specNamesCopied.add(chNm);
            }
        }
        doInheritBelowAspects(e, path);
        if (!someSpecs) {
            if (prefix.equals("")) {
                e.setAttribute("pruneName", topEntNm);
            } else {
                e.setAttribute("pruneName", prefix + "_" + topEntNm);
            }
            return;
        }
    }

    public static void doInheritBelowAspects(Element e, String path) {
        Element aspsOf = getAspectsOf(e);
        if (aspsOf != null) {
            int indl = path.lastIndexOf(".");
            String lastEntNm = "";
            if (indl <= -1) {
                lastEntNm = path.substring(indl + 1);
            }
            LinkedList<Object> et = getActualChildren(aspsOf);
            Iterator itt = et.iterator();
            while (itt.hasNext()) {
                Element gch = (Element) itt.next();
                String gchNm = gch.getNodeName();
                if (gchNm.endsWith("Asp") || gchNm.endsWith("Dec")) {
                	LinkedList<Object> es = getActualChildren(gch);
                    Iterator is = es.iterator();
                    while (is.hasNext()) {
                        Element ent = (Element) is.next();
                        doInheritFrom(ent, ent.getNodeName(), "",
                                path + "." + "aspectsOf" + lastEntNm + "." + gchNm + "." + ent.getNodeName());
                    }
                }
            }
        }
    }

    public static String trimFullname(String fullname) {
        String trim = "";
        String[] fullArr = fullname.split("\\.");
        int specInd = 0;
        for (int i = 0; i < fullArr.length; i++) {
            String arr = fullArr[i];
            if (arr.endsWith("Spec") || arr.endsWith("Type")) {
                specInd = i;
                break;
            }
        }

        for (int i = 0; i < specInd; i++) {
            if (i != specInd - 1) {
                trim += fullArr[i] + ".";
            } else {
                trim += fullArr[i];
            }
        }
        return trim;
    }

    public static String myReplaceDot(String ss) {
        String s = ss + "";
        while (s.contains(".")) {
            int id = s.indexOf(".");
            s = s.substring(0, id) + "!" + s.substring(id + 1);
        }
        return s;
    }

    public static boolean containsParts(String s1, String s2) {
        String s11 = myReplaceDot(s1);
        String s21 = myReplaceDot(s2);
        Pattern p = Pattern.compile("!");
        String[] groups1 = p.split(s11);
        String[] groups2 = p.split(s21);
        for (int i = 0; i < groups2.length; i++) {
            String part2 = groups2[i];
            String part1 = groups1[i];
            if (!part1.contains(part2)) {
                return false;
            }
        }
        return true;
    }

    public static boolean vectContainsString(Vector vec, String specpart, String pathname) {
        int lastdot = pathname.lastIndexOf(".");
        String pathToSpec = pathname.substring(0, lastdot);
        String spec = pathname.substring(lastdot + 1);
        ///could test whether one of spec parts is in path
        Iterator it = vec.iterator();
        while (it.hasNext()) {
            String vs = (String) it.next();
            if (containsParts(vs, pathToSpec) &&
                    vs.contains(specpart)) {
                return true;
            }
        }
        return false;
    }

    public static double matchlPath(Vector selNodeVec, String nname, String pathname) {
        int matches = 0;
        Pattern p = Pattern.compile("_");
        String[] groups = p.split(nname);
        for (int i = 0; i < groups.length; i++) {
            String specpart = groups[i];
            if (vectContainsString(selNodeVec, specpart, pathname)) {
                matches++;
            }
        }
        return matches / groups.length;
    }

    public static void doInheritFrom(Element sp, Element e, String topEntNm, String prefix, String path) {
    }

    public static Element newRoot;
    public static String newPrefix;

    public static void doInheritFromTo(Element sp, Element ent, Element e,
            String prefix) {
        copyAspectsFromTo(e, ent);
        if (e.equals(pruneRoot)) {
            newRoot = ent;
            newPrefix = prefix;
        }
        copyAttributesFromTo(e, ent);
        Node par = e.getParentNode();
        if (par == null) {
            return;
        }
        par.removeChild(e);
        par.appendChild(ent);
    }

    public static void writeGenericPESForSchema(String xmlFile, String schFile) {
        String rootElemName = SESOps.sesRoot.getAttribute("name");
        Element rootNode = SESOps.sesRoot;
        if (rootElemName.equals("")) {
            NodeList nl = SESOps.sesRoot.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i).getNodeName().equals("entity")) {
                    rootNode = (Element) nl.item(i);
                    rootElemName = rootNode.getAttribute("name");
                    break;
                }
            }
        }
        createPesDoc(rootElemName);
        genericPESEntity(pruneRoot, rootNode, pruneRoot.getNodeName());
        doInherit();
        writePruneSchemaDoc(xmlFile, schFile);
    }

   

    public static void main(String argv[]) {
    	LinkedList<Object> q = new LinkedList<Object>();
        createPesDoc("root");
        q.add(pruneDoc.createElement("cc"));
        q.add(pruneDoc.createElement("bb"));
        q.add(pruneDoc.createElement("aa"));
        String[] seq = new String[]{"a", "c"};
        System.out.println(q);
        System.exit(3);
    } // main
}
