/*
 * Copyright 2002-2001, Wade Wassenberg  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package equip.ect.components.x10.javaX10project.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Properties;

/**
 * LogHandler provides a simple, standard api for handling logging of application events. The
 * properties to configure this object are given by the x10/util/LogHandler.ini file. By default,
 * this file should contain:<br>
 * <br>
 * 
 * <code><blockquote>
 * # Set LogLevel from 0 to 5:
 * <br>#   0: No Logging
 * <br>#   1: Exception Logging
 * <br>#   2: Transmission Logging
 * <br>#   3: Currently Unused
 * <br>#   4: Currently Unused
 * <br>#   5: Currently Unused
 * <br>#
 * <br># LogLevel is additive.
 * <br>LogLevel=2
 * <BR>
 * <br># LogFile specifies a path and filename to use for logging purposes.
 * <br># By not specifying a LogFile, the LogHandler will default to stdout.
 * <br>LogFile=</blockquote></code>
 * 
 * @author Wade Wassenberg
 * 
 * @version 1.0
 */

public class LogHandler implements Serializable
{

	private static LogHandler logger;

	/**
	 * getLogHandler obtains the system's singleton LogHandler that is shared by all systems.
	 * 
	 * @return LogHandler the system's log handler
	 * 
	 */

	public static LogHandler getLogHandler()
	{
		if (logger == null)
		{
			logger = new LogHandler();
		}
		return (logger);
	}

	/**
	 * logException outputs the stack trace of the pecified exception to the log.
	 * 
	 * @param exception
	 *            - the exception to log the stack trace of.
	 * @param minimumLevel
	 *            - the minimum log level that must be set for this exception to actually be logged.
	 * 
	 */

	public static void logException(final Exception exception, final int minimumLevel)
	{
		final LogHandler handler = getLogHandler();
		handler.log(exception, minimumLevel);
	}

	/**
	 * logMessage outputs the specified message to the log.
	 * 
	 * @param message
	 *            - the message to log
	 * @param minimumLevel
	 *            - the minimum log level that must be set for this message to actually be logged.
	 * 
	 */

	public static void logMessage(final String message, final int minimumLevel)
	{
		final LogHandler handler = getLogHandler();
		handler.log(message, minimumLevel);
	}

	private int logLevel;

	private PrintStream out;

	/**
	 * LogHandler constructs the singleton LogHandler based on the properties given by the
	 * x10/util/LogHandler.ini file.
	 * 
	 */

	private LogHandler()
	{
		final ClassLoader loader = getClass().getClassLoader();
		final Properties properties = new Properties();
		try
		{
			final InputStream is = loader.getResourceAsStream("x10/util/LogHandler.ini");
			if (is != null)
			{
				properties.load(is);
			}
			else
			{
				System.out.println("Could Not Find LogHandler.ini.  Using Defaults.");
			}
		}
		catch (final IOException ioe)
		{
		}
		logLevel = 1;
		try
		{
			logLevel = Integer.parseInt(properties.getProperty("LogLevel", "1"));
		}
		catch (final NumberFormatException nfe)
		{
		}
		final String logFile = properties.getProperty("LogFile");
		if ((logFile == null) || (logFile.trim().length() == 0))
		{
			out = System.out;
		}
		else
		{
			try
			{
				out = new PrintStream(new FileOutputStream(logFile));
			}
			catch (final IOException ioe)
			{
				System.out.println("Error opening \"" + logFile + "\" for writing.  Using default instead.");
				out = System.out;
			}
		}
	}

	/**
	 * log outputs the stack trace of the pecified exception to the log.
	 * 
	 * @param exception
	 *            - the exception to log the stack trace of.
	 * @param minimumLevel
	 *            - the minimum log level that must be set for this exception to actually be logged.
	 * 
	 */

	public void log(final Exception exception, final int minimumLevel)
	{
		if (logLevel >= minimumLevel)
		{
			exception.printStackTrace(out);
			out.flush();
		}
	}

	/**
	 * log outputs the specified message to the log.
	 * 
	 * @param message
	 *            - the message to log
	 * @param minimumLevel
	 *            - the minimum log level that must be set for this message to actually be logged.
	 * 
	 */

	public void log(final String message, final int minimumLevel)
	{
		if (logLevel >= minimumLevel)
		{
			out.println(message);
			out.flush();
		}
	}
}