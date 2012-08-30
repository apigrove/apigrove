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
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alu.e3.data.model.LogLevel;

/**
 * The NonJavaLogger class is designed to enable non-Java E3 components to log to syslog.
 * 
 * IMPORTANT NOTE:
 * In order to enable the setLogLevel() function, some entries must be made in the system sudoers file:
 * 
 * 		e3	hostname=(root)NOPASSWD:/bin/cp /home/$user/tmp/e3syslog.conf /etc/rsyslog.conf
 * 		e3	hostname=(root)NOPASSWD:/etc/init.d/rsyslog restart
 * 
 * where "hostname" is replaced with the actual system hostname.  We assume these sudoers changes will
 * be performed as a part of the E3 install process.  (Otherwise calls to setLogLevel() will result in
 * permission-denied IOExceptions.)
 * 
 * Some limitations on parsing syslog.conf rules:
 * <ul>
 * <li> Does not handle multiple selectors, such as "e3.info;kern.debug	/var/log/e3.log"
 * <li> Does not handle multiple facilities, such as "e3,cron.info	/var/log/e3.log"
 * <li> Does not handle level prefixes, such as "=" and "!="
 * </ul>
 * 
 */

public class NonJavaLogger {

	private static final CategoryLogger logger = CategoryLoggerFactory.getLogger(NonJavaLogger.class, Category.LOG);

	// The E3-non-java logging parameters (in syslog) are arbitrary:
	public static final String defaultLogLevel = "info";
	private static String e3Facility = "local3";	// reflects both default & syslog.conf value (if written)

	// This path is specified in the sudoers file and can't be changed here unless sudoers is also changed:
	public static final String defaultLogFilePath = "/var/log/e3syslog.log";	

	// The paths and control commands for syslog are system-dependent:
	// On RHEL & OS-X: configFilePath = "/etc/syslog.conf";
	// On CentOS: configFilePath = "/etc/rsyslog.conf";
	// On RHEL: syslogRestartCmd = "sudo /etc/init.d/syslog restart";
	// On CentOS: syslogRestartCmd = "sudo /etc/init.d/rsyslog restart";
	// On OS-X: syslogRestartCmd = "sudo kill -SIGHUP `cat /var/run/syslog.pid`";
	private static String configFilePath1 = "/etc/syslog.conf";		// RHEL & OS-X
	private static String configFilePath2 = "/etc/rsyslog.conf";	// CentOS
	private static String configFilePath = null;
	private static String tmpConfigFilePath = "/home/e3/tmp/e3syslog.conf";	// arbitrary, used for copying to system file	
	private static String servicePath1 = "/etc/init.d/syslog";	// RHEL
	private static String servicePath2 = "/etc/init.d/rsyslog";	// CentOS	
	private static String servicePath = null;
	private static String logrotateFilePath = "/etc/logrotate.d/e3";
	private static String tmpLogrotateFilePath = "/home/e3/tmp/e3logrotate"; 
	
	// Create a static object to synchronize access to the (local) syslog config file
	private static final Object configFileLock = new Object();
	public static Object getConfigFileLock() {
		return configFileLock;
	}

	// Build a static syslog-line regex matching pattern for efficiency
	private final static String newLine = System.getProperty("line.separator");
	private static Pattern e3RulePattern = NonJavaLogger.compileE3RulePattern();
	
	// Determine the actual config-file path on this system:
	private static boolean locateConfigFile() {
		// Default to configFilePath1, even if we can't find an alternative
		configFilePath = configFilePath1;
		File configFile = new File(configFilePath);
		if (configFile.exists()) {
			return true;
		} else {
			configFile = new File(configFilePath2);
			if (configFile.exists()) {
				configFilePath = configFilePath2;
				return true;
			}
		}
		return false;
	}

	// Determine the actual syslog service path on this system:
	private static boolean locateService() {
		// Default to servicePath1, even if we can't find an alternative
		servicePath = servicePath1;
		File serviceFile = new File(servicePath);
		if (serviceFile.exists()) {
			return true;
		} else {
			serviceFile = new File(servicePath2);
			if (serviceFile.exists()) {
				servicePath = servicePath2;
				return true;
			}
		}
		return false;
	}

	// Recompile the pattern every time e3Facility changes
	private static Pattern compileE3RulePattern() 
	{
		return Pattern.compile("^\\s*" + e3Facility + "\\." + LogLevel.SyslogLevel.configPatternString + "(\\s+)" + LoggingUtil.pathPatternString + "\\s*$");
	}
	
	private static String copyConfigFileCmd()
	{
		return "sudo /bin/cp " + tmpConfigFilePath + " " + getConfigFilePath();
	}

	private static String syslogRestartCmd()
	{
		return "sudo " + getSyslogServicePath() + " restart";
	}
	
	private static String copyLogrotateFileCmd()
	{
		return "sudo /bin/cp " + tmpLogrotateFilePath + " " + getLogrotateFilePath();
	}

	private static boolean isEmpty(String s)
	{
		return ((s == null) || (s.length() == 0));
	}

	private static boolean replaceE3Rule(String facility, String level, String logFilePath) throws IOException
	{
		StringBuilder modContents = new StringBuilder();
		StringBuilder replacementRule = new StringBuilder();
		replacementRule.append((NonJavaLogger.isEmpty(facility) ? e3Facility : facility) + ".");
		replacementRule.append((NonJavaLogger.isEmpty(level) ? "$1" : level) + "$2");
		replacementRule.append(NonJavaLogger.isEmpty(logFilePath) ? "$3" : logFilePath);

		Scanner scanner = new Scanner(new FileInputStream(getConfigFilePath())); // option for encoding
		boolean foundE3Rule = false;
		String newRule = null;
		int copyExitValue = 1;
		
		synchronized(getConfigFileLock()) {
			try {
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					// Check for comment lines (this is specific to logging-config files!)
					if (!line.matches("^\\s*#.*")) {
						Matcher m = e3RulePattern.matcher(line);
						if (m.matches()) {
							foundE3Rule = true;
							logger.debug("Existing e3-syslog rule: {}", line);
							line = m.replaceAll(replacementRule.toString());
							newRule = line;
						}
					}
					modContents.append(line + newLine);
				}
			}
			finally {
				scanner.close();
			}

			// Append a new rule if none already exists
			if (!foundE3Rule) {
				newRule = (NonJavaLogger.isEmpty(facility) ? e3Facility : facility) + "." + 
						(NonJavaLogger.isEmpty(level) ? defaultLogLevel : level) + "\t\t" + 
						(NonJavaLogger.isEmpty(logFilePath) ? defaultLogFilePath : logFilePath);
				logger.debug("Adding new e3-syslog rule: {}", newRule);
				modContents.append(newLine + newRule + newLine);
			}

			File tmpFile = new File(tmpConfigFilePath);
			File tmpDir = tmpFile.getParentFile();
			if (!tmpDir.exists()) {
				tmpDir.mkdirs();
			}
			FileWriter fw = new FileWriter(tmpConfigFilePath, false);
			try {
				fw.write(modContents.toString());
			} finally {
				fw.close();
			}
			logger.debug("Attempting to copy syslog.conf: {}", copyConfigFileCmd());
			Process p = Runtime.getRuntime().exec(copyConfigFileCmd());
			try {
				p.waitFor();
				copyExitValue = p.exitValue();
				logger.debug("Syslog config copy process returned {}", String.valueOf(copyExitValue));
			} catch (InterruptedException ex) {
				logger.warn("Syslog config copy process was interrupted!");
				return false;
			} finally {
				tmpFile.delete();
			}
		}
		
		Process p = Runtime.getRuntime().exec(syslogRestartCmd());
		int restartExitValue = 1;
		logger.debug("Attempting to restart rsyslog.d: {}", syslogRestartCmd());
		try {
			p.waitFor();
			restartExitValue = p.exitValue();
			logger.debug("Syslog restart process returned {}", String.valueOf(restartExitValue));
		} catch (InterruptedException ex) {
			logger.warn("Syslog daemon restart process was interrupted!");
			return false;
		}

		if ((copyExitValue == 0) && (restartExitValue == 0)) {
			logger.debug("Successfully changed E3 syslog rule: {}", newRule);
		
			// Save any new facility value if we've successfully written syslog.conf
			if (!NonJavaLogger.isEmpty(facility) && !facility.equals(e3Facility)) {
				e3Facility = facility;
				e3RulePattern = NonJavaLogger.compileE3RulePattern();
			}
		}
		return foundE3Rule;
	}
	
	private static String logrotateContents(String e3LogFilePath) 
	{
		return e3LogFilePath + " {\n" +
	        "missingok \n" +
	        "notifempty \n" +
	        "daily \n" +
	        "create 0660 e3 root \n" +
	        "dateext \n" +
	        "dateformat .%Y%m%d \n" +
	    "}";
	}

	private static boolean writeE3LogrotateFile(String logFilePath) throws IOException
	{
		boolean success = false;
		boolean useTempFile = false;
		String contents = logrotateContents(logFilePath);

		String writePath;
		File tmpFile = null;
		if (useTempFile) {
			writePath = tmpLogrotateFilePath;
			tmpFile = new File(writePath);
			File tmpDir = tmpFile.getParentFile();
			if (!tmpDir.exists()) {
				tmpDir.mkdirs();
			}
		} else {
			writePath = logrotateFilePath;
		}
		FileWriter fw = new FileWriter(writePath, false);
		try {
			fw.write(contents);
		} finally {
			fw.close();
		}
		
		if (useTempFile) {
			logger.debug("Attempting to copy e3 logrotate config file: {}", copyLogrotateFileCmd());
			Process p = Runtime.getRuntime().exec(copyLogrotateFileCmd());
			int copyExitValue = 1;
			try {
				p.waitFor();
				copyExitValue = p.exitValue();
				logger.debug("Logrotate config copy process returned {}", String.valueOf(copyExitValue));
				success = (copyExitValue == 0);
			} catch (InterruptedException ex) {
				logger.warn("Logrotate config copy process was interrupted!");
				success = false;
			} finally {
				tmpFile.delete();
			}
		} else {
			// If we've gotten this far without an exception we've written the file
			success = true;
		}
		
		logger.debug("{} change of E3 logrotate config file: {}", success ? "Successful" : "Unsuccessful",
				success ? contents : logrotateFilePath);
		
		return success;
	}
	
	/**
	 * Attempt to parse the current syslog config file to determine
	 * the current log level for the E3 facility.
	 * 
	 * @return	The current syslog level, or null if an E3-facility rule is not found 
	 * @throws IOException
	 */
	public static String getLogLevel() throws IOException 
	{
		String level = null;
		synchronized(getConfigFileLock()) {
			level = LoggingUtil.matchGroup(new FileInputStream(getConfigFilePath()), e3RulePattern, 1);
		}
		return LogLevel.isValidSyslogLevel(level) ? level : null;
	}

	/**
	 * Attempt to set the local syslog level for the E3 facility
	 * by modifying the syslog config file and restarting the syslog
	 * daemon.  If the config file is writable but an E3 facility
	 * rule is not found, will add one to the config file.
	 * 
	 * @param newLevel	A valid syslog level, one of: 
	 * 	<ul><li>EMERG <li>ALERT <li>CRIT <li>ERR <li>WARNING <li>NOTICE <li>INFO <li>DEBUG</ul>
	 * @throws IOException
	 */
	public static void setLogLevel(String newLevel) throws IOException
	{
		logger.debug("Call to set syslog logLevel to {}", newLevel);

		if (LogLevel.isValidSyslogLevel(newLevel)) {
			// Relies on config-file lock being reentrant!
			synchronized(getConfigFileLock()) {
				// Since modifying the syslog config file also requires a restart of the syslog deamon,
				// only proceed if newLevel is different than current level
				String currentSyslogLevel = NonJavaLogger.getLogLevel();
				if (newLevel.equalsIgnoreCase(currentSyslogLevel)) {
					logger.debug("current local syslog level is already at {}", newLevel);
				} else {
					NonJavaLogger.replaceE3Rule("", newLevel, "");
				}
			}
		} else {
			throw new IllegalArgumentException("Invalid log level value");			
		}
	}
	
	/**
	 * Attempts to parse the syslog config file to determine the path of
	 * the log-file target specified in the E3 rule.  Returns an empty
	 * string if no rule is found, or an IOException if the config file
	 * is not found or cannot be opened due to insufficient permissions.
	 * 
	 * @return	Path of E3 syslog file, or empty string if not found
	 * @throws IOException
	 */
	public static String getLogFilePath() throws IOException
	{
		String path = null;
		synchronized(getConfigFileLock()) {
			path = LoggingUtil.matchGroup(new FileInputStream(getConfigFilePath()), e3RulePattern, 3);
		}
		return path;
	}

	/**
	 * Attempt to set the syslog log-file target for the E3 facility
	 * by modifying the syslog config file and restarting the syslog
	 * daemon.  If the config file is writable but an E3 facility
	 * rule is not found, will add one to the config file.
	 * 
	 * @param newPath	The new log-file path
	 * @throws IOException
	 */
	public static void setLogFilePath(String newPath) throws IOException
	{
		logger.debug("Call to set syslog logFilePath to {}", newPath);
		String path = newPath == null ? "" : newPath.trim();
		if (path.length() == 0 || !path.matches(LoggingUtil.pathPatternString)) {
			throw new IllegalArgumentException("Illegal log-file path");			
		}
		// Since modifying the syslog config file also requires a restart of the syslog deamon,
		// only proceed if path is different than current path
		// Relies on config-file lock being reentrant!
		synchronized(getConfigFileLock()) {
			String currentPath = NonJavaLogger.getLogFilePath();
			if (newPath.equalsIgnoreCase(currentPath)) {
				logger.debug("current local syslog log-file path is already at {}", newPath);
			} else {
				// Change the log-rotate file first, since replaceE3Rule() also restarts the syslog daemon
				writeE3LogrotateFile(path);
				replaceE3Rule("", "", path);
			}
		}
	}
	
	public static String getE3Facility()
	{
		return e3Facility;
	}
	
	/**
	 * Attempt to set the E3-specific facility for syslog logging
	 * by modifying the syslog config file and restarting the syslog
	 * daemon.  If the config file is writable but an E3 facility
	 * rule is not found, will add one to the config file.
	 * 
	 * @param newPath	The syslog facility to use, may not be a system facility
	 * @throws IOException
	 */
	public static void setE3Facility(String newFacility) throws IOException
	{
		// Allow "user" facility?
		logger.debug("Call to set syslog facility to {}", newFacility);
		final String sysFacilities = "(?i)(auth|authpriv|cron|daemon|kern|lpr|mail|mark|news|security|syslog|user|uucp)(?-i)";
		String facility = newFacility.trim().toLowerCase();
		if (facility.matches(sysFacilities)) {
			throw new IllegalArgumentException("Can't use system facilities for E3 syslogging!");
		}		
		NonJavaLogger.replaceE3Rule(facility, "", "");
	}
	
	/**
	 * Returns the assumed path for the local syslog config file.
	 * 
	 * @return	Path for the syslog config file.
	 */
	public static String getConfigFilePath()
	{
		if (configFilePath == null) {
			locateConfigFile();
		}
		return configFilePath;
	}

	/**
	 * Returns an array of known syslog config file paths.
	 * The current set is based on known Linux paths.
	 * 
	 * @return	An array of possible syslog config paths.
	 */
	public static String[] getAltConfigFilePaths()
	{
		String [] paths = {configFilePath1, configFilePath2};
		return paths;
	}

	/**
	 * Set the path for the syslog config file to be used
	 * in all subsequent access operations.
	 * 
	 * @param newPath	Path to the syslog config file.
	 */
	static void setConfigFilePath(String newPath)
	{
		configFilePath = newPath;
	}
	
	/**
	 * Returns the path to the syslog daemon, which needs to
	 * be restarted in order to pick up any changes made to the
	 * syslog config file.
	 * 
	 * @return	Path for the syslog daemon file.
	 */
	public static String getSyslogServicePath()
	{
		if (servicePath == null) {
			locateService();
		}
		return servicePath;
	}
	
	/**
	 * Sets the path to the syslog daemon, which needs to
	 * be restarted in order to pick up any changes made to the
	 * syslog config file.
	 * 
	 * This is normally a default but may have to be
	 * set on a per-system basis.
	 * 
	 * @param newCmd	The path to the syslog daemon
	 */
	static void setSyslogServicePath(String newPath)
	{
		servicePath = newPath;
	}

	/**
	 * Returns the path to the e3 logrotate config file.
	 * 
	 * @return	The e3-logrotate file path
	 */
	public static String getLogrotateFilePath()
	{
		return logrotateFilePath;
	}

	/**
	 * Attempt to parse the specified syslog config file to determine
	 * the log level for the E3 facility.  This function is provided for
	 * use with config files retrieved from remote hosts.  No locking or
	 * access synchronization on the config file is attempted.
	 * 
	 * @return	The current syslog level, or null if an E3-facility rule is not found 
	 * @throws IOException
	 */
	public static String getLogLevelFromConfigFile(String configFilePath) throws IOException
	{
		String level = LoggingUtil.matchGroup(new FileInputStream(configFilePath), e3RulePattern, 1);
		return LogLevel.isValidSyslogLevel(level) ? level : null;
	}

	/**
	 * Attempt to parse the specified syslog config file to determine
	 * the log target path for the E3 facility.  This function is provided for
	 * use with config files retrieved from remote hosts.  No locking or
	 * access synchronization on the config file is attempted.
	 * 
	 * @return	The log-file path, or an empty string if an E3-facility rule is not found 
	 * @throws IOException
	 */
	public static String getLogFilePathFromConfigFile(String configFilePath) throws IOException
	{
		return LoggingUtil.matchGroup(new FileInputStream(configFilePath), e3RulePattern, 3);
	}

}
