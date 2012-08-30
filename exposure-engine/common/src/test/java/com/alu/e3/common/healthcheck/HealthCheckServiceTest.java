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
package com.alu.e3.common.healthcheck;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alu.e3.common.osgi.api.IHealthCheckFactory;
import com.alu.e3.data.IHealthCheckService;

public class HealthCheckServiceTest {
	
	private IHealthCheckService hcs;
	
	public HealthCheckServiceTest() {		
	}
	
	@Before
	public void setUp() throws Exception {
		
		hcs = new HealthCheckService(IHealthCheckFactory.GATEWAY_TYPE);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testHealthCheck() {
		assertNotNull("Check that healthcheck service is not null", hcs);
		
		hcs.start();
		
		assertFalse(hcs.check("FakeHost"));
		assertTrue(hcs.check("")); //localhost
		
		hcs.stop();
	}
	
	@Test
	public void testHealthCheck2() {
		assertNotNull("Check that healthcheck service is not null", hcs);
		
//		hcs.start();
//		
//		assertFalse(hcs.check("FakeHost"));
//		assertTrue(hcs.check("")); //localhost
//		
//		hcs.stop();
//		
//		hcs.start();
//		assertTrue(hcs.check("")); //localhost
//		hcs.stop();
	}
}