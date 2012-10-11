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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;

public class BaseStoreFileService {

	private static CategoryLogger logger = CategoryLoggerFactory.getLogger(BaseStoreFileService.class, Category.AUTH);

	private String storePath;
	private String storePassword;

	public BaseStoreFileService() {}

	public void setStorePath(String storePath) {
		this.storePath = storePath;
	}

	public void setStorePassword(String storePassword) {
		this.storePassword = storePassword;
	}

	public void init(){
		Security.addProvider(new BouncyCastleProvider());

		File f = new File(storePath);
		if(f.exists()) {
			f.delete();
		}

		FileOutputStream out = null;
		try {
			KeyStore ks = KeyStore.getInstance("BKS");
			ks.load(null, storePassword.toCharArray());
			out = new FileOutputStream(f);
			ks.store(out, storePassword.toCharArray());
		}
		catch (Exception e) {
			if (logger.isErrorEnabled()) {
				logger.error("Unable to create the store", e);
			}
		} 
		finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// Nothing to do
				}
			}
		}
	}

	protected void destroy(){
		try {
			File f = new File(storePath);
			if(f.exists()) {
				f.delete();
			}
		} catch (Exception e) {
			if (logger.isWarnEnabled()) {
				logger.warn("Unable to delete:{}", storePath, e);
			}
		}
	}

	public KeyStore loadStore() throws Exception {
		FileInputStream out = null;
		File f = new File(storePath);

		KeyStore keystore = KeyStore.getInstance("BKS");
		try {
			out = new FileInputStream(f);
			keystore.load(out, storePassword.toCharArray());
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException ioe) {
				// Nothing to do
			}
		}
		return keystore;
	}

	public void saveStore(KeyStore ks) throws Exception {	
		FileOutputStream out = null;
		File f = new File(storePath);

		try {
			out = new FileOutputStream (f);
			ks.store(out, storePassword.toCharArray());
		} finally {
			try {
				out.close();
			} catch (IOException ioe) {
				// Nothing to do
			}
		}
	}
}
