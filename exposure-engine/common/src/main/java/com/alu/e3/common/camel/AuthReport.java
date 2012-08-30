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

public class AuthReport {
	
	private AuthIdentity authIdentity;
	private boolean isApiActive;
	private boolean isStatusChecked;
	private boolean isNotAuthorized;

	// error case
	private boolean hasNoPolicy;
	private boolean isApiNotFound;	
	private boolean isAuthNotFound;
	private boolean isBadRequest;

	/**
	 * Tells if allowed or not
	 * @return
	 */
	public boolean isAllowed() {
		return (authIdentity != null && isApiActive);
	}
	
	
	/**
	 * @return the isNotAuthorized
	 */
	public boolean isNotAuthorized() {
		return isNotAuthorized;
	}

	/**
	 * @param isNotAuthorized the isNotAuthorized to set
	 */
	public void setNotAuthorized(boolean isNotAuthorized) {
		this.isNotAuthorized = isNotAuthorized;
	}

	/**
	 * @return the hasNoPolicy
	 */
	public boolean hasNoPolicy() {
		return hasNoPolicy;
	}

	/**
	 * @param hasNoPolicy the hasNoPolicy to set
	 */
	public void setHasNoPolicy(boolean hasNoPolicy) {
		this.hasNoPolicy = hasNoPolicy;
	}

	/**
	 * @return the isApiNotFound
	 */
	public boolean isApiNotFound() {
		return isApiNotFound;
	}

	/**
	 * @param isApiNotFound the isApiNotFound to set
	 */
	public void setApiNotFound(boolean isApiNotFound) {
		this.isApiNotFound = isApiNotFound;
	}

	/**
	 * @return the authIdentity
	 */
	public AuthIdentity getAuthIdentity() {
		return authIdentity;
	}

	/**
	 * @param authIdentity the authIdentity to set
	 */
	public void setAuthIdentity(AuthIdentity authIdentity) {
		this.authIdentity = authIdentity;	
	}

	/**
	 * @return the isApiActive
	 */
	public boolean isApiActive() {
		return this.isApiActive;
	}
	
	/**
	 * @param isApiActive the isApiActive to set
	 */
	public void setApiActive(boolean isApiActive) {
		this.isApiActive = isApiActive;
		this.setStatusChecked(true);
	}

	/**
	 * @return the isAuthNotFound
	 */
	public boolean isAuthNotFound() {
		return isAuthNotFound;
	}

	/**
	 * @param isAuthNotFound the isAuthNotFound to set
	 */
	public void setAuthNotFound(boolean isAuthNotFound) {
		this.isAuthNotFound = isAuthNotFound;
	}

	/**
	 * @return the isBadrequest
	 */
	public boolean isBadRequest() {
		return isBadRequest;
	}

	/**
	 * @param isBadrequest the isBadrequest to set
	 */
	public void setBadRequest(boolean isBadRequest) {
		this.isBadRequest = isBadRequest;
	}


	/**
	 * @return the hasStatusChecked
	 */
	public boolean isStatusChecked() {
		return isStatusChecked;
	}


	/**
	 * @param hasStatusChecked the hasStatusChecked to set
	 */
	public void setStatusChecked(boolean isStatusChecked) {
		this.isStatusChecked = isStatusChecked;
	}



}
