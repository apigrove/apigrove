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
package com.alu.e3.rate.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alu.e3.common.camel.AuthIdentity;
import com.alu.e3.common.camel.ExchangeConstantKeys;
import com.alu.e3.data.model.Auth;
import com.alu.e3.data.model.enumeration.ActionType;
import com.alu.e3.rate.manager.IGatewayRateManager;
import com.alu.e3.rate.model.LimitCheckResult;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/gateway-rate.rate-limit-test.xml" })
public class RateLimitProcessorTest {

	@Autowired
    protected CamelContext camelContext;
	
	@EndpointInject(uri = "mock:checkAuth")
    protected MockEndpoint checkAuth;
	
	@EndpointInject(uri = "mock:checkRate")
    protected MockEndpoint checkRate;
	
	@EndpointInject(uri = "mock:testMessage")
    protected MockEndpoint testMessage;
	
	@EndpointInject(uri = "mock:handleException")
    protected MockEndpoint handleException;
	
	@Produce(uri = "direct:test")
    protected ProducerTemplate producerTemplate;
	
	@Test
	@DirtiesContext
	public void testRateOK() throws Exception {
		AuthIdentity authIdentity = new AuthIdentity();
		Auth auth = new Auth();
		auth.setId("abc");
		authIdentity.setAuth(auth);

		AuthProcessorMock authProcessor =  new AuthProcessorMock(authIdentity);

		checkAuth.whenAnyExchangeReceived(authProcessor);

		RateLimitProcessor rateProcessor = new RateLimitProcessor();
		rateProcessor.setGatewayRateMger(new IGatewayRateManager() {

			@Override
			public LimitCheckResult isAllowed(AuthIdentity authIdentity, boolean isTDREnabled) {
				LimitCheckResult result = new LimitCheckResult();
				return result;
			}

		});

		checkRate.whenAnyExchangeReceived(rateProcessor);

		testMessage.setExpectedMessageCount(1);

		producerTemplate.requestBody(String.class);

		testMessage.assertIsSatisfied();	
	}

	@Test
	@DirtiesContext
	public void testRateKO() throws Exception {
		AuthIdentity authIdentity = new AuthIdentity();
		Auth auth = new Auth();
		auth.setId("ko");
		authIdentity.setAuth(auth);

		AuthProcessorMock authProcessor =  new AuthProcessorMock(authIdentity);

		checkAuth.whenAnyExchangeReceived(authProcessor);

		RateLimitProcessor rateProcessor = new RateLimitProcessor();
		rateProcessor.setGatewayRateMger(new IGatewayRateManager() {

			@Override
			public LimitCheckResult isAllowed(AuthIdentity authIdentity, boolean isTDREnabled) {
				LimitCheckResult result = new LimitCheckResult();
				result.setActionType(ActionType.REJECT);
				return result;
			}
		});

		checkRate.whenAnyExchangeReceived(rateProcessor);

		testMessage.setExpectedMessageCount(0);
		testMessage.allMessages().body().isNull();

		handleException.setExpectedMessageCount(1);
		handleException.message(0).property("CamelExceptionCaught").equals("com.alu.e3.rate.RatePolicyRuntimeException: Rate limit exceeded");

		producerTemplate.requestBody(String.class);

		testMessage.assertIsSatisfied();	
	}

	@Test
	@DirtiesContext
	public void testAuthIdentityRequired() throws Exception {
		RateLimitProcessor rateProcessor = new RateLimitProcessor();
		rateProcessor.setGatewayRateMger(new NeverAllowed());

		checkRate.whenAnyExchangeReceived(rateProcessor);

		testMessage.setExpectedMessageCount(0);
		testMessage.allMessages().body().isNull();

		handleException.setExpectedMessageCount(1);
		handleException.message(0).property("CamelExceptionCaught").equals("The property " + ExchangeConstantKeys.E3_AUTH_IDENTITY.toString() + " is required to check the rate limits.");

		producerTemplate.requestBody(String.class);

		testMessage.assertIsSatisfied();
		handleException.assertIsSatisfied();
	}

	@Test
	@DirtiesContext
	public void testRateLimitExeeded() throws Exception {
		RateLimitProcessor rateProcessor = new RateLimitProcessor();
		rateProcessor.setGatewayRateMger(new NeverAllowed());

		checkRate.whenAnyExchangeReceived(rateProcessor);

		testMessage.setExpectedMessageCount(0);
		testMessage.allMessages().body().isNull();

		handleException.setExpectedMessageCount(1);
		handleException.message(0).property("CamelExceptionCaught").equals("com.alu.e3.rate.RatePolicyRuntimeException: Rate limit exceeded");

		producerTemplate.requestBody(String.class);

		testMessage.assertIsSatisfied();
		handleException.assertIsSatisfied();
	}

	@Test
	@DirtiesContext
	public void testRateLimitNotExeeded() throws Exception {
		AuthProcessorMock authProcessor =  new AuthProcessorMock(new AuthIdentity());

		checkAuth.whenAnyExchangeReceived(authProcessor);

		RateLimitProcessor rateProcessor = new RateLimitProcessor();
		rateProcessor.setGatewayRateMger(new AlwaysAllowed());

		checkRate.whenAnyExchangeReceived(rateProcessor);

		testMessage.setExpectedMessageCount(1);

		producerTemplate.requestBody(String.class);

		testMessage.assertIsSatisfied();
	}

	class AlwaysAllowed implements IGatewayRateManager {

		@Override
		public LimitCheckResult isAllowed(AuthIdentity authIdentity, boolean isTDREnabled) {
			LimitCheckResult result = new LimitCheckResult();
			return result;
		}

	}
	class NeverAllowed implements IGatewayRateManager {

		@Override

		public LimitCheckResult isAllowed(AuthIdentity authIdentity, boolean isTDREnabled) {
			LimitCheckResult result = new LimitCheckResult();
			result.setActionType(ActionType.REJECT);
			return result;
		}

	}

	class AuthProcessorMock implements Processor {

		protected AuthIdentity authIdentity;

		public AuthProcessorMock(AuthIdentity authIdentity) {
			super();
			this.authIdentity = authIdentity;
		}

		@Override
		public void process(Exchange exchange) throws Exception {
			exchange.setProperty(ExchangeConstantKeys.E3_AUTH_IDENTITY.toString(), authIdentity);				
		}

	}
}
