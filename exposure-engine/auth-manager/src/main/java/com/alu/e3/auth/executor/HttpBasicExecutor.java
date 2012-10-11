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

import org.apache.camel.Exchange;
import org.apache.commons.codec.binary.Base64;

import com.alu.e3.auth.access.IAuthDataAccess;
import com.alu.e3.auth.model.AuthType;
import com.alu.e3.common.camel.AuthReport;
import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.data.model.Api;
import com.alu.e3.gateway.common.camel.exception.GatewayExceptionCode;

public class HttpBasicExecutor implements IAuthExecutor {
	
	private static final CategoryLogger logger = CategoryLoggerFactory.getLogger(HttpBasicExecutor.class, Category.AUTH);
		
	private IAuthDataAccess dataAccess;
	
	public HttpBasicExecutor(IAuthDataAccess dataAcccess){
		this.dataAccess = dataAcccess;
	}
	
	@Override
	public AuthReport checkAllowed(Exchange exchange, Api api) {
		
		AuthReport authReport = new AuthReport();
		
		String authHeader = (String) exchange.getIn().getHeader("Authorization");
		
		if(authHeader != null){
			String[] chunks = authHeader.split(" ");
			
			// Only expect two parts: the auth scheme and the user/pass encoding
			if(chunks.length == 2){
				String scheme = chunks[0];
				if("Basic".equalsIgnoreCase(scheme)){
					String base64 = chunks[1];
					String decoded = new String(Base64.decodeBase64(base64.getBytes()));
					chunks = decoded.split(":");
					if(chunks.length >= 2){
						String user = chunks[0];
						String pass = chunks[1];
						// Checks if the user is allowed to use this service
						authReport = dataAccess.checkAllowed(api, user, pass);
					}
					else{
						if(logger.isDebugEnabled()) {
							logger.debug("Unable to decode user/pass");
						}
						authReport.setBadRequest(true);
					}
				}
				else{
					if(logger.isDebugEnabled()) {
						logger.debug("Auth scheme not Basic ("+scheme+"). Cannot authenticate request");
					}
					authReport.setBadRequest(true);
				}
			}
			else{
				if(logger.isDebugEnabled()) {
					logger.debug("Improperly formed authorization header:"+authHeader);
				}
				authReport.setBadRequest(true);
			}
		}
		else{
			if(logger.isDebugEnabled()) {
				logger.debug("Http Basic Authentication Header is missing");
			}
			authReport.setBadRequest(true);
		}
		
		return authReport;
	}

	@Override
	public AuthType getType() {
		return AuthType.BASIC;
	}

	@Override
	public GatewayExceptionCode getErrorCode() {
		return GatewayExceptionCode.AUTHORIZATION_BASIC;
	}

}
