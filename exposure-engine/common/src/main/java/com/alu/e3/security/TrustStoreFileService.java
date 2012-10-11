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
package com.alu.e3.security;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.common.osgi.api.IStoreChangedListener;
import com.alu.e3.common.osgi.api.IStoreChangedObservable;
import com.alu.e3.common.osgi.api.ITrustStoreService;

public class TrustStoreFileService extends BaseStoreFileService implements ITrustStoreService {
	
	private static CategoryLogger logger = CategoryLoggerFactory.getLogger(TrustStoreFileService.class, Category.AUTH);

	private List<IStoreChangedListener> listeners;
	
	public TrustStoreFileService() {
		super();
		listeners = new ArrayList<IStoreChangedListener>();
	}
	
	@Override
	public void addStoreChangedListener(IStoreChangedListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public void removeStoreChangedListener(IStoreChangedListener listener) {
		listeners.remove(listener);
	}
	
	@Override
	public KeyStore loadTrustStore() {
		KeyStore keystore = null;
		
		try {
			keystore = loadStore();
		}
		catch (Exception e) {
			if (logger.isErrorEnabled()) {
				logger.error("Unable to load the truststore", e);
			}
		}

		return keystore;
	}

	@Override
	public void saveTrustStore(KeyStore ks) {
		
		try {
			saveStore(ks);
		}
		catch (Exception e) {
			if (logger.isErrorEnabled()) {
				logger.error("Unable to save the truststore", e);
			}
		}
		
		fireStoreChangedEvent();
	}

	private void fireStoreChangedEvent() {
		for(IStoreChangedListener listener : listeners)
			listener.onStoreChanged();
	}
}
