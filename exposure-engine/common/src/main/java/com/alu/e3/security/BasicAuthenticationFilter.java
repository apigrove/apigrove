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

import javax.ws.rs.core.Response;

import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.interceptor.security.AuthenticationException;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.security.SimpleAuthorizingFilter;
import org.apache.cxf.message.Message;
public class BasicAuthenticationFilter extends SimpleAuthorizingFilter {
	
	private String username;
	private String password;
	private boolean enabled;
	
	public BasicAuthenticationFilter() {

	}

	public Response handleRequest(Message m, ClassResourceInfo resourceClass) {
		AuthorizationPolicy policy = m
				.get(AuthorizationPolicy.class);
		
		if(!enabled){
			// not activated 
			// null means success			
			return null;
		}
		
		if (policy != null) {

			String givenUsername = policy.getUserName();
			String givenPassword = policy.getPassword();

			if(username.equals(givenUsername) && password.equals(givenPassword)){
				// let request to continue
				// null means success 
				return null;
			}			
		}		
		
		// authentication failed
		throw new AuthenticationException("Unauthorized");
		// authentication failed, request the authetication, add the realm
		// name if needed to the value of WWW-Authenticate
		//return Response.status(Response.Status.UNAUTHORIZED).header("WWW-Authenticate", "Basic").build();

	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}



}