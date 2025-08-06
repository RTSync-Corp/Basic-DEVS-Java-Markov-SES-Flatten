package com.ms4systems.devs.core.util;

import org.w3c.dom.*;
import java.util.*;
import java.util.regex.Pattern;

public class sesToGenericSchema extends SESOps {

    protected static String globals;
    protected static HashSet<Object> globalSet = new HashSet<Object>();

    public static void writeSchemaToXML(String myFileName) {
        fileHandler.writeToFile(myFileName + ".xsd", writeComplexSchemaDoc());
    }

    public static void writeSimpleSchemaToXML(String myFileName) {
        fileHandler.writeToFile(myFileName + ".xsd", writeSimpleSchemaDoc());
    }

    public static String writeSimpleSchemaDoc() {
        declared = new HashSet<Object>();
        return writeRootSimple(sesDoc.getDocumentElement());
    }

    public static String writeComplexSchemaDoc() {
        return writeDTDComplex();
    }

    public static String writeDTDComplex() {
        declared = new HashSet<Object>();
        return writeRootComplex(sesDoc.getDocumentElement());
    }

    public static void writeDTDComplex(String myFileName, String namespace,
            String schemaLocation) {
        declared = new HashSet<Object>();
        fileHandler.writeToFile(myFileName + ".xsd", writeRootComplex(sesDoc.getDocumentElement(), namespace, schemaLocation));

    }

    public static void writeDTDComplex(String myFileName) {
        declared = new HashSet<Object>();
        fileHandler.writeToFile(myFileName + ".xsd", writeRootComplex(sesDoc.getDocumentElement()));
    }

    public static String writeMultEnt(LinkedList<Object> nl) {
        Iterator it = nl.iterator();
        while (it.hasNext()) { // don't use the refs placed at end
            writeEntity((Element) it.next());
        }
        return "";
    }

    public static String writeSimpleReference(NodeList nl) {
        String s = "";
        for (int i = 0; i < nl.getLength(); i++) {
            s += "\n" + writeSimpleReference((Element) nl.item(i));
        }
        return s;
    }

    public static String writeSimpleReference(Element simpleReference) {
        String name = getNodeNameAttrib(simpleReference, "simpleReference",
                "name");
        if (name.equals("")) {
            return name;
        }
        String s = "\n" + "<xs:simpleType name=" + DomToXML.quote(name) + ">";

        String entityType = "string";
        String restrictionBase = getNodeNameAttrib(simpleReference,
                "simpleReference", "restrictionBase");
        if (!restrictionBase.equals("unknown")) {
            entityType = restrictionBase;

            s += "\n" + "<xs:restriction base="
                    + DomToXML.quote("xs:" + entityType) + ">";
        }

        String restValPairs = getNodeNameAttrib(simpleReference,
                "simpleReference", "restrictionValuePairs");

        if (!restValPairs.equals("unknown")) {
            Pattern p = Pattern.compile(",");
            String[] groups = p.split(restValPairs);
            if (groups.length % 2 == 1) {
                System.out.println("RestrictionValuePairs not even length: "
                        + restValPairs);
                return "";
            }

            String restrict = "";
            String value = "";
            for (int i = 0; i < groups.length; i++) {
                groups[i] = groups[i].trim();
                if (i % 2 == 0) {
                    restrict = groups[i];
                } else if (i % 2 == 1) {
                    value = groups[i];
                    s += "\n" + "<xs:" + restrict + " value="
                            + DomToXML.quote(value) + "/>";
                }
            }
            s += "\n" + "</xs:restriction>";
            s += "\n" + "</xs:simpleType> ";

        }
        return s;
    }

    public static void writeComplexDictionary(String[] xmlFiles,
            String dictName, String namespace) {

        if (xmlFiles.length == 0) {
            return;
        }
        String source = xmlFiles[0];
        restoreSesDoc(source);
        for (int i = 1; i < xmlFiles.length; i++) {
            addToComplexFrom(source, xmlFiles[i]);
        }

        writeSesDoc(dictName + ".xml");
        restoreSesDoc(dictName + ".xml");

        fileHandler.writeToFile(dictName + ".xsd", writeRootComplex(sesDoc.getDocumentElement(), namespace, ""));

    }

    public static String writeRootComplex(Element root, String namespace,
            String schemaLocation) {
        globals = "\n";
        NodeList nl = sesDoc.getElementsByTagName("complex");
        NodeList nlr = sesDoc.getElementsByTagName("complexReference");
        NodeList nls = sesDoc.getElementsByTagName("simpleReference");

        String includeString = "";
        if (!schemaLocation.equals("")) {
            includeString = "\n" + "<xs:include schemaLocation = "
                    + DomToXML.quote(schemaLocation) + "/>";
        }
        String header = "<?xml version='1.0' encoding='us-ascii'?>\n" + "\n"
                + "<xs:schema xmlns:xs="
                + DomToXML.quote("http://www.w3.org/2001/XMLSchema")
                + " elementFormDefault=" + DomToXML.quote("qualified")
                + " attributeFormDefault=" + DomToXML.quote("unqualified")
                + " xmlns = " + DomToXML.quote(namespace)
                + " targetNamespace = " + DomToXML.quote(namespace)
                + ">" + includeString + writeSimpleReference(nls)
                + writeComplex(nl) + writeComplexReference(nlr);

        LinkedList<Object> nlent = getElementChildrenOfElement(root, "entity");
        String s;
        if (!schemaLocation.equals("")) { // for use of dictionary
            s = writeEntity((Element) nlent.getFirst());
            s += "\n" + "</xs:schema>";
            return header + globals + s;
        } else { // for construction of dictionary
            s = writeMultEnt(nlent) + "\n" + "</xs:schema>";
            return header + globals + s;
        }
    }

    public static String writeRootSimple(Element root) {
        globals = "\n";
        NodeList nl = sesDoc.getElementsByTagName("complex");
        NodeList nlr = sesDoc.getElementsByTagName("complexReference");
        NodeList nls = sesDoc.getElementsByTagName("simpleReference");

        Element topEnt = root;
        if (root.getNodeName().equals("top")) {
            topEnt = (Element) getElementChildrenOfElement(root, "entity").getFirst();
        }

        String header = "<?xml version='1.0' encoding='us-ascii'?>\n" + "\n"
                + "<xs:schema xmlns:xs="
                + DomToXML.quote("http://www.w3.org/2001/XMLSchema")
                + " elementFormDefault=" + DomToXML.quote("qualified")
                + " attributeFormDefault=" + DomToXML.quote("unqualified")
                + ">" + writeComplex(nl) // ;
                + writeComplexReference(nlr) + writeSimpleReference(nls);


        String s = writeEntity(topEnt) + "\n";
        int len = s.length() - 23 - topEnt.getAttribute("name").length();
        if (len > 0) {
            s = s.substring(0, s.length() - 23
                    - topEnt.getAttribute("name").length());
            
        }
        s += "</xs:schema>";
        return header + globals + s;
    }

    public static String writeRootComplex(Element root) {
        globals = "\n";
        NodeList nl = sesDoc.getElementsByTagName("complex");
        NodeList nlr = sesDoc.getElementsByTagName("complexReference");
        NodeList nls = sesDoc.getElementsByTagName("simpleReference");
        Element topEnt = root;
        if (!getElementChildrenOfElement(root, "entity").isEmpty()) {
            topEnt = (Element) getElementChildrenOfElement(root, "entity").getFirst();
        }
        String header = "<?xml version='1.0' encoding='us-ascii'?>\n" + "\n"
                + "<xs:schema xmlns:xs="
                + DomToXML.quote("http://www.w3.org/2001/XMLSchema")
                + " elementFormDefault=" + DomToXML.quote("qualified")
                + " attributeFormDefault=" + DomToXML.quote("unqualified")
                + ">" + writeComplex(nl) // ;
                + writeComplexReference(nlr) + writeSimpleReference(nls);

        String s = writeEntity(topEnt) + "\n";//bpz 2007

        int len = 23 + topEnt.getAttribute("name").length();
        if (s.length() < len) {
            s = "";
            
        } else {
            s = s.substring(0, s.length() - 23
                    - topEnt.getAttribute("name").length());
            
        }
        s += "</xs:schema>";
        return header + globals + s;
    }

    public static String writeComplex(NodeList nl) {
        String s = "";

        for (int i = 0; i < nl.getLength(); i++) {
            s += "\n" + writeComplex((Element) nl.item(i));
        }
        return s;
    }

    public static String writeComplexReference(NodeList nl) {
        String s = "";
        for (int i = 0; i < nl.getLength(); i++) {
            s += "\n" + writeComplexReference((Element) nl.item(i));
        }
        return s;
    }

    public static String writeComplexEntity(Element complex) {
        String s = "<xs:complexType name="
                + DomToXML.quote(getNodeNameAttrib(complex, "complex", "name"))
                + ">";

        String entityType = getNodeNameAttrib(complex, "complex", "entityType");
        if (!entityType.equals("entity")) {
            return "";
        }
        s += "\n" + "<xs:simpleContent><xs:extension base="
                + DomToXML.quote(entityType) + ">";

        LinkedList<Object> et = getElementChildrenOfElement(complex, "var");
        if (!et.isEmpty()) {
            Iterator itq = et.iterator();
            s += "";
            while (itq.hasNext()) {
                Element var = (Element) itq.next();
                String varnm = var.getAttribute("name");
                s += "\n" + "<xs:attribute name=" + DomToXML.quote(varnm);
                String rangeSpec = var.getAttribute("rangeSpec");
                s += handleRange(rangeSpec);
                s += handleUse(var);
            }
        }

        s += "\n" + "</xs:extension></xs:simpleContent>";

        s += "\n" + "</xs:complexType>" + "\n";

        return s;
    }

    public static boolean isAtomicEntity(Element complex) {

        LinkedList<Object> et = getElementChildrenOfElement(complex, "var");
        if (!et.isEmpty()) {
            return false;
        } else {
            et = getElementChildrenOfElement(complex, "aspect");
            if (!et.isEmpty()) {
                return false;
            } else {
                et = getElementChildrenOfElement(complex, "specialization");
                if (!et.isEmpty()) {
                    return false;
                } else {
                    et = getElementChildrenOfElement(complex, "multiAspect");
                    if (!et.isEmpty()) {
                        return false;
                    }
                    return true;
                }
            }
        }
    }

    public static String writeComplex(Element complex) {
        String entype = getNodeNameAttrib(complex, "complex", "entityType");
        if (entype.equals("entity")) {
            return writeComplexEntity(complex);
        }
        String s = "<xs:complexType name="
                + DomToXML.quote(getNodeNameAttrib(complex, "complex", "name"))
                + ">";
        boolean content = true; // false;

        if (content) {
            String entityType = "xs:string";
            String etype = getNodeNameAttrib(complex, "complex", "entityType");
            if (!etype.equals("unknown")) {
                entityType = "xs:" + etype;
            }

            s += "\n" + "<xs:simpleContent><xs:extension base="
                    + DomToXML.quote(entityType) + ">";
        }

        LinkedList<Object> et = getElementChildrenOfElement(complex, "var");
        if (!et.isEmpty()) {
            Iterator itq = et.iterator();
            s += "";
            while (itq.hasNext()) {
                Element var = (Element) itq.next();
                String varnm = var.getAttribute("name");
                s += "\n" + "<xs:attribute name=" + DomToXML.quote(varnm);
                String rangeSpec = var.getAttribute("rangeSpec");
                s += handleRange(rangeSpec);
                s += handleUse(var);
            }
        }
        if (content) {
            s += "\n" + "</xs:extension></xs:simpleContent>";
        }
        s += "\n" + "</xs:complexType>" + "\n";

        String complexName = getNodeNameAttrib(complex, "complex", "name");
        String refName = getNodeNameAttrib(complex, "complex", "referenceName");
        if (refName.equals("unknown")) {
            refName = DomToXML.quote("restricted" + complexName);
        }

        if (refName.equals("")) {
            return ""; // bpz 2006
        }

        String restValPairs = getNodeNameAttrib(complex, "complex",
                "restrictionValuePairs");

        if (!restValPairs.equals("unknown")) {

            s += "\n" + "<xs:complexType name= "
                    + DomToXML.quote(refName + "Type") + ">";
            s += "\n" + "<xs:simpleContent><xs:restriction base="
                    + DomToXML.quote(complexName) + ">";
            if (!restValPairs.equals("")) {
                Pattern p = Pattern.compile(",");
                String[] groups = p.split(restValPairs);

                if (groups.length % 2 == 1) {
                    System.out.println("RestrictionValuePairs not even length: "
                            + restValPairs);
                    return "";
                }

                String restrict = "";
                String value = "";
                for (int i = 0; i < groups.length; i++) {
                    groups[i] = groups[i].trim();
                    if (i % 2 == 0) {
                        restrict = groups[i];
                    } else if (i % 2 == 1) {
                        value = groups[i];
                        s += "\n" + "<xs:" + restrict + " value="
                                + DomToXML.quote(value) + "/>";
                    }
                }
            }
            s += "\n" + "</xs:restriction></xs:simpleContent>";
            s += "\n" + "</xs:complexType> ";
        }
        return s;
    }

    public static String writeComplexReference(Element complexReference) {
        String name = getNodeNameAttrib(complexReference, "complexReference",
                "name");
        if (name.equals("")) {
            return name;
        }
        String s = "\n" + "<xs:complexType name=" + DomToXML.quote(name) + ">";

        String entityType = "string";
        String restrictionBase = getNodeNameAttrib(complexReference,
                "complexReference", "restrictionBase");
        if (!restrictionBase.equals("unknown")) {
            entityType = restrictionBase;

            s += "\n" + "<xs:simpleContent><xs:restriction base="
                    + DomToXML.quote(entityType) + ">";
        }

        String restValPairs = getNodeNameAttrib(complexReference,
                "complexReference", "restrictionValuePairs");

        if (!restValPairs.equals("unknown")) {
            Pattern p = Pattern.compile(",");
            String[] groups = p.split(restValPairs);
            if (groups.length % 2 == 1) {
                System.out.println("RestrictionValuePairs not even length: "
                        + restValPairs);
                return "";
            }

            String restrict = "";
            String value = "";
            for (int i = 0; i < groups.length; i++) {
                groups[i] = groups[i].trim();
                if (i % 2 == 0) {
                    restrict = groups[i];
                } else if (i % 2 == 1) {
                    value = groups[i];
                    s += "\n" + "<xs:" + restrict + " value="
                            + DomToXML.quote(value) + "/>";
                }
            }
            s += "\n" + "</xs:restriction></xs:simpleContent>";
            s += "\n" + "</xs:complexType> ";

        }
        return s;
    }

    public static String writeEntity(Element entity) {

        return writeEntity("", entity);
    }

    public static String writeEntity(String prefix, Element entity) {
        if (entity == null) {
            return "";
        }
        if (isAtomicEntity(entity)) {
            String entityNam = getNodeNameAttrib(entity, "entity", "name");

            return "<xs:element  name ="
                    + DomToXML.quote(getNodeNameAttrib(entity, "entity", "name"))
                    + "/>";
        }
        String s = "";
        String entityName = getNodeNameAttrib(entity, "entity", "name");

        if (globalSet.contains(entityName)) {
            return "<xs:element  ref=" + DomToXML.quote(entityName) + "/>";
        }

        boolean sequence = false;
        boolean choice = false;
        boolean attribute = false;
        String aspect = "";
        String spec = "";
        String type = handleComplexVar(entity);

        s += "\n" + "  <xs:element name=" + DomToXML.quote(entityName) + type
                + ">";
        s += handleSimpleVar(entity);

        LinkedList<Object> es = collectAspectsOf(entity);
        Iterator is = es.iterator();
        LinkedList<Object> ec = getChildrenOfElement(entity, "specialization");
        Iterator it = ec.iterator();

        if (!ec.isEmpty() || !es.isEmpty()) {

            sequence = true;
            choice = true;
            while (it.hasNext()) {
                spec += writeSpec((String) it.next());
            }

            if (!es.isEmpty()) {
                aspect += "\n"
                        + "<xs:element name = "
                        + DomToXML.quote("aspectsOf"
                        + getNodeNameAttrib(entity, "entity", "name"))
                        + ">";
                aspect += "\n" + " <xs:complexType>";
                aspect += "\n" + "<xs:choice>";
                while (is.hasNext()) {
                    String aspName = (String) is.next();
                    aspect += writeAsp(aspName);
                }
                aspect += "\n" + "</xs:choice>";
                aspect += "\n" + " </xs:complexType>";
                aspect += "\n" + "</xs:element>";
            }

        }
        LinkedList<Object> et = getElementChildrenOfElement(entity, "var");
        String vars = "";

        if (!et.isEmpty()) {
            Iterator itq = et.iterator();
            attribute = true;
            while (itq.hasNext()) {
                Element var = (Element) itq.next();
                String varnm = var.getAttribute("name");
                vars += "\n" + "<xs:attribute name=" + DomToXML.quote(varnm);

                String rangeSpec = var.getAttribute("rangeSpec");
                if (varnm.endsWith("efaultValue")) {
                    rangeSpec = "string";
                    
                }
                vars += handleRange(rangeSpec);
                vars += handleUse(var);
            }
        }
        if (sequence || choice || attribute) {
            s += "\n" + " <xs:complexType>";
            if (sequence || choice) {
                s += "\n" + " <xs:sequence>";
                s += aspect;
                s += spec;
            }
        }

        if (sequence || choice || attribute) {
            if (sequence || choice) {
                s += "\n" + " </xs:sequence>";
            }
            if (attribute) {
                s += vars;
            }
            s += "\n" + " </xs:complexType>";
        }

        s += "\n" + "  </xs:element>";
        if (!isAtomicEntity(entity)) {
            globals += s + "\n";
            globalSet.add(entityName);
            return "\n" + "  <xs:element ref = " + DomToXML.quote(entityName)
                    + prefix + "/>";
        }
        return s;
    }

    public static String writeSpec(String specNm) {
        if (specNm == null) {
            return " ";
        }
        if (globalSet.contains(specNm)) {
            return "<xs:element  ref=" + DomToXML.quote(specNm) + "/>";
        }
        String s = "";

        LinkedList<Object> es = getEntityChildrenOf(getElement("specialization", specNm));
        if (!es.isEmpty()) {
            s += "\n" + " <xs:element name = " + DomToXML.quote(specNm) + ">";
            if (isDeclared(specNm)) {
                return s + "\n" + " </xs:element>";
            }

            s += "\n" + " <xs:complexType>";
            s += "\n" + "<xs:choice>";
            Iterator it = es.iterator();
            while (it.hasNext()) {
                s += writeEntity(getElement("entity", (String) it.next()));
            }
            s += "\n" + " </xs:choice>";
            s += "\n" + " </xs:complexType>";
            s += "\n" + " </xs:element>";
        }
        globals += s + "\n";
        globalSet.add(specNm);
        return "\n" + "  <xs:element ref = " + DomToXML.quote(specNm) + "/>";

        // return s;
    }

    public static String writeAsp(String aspNm) {
        if (aspNm == null) {
            return " ";
        }
        if (globalSet.contains(aspNm)) {
            return "<xs:element  ref=" + DomToXML.quote(aspNm) + "/>";
        }
        String s = "";
        Element asp = getElement("aspect", aspNm);
        if (asp != null) {
            String cop = asp.getAttribute("coupling");
            LinkedList<Object> es = getEntityChildrenOf(asp);
            if (!es.isEmpty()) {
                s += "\n" + " <xs:element name = " + DomToXML.quote(aspNm)
                        + ">";
                if (isDeclared(aspNm)) {
                    return s + "\n" + " </xs:element>";
                }

                s += "\n" + " <xs:complexType>";
                s += "\n" + "<xs:sequence>";
                Iterator it = es.iterator();
                while (it.hasNext()) {
                    s += writeEntity(getElement("entity", (String) it.next()));
                }
                s += "\n" + " </xs:sequence>";
                s += "\n" + "<xs:attribute name = "
                        + DomToXML.quote("coupling")
                        + " fixed = " + DomToXML.quote(cop)
                        + " />";
                s += "\n" + " </xs:complexType>";
                s += "\n" + " </xs:element>";
                globals += s + "\n";
                globalSet.add(aspNm);
                return "\n" + "  <xs:element ref = " + DomToXML.quote(aspNm)
                        + "/>";

            }
        } else {
            return writeMultiAsp(aspNm);
        }
        return s;
    }

    public static String writeMultiAsp(String aspNm) {
        if (aspNm == null) {
            return " ";
        }
        if (globalSet.contains(aspNm)) {
            return "<xs:element  ref=" + DomToXML.quote(aspNm) + "/>";
        }
        String s = "";
        Element asp = getElement("multiAspect", aspNm);
        if (asp != null) {
            LinkedList<Object> es = getEntityChildrenOf(asp);
            if (!es.isEmpty()) {
                Element mult = getElement("multiAspect", aspNm);
                LinkedList<Object> mcs = getChildrenOfElement(mult, "entity");
                if (mcs != null) {
                    Iterator mit = mcs.iterator();
                    if (mit.hasNext()) {
                        Element ent = getElement("entity", (String) mit.next());
                        String entNm = getNodeNameAttrib(ent, "entity", "name");
                        if (!entNm.equals("")) {
                            mcs = getElementChildrenOfElement(mult,
                                    "numberComponentsVar");
                            mit = mcs.iterator();
                            if (mit.hasNext()) {
                                Element ncv = (Element) mit.next();
                                String ncvar = ncv.getAttribute("name");
                                String min = ncv.getAttribute("min");
                                if (!min.equals("")) {
                                    min = " minOccurs= " + DomToXML.quote(min);
                                }
                                String max = ncv.getAttribute("max");
                                if (!max.equals("")) {
                                    max = " maxOccurs= " + DomToXML.quote(max);
                                }

                                s += "\n" + " <xs:element name = "
                                        + DomToXML.quote(aspNm) + ">";
                                if (isDeclared(aspNm)) {
                                    return s + "\n" + " </xs:element>";
                                }
                                s += "\n" + " <xs:complexType>";
                                s += "\n" + "<xs:sequence>";
                                s += writeEntity(min + max, ent);
                                s += "\n" + "</xs:sequence>";
                                s += "\n" + " <xs:attribute name="
                                        + DomToXML.quote(ncvar) + " type="
                                        + DomToXML.quote("xs:int") + ">"
                                        + "</xs:attribute>";
                                s += "\n" + " </xs:complexType>";
                                s += "\n" + " </xs:element>";

                            }
                        }
                    }
                }
            }
        }
        globals += s + "\n";
        globalSet.add(aspNm);
        return "\n" + "  <xs:element ref = " + DomToXML.quote(aspNm) + "/>";

    }

    public static String handleUse(Element var) {
        String s = "";

        String fixed = var.getAttribute("fixed");
        if (!fixed.equals("") && !fixed.equals("unknown")) {
            s += " fixed = " + DomToXML.quote(fixed);
            String use = var.getAttribute("use");
            if (!use.equals("unknown")) {
                s += " use = " + DomToXML.quote(use);
            }

        } else {
            String Default = var.getAttribute("default");
            if (!Default.equals("") && !Default.equals("unknown")) {
                s += " default = " + DomToXML.quote(Default);
                s += " use = " + DomToXML.quote("optional");
            } else {
                String use = var.getAttribute("use");
                     if (use.equals("")) {
            s += " use = " + DomToXML.quote("optional");
        }   else
                if (!use.equals("unknown")) {
                    s += " use = " + DomToXML.quote(use);
                }

            }
        }

        s += " />";
        return s;
    }

    public static String handleSimpleVar(Element entity) {
        LinkedList<Object> ecv = getElementChildrenOfElement(entity, "simpleVar");
        String newsimple = "";
        if (!ecv.isEmpty()) {
            Iterator it = ecv.iterator();
            while (it.hasNext()) {
                Element simpleVar = (Element) it.next();
                String svarnm = simpleVar.getAttribute("name");
                LinkedList<Object> addvars = getElementChildrenOfElement(simpleVar, "addvar");
                if (!addvars.isEmpty()) {
                    newsimple += "\n" + makeComplexExtension(svarnm, addvars);
                }
            }
        }
        return newsimple;
    }

    public static String handleRange(String rangeSpec) {
        HashSet<Object> dataTypes = new HashSet<Object>();
        dataTypes.add("string");
        dataTypes.add("int");
        dataTypes.add("double");
        dataTypes.add("boolean");
        dataTypes.add("long");
        dataTypes.add("decimal");
        String s = " type = ";
        if (!rangeSpec.equals("unknown")) {
            if (dataTypes.contains(rangeSpec)) {
                s += DomToXML.quote("xs:" + rangeSpec);
            } else {
                int ind = rangeSpec.indexOf("with");
                if (ind == -1) {
                    int indcomma = rangeSpec.indexOf(",");
                    if (indcomma == -1) {
                        return s += DomToXML.quote(rangeSpec);
                    }
                    rangeSpec = rangeSpec.substring(0, indcomma)
                            + rangeSpec.substring(indcomma + 1);
                    return s += DomToXML.quote(rangeSpec);
                }
                String type = rangeSpec.substring(0, ind).trim();
                sesParse sp = new sesParse();
                ind = rangeSpec.indexOf("values");
                rangeSpec = rangeSpec.substring(ind + 6).trim();
                Pair pp = sp.parseConnective(rangeSpec);
                String vals = pp.getValue().toString();
                vals = vals.substring(1, vals.length() - 1);
                if (vals.equals("")) {
                    return s += DomToXML.quote(type + rangeSpec);
                }

                Pattern p = Pattern.compile(",");
                String[] groups = p.split(vals);
                String comp = groups[groups.length - 1].trim();
                for (int i = 0; i < groups.length - 1; i++) {
                    groups[i] = groups[i].trim();
                    comp += groups[i];
                }
                s += DomToXML.quote(type + comp);
            }
            return s;
        }
        return "";
    }

    public static String makeComplexExtension(String type, LinkedList<Object> vars) {
        String s = "<xs:complexType>";
        s += "\n" + " <xs:simpleContent > <xs:extension base = "
                + DomToXML.quote(type) + ">";
        Iterator it = vars.iterator();
        while (it.hasNext()) {
            Element var = (Element) it.next();
            String varNm = var.getAttribute("name");
            s += "\n" + " <xs:attribute name = " + DomToXML.quote(varNm);

            String rangeSpec = var.getAttribute("rangeSpec");
            if (rangeSpec != null) {
                s += " type = " + DomToXML.quote("xs:" + rangeSpec);
            }
            s += handleUse(var);
        }
        s += "\n" + " </xs:extension > </xs:simpleContent > </xs:complexType>";
        return s;
    }

    public static void addToComplexFrom(String sesFile, String sesAddFile) {
        if (sesDoc == null) {
            restoreSesDoc(sesFile);
        }

        Document doc1 = XMLToDom.getDocument(sesAddFile);
        Element root1 = doc1.getDocumentElement();

        NodeList nl = root1.getChildNodes();

        for (int i = 0; i < nl.getLength(); i++) {
            Node nd = nl.item(i);
            if (!nd.getNodeName().equals("#text")
                    && nd.getNodeType() != Node.COMMENT_NODE) {
                sesRoot.appendChild(sesDoc.importNode(nd, true));
            }
        }
    }

    public static String handleComplexVar(Element entity) {
        LinkedList<Object> ecv = getElementChildrenOfElement(entity, "complexVar");
        String type = "";
        if (!ecv.isEmpty()) {
            Element complexVar = (Element) ecv.getFirst();
            String cvarnm = complexVar.getAttribute("name");
            type = " type = " + DomToXML.quote(cvarnm);
        }
        return type;
    }

    public static LinkedList<Object> expandAllMultAspNWriteSimpleSchema(String topEntNm,
            String folderToPutXSD) {
        LinkedList<Object> specs = SESOps.expandAllMultiAsp();
        SESOps.writeSesDoc(folderToPutXSD + topEntNm + "Expand.xml");
        writeSimpleSchemaToXML(folderToPutXSD + topEntNm + "Expand");
        return specs;
    }

    public static void cutChildFromParent(Node ch, Node par) {
        NodeList nl = ch.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            par.appendChild(nl.item(i));
        }
        par.removeChild(ch);
    }

    public static void cutGrandChildFromParent(Node ch, Node par) {
        NodeList nl = ch.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node gc = nl.item(i);
            NodeList nll = gc.getChildNodes();
            for (int j = 0; j < nll.getLength(); j++) {
                par.appendChild(nll.item(j));
            }
        }
        par.removeChild(ch);
    }

    public static void cutGreatGrandChildFromParent(Node ch, Node par) {
        NodeList nl = ch.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node gc = nl.item(i);
            NodeList nll = gc.getChildNodes();
            for (int j = 0; j < nll.getLength(); j++) {
                Node ggc = nl.item(j);
                NodeList nlll = ggc.getChildNodes();
                for (int k = 0; k < nlll.getLength(); k++) {
                    par.appendChild(nlll.item(k));
                }
            }
        }
        par.removeChild(ch);
    }

    public static Document cutAspectsOf(String xsdFile) {
        Document doc = XMLToDom.getDocument(xsdFile);
        LinkedList<Object> q = new LinkedList<Object>(); // getAllStartsWith(doc, "aspectsOf");
        Iterator it = q.iterator();
        while (it.hasNext()) {
            Node el = (Node) it.next();
            Node par = el.getParentNode();
            cutGreatGrandChildFromParent(el, par);
        }
        return cutItemsOf(doc);
    }

    public static Document cutItemsOf(Document doc) {
        LinkedList<Object> q = getAllEndsWith(doc, "Spec");
        q.addAll(getAllEndsWith(doc, "Type"));
        q.addAll(getAllEndsWith(doc, "Asp"));
        q.addAll(getAllEndsWith(doc, "Dec"));
        // System.out.println(q);
        Iterator it = q.iterator();
        while (it.hasNext()) {
            Node el = (Node) it.next();
            Node par = el.getParentNode();
            cutChildFromParent(el, par);
        }
        return doc;
    }

    public static LinkedList<Object> getAllStartsWith(Document doc, String start) {
        LinkedList<Object> q = new LinkedList<Object>();
        NodeList nl = doc.getElementsByTagName("xs:element");
        for (int i = 0; i < nl.getLength(); i++) {
            Element n = (Element) nl.item(i);
            if (n.getAttribute("name").startsWith(start)) {
                q.add(n);
            }
        }
        return q;
    }

    public static LinkedList<Object> getAllEndsWith(Document doc, String end) {
        LinkedList<Object> q = new LinkedList<Object>();
        NodeList nl = doc.getElementsByTagName("xs:element");
        for (int i = 0; i < nl.getLength(); i++) {
            Element n = (Element) nl.item(i);
            if (n.getAttribute("name").endsWith(end)) {
                q.add(n);
            }
        }
        return q;
    }

    // /////////////////////////////////////
    public static void main(String argv[]) {

        System.exit(3);
    } // main
}
