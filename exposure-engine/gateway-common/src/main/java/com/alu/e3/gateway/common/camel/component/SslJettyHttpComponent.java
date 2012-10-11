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
package com.alu.e3.gateway.common.camel.component;

import java.security.KeyStore;
import java.security.cert.CRL;
import java.util.Collection;
import java.util.Map;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.camel.Endpoint;
import org.eclipse.jetty.http.ssl.SslContextFactory;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SslJettyHttpComponent extends DispatchingHttpComponent {

	Logger LOG = LoggerFactory.getLogger(SslJettyHttpComponent.class);
	
	private X509TrustManager trustManager;
	private String keyStorePath;
	private String keyStorePassword;
	private String keyStoreKeyPassword;
	
	@Override
	protected Endpoint createEndpoint(String uri, String remaining,
			Map<String, Object> parameters) throws Exception {
		uri = uri.startsWith("ssljetty:") ? uri.substring(3) : uri;
		return super.createEndpoint(uri, remaining, parameters);
	}
	
	@Override
	protected SslSelectChannelConnector createSslSocketConnector()
			throws Exception {
		if (LOG.isDebugEnabled()) {
			LOG.debug("A new SSL Connector is being made!!");
		}
		
		SslContextFactory sslContextFactory = new SslContextFactory() {
			@Override
			protected TrustManager[] getTrustManagers(KeyStore trustStore,
					Collection<? extends CRL> crls) throws Exception
			{
				if (trustManager != null) {
					return new TrustManager[] {trustManager};
				} else {
					return super.getTrustManagers(trustStore, crls);
				}
			}
		};

		sslContextFactory.setKeyStoreType("BKS");
		sslContextFactory.setKeyStoreProvider("BC");
		
		// This password (used to encrypt keys in the keystore) must not be longer than 7 characters!!!
		sslContextFactory.setKeyManagerPassword(keyStoreKeyPassword);
		sslContextFactory.setKeyStorePassword(keyStorePassword);
		
		sslContextFactory.setKeyStore(keyStorePath);

		if (trustManager != null) {
			// set this flag only when there's at least one CA 
			sslContextFactory.setWantClientAuth(trustManager.getAcceptedIssuers().length > 0);
		}
		
		return new SslSelectChannelConnector(sslContextFactory);
	}
	
	public void setTrustManager(X509TrustManager trustManager) {
		this.trustManager = trustManager;
	}

	public void setKeyStorePath(String keyStorePath) {
		this.keyStorePath = keyStorePath;
	}
	
	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}
	
	public void setKeyStoreKeyPassword(String keyStoreKeyPassword) {
		this.keyStoreKeyPassword = keyStoreKeyPassword;
	}
}
