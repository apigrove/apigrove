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

import java.io.IOException;
import java.io.StringReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.util.Arrays;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alu.e3.common.InvalidIDException;
import com.alu.e3.common.caching.IEntryListener;
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.common.osgi.api.IKeyStoreService;
import com.alu.e3.data.DataEntryEvent;
import com.alu.e3.data.IDataManagerListener;
import com.alu.e3.data.model.Certificate;
import com.alu.e3.data.model.Key;

public class KeyListener implements IEntryListener<String, Key>, IDataManagerListener{
	
	private static Logger LOG = LoggerFactory.getLogger(KeyListener.class);
	
	private IDataManager dataManager = null;
	private IKeyStoreService keyStoreService = null;
	private String keyStoreKeyPassword;
	
	/* 
	 * Jetty and/or camel-jetty do not use key aliases! 
	 * They  simply use the first key they find in the keystore.
	 * So make jetty's key come up first
	 */
	private static String ALIAS = "1";
	
	public void init() {
		dataManager.addListener(this);
	}
	
	public void destroy() {
		dataManager.removeListener(this);
	}
	
	public IDataManager getDataManager(){
		return this.dataManager;
	}

	public void setDataManager(IDataManager dm){
		this.dataManager = dm;
	}
	
	@Override
	public void dataManagerReady() {
		this.dataManager.addKeyListener(this);
	}

	public void setKeyStoreService(IKeyStoreService kss){
		this.keyStoreService = kss;
	}
		
	@Override
	public void entryAdded(DataEntryEvent<String, Key> event) {
		doKeyStoreUpdate(event);
	}

	@Override
	public void entryUpdated(DataEntryEvent<String, Key> event) {
		doKeyStoreUpdate(event);
	}


	@Override
	public void entryRemoved(DataEntryEvent<String, Key> event) {

		synchronized(keyStoreService) {
			
			KeyStore ks = keyStoreService.loadKeyStore();
			
			if(ks == null){
				LOG.error("Null keystore");
			}
			else {
				try {
					if(ks.containsAlias(ALIAS)) {
						ks.deleteEntry(ALIAS);
					}
					
					keyStoreService.saveKeyStore(ks);
				} 
				catch (KeyStoreException e) {
					LOG.error("Error while removing the key", e);
					throw new RuntimeException("Error while removing the key");
				}
			}
		}
	}
	
	private void doKeyStoreUpdate(DataEntryEvent<String, Key> event) {
		final Key key = event.getValue();
		Certificate cert = null;
		if(key.getActiveCertId() != null && key.getActiveCertId().length() > 0) {
			try {
				cert = dataManager.getCertById(key.getActiveCertId());				
			}
			catch (InvalidIDException e) {
				LOG.error("Certificate not found "+key.getActiveCertId(), e);
				throw new RuntimeException("Certificate not found "+key.getActiveCertId());
			}
		}
		
		if(cert == null){
			// A key has been uploaded without a certificate. Don't add it to the keystore. 
			// This is a standard use-case. Don't error.			
			return;
		}

		PrivateKey jkey = null;
		java.security.cert.Certificate jcert = null;
		
		try {
			
			PasswordFinder passwordFinder = null;
			
			if (key.getKeyPassphrase() != null) {
				passwordFinder = new PasswordFinder() {
					
					@Override
					public char[] getPassword() {
						return key.getKeyPassphrase().toCharArray();
					}
				};
			}
			
			PEMReader pemr = new PEMReader(new StringReader(key.getData()), passwordFinder);
			Object pemobj = pemr.readObject();
			if(pemobj instanceof KeyPair){
				jkey = ((KeyPair)pemobj).getPrivate();
			} else if (pemobj instanceof PrivateKey){
				jkey = (PrivateKey)pemobj;
			} else {
				LOG.error("The PEM object in Key "+key.getId()+" is not a Private Key");
				throw new RuntimeException("The PEM object in Key "+key.getId()+" is not a Private Key");
			}
		} catch(IOException e){
			LOG.error("Failed to read Key "+key.getId()+" data.", e);
			throw new RuntimeException("Failed to read Key "+key.getId()+" data.");
		}
		
		try{
			PEMReader pemr = new PEMReader(new StringReader(cert.getData()));
			Object pemobj = pemr.readObject();
			if(pemobj instanceof java.security.cert.Certificate){
				jcert = (java.security.cert.Certificate)pemobj;
			} else {
				LOG.error("The PEM object in Certificate "+cert.getId()+" is not a Certificate");
				throw new RuntimeException("The PEM object in Certificate "+cert.getId()+" is not a Certificate");
			}
		} catch(IOException e){
			LOG.error("Failed to read Certificate "+cert.getId()+" data.", e);
			throw new RuntimeException("Failed to read Certificate "+cert.getId()+" data.");
		}
		
		synchronized(keyStoreService) {
			
			KeyStore ks = keyStoreService.loadKeyStore();
			
			if(ks == null) {
				LOG.error("KeyStoreService did not give me my keystore!");
				throw new RuntimeException("KeyStoreService did not give me my keystore!");
			}
			
			try {
				if(ks.containsAlias(ALIAS)) {
					ks.deleteEntry(ALIAS);
				}
				
				ks.setKeyEntry(ALIAS, jkey, keyStoreKeyPassword.toCharArray(), (java.security.cert.Certificate[]) Arrays.asList(jcert).toArray());
				
				keyStoreService.saveKeyStore(ks);
			}
			catch (KeyStoreException e) {
				LOG.error("Key not updated", e);
				throw new RuntimeException("Key not updated");
			}
		}
	}
	
	public void setKeyStoreKeyPassword(String keyStoreKeyPassword) {
		this.keyStoreKeyPassword = keyStoreKeyPassword;
	}
}
