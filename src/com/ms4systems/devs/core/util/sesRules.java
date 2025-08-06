package com.ms4systems.devs.core.util;

import java.util.*;
import java.util.regex.Pattern;

import com.ms4systems.devs.core.util.sesRelationExtend.pruningRules;

public class sesRules
        extends pruneOps {

//ent from spec implies ent2 from spec2
    public static boolean checkSpecChoiceOf(String entNm, String specNm,
            String entNm2, String specNm2) {

        return !isChoiceOf(entNm, specNm)
                || isUniqueChoiceOf(entNm2, specNm2);
    }

//ent from spec implies not ent2 from spec2
    public static boolean checkSpecNonChoiceOf(String entNm, String specNm,
            String entNm2, String specNm2) {
        return !(isChoiceOf(entNm, specNm)
                && isChoiceOf(entNm2, specNm2));
    }

//ent from spec implies  asp from aspectsOf ent2
    public static boolean checkAspChoiceOf(String entNm, String specNm,
            String aspNm, String entNm2) {
        return !isChoiceOf(entNm, specNm)
                || isAspChoiceOf(aspNm, entNm2);
    }

    public static int multiAspHasNumber(String multiAsp, String numberAttr) {
        return Integer.parseInt(
                getPruneElement(multiAsp).getAttribute(numberAttr));
    }

    public static boolean multiAspNumberInRange(String multiAsp, String numberAttr, int low, int high) {
        return multiAspHasNumber(multiAsp, numberAttr) >= low
                && multiAspHasNumber(multiAsp, numberAttr) < high;
    }

    //ent from spec implies  multiAspNumber in range
    public static boolean checkMultiAspNumber(
            String entNm, String specNm,
            String multiAsp, String entNm2, String numberAttr, int low, int high) {
        return !isChoiceOf(entNm, specNm)
                || !checkAspChoiceOf(entNm, specNm, multiAsp, entNm2)
                || multiAspNumberInRange(multiAsp, numberAttr, low, high);
    }

    public boolean checkRuleSatisfaction() {
        return checkSpecChoiceOf("red", "colorSpec", "large", "sizeSpec");
    }

    public static void main(String argv[]) {
        Pair p = parse("if select A from B for C under X then select D from E for F under Y  ");
        Hashtable<Object,Object> cond = (Hashtable<Object,Object>) p.getKey();
        Hashtable<Object,Object> action = (Hashtable<Object,Object>) p.getValue();
        sesRules sr = new sesRules();
        System.exit(3);
    } // main
/////////////////////////////////////////////////////////////////////

    public static Pair parse(String sentance) {
        if (sentance.trim().equals("")) {
            return null;
        }
        sesParse par = new sesParse();
        String[] groups = par.getParts(sentance);
        if (CheckIfSelectAspect(groups) != null) {
            return CheckIfSelectAspect(groups);
        } 
        else if (CheckIfSelectSpecialization(groups) != null) {
            return CheckIfSelectSpecialization(groups);
        }
        return null;
    }

    public static Pair CheckIfSelectSpecialization(String[] groups) {
        if (groups[0].toLowerCase().equals("if")) {
            if (!groups[1].toLowerCase().equals("not")) {
                for (int i = 1; i < groups.length; i++) {
                    if (groups[i].equals("then")) {
                        int separate = i + 1;
                        return new Pair(
                                checkSelectSpecialization(groups, 1, separate - 1),
                                checkSelectSpecialization(groups, separate, groups.length));

                    }
                }
            } else {//not follows if
                for (int i = 1; i < groups.length; i++) {
                    if (groups[i].equals("then")) {
                        int separate = i + 1;
                        return new Pair(
                                checkNotSelectSpecialization(groups, 2, separate - 1),
                                checkSelectSpecialization(groups, separate, groups.length));

                    }
                }
                return null;
            }
        }
        return null;
    }

    public static Pair CheckIfSelectSpecializationAndSetMult(String[] groups) {
        if (groups[0].toLowerCase().equals("if")) {
            for (int i = 1; i < groups.length; i++) {
                if (groups[i].equals("then")) {
                    int separate = i + 1;
                    return new Pair(
                            checkSelectSpecialization(groups, 1, separate - 1),
                            checkMultiplicity(groups, separate, groups.length));
                }
            }
        }
        return null;
    }

    public static Hashtable<Object,Object> checkMultiplicity(String[] groups, int start, int end) {
        Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (end - start == 8 && groups[start + 1].toLowerCase().equals("multiplicity")) {
            f.put("specToExpand", groups[start + 3]);
            f.put("multiplicity", groups[start + 5]);
            f.put("node", groups[start + 7]);
            return f;
        }
        return null;
    }

    public static Pair CheckIfSelectAspect(String[] groups) {
        return null;
    }

    public static Hashtable<Object,Object> checkSelectSpecialization(String[] groups, int start, int end) {
        Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if(end - start >= 6 && groups[start].toLowerCase().equals("select")&& groups[start+5].equals("all")){
        	f.put("select", groups[start]);
            f.put("entToSelect", groups[start + 1]);
            f.put("specialization", groups[start + 3]);
            f.put("parent", groups[start + 6]);
            // all keyword for multiple aspect (cs 11/01/12)
            sesRelationExtend.allActionEnt.add(groups[start+6]);
            String underSeq = "";
            for (int i = start + 7; i < end; i += 2) {
                if (groups[i].equals("under")) {
                    underSeq += groups[i + 1] + ".";
                }
            }
            f.put("underSequence", underSeq);
            return f;
        }else
        if (end - start >= 6 && groups[start].toLowerCase().equals("select")) {
            f.put("select", groups[start]);
            f.put("entToSelect", groups[start + 1]);
            f.put("specialization", groups[start + 3]);
            f.put("parent", groups[start + 5]);
            String underSeq = "";
            for (int i = start + 6; i < end; i += 2) {
                if (groups[i].equals("under")) {
                    underSeq += groups[i + 1] + ".";
                }
            }
            f.put("underSequence", underSeq);
            return f;
        }
        return null;
    }

    public static Hashtable<Object,Object> checkNotSelectSpecialization(String[] groups, int start, int end) {
        Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (end - start >= 6 && groups[start].toLowerCase().equals("select")) {
            f.put("not", "not");
            f.put("select", groups[start]);
            f.put("entToSelect", groups[start + 1]);
            f.put("specialization", groups[start + 3]);
            f.put("parent", groups[start + 5]);
            String underSeq = "";
            for (int i = start + 6; i < end; i += 2) {
                if (groups[i].equals("under")) {
                    underSeq += groups[i + 1] + ".";
                }
            }
            f.put("underSequence", underSeq);
            return f;
        }
        return null;
    }

    public static Hashtable<Object,Object> checkSelectAspect(String[] groups) {
        Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (groups.length >= 6 && groups[0].toLowerCase().equals("select")
                && groups[3].toLowerCase().equals("aspects")) {
            f.put("selectAspect", groups[0]);
            f.put("aspToSelect", groups[1]);
            f.put("parent", groups[5]);
            String underSeq = "";
            for (int i = 6; i < groups.length; i += 2) {
                if (groups[i].equals("under")) {
                    underSeq += groups[i + 1] + ".";
                }
            }
            f.put("underSequence", underSeq);
            return f;
        }
        return null;
    }

    public static Pair selectAspectFromEntity(String asp, String ent) {
        return new Pair(ent, ent + "-" + asp + "Dec," + ent);
    }

    public static Pair selectAspectFromEntityInContext(
            String asp, String ent, String context) {
        return new Pair(ent, context + ":" + ent + "-" + asp + "Dec," + ent);
    }

    public static Pair selectEntityFromSpec(String ent, String spec, String parentEnt) {
        return new Pair(parentEnt, ent + "," + parentEnt + "-" + spec + "Spec");
    }

    public static Pair selectEntityFromSpecInContext(
            String ent, String spec, String parentEnt, String context) {
        return new Pair(parentEnt, context + ":" + ent + "," + parentEnt + "-" + spec + "Spec");
    }

    public static pruningRules parseNInterpret(String contents, sesRelationExtend ses) {
        pruningRules pr = new pruningRules();    	
        Pattern p = Pattern.compile("!");
        String[] sentences = p.split(contents);

        Pair condAct = null;

        for (int i = 0; i < sentences.length; i++) {
            condAct = sesRules.parse(sentences[i]);
            if (condAct == null) {
                continue;
            }
            Hashtable<Object,Object> cond = (Hashtable<Object,Object>) condAct.getKey();
            Hashtable<Object,Object> act = (Hashtable<Object,Object>) condAct.getValue();
            if (cond == null) {
                continue;
            }
            if (act == null) {
                continue;
            }
            
            if (cond.get("select") != null) {
                Pair cc, aa = null;
                String entToSelect = (String) cond.get("entToSelect");
                String specialization = (String) cond.get("specialization");
                String parent = (String) cond.get("parent");
                HashSet<Object> es = new HashSet<Object>();
                if (cond.get("not") == null) {
                    es.add(entToSelect);
                } else {
                    Hashtable<Object,HashSet<Object>> specHasEntity = ses.getRelation("specHasEntity");
                    String spec = parent + "-" + specialization + "Spec";
                    Set specents = specHasEntity.get(spec);
                    specents.remove(entToSelect);
                    es.addAll(specents);
                }
                for (Object o : es) {
                    String tempentToSelect = o.toString();

                    String underSeq = (String) cond.get("underSequence");
                    if (underSeq.length() == 0) {
                        cc = selectEntityFromSpec(tempentToSelect, specialization, parent);
                    } else {
                        cc = selectEntityFromSpecInContext(tempentToSelect,
                                specialization, parent, underSeq);
                    }

                    if (act.get("select") != null) {
                        String actentToSelect = (String) act.get("entToSelect");
                        String actspecialization = (String) act.get("specialization");
                        String actparent = (String) act.get("parent");
                       String  actunderSeq = (String) act.get("underSequence");
                        if (actunderSeq.length() == 0) {
                            aa = selectEntityFromSpec(actentToSelect, actspecialization, actparent);
                        } else {
                            aa = selectEntityFromSpecInContext(actentToSelect,
                                    actspecialization, actparent, actunderSeq);
                        }
                    }
                    pr.add(cc.getKey().toString(),
                            cc.getValue().toString(),
                            aa);
                }
            }
        }
        return pr;
    }
}
