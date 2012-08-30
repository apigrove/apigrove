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
package com.alu.e3.common.camel;

import java.util.ArrayList;
import java.util.List;

import com.alu.e3.data.model.Api;
import com.alu.e3.data.model.Auth;
import com.alu.e3.data.model.CallDescriptor;

public class AuthIdentity {
	
	private Api api;
	private Auth auth;
	private String appId;
	private List<CallDescriptor> callDescriptors;
		
	/**
	 * @return the api
	 */
	public Api getApi() {
		return api;
	}
	/**
	 * @param api the api to set
	 */
	public void setApi(Api api) {
		this.api = api;
	}
	
	
	/**
	 * @return the auth
	 */
	public Auth getAuth() {
		return auth;
	}
	/**
	 * @param auth the auth to set
	 */
	public void setAuth(Auth auth) {
		this.auth = auth;
	}
	
	/**
	 * @return the call descriptors (include policy)
	 */
	public List<CallDescriptor> getCallDescriptors() {
		
		if(this.callDescriptors == null) {
			this.callDescriptors = new ArrayList<CallDescriptor>();
		}
		
		return callDescriptors;
	}

	/**
	 * @return the appId
	 */
	public String getAppId() {
		return appId;
	}
	
	/**
	 * @param appId the appId to set
	 */
	public void setAppId(String appId) {
		this.appId = appId;
	}
}
