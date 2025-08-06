package com.ms4systems.devs.core.util;

import java.util.*;

public class restrictRelation extends Hashtable<Object,HashSet<Object>> {
    
	public String domainSpec, rangeSpec;
    public HashSet<Object> domain, range;

    public restrictRelation(String domainSpec, String rangeSpec,
    		HashSet<Object> domain, HashSet<Object> range) {
        this.domainSpec = domainSpec;
        this.rangeSpec = rangeSpec;
        this.domain = domain;
        this.range = range;
    }

    public void setRelation(Hashtable<Object,HashSet<Object>> r){
    	Enumeration<Object> e = r.keys();
    	while(e.hasMoreElements()){
    		Object key = e.nextElement();
    		HashSet<Object> valSet = r.get(key);
    		Iterator<Object> it = valSet.iterator();
    		while(it.hasNext()){
    			Object val = it.next();
    			addPair((String)key,(String)val);
    		}
    	}
    }

    public void addPair(String key, String val) {
        if (domain.contains(key) && range.contains(val)) {
        	if(containsKey(key)){
        		get(key).add(val);
        	}else{
        		HashSet<Object> valSet = new HashSet<Object>();
        		valSet.add(val);
        		put(key,valSet);
        	}
           // put(key, val);
        } else {
            System.out.println(key + " or " + val + " not in domain/range");
            System.exit(3);
        }
    }

    public void addAllVals(String key) {
        Iterator<Object> is = range.iterator();
        while (is.hasNext()) {
            addPair(key, (String) is.next());
        }
    }

    public void addAllKeys(String val) {
        Iterator<Object> is = domain.iterator();
        while (is.hasNext()) {
            addPair((String) is.next(), val);
        }
    }

    public void addAllPairs() {
        Iterator<Object> it = domain.iterator();
        Iterator<Object> is = range.iterator();
        while (it.hasNext()) {
            while (is.hasNext()) {
                addPair((String) it.next(), (String) is.next());
            }
        }
    }

    public void addAllValsExcept(String key, String[] vals) {
        Set<Object> es = stringOps.toEnsembleSet(vals);
        HashSet<Object> rc = new HashSet<Object>(range);
        rc.removeAll(es);
        Iterator<Object> is = rc.iterator();
        while (is.hasNext()) {
            addPair(key, (String) is.next());
        }
    }

    public void addAllKeysExcept(String[] keys, String val) {
        Set<Object> es = stringOps.toEnsembleSet(keys);
        HashSet<Object> dc = new HashSet<Object>(domain);
        dc.removeAll(es);
        Iterator<Object> is = dc.iterator();
        while (is.hasNext()) {
            addPair((String) is.next(), val);
        }
    }

    public LinkedList<Object> place(LinkedList<Object> specs) {
        Iterator<Object> it = specs.iterator();
        int i = 0;
        while (it.hasNext()) {
            String spec = (String) it.next();
            if (spec.equals(rangeSpec)) {
                break;
            } else {
                i++;
            }
        }
        if (!specs.contains(domainSpec)) {
            specs.add(i, domainSpec);
        }
        if (!specs.contains(rangeSpec)) {
            if (i + 1 < specs.size()) {
                specs.add(i + 1, rangeSpec);
            } else {
                specs.add(rangeSpec);
            }
        }
        return specs;
    }

  public  restrictRelation makeConverse(){
        restrictRelation r = new restrictRelation(rangeSpec,domainSpec,range,domain);
        Enumeration<Object> e = keys();
        while(e.hasMoreElements()){
        	Object key = e.nextElement();
        	HashSet<Object> valSet = get(key);
        	Iterator<Object> it = valSet.iterator();
        	while(it.hasNext()){
        		Object val = it.next();
        		if(r.containsKey(key)){
        			r.get(key).add(val);
        		}else {
        			HashSet<Object> valSet2 = new HashSet<Object>();
        			valSet2.add(val);
        			r.put(key, valSet2);
        		}
        	}
        }
        
        return r;
    }
}
