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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alu.e3.data.model.enumeration.StatusType;
import com.alu.e3.data.model.sub.ApiIds;
import com.alu.e3.data.model.sub.HeaderTransformation;
import com.alu.e3.data.model.sub.IForwardProxy;
import com.alu.e3.data.model.sub.TdrGenerationRule;
import com.alu.e3.data.model.sub.Validation;

public class Api implements Serializable {
	/**
	 * USID - Unique Serial ID
	 */
	private static final long serialVersionUID = -4157232186322050081L;

	private String id;
	private List<String> policyIds;
	private TdrGenerationRule tdrGenerationRule;
	private StatusType status;	

	transient private ApiDetail apiDetail;

	private List<ApiIds> contextIds;

	private int apiId;

	private String tdrOnUse;
	private String tdrOnLimitReached;

	private Validation validation;

	private Map<String, String> properties;

	private Boolean headerTransEnabled = false;

	private List<HeaderTransformation> headerTransformations;
	
	private IForwardProxy localProxy;
	private boolean useGlobalProxy;

	
	private Boolean internal = false;

	private List<String> whiteListedIps;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public List<String> getPolicyIds() {
		if (policyIds==null) policyIds = new ArrayList<String>();
		return policyIds;
	}

	public void setPolicyIds(List<String> policyIds) {
		this.policyIds = policyIds;
	}

	public TdrGenerationRule getTdrGenerationRule() {
		return tdrGenerationRule;
	}
	public void setTdrGenerationRule(TdrGenerationRule tdrGenerationRule) {
		this.tdrGenerationRule = tdrGenerationRule;
	}

	public void setApiDetail(ApiDetail apiDetail) {
		this.apiDetail = apiDetail;
	}
	public ApiDetail getApiDetail() {
		return apiDetail;
	}

	public List<ApiIds> getContextIds() {
		if (contextIds==null) contextIds = new ArrayList<ApiIds>();
		return contextIds;
	}

	public void setApiId(int apiId) {
		this.apiId = apiId;
	}

	public int getApiId() {
		return apiId;
	}

	public void setTdrOnUse(String tdrOnUse) {
		this.tdrOnUse = tdrOnUse;
	}
	public String getTdrOnUse() {
		return tdrOnUse;
	}

	public void setTdrOnLimitReached(String tdrOnLimitReached) {
		this.tdrOnLimitReached = tdrOnLimitReached;
	}
	public String getTdrOnLimitReached() {
		return tdrOnLimitReached;
	}
	public Validation getValidation() {
		return validation;
	}
	public void setValidation(Validation validation) {
		this.validation = validation;
	}
	public Map<String, String> getProperties() {
		if(properties==null) properties = new LinkedHashMap<String,String>();
		return properties;
	}
	public void setProperties(Map<String, String> props){
		this.properties = props;
	}
	public List<HeaderTransformation> getHeaderTransformations() {
		if(headerTransformations==null) headerTransformations = new ArrayList<HeaderTransformation>();
		return headerTransformations;
	}
	public void setHeaderTransformation(List<HeaderTransformation> hts) {
		this.headerTransformations = hts;
	}
	public Boolean getHeaderTransEnabled() {
		return headerTransEnabled;
	}
	public void setHeaderTransEnabled(Boolean headerTransEnabled) {
		this.headerTransEnabled = headerTransEnabled;
	}
	public Boolean getInternal(){
		return this.internal;
	}
	public void setInternal(boolean internal){
		this.internal = internal;
	}
	public StatusType getStatus() {
		return status;
	}
	public void setStatus(StatusType status) {
		this.status = status;
	}
	public IForwardProxy getForwardProxy() {
		return localProxy;
	}
	public void setForwardProxy(IForwardProxy localProxy) {
		this.localProxy = localProxy;
	}
	public boolean isUseGlobalProxy() {
		return useGlobalProxy;
	}
	public void setUseGlobalProxy(boolean useGlobalProxy) {
		this.useGlobalProxy = useGlobalProxy;
	}
	public List<String> getWhiteListedIps() {
		if (whiteListedIps==null) whiteListedIps = new ArrayList<String>();
		return whiteListedIps;
	}
}
