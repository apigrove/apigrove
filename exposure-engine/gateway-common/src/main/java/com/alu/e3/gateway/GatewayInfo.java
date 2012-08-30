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
package com.alu.e3.gateway;

import java.util.LinkedList;

import com.alu.e3.common.caching.ICacheManager;
import com.alu.e3.common.caching.ICacheTable;
import com.alu.e3.common.caching.IEntryListener;
import com.alu.e3.common.info.GatewayStatus;
import com.alu.e3.common.info.IGatewayInfo;
import com.alu.e3.common.info.IGatewayInfoListener;
import com.alu.e3.common.osgi.api.IInstanceInfo;
import com.alu.e3.data.DataEntryEvent;

/**
 * Get ans set the status of the current instance.
 */
public class GatewayInfo implements IGatewayInfo, IEntryListener<String, String> {
	
	private ICacheManager cacheManager;
	private ICacheTable<String, String> cachingTableGatewayInfo;
	private IInstanceInfo instanceInfo;
	
	public void setCacheManager(ICacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public void setInstanceInfo(IInstanceInfo instanceInfo) {
		this.instanceInfo = instanceInfo;
	}
	
	public void setCachingTableGatewayInfo(ICacheTable<String, String> cachingTableGatewayInfo) {
		this.cachingTableGatewayInfo = cachingTableGatewayInfo;
	}

	public void setGatewayInfoListeners(LinkedList<IGatewayInfoListener> gatewayInfoListeners) {
		this.gatewayInfoListeners = gatewayInfoListeners;
	}
	
	public void init() {
		// Due to restart this bundle restriction
		// we must createOrGet the cached table instead of create or null !
		cachingTableGatewayInfo = this.cacheManager.createOrGetTable(IGatewayInfo.GATEWAY_STATUS_TABLE_NAME, false, null);
		this.setStatus(GatewayStatus.DOWN);
		
		// Since destroy() method has been called on restart this bundle
		// We can re-call addEntryListener(...) without any doubts.
		cachingTableGatewayInfo.addEntryListener(this);
		
		instanceInfo.setGateway(true);
		instanceInfo.setGatewayInfo(this);
	}
	
	public void destroy()
	{
		if (cachingTableGatewayInfo != null)
			cachingTableGatewayInfo.removeEntryListener(this);
	}

	@Override
	public void setStatus(String instanceStatus) {
		cachingTableGatewayInfo.set(IGatewayInfo.GATEWAY_STATUS_CACHE_ENTRY, instanceStatus);
	}
	
	@Override
	public String getStatus() {
		String status = cachingTableGatewayInfo.get(IGatewayInfo.GATEWAY_STATUS_CACHE_ENTRY);
		if(status == null)
			status = GatewayStatus.DOWN;
		return status;
	}

	/**
	 * Gateway Status listener.
	 */
	private LinkedList<IGatewayInfoListener> gatewayInfoListeners = new LinkedList<IGatewayInfoListener>();
	
	@Override
	public void addGatewayInfoListener(IGatewayInfoListener listener) {
		gatewayInfoListeners.add(listener);
	}
	
	@Override
	public void removeGatewayInfoListener(IGatewayInfoListener listener) {
		gatewayInfoListeners.remove(listener);
	}

	
	/**
	 * Status Table listener
	 */
	@Override
	public void entryAdded(DataEntryEvent<String, String> event) {
		fireGatewayStatusEvent(event);
	}

	@Override
	public void entryUpdated(DataEntryEvent<String, String> event) {
		fireGatewayStatusEvent(event);
	}
	
	private void fireGatewayStatusEvent(DataEntryEvent<String, String> event)
	{
		if (!IGatewayInfo.GATEWAY_STATUS_CACHE_ENTRY.equals(event.getKey()))
			return;
		
		
		IGatewayInfoListener[] listeners = null;
		synchronized(this)
		{
			listeners = new IGatewayInfoListener[0];
			listeners = gatewayInfoListeners.toArray(listeners);
		}
		
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].gatewayStatusChanged(event.getValue());
		}
	}
	
	@Override
	public void entryRemoved(DataEntryEvent<String, String> event) { }
}