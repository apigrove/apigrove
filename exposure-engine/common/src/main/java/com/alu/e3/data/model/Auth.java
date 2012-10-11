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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alu.e3.data.model.enumeration.StatusType;
import com.alu.e3.data.model.sub.AuthIds;
import com.alu.e3.data.model.sub.HeaderTransformation;
import com.alu.e3.data.model.sub.TdrGenerationRule;

public class Auth implements Serializable {

	/**
	 * USID - Unique Serial ID
	 */
	private static final long serialVersionUID = 4591944093429136320L;

	private String id;
	private StatusType status;
	
	private List<AuthIds> policyContexts;

	private String apiContext;
	private String policyContext;

	private TdrGenerationRule tdrGenerationRule;
	transient private AuthDetail authDetail;

	private Map<String, String> properties;

	private List<HeaderTransformation> headerTransformations;

	// Must define the wssePassword on the Auth object
	// because it must be available to be examined from the Gateway.
	private byte[] wssePassword;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public StatusType getStatus() {
		return status;
	}
	public void setStatus(StatusType status) {
		this.status = status;
	}
	
	public List<AuthIds> getPolicyContexts() {
		if (policyContexts==null) policyContexts = new ArrayList<AuthIds>();
		return policyContexts;
	}

	public void setPolicyContexts(List<AuthIds> policyContexts) {
		this.policyContexts = policyContexts;
	}

	public String getPolicyContext() {
		return policyContext;
	}
	public void setPolicyContext(String value) {
		this.policyContext = value;
	}
	public String getApiContext() {
		return apiContext;
	}
	public void setApiContext(String value) {
		this.apiContext = value;
	}

	public TdrGenerationRule getTdrGenerationRule() {
		return tdrGenerationRule;
	}
	public void setTdrGenerationRule(TdrGenerationRule tdrGenerationRule) {
		this.tdrGenerationRule = tdrGenerationRule;
	}

	public AuthDetail getAuthDetail() {
		return authDetail;
	}
	public void setAuthDetail(AuthDetail authDetail) {
		this.authDetail = authDetail;
	}
	public Map<String, String> getProperties() {
		if(this.properties == null){
			this.properties = new HashMap<String,String>();
		}
		return this.properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public List<HeaderTransformation> getHeaderTransformations() {
		return this.headerTransformations;
	}

	public void setHeaderTransformation(List<HeaderTransformation> hts) {
		this.headerTransformations = hts;
	}
	public byte[] getWssePassword() {
		return wssePassword;
	}
	public void setWssePassword(byte[] wssePassword) {
		this.wssePassword = wssePassword;
	}
}
