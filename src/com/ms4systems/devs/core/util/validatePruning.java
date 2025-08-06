package com.ms4systems.devs.core.util;

import org.w3c.dom.*;
import java.util.*;
import java.util.regex.Pattern;

public class validatePruning extends pruneOps {	
	
    public static LinkedList<Object> getAttributes(Element e) {
        LinkedList<Object> sh = new LinkedList<Object>();
        NamedNodeMap m = e.getAttributes();

        if (m == null) {
            return sh;
        }
        for (int i = 0; i < m.getLength(); i++) {
            Attr n = (Attr) m.item(i);
            sh.add(n.getName());
        }
        return sh;
    }


    public static Element findMatch(Element na, LinkedList<Object> b) {
        Iterator it = b.iterator();
        while (it.hasNext()) {
            Element se = (Element) it.next();
            if (na.getNodeName().equals(se.getAttribute("name"))) {
                return se;
            }
        }
        return null;
    }

    public static Hashtable<Object,Object> makeCorrespondence(LinkedList<Object> a, LinkedList<Object> b) {
        Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        Iterator it = a.iterator();
        while (it.hasNext()) {
            Element na = (Element) it.next();
            Element nb = findMatch(na, b);
            if (nb == null) {
                return f;
            }
            f.put(na, nb);
        }
        return f;
    }

    public static HashSet<Object> queueToEnsembleSet(LinkedList<Object> names) {
        HashSet<Object> s = new HashSet<Object>();
        Iterator it = names.iterator();
        while (it.hasNext()) {
            s.add(it.next());
        }
        return s;
    }

    public static boolean checkValidBrothers(LinkedList<Object> names) {
        return names.size() == queueToEnsembleSet(names).size();
    }

    public static boolean validateEntity(Element e, Element se) {
        if (!e.getNodeName().equals(se.getAttribute("name"))) {
            System.out.println("PES and SES entity don't agree " +
                               e.getNodeName() + "!=" +
                               se.getAttribute("name"));
            return false;
        }

        LinkedList<Object> sesAttrs = SESOps.getChildrenOf(se.getAttribute("name"),
                                              "var");
        LinkedList<Object> pesAttrs = getAttributes(e);
        pesAttrs.remove("xmlns:xsi");
        pesAttrs.remove("xsi:noNamespaceSchemaLocation");
        if (!(sesAttrs.containsAll(pesAttrs))) {
            System.out.println(
                    " ses entity vars: " + sesAttrs +
                    " do not contain pruned entity attributes: " + pesAttrs
                    );

            return false;
        }
        if (!checkAttributes(e, se)) {
            return false;
        }
        LinkedList<Object> aspects = SESOps.getChildrenOf(se.getAttribute("name"),
                                             "aspect");
        aspects.addAll(SESOps.getChildrenOf(se.getAttribute("name"),
                                            "multiAspect"));
        LinkedList<Object> specs = SESOps.getChildrenOf(se.getAttribute("name"),
                                           "specialization");

        LinkedList<Object> es = getActualChildren(e);

        int numSpec = 0;
        int numAspOf = 0;
        LinkedList<Object> pruneSpecs = new LinkedList<Object>();
        LinkedList<Object> pruneAsps = new LinkedList<Object>();
        if (!es.isEmpty()) {
            Iterator it = es.iterator();
            while (it.hasNext()) {
                Element ch = (Element) it.next();
                String chNm = ch.getNodeName();
                if (chNm.endsWith("Spec") || chNm.endsWith("Type")) {
                    pruneSpecs.add(chNm);
                    numSpec++;
                    if (!validateSpec(ch, specs, se)) {
                        return false;
                    }
                } else if (chNm.startsWith("aspectsOf")) {
                    numAspOf++;
                    LinkedList<Object> et = getActualChildren(ch);
                    if (!et.isEmpty()) {
                        Iterator itt = et.iterator();
                        while (itt.hasNext()) {
                            Element gch = (Element) itt.next();

                            pruneAsps.add(gch.getNodeName());
                            if (!validateAsp(gch, aspects, se)) {
                                return false;
                            }
                        }
                    }

                    if (!checkValidBrothers(pruneAsps)) {
                        System.out.println("valid brothers axiom of " +
                                           se.getAttribute("name") +
                                           " is not satisfied");

                        return false;
                    }

                    if (et.size() != 1) {
                        System.out.println(e.getNodeName() +
                                           " does not have 1 aspect ");
                        return false;
                    }
                }
            }
        }

        if (!checkValidBrothers(pruneSpecs)) {
            System.out.println("valid brothers axiom of " +
                               se.getAttribute("name") +
                               " is not satisfied");

            return false;
        }

        if (specs.size() > numSpec) {
            System.out.println(
                    "too small a number " + numSpec +
                    " of specializations " +
                    specs + " of " + se.getAttribute("name") + " selected");
            return false;
        }

        if (aspects.size() > 0 && numAspOf != 1) {
            System.out.println(
                    se.getAttribute("name") + " does not have 1 aspectsOf ");
            return false;
        }

        return true;
    }

    public static boolean validateSpec(Element sp, LinkedList<Object> specs, Element se) {
        if (!specs.contains(sp.getNodeName())) {
            System.out.println("specializations " + specs + " of " +
                               se.getAttribute("name") +
                               " does not contain "
                               + sp.getNodeName());
            return false;
        }

        LinkedList<Object> es = getActualChildren(sp);
        if (es.size() != 1) {
            System.out.println(sp.getNodeName() +
                               " does not have 1 choice ");
            return false;
        }

        Element ent = (Element) es.getFirst();
        Element spe = SESOps.getElement("specialization", sp.getNodeName());
        LinkedList<Object> entities = SESOps.getChildrenOfElement(spe, "entity");
        if (!entities.contains(ent.getNodeName())) {
            System.out.println("entities " + entities +
                               " does not contain "
                               + ent.getNodeName());
            return false;
        }
        Element sen = SESOps.getElement("entity", ent.getNodeName());
        return validateEntity(ent, sen);
    }

    public static boolean validateAsp(Element asp, LinkedList<Object> aspects,
                                      Element sem) {
        if (!aspects.contains(asp.getNodeName())) {
            System.out.println("aspects" + aspects +
                               " of " + sem.getAttribute("name") +
                               " does not contain "
                               + asp.getNodeName());
            return false;
        }

        Element as = SESOps.getElement("aspect", asp.getNodeName());
        if (as != null) {
            LinkedList<Object> entities = SESOps.getElementChildrenOfElement(as,
                    "entity");
            LinkedList<Object> es = getActualChildren(asp);
            if (entities.size() != es.size()) {
                System.out.println("number of entities of " +
                                   asp.getNodeName() +
                                   " not equal in SES and PES ");
                return false;
            }

            if (!es.isEmpty()) {
                Hashtable<Object,Object> f = makeCorrespondence(es, entities);
                if (f.size() != entities.size()) {
                    System.out.println(
                            "can't make correspondance of entities in" +
                            asp.getNodeName());
                    return false;
                } else {
                	Enumeration<Object> e = f.keys();
                	while(e.hasMoreElements()){
                		Object key = e.nextElement();
                		Object val = f.get(key);
                		Element el = (Element) key;
                        Element sen = (Element) val;
                        if (!validateEntity(el, sen)) {
                            return false;
                        }
                	}                    
                }
            }
            return true;
        }
        as = SESOps.getElement("multiAspect", asp.getNodeName());
        if (as == null) {
            System.out.println("there is no aspect or multiAspect" +
                               " of " + sem.getAttribute("name") + ": " +
                               asp.getNodeName());
            return false;
        }

        LinkedList<Object> entities = SESOps.getElementChildrenOfElement(as, "entity");

        LinkedList<Object> es = getActualChildren(asp);
        if (!es.isEmpty() && !sameNames(es)) {
            System.out.println("not all entities of " + asp.getNodeName() +
                               " have the same name ");
            return false;
        }
        if (multiAspectNames.contains(asp.getNodeName())) {
            Element mult = SESOps.getElement("multiAspect", asp.getNodeName());

            LinkedList<Object> mcs = SESOps.getChildrenOfElement(mult, "entity");
            Iterator mit = mcs.iterator();
            Element se = SESOps.getElement("entity", (String) mit.next());

            String cop = mult.getAttribute("coupling");

            mcs = getElementChildrenOfElement(mult, "numberComponentsVar");
            mit = mcs.iterator();
            Element ncv = (Element) mit.next();
            String ncvar = ncv.getAttribute("name");
            String numEnts = asp.getAttribute(ncvar);
            if (numEnts.equals("")) {
                numEnts = "0";
            }
            int NumEnts = Integer.parseInt(numEnts);
            String min = ncv.getAttribute("min");
            int Min = 0;
            if (!min.equals("") && !min.equals("unknown")) {
                Min = Integer.parseInt(min);
            }
            String max = ncv.getAttribute("max");
            int Max = 0;
            if (!max.equals("") && !max.equals("unknown")) {
                Max = Integer.parseInt(max);
            }
            if (NumEnts != es.size()) {
                System.out.println("actual number of entities of " +
                                   asp.getNodeName() +
                                   " not equal to " + NumEnts);
                return false;
            }
            if (NumEnts < Min || NumEnts > Max) {
                System.out.println("number of entities of " +
                                   asp.getNodeName() +
                                   " not in allowed range ");
                return false;
            }
            Iterator is = es.iterator();
            while (is.hasNext()) {
                Element e = (Element) is.next();
                if (!validateEntity(e, se)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean validate(String SESxmlFile, String PESxmlFile) {
        restoreSesDoc(SESxmlFile);
        restorePruneDoc(PESxmlFile);
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
        return validateEntity(pruneRoot, rootNode);
    }

    public static boolean validate() {
        return validateEntity(pruneRoot, SESOps.sesRoot);
    }

    public static boolean validate(sesRelation ses) {
        if (checkRules(ses)) {
            return validateEntity(pruneRoot, SESOps.sesRoot);
        } else {
            return false;
        }
    }

///////////////////
    public static boolean conformsTo() {
        return conformsToEntity(pruneRoot, SESOps.sesRoot);
    }

    public static boolean conformsTo(sesRelation ses) {
        if (checkRules(ses)) {
            return conformsToEntity(pruneRoot, SESOps.sesRoot);
        } else {
            return false;
        }
    }

    public static boolean conformsTo(String PESxmlFile, String SESxmlFile) {
        restoreSesDoc(SESxmlFile);
        restorePruneDoc(PESxmlFile);
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
        return conformsToEntity(pruneRoot, rootNode);
    }

    public static boolean conformsToEntity(Element e, Element se) {
        if (!e.getNodeName().equals(se.getAttribute("name"))) {
            System.out.println("PES and SES entity don't agree " +
                               e.getNodeName() + "!=" +
                               se.getAttribute("name"));
            return false;
        }

        LinkedList<Object> sesAttrs = SESOps.getChildrenOf(se.getAttribute("name"),
                                              "var");
        LinkedList<Object> pesAttrs = getAttributes(e);
        pesAttrs.remove("xmlns:xsi");
        pesAttrs.remove("xsi:noNamespaceSchemaLocation");
        if (!(sesAttrs.containsAll(pesAttrs))) {
            System.out.println(
                    " ses entity vars: " + sesAttrs +
                    " do not contain pruned entity attributes: " + pesAttrs
                    );

            return false;
        }
        if (!checkAttributes(e, se)) {
            return false;
        }
        LinkedList<Object> aspects = SESOps.getChildrenOf(se.getAttribute("name"),
                                             "aspect");
        aspects.addAll(SESOps.getChildrenOf(se.getAttribute("name"),
                                            "multiAspect"));
        LinkedList<Object> specs = SESOps.getChildrenOf(se.getAttribute("name"),
                                           "specialization");
        LinkedList<Object> es = getActualChildren(e);

        int numSpec = 0;
        int numAspOf = 0;
        LinkedList<Object> pruneSpecs = new LinkedList<Object>();
        LinkedList<Object> pruneAsps = new LinkedList<Object>();
        if (!es.isEmpty()) {
            Iterator it = es.iterator();
            while (it.hasNext()) {
                Element ch = (Element) it.next();
                String chNm = ch.getNodeName();
                if (chNm.endsWith("Spec") || chNm.endsWith("Type")) {
                    pruneSpecs.add(chNm);
                    numSpec++;
                    if (!conformsToSpec(ch, specs, se)) {
                        return false;
                    }
                } else if (chNm.startsWith("aspectsOf")) {
                    numAspOf++;
                    LinkedList<Object> et = getActualChildren(ch);
                    if (!et.isEmpty()) {
                        Iterator itt = et.iterator();
                        while (itt.hasNext()) {
                            Element gch = (Element) itt.next();

                            pruneAsps.add(gch.getNodeName());
                            if (!conformsToAsp(gch, aspects, se)) {
                                return false;
                            }
                        }
                    }

                    if (!checkValidBrothers(pruneAsps)) {
                        System.out.println("valid brothers axiom of " +
                                           se.getAttribute("name") +
                                           " is not satisfied");

                        return false;
                    }

                    if (et.size() == 0) {
                        System.out.println(e.getNodeName() +
                                           " bas no aspects ");
                        return false;
                    }
                }
            }
        }

        if (!checkValidBrothers(pruneSpecs)) {
            System.out.println("valid brothers axiom of " +
                               se.getAttribute("name") +
                               " is not satisfied");

            return false;
        }

        if (specs.size() > numSpec) {
            System.out.println(
                    "too small a number " + numSpec +
                    " of specializations " +
                    specs + " of " + se.getAttribute("name") + " selected");
            return false;
        }

        if (aspects.size() > 0 && numAspOf != 1) {
            System.out.println(
                    se.getAttribute("name") + " does not have 1 aspectsOf ");
            return false;
        }

        return true;
    }

    public static boolean conformsToSpec(Element sp, LinkedList<Object> specs,
                                         Element se) {
        if (!specs.contains(sp.getNodeName())) {
            System.out.println("specializations " + specs + " of " +
                               se.getAttribute("name") +
                               " does not contain "
                               + sp.getNodeName());
            return false;
        }

        LinkedList<Object> es = getActualChildren(sp);
        if (es.size() == 0) {
            System.out.println(sp.getNodeName() + " is over-pruned ");
            return false;
        }
        Element spe = SESOps.getElement("specialization", sp.getNodeName());
        LinkedList<Object> entities = SESOps.getChildrenOfElement(spe, "entity");
        LinkedList<Object> names = new LinkedList<Object>();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Element ent = (Element) it.next();
            String entNm = ent.getNodeName();
            names.add(entNm);
            if (!entities.contains(entNm)) {
                System.out.println("entities " + entities + " of " +
                                   sp.getNodeName() +
                                   " do not contain "
                                   + entNm);
                return false;
            }
            Element sen = SESOps.getElement("entity", entNm);
            if (!conformsToEntity(ent, sen)) {
                return false;
            }
        }
        if (!checkValidBrothers(names)) {
            System.out.println("valid brothers axiom of " +
                               sp.getNodeName() +
                               " is not satisfied");
            return false;
        }

        return true;
    }

    public static boolean conformsToAsp(Element asp, LinkedList<Object> aspects,
                                        Element sem) {
        if (!aspects.contains(asp.getNodeName())) {
            System.out.println("aspects" + aspects +
                               " of " + sem.getAttribute("name") +
                               " does not contain "
                               + asp.getNodeName());
            return false;
        }

        Element as = SESOps.getElement("aspect", asp.getNodeName());
        if (as != null) {
            LinkedList<Object> entities = SESOps.getElementChildrenOfElement(as,
                    "entity");
            LinkedList<Object> es = getActualChildren(asp);
            if (entities.size() != es.size()) {
                System.out.println("number of entities of " +
                                   asp.getNodeName() +
                                   " not equal in SES and PES ");
                return false;
            }

            if (!es.isEmpty()) {
                Hashtable<Object,Object> f = makeCorrespondence(es, entities);
                if (f.size() != entities.size()) {
                    System.out.println(
                            "can't make correspondance of entities in" +
                            asp.getNodeName());
                    return false;
                } else {
                	Enumeration<Object> e = f.keys();
                	while(e.hasMoreElements()){
                		Object key = e.nextElement();
                		Object val = f.get(key);
                		Element el = (Element) key;
                        Element sen = (Element) val;
                        if (!conformsToEntity(el, sen)) {
                            return false;
                        }
                	}
                    
                }
            }
            return true;
        }
        as = SESOps.getElement("multiAspect", asp.getNodeName());
        if (as == null) {
            System.out.println("there is no aspect or multiAspect" +
                               " of " + sem.getAttribute("name") + ": " +
                               asp.getNodeName());
            return false;
        }

        LinkedList<Object> entities = SESOps.getElementChildrenOfElement(as, "entity");

        LinkedList<Object> es = getActualChildren(asp);
        if (!es.isEmpty() && !sameNames(es)) {
            System.out.println("not all entities of " + asp.getNodeName() +
                               " have the same name ");
            return false;
        }
        if (multiAspectNames.contains(asp.getNodeName())) {
            Element mult = SESOps.getElement("multiAspect", asp.getNodeName());

            LinkedList<Object> mcs = SESOps.getChildrenOfElement(mult, "entity");
            Iterator mit = mcs.iterator();
            Element se = SESOps.getElement("entity", (String) mit.next());

            String cop = mult.getAttribute("coupling");

            mcs = getElementChildrenOfElement(mult, "numberComponentsVar");
            if (mcs.isEmpty()) {
                return true;
            }
            mit = mcs.iterator();
            Element ncv = (Element) mit.next();
            String ncvar = ncv.getAttribute("name");
            String numEnts = asp.getAttribute(ncvar);
            if (numEnts.equals("")) {
                numEnts = "0";
            }
            int NumEnts = Integer.parseInt(numEnts);
            String min = ncv.getAttribute("min");
            int Min = 0;
            if (!min.equals("") && !min.equals("unknown")) {
                Min = Integer.parseInt(min);
            }
            String max = ncv.getAttribute("max");
            int Max = 0;
            if (!max.equals("") && !max.equals("unknown")) {
                Max = Integer.parseInt(max);
            }
            if (NumEnts != es.size()) {
                System.out.println("actual number of entities of " +
                                   asp.getNodeName() +
                                   " not equal to " + NumEnts);
                return false;
            }
            if (NumEnts < Min || NumEnts > Max) {
                System.out.println("number of entities of " +
                                   asp.getNodeName() +
                                   " not in allowed range ");
                return false;
            }
            Iterator is = es.iterator();
            while (is.hasNext()) {
                Element e = (Element) is.next();
                if (!conformsToEntity(e, se)) {
                    return false;
                }
            }
        }
        return true;
    }

/////////////////////////
    public static boolean checkAttributes(Element e, Element se) {
        NamedNodeMap m = e.getAttributes();
        if (m == null) {
            return true;
        }
        for (int i = 0; i < m.getLength(); i++) {
            Attr n = (Attr) m.item(i);
            String name = n.getName();
            String val = n.getValue();
            Element var = SESOps.getChildElement(se, "var", name);
            if (var == null) {
                continue;
            }
            String range = var.getAttribute("rangeSpec");
            String varNm = var.getAttribute("name");
            if (!checkSimpleReference(varNm, val,range)) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkSimpleReference(String varNm, String val,String range) {
        NodeList nl = sesDoc.getElementsByTagName("var");
        Element simpleRef = null;
        for (int i = 0; i < nl.getLength(); i++) {
        	Element var = (Element) nl.item(i);
        	String varnm = var.getAttribute("name");
        	if (varnm.equals(varNm)){
        		Element simpleReference = 	SESOps.getChildElement(var,"simpleReference");
        	
            if (simpleReference != null){
            String name = simpleReference.getAttribute("name");
                simpleRef = simpleReference;
                break;
        }
        	}
        }
        if (simpleRef == null) {
            return checkRange(range, val);
        }

        Element simpleReference = simpleRef;
        String name = getNodeNameAttrib(simpleReference, "simpleReference",
                                        "name");

        String entityType = "string";
        String restrictionBase = getNodeNameAttrib(simpleReference,
                "simpleReference",
                "restrictionBase");
        if (!restrictionBase.equals("unknown")) {
            entityType = restrictionBase;
        }

        String restValPairs = getNodeNameAttrib(simpleReference,
                                                "simpleReference",
                                                "restrictionValuePairs");

        if (!restValPairs.equals("unknown")) {
            Pattern p = Pattern.compile(",");
            String[] groups = p.split(restValPairs);
            if (groups.length % 2 == 1) {
                System.out.println(
                        "RestrictionValuePairs not even length: "
                        + restValPairs);
                return false;
            }
            Set<Object> grps = stringOps.toEnsembleSet(groups);
            boolean isEnum = false;
            if (grps.contains("enumeration")) {
                isEnum = true;
            }
            String restrict = "";
            String value = "";
            if (!isEnum && (entityType.equals("int")
                            || entityType.equals("double"))) {
                if (!isDouble(val)) {
                    System.out.println("Value " + val +
                                       " is not int or double ");
                    return false;
                }

                for (int ii = 0; ii < groups.length; ii++) {
                    groups[ii] = groups[ii].trim();
                    if (ii % 2 == 0) {
                        restrict = groups[ii];
                    } else if (ii % 2 == 1) {
                        value = groups[ii];
                        double valueDouble = Double.parseDouble(value);
                        double valDouble = Double.parseDouble(val);
                        if (restrict.equals("minExclusive")) {
                            if (valDouble <= valueDouble) {
                                System.out.println(
                                        "Value " + valDouble +
                                        " falls below " +
                                        valueDouble);
                                return false;
                            }
                        }
                        if (restrict.equals("maxExclusive")) {
                            if (valDouble >= valueDouble) {
                                System.out.println(
                                        "Value " + valDouble +
                                        " falls above " +
                                        valueDouble);

                                return false;
                            }
                        }
                        if (restrict.equals("minInclusive")) {
                            if (valDouble < valueDouble) {
                                System.out.println(
                                        "Value " + valDouble +
                                        " falls below " +
                                        valueDouble);

                                return false;
                            }
                        }
                        if (restrict.equals("maxInclusive")) {
                            if (valDouble > valueDouble) {
                                System.out.println(
                                        "Value " + valDouble +
                                        " falls above " +
                                        valueDouble);

                                return false;
                            }
                        }
                    }
                }
            } else {
                HashSet<Object> values = new HashSet<Object>();
                for (int ii = 0; ii < groups.length; ii++) {
                    groups[ii] = groups[ii].trim();
                    if (ii % 2 == 0) {
                        restrict = groups[ii];
                    } else if (ii % 2 == 1) {
                        value = groups[ii];
                        values.add(value);
                        if (restrict.equals("enumeration")) {
                            if (value.equals(val)) {
                                return true;
                            }
                        }
                    }
                }
                System.out.println(
                        "Value: " + val + " not one of specified values:" +
                        values);
                return false;
            }
        }
        return true;
    }

    public static boolean checkRange(String range, String s) {
        if (range.equals("int")) {
            return isInt(s);
        }
        if (range.equals("double")) {
            return isInt(s);
        }
        if (range.equals("boolean")) {
            return isInt(s);
        } else if (range.equals("string")) {
            return true;
        }
        System.out.println("Not a recognized type: " + s);
        return false;
    }

    public static boolean isInt(String s) {
        boolean res = true;
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException x) {
            res = false;
            System.out.println(x);
        }
        return res;
    }

    public static boolean isDouble(String s) {
        boolean res = true;
        try {
            Double.parseDouble(s);
        } catch (NumberFormatException x) {
            res = false;
            System.out.println(x);
        }
        return res;
    }

    public static boolean isBoolean(String s) {
        boolean res = false;
        if (s.equals("true") || s.equals("false")) {
            res = true;
        } else {
            System.out.println("Not a boolean: For input string: " + s);
        }
        return res;
    }

    public static boolean checkRules(sesRelation ses) {
        Hashtable<Object,Object> rF = ses.restrictRelationFn;
        Enumeration<Object> e = rF.keys();
        while(e.hasMoreElements()){
        	Object key = e.nextElement();
        	Object val = rF.get(key);
        	restrictRelation r = (restrictRelation) val;
            String domSpec = r.domainSpec;
            String ranSpec = r.rangeSpec;
            Element domsp = pruneOps.getPruneElement(domSpec);
            Element ransp = pruneOps.getPruneElement(ranSpec);
            LinkedList<Object> domch = pruneOps.getActualChildren(domsp);
            LinkedList<Object> ranch = pruneOps.getActualChildren(ransp);
            Iterator id = domch.iterator();
            Iterator ir = ranch.iterator();
            while (id.hasNext()) {
                String entNm1 = ((Element) id.next()).getNodeName();
                HashSet<Object> allowedVals = new HashSet<Object>(r.get(entNm1));
                while (ir.hasNext()) {
                    String entNm2 = ((Element) ir.next()).getNodeName();
                    if (allowedVals.contains(entNm2)) {
                        break;
                    } else {
                        System.out.println("The pair : "
                                           + entNm1 + "," + entNm2 +
                                           " doesn't satisfy the relation for " +
                                           domSpec + "." + ranSpec);
                        return false;
                    }
                }
            }
        }
        
        return true;
    }

    public static void main(String argv[]) {
        System.out.println(isBoolean("true"));

        System.exit(3);
    } // main

}
