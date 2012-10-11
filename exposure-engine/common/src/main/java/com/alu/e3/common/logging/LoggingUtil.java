/**
 * Copyright © 2012 Alcatel-Lucent.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * Licensed to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.alu.e3.common.logging;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.alu.e3.data.model.LogLevel;

/**
 * The LoggingUtil class provides static values and functions related to logging, 
 * for use in getting and setting log levels and modifying config files, etc.
 * 
 */

public class LoggingUtil {


	public static final String defaultKarafPath = System.getProperty("user.home") + "/apache-servicemix";
	public static final String defaultLogPath = defaultKarafPath + "/data/log/e3.log";
	public static final String defaultSMXLogPath = defaultKarafPath + "/data/log/servicemix.log";
	public static final String defaultConfigName = "org.ops4j.pax.logging.cfg"; 
	public static final String defaultConfigPath = defaultKarafPath + "/etc/" + defaultConfigName;

	public static final String e3Appender = "E3Appender";
	public static final String e3Prefix = "log4j.appender." + e3Appender;
	public static final String e3LoggerPrefix = "log4j.logger.com.alu.e3";
	public static final String smxAppender = "out";
	public static final String smxPrefix = "log4j.appender." + smxAppender;
	public static final String smxLoggerPrefix = "log4j.rootLogger";
	
	public static final String rollingFileSize = "20MB";
	public static final String maxRollingFileSize = String.valueOf(Long.MAX_VALUE);
	public static final String rollingLog4jConf = "# E3 appender start\n" + 
			"log4j.appender.E3Appender=org.apache.log4j.RollingFileAppender\n" + 
			"log4j.appender.E3Appender.layout=org.apache.log4j.PatternLayout\n" + 
			"log4j.appender.E3Appender.layout.ConversionPattern= %-5p %d %-20c [%L] - %m%n\n" + 
			"log4j.appender.E3Appender.file=${logPath}\n" + 
			"log4j.appender.E3Appender.append=true\n" + 
			"log4j.appender.E3Appender.maxFileSize=" + rollingFileSize + "\n" + 
			"log4j.appender.E3Appender.maxBackupIndex=100\n" + 
			"log4j.appender.E3Appender.threshold=${level}\n" + 
			"log4j.logger.com.alu.e3=${level}, E3Appender\n" + 
			"log4j.additivity.com.alu.e3=false\n" + 
			"# E3 appender end";
	public static final String minRollingDatePattern = "yyyy-MM-dd-HH-mm";
	public static final String hourRollingDatePattern = "yyyy-MM-dd-HH";
	public static final String maxRollingDatePattern = "yyyy-MM";
	public static final String dailyRollingDatePattern = hourRollingDatePattern;	// how often to rotate logs (if using dailyRolling)?
	public static final String dailyRollingLog4jConf = "# E3 appender start\n" + 
			"log4j.appender.E3Appender=org.apache.log4j.DailyRollingFileAppender\n" + 
			"log4j.appender.E3Appender.layout=org.apache.log4j.PatternLayout\n" + 
			"log4j.appender.E3Appender.layout.ConversionPattern= %-5p %d %-20c [%L] - %m%n\n" + 
			"log4j.appender.E3Appender.file=${logPath}\n" + 
			"log4j.appender.E3Appender.append=true\n" + 
			"log4j.appender.E3Appender.DatePattern='.'" + dailyRollingDatePattern + "\n" +
			"log4j.appender.E3Appender.threshold=${level}\n" + 
			"log4j.logger.com.alu.e3=${level}, E3Appender\n" + 
			"log4j.additivity.com.alu.e3=false\n" + 
			"# E3 appender end";
	public static final String defaultLog4jConf = dailyRollingLog4jConf;	// default to daily-rolling file appender

	public static final String pathPatternString = "([-\\.a-zA-Z0-9_~\\${}" + "\\" + File.separator + "]+)";
	public static final String datePatternPatternString = "([-_yMdHm]+)";
	public static final String generationPatternString = "(\\d+)";	// one or more digits
	public static final String rollingFileExtPatternString = "(\\.\\d+)";
	public static final String dailyRollingFileExtPatternString = "(\\.(\\d\\d\\d\\d[-_]\\d\\d[-_]\\d\\d([-_]\\d\\d)?([-_]\\d\\d)?))";	// hours and minutes optional
	public static final String rollingFilePatternString = "(.+)" + rollingFileExtPatternString + "$";
	public static final String dailyRollingFilePatternString = "(.+)" +  dailyRollingFileExtPatternString + "$";	// minutes optional
		
	private static final CategoryLogger logger = CategoryLoggerFactory.getLogger(LoggingUtil.class, Category.LOG);

	// Create a static object to synchronize access to the (local) logging config file
	private static final Object configFileLock = new Object();
	public static Object getConfigFileLock() {
		return configFileLock;
	}

	// Define an enum for the (3) different types of log files we care about
	public enum LogFileSource {		
		JAVA, SYSLOG, SMX;
	}
	
	// Define an enum for the methods of log rotation, and the associated rotated-file extensions
	public enum LogRotationMethod {
		ROLLING, DAILYROLLING;
	}

	// Prevent instantiation - this class is to be statically
	private LoggingUtil()
	{
		
	}
	
	/**
	 * Define some useful static patterns
	 */
	public static Pattern logPathPattern(String appenderPrefix) 
	{ 
		return Pattern.compile("(^\\s*" + appenderPrefix + "\\.file\\s*=\\s*)" + pathPatternString + "\\s*$");
	}
	
	public static Pattern thresholdPattern(String appenderPrefix)
	{
		return Pattern.compile("(^\\s*" + appenderPrefix + "\\.threshold\\s*=\\s*)" + LogLevel.Log4JLevel.patternString + "\\s*$");
	}
	
	public static Pattern datePatternPattern(String appenderPrefix) {
		return Pattern.compile("(^\\s*" + appenderPrefix + "\\.DatePattern\\s*=\\s*'\\.')" + datePatternPatternString + "\\s*$");	// DatePattern value must start with '.'
	}
	
	public static final Pattern loggerPattern(String loggerPrefix, String appenderName) {
		return Pattern.compile("(^\\s*" + loggerPrefix + "\\s*=\\s*)" + LogLevel.Log4JLevel.patternString + "(\\s*,.*\\b" + appenderName + "\\b.*$)");
	}

	// Convenience function to get the appropriate appender prefix
	public static String appenderPrefix(LogFileSource logSource)
	{
		if (LogFileSource.JAVA.equals(logSource)) {
			return e3Prefix;
		} else if (LogFileSource.SMX.equals(logSource)) {
			return smxPrefix;
		}
		throw new IllegalArgumentException("Invalid log-source argument: " + (logSource == null ? "(null)" : logSource));
	}
	
	// Convenience function to get the appropriate appender name
	public static String appenderName(LogFileSource logSource)
	{
		if (LogFileSource.JAVA.equals(logSource)) {
			return e3Appender;
		} else if (LogFileSource.SMX.equals(logSource)) {
			return smxAppender;
		}
		throw new IllegalArgumentException("Invalid log-source argument: " + (logSource == null ? "(null)" : logSource));	
	}
	
	// Convenience function to get the appropriate logger prefix
	public static String loggerPrefix(LogFileSource logSource)
	{
		if (LogFileSource.JAVA.equals(logSource)) {
			return e3LoggerPrefix;
		} else if (LogFileSource.SMX.equals(logSource)) {
			return smxLoggerPrefix;
		}
		throw new IllegalArgumentException("Invalid log-source argument: " + (logSource == null ? "(null)" : logSource));			
	}
	
	/**
	 * Parses the local logging-config file and attempts to find and return
	 * the path of the log-file target for the specified log source.
	 * Obtains shared config-file lock.
	 * 
	 * @param logSource			The type of log; currently accepts either
	 * <code>LogFileSource.JAVA</code> or <code>LogFileSource.SMX</code>
	 * @return					Path of log file, or empty string if not found
	 * @throws IOException
	 */
	public static String getLocalLogFilePath(LogFileSource logSource) throws IOException
	{
		String path = null;
		synchronized(getConfigFileLock()) {
			path = LoggingUtil.getLogFilePathFromConfigFile(new FileInputStream(defaultConfigPath), logSource, true);
		}
		return path;
	}
	
	/**
	 * Parses the specified logging-config file and attempts to find and return
	 * the path of the log-file target for the specified log source.
	 * Performs no locking on specified config file.
	 * 
	 * @param configFilePath	Path of config file (see defaultConfigPath for default) 
	 * @param logSource			The type of log; currently accepts either
	 * <code>LogFileSource.JAVA</code> or <code>LogFileSource.SMX</code>
	 * @param localHost			If <code>true</code>, will query environment variables to expand
	 * any variables in the returned path; if <code>false</code>, will use default values to
	 * expand any variables
	 * @return					Path of log file, or empty string if not found
	 * @throws IOException
	 */
	public static String getLogFilePathFromConfigFile(String configFilePath, LogFileSource logSource, boolean localHost) throws IOException
	{
		return LoggingUtil.getLogFilePathFromConfigFile(new FileInputStream(configFilePath), logSource, localHost);
	}
	
	/**
	 * Parses the specified logging-config file and attempts to find and return
	 * the path of the log-file target for the specified log source.
	 * Performs no locking on specified config file.
	 * 
	 * @param configFileStream	InputStream for config-file 
	 * @param logSource			The type of log; currently accepts either
	 * <code>LogFileSource.JAVA</code> or <code>LogFileSource.SMX</code>
	 * @param localHost			If <code>true</code>, will query environment variables to expand
	 * any variables in the returned path; if <code>false</code>, will use default values to
	 * expand any variables
	 * @return					Path of log file, or empty string if not found
	 * @throws IOException
	 */
	public static String getLogFilePathFromConfigFile(InputStream configFileStream, LogFileSource logSource, boolean localHost) throws IOException
	{
		String logFilePath = LoggingUtil.matchGroup(configFileStream, logPathPattern(appenderPrefix(logSource)), 2);
		return LoggingUtil.expandConfigVariablesInPath(logFilePath, localHost);
	}

	/**
	 * Parses the local logging-config file and attempts to find and return
	 * the log-level (log4j-style) for the specified log source.
	 * Obtains shared config-file lock.
	 * 
	 * @param logSource			The type of log; currently accepts either
	 * <code>LogFileSource.JAVA</code> or <code>LogFileSource.SMX</code>
	 * @return					LogLevel representing current log4j level, or null if not found
	 * @throws IOException
	 */
	public static LogLevel getLocalLogLevel(LogFileSource logSource) throws IOException
	{
		LogLevel level = null;
		synchronized(getConfigFileLock()) {
			level = LoggingUtil.getLogLevelFromConfigFile(new FileInputStream(defaultConfigPath), logSource);
		}
		return level;
	}
	
	/**
	 * Parses the specified logging-config file and attempts to find and return
	 * the log-level (log4j-style) for the specified log source.
	 * Performs no locking on specified config file.
	 * 
	 * @param configFileStream	Path of config file (see defaultConfigPath for default)
	 * @param logSource			The type of log; currently accepts either
	 * <code>LogFileSource.JAVA</code> or <code>LogFileSource.SMX</code>
	 * @return					LogLevel representing current log4j level, or null if not found
	 * @throws IOException
	 */
	public static LogLevel getLogLevelFromConfigFile(String configFilePath, LogFileSource logSource) throws IOException
	{
		return LoggingUtil.getLogLevelFromConfigFile(new FileInputStream(configFilePath), logSource);
	}
	
	/**
	 * Parses the specified logging-config file and attempts to find and return
	 * the log-level (log4j-style) for the specified log source.
	 * Performs no locking on specified config file.
	 * 
	 * @param configFileStream	InputStream for config-file 
	 * @param logSource			The type of log; currently accepts either
	 * <code>LogFileSource.JAVA</code> or <code>LogFileSource.SMX</code>
	 * @return					LogLevel representing current log4j level, or null if not found
	 * @throws IOException
	 */
	public static LogLevel getLogLevelFromConfigFile(InputStream configFileStream, LogFileSource logSource) throws IOException
	{
		// For source LogFileSource.JAVA, the level is found in two places: the logger line and the appender line
		// For LogFileSource.SMX, there is only the (root) logger line
		// So, for simplicity check the logger line in either case
		LogLevel logLevel = null;
		String level = LoggingUtil.matchGroup(configFileStream, loggerPattern(loggerPrefix(logSource), appenderName(logSource)), 2);
		if (LogLevel.isValidLogLevel(level)) {
			logLevel = new LogLevel(level);
		}
		return logLevel;
		
	}
	
	/**
	 * Parses the local logging-config file and attempts to find and return
	 * the date pattern for the specified log source's target log file, which in turn
	 * implies a log-rotation method (Rolling or DailyRolling).
	 * Obtains shared config-file lock.
	 * 
	 * @param logSource			The type of log; currently accepts either
	 * <code>LogFileSource.JAVA</code> or <code>LogFileSource.SMX</code>
	 * @return					The date pattern specified for the E3 appender, or empty string if not found
	 * @throws IOException
	 */
	public static String getLocalDatePattern(LogFileSource logSource) throws IOException
	{
		String pattern = null;
		synchronized(getConfigFileLock()) {
			pattern = LoggingUtil.getDatePatternFromConfigFile(new FileInputStream(defaultConfigPath), logSource);
		}
		return pattern;
	}
	
	/**
	 * Parses the specified logging-config file and attempts to find and return
	 * the date pattern for the specified log source's target log file, which in turn
	 * implies a log-rotation method (Rolling or DailyRolling).
	 * Performs no locking on specified config file.
	 * 
	 * @param configFileStream	Path of config file (see defaultConfigPath for default)
	 * @param logSource			The type of log; currently accepts either
	 * <code>LogFileSource.JAVA</code> or <code>LogFileSource.SMX</code>
	 * @return					The date pattern specified for the E3 appender, or empty string if not found
	 * @throws IOException
	 */
	public static String getDatePatternFromConfigFile(InputStream configFileStream, LogFileSource logSource) throws IOException
	{
		return LoggingUtil.matchGroup(configFileStream, datePatternPattern(appenderPrefix(logSource)), 2);
	}
	
	/**
	 * Parses the specified logging-config file and attempts to find and return
	 * the date pattern for the specified log source's target log file, which in turn
	 * implies a log-rotation method (Rolling or DailyRolling).
	 * Performs no locking on specified config file.
	 * 
	 * @param configFileStream	InputStream for config-file
	 * @param logSource			The type of log; currently accepts either
	 * <code>LogFileSource.JAVA</code> or <code>LogFileSource.SMX</code>
	 * @return					The date pattern specified for the E3 appender, or empty string if not found
	 * @throws IOException
	 */
	public static String getDatePatternFromConfigFile(String configFilePath, LogFileSource logSource) throws IOException
	{
		return LoggingUtil.getDatePatternFromConfigFile(new FileInputStream(configFilePath), logSource);
	}

	/**
	 * Parses the local logging-config file and attempts to infer the log-rotation
	 * method used by the specified log source from a date-pattern specification.  
	 * Obtains shared config-file lock.
	 * 
	 * @param logSource			The type of log; currently accepts either
	 * <code>LogFileSource.JAVA</code> or <code>LogFileSource.SMX</code>
	 * @return					The inferred log-rotation method, default to ROLLING
	 * @throws IOException
	 */
	public static LogRotationMethod getLocalRotationMethod(LogFileSource logSource) throws IOException
	{
		LogRotationMethod method = null;
		synchronized(getConfigFileLock()) {
			method = LoggingUtil.getRotationMethodFromConfigFile(new FileInputStream(defaultConfigPath), logSource);
		}
		return method;
	}
	
	/**
	 * Parses the specified logging-config file and attempts to infer the log-rotation
	 * method used by the specified log source from a date-pattern specification.  
	 * Performs no locking on specified config file.
	 * 
	 * @param configFileStream	Path of config file (see defaultConfigPath for default)
	 * @param logSource			The type of log; currently accepts either
	 * <code>LogFileSource.JAVA</code> or <code>LogFileSource.SMX</code>
	 * @return					The inferred log-rotation method, default to ROLLING
	 * @throws IOException
	 */
	public static LogRotationMethod getRotationMethodFromConfigFile(InputStream configFileStream, LogFileSource logSource) throws IOException
	{
		String datePattern = LoggingUtil.getDatePatternFromConfigFile(configFileStream,logSource);
		if ((datePattern != null) && (datePattern.length() != 0)) {
			return LogRotationMethod.DAILYROLLING;
		} else {
			return LogRotationMethod.ROLLING;
		}
	}

	/**
	 * Parses the specified logging-config file and attempts to infer the log-rotation
	 * method used by the specified log source from a date-pattern specification.  
	 * Performs no locking on specified config file.
	 * 
	 * @param configFileStream	InputStream for config-file
	 * @param logSource			The type of log; currently accepts either
	 * <code>LogFileSource.JAVA</code> or <code>LogFileSource.SMX</code>
	 * @return					The inferred log-rotation method, default to ROLLING
	 * @throws IOException
	 */
	public static LogRotationMethod getRotationMethodFromConfigFile(String configFilePath, LogFileSource logSource) throws IOException
	{
		return LoggingUtil.getRotationMethodFromConfigFile(new FileInputStream(configFilePath), logSource);
	}
	
	/**
	 * Scans through the inputStream and looks for matches in the regex pattern, returning
	 * the match at the specified index.  If multiple lines match, will return the
	 * last encountered matchgroup.
	 * 
	 * @param inputStream	InputStream to parse
	 * @param pattern		Regex pattern to match each line against
	 * @param index			Index of match group to return
	 * @return				Any match found, last occurrence if multiple, empty string if none found
	 * @throws IOException
	 */
	public static String matchGroup(InputStream inputStream, Pattern pattern, int index) throws IOException
	{
		String match = "";
		if ((inputStream != null) && (pattern != null)) {
			Scanner scanner = new Scanner(inputStream); // option for encoding
			try {
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					// Check for config-file comment lines
					if (line.matches("\\s*#.*")) {
						//logger.debug("Skipping comment line: {}", line);
						continue;
					}
					Matcher m = pattern.matcher(line);
					if (m.matches()) {
						// Don't break, but scan rest of lines in case there are redundant rules
						//logger.debug("Found e3config match: " + line);
						match = m.group(index);
					} else {
						logger.trace("No match between {} and {}", pattern, line);
					}
				}
			}
			finally {
				scanner.close();
			}
		}
		return match;
	}

	/**
	 * Sets the specified log level for the specified logSource on the local machine.
	 * 
	 * @param logSource	The type of log; currently accepts either
	 * <code>LogFileSource.JAVA</code> or <code>LogFileSource.SMX</code>
	 * @param logLevel	New log level (log4j value)
	 * @return	Returns <code>true</code> if a value was previously set
	 * @throws IOException
	 */
	public static boolean setLocalLogLevel(LogFileSource logSource, LogLevel logLevel) throws IOException
	{
		if (logger.isDebugEnabled()) {
			logger.debug("Call to setLocalLogLevel with level {} and source {}", logLevel, logSource);
		}
		if (LogFileSource.JAVA.equals(logSource)) {
			return LoggingUtil.setLocalJavaLogLevel(logLevel);
		} else if (LogFileSource.SMX.equals(logSource)) {
			return LoggingUtil.setLocalSMXLogLevel(logLevel);
		} else {
			throw new IllegalArgumentException("Invalid log-file source in call to setLocalLogLevel: " + logSource);
		}
	}
	/**
	 * Sets the local E3Appender (java) log level by modifying the local logging
	 * config file, which is watched by servicemix. If the config file doesn't
	 * contain either of the expected log-level rules, a new E3Appender
	 * section will be appended to the config file. 
	 * 
	 * @return Returns <code>true</code> if the expected rules were found and modified 
	 * @param logLevel	The new logLevel
	 * @throws IOException
	 */
	public static boolean setLocalJavaLogLevel(LogLevel newLogLevel) throws IOException
	{		
		if (newLogLevel == null) {
			throw new IllegalArgumentException("Log-level parameter must not be null!");			
		}
		// The E3Appender uses two threshold settings, one for the logger and one for the appender
		// Find and replace both
		final String newLine = System.getProperty("line.separator");
		StringBuilder modContents = new StringBuilder();
		Scanner scanner = new Scanner(new FileInputStream(LoggingUtil.defaultConfigPath)); // option for encoding
		Pattern thresholdPattern = LoggingUtil.thresholdPattern(LoggingUtil.appenderPrefix(LogFileSource.JAVA));
		Pattern loggerPattern = LoggingUtil.loggerPattern(LoggingUtil.e3LoggerPrefix, LoggingUtil.e3Appender);
		boolean foundThresholdRule = false;
		boolean foundLoggerRule = false;
		
		// Use cooperative lock in LoggingUtil to synchronize access to the local config file
		synchronized(LoggingUtil.getConfigFileLock()) {
			// Requires re-entrant config-file lock!
			if (newLogLevel.equals(getLocalLogLevel(LogFileSource.JAVA))) {
				if (logger.isDebugEnabled()) {
					logger.debug("Java log-level set to existing level: {}", newLogLevel);
				}
				return true;
			}
			try {
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					// Check for config-file comment lines
					if (line.matches("\\s*#.*")) {
						//logger.debug("Skipping comment line: {}", line);
					} else {
						Matcher m = thresholdPattern.matcher(line);
						if (m.matches()) {
							foundThresholdRule = true;
							line = m.replaceAll("$1" + newLogLevel.toString());
						} else {
							m = loggerPattern.matcher(line);
							if (m.matches()) {
								foundLoggerRule = true;
								line = m.replaceAll("$1" + newLogLevel.toString() + "$3");
							}
						}
					}
					modContents.append(line + newLine);
				}
			}
			finally {
				scanner.close();
			}

			// If E3 rules aren't found, append the default config settings
			if (!foundThresholdRule && !foundLoggerRule) {
				if (logger.isDebugEnabled()) {
					logger.debug("Using default config lines");
				}
				String log4jConf = defaultLog4jConf.replaceAll("\\$\\{level\\}", newLogLevel.toString()).replaceAll("\\$\\{logPath\\}", LoggingUtil.defaultLogPath);
				modContents.append(newLine + log4jConf + newLine);
			}

			// Write the modified contents to (active) config file, hope it's being 
			// watched by servicemix and so will update log level 
			FileWriter fw = new FileWriter(LoggingUtil.defaultConfigPath, false);
			try {
				fw.write(modContents.toString());
			} finally {
				fw.close();
			}
		}
		
		// Return of true means we've modified existing rules
		return (foundThresholdRule && foundLoggerRule);
	}

	/**
	 * Sets the local servicemix log level by modifying the local logging
	 * config file, which is watched by servicemix. If the expected rules are not
	 * found in the config file, this function will <em>not</em> add them, since
	 * servicemix output currently goes through the rootLogger and programmatic
	 * changes could have widespread side-effects.
	 * 
	 * @param logLevel	The new logLevel
	 * @return Returns <code>true</code> if the expected rules were found and modified 
	 * @throws IOException
	 */
	public static boolean setLocalSMXLogLevel(LogLevel newLogLevel) throws IOException
	{		
		if (newLogLevel == null) {
			throw new IllegalArgumentException("Log-level parameter must not be null!");			
		}
		// The servicemix appender uses just one threshold setting, on the rootLogger
		final String newLine = System.getProperty("line.separator");
		StringBuilder modContents = new StringBuilder();
		Scanner scanner = new Scanner(new FileInputStream(LoggingUtil.defaultConfigPath)); // option for encoding
		Pattern loggerPattern = LoggingUtil.loggerPattern(LoggingUtil.smxLoggerPrefix, LoggingUtil.smxAppender);
		boolean foundLoggerRule = false;
		
		// Use cooperative lock in LoggingUtil to synchronize access to the local config file
		synchronized(LoggingUtil.getConfigFileLock()) {
			// Requires re-entrant config-file lock!
			if (newLogLevel.equals(getLocalLogLevel(LogFileSource.SMX))) {
				logger.debug("SMX log-level set to existing level: {}", newLogLevel);
				return true;
			}
			try {
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					// Check for config-file comment lines
					if (line.matches("\\s*#.*")) {
						//logger.debug("Skipping comment line: {}", line);
					} else {
						Matcher m = loggerPattern.matcher(line);
						if (m.matches()) {
							foundLoggerRule = true;
							line = m.replaceAll("$1" + newLogLevel.toString() + "$3");
						}
					} 
					modContents.append(line + newLine);
				}
			}
			finally {
				scanner.close();
			}

			// If smx appender rules aren't found we don't do anything, because we'd
			// need to adjust the rootLogger settings
			if (!foundLoggerRule) {
				logger.warn("Couldn't find rootLogger settings for servicemix in config file: {}", LoggingUtil.defaultConfigPath);
			} else {
				// Write the modified contents to (active) config file, hope it's being 
				// watched by servicemix and so will update log level 
				FileWriter fw = new FileWriter(LoggingUtil.defaultConfigPath, false);
				try {
					fw.write(modContents.toString());
				} finally {
					fw.close();
				}
			}
		}
		
		// Return of true means we've modified existing rule
		return (foundLoggerRule);
	}

	/**
	 * Replace karaf environment variables (such as "${karaf.data}") in path.
	 * Only works on the localhost, and when path contains at most one variable.
	 * 
	 * @param path	The path to parse
	 * @return	The path with variables replaced by values
	 */
	public static String expandConfigVariablesInPath(String path, boolean checkEnvVars)
	{
		final String regexPrefix = "^(.*)\\$\\{(?i)karaf\\.";
		final String regexSuffix = "(?-i)\\}(.*)$";
		final String dataRegex = regexPrefix + "data" + regexSuffix;
		final String homeRegex = regexPrefix + "home" + regexSuffix;
		final String baseRegex = regexPrefix + "base" + regexSuffix;

		if (path != null) {
			if (path.matches(dataRegex)) {
				logger.debug("Found karaf.data variable in path: {}", path);
				String karafDataPath = null;
				if (checkEnvVars) {
					karafDataPath = System.getenv("KARAF_DATA");
					logger.debug("Env. karaf data path: {}", karafDataPath);
				}
				if ((karafDataPath != null) && (karafDataPath.length() > 0)) {
					path = path.replaceFirst(dataRegex, "$1" + karafDataPath + "$2");
				} else {
					path = path.replaceFirst(dataRegex, defaultKarafPath + File.separator + "data" + "$2");
				}
				logger.debug("Replacement path: {}", path);
			} else if (path.contains("${karaf.home}")) {
				logger.debug("Found karaf.home variable in path: {}", path);
				String karafHomePath = null;
				if (checkEnvVars) {
					karafHomePath = System.getenv("KARAF_HOME");
					logger.debug("Env. karaf home path: {}", karafHomePath);
				}
				if ((karafHomePath != null) && (karafHomePath.length() > 0)) {
					path = path.replaceFirst(homeRegex, "$1" + karafHomePath + "$2");
				} else {
					path = path.replaceFirst(homeRegex, defaultKarafPath + "$2");
				}
				logger.debug("Replacement path: {}", path);
			} else if (path.contains("${karaf.base}")) {
				logger.debug("Found karaf.base variable in path: {}", path);
				String karafBasePath = null;
				if (checkEnvVars) {
					karafBasePath = System.getenv("KARAF_BASE");
					logger.debug("Env. karaf base path: {}", karafBasePath);
				}
				if ((karafBasePath != null) && (karafBasePath.length() > 0)) {
					path = path.replaceFirst(baseRegex, "$1" + karafBasePath + "$2");
				} else {
					path = path.replaceFirst(baseRegex, defaultKarafPath + "$2");
				}
				logger.debug("Replacement path: {}", path);
			}
		}
		return path;
	}
	
	/**
	 * Returns the path the the apache-servicemix/karaf install on this machine.
	 * Looks up the karaf path in the environment variable 'KARAF_HOME'; if this
	 * doesn't return a value, uses a hard-coded value.
	 * 
	 * @return	The path the karaf install
	 */
	public static String karafPath() 
	{
		String karafHomePath = System.getenv("KARAF_HOME");
		if ((karafHomePath == null) || (karafHomePath.length() == 0)) {
			karafHomePath = defaultKarafPath;
		}
		return karafHomePath;
	}
}
