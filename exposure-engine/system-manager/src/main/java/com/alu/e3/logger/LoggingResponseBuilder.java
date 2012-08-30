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

import com.alu.e3.common.logging.LoggingUtil.LogFileSource;
import com.alu.e3.data.model.LogLevel;

/**
 * A utility class for building Response objects returned by 
 * LoggingManager to REST-API calls.
 * 
 * This is intended as a simple equivalent for 
 * the BasicResponse family found in Provisioning.
 * It will probably be replaced by something more
 * real as SystemManager requires it .... 
 * 
 */

public class LoggingResponseBuilder {

	// Only for SUCCESS status responses
	public static String createResponseContent(String result)
	{
		if (result != null) {
	    	return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><response><status>SUCCESS</status>" +
	    			result + "</response>";			
		} else {
			return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><response><status>SUCCESS</status></response>";
		}
	}
	
	public static String logLevelToXml(LogLevel logLevel) 
	{ 
		if ((logLevel == null) || (logLevel.getLevel() == null)) {
			return "<logLevel><level /></logLevel>"; 
		} else {
			return "<logLevel><level>" + logLevel.toString() + "</level></logLevel>";
		}
	}

	public static String syslogLevelToXml(LogLevel logLevel)
	{
		if ((logLevel == null) || (logLevel.getSyslogLevel() == null)) {
			return "<logLevel><level /></logLevel>"; 
		} else {
			return "<logLevel><level>" + logLevel.getSyslogLevel() + "</level></logLevel>";
		}
		
	}

	public static String syslogLevelToXml(String syslogLevel)
	{
		if (syslogLevel == null) {
			return "<logLevel><level /></logLevel>"; 
		} else {
			return "<logLevel><level>" + syslogLevel + "</level></logLevel>";
		}
		
	}
	
	public static String logLinesToXml(LogFileSource logType, String ipAddress, String logLines)
	{
		StringBuilder xml = new StringBuilder();
		xml.append("<log>");
		xml.append("<source>" + logType.toString() + "</source>");
		if (ipAddress != null) {
			xml.append("<ipAddress>" + ipAddress + "</ipAddress>");
		} else {
			xml.append("<ipAddress />");
		}
		if ((logLines != null) && (logLines.length() > 0)) {
			xml.append("<lines><li>" + logLines.replaceFirst("\\n$", "").replaceAll("\\n", "</li><li>") + "</li></lines>");
		}
		xml.append("</log>");
		return xml.toString();
	}

	public static String logCollectionToXml(String xmlLogs)
	{
		StringBuilder collection = new StringBuilder();
		collection.append("<logCollection>");
		if (xmlLogs != null) {
			collection.append(xmlLogs);
		}
		collection.append("</logCollection>");
		return collection.toString();
	}
}
