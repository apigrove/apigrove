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
package com.alu.e3.prov.restapi.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The connection parameters for API target hosts. 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "loadBalancing", propOrder = {
    "targetHealthCheck",
    "failOver"
})
public class LoadBalancing {
	
	/**
	 * The type of load balancing.
	 */
	@XmlAttribute(name = "type", required = false)
	protected LoadBalancingType loadBalancingType = LoadBalancingType.ROUND_ROBIN;
	
	/**
	 * The target health check for this load balancing.
	 */
    @XmlElement(required = false)
    protected TargetHealthCheck targetHealthCheck;
    
    /**
     * The fail over for this load balancing.
     */
    @XmlElement(required = false)
    protected FailOver failOver;
	
	/**
	 * @return the loadBalancingType
	 */
	public LoadBalancingType getLoadBalancingType() {
		return loadBalancingType;
	}

	/**
	 * @param loadBalancingType the loadBalancingType to set
	 */
	public void setLoadBalancingType(LoadBalancingType loadBalancingType) {
		this.loadBalancingType = loadBalancingType;
	}

	/**
	 * @return the target Health Check attribute for this load balancing.
	 */
	public TargetHealthCheck getTargetHealthCheck() {
		return targetHealthCheck;
	}

	/**
	 * @param targetHealthCheck the targetHealthCheck to set
	 */
	public void setTargetHealthCheck(TargetHealthCheck targetHealthCheck) {
		this.targetHealthCheck = targetHealthCheck;
	}

	/**
	 * @return the fail over for this load balancing
	 */
	public FailOver getFailOver() {
		return failOver;
	}

	/**
	 * @param failOver the failOver to set
	 */
	public void setFailOver(FailOver failOver) {
		this.failOver = failOver;
	}    
}
