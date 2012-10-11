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
package com.alu.e3.tdr.service.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.alu.e3.common.performance.PerfWatch;
import com.alu.e3.tdr.service.ITdrQueueService;

public class TdrQueueService implements ITdrQueueService {

	private LinkedBlockingQueue<Map<String, List<Map<String, Object>>>> queue;

	private int queueWaitingSize = 2000; // Only default

	private static PerfWatch perfWatch;
	public PerfWatch getPerfWatch() {
		if (perfWatch == null )
			perfWatch = new PerfWatch();
		
		return perfWatch;
	}
	
	public void setQueueWaitingSize(int size) {
		this.queueWaitingSize = size;
		init();
	}

	private void init() {
		LinkedBlockingQueue<Map<String, List<Map<String, Object>>>> lastQueue = null;

		if (queue != null)
			lastQueue = queue;

		queue = new LinkedBlockingQueue<Map<String, List<Map<String, Object>>>>(
				queueWaitingSize);
		// A consumer may still have a take in progress on lastQueue
		// but at this point queue is now the queue where future lock will
		// happen

		if (lastQueue != null) {
			// Transfer queue contents
			List<Map<String, List<Map<String, Object>>>> toTransfer = new LinkedList<Map<String, List<Map<String, Object>>>>();
			// Safe drainTo, 'cause queue reference has changed

			do {
				lastQueue.drainTo(toTransfer);
				// Safe addAll, 'cause toTransfer will not be modified during
				// the call
				queue.addAll(toTransfer);
				// Notify possibly offerer
				toTransfer.clear();
				// TODO: Check the good existing of this loop, and the
				// concurrent-ness between
				// queue.addAll and putOrWait of offerers.
			} while (!lastQueue.isEmpty());

			lastQueue.clear();
		}
	}


	@Override
	public void putOrWait(Map<String, List<Map<String, Object>>> elem) throws InterruptedException {
	
		Long startTime = System.nanoTime();
			
		queue.put(elem);
		
		getPerfWatch().getElapsedTime().addAndGet(System.nanoTime()-startTime);
		getPerfWatch().getIterationCount().getAndIncrement();
		getPerfWatch().log("TdrQueueService.putOrWait()");
	}

	@Override
	public Map<String, List<Map<String, Object>>> getOrWait()
			throws InterruptedException {
		return queue.take();
		
	}

	@Override
	public int getQueueSize() {
		return queue.size();
	}

	@Override
	public List<Map<String, List<Map<String, Object>>>> getMultiple(int capacity)
			throws InterruptedException {
		List<Map<String, List<Map<String, Object>>>> dataPack = new LinkedList<Map<String, List<Map<String, Object>>>>();
		queue.drainTo(dataPack, capacity);
		return dataPack;
	}
}