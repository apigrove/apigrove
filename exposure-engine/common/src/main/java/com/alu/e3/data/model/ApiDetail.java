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
package com.alu.e3.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.alu.e3.data.model.enumeration.ApiNotificationFormat;
import com.alu.e3.data.model.enumeration.ApiSubscriptionStep;
import com.alu.e3.data.model.enumeration.ApiType;
import com.alu.e3.data.model.enumeration.NBAuthType;
import com.alu.e3.data.model.enumeration.StatusType;
import com.alu.e3.data.model.sub.APIContext;
import com.alu.e3.data.model.sub.HTTPSType;

public class ApiDetail implements Serializable {
	/**
	 * USID - Unique Serial ID
	 */
	private static final long serialVersionUID = 8367478855366616966L;
	
	private String displayName;
	private String version;
	private ApiType type;
	private ApiSubscriptionStep subscriptionStep;
	private ApiNotificationFormat notificationFormat;
	private String endpoint;
	//Certificate for HTTPS NR request (c.f documentation)
	private HTTPSType https;
	private Boolean tdrEnabled;
	private Boolean notification;
	private List<NBAuthType> enabledAuthType;
	private String authKeyName;
	private String authHeaderName;
	private StatusType status;
	private transient List<APIContext> contexts;

	private String allowedMethods;
	
	private boolean isApiDeployed;
	
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
	public ApiType getType() {
		return type;
	}
	public void setType(ApiType type) {
		this.type = type;
	}
	
	public ApiSubscriptionStep getSubscriptionStep() {
		return subscriptionStep;
	}
	
	public void setSubscriptionStep(ApiSubscriptionStep subscriptionType) {
		this.subscriptionStep = subscriptionType;
	}
	
	public ApiNotificationFormat getNotificationFormat() {
		return notificationFormat;
	}
	
	public void setNotificationFormat(ApiNotificationFormat notificationFormat) {
		this.notificationFormat = notificationFormat;
	}
	
	public String getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	
	public Boolean getTdrEnabled() {
		return tdrEnabled;
	}
	public void setTdrEnabled(Boolean tdrEnabled) {
		this.tdrEnabled = tdrEnabled;
	}
	
	public Boolean getNotification() {
		return notification;
	}
	public void setNotification(Boolean notification) {
		this.notification = notification;
	}
	
	public List<NBAuthType> getEnabledAuthType() {
		if (enabledAuthType==null) enabledAuthType = new ArrayList<NBAuthType>();
		return enabledAuthType;
	}
	
	public String getAuthKeyName() {
		return authKeyName;
	}
	public void setAuthKeyName(String authKeyName) {
		this.authKeyName = authKeyName;
	}
	
	public String getAuthHeaderName() {
		return authHeaderName;
	}
	public void setAuthHeaderName(String authHeaderName) {
		this.authHeaderName = authHeaderName;
	}
	
	public List<APIContext> getContexts() {
		if (contexts==null) contexts = new ArrayList<APIContext>();
		return contexts;
	}
	
	public HTTPSType getHttps() {
		return https;
	}
	public void setHttps(HTTPSType httpsType) {
		this.https = httpsType;
	}
	
	public StatusType getStatus() {
		return status;
	}
	
	public void setStatus(StatusType status) {
		this.status = status;
	}
	
	public boolean isApiDeployed() {
		return isApiDeployed;
	}
	
	public void setIsApiDeployed(boolean isApiDeployed) {
		this.isApiDeployed = isApiDeployed;
	}

	public String getAllowedMethods() {
		return this.allowedMethods;
	}
	public void setAllowedMethods(String httpMethods) {
		this.allowedMethods = httpMethods;
	}
}
