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

public class AuthIds implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6092067815025057485L;

	private String policyId;
	
	private int policyContextId;
	
	private int policyBucketId;
	
	private boolean statusActive;
	
	private String bucketId;
	
	public AuthIds(String policyId, String bucketId, int policyContextId, int policyBucketId, boolean statusActive) {
		this.policyId = policyId;
		this.policyContextId = policyContextId;
		this.policyBucketId = policyBucketId;
		this.statusActive = statusActive;
		this.bucketId = bucketId;
	}
	
	public int getPolicyBucketId() {
		return policyBucketId;
	}
	
	public int getPolicyContextId() {
		return policyContextId;
	}
	
	public String getPolicyId() {
		return policyId;
	}
	
	public String getBucketId() {
		return bucketId;
	}
	
	public void setPolicyBucketId(int policyBucketId) {
		this.policyBucketId = policyBucketId;
	}
	
	public void setPolicyContextId(int policyContextId) {
		this.policyContextId = policyContextId;
	}
	
	public void setPolicyId(String policyId) {
		this.policyId = policyId;
	}
	
	public void setBucketId(String bucketId) {
		this.bucketId = bucketId;
	}

	public boolean isStatusActive() {
		return statusActive;
	}

	public void setStatusActive(boolean statusActive) {
		this.statusActive = statusActive;
	}
}
