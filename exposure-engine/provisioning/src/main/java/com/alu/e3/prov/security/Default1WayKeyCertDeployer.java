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
package com.alu.e3.prov.security;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.data.IDataManagerListener;
import com.alu.e3.data.model.Certificate;
import com.alu.e3.data.model.CertificateDetail;
import com.alu.e3.data.model.Key;
import com.alu.e3.data.model.KeyDetail;
import com.alu.e3.osgi.api.ITopology;
import com.alu.e3.topology.model.ITopologyListener;

public class Default1WayKeyCertDeployer implements IDataManagerListener, ITopologyListener {

	private static final String KEY_FILE_NAME = "e3.default.key";
	private static final String CERT_FILE_NAME = "e3.default.crt";
	
	private static final String DEFAULT_KEY_ID = "default";
	private static final String DEFAULT_KEY_NAME = "Default Key";
	private static final String DEFAULT_KEY_TYPE = "Defautl Type";
	
	private static final String DEFAULT_CERT_ID = "default";
	private static final String DEFAULT_CERT_NAME = "Default Cert";

	private static final Logger logger = LoggerFactory.getLogger(Default1WayKeyCertDeployer.class);
	
	private File defaultKeyCertDirectory;
	private ITopology topology;
	private IDataManager dataManager;
	
	private boolean topologyServiceReady = false;
	private boolean dataManagerServiceReady = false;
	
	private String keyData = null;
	private String certData = null;
	
	public Default1WayKeyCertDeployer() {}
	
	/**
	 * Sets the default key/cert location to where this 'Deployer' will search for key and cert at startup.
	 * @param defaultKeyCertDirectory
	 */
	public void setDefaultKeyCertDirectory(File defaultKeyCertDirectory) {
		this.defaultKeyCertDirectory = defaultKeyCertDirectory;
		if (this.defaultKeyCertDirectory==null) throw new IllegalArgumentException("defaultKeyCertDirectory must be not null");
		if (!this.defaultKeyCertDirectory.exists()) throw new IllegalArgumentException("defaultKeyCertDirectory:"+defaultKeyCertDirectory.getAbsolutePath()+" does not exist");
		if (!this.defaultKeyCertDirectory.isDirectory()) throw new IllegalArgumentException("defaultKeyCertDirectory:"+defaultKeyCertDirectory.getAbsolutePath()+" must be a directory");
	}
	
	/**
	 * Sets the data manager (Preferred by Spring).
	 * @param dataManager
	 */
	public void setDataManager(IDataManager dataManager) {
		this.dataManager = dataManager;
	}

	/**
	 * Sets the topology service (Preferred by Spring).
	 * @param topology
	 */
	public void setTopology(ITopology topology) {
		this.topology = topology;
	}
	
	/**
	 * Initializes listeners on this 'Deployer'.
	 */
	protected void init() {
		if(logger.isDebugEnabled()) {
			logger.debug("Initializing Default1WayKeyCertDeployer ...");
		
			logger.debug("Checking default key availabilty ...");
		}
		File defaultKeyFile = new File(defaultKeyCertDirectory, KEY_FILE_NAME);
		if (!defaultKeyFile.exists()) throw new IllegalArgumentException("Key file:"+defaultKeyFile.getAbsolutePath()+" does not exists");
		if (!defaultKeyFile.isFile()) throw new IllegalArgumentException("Key file:"+defaultKeyFile.getAbsolutePath()+" is not a regular file");
		
		if(logger.isDebugEnabled()) {
			logger.debug("Checking default cert availabilty ...");
		}
		File defaultCertFile = new File(defaultKeyCertDirectory, CERT_FILE_NAME);
		if (!defaultCertFile.exists()) throw new IllegalArgumentException("Cert file:"+defaultCertFile.getAbsolutePath()+" does not exists");
		if (!defaultCertFile.isFile()) throw new IllegalArgumentException("Cert file:"+defaultCertFile.getAbsolutePath()+" is not a regular file");
		
		if(logger.isDebugEnabled()) {
			logger.debug("Loading default key data ...");
		}
		Reader readerKey = null;
		try {
			readerKey = new FileReader(defaultKeyFile);
			keyData = IOUtils.toString(readerKey);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("Key file:"+defaultKeyFile.getAbsolutePath()+" does not exists", e);
		} catch (IOException e) {
			throw new IllegalArgumentException("Key file:"+defaultKeyFile.getAbsolutePath()+" reading error", e);
		} finally {
			IOUtils.closeQuietly(readerKey);
			readerKey = null;
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("Loading default cert data ...");
		}
		Reader readerCert = null;
		try {
			readerCert = new FileReader(defaultCertFile);
			certData = IOUtils.toString(readerCert);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("Cert file:"+defaultCertFile.getAbsolutePath()+" does not exists", e);
		} catch (IOException e) {
			throw new IllegalArgumentException("Cert file:"+defaultCertFile.getAbsolutePath()+" reading error", e);
		} finally {
			IOUtils.closeQuietly(readerCert);
			readerCert = null;
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("Listening some needed services ...");
		}
		topology.addTopologyListener(this);
		dataManager.addListener(this);
		if(logger.isDebugEnabled()) {
			logger.debug("Initialization done.");
		}
	}
	
	/**
	 * Cleans listeners on this 'Deployer'.
	 */
	protected void destroy() {
		if(logger.isDebugEnabled()) {
			logger.debug("Destroying Default1WayKeyCertDeployer ...");
		}
		topology.removeTopologyListener(this);
		dataManager.removeListener(this);
		if(logger.isDebugEnabled()) {
			logger.debug("Destroy done.");
		}
	}

	/**
	 * Called when topology service is ready.
	 */
	@Override
	public void onReady() {
		topologyServiceReady = true;
		installDefault1WayKeyCert();
	}
	
	/**
	 * Called when dataManager service is ready.
	 */
	@Override
	public void dataManagerReady() {
		dataManagerServiceReady = true;
		installDefault1WayKeyCert();
	}

	/**
	 * Installs one default key, one default cert and one default key/cert association.
	 */
	private void installDefault1WayKeyCert() {
		if (!topologyServiceReady || !dataManagerServiceReady) {
			// One of topology or dataManager service is not ready
			return;
		}
		if(logger.isDebugEnabled()) {
			logger.debug("Installing default 1Way Key/Cert couple ...");
			
			logger.debug("Installing default 1Way Key ...");
		}
		Key defaultKey = new Key();
		KeyDetail defaultKeyDetail = new KeyDetail();
		
		defaultKey.setId(DEFAULT_KEY_ID);
		defaultKey.setData(keyData);
		defaultKey.setActiveCertId(null);
		defaultKey.setKeyDetail(defaultKeyDetail);
		
		defaultKeyDetail.setId(DEFAULT_KEY_ID);
		defaultKeyDetail.setName(DEFAULT_KEY_NAME);
		defaultKeyDetail.setType(DEFAULT_KEY_TYPE);
		
		dataManager.addKey(defaultKey);
		

		if(logger.isDebugEnabled()) {
			logger.debug("Installing default 1Way Cert ...");
		}
		Certificate defaultCert = new Certificate();
		CertificateDetail defaultCertDetail = new CertificateDetail();
		
		defaultCert.setId(DEFAULT_CERT_ID);
		defaultCert.setCertDetail(defaultCertDetail);
		defaultCert.setData(certData);
		defaultCert.setPassword(DEFAULT_CERT_ID);
		
		defaultCertDetail.setId(DEFAULT_CERT_ID);
		defaultCertDetail.setKeyId(DEFAULT_KEY_ID);
		defaultCertDetail.setName(DEFAULT_CERT_NAME);
		
		dataManager.addCert(defaultCert);
		
		// Now, key is installed, cert is installer under the previous key,0
		// let's update the key to use the default cert
		if(logger.isDebugEnabled()) {
			logger.debug("Updating default Key/Cert association ...");
		}
		defaultKey.setData(null);
		defaultKey.setActiveCertId(DEFAULT_CERT_ID);
		dataManager.updateKey(defaultKey);
		
		if(logger.isDebugEnabled()) {
			logger.debug("Installation done.");
		}
	}
	
}
