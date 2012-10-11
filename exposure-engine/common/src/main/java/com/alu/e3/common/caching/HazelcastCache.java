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

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.alu.e3.common.E3Constant;
import com.alu.e3.common.caching.internal.HandlerPool;
import com.alu.e3.common.caching.internal.MapHandler;
import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.common.osgi.api.ITopologyClient;
import com.alu.e3.data.DataEntryEvent;
import com.hazelcast.client.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.IMap;

public class HazelcastCache<K, V> implements ICacheTable<K, V> {

	protected static final CategoryLogger logger = CategoryLoggerFactory.getLogger(HazelcastCache.class, Category.DMGR);

	protected IMap<K, V> localMap;
	protected boolean isReplicated = false;
	protected String name;

	private Map<IEntryListener<K, V>, EntryListener<K, V>> entryListenersMap;
	protected ITopologyClient topologyClient;
	
	protected HandlerPool<String, MapHandler<K,V>> mapHandlerPool = new HandlerPool<String, MapHandler<K,V>>(E3Constant.HAZELCAST_HANDLER_POOL_MAX_SIZE);


	public void setTopologyClient(ITopologyClient topologyClient) {
		this.topologyClient = topologyClient;
	}

	public IMap<K, V> getMap() {
		return localMap;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setMap(IMap<K, V> map) throws InvalidParameterException {
		if (map == null)
			throw new InvalidParameterException("The parameter map is mandatory");

		this.localMap = map;
		this.name = map.getName();
	}

	public void setIsReplicated(boolean isReplicated) {
		this.isReplicated = isReplicated;
	}

	@Override
	public V get(Object key) {
		if (localMap == null) {
			logger.error("Attempt to get a key on a cache without a map");
		}

		ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(HazelcastCacheManager.class.getClassLoader());

		V retValue = localMap == null ? null : localMap.get(key);

		Thread.currentThread().setContextClassLoader(previousClassLoader);

		return retValue;
	}

	@Override
	public boolean set(K key, V value) {
		if (localMap == null) {
			logger.error("Attempt to put a key in a cache without a map");
			return false;
		}

		ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(HazelcastCacheManager.class.getClassLoader());

		localMap.put(key, value);

		if (isReplicated && topologyClient != null) {
			Set<String> instances = getAllInstanceIPs();

			MapHandler<K, V> mapHandler = null;
			for (String instanceIP : instances) {
				try {
					mapHandler = mapHandlerPool.get(new StringBuffer(instanceIP).append(localMap.getName()).toString());
					if (mapHandler==null)
					{
						mapHandler = new MapHandler<K, V>(instanceIP, localMap.getName());
						mapHandlerPool.put(new StringBuffer(instanceIP).append(localMap.getName()).toString(), mapHandler);
					}else
					{
						if (!mapHandler.isClientActive())
						{
							mapHandler.dispose();
							String poolKey = new StringBuffer(instanceIP).append(localMap.getName()).toString();
							mapHandlerPool.remove(poolKey);
							mapHandler = new MapHandler<K, V>(instanceIP, localMap.getName());
							mapHandlerPool.put(poolKey, mapHandler);
						}
					}

					if (!mapHandler.isLocal()) {
						IMap<K, V> map = mapHandler.getMap();
						map.put(key, value);
					}
				} catch (IllegalStateException ise) {
					logger.warn("Was not able to set the entry " + key + " of the cache " + localMap.getName() + " for gateway " + instanceIP + ". This probably means that this instance is down.", ise);
				} catch(Exception e){
					logger.error("Unexpected error updating the entry " + key + " of the cache " + localMap.getName() + " for gateway " + instanceIP, e);
				}

			}
		}

		Thread.currentThread().setContextClassLoader(previousClassLoader);

		return true;
	}

	@Override
	public boolean set(K key, V value, String instanceIP) {
		logger.debug("Setting map:{}, key:{} value:{} on node ip:{}", new Object[]{name, key, value, instanceIP});
		
		if (localMap == null) {
			logger.error("Attempt to put a key in a cache without a map");
			return false;
		}

		ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(HazelcastCacheManager.class.getClassLoader());

		MapHandler<K, V> mapHandler = null;
		try {
			mapHandler = mapHandlerPool.get(new StringBuffer(instanceIP).append(localMap.getName()).toString());
			if (mapHandler==null)
			{
				logger.debug("Creating MapHandler map:{}, key:{} on node ip:{}", new Object[]{name, key, instanceIP});
				mapHandler = new MapHandler<K, V>(instanceIP, localMap.getName());
				mapHandlerPool.put(new StringBuffer(instanceIP).append(localMap.getName()).toString(), mapHandler);
			}else
			{
				logger.debug("Reusing MapHandler map:{}, key:{} on node ip:{}", new Object[]{name, key, instanceIP});
				if (!mapHandler.isClientActive())
				{
					logger.debug("Recreating MapHandler map:{}, key:{} on node ip:{}", new Object[]{name, key, instanceIP});
					mapHandler.dispose();
					String poolKey = new StringBuffer(instanceIP).append(localMap.getName()).toString();
					mapHandlerPool.remove(poolKey);
					mapHandler = new MapHandler<K, V>(instanceIP, localMap.getName());
					mapHandlerPool.put(poolKey, mapHandler);
				}
			}

			if (! mapHandler.isLocal()) {
				logger.debug("Pushing value map:{}, key:{} on node ip:{}", new Object[]{name, key, instanceIP});
				IMap<K, V> map = mapHandler.getMap();
				map.put(key, value);
			}
		} 
		catch (IllegalStateException ise) {
			logger.error("Was not able to set the entry " + key + " of the cache " + localMap.getName() + " for gateway " + instanceIP + ". This probably means that this instance is down.", ise);
		} 

		Thread.currentThread().setContextClassLoader(previousClassLoader);

		return true;
	}

	@Override
	public void reloadSlave(String ip) {
		if (logger.isDebugEnabled()) {
			logger.debug("Reloading table:{} for node ip:{}", name, ip);
		}
		
		if (localMap == null) {
			logger.warn("Attempt to reload a slave cache from a master with no map");
			return;
		}

		MapHandler<K, V> mapHandler = mapHandlerPool.get(new StringBuffer(ip).append(localMap.getName()).toString());
		try {
			if (mapHandler==null)
			{
				if (logger.isDebugEnabled()) {
					logger.debug("Creating mapHandler for table:{} for node ip:{}", name, ip);
				}
				mapHandler = new MapHandler<K, V>(ip, localMap.getName());
				mapHandlerPool.put(new StringBuffer(ip).append(localMap.getName()).toString(), mapHandler);
			}else
			{
				if (logger.isDebugEnabled()) {
					logger.debug("Reusing mapHandler for table:{} for node ip:{}", name, ip);
				}
				if (!mapHandler.isClientActive())
				{
					if (logger.isDebugEnabled()) {
						logger.debug("Recreating mapHandler for table:{} for node ip:{}", name, ip);
					}
					mapHandler.dispose();
					String poolKey = new StringBuffer(ip).append(localMap.getName()).toString();
					mapHandlerPool.remove(poolKey);
					mapHandler = new MapHandler<K, V>(ip, localMap.getName());
					mapHandlerPool.put(poolKey, mapHandler);
				}
			}
			IMap<K, V> map = mapHandler.getMap();
			if (!mapHandler.isLocal()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Pushing allData for table:{} for node ip:{}", name, ip);
				}
				map.putAll(localMap);
			}
		} catch (Exception e) {
			logger.warn("Unable to reload slave", e);
		}
	}

	@Override
	public V remove(Object key) {

		if (localMap == null) {
			logger.error("Attempt to remove a key in a cache without a map");
			return null;
		}

		ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(HazelcastCacheManager.class.getClassLoader());

		V value = localMap.remove(key);

		if (value == null) {
			logger.error("Error removing a key from the master cache");
			Thread.currentThread().setContextClassLoader(previousClassLoader);
			return null;
		}

		if (isReplicated && topologyClient != null) {
			Set<String> instances = getAllInstanceIPs();

			MapHandler<K, V> mapHandler = null;
			for (String instanceIP : instances) {
				try {
					mapHandler = mapHandlerPool.get(new StringBuffer(instanceIP).append(localMap.getName()).toString());
					if (mapHandler==null)
					{
						mapHandler = new MapHandler<K, V>(instanceIP, localMap.getName());
						mapHandlerPool.put(new StringBuffer(instanceIP).append(localMap.getName()).toString(), mapHandler);
					}else
					{
						if (!mapHandler.isClientActive())
						{
							mapHandler.dispose();
							String poolKey = new StringBuffer(instanceIP).append(localMap.getName()).toString();
							mapHandlerPool.remove(poolKey);
							mapHandler = new MapHandler<K, V>(instanceIP, localMap.getName());
							mapHandlerPool.put(poolKey, mapHandler);
						}
					}
					
					IMap<K, V> map = mapHandler.getMap();
					if (! mapHandler.isLocal()) {
						map.remove(key);
					}
				} catch (IllegalStateException ise) {
					logger.warn("Was not able to set the entry " + key + " of the cache " + localMap.getName() + " for gateway " + instanceIP + ". This probably means that this instance is down.");
				} finally {
					if (mapHandler != null)
						mapHandler.dispose();
				}
			}
		}

		Thread.currentThread().setContextClassLoader(previousClassLoader);

		return value;
	}

	protected Set<String> getAllInstanceIPs() {
		Set<String> instanceIPs = new HashSet<String>();

		// add all gateway internal IPs to use
		instanceIPs.addAll(getInternalIPs(E3Constant.E3GATEWAY));
		// add all gateway external IPs to use
		instanceIPs.addAll(getExternalIPs(E3Constant.E3GATEWAY));

		return instanceIPs;
	}

	private Set<String> getInternalIPs(String type) {
		// get manager area
		String managerArea = topologyClient.getMyArea();
		if (logger.isDebugEnabled()) {
			logger.debug("Current manager area '" + managerArea + "'");
			logger.debug("Adding gateway internal ips for manager area '" + managerArea + "'");
		}
		// use internal ip for gateways having the same area than the manager
		return topologyClient.getInternalIPsOfType(type, managerArea);
	}

	private Set<String> getExternalIPs(String type) {
		// get all other areas than the manager's one
		Set<String> otherAreas = topologyClient.getAllOtherAreas();
		Set<String> externalGatewayIPs = new HashSet<String>();

		// iterate through area list to gather external gateway ips
		for (String area : otherAreas) {
			if (logger.isDebugEnabled()) {
				logger.debug("Adding gateway external ips non manager for area '" + area + "'");
			}
			externalGatewayIPs.addAll(topologyClient.getExternalIPsOfType(type, area));
		}

		return externalGatewayIPs;
	}

	@Override
	public boolean containsKey(Object key) {
		if (localMap == null) {
			logger.error("Attempt to check if a key is contained in a cache without a map");
		}

		return localMap == null ? false : localMap.containsKey(key);
	}

	@Override
	public Set<K> getAllKeys() {
		if (localMap == null) {
			logger.error("Attempt to get all keys of a cache without a map");
		}

		return localMap == null ? new HashSet<K>() : localMap.keySet();
	}

	@Override
	public Collection<V> getAllValues() {
		if (localMap == null) {
			logger.error("Attempt to get all values of a cache without a map");
		}

		return localMap == null ? new HashSet<V>() : localMap.values();
	}

	protected HazelcastClient getHazelcastClient(String ip) {

		ClientConfig clientConfig = new ClientConfig();

		clientConfig.addAddress(ip + ":" + E3Constant.HAZELCAST_PORT);
		HazelcastClient client = HazelcastClient.newHazelcastClient(clientConfig);

		return client;
	}

	@Override
	public void lock(K key) {
		if (localMap == null) {
			logger.error("Attempt to lock a key in a cache without a map");
		} else {
			localMap.lock(key);
		}
	}

	@Override
	public void unlock(K key) {
		if (localMap == null) {
			logger.error("Attempt to unlock a key in a cache without a map");
		} else {
			localMap.unlock(key);
		}
	}

	@Override
	public void clear() {
		if (localMap == null) {
			logger.error("Attempt to clear a cache without a map");
		} else {
			localMap.clear();
		}
	}

	@Override
	public void addEntryListener(final IEntryListener<K, V> listener) {
		EntryListener<K, V> hzListener = new HZListener(listener);

		getEntryListenersMap().put(listener, hzListener);

		localMap.addEntryListener(hzListener, true);
	}

	@Override
	public void removeEntryListener(IEntryListener<K, V> listener) {
		EntryListener<K, V> hzListener = getEntryListenersMap().get(listener);
		getEntryListenersMap().remove(listener);
		localMap.removeEntryListener(hzListener);
	}

	/**
	 * @return the entryListenersMap
	 */
	protected Map<IEntryListener<K, V>, EntryListener<K, V>> getEntryListenersMap() {
		if (entryListenersMap == null)
			entryListenersMap = new HashMap<IEntryListener<K, V>, EntryListener<K, V>>();
		return entryListenersMap;
	}

	@Override
	public void addEntryListener(IEntryListener<K, V> listener, K key) {
		EntryListener<K, V> hzListener = new HZListener(listener);

		getEntryListenersMap().put(listener, hzListener);

		localMap.addEntryListener(hzListener, key, true);
	}

	@Override
	public void removeEntryListener(IEntryListener<K, V> listener, K key) {
		EntryListener<K, V> hzListener = getEntryListenersMap().get(listener);
		getEntryListenersMap().remove(listener);
		localMap.removeEntryListener(hzListener, key);
	}
	
	public class HZListener implements EntryListener<K, V> {
		protected IEntryListener<K, V> listener;
		
		public HZListener(IEntryListener<K, V> listener) {
			this.listener = listener;
		}
		
		@Override
		public void entryAdded(EntryEvent<K, V> event) {
			DataEntryEvent<K, V> dataEntryEvent = new DataEntryEvent<K, V>(event.getKey(), event.getValue());
			listener.entryAdded(dataEntryEvent);
		}

		@Override
		public void entryRemoved(EntryEvent<K, V> event) {
			DataEntryEvent<K, V> dataEntryEvent = new DataEntryEvent<K, V>(event.getKey(), event.getValue());
			listener.entryRemoved(dataEntryEvent);
		}

		@Override
		public void entryUpdated(EntryEvent<K, V> event) {
			DataEntryEvent<K, V> dataEntryEvent = new DataEntryEvent<K, V>(event.getKey(), event.getValue());
			listener.entryUpdated(dataEntryEvent);
		}

		@Override
		public void entryEvicted(EntryEvent<K, V> event) {

		}

	}
}
