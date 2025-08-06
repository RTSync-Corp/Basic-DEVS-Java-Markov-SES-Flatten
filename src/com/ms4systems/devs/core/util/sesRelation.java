package com.ms4systems.devs.core.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;

public class sesRelation extends ses {

    public String rootEntityName;
    public HashSet<Object> entityNames, aspectNames, specNames, multiAspectNames, varNames, allNames;
    public HashSet<Object> contextFree;
    public Hashtable<Object,HashSet<Object>> entityHasAspect, entityHasSpec, aspectHasEntity, specHasEntity, entityHasMultiAspect, multiAspectHasEntity, multiAspectHasVar, entityHasVar, aspectHasCoupling;
    public Hashtable<Object, Object> rename, varHasRange, restrictRelationFn;
    public String globals = "";

    // public GenCol.Queue specsInOrder;
    public Hashtable<Object, Object> getRelations() {
    	Hashtable<Object, Object> f = new Hashtable<Object, Object>();
        f.put("entityHasAspect", entityHasAspect);
        f.put("entityHasSpec", entityHasSpec);
        f.put("aspectHasEntity", aspectHasEntity);
        f.put("specHasEntity", specHasEntity);
        f.put("entityHasMultiAspect", entityHasMultiAspect);
        f.put("multiAspectHasEntity", multiAspectHasEntity);
        f.put("multiAspectHasVar", multiAspectHasVar);
        f.put("entityHasVar", entityHasVar);
        f.put("aspectHasCoupling", aspectHasCoupling);
        f.put("varHasRange", varHasRange);
        return f;
    }

    public Hashtable<Object, Object> getEnsembleSets() {
    	Hashtable<Object, Object> f = new Hashtable<Object, Object>();
        f.put("entityNames", entityNames);
        f.put("aspectNames", aspectNames);
        f.put("specNames", specNames);
        f.put("multiAspectNames", multiAspectNames);
        f.put("varNames", varNames);
        f.put("allNames", allNames);
        return f;
    }

    @SuppressWarnings("unchecked")
	public HashSet<Object> getEnsembleSet(String name) {
        return (HashSet<Object>) getEnsembleSets().get(name);
    }

    @SuppressWarnings("unchecked")
	public Hashtable<Object, HashSet<Object>> getRelation(String name) {
        return (Hashtable<Object, HashSet<Object>>) getRelations().get(name);
    }

    public sesRelation() {
        super();
        rootEntityName = "unknown";
        entityNames = new HashSet<Object>();
        aspectNames = new HashSet<Object>();
        specNames = new HashSet<Object>();
        multiAspectNames = new HashSet<Object>();
        varNames = new HashSet<Object>();
        allNames = new HashSet<Object>();
        contextFree = new HashSet<Object>();
        
        // relation
        entityHasAspect = new Hashtable<Object,HashSet<Object>>();
        entityHasMultiAspect = new Hashtable<Object,HashSet<Object>>();
        entityHasSpec = new Hashtable<Object,HashSet<Object>>();
        entityHasVar = new Hashtable<Object,HashSet<Object>>();
        aspectHasEntity = new Hashtable<Object,HashSet<Object>>();
        specHasEntity = new Hashtable<Object,HashSet<Object>>();
        multiAspectHasEntity = new Hashtable<Object,HashSet<Object>>();
        multiAspectHasVar = new Hashtable<Object,HashSet<Object>>();
        aspectHasCoupling = new Hashtable<Object,HashSet<Object>>();
        // function
        rename = new Hashtable<Object,Object>();
        varHasRange = new Hashtable<Object,Object>();
        restrictRelationFn = new Hashtable<Object,Object>();
    }

    public sesRelation(String xmlFile) {
        super(xmlFile);
        rootEntityName = sesRoot.getAttribute("name");
        allNames = new HashSet<Object>();
        entityNames = super.getNames("entity");
        allNames.addAll(entityNames);
        aspectNames = super.getNames("aspect");
        allNames.addAll(aspectNames);
        specNames = super.getNames("specialization");
        allNames.addAll(specNames);
        multiAspectNames = super.getNames("multiAspect");
        allNames.addAll(multiAspectNames);
        varNames = super.getNames("var");
        contextFree = new HashSet<Object>();

        entityHasAspect = super.type1HasType2("entity", "aspect");
        entityHasMultiAspect = super.type1HasType2("entity", "multiAspect");
        entityHasSpec = super.type1HasType2("entity", "specialization");
        entityHasVar = entityHasVarVal(); 
        // taking in attribute node as a whole as used in ATCGEn
        aspectHasEntity = super.type1HasType2("aspect", "entity");
        specHasEntity = super.type1HasType2("specialization", "entity");
        multiAspectHasEntity = super.type1HasType2("multiAspect", "entity");
        multiAspectHasVar = super.type1HasType2("multiAspect",
                "numberComponentsVar");

        aspectHasCoupling = new Hashtable<Object,HashSet<Object>>();
        transferCoupling();
        varHasRange = new Hashtable<Object,Object>();
        rename = new Hashtable<Object,Object>();
        restrictRelationFn = new Hashtable<Object,Object>();
    }

    public String getRootEntityName() {
        return rootEntityName;
    }

    public void transferCoupling() {
        Iterator<Object> it = aspectNames.iterator();
        while (it.hasNext()) {
            String aspect = (String) it.next();
            Element el = SESOps.getElement("aspect", aspect);
            String s = el.getAttribute("coupling");

            while (true) {
                if (s.equals("") || s.contains("unknown")) {
                    break;
                }
                int firstLeftParen = s.indexOf("{");
                int firstRightParen = s.indexOf("}");
                if (firstLeftParen > -1) {
                    String fstring = s.substring(firstLeftParen + 1,
                            firstRightParen);
                    Hashtable<Object,Object> f = parseCoupling(fstring);
                    addCouplingToAspect(f, aspect);

                    s = s.substring(firstRightParen + 1, s.length());
                }
            }
        }
    }

    public static Hashtable<Object,Object> parseCoupling(String fstring) {
    	Hashtable<Object,Object> f = new Hashtable<Object,Object>();
    	HashSet<Object> es = new HashSet<Object>();
        es.add("source");
        es.add("destination");
        es.add("outport");
        es.add("inport");
        Iterator<Object> it = es.iterator();
        while (it.hasNext()) {
            String role = (String) it.next();
            int roleInd = fstring.indexOf(role);
            if (roleInd > -1) {
                String rolepart = fstring.substring(roleInd + role.length(),
                        fstring.length());
                int firstComma = rolepart.indexOf(",");
                if (firstComma > -1) {
                    rolepart = rolepart.substring(1, firstComma);
                }
                rolepart = rolepart.replaceFirst("=", "");
                f.put(role, rolepart);
            }
        }
        return f;
    }

    public sesRelation(String xmlFile, boolean max) {
        super(xmlFile);
        rootEntityName = sesRoot.getAttribute("name");
        allNames = new HashSet<Object>();
        entityNames = super.getNames("entity");
        allNames.addAll(entityNames);
        aspectNames = super.getNames("aspect");
        allNames.addAll(aspectNames);
        specNames = super.getNames("specialization");
        allNames.addAll(specNames);
        multiAspectNames = super.getNames("multiAspect");
        allNames.addAll(multiAspectNames);
        varNames = super.getNames("var");
        contextFree = new HashSet<Object>();

        entityHasAspect = super.type1HasAllType2("entity", "aspect");
        entityHasMultiAspect = super.type1HasAllType2("entity", "multiAspect");
        entityHasSpec = super.type1HasAllType2("entity", "specialization");
        entityHasVar = entityHasVarVal(); 
        // taking in attribute node as a whole as used in ATCGEn
        aspectHasEntity = super.type1HasAllType2("aspect", "entity");
        specHasEntity = super.type1HasAllType2("specialization", "entity");
        multiAspectHasEntity = super.type1HasAllType2("multiAspect", "entity");
        multiAspectHasVar = super.type1HasAllType2("multiAspect",
                "numberComponentsVar");

        aspectHasCoupling = new Hashtable<Object,HashSet<Object>>();
        transferCoupling();
        varHasRange = new Hashtable<Object,Object>();
        rename = new Hashtable<Object,Object>();
        restrictRelationFn = new Hashtable<Object,Object>();
    }

    public sesRelation(Document doc) {
        super(doc);
        rootEntityName = sesRoot.getAttribute("name");
        allNames = new HashSet<Object>();
        entityNames = super.getNames("entity");
        allNames.addAll(entityNames);
        aspectNames = super.getNames("aspect");
        allNames.addAll(aspectNames);
        specNames = super.getNames("specialization");
        allNames.addAll(specNames);
        multiAspectNames = super.getNames("multiAspect");
        allNames.addAll(multiAspectNames);
        varNames = super.getNames("var");
        contextFree = new HashSet<Object>();

        entityHasAspect = super.type1HasAllType2("entity", "aspect");
        entityHasMultiAspect = super.type1HasAllType2("entity", "multiAspect");
        entityHasSpec = super.type1HasAllType2("entity", "specialization");
        entityHasVar = entityHasVarVal(); 
        // taking in attribute node as a whole as used in ATCGEn
        aspectHasEntity = super.type1HasAllType2("aspect", "entity");
        specHasEntity = super.type1HasAllType2("specialization", "entity");
        multiAspectHasEntity = super.type1HasAllType2("multiAspect", "entity");
        multiAspectHasVar = super.type1HasAllType2("multiAspect",
                "numberComponentsVar");

        aspectHasCoupling = new Hashtable<Object,HashSet<Object>>();
        transferCoupling();
        varHasRange = new Hashtable<Object,Object>();
        rename = new Hashtable<Object,Object>();
        restrictRelationFn = new Hashtable<Object,Object>();
    }

    public void setRoot(String rootEntityName) {
        this.rootEntityName = rootEntityName;
        entityNames.add(rootEntityName);  //bpz
    }

    public static Hashtable<Object,HashSet<Object>> copyRelation(Hashtable<Object,HashSet<Object>> r) {
    	Hashtable<Object,HashSet<Object>> n = new Hashtable<Object,HashSet<Object>>();
    	Enumeration<Object> e = r.keys();
        
        while (e.hasMoreElements()) {
            Object key = e.nextElement();
            HashSet<Object> value = r.get(key);
            n.put(key, value);
        }
        return n;
    }

    public static Hashtable<Object,Object> copyFunction(Hashtable<Object,Object> r) {
    	Hashtable<Object,Object> n = new Hashtable<Object,Object>();
    	Enumeration<Object> e = r.keys();
        
        while (e.hasMoreElements()) {
            Object key = e.nextElement();
            Object value = r.get(key);
            n.put(key, value);
        }
        return n;
    }

    public void copyFrom(sesRelation ses) {
        rootEntityName = ses.rootEntityName;
        entityNames = new HashSet<Object>(ses.entityNames);
        aspectNames = new HashSet<Object>(ses.aspectNames);
        specNames = new HashSet<Object>(ses.specNames);
        multiAspectNames = new HashSet<Object>(ses.multiAspectNames);
        varNames = new HashSet<Object>(ses.varNames);
        allNames = new HashSet<Object>(ses.allNames);

        entityHasAspect = copyRelation(ses.entityHasAspect);
        entityHasMultiAspect = copyRelation(ses.entityHasMultiAspect);
        entityHasSpec = copyRelation(ses.entityHasSpec);

        entityHasVar = copyRelation(ses.entityHasVar);
        varHasRange = copyFunction(ses.varHasRange);
        aspectHasEntity = copyRelation(ses.aspectHasEntity);
        specHasEntity = copyRelation(ses.specHasEntity);
        multiAspectHasEntity = copyRelation(ses.multiAspectHasEntity);

        multiAspectHasVar = copyRelation(ses.multiAspectHasVar);
        aspectHasCoupling = copyRelation(ses.aspectHasCoupling);
    }
    @SuppressWarnings("unchecked")
	public void refineSpecWithLike(){
    	Hashtable<Object,HashSet<Object>> newEntityHasSpec = new Hashtable<Object,HashSet<Object>>();
    	Enumeration<Object> en = entityHasSpec.keys();
    	while(en.hasMoreElements()){
    		String entity = (String)en.nextElement();
    		//spec name set
    		HashSet<Object> valSet = new HashSet<Object>();
    		Iterator<Object> it = entityHasSpec.get(entity).iterator();
    		while(it.hasNext()){
    			String specName = (String)it.next();
    			// spec Name is created with like keyword
    			if(!specName.startsWith(entity)){
    				int index = specName.indexOf("-");
    				String newSpecName = entity+specName.substring(index);
    				HashSet<Object> entSet = specHasEntity.get(specName);
    				specHasEntity.put(newSpecName, new HashSet<Object>(entSet));
    				valSet.add(newSpecName);
    			}else {
    				valSet.add(specName);
    			}
    		}
    		newEntityHasSpec.put(entity, valSet);
    	}
    	if(newEntityHasSpec.size()>0){
    		entityHasSpec = copyRelation(newEntityHasSpec);
    	}
    }
    public sesRelation makeCopy() {
        sesRelation ses = new sesRelation();
        ses.copyFrom(this);
        return ses;
    }

    public Hashtable<Object,HashSet<Object>> entityHasVarVal() {
    	Hashtable<Object,HashSet<Object>> r = new Hashtable<Object,HashSet<Object>>();
        SESOps.sesDoc = sesDoc;
        SESOps.sesRoot = sesRoot;
        HashSet<Object> ents = getNames("entity");
        if (ents.equals(null)) {
            return r;
        }
        Iterator<Object> it = ents.iterator();
        while (it.hasNext()) {
            String entNm = (String) it.next();
            Element el = SESOps.getElement("entity", entNm);
            if (el.equals(null)) {
                continue;
            }
            LinkedList<Object> vars = SESOps.getElementChildrenOfElement(el, "var");
            Iterator<Object> iq = vars.iterator();
            while (iq.hasNext()) {
                Element var = (Element) iq.next();
                if(r.containsKey(entNm)){
                	r.get(entNm).add(var.getAttributes());
                }else {
                	HashSet<Object> value = new HashSet<Object>();
                	value.add(var.getAttributes());
                	r.put(entNm, value);
                }
            }
        }
        return r;
    }

    public HashSet<Object> getRelNames(String type) { // can't over-ride super
        if (type.equals("entity")) {
            return entityNames;
        } else if (type.equals("var")) {
            return varNames;
        } else if (type.equals("aspect")) {
            return aspectNames;
        } else if (type.equals("multiAspect")) {
            return multiAspectNames;
        } else if (type.equals("specialization")) {
            return specNames;
        } else if (type.equals("all")) {
            return allNames;
        } else {
            return null;
        }
    }

    public String isOfType(String name) {
        if (entityNames.contains(name)) {
            return "entity";
        }
        if (varNames.contains(name)) {
            return "var";
        }
        if (aspectNames.contains(name)) {
            return "aspect";
        }
        if (multiAspectNames.contains(name)) {
            return "multiAspect";
        }
        if (specNames.contains(name)) {
            return "specialization";
        }
        if (allNames.contains(name)) {
            return "all";
        } else {
            return null;
        }
    }

    public boolean equalNames(sesRelation se, String type) {
        HashSet<Object> seNames = se.getRelNames(type);
        HashSet<Object> myNames = getRelNames(type);
        return seNames.containsAll(myNames) && myNames.containsAll(seNames);
    }

    public HashSet<Object> commonNames(sesRelation se, String type) {
    	HashSet<Object> seNames = new HashSet<Object>(se.getRelNames(type));
        // to avoid having side effect on se by doing retainAll
    	HashSet<Object> Names = getRelNames(type);
        seNames.retainAll(getRelNames(type));
        return seNames;
    }

    public HashSet<Object> commonNames(sesRelation se) {
        return commonNames(se, "all");
    }

    public static boolean isInRangeOf(Hashtable<Object,HashSet<Object>> r, String item) {
    	HashSet<Object> valueSet = new HashSet<Object>();
    	Enumeration<Object> e = r.keys();
    	while(e.hasMoreElements()){
    		Object key = e.nextElement();
    		HashSet<Object> value = r.get(key);
    		LinkedList<Object> queue = new LinkedList<Object>();
    		queue.retainAll(value);
    		valueSet.add(queue.getFirst());
    	}
        return valueSet.contains(item);
    }

    public boolean entityIsOrphan(String entity) {
        if (isInRangeOf(aspectHasEntity, entity)) {
            return false;
        }
        if (isInRangeOf(specHasEntity, entity)) {
            return false;
        }
        if (isInRangeOf(multiAspectHasEntity, entity)) {
            return false;
        } else {
            return true;
        }
    }

    public boolean aspectIsOrphan(String aspect) {
        if (isInRangeOf(entityHasAspect, aspect)) {
            return false;
        } else {
            return true;
        }
    }

    public boolean specIsOrphan(String spec) {
        if (isInRangeOf(entityHasSpec, spec)) {
            return false;
        } else {
            return true;
        }
    }

    public Hashtable<Object,HashSet<Object>> entityHasAspect() {
        return entityHasAspect;
    }

    public Hashtable<Object,HashSet<Object>> entityHasMultiAspect() {
        return entityHasMultiAspect;
    }

    public Hashtable<Object,HashSet<Object>> entityHasSpec() {
        return entityHasSpec;
    }

    public Hashtable<Object,HashSet<Object>> aspectHasEntity() {
        return aspectHasEntity;
    }

    public Hashtable<Object,HashSet<Object>> multiAspectHasEntity() {
        return multiAspectHasEntity;
    }

    public Hashtable<Object,HashSet<Object>> multiAspectHasVar() {
        return multiAspectHasVar;
    }

    public Hashtable<Object,HashSet<Object>> specHasEntity() {
        return specHasEntity;
    }
    public Hashtable<Object,HashSet<Object>> aspectHasCoupling(){
    	return aspectHasCoupling;
    }
    public Hashtable<Object,HashSet<Object>> entityHasVar() {
        return entityHasVar;
    }
    
    public Hashtable<Object,HashSet<Object>> type1HasType2(String type1, String type2) {
        if (type1.equals("entity") && type2.equals("aspect")) {
            return entityHasAspect;
        } else if (type1.equals("entity") && type2.equals("multiAspect")) {
            return entityHasMultiAspect;
        } else if (type1.equals("entity") && type2.equals("specialization")) {
            return entityHasSpec;
        } else if (type1.equals("entity") && type2.equals("var")) {
            return entityHasVar;
        } else if (type1.equals("aspect") && type2.equals("entity")) {
            return aspectHasEntity;
        } else if (type1.equals("specialization") && type2.equals("entity")) {
            return specHasEntity;
        } else if (type1.equals("multiAspect") && type2.equals("entity")) {
            return multiAspectHasEntity;
        } else if (type1.equals("multiAspect") && type2.equals("NumberComponentsVar")) {
            return multiAspectHasVar;
        } else {
            return null;
        }
    }

    public Hashtable<Object,HashSet<Object>> typeHasVar(String type) {
        return type1HasType2(type, "var");
    }

    // ////////////////////////
    public boolean includeEntityHasAspect(sesRelation se) {
        return include(se, "entity", "aspect");
    }

    public boolean includeEntityHasMultiAspect(sesRelation se) {
        return include(se, "entity", "multiAspect");
    }

    public boolean includeAspectHasEntity(sesRelation se) {
        return include(se, "aspect", "entity");
    }

    public boolean includeMultiAspectHasEntity(sesRelation se) {
        return include(se, "multiAspect", "entity");
    }

    public boolean includeEntityHasSpec(sesRelation se) {
        return include(se, "entity", "specialization");
    }

    public boolean includeEntityHasVar(sesRelation se) {
        return include(se, "entity", "var");
    }

    public boolean includeMultiAspectHasVar(sesRelation se) {
        return include(se, "multiAspect", "NumberComponentsVar");
    }

    public boolean includeSpecHasEntity(sesRelation se) {
        return include(se, "specialization", "entity");
    }

    public boolean equal(sesRelation se, String type1, String type2) {
        return include(se, type1, type2) && se.include(this, type1, type2);
    }

    public boolean equalEntityHasAspect(sesRelation se) {
        return equal(se, "entity", "aspect");
    }

    public boolean equalEntityHasMultiAspect(sesRelation se) {
        return equal(se, "entity", "multiAspect");
    }

    public boolean equalAspectHasEntity(sesRelation se) {
        return equal(se, "aspect", "entity");
    }

    public boolean equalMultiAspectHasEntity(sesRelation se) {
        return equal(se, "multiAspect", "entity");
    }

    public boolean equalEntityHasSpec(sesRelation se) {
        return equal(se, "entity", "specialization");
    }

    public boolean equalEntityHasVar(sesRelation se) {
        return equal(se, "entity", "var");
    }

    public boolean equalMultiAspectHasVar(sesRelation se) {
        return equal(se, "multiAspect", "NumberComponentsVar");
    }

    public boolean equalSpecHasEntity(sesRelation se) {
        return equal(se, "specialization", "entity");
    }

    // //////////////////////////
    public boolean include(sesRelation se, String item, String type1,
            String type2) {
        // item is of type1
        Hashtable<Object,HashSet<Object>> r1 = type1HasType2(type1, type2);
        HashSet<Object> es1 = r1.get(item);
        Hashtable<Object,HashSet<Object>> r2 = se.type1HasType2(type1, type2);
        HashSet<Object> es2 = r2.get(item);
        return es1.containsAll(es2);
    }

    public boolean include(sesRelation se, String type1, String type2) {
        if (getRelNames(type1).isEmpty() && !se.getRelNames(type1).isEmpty()) {
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

    public boolean include(sesRelation se) {
        return includeEntityHasAspect(se) && includeEntityHasMultiAspect(se) && includeAspectHasEntity(se) && includeMultiAspectHasEntity(se) && includeEntityHasSpec(se) && includeEntityHasVar(se) && includeMultiAspectHasVar(se) && includeSpecHasEntity(se);
    }

    public boolean equal(sesRelation se) {
        return equalEntityHasAspect(se) && equalEntityHasMultiAspect(se) && equalAspectHasEntity(se) && equalMultiAspectHasEntity(se) && equalEntityHasSpec(se) && equalEntityHasVar(se) && equalMultiAspectHasVar(se) && equalSpecHasEntity(se);
    }

    public HashSet<Object> matching(sesRelation ses, HashSet<Object> entities) {
    	HashSet<Object> es = new HashSet<Object>();
        Iterator<Object> it = entities.iterator();
        while (it.hasNext()) {
            String entity = (String) it.next();
            sesRelation thisSes = extractSesFromEntity(entity);
            sesRelation otherSes = ses.extractSesFromEntity(entity);
            if (thisSes.equal(otherSes)) {
                es.add(entity);
            }
        }
        return es;
    }

    public HashSet<Object> matching(sesRelation ses) {
    	HashSet<Object> comEnts = commonNames(ses, "entity");
        return matching(ses, comEnts);
    }

    // /////////////////////////////
    public sesRelation extractSesFromEntity(String entity) {
        sesRelation ses = new sesRelation();
        ses.rootEntityName = entity;
        populateEntityHasAspect(ses, entity);
        populateEntityHasSpec(ses, entity);
        populateEntityHasMultiAspect(ses, entity);
        populateEntityHasVar(ses, entity);
        return ses;
    }

    public void populateEntityHasAspect(sesRelation ses, String entity) {
        ses.entityNames.add(entity);
        ses.allNames.add(entity);
        HashSet<Object> aspects = entityHasAspect.get(entity);
        Iterator<Object> ia = aspects.iterator();
        while (ia.hasNext()) {
            String asp = (String) ia.next();
            
            if(ses.entityHasAspect.containsKey(entity)){
            	ses.entityHasAspect.get(entity).add(asp);
            }else {
            	HashSet<Object> value = new HashSet<Object>();
            	value.add(asp);
            	ses.entityHasAspect.put(entity, value);
            }
            
            populateAspectHasEntity(ses, asp);
        }
    }

    public void populateAspectHasEntity(sesRelation ses, String aspect) {
        ses.aspectNames.add(aspect);
        ses.allNames.add(aspect);
        HashSet<Object> entities = aspectHasEntity.get(aspect);
        Iterator<Object> ia = entities.iterator();
        while (ia.hasNext()) {
            String ent = (String) ia.next();
            if(ses.aspectHasEntity.containsKey(aspect)){
            	ses.aspectHasEntity.get(aspect).add(ent);
            }else {
            	HashSet<Object> value = new HashSet<Object>();
            	value.add(ent);
            	ses.aspectHasEntity.put(aspect, value);
            }
            
            populateEntityHasAspect(ses, ent);
            populateEntityHasMultiAspect(ses, ent);
            populateEntityHasSpec(ses, ent);
            populateEntityHasVar(ses, ent);
        }
    }

    public void populateEntityHasMultiAspect(sesRelation ses, String entity) {
        ses.entityNames.add(entity);
        ses.allNames.add(entity);
        HashSet<Object> aspects = entityHasMultiAspect.get(entity);
        Iterator<Object> ia = aspects.iterator();
        while (ia.hasNext()) {
            String asp = (String) ia.next();
            if(ses.entityHasMultiAspect.containsKey(entity)){
            	ses.entityHasMultiAspect.get(entity).add(asp);
            }else {
            	HashSet<Object> value = new HashSet<Object>();
            	value.add(asp);
            	ses.entityHasMultiAspect.put(entity, value);
            }
            populateMultiAspectHasEntity(ses, asp);
            populateMultiAspectHasVar(ses, asp);
        }
    }

    public void populateMultiAspectHasEntity(sesRelation ses, String aspect) {
        ses.multiAspectNames.add(aspect);
        ses.allNames.add(aspect);
        HashSet<Object> entities = multiAspectHasEntity.get(aspect);
        Iterator<Object> ia = entities.iterator();
        while (ia.hasNext()) {
            String ent = (String) ia.next();
            if(ses.multiAspectHasEntity.containsKey(aspect)){
            	ses.multiAspectHasEntity.get(aspect).add(ent);
            }else {
            	HashSet<Object> value = new HashSet<Object>();
            	value.add(ent);
            	ses.multiAspectHasEntity.put(aspect, value);
            }
            populateEntityHasAspect(ses, ent);
            populateEntityHasMultiAspect(ses, ent);
            populateEntityHasSpec(ses, ent);
            populateEntityHasVar(ses, ent);
        }
    }

    public void populateEntityHasVar(sesRelation ses, String entity) {
        HashSet<Object> vars = entityHasVar.get(entity);
        Iterator<Object> ia = vars.iterator();
        while (ia.hasNext()) {
            Object var = ia.next();
            if(ses.entityHasVar.containsKey(entity)){
            	ses.entityHasVar.get(entity).add(var);
            }else {
            	HashSet<Object> value = new HashSet<Object>();
            	value.add(var);
            	ses.entityHasMultiAspect.put(entity, value);
            }
        }
    }

    public void populateMultiAspectHasVar(sesRelation ses, String aspect) {
    	HashSet<Object> hvalue = multiAspectHasVar.get(aspect);
    	LinkedList<Object> queue = new LinkedList<Object>();
    	queue.retainAll(hvalue);
        String var = (String) queue.getFirst();
        if(ses.multiAspectHasVar.containsKey(aspect)){
        	ses.multiAspectHasVar.get(aspect).add(var);
        }else {
        	HashSet<Object> value = new HashSet<Object>();
        	value.add(var);
        	ses.multiAspectHasVar.put(aspect, value);
        }
    }

    public void populateEntityHasSpec(sesRelation ses, String entity) {
        ses.entityNames.add(entity);
        ses.allNames.add(entity);
        HashSet<Object> specializations = entityHasSpec.get(entity);
        Iterator<Object> ia = specializations.iterator();
        while (ia.hasNext()) {
            String spec = (String) ia.next();
            if(ses.entityHasSpec.containsKey(entity)){
            	ses.entityHasSpec.get(entity).add(spec);
            }else {
            	HashSet<Object> value = new HashSet<Object>();
            	value.add(spec);
            	ses.entityHasSpec.put(entity, value);
            }
            populateSpecHasEntity(ses, spec);
        }
    }

    public void populateSpecHasEntity(sesRelation ses, String specialization) {
        ses.specNames.add(specialization);
        HashSet<Object> entities = specHasEntity.get(specialization);
        Iterator<Object> ia = entities.iterator();
        while (ia.hasNext()) {
            String ent = (String) ia.next();
            if(ses.specHasEntity.containsKey(specialization)){
            	ses.specHasEntity.get(specialization).add(ent);
            }else {
            	HashSet<Object> value = new HashSet<Object>();
            	value.add(ent);
            	ses.specHasEntity.put(specialization, value);
            }
            populateEntityHasSpec(ses, ent);
            populateEntityHasAspect(ses, ent);
            populateEntityHasVar(ses, ent);
        }
    }

    // /////////////////////////////
    public void createSesDoc() {
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

    public void toDOM() {
        toDOMFrom(rootEntityName);
        SESOps.sesRoot = SESOps.getSesDoc().getDocumentElement();
    }

    public void toDOMFrom(String entity) {
        createSesDoc();
        sesRoot.setAttribute("name", entity);
        convertEntity(sesRoot, entity);
        SESOps.sesDoc = sesDoc;
        SESOps.allNames = allNames;
        SESOps.entityNames = entityNames;
        SESOps.aspectNames = aspectNames;
        SESOps.specNames = specNames;
        SESOps.multiAspectNames = multiAspectNames;
        SESOps.varNames = varNames;
        System.out.println("Overlap in names: " + (allNames.size() != entityNames.size() + aspectNames.size() + specNames.size() + multiAspectNames.size()));
        System.out.println("Overlap vars and entity names: " + SESOps.checkIntersect(entityNames, varNames));
        System.out.println("Names have internal spaces :" + SESOps.haveSpace(allNames));
        System.out.println("Names have  spaces :" + SESOps.haveSpace(varNames)); // bpz

    }

    public void convertEntity(Element el, String entity) {
        if (entity == null) {
            return;
        }
        HashSet<Object> aspects = entityHasAspect.get(entity);
        if(aspects!=null){
	        Iterator<Object> ia = aspects.iterator();
	        while (ia.hasNext()) {
	            String aspNm = (String) ia.next();
	            Element aspect = sesDoc.createElement("aspect");
	            el.appendChild(aspect);
	            aspect.setAttribute("name", aspNm);
	            convertAspect(aspect, aspNm);
	        }
        }
        HashSet<Object> multiAspects = entityHasMultiAspect.get(entity);
        if(multiAspects != null){
	        Iterator<Object> im = multiAspects.iterator();
	        while (im.hasNext()) {
	            String aspNm = (String) im.next();
	            Element aspect = sesDoc.createElement("multiAspect");
	            el.appendChild(aspect);
	            aspect.setAttribute("name", aspNm);
	            convertMultiAspect(aspect, aspNm);
	        }
        }
        HashSet<Object> specs = entityHasSpec.get(entity);
        if(specs!=null){
	        Iterator<Object> is = specs.iterator();
	        while (is.hasNext()) {
	            String specNm = (String) is.next();
	            Element spec = sesDoc.createElement("specialization");
	            el.appendChild(spec);
	            spec.setAttribute("name", specNm);
	            convertSpec(spec, specNm);
	        }
        }
        HashSet<Object> vars = entityHasVar.get(entity);
        if(vars != null){
	        Iterator<Object> iv = vars.iterator();
	        while (iv.hasNext()) {
	            Element var = sesDoc.createElement("var");
	            el.appendChild(var);
	            Object varNmOrAtt = iv.next();
	            if (varNmOrAtt instanceof String) {
	                var.setAttribute("name", (String) varNmOrAtt);
	                String range = (String) varHasRange.get(entity + "." + (String) varNmOrAtt);
	                if (range != null) {
	                    if (range.startsWith("string") // check if just string
	                            && range.length() > "string".length()) {
	                        Element simpleRef = sesDoc.createElement("simpleReference");
	                        String s[] = getPartsForString(range, "string".length());
	                        simpleRef.setAttribute("name", "string" + s[0]);
	                        simpleRef.setAttribute("restrictionValuePairs", s[1]);
	                        simpleRef.setAttribute("restrictionBase", "string");
	                        var.appendChild(simpleRef);
	                        var.setAttribute("rangeSpec", "string" + s[2]);
	
	                    } else if ((range.startsWith("int") && range.length() > "int".length()) || (range.startsWith("double") && range.length() > "double".length())) {
	                        Element simpleRef = sesDoc.createElement("simpleReference");
	                        String type = "int";
	                        if (range.startsWith("double")) {
	                            type = "double";
	                        }
	                        String s[] = getPartsForString(range, type.length());
	                        simpleRef.setAttribute("name", type + s[0]);
	                        simpleRef.setAttribute("restrictionValuePairs", s[1]);
	                        simpleRef.setAttribute("restrictionBase", type);
	                        var.appendChild(simpleRef);
	                        var.setAttribute("rangeSpec", type + s[2]);
	                    } else {
	                        var.setAttribute("rangeSpec", range);
	                    }
	                } else {
	                    var.setAttribute("rangeSpec", "double");
	                }
	            } else {
	                NamedNodeMap m = (NamedNodeMap) varNmOrAtt;
	                if (m == null) {
	                    continue;
	                }
	                for (int i = 0; i < m.getLength(); i++) {
	                    Attr n = (Attr) m.item(i);
	                    Attr ne = (Attr) sesDoc.importNode(n, true);
	                    var.setAttributeNode(ne);
	                }
	            }
	        }
        }
    }

    public String[] getPartsForString(String rangeSpec, int index) {
        if (rangeSpec.endsWith("interval")) {
            return getPartsForInt(rangeSpec.substring(0, rangeSpec.length() - 8), index);
        }
        String s[] = new String[]{"", "", ""};
        if (index >= rangeSpec.length()) {
            s[0] = rangeSpec;
            s[1] += ",";
            ;
        } else {
            s[2] = " with values ";
            Pattern p = Pattern.compile(",");
            String enumprt = rangeSpec.substring(index + 1,
                    rangeSpec.length() - 1);
            String[] groups = p.split(enumprt);
            String first = groups[0].trim();
            s[0] += first;
            s[1] += "enumeration, " + first + " ,";
            if (groups.length > 1) {
                for (int i = 1; i < groups.length; i++) {
                    groups[i] = groups[i].trim();
                    s[0] += groups[i];
                    s[1] += "enumeration, " + groups[i] + " ,";
                    s[2] += groups[i] + " ,";
                }
                s[2] += " and " + first;
            } else {
                s[2] += first;
            }
        }
        return s;
    }


    public String[] getPartsForInt(String rangeSpec, int index) {
        String s[] = new String[]{"", "", ""};
        Pattern p = Pattern.compile(",");
        String endToend = rangeSpec.substring(index, rangeSpec.length());
        String minMax = rangeSpec.substring(index + 1, rangeSpec.length() - 1);

        String[] groups = p.split(minMax);
        if (groups.length == 2) {
            for (int i = 0; i < 2; i++) {
                groups[i] = groups[i].trim();
                s[0] += groups[i];
                if (i == 0) {
                    if (endToend.startsWith("[")) {
                        s[1] += "minInclusive, " + groups[i] + " ,";
                    } else {
                        s[1] += "minExclusive, " + groups[i] + " ,";
                    }
                } else if (i == 1) {
                    if (endToend.endsWith("]")) {
                        s[1] += "maxInclusive, " + groups[i] + " ,";
                    } else {
                        s[1] += "maxExclusive, " + groups[i] + " ,";
                    }
                }
            }
            s[2] = minMax;
        } else {
            groups = p.split(minMax);
            for (int i = 0; i < groups.length; i++) {
                groups[i] = groups[i].trim();
                s[0] += groups[i];
                s[1] += "enumeration, " + groups[i] + " ,";
            }
        }
        return s;
    }

    public void convertAspect(Element el, String aspect) {
    	if(aspectHasCoupling.get(aspect)!=null){
	    	HashSet<Object> es = new HashSet<Object>(aspectHasCoupling.get(aspect));
	        Iterator<Object> it = es.iterator();
	        while (it.hasNext()) {
	            Hashtable<Object,Object> f = (Hashtable<Object,Object>) it.next();
	            String coupling = el.getAttribute("coupling");
	            el.setAttribute("coupling", coupling + "\n" + f.toString());
	        }
        }
        HashSet<Object> entities = aspectHasEntity.get(aspect);
        if(entities != null){
	        Iterator<Object> ia = entities.iterator();
	        while (ia.hasNext()) {
	            String entNm = (String) ia.next();
	            Element entity = sesDoc.createElement("entity");
	            el.appendChild(entity);
	            entity.setAttribute("name", entNm);
	            convertEntity(entity, entNm);
	        }
        }
    }

    public void convertMultiAspect(Element el, String aspect) {
    	if(aspectHasCoupling.get(aspect)!=null){
	    	HashSet<Object> es = new HashSet<Object>(aspectHasCoupling.get(aspect));
	        Iterator<Object> it = es.iterator();
	        while (it.hasNext()) {
	        	Hashtable<Object,Object> f = (Hashtable<Object,Object>) it.next();
	            String coupling = el.getAttribute("coupling");
	            el.setAttribute("coupling", coupling + "\n" + f.toString());
	        }
    	}
        // bpz Jan 006
        LinkedList<Object> queue = new LinkedList<Object>();
        if(multiAspectHasVar.get(aspect)!=null){
        	queue.addAll(multiAspectHasEntity.get(aspect));
	        String numVar = (String)queue.getFirst();
	        Element numvar = sesDoc.createElement("numberComponentsVar");
	        el.appendChild(numvar);
	        numvar.setAttribute("name", numVar);
	        numvar.setAttribute("min", "0");
	        numvar.setAttribute("max", "10");
        }
        queue = new LinkedList<Object>();
        if(multiAspectHasEntity.get(aspect)!= null){
        	queue.addAll(multiAspectHasEntity.get(aspect));
                if (queue != null && queue.size() !=0){
	        String entNm = (String)queue.getFirst();
	        Element entity = sesDoc.createElement("entity");
	        el.appendChild(entity);
	        entity.setAttribute("name", entNm);
	        convertEntity(entity, entNm);
        }
        }
    }

    public void convertSpec(Element el, String spec) {
    	if(specHasEntity.get(spec)!=null){
	        HashSet<Object> entities = specHasEntity.get(spec);
	        Iterator<Object>ia = entities.iterator();
	        while (ia.hasNext()) {
	            String entNm = (String) ia.next();
	            Element entity = sesDoc.createElement("entity");
	            el.appendChild(entity);
	            entity.setAttribute("name", entNm);
	            convertEntity(entity, entNm);
	        }
    	}
    }

    // /////////////////////////////
    public static HashSet<Object> replace(HashSet<Object> es, String orig,
            String newname) {
        boolean removed = es.remove(orig);
        if (removed) {
            es.add(newname);
        }
        return es;
    }

    public static Hashtable<Object,HashSet<Object>> replaceKey(Hashtable<Object,HashSet<Object>> r, String orig,
            String newname) {
        
    	HashSet<Object> vals = new HashSet<Object>();
    	Enumeration<Object> e = r.keys();
    	while(e.hasMoreElements()){
    		Object key = e.nextElement();
    		if(key.equals(orig)){
    			HashSet<Object> value = r.get(key);
    			Iterator<Object> it = value.iterator();
    			while(it.hasNext()){
    				vals.add(it.next());
    			}
    		}
    	}
    	r.remove(orig);
    	r.put(newname, vals);
            
        return r;
    }

    public static Hashtable<Object,HashSet<Object>> replaceValue(Hashtable<Object,HashSet<Object>> r, String orig,
            String newname) {
        
    	HashSet<Object> keys = new HashSet<Object>();
        Enumeration<Object> e = r.keys();
        while(e.hasMoreElements()){
        	Object key = e.nextElement();
        	HashSet<Object> value = r.get(key);
        	LinkedList <Object> queue = new LinkedList<Object>();
        	queue.retainAll(value);
                if (queue.size() != 0){
        	Object firstValue = queue.getFirst();
        	if(firstValue.equals(orig)){
        		keys.add(key);
        		queue.remove(firstValue);
        	}
                }
        }
        Iterator<Object>it = keys.iterator();
        while(it.hasNext()){
        	Object key = it.next();
        	if(r.containsKey(key)){
        		r.get(key).add(newname);
        	}else {
        		HashSet<Object> value = new HashSet<Object>();
        		value.add(newname);
        		r.put(key, value);
        	}
        }      
        
        return r;
    }

    public static Hashtable<Object,HashSet<Object>> replaceBoth(Hashtable<Object,HashSet<Object>> r, String orig,
            String newname) {

        Hashtable<Object,HashSet<Object>> rep = replaceKey(r, orig, newname);
        rep = replaceValue(rep, orig, newname);
        return rep;
    }

    public void replaceAll(String orig, String newname) {
        entityNames = replace(entityNames, orig, newname);
        aspectNames = replace(aspectNames, orig, newname);
        specNames = replace(specNames, orig, newname);
        multiAspectNames = replace(multiAspectNames, orig, newname);
        varNames = replace(varNames, orig, newname);
        allNames = replace(allNames, orig, newname);

        entityHasAspect = replaceBoth(entityHasAspect, orig, newname);
        entityHasMultiAspect = replaceBoth(entityHasMultiAspect, orig, newname);
        entityHasSpec = replaceBoth(entityHasSpec, orig, newname);
        entityHasVar = replaceBoth(entityHasVar, orig, newname);
        aspectHasEntity = replaceBoth(aspectHasEntity, orig, newname);
        specHasEntity = replaceBoth(specHasEntity, orig, newname);
        multiAspectHasEntity = replaceBoth(multiAspectHasEntity, orig, newname);
        multiAspectHasVar = replaceBoth(multiAspectHasVar, orig, newname);
        aspectHasCoupling = replaceBoth(aspectHasCoupling, orig, newname);
    }

    public void addRename(String orig, String newname) {
        rename.put(orig, newname);
    }

    public void setRename(Hashtable<Object,Object> f) {
        rename = f;
    }

    public Hashtable<Object,Object> getRename() {
        return rename;
    }

    public void replaceAll() {
    	Enumeration<Object> e = rename.keys();
    	while(e.hasMoreElements()){
    		String orig = (String)e.nextElement();
    		String newname = (String)rename.get(orig);
    		replaceAll(orig,newname);
    	}
        toDOM();
    }

    public void printCommonality(sesRelation ses) {
    	HashSet<Object> seNames = new HashSet<Object>(ses.getRelNames("entity"));
        seNames.addAll(entityNames);
        System.out.println("All entities : " + seNames);

        HashSet<Object> commEnts = commonNames(ses, "entity");

        System.out.println("Common entities : " + commEnts);

        System.out.println("Common entities percent: " + (100.00 * commEnts.size() / seNames.size()));
        HashSet<Object> matchSub = matching(ses);
        System.out.println("Matching entities : " + matchSub);
        System.out.println("Common subSESes percent: " + (100.00 * matchSub.size() / commEnts.size()));
    }

    // /////////////////////////////
    public static HashSet<Object> remove(HashSet<Object> es, String orig) {
        es.remove(orig);
        return es;
    }

    public static Hashtable<Object,HashSet<Object>> removeKey(Hashtable<Object,HashSet<Object>> r, String orig) {
        {
            r.remove(orig);
            return r;
        }
    }

    public static Hashtable<Object,HashSet<Object>> removeValue(Hashtable<Object,HashSet<Object>> r, Object orig) {
    	HashSet<Object> keys = new HashSet<Object>();
    	Enumeration<Object> e = r.keys();
    	while(e.hasMoreElements()){
    		Object key = e.nextElement();
    		HashSet<Object> value = r.get(key);
    		LinkedList<Object> queue = new LinkedList<Object>();
    		queue.addAll(value);
    		Iterator<Object> it = queue.iterator();
    		while(it.hasNext()){
    			Object element = it.next();
    			if(element.equals(orig)){
    				value.remove(element);
                                break;
    			}
    		}
    	}
        return r;
    }

    public static Hashtable<Object,HashSet<Object>> removeBoth(Hashtable<Object,HashSet<Object>> r, String orig) {
        Hashtable<Object,HashSet<Object>> rep = removeKey(r, orig);
        rep = removeValue(rep, orig);
        return rep;
    }

    public void removeSetOfItem(Hashtable<Object,HashSet<Object>> r, String item) {
        while (true) {
        	HashSet<Object> es = r.get(item);
            if (es == null || es.isEmpty()) {
                return;
            }
            LinkedList<Object> queue = new LinkedList<Object>();
            queue.retainAll(es);
            queue.removeFirst();
            break;
        }
    }

    public void removeEntity(String entity) {
        removeValue(aspectHasEntity, entity);
        removeValue(specHasEntity, entity);
        removeValue(multiAspectHasEntity, entity);

        removeEntityHasAspect(entity);
        removeEntityHasSpec(entity);
        removeEntityHasMultiAspect(entity);
        removeEntityHasVar(entity);
        clean();
    }

    public void removeSpec(String spec) {
        removeValue(entityHasSpec, spec);
        clean();
    }

    public void removeAsp(String asp) {
        removeValue(entityHasAspect, asp);
        clean();
    }

    public void removeEntityHasAspect(String entity) {
        removeSetOfItem(entityHasAspect, entity);
    }

    public void removeEntityHasSpec(String entity) {
        removeSetOfItem(entityHasSpec, entity);
    }

    public void removeEntityHasMultiAspect(String entity) {
        removeSetOfItem(entityHasMultiAspect, entity);
    }

    public void removeEntityHasVar(String entity) {
        removeSetOfItem(entityHasVar, entity);
    }

    public void clean() {
    }

    public String toString() {
        String s = "\n ===============================================";
        s += "\n Relations for " + rootEntityName;
        s += "\n ===============================================";
        s += "\n entityHasAspect \n" + entityHasAspect();
        s += "\n entityHasMultiAspect \n" + entityHasMultiAspect();
        s += "\n entityHasSpec \n" + entityHasSpec();
        s += "\n entityHasVar \n" + entityHasVar();
        s += "\n aspectHasEntity \n" + aspectHasEntity();
        s += "\n multiAspectHasEntity \n" + multiAspectHasEntity();
        s += "\n multiAspectHasVar \n" + multiAspectHasVar();
        s += "\n specHasEntity \n" + specHasEntity();
        s += "\n aspectHasCoupling \n"+ aspectHasCoupling();
        return s;
    }

    public void printRelations() {
        System.out.println(this);
    }

    // ///////////////////////////////////
    public HashSet<Object> getPreImages(String item, Hashtable<Object,HashSet<Object>> r) {
        HashSet<Object> es = new HashSet<Object>();
        Enumeration<Object> e = r.keys();
        while(e.hasMoreElements()){
        	Object key = e.nextElement();
        	HashSet<Object> value = r.get(key);
        	Iterator<Object> it = value.iterator();
        	while(it.hasNext()){
        		if(it.next().equals(item)){
        			es.add(key);
        		}
        	}
        }
        return es;
    }

    public HashSet<Object> getAspectParents(String entNm) {
    	HashSet<Object> es = getPreImages(entNm, aspectHasEntity);
        return es;
    }

    public HashSet<Object> getMultiAspectParents(String entNm) {
    	HashSet<Object> es = getPreImages(entNm, multiAspectHasEntity);
        return es;
    }

    public HashSet<Object> getSpecializationParents(String entNm) {
    	HashSet<Object> es = getPreImages(entNm, specHasEntity);
        return es;
    }

    public HashSet<Object> getEntityParentsOfAspect(String aspNm) {
    	HashSet<Object> es = getPreImages(aspNm, entityHasAspect);
        es.addAll(getPreImages(aspNm, entityHasMultiAspect));
        return es;
    }

    public HashSet<Object> getParentsViaAspect(String entNm) {
    	HashSet<Object> es = getAspectParents(entNm);
        es.addAll(getMultiAspectParents(entNm));
        Iterator<Object> it = es.iterator();
        HashSet<Object> er = new HashSet<Object>();
        while (it.hasNext()) {
            String asp = (String) it.next();
            er.addAll(getEntityParentsOfAspect(asp));
        }
        return er;
    }

    public HashSet<Object> getPathsViaAspect(String entNm) {
    	HashSet<Object> es = getParentsViaAspect(entNm);
    	HashSet<Object> er = new HashSet<Object>();
        Iterator<Object> it = es.iterator();
        while (it.hasNext()) {
            String parent = (String) it.next();
            er.add(entNm + ".partOf." + parent);
        }
        return er;
    }

    public HashSet<Object> getSpecParents(String entNm) {
        return getPreImages(entNm, specHasEntity);
    }

    public HashSet<Object> getEntityParentsOfSpec(String aspNm) {
        return getPreImages(aspNm, entityHasSpec);
    }

    public HashSet<Object> getParentsViaSpec(String entNm) {
    	HashSet<Object> es = getSpecParents(entNm);
        Iterator<Object> it = es.iterator();
        HashSet<Object> er = new HashSet<Object>();
        while (it.hasNext()) {
            String spec = (String) it.next();
            er.addAll(getEntityParentsOfSpec(spec));
        }
        return er;
    }

    public HashSet<Object> getPathsViaSpec(String entNm) {
    	HashSet<Object> es = getParentsViaSpec(entNm);
    	HashSet<Object> er = new HashSet<Object>();
        Iterator<Object> it = es.iterator();
        while (it.hasNext()) {
            String parent = (String) it.next();
            er.add(entNm + ".typeOf." + parent);
        }
        return er;
    }

    public HashSet<Object> getParentPaths(String entNm) {
    	HashSet<Object> es = getPathsViaAspect(entNm);
        es.addAll(getPathsViaSpec(entNm));
        return es;
    }


    public int getNumberOccurrences(String entNm) {
        return getAspectParents(entNm).size() + getSpecParents(entNm).size();
    }

    // //////////////////////////////
    public HashSet<Object> uniqueIDsForEntity(String entNm) {
    	HashSet<Object> es = uniqueIDsForEntityViaAspect(entNm);
        es.addAll(uniqueIDsForEntityViaSpec(entNm));
        if (es.isEmpty() || contextFree.contains(entNm)) {
            es.add(entNm);
        }
        return es;
    }

    public HashSet<Object> uniqueIDsForEntityViaAspect(String entNm) {
    	HashSet<Object> es = getAspectParents(entNm);
    	HashSet<Object> er = new HashSet<Object>();
        if (es.isEmpty()) {
            return er;
        }
        Iterator<Object> it = es.iterator();
        while (it.hasNext()) {
            String parent = (String) it.next();
            HashSet<Object> ep = uniqueIDsForAspect(parent);
            if (ep.size() <= 1) {
                er.add(entNm);
                return er;
            }
            Iterator<Object> ip = ep.iterator();
            while (ip.hasNext()) {
                String id = (String) ip.next();
                er.add(id + "." + entNm);
            }
        }
        return er;
    }

    public HashSet<Object> uniqueIDsForEntityViaSpec(String entNm) {
    	HashSet<Object> es = getSpecParents(entNm);
    	HashSet<Object> er = new HashSet<Object>();
        if (es.isEmpty()) {
            return er;
        }

        Iterator<Object> it = es.iterator();
        while (it.hasNext()) {
            String parent = (String) it.next();
            HashSet<Object> ep = uniqueIDsForSpec(parent);
            if (ep.size() <= 1) {
                er.add(entNm);
                return er;
            }

            Iterator<Object> ip = ep.iterator();
            while (ip.hasNext()) {
                String id = (String) ip.next();
                er.add(id + "." + entNm);
            }
        }
        return er;
    }

    public HashSet<Object> uniqueIDsForAspect(String aspNm) {
    	HashSet<Object> es = getEntityParentsOfAspect(aspNm);
    	HashSet<Object> er = new HashSet<Object>();
        if (es.size() <= 1 || contextFree.contains(aspNm)) {
            er.add(aspNm);
            return er;
        }
        Iterator<Object> it = es.iterator();
        while (it.hasNext()) {
            String parent = (String) it.next();
            HashSet<Object> ep = uniqueIDsForEntity(parent);
            if (ep.size() >= 1) {
                Iterator<Object> ip = ep.iterator();
                while (ip.hasNext()) {
                    String id = (String) ip.next();
                    er.add(id + "." + aspNm);
                }
            }
        }
        if (er.isEmpty()) {
            er.add(aspNm);
        }
        return er;
    }

    public HashSet<Object> uniqueIDsForSpec(String specNm) {
    	HashSet<Object> es = getEntityParentsOfSpec(specNm);
    	HashSet<Object> er = new HashSet<Object>();
        if (es.isEmpty() || contextFree.contains(specNm)) {
            er.add(specNm);
            return er;
        }
        Iterator<Object> it = es.iterator();
        while (it.hasNext()) {
            String parent = (String) it.next();
            HashSet<Object> ep = uniqueIDsForEntity(parent);
            Iterator<Object> ip = ep.iterator();
            while (ip.hasNext()) {
                String id = (String) ip.next();
                er.add(id + "." + specNm);
            }
        }
        return er;
    }

    public HashSet<Object> uniqueIDsForSpecs() {
    	HashSet<Object> es = new HashSet<Object>();
        Iterator<Object> it = specNames.iterator();
        while (it.hasNext()) {
            String specNm = (String) it.next();
            es.addAll(uniqueIDsForSpec(specNm));
        }
        return es;
    }

    public HashSet<Object> uniqueIDsForEntities() {
    	HashSet<Object> es = new HashSet<Object>();
        Iterator<Object> it = entityNames.iterator();
        while (it.hasNext()) {
            String entNm = (String) it.next();
            es.addAll(uniqueIDsForEntity(entNm));
        }
        return es;
    }

    public String getNmFrom(String IDNm) {
        String nM = IDNm;
        int lastDot = IDNm.lastIndexOf(".");
        if (lastDot > -1) {
            nM = IDNm.substring(lastDot + 1, IDNm.length());
        }
        return nM;
    }

    public Hashtable<Object,HashSet<Object>> replaceUniqueIDs(Hashtable<Object,HashSet<Object>> r) {
        Hashtable<Object,HashSet<Object>> res = new Hashtable<Object,HashSet<Object>>();
        Enumeration<Object> e = r.keys();
        while(e.hasMoreElements()){
        	String key = (String)e.nextElement();
        	HashSet<Object> value = r.get(key);
        	
        	Iterator<Object> it = value.iterator();
        	while(it.hasNext()){
        		String val = (String)it.next();
        		HashSet<Object> keySet;
            	if (key.endsWith("Spec") || key.endsWith("Type")) {
                    keySet = uniqueIDsForSpec(key);
                } else if (key.endsWith("Dec") || key.endsWith("Asp")) {
                    keySet = uniqueIDsForAspect(key);

                } else {
                    keySet = uniqueIDsForEntity(key);
                }
            	
            	Iterator<Object> ik = keySet.iterator();
                while (ik.hasNext()) {
                    String newkey = (String) ik.next();
                    String newval = newkey + "." + val;
                    if(res.containsKey(newkey)){
                    	res.get(newkey).add(newval);
                    }else {
                    	HashSet<Object> valSet = new HashSet<Object>();
                    	valSet.add(newval);
                    	res.put(newkey, valSet);
                    }
                    
                }
        	}       	
        }
       
        return res;
    }

    public sesRelation longForm() {
        sesRelation ses = new sesRelation();
        return ses;
    }

    public void printTree() {
        if (!checkStrictHier()) {
            return;
        }
        this.toDOM();
        super.printTree();
    }

    public String printTreeString() {
        if (!checkStrictHier()) {
            return "";
        }
        this.toDOM();
        return super.printTreeString();
    }

    // ////////////////// generate graph and check hierarchy
    public void generateItemsGraph() {
    	HashSet<Object> ens = new HashSet<Object>();
        ens.add(rootEntityName);
        generateGraph(ens, new HashSet<Object>(), new HashSet<Object>());
    }

    public void generateGraph(HashSet<Object> entities,
    		HashSet<Object> aspects, HashSet<Object> specs) {
    	HashSet<Object> newentities = new HashSet<Object>(entities);
    	if(newentities==null)return;
        int niter = 0;
        while (niter++ < 100) {

        	HashSet<Object> newaspects = new HashSet<Object>();
        	HashSet<Object> newspecs = new HashSet<Object>();

            Iterator<Object> ie = newentities.iterator();
            while (ie.hasNext()) {
                String entity = (String) ie.next();
                newaspects.addAll(entityHasAspect.get(entity));
                newaspects.addAll(entityHasMultiAspect.get(entity));
                newspecs.addAll(entityHasSpec.get(entity));
            }
            //

            newaspects.removeAll(aspects);
            //
            newspecs.removeAll(specs);

            Iterator<Object> ia = newaspects.iterator();
            while (ia.hasNext()) {
                String aspect = (String) ia.next();
                newentities.addAll(aspectHasEntity.get(aspect));
                newentities.addAll(multiAspectHasEntity.get(aspect));
            }

            Iterator<Object> is = newspecs.iterator();
            while (is.hasNext()) {
                String spec = (String) is.next();
                newentities.addAll(specHasEntity.get(spec));
            }
            //
            newentities.removeAll(entities);

            if (newaspects.isEmpty() && newspecs.isEmpty() && newentities.isEmpty()) {
                String s = "\n ===============================================";
                s += "\n Items for " + rootEntityName;
                s += "\n ===============================================" + "\n";

                System.out.println(s + "entities" + "\n" + entities);
                System.out.println("aspects" + "\n" + aspects);
                System.out.println("specializations" + "\n" + specs);

                return;
            }
            aspects.addAll(newaspects);
            specs.addAll(newspecs);
            entities.addAll(newentities);
        }
    }

    public boolean checkStrictHier() {
        boolean result = checkHierEntity(rootEntityName);
        if (result) {
            System.out.println("Strict Hierarchy Axiom is satisfied");
            return true;
        }
        return false;
    }

    public boolean checkHierEntity(String entityPath) {

        boolean aspHier = true;
        boolean multAspHier = true;
        boolean specHier = true;

        String entity = getNmFrom(entityPath);
        HashSet<Object> aspects = entityHasAspect.get(entity);
        if(aspects!=null){ 
	        Iterator<Object> ia = aspects.iterator();
	        while (ia.hasNext()) {
	            String aspNm = (String) ia.next();
	            if (validateSes.onPath(entityPath, aspNm)) {
	                System.out.println("repeat aspect found on path: " + entityPath + "." + aspNm);
	                return false;
	            }
	            aspHier = checkHierAspect(entityPath + "." + aspNm);
	        }
        }

        HashSet<Object> multiAspects = entityHasMultiAspect.get(entity);
        if(multiAspects != null){
        	Iterator<Object> ia = multiAspects.iterator();
	        while (ia.hasNext()) {
	            String aspNm = (String) ia.next();
	            if (validateSes.onPath(entityPath, aspNm)) {
	                System.out.println("repeat multAspect found on path: " + entityPath + "." + aspNm);
	                return false;
	            }
	            multAspHier = checkHierMultiAspect(entityPath + "." + aspNm);
	        }
        }
        HashSet<Object> specs = entityHasSpec.get(entity);
        if(specs != null){
	        Iterator<Object> is = specs.iterator();
	        while (is.hasNext()) {
	            String specNm = (String) is.next();
	            if (validateSes.onPath(entityPath, specNm)) {
	                System.out.println("repeat spec found on path: " + entityPath + "." + specNm);
	                return false;
	            }
	
	            specHier = checkHierSpec(entityPath + "." + specNm);
	        }
        }
        return aspHier && multAspHier && specHier;
    }

    public boolean checkHierAspect(String aspectPath) {
        String aspect = getNmFrom(aspectPath);
        HashSet<Object> entities = aspectHasEntity.get(aspect);
        boolean result = true;
        if(entities != null){
	        Iterator<Object> ie = entities.iterator();
	        
	        while (ie.hasNext()) {
	            String entNm = (String) ie.next();
	            if (validateSes.onPath(aspectPath, entNm)) {
	                System.out.println("repeat entity found on path: " + aspectPath + "." + entNm);
	                return false;
	            }
	            result = result && checkHierEntity(aspectPath + "." + entNm);
	        }
        }
        return result;
    }

    public boolean checkHierMultiAspect(String aspectPath) {
        String aspect = getNmFrom(aspectPath);
        HashSet<Object> entities = multiAspectHasEntity.get(aspect);
        boolean result = true;
        if(entities!=null){
	        Iterator<Object> ie = entities.iterator();
	        
	        while (ie.hasNext()) {
	            String entNm = (String) ie.next();
	            if (validateSes.onPath(aspectPath, entNm)) {
	                System.out.println("repeat entity found on path: " + aspectPath + "." + entNm);
	                return false;
	            }
	            result = result && checkHierEntity(aspectPath + "." + entNm);
	        }
        }
        return result;
    }

    public boolean checkHierSpec(String specPath) {
        String spec = getNmFrom(specPath);
        HashSet<Object> entities = specHasEntity.get(spec);
        boolean result = true;
        if(entities !=null){
	        Iterator<Object> ie = entities.iterator();
	        
	        while (ie.hasNext()) {
	            String entNm = (String) ie.next();
	            if (validateSes.onPath(specPath, entNm)) {
	                System.out.println("repeat entity found on path: " + specPath + "." + entNm);
	                return false;
	            }
	            result = result && checkHierEntity(specPath + "." + entNm);
	        }
        }
        return result;
    }

    public void writeSesDoc(String xmlFile, String folderFordtd) {
        toDOM();
        SESOps.writeSesDoc(xmlFile, folderFordtd);
    }

    public void restoreSesDoc(String xmlFile) {

        validateSes.restoreSesDoc(xmlFile);
    }

    public void writeDTDToXML(String dtdFile) {
        toDOM();
        validateSes.writeDTDToXML(dtdFile); // needs aspectNames
    }

    public static void addAspectToEntityContextFree(sesRelation ses,
            String aspect, String entity) {
        ses.contextFree.add(aspect);
        addAspectToEntity(ses, aspect, entity);
    }

    public static void addAspectToEntity(sesRelation ses, String aspect,
            String entity) {
        if(ses.entityHasAspect.containsKey(entity)){
        	ses.entityHasAspect.get(entity).add(aspect);
        }else{
        	HashSet<Object> valSet = new HashSet<Object>();
        	valSet.add(aspect);
        	ses.entityHasAspect.put(entity, valSet);
        }
        ses.entityNames.add(entity);
        ses.allNames.add(entity);
        ses.aspectNames.add(aspect);
        ses.allNames.add(aspect);
    }

    public static void addSpecToEntityContextFree(sesRelation ses, String spec,
            String entity) {
        ses.contextFree.add(spec);
        addSpecToEntity(ses, spec, entity);
    }

    public static void addSpecToEntity(sesRelation ses, String spec,
            String entity) {
        if(ses.entityHasSpec.containsKey(entity)){
        	ses.entityHasSpec.get(entity).add(spec);
        }else{
        	HashSet<Object> valSet = new HashSet<Object>();
        	valSet.add(spec);
        	ses.entityHasSpec.put(entity, valSet);
        }
        ses.entityNames.add(entity);
        ses.allNames.add(entity);
        ses.specNames.add(spec);
        ses.allNames.add(spec);
    }

    public static void addMultiAspectToEntityContextFree(sesRelation ses,
            String multiAspect, String entity) {
        ses.contextFree.add(multiAspect);
        addMultiAspectToEntity(ses, multiAspect, entity);
    }

    public static void addMultiAspectToEntity(sesRelation ses, String aspect,
            String entity) {
        if(ses.entityHasMultiAspect.containsKey(entity)){
        	ses.entityHasMultiAspect.get(entity).add(aspect);
        }else{
        	HashSet<Object> valSet = new HashSet<Object>();
        	valSet.add(aspect);
        	ses.entityHasMultiAspect.put(entity, valSet);
        }
        ses.entityNames.add(entity);
        ses.allNames.add(entity);
        ses.multiAspectNames.add(aspect);
        ses.allNames.add(aspect);
        String value = "numContainedIn" + entity ;
        if(ses.multiAspectHasVar.containsKey(aspect)){
        	ses.multiAspectHasVar.get(aspect).add(value);
        }else{
        	HashSet<Object> valSet = new HashSet<Object>();
        	valSet.add(value);
        	ses.multiAspectHasVar.put(aspect, valSet);
        }
    }

    public static void addEntityToAspectContextFree(sesRelation ses,
            String entity, String aspect) {
        ses.contextFree.add(aspect);
        addEntityToAspect(ses, entity, aspect);
    }

    public static void addEntityToAspect(sesRelation ses, String entity,
            String aspect) {
        if(ses.aspectHasEntity.containsKey(aspect)){
        	ses.aspectHasEntity.get(aspect).add(entity);
        }else{
        	HashSet<Object> valSet = new HashSet<Object>();
        	valSet.add(entity);
        	ses.aspectHasEntity.put(aspect, valSet);
        }
        ses.entityNames.add(entity);
        ses.allNames.add(entity);
        ses.aspectNames.add(aspect);
        ses.allNames.add(aspect);
    }

    public static void addEntityToSpecContextFree(sesRelation ses,
            String entity, String spec) {
        ses.contextFree.add(spec);
        addEntityToSpec(ses, entity, spec);
    }

    public static void addEntityToSpec(sesRelation ses, String entity,
            String spec) {
        if(ses.specHasEntity.containsKey(spec)){
        	ses.specHasEntity.get(spec).add(entity);
        }else{
        	HashSet<Object> valSet = new HashSet<Object>();
        	valSet.add(entity);
        	ses.specHasEntity.put(spec, valSet);
        }
        ses.entityNames.add(entity);
        ses.allNames.add(entity);
        ses.specNames.add(spec);
        ses.allNames.add(spec);
    }

    public static void addEntityToMultiAspectContextFree(sesRelation ses,
            String entity, String multiAspect) {
        ses.contextFree.add(multiAspect);
        addEntityToMultiAspect(ses, entity, multiAspect);
    }

    public static void addEntityToMultiAspect(sesRelation ses, String entity,
            String aspect) {
        if(ses.multiAspectHasEntity.containsKey(aspect)){
        	ses.multiAspectHasEntity.get(aspect).add(entity);
        }else{
        	HashSet<Object> valSet = new HashSet<Object>();
        	valSet.add(entity);
        	ses.multiAspectHasEntity.put(aspect, valSet);
        }
        ses.entityNames.add(entity);
        ses.allNames.add(entity);
        ses.multiAspectNames.add(aspect);
        ses.allNames.add(aspect);
    }

    public static void addVarToEntity(sesRelation ses, String var, String entity) {
        if(ses.entityHasVar.containsKey(entity)){
        	ses.entityHasVar.get(entity).add(var);
        }else{
        	HashSet<Object> valSet = new HashSet<Object>();
        	valSet.add(var);
        	ses.entityHasVar.put(entity, valSet);
        }
        ses.entityNames.add(entity);
        ses.allNames.add(entity);
    }

    public static void addRangeToVar(sesRelation ses, String range, String var) {
        ses.varHasRange.put(var, range);
    }

    public static void addFunctionToEntity(sesRelation ses, String fn,
            String entity) {
        String value = "function: " + fn;
        if(ses.entityHasVar.containsKey(entity)){
        	ses.entityHasVar.get(entity).add(value);
        }else{
        	HashSet<Object> valSet = new HashSet<Object>();
        	valSet.add(value);
        	ses.entityHasVar.put(entity, valSet);
        }
        ses.entityNames.add(entity);
        ses.allNames.add(entity);
    }

    public static void addCouplingToAspect(sesRelation ses, Hashtable<Object,Object> fn,
            String aspect) {
    	if(ses.aspectHasCoupling.containsKey(aspect)){
    		ses.aspectHasCoupling.get(aspect).add(fn);
    	}else {
    		HashSet<Object> valSet = new HashSet<Object>();
    		valSet.add(fn);
    		ses.aspectHasCoupling.put(aspect, valSet);
    	}
    }

    // //////////////////////////
    public void addAspectToEntityContextFree(String aspect, String entity) {
        addAspectToEntityContextFree(this, aspect, entity);
    }

    public void addAspectToEntity(String aspect, String entity) {
        addAspectToEntity(this, aspect, entity);
    }

    public void addSpecToEntityContextFree(String spec, String entity) {
        addSpecToEntityContextFree(this, spec, entity);
    }

    public void addSpecToEntity(String spec, String entity) {
        addSpecToEntity(this, spec, entity);
    }

    public void addMultiAspectToEntityContextFree(String multiAspect,
            String entity) {
        addMultiAspectToEntityContextFree(this, multiAspect, entity);
    }

    public void addMultiAspectToEntity(String aspect, String entity) {
        addMultiAspectToEntity(this, aspect, entity);
    }

    public void addEntityToAspectContextFree(String entity, String aspect) {
        addAspectToEntityContextFree(this, entity, aspect);
    }

    public void addEntityToAspect(String entity, String aspect) {
        addEntityToAspect(this, entity, aspect);
    }

    public void addEntityToSpecContextFree(String entity, String spec) {
        addSpecToEntityContextFree(this, entity, spec);
    }

    public void addEntityToSpec(String entity, String spec) {
        addEntityToSpec(this, entity, spec);
    }

    public void addEntityToMultiAspectContextFree(String entity,
            String multiAspect) {
        addMultiAspectToEntityContextFree(this, entity, multiAspect);
    }

    public void addEntityToMultiAspect(String entity, String aspect) {
        addEntityToMultiAspect(this, entity, aspect);
    }

    public void addVarToEntity(String var, String entity) {
        addVarToEntity(this, var, entity);
    }

    public void addRangeToVar(String range, String var) {
        addRangeToVar(this, range, var);
    }

    public void addFunctionToEntity(String fn, String entity) {
        addFunctionToEntity(this, fn, entity);
    }

    public void addCouplingToAspect(Hashtable<Object,Object> fn, String aspect) {
        addCouplingToAspect(this, fn, aspect);
    }

    // //////////////////////////
    public static Hashtable<Object,HashSet<Object>> merge(Hashtable<Object,HashSet<Object>> r, Hashtable<Object,HashSet<Object>> s) {
        Hashtable<Object,HashSet<Object>> res = new Hashtable<Object,HashSet<Object>>();
        Enumeration<Object> e = r.keys();
        while(e.hasMoreElements()){
        	Object key = e.nextElement();
        	HashSet<Object> value = r.get(key);
        	if(res.containsKey(key)){
        		res.get(key).addAll(value);
        	}else{
        		res.put(key, value);
        	}
        }
        e = s.keys();
        while(e.hasMoreElements()){
        	Object key = e.nextElement();
        	HashSet<Object> value = s.get(key);
        	if(res.containsKey(key)){
        		res.get(key).addAll(value);
        	}else{
        		res.put(key, value);
        	}
        }
        return res;
    }
    // merge functions 
    public static Hashtable<Object,Object> mergeFunction(Hashtable<Object,Object> r, Hashtable<Object,Object> s) {
    	Hashtable<Object,Object> res = new Hashtable<Object,Object>();
    	Enumeration<Object> e = r.keys();
    	while(e.hasMoreElements()){
    		Object key = e.nextElement();
    		Object value = r.get(key);
    		res.put(key, value);
    	}
    	e = s.keys();
    	while(e.hasMoreElements()){
    		Object key = e.nextElement();
    		Object value = s.get(key);
    		res.put(key, value);
    	}        
        return res;
    }

    public void mergeSeS(sesRelation r) {
        this.entityHasAspect = merge(this.entityHasAspect, r.entityHasAspect);
        this.entityHasSpec = merge(this.entityHasSpec, r.entityHasSpec);
        this.aspectHasEntity = merge(this.aspectHasEntity, r.aspectHasEntity);
        this.specHasEntity = merge(this.specHasEntity, r.specHasEntity);
        this.entityHasMultiAspect = merge(this.entityHasMultiAspect,
                r.entityHasMultiAspect);
        this.multiAspectHasEntity = merge(this.multiAspectHasEntity,
                r.multiAspectHasEntity);
        this.multiAspectHasVar = merge(this.multiAspectHasVar,
                r.multiAspectHasVar);
        this.entityHasVar = merge(this.entityHasVar, r.entityHasVar);
        this.aspectHasCoupling = merge(this.aspectHasCoupling,
                r.aspectHasCoupling);

        this.entityNames.addAll(r.entityNames);
        this.aspectNames.addAll(r.aspectNames);
        this.specNames.addAll(r.specNames);
        this.multiAspectNames.addAll(r.multiAspectNames);
        this.varNames.addAll(r.varNames);
        this.allNames.addAll(r.allNames);
        mergeFunction(this.varHasRange, r.varHasRange);
        this.printTree();
    }

    public static void merge(sesRelation ses, sesRelation r) {
    }

    public static void addEntityToAspect(sesRelation ses,
            sesRelation forEntity, String aspect) {
        addEntityToAspect(ses, forEntity.rootEntityName, aspect);
        merge(ses, forEntity);
    }

    public static void addEntityToMultiAspect(sesRelation ses,
            sesRelation forEntity, String aspect) {
        addEntityToMultiAspect(ses, forEntity.rootEntityName, aspect);
        merge(ses, forEntity);
    }

    public void addEntityToAspect(sesRelation forEntity, String aspect) {
        addEntityToAspect(this, forEntity, aspect);
    }

    public void addEntityToMultiAspect(sesRelation forEntity, String aspect) {
        addEntityToMultiAspect(this, forEntity, aspect);
    }

    public static void addEntityToSpec(sesRelation ses, sesRelation forEntity,
            String spec) {
        addEntityToSpec(ses, forEntity.rootEntityName, spec);
        merge(ses, forEntity);
    }

    public void addEntityToSpec(sesRelation forEntity, String spec) {
        addEntityToSpec(this, forEntity, spec);
    }

    public sesRelation substructure(String entity) {
        sesRelation ses = new sesRelation();
        if (entity == null) {
            return ses;
        }
        ses.setRoot(entity);
        substructureBelowEntity(entity, ses);
        return ses;
    }

    public void substructureBelowEntity(String entity, sesRelation ses) {
        HashSet<Object> vars = entityHasVar.get(entity);
        if(vars == null) vars = new HashSet<Object>();
        ses.varNames.addAll(vars);
        Iterator<Object> iv = vars.iterator();
        while (iv.hasNext()) {
            Object o = iv.next();
            String varNm = o.toString();
            if (o instanceof String) {
            	if(ses.entityHasVar.containsKey(entity)){
            		ses.entityHasVar.get(entity).add(varNm);
            	}else{
            		HashSet<Object> value = new HashSet<Object>();
            		value.add(varNm);
            		ses.entityHasVar.put(entity, value);
            	}
                
                
                String range = (String) varHasRange.get(entity + "." + varNm);
                if (range == null) {
                    range = "string";
                }
                ses.varHasRange.put(entity + "." + varNm, range);
            } else if (o instanceof NamedNodeMap) {

                NamedNodeMap m = (NamedNodeMap) o;
                if(ses.entityHasVar.containsKey(entity)){
            		ses.entityHasVar.get(entity).add(m);
            	}else{
            		HashSet<Object> value = new HashSet<Object>();
            		value.add(m);
            		ses.entityHasVar.put(entity, value);
            	}
            }
        }

        HashSet<Object> aspects = entityHasAspect.get(entity);
        if(aspects == null) aspects=new HashSet<Object>();
        ses.aspectNames.addAll(aspects);
        Iterator<Object> ia = aspects.iterator();
        while (ia.hasNext()) {
            String aspNm = (String) ia.next();
            if(ses.entityHasAspect.containsKey(entity)){
        		ses.entityHasAspect.get(entity).add(aspNm);
        	}else{
        		HashSet<Object> value = new HashSet<Object>();
        		value.add(aspNm);
        		ses.entityHasAspect.put(entity, value);
        	}
            ses.aspectHasCoupling = this.aspectHasCoupling;
            substructureBelowAspect(aspNm, ses);
        }

        HashSet<Object> multiAspects = entityHasMultiAspect.get(entity);
        if(multiAspects == null) multiAspects = new HashSet<Object>();
        ses.multiAspectNames.addAll(multiAspects);
        ia = multiAspects.iterator();
        while (ia.hasNext()) {
            String aspNm = (String) ia.next();
            if(ses.entityHasMultiAspect.containsKey(entity)){
        		ses.entityHasMultiAspect.get(entity).add(aspNm);
        	}else{
        		HashSet<Object> value = new HashSet<Object>();
        		value.add(aspNm);
        		ses.entityHasMultiAspect.put(entity, value);
        	}
            substructureBelowMultiAspect(aspNm, ses);
        }

        HashSet<Object> specs = entityHasSpec.get(entity);
        if(specs == null) specs = new HashSet<Object>();
        ses.specNames.addAll(specs);
        Iterator<Object> is = specs.iterator();
        while (is.hasNext()) {
            String specNm = (String) is.next();
            if(ses.entityHasSpec.containsKey(entity)){
        		ses.entityHasSpec.get(entity).add(specNm);
        	}else{
        		HashSet<Object> value = new HashSet<Object>();
        		value.add(specNm);
        		ses.entityHasSpec.put(entity, value);
        	}
            substructureBelowSpec(specNm, ses);
        }
    }

    public void substructureBelowAspect(String aspect, sesRelation ses) {
    	HashSet<Object> ents = aspectHasEntity.get(aspect);
    	if(ents == null) ents = new HashSet<Object>();
        ses.entityNames.addAll(ents);
        Iterator<Object> ie = ents.iterator();
        while (ie.hasNext()) {
            String entNm = (String) ie.next();
            if(ses.aspectHasEntity.containsKey(aspect)){
        		ses.aspectHasEntity.get(aspect).add(entNm);
        	}else{
        		HashSet<Object> value = new HashSet<Object>();
        		value.add(entNm);
        		ses.aspectHasEntity.put(aspect, value);
        	}
            substructureBelowEntity(entNm, ses);
        }
    }

    public void substructureBelowMultiAspect(String aspect, sesRelation ses) {
        HashSet<Object> varSet = multiAspectHasVar.get(aspect);
        if(varSet == null) varSet = new HashSet<Object>();
        LinkedList<Object> queue = new LinkedList<Object>();
        queue.retainAll(varSet);
        if (queue.size()!=0){
        String var = (String)queue.getFirst();
        if(ses.multiAspectHasVar.containsKey(aspect)){
    		ses.multiAspectHasVar.get(aspect).add(var);
    	}else{
    		HashSet<Object> value = new HashSet<Object>();
    		value.add(var);
    		ses.multiAspectHasVar.put(aspect, value);
    	}
        }
        HashSet<Object> ents = multiAspectHasEntity.get(aspect);
        if(ents == null) ents = new HashSet<Object>();
        ses.entityNames.addAll(ents);
        Iterator<Object> ie = ents.iterator();
        while (ie.hasNext()) {
            String entNm = (String) ie.next();
            if(ses.multiAspectHasEntity.containsKey(aspect)){
        		ses.multiAspectHasEntity.get(aspect).add(entNm);
        	}else{
        		HashSet<Object> value = new HashSet<Object>();
        		value.add(entNm);
        		ses.multiAspectHasEntity.put(aspect, value);
        	}
            substructureBelowEntity(entNm, ses);
        }
    }

    public void substructureBelowSpec(String spec, sesRelation ses) {
    	HashSet<Object> ents = specHasEntity.get(spec);
    	 if(ents == null) ents = new HashSet<Object>();
        ses.entityNames.addAll(ents);
        Iterator<Object> ie = ents.iterator();
        while (ie.hasNext()) {
            String entNm = (String) ie.next();
            if(ses.specHasEntity.containsKey(spec)){
        		ses.specHasEntity.get(spec).add(entNm);
        	}else{
        		HashSet<Object> value = new HashSet<Object>();
        		value.add(entNm);
        		ses.specHasEntity.put(spec, value);
        	}
            substructureBelowEntity(entNm, ses);
        }
    }

    public static sesRelation makeMetaData() {
        sesRelation ses = new sesRelation();
        ses.setRoot("MetaData");
        ses.addAspectToEntity("MetaDataDec", "MetaData");
        ses.addEntityToAspect("urgency", "MetaDataDec");
        ses.addVarToEntity("level", "urgency");
        ses.addRangeToVar("int", "level");
        ses.addEntityToAspect("authority", "MetaDataDec");
        ses.addVarToEntity("degree", "authority");
        ses.addRangeToVar("string", "degree");
        return ses;
    }

    public static sesRelation makeMetaData(String entity, sesRelation meta) {
        sesRelation ses = new sesRelation();
        ses.setRoot(entity + "MetaData");
        ses.addAspectToEntity("metaDataAspFor" + entity, entity + "MetaData");
        ses.addEntityToAspect(entity, "metaDataAspFor" + entity);
        ses.addEntityToAspect(meta, "metaDataAspFor" + entity);
        return ses;
    }

    public void replaceEntityByMetaData(String entity, sesRelation meta) {
        HashSet<Object> aspParents = getAspectParents(entity);
        HashSet<Object> multiAspParents = getMultiAspectParents(entity);
        HashSet<Object> specParents = getSpecializationParents(entity);

        removeValue(aspectHasEntity, entity);
        removeValue(specHasEntity, entity);
        removeValue(multiAspectHasEntity, entity);

        if (rootEntityName.equals(entity)) {
            rootEntityName = entity + "MetaData";
        }

        Iterator<Object> it = aspParents.iterator();
        while (it.hasNext()) {
            addEntityToAspect(meta, (String) it.next());
        }
        it = multiAspParents.iterator();
        while (it.hasNext()) {
            addEntityToMultiAspect(meta, (String) it.next());
        }

        it = specParents.iterator();
        while (it.hasNext()) {
            addEntityToSpec(meta, (String) it.next());
        }
        merge(this, meta);
    }

    public void replaceEntityByMetaData(sesRelation meta) {
        HashSet<Object> ents = new HashSet<Object>(entityNames);
        Iterator<Object> it = ents.iterator();
        while (it.hasNext()) {
            String entNm = (String) it.next();
            sesRelation metaWEnt = sesRelation.makeMetaData(entNm, meta);
            replaceEntityByMetaData(entNm, metaWEnt);
        }
    }

    public static void writeGenericPESWMetaData(String xmlFile, String dtdFile) {
        generatePrunings.createPesDoc(SESOps.sesRoot.getAttribute("name"));
        generatePrunings.genericPESEntity(generatePrunings.pruneRoot,
                SESOps.sesRoot, generatePrunings.pruneRoot.getNodeName());
        NodeList nl = generatePrunings.pruneDoc.getElementsByTagName("MetaData");
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            Element el = (Element) n;
            Node p = el.getParentNode();
            String pNm = p.getNodeName();
            el.setAttribute("sourceEntity", pNm.substring("metaDataAspFor".length(), pNm.length()));
        }
        generatePrunings.writePruneDoc(xmlFile, dtdFile);
    }

    // ///////////////
    public void changeSpecsToAttrs() {
        Iterator<Object> it = specNames.iterator();
        while (it.hasNext()) {
            changeSpecToAttr((String) it.next());
        }
    }

    public void changeSpecToAttr(String spec) {
    	HashSet<Object> entities = specHasEntity.get(spec);
        if (!entities.isEmpty()) {
        	HashSet<Object> parEnts = getEntityParentsOfSpec(spec);
            removeValue(entityHasSpec, spec);
            Iterator<Object> is = parEnts.iterator();
            while (is.hasNext()) {
                String parEnt = (String) is.next();
                addVarToEntity(spec, parEnt);
                String s = "string[";
                Iterator<Object> it = entities.iterator();
                while (it.hasNext()) {
                    s += it.next() + ",";
                }
                s = s.substring(0, s.length() - 1);
                s += "]";
                addRangeToVar(s, parEnt + "." + spec);
            }
        }
    }

    public void changeAttrsToSpecs() {
        Iterator<Object> it = entityNames.iterator();
        while (it.hasNext()) {
            String entity = (String) it.next();
            HashSet<Object> vars = entityHasVar.get(entity);
            HashSet<Object> es = new HashSet<Object>(vars); // need copy
            Iterator<Object> iv = es.iterator();
            while (iv.hasNext()) {
                Object o = iv.next();
                if (o instanceof String) {
                    changeAttrToSpec(entity, (String) o);
                } else {
                    changeAttrToSpec(entity, (NamedNodeMap) o);
                }
            }
        }
    }

    public void changeAttrToSpec(String entity, NamedNodeMap m) {
        if (m != null) {
            Attr n = (Attr) m.item(0);
            String var = n.getValue();
            addSpecToEntity(var + "Spec", entity);
            n = (Attr) m.item(1);
            String range = n.getValue();
            addEnts(range, var + "Spec");
            removeValue(entityHasVar, m);
        }
    }

    public void changeAttrToSpec(String entity, String var) {
        addSpecToEntity(var + "Spec", entity);
        String rangeSpec = (String) varHasRange.get(entity + "." + var);
        Pattern p = Pattern.compile(",");
        int index = rangeSpec.indexOf("[");
        String enumprt = rangeSpec.substring(index + 1, rangeSpec.length() - 1);
        String[] groups = p.split(enumprt);
        for (int i = 0; i < groups.length; i++) {
            addEntityToSpec(groups[i].trim(), var + "Spec");
        }
        removeValue(entityHasVar, var);
    }

    public void addEnts(String rangeSpec, String spec) {
        HashSet<Object> dataTypes = new HashSet<Object>();
        dataTypes.add("string");
        dataTypes.add("int");
        dataTypes.add("double");
        dataTypes.add("boolean");
        if (!rangeSpec.equals("unknown")) {
            if (dataTypes.contains(rangeSpec)) {
                return;
            } else {
                int ind = rangeSpec.indexOf("with");
                if (ind == -1) {
                    return;
                }
                sesParse sp = new sesParse();
                ind = rangeSpec.indexOf("values");
                rangeSpec = rangeSpec.substring(ind + 6).trim();
                Pair pp = sp.parseConnective(rangeSpec);
                String vals = pp.getValue().toString();
                vals = vals.substring(1, vals.length() - 1);
                if (vals.equals("")) {
                    return;
                }
                Pattern p = Pattern.compile(",");
                String[] groups = p.split(vals);
                for (int i = 0; i < groups.length; i++) {
                    groups[i] = groups[i].trim();
                    addEntityToSpec(groups[i], spec);
                }
            }
        }
    }

    // /////////////////////////////////////////////
    public restrictRelation makeRelationForSpecs(String spec1, String spec2) {

        HashSet<Object> entities1 = new HashSet<Object>(specHasEntity.get(spec1));
        HashSet<Object> entities2 = new HashSet<Object>(specHasEntity.get(spec2));
        restrictRelation r = new restrictRelation(spec1, spec2, entities1,
                entities2);
        restrictRelationFn.put(spec1 + "." + spec2, r);
        return r;
    }

    public restrictRelation replaceByConverse(restrictRelation r) {
        restrictRelation rcon = r.makeConverse();
        removeRestrictRel(r);
        restrictRelationFn.put(rcon.domainSpec + "." + rcon.rangeSpec, rcon);
        return rcon;
    }

    public void restrictSpecEnts(restrictRelation r, String spec1,
            String spec2, String selentFromspec1) {
        if (selentFromspec1 == null || selentFromspec1.equals("")) {
            return;
        }
        specHasEntity.remove(spec1);
        if(specHasEntity.containsKey(spec1)){
        	specHasEntity.get(spec1).add(selentFromspec1);
        }else{
        	HashSet<Object> value = new HashSet<Object>();
        	value.add(selentFromspec1);
        	specHasEntity.put(spec1, value);
        }
        HashSet<Object> allowedVals = new HashSet<Object>(r.get(selentFromspec1));
        HashSet<Object> curVals = new HashSet<Object>(specHasEntity.get(spec2));
        curVals.retainAll(allowedVals);
        specHasEntity.remove(spec2);

        Iterator<Object> it = curVals.iterator();
        while (it.hasNext()) {
            if(specHasEntity.containsKey(spec2)){
            	specHasEntity.get(spec2).add(it.next());
            }else{
            	HashSet<Object> value = new HashSet<Object>();
            	value.add(it.next());
            	specHasEntity.put(spec2, value);
            }
        }
    }

    public restrictRelation findRestrictRel(String spec1, String spec2) {
        restrictRelation r = (restrictRelation) restrictRelationFn.get(spec1 + "." + spec2);
        return r;
    }

    public void removeRestrictRel(restrictRelation r) {
        restrictRelationFn.remove(r.domainSpec + "." + r.rangeSpec);
    }

    public LinkedList<Object> influencees(LinkedList<Object> q) {
    	LinkedList<Object> es = new LinkedList<Object>();
        Iterator<Object> it = q.iterator();
        while (it.hasNext()) {
            String spec = (String) it.next();
            Iterator<Object> is = specNames.iterator();
            while (is.hasNext()) {
                String specSecond = (String) is.next();
                restrictRelation r = findRestrictRel(spec, specSecond);
                if (r != null) {
                    es.add(specSecond);
                }
            }
        }
        return es;
    }

    public HashSet<Object> findFirst() {
    	HashSet<Object> doms = new HashSet<Object>();
    	HashSet<Object> rans = new HashSet<Object>();
    	Enumeration<Object> e = restrictRelationFn.keys();
    	while(e.hasMoreElements()){
    		String sp1sp2 = (String)e.nextElement();
            String domSpec = sp1sp2.substring(0, sp1sp2.indexOf("."));
            doms.add(domSpec);
            String ranSpec = sp1sp2.substring(sp1sp2.indexOf(".") + 1);
            rans.add(ranSpec);
    		
    	}
        
        doms.removeAll(rans);
        return doms;
    }

    public LinkedList<Object> placeInOrder() {
        if (restrictRelationFn.isEmpty()) {
            return new LinkedList<Object>();
        }
        LinkedList<Object> order = new LinkedList<Object>();
        order.retainAll(findFirst());
        if (order.isEmpty()) {
            System.out.println("There is a cycle of dependencies");
            System.exit(3);
        }
        int lastSize = 1;
        while (true) {
        	HashSet<Object> nes = new HashSet<Object>();
        	Enumeration<Object> e = restrictRelationFn.keys();
        	while(e.hasMoreElements()){
        		String sp1sp2 = (String)e.nextElement();
        		String domSpec = sp1sp2.substring(0, sp1sp2.indexOf("."));
                String ranSpec = sp1sp2.substring(sp1sp2.indexOf(".") + 1);
                if (order.contains(domSpec)) {
                    nes.add(ranSpec);
                }
        	}
            
            Iterator<Object> is = nes.iterator();
            while (is.hasNext()) {
                String sp = (String) is.next();
                if (!order.contains(sp)) {
                    order.add(sp);
                }
            }
            if (lastSize == order.size()) {
                return order;
            } else {
                lastSize = order.size();
            }
        }
    }

    public void setRelations(pes p) {
        clearRelations();
        p.getRelations(this);
    }

    public void clearRelations() {
        restrictRelationFn = new Hashtable<Object,Object>();
    }

    // /////////////////
    public void backToNatLang() {
        System.out.println(natLangToSes.backToNatLang(this));
    }

    public void backToNatLang(String folder) {
        fileHandler.writeToFile(folder + rootEntityName + "SesNatLang.ses",
                natLangToSes.backToNatLang(this));
    }

    public boolean isLeaf(String entity) {
    	HashSet<Object> aspects = entityHasAspect.get(entity);
    	if(aspects== null) aspects = new HashSet<Object>();
    	
        HashSet<Object> specs = entityHasSpec.get(entity);
        if(specs ==null) specs = new HashSet<Object>();
        
        return aspects.isEmpty() && specs.isEmpty();
    }

    // ////////////////////
    public HashSet<Object> getInports(String aspect, String compNm) {
    	HashSet<Object> couplingSet = new HashSet<Object>(aspectHasCoupling.get(aspect));
        return getInports(couplingSet, compNm);
    }

    public HashSet<Object> getInports(HashSet<Object> couplingSet, String compNm) {
    	HashSet<Object> es = new HashSet<Object>();
        Iterator<Object> it = couplingSet.iterator();
        while (it.hasNext()) {
            Hashtable<Object,Object> f = (Hashtable<Object,Object>) it.next();
            if (f.get("destination").equals(compNm)) {
                es.add(f.get("inport"));
            }
        }
        return es;
    }

    public HashSet<Object> getOutports(String aspect, String compNm) {
        HashSet<Object> couplingSet = new HashSet<Object>(aspectHasCoupling.get(aspect));
        return getOutports(couplingSet, compNm);
    }

    public HashSet<Object> getOutports(HashSet<Object> couplingSet,
            String compNm) {
    	HashSet<Object> es = new HashSet<Object>();
        Iterator<Object> it = couplingSet.iterator();
        while (it.hasNext()) {
        	Hashtable<Object,Object> f = (Hashtable<Object,Object>) it.next();
            if (f.get("source").equals(compNm)) {
                es.add(f.get("outport"));
            }
        }
        return es;
    }

    public Hashtable<Object,HashSet<Object>> findSourceNOutport(String aspect, String destination,
            String inport) {
        Hashtable<Object,HashSet<Object>> es = new Hashtable<Object,HashSet<Object>>();
        HashSet<Object> couplingSet = new HashSet<Object>(aspectHasCoupling.get(aspect));
        Iterator<Object> it = couplingSet.iterator();
        while (it.hasNext()) {
        	Hashtable<Object,Object> f = (Hashtable<Object,Object>) it.next();
            if (f.get("destination").equals(destination) && f.get("inport").equals(inport)) {
                if(es.containsKey(f.get("source"))){
                	es.get(f.get("source")).add(f.get("outport"));
                }else {
                	HashSet<Object> value = new HashSet<Object>();
                	value.add(f.get("outport"));
                	es.put(f.get("source"), value);
                }
            }
        }
        return es;
    }

    public Hashtable<Object,HashSet<Object>> findDestinationNInport(String aspect, String source,
            String outport) {
        Hashtable<Object,HashSet<Object>> es = new Hashtable<Object,HashSet<Object>>();
        HashSet<Object> couplingSet = new HashSet<Object>(aspectHasCoupling.get(aspect));
        Iterator<Object> it = couplingSet.iterator();
        while (it.hasNext()) {
        	Hashtable<Object,Object> f = (Hashtable<Object,Object>) it.next();
            if (f.get("source").equals(source) && f.get("outport").equals(outport)) {
                if(es.containsKey(f.get("destination"))){
                	es.get(f.get("destination")).add(f.get("inport"));
                }else {
                	HashSet<Object> value = new HashSet<Object>();
                	value.add(f.get("inport"));
                	es.put(f.get("destination"), value);
                }
            }
        }
        return es;
    }

    public void removeCoupling(String aspect, String source, String outport,
            String destination, String inport) {
        Hashtable<Object,HashSet<Object>> r = aspectHasCoupling;
        aspectHasCoupling = new Hashtable<Object,HashSet<Object>>();
        Enumeration<Object> e = r.keys();
        while(e.hasMoreElements()){
        	Object key = e.nextElement();
        	String asp = (String)key;
        	HashSet<Object> value = r.get(key);
        	LinkedList<Object> queue = new LinkedList<Object>();
        	queue.retainAll(value);
        	Hashtable<Object, Object> f = (Hashtable<Object,Object>)queue.getFirst();
        	if (asp.equals(aspect) && f.get("source").equals(source) && f.get("outport").equals(outport) && f.get("destination").equals(destination) && f.get("inport").equals(inport)) {
            } else {
                if(aspectHasCoupling.containsKey(asp)){
                	aspectHasCoupling.get(asp).add(f);
                }else {
                	HashSet<Object> val = new HashSet<Object>();
                	val.add(f);
                	aspectHasCoupling.put(asp, val);
                }
            }
        	
        }        
    }
    public void addIOWrap(String aspect, String compNm) {
        addEntityToAspect(compNm + "IOWrap", aspect);
        HashSet<Object> inports = getInports(aspect, compNm);
        Iterator<Object> it = inports.iterator();
        while (it.hasNext()) {
            String inport = (String) it.next();
            Hashtable<Object,HashSet<Object>> r = findSourceNOutport(aspect, compNm, inport);
            Enumeration<Object> e =r.keys();
            while(e.hasMoreElements()){
            	Object key = e.nextElement();
            	HashSet<Object> value = r.get(key);
            	Iterator<Object> ite = value.iterator();
            	while(ite.hasNext()){
            		String source = (String) key;
            		String outport = (String)ite.next();
            		removeCoupling(aspect, source, outport, compNm, inport);
                    Hashtable<Object,Object> f = new Hashtable<Object,Object>();
                    f.put("source", source);
                    f.put("outport", outport);
                    f.put("destination", compNm + "IOWrap");
                    f.put("inport", source + "." + outport + ".IOW");
                    addCouplingToAspect(f, aspect);
                    f = new Hashtable<Object,Object>();
                    f.put("source", compNm + "IOWrap");
                    f.put("outport", source + "." + outport);
                    f.put("destination", compNm);
                    f.put("inport", inport);

                    addCouplingToAspect(f, aspect);
            	}
            	
            }
        }
        HashSet<Object> outports = getOutports(aspect, compNm);
        it = outports.iterator();
        while (it.hasNext()) {
            String outport = (String) it.next();
            Hashtable<Object,HashSet<Object>> r = findDestinationNInport(aspect, compNm, outport);
            Enumeration<Object> e=r.keys();
            while(e.hasMoreElements()){
            	Object key = e.nextElement();
            	HashSet<Object> value = r.get(key);
            	Iterator<Object> ite = value.iterator();
            	while(ite.hasNext()){
            		String destination = (String)key;
            		String inport = (String)ite.next();
            		removeCoupling(aspect, compNm, outport, destination, inport);
            		Hashtable<Object,Object> f = new Hashtable<Object,Object>();
                    f.put("source", compNm);
                    f.put("outport", outport);
                    f.put("destination", compNm + "IOWrap");
                    f.put("inport", compNm + "." + outport + ".IOW");

                    addCouplingToAspect(f, aspect);
                    f = new Hashtable<Object,Object>();
                    f.put("source", compNm + "IOWrap");
                    f.put("outport", compNm + "." + outport);
                    f.put("destination", destination);
                    f.put("inport", inport);
                    if (!aspectHasEntity.get(aspect).contains(destination)) {
                        f.put("inport", compNm + "." + outport);
                    }
                    addCouplingToAspect(f, aspect);
            	}
            }
        }
        if (!compNm.equals(rootEntityName)) {
            addRename(rootEntityName, rootEntityName + "IOW");
            replaceAll();
            rootEntityName = rootEntityName + "IOW";
        }
    }

    public void addObserver(String aspect, String compNm) {
        addEntityToAspect(compNm + "Observer", aspect);
        HashSet<Object> inports = getInports(aspect, compNm);
        Iterator<Object> it = inports.iterator();
        while (it.hasNext()) {
            String inport = (String) it.next();
            Hashtable<Object,HashSet<Object>> r = findSourceNOutport(aspect, compNm, inport);
            Enumeration<Object> e = r.keys();
            while(e.hasMoreElements()){
            	Object key = e.nextElement();
            	HashSet<Object> value = r.get(key);
            	Iterator<Object> ite = value.iterator();
            	while(ite.hasNext()){
            		String source = (String)key;
            		String outport = (String)ite.next();
            		Hashtable<Object,Object> f = new Hashtable<Object,Object>();
                    f.put("source", source);
                    f.put("outport", outport);
                    f.put("destination", compNm + "Observer");
                    f.put("inport", source + "." + outport + ".Obs");
                    addCouplingToAspect(f, aspect);
            	}
            }
        }
        HashSet<Object> outports = getOutports(aspect, compNm);
        it = outports.iterator();
        while (it.hasNext()) {
            String outport = (String) it.next();
            Hashtable<Object,HashSet<Object>> r = findDestinationNInport(aspect, compNm, outport);
            Enumeration<Object> e = r.keys();
            while(e.hasMoreElements()){
            	Object key = e.nextElement();
            	HashSet<Object> value = r.get(key);
            	Iterator<Object> ite = value.iterator();
            	while(ite.hasNext()){
            		String destination = (String)key;
            		String inport = (String)ite.next();
            		Hashtable<Object,Object> f = new Hashtable<Object,Object>();
            		f.put("source", compNm);
                    f.put("outport", outport);
                    f.put("destination", compNm + "Observer");
                    f.put("inport", compNm + "." + outport + ".Obs");
                    addCouplingToAspect(f, aspect);
                    HashSet<Object> es = getEntityParentsOfAspect(aspect);
                    Iterator<Object> is = es.iterator();
                    while (is.hasNext()) {
                        String parent = (String) is.next();
                        if (parent.equals(rootEntityName)) {
                            parent = rootEntityName + "WObs";
                        }
                        f = new Hashtable<Object,Object>();
                        f.put("source", compNm + "Observer");
                        f.put("outport", "report");
                        f.put("destination", parent);
                        f.put("inport", "report");
                        addCouplingToAspect(f, aspect);
                    }
            	}
            }
        }
        if (!compNm.equals(rootEntityName)) {
            addRename(rootEntityName, rootEntityName + "WObs");
            replaceAll();
            rootEntityName = rootEntityName + "WObs";
        }
    }

    public void addSpecToAllEntities(String spec) {
        Iterator<Object> it = entityNames.iterator();
        while (it.hasNext()) {
            String entity = (String) it.next();
            if (!entity.equals(rootEntityName)) {
                addSpecToEntity(spec, entity);
            }
        }
    }

    public void prePruneObsWrap(String specialization) {
        boolean notDone = false;
        HashSet<Object> doneSet = new HashSet<Object>();
        while (!notDone) {
            notDone = true;
            Iterator<Object> it = aspectNames.iterator();
            while (it.hasNext() && notDone) {
                String aspect = (String) it.next();
                HashSet<Object> entities = aspectHasEntity.get(aspect);
                Iterator<Object> ie = entities.iterator();
                while (ie.hasNext() && notDone) {
                    String entity = (String) ie.next();
                    HashSet<Object> specs = entityHasSpec.get(entity);
                    Iterator<Object> is = specs.iterator();
                    while (is.hasNext() && notDone) {
                        String spec = (String) is.next();
                        if (spec.equals(specialization)) {
                        	HashSet<Object> ents = specHasEntity.get(spec);
                            Iterator<Object> ir = ents.iterator();
                            while (ir.hasNext() && notDone) {
                                String en = (String) ir.next();
                                boolean eitherNotDone = true;
                                if (!doneSet.contains(entity) && en.equals("IOWrap")) {
                                    addIOWrap(aspect, entity);
                                    doneSet.add(entity);
                                    eitherNotDone = false;
                                } else if (!doneSet.contains(entity) && en.equals("IOObs")) {

                                    addObserver(aspect, entity);
                                    doneSet.add(entity);
                                    eitherNotDone = false;
                                }
                                doneSet.add(entity);
                                notDone = eitherNotDone;
                                break;
                            }
                        }
                    }
                }
            }
        }
        removeSpec(specialization);
    }

    public void pickEntityFromSpec(String entity, String spec) {
        specHasEntity.remove(spec);
        addEntityToSpec(entity, spec);
    }

    public String entityWLargestSubstr() {
        String topEnt = "";
        int sessize = entityNames.size();
        int max = -1;
        Iterator<Object> it = entityNames.iterator();
        while (it.hasNext()) {
            String ent = (String) it.next();
            int testsize = substructure(ent).entityNames.size();
            if (testsize == sessize) {
                return ent;
            }
            if (testsize > max) {
                topEnt = ent;
                max = testsize;
            }
        }
        return topEnt;
    }

    public void setRootToMax() {
        setRoot(entityWLargestSubstr());
    }

    public boolean includeEntities(sesRelation sesfroment) {
        Iterator<Object> it = sesfroment.entityNames.iterator();
        while (it.hasNext()) {
            String ent = (String) it.next();
            if (!entityNames.contains(ent)) {
                return false;
            }
        }
        return true;
    }

    public HashSet<Object> dominates(sesRelation sesfroment, HashSet<Object> dominators) {
        HashSet<Object> dominated = new HashSet<Object>();
        Iterator<Object> it = dominators.iterator();
        while (it.hasNext()) {
            sesRelation dominator = (sesRelation) it.next();
            if (sesfroment.includeEntities(dominator)) {
                dominated.add(dominator);
            }
        }
        return dominated;
    }

    public boolean newDominator(sesRelation sesfroment, HashSet<Object> dominators) {
        Iterator<Object> it = dominators.iterator();
        while (it.hasNext()) {
            sesRelation dominator = (sesRelation) it.next();
            if (dominator.includeEntities(sesfroment)) {
                return false;
            }
        }
        return true;
    }

    public HashSet<Object> findAllMaxSubstr() {
        HashSet<Object> dominators = new HashSet<Object>();
        Iterator<Object> it = entityNames.iterator();
        while (it.hasNext()) {
            int size = dominators.size();
            String ent = (String) it.next();
            if (ent == null) {
                continue;
            }
            sesRelation sesfroment = substructure(ent);
            HashSet<Object> dominated = dominates(sesfroment, dominators);
            dominators.removeAll(dominated);
            if (newDominator(sesfroment, dominators)) {
                dominators.add(sesfroment);
            }
        }
        return dominators;
    }

    //  added by ming for svg
    public Document toDOMFORSVG() {
        return toDOMFromFORSVG(rootEntityName);
    }

    public Document toDOMFromFORSVG(String entity) {
        createSesDoc();
        sesRoot.setAttribute("name", entity);
        convertEntity(sesRoot, entity);
        SESOps.sesDoc = sesDoc;
        SESOps.allNames = allNames;
        SESOps.entityNames = entityNames;
        SESOps.aspectNames = aspectNames;
        SESOps.specNames = specNames;
        SESOps.multiAspectNames = multiAspectNames;
        SESOps.varNames = varNames;
        System.out.println("Overlap in names: " + (allNames.size()
                != entityNames.size()
                + aspectNames.size()
                + specNames.size()
                + multiAspectNames.size()));
        System.out.println("Overlap vars and entity names: "
                + SESOps.checkIntersect(entityNames, varNames));
        System.out.println("Names have internal spaces :"
                + SESOps.haveSpace(allNames));
        System.out.println("Names have  spaces :" + SESOps.haveSpace(varNames)); //bpz Dec 05
        return sesDoc;
    }

    //end of by ming
    ////////////////////////from sesRelationExtend
    public boolean isMultiEnt(String ent) {
        return !entityHasMultiAspect.get(ent).isEmpty();
    }

    public String getSingleOfMult(String ent) {
        HashSet<Object> multiasps = entityHasMultiAspect.get(ent);
        if (multiasps != null){
        Iterator<Object> it = multiasps.iterator();
        while (it.hasNext()) {
            String multiasp = (String) it.next();
            HashSet<Object> ents = multiAspectHasEntity.get(multiasp);
            Iterator<Object> ie = ents.iterator();
            return (String) ie.next();
        }
        }
        return "";
    }

    public HashSet<Object> getMultiEntsOfEnt(String entity) {
    	HashSet<Object> es = new HashSet<Object>();
    	HashSet<Object> aspects = entityHasAspect.get(entity);
        Iterator<Object> ia = aspects.iterator();
        while (ia.hasNext()) {
            String aspNm = (String) ia.next();

            HashSet<Object> ents = aspectHasEntity.get(aspNm);
            Iterator<Object> ie = ents.iterator();
            while (ie.hasNext()) {
                String ent = (String) ie.next();
                if (isMultiEnt(ent)) {
                    String single = getSingleOfMult(ent);
                    es.add(single);
                }
            }
        }
        return es;
    }

    public HashSet<Object> getMultiAspOfEnt(String entity) {
    	HashSet<Object> es = new HashSet<Object>();
    	HashSet<Object> aspects = entityHasAspect.get(entity);
        Iterator<Object> ia = aspects.iterator();
        while (ia.hasNext()) {
            String aspNm = (String) ia.next();

            HashSet<Object> ents = aspectHasEntity.get(aspNm);
            Iterator<Object> ie = ents.iterator();
            while (ie.hasNext()) {
                String ent = (String) ie.next();
                HashSet<Object> multiasps = entityHasMultiAspect.get(ent);
                es.addAll(multiasps);
            }
        }
        return es;
    }
    
    public HashSet<Object> getAspectNames(){
    	return aspectNames;
    }
    
    public HashSet<Object> getMultiAspectNames(){
    	return multiAspectNames;
    }
    
    public void createFolderHierarchy(String folder) {
        createFolderHierEntity(getMultiEntsOfEnt(this.getRootEntityName()),
                this.getRootEntityName(), folder);
    }

    public void createFolderHierEntity(HashSet<Object> multiEnts, String entity, String path) {
        if (entity == null) {
            return;
        }

        HashSet<Object> aspects = entityHasAspect.get(entity);
        Iterator<Object> ia = aspects.iterator();
        while (ia.hasNext()) {
            String aspNm = (String) ia.next();
            createFolderHierAspect(multiEnts, aspNm, path);
        }

        HashSet<Object> multiAspects = entityHasMultiAspect.get(entity);
        Iterator<Object> im = multiAspects.iterator();
        while (im.hasNext()) {
            String maspNm = (String) im.next();
            createFolderHierMultiAspect(maspNm, multiEnts, path);
        }

        HashSet<Object> specs = entityHasSpec.get(entity);
        Iterator<Object> is = specs.iterator();
        while (is.hasNext()) {
            String specNm = (String) is.next();
            createFolderHierSpec(specNm, path);
        }

    }

    public void createFolderHierAspect(HashSet<Object> multiEnts, String aspect, String path) {
    	HashSet<Object> entities = aspectHasEntity.get(aspect);
        Iterator<Object> ia = entities.iterator();
        while (ia.hasNext()) {
            String entNm = (String) ia.next();
            createFolderHierEntity(multiEnts, entNm, path);
        }

    }

    public void createFolderHierMultiAspect(String aspect,
    		HashSet<Object> multiEnts, String path) {
    	HashSet<Object> val = multiAspectHasEntity.get(aspect);
    	LinkedList<Object> queue = new LinkedList<Object>();
    	queue.retainAll(val);
        String entNm = (String) queue.getFirst();
        File file = new File(path + "/" + aspect + "-" + entNm);
        if (!file.exists()) {
            file.mkdir();
        }

        sesRelation sub = substructure(entNm);
        sub.writeSesDoc(file + "/" + entNm + "SeS.xml");
        sesToGenericSchema.writeSchemaToXML(file + "/" + entNm + "Schema");
        fileHandler.copyFile("ses.dtd", ".", file.toString() + "/");
        sesRelation cop = makeCopy();
        Iterator<Object> it = multiEnts.iterator();
        while (it.hasNext()) {
            String entN = (String) it.next();
            HashSet<Object> multiasps = getMultiAspectParents(entN);
            Iterator<Object> ia = multiasps.iterator();
            while (ia.hasNext()) {
                String multasp = (String) ia.next();
                cop.removeEntity(entN);
                cop.addEntityToMultiAspect(entN, multasp);
            }

        }
        cop.writeSesDoc(file + "/" + "SesFor" + cop.getRootEntityName() + ".xml");
        cop.writeSesDoc(path + "/" + "SesFor" + cop.getRootEntityName() + ".xml");
        sub.createFolderHierEntity(getMultiEntsOfEnt(entNm), entNm, path + "/" + aspect + "-" + entNm);
    }

    public void createFolderHierSpec(String spec, String path) {
        HashSet<Object> entities = specHasEntity.get(spec);
        Iterator<Object> ia = entities.iterator();
        while (ia.hasNext()) {
            String entNm = (String) ia.next();
            createFolderHierEntity(getMultiEntsOfEnt(entNm), entNm, path);
        }

    }

    public static void recreate(String PESxmlFile, String SESxmlFile, String path) {
        recreateFromMultAsp(PESxmlFile, SESxmlFile, path);
    }

    public static void recreateFromMultAsp(String PESxmlFile, String SESxmlFile, String path) {
        if (path.equals("")) {
            System.out.println("SaveFolder not defined");
            System.exit(4);
        }
        recreate(XMLToDom.getDocument(PESxmlFile),
                XMLToDom.getDocument(SESxmlFile), path);
    }

    public static void recreate(Document pruneDoc, Document sesDoc, String path) {
        Element sesRoot = sesDoc.getDocumentElement();
        String rootElemName = sesRoot.getAttribute("name");
        Element rootNode = sesRoot;
        if (rootElemName.equals("")) {
            NodeList nl = sesRoot.getChildNodes();
            for (int i = 0; i
                    < nl.getLength(); i++) {
                if (nl.item(i).getNodeName().equals("entity")) {
                    rootNode = (Element) nl.item(i);
                    rootElemName =
                            rootNode.getAttribute("name");
                    break;
                }

            }
        }
        sesRelation ses = new sesRelation(sesDoc);
        HashSet<Object> multiasps = ses.getMultiAspOfEnt(rootElemName);
        Iterator<Object> it = multiasps.iterator();
        while (it.hasNext()) {
            String multasp = (String) it.next();
            HashSet<Object> ents = ses.multiAspectHasEntity.get(multasp);
            Iterator<Object> ie = ents.iterator();
            String multiAspSaveFolder = path + "/" + multasp + "-" + (String) ie.next();
            recreateEntity(rootNode, multiAspSaveFolder, pruneDoc);
            pruneOps.pruneDoc = pruneDoc;
            String folder = path + "/";
            pruneOps.writePruneDoc(folder + rootElemName + "Composite" + "Inst.xml", "ses.dtd");
        }
    }

    public static void recreateEntity(Element se, String multiAspSaveFolder, Document pruneDoc) {
        LinkedList<Object> aspects = SESOps.getElementChildrenOfElement(se,
                "aspect");
        LinkedList<Object> mulasps = SESOps.getElementChildrenOfElement(se,
                "multiAspect");
        LinkedList<Object> specs = SESOps.getElementChildrenOfElement(se,
                "specialization");
        Iterator<Object> it = specs.iterator();
        while (it.hasNext()) {
            Element ch = (Element) it.next();
            recreateSpec(ch, multiAspSaveFolder, pruneDoc);
        }

        Iterator<Object> itt = aspects.iterator();
        while (itt.hasNext()) {
            Element as = (Element) itt.next();
            recreateAsp(as, multiAspSaveFolder, pruneDoc);
        }

        itt = mulasps.iterator();
        while (itt.hasNext()) {
            Element as = (Element) itt.next();
            String aspNm = as.getAttribute("name");
            if (multiAspSaveFolder.contains(aspNm)) {
                recreateMultiAsp(as, multiAspSaveFolder, pruneDoc);
            }
        }

    }

    public static void recreateSpec(Element sp, String multiAspSaveFolder, Document pruneDoc) {
    	LinkedList<Object> es = SESOps.getChildrenOfElement(sp, "aspect");
        Iterator<Object> it = es.iterator();
        while (it.hasNext()) {
            Element ent = (Element) it.next();
            recreateEntity(ent, multiAspSaveFolder, pruneDoc);
        }

    }

    public static void recreateAsp(Element as, String multiAspSaveFolder, Document pruneDoc) {
    	LinkedList<Object> entities = SESOps.getElementChildrenOfElement(as,
                "entity");
        Iterator<Object> it = entities.iterator();
        while (it.hasNext()) {
            Element ent = (Element) it.next();
            recreateEntity(ent, multiAspSaveFolder, pruneDoc);
        }

    }

    public static void recreateMultiAsp(Element mult, String multiAspSaveFolder, Document pruneDoc) {
    	LinkedList<Object> mcs = SESOps.getElementChildrenOfElement(mult, "entity");
        Iterator<Object> mit = mcs.iterator();
        Element representative = (Element) mit.next();
        LinkedList<Object> mcv = pruneOps.getElementChildrenOfElement(mult, "numberComponentsVar");
        if (mcv.isEmpty()) {
            return;
        }

        mit = mcv.iterator();
        Element ncv = (Element) mit.next();
        String ncvar = ncv.getAttribute("name");

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

        String pathExtend = multiAspSaveFolder + "/";
        File file = new File(pathExtend);
        if (!new File(pathExtend).exists()) {
            System.out.println(pathExtend + " does not exist");
        } else {
            Element asp = XMLToDom.getElementOccurrence(pruneDoc,
                    mult.getAttribute("name"));

            File[] files = fileHandler.chooseMultiFileString(pathExtend);

            Element rep = XMLToDom.getElementOccurrence(pruneDoc,
                    representative.getAttribute("name"));
            if (files.length > 0) {
                asp.removeChild(rep);
            }

            String numEnts = asp.getAttribute(ncvar);
            asp.setAttribute(ncvar, "" + files.length);
            for (int i = 0; i
                    < files.length; i++) {
                String fileNm = files[i].getName();
                if (!fileNm.endsWith("inst.xml") && !fileNm.endsWith("Inst.xml")) {
                    continue;
                }
                Document componentpruneDoc = XMLToDom.getDocument(pathExtend + fileNm);
                Element prime = componentpruneDoc.getDocumentElement();
                Element newNode = (Element) pruneDoc.importNode(prime, true);
                asp.appendChild(newNode);
            }

        }
    }

    public static class forInstFilter implements FilenameFilter {

        public boolean accept(File dir, String name) {
            return name.contains("inst") || name.contains("Inst");
        }
    }

    public void elaborateCoupling() {
        HashSet<Object> asps = multiAspectNames;
        Iterator<Object> it = asps.iterator();
        while (it.hasNext()) {
            String asp = (String) it.next();
            elaborateCouplingForMultAsp(asp);
        }
        asps = aspectNames;
        it = asps.iterator();
        while (it.hasNext()) {
            String asp = (String) it.next();
            elaborateCouplingForAsp(asp);
        }


    }

    public HashSet<Object> interpretFunction(Hashtable<Object,Object> f, HashSet<Object> ents) {
    	HashSet<Object> es = new HashSet<Object>();
    	HashSet<Object> result = new HashSet<Object>();
    	HashSet<Object> wp = checkAllWPortlToAll(f, new HashSet<Object>(ents));
        result.addAll(wp);
        if (wp.isEmpty()) {
            result.addAll(checkSourceToAllWPort(f, new HashSet<Object>(ents)));
            wp = checkAllToAll(f, new HashSet<Object>(ents));
            result.addAll(wp);
            if (wp.isEmpty()) {
                result.addAll(checkSourceToAll(f, new HashSet<Object>(ents)));
            }
        }
        result.addAll(checkEachToEach(f, new HashSet<Object>(ents)));
        wp = checkAllToAllWPort(f, new HashSet<Object>(ents));
        result.addAll(wp);

        wp = checkAllToDestinationWPort(f, new HashSet<Object>(ents));
        result.addAll(wp);
        if (wp.isEmpty()) {
            result.addAll(checkAllToDestination(f, new HashSet<Object>(ents)));
        }

        if (!result.isEmpty()) {
            es.addAll(result);
        } else {
            es.add(f);
        }
        return es;
    }

    public void elaborateCouplingForAsp(String asp) {
    	HashSet<Object> newCouplings = new HashSet<Object>();
    	HashSet<Object> es = aspectHasCoupling.get(asp);
        Iterator<Object> it = es.iterator();
        while (it.hasNext()) {
            Hashtable<Object,Object> f = (Hashtable<Object,Object>) it.next();
            HashSet<Object> ents = aspectHasEntity.get(asp);
            newCouplings.addAll(interpretFunction(f, ents));
            String coupling = newCouplings.toString();
            coupling += "";
        }
        aspectHasCoupling.remove(asp);
        Iterator<Object> is = newCouplings.iterator();
        while (is.hasNext()) {
        	Hashtable<Object,Object> f = (Hashtable<Object,Object>) is.next();
            addCouplingToAspect(f, asp);
        }
        System.out.println("Couplings " + aspectHasCoupling.get(asp));
    }

    public void elaborateCouplingForMultAsp(String asp) {
        HashSet<Object> parents = this.getEntityParentsOfAspect(asp);
        String parent = (String) parents.iterator().next();
        HashSet<Object> val = multiAspectHasEntity.get(asp);
        LinkedList<Object> queue = new LinkedList<Object>();
        queue.retainAll(val);
        String ent = (String) queue.getFirst();
        HashSet<Object> es = getParentsViaAspect(parent);
        if (!es.isEmpty()) {
            String grandparent = (String) es.iterator().next();            
            HashSet<Object> valgran = entityHasAspect.get(grandparent);
            LinkedList<Object> queueGran = new LinkedList<Object>();
            queueGran.retainAll(valgran);
            String gradparasp = (String) queueGran.getFirst();
            HashSet<Object> valent = entityHasSpec.get(ent);
            LinkedList<Object> queueEnt = new LinkedList<Object>();
            queueEnt.retainAll(valent);
            String spec = (String) queueEnt.getFirst();
            HashSet<Object> ess = specHasEntity.get(spec);
            Iterator<Object> it = ess.iterator();
            while (it.hasNext()) {
                String specent = (String) it.next();
                addEntityToAspect(specent + "_" + ent, gradparasp);
                sesRelation entcopy = this.substructure(ent);
                entcopy.entityHasSpec.get(ent).remove(spec);
                
                
                entcopy.replaceAll(ent, specent + "_" + ent);
                this.mergeSeS(entcopy);



            }
            aspectHasEntity.get(gradparasp).remove(parent);
            System.out.println("ents of grand " + aspectHasEntity.get(gradparasp));
            this.printTree();
        } else {
        }
    }

    public static String getClassOfName(String name, HashSet<Object> classes) {
        Iterator<Object> it = classes.iterator();
        while (it.hasNext()) {
            String cl = (String) it.next();
            if (name.startsWith(cl)) {
            }
            return cl;
        }
        return "";
    }

    public static String getClassOfName(String name) {
        int ind = name.lastIndexOf("_");
        return name.substring(ind + 1).trim();
    }

    public HashSet<Object> checkSourceToAll(Hashtable<Object,Object> f, HashSet<Object> entsCopy) {
    	HashSet<Object> es = new HashSet<Object>();
        String fsource = ((String) f.get("source")).trim();
        String fdestination = ((String) f.get("destination")).trim();
        if (fdestination.startsWith("all") && !fdestination.startsWith("allWPort")) {
            String cl = getClassOfName(fdestination);
            entsCopy.remove(fsource);
            Iterator<Object> it = entsCopy.iterator();
            while (it.hasNext()) {
                String ent = ((String) it.next()).trim();
                if (ent.endsWith(cl)) {
                	Hashtable<Object,Object> fn = new Hashtable<Object,Object>();
                    fn.put("source", fsource);
                    fn.put("destination", ent);
                    fn.put("outport", f.get("outport"));
                    fn.put("inport", f.get("inport"));
                    es.add(fn);
                }
            }
        }
        return es;
    }

    public HashSet<Object> checkSourceToAllWPort(Hashtable<Object,Object> f, HashSet<Object> entsCopy) {
    	HashSet<Object> es = new HashSet<Object>();
        String fsource = ((String) f.get("source")).trim();
        String fdestination = ((String) f.get("destination")).trim();
        if (fdestination.startsWith("allWPort")) {
            String cl = getClassOfName(fdestination);
            entsCopy.remove(fsource);
            Iterator<Object> it = entsCopy.iterator();
            while (it.hasNext()) {
                String ent = ((String) it.next()).trim();
                if (ent.endsWith(cl) && !ent.equals(fsource)) {
                    int ind = ent.lastIndexOf("_");
                    if (ind > 0) {
                        String port = ent.substring(0, ind).trim();
                        Hashtable<Object,Object> fn = new Hashtable<Object,Object>();
                        fn.put("source", fsource);
                        fn.put("destination", ent);
                        fn.put("outport", f.get("outport") + port);
                        fn.put("inport", f.get("inport"));
                        es.add(fn);
                    }
                }
            }
        }
        return es;
    }

    public HashSet<Object> checkAllWPortlToAll(Hashtable<Object,Object> f, HashSet<Object> entsCopy) {
    	HashSet<Object> es = new HashSet<Object>();
        String fsource = ((String) f.get("source")).trim();
        String fdestination = ((String) f.get("destination")).trim();
        if (fsource.startsWith("all") && fdestination.startsWith("allWPort")) {
        	HashSet<Object> newcopy = new HashSet<Object>(entsCopy);
            String cl = getClassOfName(fsource);
            Iterator<Object> it = newcopy.iterator();
            while (it.hasNext()) {
                String srcent = (String) it.next();
                if (srcent.endsWith(cl)) {
                	Hashtable<Object,Object> fn = new Hashtable<Object,Object>();
                    fn.put("source", srcent);
                    fn.put("destination", f.get("destination"));
                    fn.put("outport", f.get("outport"));
                    fn.put("inport", f.get("inport"));
                    es.addAll(checkSourceToAllWPort(fn, new HashSet<Object>(entsCopy)));
                }
            }
        }
        return es;
    }

    public HashSet<Object> checkAllToAllWPort(Hashtable<Object,Object> f, HashSet<Object> entsCopy) {
    	HashSet<Object> es = new HashSet<Object>();
        String fsource = ((String) f.get("source")).trim();
        String fdestination = ((String) f.get("destination")).trim();
        if (fsource.startsWith("allWPort") && fdestination.startsWith("all")) {
        	HashSet<Object> newcopy = new HashSet<Object>(entsCopy);
            String cl = getClassOfName(fdestination);
            Iterator<Object> it = newcopy.iterator();
            while (it.hasNext()) {
                String destent = (String) it.next();
                if (destent.endsWith(cl)) {
                	Hashtable<Object,Object> fn = new Hashtable<Object,Object>();
                    fn.put("source", f.get("source"));
                    fn.put("destination", destent);
                    fn.put("outport", f.get("outport"));
                    fn.put("inport", f.get("inport"));
                    es.addAll(checkAllToDestinationWPort(fn, new HashSet<Object>(entsCopy)));
                }
            }
        }
        return es;
    }

    public HashSet<Object> checkAllToDestination(Hashtable<Object,Object> f, HashSet<Object> entsCopy) {
    	HashSet<Object> es = new HashSet<Object>();
        String fsource = ((String) f.get("source")).trim();
        String fdestination = ((String) f.get("destination")).trim();
        if (fsource.startsWith("all")
                && !fsource.startsWith("allWPort")
                && !fdestination.startsWith("all")) {
            String cl = getClassOfName(fsource);
            entsCopy.remove(fsource);
            Iterator<Object> it = entsCopy.iterator();
            while (it.hasNext()) {
                String ent = ((String) it.next()).trim();
                if (ent.endsWith(cl) && !ent.equals(fdestination)) {
                	Hashtable<Object,Object> fn = new Hashtable<Object,Object>();
                    fn.put("source", ent);
                    fn.put("destination", fdestination);
                    fn.put("outport", f.get("outport"));
                    fn.put("inport", f.get("inport"));
                    es.add(fn);
                }
            }
        }
        return es;
    }

    public HashSet<Object> checkAllToDestinationWPort(Hashtable<Object,Object> f, HashSet<Object> entsCopy) {
    	HashSet<Object> es = new HashSet<Object>();
        String fsource = ((String) f.get("source")).trim();
        String fdestination = ((String) f.get("destination")).trim();
        if (fsource.startsWith("allWPort")) {
            String cl = getClassOfName(fsource);
            entsCopy.remove(fsource);
            Iterator<Object> it = entsCopy.iterator();
            while (it.hasNext()) {
                String ent = ((String) it.next()).trim();
                if (ent.endsWith(cl) && !ent.equals(fdestination)) {
                    int ind = ent.lastIndexOf("_");
                    if (ind > 0) {
                        String port = ent.substring(0, ind).trim();
                        Hashtable<Object,Object> fn = new Hashtable<Object,Object>();
                        fn.put("source", ent);
                        fn.put("destination", fdestination);
                        fn.put("outport", f.get("outport"));
                        fn.put("inport", f.get("inport") + port);
                        es.add(fn);
                    }
                }
            }
        }
        return es;
    }

    public HashSet<Object> checkAllToAll(Hashtable<Object,Object> f, HashSet<Object> entsCopy) {
    	HashSet<Object> es = new HashSet<Object>();
        String fsource = ((String) f.get("source")).trim();
        String fdestination = ((String) f.get("destination")).trim();
        if (fsource.startsWith("all") && fdestination.startsWith("all")) {
            String sourcecl = getClassOfName(fsource);
            entsCopy.remove(fsource);
            String destcl = getClassOfName(fdestination);
            entsCopy.remove(fdestination);
            Iterator<Object> it = entsCopy.iterator();
            while (it.hasNext()) {
                String srcent = (String) it.next();
                if (srcent.endsWith(sourcecl)) {
                    Iterator<Object> is = entsCopy.iterator();
                    while (is.hasNext()) {
                        String destent = ((String) is.next()).trim();
                        if (destent.endsWith(destcl)
                                && srcent.endsWith(sourcecl) && !srcent.equals(destent)) {
                        	Hashtable<Object,Object> fn = new Hashtable<Object,Object>();
                            fn.put("source", srcent);
                            fn.put("destination", destent);
                            fn.put("outport", f.get("outport"));
                            fn.put("inport", f.get("inport"));
                            es.add(fn);
                        }
                    }
                }
            }
        }
        return es;
    }

    public boolean matchPredicate(String srcent, String destent) {
        int ind = srcent.lastIndexOf("_");
        String srcsub = srcent.substring(0, ind);
        ind = destent.lastIndexOf("_");
        String destsub = destent.substring(0, ind);
        return srcsub.equals(destsub);


    }

    public HashSet<Object> checkEachToEach(Hashtable<Object,Object> f, HashSet<Object> entsCopy) {
    	HashSet<Object> es = new HashSet<Object>();
        String fsource = ((String) f.get("source")).trim();
        String fdestination = ((String) f.get("destination")).trim();
        if (fsource.startsWith("each") && fdestination.startsWith("each")) {
            String sourcecl = getClassOfName(fsource);
            entsCopy.remove(fsource);
            String destcl = getClassOfName(fdestination);
            entsCopy.remove(fdestination);
            Iterator<Object> it = entsCopy.iterator();
            while (it.hasNext()) {
                String srcent = ((String) it.next()).trim();
                if (srcent.endsWith(sourcecl)) {
                    Iterator<Object> is = entsCopy.iterator();
                    while (is.hasNext()) {
                        String destent = (String) is.next();
                        if (destent.endsWith(destcl)
                                && srcent.endsWith(sourcecl)
                                && !srcent.equals(destent)
                                && matchPredicate(srcent, destent)) {
                        	Hashtable<Object,Object> fn = new Hashtable<Object,Object>();
                            fn.put("source", srcent);
                            fn.put("destination", destent);
                            fn.put("outport", f.get("outport"));
                            fn.put("inport", f.get("inport"));
                            es.add(fn);


                        }
                    }
                }
            }
        }
        return es;


    }
// ///////////////

    public void restructureAllMultiAspects() {
        HashSet<Object> grandParents = (HashSet<Object>)entityHasAspect.keySet();
        for (Object o : grandParents) {
            String grandParent = (String) o;
            HashSet<Object> aspects = entityHasAspect.get(grandParent);
            Iterator<Object> im = aspects.iterator();
            if (im.hasNext()) {
                String aspNm = (String) im.next();
                HashSet<Object> ents = aspectHasEntity.get(aspNm);
                for (Object oe : ents) {
                    String entity = (String) oe;
                    restructureMultiAspect(entity, aspNm);
                }
            }
        }
    }

    public void restructureMultiAspect(String entity, String grandParAsp) {
    	HashSet<Object> entsToAdd = new HashSet<Object>();
        String gchd = getSingleOfMult(entity);
        if (gchd.equals("")) {
            return;
        }
        HashSet<Object> multiAspects = entityHasMultiAspect.get(entity);
        Iterator<Object> im = multiAspects.iterator();
        if (im.hasNext()) {
            String maspNm = (String) im.next();
            String chd = (String) new LinkedList<Object>(multiAspectHasEntity.get(maspNm)).getFirst();
            HashSet<Object> specs = entityHasSpec.get(chd);
            Iterator<Object> it = specs.iterator();
            if (it.hasNext()) {
                String spectoexpand = (String) it.next();
                HashSet<Object> ents = specHasEntity.get(spectoexpand);

                Iterator<Object> ie = ents.iterator();
                while (ie.hasNext()) {
                    String specEnt = (String) ie.next();
                    entsToAdd.add(specEnt + "_" + gchd);
                    sesRelation sub = substructure(gchd);
                    sesRelation cop = new sesRelation();
                    cop.copyFrom(sub);
                    cop.entityHasSpec.get(gchd).remove(spectoexpand);
                    cop.addRename(gchd, specEnt + "_" + gchd);
                    HashSet<Object> cspecs = cop.entityHasSpec.get(gchd);
                    Iterator<Object> itc = cspecs.iterator();
                    while (itc.hasNext()) {
                        String cspec = itc.next().toString();
                        cop.addRename(cspec, specEnt + "_" + cspec);
                    }

                    cop.replaceAll();
                    mergeSeS(cop);
                }
            }
            aspectHasEntity.get(grandParAsp).remove(entity);
        }
        for (Object o : entsToAdd) {
            addEntityToAspect(o.toString(), grandParAsp);
        }
    }

    public void restructureAllMultiAspectsOverSpec(String spec) {

      Set grandParents = entityHasAspect.keySet();
        for (Object o : grandParents) {
            String grandParent = (String) o;
            HashSet<Object> aspects = entityHasAspect.get(grandParent);
            if (aspects != null){
            Iterator<Object> im = aspects.iterator();
            while (im.hasNext()) {
                String aspNm = (String) im.next();
                HashSet<Object> ents = aspectHasEntity.get(aspNm);
                // Concurent ModificationException (6/20/12)
                HashSet<Object> copyEnts = new HashSet<Object>(ents);
               // for (Object oe : ents) {
                for (Object oe : copyEnts) {
                    String entity = (String) oe;
                    restructureMultiAspect(entity, aspNm, spec);
                }
            }
        }  
        }
    }

    public void restructureMultiAspect(String entity, String grandParAsp, String spectoexpand) {
        Hashtable<Object,Object> entsNsubsToAdd = new Hashtable<Object,Object>();
        String gchd = getSingleOfMult(entity);
        if (gchd.equals("")) {
            return;
        }
        HashSet<Object> multiAspects = entityHasMultiAspect.get(entity);
        Iterator<Object> im = multiAspects.iterator();
        if (im.hasNext()) {
            String maspNm = (String) im.next();
            String chd = (String) new LinkedList<Object>(multiAspectHasEntity.get(maspNm)).getFirst();
            HashSet<Object> specs = entityHasSpec.get(chd);
            if (specs!= null && specs.contains(spectoexpand)) {
            	HashSet<Object> ents = specHasEntity.get(spectoexpand);
                Iterator<Object> ie = ents.iterator();
                while (ie.hasNext()) {
                    String specEnt = (String) ie.next();
                    sesRelation substr = substructure(specEnt);
                    substr.addRename(specEnt, specEnt + "_" + gchd);
                    substr.replaceAll();
                    substr.setRoot(specEnt + "_" + gchd);
                    HashSet<Object> subasps = substr.entityHasAspect.get(specEnt + "_" + gchd);
                    for (Object asp : subasps) {
                        int ind = asp.toString().lastIndexOf("-");
                        String actualAsp = asp.toString().substring(ind);
                        substr.addRename(asp.toString(), specEnt + "_" + gchd + "_" + actualAsp.toString());
                    }
                    HashSet<Object> subspecs = substr.entityHasSpec.get(specEnt + "_" + gchd);
                    for (Object spec : subspecs) {
                        int ind = spec.toString().lastIndexOf("-");
                        String actualSpec = spec.toString().substring(ind);
                        substr.addRename(spec.toString(), specEnt + "_" + gchd + actualSpec.toString());
                    }
                    substr.replaceAll();
                    entsNsubsToAdd.put(specEnt + "_" + gchd, substr);
                    sesRelation sub = substructure(gchd);
                    sesRelation cop = new sesRelation();
                    cop.copyFrom(sub);
                    cop.entityHasSpec.get(gchd).remove(spectoexpand);
                    HashSet<Object> asps = cop.entityHasAspect.get(gchd);
                    if(asps != null){
	                    for (Object asp : asps) {
	                        cop.addRename(asp.toString(), specEnt + "_" + asp.toString());
	                    }
                    }
                    cop.addRename(gchd, specEnt + "_" + gchd);
                    HashSet<Object> cspecs = cop.entityHasSpec.get(gchd);
                    if(cspecs!= null){
	                    Iterator<Object> itc = cspecs.iterator();
	                    while (itc.hasNext()) {
	                        String cspec = itc.next().toString();
	                        cop.addRename(cspec, specEnt + "_" + cspec);
	                    }
                    }

                    cop.replaceAll();
                    mergeSeS(cop);
                }

                aspectHasEntity.get(grandParAsp).remove(entity);
            }

            Enumeration<Object> e = entsNsubsToAdd.keys();
            while(e.hasMoreElements()){
            	String key = (String)e.nextElement();
            	addEntityToAspect(key, grandParAsp);
                sesRelation substr = (sesRelation) entsNsubsToAdd.get(key);
                mergeSeS(substr);
            }
        }
    }
    //////////////////////////

    public void addImpliedInheritance(String entity, sesRelation other) {
        for (Object o : other.entityNames) {
            String entNm = o.toString();
            if (entity.equals(entNm)) {
                continue;
            }
            if (entity.endsWith(entNm)) {
                sesRelation sub = other.substructure(entNm);
                sub.addRename(entNm, entity);
                int ind = entity.lastIndexOf("_");
                String specEnt = entity.substring(0,ind);
                HashSet<Object> asps = sub.entityHasAspect.get(entNm);
                for (Object asp : asps) {
                    sub.addRename(asp.toString(), specEnt + "_" + asp.toString());
                }
                sub.replaceAll();
                sub.setRoot(entity);
                sub.printTree();
                this.mergeSeS(sub);
                return;
            }
        }
    }

    public void addImpliedInheritance(sesRelation other) {
    	HashSet<Object> entNames = new HashSet<Object>(entityNames);
        for (Object o : entNames) {
            String entNm = o.toString();
            if (entNm.contains("_")) {
                addImpliedInheritance(entNm, other);
            }
        }
    }

    public static void createNAddImpliedInheritance(sesRelation sesm, String folderTxt, String sestxtfile) {
        String sestxt = fileHandler.readFromFile(folderTxt + sestxtfile);
        Pattern p = Pattern.compile("!");
        String rootEntityName = "";
        String[] sentences = p.split(sestxt);
        Hashtable<Object,Object> f = null;
        sesParse par = new sesParse();
        for (int i = 0; i < sentences.length; i++) {
            f = par.parse(sentences[i]);
            if (f == null) {
                continue;
            }
            rootEntityName = (String) f.get("entity");
            break;
        }
        HashSet<Object> getMaxSes = natLangToSes.getMaxSesFromContents(sestxt);
        Iterator<Object> it = getMaxSes.iterator();
        while (it.hasNext()) {
            sesRelation ses = (sesRelation) it.next();
            if ((ses.getRootEntityName().equals(rootEntityName))) {
                sesm = ses;
                break;
            }
        }
        it = getMaxSes.iterator();
        while (it.hasNext()) {
            sesRelation ses = (sesRelation) it.next();
            if (!ses.getRootEntityName().equals(rootEntityName)) {
                sesm.addImpliedInheritance(ses);
            }
        }
        if (sesm.toString().toLowerCase().equals("unknown")) {
            System.out.println("empty rootSes");
            System.exit(5);
        }
    }

    public static sesRelation createNAddImpliedInheritance(String folderTxt, String sestxtfile) {
        String sestxt = fileHandler.readFromFile(folderTxt + sestxtfile);
        Pattern p = Pattern.compile("!");
        String rootEntityName = "";
        String[] sentences = p.split(sestxt);
        Hashtable<Object,Object> f = null;
        sesParse par = new sesParse();
        for (int i = 0; i < sentences.length; i++) {
            f = par.parse(sentences[i]);
            if (f == null) {
                continue;
            }
            rootEntityName = (String) f.get("entity");
            break;
        }
        sesRelation rootSes = new sesRelation();
        HashSet<Object> getMaxSes = natLangToSes.getMaxSesFromContents(sestxt);
        Iterator<Object> it = getMaxSes.iterator();
        while (it.hasNext()) {
            sesRelation ses = (sesRelation) it.next();
            if ((ses.getRootEntityName().equals(rootEntityName))) {
                rootSes = ses;
                break;
            }
        }
        it = getMaxSes.iterator();
        while (it.hasNext()) {
            sesRelation ses = (sesRelation) it.next();
            if (!ses.getRootEntityName().equals(rootEntityName)) {
                rootSes.addImpliedInheritance(ses);
            }
        }
        if (rootSes.toString().toLowerCase().equals("unknown")) {
            System.out.println("empty rootSes");
            System.exit(5);
        }
        return rootSes;
    }
    // With a ses content (cseo 6/18/2018)
    public static sesRelation createNAddImpliedInheritanceWContent(String folderTxt, String sesContent) {
       
        Pattern p = Pattern.compile("!");
        String rootEntityName = "";
        String[] sentences = p.split(sesContent);
        Hashtable<Object,Object> f = null;
        sesParse par = new sesParse();
        for (int i = 0; i < sentences.length; i++) {
            f = par.parse(sentences[i]);
            if (f == null) {
                continue;
            }
            rootEntityName = (String) f.get("entity");
            break;
        }
        sesRelation rootSes = new sesRelation();
        HashSet<Object> getMaxSes = natLangToSes.getMaxSesFromContents(sesContent);
        Iterator<Object> it = getMaxSes.iterator();
        while (it.hasNext()) {
            sesRelation ses = (sesRelation) it.next();
            if ((ses.getRootEntityName().equals(rootEntityName))) {
                rootSes = ses;
                break;
            }
        }
        it = getMaxSes.iterator();
        while (it.hasNext()) {
            sesRelation ses = (sesRelation) it.next();
            if (!ses.getRootEntityName().equals(rootEntityName)) {
                rootSes.addImpliedInheritance(ses);
            }
        }
        if (rootSes.toString().toLowerCase().equals("unknown")) {
            System.out.println("empty rootSes");
            System.exit(5);
        }
        return rootSes;
    }
//////////////////////
    public static void main(String argv[]) {
    }
}
