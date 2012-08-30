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
package com.alu.e3.gateway.targethealthcheck;

import com.alu.e3.gateway.loadbalancer.ManagedTargetHost;

/**
 * Interface that each TargetHealthCheck Service must implement.
 * 
 * A TargetHealthCheck Service monitors a set of ManagedTargetHost and update their status.
 * This status is used by the LoadBalancer to determine if a target can be used or not.
 *
 */
public interface ITargetHealthCheckService {
	
	/**
	 * The name of the HealthCheck Service. Must be unique for all services.
	 * This is the name that must be used at API Provisioning time.
	 * 
	 * @return The name of the HealthCheck Service.
	 */
	public String getName();
	
	/**
	 * init-method of the bean, called by Spring.
	 */
	public void init();
	
	/**
	 * Called by TargetHostManager when the HealthCheckService has been registered.
	 * This should start the target health check operation.
	 */
	public void start();
	
	/**
	 * Stops the target health check operation.
	 */
	public void stop();
	
	/**
	 * destroy-method of the bean, called by Spring.
	 * You should stop the health check operation in this method.
	 */
	public void destroy();

	/**
	 * Registers a ManagedTargetHost to this health check service.
	 * This means that this health check service will now monitor this target.
	 * @param target The ManagedTargetHost to monitor
	 */
	public void registerTarget(ManagedTargetHost target);
	
	/**
	 * Removes a target from health check service
	 * @param target The ManagedTargetHost to remove from monitoring
	 */
	public void unregisterTarget(ManagedTargetHost target);


}