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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Before;
import org.junit.Test;

import com.alu.e3.common.camel.AuthIdentity;
import com.alu.e3.common.camel.ExchangeConstantKeys;
import com.alu.e3.data.model.Api;
import com.alu.e3.data.model.Auth;
import com.alu.e3.data.model.CallDescriptor;
import com.alu.e3.data.model.Policy;
import com.alu.e3.gateway.common.camel.processor.PropertyExtractionProcessor;

public class PropertyExtractProcessorTest {

	Exchange exchange;
	Auth auth;
	Api api;
	Policy policy1;
	AuthIdentity id;
	Processor processor;

	@Test
	public void testAllEmpty() throws Exception{
		processor.process(exchange);
	}

	@Test
	public void testNullAuth() throws Exception{
		id.setAuth(null);
		processor.process(exchange);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testBasic() throws Exception{
		Map<String, String> authProps = new HashMap<String, String>();
		authProps.put("auth_1", "auth_1_val");
		auth.setProperties(authProps);

		Map<String, String> apiProps = new HashMap<String, String>();
		apiProps.put("api_1", "api_1_val");
		api.setProperties(apiProps);

		Map<String, String> policyProps = new HashMap<String, String>();
		policyProps.put("policy_1", "policy_1_val");
		policy1.setProperties(policyProps);

		processor.process(exchange);

		// get the properties out of the exchange
		Map<String, String> props = (Map<String, String>) exchange.getProperty(ExchangeConstantKeys.E3_MODEL_PROPERTIES.toString());

		assertTrue(props.containsKey("auth_1"));
		assertEquals("auth_1_val", props.get("auth_1"));

		assertTrue(props.containsKey("api_1"));
		assertEquals("api_1_val", props.get("api_1"));

		assertTrue(props.containsKey("policy_1"));
		assertEquals("policy_1_val", props.get("policy_1"));

	}

	/**
	 * Testing that conflicts are resolved appropriately 
	 * auth > policy > api 
	 * policy >= policy // Conflicts are resolved in an undefined way
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testConflicts() throws Exception{
		Map<String, String> authProps = new HashMap<String, String>();
		authProps.put("a", "auth");
		auth.setProperties(authProps);

		Map<String, String> apiProps = new HashMap<String, String>();
		apiProps.put("a", "api");
		apiProps.put("b", "api");
		api.setProperties(apiProps);

		Map<String, String> policyProps = new HashMap<String, String>();
		policyProps.put("a", "policy");
		policyProps.put("b", "policy");
		policy1.setProperties(policyProps);

		processor.process(exchange);

		// get the properties out of the exchange
		Map<String, String> props = (Map<String, String>) exchange.getProperty(ExchangeConstantKeys.E3_MODEL_PROPERTIES.toString());

		assertTrue(props.containsKey("a"));
		assertEquals("auth", props.get("a"));

		assertTrue(props.containsKey("b"));
		assertEquals("policy", props.get("b"));

	}





	@Before
	public void setup(){
		exchange = new DefaultExchange(new DefaultCamelContext());
		processor = new PropertyExtractionProcessor();

		id = new AuthIdentity();
		api = new Api();
		policy1 = new Policy();
		auth = new Auth();
		CallDescriptor cd = new CallDescriptor(policy1, 0, 0);

		id.setApi(api);
		id.setAuth(auth);
		id.getCallDescriptors().add(cd);

		exchange.setProperty(ExchangeConstantKeys.E3_AUTH_IDENTITY.toString(), id);
	}
}
