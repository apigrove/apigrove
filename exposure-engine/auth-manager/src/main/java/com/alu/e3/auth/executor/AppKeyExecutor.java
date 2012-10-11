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
package com.alu.e3.auth.executor;

import java.util.Map;

import org.apache.camel.Exchange;

import com.alu.e3.auth.access.DataAccessRuntimeException;
import com.alu.e3.auth.access.IAuthDataAccess;
import com.alu.e3.auth.model.AuthType;
import com.alu.e3.common.camel.AuthReport;
import com.alu.e3.common.camel.ExchangeConstantKeys;
import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.data.model.Api;
import com.alu.e3.gateway.common.camel.exception.GatewayExceptionCode;

/**
 * class AppKeyAuthProtocol
 */
public class AppKeyExecutor implements IAuthExecutor{
	
	private static final CategoryLogger logger = CategoryLoggerFactory.getLogger(AppKeyExecutor.class, Category.AUTH);
	
	private String keyName = "";
	private String headerName = "";
	private IAuthDataAccess dataAccess; 
	
	/**
	 * Constructor
	 */
	public AppKeyExecutor(String keyname, String headerName, IAuthDataAccess dataAccess) {
		this.keyName = keyname;
		this.headerName = headerName;
		this.dataAccess = dataAccess;
	}
	
	/**
	 * Look for the value in the http query and removes it.
	 * @param exchange
	 * @param value
	 */
	private void removeHttpQueryValue(Exchange exchange, String value) {
		String query = exchange.getIn().getHeader(Exchange.HTTP_QUERY, String.class);
		
		if (query != null) {
		
			if (query.contains("&"+value)) {
				query = query.replace("&"+value, "");
			}
			else if (query.contains(value+"&")) {
				query = query.replace(value+"&", "");
			}
			else if (query.contains(value)) {
				// on its own
				query = null;
			}
			
			exchange.getIn().setHeader(Exchange.HTTP_QUERY, query);
			// if we use the following line instead then we lose all the other header values
			//exchange.getOut().setHeader(Exchange.HTTP_QUERY, query);	
		}
	}
	
	@Override
	public AuthReport checkAllowed(Exchange exchange, Api api) {
		
		AuthReport authReport = new AuthReport();
		Object keyObj = null;
		
		Map<?, ?> parameters = exchange.getProperty(ExchangeConstantKeys.E3_REQUEST_PARAMETERS.toString(), Map.class);
		if (parameters == null) {
			if(logger.isDebugEnabled()) {
				logger.debug("Request parameters not set");
			}
			authReport.setBadRequest(true);
		} else {
		
			keyObj = parameters.get(keyName);
			if (keyObj == null) { // No parameter by keyName, checking for a header "headerName"
				keyObj = exchange.getIn().getHeader(headerName, String.class); 
				if (keyObj == null) {		
					// Abort
					if(logger.isDebugEnabled()) {
						logger.debug("Unable to find url parameter or header matching the provisioned api key name");
					}
					authReport.setBadRequest(true);
				}
			}
		}
		
		// if not a bad request
		if(!authReport.isBadRequest()) {
		
			String authKey = keyObj.toString();
			if(logger.isDebugEnabled()) {
				logger.debug("authKey= " + authKey);
			}
			
			// remove the credential from URL
			removeHttpQueryValue(exchange, keyName+"="+authKey);
			
			// remove the credential from Header
			exchange.getIn().removeHeader(headerName);
	
			//Call AuthManager
			try {
				authReport = dataAccess.checkAllowed(api, authKey);
			} catch(DataAccessRuntimeException e) {
					logger.error("Data Access Issue", e);
			} catch(Exception e) {
					logger.error("Data Access Issue", e);
			}
		}

		return authReport;
	}

	@Override
	public AuthType getType() {
		return AuthType.AUTHKEY;
	}

	@Override
	public GatewayExceptionCode getErrorCode() {
		return GatewayExceptionCode.AUTHORIZATION;
	}

}
