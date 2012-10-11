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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.cxf.jaxrs.ext.Description;

import com.alu.e3.Utilities;
import com.alu.e3.common.caching.IEntryListener;
import com.alu.e3.common.logging.LoggingUtil;
import com.alu.e3.common.logging.LoggingUtil.LogFileSource;
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.common.logging.NonJavaLogger;
import com.alu.e3.logger.LogCollector;
import com.alu.e3.data.DataEntryEvent;
import com.alu.e3.data.IDataManagerListener;
import com.alu.e3.data.model.LogLevel;
import com.alu.e3.osgi.api.ITopology;


/**
 * The LoggingManager class provides a REST-API interface for controlling log levels
 * and retrieving collected log lines.  It also provides functions for setting
 * global log levels by broadcasting log-level updates via the DataManager.
 * 
 * Important note:
 * 
 * The Log Collector is currently not run either by a Timer or by a Spring
 * scheduler.  It is only run by a call to the "forceLogCollection" REST-API
 * method.  This will be changed shortly ....
 * 
 */

@Path("/system-manager/logging")
public class LoggingManager implements IDataManagerListener, IEntryListener<String, LogLevel> {

	/* Final Members. */
	private static final CategoryLogger logger = CategoryLoggerFactory.getLogger(LoggingManager.class, Category.LOG);


	/* Members */
	private IDataManager dataManager;
	private boolean dataManagerReady;
	private ExecutorService collectionExecutor;	
	private Future<Long> collectionResult;
	private ITopology topology;

	
	/**
	 * Default Constructor
	 */
	private LoggingManager() 
	{
		dataManagerReady = false;
	}
	
	public void init() 
	{
		dataManager.addListener(this);
	}
	
	/*
	 * IEntryListener implementation
	 */

	/**
	 * Property setter
	 * 
	 * @param dataManager	The new IDataManager reference
	 */
	public void setDataManager(IDataManager dataManager) 
	{
		// set dataManager before adding as listener, otherwise, dataManagerReady might fail!
		this.dataManager = dataManager;
		dataManagerReady = false;
	}
	
	/**
	 * Called by DataManager when it's ready (has loaded all its tables)
	 */
	@Override
	public void dataManagerReady() 
	{
		if(logger.isDebugEnabled()) {
			logger.debug("DataManager ready, registering as Log-Level Listener");
		}
		dataManagerReady = true;
		this.dataManager.addLogLevelListener(this);
	}
			
	/**
	 * Called by Spring magic 
	 */
	public void destroy() 
	{
		if(logger.isDebugEnabled()) {
			logger.debug("Destroying, removing as listener");
		}

		dataManagerReady = false;
		if (dataManager != null) {
			dataManager.removeListener(this);
			dataManager.removeLogLevelListener(this);
		}
	}

	/**
	 * Sets the static topology that represents the current system
	 * topology, assumed to be updated on changes.  Set by spring.
	 * 
	 * @param topology	The current system topology.
	 */
	public void setTopology(ITopology topology)
	{
		this.topology = topology;
	}

	/*
	 * IEntryListener implementation
	 */
	
	/**
	 * The (single) log level value has been added.
	 */
	@Override
	public void entryAdded(DataEntryEvent<String, LogLevel> event) 
	{
		// The manager does nothing here (except perhaps verify)
		// The loggingClient does the actual work of changing log level
	}


	/**
	 * The log entry has been updated.
	 */
	@Override
	public void entryUpdated(DataEntryEvent<String, LogLevel> event) 
	{
		// The manager does nothing here (except perhaps verify)
		// The loggingClient does the actual work of changing log level
	}


	/**
	 * A log entry has been removed, this should probably not happen
	 */
	@Override
	public void entryRemoved(DataEntryEvent<String, LogLevel> event) 
	{
 	}

	/**
	 *  Currently, we do not handle instanceId parameters as intended.
	 *  (This is deferred until there is some mechanism for callers
	 *  to get instanceIds from a Topology object or client.)
	 *  We have left the instanceId-parameter
	 *  form of the REST-API calls in for future use, but for now
	 *  we interpret instanceIds only as ints where 0 means "all instances"
	 *  and anything else means localhost.
	 *  
	 *  @param instanceId	ID of instance to query
	 */
	private boolean isGlobalInstanceId(String instanceId)
	{
		boolean globalLevel = true;
    	if (instanceId != null) {
    		try {
    			int instanceInt = Integer.parseInt(instanceId);
    			globalLevel = (instanceInt == 0);
    		} catch (NumberFormatException ex){
    			// ignore malformed instanceId, and assume globalLevel
    			if(logger.isDebugEnabled()) {
    				logger.debug("Got exception when trying to convert: {}", instanceId);
    			}
    		}
    	}
    	return globalLevel;
	}
	
    /**
     * The REST method for getting the set of logging categories and the enabled status for each.
     */
    @GET
    @Path("/loggingCategories")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "REST API to retrieve the set of logging categories and the enable/disable status for each.")
    public Response restGetLoggingCategories() 
    {    	
    	if(logger.isDebugEnabled()) {
    		logger.debug("rest-api call to getLoggingCategories");
    	}
    	CategoryResponse response = new CategoryResponse(CategoryResponse.SUCCESS);
    	List<CategoryWrapper> categoryList = CategoryListWrapper.fromDataModel(Arrays.asList(Category.values()));
    	response.setCategories(categoryList);
    	ResponseBuilder builder = Response.ok(response);
    	return builder.build();
    }

    /**
     * The REST method for setting the set of logging categories and the enabled status for each.
     */
    @PUT
    @Path("/loggingCategories")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	@Description(value = "REST API to put the set of logging categories and the enable/disable status for each.")
    public Response restPutLoggingCategories(CategoryListWrapper categoryListWrapper) 
    {    	
    	if(logger.isDebugEnabled()) {
    		logger.debug("rest-api call to putLoggingCategories");
    	}
    	// The only exception possible is an IllegalArgumentException when trying to convert an illegal Category (enum) name
    	try {
    		// Just calling toDataModel on the Category list will set values for this (manager) instance
    		List<Category> requestCategories = CategoryListWrapper.toDataModel(categoryListWrapper);
    		if ((this.dataManager == null) || !dataManagerReady) {
    			logger.error("call to put loggingCategories but DataManager is null or not ready!");
    		} else {
    			for (Category c : requestCategories) {
    				// Set the shared values explicitly
    				if(logger.isDebugEnabled()) {
    					logger.debug("Requested category: {}: {}", c.name(), c.fullname() + ", " + c.description() + ", " + c.enabled());
    				}
    				this.dataManager.setLoggingCategory(c, c.enabled());
    			}
    		}
  
    	} catch (Exception ex) {
			ex.printStackTrace(System.err);
			String result = "PutLoggingCategories error: \n\n";
			result += ex.getMessage() + "\n";
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
		}
    	
		CategoryResponse response = new CategoryResponse(CategoryResponse.SUCCESS);
		List<CategoryWrapper> categoryList = CategoryListWrapper.fromDataModel(Arrays.asList(Category.values()));
		response.setCategories(categoryList);
		ResponseBuilder builder = Response.ok(response);
    	return builder.build();
    }
    
    /**
     * The REST method for getting the global log level.
     */
    @GET
    @Path("/logLevel")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "REST API to retrieve the global java log level.")
    public Response restGetLogLevel() 
    {
    	LogLevel logLevel = null;
    	if(logger.isDebugEnabled()) {
    		logger.debug("rest-api call to getLogLevel");
    	}
    	try {
			// The parameterless (no instanceId) version could get either the
			// local or the global log level.
			// For now, it gets the global level.
    		logLevel = getGlobalLogLevel();
    	} catch (Exception e) {
			e.printStackTrace(System.err);

			String result = "getLogLevel\n\n";
			result += e.getMessage();
			result += "\n\n";
			result += Utilities.getStackTrace(e);

			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
		}
    	
    	String content = LoggingResponseBuilder.createResponseContent(LoggingResponseBuilder.logLevelToXml(logLevel));
    	if(logger.isDebugEnabled()) {
    		logger.debug("LogLevel response: {}", content);
    	}
    	return Response.ok(content, MediaType.APPLICATION_XML_TYPE).build();
    }

    /**
     * The REST method for getting the log level for a particular
     * instance.
     * 
     * Since callers do not currently have a way of retrieving instance-ids,
     * this function only handles two cases:
     * <ul>
     * <li> instanceId == 0: return the global log level
     * <li> instanceId != 0: return the log level for this instance (localhost)
     * </ul>
     * 
     * @param instanceId	The ID for the instance to query (see note above)
     */
    @GET
    @Path("/instances/{instanceId}/logLevel")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "REST API to retrieve the java log level for an instance (1 for localhost, 0 for global).")
    public Response restGetInstanceLogLevel(@PathParam("instanceId") String instanceId) 
    {
    	LogLevel logLevel = null;
    	if(logger.isDebugEnabled()) {
    		logger.debug("rest-api call to getInstanceLogLevel with instanceId: {}", (instanceId == null ? "(null)" : instanceId));
    	}
    	try {
    		logLevel = isGlobalInstanceId(instanceId) ? getGlobalLogLevel() : getLocalLogLevel();		
    	} catch (Exception e) {
			e.printStackTrace(System.err);

			String result = "getLogLevel for instanceId: " + instanceId + "\n\n";
			result += e.getMessage();
			result += "\n\n";
			result += Utilities.getStackTrace(e);

			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
		}
    	
      	String content = LoggingResponseBuilder.createResponseContent(LoggingResponseBuilder.logLevelToXml(logLevel));
      	if(logger.isDebugEnabled()) {
      		logger.debug("LogLevel response: {}", content);
      	}
    	return Response.ok(content, MediaType.APPLICATION_XML_TYPE).build();
    }
        
   /**
    * The REST method for putting the global log level.  This value is placed
    * in the DataManager cache and distributed to all instances, where
    * each LoggingClient will pick it up and set the local log level.
    * (This is also true for this SystemManager instance.)
    * 
    * @param level	A valid log4j log level string (OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL)
    */
    @PUT
    @Path("/logLevel")
	@Produces({ MediaType.APPLICATION_XML })
	//@Consumes({ MediaType.TEXT_PLAIN })
	@Description(value = "REST API to set the global java log level.")
    public Response restSetLogLevel(String level) 
    {
    	if(logger.isDebugEnabled()) {
    		logger.debug("rest-api call to setLogLevel with level = {}", level != null ? level : "(null)");
    	}
    	LogLevel logLevel = null;
    	try {
    		if (LogLevel.isValidLogLevel(level)) {
    			logLevel = new LogLevel(level);
    			// The parameterless (no instanceId) version could set either the
    			// local or the global log level.
    			// For now, it sets the global level.
    			setGlobalLogLevel(logLevel);
    		} else {
    			if(logger.isDebugEnabled()) {
    				logger.debug("Call to REST setLogLevel with invalid log level string: {}", level);
    			}
    			String result = "Invalid logLevel '" + level + "' - must be one of " + LogLevel.Log4JLevel.valuesList;
    			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(result).build();
    		}
		
    	} catch (Exception e) {
			e.printStackTrace(System.err);

			String result = "setLogLevel " + logLevel + "\n\n";
			result += e.getMessage();
			result += "\n\n";
			result += Utilities.getStackTrace(e);

			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
		}
    	
      	String content = LoggingResponseBuilder.createResponseContent(LoggingResponseBuilder.logLevelToXml(logLevel));
      	if(logger.isDebugEnabled()) {
      		logger.debug("LogLevel response: {}", content);
      	}
    	return Response.ok(content, MediaType.APPLICATION_XML_TYPE).build();
    }
       
    /**
     * The REST method for setting the log level for a particular
     * instance.
     * 
     * Since callers do not currently have a way of retrieving instance-ids,
     * this function only handles two cases:
     * <ul>
     * <li> instanceId == 0: set the global log level
     * <li> instanceId != 0: set the log level for this instance (localhost)
     * </ul>
     * 
     * @param instanceId	The ID of the instance for which to change the log level (see note above)
     * @param level			A valid log4j log level string (OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL)
     */
    @PUT
    @Path("/instances/{instanceId}/logLevel")
	@Produces({ MediaType.APPLICATION_XML })
	//@Consumes({ MediaType.TEXT_PLAIN })
	@Description(value = "REST API to set the java log level for an instance (1 for localhost, 0 for global).")
    public Response restSetInstanceLogLevel(@PathParam("instanceId") String instanceId, String level) 
    {
    	logger.debug("rest-api call to setInstanceLogLevel with instanceId = {}, level = {}", 
    			instanceId != null ? instanceId : "(null)", level != null ? level : "(null)"); 
    	LogLevel logLevel = null;
    	try {
    		if (LogLevel.isValidLogLevel(level)) {
    			logLevel = new LogLevel(level);
    			if (isGlobalInstanceId(instanceId)) {
    				setGlobalLogLevel(logLevel);
    			} else {
    				setLocalLogLevel(logLevel);
    			}
    		} else {
    			if(logger.isDebugEnabled()) {
    				logger.debug("Call to REST setLogLevel with invalid log level string: {}", level);
    			}
    			String result = "Invalid logLevel '" + level + "' - must be one of " + LogLevel.Log4JLevel.valuesList;
    			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(result).build();    			
    		}
		
    	} catch (Exception e) {
			e.printStackTrace(System.err);

			String result = "setLogLevel " + logLevel + "\n\n";
			result += e.getMessage();
			result += "\n\n";
			result += Utilities.getStackTrace(e);

			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
		}
    	
      	String content = LoggingResponseBuilder.createResponseContent(LoggingResponseBuilder.logLevelToXml(logLevel));
      	if(logger.isDebugEnabled()) {
      		logger.debug("LogLevel response: {}", content);
      	}
    	return Response.ok(content, MediaType.APPLICATION_XML_TYPE).build();
    }

    /**
     * The REST method for getting the global servicemix log level.
     */
    @GET
    @Path("/smxlogLevel")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "REST API to retrieve the global servicemix log level.")
    public Response restGetSMXLogLevel() 
    {
    	LogLevel logLevel = null;
    	if(logger.isDebugEnabled()) {
    		logger.debug("rest-api call to getSMXLogLevel");
    	}
    	try {
			// The parameterless (no instanceId) version could get either the
			// local or the global log level.
			// For now, it gets the global level.
    		logLevel = getGlobalSMXLogLevel();
    	} catch (Exception e) {
			e.printStackTrace(System.err);

			String result = "getSMXLogLevel\n\n";
			result += e.getMessage();
			result += "\n\n";
			result += Utilities.getStackTrace(e);

			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
		}
    	
    	String content = LoggingResponseBuilder.createResponseContent(LoggingResponseBuilder.logLevelToXml(logLevel));
    	if(logger.isDebugEnabled()) {
    		logger.debug("SMXLogLevel response: {}", content);
    	}
    	return Response.ok(content, MediaType.APPLICATION_XML_TYPE).build();
    }

    /**
     * The REST method for getting the servicemix log level for a particular
     * instance.
     * 
     * Since callers do not currently have a way of retrieving instance-ids,
     * this function only handles two cases:
     * <ul>
     * <li> instanceId == 0: return the global log level
     * <li> instanceId != 0: return the log level for this instance (localhost)
     * </ul>
     * 
     * @param instanceId	The ID for the instance to query (see note above)
     */
    @GET
    @Path("/instances/{instanceId}/smxlogLevel")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "REST API to retrieve the servicemix log level for an instance (1 for localhost, 0 for global).")
    public Response restGetInstanceSMXLogLevel(@PathParam("instanceId") String instanceId) 
    {
    	LogLevel logLevel = null;
    	if(logger.isDebugEnabled()) {
    		logger.debug("rest-api call to getInstanceSMXLogLevel with instanceId: {}", (instanceId == null ? "(null)" : instanceId));
    	}
    	try {
    		logLevel = isGlobalInstanceId(instanceId) ? getGlobalSMXLogLevel() : getLocalSMXLogLevel();		
    	} catch (Exception e) {
			e.printStackTrace(System.err);

			String result = "getSMXLogLevel for instanceId: " + instanceId + "\n\n";
			result += e.getMessage();
			result += "\n\n";
			result += Utilities.getStackTrace(e);

			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
		}
    	
      	String content = LoggingResponseBuilder.createResponseContent(LoggingResponseBuilder.logLevelToXml(logLevel));
      	if(logger.isDebugEnabled()) {
      		logger.debug("SMXLogLevel response: {}", content);
      	}
    	return Response.ok(content, MediaType.APPLICATION_XML_TYPE).build();
    }
        
   /**
    * The REST method for putting the global servicemix log level.  This value is placed
    * in the DataManager cache and distributed to all instances, where
    * each LoggingClient will pick it up and set the local log level.
    * (This is also true for this SystemManager instance.)
    * 
    * @param level	A valid log4j log level string (OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL)
    */
    @PUT
    @Path("/smxlogLevel")
	@Produces({ MediaType.APPLICATION_XML })
	//@Consumes({ MediaType.TEXT_PLAIN })
	@Description(value = "REST API to set the global servicemix log level.")
    public Response restSetSMXLogLevel(String level) 
    {
    	if(logger.isDebugEnabled()) {
    		logger.debug("rest-api call to setSMXLogLevel with level = {}", level != null ? level : "(null)");
    	}
    	LogLevel logLevel = null;
    	try {
    		if (LogLevel.isValidLogLevel(level)) {
    			logLevel = new LogLevel(level);
    			// The parameterless (no instanceId) version could set either the
    			// local or the global log level.
    			// For now, it sets the global level.
    			setGlobalSMXLogLevel(logLevel);
    		} else {
    			if(logger.isDebugEnabled()) {
    				logger.debug("Call to REST setSMXLogLevel with invalid log level string: {}", level);
    			}
    			String result = "Invalid logLevel '" + level + "' - must be one of " + LogLevel.Log4JLevel.valuesList;
    			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(result).build();
    		}
		
    	} catch (Exception e) {
			e.printStackTrace(System.err);

			String result = "setSMXLogLevel " + logLevel + "\n\n";
			result += e.getMessage();
			result += "\n\n";
			result += Utilities.getStackTrace(e);

			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
		}
    	
      	String content = LoggingResponseBuilder.createResponseContent(LoggingResponseBuilder.logLevelToXml(logLevel));
      	if(logger.isDebugEnabled()) {
      		logger.debug("SMXLogLevel response: {}", content);
      	}
    	return Response.ok(content, MediaType.APPLICATION_XML_TYPE).build();
    }
       
    /**
     * The REST method for setting the servicemix log level for a particular
     * instance.
     * 
     * Since callers do not currently have a way of retrieving instance-ids,
     * this function only handles two cases:
     * <ul>
     * <li> instanceId == 0: set the global log level
     * <li> instanceId != 0: set the log level for this instance (localhost)
     * </ul>
     * 
     * @param instanceId	The ID of the instance for which to change the log level (see note above)
     * @param level			A valid log4j log level string (OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL)
     */
    @PUT
    @Path("/instances/{instanceId}/smxlogLevel")
	@Produces({ MediaType.APPLICATION_XML })
	//@Consumes({ MediaType.TEXT_PLAIN })
	@Description(value = "REST API to set the servicemix log level for an instance (1 for localhost, 0 for global).")
    public Response restSetInstanceSMXLogLevel(@PathParam("instanceId") String instanceId, String level) 
    {
    	logger.debug("rest-api call to setInstanceSMXLogLevel with instanceId = {}, level = {}", 
    			instanceId != null ? instanceId : "(null)", level != null ? level : "(null)"); 
    	LogLevel logLevel = null;
    	try {
    		if (LogLevel.isValidLogLevel(level)) {
    			logLevel = new LogLevel(level);
    			if (isGlobalInstanceId(instanceId)) {
    				setGlobalSMXLogLevel(logLevel);
    			} else {
    				setLocalSMXLogLevel(logLevel);
    			}
    		} else {
    			if(logger.isDebugEnabled()) {
    				logger.debug("Call to REST setSMXLogLevel with invalid log level string: {}", level);
    			}
    			String result = "Invalid logLevel '" + level + "' - must be one of " + LogLevel.Log4JLevel.valuesList;
    			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(result).build();    			
    		}
		
    	} catch (Exception e) {
			e.printStackTrace(System.err);

			String result = "setSMXLogLevel " + logLevel + "\n\n";
			result += e.getMessage();
			result += "\n\n";
			result += Utilities.getStackTrace(e);

			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
		}
    	
      	String content = LoggingResponseBuilder.createResponseContent(LoggingResponseBuilder.logLevelToXml(logLevel));
      	if(logger.isDebugEnabled()) {
      		logger.debug("SMXLogLevel response: {}", content);
      	}
    	return Response.ok(content, MediaType.APPLICATION_XML_TYPE).build();
    }

    /**
     * The REST method for getting the global syslog level.
     */
    @GET
    @Path("/syslogLevel")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "REST API to retrieve the global syslog level.")
    public Response restGetSyslogLevel() 
    {
    	if(logger.isDebugEnabled()) {
    		logger.debug("rest-api call to getSyslogLevel");
    	}
    	String syslogLevel = null;
    	try {
    		syslogLevel = getGlobalSyslogLevel();
    	} catch (Exception e) {
			e.printStackTrace(System.err);

			String result = "getGlobalSyslogLevel\n\n";
			result += e.getMessage();
			result += "\n\n";
			result += Utilities.getStackTrace(e);

			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
		}
    	
      	String content = LoggingResponseBuilder.createResponseContent(LoggingResponseBuilder.syslogLevelToXml(syslogLevel));
      	if(logger.isDebugEnabled()) {
      		logger.debug("LogLevel response: {}", content);
      	}
    	return Response.ok(content, MediaType.APPLICATION_XML_TYPE).build();
    }

   /**
     * The REST method for getting the syslog level for a particular
     * instance.
     * 
     * Since callers do not currently have a way of retrieving instance-ids,
     * this function only handles two cases:
     * <ul>
     * <li> instanceId == 0: return the global syslog level
     * <li> instanceId != 0: return the syslog level for this instance (localhost)
     * </ul>
     * 
     * @param instanceId	The ID for the instance to query (see note above)
     */
    @GET
    @Path("/instances/{instanceId}/syslogLevel")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "REST API to retrieve the syslog level for an instance (1 for localhost, 0 for global).")
    public Response restGetInstanceSyslogLevel(@PathParam("instanceId") String instanceId) 
    {
    	String syslogLevel = null;
    	if(logger.isDebugEnabled()) {
    		logger.debug("rest-api call to getInstanceSyslogLevel with instanceId: {}", (instanceId == null ? "(null)" : instanceId));
    	}
    	try {
    		syslogLevel = isGlobalInstanceId(instanceId) ? getGlobalSyslogLevel() : getLocalSyslogLevel();		
    	} catch (Exception e) {
			e.printStackTrace(System.err);

			String result = "getSyslogLevel for instanceId: " + instanceId + "\n\n";
			result += e.getMessage();
			result += "\n\n";
			result += Utilities.getStackTrace(e);

			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
		}
    	
      	String content = LoggingResponseBuilder.createResponseContent(LoggingResponseBuilder.syslogLevelToXml(syslogLevel));
      	if(logger.isDebugEnabled()) {
      		logger.debug("SyslogLevel response: {}", content);
      	}
    	return Response.ok(content, MediaType.APPLICATION_XML_TYPE).build();
    }

    /**
     * The REST method for setting the global syslog level.  This value is placed
     * in the DataManager cache and distributed to all instances, where
     * each LoggingClient will pick it up and set the local syslog level.
     * (This is also true for this SystemManager instance.)
     * 
     * @param syslogLevel	A valid syslog level string (EMERG, ALERT, CRIT, ERR, WARNING, NOTICE, INFO, DEBUG)
     */
    @PUT
    @Path("/syslogLevel")
	@Produces({ MediaType.APPLICATION_XML })
	//@Consumes({ MediaType.TEXT_PLAIN })
	@Description(value = "REST API to set the global syslog level.")
    public Response restSetSyslogLevel(String syslogLevel) 
    {
    	logger.debug("rest-api call to setGlobalSyslogLevel with level = {}", syslogLevel != null ? syslogLevel : "(null)"); 
    	String validatedLevel = null;
   		if (LogLevel.isValidSyslogLevel(syslogLevel)) {
   			validatedLevel = syslogLevel;
   		} 
   		if (validatedLevel == null) {
   			if(logger.isDebugEnabled()) {
   				logger.debug("Call to REST setSyslogLevel with invalid syslog level string: {}", syslogLevel);
   			}
			String result = "Invalid syslog level '" + syslogLevel + "' - must be one of " + LogLevel.SyslogLevel.valuesList;
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(result).build();
		}
   		try {
    		setGlobalSyslogLevel(validatedLevel);
    	} catch (Exception e) {
			e.printStackTrace(System.err);

			String result = "setGlobalSyslogLevel " + syslogLevel + "\n\n";
			result += e.getMessage();
			result += "\n\n";
			result += Utilities.getStackTrace(e);

			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
		}
    	
      	String content = LoggingResponseBuilder.createResponseContent(LoggingResponseBuilder.syslogLevelToXml(validatedLevel));
      	if(logger.isDebugEnabled()) {
      		logger.debug("SyslogLevel response: {}", content);
      	}
    	return Response.ok(content, MediaType.APPLICATION_XML_TYPE).build();
    }
    
     /**
     * The REST method for setting the syslog level for a particular
     * instance.
     * 
     * Since callers do not currently have a way of retrieving instance-ids,
     * this function only handles two cases:
     * <ul>
     * <li> instanceId == 0: set the global syslog level
     * <li> instanceId != 0: set the log syslevel for this instance (localhost)
     * </ul>
     * 
     * @param instanceId	The ID of the instance for which to change the syslog level (see note above)
     * @param level			A valid syslog level string (EMERG, ALERT, CRIT, ERR, WARNING, NOTICE, INFO, DEBUG)
     */
    @PUT
    @Path("/instances/{instanceId}/syslogLevel")
	@Produces({ MediaType.APPLICATION_XML })
	//@Consumes({ MediaType.TEXT_PLAIN })
	@Description(value = "REST API to set the syslog level for an instance (1 for localhost, 0 for global).")
    public Response restSetInstanceSyslogLevel(@PathParam("instanceId") String instanceId, String syslogLevel) 
    {
    	logger.debug("rest-api call to setInstanceSysogLevel with instanceId = {}, level = {}", 
    			instanceId != null ? instanceId : "(null)", syslogLevel != null ? syslogLevel : "(null)"); 
    	String validatedLevel = null;
   		if (LogLevel.isValidSyslogLevel(syslogLevel)) {
   			validatedLevel = syslogLevel;
   		} 
   		if (validatedLevel == null) {
   			if(logger.isDebugEnabled()) {
   				logger.debug("Call to REST setSyslogLevel with invalid syslog level string: {}", syslogLevel);
   			}
			String result = "Invalid syslog level '" + syslogLevel + "' - must be one of " + LogLevel.SyslogLevel.valuesList;
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(result).build();
		}
    	try {
    		if (isGlobalInstanceId(instanceId)) {
    			setGlobalSyslogLevel(validatedLevel);
    		} else {
    			setLocalSyslogLevel(validatedLevel);
    		}		
    	} catch (Exception e) {
			e.printStackTrace(System.err);

			String result = "setSyslogLevel " + syslogLevel + "\n\n";
			result += e.getMessage();
			result += "\n\n";
			result += Utilities.getStackTrace(e);

			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
		}
    	
      	String content = LoggingResponseBuilder.createResponseContent(LoggingResponseBuilder.syslogLevelToXml(validatedLevel));
      	if(logger.isDebugEnabled()) {
      		logger.debug("SyslogLevel response: {}", content);
      	}
    	return Response.ok(content, MediaType.APPLICATION_XML_TYPE).build();
    }

    /** 
     * A REST method for triggering a log collection operation.  This method
     * is provided for debugging and testing: the actual log collector will
     * run on a periodic timer.
     */
    @GET
    @Path("/forceLogCollection")
    @Consumes({MediaType.WILDCARD})
    @Produces({MediaType.APPLICATION_XML})
    @Description(value = "REST API to trigger log collection across all instances in the topology.")
    public Response forceLogCollection()
    {
    	if(logger.isDebugEnabled()) {
    		logger.debug("rest-api call to forceLogCollection");
    	}
    	try {
    		startLogCollection();
    	} catch (Exception e) {
			String result = "forceLogCollection\n\n";
			result += e.getMessage();
			result += "\n\n";
			result += Utilities.getStackTrace(e);

			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
    	}
 	    String content = LoggingResponseBuilder.createResponseContent(null);
    	return Response.ok(content, MediaType.APPLICATION_XML_TYPE).build();
    }
    
    /** 
     * A REST method for retrieving the latest <code>numLines</code> lines from
     * the collected logs.
     * 
     * @param numLines	A positive int in string form
     */
    @GET
    @Path("/collectedLogLines/{numLines}")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "REST API to retrieve a specified number of log lines from the collected (rotated) logs.")
    public Response restGetLogLines(@PathParam("numLines") String numLines) 
    {
    	logger.debug("rest-api call to collectedLogLines/{numLines} with numLines = {}", 
    			numLines == null ? "(null)" : numLines);
    	int linesRequested = 0;
   		try {
			linesRequested = Integer.parseInt(numLines);
		} catch (NumberFormatException ex){
			if(logger.isDebugEnabled()) {
				logger.debug("Got exception when trying to convert: {}", numLines);
			}
			String result = "Invalid {numLines} parameter: " + numLines;
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(result).build();    			
		}

   		// Reading already-collected log lines doesn't actually
   		// require a current topology (could be used for reading lines
   		// from specific instances, however)
 		LogCollector logCollector = new LogCollector(this.topology);
 		String logs = logCollector.getCollectedLogLines(linesRequested);
 	    String content = LoggingResponseBuilder.createResponseContent(logs);
    	return Response.ok(content, MediaType.APPLICATION_XML_TYPE).build();
    }

    /** 
     * A REST method for retrieving the latest <code>numLines</code> lines from
     * the collected logs.
     * 
     * @param numLines	A positive int in string form
     */
    @GET
    @Path("/activeLogLines/{numLines}")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "REST API to retrieve a specified number of log lines from the active log files.")
    public Response restGetActiveLogLines(@PathParam("numLines") String numLines) 
    {
    	logger.debug("rest-api call to activeLogLines/{numLines} with numLines = {}", 
    			numLines == null ? "(null)" : numLines);
   	int linesRequested = 0;
   		try {
			linesRequested = Integer.parseInt(numLines);
		} catch (NumberFormatException ex){
			if(logger.isDebugEnabled()) {
				logger.debug("Got exception when trying to convert: {}", numLines);
			}
			String result = "Invalid {numLines} parameter: " + numLines;
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(result).build();    			
		}

  		LogCollector logCollector = new LogCollector(this.topology);
 		String logs = logCollector.collectAllActiveLogs(linesRequested);
 	    String content = LoggingResponseBuilder.createResponseContent(logs);
    	return Response.ok(content, MediaType.APPLICATION_XML_TYPE).build();
    }

	/**
	 * Returns the global log level from the DataManager's cache.
	 *
	 * @return A valid log4j LogLevel (OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL)
	 * if a global log-level is found in the DataManager cache,
	 * or <code>null</code> if not.
	 */
	public LogLevel getGlobalLogLevel()
	{
		LogLevel logLevel = null;
		// Get the system-wide log level value (in the cache)
		if ((this.dataManager == null) || !dataManagerReady) {
			logger.error("call to shared getLogLevel but DataManager is null or not ready!");
		} else {
			logLevel = dataManager.getLogLevel();
			logger.debug("retrieved cached log-level value: {}", (logLevel == null ? "(null)" : logLevel.toString()));
		}
		return logLevel;
	}
	
	/**
	 * Returns the local java log level (for localhost).
	 * 
	 * @return A valid log4j LogLevel (OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL)
	 * if an E3Appender specification is found in the local logging config file,
	 * or <code>null</code> if not.
	 */
	public LogLevel getLocalLogLevel() throws IOException
	{
		// Get the local log value (in the config file)
		return LoggingUtil.getLocalLogLevel(LogFileSource.JAVA);
	}

	/**
	 * Returns the global servicemix log level from the DataManager's cache.
	 *
	 * @return A valid log4j LogLevel (OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL)
	 * if a global log-level is found in the DataManager cache,
	 * or <code>null</code> if not.
	 */
	public LogLevel getGlobalSMXLogLevel()
	{
		LogLevel logLevel = null;
		// Get the system-wide log level value (in the cache)
		if ((this.dataManager == null) || !dataManagerReady) {
			logger.error("call to shared getSMXLogLevel but DataManager is null or not ready!");
		} else {
			logLevel = dataManager.getSMXLogLevel();
			logger.debug("retrieved cached smxlog-level value: {}", (logLevel == null ? "(null)" : logLevel.toString()));
		}
		return logLevel;
	}

	/**
	 * Returns the local servicemix log level (for localhost).
	 * 
	 * @return A valid log4j LogLevel (OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL)
	 * if a rootLogger appender specification is found in the local logging config file,
	 * or <code>null</code> if not.
	 */
	public LogLevel getLocalSMXLogLevel() throws IOException
	{
		// Get the local log value (in the config file)
		return LoggingUtil.getLocalLogLevel(LogFileSource.SMX);
	}

	/**
	 * Returns the global syslog level from the DataManager's cache.
	 *
	 * @return A valid syslog level string (EMERG, ALERT, CRIT, ERR, WARNING, NOTICE, INFO, DEBUG)
	 * if a global log-level is found in the DataManager cache,
	 * or <code>null</code> if not.
	 */
	public String getGlobalSyslogLevel()
	{
		String syslogLevel = null;
		// Get the system-wide syslog level value (in the cache)
		if ((this.dataManager == null) || !dataManagerReady) {
			logger.error("call to shared getLogLevel but DataManager is null or not ready!");
		} else {
			LogLevel logLevel = dataManager.getSyslogLevel();
			logger.debug("retrieved cached log-level value: {}", (logLevel == null ? "(null)" : logLevel.toString()));
			if (logLevel != null) {
				syslogLevel = logLevel.getSyslogLevel().name();
			}
		}
		return syslogLevel;
	}

	/**
	 * Returns the local syslog level specific to the E3 facility,
	 * if one is set in the local syslog config file.
	 * 
	 * @return	A valid syslog level string (EMERG, ALERT, CRIT, ERR, WARNING, NOTICE, INFO, DEBUG),
	 * or <code>null</code> if no E3-specific level can be determined.
	 * @throws IOException
	 */
	public String getLocalSyslogLevel() throws IOException
	{
		return NonJavaLogger.getLogLevel();
	}
	
	/**
	 * Sets the global log level by putting new logLevel in shared
	 * (DataManager) cache and letting the LoggingClient for
	 * each instance pick up and set the new level.
	 * 
	 * @param logLevel A valid log4j LogLevel (OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL)
	 */
	public void setGlobalLogLevel(LogLevel logLevel)
	{
		if ((logLevel == null)  || (logLevel.getLevel() == null)) {
			logger.warn("setGlobalLogLevel called with null level!");
			return;
		}
		logger.debug("setGlobalLogLevel with level: {}", logLevel.toString());

   		// Set the system-wide log level (in the cache)
		// We'll set our local log level when we pick up the cache change
		if ((this.dataManager == null) || !dataManagerReady) {
			logger.error("call to shared setLogLevel but DataManager is null or not ready!");
		} else {
			logger.debug("setting cached log-level value to: {}", logLevel.toString());
			dataManager.setLogLevel(logLevel);
		}
	}

	/**
	 * Sets the log level for this instance.
	 * 
	 * @param logLevel	A valid log4j LogLevel (OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL)
	 * @throws IOException 
	 */
	public void setLocalLogLevel(LogLevel logLevel) throws IOException
	{
		if ((logLevel == null)  || (logLevel.getLevel() == null)) {
			logger.warn("setLocalLogLevel called with null level!");
			return;
		}
		logger.debug("setLocalLogLevel with level: {}", logLevel.toString());		
		LoggingUtil.setLocalJavaLogLevel(logLevel);
	}

	/**
	 * Sets the global servicemix log level by putting new smxlogLevel 
	 * in the shared (DataManager) cache and letting the LoggingClient for
	 * each instance pick up and set the new level.
	 * 
	 * @param logLevel A valid log4j LogLevel (OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL)
	 */
	public void setGlobalSMXLogLevel(LogLevel logLevel)
	{
		if ((logLevel == null)  || (logLevel.getLevel() == null)) {
			logger.warn("setGlobalSMXLogLevel called with null level!");
			return;
		}
		logger.debug("setGlobalSMXLogLevel with level: {}", logLevel.toString());

   		// Set the system-wide log level (in the cache)
		// We'll set our local log level when we pick up the cache change
		if ((this.dataManager == null) || !dataManagerReady) {
			logger.error("call to shared setSMXLogLevel but DataManager is null or not ready!");
		} else {
			logger.debug("setting cached smxlog-level value to: {}", logLevel.toString());
			dataManager.setSMXLogLevel(logLevel);
		}
	}

	/**
	 * Sets the servicemix log level for this instance.
	 * 
	 * @param logLevel	A valid log4j LogLevel (OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL)
	 * @throws IOException 
	 */
	public void setLocalSMXLogLevel(LogLevel logLevel) throws IOException
	{
		if ((logLevel == null)  || (logLevel.getLevel() == null)) {
			logger.warn("setLocalSMXLogLevel called with null level!");
			return;
		}
		logger.debug("setLocalSMXLogLevel with level: {}", logLevel.toString());
		LoggingUtil.setLocalSMXLogLevel(logLevel);
	}

	/**
	 * Sets the global syslog level by putting a new logLevel in shared
	 * (DataManager) cache and letting the LoggingClient for
	 * each instance pick up and set the new level.
	 * 
	 * @param logLevel A valid syslog level string (EMERG, ALERT, CRIT, ERR, WARNING, NOTICE, INFO, DEBUG)
	 */
	public void setGlobalSyslogLevel(String syslogLevel)
	{
		if ((syslogLevel == null) || !LogLevel.isValidSyslogLevel(syslogLevel))  {
			logger.warn("setGlobalSyslogLevel called with invalid syslog level: {}", syslogLevel == null ? "(null)" : syslogLevel);
			return;
		}
		logger.debug("setGlobalSyslogLevel with level: {}", syslogLevel);

   		// Set the system-wide syslog level (in the cache)
		// LoggingClient will set our local syslog level when it picks up the cache change
		if ((this.dataManager == null) || !dataManagerReady) {
			logger.error("call to shared setSyslogLevel but DataManager is null or not ready!");
		} else {
			LogLevel equivLevel = new LogLevel(syslogLevel);
			dataManager.setSyslogLevel(equivLevel);			
			logger.debug("setting cached syslog-level value to: {} (requested {})", equivLevel.toString(), syslogLevel);
		}
	}

	/**
	 * Attempts to set the syslog level for the E3 facility on the localhost.
	 * Calls out to the local loggingClient to actually change the syslog settings.
	 * 
	 * @param level	A valid syslog level string (EMERG, ALERT, CRIT, ERR, WARNING, NOTICE, INFO, DEBUG)
	 * @throws IOException
	 */
	public void setLocalSyslogLevel(String level) throws IOException
	{
		if ((level == null)  || !LogLevel.isValidSyslogLevel(level)) {
			logger.warn("setLocalSyslogLevel called with invalid level string: {}", 
					level == null ? "(null)" : level);
			return;
		}

		// See note at top of NonJavaLogger.java about necessary permissions to change syslog level!
		logger.debug("setLocalSylogLevel with level: {}", level);
    	NonJavaLogger.setLogLevel(level); 
	}

	/**
	 * Fire off a log-collection operation.  This function is called both
	 * by the REST-API for asynchronous calls and by the spring task
	 * scheduler to run periodic log collection.
	 * 
	 * If another log collection operation is currently running, the
	 * new collection execution will return (almost) immediately
	 * after handling a TimeoutException on trying to obtain the LogCollector write lock. 
	 */
	public void startLogCollection() 
	{
		logger.debug("Running log collector from LoggingManager ...");
		if ((collectionExecutor != null) && (collectionResult != null)) {
			if (!collectionResult.isDone() && !collectionResult.isCancelled()) {
				logger.debug("Log Collector is still active - delaying new collection ...");
				return;
			}
		}
		
		LogCollector logCollector = new LogCollector(this.topology);
		collectionExecutor = Executors.newSingleThreadExecutor();
		collectionResult = collectionExecutor.submit(logCollector);
	}

	/**
	 * General Setters
	 */
	public void setCollectionExecutor(ExecutorService collectionExecutor) {
		this.collectionExecutor = collectionExecutor;
	}

	public void setCollectionResult(Future<Long> collectionResult) {
		this.collectionResult = collectionResult;
	}

}
