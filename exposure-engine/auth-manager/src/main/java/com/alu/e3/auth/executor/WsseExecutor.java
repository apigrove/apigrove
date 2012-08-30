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

import java.io.InputStream;
import org.apache.camel.Exchange;

import com.alu.e3.auth.access.IAuthDataAccess;
import com.alu.e3.auth.model.AuthType;
import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.common.tools.WsseTools;
import com.alu.e3.common.camel.AuthReport;
import com.alu.e3.gateway.common.camel.exception.GatewayExceptionCode;

public class WsseExecutor implements IAuthExecutor{

	private static final CategoryLogger LOG = CategoryLoggerFactory.getLogger(WsseExecutor.class, Category.AUTH);

	private IAuthDataAccess dataAccess;
	private String apiId = "";

	public WsseExecutor(String apiId, IAuthDataAccess dataAcccess){
		this.apiId = apiId;
		this.dataAccess = dataAcccess;
	}

	@Override
	public AuthReport checkAllowed(Exchange exchange) {

		AuthReport authReport = new AuthReport();;
		
		InputStream in = exchange.getIn().getBody(InputStream.class);
		
		WsseTools.WsseUsernameToken tok = null;
		
		try {
			tok = WsseTools.parseXml(in);
		} catch (Exception e) {
			LOG.debug("Parse exception: " + e.getMessage());
			authReport.setBadRequest(true);
		}
		
		if(!authReport.isBadRequest()) {
			authReport = dataAccess.checkAllowed(tok.getUsername(), tok.getPassword(), tok.isPasswordText(), tok.getNonce(), tok.getCreated(), apiId);
		}
		
		return authReport;
	}

	@Override
	public AuthType getType() {
		return AuthType.WSSE;
	}

	@Override
	public GatewayExceptionCode getErrorCode() {
		return GatewayExceptionCode.AUTHORIZATION_WSSE;
	}
	
}
