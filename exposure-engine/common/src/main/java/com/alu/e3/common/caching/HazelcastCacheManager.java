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

import java.util.HashMap;
import java.util.Map;

import com.alu.e3.common.E3Constant;
import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.common.osgi.api.ITopologyClient;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.QueueConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;

public class HazelcastCacheManager implements ICacheManager {

	private static CategoryLogger logger = CategoryLoggerFactory.getLogger(HazelcastCacheManager.class, Category.DMGR);
	
	protected ITopologyClient topologyClient;

	protected HazelcastInstance hazelcastInstance;

	protected Map<String, HazelcastCache<?, ?>> tables = new HashMap<String, HazelcastCache<?, ?>>();
	protected Map<String, HazelcastCacheQueue<?>> queues = new HashMap<String, HazelcastCacheQueue<?>>();

	public HazelcastCacheManager() {}
	
	public void init() {
		init(false);
	}
	
	public void init(boolean portAutoIncrement) {
		logger.debug("Init of cacheManager with portAutoIncrement: {}", portAutoIncrement);
		// check if an Hazelcast instance is already running
		setHazelcastInstance(Hazelcast.getHazelcastInstanceByName(E3Constant.HAZELCAST_NAME));
		
		if (this.hazelcastInstance  != null) {
			// should not happen except when running the JUnit
			logger.warn("HazelcastCacheManager: HazelcastInstance is already running");
		}
		else {
			logger.debug("Normal start of new HazelcastInstance");
			// create the instance
			Config cfg = new Config();
			cfg.setPort(E3Constant.HAZELCAST_PORT);
			cfg.setPortAutoIncrement(portAutoIncrement);
			
			cfg.setInstanceName(E3Constant.HAZELCAST_NAME);

			cfg.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
			
			// Close doors that are not supposed to be opened
			cfg.setProperty("hazelcast.memcache.enabled", "false");
			cfg.setProperty("hazelcast.rest.enabled", "false");
			cfg.setProperty("hazelcast.mancenter.enabled", "false");
			
			cfg.setProperty("hazelcast.version.check.enabled", "false");
			

			setHazelcastInstance(Hazelcast.newHazelcastInstance(cfg));

			logger.debug("Normal start: done.");
		}
	}

	/**
	 * Called when the bundle is stopped.
	 */
	public void destroy() {
		logger.debug("Destroy HazelcastInstance ...");
		this.hazelcastInstance.getLifecycleService().shutdown();

		int attempt = 10;
		do {
			logger.debug("Waiting HazelcastInstance termination, attempt:{}", attempt);
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				logger.error("Problem while waiting HazelcastInstance to terminate !", e);
			}
		} while(this.hazelcastInstance.getLifecycleService().isRunning() && attempt-- > 0 );
		
		// Force killing it !
		if (this.hazelcastInstance.getLifecycleService().isRunning())
			this.hazelcastInstance.getLifecycleService().kill();
		
		logger.debug("Destroy: Done and forced.");
	}
	
	public void setTopologyClient(ITopologyClient topologyClient) {
		logger.debug("Set ITopologyClient on cacheManager");
		
		this.topologyClient = topologyClient;
	}

	public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
	}

	@Override
	public <X, Y> ICacheTable<X, Y> createTable(String name, boolean isReplicated, Map<String, String> properties) {
		// In this method context <X, Y> is used as generic declaration instead of <K, V>
		// cause this whole class is not generic and
		// we may keep in mind this.'tables' is a generic on <?, ?> (multiple K,V different couples).
		if(tables.get(name) == null) {
			setTableProperties(name, properties);
			HazelcastCache<X, Y> table = new HazelcastCache<X, Y>();
			tables.put(name, table);

			IMap<X, Y> map = hazelcastInstance.getMap(name);
			table.setMap(map);
			table.setTopologyClient(topologyClient);
			table.setIsReplicated(isReplicated);

			return table;
		}
		else {
			// error: already created
			return null;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <K, V> ICacheTable<K, V> createOrGetTable(String name, boolean isReplicated, Map<String, String> properties) {
		ICacheTable<K, V> table = createTable(name, isReplicated, properties);
		// Here, we really try a cast from this.'table' elements
		// which are <any,any> to a specific one <K, V>. 
		if (table == null)
			table = (ICacheTable<K, V>) tables.get(name);
		return table;
	}

	@Override
	public <K, V extends IAckData> ICacheTable<K, V> createAckTable(String name, boolean isReplicated, Map<String, String> properties) {
		// Same as createTable(...)
		// but with <K, V>
		if(tables.get(name) == null) {
			setTableProperties(name, properties);

			HazelcastAckCache<K, V> table = new HazelcastAckCache<K, V>();
			tables.put(name, table);

			IMap<K, V> map = hazelcastInstance.getMap(name);
			table.setMap(map);
			table.setTopologyClient(topologyClient);
			table.setIsReplicated(isReplicated);

			return table;
		}
		else {
			// error: already created
			return null;
		}
	}

	@Override
	public <E> ICacheQueue<E> createQueue(String name, Map<String, String> properties) {
		if(queues.get(name) == null) {
			setQueueProperties(name, properties);

			HazelcastCacheQueue<E> queue = new HazelcastCacheQueue<E>();
			queues.put(name, queue);

			IQueue<E> hzQueue = hazelcastInstance.getQueue(name);
			queue.setQueue(hzQueue);
			//			queue.setTopology(topology);
			//			queue.setIsReplicated(isReplicated);

			return queue;
		}
		else {
			// error: already created
			return null;
		}
	}

	@Override
	public <E> ICacheQueue<E> getOrCreateQueue(String name, Map<String, String> properties) {
		@SuppressWarnings("unchecked")
		ICacheQueue<E> queue = (ICacheQueue<E>) queues.get(name);
		if(queue == null)
			queue = createQueue(name, properties);

		return queue;
	}

	private void setTableProperties(String name, Map<String, String> properties) {
		if(properties != null) { 
			Config cfg = hazelcastInstance.getConfig();
			MapConfig mapCfg = cfg.getMapConfig(name);
			if(mapCfg == null) {
				mapCfg = new MapConfig();
				cfg.addMapConfig(mapCfg);
			}

			String value = null;
			if((value = properties.get("eviction-policy")) != null) {
				mapCfg.setEvictionPolicy(value);
			}

			if((value = properties.get("eviction-percentage")) != null) {
				mapCfg.setEvictionPercentage(Integer.parseInt(value));
			}

			if((value = properties.get("time-to-live-seconds")) != null) {
				mapCfg.setTimeToLiveSeconds(Integer.parseInt(value));
			}

			// TODO: remove the comment to enable LDAP persistence
			/*
			if((value = properties.get("map-store-name")) != null) {

				String mapStoreIP = properties.get("map-store-ip");
				String mapStorePort = properties.get("map-store-port");
				String mapStoreUser = properties.get("map-store-user");
				String mapStorePassword = properties.get("map-store-password");

				MapStoreConfig mapStoreConfig = mapCfg.getMapStoreConfig();

				if (mapStoreConfig == null) {
					mapStoreConfig = new MapStoreConfig();
				}

				mapStoreConfig.setEnabled(true);

				HazelcastDataStoreLDAP dataStore = new HazelcastDataStoreLDAP("ou="+value, mapStoreIP, mapStorePort, mapStoreUser, mapStorePassword);

				mapStoreConfig.setImplementation(dataStore);

				mapCfg.setMapStoreConfig(mapStoreConfig);
			}
				 */
		}
	}

	private void setQueueProperties(String name, Map<String, String> properties) {
		if(properties != null) { 
			Config cfg = hazelcastInstance.getConfig();
			QueueConfig queueCfg = cfg.getQueueConfig(name);
			if(queueCfg == null) {
				queueCfg = new QueueConfig();
				cfg.addQueueConfig(queueCfg);
			}
		}
	}

}
