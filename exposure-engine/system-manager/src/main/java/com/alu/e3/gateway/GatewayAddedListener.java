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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alu.e3.common.E3Constant;
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.common.osgi.api.ITopologyClient;
import com.alu.e3.data.IDataManagerListener;
import com.alu.e3.data.model.Instance;
import com.alu.e3.data.topology.IInstanceListener;
import com.alu.e3.data.topology.InstanceEvent;

public class GatewayAddedListener implements IInstanceListener, IDataManagerListener{
	
	private static final Logger logger = LoggerFactory.getLogger(GatewayAddedListener.class);
	
	protected ITopologyClient topologyClient;
	protected IDataManager dataManager;
		
	public void setDataManager(IDataManager dataManager) {
		this.dataManager = dataManager;
	}

	public void setTopologyClient(ITopologyClient topologyClient) {
		this.topologyClient = topologyClient;
	}
	
	public GatewayAddedListener() {}
	
	public void init() {
		dataManager.addListener(this);
	}
	
	public void destroy() {
		dataManager.removeListener(this);
	}
	
	@Override
	public void dataManagerReady() {
		List<Instance> gateways = topologyClient.getAllInstancesOfType(E3Constant.E3GATEWAY);
		
		if (gateways != null) {
			for (Instance gateway : gateways) {
				reloadGateway(gateway);
			}
		}
		
		topologyClient.addInstanceListener(this);
	}
	
	@Override
	public void instanceAdded(InstanceEvent event) {
		if (dataManager == null) {
			logger.warn("No dataManager yet set to handle instanceAdded");
			return;
		}
		if (event == null) {
			logger.warn("Null event on instanceAdded");
			return;
		}

		// Instance type added need to be filtered
		if(E3Constant.E3GATEWAY.equals(event.getType())) {
			
			Instance gateway = event.getInstance();
			
			if (gateway == null) {
				logger.warn("No instance set on event");
				return;
			}
			
			reloadGateway(gateway);
		}
	}

	@Override
	public void instanceRemoved(InstanceEvent event) {
		// Nothing to be done here
	}
	
	private void reloadGateway(Instance gateway) {
		String myArea = topologyClient.getMyArea();
		String gatewayIP;
		
		if (myArea == null || myArea.equals(gateway.getArea())) {
			gatewayIP = gateway.getInternalIP();
		}
		else {
			gatewayIP = gateway.getExternalIP();			
		}
		
		dataManager.reloadGateway(gatewayIP);
	}
}
