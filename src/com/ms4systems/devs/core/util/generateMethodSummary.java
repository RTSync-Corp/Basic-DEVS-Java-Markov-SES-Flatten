package com.ms4systems.devs.core.util;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author bernie
 */


/**
 *
 * @author bernie
 */

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;



public class generateMethodSummary {
   
    public static HashSet<Object> findMethods(String contents) {
        HashSet<Object> es = new HashSet<Object>();
        Pattern p = Pattern.compile("public");
        String groups[] = p.split(contents);
        for (int i = 0; i < groups.length; i++) {
            int ind = groups[i].indexOf("(");
            if (ind > -1) {
                groups[i] = groups[i].substring(0, ind);
                ind = groups[i].lastIndexOf(" ");
                String methodNm = groups[i].substring(ind++);
                methodNm = methodNm.trim();
                if (methodNm.length() > 1 && !methodNm.contains(";") && !methodNm.contains("@") && !methodNm.contains(".") && !methodNm.contains("]") && !methodNm.contains("[")) {
                    es.add(methodNm);
                }
            }
        }
        return es;
    }

    public static void writeTraverseFolderHierarchy(String folder) {                    
        sesRelation ses = new sesRelation();
        ses.setRoot("MethodsSummary");
        ses.addAspectToEntity("SummaryDec", "MethodsSummary");
        traverseFolderHierarchy(new File(folder), ses, "SummaryDec");

        ses.writeSesDoc(".\\MethodsSummarySeS.xml");
    }

    public static Hashtable<Object,Integer> nameBag = new Hashtable<Object,Integer>();

    public static String nameGen(String nm) {
    	if(nameBag.containsKey(nm)){
    		Integer val = nameBag.get(nm);
    		nameBag.put(nm, new Integer(val.intValue()+1));
    	}else{
    		nameBag.put(nm, new Integer(1));
    	}
        
        if (nameBag.get(nm).intValue() > 1) {
            return nm + nameBag.get(nm).intValue();
        } else {
            return nm;
        }

    }

    public static void traverseFolderHierarchy(File folder, sesRelation ses, String aspect) {
        File[] folders = folder.listFiles();
        for (int i = 0; i < folders.length; i++) {
            File fi = folders[i];
            if (fi.isDirectory()) {
                String fname = fi.getName();
                fname = nameGen(fname);
                ses.addEntityToAspect(fname, aspect);
                if (fi.isDirectory()) {
                    File[] dirs = fi.listFiles();
                    ses.addAspectToEntity(fname + "Dec", fname);
                    for (int k = 0; k < dirs.length; k++) {
                        File dirk = dirs[k];
                        String dname = dirk.getName();
                        dname = nameGen(dname);
                        if (dirk.isFile()) {
                            if (dirk.getName().endsWith("java")) {
                                ses.addEntityToAspect(dname, fname + "Dec");
                                ses.addVarToEntity("path", dname);
                                ses.addRangeToVar(dirk.getPath(), dname + ".path");
                                ses.addAspectToEntity(dname + "Dec", dname);
                                String content = fileHandler.readFromFile(dirk);
                                HashSet<Object> es = findMethods(content);
                                Iterator it = es.iterator();
                                while (it.hasNext()) {
                                    String s = (String) it.next();
                                    ses.addEntityToAspect(s, dname + "Dec");
                                }
                            }
                        } else {//is directory
                            ses.addEntityToAspect(dname, fname + "Dec");
                            ses.addAspectToEntity(dname + "Dec", dirk.getName());
                            traverseFolderHierarchy(dirk, ses, dname + "Dec");
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws ClassNotFoundException {
    }
}