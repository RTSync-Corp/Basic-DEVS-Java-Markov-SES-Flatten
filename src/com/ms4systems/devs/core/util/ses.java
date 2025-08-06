package com.ms4systems.devs.core.util;


import org.w3c.dom.*;
import java.util.*;

public class ses {

    public static sesRelation create(String RootEntityName) {
        sesRelation sr = new sesRelation();
        sr.rootEntityName = RootEntityName;
        sr.entityNames.add(RootEntityName);
        //bpz
        return sr;
    }

    protected Document sesDoc;
    protected Element sesRoot;

    public ses() {
        super();
    }

    public ses(String xmlFile) {
        this(XMLToDom.getDocument(xmlFile));
    }

    public ses(Document sesDoc) {
        this.sesDoc = sesDoc;

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
    }

    public HashSet<Object> getNames(String type) {
        HashSet<Object> names = new HashSet<Object>();
        NodeList nl = sesDoc.getElementsByTagName(type);
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            names.add(SESOps.getNodeNameAttrib(n, type, "name"));
        }
        return names;
    }

    public HashSet<Object> commonNames(ses se, String type) {
    	HashSet<Object> seNames = se.getNames(type);
        seNames.retainAll(getNames(type));
        return seNames;
    }

    public boolean equalNames(ses se, String type) {
    	HashSet<Object> seNames = se.getNames(type);
    	HashSet<Object> myNames = getNames(type);
        return seNames.containsAll(myNames) && myNames.containsAll(seNames);
    }

    public Hashtable<Object,HashSet<Object>> entityHasAspect() {
        return type1HasType2("entity", "aspect");
    }

    public Hashtable<Object,HashSet<Object>> entityHasMultiAspect() {
        return type1HasType2("entity", "multiAspect");
    }

    public Hashtable<Object,HashSet<Object>> entityHasSpec() {
        return type1HasType2("entity", "specialization");
    }

    public Hashtable<Object,HashSet<Object>> aspectHasEntity() {
        return type1HasType2("aspect", "entity");
    }

    public Hashtable<Object,HashSet<Object>> multiAspectHasEntity() {
        return type1HasType2("multiAspect", "entity");
    }

    public Hashtable<Object,HashSet<Object>> multiAspectHasVar() {
        return type1HasType2("multiAspect", "numberComponentsVar");
    }

    public Hashtable<Object,HashSet<Object>> specHasEntity() {
        return type1HasType2("specialization", "entity");
    }

    public Hashtable<Object,HashSet<Object>> typeHasVar(String type) {
        return type1HasType2(type, "var");
    }

    public Hashtable<Object,HashSet<Object>> entityHasVar() {
        return type1HasType2("entity", "var");
    }

    public Hashtable<Object,HashSet<Object>> type1HasType2(String type1, String type2) {
    	Hashtable<Object,HashSet<Object>> r = new Hashtable<Object,HashSet<Object>>();
        SESOps.sesDoc = sesDoc;
        SESOps.sesRoot = sesRoot;
        HashSet<Object> type1Names = getNames(type1);
        if (type1Names.equals(null)) {
            return r;
        }
        Iterator<Object> it = type1Names.iterator();
        while (it.hasNext()) {
            String typeName = (String) it.next();
            Element el = SESOps.getElement(type1, typeName);
            if (el.equals(null)) {
                continue;
            }
            LinkedList<Object> entities = SESOps.getChildrenOfElement(el, type2);

            Iterator<Object> iq = entities.iterator();
            while (iq.hasNext()) {
                String type2Name = (String) iq.next();
                if(r.containsKey(typeName)){
                	r.get(typeName).add(type2Name);
                }else {
                	HashSet<Object> value = new HashSet<Object>();
                	value.add(type2Name);
                	r.put(typeName, value);
                }
                
            }
        }
        return r;
    }

    public Hashtable<Object,HashSet<Object>> type1HasAllType2(String type1, String type2) {
           Hashtable<Object,HashSet<Object>> r = new Hashtable<Object,HashSet<Object>>();
           SESOps.sesDoc = sesDoc;
           SESOps.sesRoot = sesRoot;
           HashSet<Object> type1Names = getNames(type1);
           if (type1Names.equals(null)) {
               return r;
           }
           Iterator<Object> it = type1Names.iterator();
           while (it.hasNext()) {
               String typeName = (String) it.next();
               LinkedList<Object> typs = SESOps.getAllElements(type1,typeName);
               for (int i = 0; i < typs.size(); i++) {
                   Element el = (Element) typs.get(i);
                   LinkedList<Object> entities = SESOps.getChildrenOfElement(el, type2);
                   Iterator<Object> iq = entities.iterator();
                   while (iq.hasNext()) {
                       String type2Name = (String) iq.next();
                       if(r.containsKey(typeName)){
                    	   r.get(typeName).add(type2Name);
                       }else {
                    	   HashSet<Object> value = new HashSet<Object>();
                    	   value.add(type2Name);
                    	   r.put(typeName, value);
                       }
                       
                   }
               }
           }
           return r;
       }

    ////////////////////////////
    public boolean include(ses se, String item, String type1, String type2) {
        //item is of type1
        Hashtable<Object,HashSet<Object>> r1 = type1HasType2(type1, type2);
        HashSet<Object> es1 = r1.get(item);
        Hashtable<Object,HashSet<Object>> r2 = se.type1HasType2(type1, type2);
        HashSet<Object> es2 = r2.get(item);
        return es1.containsAll(es2);
    }

    public boolean includeEntityHasAspect(ses se, String entity) {
        return include(se, entity, "entity", "aspect");
    }

    public boolean includeEntityHasMultiAspect(ses se, String entity) {
        return include(se, entity, "entity", "multiAspect");
    }

    public boolean includeAspectHasEntity(ses se, String aspect) {
        return include(se, aspect, "aspect", "entity");
    }

    public boolean includeMultiAspectHasEntity(ses se, String aspect) {
        return include(se, aspect, "multiAspect", "entity");
    }

    public boolean includeEntityHasSpec(ses se, String entity) {
        return include(se, entity, "entity", "specialization");
    }

    public boolean includeSpecHasEntity(ses se, String spec) {
        return include(se, spec, "specialization", "entity");
    }

    public boolean includeEntityHasVar(ses se, String entity) {
        return include(se, entity, "entity", "var");
    }

    public boolean includeMultiAspectHasVar(ses se, String multiAspect) {
        return include(se, multiAspect, "entity", "var");
    }

    ////////////////////////////
    public boolean equal(ses se, String item, String type1, String type2) {
        return include(se, item, type1, type2) &&
                se.include(this, item, type1, type2);
    }

    public boolean equalEntityHasAspect(ses se, String entity) {
        return includeEntityHasAspect(se, entity) &&
                se.includeEntityHasAspect(this, entity);
    }

    public boolean equalEntityHasMultiAspect(ses se, String entity) {
        return includeEntityHasMultiAspect(se, entity) &&
                se.includeEntityHasMultiAspect(this, entity);
    }

    public boolean equalAspectHasEntity(ses se, String aspect) {
        return includeAspectHasEntity(se, aspect) &&
                se.includeAspectHasEntity(this, aspect);
    }

    public boolean equalMultiAspectHasEntity(ses se, String aspect) {
        return includeMultiAspectHasEntity(se, aspect) &&
                se.includeMultiAspectHasEntity(this, aspect);
    }

    public boolean equalSpecHasEntity(ses se, String spec) {
        return includeSpecHasEntity(se, spec) &&
                se.includeSpecHasEntity(this, spec);
    }

    public boolean equalEntityHasVar(ses se, String entity) {
        return includeEntityHasVar(se, entity) &&
                se.includeEntityHasVar(this, entity);
    }

    public boolean equalMultiAspectmulHasVar(ses se, String multiAspect) {
        return includeMultiAspectHasVar(se, multiAspect) &&
                se.includeMultiAspectHasVar(this, multiAspect);
    }

    ////////////////////////////

    public boolean include(ses se, String type1, String type2) {
        if (getNames(type1).isEmpty()
            && !se.getNames(type1).isEmpty()) {
            return false;
        }
        HashSet<Object> commons = commonNames(se, type1);
        Iterator<Object> it = commons.iterator();
        while (it.hasNext()) {
            if (!include(se, (String) it.next(), type1, type2)) {
                return false;
            }
        }
        return true;
    }

    public boolean includeEntityHasAspect(ses se) {
        return include(se, "entity", "aspect");
    }

    public boolean includeEntityHasMultiAspect(ses se) {
        return include(se, "entity", "multiAspect");
    }

    public boolean includeAspectHasEntity(ses se) {
        return include(se, "aspect", "entity");
    }

    public boolean includeMultiAspectHasEntity(ses se) {
        return include(se, "multiAspect", "entity");
    }

    public boolean includeMultiAspectHasVar(ses se) {
        return include(se, "multiAspect", "numberComponentsVar");
    }

    public boolean includeEntityHasSpec(ses se) {
        return include(se, "entity", "specialization");
    }

    public boolean includeEntityHasVar(ses se) {
        return include(se, "entity", "var");
    }

    public boolean includeSpecHasEntity(ses se) {
        return include(se, "specialization", "entity");
    }

    ////////////////////////////
    public boolean equal(ses se, String type1, String type2) {
        return include(se, type1, type2) && se.include(this, type1, type2);
    }

    public boolean equalEntityHasAspect(ses se) {
        return equal(se, "entity", "aspect");
    }

    public boolean equalEntityHasMultiAspect(ses se) {
        return equal(se, "entity", "multiAspect");
    }

    public boolean equalAspectHasEntity(ses se) {
        return equal(se, "aspect", "entity");
    }

    public boolean equalMultiAspectHasEntity(ses se) {
        return equal(se, "multiAspect", "entity");
    }

    public boolean equalEntityHasSpec(ses se) {
        return equal(se, "entity", "specialization");
    }

    public boolean equalEntityHasVar(ses se) {
        return equal(se, "entity", "var");
    }

    public boolean equalMultiAspectHasVar(ses se) {
        return equal(se, "multiAspect", "var");
    }

    public boolean equalSpecHasEntity(ses se) {
        return equal(se, "specialization", "entity");
    }

    ////////////////////////////

    public boolean include(ses se) {
        return includeEntityHasAspect(se) &&
                includeEntityHasMultiAspect(se) &&
                includeAspectHasEntity(se) &&
                includeMultiAspectHasEntity(se) &&
                includeEntityHasSpec(se) &&
                includeEntityHasVar(se) &&
                includeMultiAspectHasEntity(se) &&
                includeMultiAspectHasVar(se) &&
                includeSpecHasEntity(se);
    }

    public boolean equal(ses se) {
        return equalEntityHasAspect(se) &&
                equalEntityHasMultiAspect(se) &&
                equalAspectHasEntity(se) &&
                equalMultiAspectHasEntity(se) &&
                equalEntityHasSpec(se) &&
                equalEntityHasVar(se) &&
                equalMultiAspectHasVar(se) &&
                equalSpecHasEntity(se);
    }

    ////////////////////////////
    public void printNames(String type) {
        SESOps.sesDoc = sesDoc;
        SESOps.sesRoot = sesRoot;
        SESOps.printNames(type);
    }

    public void printTree() {
        SESOps.sesDoc = sesDoc;
        SESOps.sesRoot = sesRoot;
        SESOps.printTree();
    }
    public String printTreeString() {
        SESOps.sesDoc = sesDoc;
        SESOps.sesRoot = sesRoot;
       return SESOps.printTreeString();
    }
    public void writeSesDoc(String xmlFile) {
        SESOps.sesDoc = sesDoc;
        SESOps.sesRoot = sesRoot;
        SESOps.writeSesDoc(xmlFile);
    }

    public void expandAllMultiAsp() {
        SESOps.sesDoc = sesDoc;
        SESOps.sesRoot = sesRoot;
        SESOps.expandAllMultiAsp();
    }

    public void expandMultiAsp(String multaspNm) {
        SESOps.sesDoc = sesDoc;
        SESOps.sesRoot = sesRoot;
        SESOps.expandMultiAsp(multaspNm);
    }
    ////////////////////////////
    public static void main(String argv[]) {
        String folderToGetSESFrom = "C:/ACTGEN/src/XML/SES/Example1/";

        ses s1 = new ses(folderToGetSESFrom + "sesForHouse.xml");
        s1.printTree();

        folderToGetSESFrom = "C:/ACTGEN/src/XML/SES/Example4/";

        ses s2 = new ses(folderToGetSESFrom + "sesForHouse.xml");

        folderToGetSESFrom = "C:/ACTGEN/src/XML/SES/Example5/";

        ses s3 = new ses(folderToGetSESFrom + "sesForHouse.xml");

        System.out.println(s3.equal(s3));
        System.out.println(s1.include(s3));
        System.exit(3);

    } // main



}
