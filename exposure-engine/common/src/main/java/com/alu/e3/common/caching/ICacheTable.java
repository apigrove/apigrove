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
import java.util.Set;

public interface ICacheTable<K, V> {

	String getName();
	
	void clear();
	boolean set(K key, V value);
	
	V get(K key);
	
	V remove(K key);
	
	boolean containsKey(K key);

	Set<K> getAllKeys();
	Collection<V> getAllValues();
	
	void lock(K key);
	void unlock(K key);
	
	void addEntryListener(IEntryListener<K, V> listener);
	void removeEntryListener(IEntryListener<K, V> listener);
	
	void addEntryListener(IEntryListener<K,V> listener, K key);
	void removeEntryListener(IEntryListener<K,V> listener, K key);

	// populate a slave instance with the master content 
	void reloadSlave(String ip);
	
	boolean set(K key, V value, String instanceIP);
}
