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
package com.alu.e3;

import org.apache.log4j.Logger;

import com.alu.e3.common.E3Constant;
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.data.IDataManagerListener;
import com.alu.e3.data.model.sub.ForwardProxy;

public class SettingsBootstrapper implements IDataManagerListener {
	private final static Logger LOG = Logger.getLogger(SettingsBootstrapper.class);
	
	protected IDataManager dataManager;
	
	protected String proxyHost;
    protected String proxyPort;
    protected String proxyUser;
    protected String proxyPass;
	
    public void init() {
    	if (dataManager == null) {
    		return;
    	}

    	dataManager.addListener(this);
    }
	
	public String getProxyHost() {
		return proxyHost;
	}
	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}
	public String getProxyPort() {
		return proxyPort;
	}
	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}
	public String getProxyUser() {
		return proxyUser;
	}
	public void setProxyUser(String proxyUser) {
		this.proxyUser = proxyUser;
	}
	public String getProxyPass() {
		return proxyPass;
	}
	public void setProxyPass(String proxyPass) {
		this.proxyPass = proxyPass;
	}
	public IDataManager getDataManager() {
		return dataManager;
	}
	public void setDataManager(IDataManager dataManager) {
		this.dataManager = dataManager;
	}
    
	/**
	 * Called when the DataManager is ready
	 */
	@Override
	public void dataManagerReady() {
		if(this.dataManager != null) {
			// Removing from DataManagerListener
			this.dataManager.removeListener(this);

			// Check that all the properties were set
			
			if (proxyHost==null || proxyHost.length() < 1) {
				if (LOG.isDebugEnabled())
					LOG.debug("Proxy host was not set");
				return;
			}
			if (proxyPort==null || proxyPort.length() < 1) {
				if (LOG.isDebugEnabled())
					LOG.debug("Proxy port was not set");
				return;
			}
			if (proxyUser==null || proxyUser.length() < 1) {
				if (LOG.isDebugEnabled())
					LOG.debug("Proxy user was not set");
				return;
			}
			if (proxyPass==null || proxyPass.length() < 1) {
				if (LOG.isDebugEnabled())
					LOG.debug("Proxy password was not set");
				return;
			}
			
			ForwardProxy proxy = new ForwardProxy();
			proxy.setProxyHost(proxyHost);
			proxy.setProxyPort(proxyPort);
			proxy.setProxyUser(proxyUser);
			proxy.setProxyPass(proxyPass);
			
			dataManager.putSettingString(E3Constant.GLOBAL_PROXY_SETTINGS, proxy.serialize());
		}
	}
}
