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
package com.hazelcast.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.IMap;
import com.hazelcast.nio.Data;

public class GroveMProxyImpl extends MProxyImpl {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4776207704800761598L;

	private static final String EVENT_SOURCE = "Unknown";
	
	private List<EntryListener<Object, Object>> listeners;
	private Map<Object, List<EntryListener<Object, Object>>> perKeyListeners;
	
	/**
	 * Using internally an {@link ExecutorService} to change the ThreadContext during notification.
	 * For more details, see comment below in method fireEventToAllListeners.
	 */
	private ExecutorService exectuorService;
	private static final int EVENTS_CORE_POOL_SIZE = 10;
	private static final int EVENTS_MAXIMUM_POOL_SIZE = 10;
	private static final long EVENT_KEEP_ALIVE_TIME = 1;
	private static final TimeUnit EVENT_KEEP_ALIVE_TIME_UNIT = TimeUnit.MINUTES;
	private BlockingQueue<Runnable> eventQueue;
	
	public GroveMProxyImpl(String name, FactoryImpl factory) {
		super(name, factory);
		listeners = new ArrayList<EntryListener<Object,Object>>();
		perKeyListeners = new HashMap<Object, List<EntryListener<Object,Object>>>();
		eventQueue = new LinkedBlockingQueue<Runnable>();
		exectuorService =  new ThreadPoolExecutor(EVENTS_CORE_POOL_SIZE, EVENTS_MAXIMUM_POOL_SIZE, EVENT_KEEP_ALIVE_TIME, EVENT_KEEP_ALIVE_TIME_UNIT, eventQueue);
	}

	/**
	 * Called with a set AND multiple time when a remote putAll is made.
	 */
	@Override
	public void set(Object key, Object value, long time, TimeUnit timeunit) {
		super.set(key, value, time, timeunit);
		putOrSetEventBroadcast(key, value, null);
	}
	
	/**
	 * Synchronous Put(key, value) with synchronous {@link EntryEvent} notifications.
	 * @see IMap.put
	 */
	@Override
	public Object put(Object key, Object value) {
		Object oldValue = super.put(key, value);
		
		putOrSetEventBroadcast(key, value, oldValue);
		
		return oldValue;
	}

	@SuppressWarnings("unchecked")
	private void putOrSetEventBroadcast(Object key, Object value, Object oldValue) {
		EntryEvent<Object, Object> event;
		if (isRemoteObject(key) && isRemoteObject(value)) {
			if (oldValue == null)
				event = new DataAwareEntryEvent(null, EntryEvent.TYPE_ADDED, EVENT_SOURCE, (Data)key, (Data)value, null, true);
			else
				event = new DataAwareEntryEvent(null, EntryEvent.TYPE_UPDATED, EVENT_SOURCE, (Data)key, (Data)value, (Data)oldValue, true);
		}
		else {
			if (oldValue == null)
				event = new EntryEvent<Object, Object>(EVENT_SOURCE, null, EntryEvent.TYPE_ADDED, key, null, value);
			else
				event = new EntryEvent<Object, Object>(EVENT_SOURCE, null, EntryEvent.TYPE_UPDATED, key, oldValue, value);
		}
		
		fireEventToAllListeners(key, event);
	}
	
	private boolean isRemoteObject(Object o) {
		if (o==null) return true;
		return o instanceof Data;
	}

	/**
	 * Only called when the putAll call is made locally.
	 * Unless this, we must fire our event manually (locally).
	 * @param allEntries all entries to put
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public void putAll(Map allEntries) {
		//super.putAll(allEntries);
		for(Object key : allEntries.keySet()) {
			set(key, allEntries.get(key), 0 , TimeUnit.MILLISECONDS);
		}
	}
	
	@Override
	public void clear() {
		super.clear();
	}
	
	/**
	 * Globally synchronous fire event method but asynchronous between each event notification.
	 * @param key the key event to broadcast
	 * @param event the event to broadcast
	 */
	private void fireEventToAllListeners(Object key, final EntryEvent<Object, Object> event) {
		Set<EntryListener<Object, Object>> wholeListeners = new HashSet<EntryListener<Object, Object>>();
		
		// Add 'global' listeners
		wholeListeners.addAll(listeners);
		// Add perKey listeners if any
		if (perKeyListeners.containsKey(key))
			wholeListeners.addAll(perKeyListeners.get(key));
		
		// We will use a serviceExectutor to :
		// 1- Parallelize event broadcast
		// 2- Ease join using Future
		// 3- Change the thread context execution to broadcast event due to
		//    a Hazelcast arch. limitation:
		//    Executing a get(key) operation on a map that have received a client put event (Packet)
		//    checks the current ThreadContext and sees that we use the same 'map server' thread context
		//    to both, consume request packet and get the local map value
		//    so it decides to downCast the internal value into Data packet...
		List<Future<?>> futureEvents = new ArrayList<Future<?>>();
		
		// Registers the Futures ...
		for(EntryListener<Object, Object> listener : wholeListeners) {
			final EntryListener<Object, Object> toNotify = listener;
			
			// Keep track of the Future to join on them later
			futureEvents.add(
					// Submit job (=event execution)
					exectuorService.submit(
						new Runnable() {
							@Override
							public void run() {
								switch(event.getEventType()) {
								case ADDED:
									toNotify.entryAdded(event);
									break;
								case EVICTED:
									toNotify.entryEvicted(event);
									break;
								case REMOVED:
									toNotify.entryRemoved(event);
									break;
								case UPDATED:
									toNotify.entryUpdated(event);
									break;
								}
							}
						}
					)
				);
		}
		
		// Similar to a join
		for(Future<?> futureEvent : futureEvents) {
			try {
				// Blocking instruction while Future runnable has not finished
				futureEvent.get();
			} catch (InterruptedException e) {
				throw new RuntimeException("FutureEvent had been interrupted", e);
			} catch (ExecutionException e) {
				throw new RuntimeException("FutureEvent execution had a problem", e);
			}
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean evict(Object key) {
		boolean evicted = super.evict(key);
		EntryEvent<Object, Object> event;
		if (isRemoteObject(key)) {
			event = new DataAwareEntryEvent(null, EntryEvent.TYPE_EVICTED, EVENT_SOURCE, (Data) key, null, null, true);
		}
		else { 
			event = new EntryEvent<Object, Object>(EVENT_SOURCE, null, EntryEvent.TYPE_EVICTED, key, null);
		}
		fireEventToAllListeners(key, event);
		return evicted;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object remove(Object key) {
		Object oldValue = super.remove(key);
		EntryEvent<Object, Object> event;
		if (isRemoteObject(key)) {
			event = new DataAwareEntryEvent(null, EntryEvent.TYPE_REMOVED, EVENT_SOURCE, (Data) key, (Data)oldValue, (Data)oldValue, true);
		}
		else { 
			event = new EntryEvent<Object, Object>(EVENT_SOURCE, null, EntryEvent.TYPE_REMOVED, key, oldValue, oldValue);
		}
		fireEventToAllListeners(key, event);
		return oldValue;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void addEntryListener(@SuppressWarnings("rawtypes") EntryListener listener, boolean includeValue) {
		listeners.add(listener);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void addEntryListener(@SuppressWarnings("rawtypes")EntryListener listener, Object key, boolean includeValue) {
		if (!perKeyListeners.containsKey(key))
			perKeyListeners.put(key, new ArrayList<EntryListener<Object, Object>>());
		perKeyListeners.get(key).add(listener);
	}
	
	@Override
	public void addLocalEntryListener(@SuppressWarnings("rawtypes")EntryListener entryListener) {
		throw new UnsupportedOperationException("GroveMProxyImpl not yet support adding local entry listener.");
	}
	
	@Override
	public void removeEntryListener(@SuppressWarnings("rawtypes")EntryListener listener) {
		listeners.remove(listener);
	}
	
	@Override
	public void removeEntryListener(@SuppressWarnings("rawtypes")EntryListener listener, Object key) {
		perKeyListeners.get(key).remove(listener);
		if (perKeyListeners.get(key).isEmpty())
			perKeyListeners.remove(key);
	}
}
