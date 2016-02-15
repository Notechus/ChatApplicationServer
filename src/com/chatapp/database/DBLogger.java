package com.chatapp.database;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Logging system. Singleton
 * 
 * @author notechus
 */
public class DBLogger
{

	/** Logger */
	private static final Logger logger = Logger.getLogger("chatapp.database");
	/** File handler */
	private FileHandler fh;

	/**
	 * Default constructor, will configure logger
	 */
	private DBLogger()
	{
		try
		{
			// This block configure the logger with handler and formatter
			fh = new FileHandler("MainLog.log", true);
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
			logger.setUseParentHandlers(false);
			logger.info("Started application");
		} catch (SecurityException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static class DBHolder
	{

		private static final DBLogger instance = new DBLogger();
	}

	public static DBLogger getInstance()
	{
		return DBHolder.instance;
	}

	public Logger getLog()
	{
		return logger;
	}
}
