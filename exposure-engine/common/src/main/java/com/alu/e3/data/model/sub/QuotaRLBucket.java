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

public class QuotaRLBucket implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7985922344329398444L;
	
	private String id;
	private List<String> authIds;
	
	private int bucketId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public List<String> getAuthIds() {
		if (authIds==null) authIds = new ArrayList<String>();
		return authIds;
	}

	public int getBucketId() {
		return bucketId;
	}
	
	public void setBucketId(int bucketId) {
		this.bucketId = bucketId;
	}
}
