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
package com.alu.e3.auth.access.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Set;

import com.alu.e3.auth.access.IAuthDataAccess;
import com.alu.e3.common.camel.AuthReport;
import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.common.tools.CanonicalizedIpAddress;
import com.alu.e3.auth.model.AuthIdentityHelper;

/**
 * class  FileData
 */
public class FileData implements IAuthDataAccess {
	
	/* slf4j logger */
	private static final CategoryLogger logger = CategoryLoggerFactory.getLogger(FileData.class, Category.AUTH);
	
	/* members */
	File fData = null;
	boolean bUseOptimized = true;
	Hashtable<String, Set<String>> hashTable = null;
 
	/**
	 * Constructor
	 * @param filePath
	 */
	public FileData(String filePath) {
		
		logger.debug("new FileData object with file:" + filePath);
		this.fData = new File(filePath);
		
		/* Look up in the resources if absolute path not found. */
		if (!this.fData.exists())
		{
			URL fileURL = getClass().getResource("/" + filePath);
			if (fileURL != null)
			this.fData = new File(fileURL.getPath());
		}
		
		/* Store data in memory. */
		if (bUseOptimized) {
			hashTable = new Hashtable<String, Set<String>>(5);
			
			if (!this.fData.exists())
				return;
			
			Scanner scanner = null;
			try{
				/* loop on each line. */
				scanner = new Scanner(this.fData);
				while (scanner.hasNextLine()) {
				    String[] words = scanner.nextLine().split(" ");
				    if (words.length < 2)
				    	continue;
				    Set<String> set = new HashSet<String>();
				    for(int i=1; i < words.length ; i++){
				    	if( ! words[i].isEmpty())
				    		set.add(words[i]);
				    }
				    hashTable.put(words[0], set);
				}
		    } catch (FileNotFoundException e) {
		    	logger.error("An Error occured:" + e.getMessage());
				e.printStackTrace();
			}
		    finally{
		    	if (scanner != null)
		    		scanner.close();
		    }
		}
		
	}

	/**
	 * checkAppKey
	 * check if an auth key and an app id match. 
	 */
	@Override 
	public AuthReport checkAllowed(String authKey, String apiId) {

		AuthReport authReport = new AuthReport();
		
		logger.debug("Lookup if AuthKey:" + authKey + " is associated with appId:" + apiId);
		
		String appId = findString(apiId, authKey);
		
		if(appId != null) {
			
			AuthIdentityHelper authIdentityHelper = new AuthIdentityHelper();
			
			authIdentityHelper.setApi(apiId);
			authIdentityHelper.setAppId(appId);
			authIdentityHelper.setAuth(authKey);
			
			authReport.setAuthIdentity( authIdentityHelper.getAuthIdentity());
			authReport.setApiActive(true);
			
		} else {
			authReport.setNotAuthorized(true);
		}
		
		return authReport;
	}
	
	@Override
	public AuthReport checkAllowed(String username, String password, String apiId) {
		
		AuthReport authReport  = new AuthReport();
		
		logger.debug("Lookup if username:password: " + username+":"+password + " is associated with appId:" + apiId);
		
		String appId = findString(apiId, username+":"+password);
		
		if(appId != null) {
			
			AuthIdentityHelper authIdentityHelper = new AuthIdentityHelper();
			
			authIdentityHelper.setApi(apiId);
			authIdentityHelper.setAppId(appId);
			authIdentityHelper.setAuth(username, password);
			
			authReport.setAuthIdentity( authIdentityHelper.getAuthIdentity());
			authReport.setApiActive(true);
			
		} else {
			authReport.setNotAuthorized(true);
		}
		
		return authReport;
	}

	@Override
	public AuthReport checkAllowed(String username, String passwordDigest, boolean isPasswordText, String nonce, String created, String apiId) {
		// TODO: Implement FileDate to support WSSE
		return checkAllowed(username, "", apiId);
	}

	@Override
	public AuthReport checkAllowed(CanonicalizedIpAddress ipCanonicalized, String apiId) {
		
		AuthReport authReport = new AuthReport();
		
		String ip = ipCanonicalized.getIp();
		
		String appId = findString(apiId, ip);
		
		if(appId != null) {
			
			AuthIdentityHelper authIdentityHelper = new AuthIdentityHelper();

			authIdentityHelper.setApi(apiId);
			authIdentityHelper.setAppId(appId);
			authIdentityHelper.setAuth(ipCanonicalized);
			
			authReport.setAuthIdentity( authIdentityHelper.getAuthIdentity());
			authReport.setApiActive(true);
			
		} else {
			authReport.setNotAuthorized(true);
		}
		
		return authReport;
	}	

	

	@Override
	public AuthReport checkAllowed(String apiId) {
		
		AuthReport authReport = new AuthReport();
		
		logger.debug("Lookup if noauth is true for appId:" + apiId);
		
		String appId = findString(apiId, "noauth:true");
		
		if(appId != null) {
			
			AuthIdentityHelper authIdentityHelper = new AuthIdentityHelper();

			authIdentityHelper.setApi(apiId);
			authIdentityHelper.setAppId(appId);
			
			authReport.setAuthIdentity( authIdentityHelper.getAuthIdentity());
			authReport.setApiActive(true);
			
		} else {
			authReport.setNotAuthorized(true);
		}
		
		return authReport;
	}

	@Override
	public AuthReport checkOAuthAllowed(String clientId, String clientSecret, String apiId) {
		
		AuthReport authReport = new AuthReport();
		
		logger.debug("Lookup if clientId:clientSecret: " + clientId+":"+clientSecret + " is associated with appId:" + apiId);
		
		String appId = findString(apiId, clientId+":"+clientSecret);
		
		if(appId != null) {
			
			AuthIdentityHelper authIdentityHelper = new AuthIdentityHelper();
			
			authIdentityHelper.setApi(apiId);
			authIdentityHelper.setAppId(appId);
			authIdentityHelper.setOAuth(clientId, clientSecret);
			
			authReport.setAuthIdentity( authIdentityHelper.getAuthIdentity());
			authReport.setApiActive(true);
			
		} else {
			authReport.setNotAuthorized(true);
		}
		
		return authReport;
	}

	String findString(String key, String value) {
		/* Optimized */
		if (hashTable != null)
		{
			Set<String> values = hashTable.get(key);
			if(values != null) {
				for(String s : values){
					if(s != null && s.equals(value))
						return key;
				}
			}
		}
		
		/* If no database, return null. */
		else if (this.fData.exists() == false){
			// Do nothing
			return null;
		}
		else {
			Scanner scanner = null;
			try{
				/* loop on each line. */
				scanner = new Scanner(this.fData);
				while (scanner.hasNextLine()) {
					String[] words = scanner.nextLine().split(" ");
					if (words.length < 2)
						continue;
					if (words[0].equals(key)){
						for(int i=1; i < words.length ; i++){
							if(words[i].equals(value)){
								return words[i];
							}
						}
					}
				}
			} catch (FileNotFoundException e) {
				logger.error("An Error occured:" + e.getMessage());
				e.printStackTrace();
			}
			finally{
				if (scanner != null)
					scanner.close();
			}
		}
		return null;
	}
	
	
}
