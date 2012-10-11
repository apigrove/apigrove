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
package com.alu.e3.gateway.common.camel.processor;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.alu.e3.common.E3Constant;
import com.alu.e3.common.InvalidIDException;
import com.alu.e3.common.camel.AuthIdentity;
import com.alu.e3.common.camel.ExchangeConstantKeys;
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.data.model.Auth;
import com.alu.e3.data.model.CallDescriptor;
import com.alu.e3.gateway.common.camel.exception.GatewayException;
import com.alu.e3.gateway.common.camel.exception.GatewayExceptionCode;

public class SubscriberIDExtractor implements Processor {

	protected  IDataManager dataManager;


	public void setDataManager(IDataManager dataManager) {
		this.dataManager = dataManager;
	}

	@Override
	public void process(Exchange exchange) throws Exception {

		// Get current AuthIdentity
		AuthIdentity authIdentity = exchange.getProperty( ExchangeConstantKeys.E3_AUTH_IDENTITY.toString(), AuthIdentity.class);

		// extracts subscriber id and checks authorization
		String subscriberId = extractSubscriberId(exchange);
		if (null == subscriberId) return ;

		List<CallDescriptor> callDescriptors = checkSubscriberIdAuth(subscriberId, authIdentity);// throws if failed
		// merge call descriptor
		mergeCallDescriptor(callDescriptors, authIdentity);					
	}

	protected String extractSubscriberId(Exchange exchange) throws GatewayException {

		String subscriberId = exchange.getIn().getHeader(E3Constant.SUBSCRIBER_ID_HEADER_NAME, String.class);

		if (subscriberId == null || subscriberId.isEmpty()) return null;

		String[] parts = subscriberId.split("\\|");

		if (parts.length != 2 || parts[1].isEmpty()) return null;

		return parts[1];
	}


	protected List<CallDescriptor> checkSubscriberIdAuth(String subscriberId, AuthIdentity authIdentity) throws GatewayException {

		// Get subscriber matching CallDescriptors
		Auth auth;
		try {
			auth = dataManager.getAuthById(subscriberId);
		} catch (InvalidIDException e) {
			throw new GatewayException(GatewayExceptionCode.AUTHORIZATION, e.getMessage() );
		}

		if (auth == null || !auth.getStatus().isActive()) {
			throw new GatewayException(GatewayExceptionCode.AUTHORIZATION, "Authorization status is invalid");
		} 

		return 	dataManager.getMatchingPolicies(authIdentity.getApi(), auth);

	}

	protected void mergeCallDescriptor(List<CallDescriptor> callDescriptors, AuthIdentity authIdentity) {

		if ( null == callDescriptors)return; // no specific descriptor 

		// Merge lists of CallDescriptors on the current AuthIdentity
		List<CallDescriptor> descriptors = authIdentity.getCallDescriptors();

		for (CallDescriptor callDescriptor : callDescriptors) {
			if (!descriptors.contains(callDescriptor)) {
				descriptors.add(callDescriptor);
			}
		}
	}

}
