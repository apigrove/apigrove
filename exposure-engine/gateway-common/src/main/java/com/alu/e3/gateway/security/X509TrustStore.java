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

import java.io.ByteArrayInputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alu.e3.common.caching.IEntryListener;
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.data.DataEntryEvent;
import com.alu.e3.data.IDataManagerListener;
import com.alu.e3.data.model.Certificate;
import com.alu.e3.data.model.SSLCRL;

public class X509TrustStore implements IDataManagerListener, PKIXConfigProvider {
	private static final Logger LOG = LoggerFactory.getLogger(X509TrustStore.class);
	
	private final Map<String, X509Certificate> caMap = new HashMap<String, X509Certificate>();
	private final Map<String, CRL> crlMap = new HashMap<String, CRL>();
	private final List<PKIXConfigChangeListener> configListeners =
			Collections.synchronizedList(new LinkedList<PKIXConfigChangeListener>());

	private final IEntryListener<String, Certificate> caListener = new IEntryListener<String, Certificate>() {
		@Override
		public void entryAdded(DataEntryEvent<String, Certificate> event) {
			try {
				addCA(event.getKey(), generateCertificate(event.getValue().getData()));
			} catch (Exception e) {
				LOG.error("Unable to add CA " + event.getKey(), e);
			}
		}

		@Override
		public void entryUpdated(DataEntryEvent<String, Certificate> event) {
			try {
				addCA(event.getKey(), generateCertificate(event.getValue().getData()));
			} catch (Exception e) {
				LOG.error("Unable to update CA " + event.getKey(), e);
			}
		}

		@Override
		public void entryRemoved(DataEntryEvent<String, Certificate> event) {
			try {
				removeCA(event.getKey());
			} catch (Exception e) {
				LOG.error("Unable to remove CA " + event.getKey(), e);
			}
		}
	};
	private final IEntryListener<String, SSLCRL> crlListener = new IEntryListener<String, SSLCRL>() {
		@Override
		public void entryAdded(DataEntryEvent<String, SSLCRL> event) {
			try {
				addCRL(event.getKey(), generateCRL(event.getValue().getContent()));
			} catch (Exception e) {
				LOG.error("Unable to add CRL " + event.getKey(), e);
			}
		}

		@Override
		public void entryUpdated(DataEntryEvent<String, SSLCRL> event) {
			try {
				addCRL(event.getKey(), generateCRL(event.getValue().getContent()));
			} catch (Exception e) {
				LOG.error("Unable to update CRL " + event.getKey(), e);
			}
		}

		@Override
		public void entryRemoved(DataEntryEvent<String, SSLCRL> event) {
			try {
				removeCRL(event.getKey());
			} catch (Exception e) {
				LOG.error("Unable to remove CRL " + event.getKey(), e);
			}
		}
	};
	
	private CertificateFactory	certFactory;
	private Provider provider;
	private IDataManager dataManager;
	private volatile PKIXConfig config;
	
	public void setProvider(String provider) {
		try {
			certFactory = CertificateFactory.getInstance("X.509", provider);
			this.provider = certFactory.getProvider();
		} catch (Exception e) {
			LOG.error("Could not set provider to '" + provider + "'", e);
		}
	}

	public void setDataManager(IDataManager dm) {
		this.dataManager = dm;
	}
	
	public void init() {
		try {
			if (certFactory == null) {
				certFactory = CertificateFactory.getInstance("X.509");
				provider = certFactory.getProvider();
			}
			config = new PKIXConfig(caMap.values(), crlMap.values(), provider);
		} catch (Exception e) {
			LOG.error("Could not initialize config", e);
		}
		dataManager.addListener(this);		
	}
	
	public void destroy() {
		dataManager.removeListener(this);
	}
	
	@Override
	public void dataManagerReady() {
		dataManager.addCAListener(caListener); 
		dataManager.addCrlListener(crlListener); 
	}

	@Override
	public PKIXConfig getConfig() {
		return config;
	}

	@Override
	public void addListener(PKIXConfigChangeListener listener) {
		configListeners.add(listener);
	}
	
	@Override
	public void removeListener(PKIXConfigChangeListener listener) {
		configListeners.remove(listener);
	}

	private X509Certificate generateCertificate(String contents) throws CertificateException {
		return (X509Certificate) certFactory.generateCertificate(
				new ByteArrayInputStream(contents.getBytes()));
	}
	
	private CRL generateCRL(String contents) throws CertificateException, CRLException {
		return certFactory.generateCRL(new ByteArrayInputStream(contents.getBytes()));
	}
	
	private synchronized void addCA(String alias, X509Certificate ca)
			throws NoSuchAlgorithmException, InvalidAlgorithmParameterException
	{
		caMap.put(alias, ca);
		reconfigure();
	}
		
	private synchronized void removeCA(String alias)
			throws NoSuchAlgorithmException, InvalidAlgorithmParameterException
	{
		caMap.remove(alias);
		reconfigure();
	}

	private synchronized void addCRL(String id, CRL crl)
			throws NoSuchAlgorithmException, InvalidAlgorithmParameterException
	{
		crlMap.put(id, crl);
		reconfigure();
	}
		
	private synchronized void removeCRL(String id)
			throws NoSuchAlgorithmException, InvalidAlgorithmParameterException
	{
		crlMap.remove(id);
		reconfigure();
	}
	
	private void reconfigure() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
		config = new PKIXConfig(caMap.values(), crlMap.values(), provider);
		for (PKIXConfigChangeListener listener : configListeners) {
			listener.configChanged(config);
		}
	}
}
