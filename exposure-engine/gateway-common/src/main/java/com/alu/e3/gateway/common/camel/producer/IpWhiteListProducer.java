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
package com.alu.e3.gateway.common.camel.producer;

import javax.servlet.http.HttpServletRequest;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;

import com.alu.e3.common.camel.ExchangeConstantKeys;
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.common.tools.CanonicalizedIpAddress;
import com.alu.e3.common.tools.CommonTools;
import com.alu.e3.data.model.Api;
import com.alu.e3.gateway.common.camel.exception.GatewayException;
import com.alu.e3.gateway.common.camel.exception.GatewayExceptionCode;

public class IpWhiteListProducer extends DefaultProducer {

	private IDataManager dataManager;
	private String apiId;

	public IpWhiteListProducer(Endpoint endpoint, IDataManager dataManager, String apiId) {
		super(endpoint);
		this.dataManager = dataManager;
		this.apiId = apiId;
	}
	
	@Override
	public void process(Exchange exchange) throws Exception {
		Api api = this.dataManager.getApiById(apiId, false);
		HttpServletRequest request = (HttpServletRequest) exchange.getIn().getHeader(Exchange.HTTP_SERVLET_REQUEST);
		//retrieve the real IP adress from the request
		String remoteAddr = CommonTools.remoteAddr(request);
		CanonicalizedIpAddress ip = new CanonicalizedIpAddress(remoteAddr);
		if(this.dataManager.isIpAllowed(api, ip.getIp())) {
			exchange.setProperty(ExchangeConstantKeys.E3_API.toString(), api);		
		}
		else {
			Exception exception = new GatewayException(GatewayExceptionCode.AUTHORIZATION, "Not Authorized from this IP address");
			exchange.setException(exception);			
		}		
	}
}
