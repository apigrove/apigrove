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
package com.alu.e3.prov.service;

import java.util.List;

import com.alu.e3.prov.restapi.model.Api;
import com.alu.e3.prov.restapi.model.ApiContext;
import com.alu.e3.prov.restapi.model.Status;
import com.alu.e3.prov.restapi.model.SubscriptionStep;

public class CommonApiCheck implements ICommonApiCheck  {
	
	public CommonApiCheck(){
		
	}
	/* (non-Javadoc)
	 * @see com.alu.e3.prov.service.ICommonApiCheck#assertHasDefaultContext(com.alu.e3.prov.restapi.model.Api)
	 */
	@Override
	public final void assertHasDefaultContext(Api provRequest) {
		boolean hasDefault = false;
		for(ApiContext context : provRequest.getContexts()) {
			if(context.isDefaultContext()) {
				if(hasDefault)
					throw new IllegalArgumentException("An api must have one and only one default context.");
				else
					hasDefault = true;
			}
				
		}
		if(!hasDefault) {
			throw new IllegalArgumentException("An api must have one and only one default context.");
		}
	}
	
	/* (non-Javadoc)
	 * @see com.alu.e3.prov.service.ICommonApiCheck#assertCompositionApiConsistency(com.alu.e3.prov.restapi.model.Api)
	 */
	@Override
	public final boolean assertCompositionApiConsistency(Api provRequest) {
		boolean canCreateJarFile = false;
		
		if (provRequest.getSubscriptionStep() != null) {
			// This API is marked as Subscription or Notification
			if (SubscriptionStep.SUBSCRIPTION == provRequest.getSubscriptionStep()) {
				// Nothing special to test
				// OPTIONAL: 
				// 1- We can't have Notification format
				if (provRequest.getNotificationFormat() != null)
					throw new IllegalArgumentException("An api can't have a NotificationFormat in Subscription step mode.");
				// 2- We must have target hosts
				canCreateJarFile = assertHasAtLeastOneTargetHost(provRequest, "An active context must have at least one target host.");
			}
			else if (SubscriptionStep.NOTIFICATION == provRequest.getSubscriptionStep()) {
				// 1- We can't have target hosts
				assertHasNOTAnyTargetHost(provRequest, "An api marked as Notification step can't have any target host.");
				// 2- We must have a notification format
				if (provRequest.getNotificationFormat() == null)
					throw new IllegalArgumentException("An api marked as Notification must have a NotificationFormat.");
				
				// we can always create the JAR file in case of the provisioning of a notification API
				canCreateJarFile = true;
			}
		} else {
			// We are on MT standard call (either is a COMPOSITE or NOT api
			// 1- We must have target hosts
			canCreateJarFile = assertHasAtLeastOneTargetHost(provRequest, "An active context must have at least one target host.");
			// 2- We can't have notification format
			if (provRequest.getNotificationFormat() != null)
				throw new IllegalArgumentException("An api without Notification step flag can't have NotificationFormat.");
		}
		
		return canCreateJarFile;
	}

	private final boolean assertHasAtLeastOneTargetHost(Api provRequest, String msg) {
		boolean hasTargetHost = false;
		if (provRequest.getContexts() != null) {
			List<ApiContext> apiCtxs = provRequest.getContexts();
			for (ApiContext apiCtx : apiCtxs) {
				if (apiCtx.getStatus() == Status.ACTIVE) {
					// we must have at least one target host for an active API context
					if (apiCtx.getTargetHosts() == null || apiCtx.getTargetHosts().size() == 0) {
						throw new IllegalArgumentException(msg);						
					}
					else {
						hasTargetHost = true;
					}
				}
			}
		}
		
		return hasTargetHost;
	}
	
	private final void assertHasNOTAnyTargetHost(Api provRequest, String msg) {
		if (provRequest.getContexts() != null) {
			List<ApiContext> apiCtxs = provRequest.getContexts();
			for (ApiContext apiCtx : apiCtxs) {
				if (apiCtx.getTargetHosts() != null) {
					if (apiCtx.getTargetHosts().size() > 0) {
						throw new IllegalArgumentException(msg);
					}
				}
			}
		}
	}
}
