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

public class ApiIds implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3466986220094495971L;

	private int apiContextId;
	
	private String apiContextName;
	
	private int apiBucketId;
	
	private boolean statusActive;
	
	public ApiIds(String apiContextName, int apiContextId, int apiBucketId, boolean statusActive) {
		this.apiContextName = apiContextName;
		this.apiContextId = apiContextId;
		this.apiBucketId = apiBucketId;
		this.statusActive = statusActive;
	}
	
	public int getApiBucketId() {
		return apiBucketId;
	}
	
	public int getApiContextId() {
		return apiContextId;
	}
	
	public String getApiContextName() {
		return apiContextName;
	}
	
	public void setApiBucketId(int apiBucketId) {
		this.apiBucketId = apiBucketId;
	}
	
	public void setApiContextId(int apiContextId) {
		this.apiContextId = apiContextId;
	}
	
	public void setApiContextName(String apiContextName) {
		this.apiContextName = apiContextName;
	}

	public boolean isStatusActive() {
		return statusActive;
	}

	public void setStatusActive(boolean statusActive) {
		this.statusActive = statusActive;
	}
}
