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
/**
 * 
 */
package com.alu.e3.gateway.loadbalancer;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alu.e3.data.model.sub.TargetHost;
import com.alu.e3.gateway.loadbalancer.TargetHostManager.TargetStatus;
import com.alu.e3.gateway.targethealthcheck.ITargetHealthCheckService;

/**
 * Wraps a TargetHost (from E3's DataModel) with additional data used by LoadBalancer and TargetHostManager.
 */
public class ManagedTargetHost {

	private static final Logger logger = LoggerFactory.getLogger(TargetHostManager.class);
	
	protected final TargetHost targetHost;
	
	// Reference of the ManagedTargetHost, used by LoadBalancer to ask status of a ManagedTargetHost
	protected String reference;
	
	protected TargetStatus status;
	
	// Number of APIs using this ManagedTargetHost
	protected AtomicInteger numberOfUse;
	
	// Service on which this ManagedTargetHost is registered
	protected ITargetHealthCheckService healthCheckService;
	
	/**
	 * Construct a new ManagedTargetHost from a TargetHost. 
	 * Default status is AVAILABLE and numberOfUse is 0.
	 * @param targetHost The targetHost to add
	 */
	public ManagedTargetHost(TargetHost targetHost) {
		this.targetHost = targetHost;
		this.numberOfUse = new AtomicInteger();
		this.status = TargetStatus.AVAILABLE;
	}

	/**
	 * Returns the associated targetHost object from DataModel
	 * @return The associated targetHost object from DataModel
	 */
	public TargetHost getTargetHost() {
		return targetHost;
	}
	
	/**
	 * Returns the ManagedTargetHost reference
	 * @return ManagedTargetHost reference
	 */
	public String getReference() {
		return reference;
	}

	/**
	 * Sets the ManagedTargetHost reference
	 * @param reference The string to set as reference for this ManagedTargetHost
	 */
	public void setReference(String reference) {
		this.reference = reference;
	}
	
	/**
	 * Returns the HealthCheckService on which this object is registered on
	 * @return The HealthCheckService on which this object is registered on
	 */
	public ITargetHealthCheckService getHealthCheckService() {
		return healthCheckService;
	}
	
	/**
	 * Sets the HealthCheckService on which this object is registered on.
	 * @param healthCheckService
	 */
	public void setHealthCheckService(
			ITargetHealthCheckService healthCheckService) {
		this.healthCheckService = healthCheckService;
	}
	
	/**
	 * Returns the usage of this ManagedTargetHost, i.e. the number of deployed APIs that have the same target.
	 * @return usage of this ManagedTargetHost
	 */
	public AtomicInteger getNumberOfUse() {
		return numberOfUse;
	}

	/**
	 * Sets the usage of this ManagedTargetHost, i.e. the number of deployed APIs that have the same target.
	 * @param numberOfUse number of APIs
	 */
	public void setNumberOfUse(AtomicInteger numberOfUse) {
		this.numberOfUse = numberOfUse;
	}
	
	/**
	 * Returns the status of this ManagedTargetHost
	 * @return The status of this ManagedTargetHost
	 */
	public TargetStatus getStatus() {
		return status;
	}
	
	/**
	 * Sets the status of this ManagedTargetHost
	 * @param status The new status to set for this object
	 */
	public synchronized void setStatus(TargetStatus status) {
		this.status = status;
	}
	
	/**
	 * Determine if the object is AVAILABLE or not.
	 * @return true if status is AVAILABLE, false otherwise.
	 */
	public boolean isAvailable() {
		return status == TargetStatus.AVAILABLE;
	}
	
	/**
	 * Computes a "hash" from a TargetHost, used by TargetHostManager 
	 * to reuse ManagedTargetHost of different APIs with same targets.
	 * @param targetHost The TargetHost to hash
	 * @return The hash, composed of protocol+host+port
	 */
	public static String computeTargetHostHash(TargetHost targetHost) {
		return computeTargetHostHash(targetHost, "");
	}
	
	/**
	 * Hash of protocol+host+port + a custom part.
	 * Typically, the custom part is the name of the HealthCheckService. This means that 2 APIs with 
	 * same target but different HealthCheck Services are two ManagedTargetHost different objects.
	 * @param targetHost The TargetHost to hash
	 * @param custom A custom value to add to the hashing
	 * @return A Hash of protocol+host+port+custom
	 */
	public static String computeTargetHostHash(TargetHost targetHost, String custom) {
		URL url = parseUrl(targetHost.getUrl());
		String src = null;
		
		if(url != null) {
			src = url.getProtocol() + url.getHost() + url.getPort() + custom; 
		} else {
			src = UUID.randomUUID().toString();
		}
		
		return src;
	}

	/**
	 * Utility method that constructs an URL from a String without throwing exceptions.
	 * @param strUrl The URL to explode
	 * @return An URL object if URL was correct, null otherwise.
	 */
	public static URL parseUrl(String strUrl) {
		URL url = null;
		try {
			url = new URL(strUrl);
		} catch (MalformedURLException e) {
			logger.error("Error when exploding URL", e);
		}
		
		return url;
	}
	
	public String toString() {
		return "["+ reference + " ; " + targetHost.getUrl() + " ; " + status + "]";
	}
}
