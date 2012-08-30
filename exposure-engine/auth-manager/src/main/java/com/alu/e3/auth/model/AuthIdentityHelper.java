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
package com.alu.e3.auth.model;

import com.alu.e3.common.camel.AuthIdentity;
import com.alu.e3.common.tools.CanonicalizedIpAddress;
import com.alu.e3.data.model.Api;
import com.alu.e3.data.model.Auth;
import com.alu.e3.data.model.AuthDetail;

public class AuthIdentityHelper {

	private AuthIdentity authIdentity = null;
	
	public AuthIdentityHelper() {
		this.authIdentity = new AuthIdentity();
	}
	
	/**
	 * @return the authIdentity
	 */
	public AuthIdentity getAuthIdentity() {
		return authIdentity;
	}
	
	/**
	 * Sets the API
	 * @param apiId
	 */
	public void setApi(String apiId) {
		
		Api api = new Api();
		api.setId(apiId);
		
		this.authIdentity.setApi(api);
	}

	/**
	 * Sets the Auth from an auth key
	 * @param authKey
	 */
	public void setAuth(String authKey) {
		
		Auth auth = new Auth();		
		auth.setAuthDetail(new AuthDetail());
		auth.getAuthDetail().setAuthKeyValue(authKey);
		
		this.authIdentity.setAuth(auth);
	}
	

	/**
	 * Sets the Auth from a user name and password
	 * @param userName
	 * @param password
	 */
	public void setAuth(String userName, String password) {
		
		Auth auth = new Auth();		
		auth.setAuthDetail(new AuthDetail());
		auth.getAuthDetail().setUsername(userName);
		auth.getAuthDetail().setPassword(password.getBytes());	
		
		this.authIdentity.setAuth(auth);
	}


	/**
	 * Sets the Auth from a clientId and clientSecret
	 * @param clientId
	 * @param clientSecret
	 */
	public void setOAuth(String clientId, String clientSecret) {
		
		Auth auth = new Auth();		
		auth.setAuthDetail(new AuthDetail());
		auth.getAuthDetail().setClientId(clientId);
		auth.getAuthDetail().setClientSecret(clientSecret);	
		
		this.authIdentity.setAuth(auth);
	}


	/**
	 * Sets the Auth from a CanonicalizedIpAddress
	 * @param ip
	 */
	public void setAuth(CanonicalizedIpAddress ip) {
		
		Auth auth = new Auth();		
		auth.setAuthDetail(new AuthDetail());
		auth.getAuthDetail().getWhiteListedIps().add(ip.getIp());
		
		this.authIdentity.setAuth(auth);
	}
	
	/**
	 * Sets the Application id
	 * @param appId
	 */
	public void setAppId(String appId) {
		this.authIdentity.setAppId(appId);
	}
}
