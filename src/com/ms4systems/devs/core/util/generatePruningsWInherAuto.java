package com.ms4systems.devs.core.util;

import org.w3c.dom.*;
import java.util.*;

public class generatePruningsWInherAuto extends generatePruningsWInher {

    public static void doInherit() {
   //     GenCol.Queue q  = getActualChildren(pruneRoot);
     doInheritFrom(pruneRoot, pruneRoot.getNodeName(), "", pruneRoot.getNodeName());//pruneRoot.getNodeName());
      //   q  = getActualChildren(pruneRoot);
     if (newRoot != null) {
            changeRoot(pruneRoot, newRoot, newPrefix);
        }
      //   q  = getActualChildren(pruneRoot);
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
        Iterator<Object> it = es.iterator();
        boolean someSpecs = false;
        while (it.hasNext()) {
            Element ch = (Element) it.next();
            String chNm = ch.getNodeName();
            if (chNm.endsWith("Spec") || chNm.endsWith("Type")) {
                // if (!specNamesCopied.contains(chNm)) {
                //    if (prefix.equals(""))
                doInheritFrom(ch, e, topEntNm, prefix, path);//e.getNodeName());
                someSpecs = true;
                // else
                //     doInheritFrom(ch, e, topEntNm,e.getNodeName() + "_" + prefix);
                specNamesCopied.add(chNm);
            // }
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
            Iterator<Object> itt = et.iterator();
            while (itt.hasNext()) {
                Element gch = (Element) itt.next();
                String gchNm = gch.getNodeName();
                if (gchNm.endsWith("Asp") || gchNm.endsWith("Dec")) {
                    LinkedList<Object> es = getActualChildren(gch);
                    Iterator<Object> is = es.iterator();
                    while (is.hasNext()) {
                        Element ent = (Element) is.next();
                        doInheritFrom(ent, ent.getNodeName(), "",
                                path + "." + "aspectsOf" + lastEntNm + "." + gchNm + "." + ent.getNodeName());
                    }
                }
            }
        }
    }

 
    public static void doInheritFrom(Element sp, Element e, String topEntNm, String prefix, String path) {
        String pathname = path + "." + sp.getNodeName();
        LinkedList<Object> entities = getActualChildren(sp);
        if (entities.isEmpty()) {
            return;
        }

        Iterator<Object> it = entities.iterator();
        while (it.hasNext()) {
             Element ent = (Element) it.next();          
                copyOtherSpecsFromTo(sp.getNodeName(), e, ent);
                selectEntityFromSpec(sp, ent.getNodeName());
                String newEntName = ent.getNodeName();
                if (!prefix.equals("")) {
                    newEntName = newEntName + "_" + prefix;
                }
                doInheritFromTo(sp, ent, e, newEntName);
                doInheritFrom(ent, topEntNm, newEntName, path);
                // doInheritFromTo(sp, ent, e, ent.getNodeName() + "_" + prefix);
                // doInheritFrom(ent, ent.getNodeName() + "_" + prefix);
               removeAllAspects(e);
                removeAllAttributes(e);
                // removeAllCopiedSpecs(e);

                return;
            }
    }


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
    //}
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
        genericPESEntity(pruneRoot, rootNode,pruneRoot.getNodeName());
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
        q = orderBy(q, seq);
        System.out.println(q);
        System.exit(3);
    } // main
}

