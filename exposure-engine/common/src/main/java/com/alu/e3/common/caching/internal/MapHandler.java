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
package com.alu.e3.common.caching.internal;

import java.util.concurrent.RejectedExecutionException;

import com.alu.e3.common.E3Constant;
import com.alu.e3.common.tools.CommonTools;
import com.hazelcast.client.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class MapHandler<K, V> implements Disposable {

	private HazelcastClient client;
	private IMap<K, V> map;
	
	public MapHandler(String ip, String mapName) {
		
		if (CommonTools.isLocal(ip)) {
			HazelcastInstance hzI = Hazelcast.getHazelcastInstanceByName(E3Constant.HAZELCAST_NAME);
			
			if (hzI != null) {
				map = hzI.getMap(mapName);
			}
			else {
				// TODO log
				System.out.println("MapHandler: HazelcastInstance not found");
			}
		} else {

			ClientConfig clientConfig = new ClientConfig();
			clientConfig.addAddress(ip + ":" + E3Constant.HAZELCAST_PORT);
			client = HazelcastClient.newHazelcastClient(clientConfig);
			map = client.getMap(mapName);
		}
	}
	
	public boolean isLocal() {
		return client == null;
	}
	
	public boolean isClientActive()
	{
		if (client!=null)
		{
			return client.isActive();
		}
		else
		{
			return false;
		}
	}

	public IMap<K, V> getMap() {
		return map;
	}
	
	public void dispose() {
		try {
			if (client!=null) 
				client.shutdown();
		} catch (RejectedExecutionException e) {
			// Nothing to do
		}
	}
}
