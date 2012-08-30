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
package com.alu.e3.rate.manager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.alu.e3.data.IDataManagerUsedBucketIdsListener;
import com.alu.e3.rate.model.ApiCall;
import com.alu.e3.rate.model.GatewayRate;
import com.alu.e3.rate.model.GatewayQueueRate;

public class GatewayDataManager implements IDataManagerUsedBucketIdsListener {
	
	private static final int FREE_LOCK_COUNTER_MAX_SIZE = 100;
	
	private Queue<ApiCall> apiCallsPerMinuteQueue = new ConcurrentLinkedQueue<ApiCall>();
	private Queue<ApiCall> apiCallsPerSecondQueue = new ConcurrentLinkedQueue<ApiCall>();
	
	private ConcurrentHashMap<Integer, GatewayRate> gatewayRateMap = new ConcurrentHashMap<Integer, GatewayRate>();
	
	private ConcurrentHashMap<Integer, GatewayQueueRate> gatewayQueueRateMap = new ConcurrentHashMap<Integer, GatewayQueueRate>();
	
	private ConcurrentLinkedQueue<LockCounter> lockPool = new ConcurrentLinkedQueue<LockCounter>();
	private HashMap<Integer, LockCounter> bucketMapLock = new HashMap<Integer, LockCounter>();

	public class LockCounter {
		int count;
		
		public LockCounter() {
			count = 0;
		}		
	}
	
	private LockCounter getLock() {
		LockCounter lock;
	
		lock = lockPool.poll();
		
		if (lock == null) {
			lock = new LockCounter();
		}
		
		return lock;
	}
	
	private void releaseLock(LockCounter lock) {
		if (lockPool.size() < FREE_LOCK_COUNTER_MAX_SIZE) {
			lockPool.add(lock);
		}
	}
	
	public LockCounter getLockForBucket(Integer bucketId) {
		LockCounter lock;
		
		synchronized (bucketMapLock) {
			lock = bucketMapLock.get(bucketId);
			
			if (lock == null) {
				lock = getLock();
				bucketMapLock.put(bucketId, lock);
			}
			
			lock.count++;
		}
		
		return lock;
	}
	
	public void releaseLockForBucket(Integer bucketId) {
		releaseLockForBucket(bucketId, bucketMapLock.get(bucketId));
	}

	public void releaseLockForBucket(Integer bucketId, LockCounter lock) {
		synchronized (bucketMapLock) {
			lock.count--;
			
			if (lock.count == 0) {
				bucketMapLock.remove(bucketId);
				releaseLock(lock);
			}
		}
	}

	public void addCallToMinuteQueue(ApiCall call) {
		apiCallsPerMinuteQueue.add(call);
	}
	
	public boolean removeCallFromMinuteQueue(ApiCall call) {
		return apiCallsPerMinuteQueue.remove(call);
	}
	
	/**
	 * Retrieves, but does not remove, the head of this queue, or returns null if this queue is empty.
	 * @param call
	 * @return
	 */
	public ApiCall peekCallFromMinuteQueue() {
		return this.apiCallsPerMinuteQueue.peek();
	}
	
	public Iterator<ApiCall> getIteratorFromMinuteQueue() {
		return this.apiCallsPerMinuteQueue.iterator();
	}
	
	public void addCallToSecondQueue(ApiCall call) {
		apiCallsPerSecondQueue.add(call);
	}
	
	public boolean removeCallFromSecondQueue(ApiCall call) {
		return apiCallsPerSecondQueue.remove(call);
	}
	
	/**
	 * Retrieves, but does not remove, the head of this queue, or returns null if this queue is empty.
	 * @param call
	 * @return
	 */
	public ApiCall peekCallFromSecondQueue() {
		return this.apiCallsPerSecondQueue.peek();
	}
	
	public Iterator<ApiCall> getIteratorFromSecondQueue() {
		return this.apiCallsPerSecondQueue.iterator();
	}
	
	
	@Override
	public void usedBucketIdsRemoved(Integer id) {
		removeFromRateMap(id);
		
	}	
	/******************************************************************************************************************/
	
	public void putInRateMap(Integer key, GatewayRate rate) {
		gatewayRateMap.put(key,rate);
	}
	
	public GatewayRate getFromRateMap(Integer key) {
		return gatewayRateMap.get(key);
	}

	public GatewayRate removeFromRateMap(Integer key) {
		return this.gatewayRateMap.remove(key);
	}	
	
	public Iterator<Integer> getIteratorFromRateMapApiCallsKeys() {
		return this.gatewayRateMap.keySet().iterator(); 
	}
	
	public void putInQueueRateMap(Integer key, GatewayQueueRate rate) {
		gatewayQueueRateMap.put(key,rate);
	}
	
	public GatewayQueueRate getFromQueueRateMap(Integer key) {
		return gatewayQueueRateMap.get(key);
	}
	
	public GatewayQueueRate removeFromQueueRateMap(Integer key) {
		return this.gatewayQueueRateMap.remove(key);
	}
	
	public Iterator<Integer> getIteratorFromQueueRateMapApiCallsKeys() {
		return this.gatewayQueueRateMap.keySet().iterator();
	}


}
