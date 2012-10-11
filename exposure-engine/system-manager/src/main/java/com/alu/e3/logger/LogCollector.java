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
package com.alu.e3.logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringEscapeUtils;

import com.alu.e3.Utilities;
import com.alu.e3.common.E3Constant;
import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.common.logging.LoggingUtil;
import com.alu.e3.common.logging.LoggingUtil.LogFileSource;
import com.alu.e3.common.logging.NonJavaLogger;
import com.alu.e3.common.tools.CommonTools;
import com.alu.e3.data.model.Instance;
import com.alu.e3.installer.NonExistingManagerException;
import com.alu.e3.installer.command.SSHCommand;
import com.alu.e3.installer.command.ShellCommandResult;
import com.alu.e3.osgi.api.ITopology;
import com.jcraft.jsch.JSchException;

/**
 * The LogCollector class provides an object to traverse the topology
 * and visit each instance, collecting log files to a repository on
 * the system manager.
 * 
 * There are still some unresolved issues in the design and/or
 * implementation of the Log Collector.  These include:
 * <ul>
 * <li> Throughout the code, instances are referred to by IP-Address.
 * This is probably not right, and will have to be changed to some
 * sort of (non-volatile) instance identifier.
  * <li> If logging config files are moved or significantly edited
 * (or not set up properly on install) the Collector may not be 
 * able to find remote log files.
 * </ul>
 */

public class LogCollector implements Callable<Long> {
	
	private static final String COLLECTION_PATH = System.getProperty("user.home") + "/apache-servicemix/data/logCollection";
	private static final CategoryLogger logger = CategoryLoggerFactory.getLogger(LogCollector.class, Category.LOG);
	
	// Should collected files be deleted on the source after collection?
	private static final boolean deleteAfterCollect = true;
	
	// If a file already exists with the copied log-file name, append a ".1" etc to uniquify?
	// Don't uniquify if we don't delete source logs, since this will lead to local duplicates
	private static final boolean uniquifyCopiedFilenames = deleteAfterCollect;
	private static final int uniquifyLimit = 100;	// unique filename extension limit, after this overwrite
	
	// Should the collector only collect (and delete) files owned by the ssh user?
	// This feature hasn't yet been tested ...
	private static final boolean collectOnlyUserOwnedLogs = false;
	
	// Set a short ssh connect timeout since we assume all instances are close, 
	// and on timeout we can try again on next collection 
	private static final int sshSessionTimeout = 10000; // 10s 	
	
	/*
	 * Synchronization policy: allow only one writer to operate at a time.
	 * Allow concurrent readers and writer, except that readers must ignore
	 * any files the writer is currently writing, indicated by a specific suffix
	 */
	// For now: use a lock to allow only one writer (collector) to run at a time
	private static final Lock writerLock = new ReentrantLock();
	// Reader should ignore these files - collector is currently writing
	private static final String workingFileSuffix = "~";	
		
	// Each logCollector instance gets its own serial number
	// A class-static atomic variable keeps the serial numbers unique
	private static final AtomicLong serialNumber = new AtomicLong();
	private static long generateSerialNumber() { return serialNumber.getAndIncrement(); }
	private static final AtomicLong lastCompletedCollector = new AtomicLong();
	private final long collectorSerialNumber;
	
	// Each collector can have its own topology (we could split large
	// topologies for collection by multiple collectors)
	private final ITopology topology;
	
	/**
	 * Data structure to hold information about a (remote) file
	 *
	 */
	private static class FileInfo {
		public String filePath;
		public String fileSize;
		public String fileModtime;
		
		FileInfo(String path, String size, String modtime) {
			filePath = path;
			fileSize = size;
			fileModtime = modtime;
		}
	}
	
	/**
	 * File-name filter to check for log files (by extension)
	 * and to ignore "working files" (files being written by collector)
	 */
	private static class LogFileFilter implements FileFilter
	{
		private String basename;
		public LogFileFilter(String basename) 
		{
			this.basename = basename;
		}
		public boolean accept(File pathname) 
		{
			String filename = pathname.getName();
			if (basename != null) {
				return ((filename.matches(basename + LoggingUtil.rollingFileExtPatternString) || 
						filename.matches(basename + LoggingUtil.dailyRollingFileExtPatternString) &&
						!filename.endsWith(LogCollector.workingFileSuffix)));
			} else {
				return ((filename.matches(LoggingUtil.rollingFilePatternString) || 
						filename.matches(LoggingUtil.dailyRollingFilePatternString) &&
						!filename.endsWith(LogCollector.workingFileSuffix)));				
			}
		}
	}

	/**
	 * LogCollector constructor without parameters to satisfy spring-bean requirements.
	 */
	public LogCollector(ITopology topology) 
	{
		this.topology = topology;
		this.collectorSerialNumber = LogCollector.generateSerialNumber();
	}
	
	public long getCollectorID()
	{
		return collectorSerialNumber;
	}
	
	public static long getLastCollectorID() 
	{
		return lastCompletedCollector.longValue();
	}
	
	/**
	 * Traverse the topology and collect logs from each instance found.
	 * 
	 * @return The number of bytes collected in all logs
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	@Override
	public Long call() throws InterruptedException, TimeoutException, NonExistingManagerException
	{
		if(logger.isDebugEnabled()) {
			logger.debug("Call to LogCollector.call()");
		}
		long bytesCollected = collectAllLogs(0, TimeUnit.SECONDS);
		return Long.valueOf(bytesCollected);
	}

	/**
	 * Traverse the topology and collect logs from each instance found.
	 * 
	 * @param waitTimeout	The time to wait if another collector is running
	 * @param unit	The time unit for waitTimeout
	 * @return The number of bytes collected in all logs
	 * @throws InterruptedException
	 * @throws TimeoutException
	 * @throws NonExistingManagerException 
	 */
	public long collectAllLogs(long waitTimeout, TimeUnit unit) throws InterruptedException, TimeoutException, NonExistingManagerException
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
		String launchDate = dateFormat.format(new Date());
		logger.debug("Launching system-manager log-file collection ({}) ...", launchDate);

		if (!LogCollector.writerLock.tryLock(waitTimeout, unit)) {
			logger.warn("Attempt to run log collector but cannot acquire lock (another collection must be running)");
			throw new TimeoutException("Timeout waiting to acquire log collector write lock");
		}
		long collectionCount = 0L;
		logger.debug("LogCollector ID: {}", getCollectorID());

		try {
			Set<String> visitedIPs = new HashSet<String>();

			// Create the top-level log collection directory, if this is the first time run
			File collectionDir = new File(COLLECTION_PATH);
			if (!collectionDir.exists() && !collectionDir.mkdirs()) {
				logger.error("Unable to create log-collection directory: {}", COLLECTION_PATH);
				return 0L;
			}

			// Iterate through all instances in the current topology			
			Instance logCollectorInstance = Utilities.getManagerByIP(CommonTools.getLocalHostname(), CommonTools.getLocalAddress(),  topology.getInstancesByType(E3Constant.E3MANAGER), logger);
			List<Instance> instances = getInstanceList(this.topology);
			if(logger.isDebugEnabled()) {
				logger.debug("There are {} instances in the current topology", instances.size());
			}
			for (Instance logSource : instances) {
				LogCollector.logInstance(logSource);	// for debugging

				// Avoid visiting the same address twice
				String ipAddress = logSource.getInternalIP();
				if (ipAddress == null) {
					logger.warn("Encountered instance node with null ipAddress during log collection!");
					continue;
				}
				if (CommonTools.isLocal(ipAddress)) {
					ipAddress = E3Constant.localhost;	// stay consistent
				}
				if (visitedIPs.contains(ipAddress)) {
					if(logger.isDebugEnabled()) {
						logger.debug("Skipping already-visited address: {}", ipAddress);
					}
					continue;
				}
				visitedIPs.add(ipAddress);

				// Create or verify the existence of a log-collection target directory
				String sanitizedHost = ipToCollectionDirectory(ipAddress);			
				File instanceCollectionDir = new File(COLLECTION_PATH, sanitizedHost);
				if (instanceCollectionDir.exists()) {
					if (!instanceCollectionDir.isDirectory()) {
						logger.error("Log-collection target exists but is not a directory: {}", instanceCollectionDir.getAbsolutePath());
						continue;					
					}
				} else {
					if (!instanceCollectionDir.mkdirs()) {
						logger.error("Unable to create log-collection directory: {}", instanceCollectionDir.getAbsolutePath());
						continue;
					}
				}

				// Finally, perform log collection
				// There may be a chance for parallelism here by farming the collection work for each instance
				// out to a separate worker thread.  At a minimum the local collection could occur in parallel with
				// collection on a remote host.
				if (ipAddress.equalsIgnoreCase(E3Constant.localhost)) {
					try {
						collectionCount += collectAllLocalLogs(instanceCollectionDir);
					} catch (IOException ex) { 
						logger.warn("Error trying to copy local log files to {}", instanceCollectionDir.getAbsolutePath());
					}
				} else {
					try {
						collectionCount += collectAllRemoteLogs(logSource, logCollectorInstance, instanceCollectionDir);					
					} catch (JSchException ex) {
						if(logger.isDebugEnabled()) {
							logger.debug("Could not connect to host: {}", logSource.getInternalIP());
							logger.debug(ex.getLocalizedMessage());
						}
					} catch (IOException ex) {
						if(logger.isDebugEnabled()) {
							logger.debug("Got IOException while connecting to or transferring files from host: {}", logSource.getInternalIP());
							logger.debug(ex.getLocalizedMessage());
						}
					}
				}
				// At this point the collection has "completed", even if IOExceptions could have
				// occurred and been caught above
				LogCollector.lastCompletedCollector.set(getCollectorID());
				logger.debug("Completed log collection with ID: {} ({})", getCollectorID(), dateFormat.format(new Date()));
			}
		} finally {
			LogCollector.writerLock.unlock();
		}
		return collectionCount;
	}
	
	/**
	 * Traverse the topology and collect a certain number of lines from the
	 * active logs on each instance found.
	 * 
	 * @param numLines	The number of lines to retrieve from each log file on each instance
	 * @return	An XML-structured string with a single <code>&lt;logCollection&gt;</code> node and
	 * <code>&lt;log&gt;</code> node for each log-file found (including multiple instances).
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	public String collectAllActiveLogs(int numLines)
	{
		StringBuilder sb = new StringBuilder();

		Set<String> visitedIPs = new HashSet<String>();

		// Iterate through all instances in the current topology
		// (Note: use TRACE-level log statements so that our logging doesn't appear in returned log tails)
		List<Instance> instances = getInstanceList(this.topology);
		logger.trace("There are {} instances in the current topology", instances.size());

		Instance collectorInstance;
		try {
			collectorInstance = Utilities.getManagerByIP(CommonTools.getLocalHostname(), CommonTools.getLocalAddress(),  topology.getInstancesByType(E3Constant.E3MANAGER), logger);
		} catch (NonExistingManagerException e) {
			String errMsg = "Cannot find manager at " + CommonTools.getLocalAddress() + " while collecting active logs.";
			logger.error(errMsg);
			return errMsg;
		}
		
		for (Instance instance : instances) {

			// Avoid visiting the same address twice
			String ipAddress = instance.getInternalIP();
			if (ipAddress == null) {
				logger.warn("Encountered instance node with null ipAddress during log collection!");
				continue;
			}
			if (CommonTools.isLocal(ipAddress)) {
				ipAddress = E3Constant.localhost;	// stay consistent
			}
			if (visitedIPs.contains(ipAddress)) {
				logger.trace("Skipping already-visited address: {}", ipAddress);
				continue;
			}
			visitedIPs.add(ipAddress);

			// Finally, perform log collection (of active-log tails)
			if (ipAddress.equalsIgnoreCase(E3Constant.localhost)) {
				try {
					String logs = getTailOfLocalActiveLogs(numLines);
					if (logs != null) {
						sb.append(logs);
					}
				} catch (IOException ex) { 
					logger.warn("Error trying to get tail of active log files from {}", ipAddress);
				}
			} else {
				try {
					String logs = getTailOfRemoteActiveLogs(instance, collectorInstance, numLines);
					if (logs != null) {
						sb.append(logs);
					}
				} catch (JSchException ex) {
					logger.warn("Could not connect to host: {}", instance.getInternalIP());
					logger.warn(ex.getLocalizedMessage());
				} catch (IOException ex) {
					logger.warn("Got IOException while connecting to or transferring files from host: {}", instance.getInternalIP());
					logger.warn(ex.getLocalizedMessage());
				}
			}

		}
		return LoggingResponseBuilder.logCollectionToXml(sb.toString());
	}

	/** 
	 * A top-level method to return a certain number of most-recent log
	 * lines from previously-connected log files from all instances.
	 * 
	 * @param numLines	The requested number of log lines to retrieve.  
	 * Fewer lines may be returned if sufficient log entries are not available. 
	 * @return	An XML-structured string with a single <code>&lt;logCollection&gt;</code> node and
	 * <code>&lt;log&gt;</code> node for each log-file found (including multiple instances).
	 */
	public String getCollectedLogLines(int numLines) 
	{
		File collectionDir = new File(COLLECTION_PATH);
   		if (!collectionDir.exists() || !collectionDir.isDirectory()) {
   			return null;
   		}
		
   		if(logger.isDebugEnabled()) {
   			logger.debug("Attempting to get all collected logs ...");
   		}
		StringBuilder logLines = new StringBuilder();	
   		File[] files = collectionDir.listFiles();	// get a list of instance directories
   		for (File item : files) {
   			if(logger.isDebugEnabled()) {
   				logger.debug("Collection directory entry: {}", item.getAbsolutePath());
   			}
   			if (item.isDirectory()) {
   				String ipAddress = collectionDirectoryToIP(item.getName());
   				if(logger.isDebugEnabled()) {
   					logger.debug("Getting logs from: {}", ipAddress);
   				}
   				// First get the java logs
   				String contents = getCollectedLogLinesFromInstance(ipAddress, LogFileSource.JAVA, numLines);
   				if (contents != null) {
   					logLines.append(contents);
   				}
   				// Next get the servicemix logs
   				contents = getCollectedLogLinesFromInstance(ipAddress, LogFileSource.SMX, numLines);
  				if (contents != null) {
   					logLines.append(contents);
   				}
  				// Get the E3-facility syslog files
   				contents = getCollectedLogLinesFromInstance(ipAddress, LogFileSource.SYSLOG, numLines);
  				if (contents != null) {
   					logLines.append(contents);
   				}  				
  			}
   		}
   		
		return LoggingResponseBuilder.logCollectionToXml(logLines.toString());
	}
	
	/**
	 * Returns the specified number of most-recent log lines from previously-
	 * collected log files for a particular instance and log source.
	 *  
	 * @param ipAddress	The ip-address of the instance
	 * @param logSource	The source for the logs (JAVA, SMX, SYSLOG)
	 * @param numLines	The number of lines to retrieve. Fewer lines may be returned if the 
	 * requested number is not available.
	 * @return	Log lines in XML structure, with a top-level <code>&lt;log&gt;</code> node.
	 */
	public String getCollectedLogLinesFromInstance(String ipAddress, LogFileSource logSource, int numLines)
	{
		StringBuilder logLines = null;
		String logFilePath = null;
		int lineCount = 0;
  		
   		File collectionDir = new File(COLLECTION_PATH, LogCollector.ipToCollectionDirectory(ipAddress));
   		if (!collectionDir.exists() || !collectionDir.isDirectory()) {
   			logger.warn("No log collection directory for ipAddress: {}", ipAddress);
   			return null;
   		}
   		File logSourceSubdir = new File(collectionDir, logSource.toString());
  		if (!logSourceSubdir.exists() || !logSourceSubdir.isDirectory()) {
   			logger.warn("No log-type '{}' collection subdirectory for ipAddress: {}", logSource.toString(), ipAddress);
   			return null;
   		}
  		
   		// Get the list of collected log files in date order
   		File[] files = logSourceSubdir.listFiles(new LogFileFilter(null)); // get all log files, regardless of basename and ext
   		Arrays.sort(files, new Comparator<Object>() {
   			public int compare(Object o1, Object o2) {
   				// Sort by decreasing date first, and then decreasing alphabetical
   				File f1 = (File)o1; File f2 = (File)o2;
   				int result = (Long.valueOf(f2.lastModified())).compareTo(Long.valueOf(f1.lastModified()));
   				if (result == 0) {
   					result = f2.getName().compareTo(f1.getName());
   				}
   				return result;
   			}
   		});
   		logger.trace("Sorted log-files:");
   		for (File logFile : files) {
   			logger.trace("{}", logFile.getName());
   		}
   		logLines = new StringBuilder();
   		try {
   			for (File file : files) {
   				String fileName = file.getName();
   				if(logger.isDebugEnabled()) {
   					logger.debug("Consider file: {}", fileName);
   				}
   				logFilePath = file.getAbsolutePath();
   				if(logger.isDebugEnabled()) {
   					logger.debug("Retrieving {} log lines from file {}", String.valueOf(numLines-lineCount), logFilePath);
   				}
   				String logContent = LogCollector.getTailOfFile(file, numLines - lineCount); 
   				logLines.insert(0, logContent);
   				int retrievedCount = lineCount(logContent);
   				if(logger.isDebugEnabled()) {
   					logger.debug("Actually got {} lines", String.valueOf(retrievedCount));
   				}
   				lineCount += retrievedCount;
   				if (lineCount >= numLines) {
   					break;
   				}
   			}
   		} catch (IOException ex) {
   			// Swallow exception from any one file read and hope to get lines from the next log
   			logger.warn("Couldn't read from log file {}", logFilePath == null ? "(null)" : logFilePath);
   		}  		
   		if(logger.isDebugEnabled()) {
   			logger.debug("Got {} of {} requested lines", String.valueOf(lineCount), String.valueOf(numLines));
   		}
   		
 		return LoggingResponseBuilder.logLinesToXml(logSource, ipAddress, 
			StringEscapeUtils.escapeXml(logLines.toString()));
	}
	
	/**
	 * Collects logs from the localhost for all log sources.
	 * 
	 * @param instanceCollectionDir	Target directory to place collected logs (subdirectories for
	 * 	each log source will be created)
	 * @return The number of bytes in all files collected
	 * </ul>
	 * @throws IOException
	 */
	private long collectAllLocalLogs(File instanceCollectionDir) throws IOException
	{
		// First, get the E3Appender (java) logs
		// Parse the log-config file to find path to logs
		long bytesCollected = 0L;
		String logFilePath = LoggingUtil.getLocalLogFilePath(LogFileSource.JAVA);
		if ((logFilePath == null) || (logFilePath.length() == 0)) {
			// If we can't determine the log-file path from the config file,
			// look anyway in the usual servicemix log directory for any log files with the default name
			logger.warn("Localhost is not using E3Appender, using default log-file path (check log-config file: {})", LoggingUtil.defaultConfigPath);
			logFilePath = LoggingUtil.defaultLogPath;
		}
		if(logger.isDebugEnabled()) {
			logger.debug("java log file path {}", logFilePath);
		}
   		File logFile = new File(logFilePath);
   		bytesCollected += collectLocalLogs(logFile.getParentFile(), instanceCollectionDir, logFile.getName(), LogFileSource.JAVA);
	   	
	   	// Next, get the serviceMix logs
   		logFilePath = LoggingUtil.getLocalLogFilePath(LogFileSource.SMX);
		if ((logFilePath == null) || (logFilePath.length() == 0)) {
			// Same situation as above
			logger.warn("Localhost log-config file ({}) does not specify servicemix log location, using default", LoggingUtil.defaultConfigPath);
			logFilePath = LoggingUtil.defaultSMXLogPath;
		}
		if(logger.isDebugEnabled()) {
			logger.debug("smx log file path {}", logFilePath);
		}
	   	File smxLogFile = new File(logFilePath);
	   	bytesCollected += collectLocalLogs(smxLogFile.getParentFile(), instanceCollectionDir, smxLogFile.getName(), LogFileSource.SMX);
	
	   	// Collect the E3-specific syslog files
	   	logFilePath = NonJavaLogger.getLogFilePath();
	   	if ((logFilePath == null) || (logFilePath.length() == 0)) {
			// Same situation as above
			logFilePath = NonJavaLogger.defaultLogFilePath;
			logger.warn("Localhost syslog-config file ({}) does not specify log location, using default", logFilePath);
		}
	   	if(logger.isDebugEnabled()) {
	   		logger.debug("syslog file path: {}", logFilePath);
	   	}
	   	File syslogFile = new File(logFilePath);
	   	bytesCollected += collectLocalLogs(syslogFile.getParentFile(), instanceCollectionDir, syslogFile.getName(), LogFileSource.SYSLOG);
	   	return bytesCollected;
	}
	
	/**
	 * Collects logs from the specified instance for all log sources.
	 * 
	 * @param logSource	The topology instance to collect logs from
	 * @param instanceCollectionDir	Target directory to place collected logs (subdirectories for
	 * 	each log source will be created)
	 * @return The number of bytes in all files collected
	 * @throws JSchException, IOException
	 */
	private long collectAllRemoteLogs(Instance logSource, Instance logDestination, File instanceCollectionDir)  throws JSchException, IOException
	{
		if (logSource == null) {
			throw new NullPointerException("The instance than contains remote logs cannot be null");
		}
		if (logDestination == null) {
			throw new NullPointerException("The instance where to copy logs cannot be null");
		}

		// First, try to open a new SSH session to instance
		long bytesCollected = 0L;
		String ipAddress = logSource.getInternalIP();
		if(logger.isDebugEnabled()) {
			logger.debug("trying to connect to {} via ssh ...", ipAddress);
		}
		SSHCommand sshCommand = new SSHCommand();
		sshCommand.connect(logDestination.getSSHKey(), ipAddress, 22, logSource.getUser(), logSource.getPassword(), sshSessionTimeout);					
		
		// Start with E3Appender Java logs first
		// Get a local copy of the logging config file to determine log-file path
		String remoteLogPath = null;
		File localConfigFile = new File(instanceCollectionDir, "java-logging.cfg");
		String localConfigFilePath = localConfigFile.getAbsolutePath();
		if (copyRemoteConfigFile(sshCommand, localConfigFilePath, LogFileSource.JAVA)) {
			remoteLogPath = LoggingUtil.getLogFilePathFromConfigFile(localConfigFilePath, LogFileSource.JAVA, false);	
		} else {
			logger.warn("Couldn't retrieve E3 Java logging config file from host {}, will try default path", ipAddress);			
		}
		if ((remoteLogPath == null) || (remoteLogPath.length() == 0)) {
			// If we can't find a logging config file with an E3Appender section,
			// look anyway in the usual servicemix log directory for any log files with the default name
			logger.warn("Instance at {} is not using E3Appender (check log-config file: {})", ipAddress, LoggingUtil.defaultConfigPath);
			remoteLogPath = LoggingUtil.defaultLogPath;
		}
		File localTargetDir = createLocalLogTargetDir(instanceCollectionDir, LogFileSource.JAVA);
		if (localTargetDir == null) {
			logger.warn("Couldn't create log-collection directory: {}", instanceCollectionDir + File.separator + LogFileSource.JAVA.toString());
		} else {
			File remoteLog = new File(remoteLogPath);
			List<FileInfo> logList = getMatchingRemoteFileList(sshCommand, remoteLog.getParent(), remoteLog.getName());
			for (FileInfo remoteFileInfo : logList) {
				File localCopy = new File(localTargetDir, targetNameForLogFile(remoteFileInfo, localTargetDir));
				try {
					bytesCollected += copyRemoteFileWithWorkingTemp(sshCommand, remoteFileInfo, localCopy.getAbsolutePath(), deleteAfterCollect);
				} catch (Exception ex) {
					// Continue copy attempts if we experience an error
					logger.warn("Failed to copy remote file: {} ({})", remoteFileInfo.filePath, ex.getLocalizedMessage());
				}
			}	
		}
		
		// Now try to get the remote serviceMix log files
		// We use the same log-config file as the java logs to parse the path
		remoteLogPath = LoggingUtil.getLogFilePathFromConfigFile(localConfigFilePath, LogFileSource.SMX, false);
		localConfigFile.delete();	// no longer needed
		if ((remoteLogPath == null) || (remoteLogPath.length() == 0)) {
			// If we can't find a logging config file with the proper appender section,
			// look anyway in the usual servicemix log directory for any log files with the default name
			logger.warn("Instance at {} is not using expected appender for servicemix rootLogger (check log-config file: {})", ipAddress, LoggingUtil.defaultConfigPath);
			remoteLogPath = LoggingUtil.defaultSMXLogPath;
		}
		localTargetDir = createLocalLogTargetDir(instanceCollectionDir, LogFileSource.SMX);
		if (localTargetDir == null) { 
			logger.warn("Couldn't create log-collection directory: {}", instanceCollectionDir + File.separator + LogFileSource.SMX.toString());
		} else {
			File remoteLog = new File(remoteLogPath);
			List<FileInfo> logList = getMatchingRemoteFileList(sshCommand, remoteLog.getParent(), remoteLog.getName());
			for (FileInfo remoteFileInfo : logList) {
				File localCopy = new File(localTargetDir, targetNameForLogFile(remoteFileInfo, localTargetDir));
				try {
					bytesCollected += copyRemoteFileWithWorkingTemp(sshCommand, remoteFileInfo, localCopy.getAbsolutePath(), deleteAfterCollect);
				} catch (Exception ex) {
					// Continue copy attempts if we experience an error
					logger.warn("Failed to copy remote file: {} ({})", remoteFileInfo.filePath, ex.getLocalizedMessage());
				}
			}
		}
		
		// Collect the E3-specific syslog files
		// For syslog we parse the rsyslog config file for the log-file path
		localConfigFile = new File(instanceCollectionDir, "syslog.cfg");
		localConfigFilePath = localConfigFile.getAbsolutePath();
		if (copyRemoteConfigFile(sshCommand, localConfigFilePath, LogFileSource.SYSLOG)) {
			remoteLogPath = NonJavaLogger.getLogFilePathFromConfigFile(localConfigFilePath);
			localConfigFile.delete();	
		} else {
			logger.warn("Couldn't retrieve E3 syslog config file from host {}", ipAddress);			
		}
		if ((remoteLogPath == null) || (remoteLogPath.length() == 0)) {
			// Try default path 
			remoteLogPath = NonJavaLogger.defaultLogFilePath;
			logger.warn("Instance at {} does not specify an E3-specific syslog file, trying default: {}", ipAddress, remoteLogPath);
		} 
		localTargetDir = createLocalLogTargetDir(instanceCollectionDir, LogFileSource.SYSLOG);
		if (localTargetDir == null) {
			logger.warn("Couldn't create log-collection directory: {}", instanceCollectionDir + File.separator + LogFileSource.SYSLOG.toString());
		} else {
			File remoteLog = new File(remoteLogPath);
			List<FileInfo> logList = getMatchingRemoteFileList(sshCommand, remoteLog.getParent(), remoteLog.getName());
			for (FileInfo remoteFileInfo : logList) {
				File localCopy = new File(localTargetDir, targetNameForLogFile(remoteFileInfo, localTargetDir));
				try {
					bytesCollected += copyRemoteFileWithWorkingTemp(sshCommand, remoteFileInfo, localCopy.getAbsolutePath(), deleteAfterCollect);
				} catch (Exception ex) {
					// Continue copy attempts if we experience an error
					logger.warn("Failed to copy remote file: {} ({})", remoteFileInfo.filePath, ex.getLocalizedMessage());
				}
			}
		}
		
		// We're done - disconnect and return number of bytes copied
		sshCommand.disconnect();
		if(logger.isDebugEnabled()) {
			logger.debug("connected/disconnected!");
		}
		return bytesCollected;
	}
	
	/**
	 * Visit a particular local directory and collect all the log files that start with
	 * a particular base filename, copying them to the specified target directory.
	 * 
	 * @param sourceDir	The directory in which the log files are located
	 * @param targetDir	The directory to put the copied files
	 * @param baseName	The basename of the log files to collect (such as "e3.log", "servicemix.log", etc)
	 * @param logSource	The type of logs to collect (JAVA, SMX, SYSLOG); the string form of the type will 
	 * 					be used as a destination subdirectory under targetDir
	 * @return The number of bytes in all files collected
	 */
	private long collectLocalLogs(File sourceDir, File targetDir, final String baseName, LogFileSource logSource) 
	{
		long bytesCollected = 0L;
   		
		logger.debug("Collecting logs from localhost from {} with base {}", sourceDir, baseName);
		// New: Get all log files with a matching basename, regardless of rotation type
	 	File[] logFiles = sourceDir.listFiles(new LogFileFilter(baseName));
	 	
	 	if (logFiles == null) {
	 		logger.warn("Error retrieving file list from {} matching name {}", sourceDir, baseName);
	 		return 0L;
	 	}
	 		
	 	// Make or use a specific subdirectory for this log type
	 	targetDir = new File(targetDir, logSource.toString());
	 	if (!targetDir.exists()) {
	 		targetDir.mkdirs();
	 	} else if (!targetDir.isDirectory()) {
	 		logger.error("Target for local log collection is not a directory: {}", targetDir.getAbsolutePath());
	 		return 0L;
	 	}
	 		
   		File logFile;
	 	for (File log : logFiles) {
	 		logFile = log;
	 		if(logger.isDebugEnabled()) {
	 			logger.debug("Copying log file {} ...", logFile.getAbsolutePath());
	 		}
	 		String destFileName = targetNameForLogFile(logFile, targetDir);
	 		File destFile = new File(targetDir, destFileName);
	 		try {
	 			bytesCollected += LogCollector.copyLocalFileWithWorkingTemp(logFile, destFile, deleteAfterCollect);
	 		} catch (IOException ex) {
	 			// Continue copying despite single-file error
				logger.error("Could not copy file {}: {}", logFile.getAbsolutePath() + " to " + targetDir.getAbsolutePath(), ex.getLocalizedMessage());	 				
	 		}
	 	}
		return bytesCollected;
	}
	
	/**
	 * Collects logs from the localhost for all log sources.
	 * 
	 * @param numLines	The requested number of log lines to retrieve.  
	 * Fewer lines may be returned if sufficient log entries are not available. 
	 * @return	An XML-structured string with a <code>&lt;log&gt;</code> node for 
	 * each log-file found..
	 * @throws IOException
	 */
	private String getTailOfLocalActiveLogs(int numLines) throws IOException
	{
		// First, get the E3Appender (java) log
		// Parse the log-config file to find path to log file
		// Note: use TRACE-level logging here since our output may appear in retrieved log lines
		StringBuilder sb = new StringBuilder();
   		String logFilePath = LoggingUtil.getLocalLogFilePath(LogFileSource.JAVA);
		if ((logFilePath == null) || (logFilePath.length() == 0)) {
			// If we can't determine the log-file path from the config file,
			// look anyway in the usual servicemix log directory for any log files with the default name
			logger.warn("Localhost is not using E3Appender, using default log-file path (check log-config file: {})", LoggingUtil.defaultConfigPath);
			logFilePath = LoggingUtil.defaultLogPath;
		}
   		logger.trace("java log file path {}", logFilePath);
   		File logFile = new File(logFilePath);
   		String logLines = execTailOnFile(logFile, numLines);
   		if (logLines != null) {
   			sb.append(LoggingResponseBuilder.logLinesToXml(LogFileSource.JAVA, E3Constant.localhost, StringEscapeUtils.escapeXml(logLines.toString())));
   		}
   		
	   	// Next, get the serviceMix log
   		logFilePath = LoggingUtil.getLocalLogFilePath(LogFileSource.SMX);
		if ((logFilePath == null) || (logFilePath.length() == 0)) {
			// Same situation as above
			logger.warn("Localhost log-config file ({}) does not specify servicemix log location, using default", LoggingUtil.defaultConfigPath);
			logFilePath = LoggingUtil.defaultSMXLogPath;
		}
   		logger.trace("local smx log file path {}", logFilePath);
	   	File smxLogFile = new File(logFilePath);
	   	logLines = execTailOnFile(smxLogFile, numLines);
   		if (logLines != null) {
   			sb.append(LoggingResponseBuilder.logLinesToXml(LogFileSource.SMX, E3Constant.localhost, StringEscapeUtils.escapeXml(logLines.toString())));
   		}
   		
	   	// Collect the E3-specific syslog files
	   	logFilePath = NonJavaLogger.getLogFilePath();
	   	if ((logFilePath == null) || (logFilePath.length() == 0)) {
			// Same situation as above
			logFilePath = NonJavaLogger.defaultLogFilePath;
			logger.warn("Localhost syslog-config file does not specify an E3-specific log location, using default: {}", LoggingUtil.defaultLogPath);
	   	}
	   	logger.trace("local syslog file path: {}", logFilePath);
	   	logFile = new File(logFilePath);
	   	logLines = execTailOnFile(logFile, numLines);
   		if (logLines != null) {
   			sb.append(LoggingResponseBuilder.logLinesToXml(LogFileSource.SYSLOG, E3Constant.localhost, StringEscapeUtils.escapeXml(logLines.toString())));
   		}
   		return sb.toString();
	}

	/**
	 * Retrieves the last <code>numLines</code> lines from the remote machine's active logs.
	 * 
	 * @param numLines	The requested number of log lines to retrieve.  
	 * Fewer lines may be returned if sufficient log entries are not available. 
	 * @return	An XML-structured string with a <code>&lt;log&gt;</code> node for 
	 * each log-file found..
	 * @throws JSchException, IOException
	 */
	private String getTailOfRemoteActiveLogs(Instance logSource, Instance logDestination, int numLines) throws JSchException, IOException
	{
		if (logSource == null) {
			throw new NullPointerException ("Log source cannot be null");
		}
		if (logDestination == null) {
			throw new NullPointerException ("Log destination cannot be null");
		}
		
		StringBuilder sb = new StringBuilder();
		
		// First, try to open a new SSH session to instance
		// Note: use TRACE-level logging here since our output may appear in retrieved log lines
		String ipAddress = logSource.getInternalIP();
		logger.trace("trying to connect to {} via ssh ...", ipAddress);
		
		SSHCommand sshCommand = new SSHCommand();
		sshCommand.connect(logDestination.getSSHKey(), ipAddress, 22, logSource.getUser(), logSource.getPassword(), sshSessionTimeout);					

		// Start with E3Appender Java log first
		// Get a local copy of the logging config file to determine log-file path
		String remoteLogPath = null;
		File localConfigFile = File.createTempFile("java-logging", ".cfg");
		boolean gotConfigFile = false;
		String localConfigFilePath = localConfigFile.getAbsolutePath();
		if (copyRemoteConfigFile(sshCommand, localConfigFilePath, LogFileSource.JAVA)) {
			gotConfigFile = true;
			remoteLogPath = LoggingUtil.getLogFilePathFromConfigFile(localConfigFilePath, LogFileSource.JAVA, false);	
		} else {
			logger.warn("Couldn't retrieve E3 Java logging config file from host {}, will try default path", ipAddress);			
		}
		if ((remoteLogPath == null) || (remoteLogPath.length() == 0)) {
			// If we can't find a logging config file with an E3Appender section,
			// look anyway in the usual servicemix log directory for any log files with the default name
			logger.warn("Instance at {} is not using E3Appender (check log-config file: {})", ipAddress, LoggingUtil.defaultConfigPath);
			remoteLogPath = LoggingUtil.defaultLogPath;
		}
   		logger.trace("java log file path for instance {}: {}", ipAddress, remoteLogPath);
   		String logLines = execTailOnRemoteFile(sshCommand, remoteLogPath, numLines);
   		if (logLines != null) {
   			sb.append(LoggingResponseBuilder.logLinesToXml(LogFileSource.JAVA, ipAddress, StringEscapeUtils.escapeXml(logLines.toString())));
   		}
   		
	   	// Next, get the serviceMix log
   		// We use the same logging config file to get the servicemix log-file path
   		remoteLogPath = null;
   		if (gotConfigFile) {
   			remoteLogPath = LoggingUtil.getLogFilePathFromConfigFile(localConfigFilePath, LogFileSource.SMX, false);
   	   		localConfigFile.delete();	// We're done with the local version of the java/smx config file
   		}
		if ((remoteLogPath == null) || (remoteLogPath.length() == 0)) {
			// Same situation as above
			logger.warn("Could not parse servicemix log-file path from config file for instance at {}, using default", ipAddress);
			remoteLogPath = LoggingUtil.defaultSMXLogPath;
		}
   		logger.trace("smx log file path for instance {}: {}", ipAddress, remoteLogPath);
   		logLines = execTailOnRemoteFile(sshCommand, remoteLogPath, numLines);
   		if (logLines != null) {
   			sb.append(LoggingResponseBuilder.logLinesToXml(LogFileSource.SMX, ipAddress, StringEscapeUtils.escapeXml(logLines.toString())));
   		}
	
	   	// Collect the E3-specific syslog files
		// For syslog we parse the rsyslog config file for the log-file path
   		remoteLogPath = null;
   		localConfigFile = File.createTempFile("syslog", ".cfg");
		localConfigFilePath = localConfigFile.getAbsolutePath();
		if (copyRemoteConfigFile(sshCommand, localConfigFilePath, LogFileSource.SYSLOG)) {
			remoteLogPath = NonJavaLogger.getLogFilePathFromConfigFile(localConfigFilePath);
			localConfigFile.delete();	
		} else {
			logger.warn("Couldn't retrieve E3 syslog config file from host {}", ipAddress);			
		}
	   	if ((remoteLogPath == null) || (remoteLogPath.length() == 0)) {
			// Same situation as above
			logger.warn("Could not parse E3-specific log-file path from syslog config file for instance at {}, using default", ipAddress);
			remoteLogPath = NonJavaLogger.defaultLogFilePath;
	   	}
	   	logger.trace("syslog file path: {}", remoteLogPath);
	   	logLines = execTailOnRemoteFile(sshCommand, remoteLogPath, numLines);
	   	if (logLines != null) {
	   		sb.append(LoggingResponseBuilder.logLinesToXml(LogFileSource.SYSLOG, ipAddress, StringEscapeUtils.escapeXml(logLines.toString())));
	   	}
	   	return sb.toString();
	}

	// Code based on Stack Overflow suggestion:
	// http://stackoverflow.com/questions/686231/java-quickly-read-the-last-line-of-a-text-file
	// Pretty basic: byte-based (no Unicode) and relies on Unix-style EOL 0xA
	private static String getTailOfFile(File file, int numLines) throws FileNotFoundException, IOException
	{
		if (numLines < 0) {
			return null;
		} else if (numLines == 0) {
			return "";
		}
		java.io.RandomAccessFile raFile = new java.io.RandomAccessFile(file, "r");
		long fileLength = file.length() - 1;
		StringBuilder sb = new StringBuilder();
		int line = 0;

		for (long filePointer = fileLength; filePointer >= 0; filePointer--) {
			raFile.seek(filePointer);
			int readByte = raFile.readByte();

			if (readByte == 0xA) {
				if (filePointer < fileLength) {
					line = line + 1;
					if (line >= numLines) {
						break;
					}
				}
			}
			sb.append((char)readByte);
		}

		String lastLines = sb.reverse().toString();
		return lastLines;
	}

	private static String execTailOnFile(File file, int numLines) throws IOException 
	{
		String numLinesArg = String.valueOf(numLines);
		if ((numLines < 0) || (numLinesArg == null) || (numLinesArg.length() == 0) || (file == null) || !file.exists()) {
			return null;
		} else if (numLines == 0) {
			return "";
		}
		ProcessBuilder processBuilder = new ProcessBuilder("/usr/bin/tail", "-n " + numLinesArg, file.getAbsolutePath());
		File workingDirectory = file.getParentFile();
		if (workingDirectory != null) {
			processBuilder.directory(workingDirectory);
		}
		Process p = processBuilder.start();
		
		// Get tail's output: its InputStream
		InputStream is = p.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader reader = new BufferedReader(isr); 
		StringBuilder sb = new StringBuilder();
		String line;

		if (reader != null) {
			try {
				while ((line = reader.readLine()) != null) {
					sb.append(line).append("\n");
				}
			} finally {
				try {
					if (reader != null) {
						reader.close();
					}
					if (is != null) {
						is.close();
					}
				} catch (IOException ioe) {
					// Nothing to do on close exception
				}
			}
		}
		/*		
		 // We only need to wait for p to finish if we want the exit value
		try {
			p.waitFor();
		} catch (InterruptedException ex) {
			logger.warn("Tail on logfile {} interrupted!", file.getAbsoluteFile());
		}
		logger.debug("Tail process exited with code {} ", String.valueOf(p.exitValue()));
		*/
		return sb.toString();
	}

	/**
	 * Count the number of newline characters in a string.
	 * 
	 * @param logLines	The string to parse (assumed to be log lines)
	 * @return	The number of newlines found
	 */
	private static int lineCount(String logLines) 
	{
		return logLines.split(System.getProperty("line.separator")).length;
	}
	
	/**
	 * Traverse the specified topology and return a list of instances.  
	 * Included instance types:
	 * <ul>
	 * <li>E3Gateway
	 * <li>E3GatewayA
	 * <li>E3Manager
	 * <li>E3ManagerA
	 * </ul>
	 * 
	 * @param t	The topology to traverse
	 * @return	A List of instances found in the topology
	 */
	private List<Instance> getInstanceList(ITopology t) 
	{
		List<Instance> instances = new LinkedList<Instance>();
	   	if (t == null) {
	   		logger.warn("topology is null when trying to retrieve instances!");
	   	} else {
	   		instances.addAll(t.getInstancesByType("E3Gateway"));
	   		instances.addAll(t.getInstancesByType("E3Manager"));
	   		instances.addAll(t.getInstancesByType("E3GatewayA"));
	   		instances.addAll(t.getInstancesByType("E3ManagerA"));
	   		// add other types?
	   	}
		return instances; 
	}
	
	/*
	 * Local file operations
	 */
	
	/**
	 * Copy a file from one location to another on the localhost, first moving
	 * (renaming) the source file out of the way of any rotator process, and
	 * then optionally deleting the source file after a successful copy.  
	 * Will attempt to replicate the modification time from the original file.
	 * 
	 * @param sourceFile	File to copy
	 * @param destFile		Destination file
	 * @param deleteSource	If <code>true</code>, will delete original after copy
	 * @return				The number of bytes copied
	 * @throws IOException
	 */
	public static long copyLocalFile(File sourceFile, File destFile, boolean deleteSource) throws IOException 
	{
		long bytesCopied = 0L;
		
		if ((sourceFile == null) || (destFile == null)) { 
			throw new NullPointerException("Source or destination file is null (source: " + sourceFile + ", dest: " + destFile + ")");
		}
		
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		String origSourcePath = sourceFile.getPath();
		File tempFile = new File(tempNameForSourceFile(sourceFile.getPath()));
		FileChannel source = null;
		FileChannel destination = null;
		IOException cleanupException = null;
		boolean success = false;
	
		// Copy and validate result
		try {
			// Rename source file to temporary name before copying
			if(logger.isDebugEnabled()) {
				logger.debug("Renaming local file to: {}", tempFile.getPath());
			}
			if (!sourceFile.renameTo(tempFile)) {
				logger.error("Could not move file to new name: {}", tempFile.getAbsolutePath());
			} else {
				source = new FileInputStream(tempFile).getChannel();
				destination = new FileOutputStream(destFile).getChannel();
				bytesCopied = destination.transferFrom(source, 0, source.size());
				copyModificationTime(tempFile, destFile);
				
				// Check integrity of copy
				success = validateFileCopy(tempFile, destFile);
				if (!success) {
					logger.warn("Copy of file {} did not pass integrity check!", origSourcePath);
				}
			}
		} catch (IOException ex) {
			// If there's been an error copying the file, we may be left with a zero-length or incomplete file
			if (!success) {
				if(logger.isDebugEnabled()) {
					logger.debug("Deleting failed copy of local file: {}", destFile.getAbsolutePath());
				}
				destFile.delete();
			}
		} finally {
			// Use a try-block during cleanup, but only throw exception if the
			// main file-copy try-block doesn't
			try {
				if (source != null) {
					source.close();
				}
				if (destination != null) {
					destination.close();
				}
				if (deleteSource && success) {
					if(logger.isDebugEnabled()) {
						logger.debug("Deleting local source file: {}", tempFile.getAbsolutePath());
					}
					tempFile.delete();
				} else {
					// Move source file back from temp name
					if (tempFile == null || !tempFile.renameTo(new File(origSourcePath))) {
						logger.error("Could not restore original filename: {}", origSourcePath);
					}
				}
			} catch (IOException ex) {
				logger.warn("IOException during local file-copy cleanup: {}", ex);
				cleanupException = new IOException(ex);
			}
		}

		if (cleanupException != null) {
			throw cleanupException;
		}
		return bytesCopied;
	}
	
	/**
	 * Copy the modification time from one local file to another.
	 * 
	 * @param sourceFile	The file to copy the mod time from
	 * @param destFile	The file to copy the mod time to
	 * @throws IOException
	 */
	public static void copyModificationTime(File sourceFile, File destFile) throws IOException
	{
		destFile.setLastModified(sourceFile.lastModified());
	}
	
	private static boolean validateFileCopy(File sourceFile, File destFile) throws IOException
	{
		boolean success;
		long sourceLength = sourceFile.length();
		long destLength = destFile.length();
		if (sourceLength != destLength) { 
			logger.error("File-size difference after copy of file {}: {}", 
					sourceFile.getAbsolutePath(), "orig size: " + String.valueOf(sourceLength) + ", copy size: " + String.valueOf(destLength));
			success = false;
		} else {
			// Check if last lines of files are the same
			String sourceTail = execTailOnFile(sourceFile, 1);
			String destTail = execTailOnFile(destFile, 1);
			success = sourceTail != null && sourceTail.equals(destTail);
			if (!success) {
				logger.error("Last lines of copied files are different: '{}' vs '{}'", sourceTail, destTail);
			}
		}
		
		return success;
	}
	
	/**
	 * Make a copy of a local file, but write first to a "working" temporary file and then
	 * rename the temporary file to the destination name.
	 * 
	 * @param sourceFile	The file to copy
	 * @param destFile	The final destination of the copy
	 * @param deleteSource	If <code>true</code>, will delete original after copy
	 * @return	The number of bytes copied
	 * @throws IOException
	 */
	public static long copyLocalFileWithWorkingTemp(File sourceFile, File destFile, boolean deleteSource) throws IOException 
	{
		File tempLocalFile = new File(tempNameForWorkingFile(destFile.getPath()));
		long bytesCopied = copyLocalFile(sourceFile, tempLocalFile, deleteSource);
		tempLocalFile.renameTo(destFile);
		return bytesCopied;
	}

	/*
	 * Remote file operations
	 */
	
	/**
	 * Copies the appropriate logging-config file for the logSource from the ssh remote host to
	 * the path specified by destFilePath. May search remote host at multiple locations for
	 * config file, or use a default path.
	 * 
	 * @param sshCommand	An connected SSHCommand session
	 * @param destFilePath	The target path for the copy of the config file
	 * @param logSource	The type of log-config file to look for
	 * @return <ul>
	 * 		<li><code>true</code> if an appropriate log-config file was found and copied
	 * 		<li><code>false</code> otherwise
	 * </ul>
	 */
	private static boolean copyRemoteConfigFile(SSHCommand sshCommand, String destFilePath, LogFileSource logSource)
	{
		boolean success = false;
		if (logSource.equals(LogFileSource.JAVA)) {	
			try {
				// We assume there is only one location for the java-logging config file
				String remoteConfigPath = LoggingUtil.defaultConfigPath;
				success = (sshCommand.copyFrom(remoteConfigPath, destFilePath) > 0);
			} catch (Exception ex) {
				// swallow exception since failure means we try default paths, etc
			}
		
		} else if (logSource.equals(LogFileSource.SYSLOG)) {
		
			// There are a couple of pre-defined alternatives for syslog config file paths
			// And NonJavaLogger.getConfigFilePath() could return a non-default value if setConfigFilePath() has been used
			String [] altPaths = NonJavaLogger.getAltConfigFilePaths();
			List<String> pathCandidates = new LinkedList<String>();
			pathCandidates.add(NonJavaLogger.getConfigFilePath());
			pathCandidates.addAll(Arrays.asList(altPaths));
			//boolean foundConfigFile = false;
			for (String pathCand : pathCandidates) {
				String remoteConfigPath = pathCand;	
				try {
					if(logger.isDebugEnabled()) {
						logger.debug("Trying syslog config path: {}", pathCand);
					}
					if (sshCommand.copyFrom(remoteConfigPath, destFilePath) > 0) {
						success = true;
						break;
					}
				} catch (Exception ex) {
					if(logger.isDebugEnabled()) {
						logger.debug("Failed on path {}", remoteConfigPath);
					}
				}
			}			
		} else {
			logger.warn("Invalid log-file type passed to getRemoteLogPath: {}", logSource.toString());
		}
		
		return success;
	}
		
	/**
	 * Creates a local directory to hold collected log files, specific to the log source (JAVA, SMX, SYSLOG)
	 * 
	 * @param instanceCollectionDir	The parent directory for the new directory
	 * @param logSource	The log-file source (determines name of created directory)
	 * @return	A File object representing the new directory, <code>null</code> on error
	 */
	private static File createLocalLogTargetDir(File instanceCollectionDir, LogFileSource logSource) 
	{
		File logTargetDir = new File(instanceCollectionDir, logSource.toString());
		if (logTargetDir.exists()) {
			if (!logTargetDir.isDirectory()) {
				logger.error("Log collection target path is not a directory: {}", logTargetDir.getAbsolutePath());
				return null;
			}
		} else if (!logTargetDir.mkdirs()) {
			logger.error("Could not create log collection target directory: {}", logTargetDir.getAbsolutePath());
		} 
		return logTargetDir;
	}
			
	/**
	 * Searches the contents of the specified remote directory, and returns a list of FileInfo
	 * instances that represent remote files that match the specified basename.
	 * 
	 * @param sshCommand	An active (connected) SSHCommand 
	 * @param remoteDirPath	The path on the remote machine to search for matching files
	 * @param baseName	A base filename to compare against each remote filename (no path) to determine a match
	 * @return	A List of matching files in FileInfo form, empty if no matches were found
	 * @throws JSchException
	 * @throws IOException
	 */
	private static List<FileInfo> getMatchingRemoteFileList(SSHCommand sshCommand, String remoteDirPath, String baseName) throws JSchException, IOException
	{
		if ((sshCommand == null) || !sshCommand.isConnected()) {
			throw new JSchException("Not connected with a valid SSH session");
		}
		String remoteUsername = null;
		if (LogCollector.collectOnlyUserOwnedLogs) {
			remoteUsername = sshCommand.getSessionUsername();
			if (remoteUsername == null) {
				throw new JSchException("Username for SSH session is not valid");	
			}
		}
		// First get a list of *all* files in the remote directory, and then
		// parse the results for a match with the target basename
		// (Prefer to do the matching work locally rather than remotely)
		List<FileInfo> matches = new LinkedList<FileInfo>();
		String findCmd;
		if (LogCollector.collectOnlyUserOwnedLogs) {
			findCmd = "find " + remoteDirPath + " -maxdepth 1 -user " + remoteUsername + " -perm -664 -type f";
		} else {
			findCmd = "find " + remoteDirPath + " -maxdepth 1 -type f";	
		}
		findCmd = findCmd + " -printf '%T@\t%s\t%p\n' 2> /dev/null";
		ShellCommandResult sshResult = sshCommand.execShellCommand(findCmd);
		String[] allFiles = sshResult.getResult().split("\n");
		LogFileFilter baseFileFilter = new LogFileFilter(baseName);
		
		for (String remoteFileItem : allFiles) {
			String[] remoteFileItems = remoteFileItem.split("\t");
			if (remoteFileItems.length != 3) {
				logger.warn("Got remote file entry, but not formatted as expected: {}", remoteFileItem);
			} else {
				String remoteFilePath = remoteFileItems[2];
				File remoteLogFile = new File(remoteFilePath);
				//logger.debug("Consider {} vs {}", remoteLogFile.getName(), filenameRegex);
				if (baseFileFilter.accept(remoteLogFile)) {
					matches.add(new FileInfo(remoteFilePath, remoteFileItems[1], remoteFileItems[0]));
					//logger.debug("{} matches!", remoteLogFile.getName());
				} else {
					logger.trace ("No match between regex '{}' and '{}'", baseName, remoteLogFile.getName());
				}
			}
		}
		
		return matches;
	}

	/**
	 * Formats the filename for a copied log file to enable identification and sorting.
	 * Currently transforms rolling-appender style (".1") file suffixes to
	 * daily-rolling-appender style ("yyyy-MM-dd-HH-ss)".
	 * 
	 * @param logFileInfo	FileInfo structure for log file
	 * @param localTargetDir	The directory where this file will be placed
	 * @return	New filename (no path) for log file
	 */
	private static String targetNameForLogFile(FileInfo logFileInfo, File localTargetDir) 
	{
		// Assume log files have either a rolling file extension (e.g. ".1")
		// or a daily-rolling file extension ("2012-05-01-14-00")
		// Convert log files with rolling extensions to dailyRolling for uniqueness
		File logFile = new File(logFileInfo.filePath);
		String localName = logFile.getName();
		if (localName.matches(LoggingUtil.rollingFilePatternString)) {
			long modTime =  (long) (Double.parseDouble(logFileInfo.fileModtime) * 1000.0);
			Date modDate = new Date(modTime);
			DateFormat format = new SimpleDateFormat(LoggingUtil.minRollingDatePattern);
			String formattedDate = format.format(modDate);
			localName = localName.replaceFirst(LoggingUtil.rollingFilePatternString, "$1." + formattedDate);
			
			// Uniquify in target directory
			// Don't uniquify if we don't delete source logs, since this will result in local duplicates
			if (uniquifyCopiedFilenames) {
				int id = 0;
				File targetFile = new File(localTargetDir, localName);
				boolean uniquified = false;
				while (targetFile.exists()) {
					if (++id > uniquifyLimit) {
						break;
					}
					uniquified = true;
					targetFile = new File(localTargetDir, localName + "." + String.valueOf(id));
				}
				if (uniquified) {
					if(logger.isDebugEnabled()) {
						logger.debug("Uniquified filename {} to {}", logFile.getName(), targetFile.getName());
					}
				}
				localName = targetFile.getName();
			}
			if(logger.isDebugEnabled()) {
				logger.debug("Changed target filename {} to {}", logFile.getName(), localName);
			}
		}
		return localName;
	}

	/**
	 * Formats the filename for a copied log file to enable identification and sorting.
	 * Currently transforms rolling-appender style (".1") file suffixes to
	 * daily-rolling-appender style ("yyyy-MM-dd-HH-ss)".
	 * 
	 * @param logFile	Log-file File object
	 * @param localTargetDir	The directory where this file will be placed
	 * @return	New filename (no path) for log file
	 */
	private static String targetNameForLogFile(File logFile, File localTargetDir) 
	{
		// Assume log files have either a rolling file extension (e.g. ".1")
		// or a daily-rolling file extension ("2012-05-01-14-00")
		// Convert log files with rolling extensions to dailyRolling for uniqueness
		String localName = logFile.getName();
		if (localName.matches(LoggingUtil.rollingFilePatternString)) {
			long modTime =  logFile.lastModified();
			Date modDate = new Date(modTime);
			DateFormat format = new SimpleDateFormat(LoggingUtil.minRollingDatePattern);
			String formattedDate = format.format(modDate);
			localName = localName.replaceFirst(LoggingUtil.rollingFilePatternString, "$1." + formattedDate);
			
			// Uniquify in target directory, requested
			// Don't uniquify if we don't delete the original (source) log file,
			// since we'll end up with duplicates
			if (uniquifyCopiedFilenames) {
				int id = 0;
				File targetFile = new File(localTargetDir, localName);
				while (targetFile.exists()) {
					if (++id > uniquifyLimit) {
						break;
					}
					targetFile = new File(localTargetDir, localName + "." + String.valueOf(id));
					if(logger.isDebugEnabled()) {
						logger.debug("Uniquified filename {} to {}", logFile.getName(), targetFile.getName());
					}
				}
				localName = targetFile.getName();
			}
			if(logger.isDebugEnabled()) {
				logger.debug("Changed target filename {} to {}", logFile.getName(), localName);
			}
		}
		return localName;
	}

	/**
	 * Copies a file on a remote host to a local path. Before copying, renames source file
	 * with a temporary name to move it out of the way of any rotator process.
	 * Optionally deletes the file from the remote host.
	 * 
	 * @param sshCommand	An active (connected) SSHCommand
	 * @param remoteFileInfo	A FileInfo structure representing the remote file
	 * @param localFilePath	The full path for the local file copy
	 * @param deleteSource	If <code>true</code> and the copied succeeds, the remote file will be deleted
	 * @return	The number of bytes copied.
	 * @throws JSchException
	 * @throws IOException
	 */
	private static long copyRemoteFile(SSHCommand sshCommand, FileInfo remoteFileInfo, String localFilePath, boolean deleteSource) throws JSchException, IOException
	{
		String remoteFilePath = remoteFileInfo.filePath;
		String tempRemotePath = tempNameForSourceFile(remoteFilePath);
		File localFile = null;
		long bytesCopied = 0L;
		IOException cleanupIOException = null;
		JSchException cleanupJSchException = null;
		boolean success = false;
		
		// Copy file and validate result
		try {
			// Rename the remote file with a temporary name before copying	
			if(logger.isDebugEnabled()) {
				logger.debug("Renaming remote file to: {}", tempRemotePath);
			}
			sshCommand.execShellCommand("mv " + remoteFilePath + " " + tempRemotePath);

			// Copy the (renamed) remote file to local destination
			remoteFileInfo.filePath = tempRemotePath;	// remote file name is needed during validation
			bytesCopied = sshCommand.copyFrom(tempRemotePath, localFilePath);
			localFile = new File(localFilePath);
			long modTime =  (long) (Double.parseDouble(remoteFileInfo.fileModtime) * 1000.0);
			localFile.setLastModified(modTime);
			
			// Check integrity of copy
			success = validateRemoteFileCopy(sshCommand, remoteFileInfo, localFilePath);
			if (!success) {
				logger.warn("Copy of file {} did not pass integrity check!", remoteFilePath);
			}
		} catch (IOException ex) {
			// If there's been an error copying the file, we may be left with a zero-length or incomplete file
			if ((localFile != null) && localFile.exists() && !success) {
				if(logger.isDebugEnabled()) {
					logger.debug("Deleting failed local copy of remote file: {}", localFile.getAbsolutePath());
				}
				localFile.delete();
			}
		} finally {
			// Use a try-block during cleanup, but only throw exception if the 
			// main file-copy try-block doesn't
			try {
				if (deleteSource && success) {
					if(logger.isDebugEnabled()) {
						logger.debug("Deleting remote file: {}", tempRemotePath);
					}
					sshCommand.execShellCommand("rm " + tempRemotePath);
				} else {
					// Move source file back from temporary name
					sshCommand.execShellCommand("mv " + tempRemotePath + " " + remoteFilePath);
				}
			} catch (JSchException ex) {
				logger.warn("JSchException during remote file copy cleanup: {}", ex);
				cleanupJSchException = new JSchException(ex.getMessage());				
			} catch (IOException ex) {
				logger.warn("IOException during remote file copy cleanup: {}", ex);
				cleanupIOException = new IOException(ex);
			}
			remoteFileInfo.filePath = remoteFilePath;	// restore original file name in argument
		}
	
		if (cleanupJSchException != null) {
			throw cleanupJSchException;
		} else if (cleanupIOException != null) {
			throw cleanupIOException;
		}
		return bytesCopied;
	}
	
	private static boolean validateRemoteFileCopy(SSHCommand sshCommand, FileInfo remoteFileInfo, String localFilePath) throws JSchException, IOException
	{
		boolean success;
		File localFile = new File(localFilePath);
		String remoteFilePath = remoteFileInfo.filePath;
		long remoteLength = Long.parseLong(remoteFileInfo.fileSize);
		long localLength = localFile.length();
		if (localLength != remoteLength) {
			logger.warn("File-size difference after copy of remote log file {}: {}", 
					remoteFilePath, "remote size: " + String.valueOf(remoteLength) + ", local size: " + String.valueOf(localLength));
			success = false;
		} else {
			// Check if last lines of files are the same
			String remoteTail = execTailOnRemoteFile(sshCommand, remoteFilePath, 1);
			String localTail = execTailOnFile(localFile, 1);
			success = remoteTail != null && remoteTail.equals(localTail);
			if (!success) {
				logger.error("Last lines of copied files are different: '{}' vs '{}'", localTail, remoteTail);
			}
		}
		
		return success;
	}
	
	/**
	 * Copies a file on a remote host to a local path, using a temporary local file intermediary. Optionally deletes the file from the remote host.
	 * 
	 * @param sshCommand	An active (connected) SSHCommand
	 * @param remoteFileInfo	A FileInfo structure representing the remote file
	 * @param localFilePath	The full path for the local file copy
	 * @param deleteSource	If <code>true</code> and the copied succeeds, the remote file will be deleted
	 * @return	The number of bytes copied.
	 * @throws JSchException
	 * @throws IOException
	 */
	private static long copyRemoteFileWithWorkingTemp(SSHCommand sshCommand, FileInfo remoteFileInfo, String localFilePath, boolean deleteSource) throws JSchException, IOException
	{
		String tempLocalPath = tempNameForWorkingFile(localFilePath);
		long bytesCopied = copyRemoteFile(sshCommand, remoteFileInfo, tempLocalPath, deleteSource);
		(new File(tempLocalPath)).renameTo(new File(localFilePath));
		return bytesCopied;
	}
	
	private static String execTailOnRemoteFile(SSHCommand sshCommand, String remoteFilePath, int numLines) throws JSchException, IOException
	{
		if ((sshCommand == null) || !sshCommand.isConnected()) {
			throw new JSchException("Not connected with a valid SSH session");
		}
		String numLinesArg = String.valueOf(numLines);
		if ((numLines < 0) || (numLinesArg == null) || (numLinesArg.length() == 0) || (remoteFilePath == null) || (remoteFilePath.length() == 0)) {
			return null;
		} else if (numLines == 0) {
			return "";
		}

		String tailCmd = "/usr/bin/tail -n " + numLinesArg + " " + remoteFilePath;
		ShellCommandResult sshResult = sshCommand.execShellCommand(tailCmd);
		if(logger.isDebugEnabled()) {
			logger.debug("Remote tail process exited with code {}", String.valueOf(sshResult.getExitStatus()));
		}
		String logLines = sshResult.getResult();
		return logLines;
	}
	
	/**
	 * Format an ip address string for use as a directory name.
	 * 
	 * @param ipAddress	The ip address in the usual IPv4 format
	 * @return
	 */
	private static String ipToCollectionDirectory(String ipAddress)
	{
		return ipAddress.replace(".", "_");
	}
	
	/**
	 * Reverses the formatting performed by the <code>ipToCollectionDirectory</code> function.
	 * 
	 * @param dirName	The collection directory name
	 * @return	The name re-formatted as an IPv4 address
	 */
	private static String collectionDirectoryToIP(String dirName) 
	{
		String ip = dirName.replaceAll("^(\\d{1,3})_(\\d{1,3})_(\\d{1,3})_(\\d{1,3})$", "$1\\.$2\\.$3\\.$4");
		return ip;
	}
		
	private static String tempNameForWorkingFile(String fileNameOrPath) 
	{
		return fileNameOrPath + workingFileSuffix;
	}
	
	private static String tempNameForSourceFile(String fileNameOrPath)
	{
		Date modDate = new Date();
		DateFormat format = new SimpleDateFormat(LoggingUtil.minRollingDatePattern);
		String formattedDate = format.format(modDate);
		return fileNameOrPath + ".collected_" + formattedDate;
	}
	
	/**
	 * Log an entire Instance structure.
	 * 
	 * @param instance The Instance to send to the log
	 */
	public static void logInstance(Instance instance) 
	{
		if(logger.isDebugEnabled()) {
			logger.debug("Got instance: {}", instance);
		}
		logger.trace("SSHKeyName: {}", instance.getSSHKeyName());
		logger.trace("User: {}", instance.getUser());
		logger.trace("Password: {}", instance.getPassword());
		logger.trace("Port: {}", instance.getPort());
		logger.trace("Area: {}", instance.getArea());
		logger.trace("SSHKey: {}", instance.getSSHKey());
	}	
	
	/**
	 * Gets a specified number of most-recent log lines from the localhost's
	 * rotated logs.
	 * 
	 * This method has been supplanted by the other log-collection methods in this class.
	 * 
	 * @param numLines
	 * @return
	 * @throws IOException
	 */
	public static String getLocalLogLines(LogFileSource logSource, int numLines) throws IOException 
	{
		if (!LogFileSource.JAVA.equals(logSource) && !LogFileSource.SMX.equals(logSource)) {
			logger.warn("Unexpected log-source value: {}", logSource == null ? "(null)" : logSource);
			return null;
		}
			
		StringBuilder logLines = null;
   		String logFilePath = LoggingUtil.getLocalLogFilePath(logSource);
   		int lineCount = 0;
   		
   		File logDir = (new File(logFilePath)).getParentFile();
   		if (!logDir.exists() || !logDir.isDirectory()) {
   			return null;
   		}
   		
   		// Try getting lines from the current log file(s) ...
   		logLines = new StringBuilder();
   		int logGen = 1;
   		try {
   		   	while (lineCount < numLines) {
   		   		File logFile = new File(logFilePath + "." + String.valueOf(logGen));
   		   		if (!logFile.exists()) {
   					break;
   				}
   				String logContent = getTailOfFile(logFile, numLines - lineCount);
   				logLines.insert(0, logContent);
   				lineCount += lineCount(logContent);
   				logGen++;
   		   	}
		} catch (IOException ex) {
   			logger.warn("Couldn't read from log file {}", logFilePath);
   		}

		return LoggingResponseBuilder.logLinesToXml(LogFileSource.JAVA, E3Constant.localhost, logLines.toString());
	}


}
