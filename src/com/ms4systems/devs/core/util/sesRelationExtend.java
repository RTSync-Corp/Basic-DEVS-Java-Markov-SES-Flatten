package com.ms4systems.devs.core.util;

import org.w3c.dom.*;
import java.util.*;
import java.util.regex.*;

public class sesRelationExtend extends sesRelation {
//upgraded to sesRelation

    public pruningRules PruningRules;
    public static HashSet<Object> enums = new HashSet<Object>();

    public sesRelationExtend() {
        super();
        PruningRules = new pruningRules();
    }

    public sesRelationExtend(String xmlFile) {
        super(xmlFile);
        PruningRules = new pruningRules();
    }

    public sesRelationExtend(Document doc) {
        super(doc);
        PruningRules = new pruningRules();
    }

    public sesRelationExtend(sesRelation ses) {
        copyFrom(ses);
        PruningRules = new pruningRules();
    }

    public sesRelationExtend(String folderTxt, String sestxtfile) {
        sesRelation ses =
                createNAddImpliedInheritance(folderTxt, sestxtfile);
        copyFrom(ses);
        PruningRules = addPruningRules(folderTxt + sestxtfile);
    }
    public sesRelationExtend(String folderTxt, String sestxtfile, String explain) {
    	PruningRules = new pruningRules();
      sesRelation ses =
              createNAddImpliedInheritance(folderTxt, sestxtfile);
      for(String subSesFile : subSeSList){
    	  sesRelation subSeS = createNAddImpliedInheritance(folderTxt, subSesFile);
    	  ses.mergeSeS(subSeS);
      }
      copyFrom(ses);
      refineSpecWithLike();
      PruningRules.addAll(addPruningRules(folderTxt + sestxtfile));
      
      for(String subSesFile : subSeSList){
    	  PruningRules.addAll(addPruningRules(folderTxt + subSesFile));
      }
      
  }
    // with a ses content (cseo 6/18/2018)
    public sesRelationExtend(String folderTxt, String sesContent, boolean content) {
    	PruningRules = new pruningRules();
      sesRelation ses =
    	  createNAddImpliedInheritanceWContent(folderTxt, sesContent);
      for(String subSesFile : subSeSList){
    	  sesRelation subSeS = createNAddImpliedInheritance(folderTxt, subSesFile);
    	  ses.mergeSeS(subSeS);
      }
      copyFrom(ses);
      refineSpecWithLike();
      PruningRules.addAll(addPruningRulesWContent(sesContent));
      
      for(String subSesFile : subSeSList){
    	  PruningRules.addAll(addPruningRules(folderTxt + subSesFile));
      }
      
    }
    public void doExtendedCoupling(String natLangFile) {
        if (natLangFile.equals("")) {
            return;
        }
        String contents = fileHandler.getContentsAsString(
                natLangFile);
        if (contents == null) {
            System.out.println("wrong file path");
        }
        parseExtended(contents);
        
    }
    // With a ses content (cseo 6/18/2018)
    public void doExtendedCouplingWContent(String sesContent) {
        
        if (sesContent == null) {
            System.out.println("no content");
        }
        parseExtended(sesContent);
        
    }
    public void parseExtended(String contents) {
        Pattern p = Pattern.compile("!");
        String[] sentences = p.split(contents);
        for (int i = 0; i < sentences.length; i++) {
            new sesParse().extendedParse(sentences[i], this);
        }
    }

    public pruningRules addPruningRules(String folderPlusnatLangFile) {
        if (folderPlusnatLangFile.equals("")) {
            return null;
        }
        String contents = fileHandler.getContentsAsString(folderPlusnatLangFile);
        if (contents == null) {
            System.out.println("wrong file path");
            return null;
        }
        return sesRules.parseNInterpret(contents, this);
    }
    // With a ses content (cseo 6/18/2018)
    public pruningRules addPruningRulesWContent(String sesContent) {
 
        if (sesContent == null) {
            System.out.println("wrong file path");
            return null;
        }
        return sesRules.parseNInterpret(sesContent, this);
    }
    public sesRelationExtend makeCopyExtend() {
        sesRelationExtend ses = new sesRelationExtend();
        ses.copyFrom(this);
        return ses;
    }

    public sesRelationExtend substructureExtend(String entity) {
        sesRelation sub = substructure(entity);
        sesRelationExtend ses = new sesRelationExtend();
        ses.copyFrom(sub);
        return ses;
    }
    protected static Hashtable<Object, Integer> nameGen = new Hashtable<Object, Integer>();
    public static Hashtable<Object, Object> multiplicityFn = new Hashtable<Object, Object>();
    public static Hashtable<Object, Object> multNumtoEnt = new Hashtable<Object, Object>();
    public static Hashtable<Object,Object> specToEnt = new Hashtable<Object, Object>();
    
    public static HashSet<String> allActionEnt = new HashSet<String>();
    public static List<String> subSeSList = new ArrayList<String>();
    public static List<String> subStructureList = new ArrayList<String>();
    public static List<String> subtractSeSList = new ArrayList<String>();
    
    public static String getNext(String s) {
        String res = "";
        if (nameGen.get(s) == null) {
            res = s + "0";
        } else {
            res = s + nameGen.get(s).intValue();
        }
        if (nameGen.containsKey(s)) {
            int size = nameGen.get(s).intValue();
            nameGen.put(s, new Integer(size + 1));
        } else {
            nameGen.put(s, new Integer(1));
        }
        return res;
    }

    public static String getNextAfterFirst(String s) {
        if (nameGen.containsKey(s)) {
            int size = nameGen.get(s).intValue();
            nameGen.put(s, new Integer(size + 1));
            return s + size;
        } else {
            nameGen.put(s, new Integer(1));
            return s;
        }

    }

    public static String getRepresentative(String s) {
        for (Object o : nameGen.keySet()) {
            String nm = o.toString();
            if (s.startsWith(nm)) {
                return nm;
            }
        }
        return "";
    }

    public static void resetAll() {
        nameGen = new Hashtable<Object, Integer>();
    }

    public static int getNextIntFor(String s) {
        return nameGen.get(s).intValue();
    }

    public static void reset(String s) {
        nameGen.remove(s);
    }
    public static void addSubSeSFile(String fileName){
    	subSeSList.add(fileName);
    }
    public static void addSubStructure(String fileName){
    	subStructureList.add(fileName);
    }
    public static void addSubtractSeS(String fileName){
    	subtractSeSList.add(fileName);
    }
    public static void addSpecAndMultiplicity(String specToExpand, int[] max) {
        multiplicityFn.put(specToExpand, max);
    }
    public static void addMultNumtoEnt(String ent, Integer num){
    	multNumtoEnt.put(ent, num);
    }
    
    @Override
    public void restructureAllMultiAspectsOverSpec(String spec) {
        Set<Object> grandParents = entityHasAspect.keySet();
        for (Object o : grandParents) {
            String grandParent = (String) o;
            Set<Object> aspects = entityHasAspect.get(grandParent);
            if (aspects != null) {
                HashSet<Object> as = new HashSet<Object>();
                as.addAll(aspects);
                Iterator<Object> im = as.iterator();
                while (im.hasNext()) {
                    String aspNm = (String) im.next();
                    Set<Object> ents = aspectHasEntity.get(aspNm);
                    HashSet<Object> os = new HashSet<Object>();
                    os.addAll(ents);
                    for (Object oe : os) {
                        String entity = (String) oe;
                        restructureMultiAspect(entity, aspNm, spec);
                    }
                }
            }
        }
    }

    @Override
    public void restructureMultiAspect(String entity, String grandParAsp, String spectoexpand) {
        String separator = "";
        Hashtable<Object, Object> entsNsubsToAdd = new Hashtable<Object, Object>();
        String gchd = getSingleOfMult(entity);
        if (gchd.equals("")) {
            return;
        }
        Set<Object> multiAspects = entityHasMultiAspect.get(entity);
        Iterator<Object> im = multiAspects.iterator();
        if (im.hasNext()) {
            String maspNm = (String) im.next();
            String chd = (String) (new LinkedList<Object>(multiAspectHasEntity.get(maspNm))).getFirst();
            Set<Object> specs = entityHasSpec.get(chd);
            spectoexpand = chd + "-" + spectoexpand + "Spec";
            int imax = 0;
            int iimax = 0;
            int iiimax = 0;
            if (specs.contains(spectoexpand)) {
                int[] max = (int[]) multiplicityFn.get(spectoexpand);
                if (max == null) {
                    System.out.println("MultiplicityFn not defined for " + spectoexpand);
                } else {

                    if (max.length == 1) {
                        imax = max[0];
                    } else if (max.length == 2) {
                        imax = max[0];
                        iimax = max[1];
                    } else if (max.length == 3) {
                        imax = max[0];
                        iimax = max[1];
                        iiimax = max[2];
                    }
                }

                HashSet<Object> enums = getBackEnums(specHasEntity.get(spectoexpand));
                if(enums == null) enums = new HashSet<Object>();
                
                HashSet<Object> ents = new HashSet<Object>();
                TreeSet oents = new TreeSet(enums);
                
                
                enums.addAll(oents);
                int count = oents.size();
                if (count == 0 || max == null) {
                    ents = new HashSet<Object>(oents);
                } else if (count == 1) {
                    Iterator oie = oents.iterator();
                    String ospecEnt = (String) oie.next();
                    reset(ospecEnt);
                    for (int i = 0; i < imax; i++) {
                        ents.add(getNext(ospecEnt));
                    }
                } else if (count == 2) {
                    Iterator oie = oents.iterator();
                    String ospecEnti = (String) oie.next();
                    reset(ospecEnti);
                    String ospecEntii = (String) oie.next();
                    for (int i = 0; i < imax; i++) {
                        String ent = getNext(ospecEnti);
                        reset(ospecEntii);
                        for (int ii = 0; ii < iimax; ii++) {
                            ents.add(ent
                                    + separator + getNext(ospecEntii));
                        }
                    }
                } else if (count == 3) {
                    Iterator oie = oents.iterator();
                    String ospecEnti = (String) oie.next();
                    String ospecEntii = (String) oie.next();
                    String ospecEntiii = (String) oie.next();
                    reset(ospecEnti);
                    for (int i = 0; i < imax; i++) {
                        String ent = getNext(ospecEnti);
                        reset(ospecEntii);
                        for (int ii = 0; ii < iimax; ii++) {
                            String ient = ent + separator + getNext(ospecEntii);
                            reset(ospecEntiii);
                            for (int iii = 0; iii < iiimax; iii++) {
                                ents.add(ient
                                        + separator + getNext(ospecEntiii));
                            }
                        }
                    }
                }
                HashSet<Object> pars = new HashSet<Object>();
                Enumeration<Object> e = entityHasSpec.keys();
                while (e.hasMoreElements()) {
                    Object key = e.nextElement();
                    HashSet<Object> valSet = entityHasSpec.get(key);
                    Iterator it = valSet.iterator();
                    while (it.hasNext()) {
                        Object val = it.next();
                        if (val.equals(spectoexpand)) {
                            pars.add(key);
                        }
                    }
                }

                this.removeSpec(spectoexpand);
                this.addSpecToEntity(spectoexpand, entity);// chd);
                Iterator ip = pars.iterator();
                while (ip.hasNext()) {
                    String par = (String) ip.next();
                    this.addSpecToEntity(spectoexpand, par);
                }

                Iterator ie = ents.iterator();
                while (ie.hasNext()) {
                    String specEnt = (String) ie.next();
                    this.addEntityToSpec(specEnt, spectoexpand);
                    sesRelation substr = substructure(specEnt);
                    substr.addRename(specEnt, specEnt + "_" + gchd);
                    substr.replaceAll();
                    substr.setRoot(specEnt + "_" + gchd);
                    Set<Object> subasps = substr.entityHasAspect.get(specEnt + "_" + gchd);
                    for (Object asp : subasps) {
                        int ind = asp.toString().lastIndexOf("-");
                        String actualAsp = asp.toString().substring(ind);
                        substr.addRename(asp.toString(), specEnt + "_" + gchd + "_" + actualAsp.toString());
                    }
                    Set<Object> subspecs = substr.entityHasSpec.get(specEnt + "_" + gchd);
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
                    if (cop.entityHasSpec.containsKey(gchd)) {
                        cop.entityHasSpec.get(gchd).remove(spectoexpand);
                    }
                    Set<Object> asps = cop.entityHasAspect.get(gchd);
                    if (asps != null) {
                        for (Object asp : asps) {
                            cop.addRename(asp.toString(), specEnt + "_" + asp.toString());
                        }
                    }
                    cop.addRename(gchd, specEnt + "_" + gchd);
                    Set<Object> cspecs = cop.entityHasSpec.get(gchd);
                    Iterator<Object> itc = cspecs.iterator();
                    while (itc.hasNext()) {
                        String cspec = itc.next().toString();
                        cop.addRename(cspec, specEnt + "_" + cspec);
                    }

                    cop.replaceAll();
                    mergeSeS(cop);
                }

                if (aspectHasEntity.containsKey(grandParAsp)) {
                    aspectHasEntity.get(grandParAsp).remove(entity);
                }
            }
            Enumeration<Object> e = entsNsubsToAdd.keys();
            while (e.hasMoreElements()) {
                Object key = e.nextElement();
                Object val = entsNsubsToAdd.get(key);
                addEntityToAspect(key.toString(), grandParAsp);
                sesRelation substr = (sesRelation) val;
                mergeSeS(substr);
            }

        }
    }
    public static boolean isNumber(String s) {
        try {
            int i = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static HashSet<Object> getBackEnums(Set ents) {
    	HashSet<Object> res = new HashSet<Object>();
        Iterator it = ents.iterator();
        while (it.hasNext()) {
            String Enum = it.next().toString();
            boolean foundNumberSubstring = false;
            for (int i = 1; i < Enum.length(); i++) {
            if (isNumber(Enum.substring(i))){
                res.add(Enum.substring(0,i));
                foundNumberSubstring = true;
                break;
            }
            }
            if (!foundNumberSubstring)
             res.add(Enum);
        }
        return res;
    }
    public HashSet<Object> commonVariables = new HashSet<Object>();

    public static void addVarToEntities(String var, sesRelationExtend ses) {
        ses.commonVariables.add(var);
        HashSet<Object> entities = ses.getEnsembleSet("entityNames");
        for (Object o : entities) {
            String ent = o.toString();
            ses.addVarToEntity(var, ent);
            ses.addRangeToVar("double", ent + "." + var);
            ses.addVarToEntity(var + "DefaultValue", ent);
            ses.addRangeToVar("1", ent + "." + var + "DefaultValue");
        }
    }

    public void addVarToEntities(String var) {
        addVarToEntities(var, this);
    }

    public void expandSpec(String spectoexpand, int[] max) {
        int indh = spectoexpand.indexOf("-");
        String chd = spectoexpand.substring(0, indh);
        String separator = "";
        Hashtable<Object, Object> entsNsubsToAdd = new Hashtable<Object, Object>();
        int imax = 0;
        int iimax = 0;
        int iiimax = 0;
        if (max == null) {
            System.out.println("MultiplicityFn not defined for " + spectoexpand);
        } else {
            if (max.length == 1) {
                imax = max[0];
            } else if (max.length == 2) {
                imax = max[0];
                iimax = max[1];
            } else if (max.length == 3) {
                imax = max[0];
                iimax = max[1];
                iiimax = max[2];
            }
        }
        HashSet<Object> ents = new HashSet<Object>();
        TreeSet oents = new TreeSet(specHasEntity.get(spectoexpand));
        int count = oents.size();
        if (count == 0 || max == null) {
            ents = new HashSet<Object>(oents);
        } else if (count == 1) {
            Iterator oie = oents.iterator();
            String ospecEnt = (String) oie.next();
            reset(ospecEnt);
            for (int i = 0; i < imax; i++) {
                ents.add(getNext(ospecEnt));
            }
        } else if (count == 2) {
            Iterator oie = oents.iterator();
            String ospecEnti = (String) oie.next();
            reset(ospecEnti);
            String ospecEntii = (String) oie.next();
            for (int i = 0; i < imax; i++) {
                String ent = getNext(ospecEnti);
                reset(ospecEntii);
                for (int ii = 0; ii < iimax; ii++) {
                    ents.add(ent
                            + separator + getNext(ospecEntii));
                }
            }
        } else if (count == 3) {
            Iterator oie = oents.iterator();
            String ospecEnti = (String) oie.next();
            String ospecEntii = (String) oie.next();
            String ospecEntiii = (String) oie.next();
            reset(ospecEnti);
            for (int i = 0; i < imax; i++) {
                String ent = getNext(ospecEnti);
                reset(ospecEntii);
                for (int ii = 0; ii < iimax; ii++) {
                    String ient = ent + separator + getNext(ospecEntii);
                    reset(ospecEntiii);
                    for (int iii = 0; iii < iiimax; iii++) {
                        ents.add(ient
                                + separator + getNext(ospecEntiii));
                    }
                }
            }
        }
        HashSet<Object> pars = new HashSet<Object>();

        Enumeration<Object> e = entityHasSpec.keys();
        while (e.hasMoreElements()) {
            Object key = e.nextElement();
            HashSet<Object> valSet = entityHasSpec.get(key);
            Iterator it = valSet.iterator();
            while (it.hasNext()) {
                Object val = it.next();
                if (val.equals(spectoexpand)) {
                    pars.add(key);
                }

            }
        }

        this.removeSpec(spectoexpand);
        Iterator ip = pars.iterator();
        while (ip.hasNext()) {
            String par = (String) ip.next();
            this.addSpecToEntity(spectoexpand, par);
        }
        Iterator ie = ents.iterator();
        while (ie.hasNext()) {
            String specEnt = (String) ie.next();
            this.addEntityToSpec(specEnt, spectoexpand);
            sesRelation substr = substructure(specEnt);
            substr.addRename(specEnt, specEnt + "_" + chd);
            substr.replaceAll();
            substr.setRoot(specEnt + "_" + chd);
            Set subasps = substr.entityHasAspect.get(specEnt + "_" + chd);
            for (Object asp : subasps) {
                int ind = asp.toString().lastIndexOf("-");
                String actualAsp = asp.toString().substring(ind);
                substr.addRename(asp.toString(), specEnt + "_" + chd + "_" + actualAsp.toString());
            }
            Set subspecs = substr.entityHasSpec.get(specEnt + "_" + chd);
            for (Object spec : subspecs) {
                int ind = spec.toString().lastIndexOf("-");
                String actualSpec = spec.toString().substring(ind);
                substr.addRename(spec.toString(), specEnt + "_" + chd + actualSpec.toString());
            }
            substr.replaceAll();
            entsNsubsToAdd.put(specEnt + "_" + chd, substr);
            sesRelation sub = substructure(chd);
            sesRelation cop = new sesRelation();
            cop.copyFrom(sub);
            if (cop.entityHasSpec.containsKey(chd)) {
                cop.entityHasSpec.get(chd).remove(spectoexpand);
            }
            Set asps = cop.entityHasAspect.get(chd);
            for (Object asp : asps) {
                cop.addRename(asp.toString(), specEnt + "_" + asp.toString());
            }
            cop.addRename(chd, specEnt + "_" + chd);
            Set cspecs = cop.entityHasSpec.get(chd);
            Iterator itc = cspecs.iterator();
            while (itc.hasNext()) {
                String cspec = itc.next().toString();
                cop.addRename(cspec, specEnt + "_" + cspec);
            }
            cop.replaceAll();
            mergeSeS(cop);
        }
    }

    public boolean isSpecOfMultiAsp(String spec) {
        Set grandParents = entityHasAspect.keySet();
        for (Object o : grandParents) {
            String grandParent = (String) o;
            Set aspects = entityHasAspect.get(grandParent);
            Iterator im = aspects.iterator();
            while (im.hasNext()) {
                String aspNm = (String) im.next();
                Set ents = aspectHasEntity.get(aspNm);
                for (Object oe : ents) {
                    String entity = (String) oe;
                    Set multiAspects = entityHasMultiAspect.get(entity);
                    if (multiAspects != null) {
                        Iterator ima = multiAspects.iterator();
                        if (ima.hasNext()) {
                            String maspNm = (String) ima.next();
                            String chd = (String) (new LinkedList<Object>(multiAspectHasEntity.get(maspNm))).getFirst();
                            if (spec.startsWith(chd)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

/////////////////////////////
    public static class pruningRule {

        public String entity, condition;
        public Pair action;

        public pruningRule(String entity, String condition, Pair action) {
            this.entity = entity;
            this.condition = condition;
            this.action = action;
        }
    }

    public static class pruningRules extends HashSet<pruningRule> {

        public pruningRules() {
        }

        public void add(String entity, String condition, Pair action) {
            add(new pruningRule(entity, condition, action));
        }

        public HashSet<Object> getAction(String entity, String condition) {
            HashSet<Object> es = new HashSet<Object>();
            for (pruningRule pr : this) {
                if (pr.entity.equals(entity)
                        && pr.condition.equals(condition)) {
                    es.add(pr.action);
                }
            }
            return es;
        }
    }

    public Hashtable<Object, HashSet<Object>> specEntsToSpecs() {
        Hashtable<Object, HashSet<Object>> r = new Hashtable<Object, HashSet<Object>>();
        Hashtable<Object, HashSet<Object>> specHasEntity = this.specHasEntity();
        Enumeration<Object> e = specHasEntity.keys();
        while (e.hasMoreElements()) {
            Object key = e.nextElement();
            HashSet<Object> valSet = specHasEntity.get(key);
            Iterator<Object> it = valSet.iterator();
            while (it.hasNext()) {
                Object val = it.next();
                Object spec = key;
                Object specEnt = val;

                if (r.containsKey(specEnt)) {
                    r.get(specEnt).add(spec);
                } else {
                    HashSet<Object> vSet = new HashSet<Object>();
                    vSet.add(spec);
                    r.put(specEnt, vSet);
                }
            }
        }

        return r;
    }

    public String downSelect(String specEnt, String spec) {
        if (!specEntsToSpecs().containsKey(specEnt) && !specEntsToSpecs().get(specEnt).contains(spec)) {
            return null;
        }
        if (specHasEntity.containsKey(spec)) {
            specHasEntity.get(spec).remove(specEnt);
        }

        return specEnt;
    }

    public String downSelectFromEntity(String specEnt, String entity) {
        Set uspecs = specEntsToSpecs().get(specEnt);
        Set dspecs = this.entityHasSpec.get(entity);
        HashSet<Object> es = new HashSet<Object>(uspecs);
        es.retainAll(dspecs);//commom specs containing specEnt should be the one.
        if (es.size() == 1) {
            return downSelect(specEnt, (String) es.iterator().next());
        } else {
            return null;
        }
    }

///////////////////////////////////
    public void addInportDownTo(String inport, String target, String parent) {
        if (parent.equals(target)) {
            return;
        }
        Set aspects = entityHasAspect.get(parent);
        for (Object a : aspects) {
            String aspect = (String) a;
            Set chds = aspectHasEntity.get(aspect);
            for (Object chd : chds) {
                sesRelation sub = substructure(chd.toString());
                if (sub.entityNames.contains(target)) {
                    Hashtable<Object, Object> f = new Hashtable<Object, Object>();
                    f.put("outport", inport);
                    f.put("source", parent);
                    f.put("inport", inport);
                    f.put("destination", chd.toString());
                    addCouplingToAspect(f, aspect);
                    addInportDownTo(inport, target, chd.toString());
                }
            }
        }
    }

    public void addOutportDownTo(String outport, String target, String parent) {
        if (parent.equals(target)) {
            return;
        }
        Set aspects = entityHasAspect.get(parent);
        for (Object a : aspects) {
            String aspect = (String) a;
            Set chds = aspectHasEntity.get(aspect);
            for (Object chd : chds) {
                sesRelation sub = substructure(chd.toString());
                if (sub.entityNames.contains(target)) {
                    Hashtable<Object, Object> f = new Hashtable<Object, Object>();
                    f.put("outport", outport);
                    f.put("source", chd.toString());
                    f.put("inport", outport);
                    f.put("destination", parent);
                    addCouplingToAspect(f, aspect);
                    addOutportDownTo(outport, target, chd.toString());
                }
            }
        }
    }

    public String findCommonParent(String entity, String entity1) {
        HashSet<Object> es = findCommonParents(entity, entity1);
        return findCommonAncestor(es);
    }

    public HashSet<Object> findCommonParents(String entity, String entity1) {
        HashSet<Object> parents = new HashSet<Object>();
        for (Object o : entityNames) {
            sesRelation sub = substructure(o.toString());
            if (sub.entityNames.contains(entity)
                    && sub.entityNames.contains(entity1)) {
                parents.add(o.toString());
            }
        }
        return parents;
    }

    public HashSet<Object> findCommonAncestor(HashSet<Object> es, String parent) {
        for (Object o : es) {
            sesRelation sub = substructure(o.toString());
            if (sub.entityNames.contains(parent)
                    && !parent.equals(o.toString())) {
                es.remove(o);
                return es;
            }
        }
        return null;
    }

    public String findCommonAncestor(HashSet<Object> es) {
        if (es.isEmpty()) {
            System.out.println("Empty common set");
            System.exit(3);
        }
        if (es.size() == 1) {
            return es.iterator().next().toString();
        }
        for (Object o : es) {
            HashSet<Object> res = findCommonAncestor(es, o.toString());
            if (res != null) {
                return findCommonAncestor(res);
            }
        }
        return es.iterator().next().toString();
    }

    public String representsTargetUnderParent(String target, String parent) {
        Set aspects = entityHasAspect.get(parent);
        for (Object a : aspects) {
            String aspect = (String) a;
            Set chds = aspectHasEntity.get(aspect);
            for (Object chd : chds) {
                sesRelation sub = substructure(chd.toString());
                if (sub.entityNames.contains(target)) {
                    return chd.toString();
                }
            }
        }
        return "";
    }

    public void addExtendedCoupling(String source, String port, String dest) {
        String parent = findCommonParent(source, dest);
        String repOfSource = representsTargetUnderParent(source, parent);
        String repOfDest = representsTargetUnderParent(dest, parent);
        Set aspects = entityHasAspect.get(parent);
        for (Object a : aspects) {
            String aspect = (String) a;
            Set chds = aspectHasEntity.get(aspect);
            if (chds.contains(repOfSource) && chds.contains(repOfDest)) {
                Hashtable<Object, Object> f = new Hashtable<Object, Object>();
                f.put("outport", "out" + port);
                f.put("source", repOfSource);
                f.put("inport", "in" + port);
                f.put("destination", repOfDest);
                addCouplingToAspect(f, aspect);
            }
        }
        addOutportDownTo("out" + port, source, repOfSource);
        addInportDownTo("in" + port, dest, repOfDest);
    }
    public HashSet<Object> getEntitySet(){
    	return entityNames;
    }
    ////////////////////////////
    public static void main(String argv[]) {
        sesRelation ses = new sesRelation();
        ses.setRoot("balls");

        ses.addMultiAspectToEntity("ballsMultiAsp", "balls");
        ses.addEntityToMultiAspect("ball", "ballsMultiAsp");

        ses.addVarToEntity("weight", "ball");
        ses.addRangeToVar("double", "ball.weight");
        ses = new sesRelation("sesForballs.xml");
        ses.printTree();
        ses.substructure("ball").printTree();
        System.exit(3);
    } // main
}
