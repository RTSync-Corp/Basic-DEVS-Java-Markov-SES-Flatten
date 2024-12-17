package com.ms4systems.devs.log;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import com.ms4systems.devs.events.SimulationEvent;
import com.ms4systems.devs.events.SimulationEventListener;
import com.ms4systems.devs.events.SimulationEventType;

public class SimulationLogger extends SimulationEventListener {
	private static final long serialVersionUID = 1L;
	public static final String DEVS_LOGGER_PREFIX = "devs"; 
	static private Logger devsLogger = null;
	
	//cseo 1/29/2021
	private boolean disableLogging= false;
	
	static class SysOutHandler extends StreamHandler {
		// Private method to configure a ConsoleHandler from LogManager
	    // properties and/or default values as specified in the class
	    // javadoc.
	    private void configure() {

		setLevel(Level.INFO);
		setFilter(null);
		setFormatter(new SimpleFormatter());
		try {
		    setEncoding(null);
		} catch (Exception ex) {
		 
		}
	    }

	    /**
	     * Create a <tt>ConsoleHandler</tt> for <tt>System.err</tt>.
	     * <p>
	     * The <tt>ConsoleHandler</tt> is configured based on
	     * <tt>LogManager</tt> properties (or their default values).
	     * 
	     */
	    public SysOutHandler() {
		configure();
		setOutputStream(System.out);
	    }

	    /**
	     * Publish a <tt>LogRecord</tt>.
	     * <p>
	     * The logging request was made initially to a <tt>Logger</tt> object,
	     * which initialized the <tt>LogRecord</tt> and forwarded it here.
	     * <p>
	     * @param  record  description of the log event. A null record is
	     *                 silently ignored and is not published
	     */
	    public void publish(LogRecord record) {
		super.publish(record);	
		flush();
	    }

	    /**
	     * Override <tt>StreamHandler.close</tt> to do a flush but not
	     * to close the output stream.  That is, we do <b>not</b>
	     * close <tt>System.err</tt>.
	     */
	    public void close() {
		flush();
	    }
	}
	
	public Logger getDevsLogger() {
		if (devsLogger==null)
			devsLogger=createDevsLogger();
		return devsLogger;
	}

	protected static Logger createDevsLogger() {
		final Logger logger = Logger.getLogger(DEVS_LOGGER_PREFIX);
		logger.setUseParentHandlers(false);
		final SysOutHandler handler = new SysOutHandler(); 
		handler.setFormatter(createDevsFormatter());
		logger.addHandler(handler);
		return logger;
	}
	
	protected static Formatter createDevsFormatter() {
		return new SimulationLogFormatter();
	}

	public SimulationLogger() {
		super(SimulationEventType.values()); // Log all events
	}

	@Override
	public void eventOccurred(SimulationEvent event) {
		//cseo (1/29/2021)
		if (disableLogging) {
			return;
		}
		getDevsLogger().log(createLogRecord(event));
		for (Handler h : getDevsLogger().getHandlers()) {
			h.flush();
		}
	}

	protected LogRecord createLogRecord(SimulationEvent event) {
		return new SimulationLogRecord(event);
	}
	
	//cseo (1/29/2021)
	public void setDisableLogging(boolean disableLogging) {
		this.disableLogging = disableLogging;
	}
}
