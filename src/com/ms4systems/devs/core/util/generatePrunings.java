package com.ms4systems.devs.core.util;

import org.w3c.dom.*;

import java.io.StringWriter;
import java.util.*;

import javax.swing.JOptionPane;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

@SuppressWarnings({"rawtypes","unused"})
public class generatePrunings extends validatePruning {

    protected static Random r = new Random(331);
    public static boolean selectLegal = true;
    public static double errorPercent = 10;

    public static void setSeed(long seed) {
        r.setSeed(seed);
    }

    public static double getNextRandom() {
        return r.nextDouble();
    }

    public static String selectFrom(LinkedList<Object> q) {
        if (selectLegal) {
            return (String) q.get(r.nextInt(q.size()));
        } else {
            if (r.nextDouble() < (errorPercent / 100)) {
                return "XXX" + (String) q.get(r.nextInt(q.size()));
            } else {
                return (String) q.get(r.nextInt(q.size()));
            }
        }
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
                if (name.contains(" ")) {
                    name = name.replaceAll(" ", "");
                }
                Attr m = (Attr) ma.item(i + 1);
                String rangeSpec = m.getName();
                if (rangeSpec.equals("rangeSpec")) {
                    String range = m.getNodeValue();
                    if (range.contains(" ")) {
                        range = range.replaceAll(" ", "");
                    }
                    if (range.equals("")) {
                        pesEl.setAttribute(name, name + "Value"); // choice
                    } else {
                        pesEl.setAttribute(name, range + "Value"); // choice
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
        Iterator<Object> iv = vars.iterator();
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

        Iterator<Object> it = specs.iterator();
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
        Iterator<Object> it = entities.iterator();
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
        Iterator<Object> mit = mcs.iterator();
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

    public static String getMatchCoupling(String aspNm) {
        NodeList nl = sesDoc.getElementsByTagName("aspect");
        for (int i = 0; i < nl.getLength(); i++) {
            Element asp = (Element) nl.item(i);
            String nm = asp.getAttribute("name");
            if (nm.equals(aspNm)
                    && !asp.getAttribute("coupling").equals("")) {
                return asp.getAttribute("coupling");
            }
        }
        return "";
    }

    public static void genericPESEntity(Element e, Element se, String path) {
        if (e == null || se == null) {
            return;
        }
        e.setAttribute("path", path);
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
            Element spec = pruneDoc.createElement(chNm);
            e.appendChild(spec);
            genericPESSpec(spec, path);
        }

        if (!aspects.isEmpty()) {
            String chNm = "aspectsOf" + se.getAttribute("name");
            Element aspof = pruneDoc.createElement(chNm);
            e.appendChild(aspof);
            String aspNm = selectFrom(aspects);

            Element asp = pruneDoc.createElement(aspNm);
            String coupling = getMatchCoupling(aspNm);
            asp.setAttribute("coupling", coupling);
            aspof.appendChild(asp);
            if (multiAspectNames.contains(asp.getNodeName())) {
                genericPESMultiAsp(asp, path);
            } else {
                genericPESAsp(asp, path + "." + chNm);
            }
        }
    }

    public static void genericPESSpec(Element spec, String path) {

        Element spe = SESOps.getElement("specialization", spec.getNodeName());
        if (spe == null) {
            return;
        }
        String nm = spe.getAttribute("name");
        LinkedList<Object> entities = SESOps.getChildrenOfElement(spe, "entity");
        if (entities.isEmpty()) {
            return;
        }
        Iterator it = entities.iterator();
        while (it.hasNext()) {
            String entNm = (String) it.next();
            Element ent = pruneDoc.createElement(entNm);
            spec.appendChild(ent);
            spec.setAttribute("path", path + "." + spec.getNodeName() + "."
                    + entNm);
            Element sen = SESOps.getElement("entity", entNm);
            genericPESEntity(ent, sen, path);
        }
    }

    public static Element createSafeElement(Document doc, String s) {
        Element ent = doc.createElement("InvalidXML");
        try {
            ent = doc.createElement(s);
        } catch (DOMException e) {
            System.out.println("InvalidXML" + s);
        }
        return ent;
    }

    public static void genericPESAsp(Element asp, String path) {

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
            Element ent = createSafeElement(pruneDoc, entNm);
            asp.appendChild(ent);
            Element sen = SESOps.getElement("entity", entNm);
            ent.setAttribute("path", path + "." + asp.getNodeName() + "."
                    + entNm);
            genericPESEntity(ent, sen, path + "." + asp.getNodeName() + "."
                    + entNm);
        }
    }

    public static void genericPESMultiAsp(Element asp, String path) {

        Element mult = SESOps.getElement("multiAspect", asp.getNodeName());

        LinkedList<Object> mcs = SESOps.getChildrenOfElement(mult, "entity");
        if (mcs.isEmpty()) {
            return;
        }
        Iterator mit = mcs.iterator();
        Element se = SESOps.getElement("entity", (String) mit.next());

        String cop = mult.getAttribute("coupling");

        mcs = getElementChildrenOfElement(mult, "numberComponentsVar");
        mit = mcs.iterator();
        Element ncv = (Element) mit.next();
        String ncvar = ncv.getAttribute("name");
        LinkedList<Object> entities = SESOps.getChildrenOfElement(mult, "entity");
        if (entities.isEmpty()) {
            return;
        }

        String entNm = (String) entities.getFirst();
        Element ent = pruneDoc.createElement(entNm);
        asp.appendChild(ent);
        Element sen = SESOps.getElement("entity", entNm);
        int NumEnts = 1; // genMultiAsp(ent, sen);
        asp.setAttribute(ncvar, "" + NumEnts);
        genericPESEntity(ent, sen, path);
    }

    public static int genMultiAsp(Element e, Element sen, String path) {
        LinkedList<Object> entities = new LinkedList<Object>();
        LinkedList<Object> specs = SESOps.getChildrenOf(sen.getAttribute("name"),
                "specialization");

        Iterator it = specs.iterator();
        if (it.hasNext()) {
            String chNm = (String) it.next();
            Element spe = SESOps.getElement("specialization", chNm);
            if (spe == null) {
                return 0;
            }
            Element spec = pruneDoc.createElement(chNm);
            e.appendChild(spec);
            entities = SESOps.getChildrenOfElement(spe, "entity");
            if (entities.isEmpty()) {
                return 0;
            }

            Iterator is = entities.iterator();
            while (is.hasNext()) {
                String entNm = (String) is.next();
                Element ent = pruneDoc.createElement(entNm);
                spec.appendChild(ent);
                Element se = SESOps.getElement("entity", entNm);
                genericPESEntity(ent, se, path);
            }
        }
        return entities.size();
    }

    public static void writeGenericPES(String xmlFile, String dtdFile) {
        createPesDoc(SESOps.sesRoot.getAttribute("name"));
        pruneRoot.setAttribute("path", pruneRoot.getNodeName());
        genericPESEntity(pruneRoot, SESOps.sesRoot, pruneRoot.getNodeName());
        writePruneDoc(xmlFile, dtdFile);
    }

    public static void writeGenericPESForSchema(String sesFile, String xmlFile,
            String schFile) {
        restoreSesDoc(sesFile);
        writeGenericPESForSchema(xmlFile, schFile);
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
        writePruneSchemaDoc(xmlFile, schFile);
    }

    public static void selectBasedOnRoot(String rootNm, Element spec) {
        Element selent = null;
        LinkedList<Object> chds = getActualChildren(spec);
        if (chds.size() > 0) {
            Iterator it = chds.iterator();
            while (it.hasNext()) {
                Element ch = (Element) it.next();
                if (rootNm.startsWith(ch.getNodeName())) {
                    selent = ch;
                }
            }
            if (selent == null) {
                return;
            }
            selent = (Element) selent.cloneNode(true);
            it = chds.iterator();
            while (it.hasNext()) {
                Element ch = (Element) it.next();
                spec.removeChild(ch);
            }
            spec.appendChild(selent);
        }
    }

    public static void selectEntityFromSpec(Element spec) {
        LinkedList<Object> chds = getActualChildren(spec);
        Element ent = (Element) chds.get(r.nextInt(chds.size()));
        String entNm = ent.getNodeName();
        selectEntityFromSpec(spec, entNm);
    }

    public static void selectEntityFromSpec(Element spec, String entNm) {
        LinkedList<Object> chds = getActualChildren(spec);
        Iterator it = chds.iterator();
        while (it.hasNext()) {
            Element ch = (Element) it.next();
            if (!ch.getNodeName().equals(entNm)) {
                spec.removeChild(ch);
            }
        }
    }

    public static HashSet<Object> isFirstInRelWith(sesRelation ses, Element spec,
            LinkedList<Object> specs) {
        HashSet<Object> es = new HashSet<Object>();
        String specIncoming = spec.getNodeName();
        Iterator it = specs.iterator();
        while (it.hasNext()) {
            Element spec2 = (Element) it.next();
            String specSecond = spec2.getNodeName();
            restrictRelation r = ses.findRestrictRel(specIncoming, specSecond);
            if (r != null) {
                es.add(spec2);
            }
        }
        return es;
    }

    public static void doMultRestriction(LinkedList<Object> specs, sesRelation ses,
            Element spec, Element root) {
        HashSet<Object> allowedVals = new HashSet<Object>();
        HashSet<Object> es = isFirstInRelWith(ses, spec, specs);
        if (es.isEmpty()) {
            return;
        }

        restrictRelation r;
        String specIncoming = spec.getNodeName();
        LinkedList<Object> chds = getActualChildren(spec);
        Iterator it = specs.iterator();
        while (it.hasNext()) {
            Element spec2 = (Element) it.next();
            String specSecond = spec2.getNodeName();
            r = ses.findRestrictRel(specIncoming, specSecond);
            if (r != null) {
                Iterator ic = chds.iterator();
                while (ic.hasNext()) {
                    Element ent = (Element) ic.next();
                    String entNm = ent.getNodeName();
                    allowedVals.addAll(r.get(entNm));
                }
                restrictSpecEntities(allowedVals, spec2);
            }
        }
    }

    public static void restrictSpecEntities(HashSet<Object> allowedVals,
            Element spec) {
        LinkedList<Object> chds = getActualChildren(spec);
        Iterator it = chds.iterator();
        while (it.hasNext()) {
            Element nc = (Element) it.next();
            String enm = nc.getNodeName();
            if (!allowedVals.contains(enm)) {
                spec.removeChild(nc);
            }
        }
    }

    public static void pruneWRelation(String sesFile, String relationFile,
            String pesFile) {
        sesRelation ses = new sesRelation(sesFile);
        pes p = new pes(relationFile);
        ses.setRelations(p);
        pruneOps.restorePruneDoc(pesFile);
        pruneWRelation(ses);
    }

    public static void pruneWRelation(sesRelation ses) {
        LinkedList<Object> specsInOrder = ses.placeInOrder();
        String[] order = new String[specsInOrder.size()];
        for (int i = 0; i < specsInOrder.size(); i++) {
            order[i] = (String) specsInOrder.get(i);
        }
        pruneWExpandRelation(ses, order,
                pruneOps.pruneRoot.getAttribute("name"), "");
        pruneOps.pruneRoot = null;
    }

    public static void pruneWRelation(sesRelation ses, String[] order) {
        pruneWExpandRelation(ses, order,
                pruneOps.pruneRoot.getAttribute("name"), "");
    }

    public static void pruneWExpandRelation(sesRelation ses,
            String entToExpand, String folder) {

        LinkedList<Object> specsInOrder = ses.placeInOrder();
        String[] order = new String[specsInOrder.size()];
        for (int i = 0; i < specsInOrder.size(); i++) {
            order[i] = (String) specsInOrder.get(i);
        }
        pruneWExpandRelation(ses, order, entToExpand, folder);
    }

    public static void pruneWExpandRelation(sesRelation ses, String[] order,
            String entToExpand, String folder) {
        LinkedList<Object> specentNms = new LinkedList<Object>();
        if (pruneRoot != null) {
            specentNms.add(pruneRoot.getNodeName());
        }
        if (!folder.equals("")) {
            specentNms = sesToGenericSchema.expandAllMultAspNWriteSimpleSchema(
                    entToExpand, folder);

            generatePrunings.writeGenericPESForSchema(folder + "generic"
                    + entToExpand + "ExpandInstance.xml", entToExpand
                    + "Expand.xsd");
        }
        Iterator it = specentNms.iterator();
        while (it.hasNext()) {
            String rootNm = (String) it.next();
            Element root = getPruneElement(rootNm);
            LinkedList<Object> specs = allSpecsFrom(root);
            specs = orderBy(specs, order);
            if (specs.isEmpty()) {
                System.out.println("No specializations to prune");
                return;
            }
            Element corsp = (Element) specs.getFirst();
            pruneWExpandRelation(root, ses, corsp, specs);
        }
        if (!folder.equals("")) {
            writePruneSchemaDoc(folder + "pruned" + entToExpand
                    + "ExpandInstance.xml", entToExpand + "Expand.xsd");
        }
    }

    public static void pruneWExpandRelation(Element root, sesRelation ses,
            Element corrSpec, LinkedList<Object> specs) {
        String rootNm = root.getNodeName();
        if (!specs.isEmpty()) {
            Iterator it = specs.iterator();
            while (it.hasNext()) {
                Element ch = (Element) it.next();
                String chNm = ch.getNodeName();
                if (chNm.endsWith("Spec") || chNm.endsWith("Type")) {
                    selectBasedOnRoot(rootNm, corrSpec);
                    LinkedList<Object> ents = getActualChildren(ch);
                    if (ents.size() > 1) {
                        javax.swing.JPanel pan = new javax.swing.JPanel();
                        javax.swing.JOptionPane p = new javax.swing.JOptionPane();
                        int res = JOptionPane.showConfirmDialog(
                                pan,
                                "Do selection From ?"
                                + ents
                                + " of "
                                + chNm
                                + " under "
                                + rootNm
                                + " [No =  leave as already restricted]");
                        if (JOptionPane.YES_OPTION == res) {
                            pan = new javax.swing.JPanel();
                            p = new javax.swing.JOptionPane();
                            String response = JOptionPane.showInputDialog(pan,
                                    "select one of above or random");
                            if (response.equals("random")) {
                                selectEntityFromSpec(ch);
                            } else {
                                selectEntityFromSpec(ch, response);
                            }
                        }
                    }
                    doMultRestriction(specs, ses, ch, root);
                }
            }
        }
    }

    public static LinkedList<Object> orderBy(LinkedList<Object> q, String[] sequence) {
        LinkedList<Object> res = new LinkedList<Object>();
        for (int i = 0; i < sequence.length; i++) {
            Iterator it = q.iterator();
            while (it.hasNext()) {
                Element el = (Element) it.next();
                String nm = el.getNodeName();
                if (nm.startsWith(sequence[i])) {
                    res.add(el);
                    q.remove(el);
                    break;
                }
            }
        }
        res.addAll(q);
        return res;
    }

    //////////////////////////////////////// new for context based pruning

    public static void genericPESEntityFull(Element e, Element se, String path) {
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
            Element spec = pruneDoc.createElement(chNm);
            e.appendChild(spec);
            genericPESSpecFull(spec, path);
        }

        if (!aspects.isEmpty()) {
            String chNm = "aspectsOf" + se.getAttribute("name");
            Element aspof = pruneDoc.createElement(chNm);
            e.appendChild(aspof);
            for (Object o : aspects) {
                String aspNm = o.toString();
                Element asp = pruneDoc.createElement(aspNm);
                String coupling = getMatchCoupling(aspNm);
                asp.setAttribute("coupling", coupling);
                aspof.appendChild(asp);

                if (multiAspectNames.contains(asp.getNodeName())) {
                    genericPESMultiAspFull(asp, path);
                } else {
                    genericPESAspFull(asp, path + "." + chNm);
                }
            }
        }
    }

    public static void genericPESAspFull(Element asp, String path) {
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
            Element ent = createSafeElement(pruneDoc, entNm);
            asp.appendChild(ent);
            Element sen = SESOps.getElement("entity", entNm);
            ent.setAttribute("path", path + "." + asp.getNodeName() + "."
                    + entNm);
            genericPESEntityFull(ent, sen, path + "." + asp.getNodeName() + "."
                    + entNm);
        }
    }

    public static void genericPESMultiAspFull(Element asp, String path) {
        Element mult = SESOps.getElement("multiAspect", asp.getNodeName());
        
        LinkedList<Object> mcs = SESOps.getChildrenOfElement(mult, "entity");
        if (mcs.isEmpty()) {
            return;
        }
        Iterator mit = mcs.iterator();
        Element se = SESOps.getElement("entity", (String) mit.next());

        String cop = mult.getAttribute("coupling");

        mcs = getElementChildrenOfElement(mult, "numberComponentsVar");
        if (mcs.isEmpty()) {
            return;
        }
        mit = mcs.iterator();
        Element ncv = (Element) mit.next();
        String ncvar = ncv.getAttribute("name");
        LinkedList<Object> entities = SESOps.getChildrenOfElement(mult, "entity");
        if (entities.isEmpty()) {
            return;
        }

        String entNm = (String) entities.getFirst();
        Element ent = pruneDoc.createElement(entNm);
        asp.appendChild(ent);
        Element sen = SESOps.getElement("entity", entNm);
        int NumEnts = 1; // genMultiAsp(ent, sen);
        asp.setAttribute(ncvar, "" + NumEnts);
        genericPESEntityFull(ent, sen, path);
    }

    public static void genericPESSpecFull(Element spec, String path) {
        Element spe = SESOps.getElement("specialization", spec.getNodeName());
        if (spe == null) {
            return;
        }
        String nm = spe.getAttribute("name");
        LinkedList<Object> entities = SESOps.getChildrenOfElement(spe, "entity");
        if (entities.isEmpty()) {
            return;
        }
        Iterator it = entities.iterator();
        while (it.hasNext()) {
            String entNm = (String) it.next();
            Element ent = pruneDoc.createElement(entNm);
            spec.appendChild(ent);
            spec.setAttribute("path", path + "." + spec.getNodeName());
            Element sen = SESOps.getElement("entity", entNm);
            genericPESEntityFull(ent, sen, path + "." + entNm);
        }
    }

    public static Element selectNGetEntityFromSpec(Element spec, String entNm) {
        Element res = null;
        LinkedList<Object> chds = getActualChildren(spec);
        if (chds == null) {
            return null;
        }
        if (chds.size() == 1) {
            return (Element) chds.getFirst();
        }
        Iterator it = chds.iterator();
        while (it.hasNext()) {
            Element ch = (Element) it.next();
            if (!ch.getNodeName().equals(entNm)) {
                spec.removeChild(ch);
            } else {
                res = ch;
            }
        }
        return res;
    }

    public static Element selectNGetEntityFromSpec(Element spec) {
        LinkedList<Object> chds = getActualChildren(spec);
        return selectElementFrom(chds);
    }

    public static LinkedList<Object> getSpecializationElementsOfEntity(Element ent) {
        LinkedList<Object> res = new LinkedList<Object>();
        LinkedList<Object> chds = getActualChildren(ent);
        Iterator it = chds.iterator();
        while (it.hasNext()) {
            Element ch = (Element) it.next();
            if (ch.getNodeName().endsWith("Spec")) {
                res.add(ch);
            }
        }
        return res;
    }

    public static boolean checkSpecIsLike(String specNm, String elSpecNm) {
        int ind = specNm.lastIndexOf("-");
        if (ind < 0) {
            return false;
        }
        String specPt = specNm.substring(ind);
        int ind2 = elSpecNm.lastIndexOf("-");
        if (ind2 < 0) {
            return false;
        }
        String elspecPt = elSpecNm.substring(ind2);
        return specPt.equals(elspecPt);
    }

    public static boolean checkAspIsLike(String specNm, String elSpecNm) {
        if (specNm.equals(elSpecNm)) {
            return true;
        }

        int ind = specNm.lastIndexOf("-");
        if (ind < 0) {
            return false;
        }
        String specPt = specNm.substring(ind + 1);
        specPt = specPt.substring(0, specPt.length() - "Dec".length());
        return specPt.equals(elSpecNm);
    }

    public static Element getSpecializationOfEntity(String specNm, Element el) {
        LinkedList<Object> specs = getSpecializationElementsOfEntity(el);
        Iterator it = specs.iterator();
        while (it.hasNext()) {
            Element spec = (Element) it.next();
            if (checkSpecIsLike(spec.getNodeName(), specNm)) {
                return spec;
            }
        }
        return null;
    }

    public static LinkedList<Object> getAspectElementsOfEntity(Element ent) {
        Element aspsof = getAspectsOf(ent);
        if (aspsof == null) {
            return new LinkedList<Object>();
        }
        return getActualChildren(aspsof);
    }

    public static Element selectNGetAspectFromEntity(String aspNm, Element ent) {
        Element asp = null;
        Element aspsof = getAspectsOf(ent);
        LinkedList<Object> chds = getActualChildren(aspsof);
        if (chds.size() == 1) {
            return (Element) chds.getFirst();
        }
        Iterator it = chds.iterator();
        while (it.hasNext()) {
            Element ch = (Element) it.next();
            if (checkAspIsLike(ch.getNodeName(), aspNm)) {
                asp = ch;
            } else {
                aspsof.removeChild(ch);
            }
        }
        return asp;
    }

    public static Element selectElementFrom(LinkedList<Object> q) {
        if (q.isEmpty()) {
            return null;
        }
        if (q.size() == 1) {
            return (Element) q.getFirst();
        }
        // get the total number and make new list
        ArrayList<String> specList = new ArrayList<String>();
        Iterator<Object> it = q.iterator();
        while(it.hasNext()){
        	Element el = (Element)it.next();
        	String name = el.getNodeName();
        	String parentName = el.getParentNode().getNodeName();
        	int ind = parentName.indexOf("-");
        	String specName = parentName.substring(ind+1,parentName.length()-4);
        	String key = name+"-"+specName;
        	if(sesRelationExtend.multNumtoEnt.containsKey(key)){
        		int numRep = (Integer)sesRelationExtend.multNumtoEnt.get(key);
        		for(int i=0; i< numRep; i++){
        			specList.add(name);
        		}
        	}else{
        		specList.add(name);
        	}
        }
        String entName = specList.get(r.nextInt(specList.size()));
        for(int i = 0; i < q.size(); i++){
        	Element entEl = (Element)q.get(i);
        	if(entEl.getNodeName().equals(entName)){
        		return entEl;
        	}        	
        }
        return null;
    }
    // /////////////////

    public static void main(String argv[]) {
        String x = "aa[bb";
        HashSet<Object> es = new HashSet<Object>();
        es.add("[");
        es.add("[]");
        Iterator it = es.iterator();
        while (it.hasNext()) {
            String s = (String) it.next();
            if (x.contains(s)) {
                int ind = x.indexOf(s);
                x = x.substring(0, ind) + "_"
                        + x.substring(ind + 1, x.length());
            }
        }
        System.out.println(x);
        System.exit(3);
    } // main
}
