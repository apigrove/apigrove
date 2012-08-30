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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.springframework.beans.factory.annotation.Autowired;

import com.alu.e3.common.info.GatewayStatus;
import com.alu.e3.common.info.IGatewayInfo;
import com.alu.e3.common.info.IGatewayInfoListener;
import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.common.osgi.api.IHealthCheckFactory;
import com.alu.e3.data.IHealthCheckService;

public class GatewayWatcher implements IGatewayInfoListener{

	protected static final CategoryLogger logger = CategoryLoggerFactory.getLogger(GatewayWatcher.class, Category.SYS);

	private IGatewayInfo gatewayInfo;
	private IHealthCheckFactory healthCheckFactory;
	private IHealthCheckService healthCheck;
	private IHealthCheckService healthCheckInternal;

	protected BundleContext bundleContext;

	public GatewayWatcher() {}
	
	public void init() {
		gatewayInfo.addGatewayInfoListener(this);

		healthCheck = healthCheckFactory.getHealthCheckService(IHealthCheckFactory.GATEWAY_TYPE);

		uninstallApiBundles();
		
		healthCheckInternal = healthCheckFactory.getHealthCheckService(IHealthCheckFactory.GATEWAY_INTERNAL_TYPE);
		healthCheckInternal.start();
	}

	public void setGatewayInfo(IGatewayInfo gatewayInfo) {
		this.gatewayInfo = gatewayInfo;
	}

	public void setHealthCheckFactory(IHealthCheckFactory healthCheckFactory) {
		this.healthCheckFactory = healthCheckFactory;
	}

	public void destroy() {
		if (healthCheck != null)
			healthCheck.stop();
		if (healthCheckInternal != null)
			healthCheckInternal.stop();
		
		if (gatewayInfo != null)
			gatewayInfo.removeGatewayInfoListener(this);
	}
	
	@Autowired
	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	@Override
	public void gatewayStatusChanged(String status) {
		
		if(GatewayStatus.PROVISIONED.equals(status)) {
			
			healthCheck.start();
			
			gatewayInfo.setStatus(GatewayStatus.UP);			
		}
	}

	private void uninstallApiBundles() {
		String prefix = ApiDeploymentManager.BUNDLE_LOCATION_URI_PREFIX;
		
		Bundle[] bundles = bundleContext.getBundles();
		for(Bundle bundle : bundles) {
			try {
				if(bundle.getLocation().startsWith(prefix)) {
					logger.debug("Uninstalling bundle " + bundle.getSymbolicName());
					bundle.uninstall();
				}
			} catch (BundleException e) {
				logger.error("Unable to uninstall bundle " + bundle.getSymbolicName());
			}
		}
	}

}
