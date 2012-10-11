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
package com.alu.e3.gateway.config.certshuffle;

import java.io.StringReader;
import java.security.KeyStore;
import java.security.cert.Certificate;

import org.bouncycastle.openssl.PEMReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alu.e3.common.caching.IEntryListener;
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.common.osgi.api.ITrustStoreService;
import com.alu.e3.data.DataEntryEvent;
import com.alu.e3.data.IDataManagerListener;

public class TrustStoreListener implements IEntryListener<String, com.alu.e3.data.model.Certificate>, IDataManagerListener {
	
	private static Logger LOG = LoggerFactory.getLogger(TrustStoreListener.class);
	
	private ITrustStoreService trustStoreService = null;
	private IDataManager dataManager;
	
	public void setTrustStoreService(ITrustStoreService trustStoreService) {
		this.trustStoreService = trustStoreService;
	}

	public void setDataManager(IDataManager dataManager) {
		this.dataManager = dataManager;
	}
	
	public void init() {
		dataManager.addListener(this);
	}
	
	public void destroy() {
		dataManager.removeListener(this);
	}
	
	@Override
	public void dataManagerReady() {
		dataManager.addCAListener(this);
	}
	
	private void addCertificate(String alias, com.alu.e3.data.model.Certificate model) {
		Certificate cert;
		
		try {
			PEMReader pemr = new PEMReader(new StringReader(model.getData()));
			cert = (Certificate)pemr.readObject();
			
			KeyStore ks = trustStoreService.loadTrustStore();
			ks.setCertificateEntry(alias, cert);
			trustStoreService.saveTrustStore(ks);
		}
		catch (Exception e) {
			LOG.error("Error while storing the certificate " + alias, e);
			throw new RuntimeException("Error while storing the certificate " + alias);
		}
	}

	private void removeCertificate(String alias, com.alu.e3.data.model.Certificate model) {
		
		try {
			KeyStore ks = trustStoreService.loadTrustStore();
			ks.deleteEntry(alias);
			trustStoreService.saveTrustStore(ks);
		} 
		catch (Exception e) {
			LOG.error("Error while deleting the certificate " + alias, e);
			throw new RuntimeException("Error while deleting the certificate " + alias);
		}				
	}
	
	@Override
	public void entryAdded(DataEntryEvent<String, com.alu.e3.data.model.Certificate> event) {
		addCertificate(event.getKey(), event.getValue());
	}

	@Override
	public void entryUpdated(DataEntryEvent<String, com.alu.e3.data.model.Certificate> event) {
		addCertificate(event.getKey(), event.getValue());
	}

	@Override
	public void entryRemoved(DataEntryEvent<String, com.alu.e3.data.model.Certificate> event) {
		removeCertificate(event.getKey(), event.getValue());
	}
}
