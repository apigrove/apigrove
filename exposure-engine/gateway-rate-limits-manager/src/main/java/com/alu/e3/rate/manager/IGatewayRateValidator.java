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
package com.alu.e3.rate.manager;

import com.alu.e3.common.camel.AuthIdentity;
import com.alu.e3.data.model.CallDescriptor;
import com.alu.e3.data.model.Limit;
import com.alu.e3.data.model.enumeration.ActionType;
import com.alu.e3.rate.model.GatewayQueueRate;
import com.alu.e3.rate.model.GatewayRate;
import com.alu.e3.rate.model.LimitCheckResult;

public interface IGatewayRateValidator {

	class ApiCallStatus {
		public boolean apiCallIsSuccess;
		public ActionType apiCallAction;
	}
	
	class RateLimitParams {
		public GatewayRate gr;
		public Limit limit;
		public boolean isTDREnabled;
		public LimitCheckResult result;
		public AuthIdentity authIdentity;
		public CallDescriptor callDescriptor;
	}
	
	GatewayRate getRateForBucket(Integer bucketID);
	GatewayRate getRateForBucket(Integer bucketID, Limit limit, boolean mustResetRateLimit);
	GatewayQueueRate getQueueRateForBucket(Integer bucketID);
	boolean isRateLimitInvalid(long currentTime);
	void updateRateLimitAndQuotaValues(GatewayRate gatewayRate, Limit limit, long currentTime);
	void checkRateLimitAndQuota(ApiCallStatus apiCallStatus, RateLimitParams params);
	void updateRateWithStatus(GatewayRate gatewayRate, ApiCallStatus apiCallStatus);
}
