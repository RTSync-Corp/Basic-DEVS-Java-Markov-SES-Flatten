/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ms4systems.devs.core.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;
import java.nio.file.*;


import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.core.model.impl.AtomicModelImpl;
import com.ms4systems.devs.core.model.impl.CoupledModelImpl;

import java.io.Serializable;


//import Models.java.*;
/**
 *
 * @author bernie
 */
public class WriteClassFiles extends PESToDEVS {

	public static Hashtable<String,Vector<AtomicModelImpl>> hashAtomicPorts = new Hashtable<String,Vector<AtomicModelImpl>>();
	
	public static void mapDEVSToFileWExceptions(AtomicModel m, String packageNm,
            String folder, HashSet<Object> excepts) {
        String digclassNm = m.getName();
        if (digclassNm.toLowerCase().contains("notpresent")) {
            return;
        }
        if (excepts.contains(digclassNm)) {
            return;
        }
        if (PESToDEVSOnTheFly.inheritanceSelections.contains(digclassNm)) {
            return;
        }
        
        // check if a java file exists or not
        String javafolder = folder;
        System.out.println("javafolder : "+javafolder);
        String javaNm = javafolder+digclassNm+".java";
        File javafile = new File(javaNm.trim());
        
        //check if a dnl file exists or not
        String dnlfolder = folder.replace("java", "dnl");
        String fileNm = dnlfolder + digclassNm + ".dnl";
        File file = new File(fileNm);

        if (file.exists()) {
            file.setWritable(true);
        }
        if (file.exists() && m instanceof AtomicModelImpl && !(m instanceof CoupledModelImpl) && !digclassNm.contains("_")) {
            return;
        }
        
        Path path = Paths.get(javaNm);
        System.out.println("Javafile : "+javaNm+" : exist? "+javafile.getAbsoluteFile().exists());
        System.out.println("Javafile : "+javaNm+" : Files.exists(path)? "+Files.exists(path));
        // If a java file exists, don't write the java file (4/7/2023)
        if (javafile.getAbsoluteFile().exists() && m instanceof AtomicModelImpl && !(m instanceof CoupledModelImpl) && !digclassNm.contains("_")) {
            return;
        }
        
        String s = "";
        if (m instanceof CoupledModelImpl) {

            s += mapCoupledToFileWExceptions((CoupledModelImpl) m, packageNm, folder, excepts);
        } else {

            s += mapAtomicToFileWExceptions((AtomicModelImpl) m, packageNm, folder, excepts);
        }
        try {
            OutputStream stream = new FileOutputStream(folder + digclassNm + ".java");
            stream.write(s.getBytes());
            stream.close();
            System.out.println("File was rewritten: "
                    + folder + digclassNm + ".java");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	public static void mapDEVSToFileWExceptions(AtomicModel m, String packageNm,
            String folder, HashSet<Object> excepts, boolean isAnimation) {
        String digclassNm = m.getName();
        if (digclassNm.toLowerCase().contains("notpresent")) {
            return;
        }
        if (excepts.contains(digclassNm)) {
            return;
        }
        if (PESToDEVSOnTheFly.inheritanceSelections.contains(digclassNm)) {
            return;
        }
        String dnlfolder = folder.replace("java", "dnl");
        String fileNm = dnlfolder + digclassNm + ".dnl";
        File file = new File(fileNm);

        if (file.exists()) {
            file.setWritable(true);
        }
        if (file.exists() && m instanceof AtomicModelImpl && !(m instanceof CoupledModelImpl) && !digclassNm.contains("_")) {
            return;
        }
        String s = "";
        if (m instanceof CoupledModelImpl) {

            s += mapCoupledToFileWExceptions((CoupledModelImpl) m, packageNm, folder, excepts);
        } else {

            s += mapAtomicToFileWExceptions((AtomicModelImpl) m, packageNm, folder, excepts);
        }
        try {
            OutputStream stream = new FileOutputStream(folder + digclassNm + ".java");
            stream.write(s.getBytes());
            stream.close();
            System.out.println("File was rewritten: "
                    + folder + digclassNm + ".java");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String mapCoupledToFileWExceptions(CoupledModelImpl m, String packageNm,
            String folder, HashSet<Object> noWrite) {
        String digclassNm = m.getName();

        String parentNameFromInput = getparentNameIfInherited(digclassNm);
        Class cl = m.getClass();
        String myClassNm = cl.getSimpleName();
        String superClassNm = "CoupledModelImpl";
        if (parentNameFromInput.equals(digclassNm)) {
            //not inherited
            if (m.getParent() != null
                    && !myClassNm.equals(digclassNm)) {
                superClassNm = myClassNm;
            }
        } else {//is inherited
            if (!myClassNm.equals("scalableDigraph")
                    && !myClassNm.equals(digclassNm)
                    && m.getChildren().size() == 0) {
                superClassNm = myClassNm;
                return writeClass(packageNm, digclassNm, superClassNm);
            }
        }
        if (packageNm.endsWith(".")) {
            packageNm = packageNm.substring(0, packageNm.length() - 1);
        }
        String st = "package " + packageNm + ";";

        st += "\nimport com.ms4systems.devs.core.model.impl.CoupledModelImpl;";
        st += "\nimport com.ms4systems.devs.core.message.Port;";
        st += "\nimport com.ms4systems.devs.core.simulation.Simulation;";
        st += "\nimport com.ms4systems.devs.helpers.impl.SimulationOptionsImpl;";
        st += "\nimport com.ms4systems.devs.simviewer.standalone.SimViewer;";
        st += "\nimport java.io.Serializable;";
        
        if(superClassNm.equals("CoupledModelImpl")){
        	st += "\nimport com.ms4systems.devs.extensions.StateVariableBased;";
        	st += "\nimport com.ms4systems.devs.core.model.AtomicModel;";
        	st += "\nimport java.util.ArrayList;";
        	st += "\nimport java.util.HashSet;";
        	st += "\nimport java.util.TreeSet;";        	
        	st += "\n";
        	st += "\npublic class " + digclassNm + " extends " + superClassNm + " implements StateVariableBased{ ";
            
        }else{
        	st += "\n";
        	st += "\npublic class " + digclassNm + " extends " + superClassNm + "{ ";
            
        }
        String s = "";
        s += "\n\tprivate static final long serialVersionUID = 1L;";
        s += "\n\tprotected SimulationOptionsImpl options = new SimulationOptionsImpl();";

        s += "\n\t" + m.writePorts();
        s += "\n\tpublic " + digclassNm + "(){";
        s += "\n\t\tthis(" + DomToXML.quote(digclassNm) + ");";
        s += "\n\t}";
        s += "\n\tpublic " + digclassNm + "(String nm) {";
        s += "\n\t\tsuper(nm);";
        if (m.getChildren().size() > 0) {
            s += "\n\t\tmake();";
        }
        s += "\n\t}";
        s += "\n\tpublic void make(){";
        s += "\n";
        ArrayList<AtomicModel> al = m.getChildren();
        for (AtomicModel comp : al) {
            String compinstNm = comp.getName();
            String classNm = comp.getName();
            if (compinstNm.toLowerCase().contains("notpresent")) {
                continue;
            }
            if (comp instanceof CoupledModelImpl) {

                s += "\n\t\t" + classNm + " " + compinstNm + " = new " + classNm + "();";
                s += "\n\t\taddChildModel(" + compinstNm + ");";
                mapDEVSToFileWExceptions((CoupledModelImpl) comp, packageNm, folder, noWrite);

            } else {
                s += "\n\t\t" + classNm + " " + compinstNm + " = new " + classNm + "();";
                s += "\n\t\taddChildModel(" + compinstNm + ");";
                mapDEVSToFileWExceptions((AtomicModel) comp, packageNm, folder, noWrite);
            }
        }



        s += m.writeCoupling();

        s += "\n";

        s += "\n\t}";
      
        //Add functioning StateVariableBased interface (1/17/13)
        try {
        	InputStream is = WriteClassFiles.class.getResourceAsStream("resources/CoupledModelImpl_StateBasedFunctions.txt");
        	ByteArrayOutputStream os = new ByteArrayOutputStream();
			is.close();
			os.close();
			
			s += os.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}         
        
        
        // Add a main function (5/14/12)
        s += "\n\tpublic static void main(String[] args){";
        s += "\n\t\tSimulationOptionsImpl options = new SimulationOptionsImpl(args, true);";
        s += "\n\t\t// Uncomment the following line to disable SimViewer for this model";
    	s += "\n\t\t// options.setDisableViewer(true);";

		s += "\n\t\t// Uncomment the following line to disable plotting for this model";
		s += "\n\t\t// options.setDisablePlotting(true);\n";
		s += "\n\t\t// Uncomment the following line to disable logging for this model";
		s += "\n\t\t// options.setDisableLogging(true);\n";
        s += "\n\t\t"+digclassNm+" model = new "+digclassNm+"();";
        s += "\n\t\tmodel.options = options;";
        s += "\n\t\tif(options.isDisableViewer()){ // Command Line output only";
        s += "\n\t\t\tSimulation sim = new com.ms4systems.devs.core.simulation.impl.SimulationImpl(\""+digclassNm+" Simulation\",model,options);";;
        s += "\n\t\t\tsim.startSimulation(0);";
        s += "\n\t\t\tsim.simulateIterations(Long.MAX_VALUE);";
        s += "\n\t\t}else { //Use SimViewer";
        s += "\n\t\t\tSimViewer viewer = new SimViewer();";
        s += "\n\t\t\tviewer.open(model,options);";
        s += "\n\t\t}";
        s += "\n\t}";
        s += "\n}";
        return st + s;
    }
    
    // This is for checking if digclassNm has an inheritance (9/28/2018 cseo) 
    public static String getparentNameIfInherited(String digclassNm) {
    	int index = digclassNm.lastIndexOf("_");
        if (index > -1) {
            return digclassNm.substring(index+1);
        }
        return digclassNm;
    }

    public static String mapAtomicToFileWExceptions(AtomicModelImpl m, String packageNm,
            String folder, HashSet<Object> noWrite) {
        String publicDeclarations = "";
        String digclassNm = m.getName();
        String parentNameFromInput = getparentNameIfInherited(digclassNm);
        Class cl = m.getClass();
        String myClassNm = cl.getSimpleName();
        String superClassNm = "AtomicModelImpl";
        if (parentNameFromInput.equals(digclassNm)) {
            //not inherited
            if ( 
                    !myClassNm.equals(digclassNm)) {
                superClassNm = myClassNm;
            }
        } else {//inherited

            if (!myClassNm.equals("scalableAtomic")
                    && !myClassNm.equals(digclassNm)) {
                superClassNm = myClassNm;
            }
        }

        if (packageNm.endsWith(".")) {
            packageNm = packageNm.substring(0, packageNm.length() - 1);
        }
        
        
        String st = "package " + packageNm + ";";
        st += "\n";
        st += "\nimport com.ms4systems.devs.core.model.impl.AtomicModelImpl;";
        st += "\nimport com.ms4systems.devs.core.message.Port;";
        st += "\nimport java.io.Serializable;";
        st += "\nimport com.ms4systems.devs.extensions.PhaseBased;";
        st += "\nimport com.ms4systems.devs.extensions.ProvidesTooltip;";
        st += "\nimport com.ms4systems.devs.extensions.StateVariableBased;";
        st += "\n";

        if(superClassNm.equals("AtomicModelImpl")){
	        st += "\npublic class " + digclassNm + " extends " + superClassNm + 
	        	" implements PhaseBased, " +
	        	"StateVariableBased, " +
	        	"ProvidesTooltip { ";
        }else {
        	st += "\npublic class " + digclassNm + " extends " + superClassNm +" { ";
        }
        String s = "";
        s += "\n\tprivate static final long serialVersionUID = 1L;";
        if (superClassNm.equals("AtomicModelImpl")) {
        	
        	if(hashAtomicPorts.containsKey(digclassNm)){// atomic models in different locations are integrated to show all ports
        		Vector<String> inputports = new Vector<String>();
        		Vector<String> outputports = new Vector<String>();
        		Vector<AtomicModelImpl> vAtomic = hashAtomicPorts.get(digclassNm);
        		vAtomic.add(m);
        		for(AtomicModelImpl atomic : vAtomic){
        			ArrayList<Port<? extends Serializable>> aList = atomic.getInputPorts();
        			for(Port<? extends Serializable> inport : aList){
        				if(!inputports.contains(inport.getName())) inputports.add(inport.getName());
        			}
        			ArrayList<Port<? extends Serializable>> aoutList = atomic.getOutputPorts();
        			for(Port<? extends Serializable> outport : aoutList){
        				if(!outputports.contains(outport.getName())) outputports.add(outport.getName());
        			}
        		}
        		
                for (String in : inputports) {
                    s += "\n\tpublic final Port<? extends Serializable> " + in
                            + "= addInputPort(" + DomToXML.quote(in)
                            + ",Serializable.class);";
                }
                
                for (String out : outputports) {
                    s += "\n\tpublic final Port<? extends Serializable> " + out
                            + "= addOutputPort(" + DomToXML.quote(out)
                            + ",Serializable.class);";
                }
        		
        	}else{
        	}
        }
        s += "\n\tpublic " + digclassNm + "(){";


        s += "\n\t\tthis(" + DomToXML.quote(digclassNm) + ");";
        s += "\n\t}";
        s += "\n\tpublic " + digclassNm + "(String nm) {";
        s += "\n\t\tsuper(nm);";
        s += "\n\t}";
           
        if(superClassNm.equals("AtomicModelImpl")){
	        s += "\n\tpublic String getTooltip() {";
	        s += "\n\t\treturn null;";
	        s += "\n\t}";
	        s += "\n\tpublic String[] getStateVariableNames() {";
	        s += "\n\t\treturn  new String[]{};";
	        s += "\n\t}";
	        s += "\n\tpublic Object[] getStateVariableValues() {";
	        s += "\n\t\treturn null;";
	        s += "\n\t}";
	        s += "\n\tpublic Class<?>[] getStateVariableTypes() {" +
	        		"\n\t\treturn new Class<?>[0];" +
	        		"\n\t}";
	        s += "\n\tpublic void setStateVariableValue(int index, Object value) {}";
	        s += "\n\tpublic String[] getPhaseNames() {";
	        s += "\n\t\treturn  new String[]{};";
	        s += "\n\t}";
	        s += "\n\tpublic String getPhase() {";
	        s += "\n\t\treturn null;";
	        s += "\n\t}";
	        s += "\n\tpublic boolean phaseIs(String phase) {";
	        s += "\n\t\treturn false;";
	        s += "\n\t}";
        }
        s += "\n}";
        
        // save atomic models
        if(!hashAtomicPorts.containsKey(digclassNm)){
        	
        	Vector<AtomicModelImpl> vModel = new Vector<AtomicModelImpl>();
        	vModel.add(m);
        	hashAtomicPorts.put(digclassNm, vModel);
        }
        
        return st + publicDeclarations + s;
    }

    public static String writeClass(String packageNm, String digclassNm,
            String superClassNm) {
        if (packageNm.endsWith(".")) {
            packageNm = packageNm.substring(0, packageNm.length() - 1);
        }
        String st = "package " + packageNm + ";";


        st += "\nimport com.ms4systems.devs.core.model.impl.AtomicModelImpl;";
        st += "\n";

        st += "\npublic class " + digclassNm + " extends " + superClassNm + "{ ";
        String s = "";
        s += "\n\tpublic " + digclassNm + "(){";
        s += "\n\t\tthis(" + DomToXML.quote(digclassNm) + ");";
        s += "\n\t}";
        s += "\n\tpublic " + digclassNm + "(String nm) {";
        s += "\n\t\tsuper(nm);";
        s += "\n\t}";
        s += "\n}";
        return st + s;
    }
}
