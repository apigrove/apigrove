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

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.commons.codec.binary.Base64;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

import com.alu.e3.auth.AuthHttpHeaders;
import com.alu.e3.auth.MockAuthDataAccess;
import com.alu.e3.common.camel.AuthReport;

public class HttpBasicExecutorTest {

	final CamelContext context = new DefaultCamelContext();

	@Test
	public void testWin() {
		Exchange exchange = new DefaultExchange(context);
		
		// Setting the username = "win" should succeed
		exchange.getIn().setHeader(AuthHttpHeaders.Authorization.toString(), "Basic "+new String(Base64.encodeBase64("win:blarg".getBytes())));
		HttpBasicExecutor executor = new HttpBasicExecutor("123", new MockAuthDataAccess(null, "win:blarg", null));
		
		AuthReport authReport = executor.checkAllowed(exchange);		

		assertNotNull("This authentication should have succeeded", authReport.getAuthIdentity());
	}
	
	@Test
	public void testFailNoEncoding(){
		Exchange exchange = new DefaultExchange(context);
		
		// Setting the username = "win" should succeed
		exchange.getIn().setHeader(AuthHttpHeaders.Authorization.toString(), "Basic "+"win:blarg");
		HttpBasicExecutor executor = new HttpBasicExecutor("123", new MockAuthDataAccess(null, "win:blarg", null));
		
		AuthReport authReport = executor.checkAllowed(exchange);

		assertNull("This authentication should have failed", authReport.getAuthIdentity());
	}
	
	@Test
	public void testFailNoHeader(){
		Exchange exchange = new DefaultExchange(context);
		
		// Setting the username = "win" should succeed
		HttpBasicExecutor executor = new HttpBasicExecutor("123", new MockAuthDataAccess(null, "win:blarg", null));
		
		AuthReport authReport = executor.checkAllowed(exchange);

		assertNull("This authentication should have failed", authReport.getAuthIdentity());
	}
	
	@Test
	public void testFailBadFormat(){
		Exchange exchange = new DefaultExchange(context);
		
		// Setting the username = "win" should succeed
		exchange.getIn().setHeader(AuthHttpHeaders.Authorization.toString(), "Vlasic "+new String(Base64.encodeBase64("win:blarg".getBytes())));
		HttpBasicExecutor executor = new HttpBasicExecutor("123", new MockAuthDataAccess(null, "win:blarg", null));
		
		AuthReport authReport = executor.checkAllowed(exchange);
		
		assertNull("This authentication should have failed", authReport.getAuthIdentity());
	}
	
	@Test
	public void testFailBadFormat2(){
		Exchange exchange = new DefaultExchange(context);
		
		// Setting the username = "win" should succeed
		// This one is bad because it is missing the space between Basic and the user/pass
		exchange.getIn().setHeader(AuthHttpHeaders.Authorization.toString(), "Basic"+new String(Base64.encodeBase64("win:blarg".getBytes())));
		HttpBasicExecutor executor = new HttpBasicExecutor("123", new MockAuthDataAccess(null, "win:blarg", null));
		
		AuthReport authReport = executor.checkAllowed(exchange);		

		assertNull("This authentication should have failed", authReport.getAuthIdentity());
	}
	
	@Test
	public void testFailNotAllowed(){
		Exchange exchange = new DefaultExchange(context);

		// This one should be denied because the MockData is rigged to return null
		exchange.getIn().setHeader(AuthHttpHeaders.Authorization.toString(), "Basic "+new String(Base64.encodeBase64("win:blarg".getBytes())));
		HttpBasicExecutor executor = new HttpBasicExecutor("123", new MockAuthDataAccess(null, null, null));
		
		AuthReport authReport = executor.checkAllowed(exchange);
		
		assertNull("This authentication should have failed", authReport.getAuthIdentity());
	}

}
