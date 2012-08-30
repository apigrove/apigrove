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
package com.alu.e3.gateway.camel.processor;

import org.apache.camel.Exchange;

import com.alu.e3.common.camel.AuthIdentity;
import com.alu.e3.common.camel.ExchangeConstantKeys;
import com.alu.e3.data.model.Api;
import com.alu.e3.data.model.Auth;
import com.alu.e3.data.model.CallDescriptor;
import com.alu.e3.data.model.ExtractFromType;
import com.alu.e3.data.model.Policy;
import com.alu.e3.data.model.sub.TdrDynamicRule;
import com.alu.e3.data.model.sub.TdrStaticRule;
import com.alu.e3.gateway.camel.MockExchange;

public class TestHelper {

	public static Exchange setupExchange(){
		Exchange exchange = new MockExchange();

		AuthIdentity id = new AuthIdentity();
		Api api = new Api();
		Policy policy1 = new Policy();
		Auth auth = new Auth();
		CallDescriptor cd = new CallDescriptor(policy1, 0, 0);

		id.setApi(api);
		id.setAuth(auth);
		id.getCallDescriptors().add(cd);

		exchange.setProperty(ExchangeConstantKeys.E3_AUTH_IDENTITY.toString(), id);

		return exchange;
	}

	/**
	 * Helper function for building static tdr rules
	 * @param name
	 * @param value
	 * @param propName
	 * @param types
	 * @return
	 */
	public static TdrStaticRule getStaticRule(String name, String value, String propName, String...types){
		TdrStaticRule rule = new TdrStaticRule();
		rule.setTdrPropName(name);
		if(value != null && !value.equals("")){
			rule.setValue(value);
		}
		else if(propName != null && !propName.equals("")){
			rule.setPropertyName(propName);
		}

		for(String type : types)
			rule.getTypes().add(type);

		return rule;
	}

	/**
	 * Helper function for building dynamic tdr rules
	 * @param name
	 * @param headerName
	 * @param types
	 * @return
	 */
	public static TdrDynamicRule getDynamicRule(String name, String headerName, ExtractFromType efType, String...types){
		TdrDynamicRule rule = new TdrDynamicRule();
		rule.setTdrPropName(name);
		rule.setHttpHeaderName(headerName);
		rule.setExtractFrom(efType);

		for(String type : types)
			rule.getTypes().add(type);

		return rule;
	}
}
