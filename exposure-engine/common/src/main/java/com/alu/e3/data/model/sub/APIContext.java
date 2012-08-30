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
package com.alu.e3.data.model.sub;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.alu.e3.data.model.enumeration.LoadBalancingType;
import com.alu.e3.data.model.enumeration.StatusType;

public class APIContext implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7629408427358132353L;
	
	private String id;
	private boolean defaultContext;
	private StatusType status;
	private LoadBalancing loadBalancing;
	private List<TargetHost> targetHosts;
	private int maxRateLimitTPSThreshold;
	private float maxRateLimitTPSWarning;
	private int maxRateLimitTPMThreshold;
	private float maxRateLimitTPMWarning;
	private transient Integer _REAL_maxRateLimitTPSWarning;
	private transient Integer _REAL_maxRateLimitTPMWarning;
	
	private int bucketId;

	private int contextId;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean isDefaultContext() {
		return defaultContext;
	}
	public void setDefaultContext(boolean defaultContext) {
		this.defaultContext = defaultContext;
	}
	
	public StatusType getStatus() {
		return status;
	}
	public void setStatus(StatusType status) {
		this.status = status;
	}
	public LoadBalancing getLoadBalancing() {
		if(loadBalancing == null) {
			loadBalancing = new LoadBalancing();
			loadBalancing.setLoadBalancingType(LoadBalancingType.ROUND_ROBIN);
		}
		
		return loadBalancing;
	}
	public void setLoadBalancing(LoadBalancing loadBalancing) {
		this.loadBalancing = loadBalancing;
	}	
	public List<TargetHost> getTargetHosts() {
		if(targetHosts==null) targetHosts = new ArrayList<TargetHost>();
		return targetHosts;
	}
	public Integer getMaxRateLimitTPSThreshold() {
		return maxRateLimitTPSThreshold;
	}
	public void setMaxRateLimitTPSThreshold(int maxRateLimitTPSThreshold) {
		this.maxRateLimitTPSThreshold = maxRateLimitTPSThreshold;
	}
	public float getMaxRateLimitTPSWarning() {
		return maxRateLimitTPSWarning;
	}
	public void setMaxRateLimitTPSWarning(float maxRateLimitTPSWarning) {
		this.maxRateLimitTPSWarning = maxRateLimitTPSWarning;
	}
	public int getMaxRateLimitTPMThreshold() {
		return maxRateLimitTPMThreshold;
	}
	public void setMaxRateLimitTPMThreshold(int maxRateLimitTPMThreshold) {
		this.maxRateLimitTPMThreshold = maxRateLimitTPMThreshold;
	}
	public float getMaxRateLimitTPMWarning() {
		return maxRateLimitTPMWarning;
	}
	public void setMaxRateLimitTPMWarning(float maxRateLimitTPMWarning) {
		this.maxRateLimitTPMWarning = maxRateLimitTPMWarning;
	}
	
	public int getBucketId() {
		return bucketId;
	}
	public int getContextId() {
		return contextId;
	}
	public void setBucketId(int bucketId) {
		this.bucketId = bucketId;
	}
	public void setContextId(int contextId) {
		this.contextId = contextId;
	}
	
	public int get_REAL_maxRateLimitTPSWarning() {
		if (_REAL_maxRateLimitTPSWarning==null) _REAL_maxRateLimitTPSWarning = Math.round((maxRateLimitTPSWarning/100.0f)*maxRateLimitTPSThreshold) ;
		return _REAL_maxRateLimitTPSWarning;
	}
	
	public int get_REAL_maxRateLimitTPMWarning() {
		if (_REAL_maxRateLimitTPMWarning==null) _REAL_maxRateLimitTPMWarning = Math.round((maxRateLimitTPMWarning/100.0f)*maxRateLimitTPMThreshold) ;
		return _REAL_maxRateLimitTPMWarning;
	}

}
