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

import java.util.Map;

import org.apache.camel.CamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.osgi.service.importer.OsgiServiceLifecycleListener;

public class SslJettyComponentWatcher implements OsgiServiceLifecycleListener {

	private static final Logger LOG = LoggerFactory.getLogger(SslJettyComponentWatcher.class);
	
	private CamelContext localCamelContext;
	
	/**
	 * Retrieves the local camel context.
	 * @param localCamelContext
	 */
	@Autowired
	public void setLocalCamelContext(CamelContext localCamelContext) {
		this.localCamelContext = localCamelContext;
	}
	
	public SslJettyComponentWatcher() {
		LOG.debug("SslJettyComponentWatcher construction.");
	}
	
	protected void init() {
		LOG.debug("SslJettyComponentWatcher initialization ...");
	}
	
	protected void destroy() {
		LOG.debug("SslJettyComponentWatcher destruction ...");
		localCamelContext = null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void bind(Object service, Map properties) throws Exception {
		LOG.debug("Starting local camel context ...");
		localCamelContext.start();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void unbind(Object service, Map properties) throws Exception {
		LOG.debug("Stopping local camel context ...");
		localCamelContext.stop();
	}
}
