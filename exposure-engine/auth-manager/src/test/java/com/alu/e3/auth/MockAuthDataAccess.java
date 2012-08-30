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
package com.alu.e3.auth;

import com.alu.e3.auth.access.IAuthDataAccess;
import com.alu.e3.common.camel.AuthIdentity;
import com.alu.e3.common.camel.AuthReport;
import com.alu.e3.common.tools.CanonicalizedIpAddress;

/**
 * A mock AuthManager class to help with testing.
 *
 */
public class MockAuthDataAccess implements IAuthDataAccess {

	private String expectedAuthKey;
	private String expectedUserPass; 
	private String expectedIp;

	public MockAuthDataAccess(String expectedAuthKey, String expectedUserPass, String expectedIp){
		this.expectedAuthKey = expectedAuthKey;
		this.expectedUserPass = expectedUserPass;
		this.expectedIp = expectedIp;
	}
	 
	@Override
	public AuthReport checkAllowed(String authKey, String apiId) {
		
		AuthReport authReport = new AuthReport();
		
		if(expectedAuthKey != null) {
		
			if(expectedAuthKey.equals(authKey)) {
				
				AuthIdentity authIdentity = new AuthIdentity();
				authIdentity.setAppId("1234");
				authReport.setAuthIdentity(authIdentity);
				authReport.setApiActive(true);
				
			} else {
				authReport.setNotAuthorized(true);
			}
			
		} else {
			authReport.setBadRequest(true);
		}
		
		return authReport;
	}

	@Override
	public AuthReport checkAllowed(CanonicalizedIpAddress ip, String apiId) {
		
		AuthReport authReport = new AuthReport();
	
		if(expectedIp != null) {
			if(expectedIp.equals(ip.getIp())) {
				AuthIdentity authIdentity = new AuthIdentity();
				authIdentity.setAppId("127.0.0.1");
				authReport.setAuthIdentity(authIdentity);
				authReport.setApiActive(true);
			} else {
				authReport.setNotAuthorized(true);
			}			
		} else {
			authReport.setBadRequest(true);
		}
		
		
		return authReport;
	}
	
	@Override
	public AuthReport checkAllowed(String user, String password, String apiId) {
		
		AuthReport authReport = new AuthReport();
		
		if(expectedUserPass != null) {
			
			if(expectedUserPass.equals(user+":"+password)) {
				AuthIdentity authIdentity = new AuthIdentity();
				authIdentity.setAppId("2424");
				authReport.setAuthIdentity(authIdentity);
				authReport.setApiActive(true);
			} else {
				authReport.setNotAuthorized(true);
			}			
		} else {
			authReport.setBadRequest(true);
		}
		
		return authReport;
	}

	@Override
	public AuthReport checkAllowed(String username, String passwordDigest, boolean isPasswordText, String nonce, String created, String apiId) {
		// TODO: Implement MockAuthDataAccess to support the WSSE test method
		return checkAllowed(username, "", apiId);
	}

	@Override
	public AuthReport checkAllowed(String apiId) {
		
		AuthReport authReport = new AuthReport();

		AuthIdentity authIdentity = new AuthIdentity();
		authIdentity.setAppId("3424");
		authReport.setAuthIdentity(authIdentity);
		authReport.setApiActive(true);
				
		return authReport;
	}

	@Override
	public AuthReport checkOAuthAllowed(String clientId, String clientSecret, String apiId) {
		// TODO: Implement MockAuthDataAccess to support the OAuth test method
		return null;
	}

}
