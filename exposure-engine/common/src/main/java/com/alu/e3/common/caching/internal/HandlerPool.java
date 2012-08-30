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


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class HandlerPool<K,V extends Disposable> extends LinkedHashMap<K,V> {

	/**
	 * Generated serial version UID
	 */
	private static final long serialVersionUID = 2513162685280535426L;
	
	private int maximumSize;

    public HandlerPool(int maximumSize)
    {
    	super();
    	this.maximumSize = maximumSize;
    }
    
    public HandlerPool(int initialCapacity, int maximumSize)
    {
    	super(initialCapacity);
    	this.maximumSize = maximumSize;
    }
    
    public HandlerPool(int initialCapacity, float loadFactor, int maximumSize)
    {
    	super(initialCapacity, loadFactor);
    	this.maximumSize = maximumSize;
    }
    
    public HandlerPool(int initialCapacity, float loadFactor, boolean accessOrder, int maximumSize)
    {
    	super(initialCapacity, loadFactor, accessOrder);
    	this.maximumSize = maximumSize;
    }

    public HandlerPool(Map<K, V> m, int maximumSize)
    {
    	super(m);
    	this.maximumSize = maximumSize;
    }
    
    protected boolean removeEldestEntry(Entry<K, V> arg0) {
    	
    	boolean remove = size() > this.maximumSize;
    	
    	if (remove)
    	{
        	arg0.getValue().dispose();
    	}
        return remove;
    }
}