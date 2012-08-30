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
package com.alu.e3.tdr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.apache.camel.Exchange;
import org.junit.Test;

public class TDRDataServiceTest {

	@Test
	public void test() {
		Exchange exchange = new MockExchange();
		
		// Make sure that it starts empty
		Map<String, Object> tdrProperties = TDRDataService.getTxTDRProperties(exchange);
		assertTrue(tdrProperties==null || tdrProperties.size()==0);
		
		// Add something...
		String key1 = "hello";
		String value1 = "world";
		TDRDataService.setTxTDRProperty(key1, value1, exchange);
		// Make sure that our tdr props is now size 1 and has our new property
		tdrProperties = TDRDataService.getTxTDRProperties(exchange);
		assertEquals(1, tdrProperties.size());
		assertNotNull(TDRDataService.getTxTDRProperty(key1, exchange));
		assertNotNull(tdrProperties.get(key1));
		assertEquals(value1, TDRDataService.getTxTDRProperty(key1, exchange));
		assertEquals(value1, tdrProperties.get(key1));
		
		// Add another one
		String key2 = "foo";
		String value2 = "bar";
		TDRDataService.setTxTDRProperty(key2, value2, exchange);
		// Make sure that our tdr props is now size 2 and has our new property
		tdrProperties = TDRDataService.getTxTDRProperties(exchange);
		assertEquals(2, tdrProperties.size());
		assertNotNull(TDRDataService.getTxTDRProperty(key2, exchange));
		assertNotNull(tdrProperties.get(key2));
		assertEquals(value2, TDRDataService.getTxTDRProperty(key2, exchange));
		assertEquals(value2, tdrProperties.get(key2));
		
		// Make sure we can still get the first one
		assertNotNull(TDRDataService.getTxTDRProperty(key1, exchange));
		assertNotNull(tdrProperties.get(key1));
		assertEquals(value1, TDRDataService.getTxTDRProperty(key1, exchange));
		assertEquals(value1, tdrProperties.get(key1));
		
		// Overwrite value 1 with value 3 and make sure we still only have 2
		String value3 = "hoot";
		TDRDataService.setTxTDRProperty(key1, value3, exchange);
		// Make sure that our tdr props is now size 2 and has our new property
		tdrProperties = TDRDataService.getTxTDRProperties(exchange);
		assertEquals(2, tdrProperties.size());
		assertNotNull(TDRDataService.getTxTDRProperty(key1, exchange));
		assertNotNull(tdrProperties.get(key1));
		assertEquals(value3, TDRDataService.getTxTDRProperty(key1, exchange));
		assertEquals(value3, tdrProperties.get(key1));

	}
	
}
