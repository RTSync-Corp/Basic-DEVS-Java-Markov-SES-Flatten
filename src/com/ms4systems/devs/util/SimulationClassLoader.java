package com.ms4systems.devs.util;

import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;


public class SimulationClassLoader extends URLClassLoader {
	
	private static PrintStream classOut = System.out;
	private static PrintStream classErr = System.err;
	private PrintStream out = null;
	private PrintStream err = null;
	
	public SimulationClassLoader(URL[] urls) {
		super(urls);
	}
	
	public SimulationClassLoader(URL[] urls, ClassLoader parent) {
		super(urls,parent);
	}
	
	public SimulationClassLoader(URL[] urls, ClassLoader parent,
			  URLStreamHandlerFactory factory) {
		super(urls,parent,factory);
	}
	
	public PrintStream getOut() {
		return out!=null? out : classOut;
	}

	public void setOut(PrintStream out) {
		this.out = out;
	}
	
	static public void setClassOut(PrintStream newOut) {
		classOut = newOut;
	}

	public PrintStream getErr() {
		return err!=null? err : classErr;
	}

	static public void setClassErr(PrintStream newErr) {
		classErr = newErr;
	}

	public void setErr(PrintStream err) {
		this.err = err;
	}
}
