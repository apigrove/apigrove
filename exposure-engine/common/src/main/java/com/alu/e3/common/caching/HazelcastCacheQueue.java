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
/**
 * 
 */
package com.alu.e3.common.caching;

import java.util.concurrent.TimeUnit;

import com.hazelcast.core.IQueue;

public class HazelcastCacheQueue<E> implements ICacheQueue<E> {

	protected IQueue<E> queue;

	public void setQueue(IQueue<E> queue) {
		this.queue = queue;
	}
	
	@Override
	public void post(E e) throws InterruptedException {
		queue.put(e);
	}

	@Override
	public E take() throws InterruptedException {
		return queue.take();
	}
	
	@Override
	public E poll(long timeout, TimeUnit unit) throws InterruptedException {
		return queue.poll(timeout, unit);
	}


}
