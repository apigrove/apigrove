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

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

import com.alu.e3.auth.MockAuthDataAccess;
import com.alu.e3.common.camel.AuthReport;
import com.alu.e3.common.camel.ExchangeConstantKeys;

public class AppKeyExecutorTest {

	final CamelContext context = new DefaultCamelContext();
	final String appKeyName = "appkey";
	final String appHeaderName = "appheader";
	
	@Test
	public void testWin() {
		Exchange exchange = new DefaultExchange(context);
		
		// Setting the key in the request parameters
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(appKeyName, "asdf");
		exchange.setProperty(ExchangeConstantKeys.E3_REQUEST_PARAMETERS.toString(), parameters);

		// Setting the key in the query - should be removed
		exchange.getIn().setHeader(Exchange.HTTP_QUERY, appKeyName + "=asdf");
		
		AppKeyExecutor executor = new AppKeyExecutor(appKeyName, appHeaderName, "1234", new MockAuthDataAccess("asdf", null, null));
		
		AuthReport authReport = executor.checkAllowed(exchange);
		
		assertNotNull("This authentication should have succeeded", authReport.getAuthIdentity());

		// Check the query parameter
		assertNull("The query parameter should have been removed", exchange.getIn().getHeader(Exchange.HTTP_QUERY));
	}
	
	@Test
	public void testWinHeader() {
		Exchange exchange = new DefaultExchange(context);
		exchange.setProperty(ExchangeConstantKeys.E3_REQUEST_PARAMETERS.toString(), new HashMap<String, Object>());
		
		// Setting the key in the header - should be removed
		exchange.getIn().setHeader(appHeaderName, "asdf");
		
		AppKeyExecutor executor = new AppKeyExecutor(appKeyName, appHeaderName, "1234", new MockAuthDataAccess("asdf", null, null));
		
		AuthReport authReport = executor.checkAllowed(exchange);
		
		assertNotNull("This authentication should have succeeded", authReport.getAuthIdentity());

		// Check the query parameter
		assertNull("The header should have been removed", exchange.getIn().getHeader(appHeaderName));
	}
	
	
	@Test
	public void testFailNotAllowed(){
		Exchange exchange = new DefaultExchange(context);

		// Setting the key in the request parameters
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(appKeyName, "asdf");
		exchange.setProperty(ExchangeConstantKeys.E3_REQUEST_PARAMETERS.toString(), parameters);
		
		AppKeyExecutor executor = new AppKeyExecutor(appKeyName, appHeaderName, "1234", new MockAuthDataAccess(null, null, null));

		AuthReport authReport = executor.checkAllowed(exchange);
		
		assertNull("This authentication should have failed", authReport.getAuthIdentity());
	}
	
	@Test
	public void testFailNoHeader(){
		Exchange exchange = new DefaultExchange(context);
		exchange.setProperty(ExchangeConstantKeys.E3_REQUEST_PARAMETERS.toString(), new HashMap<String, Object>());
		
		// no parameter should fail
		AppKeyExecutor executor = new AppKeyExecutor(appKeyName, appHeaderName, "1234", new MockAuthDataAccess("asdf", null, null));
		
		AuthReport authReport = executor.checkAllowed(exchange);
		
		assertNull("This authentication should have failed", authReport.getAuthIdentity());
	}
	
	@Test
	public void testFailBadFormat(){
		Exchange exchange = new DefaultExchange(context);
		
		// Setting the wrong key in the request parameters
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(appKeyName.toUpperCase(), "asdf");
		exchange.setProperty(ExchangeConstantKeys.E3_REQUEST_PARAMETERS.toString(), parameters);

		AppKeyExecutor executor = new AppKeyExecutor(appKeyName, appHeaderName, "1234", new MockAuthDataAccess("asdf", null, null));
		
		AuthReport authReport = executor.checkAllowed(exchange);
		
		assertNull("This authentication should have failed",  authReport.getAuthIdentity());
	}
	
	@Test
	public void testFailBadFormat2(){
		Exchange exchange = new DefaultExchange(context);
		
		// Setting the null key in the request parameters
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(appKeyName, null);
		exchange.setProperty(ExchangeConstantKeys.E3_REQUEST_PARAMETERS.toString(), parameters);
		
		AppKeyExecutor executor = new AppKeyExecutor(appKeyName, appHeaderName, "1234", new MockAuthDataAccess("asdf", null, null));
		
		AuthReport authReport = executor.checkAllowed(exchange);
		
		assertNull("This authentication should have failed", authReport.getAuthIdentity());
	}

}
