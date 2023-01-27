package com.sapiens.ssi.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

public class SSILogger {
	public static Logger log;
	public static LoggerContext context;


	//set the log object
	public static void setLog(Logger logObj) {
		log = logObj;
		context = (LoggerContext) LogManager.getContext(false);
	}
}
