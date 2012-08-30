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

import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.alu.e3.common.E3Constant;
import com.alu.e3.common.caching.internal.MapHandler;
import com.alu.e3.common.caching.internal.HandlerPool;
import com.alu.e3.common.caching.internal.QueueHandler;
import com.alu.e3.common.tools.CommonTools;
import com.alu.e3.data.CacheAck;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;


public class HazelcastAckCache<K, V extends IAckData> extends HazelcastCache<K, V>  {

	protected HandlerPool<String, QueueHandler<CacheAck>> queueHandlerPool = new HandlerPool<String, QueueHandler<CacheAck>>(E3Constant.HAZELCAST_HANDLER_POOL_MAX_SIZE);
	
	@Override
	public boolean set(K key, V value) {

		String queueName = value.getId();

		boolean isManagerOnly = true;
		boolean ack = true;
		try {
			if (isReplicated && topologyClient != null) {
				Set<String> instances = getAllInstanceIPs();
				 MapHandler<K, V> mapHandler = null;
				 QueueHandler<CacheAck> queueHandler = null;
				for (String instanceIP : instances) {
					 IQueue<CacheAck> queue = null;
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
						
						queueHandler =  queueHandlerPool.get(new StringBuffer(instanceIP).append(queueName).toString());
						if (queueHandler==null)
						{
							queueHandler = new QueueHandler<CacheAck>(instanceIP, queueName);
							queueHandlerPool.put(new StringBuffer(instanceIP).append(queueName).toString(), queueHandler);
						}else
						{
							if (!queueHandler.isClientActive())
							{
								queueHandler.dispose();
								String poolKey = new StringBuffer(instanceIP).append(queueName).toString();
								queueHandlerPool.remove(poolKey);
								queueHandler = new QueueHandler<CacheAck>(instanceIP, queueName);
								queueHandlerPool.put(poolKey, queueHandler);
							}
						}
						
						IMap<K, V> map = mapHandler.getMap();
						if (map == null) 
							return false;

						// Getting the queue on which the listener will post the ACK
						queue = queueHandler.getQueue();
						if (queue == null)
							return false;				

						// Remembering if the local machine is a Manager only or also a Gateway
						// We are browsing gateways so if the gateway is the local machine then we
						// are no more a manager only machine
						if (CommonTools.isLocal(instanceIP)) {
							isManagerOnly = false;
						}

						ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
						Thread.currentThread().setContextClassLoader(HazelcastCacheManager.class.getClassLoader());
						
						// Setting the value
						map.put(key, value);

						// Waiting for the acknowledgment
						CacheAck cacheAck = queue.poll(E3Constant.CACHE_ACK_TIMEOUT, TimeUnit.MILLISECONDS);
						if(cacheAck == null) {
							logger.error("Acknowledgment timeout for setting value on " + instanceIP);
						}

						Thread.currentThread().setContextClassLoader(previousClassLoader);
						
						queue.destroy();

						if(cacheAck == null || cacheAck == CacheAck.KO) {
							ack = false;
							break;
						}
					} catch (IllegalStateException ise) {
						logger.warn("Was not able to set the entry " + key + " of the cache " + localMap.getName() + " for gateway " + instanceIP + ". This probably means that this instance is down.");
					} finally {
						if (queue != null)
							queue.destroy();
					}
				}

				// Put the value only if we are a single manager (if also a gateway, the value has been added before, in the loop)
				// If we are manager only we do not need to wait for the ack
				if(isManagerOnly) {
					
					ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
					Thread.currentThread().setContextClassLoader(HazelcastCacheManager.class.getClassLoader());

					localMap.put(key, value);
					
					Thread.currentThread().setContextClassLoader(previousClassLoader);
				}
			}
		} catch(InterruptedException e) {
			logger.error("Unable to set value with ack", e);
			ack = false;
		}

		return ack;
	}

	@Override
	public boolean set(K key, V value, String instanceIP) {
		String queueName = value.getId();

		boolean isManagerOnly = true;
		boolean ack = true;
		try {
			MapHandler<K, V> mapHandler = null;
			QueueHandler<CacheAck> queueHandler = null;
			IQueue<CacheAck> queue = null;
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

				queueHandler =  queueHandlerPool.get(new StringBuffer(instanceIP).append(queueName).toString());
				if (queueHandler==null)
				{
					queueHandler = new QueueHandler<CacheAck>(instanceIP, queueName);
					queueHandlerPool.put(new StringBuffer(instanceIP).append(queueName).toString(), queueHandler);
				} else
				{
					if (!queueHandler.isClientActive())
					{
						queueHandler.dispose();
						String poolKey = new StringBuffer(instanceIP).append(queueName).toString();
						queueHandlerPool.remove(poolKey);
						queueHandler = new QueueHandler<CacheAck>(instanceIP, queueName);
						queueHandlerPool.put(poolKey, queueHandler);
					}
				}

				IMap<K, V> map = mapHandler.getMap();
				if (map == null) 
					return false;

				// Getting the queue on which the listener will post the ACK
				queue = queueHandler.getQueue();
				if (queue == null)
					return false;				

				// Remembering if the local machine is a Manager only or also a Gateway
				// We are browsing gateways so if the gateway is the local machine then we
				// are no more a manager only machine
				if (CommonTools.isLocal(instanceIP)) {
					isManagerOnly = false;
				}

				ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(HazelcastCacheManager.class.getClassLoader());

				// Setting the value
				map.put(key, value);

				// Waiting for the acknowledgment
				CacheAck cacheAck = queue.poll(E3Constant.CACHE_ACK_TIMEOUT, TimeUnit.MILLISECONDS);
				if(cacheAck == null) {
					logger.error("Acknowledgment timeout for setting value on " + instanceIP);
				}

				Thread.currentThread().setContextClassLoader(previousClassLoader);

				queue.destroy();

				if(cacheAck == null || cacheAck == CacheAck.KO) {
					ack = false;
				}
			} finally {
				if (queue != null)
					queue.destroy();
			}

			// Put the value only if we are a single manager (if also a gateway, the value has been added before, in the loop)
			// If we are manager only we do not need to wait for the ack
			if(isManagerOnly) {

				ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(HazelcastCacheManager.class.getClassLoader());

				Thread.currentThread().setContextClassLoader(previousClassLoader);
			}
		} catch(InterruptedException e) {
			logger.error("Unable to set value with ack", e);
			ack = false;
		}

		return ack;
	}

	@Override
	public V remove(Object key) {
		V value = get(key);
		if(value == null)
			return null; // not grave error if the data does not exist?

		String queueName = value.getId();

		boolean ack = true;

		if (localMap == null) {
			logger.error("Attempt to remove a key in a cache without a map");
			ack = false;
		} else { 

			// to know if the current machine is a manager only or also a gateway
			boolean isManagerOnly = true;
			IQueue<CacheAck> queue = null;
			try {
				if (isReplicated && topologyClient != null) {
					Set<String> instances = getAllInstanceIPs();

					for (String instanceIP : instances) {

						MapHandler<K, V> mapHandler = mapHandlerPool.get(new StringBuffer(instanceIP).append(localMap.getName()).toString());
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

						QueueHandler<CacheAck> queueHandler =  queueHandlerPool.get(new StringBuffer(instanceIP).append(queueName).toString());
						if (queueHandler==null)
						{
							queueHandler = new QueueHandler<CacheAck>(instanceIP, queueName);
							queueHandlerPool.put(new StringBuffer(instanceIP).append(queueName).toString(), queueHandler);
						}else
						{
							if (!queueHandler.isClientActive())
							{
								String poolKey = new StringBuffer(instanceIP).append(queueName).toString();
								queueHandler.dispose();
								queueHandlerPool.remove(poolKey);
								queueHandler = new QueueHandler<CacheAck>(instanceIP, queueName);
								queueHandlerPool.put(poolKey, queueHandler);
							}
						}

						try {

							IMap<K, V> map = mapHandler.getMap();
							if (map == null) 
								return null;

							queue = queueHandler.getQueue();
							if (queue == null)
								return null;				

							// Remembering if the local machine is a Manager only or also a Gateway
							// We are browsing gateways so if the gateway is the local machine then we
							// are no more a manager only machine
							if (CommonTools.isLocal(instanceIP)) {
								isManagerOnly = false;
							}

							ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
							Thread.currentThread().setContextClassLoader(HazelcastCacheManager.class.getClassLoader());

							// Removing the value
							map.remove(key);

							// Waiting for the acknowledgment
							CacheAck cacheAck = queue.poll(E3Constant.CACHE_ACK_TIMEOUT, TimeUnit.MILLISECONDS);
							if(cacheAck == null) {
								logger.error("Acknowledgment timeout for removing value on " + instanceIP);
							}

							Thread.currentThread().setContextClassLoader(previousClassLoader);

							if(cacheAck == null || cacheAck == CacheAck.KO) {
								ack = false;
								break;
							}
						} catch (IllegalStateException ise) {
							logger.warn("Was not able to set the entry " + key + " of the cache " + localMap.getName() + " for gateway " + instanceIP + ". This probably means that this instance is down.");
						} finally {
							if (queue != null)
								queue.destroy();
						}
					}

					// Remove the value only if we are a single manager (if also a gateway, the value has been removed before)
					// If we are manager we do not need to wait for the ack
					if(isManagerOnly) {

						ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
						Thread.currentThread().setContextClassLoader(HazelcastCacheManager.class.getClassLoader());

						localMap.remove(key);

						Thread.currentThread().setContextClassLoader(previousClassLoader);
					}
				}
			} catch(InterruptedException e) {
				logger.error("Unable to set value with ack", e);
				ack = false;
			}
		}

		if(ack)
			return value;
		else
			return null;
	}

}
