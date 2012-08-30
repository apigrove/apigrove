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

import java.io.ByteArrayInputStream;
import java.security.cert.CRL;
import java.security.cert.CertificateFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alu.e3.common.caching.IEntryListener;
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.data.DataEntryEvent;
import com.alu.e3.data.IDataManagerListener;
import com.alu.e3.data.model.SSLCRL;
import com.alu.e3.gateway.security.E3TrustManager;

public class CrlListener implements IEntryListener<String, SSLCRL>, IDataManagerListener{
	
	private static Logger LOG = LoggerFactory.getLogger(CrlListener.class);
	
	private IDataManager dataManager;
	
	public void setDataManager(IDataManager dm) {
		this.dataManager = dm;
	}
	
	public void init() {
		dataManager.addListener(this);		
	}
	
	public void destroy() {
		dataManager.removeListener(this);
	}
	
	@Override
	public void dataManagerReady() {
		this.dataManager.addCrlListener(this); 
	}
	
	private void addCRL(String id, SSLCRL sslCRL) {
		CRL crl;
		
		try {
			crl = CertificateFactory.getInstance("X.509").generateCRL(new ByteArrayInputStream(sslCRL.getContent().getBytes()));
			E3TrustManager.addCRL(id, crl);
		}
		catch (Exception e) {
			LOG.error("Unable to add CRL " + id, e);
		}		
	}
	
	@Override
	public void entryAdded(DataEntryEvent<String, SSLCRL> event) {
		addCRL(event.getKey(), event.getValue());
	}

	@Override
	public void entryUpdated(DataEntryEvent<String, SSLCRL> event) {
		addCRL(event.getKey(), event.getValue());
	}

	@Override
	public void entryRemoved(DataEntryEvent<String, SSLCRL> event) {
		try {
			E3TrustManager.removeCRL(event.getKey());
		} catch (Exception e) {
			LOG.error("Unable to remove CRL " + event.getKey(), e);
		}
	}	
}

