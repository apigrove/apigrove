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

import org.apache.camel.Endpoint;
import org.apache.camel.component.jetty.JettyHttpComponent;
import org.eclipse.jetty.http.ssl.SslContextFactory;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alu.e3.gateway.security.E3TrustManager;

public class SslJettyHttpComponent extends JettyHttpComponent {

	Logger LOG = LoggerFactory.getLogger(SslJettyHttpComponent.class);
	
	private String keyStorePath;
	private String keyStorePassword;
	private String keyStoreKeyPassword;
	private String trustStorePath;
	private String trustStorePassword;
	
	@Override
	protected Endpoint createEndpoint(String uri, String remaining,
			Map<String, Object> parameters) throws Exception {
		uri = uri.startsWith("ssljetty:") ? uri.substring(3) : uri;
		return super.createEndpoint(uri, remaining, parameters);
	}
	
	@Override
	protected SslSelectChannelConnector createSslSocketConnector()
			throws Exception {
		
		LOG.debug("A new SSL Connector is being made!!");
		
		SslContextFactory sslContextFactory = new SslContextFactory() {
			@Override
			protected TrustManager[] getTrustManagers(KeyStore trustStore,
					Collection<? extends CRL> crls) throws Exception
			{
				E3TrustManager trustManager = new E3TrustManager(trustStore);
				// set this flag only when there's at least one CA 
				setWantClientAuth(trustManager.getAcceptedIssuers().length > 0);
				return new TrustManager[] {trustManager};
			}
		};

		sslContextFactory.setKeyStoreType("BKS");
		sslContextFactory.setKeyStoreProvider("BC");
		
		// This password (used to encrypt keys in the keystore) must not be longer than 7 characters!!!
		sslContextFactory.setKeyManagerPassword(keyStoreKeyPassword);
		sslContextFactory.setKeyStorePassword(keyStorePassword);
		
		sslContextFactory.setKeyStore(keyStorePath);
		
		sslContextFactory.setTrustStoreType("BKS");
		sslContextFactory.setTrustStoreProvider("BC");

		sslContextFactory.setTrustStorePassword(trustStorePassword);
		sslContextFactory.setTrustStore(trustStorePath);

		return new SslSelectChannelConnector(sslContextFactory);
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
	
	public void setTrustStorePath(String trustStorePath) {
		this.trustStorePath = trustStorePath;
	}
	
	public void setTrustStorePassword(String trustStorePassword) {
		this.trustStorePassword = trustStorePassword;
	}
}
