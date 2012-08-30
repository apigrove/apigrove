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
import com.alu.e3.common.camel.AuthIdentity;
import com.alu.e3.common.camel.ExchangeConstantKeys;
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.data.model.Api;
import com.alu.e3.data.model.Auth;

import com.alu.e3.data.model.CallDescriptor;

public class SubscriberIDExtractor implements Processor {

	private IDataManager dataManager;
	
	public void setDataManager(IDataManager dataManager) {
		this.dataManager = dataManager;
	}
	
	public SubscriberIDExtractor() {}
	
	@Override
	public void process(Exchange exchange) throws Exception {

		String subscriberId = exchange.getIn().getHeader(E3Constant.SUBSCRIBER_ID_HEADER_NAME, String.class); 

		if(subscriberId == null)
			return;

		String[] parts = subscriberId.split("\\|");

		if(parts.length != 2)
			return;

		try {
			// Get current AuthIdentity
			AuthIdentity authIdentity = exchange.getProperty(ExchangeConstantKeys.E3_AUTH_IDENTITY.toString(), AuthIdentity.class);
						
			// Get subscriber matching CallDescriptors
			Api api = authIdentity.getApi();
			Auth auth = dataManager.getAuthById(parts[1]);
			List<CallDescriptor> subscriberDescriptors = dataManager.getMatchingPolicies(api, auth);

			// Merge lists of CallDescriptors on the current AuthIdentity
			List<CallDescriptor> descriptors = authIdentity.getCallDescriptors();
			for(CallDescriptor c : subscriberDescriptors) {
				if(!descriptors.contains(c)) {
					descriptors.add(c);
				}
			}
		} catch(Exception e) {
			// Do nothing in case of errors
		}

	}

}
