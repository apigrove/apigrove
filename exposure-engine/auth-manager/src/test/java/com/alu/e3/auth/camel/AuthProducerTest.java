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
package com.alu.e3.auth.camel;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Before;
import org.junit.Test;

import com.alu.e3.auth.AuthHttpHeaders;
import com.alu.e3.auth.camel.component.AuthComponent;
import com.alu.e3.auth.camel.endpoint.AuthEndpoint;
import com.alu.e3.auth.camel.producer.AuthProducer;
import com.alu.e3.auth.executor.MockAppKeyExecutorFactory;
import com.alu.e3.auth.executor.MockHttpBasicExecutorFactory;
import com.alu.e3.auth.executor.MockIpWhitelistExecutorFactory;
import com.alu.e3.auth.executor.MockNoAuthExecutorFactory;
import com.alu.e3.common.camel.ExchangeConstantKeys;

/**
 * This class should cover all functionality contained in the AuthEndpoint and AuthProducer classes.
 * All supporting classes are mocked to isolate the functionality.
 * 
 *
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = { "classpath:/META-INF/spring/auth-component-test.xml" })
public class AuthProducerTest {
	
	final CamelContext context = new DefaultCamelContext();
	private AuthComponent component;
	
	@Before
	public void before(){
		component = new AuthComponent(new DefaultCamelContext());
		component.registerExecutorFactory("authKey", new MockAppKeyExecutorFactory());
		component.registerExecutorFactory("basic", new MockHttpBasicExecutorFactory());
		component.registerExecutorFactory("ipList", new MockIpWhitelistExecutorFactory());
		component.registerExecutorFactory("noAuth", new MockNoAuthExecutorFactory());
	}

	/**
	 * This tests the case where the endpoint is configured correctly and we expect a successful HttpBasic authentication
	 * @throws Exception
	 */
	@Test
	public void testSuccessHttpBasic() throws Exception{
		AuthEndpoint endpoint = (AuthEndpoint) component.createEndpoint("blah?apiId=1234&basic=true");
		AuthProducer producer = (AuthProducer) endpoint.createProducer();
		Exchange exchange = new DefaultExchange(context);
		// Anything will work for the mock executor... it just has to be set
		exchange.getIn().setHeader(AuthHttpHeaders.Authorization.toString(), "blarg");
		
		producer.process(exchange);
		
		testAuthenticated(exchange);
	}

	/**
	 * Utility method to help do the same asserts for the same cases.
	 * @param exchange
	 */
	private void testAuthenticated(Exchange exchange) {
		assertNotNull("The Authentication header should not be null", exchange.getProperty(ExchangeConstantKeys.E3_AUTH_IDENTITY.toString()));
	}
	
	/**
	 * This test the case where the endpoint is configured correctly and we expect a failed httpbasic authentication
	 */
	@Test
	public void testFailHttpBasic() throws Exception{
		AuthEndpoint endpoint = (AuthEndpoint) component.createEndpoint("blah?apiId=1234&basic=true");
		AuthProducer producer = (AuthProducer) endpoint.createProducer();
		Exchange exchange = new DefaultExchange(context);
		
		producer.process(exchange);
		
		testNotAuthenticated(exchange);
	}

	/**
	 * Utility method to help do the same asserts for the same cases.
	 * @param exchange
	 */
	private void testNotAuthenticated(Exchange exchange) {
		assertNull("The Identity property should be null", exchange.getProperty(ExchangeConstantKeys.E3_AUTH_IDENTITY.toString()));
	}
	
	/**
	 * This tests the case where the endpoint is configured correctly and we expect a successful appkey authentication
	 * @throws Exception
	 */
	@Test
	public void testSuccessAppKey() throws Exception{
		AuthEndpoint endpoint = (AuthEndpoint) component.createEndpoint("blah?apiId=1234&authKey=true&keyName=appkey");
		AuthProducer producer = (AuthProducer) endpoint.createProducer();
		Exchange exchange = new DefaultExchange(context);
		// Anything will work for the mock executor... it just has to be set
		exchange.getIn().setHeader("appkey", "blarg");
		
		producer.process(exchange);
		
		testAuthenticated(exchange);
	}
	
	/**
	 * This test the case where the endpoint is configured correctly and we expect a failed appkey authentication
	 */
	@Test
	public void testFailAppKey() throws Exception{
		AuthEndpoint endpoint = (AuthEndpoint) component.createEndpoint("blah?apiId=1234&authKey=true&keyName=appkey");
		AuthProducer producer = (AuthProducer) endpoint.createProducer();
		Exchange exchange = new DefaultExchange(context);
		
		producer.process(exchange);
		
		testNotAuthenticated(exchange);
	}

	/**
	 * This tests the case where the endpoint is configured correctly and we expect a successful whitelist authentication
	 * @throws Exception
	 */
	@Test
	public void testSuccessWhitelist() throws Exception{
		AuthEndpoint endpoint = (AuthEndpoint) component.createEndpoint("blah?apiId=1234&ipList=true");
		AuthProducer producer = (AuthProducer) endpoint.createProducer();
		Exchange exchange = new DefaultExchange(context);
		// Anything will work for the mock executor... it just has to be set
		exchange.getIn().setHeader("source-ip", "127.0.0.1");
		
		producer.process(exchange);
		
		testAuthenticated(exchange);
	}
	
	/**
	 * This test the case where the endpoint is configured correctly and we expect a failed whitelist authentication
	 */
	@Test
	public void testFailWhitelist() throws Exception{
		AuthEndpoint endpoint = (AuthEndpoint) component.createEndpoint("blah?apiId=1234&ipList=true");
		AuthProducer producer = (AuthProducer) endpoint.createProducer();
		Exchange exchange = new DefaultExchange(context);
		
		producer.process(exchange);
		
		testNotAuthenticated(exchange);
	}
	
	 
	@Test
	public void testSuccessNoAuth() throws Exception{
		AuthEndpoint endpoint = (AuthEndpoint) component.createEndpoint("blah?apiId=1234&noAuth=true");
		AuthProducer producer = (AuthProducer) endpoint.createProducer();
		Exchange exchange = new DefaultExchange(context);
		
		producer.process(exchange);
		
		testAuthenticated(exchange);
	}
	
	@Test
	public void testFailNoAuth() throws Exception{
		AuthEndpoint endpoint = (AuthEndpoint) component.createEndpoint("blah?apiId=1234&noAuth=false");
		AuthProducer producer = (AuthProducer) endpoint.createProducer();
		Exchange exchange = new DefaultExchange(context);
		
		producer.process(exchange);
		
		testNotAuthenticated(exchange);
	}
	
}
