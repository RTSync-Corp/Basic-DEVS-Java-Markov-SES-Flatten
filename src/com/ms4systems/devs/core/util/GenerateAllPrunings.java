package com.ms4systems.devs.core.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;


public class GenerateAllPrunings {


	static double probWin = 1;
	static Random rand = new Random();
	static String[] A = new String[] {"a1","a2"};
	static String[] B = new String[] {"b1","b2"};
	static String[] C = new String[] {"c1","c2","c3"};
	
    static ArrayList<String> specNames = new ArrayList<String>();
	static ArrayList<String[]> choicesets = new ArrayList<String[]>();

	public static HashSet<String>  generatePrunings(String entity,ArrayList<String> specNames,ArrayList<String[]> choicesets ) {
		HashSet<String> leaves = new HashSet<String>();
		String root ="";
		leaves.add(root);
		for (int i = 0; i <choicesets.size(); i++) {
			String[] choiceset  = choicesets.get(i);
			HashSet<String> newleaves = new HashSet<String>();			
			for (String p : leaves) {
				for (String choice: choiceset) {
					String pc = p+choice;
					String sel = "\nSelect "+choice+" from "+ specNames.get(i)+" for "+entity+"!";
			    	newleaves.add(p+sel);
				}
			}
			leaves = newleaves;
		}
		System.out.println(leaves.size());
		for (String s:leaves)System.out.println(s);
		return leaves;
	}	
	
	public static HashSet<String> generateAllPrunings(sesRelationExtend rses) {
		HashSet pruningByComponent = new HashSet();
	    ArrayList<String[]> choicesets = new ArrayList<String[]>();
	    ArrayList<String> specNames = new ArrayList<String>();
	    ArrayList<String> entNames = new ArrayList<String>();
	    
	    Hashtable specsToSets = new Hashtable();	   
		Hashtable sh = rses.specHasEntity();
		 Set skeys = sh.keySet();
		for (Object sk:skeys) {
			HashSet shs = (HashSet)sh.get(sk);
			String spname = sk.toString();
			specNames.add(spname);
			 String[] specs = new String[shs.size()];
			Iterator sit = shs.iterator();
			int scount = 0;
			while (sit.hasNext()) {
				specs[scount] = (String)sit.next();
				scount++;
			}
			specsToSets.put(spname, specs);
		}
   
		Hashtable eh = rses.entityHasSpec();
		Set ekeys = eh.keySet();
		for (Object ek:ekeys) {
			String ename = ek.toString();
			choicesets = new ArrayList<String[]>();
			entNames.add(ename);
			HashSet specsForEname = (HashSet)eh.get(ek);
			ArrayList<String> specNamesForEname = new ArrayList<String> ();
			for (Object spec:specsForEname) {
				specNamesForEname.add(spec.toString());
				String[]  specents =  (String[] )specsToSets.get(spec);
				choicesets.add(specents);
			}
		HashSet pruningForComponent = generatePrunings(ename,specNamesForEname,choicesets);
		pruningByComponent.add(pruningForComponent);
		}
		HashSet<String> prunings = new HashSet<String>();

		String root ="";
		prunings.add(root);	
		for (Object o: pruningByComponent) {
			HashSet<String> compPrunings = (HashSet<String>)o;
			 HashSet<String> newPrunings = new HashSet<String>();
			     for (String p : prunings) {
		         	for (String s:compPrunings) {
			         	String pc = p+s;
		    	        newPrunings.add(pc);			
		    	        }
 			}
			prunings = newPrunings;
		}
		return prunings;
		}
	public static void main(String[] args) {
	    specNames.add("A");specNames.add("B");specNames.add("C");
		choicesets.add(A);choicesets.add(B);choicesets.add(C);
		HashSet<String> leaves = generatePrunings("test",specNames,choicesets);
		leaves = leaves;
	}	
}
