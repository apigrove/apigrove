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

import java.util.List;

/**
 * Interface of TargetHostManager.
 * 
 * A TargetHostManager acts as ManagedTargetHost Database and is used to maintain status of TargetHosts.
 * HttpLoadBalancer interacts with a TargetHostManager to get all Targets on which it must loadbalance requests for a given API.
 *
 */
public interface ITargetHostManager {
	
	/**
	 * Returns a list of TargetReferences for this pair ApiId/ApiContextId
	 * @param apiId The Api ID
	 * @param contextId The Api Context Id
	 * @return The list of TargetReferences for this pair ApiId/ApiContextId
	 */
	List<TargetReference> getTargetReferences(String apiId, String contextId);
	
	/**
	 * Returns if a Target is AVAILABLE
	 * @param targetReference The reference string of the Target to check for
	 * @return true if the target is AVAILABLE, false otherwise
	 */
	boolean isAvailable(String targetReference);
	
	/**
	 * Mark a Target as UNAVAILABLE.
	 * Call this method when sending a request to this target failed.
	 * 
	 * @param targetReference The reference string of the Target to mark as unavailable
	 */
	void notifyFailed(String targetReference);
	
}
