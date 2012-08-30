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

public interface IAckCacheTable<K, V extends IAckData> extends ICacheTable<K, V> {

	/**
	 * Sets an element in the table with acknowledgmenent
	 * @param key The key of the object to set
	 * @param value An IAckData object, whose id field will be used to get queue name for acknowledgment
	 * @return A CacheAck instance, value is null
	 * @throws InterruptedException 
	 */
	boolean setWithAck(K key, V value);

	
	/**
	 * Removes an element from the table with acknowledgmenent
	 * @param key The key of the object to set
	 * @param value An IAckData object, whose id field will be used to get queue name for acknowledgment
	 * @return A CacheAck instance, with value attribute corresponding to the value removed
	 */
	boolean removeWithAck(K key);
}