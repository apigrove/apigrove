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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import com.alu.e3.data.DataEntryEvent;

public class HashCacheTable<K, V> implements ICacheTable<K, V> {

	private Hashtable<K, V> hashTable;
	private List<IEntryListener<K, V>> listeners;
	
	public HashCacheTable() {
		hashTable = new Hashtable<K, V>();
		listeners = Collections.synchronizedList(new ArrayList<IEntryListener<K, V>>());
	}
	
	@Override
	public boolean set(K key, V value) {
		hashTable.put(key, value);
		
		DataEntryEvent<K, V> event = null;
		
		if(listeners.size() > 0) {
			if(containsKey(key)) {
				event = new DataEntryEvent<K, V>(key, value);
	
				for(IEntryListener<K, V> listener : listeners) {
					listener.entryUpdated(event);
				}
				
			} else {
				event = new DataEntryEvent<K, V>(key, value);
	
				for(IEntryListener<K, V> listener : listeners) {
					listener.entryAdded(event);
				}
			}
		}
		
		return true;

	}

	@Override
	public V get(K key) {
		return hashTable.get(key);
	}

	@Override
	public V remove(K key) {
		
		DataEntryEvent<K, V> event = null;
		
		if(listeners.size() > 0) {
			event = new DataEntryEvent<K, V>(key, null);

			for(IEntryListener<K, V> listener : listeners) {
				listener.entryRemoved(event);
			}
		}
		
		return hashTable.remove(key);
	}

	@Override
	public Set<K> getAllKeys() {
		return hashTable.keySet();
	}

	@Override
	public Collection<V> getAllValues() {
		return hashTable.values();
	}

	@Override
	public boolean containsKey(K key) {
		return hashTable.containsKey(key);
	}

	@Override
	public void lock(K key) {
		// Nothing to do
	}

	@Override
	public void unlock(K key) {
		// Nothing to do
	}

	@Override
	public void clear() {
		hashTable.clear();		
	}
	@Override
	public void addEntryListener(final IEntryListener<K, V> listener) {
		listeners.add(listener);
	}
	
	@Override
	public void removeEntryListener(IEntryListener<K, V> listener) {
		listeners.remove(listener);
	}
	
	@Override
	public String getName() {
		return "HashCacheTable";
	}

	@Override
	public void reloadSlave(String ip) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean set(K key, V value, String instanceIP) {
		// TODO Auto-generated method stub
		return false;
	}
}
