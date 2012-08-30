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
package com.alu.e3.common.tools;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Set of auto-expiring keys. A single method is provided to check
 * for a given key, and add it if it isn't already there. Each key
 * is automatically removed after a certain interval, specified when
 * the set is constructed.
 * 
 *
 * @param <E> key type
 */
public class ExpiringSet<E> {

	// auto-removal scheduler
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // the set itself
    private final Set<E> set = new HashSet<E>();

    // how long each key should live
    private final int expireSeconds;

    /**
     * Constructs a new <code>ExpiringSet</code> from which each item will
     * automatically be removed after the specified number of seconds.
     * 
     * @param expireSeconds how long each entry should live
     */
    public ExpiringSet(int expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    /**
     * Returns true if the specified key is contained in the set.
     * Otherwise it adds the key to the set and returns false.
     * 
     * @param key the key to be checked/added
     * @return true if the key was found; false if it didn't exist (and was added)
     */
    public boolean contains(final E key) {
        synchronized (set) {
            if (set.contains(key)) {
                return true;
            }
            set.add(key);   // not found, so add it
        }
        scheduler.schedule(new Runnable() {   // schedule automatic key removal
            @Override
            public void run() {
                synchronized (set) {
                    set.remove(key);
                }
            }
        }, expireSeconds, TimeUnit.SECONDS);
        return false;
    }
   
    /**
     * Releases resources associated with this <code>ExpiringSet</code>.
     */
    public void shutdown() {
    	scheduler.shutdownNow();
    	synchronized (set) {
    		set.clear();
    	}
    }
}
