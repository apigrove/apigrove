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

import com.alu.e3.common.E3Constant;
import com.alu.e3.common.tools.CommonTools;
import com.hazelcast.client.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

public class QueueHandler<V> implements Disposable {

	protected HazelcastClient client;
	protected IQueue<V> queue;
	
	public QueueHandler(String ip, String queueName) {

		if (CommonTools.isLocal(ip)) {
			HazelcastInstance hzI = Hazelcast.getHazelcastInstanceByName(E3Constant.HAZELCAST_NAME);
			
			if (hzI != null) {
				queue = hzI.getQueue(queueName);
			}
			else {
				// TODO log
				System.out.println("QueueHandler: HazelcastInstance not found");
			}
		} else {

			ClientConfig clientConfig = new ClientConfig();
			clientConfig.addAddress(ip + ":" + E3Constant.HAZELCAST_PORT);
			client = HazelcastClient.newHazelcastClient(clientConfig);
			queue = client.getQueue(queueName);
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
	
	public IQueue<V> getQueue() {
		return queue;
	}

	public void dispose() {
		if (client!=null) 
			client.shutdown();
	}
}
