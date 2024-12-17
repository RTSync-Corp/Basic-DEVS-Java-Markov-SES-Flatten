package com.ms4systems.devs.util;

import java.io.IOException;
import java.io.PrintStream;

public class SimulationAwarePrintStream extends PrintStream {
	public enum Type { OUT, ERR }

	private final Type type;
	private final PrintStream originalStream;
	
	public SimulationAwarePrintStream(Type type) {
		super(type==Type.OUT ? System.out : System.err);
		this.type = type;
		originalStream = type==Type.OUT ? System.out : System.err;
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		if (contextClassLoader instanceof SimulationClassLoader) {
			@SuppressWarnings("resource")
			final PrintStream simStream = type==Type.OUT? ((SimulationClassLoader) contextClassLoader).getOut() :
				((SimulationClassLoader) contextClassLoader).getErr();
			if (simStream.equals(this)) originalStream.write(b);
			else simStream.write(b);
		}
		else
			super.write(b);
	}
	
	@Override
	public void write(byte[] buf, int off, int len) {
		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		if (contextClassLoader instanceof SimulationClassLoader) {
			@SuppressWarnings("resource")
			final PrintStream simStream = type==Type.OUT? ((SimulationClassLoader) contextClassLoader).getOut() :
				((SimulationClassLoader) contextClassLoader).getErr();
			if (simStream.equals(this)) originalStream.write(buf,off,len);
			else simStream.write(buf,off,len);
		}
		else
			super.write(buf,off,len);
		
	}
	
	@Override
	public void write(int b) {
		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		if (contextClassLoader instanceof SimulationClassLoader) {
			@SuppressWarnings("resource")
			final PrintStream simStream = type==Type.OUT? ((SimulationClassLoader) contextClassLoader).getOut() :
				((SimulationClassLoader) contextClassLoader).getErr();
			if (simStream.equals(this)) originalStream.write(b);
			else simStream.write(b);
		}
		else
			super.write(b);
		
	}
	
	@Override
	public void close() { }

}
