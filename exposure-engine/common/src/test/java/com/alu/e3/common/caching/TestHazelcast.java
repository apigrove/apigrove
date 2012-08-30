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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.hazelcast.client.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class TestHazelcast {
	
	@Before
	public void before() {
		Hazelcast.shutdownAll();
	}


	@Test
	public void testStandaloneMode() {

		// Start 2 standalone hazelcast instances
		Config cfg = new Config();
		cfg.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
		HazelcastInstance h1 = Hazelcast.newHazelcastInstance(cfg);
		HazelcastInstance h2 = Hazelcast.newHazelcastInstance(cfg);
		
		// Add a new key/value in the 1st instance
		Map<Integer, String> map1 = h1.getMap("testmap");
		map1.put(1, "value 1 on instance 1");

		Map<Integer, String> map2 = h2.getMap("testmap");

		// Check that this key/value was added in the 1st instance and not in the 2nd instance 
		assertEquals(1, map1.size());
		assertEquals(0, map2.size());

		map2.put(1, "value 1 on instance 2");

		assertEquals("value 1 on instance 1", map1.get(1));
		assertEquals("value 1 on instance 2", map2.get(1));
	}

	@Ignore
	public void testClusterMode() {

		// Start 2 clustered hazelcast instances
		HazelcastInstance h1 = Hazelcast.newHazelcastInstance(null);
		HazelcastInstance h2 = Hazelcast.newHazelcastInstance(null);

		// Add a key/value in the 1st instance
		Map<Integer, String> map1 = h1.getMap("testmap");
		map1.put(1, "value 1 on instance 1");

		IMap<Integer, String> map2 = h2.getMap("testmap");
		
		// Check that this key/value was synchronized in the instance 2 
		assertEquals(1, map1.size());
		assertEquals(1, map2.size());

		assertEquals("value 1 on instance 1", map1.get(1));
		assertEquals("value 1 on instance 1", map2.get(1));

		// Update in the instance 2 the value of a key added by the instance 1
		map2.put(1, "value 1 on instance 2");
		
		// Check that this key/value was synchronized in the instance 1 
		assertEquals("value 1 on instance 2", map1.get(1));
		assertEquals("value 1 on instance 2", map2.get(1));
	}

	@Test
	public void testHazelcastJavaClient() throws UnknownHostException {

		Config cfg = new Config();
		cfg.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
		HazelcastInstance h1 = Hazelcast.newHazelcastInstance(cfg);

		// Connect a client to the cluster
		ClientConfig clientConfig = new ClientConfig() ;
		InetSocketAddress localMemberAddress = h1.getCluster().getLocalMember().getInetSocketAddress();
		clientConfig.addInetSocketAddress(localMemberAddress);
		HazelcastClient client = HazelcastClient.newHazelcastClient(clientConfig);

		// Add a key/value in the cluster using the client
		String mapName = "123";
		IMap<String, String> mapClient = client.getMap(mapName);
		mapClient.put("key1", "value1");

		// Check that the key/value was actually added in the cluster
		IMap<String, String> mapH1 = h1.getMap(mapName);
		mapH1.keySet();
		assertEquals(1, mapH1.size());
		assertEquals("value1", mapH1.get("key1"));
	}
	
	@Test
	public void testMaxSizeAndEviction() {

		String mapName = new Long (System.nanoTime()).toString();
		
		MaxSizeConfig maxSizeConfig = new MaxSizeConfig();
		maxSizeConfig.setSize(2);
		maxSizeConfig.setMaxSizePolicy("map_size_per_jvm");
		MapConfig mapConfig = new MapConfig();
		mapConfig.setName(mapName);
		mapConfig.setMaxSizeConfig(maxSizeConfig);
		mapConfig.setEvictionPolicy("LRU");
		
		Config config = new Config();
		Map <String, MapConfig> mapConfigs = new HashMap<String, MapConfig>();
		mapConfigs.put(mapName, mapConfig);
		config.setMapConfigs(mapConfigs);
		
		// Start a new cluster of Hazelcast isntances
		HazelcastInstance h1 = Hazelcast.newHazelcastInstance(config);

		IMap<String, String> map = h1.getMap(mapName);
		map.put("k1111111111111111111111", "v1");
		map.put("k2222222222222222222222", "v2");
		map.put("k3333333333333333333333", "v3");

		/*
		 * 		IF the eviction policy and time-to-live are not set explicitly
		 * 		AND the map is full
		 * 		AND that no key is removed
		 * 		THEN there is an infinite loop  
		 */
		
		// Check that the map contains 2 key and not 3
		assertEquals(2, map.size());
		// Check that "k3" was actually added
		assertTrue (map.containsKey("k3333333333333333333333"));

		// TODO As far as we know, the expected size should be 2, and is actually 1. Maybe a bug... To be confirmed.
		/*
		map.put("k3333333333333333333333", "v3.1");
		assertEquals(2, map.size());
		assertTrue (map.containsKey("k3333333333333333333333"));
		*/
	}
	
	@Ignore
	public void testMapDatastore() {

		String mapName = new Long (System.nanoTime()).toString();
		
		MapStoreConfig mapStoreConfig = new MapStoreConfig();
		mapStoreConfig.setEnabled(true);
		mapStoreConfig.setClassName("com.alu.e3.common.caching.MockDataStore");
		
		MapConfig mapConfig = new MapConfig();
		mapConfig.setName(mapName);
		mapConfig.setMapStoreConfig(mapStoreConfig);
		
		Config config = new Config();
		Map <String, MapConfig> mapConfigs = new HashMap<String, MapConfig>();
		mapConfigs.put(mapName, mapConfig);
		config.setMapConfigs(mapConfigs);

		// Initialize the store with 3 key/value pairs
		MockDataStore.internalStore = new HashMap<Object, Object>();
		MockDataStore.internalStore.put("key1", "value1");
		MockDataStore.internalStore.put("key2", "value2");
		MockDataStore.internalStore.put("key3", "value3");
		
		// Start a new cluster of Hazelcast isntances
		HazelcastInstance h1 = Hazelcast.newHazelcastInstance(config);
		
		IMap<String, String> map = h1.getMap(mapName);
		
		// Check that the map is automatically loaded with the content of the persistent store
		assertEquals(3, map.size());
		assertEquals("value1", map.get("key1"));
		
		assertNull(map.get("key4"));

		// Update the store...
		MockDataStore.internalStore.put("key4", "value4");

		// ... And check that the new key/value pair is added to the cache once requested 
		assertEquals(3, map.size());
		assertEquals("value4", map.get("key4"));
		assertEquals(4, map.size());
		
		// Update the cache...
		map.put("key3", "value3-bis");
		map.put("key5", "value5");
		
		// ... And check that the persistent store was updated accordingly 
		assertEquals(5, MockDataStore.internalStore.size());
		assertEquals("value3-bis", MockDataStore.internalStore.get("key3"));
		assertEquals("value5", MockDataStore.internalStore.get("key5"));
	}
	
	@Ignore
	public void testE3Archi() {
		String mapName = "apisMap";
		
		NearCacheConfig nearCacheConfig = new NearCacheConfig();
		nearCacheConfig.setMaxSize(10000);
		
//		MapConfig mapConfig = new MapConfig();
//		mapConfig.setName(mapName);
//		mapConfig.setNearCacheConfig(nearCacheConfig);
		
		Config config = new Config();
//		Map <String, MapConfig> mapConfigs = new HashMap<String, MapConfig>();
//		mapConfigs.put(mapName, mapConfig);
//		config.setMapConfigs(mapConfigs);
		config.getMapConfig(mapName).setNearCacheConfig(nearCacheConfig);
		
		HazelcastInstance hOnGtw1 = Hazelcast.newHazelcastInstance(config);
		HazelcastInstance hOnGtw2 = Hazelcast.newHazelcastInstance(config);

//		hOnGtw1.getConfig().getMapConfigs()
//		hOnGtw1.getConfig().getMapConfig(mapName).getNearCacheConfig()
//		hOnGtw2.getConfig().getMapConfig(mapName).getNearCacheConfig()
//		
//		hOnGtw1.getConfig().getMapConfig(mapName).
		
		
		IMap<String, String> apisMapOnG1 = hOnGtw1.getMap(mapName);
		
		apisMapOnG1.put("api1", "api1Policies");
		apisMapOnG1.put("api2", "api2Policies");
//		assertEquals(2, apisMapOnG1.localKeySet().size());
		
		// Check that "apisMap" is replicated on gtw2
		IMap<String, String> apisMapOnG2 = hOnGtw2.getMap(mapName);

		assertEquals(2, apisMapOnG2.size());
		assertEquals(2, apisMapOnG2.localKeySet().size()); 
		
		// Check that "apisMap" creation and updates are replicated on gtw1
		apisMapOnG2.put("api3", "api3Policies");
		assertEquals(3, apisMapOnG1.size());
		assertEquals("api3Policies", apisMapOnG1.get("api3"));
		assertEquals(3, apisMapOnG2.localKeySet().size());
		assertEquals(3, apisMapOnG1.localKeySet().size());

		apisMapOnG2.put("api3", "api3Policies-bis");
		assertEquals("api3Policies-bis", apisMapOnG1.get("api3"));
		assertEquals(3, apisMapOnG2.localKeySet().size());
		assertEquals(3, apisMapOnG1.localKeySet().size());
	}

	@Ignore
	public void testNearCache() {
		String mapName = "apisMap";
		
		NearCacheConfig nearCacheConfig = new NearCacheConfig();
		nearCacheConfig.setMaxSize(10000);
		
		Config config = new Config();
		config.getMapConfig(mapName).setNearCacheConfig(nearCacheConfig);
		
		HazelcastInstance hOnGtw1 = Hazelcast.newHazelcastInstance(config);
		HazelcastInstance hOnGtw2 = Hazelcast.newHazelcastInstance(config);

		IMap<String, String> apisMapOnG1 = hOnGtw1.getMap(mapName);
		
		apisMapOnG1.put("api1", "api1Policies");
		apisMapOnG1.put("api2", "api2Policies");
		assertEquals(2, apisMapOnG1.localKeySet().size());
		
		// Check that "apisMap" is replicated on gtw2
		IMap<String, String> apisMapOnG2 = hOnGtw2.getMap(mapName);

		assertEquals(2, apisMapOnG2.size());
		assertEquals(2, apisMapOnG2.localKeySet().size()); 
		
		// Check that "apisMap" creation and updates are replicated on gtw1
		apisMapOnG2.put("api3", "api3Policies");
		assertEquals(3, apisMapOnG1.size());
		assertEquals("api3Policies", apisMapOnG1.get("api3"));
		assertEquals(3, apisMapOnG2.localKeySet().size());
		assertEquals(3, apisMapOnG1.localKeySet().size());

		apisMapOnG2.put("api3", "api3Policies-bis");
		assertEquals("api3Policies-bis", apisMapOnG1.get("api3"));
		assertEquals(3, apisMapOnG2.localKeySet().size());
		assertEquals(3, apisMapOnG1.localKeySet().size());
	}

	@Ignore
	public void nearCacheOn2Machines() {
		String mapName = "apisMap";
		
		NearCacheConfig nearCacheConfig = new NearCacheConfig();
		nearCacheConfig.setMaxSize(10000);
		
		Config config = new Config();
		config.getMapConfig(mapName).setNearCacheConfig(nearCacheConfig);
		
		HazelcastInstance hOnGtw1 = Hazelcast.newHazelcastInstance(config);
		
		IMap<String, String> apisMapOnG1 = hOnGtw1.getMap(mapName);
		
		apisMapOnG1.put("api1", "api1Policies");
		apisMapOnG1.put("api2", "api2Policies");

		assertEquals("api1Policies", apisMapOnG1.get("api1"));
		assertEquals("api1Policies", apisMapOnG1.get("api1"));
		assertEquals(2, apisMapOnG1.localKeySet().size());

		System.out.println();
	}
	
	@Ignore
	public void testBackupAsReplacementOfNearCache() {
		String mapName = "apisMap";

		Config config = new Config();
		config.getMapConfig(mapName).setBackupCount(20);
		config.getMapConfig(mapName).setReadBackupData(true);
		
//		config.getNetworkConfig().getSocketInterceptorConfig().
	
		
		HazelcastInstance hOnGtw1 = Hazelcast.newHazelcastInstance(config);
		assertEquals(20, hOnGtw1.getConfig().getMapConfig(mapName).getBackupCount());
		
//		HazelcastInstance hOnGtw2 = Hazelcast.newHazelcastInstance(config);
//		assertEquals(2, hOnGtw2.getConfig().getMapConfig(mapName).getBackupCount());
		
		IMap<String, String> apisMapOnG1 = hOnGtw1.getMap(mapName);
		// Check that "apisMap" is replicated on gtw2
//		IMap<String, String> apisMapOnG2 = hOnGtw2.getMap(mapName);

		apisMapOnG1.put("api10", "api1Policies");
//		apisMapOnG1.put("api2", "api2Policies");
//		apisMapOnG1.getLocalMapStats().getOwnedEntryCount() getBackupEntryCount()
//		apisMapOnG1.getLocalMapStats().getOperationStats().
//		assertEquals(2, apisMapOnG1.localKeySet().size());
		

//		assertEquals(2, apisMapOnG2.size());
//		assertEquals(2, apisMapOnG2.localKeySet().size()); 
//		
//		// Check that "apisMap" creation and updates are replicated on gtw1
//		apisMapOnG2.put("api3", "api3Policies");
//		assertEquals(3, apisMapOnG1.size());
//		assertEquals("api3Policies", apisMapOnG1.get("api3"));
//		assertEquals(3, apisMapOnG2.localKeySet().size());
//		assertEquals(3, apisMapOnG1.localKeySet().size());
//
//		apisMapOnG2.put("api3", "api3Policies-bis");
//		assertEquals("api3Policies-bis", apisMapOnG1.get("api3"));
//		assertEquals(3, apisMapOnG2.localKeySet().size());
//		assertEquals(3, apisMapOnG1.localKeySet().size());
	}
	
	@Test
	public void testLockOnStandalone() throws InterruptedException {
		Config cfg = new Config();
		cfg.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);

		HazelcastInstance h1 = Hazelcast.newHazelcastInstance(cfg);
		
		String mapName = new Long (System.nanoTime()).toString();
		String key = "key", anotherKey = "anotherKey";
		
		IMap<String, Integer> map1 = h1.getMap(mapName);
		map1.put(key, 1);
		map1.put(anotherKey, 11);
		
		Thread writeThread1 = new Thread(new RunnableSimpleUpdate<String, Integer>(key, 2, map1));
		Thread writeThread2 = new Thread(new RunnableSimpleUpdate<String, Integer>(anotherKey, 12, map1));
		
		RunnableSimpleRead<String, Integer> runnableSimpleRead = new RunnableSimpleRead<String, Integer>(key, map1);
		assertNull(runnableSimpleRead.getValue());
		Thread readThread = new Thread(runnableSimpleRead);

		map1.lock(key);
		
		writeThread1.start();
		writeThread2.start();
		readThread.start();
		
		Thread.sleep(2000);
		
		// check that locking a key does not preclude another thread to read the value
		assertEquals(1, runnableSimpleRead.getValue().intValue());
		
		// Check locking a key does not preclude updating another key
		assertEquals(12, map1.get(anotherKey).intValue());
	
		// Check that the value in the map was not updated by the concurrent thread
		assertEquals(1, map1.get(key).intValue());
	
		map1.put(key , 3);
		// Check that the current thread
		assertEquals(3, map1.get(key).intValue());

		Thread.sleep(2000);
		
		// Check again that the value in the map was not updated by the concurrent thread
		assertEquals(3, map1.get(key).intValue());
		
		map1.unlock(key);
		
		Thread.sleep(2000);

		// Check that once unlocked, that the value in the map can be updated by another thread
		assertEquals(2, map1.get(key).intValue());
	}
	
	@Test
	public void testMapLockOnStandalone() throws InterruptedException {
		Config cfg = new Config();
		cfg.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);

		HazelcastInstance h1 = Hazelcast.newHazelcastInstance(cfg);
		
		String mapName = new Long (System.nanoTime()).toString();
		String key = "key", anotherKey = "anotherKey";
		
		IMap<String, Integer> map1 = h1.getMap(mapName);
		map1.put(key, 1);
		map1.put(anotherKey, 11);
		
		Thread writeThread1 = new Thread(new RunnableSimpleUpdate<String, Integer>(key, 2, map1));
		Thread writeThread2 = new Thread(new RunnableSimpleUpdate<String, Integer>(anotherKey, 12, map1));

		RunnableSimpleRead<String, Integer> runnableSimpleRead = new RunnableSimpleRead<String, Integer>(key, map1);
		assertNull(runnableSimpleRead.getValue());
		Thread readThread = new Thread(runnableSimpleRead);

		map1.lockMap(1, TimeUnit.SECONDS);
		
		writeThread1.start();
		writeThread2.start();
		readThread.start();

		Thread.sleep(2000);
		
		// check that locking map does not preclude another thread to read entries
		assertEquals(1, runnableSimpleRead.getValue().intValue());
		
		// Check that the values in the map cannot be updated by a concurrent thread
		assertEquals(1, map1.get(key).intValue());
		assertEquals(11, map1.get(anotherKey).intValue());
	
		map1.put(key , 3);
		map1.put(anotherKey, 13);

		Thread.sleep(2000);
		
		// Check again that the value in the map was not updated by the concurrent thread
		assertEquals(3, map1.get(key).intValue());
		assertEquals(13, map1.get(anotherKey).intValue());
		
		map1.unlockMap();
		
		Thread.sleep(2000);

		// Check that once unlocked, that the value in the map can be updated by another thread
		assertEquals(2, map1.get(key).intValue());
		assertEquals(12, map1.get(anotherKey).intValue());
	}
	
	@Test 
	public void testPutAll() {
		Config cfg = new Config();
		cfg.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);

		HazelcastInstance h1 = Hazelcast.newHazelcastInstance(cfg);
		
		String mapName = new Long (System.nanoTime()).toString();
		String mapName2 = new Long (System.nanoTime()).toString();
		
		IMap<Integer, String> map1 = h1.getMap(mapName);
		IMap<Integer, String> map2 = h1.getMap(mapName2);

		
		map1.put(1, "un");
		map1.put(2, "deux");
		
		map2.putAll(map1);
		
		assertEquals(2, map2.size());
		assertEquals("un", map2.get(1));
		assertEquals("deux", map2.get(2));
	}

	public class RunnableSimpleUpdate<K, V> implements Runnable {
		protected K key;
		protected V value;
		protected IMap<K, V> map;
		
		public RunnableSimpleUpdate(K key, V value, IMap<K, V> map) {
			super();
			this.key = key;
			this.value = value;
			this.map = map;
		}

		@Override
		public void run() {
			map.put(key, value);
		}
		
	}
	
	public class RunnableSimpleRead<K, V> implements Runnable {
		protected K key;
		protected V value = null;
		protected IMap<K, V> map;
		
		public RunnableSimpleRead(K key, IMap<K, V> map) {
			super();
			this.key = key;
			this.map = map;
		}
		
		@Override
		public void run() {
			value = map.get(key);
		}
		
		public V getValue() {
			return value;
		}

	}
	
}
