/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ms4systems.devs.core.util;

import java.io.File;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
/**
 *
 * @author bernie
 */
public class Mappings {

    public static String folder = System.getProperty("user.dir") + File.separator;

    //from sesRelationForCascade
    public static sesRelation toSesRelationFromTxtFile(String folder, String sestxtfile) {
        String ses = fileHandler.readFromFile(folder + sestxtfile);
        return toSesRelationFromTxt(ses);
    }

    public static sesRelation toSesRelationFromTxt(String sestxt) {
        HashSet<Object> getMaxSes = natLangToSes.getMaxSesFromContents(sestxt);
        Iterator<Object> it = getMaxSes.iterator();
        return (sesRelation) it.next();
    }

    public static String toStringFromSesRelation(sesRelation ses) {
        return natLangToSes.backToNatLang(ses);
    }

    public static void toTxtFileFromSesRelation(sesRelation ses, String folder) {
        ses.backToNatLang(folder);
    }

    public static void toXmlFileFromSesRelation(sesRelation ses, String folder) {
        String xmlfile = folder + File.separator + ses.getRootEntityName() + "SeS.xml";
        ses.writeSesDoc(xmlfile);
    }
   

    public static String[] getPrecursors(String comp, String parent, Set coupling) {
        HashSet<Object> res = new HashSet<Object>();
        for (Object fn : coupling) {
            Hashtable<Object,Object> f = (Hashtable<Object,Object>) fn;
            if (f.get("destination").equals(comp)
                    && !f.get("source").equals(parent)) {
                res.add(f.get("source"));
            }
        }
        return stringOps.toStringArray(res);
    }


    public static void SESToGenericPES(sesRelation ses) {
        ses.toDOM();//creates SESOps.sesDoc
        String rootElemName = SESOps.sesRoot.getAttribute("name");
        generatePruningsWInherAuto.createPesDoc(rootElemName);
        generatePruningsWInherAuto.genericPESEntity(pruneOps.pruneRoot, SESOps.sesRoot, pruneOps.pruneRoot.getNodeName());
        generatePruningsWInherAuto.doInherit();
    }


///////////////////////////////////////////////////////////
    public static void main(String args[]) {
        sesRelation ses = toSesRelationFromTxtFile(folder, "CascadeSes.txt");
        toXmlFileFromSesRelation(ses, folder);
    }
}
    
