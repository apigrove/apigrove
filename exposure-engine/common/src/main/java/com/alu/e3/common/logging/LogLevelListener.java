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

import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;

import com.alu.e3.common.caching.IEntryListener;
import com.alu.e3.common.logging.LoggingUtil.LogFileSource;
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.data.DataEntryEvent;
import com.alu.e3.data.IDataManagerListener;
import com.alu.e3.data.model.LogLevel;

/**
 * The LogLevelListener listens for changes to the global logLevel value (made by system-manager.LoggingManager)
 * and updates the local log level appropriately.
 * 
 */

public class LogLevelListener implements IDataManagerListener, IEntryListener<String, LogLevel> {

	private static final CategoryLogger logger = CategoryLoggerFactory.getLogger(LogLevelListener.class, Category.LOG);
		
	protected IDataManager dataManager;
	
	/**
	 * Default Constructor
	 */
	private LogLevelListener() {}

	/*
	 * IDataManagerListener 
	 */
	
	/**
	 * Property setter, auto-wired for
	 * automatic fulfillment by with netbean
	 * 
	 * @param dataManager	The new IDataManager reference
	 */
	@Autowired
	public void setDataManager(IDataManager dataManager) 
	{
		this.dataManager = dataManager;		
	}
	
	/**
	 * Called by DataManager when it's ready (has loaded all its tables)
	 */
	@Override
	public void dataManagerReady() 
	{
		logger.debug("DataManager ready, registering as Log-Level Listener");
		this.dataManager.addLogLevelListener(this);
	}
	
	public void init() 
	{
		dataManager.addListener(this);
	}
	
	public void destroy() 
	{
		logger.debug("Destroying, removing as listener");

		if (this.dataManager != null) {
			this.dataManager.removeListener(this);
			this.dataManager.removeLogLevelListener(this);
		}
	}

	/*
	 * IEntryListener 
	 */
	
	/**
	 * The (single) log level value has been added.
	 */
	@Override
	public void entryAdded(DataEntryEvent<String, LogLevel> event) 
	{
		// The handling for adding and updating an entry are the same ...
		if (event != null) {
			logger.trace("[LogLevelEntryAdded] key: {}", event.getKey());
			handleLogLevelEvent(event);
			logger.trace("[LogEntryAdded] Finished handling log-level event: {}", event.getKey());
		}
	}


	/**
	 * The log entry has been updated.
	 */
	@Override
	public void entryUpdated(DataEntryEvent<String, LogLevel> event) 
	{
		// The handling for adding and updating an entry are the same ...
		if (event != null) {
			logger.trace("[LogLevelEntryUpdated] key: {}", event.getKey());
			handleLogLevelEvent(event);
			logger.trace("[LogEntryUpdated] Finished handling log event for ip: {}", event.getKey());
		}
	}


	/**
	 * A log entry has been removed, this should probably not happen
	 */
	@Override
	public void entryRemoved(DataEntryEvent<String, LogLevel> event) 
	{
		if (event != null) {
			logger.trace("[LogEntryRemoved] for ip: {}", event.getKey());
		}
 	}

	/**
	 * Performs appropriate processing for an add or update with the LogLevel DataEntryEvent.
	 * 
	 * @param event	A LogLevel-specific event from the DataManager
	 */
	private void handleLogLevelEvent(DataEntryEvent<String, LogLevel> event) {
		String eventKey = event.getKey();
		LogLevel logLevel = event.getValue();
		if ((eventKey == null) || (logLevel == null)) {
			return;
		}
		// TODO:
		// The logLevelParameter idea is not yet implemented, but will be the way we will
		// distinguish instance-specific set-level and get-level events
		// (To be implemented when the concept of InstanceIDs is sorted out)
		String[] keyAndParam = eventKey.split(":", 2);
		String key = keyAndParam.length > 0 ? keyAndParam[0] : "";
		String param = keyAndParam.length > 1 ? keyAndParam[1] : "";
		logger.debug("Parsed cached log-level event key: '{}' and param: '{}'", key, param);
		if (key.equals(LogLevel.logLevelKey)) {
			logger.debug("New java log-level notification: ({}, {})", eventKey, event.getValue());
			logger.debug("Setting local java log-level to new (global) value: {}", logLevel.toString());
			try {
				LoggingUtil.setLocalLogLevel(LogFileSource.JAVA, logLevel);
			} catch (Exception ex) {
				logger.error("Unable to set logLevel from cache: {}", logLevel.toString());
			}
		} else if (key.equals(LogLevel.smxlogLevelKey)) {
			logger.debug("New smxlog-level notification: ({}, {})", eventKey, event.getValue());
			logger.debug("Setting local smxlog-level to new (global) value: {}", logLevel.toString());
			try {
				LoggingUtil.setLocalLogLevel(LogFileSource.SMX, logLevel);
			} catch (Exception ex) {
				logger.error("Unable to set smxlogLevel from cache: {}", logLevel.toString());
			}
		} else if (key.equals(LogLevel.syslogLevelKey)) {
			logger.debug("New syslog-level notification: ({}, {})", eventKey, event.getValue());
			String syslogLevel = logLevel.getSyslogLevel().name();
			logger.debug("Setting local syslog-level to new (global) value: {}", syslogLevel);
			try {
				NonJavaLogger.setLogLevel(syslogLevel);
			} catch (Exception ex) {
				logger.error("Unable to set syslogLevel from cache: {}", syslogLevel);
			}
		} else {
			// Check if this is a logging-category specification
			Category category = Category.fromString(key);
			if (category != null) {
				logger.debug("New category-enabled notification: ({}, {})", eventKey, event.getValue());
				// If the log-level value is log4j OFF, then the category is disabled; else, category is enabled
				if (logLevel.getLevel().equals(Level.OFF)) {
					logger.debug("Disabling logging category: {}", category);
					Category.enableCategory(category, false);
				} else {
					logger.debug("Enabling logging category: {}", category);
					Category.enableCategory(category, true);
				}
			} else {
				logger.warn("Did not recognize log-level dataManager event key: {} (value: {})", eventKey, event.getValue());
			}
		}
	}	
}
