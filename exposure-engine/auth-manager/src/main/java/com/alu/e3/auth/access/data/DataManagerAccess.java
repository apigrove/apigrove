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
	public AuthReport checkAllowed(Api api) {
		
		AuthReport authReport = new AuthReport();
		
		if(api != null) {		

			// check if the API is active (status)
			authReport.setApiActive((api.getStatus().isActive()));
			// For noAuth, Auth is always active
			authReport.setAuthActive(true);
			
			if(authReport.isApiActive()) {

				List<CallDescriptor> descriptors = this.dataManager.getMatchingPolicies(api);
				
				if(descriptors != null) {
					authReport.setAuthIdentity(new AuthIdentity());
					authReport.getAuthIdentity().setApi(api);
					authReport.getAuthIdentity().getCallDescriptors().addAll(descriptors);					
				} else {
					if(logger.isDebugEnabled()) {
						logger.debug("NoAuth method is not enabled");
					}
					authReport.setNotAuthorized(true);
				}
			}
			
		} else {
			if(logger.isDebugEnabled()) {
				logger.debug("No api found " + api.getId());
			}
			authReport.setApiNotFound(true);
		}
		
		
		return authReport;
	}	
	
	@Override
	public AuthReport checkAllowed(Api api, String authKey) {
		if(logger.isDebugEnabled()) {
			logger.debug("Checking AuthKey [" + authKey + "] for Api [" + api.getId() + "]");
		}
		Auth auth = this.dataManager.getAuthByAuthKey(authKey);
		
		return getAuthReport(api, auth);
	}

	@Override
	public AuthReport checkAllowed(Api api, CanonicalizedIpAddress ip) {
		if(logger.isDebugEnabled()) {
			logger.debug("Checking authentication for IP address [" + ip.getIp() + "] for Api [" + api.getId() + "]");
		}
		Auth auth = this.dataManager.getAuthByIP(ip.getIp());
		
		return getAuthReport(api, auth);
	}

	@Override
	public AuthReport checkAllowed(Api api, String username, String password) {
		if(logger.isDebugEnabled()) {
			logger.debug("Try authentication for username [" + username + "] and password [" + password + "] for Api [" + api.getId() + "]");
		}
		Auth auth = this.dataManager.getAuthByUserPass(username, password);
		
		return getAuthReport(api, auth);
	}

	@Override
	public AuthReport checkAllowed(Api api, String username, String passwordDigest, boolean isPasswordText, String nonce, String created) {
		if(logger.isDebugEnabled()) {
			logger.debug("Try authentication for username [" + username + "] for Api [" + api.getId() + "]");
		}
		Auth auth = this.dataManager.getWsseAuth(username, passwordDigest, isPasswordText, nonce, created);

		return getAuthReport(api, auth);		
	}

	@Override
	public AuthReport checkOAuthAllowed(Api api, String clientId, String clientSecret) {
		if(logger.isDebugEnabled()) {
			logger.debug("Try authentication for cliendId [" + clientId + "] for Api [" + api.getId() + "]");
		}
		Auth auth = this.dataManager.getAuthByOAuth(clientId, clientSecret);
		
		return getAuthReport(api, auth);				
	}


	/**
	 * Gets the AuthReport from an API id and Auth
	 * @param apiId
	 * @param auth
	 * @return authReport instance
	 */
	private AuthReport getAuthReport(Api api, Auth auth) {
	
		AuthReport authReport = new AuthReport();
		
		if(api != null) {

			// check if the API is active (status)
			authReport.setApiActive(api.getStatus().isActive());
			
			if(authReport.isApiActive()) {
				
				List<CallDescriptor> descriptors = null;
					
				if(auth != null) {
									
					authReport.setAuthActive(auth.getStatus().isActive());
					
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
							if(logger.isDebugEnabled()) {
								logger.debug("No policy found");
							}
							authReport.setHasNoPolicy(true);
						}
						
					} else {
						if(logger.isDebugEnabled()) {
							logger.debug("Auth does not match with API");
						}
						authReport.setNotAuthorized(true);
					}
				} else {
					if(logger.isDebugEnabled()) {
						logger.debug("No auth found for API");
					}
					authReport.setAuthNotFound(true);
				}
				
			}
						
		} else {
			if(logger.isDebugEnabled()) {
				logger.debug("No api found " + api.getId());
			}
			authReport.setApiNotFound(true);
		}
		
		return authReport;		
	}
	
	
	
	
}
