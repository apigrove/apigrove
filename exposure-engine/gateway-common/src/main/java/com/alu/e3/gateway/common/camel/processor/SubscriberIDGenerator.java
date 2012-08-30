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

import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.alu.e3.common.E3Constant;
import com.alu.e3.common.camel.AuthIdentity;
import com.alu.e3.common.camel.ExchangeConstantKeys;

public class SubscriberIDGenerator implements Processor {
	
	@Override
	public void process(Exchange exchange) throws Exception {

		AuthIdentity authIdentity = exchange.getProperty(ExchangeConstantKeys.E3_AUTH_IDENTITY.toString(), AuthIdentity.class);
		
		String subscriberId = null; 
		
		//TODO: Check if It's what we need as subscriberID
		if (authIdentity == null || authIdentity.getAuth() == null || authIdentity.getApi() == null)
			subscriberId = UUID.randomUUID().toString();
		else
			// Align subscriberId format with SubscriberIdExtractor processor!
			subscriberId = new StringBuilder(authIdentity.getApi().getId()).append("|").append(authIdentity.getAuth().getId()).toString();
		
		exchange.getIn().setHeader(E3Constant.SUBSCRIBER_ID_HEADER_NAME, subscriberId);
	}
}
