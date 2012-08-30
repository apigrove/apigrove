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
package com.alu.e3.common.spring;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;

import com.alu.e3.common.E3Constant;
import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;


public class SpringContextBootstrapper implements ApplicationContextAware {

	protected boolean manager;
	protected boolean gateway;
	protected String managerComponents;
	protected String gatewayComponents;
	protected String propertyFile;
	
	private static final CategoryLogger logger = CategoryLoggerFactory.getLogger(SpringContextBootstrapper.class, Category.SYS);

	@Autowired
	protected BundleContext bundleCtx;
	
	protected OsgiBundleXmlApplicationContext childContextManager;
	protected OsgiBundleXmlApplicationContext childContextGateway;

	public void init() {
		
		logger.warn("System startup");
		logger.debug("init Spring contexts (manager=" + manager + ",gateway="+ gateway + ")");

		if (isManager()) {
			childContextManager.setConfigLocations(new String[]{managerComponents});
			childContextManager.refresh();
		}
		
		if (isGateway()) {
			childContextGateway.setConfigLocations(new String[]{gatewayComponents});
			childContextGateway.refresh();
		}
	}
	
	public void reloadNatureProperties() {
		
		Properties properties = new Properties();
		FileInputStream is = null;
		try {
			is = new FileInputStream(propertyFile);
		    properties.load(is);
		} catch (IOException e) {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ioe) {
					// Nothing to do
				}
			}
		}
		
		boolean isGateway = false, isManager = false;
		String prop = properties.getProperty(E3Constant.IS_MANAGER_PROPERTY_ENTRY);
		if (prop != null && prop.equalsIgnoreCase("true"))
			isManager = true;
		prop = properties.getProperty(E3Constant.IS_GATEWAY_PROPERTY_ENTRY);
		if (prop != null && prop.equalsIgnoreCase("true"))
			isGateway = true;
		
		/* Refresh only if state's changed. */
		if (isManager && !isManager())
		{
			logger.debug("Refreshing manager Spring context.");
			childContextManager.setConfigLocations(new String[]{managerComponents});
			childContextManager.refresh();
		}
		setManager(isManager);
			
		/* Refresh only if state's changed. */
		if (isGateway && !isGateway())
		{
			logger.debug("Refreshing gateway Spring context.");
			childContextGateway.setConfigLocations(new String[]{gatewayComponents});
			childContextGateway.refresh();
		}
		setGateway(isGateway);
	}

	public void destroy(){
		if(isManager()){
			childContextManager.close();
		}

		if(isGateway()){
			childContextGateway.close();
		}
		logger.warn("System shutdown");
	}

	public void setManager(boolean manager) {
		this.manager = manager;
	}

	public boolean isManager() {
		return manager;
	}

	public void setGateway(boolean gateway) {
		this.gateway = gateway;
	}

	public boolean isGateway() {
		return gateway;
	}
	
	public void setPropertyFile(String propertyFile) {
		this.propertyFile = propertyFile;
	}

	public String getPropertyFile() {
		return this.propertyFile;
	}

	public void setManagerComponents(String managerComponents) {
		this.managerComponents = managerComponents;
	}

	public void setGatewayComponents(String gatewayComponents) {
		this.gatewayComponents = gatewayComponents;
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		
		logger.debug("Setting ApplicationContext: manager="+manager+" gateway="+ gateway);
		
		//here, we have to use a context implementation having a parent context
		//(ex: using the given AbstractRefreshableConfigApplicationContext would result in context overwrite)
		this.childContextManager = new OsgiBundleXmlApplicationContext(context);
		childContextManager.setBundleContext(bundleCtx);

		this.childContextGateway = new OsgiBundleXmlApplicationContext(context);
		childContextGateway.setBundleContext(bundleCtx);	
	}

	public void refresh() {
		init();
	}
}
