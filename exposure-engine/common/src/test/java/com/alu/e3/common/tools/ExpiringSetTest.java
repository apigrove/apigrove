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
package com.alu.e3.common.tools;

import static org.junit.Assert.*;

import org.junit.Test;

public class ExpiringSetTest {

	@Test
	public void testCreation() {
		ExpiringSet<String> expiringSet = new ExpiringSet<String>(5 * 60);
		assertNotNull(expiringSet);
	}

	@Test
	public void testAdd() throws InterruptedException {
		ExpiringSet<String> expiringSet = new ExpiringSet<String>(2);   // seconds
		assertFalse(expiringSet.contains("hello"));
		assertTrue(expiringSet.contains("hello"));
	}

	@Test
	public void testExpire() throws InterruptedException {
		ExpiringSet<String> expiringSet = new ExpiringSet<String>(2);   // seconds
		assertFalse(expiringSet.contains("hello"));
		assertTrue(expiringSet.contains("hello"));
		Thread.sleep(3 * 1000);
		assertFalse(expiringSet.contains("hello"));
		assertTrue(expiringSet.contains("hello"));
	}

	@Test
	public void testLiveExpire() throws InterruptedException {
		ExpiringSet<String> expiringSet = new ExpiringSet<String>(2);   // seconds
		assertFalse(expiringSet.contains("hello"));
		assertTrue(expiringSet.contains("hello"));
		int i = 0;
		while (i++ < 1000000) {
			if (!expiringSet.contains("hello")) {
				break;
			}
			Thread.sleep(1);
		}
		assertTrue(expiringSet.contains("hello"));
		assert(i > 1000);
		assert(i < 1000000);
	}

	@Test
	public void testShutdown() throws InterruptedException {
		ExpiringSet<String> expiringSet = new ExpiringSet<String>(2);   // seconds
		assertFalse(expiringSet.contains("hello"));
		assertTrue(expiringSet.contains("hello"));
		expiringSet.shutdown();
	}
}
