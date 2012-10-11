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
package com.alu.e3.data.model.sub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alu.e3.common.E3Constant;
import com.alu.e3.common.caching.IEntryListener;
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.data.DataEntryEvent;
import com.alu.e3.data.IDataManagerListener;

public class GlobalForwardProxy extends ForwardProxy implements IForwardProxy, IEntryListener<String, String>, IDataManagerListener {

	private static final long serialVersionUID = -610907764192820512L;

	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalForwardProxy.class);
	private final static String GLOBAL_PROXY_KEY = E3Constant.GLOBAL_PROXY_SETTINGS;
	
	protected IDataManager dataManager;
	
	public void init() {
		if (dataManager == null) {
			LOGGER.error (" This class cannot work without a data manager");
			return;
		}
		
		dataManager.addListener(this);
	}
	
	public void destroy() {
		if (dataManager != null) {
			dataManager.removeGlobalProxyListener3(this);
		}
	}
	
	protected synchronized void atomicSetAttributes(IForwardProxy proxy) {
		
		if (proxy != null) {
			if (proxy.getProxyHost() == null || "".equals(proxy.getProxyHost())) {
				LOGGER.error("Invalid proxy host");
				return;
			}
			if (proxy.getProxyPass() == null || "".equals(proxy.getProxyPass())) {
				LOGGER.error("Invalid proxy password");
				return;
			}
			if (proxy.getProxyPort() == null || "".equals(proxy.getProxyPort())) {
				LOGGER.error("Invalid proxy port");
				return;
			}
			if (proxy.getProxyUser() == null || "".equals(proxy.getProxyUser())) {
				LOGGER.error("Invalid proxy user");
				return;
			}
			
			this.setProxyHost(proxy.getProxyHost());
			this.setProxyPass(proxy.getProxyPass());
			this.setProxyPort(proxy.getProxyPort());
			this.setProxyUser(proxy.getProxyUser());
		}
	}
	
	protected synchronized void atomicUnSetAttributes() {
		this.setProxyHost(null);
		this.setProxyPass(null);
		this.setProxyPort(null);
		this.setProxyUser(null);
	}
	
	/**
	 * Spring setter for DataManager
	 * @param dataManager The DataManager to set
	 */
	public void setDataManager(IDataManager dataManager) {
		this.dataManager = dataManager;
	}

	@Override
	public void entryAdded(DataEntryEvent<String, String> event) {
		if (! GLOBAL_PROXY_KEY.equals(event.getKey())) {
			LOGGER.error("Received event for the wrong key: " + event.getKey());
		}
		
		IForwardProxy proxy = deserialize(event.getValue());
		atomicSetAttributes(proxy);
	}

	@Override
	public void entryUpdated(DataEntryEvent<String, String> event) {
		if (! GLOBAL_PROXY_KEY.equals(event.getKey())) {
			LOGGER.error("Received event for the wrong key: " + event.getKey());
		}
		
		IForwardProxy proxy = deserialize(event.getValue());
		atomicSetAttributes(proxy);
	}

	@Override
	public void entryRemoved(DataEntryEvent<String, String> event) {
		if (! GLOBAL_PROXY_KEY.equals(event.getKey())) {
			LOGGER.error("Received event for the wrong key: " + event.getKey());
		}
		
		atomicUnSetAttributes();
	}

	
	/**
	 * Called when the DataManager is ready
	 */
	@Override
	public void dataManagerReady() {
		if(this.dataManager != null) {
			// Removing from DataManagerListener
			this.dataManager.removeListener(this);

			String proxy = dataManager.getSettingString(GLOBAL_PROXY_KEY);
			if (proxy != null) {
				atomicSetAttributes(deserialize (proxy));
			}
			// Then listen to global proxy updates
			dataManager.addGlobalProxyListener3(this);
		}
	}
}
