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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.junit.Before;
import org.junit.Test;

import com.alu.e3.common.camel.AuthIdentity;
import com.alu.e3.common.camel.ExchangeConstantKeys;
import com.alu.e3.data.model.Api;
import com.alu.e3.data.model.Auth;
import com.alu.e3.data.model.ExtractFromType;
import com.alu.e3.data.model.Policy;
import com.alu.e3.data.model.sub.TdrGenerationRule;
import com.alu.e3.gateway.common.camel.processor.TDRRequestProcessor;
import com.alu.e3.gateway.common.camel.processor.TDRResponseProcessor;
import com.alu.e3.gateway.common.camel.processor.TDRStaticProcessor;
import com.alu.e3.tdr.TDRDataService;

public class ProvisionedTDRProcessorTest {

	private Exchange exchange;
	private Api api;
	private Auth auth;
	private Policy policy1;
	private Processor responseProcessor;
	private Processor requestProcessor;
	private Processor staticProcessor;

	/**
	 * Very basic test to make sure that static and dynamic tdr rules on an Auth
	 * Make it into the expected TDR
	 * @throws Exception
	 */
	@Test
	public void testAuthTDRs() throws Exception{
		// Add some tdr rules to the Auth and make sure they make it into the billing tdr
		TdrGenerationRule genRule = getBasicTestRule();
		auth.setTdrGenerationRule(genRule);

		requestProcessor.process(exchange);

		exchange.setProperty(ExchangeConstantKeys.E3_GOT_SB_RESPONSE.toString(), Boolean.TRUE);
		testPostRequest();

		responseProcessor.process(exchange);

		testPostResponse();

		staticProcessor.process(exchange);

		testPostStatic();
	}

	/**
	 * Very basic test to make sure that static and dynamic tdr rules on an Api
	 * make it into the expected TDR
	 * @throws Exception
	 */
	@Test
	public void testApiTDRs() throws Exception{
		// Add some tdr rules to the Api and make sure they make it into the billing tdr
		TdrGenerationRule genRule = getBasicTestRule();
		api.setTdrGenerationRule(genRule);

		requestProcessor.process(exchange);
		exchange.setProperty(ExchangeConstantKeys.E3_GOT_SB_RESPONSE.toString(), Boolean.TRUE);

		testPostRequest();

		responseProcessor.process(exchange);

		testPostResponse();

		staticProcessor.process(exchange);


		testPostStatic();
	}

	/**
	 * Very basic test to make sure that static and dynamic tdr rules on a Policy
	 * make it into the expected TDR
	 * @throws Exception
	 */
	@Test
	public void testPolicyTDRs() throws Exception{
		// Add some tdr rules to a Policy and make sure they make it into the billing tdr
		TdrGenerationRule genRule = getBasicTestRule();
		policy1.setTdrGenerationRule(genRule);

		requestProcessor.process(exchange);
		exchange.setProperty(ExchangeConstantKeys.E3_GOT_SB_RESPONSE.toString(), Boolean.TRUE);

		testPostRequest();

		responseProcessor.process(exchange);

		testPostResponse();

		staticProcessor.process(exchange);


		testPostStatic();
	}

	/**
	 * Test that a rule that has NO types will be applied to ALL TDRs for that
	 * transaction.
	 * @throws Exception
	 */
	@Test
	public void testCommonTdrRules() throws Exception{
		// Add some tdr rules to a Policy and make sure they make it into the billing tdr
		TdrGenerationRule genRule = getBasicTestRule();
		// Add another kind of tdr
		TdrGenerationRule quotaRule = new TdrGenerationRule();
		TDRDataService.addNewTdrGenerationRule(exchange, quotaRule, "apiRateLimit");

		// Add some rule that will apply to all TDRs
		genRule.getStaticRules().add(TestHelper.getStaticRule("COMMON-STATIC", "COMMON-STATIC-VALUE", null));
		exchange.getIn().setHeader("COMMON-DYN-HEAD", "COMMON-DYN-HEAD-VALUE");
		genRule.getDynamicRules().add(TestHelper.getDynamicRule("COMMON-DYN", "COMMON-DYN-HEAD", ExtractFromType.Response));
		auth.setTdrGenerationRule(genRule);

		requestProcessor.process(exchange);
		exchange.setProperty(ExchangeConstantKeys.E3_GOT_SB_RESPONSE.toString(), Boolean.TRUE);

		testPostRequest();

		responseProcessor.process(exchange);

		testPostResponse();

		staticProcessor.process(exchange);

		testPostStatic();

		// Make sure our new values are in the billing tdr
		Map<String, List<Map<String, Object>>> tdrData = TDRDataService.getTdrs(exchange);
		Map<String, Object> billing = tdrData.get("Billing").get(0);
		assertTrue(billing.containsKey("COMMON-STATIC"));
		assertEquals("COMMON-STATIC-VALUE", billing.get("COMMON-STATIC"));
		assertTrue(billing.containsKey("COMMON-DYN"));
		assertEquals("COMMON-DYN-HEAD-VALUE", billing.get("COMMON-DYN"));

		// Make sure that the rateLimit tdr exists
		assertTrue(tdrData.containsKey("apiRateLimit"));
		assertEquals(1, tdrData.get("apiRateLimit").size());

		// Make sure that our new values are in the rateLimit tdr
		Map<String, Object> rateLimit = tdrData.get("apiRateLimit").get(0);
		assertTrue(rateLimit.containsKey("COMMON-STATIC"));
		assertEquals("COMMON-STATIC-VALUE", rateLimit.get("COMMON-STATIC"));
		assertTrue(rateLimit.containsKey("COMMON-DYN"));
		assertEquals("COMMON-DYN-HEAD-VALUE", rateLimit.get("COMMON-DYN"));

		// Make sure that the old values are NOT in the rateLimit tdr
		assertTrue(!rateLimit.containsKey("AUTH-DYN"));
		assertTrue(!rateLimit.containsKey("AUTH-STAT-VAL"));

	}

	/**
	 * Test to make sure that if a static rule is added without either a value or
	 * a property name then it is not used.
	 * @throws Exception
	 */
	@Test
	public void testMissingStaticValueAndProperty() throws Exception{
		// Add some tdr rules to a Policy and make sure they make it into the billing tdr
		TdrGenerationRule genRule = getBasicTestRule();
		genRule.getStaticRules().add(TestHelper.getStaticRule("STATIC-NAME", null, null, "Billing"));
		api.setTdrGenerationRule(genRule);

		requestProcessor.process(exchange);
		exchange.setProperty(ExchangeConstantKeys.E3_GOT_SB_RESPONSE.toString(), Boolean.TRUE);

		testPostRequest();
		responseProcessor.process(exchange);
		testPostResponse();

		staticProcessor.process(exchange);


		testPostStatic();

		Map<String, List<Map<String, Object>>> tdrData = TDRDataService.getTdrs(exchange);
		Map<String, Object> billing = tdrData.get("Billing").get(0);
		assertTrue(!billing.containsKey("STATIC-NAME"));

	}

	/**
	 * Tests that if the request Processor gets called twice that it will only run once.
	 * @throws Exception
	 */
	@Test
	public void testDuplicateRunRequest() throws Exception{
		// Add some tdr rules to a Policy and make sure they make it into the billing tdr
		TdrGenerationRule genRule = getBasicTestRule();
		genRule.getStaticRules().add(TestHelper.getStaticRule("STATIC-NAME", null, null, "Billing"));
		api.setTdrGenerationRule(genRule);

		requestProcessor.process(exchange);
		testPostRequest();
		TDRDataService.clean(exchange);

		// This sets up the Billing TDR
		TDRDataService.setTxTDRName("Billing", exchange);
		TDRDataService.setTxTDRProperty("PROP-1", "VALUE-1", exchange);

		requestProcessor.process(exchange);

		testRequestNotRun();

	}

	/**
	 * This test should make sure that no request rules are activated on if it is a response
	 */
	@Test
	public void testRunRequestProcOnResponse() throws Exception{
		// Add some tdr rules to a Policy and make sure they make it into the billing tdr
		TdrGenerationRule genRule = getBasicTestRule();
		genRule.getStaticRules().add(TestHelper.getStaticRule("STATIC-NAME", null, null, "Billing"));
		api.setTdrGenerationRule(genRule);
		exchange.setProperty(ExchangeConstantKeys.E3_GOT_SB_RESPONSE.toString(), Boolean.TRUE);

		requestProcessor.process(exchange);
		testRequestNotRun();
	}

	/**
	 * This test should make sure that no response rules are activated on if it is a request
	 */
	@Test
	public void testRunResponseProcOnRequest()throws Exception{
		// Add some tdr rules to a Policy and make sure they make it into the billing tdr
		TdrGenerationRule genRule = getBasicTestRule();
		genRule.getStaticRules().add(TestHelper.getStaticRule("STATIC-NAME", null, null, "Billing"));
		api.setTdrGenerationRule(genRule);

		responseProcessor.process(exchange);

		testResponseNotRun();
	}

	private void testRequestNotRun() {
		Map<String, List<Map<String,Object>>> tdrData = TDRDataService.getTdrs(exchange);
		assertNotNull(tdrData);
		assertTrue(tdrData.containsKey("Billing"));
		assertEquals(1, tdrData.get("Billing").size());

		Map<String, Object> billing = tdrData.get("Billing").get(0);

		// Make sure the request ones are not in there
		assertTrue(!billing.containsKey("DYN-REQUEST"));
	}

	/**
	 * Test that if the response processor gets called twice that it will only run once
	 * @throws Exception
	 */
	@Test
	public void testDuplicateRunResponse() throws Exception{
		// Add some tdr rules to a Policy and make sure they make it into the billing tdr
		TdrGenerationRule genRule = getBasicTestRule();
		genRule.getStaticRules().add(TestHelper.getStaticRule("STATIC-NAME", null, null, "Billing"));
		api.setTdrGenerationRule(genRule);

		exchange.setProperty(ExchangeConstantKeys.E3_GOT_SB_RESPONSE.toString(), Boolean.TRUE);
		responseProcessor.process(exchange);
		TDRDataService.clean(exchange);

		// This sets up the Billing TDR
		TDRDataService.setTxTDRName("Billing", exchange);
		TDRDataService.setTxTDRProperty("PROP-1", "VALUE-1", exchange);

		responseProcessor.process(exchange);

		testResponseNotRun();
	}

	private void testResponseNotRun() {
		Map<String, List<Map<String,Object>>> tdrData = TDRDataService.getTdrs(exchange);
		assertNotNull(tdrData);
		assertTrue(tdrData.containsKey("Billing"));
		assertEquals(1, tdrData.get("Billing").size());

		Map<String, Object> billing = tdrData.get("Billing").get(0);
		assertTrue(!billing.containsKey("AUTH-DYN"));
	}

	/**
	 * Helper function to setup the basic tests above
	 * @return
	 */
	private TdrGenerationRule getBasicTestRule(){
		// Add a header to the exchange for use by the dynamic header
		exchange.getIn().setHeader("AUTH_HEAD", "AUTH_HEAD_VALUE");
		exchange.getIn().setHeader("REQUEST-HEAD", "REQUEST-HEAD-VAL");
		Map<String, String> props = new HashMap<String, String>();
		props.put("appId", "1234");
		exchange.setProperty(ExchangeConstantKeys.E3_MODEL_PROPERTIES.toString(), props);

		TdrGenerationRule genRule = new TdrGenerationRule();
		genRule.getDynamicRules().add(TestHelper.getDynamicRule("AUTH-DYN", "AUTH_HEAD", ExtractFromType.Response, "Billing"));
		genRule.getDynamicRules().add(TestHelper.getDynamicRule("DYN-REQUEST", "REQUEST-HEAD", ExtractFromType.Request, "Billing"));
		genRule.getStaticRules().add(TestHelper.getStaticRule("AUTH-STAT-VAL", "1234", null, "Billing"));
		genRule.getStaticRules().add(TestHelper.getStaticRule("AUTH-PROP-VAL", null, "appId", "Billing"));
		return genRule;
	}

	/**
	 * Helper function to test the functionality of the basic tests
	 */
	private void testPostStatic(){
		Map<String, List<Map<String,Object>>> tdrData = TDRDataService.getTdrs(exchange);
		assertNotNull(tdrData);
		assertTrue(tdrData.containsKey("Billing"));
		assertEquals(1, tdrData.get("Billing").size());

		Map<String, Object> billing = tdrData.get("Billing").get(0);
		assertTrue(billing.containsKey("AUTH-DYN"));
		assertEquals("AUTH_HEAD_VALUE", billing.get("AUTH-DYN"));
		assertTrue(billing.containsKey("AUTH-STAT-VAL"));
		assertEquals("1234", billing.get("AUTH-STAT-VAL"));
		assertTrue(billing.containsKey("AUTH-PROP-VAL"));
		assertEquals("1234", billing.get("AUTH-PROP-VAL"));

		assertTrue(billing.containsKey("DYN-REQUEST"));
		assertEquals("REQUEST-HEAD-VAL", billing.get("DYN-REQUEST"));
	}

	/**
	 * Helper function to test the functionality of the basic tests
	 */
	private void testPostResponse(){
		Map<String, List<Map<String,Object>>> tdrData = TDRDataService.getTdrs(exchange);
		assertNotNull(tdrData);
		assertTrue(tdrData.containsKey("Billing"));
		assertEquals(1, tdrData.get("Billing").size());

		Map<String, Object> billing = tdrData.get("Billing").get(0);
		assertTrue(billing.containsKey("AUTH-DYN"));
		assertTrue(!billing.containsKey("AUTH-STAT-VAL"));
		assertTrue(!billing.containsKey("AUTH-PROP-VAL"));

		assertTrue(billing.containsKey("DYN-REQUEST"));
		assertEquals("REQUEST-HEAD-VAL", billing.get("DYN-REQUEST"));
	}

	/**
	 * Helper function to test the functionality of the basic tests
	 */
	private void testPostRequest(){
		Map<String, List<Map<String,Object>>> tdrData = TDRDataService.getTdrs(exchange);
		assertNotNull(tdrData);
		assertTrue(tdrData.containsKey("Billing"));
		assertEquals(1, tdrData.get("Billing").size());

		Map<String, Object> billing = tdrData.get("Billing").get(0);

		// Make sure the response ones are not in there
		assertTrue(!billing.containsKey("AUTH-DYN"));
		assertTrue(!billing.containsKey("AUTH-STAT-VAL"));
		assertTrue(!billing.containsKey("AUTH-PROP-VAL"));

		assertTrue(billing.containsKey("DYN-REQUEST"));
		assertEquals("REQUEST-HEAD-VAL", billing.get("DYN-REQUEST"));
	}


	/**
	 * General setup function.  Build up the model and handle the 
	 * prerequisite TDR stuff.
	 */
	@Before
	public void setup(){
		exchange = TestHelper.setupExchange();
		AuthIdentity id = (AuthIdentity) exchange.getProperty(ExchangeConstantKeys.E3_AUTH_IDENTITY.toString());
		auth = id.getAuth();
		api = id.getApi();
		policy1 = id.getCallDescriptors().get(0).getPolicy();

		responseProcessor = new TDRResponseProcessor();
		requestProcessor = new TDRRequestProcessor();
		staticProcessor = new TDRStaticProcessor();

		TDRDataService.setTxTDRName("Billing", exchange);
		TDRDataService.setTxTDRProperty("PROP-1", "VALUE-1", exchange);

	}


}
