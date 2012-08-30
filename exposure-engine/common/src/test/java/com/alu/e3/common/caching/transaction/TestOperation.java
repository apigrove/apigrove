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
package com.alu.e3.common.caching.transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.security.InvalidParameterException;

import org.junit.Test;

import com.alu.e3.common.caching.HashCacheTable;
import com.alu.e3.common.transaction.Operation;
import com.alu.e3.common.transaction.OperationType;

public class TestOperation {

	@Test (expected = InvalidParameterException.class)
	public void testNewOperationIvalidParameter() {
		new Operation<Object, Object>(OperationType.CREATE, new Object(), new Object(), null);
	}

	@Test (expected = InvalidParameterException.class)
	public void testNewOperationIvalidParameter2() {
		new Operation<Object, Object>(OperationType.CREATE, null, new Object(), new HashCacheTable<Object, Object>());
	}

	@Test
	public void testRollbackCreate() {
		HashCacheTable<Object, Object> cacheTable = new HashCacheTable<Object, Object>();

		Operation<?, ?> operation = new Operation<Object, Object>(OperationType.CREATE, 1, null, cacheTable);
		cacheTable.set(1, "one");		
		assertNotNull(cacheTable.get(1));
		
		operation.rollback();
		
		assertNull(cacheTable.get(1));
	}

	@Test
	public void testRollbackRead() {
		HashCacheTable<Object, Object> cacheTable = new HashCacheTable<Object, Object>();

		cacheTable.set(1, "one");
		assertNotNull(cacheTable.get(1));

		Operation<?, ?> operation = new Operation<Object, Object>(OperationType.READ, 1, null, cacheTable);
		
		operation.rollback();
		
		assertEquals("one", cacheTable.get(1));
	}

	@Test
	public void testRollbackUpdate() {
		
		HashCacheTable<Object, Object> cacheTable = new HashCacheTable<Object, Object>();

		Operation<?, ?> operation = new Operation<Object, Object>(OperationType.UPDATE, 1, "one", cacheTable);
		cacheTable.set(1, "un");		
		assertEquals("un", cacheTable.get(1));

		operation.rollback();
		
		assertEquals("one", cacheTable.get(1));
	}
	
	@Test
	public void testRollbackDelete() {
		HashCacheTable<Object, Object> cacheTable = new HashCacheTable<Object, Object>();

		Operation<?, ?> operation = new Operation<Object, Object>(OperationType.DELETE, 1, "one", cacheTable);
		cacheTable.remove(1);		
		assertNull(cacheTable.get(1));

		operation.rollback();
		
		assertEquals("one", cacheTable.get(1));
	}
	
	
}
