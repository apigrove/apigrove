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
package com.alu.e3.gateway.loadbalancer;

import java.util.Map;

import org.apache.camel.Exchange;
import org.slf4j.Logger;

public class LoadBalancerDisplay {
	
	public static void logDebug(Logger logger, String name, String message) {
		if(logger.isDebugEnabled()) {
			logger.debug(name + " - " + message);
		}
	}
	
	public static void logError(Logger logger, String name, String message, Exception e) {
		if(logger.isErrorEnabled()) {
			logger.error(name + " - " + message, e);
		}
		
	}
	
	public static void logError(Logger logger, String name, String message) {
		if(logger.isErrorEnabled()) {
			logger.error(name + " - " + message);
		}		
	}
	
	public static void logWarn(Logger logger, String name, String message) {
		if(logger.isWarnEnabled()) {
			logger.error(name + " - " + message);
		}
		
	}
	
	public static void displayExchange(Logger logger, String name, Exchange exchange, String label) {
		if(logger.isDebugEnabled()) {
			logDebug(logger, name, ">>>>>>>>>>>>> "+ label);
			logDebug(logger, name, "Exchange ID = " + exchange.getExchangeId());
			logDebug(logger, name, "-------------");
			logDebug(logger, name, "Properties  = " + getMapAsString(exchange.getProperties()));
			logDebug(logger, name, "IN  Header  = " + getMapAsString(exchange.getIn().getHeaders()));
			logDebug(logger, name, "IN  Body    = " + getObjectAsString(exchange.getIn().getBody()));
			logDebug(logger, name, "OUT Header  = " + getMapAsString(exchange.getOut().getHeaders()));
			logDebug(logger, name, "OUT Body    = " + getObjectAsString(exchange.getOut().getBody()));		
			logDebug(logger, name, "=============");
		}
}


	public static void displayConfig(Logger logger, String name, int maxAttemptsExceeded, boolean failedOver, int failedOverErrorCode, int numberOfReferences) {
		if(logger.isDebugEnabled()) {
			logDebug(logger, name, "============= CONFIG =============");
			logDebug(logger, name, "Max Attempts  = " + maxAttemptsExceeded);
			logDebug(logger, name, "Failover  = " + failedOver);
			logDebug(logger, name, "Failover Error Code  = " + failedOverErrorCode);
			logDebug(logger, name, "Number of Target Host  = " + numberOfReferences);
			logDebug(logger, name, "===================================");
		}
	}

	private static String getObjectAsString(Object object) {
		
		String objectAsString = null;
		
		if(object != null) {
			try {
				objectAsString = (String) object;
			} catch(ClassCastException e) {
				objectAsString = object.toString();
			}
		} else {
			objectAsString = "";
		}
		
		return objectAsString;
		
	}
	
	private static String getMapAsString(Map<String, Object> map) {
		
		String mapAsString = null;
		StringBuffer buffer = new StringBuffer();
		
		for(Map.Entry<String, Object> entry : map.entrySet()) {
			buffer.append(entry.getKey() + "=" + entry.getValue() + "&");
		}
		
		mapAsString = buffer.toString();
		
		if(mapAsString.endsWith("&")) {
			mapAsString = mapAsString.substring(0,mapAsString.length()-1);
		}
		
		return mapAsString;
	}
}
