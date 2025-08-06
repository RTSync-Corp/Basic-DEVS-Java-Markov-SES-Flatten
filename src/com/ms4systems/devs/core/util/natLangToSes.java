package com.ms4systems.devs.core.util;

import org.w3c.dom.*;

import java.util.*;
import java.util.regex.*;



public class natLangToSes {

    public static boolean queryMode = false;
    
    // contains processed entities to generate a ses file
    public static List<String> entList = new ArrayList<String>();

    public static void showParse(String natLangFile) {
        String contents = fileHandler.getContentsAsString(natLangFile);
        if (contents != null) {
            Pattern p = Pattern.compile("!");
            String[] sentences = p.split(contents);
            showParse(sentences);
        } else {
            System.out.println("wrong file path");
        }
    }

    public static void showParse(String[] sentences) {
        HashSet<Object> ents = new HashSet<Object>();
        sesRelation ses = null;
        Hashtable<Object,Object> f = null;
        sesParse par = new sesParse();
        int numProcessed = 0, numParsed = 0;
        for (int i = 0; i < sentences.length; i++) {
            f = par.parse(sentences[i]);
            if (sentences[i] != null && !sentences[i].trim().equals("")) {
                if (f == null) {
                    numProcessed++;
                    System.out.println("The input " + DomToXML.quote(sentences[i].trim()) + " PARSE FAILED");
                } else {
                    numProcessed++;
                    numParsed++;
                    System.out.println("The input: " + DomToXML.quote(sentences[i].trim()) + " ! ");
                    System.out.println(" parsed into: " + f);
                    ents.add(f.get("entity"));
                    ents.addAll((LinkedList<Object>) f.get("entities"));
                }
            }
        }
        System.out.println("\nNUMBER OF UNINTERRPRETED INPUTS = " + (numProcessed - numParsed));
        TreeSet ord = new TreeSet(ents);
        System.out.println("Collected Entities " + ord);
    }

    public static sesRelation transform(String natLangFile) {
        String contents = fileHandler.getContentsAsString(natLangFile);
        if (contents != null) {
            Pattern p = Pattern.compile("!");
            String[] sentences = p.split(contents);
            return transform(sentences);
        } else {
            System.out.println("wrong file path");
            return null;
        }
    }

    public static sesRelation transform(String[] sentences) {
        return transform(sentences, null, false);
    }

    public static sesRelation transform(String[] sentences, sesRelation Ses,
            boolean exit) {
        sesRelation ses = Ses;// null;
        Hashtable<Object,Object> f = null;
        sesParse par = new sesParse();
        for (int i = 0; i < sentences.length; i++) {
            f = par.parse(sentences[i]);
            if (f == null) {
                continue;
            }
            if (ses == null && f != null) {
                ses = sesRelation.create(//"DUMMY");
                        (String) f.get("entity"));
            }
            if (f.get("aspect") != null && f.get("isLike") != null) {
                String aspNm = f.get("isLike") + "-" + (String) f.get("aspect");
                sesRelation.addAspectToEntity(ses, aspNm, (String) f.get("entity"));
            } else if (f.get("aspect") != null && f.get("entity") != null) {
                String aspNm = f.get("entity") + "-" + (String) f.get("aspect");
                sesRelation.addAspectToEntity(ses, aspNm, (String) f.get("entity"));
                LinkedList<Object> entities = (LinkedList<Object>) f.get("entities");
                for (int j = 0; j < entities.size(); j++) {
                    sesRelation.addEntityToAspect(ses,
                            (String) entities.get(j), aspNm);
                }
            } else if (f.get("aspect") != null 
                    ) {
                String entNm = getUniqueParent((String) f.get("aspect"), ses.entityHasAspect());
                if (entNm != null) {
                    // /this assumes that aspect name will be used only once
                    String aspNm = entNm + "-" + (String) f.get("aspect");
                    Hashtable<Object,Object> fn = new Hashtable<Object,Object>();
                    if (f.get("source") == null) {
                        continue;
                    }
                    fn.put("source", f.get("source"));
                    if (f.get("outport") == null) {
                        continue;
                    }
                    fn.put("outport", f.get("outport"));
                    if (f.get("destination") == null) {
                        continue;
                    }
                    fn.put("destination", f.get("destination"));
                    if (f.get("inport") != null) { //as part
                        fn.put("inport", "in" + f.get("inport"));
                        fn.put("outport", "out" + f.get("outport"));
                    } else {
                        String outpt = (String) fn.get("outport");
                        if (outpt == null) {
                            continue;
                        }
                        fn.put("outport", "out" + outpt);
                        if (entNm.equals(f.get("destination"))) {
                            fn.put("inport", "out" + outpt);
                        } else if (entNm.equals(f.get("source"))) {
                            fn.put("inport", "in" + outpt);
                            fn.put("outport", "in" + outpt);
                        } else {
                            fn.put("inport", "in" + outpt);
                        }
                    }
                    String intest = (String) fn.get("inport");
                    if (intest.startsWith("inin")) {
                        intest = intest.replaceAll("inin", "in");
                        fn.put("inport", intest);
                    }
                    String outtest = (String) fn.get("outport");
                    if (outtest.startsWith("inin")) {
                        outtest = outtest.replaceAll("outout", "out");
                        fn.put("outport", outtest);
                    }
                    sesRelation.addCouplingToAspect(ses, fn, aspNm);
                }
            } else if (f.get("multiAspect") != null) {
                String aspNm = f.get("entity") + "-" + (String) f.get("multiAspect");
                sesRelation.addMultiAspectToEntity(ses, aspNm, (String) f.get("entity"));
                sesRelation.addEntityToMultiAspect(ses, (String) f.get("entities"), aspNm);

            } else if (f.get("specialization") != null && f.get("isLike") != null) {
                String specNm = f.get("isLike") + "-" + (String) f.get("specialization");
                sesRelation.addSpecToEntity(ses, specNm, (String) f.get("entity"));

            } else if (f.get("specialization") != null) {
                String specNm = f.get("entity") + "-" + (String) f.get("specialization");
                sesRelation.addSpecToEntity(ses, specNm, (String) f.get("entity"));
                LinkedList<Object> entities = (LinkedList<Object>) f.get("entities");
                for (int j = 0; j < entities.size(); j++) {
                    sesRelation.addEntityToSpec(ses, (String) entities.get(j),
                            specNm);
                }
            } else if (f.get("vars") != null) {
                String entNm = (String) f.get("entity");
                LinkedList<Object> vars = (LinkedList<Object>) f.get("vars");
                for (int j = 0; j < vars.size(); j++) {
                    sesRelation.addVarToEntity(ses, (String) vars.get(j), entNm);
                }
            } else if (f.get("function") != null) {
                String entNm = (String) f.get("entity");
                sesRelation.addFunctionToEntity(ses,
                        (String) f.get("function"), entNm);
            } else if (f.get("rangeSpec") != null) {
                String entityVarNm = (String) f.get("entity.var");
                String range = (String) f.get("rangeSpec");
                sesRelation.addRangeToVar(ses, range, entityVarNm);
                if (f.get("values") != null) {
                    String vals = (String) f.get("values");
                    sesRelation.addRangeToVar(ses, range + vals, entityVarNm);
                }
            }
        }
        if (ses == null) {
            return new sesRelation();
        }

        HashSet<Object> dominators = ses.findAllMaxSubstr();
        if (!exit) {
            System.out.println("There are roots: " + dominators.toString());
            Iterator<Object> it = dominators.iterator();
            while (it.hasNext()) {
                sesRelation maxses = (sesRelation) it.next();
                if (ses.rootEntityName == null
                        || !ses.rootEntityName.equals(maxses.rootEntityName)) {
                    ses.setRoot(maxses.rootEntityName);
                }
                if (queryMode) {
                    javax.swing.JPanel pan = new javax.swing.JPanel();
                    javax.swing.JOptionPane p = new javax.swing.JOptionPane();
                    int res = p.showConfirmDialog(pan, "Create SES for " + maxses.rootEntityName + "?");
                    if (p.NO_OPTION == res) {
                        continue;
                    } else if (p.CANCEL_OPTION == res) {
                        System.exit(3);
                    }
                } else {
                    ses = transform(sentences, maxses, true);
                    ses.toDOM();
                    return ses;
                }
            }
        } else {
            ses.toDOM();
            return ses;
        }
        return new sesRelation();
    }

    public static HashSet<Object> getMaxSes(String natLangFile) {
        String contents = fileHandler.getContentsAsString(natLangFile);
        if (contents == null) {
            System.out.println("wrong file path");
            return null;
        }
        return getMaxSesFromContents(contents);
    }

    public static HashSet<Object> getMaxSesFromContents(String contents) {
        Pattern p = Pattern.compile("!");
        String[] sentences = p.split(contents);
        sesRelation ses = null;
        Hashtable<Object,Object> f = null;
        sesParse par = new sesParse();
        for (int i = 0; i < sentences.length; i++) {
            f = par.parse(sentences[i]);
            if (f == null) {
                continue;
            }

            if (ses == null && f != null) {
                ses = sesRelation.create(//"DUMMY");
                        (String) f.get("entity"));
            }
            if (f.get("aspect") != null && f.get("isLike") != null) {
                String aspNm = f.get("isLike") + "-" + (String) f.get("aspect");
                sesRelation.addAspectToEntity(ses, aspNm, (String) f.get("entity"));
            } else if (f.get("aspect") != null && f.get("entity") != null) {
                String aspNm = f.get("entity") + "-" + (String) f.get("aspect");
                sesRelation.addAspectToEntity(ses, aspNm, (String) f.get("entity"));
                LinkedList<Object> entities = (LinkedList<Object>) f.get("entities");
                for (int j = 0; j < entities.size(); j++) {
                    sesRelation.addEntityToAspect(ses,
                            (String) entities.get(j), aspNm);
                }
            } else if (f.get("multiAspect") != null) {
                String aspNm = f.get("entity") + "-" + (String) f.get("multiAspect");
                sesRelation.addMultiAspectToEntity(ses, aspNm, (String) f.get("entity"));
                sesRelation.addEntityToMultiAspect(ses, (String) f.get("entities"), aspNm);

            } else if (f.get("specialization") != null && f.get("isLike") != null) {
                String specNm = f.get("isLike") + "-" + (String) f.get("specialization");
                sesRelation.addSpecToEntity(ses, specNm, (String) f.get("entity"));

            } else if (f.get("specialization") != null) {
                String specNm = f.get("entity") + "-" + (String) f.get("specialization");
                sesRelation.addSpecToEntity(ses, specNm, (String) f.get("entity"));
                LinkedList<Object> entities = (LinkedList<Object>) f.get("entities");
                for (int j = 0; j < entities.size(); j++) {
                    sesRelation.addEntityToSpec(ses, (String) entities.get(j),
                            specNm);
                }
            } else if (f.get("vars") != null) {
                String entNm = (String) f.get("entity");
                LinkedList<Object> vars = (LinkedList<Object>) f.get("vars");
                for (int j = 0; j < vars.size(); j++) {
                    sesRelation.addVarToEntity(ses, (String) vars.get(j), entNm);
                }
            } else if (f.get("function") != null) {
                String entNm = (String) f.get("entity");
                sesRelation.addFunctionToEntity(ses,
                        (String) f.get("function"), entNm);
            } else if (f.get("rangeSpec") != null) {
                String entityVarNm = (String) f.get("entity.var");
                String range = (String) f.get("rangeSpec");
                sesRelation.addRangeToVar(ses, range, entityVarNm);
                if (f.get("values") != null) {
                    String vals = (String) f.get("values");
                    sesRelation.addRangeToVar(ses, range + vals, entityVarNm);
                }
            }
        }
        if (ses == null) {
            return new HashSet<Object>();
        }
        return ses.findAllMaxSubstr();
    }

    public static String getUniqueParent(String item, Hashtable<Object,HashSet<Object>> r) {
    	Enumeration<Object> e = r.keys();
    	while(e.hasMoreElements()){
    		Object key = e.nextElement();
    		HashSet<Object> valSet = r.get(key);
    		Iterator<Object> it = valSet.iterator();
    		while(it.hasNext()){
    			Object value = it.next();
    			String val = (String)value;
    			int ind = val.lastIndexOf("-");
                String valend = val.substring(ind + 1);
                if (valend.equals(item)) {
                    return (String) key;
                }
    		}
    	}
        
        return null;
    }

    public static Set<Object> getAllParents(String item, Hashtable<Object,HashSet<Object>> r) {
        Set<Object> es = new HashSet<Object>();
        Enumeration<Object> e = r.keys();
    	while(e.hasMoreElements()){
    		Object key = e.nextElement();
    		HashSet<Object> valSet = r.get(key);
    		Iterator<Object> it = valSet.iterator();
    		while(it.hasNext()){
    			Object value = it.next();
    			String val = (String)value;
    			int ind = val.lastIndexOf("-");
                String valend = val.substring(ind + 1);
                if (valend.equals(item)) {
                    es.add(key.toString());
                }
    		}
    	}
        
        return es;
    }

    public static void defineThesarusForParse() {

    }

    // //////////////////////////
    public static LinkedList<Object> getItems(String natLangFile) {
        LinkedList<Object> es = new LinkedList<Object>();
        String contents = fileHandler.getContentsAsString(natLangFile);
        if (contents != null) {
            Pattern p = Pattern.compile("\n");
            String[] sentences = p.split(contents);
            for (int i = 0; i < sentences.length; i++) {
                String featurePlus = sentences[i].trim();
                featurePlus = featurePlus.replaceAll(",", "");
                featurePlus = featurePlus.replaceAll("/", "SLASH");
                featurePlus = deleteBracket(featurePlus);
                featurePlus = deletePeriod(featurePlus);
                featurePlus = replaceAmp(featurePlus);
                if (!es.contains(featurePlus)) {
                    es.add(featurePlus);
                }
            }
        }
        return es;
    }

    public static sesRelation createFromFeatures(String natLangFile) {
        sesRelation ses = new sesRelation();
        ses.setRoot("CCM");
        ses.addAspectToEntity("featureAsp", "CCM");
        return ses;
    }

    public static String findFeature(String featurePlus, HashSet<Object> es) {
        Iterator it = es.iterator();
        while (it.hasNext()) {
            String feature = (String) it.next();
            if (featurePlus.startsWith(feature)) {
                return feature;
            }
        }
        return null;
    }

    public static sesRelation createSesFromTable(String root, String natLangFile) {
        Hashtable<Object,Object> attrToType = new Hashtable<Object,Object>();
        sesRelation ses = new sesRelation();
        ses.setRoot("CCM");
        ses.addAspectToEntity("featureAsp", "CCM");
        LinkedList<Object> featuresAttributes = getItems(natLangFile);
        Iterator<Object> it = featuresAttributes.iterator();
        while (it.hasNext()) {
            String featurePlus = (String) it.next();
            featurePlus.trim();
            Pattern p = Pattern.compile("\t");
            String[] groups = p.split(featurePlus);
            for (int i = 0; i < groups.length; i++) {
                groups[i] = groups[i].trim();
            }
            if (groups.length >= 1) {
                String feature = groups[0];
                ses.addEntityToAspect(feature, "featureAsp");
                if (groups.length >= 3) {
                    String attribute = groups[1];
                    ses.addSpecToEntity(attribute + "FOR" + feature, feature);
                    String attrType = groups[2];
                    attrToType.put(feature, attrType);

                    if (groups.length == 4) {
                        String enumerant = groups[3];
                        ses.addEntityToSpec(enumerant, attribute + "FOR" + feature);
                        ses.addVarToEntity("attrType", enumerant);
                        ses.addRangeToVar(attrType, enumerant + "." + "attrType");
                    }
                }
            }
        }
        changeSpecsToAttrs(ses, attrToType);
        return ses;
    }

    public static void changeSpecsToAttrs(sesRelation ses,
            Hashtable<Object,Object> attrToType) {
        Iterator<Object> it = ses.specNames.iterator();
        while (it.hasNext()) {
            String spec = (String) it.next();
            Set<Object> entities = ses.specHasEntity.get(spec);
            if (entities.isEmpty()) {
                HashSet<Object> parEnts = ses.getEntityParentsOfSpec(spec);
                ses.removeValue(ses.entityHasSpec, spec);
                Iterator<Object> is = parEnts.iterator();
                while (is.hasNext()) {
                    String parEnt = (String) is.next();
                    ses.addVarToEntity(spec, parEnt);
                    ses.addRangeToVar("RANGE", parEnt + "." + spec);
                    String type = (String) attrToType.get(parEnt);
                    ses.addRangeToVar(type, parEnt + "." + spec);
                }
            }
        }
    }

    public static String deletePeriod(String s) {
        char[] ca = s.toCharArray();
        String r = "";
        for (int i = 0; i < ca.length; i++) {
            if (ca[i] != '.') {
                r += ca[i];
            }
        }
        return r;
    }

    public static String deleteBracket(String s) {
        char[] ca = s.toCharArray();
        String r = "";
        for (int i = 0; i < ca.length; i++) {
            if (ca[i] != '(' && ca[i] != ')') {
                r += ca[i];
            }
        }
        return r;
    }

    public static String replaceAmp(String s) {
        char[] ca = s.toCharArray();
        String r = "";
        for (int i = 0; i < ca.length; i++) {
            if (ca[i] == '&') {
                r += 'N';
            } else {
                r += ca[i];
            }
        }
        return r;
    }

    // //////////////////
    public static String backToNatLang(sesRelation ses) {
        String s = "";
        s += toNatLangFrom(ses.rootEntityName, ses);
        Pattern p = Pattern.compile("!");
        String[] sentences = p.split(s);
        HashSet<Object> es = new HashSet<Object>();
        LinkedList<Object> res = new LinkedList<Object>();
        for (Object o : sentences) {
            if (es.add(o.toString())) {
                res.add(o.toString());
            }
        }
        s = "";
        for (Object o : res) {
        	if(!o.toString().equals(""))
        		s += o.toString() + "!";
        }
        return s;
    }

    public static String toNatLangFrom(String entity, sesRelation ses) {
        return convertEntityNL(entity, ses);
    }

    public static String convertEntityNL(String entity, sesRelation ses) {
    	
    	entList.add(entity);
        String s = "";
        Set<Object> aspects = ses.entityHasAspect.get(entity);
        if(aspects != null){
	        Iterator<Object> ia = aspects.iterator();
	        while (ia.hasNext()) {
	            String aspNm = (String) ia.next();
	            int ind = aspNm.indexOf("-");
	            String aspRedNm = aspNm;
	            if (ind != -1) {
	                aspRedNm = aspNm.substring(ind + 1, aspNm.length());
	                aspRedNm = aspRedNm.substring(0, aspRedNm.length() - 3);
	            }
	            s += "From the " + aspRedNm + " perspective, " + entity + " is made of " + convertAspectNL(aspRedNm,aspNm, ses);
	        }
        }
        Set<Object> multiAspects = ses.entityHasMultiAspect.get(entity);
        if(multiAspects != null){
	        Iterator<Object> im = multiAspects.iterator();
	        while (im.hasNext()) {
	            String aspNm = (String) im.next();
	            int ind = aspNm.indexOf("-");
	            String aspRedNm = aspNm;
	            if (ind != -1) {
	
	                aspRedNm = aspNm.substring(aspNm.indexOf("-") + 1, aspNm.length());
	                aspRedNm = aspRedNm.substring(0, aspRedNm.length() - 8);
	            }
	            s += "From the " + aspRedNm + " perspective," + " " + entity + " is(are) made of more than one " + (String) (new LinkedList<Object>(ses.multiAspectHasEntity.get(aspNm))).getFirst() + "!\n";
	        }
	        im = multiAspects.iterator();
	        while (im.hasNext()) {
	            String aspNm = (String) im.next();
	            s += convertMultiAspectNL(aspNm, ses);
	        }
	        s+="\n";
        }
        Set<Object> specs = ses.entityHasSpec.get(entity);
        if(specs!= null){
	        Iterator<Object> is = specs.iterator();
	        while (is.hasNext()) {
	            String specNm = (String) is.next();
	            s += entity + " can be ";
	            s += convertFirstSpecNL(specNm, ses);
	            String newspecNm = specNm.substring(specNm.indexOf("-") + 1, specNm.length());
	            newspecNm = newspecNm.substring(0, newspecNm.length() - 4);
	            s += " in " + newspecNm + "!\n";
	
	            s += convertSecondSpecNL(specNm, ses);
	        }
	        s+="\n";
        }
        
        
        Set<Object> vars = ses.entityHasVar.get(entity);
        if(vars!=null){
	        if (!vars.isEmpty()) {
	            Iterator<Object> iv = vars.iterator();
	            Object o = iv.next();
	            if (o instanceof String) {
	                //        s += "\n" + entity + " has ";
	                if (vars.size() == 1) {
	                    s += "\n" + entity + " has ";
	                    String first = o.toString();
	                    s += first + "!\n";
	                }
	
	                if (vars.size() > 1) {
	                    s += "\n" + entity + " has ";
	                    String first = o.toString();
	                    while (iv.hasNext()) {
	                        String varNm = iv.next().toString();
	                        s += varNm + ", ";
	                    }
	                    s += "and " + first + "!\n";
	                }
	            }
	
	            iv = vars.iterator();
	            while (iv.hasNext()) {
	                o = iv.next();
	                String varNm = o.toString();
	
	                if (o instanceof String) {
	                	String range =(String) ses.varHasRange.get(entity + "." + varNm);
	                	if(range!=null){
		                	int first = range.indexOf("[");
		                	int second = range.indexOf("]");
		                	String type = range.substring(0,first);
		                	String interval = range.substring(first,second+1);
		                    //                 s += "\n" + entity + " has ";
		                    s += "The range of ";
		                    s += entity + "'s " + varNm + " is " + type + " with values "+interval+" !";
	                	}
	                } else if (o instanceof NamedNodeMap) {
	
	                    NamedNodeMap m = (NamedNodeMap) o;
	
	                    Attr name = (Attr) m.item(0);
	                    Attr range = (Attr) m.item(1);
	                    
	                    String tempRange = range.getValue();
	                    int first = tempRange.indexOf("[");
	                	int second = tempRange.indexOf("]");
	                	String type = tempRange.substring(0,first);
	                	String interval = tempRange.substring(first,second+1);
	                	
	                    s += "\n" + entity + " has ";
	                    s += " a " + name.getValue() + "!";
	                    s += "\n" + "The range of ";
	                    s += entity + "'s " + name.getValue() + " is " + type + " with values "+interval+" !";
	
	                }
	            }
	        }
	        s+="\n";
        }        
        
        
        return s;
    }
    public static String convertPruningRulesToNL(sesRelationExtend sesRE){
    	// add if then statement
    	String contents ="";
        Iterator<sesRelationExtend.pruningRule> it = sesRE.PruningRules.iterator();
        while(it.hasNext()){
        	sesRelationExtend.pruningRule pruningRule = it.next();
        	String ent = pruningRule.entity;
        	String condition = pruningRule.condition;
        	Pair action = pruningRule.action;
        	System.out.println("entity name : "+ent);
        	System.out.println("condition : "+condition);
        	System.out.println("Pair action  : "+action);
        	
        	String specEnt = condition.substring(0, condition.indexOf(","));
          	String specNm = condition.substring(condition.indexOf("-") + 1, condition.length());
            specNm = specNm.substring(0, specNm.length() - 4);
        	
        	contents +="if select "+specEnt+" from "+specNm+" for "+ent;
        	String key = (String)action.key;
        	String value = (String)action.value;
        	
        	String specEnt2 = value.substring(0, value.indexOf(","));
          	String specNm2 = value.substring(value.indexOf("-") + 1, value.length());
            specNm2 = specNm2.substring(0, specNm2.length() - 4);
            
            contents +=" then select "+specEnt2+" from "+specNm2+" for "+key+"!\n";
        	
        }
        
        return contents;
    }
    public static String convertMultiAspectNL(String aspect, sesRelation ses) {
        String s = "";
        String entNm = (String) (new LinkedList<Object>(ses.multiAspectHasEntity.get(aspect))).getFirst();
        if(!entList.contains(entNm)){
        	s+=convertEntityNL(entNm, ses);
        }
        return s + "\n" ; 
    }

    public static String convertFirstSpecNL(String spec, sesRelation ses) {
        String s = "";
        Set<Object> entities = ses.specHasEntity.get(spec);
        if (entities.isEmpty()) {
            return s;
        }
        Iterator<Object> ia = entities.iterator();
        if (entities.size() > 2) {
            String first = (String) ia.next();
            while (ia.hasNext()) {
                String entNm = (String) ia.next();
                s += entNm + ", ";
            }
            s += "or " + first;
        } else if(entities.size()==2){
        	String first = (String) ia.next();
            while (ia.hasNext()) {
                String entNm = (String) ia.next();
                s += entNm + " ";
            }
            s += "or " + first;
        }
        else {
            s += (String) ia.next();
        }
        return s;
    }

    public static String convertSecondSpecNL(String spec, sesRelation ses) {
        String s = "";
        Set<Object> entities = ses.specHasEntity.get(spec);
        Iterator<Object> ia = entities.iterator();

        while (ia.hasNext()) {
            String entNm = (String) ia.next();
            if(!entList.contains(entNm)){
            	s += convertEntityNL(entNm, ses);
            }
        }

        return s;
    }
    public static String convertAspectNL(String aspect, sesRelation ses) {
        String s = "";
        Set<Object> entities = ses.aspectHasEntity.get(aspect);
               
        Iterator<Object> ia = entities.iterator();
        if (entities.size() > 2) {
            String first = (String) ia.next();
            while (ia.hasNext()) {
                String entNm = (String) ia.next();
                s += entNm + ", ";
            }
            s += "and " + first+"!\n";
        }else if(entities.size()==2){
        	String first = (String) ia.next();
            while (ia.hasNext()) {
                String entNm = (String) ia.next();
                s += entNm + " ";
            }
            s += "and " + first+"!\n";
        	
        } 
        else if (entities.size() > 0) {
            s += (String) ia.next() +"!\n";
        }
        
        ia = entities.iterator();
        while (ia.hasNext()) {
            String entNm = (String) ia.next();
            s += convertEntityNL(entNm, ses);
        }
        return s;
    }
    public static String convertAspectNL(String aspRedNm,String aspect, sesRelation ses) {
        String s = "";
        Set<Object> entities = ses.aspectHasEntity.get(aspect);
               
        Iterator<Object> ia = entities.iterator();
        if (entities.size() > 2) {
            String first = (String) ia.next();
            while (ia.hasNext()) {
                String entNm = (String) ia.next();
                s += entNm + ", ";
            }
            s += "and " + first+"!\n";
        }else if(entities.size()==2){
        	String first = (String) ia.next();
            while (ia.hasNext()) {
                String entNm = (String) ia.next();
                s += entNm + " ";
            }
            s += "and " + first+"!\n";
        	
        } 
        else if (entities.size() > 0) {
            s += (String) ia.next() +"!\n";
        }
        s += convertCouplingNL("From the " + aspRedNm + " perspective, ",
        		aspect, ses);
        s +="\n";
        ia = entities.iterator();
        while (ia.hasNext()) {
            String entNm = (String) ia.next();
            if(!entList.contains(entNm)){
            	s += convertEntityNL(entNm, ses);
        	}
            
        }
        return s;
    }

    public static String convertCouplingNL(String prefix, String aspect,
            sesRelation ses) {
        String s = "";
        if(ses.aspectHasCoupling != null){
	        if(ses.aspectHasCoupling.get(aspect) != null){
	        	HashSet<Object> es = new HashSet<Object>(ses.aspectHasCoupling.get(aspect));        
		        Iterator<Object> it = es.iterator();
		        while (it.hasNext()) {
		            Hashtable<Object,Object> f = (Hashtable<Object,Object>) it.next();
		            String source = (String) f.get("source");
		            String outport = (String) f.get("outport");
		            String destination = (String) f.get("destination");
		            String inport = (String) f.get("inport");
		            if(source.contains("all_")){
		            	int index = source.indexOf("_");
		            	String temp = source.substring(index+1);
		            	source = "all "+temp;
		            }else if(source.contains("each_")){
		            	int index = source.indexOf("_");
		            	String temp = source.substring(index+1);
		            	source = "each "+temp;
		            }
		            if(destination.contains("all_")){
		            	int index = destination.indexOf("_");
		            	String temp = destination.substring(index+1);
		            	destination = "all "+temp;
		            }else if(destination.contains("each_")){
		            	int index = destination.indexOf("_");
		            	String temp = destination.substring(index+1);
		            	destination = "each "+temp;
		            }
		            s += prefix + source + " sends  " + outport + " to " + destination + " as " + inport + "!\n";
		        }
	        }
        }
        return s;
    }

    public static sesRelation transform2(String natLangFile) {
        String contents = fileHandler.getContentsAsString(natLangFile);
        if (contents != null) {
            Pattern p = Pattern.compile("!");
            String[] sentences = p.split(contents);
            return transform(sentences, null, false);
        } else {
            System.out.println("wrong file path");
            return null;
        }
    }

    public static String showParseString(String natLangFile) {
        String contents = fileHandler.getContentsAsString(natLangFile);
        if (contents != null) {
            Pattern p = Pattern.compile("!");
            String[] sentences = p.split(contents);
            return showParseString(sentences);
        } else {
            return ("wrong file path");
        }

    }

    public static String showParseString(String[] sentences) {
        HashSet<Object> ents = new HashSet<Object>();
        sesRelation ses = null;
        Hashtable<Object,Object> f = null;
        sesParse par = new sesParse();
        int numProcessed = 0, numParsed = 0;
        String out = "", out2 = "";
        for (int i = 0; i < sentences.length; i++) {
            f = par.parse(sentences[i]);
            if (sentences[i] != null && !sentences[i].trim().equals("")) {
                if (f == null) {
                    numProcessed++;
                    out += "The input " + DomToXML.quote(sentences[i].trim()) + " PARSE FAILED\n\n";
                } else {
                    numProcessed++;
                    numParsed++;

                    out += ("The input: " + DomToXML.quote(sentences[i].trim()) + " ! \n\n");


                    out += " parsed into: " + f + "\n\n";
                    ents.add(f.get("entity"));
                    if (f.get("entities") != null) {
                        out2 += ents.toString() + f.get("entities") + "\n\n";
                    } else {
                        out2 += "No Entities Found\n\n";
                    }

                }
            }
        }
        out += "\nNUMBER OF UNINTERPRETTED INPUTS = " + (numProcessed - numParsed) + "\n\n";
        return out + "Entities :" + out2;

    }

    // ////////////////////
    public static void main(String argv[]) {

        System.exit(3);
    } // main
}
