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
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import com.alu.e3.auth.MockAuthDataAccess;
import com.alu.e3.common.camel.AuthReport;
import com.alu.e3.data.model.Api;

public class NoAuthExecutorTest {
	
	final CamelContext context = new DefaultCamelContext();

	@Test
	public void testNoAuth() {
		
		Exchange exchange = new DefaultExchange(context);
		
		Api api = new Api();
		api.setId("api3234");

		MockAuthDataAccess mockDA = new MockAuthDataAccess(null, null, null);
		NoAuthExecutor executor = new NoAuthExecutor(mockDA);
		
		AuthReport authReport = executor.checkAllowed(exchange, api);
		
		assertNotNull("This authentication should have succeeded", authReport.getAuthIdentity());
	}
}
