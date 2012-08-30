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
package com.alu.e3.auth.camel.producer;

import java.util.Iterator;
import java.util.List;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;

import com.alu.e3.auth.camel.endpoint.AuthEndpoint;
import com.alu.e3.auth.executor.IAuthExecutor;
import com.alu.e3.auth.model.AuthType;
import com.alu.e3.common.camel.AuthIdentity;
import com.alu.e3.common.camel.AuthReport;
import com.alu.e3.common.camel.ExchangeConstantKeys;
import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.gateway.common.camel.exception.GatewayException;
import com.alu.e3.gateway.common.camel.exception.GatewayExceptionCode;
import com.alu.e3.tdr.TDRConstant;
import com.alu.e3.tdr.TDRDataService;

public class AuthProducer extends DefaultProducer {

	private static final CategoryLogger logger = CategoryLoggerFactory.getLogger(AuthProducer.class, Category.AUTH);

	private List<IAuthExecutor> executors;


	public AuthProducer(Endpoint endpoint, List<IAuthExecutor> executors) {
		super(endpoint);
		if(!(endpoint instanceof AuthEndpoint)){
			throw new RuntimeException("AuthProducer does not support endpoint type:"+endpoint.getClass().getName());
		}

		this.executors = executors;
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		
		logger.debug("Authentication Processor is excuted");

		AuthType authType = AuthType.NO_AUTH;
		
		boolean isBasicEnabled = false;
		boolean isAllowed = false;
		boolean isApiActive = false;
		boolean isStatusChecked = false;
		AuthIdentity authIdentity = null;
		
		Iterator<IAuthExecutor> it = executors.iterator();
		while(!isAllowed && it.hasNext()) {
			
			IAuthExecutor executor = it.next();
			AuthReport authReport = executor.checkAllowed(exchange);  
			isAllowed = authReport.isAllowed();
			
			if(!isStatusChecked) {
				isStatusChecked = authReport.isStatusChecked();
				isApiActive = authReport.isApiActive();
			}

			if(!isBasicEnabled) { 
				// Check that BasicAuth method is not enabled
				GatewayExceptionCode code = executor.getErrorCode();
				if(code == GatewayExceptionCode.AUTHORIZATION_BASIC) {
					isBasicEnabled = true;
				}
			}			
			
			if(isAllowed) {
				authIdentity = authReport.getAuthIdentity();
			}
			
			// The last executor
			authType = executor.getType();
		}
		
		
		if(isAllowed) {
			logger.debug("Request allowed to use this Api");
		} else {
			logger.debug("Request not allowed to use this Api");
			manageError(isStatusChecked, isApiActive, isBasicEnabled, exchange);	
		}

		// Put this in the exchange for TDRs
		TDRDataService.setTxTDRProperty(TDRConstant.AUTHENTICATION, authType.value(), exchange);
		
		// Set the authentication result in the exchange
		exchange.setProperty(ExchangeConstantKeys.E3_AUTH_METHOD.toString(), authType.value());				
		exchange.setProperty(ExchangeConstantKeys.E3_AUTH_IDENTITY.toString(), authIdentity);		
	}
	
	private void manageError(boolean isStatusChecked, boolean isApiActive, boolean isBasicEnabled, Exchange exchange) {
		
		GatewayExceptionCode authErrorCode = null;
		String authErrorMessage = null;
		
		if(isStatusChecked) {
			
			if(isApiActive) {
				if(isBasicEnabled) {
					logger.debug("The API is active but an authorization is required");
					authErrorCode = GatewayExceptionCode.AUTHORIZATION_BASIC;
					authErrorMessage = "Authorization required";
				} else {
					logger.debug("The API is active but the request is not allowed");
					authErrorCode = GatewayExceptionCode.AUTHORIZATION;
					authErrorMessage = "Not Authorized";
				}				
				
			} else {
				logger.debug("The API is not active, the request is rejected");
				authErrorCode = GatewayExceptionCode.API_NOT_ACTIVATED;
				authErrorMessage = "API not active";
			}
			
		} else {
			// the status of the API is not trusted, them we reject the call
			logger.debug("The status of the API is not trusted, the request is rejected");
			authErrorCode = GatewayExceptionCode.AUTHORIZATION;
			authErrorMessage = "Not Authorized";
		}

		Exception exception = new GatewayException(authErrorCode, authErrorMessage);
		exchange.setException(exception);			
	}

}
