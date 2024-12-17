package com.ms4systems.devs.log;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class SimulationLogFormatter extends Formatter {

	private final DateFormat format = new SimpleDateFormat("h:mm:ss.S");
	private static final String lineSep = System.getProperty("line.separator");
	private static boolean suppressTime = false;

	@Override
	public String format(LogRecord record) {
		String loggerName = record.getLoggerName();
		if(loggerName == null) {
			loggerName = SimulationLogger.DEVS_LOGGER_PREFIX;
		}

		try {
			loggerName = URLDecoder.decode(loggerName, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		final SimulationLogRecord simRecord = (SimulationLogRecord) record;

		StringBuilder output = new StringBuilder();
		
		if (!isSuppressTime()) 
			output.append("[")
			.append(Double.toString(simRecord.getEventTime()))
			.append(", ")
			.append(format.format(new Date(record.getMillis())))
			.append("] ");
		output.append(loggerName).append(": ")
		.append(record.getMessage()).append(' ')
		.append(lineSep);
		return output.toString();		
	}

	public static boolean isSuppressTime() {
		return suppressTime;
	}

	public static void setSuppressTime(boolean suppressTime) {
		SimulationLogFormatter.suppressTime = suppressTime;
	}
}
