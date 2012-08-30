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
package com.alu.e3.prov;

import java.util.Map;

import com.alu.e3.common.caching.IAckData;
import com.alu.e3.common.caching.ICacheManager;
import com.alu.e3.common.caching.ICacheQueue;
import com.alu.e3.common.caching.ICacheTable;

public class MockCacheManager implements ICacheManager{
	
	private ICacheManager cacheManager; 
	
	public MockCacheManager() {
	}

	public <K, V> ICacheTable<K, V> createTable(String name, boolean isReplicated, Map<String, String> properties) {
		return cacheManager.createTable(name, isReplicated, properties);
	}

	public <K, V> ICacheTable<K, V> createOrGetTable(String name, boolean isReplicated, Map<String, String> properties) {
		return cacheManager.createOrGetTable(name, isReplicated, properties);

	}

	@Override
	public <K, V extends IAckData> ICacheTable<K, V> createAckTable(String name, boolean isReplicated, Map<String, String> properties) {
		return cacheManager.createTable(name, isReplicated, properties);

	}

	@Override
	public <E> ICacheQueue<E> createQueue(String name, Map<String, String> properties) {
		return cacheManager.createQueue(name, properties);
	}	

	@Override
	public <E> ICacheQueue<E> getOrCreateQueue(String name, Map<String, String> properties) {
		return cacheManager.getOrCreateQueue(name, properties);

	}

	public void setCacheManager(ICacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

};