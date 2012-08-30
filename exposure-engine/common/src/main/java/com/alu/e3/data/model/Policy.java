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

import com.alu.e3.data.model.sub.Context;
import com.alu.e3.data.model.sub.HeaderTransformation;
import com.alu.e3.data.model.sub.QuotaRLBucket;
import com.alu.e3.data.model.sub.TdrGenerationRule;

public class Policy implements Serializable {
	/**
	 * USID - Unique Serial ID
	 */
	private static final long serialVersionUID = -1748894244200870901L;

	private String id;
	private List<String> apiIds;
	private List<QuotaRLBucket> authIds;

	private transient List<Context> contexts;

	private List<Integer> contextIds;

	private TdrGenerationRule tdrGenerationRule;

	private String tdrOnLimitReached;

	private Map<String, String> properties;

	private List<HeaderTransformation> headerTransformations;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public List<String> getApiIds() {
		if (apiIds==null) apiIds = new ArrayList<String>();
		return apiIds;
	}
	public List<QuotaRLBucket> getAuthIds() {
		if (authIds==null) authIds = new ArrayList<QuotaRLBucket>();
		return authIds;
	}

	public List<Context> getContexts() {
		if (contexts==null) contexts = new ArrayList<Context>();
		return contexts;
	}

	public TdrGenerationRule getTdrGenerationRule() {
		return tdrGenerationRule;
	}

	public void setTdrGenerationRule(TdrGenerationRule tdrGenerationRules) {
		this.tdrGenerationRule = tdrGenerationRules;
	}

	public List<Integer> getContextIds() {
		if (contextIds==null) contextIds = new ArrayList<Integer>();
		return contextIds;
	}

	public void setTdrOnLimitReached(String tdrOnLimitReached) {
		this.tdrOnLimitReached = tdrOnLimitReached;
	}
	public String getTdrOnLimitReached() {
		return tdrOnLimitReached;
	}
	public Map<String,String> getProperties() {
		if(this.properties==null) this.properties = new HashMap<String,String>();
		return this.properties;
	}

	public void setProperties(Map<String,String> props){
		this.properties = props;
	}
	
	public List<HeaderTransformation> getHeaderTransformations() {
		return this.headerTransformations;
	}
	
	public void setHeaderTransformation(List<HeaderTransformation> hts) {
		this.headerTransformations = hts;
	}
}
