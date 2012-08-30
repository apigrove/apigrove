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

public class CallDescriptor {
	
	// May be null if the contextId/bucketId is for an ApiContext
	private Policy policy;
	
	private Integer contextId;
	
	private Integer bucketId;
	
	public CallDescriptor(Policy policy, int contextId, int bucketId) {
		this.policy = policy;
		this.contextId = contextId;
		this.bucketId = bucketId;
	}

	public Integer getBucketId() {
		return bucketId;
	}
	
	public Integer getContextId() {
		return contextId;
	}
	
	public Policy getPolicy() {
		return policy;
	}
	
	public void setBucketId(Integer bucketId) {
		this.bucketId = bucketId;
	}
	
	public void setContextId(Integer contextId) {
		this.contextId = contextId;
	}
	
	public void setPolicy(Policy policy) {
		this.policy = policy;
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == null)
			return false;
		
		CallDescriptor cd = (CallDescriptor) object;
		
		if(!contextId.equals(cd.getContextId()))
			return false;
		
		if(!bucketId.equals(cd.getBucketId()))
			return false;
		
		boolean equals = false;
		if(policy != null) {
			if(cd.getPolicy() != null && policy.getId() != null && cd.getPolicy().getId() != null) {
				equals = policy.getId().equals(cd.getPolicy().getId());
			}
		} else {
			if(cd.getPolicy() == null) {
				equals = true;
			}
		}
		
		return equals;

	}

}

