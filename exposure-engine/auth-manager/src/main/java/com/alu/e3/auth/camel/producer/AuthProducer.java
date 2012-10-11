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
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.data.model.Api;
import com.alu.e3.gateway.common.camel.exception.GatewayException;
import com.alu.e3.gateway.common.camel.exception.GatewayExceptionCode;
import com.alu.e3.tdr.TDRConstant;
import com.alu.e3.tdr.TDRDataService;

public class AuthProducer extends DefaultProducer {

	private static final CategoryLogger logger = CategoryLoggerFactory.getLogger(AuthProducer.class, Category.AUTH);

	private List<IAuthExecutor> executors;
	private String apiId;
	private IDataManager dataManager;

	public AuthProducer(Endpoint endpoint, List<IAuthExecutor> executors, IDataManager dataManager, String apiId) {
		super(endpoint);
		if(!(endpoint instanceof AuthEndpoint)){
			throw new RuntimeException("AuthProducer does not support endpoint type:"+endpoint.getClass().getName());
		}

		this.apiId = apiId;
		this.executors = executors;
		this.dataManager = dataManager;
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		
		if(logger.isDebugEnabled()) {
			logger.debug("Authentication Processor is excuted");
		}

		Api api = (Api)exchange.getProperty(ExchangeConstantKeys.E3_API.toString());
		if(api == null) {
			api = dataManager.getApiById(apiId);
		}

		AuthType authType = AuthType.NO_AUTH;
		
		boolean isAllowed = false;
		AuthIdentity authIdentity = null;
		AuthReport report = null;
		AuthType reportAuth = null;
		
		Iterator<IAuthExecutor> it = executors.iterator();
		while(!isAllowed && it.hasNext()) {
			
			IAuthExecutor executor = it.next();
			AuthReport authReport = executor.checkAllowed(exchange, api);  
			isAllowed = authReport.isAllowed();
			
			if (isAllowed) {
				authIdentity = authReport.getAuthIdentity();
				report = authReport;
			} else {
				if (report == null) {
					report = authReport;
					reportAuth = executor.getType();
				} else {
					if (report.compareTo(authReport) > 0) {
						report = authReport;
						reportAuth = executor.getType();
					}
				}
			}
			// The last executor
			authType = executor.getType();
		}
		
		if (isAllowed) {
			if(logger.isDebugEnabled()) {
				logger.debug("Request allowed to use this Api");
			}
		} else {
			if(logger.isDebugEnabled()) {
				logger.debug("Request not allowed to use this Api");
			}
			handleReport(exchange, report, reportAuth);
		}

		// Put this in the exchange for TDRs
		TDRDataService.setTxTDRProperty(TDRConstant.AUTHENTICATION, authType.value(), exchange);
		
		// Set the authentication result in the exchange
		exchange.setProperty(ExchangeConstantKeys.E3_AUTH_METHOD.toString(), authType.value());	
		
		//getting apiContext
		String value = null;
		if(authIdentity != null && authIdentity.getAuth() != null){
				value = authIdentity.getAuth().getApiContext();
		}
		
		exchange.setProperty(ExchangeConstantKeys.E3_AUTH_IDENTITY.toString(), authIdentity);
		exchange.setProperty(ExchangeConstantKeys.E3_AUTH_IDENTITY_APICONTEXT.toString(), value);	
	}

	private void handleReport(Exchange exchange, AuthReport report, AuthType authType) {	
		GatewayExceptionCode authErrorCode = null;
		String authErrorMessage = null;
		
		if (logger.isDebugEnabled()) {
			logger.debug("AuthType: {}; {}", authType, report);
		}
		if ((report != null) && report.isStatusChecked()) {
			if (report.isApiActive()) {
				if (report.isAuthActive()) {
					if(logger.isDebugEnabled()) {
						logger.debug("The API is active but an authorization is required");
					}
					authErrorCode = authType.authErrorCode();
					authErrorMessage = authType.authErrorMessage();
				} else if (!report.isAuthNotFound()) {
					if(logger.isDebugEnabled()) {
						logger.debug("The Auth {} is not active, the request is rejected", authType.value());
					}
					authErrorCode = GatewayExceptionCode.AUTHORIZATION;
					authErrorMessage = "Authentication: " + authType.value() + " not active.";
				} else {
					if(logger.isDebugEnabled()) {
						logger.debug("There is no Auth, the request is rejected");
					}
					authErrorCode = authType.authErrorCode();
					authErrorMessage = authType.authErrorMessage();
				}
			} else {
				if(logger.isDebugEnabled()) {
					logger.debug("The API is not active, the request is rejected");
				}
				authErrorCode = GatewayExceptionCode.API_NOT_ACTIVATED;
				authErrorMessage = "API not active";
			}
		} else {
			// the status of the API is not trusted, then we reject the call
			if(logger.isDebugEnabled()) {
				logger.debug("The status of the API is not trusted, the request is rejected");
			}
			authErrorCode = GatewayExceptionCode.AUTHORIZATION;
			authErrorMessage = "Not Authorized";
		}

		Exception exception = new GatewayException(authErrorCode, authErrorMessage);
		exchange.setException(exception);
	}
}
