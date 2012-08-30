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
package com.alu.e3.auth.access.data;

import java.util.List;

import com.alu.e3.auth.access.IAuthDataAccess;
import com.alu.e3.common.camel.AuthIdentity;
import com.alu.e3.common.camel.AuthReport;
import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.common.tools.CanonicalizedIpAddress;
import com.alu.e3.data.model.Api;
import com.alu.e3.data.model.Auth;
import com.alu.e3.data.model.CallDescriptor;
import com.alu.e3.data.model.enumeration.StatusType;

public class DataManagerAccess implements IAuthDataAccess {
	
	private static final CategoryLogger logger = CategoryLoggerFactory.getLogger(DataManagerAccess.class, Category.DMGR);
	
	private IDataManager dataManager;
	
	public DataManagerAccess() {}

	/**
	 * @return the dataManager
	 */
	public IDataManager getDataManager() {
		return dataManager;
	}

	/**
	 * @param dataManager the dataManager to set
	 */
	public void setDataManager(IDataManager dataManager) {
		this.dataManager = dataManager;
	}
	
	@Override
	public AuthReport checkAllowed(String apiId) {
		
		AuthReport authReport = new AuthReport();
		
		// get api
		Api api = this.dataManager.getApiById(apiId, false);

		if(api != null) {		

			// check if the API is active (status)
			authReport.setApiActive((api.getStatus() == StatusType.ACTIVE));	
			
			if(authReport.isApiActive()) {
				
				List<CallDescriptor> descriptors = this.dataManager.getMatchingPolicies(api);
				
				if(descriptors != null) {
					authReport.setAuthIdentity(new AuthIdentity());
					authReport.getAuthIdentity().setApi(api);
					authReport.getAuthIdentity().getCallDescriptors().addAll(descriptors);					
				} else {
					logger.debug("NoAuth method is not enabled");
					authReport.setNotAuthorized(true);
				}
			}
			
		} else {
			logger.debug("No api found " + apiId);
			authReport.setApiNotFound(true);
		}
		
		
		return authReport;
	}	
	
	@Override
	public AuthReport checkAllowed(String authKey, String apiId) {
		
		logger.debug("Checking AuthKey [" + authKey + "] for Api [" + apiId + "]");
		Auth auth = this.dataManager.getAuthByAuthKey(authKey);
		
		return getAuthReport(apiId, auth);
	}

	@Override
	public AuthReport checkAllowed(CanonicalizedIpAddress ip, String apiId) {
		
		logger.debug("Checking authentication for IP address [" + ip.getIp() + "] for Api [" + apiId + "]");
		Auth auth = this.dataManager.getAuthByIP(ip.getIp());
		
		return getAuthReport(apiId, auth);
	}

	@Override
	public AuthReport checkAllowed(String username, String password, String apiId) {
		
		logger.debug("Try authentication for username [" + username + "] and password [" + password + "] for Api [" + apiId + "]");
		Auth auth = this.dataManager.getAuthByUserPass(username, password);
		
		return getAuthReport(apiId, auth);
	}

	@Override
	public AuthReport checkAllowed(String username, String passwordDigest, boolean isPasswordText, String nonce, String created, String apiId) {
		
		logger.debug("Try authentication for username [" + username + "] for Api [" + apiId + "]");
		Auth auth = this.dataManager.getWsseAuth(username, passwordDigest, isPasswordText, nonce, created);

		return getAuthReport(apiId, auth);		
	}

	@Override
	public AuthReport checkOAuthAllowed(String clientId, String clientSecret, String apiId) {
		
		logger.debug("Try authentication for cliendId [" + clientId + "] for Api [" + apiId + "]");
		Auth auth = this.dataManager.getAuthByOAuth(clientId, clientSecret);
		
		return getAuthReport(apiId, auth);				
	}


	/**
	 * Gets the AuthReport from an API id and Auth
	 * @param apiId
	 * @param auth
	 * @return authReport instance
	 */
	private AuthReport getAuthReport(String apiId, Auth auth) {
	
		AuthReport authReport = new AuthReport();
		
		// get api
		Api api = this.dataManager.getApiById(apiId, false);

		if(api != null) {

			// check if the API is active (status)
			authReport.setApiActive((api.getStatus() == StatusType.ACTIVE));	
			
			if(authReport.isApiActive()) {
				
				List<CallDescriptor> descriptors = null;
					
				if(auth != null) {
										
					// with auth					
					descriptors = this.dataManager.getMatchingPolicies(api, auth);
					
					if (descriptors != null) {
					
						// check if a policy exists
						boolean foundPolicy = false;
						for (CallDescriptor callDescriptor : descriptors) {
							if (callDescriptor.getPolicy() != null) {
								foundPolicy = true;
								break;
							}
						}
						
						if (foundPolicy) {
							authReport.setAuthIdentity(new AuthIdentity());
							authReport.getAuthIdentity().setApi(api);
							authReport.getAuthIdentity().setAuth(auth);
							authReport.getAuthIdentity().getCallDescriptors().addAll(descriptors);
						} else {
							logger.debug("No policy found");
							authReport.setHasNoPolicy(true);
						}
						
					} else {
						logger.debug("Auth does not match with API");
						authReport.setNotAuthorized(true);
					}
				} else {
					logger.debug("No auth found for API");
					authReport.setAuthNotFound(true);
				}
				
			}
						
		} else {
			logger.debug("No api found " + apiId);
			authReport.setApiNotFound(true);
		}
		
		return authReport;		
	}
	
	
	
	
}
