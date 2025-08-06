/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ms4systems.devs.core.util;



import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;


import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.model.CoupledModel;
import com.ms4systems.devs.core.model.impl.*;

/**
 *
 * @author Bernie
 */
public class BasicWork extends CoupledModelImpl {


    
    public static String workspace;
    public static String projectNm;
    public static String folderJava;
    public String packageNm;
    public String folderTxt;
    
    public String folderSes;
    public String folderPes;
    public String folderXml;
    public String folderTest;
    
    public String folderAnimation;
    
    public String folderDnl;

    public static String prunedDoc;
    
    protected static String entityToPrune = "";
    
    public String CLASSES_DIR;

    public static boolean hasError;
    
    public HashSet<Object> findAllSpecs(sesRelation ses, String spectoexpand) {
    	HashSet<Object> specsFound = new HashSet<Object>();
    	Set<Object> key = ses.entityHasAspect.keySet();
    	if(key == null){
    		System.out.println("key is null.");
    		return new HashSet<Object>();
    	}
        Enumeration<Object> en = ses.entityHasAspect.keys();
        while(en.hasMoreElements()){
        	Object grandParent = (String)en.nextElement();
        	HashSet<Object> aspects = ses.entityHasAspect.get(grandParent);
            Iterator<Object> im = aspects.iterator();
            while (im.hasNext()) {
                String aspNm = (String) im.next();
                HashSet<Object> ents = ses.aspectHasEntity.get(aspNm);
                for (Object oe : ents) {
                    String entity = (String) oe;
                    String gchd = ses.getSingleOfMult(entity);
                    if (gchd.equals("")) {
                        continue;
                    }

                    HashSet<Object> multiAspects = ses.entityHasMultiAspect.get(entity);
                    Iterator<Object> ia = multiAspects.iterator();
                    if (ia.hasNext()) {
                        String maspNm = (String) ia.next();
                        String chd = (String) new LinkedList<Object>(ses.multiAspectHasEntity.get(maspNm)).getFirst();
                        HashSet<Object> specs = ses.entityHasSpec.get(chd);
                        for (Object os : specs) {
                            if (os.toString().endsWith(spectoexpand + "Spec")) {
                                specsFound.add(os);
                            }
                        }
                    }
                }
            }
        }

        return specsFound;
    }


    public sesRelation restructureAndSave(String sesFileNm, String folderTxt, String specNm) {
        sesRelation ses =
                sesRelation.createNAddImpliedInheritance(folderTxt, sesFileNm);

	    for(String subSesFile : sesRelationExtend.subSeSList){
	  	  sesRelation subSeS = sesRelation.createNAddImpliedInheritance(folderTxt, subSesFile);
	  	  ses.mergeSeS(subSeS);
	    }

	    
        return restructureAndSave(ses, sesFileNm, folderTxt, specNm);
    }
    // With a ses content (cseo 6/19/2018)
    public sesRelation restructureAndSaveWithContent(String sesContent, String folderTxt, String specNm) {
        sesRelation ses =
                sesRelation.createNAddImpliedInheritanceWContent(folderTxt, sesContent);

	    for(String subSesFile : sesRelationExtend.subSeSList){
	  	  sesRelation subSeS = sesRelation.createNAddImpliedInheritance(folderTxt, subSesFile);
	  	  ses.mergeSeS(subSeS);
	    }

	    
        return restructureAndSave(ses, sesContent, folderTxt, specNm);
    }
    
    public sesRelation restructureAndSave(sesRelation ses, String sesFileNm, String folderTxt, String specNm) {
    	HashSet<Object> multiAspects = ses.getEnsembleSet("multiAspectNames");
        if (multiAspects.isEmpty()) {
            return ses;
        }

        HashSet<Object> specsFound = findAllSpecs(ses, specNm);
        for (Object sp : specsFound) {
            ses.restructureAllMultiAspectsOverSpec(sp.toString());
        }

        ses.printTree();
        System.out.println(natLangToSes.backToNatLang(ses));

        ses.writeSesDoc(folderXml + ses.getRootEntityName() + "SeS.xml", workspace);
        return ses;
    }

    public sesRelation restructureAndSaveOrig(String sesFileNm, String folderTxt, String spec) {
        sesRelation ses = Mappings.toSesRelationFromTxtFile(folderTxt, sesFileNm);
        ses.restructureAllMultiAspectsOverSpec(spec);
        ses.printTree();
        HashSet<Object> multiAspects = ses.getEnsembleSet("multiAspectNames");
        if (multiAspects.isEmpty()) {
            return ses;
        }
        System.out.println(natLangToSes.backToNatLang(ses));


        return ses;
    }

    public sesRelation restructureAndSave(String sesFileNm, String specNm) {
        return restructureAndSave(sesFileNm, folderTxt, specNm);
    }

    public sesRelation restructureAndSave(String sesFileNm) {
        return restructureAndSave(sesFileNm, folderTxt, "");
    }

    public void addInheritanceSelection(String classnm) {
        PESToDEVSOnTheFly.inheritanceSelections.add(classnm);


    }

    public void addInheritanceSelections(String[] classnms) {
        for (String classnm : classnms) {
            addInheritanceSelection(classnm);


        }
    }

    public void addEntityToShow(String classnm) {
        PESToDEVSOnTheFly.entitiesToShow.add(classnm);
    }

    public void addEntitiesToShow(String[] classnms) {
        for (String classnm : classnms) {
            addEntityToShow(classnm);


        }
    }

    public void addAllEntitiesToShow(sesRelation ses) {
        if (ses != null) {
        	Hashtable<Object,HashSet<Object>> entityHasAspect = ses.entityHasAspect();
            Iterator<Object> it = entityHasAspect.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                addEntityToShow(key);
            }
        }
    }

    public void addAllInheritanceSelections(sesRelation ses) {
        if (ses != null) {
        	Hashtable<Object,HashSet<Object>> entityHasSpec = ses.entityHasSpec();
            Iterator<Object> it = entityHasSpec.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                addInheritanceSelection(key);
            }
        }
    }

    public void changeDigName(String rootNm) {
        PESToDEVSOnTheFly.changeDigName(this, rootNm);
    }



    public String constructAspect(sesRelation ses, String ent, String asp, int i, CoupledModelImpl dig) {
        String s = "";

        return s;
    }

    public void getLevel(sesRelation ses, String ent, int level, Hashtable<Object,HashSet<Object>> r) {
        HashSet<Object> asps = ses.entityHasAspect().get(ent);
        if (!asps.isEmpty()) {
            String asp = asps.iterator().next().toString();
            HashSet<Object> ents = ses.aspectHasEntity().get(asp);
            Iterator<Object> it = ents.iterator();
            while (it.hasNext()) {
                String chd = (String) it.next();
                //r.put(new Integer(level + 1), chd);
                r.get(new Integer(level + 1)).add(chd);
                getLevel(ses, chd, level + 1, r);
            }
        }
    }

    public String orderEntities(sesRelation ses, CoupledModelImpl dig) {
        String s = "";

        return s;
    }
    static Hashtable<Object,Object> assignedNames = new Hashtable<Object,Object>();

    public static void assignNames(CoupledModelImpl dig) {

    }

    public static AtomicModel findComponentAnyLevelWithName(CoupledModelImpl dig, String compNm) {
        if (dig.getName().equals(compNm)) {
            return dig;
        }

        return null;
    }

    public static AtomicModel findAtomicComponentAnyLevelWithName(CoupledModel dig, String compNm) {

        return null;
    }

    public void writeDoc() {
        assignNames(this);
        String folderDoc = workspace + projectNm +File.separator+ "Models"+File.separator+"doc"+File.separator;
    }

}


