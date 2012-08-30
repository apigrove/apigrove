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
package com.alu.e3.gateway.security;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;

import com.alu.e3.common.osgi.api.IKeyStoreService;
import com.alu.e3.common.osgi.api.IStoreChangedListener;
import com.alu.e3.common.osgi.api.ITrustStoreService;

public class SslJettyComponentReloader implements IStoreChangedListener {

	private static final String SSLJETTY_REGISTRATION_BEAN_ID = "ssljettyRegistration";

	private static final Logger LOG = LoggerFactory.getLogger(SslJettyComponentReloader.class);
	
	@Autowired
	private BundleContext bundleContext;
	@Autowired
	private ApplicationContext parentApplicationContext;
	private String contextXmlClasspath;
	
	private IKeyStoreService keyStoreService;
	private ITrustStoreService trustStoreService;
	
	private ServiceRegistration sslJettyRegistration;
	
	private OsgiBundleXmlApplicationContext childApplicationContext;
	
	/**
	 * Needs the application context to init the SslJetty component in spring-dm context.
	 * @param applicationContext
	 */
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.parentApplicationContext = applicationContext;
	}
	
	/**
	 * Needs the bundle context to init the SslJetty component in spring-dm context.
	 * @param bundleContext
	 */
	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}
	
	/**
	 * Sets the base xml classPath defining the SslJetty spring-dm'ized component. 
	 * @param contextXmlClasspath
	 */
	public void setContextXmlClasspath(String contextXmlClasspath) {
		this.contextXmlClasspath = contextXmlClasspath;
	}
	
	/**
	 * Needs the {@link IKeyStoreService} to register on changed events.
	 * @param keyStoreService
	 */
	public void setKeyStoreService(IKeyStoreService keyStoreService) {
		this.keyStoreService = keyStoreService;
	}
	
	/**
	 * Needs the {@link ITrustStoreService} to register on changed events.
	 * @param trustStoreService
	 */
	public void setTrustStoreService(ITrustStoreService trustStoreService) {
		this.trustStoreService = trustStoreService;
	}
	
	/**
	 * Inits listeners and child application context.
	 */
	protected void init() {
		LOG.debug("SslJettyComponentReloader initialization ...");

		LOG.debug("Registering store changed listeners ...");
		keyStoreService.addStoreChangedListener(this);
		trustStoreService.addStoreChangedListener(this);
		
		LOG.debug("Creating child context ...");
		String[] xmlConfigClassPathes = new String[]{contextXmlClasspath};
		childApplicationContext = new OsgiBundleXmlApplicationContext(xmlConfigClassPathes, parentApplicationContext);
		childApplicationContext.setBundleContext(bundleContext);
		
		LOG.debug("Starting child context ...");
		childApplicationContext.refresh();
		
		getOrUpdateServiceRegistration();
		
		LOG.debug("SslJettyComponentReloader init done.");
	}
	
	/**
	 * Cleans up listeners and child application context.
	 */
	protected void destroy() {
		LOG.debug("SslJettyComponentReloader destroying ...");

		LOG.debug("Closing child context ...");
		childApplicationContext.close();
		
		LOG.debug("UNRegistering store changed listeners ...");
		keyStoreService.removeStoreChangedListener(this);
		trustStoreService.removeStoreChangedListener(this);
		
		LOG.debug("SslJettyComponentReloader destroyed.");
	}
	
	/**
	 * On key/cert stores changed, reloads the SslJetty spring-dm'ized component.
	 */
	@Override
	public void onStoreChanged() {
		LOG.debug("SslJettyComponent reloading ...");
		reload();
		LOG.debug("SslJettyComponent reloaded.");
	}
	
	private void getOrUpdateServiceRegistration() {
		LOG.debug("Getting service registration ...");
		sslJettyRegistration = childApplicationContext.getBean(SSLJETTY_REGISTRATION_BEAN_ID, ServiceRegistration.class);
		
		if (sslJettyRegistration==null)
			throw new IllegalStateException("Unable to get back service registration with bean id: "+SSLJETTY_REGISTRATION_BEAN_ID);
	}
	
	/**
	 * Reload the child application context, in other terms, the SslJetty spring-dm'ized component.
	 */
	private void reload() {
		try {
			LOG.debug("Trying sslJettyREgistration unregistering ...");
			sslJettyRegistration.unregister();
		} catch(Exception e) {
			LOG.error("Bad luck, unable to unregister sslJettyREgistration", e);
		}
		
		LOG.debug("childApplicationContext normal closing ...");
		childApplicationContext.destroy();
		
		LOG.debug("childApplicationContext normal refreshing ...");
		childApplicationContext.refresh();
		
		getOrUpdateServiceRegistration();
	}
	
}
