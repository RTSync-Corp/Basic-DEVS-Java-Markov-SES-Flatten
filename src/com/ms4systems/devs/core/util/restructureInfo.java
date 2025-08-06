/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ms4systems.devs.core.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.regex.*;

/**
 *
 * @author Bernie
 */
public class restructureInfo extends sesRelation {

    public static Hashtable<Object,Object> parse(String sentance) {
        sesParse par = new sesParse();
        String[] groups = par.getParts(sentance);
        if (checkSpecsToExpand(groups) != null) {
            return checkSpecsToExpand(groups);
        } else if (checkMultiplicity(groups) != null) {
            return checkMultiplicity(groups);
        } else if (checkAddCommonVar(groups) != null) {
            return checkAddCommonVar(groups);
        } else if (checkSetVarVal(groups) != null) {
            return checkSetVarVal(groups);
        } else if (checkBound(groups) != null) {
            return checkBound(groups);
        } else if (checkSubSeS(groups)!=null){
        	return checkSubSeS(groups);
        } else if (checkSubStructure(groups)!=null){
        	return checkSubStructure(groups);
        } else if (checkSubtractSeS(groups)!=null){
        	return checkSubtractSeS(groups);
        } else if (checkMergeAll(groups)!=null){
        	return checkMergeAll(groups);
        }
        return null;
    }
    // To extract sub ses files
    public static Hashtable<Object,Object> checkSubtractSeS(String[] groups){
    	Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (groups.length == 2
                && groups[0].toLowerCase().equals("subtract")) {
            f.put("subtract", groups[1]);
            return f;
        }
    	return null;
    }
    // To extract sub ses files
    public static Hashtable<Object,Object> checkSubStructure(String[] groups){
    	Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (groups.length == 3
                && groups[0].toLowerCase().equals("substructure")) {
            f.put("substructure", groups[2]);
            return f;
        }
    	return null;
    }
    // To extract sub ses files
    public static Hashtable<Object,Object> checkSubSeS(String[] groups){
    	Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (groups.length == 3
                && groups[0].toLowerCase().equals("add")
                && groups[1].toLowerCase().equals("ses")) {
            f.put("subses", groups[2]);
            return f;
        }
    	return null;
    }
    public static Hashtable<Object,Object> checkMergeAll(String[] groups){
    	Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (groups.length == 3
                && groups[0].equals("mergeAll")) {
            f.put("mergeAll", groups[2]);
            return f;
        }
    	return null;
    }
    public static Hashtable<Object,Object> checkBound(String[] groups) {
        Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (groups.length >= 7
                && groups[0].toLowerCase().equals("set")
                && groups[1].toLowerCase().equals("count")
                && groups[2].toLowerCase().equals("bounds")) {
            f.put("entity", groups[4]);
            f.put("bounds", groups[6]);
            if (groups.length==9){
                f.put("parent",groups[8]);
            }
            return f;
        }
        return null;
    }

    public static Hashtable<Object,Object> checkSpecsToExpand(String[] groups) {
    	Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (groups.length >= 4 && groups[0].toLowerCase().equals("restructure")) {
            f.put("restructure", groups[0]);
            f.put("specToExpand", groups[3]);
            String thenSeq = "";
            for (int i = 4; i < groups.length; i += 2) {
                if (groups[i].equals("then")) {
                    thenSeq += groups[i + 1] + ".";
                }
            }
            f.put("thenSequence", thenSeq);
            return f;
        }
        return null;
    }

    public static Hashtable<Object,Object> checkMultiplicity(String[] groups) {
    	Hashtable<Object,Object> f = new Hashtable<Object,Object>();
    	// handling "set multiplicity of fast in speed as [9]!" 
    	if(groups.length == 8
                && groups[0].toLowerCase().equals("set")
                && groups[1].toLowerCase().equals("multiplicity")&& groups[4].toLowerCase().equals("in")){
    		f.put("specToExpand", groups[5]); // speed
            f.put("multiplicity", groups[7]); // [9]
            f.put("entity", groups[3]); //fast
    		return f;
    	}else
        if (groups.length == 8
                && groups[0].toLowerCase().equals("set")
                && groups[1].toLowerCase().equals("multiplicity")) {
            f.put("specToExpand", groups[3]);
            f.put("multiplicity", groups[5]);
            f.put("node", groups[7]);
            return f;
        }
        return null;
    }

    public static Hashtable<Object,Object> checkAddCommonVar(String[] groups) {
    	Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (groups.length == 4
                && groups[0].toLowerCase().equals("add")
                && groups[1].toLowerCase().equals("common")
                && groups[2].toLowerCase().equals("variable")) {
            f.put("add", groups[1]);
            f.put("commonVar", groups[3]);
            return f;
        }
        return null;
    }

    public static Hashtable<Object,Object> checkSetVarVal(String[] groups) {
    	Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (groups.length == 5
                && groups[0].toLowerCase().equals("set")
                && groups[3].toLowerCase().equals("to")) {
            f.put("set", groups[0]);
            String entity = groups[1].substring(0, groups[1].length() - 2);
            f.put("entity", entity);
            f.put("variable", groups[2]);
            f.put("value", groups[4]);
            return f;
        }
        return null;
    }

    public static Vector<String> parseNReturn(String contents) {
    	Vector<String> vRestructure = new Vector<String>();
        Pattern p = Pattern.compile("!");
        String[] sentences = p.split(contents);

        Hashtable<Object,Object> f = null;

        for (int i = 0; i < sentences.length; i++) {
            f = restructureInfo.parse(sentences[i]);
            if (f == null) {
                continue;
            }
            if (f.get("restructure") != null) {
                String specToExpand = (String) f.get("specToExpand");
                String thenSeq = (String) f.get("thenSequence");
                if (thenSeq.length() == 0) {
                	vRestructure.add(specToExpand);
                } else {
                	vRestructure.add(specToExpand + ":" + thenSeq);
                }
            }
        }
        return vRestructure;
    }
    public static void parseNRestructure(String contents, sesRelationExtend ses) {
        Pattern p = Pattern.compile("!");
        String[] sentences = p.split(contents);

        Hashtable<Object,Object> f = null;

        for (int i = 0; i < sentences.length; i++) {
            f = restructureInfo.parse(sentences[i]);
            if (f == null) {
                continue;
            }
            if (f.get("restructure") != null) {
                String specToExpand = (String) f.get("specToExpand");
                String thenSeq = (String) f.get("thenSequence");
                ses.restructureAllMultiAspectsOverSpec(specToExpand);
                if (thenSeq.length() == 0) {
                } else {
                    java.util.HashSet<String> hs = getList(thenSeq);
                    for (String spec : hs) {
                        ses.restructureAllMultiAspectsOverSpec(spec);
                    }
                }
            }
            if (f.get("add") != null) {
                String var = f.get("commonVar").toString();
                sesRelationExtend.addVarToEntities(var, ses);
            }
            if (f.get("set") != null) {
                String entity = f.get("entity").toString();
                String var = f.get("variable").toString();
                String val = f.get("value").toString();
                ses.addVarToEntity(var, entity);
                ses.addRangeToVar("double", entity + "." + var);
                ses.addVarToEntity(var + "DefaultValue", entity);
                ses.addRangeToVar(val, entity + "." + var + "DefaultValue");
                int ind = entity.lastIndexOf("_");//get sub entity
                if (ind > -1) {
                    entity = entity.substring(0, ind);
                    ses.addVarToEntity(var, entity);
                    ses.addRangeToVar("double", entity + "." + var);
                    ses.addVarToEntity(var + "DefaultValue", entity);
                    ses.addRangeToVar(val, entity + "." + var + "DefaultValue");
                }
            }
        }
    }

    public static void parseNAdd(String contents, sesRelationExtend ses) {
        Pattern p = Pattern.compile("!");
        String[] sentences = p.split(contents);

        Hashtable<Object,Object> f = null;

        for (int i = 0; i < sentences.length; i++) {
            f = restructureInfo.parse(sentences[i]);
            if (f == null) {
                continue;
            }
            if (f.get("add") != null) {
                String var = f.get("commonVar").toString();
                sesRelationExtend.addVarToEntities(var, ses);
            }
            if (f.get("set") != null) {
                String entity = f.get("entity").toString();
                String var = f.get("variable").toString();
                String val = f.get("value").toString();
                ses.addVarToEntity(var, entity);
                ses.addVarToEntity(var + "DefaultValue", entity);
                ses.addRangeToVar(val, entity + "." + var + "DefaultValue");
                addToParent(entity, var, val, ses);
               inheritSuperRange(entity, var, val, ses);
            }
        }
    }
   public static void addToParent(String entity, String var, String val, sesRelation ses) {
                   int ind = entity.indexOf("_");
            if (ind > -1) {
                String parent = entity.substring(0,ind);
                ses.addVarToEntity(var + "DefaultValue", parent);
                ses.addRangeToVar(val, parent + "." + var + "DefaultValue");
            }
   }
   
    public static void inheritSuperRange(String entity, String var, String val, sesRelation ses) {
        int ind = entity.lastIndexOf("_");
        if (ind > 0) {
            String superEnt = entity.substring(ind + 1, entity.length());
            Hashtable<Object,Object> f = ses.varHasRange;
            String superRange = (String) f.get(superEnt + "." + var);
            String inhEnt = entity.substring(0, ind);
            if (superRange == null) {
                superRange = "string";
            }
            ses.addRangeToVar(superRange, inhEnt + "." + var);
            ses.addVarToEntity(var + "DefaultValue", inhEnt);
            ses.addRangeToVar(val, inhEnt + "." + var + "DefaultValue");
        }
    }

    public static java.util.HashSet<String> getList(String path) {
        java.util.HashSet<String> hs = new java.util.HashSet<String>();
        while (true) {
            int indOfDot = path.indexOf(".");
            if (indOfDot > -1) {
                hs.add(path.substring(0, indOfDot));
                path = path.substring(indOfDot + 1);
            } else {
                break;
            }
        }
        return hs;
    }
    public static void parseForSubSeSDoc(String contents){
    	Pattern p = Pattern.compile("!");
        String[] sentences = p.split(contents);

        Hashtable<Object,Object> f = null;

        for (int i = 0; i < sentences.length; i++) {
            f = restructureInfo.parse(sentences[i]);
            if (f == null) {
                continue;
            }
           if (f.get("multiplicity") != null) {
                String specToExpand = (String) f.get("specToExpand");
                String mult = (String) f.get("multiplicity");
                String node = (String) f.get("node");
                mult = mult.substring(1, mult.length() - 1).trim();
                int ind = mult.indexOf(",");
                int[] multint = new int[]{3, 3};
                if (ind == -1) {
                    multint = new int[]{Integer.parseInt(mult)};
                } else {
                    String first = mult.substring(0, ind).trim();
                    String second = mult.substring(ind + 1, mult.length()).trim();
                    multint[0] = Integer.parseInt(first);
                    ind = second.indexOf(",");
                    if (ind == -1) {
                        multint[1] = Integer.parseInt(second);
                    } else {
                        multint = new int[]{3, 3, 3};//do later
                    }
                }
                sesRelationExtend.addSpecAndMultiplicity(node + "-" + specToExpand + "Spec", multint);
            }
        }
    }
    public static void parseForMergeAll(String folderSeS,String contents){
    	Pattern p = Pattern.compile("!");
        String[] sentences = p.split(contents);

        Hashtable<Object,Object> f = null;

        for (int i = 0; i < sentences.length; i++) {
            f = restructureInfo.parse(sentences[i]);
            if (f == null) {
                continue;
            }
           if (f.get("mergeAll") != null) {
                String rootSeS = (String) f.get("mergeAll");
                sesRelationExtend ses = new sesRelationExtend(folderSeS, rootSeS);
                HashSet<Object> set = ses.getEntitySet();
                File sesFolder = new File(folderSeS);
                List<String> sesList = new ArrayList<String>();
                if(sesFolder.isDirectory()){
                	String[] fileList = sesFolder.list();
                	sesList = Arrays.asList(fileList);
                }
                for(Object ent : set){
                	String entName = ent.toString();
                	if(ses.getRootEntityName().equals(entName))continue; // Not add root entity
                	for(String fileName : sesList){
                		if(fileName.equals(rootSeS)||fileName.contains("Merged"))continue; // Not add a parent SeS file
                		sesRelationExtend subSeS = new sesRelationExtend(folderSeS,fileName);
                		String rootEntity = subSeS.getRootEntityName();
                		if(rootEntity.equals(entName)){
                			if(!sesRelationExtend.subSeSList.contains(fileName)){
                        		sesRelationExtend.addSubSeSFile(fileName);
                        		System.out.println("merged ses file : "+fileName);
                    		}
                		}
                		
                	}
                }
            }
        }
    }
    public static void parseForSubSeS(String contents){
    	Pattern p = Pattern.compile("!");
        String[] sentences = p.split(contents);

        Hashtable<Object,Object> f = null;

        for (int i = 0; i < sentences.length; i++) {
            f = restructureInfo.parse(sentences[i]);
            if (f == null) {
                continue;
            }
           if (f.get("subses") != null) {
                String subses = (String) f.get("subses");
                
                sesRelationExtend.addSubSeSFile(subses);
            }
        }
    }
    public static void parseForSubStructure(String contents){
    	Pattern p = Pattern.compile("!");
        String[] sentences = p.split(contents);

        Hashtable<Object,Object> f = null;

        for (int i = 0; i < sentences.length; i++) {
            f = restructureInfo.parse(sentences[i]);
            if (f == null) {
                continue;
            }
           if (f.get("substructure") != null) {
                String substructure = (String) f.get("substructure");
                
                sesRelationExtend.addSubStructure(substructure);
            }
        }
    }
    public static void parseForSubtractSeS(String contents){
    	Pattern p = Pattern.compile("!");
        String[] sentences = p.split(contents);

        Hashtable<Object,Object> f = null;

        for (int i = 0; i < sentences.length; i++) {
            f = restructureInfo.parse(sentences[i]);
            if (f == null) {
                continue;
            }
           if (f.get("subtract") != null) {
                String subses = (String) f.get("subtract");
                
                sesRelationExtend.addSubtractSeS(subses);
            }
        }
    }
    public static void parseForMultiplicity(String contents) {
        Pattern p = Pattern.compile("!");
        String[] sentences = p.split(contents);

        Hashtable<Object,Object> f = null;

        for (int i = 0; i < sentences.length; i++) {
            f = restructureInfo.parse(sentences[i]);
            if (f == null) {
                continue;
            }
            if(f.get("multiplicity")!= null && f.get("entity")!=null){
            	String specToExpand = (String) f.get("specToExpand");
                String mult = (String) f.get("multiplicity");
                String entity = (String) f.get("entity");
                mult = mult.substring(1, mult.length() - 1).trim();
                sesRelationExtend.addMultNumtoEnt(entity+"-"+specToExpand, new Integer(mult));
                
            }else
           if (f.get("multiplicity") != null) {
                String specToExpand = (String) f.get("specToExpand");
                String mult = (String) f.get("multiplicity");
                String node = (String) f.get("node");
                mult = mult.substring(1, mult.length() - 1).trim();
                int ind = mult.indexOf(",");
                int[] multint = new int[]{3, 3};
                if (ind == -1) {
                    multint = new int[]{Integer.parseInt(mult)};
                } else {
                    String first = mult.substring(0, ind).trim();
                    String second = mult.substring(ind + 1, mult.length()).trim();
                    multint[0] = Integer.parseInt(first);
                    ind = second.indexOf(",");
                    if (ind == -1) {
                        multint[1] = Integer.parseInt(second);
                    } else {
                        multint = new int[]{3, 3, 3};//do later
                    }
                }
                sesRelationExtend.addSpecAndMultiplicity(node + "-" + specToExpand + "Spec", multint);
            }
        }
    }
    public static Hashtable<Object,Object> parseSESFileName(String sentance) {
        sesParse par = new sesParse();
        String[] groups = par.getParts(sentance);
        if (checkSESFileName(groups) != null) {
            return checkSESFileName(groups);
        }

        return null;
    }
    public static Hashtable<Object,Object> checkSESFileName(String[] groups) {
    	Hashtable<Object,Object> f = new Hashtable<Object,Object>();
        if (groups.length >= 3 && groups[0].toLowerCase().equals("pruned")) {
            f.put("Pruned", groups[0]);
            f.put("SESFileName", groups[2]);
           
            return f;
        }
        return null;
    }
    public static String parseSESFileNameReturn(String contents){
    	Pattern p = Pattern.compile("!");
        String[] sentences = p.split(contents);

        Hashtable<Object,Object> f = null;

        for (int i = 0; i < sentences.length; i++) {
            f = restructureInfo.parseSESFileName(sentences[i]);
            if (f == null) {
                continue;
            }
           if (f.get("Pruned") != null) {
                String sesFileName = (String) f.get("SESFileName");
                return sesFileName;
            } 
        }
        return "";
    }
}
