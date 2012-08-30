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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.net.UnknownHostException;
import java.util.jar.JarFile;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alu.e3.common.tools.BundleTools;
import com.hazelcast.client.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.Transaction;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:common.spring-test.xml"} )
public class TestICacheImpl {

	private final static String AUTH_MAP_NAME = "auth-cache";

	@Autowired
	protected ICacheTable<String, Integer> authCache;

	@Autowired
	protected HazelcastInstance instance;

	@Test
	public void testAuthHazelcastInstance() {
		assertNotNull(instance);
		assertFalse(instance.getConfig().getNetworkConfig().getJoin().getMulticastConfig().isEnabled());

		IMap<String, String> map = instance.getMap(AUTH_MAP_NAME);
		assertNotNull(map);

		assertEquals("LRU", instance.getConfig().getMapConfig(AUTH_MAP_NAME).getEvictionPolicy());
	}

	@Test
	public void testAuthCacheSetUp() {
		assertNotNull(authCache);
	}

	@Test (expected=IllegalStateException.class)
	public void testCommitThenRollback() {
		Transaction transaction = instance.getTransaction();
		transaction.begin();
		IMap<String, String> imap = instance.getMap(AUTH_MAP_NAME);
		assertNull(imap.get("key"));
		imap.put("key", "value");
		transaction.commit();
		// Check that when trying to rollback after having commited will throw an IllegalStageException
		transaction.rollback();		
	}

	@Test
	public void testHazelcastJavaClient() throws UnknownHostException {
		// Connect a client to the cluster
		ClientConfig clientConfig = new ClientConfig() ;
		clientConfig.addInetSocketAddress(instance.getCluster().getLocalMember().getInetSocketAddress());
		HazelcastClient client = HazelcastClient.newHazelcastClient(clientConfig);

		// Check that the entry is not in the cache before the client add it
		assertNull(authCache.get("key1"));

		// Add a key/value in the cluster using the client
		IMap<String, String> mapClient = client.getMap(AUTH_MAP_NAME);
		mapClient.put("key1", "value1");

		// Check that the key/value was actually added in the cluster
		assertEquals("value1", authCache.get("key1"));
	}

	@Test
	@Ignore
	public void testJarProvisioning() throws Exception {

		String bundleFilePath = getClass().getResource("/test.jar").getFile();
		File bundleFile = new File (bundleFilePath);

		// Connect a client to the cluster
		ClientConfig clientConfig = new ClientConfig() ;
		clientConfig.addInetSocketAddress(instance.getCluster().getLocalMember().getInetSocketAddress());
		HazelcastClient client = HazelcastClient.newHazelcastClient(clientConfig);

		// Get a reference on the remote routes
		IQueue<ProvisionRouteTask> provisionRouteQueue = client.getQueue("bundles-to-deploy");
		assertEquals(0, provisionRouteQueue.size());

		IQueue<ProvisionRouteResult> provisionRouteResultQueue = client.getQueue("deployment-result");
		assertEquals(0, provisionRouteResultQueue.size());

		// Send the bundle to be deployed to the Gateway/s
		long provisionRouteTaskId = System.nanoTime();
		byte[] serializedBundle = BundleTools.file2ByteArray(bundleFile);
		ProvisionRouteTask provisionRouteTask = new ProvisionRouteTask(provisionRouteTaskId, serializedBundle);

		provisionRouteQueue.add(provisionRouteTask);

		// Wait until the end of the deployment
		ProvisionRouteResult provisionRouteResult =	provisionRouteResultQueue.take();

		JarFile generatedBundle = new JarFile(bundleFile);
		String newBundlePath = provisionRouteResult.getBundlePath();
		JarFile outputBundle = new JarFile(newBundlePath);

		assertEquals (generatedBundle.size(), outputBundle.size());
		assertEquals(generatedBundle.getEntry("log4j.properties").getSize(), outputBundle.getEntry("log4j.properties").getSize());
		assertEquals(generatedBundle.getEntry("log4j.properties").getCrc(), outputBundle.getEntry("log4j.properties").getCrc());

		// check that the provision result match with the route to be provisioned
		assertEquals (provisionRouteTaskId, provisionRouteResult.getProvisionRouteId());

		// check that the task for the route to be provisionned does not exist anymore
		assertEquals(0, provisionRouteQueue.size());

		// check that all provisioning result were processed
		assertEquals(0, provisionRouteResultQueue.size());

		// Send the bundle to be deployed to the Gateway/s
		provisionRouteTaskId = System.nanoTime();
		provisionRouteTask = new ProvisionRouteTask(provisionRouteTaskId, serializedBundle);

		provisionRouteQueue.add(provisionRouteTask);

		// Wait until the end of the deployment
		provisionRouteResult =	provisionRouteResultQueue.take();

		// check that the provision result match with the route to be provisioned
		assertEquals (provisionRouteTaskId, provisionRouteResult.getProvisionRouteId());

		// check that the task for the route to be provisionned does not exist anymore
		assertEquals(0, provisionRouteQueue.size());

		// check that all provisioning result were processed
		assertEquals(0, provisionRouteResultQueue.size());
	}

	/*
	 *  Utils
	 */

	public class RunnableSimpleUpdate<K, V> implements Runnable {
		protected K key;
		protected V value;
		protected ICacheTable<K, V> cache;

		public RunnableSimpleUpdate(K key, V value, ICacheTable<K, V> cache) {
			super();
			this.key = key;
			this.value = value;
			this.cache = cache;
		}

		@Override
		public void run() {
			cache.set(key, value);
		}

	}

	public class RunnableSimpleRead<K, V> implements Runnable {
		protected K key;
		protected V value = null;
		protected ICacheTable<K, V> cache;

		public RunnableSimpleRead(K key, ICacheTable<K, V> cache) {
			super();
			this.key = key;
			this.cache = cache;
		}

		@Override
		public void run() {
			value = cache.get(key);
		}

		public V getValue() {
			return value;
		}
	}

}
