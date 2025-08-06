/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ms4systems.devs.core.util;

import java.util.*;

/**
 *
 * @author Bernie
 */
public class pruningTable extends Hashtable<Object,HashSet<Object>> {

    protected sesRelation ses;
    protected HashSet<Object> contexts;
    // Handling multiple entities for couplings
    protected HashSet<String> multiSpec;

    public pruningTable() {
        super();
        contexts = new HashSet<Object>();
        multiSpec = new HashSet<String>();
    }

    public pruningTable(sesRelation ses) {
        this();
        this.ses = ses;
    }
    
    public void addMultiSpec(String entity){
    	multiSpec.add(entity);
    }
    
    public HashSet<String> getMultiSpec(){
    	return multiSpec;
    }
    
    public void addContext(String entity){
        contexts.add(entity);
    }

    public boolean isContext(String entity){
        return contexts.contains(entity);
    }

    public HashSet<Object>  getContexts(){
        return contexts;
    }

    public void addPair(String entity, String pair) {
    	if(this.containsKey(entity)){
    		this.get(entity).add(new String[]{pair});
    	}else {
    		HashSet<Object> valSet = new HashSet<Object>();
    		valSet.add(new String[]{pair});
    		put(entity,valSet);
    	}
    }

    public void addPairs(String entity, String[] pairs) {
    	if(this.containsKey(entity)){
    		this.get(entity).add(pairs);
    	}else {
    		HashSet<Object> valSet = new HashSet<Object>();
    		valSet.add(pairs);
    		put(entity,valSet);
    	}
    }
    public void addEntity(String entity){
        addPairs(entity, new String[]{});
    }

    public String[] getPairs(String entity) {
        Set<Object> pairset = get(entity);
        if(pairset == null)return null;
        if (pairset.isEmpty())return null;
        String[] pairs = new String[]{};
        for (Object p : pairset) {
            String[] ps = (String[]) p;
            String[] temp = new String[pairs.length + ps.length];
            for (int i = 0; i < pairs.length; i++) {
                temp[i] = pairs[i];
            }
            for (int i = 0; i < ps.length; i++) {
                temp[i + pairs.length] = ps[i];
            }
            pairs = temp;
        }
        return pairs;
    }

    public HashSet<Object> aspectsToSelect() {
        Hashtable<Object,HashSet<Object>> entityHasAspect = ses.getRelation("entityHasAspect");
        HashSet<Object> hs = new HashSet<Object>();
        Enumeration<Object> e = entityHasAspect.keys();
        while(e.hasMoreElements()){
        	Object key = e.nextElement();
        	hs.addAll(entityHasAspect.get(key));
        }
        return hs;
    }

    public HashSet<Object> specializationsToSelectFrom() {
        Hashtable<Object,HashSet<Object>> specHasEntity = ses.getRelation("specHasEntity");
        return new HashSet<Object>(specHasEntity.keySet());
    }

    public HashSet<Object> entitiesToSelectFromSpec(String spec) {
        Hashtable<Object,HashSet<Object>> specHasEntity = ses.getRelation("specHasEntity");
        return new HashSet<Object>(specHasEntity.get(spec));
    }
           public void setSeS(sesRelation cop) {
            ses = cop;
        }

        public void selectAspectFromEntity( String asp, String ent) {
            addPair(ent, ent + "-" + asp + "Dec," + ent);
        }

        public void selectAspectFromEntityInContext(
                String asp, String ent, String context) {
           addPair(ent, context+":"+ent + "-" + asp + "Dec," + ent);
        }

        public void selectEntityFromSpec(String ent, String spec, String parentEnt) {
            addPair(parentEnt, ent + "," + parentEnt + "-" + spec + "Spec");
        }

        public void selectEntityFromSpecInContext(
                String ent, String spec, String parentEnt, String context) {
            addPair(parentEnt, context + ":" + ent + "," + parentEnt + "-" + spec + "Spec");
        }
}
