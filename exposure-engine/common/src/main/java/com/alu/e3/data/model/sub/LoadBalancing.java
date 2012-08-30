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

import com.alu.e3.data.model.enumeration.LoadBalancingType;

public class LoadBalancing implements Serializable {
	
	private static final long serialVersionUID = 8407909394763675964L;
	
	private LoadBalancingType loadBalancingType = LoadBalancingType.ROUND_ROBIN;
	private TargetHealthCheck targetHealthCheck;
	private FailOver failOver;
	
	public LoadBalancingType getLoadBalancingType() {
		return loadBalancingType;
	}
	public void setLoadBalancingType(LoadBalancingType loadBalancingType) {
		this.loadBalancingType = loadBalancingType;
	}

	public TargetHealthCheck getTargetHealthCheck() {
		return targetHealthCheck;
	}
	public void setTargetHealthCheck(TargetHealthCheck targetHealthCheck) {
		this.targetHealthCheck = targetHealthCheck;
	}

	public FailOver getFailOver() {
		return failOver;
	}
	public void setFailOver(FailOver failOver) {
		this.failOver = failOver;
	}    
}
