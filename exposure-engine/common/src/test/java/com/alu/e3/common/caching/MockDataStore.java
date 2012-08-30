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
package com.alu.e3.common.caching;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.hazelcast.core.MapStore;

@SuppressWarnings("rawtypes")
public class MockDataStore implements MapStore {
	
	public static HashMap<Object, Object> internalStore = new HashMap<Object, Object>();
	
	@Override
	public Object load(Object key) {
		return internalStore.get(key);
	}

	@Override
	public Map loadAll(Collection keys) {
		return internalStore;
	}

	@Override
	public Set loadAllKeys() {
		return internalStore.keySet();
	}

	@Override
	public void store(Object key, Object value) {
		internalStore.put(key, value);
	}


	@SuppressWarnings("unchecked")
	@Override
	public void storeAll(Map map) {
		internalStore.putAll(map);			
	}

	@Override
	public void delete(Object key) {
		internalStore.remove(key);
	}


	@Override
	public void deleteAll(Collection keys) {
		for (Object key: keys) {
			internalStore.remove(key);
		}
	}
	
}

