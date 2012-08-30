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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alu.e3.common.E3Constant;
import com.alu.e3.common.caching.IEntryListener;
import com.alu.e3.common.osgi.api.ITopologyClient;
import com.alu.e3.data.DataEntryEvent;
import com.alu.e3.data.model.Instance;
import com.alu.e3.data.model.Limit;
import com.alu.e3.rate.model.GatewayQueueRate;
import com.alu.e3.rate.model.GatewayRate;

public class GatewayRateValidator implements IGatewayRateValidator, IEntryListener<String, ArrayList<Instance>> {

	private static final Logger logger = LoggerFactory.getLogger(GatewayRateValidator.class);
	
	private GatewayDataManager gdm;
	private ITopologyClient topologyClient;
	
	private int gatewaysCount = 1;
	
	public void setTopologyClient(ITopologyClient topologyClient) {
		this.topologyClient = topologyClient;
	}
	
	public void setGatewayDataManager(GatewayDataManager gdm) {
		this.gdm = gdm;
	}

	public void init() {
		updateGatewaysCount();
		topologyClient.addInstanceTypeListener(this);
	}
	
	public void destroy() {
		topologyClient.removeInstanceTypeListener(this);
	}

	@Override
	public GatewayRate getRateForBucket(Integer bucketID) {
		GatewayRate gr = this.gdm.getFromRateMap(bucketID);
		
		if (gr == null) {
			gr = new GatewayRate(bucketID);
			this.gdm.putInRateMap(gr.bucketID, gr);
		}

		return gr;
	}

	@Override
	public GatewayRate getRateForBucket(Integer bucketID, Limit limit, boolean mustResetRateLimit) {
		GatewayRate gr = this.gdm.getFromRateMap(bucketID);
		
		if (gr == null) {
			gr = new GatewayRate(bucketID, limit.getCreatedDate());
			this.gdm.putInRateMap(gr.bucketID, gr);
		}
		
		if (mustResetRateLimit) {
			logger.debug("rate limit is reset");
			gr.initialized = false; // force a reset of the rate limit
		}

		return gr;
	}

	@Override
	public GatewayQueueRate getQueueRateForBucket(Integer bucketID) {
		GatewayQueueRate gqr = this.gdm.getFromQueueRateMap(bucketID);			

		if (gqr == null) {
			gqr = new GatewayQueueRate(bucketID);
			this.gdm.putInQueueRateMap(gqr.bucketID, gqr);
		}
		
		return gqr;
	}

	@Override
	public boolean isRateLimitInvalid(long currentTime) {
		// don't handle any expiration time
		return false;
	}
	
	private int resetRateLimit(long threshold) {
		int rateLimit; 
	
		if (threshold == -1) {
			rateLimit = -1;
		}
		else {
			rateLimit = (int)(threshold / this.gatewaysCount);
		}
		
		return rateLimit;
	}

	@Override
	public void updateRateLimitAndQuotaValues(GatewayRate gatewayRate, Limit limit, long currentTime) {
		if (!gatewayRate.initialized) {
			
			if (limit.getRateLimitPerSecond() != null) { 
				gatewayRate.localRateLimitSecond = resetRateLimit(limit.getRateLimitPerSecond().getThreshold());
			}
			
			if (limit.getRateLimitPerMinute() != null) { 
				gatewayRate.localRateLimitMinute = resetRateLimit(limit.getRateLimitPerMinute().getThreshold());
			}
			
			if (limit.getQuotaPerDay() != null) {
				gatewayRate.localQuotaLimitDay = resetRateLimit(limit.getQuotaPerDay().getThreshold());
			}
			
			if (limit.getQuotaPerWeek() != null) {
				gatewayRate.localQuotaLimitWeek = resetRateLimit(limit.getQuotaPerWeek().getThreshold());
			}
			
			if (limit.getQuotaPerMonth() != null) {
				gatewayRate.localQuotaLimitMonth = resetRateLimit(limit.getQuotaPerMonth().getThreshold());
			}
			
			gatewayRate.initialized = true;
		}
	}
	
	private boolean isValueOverLimit(long value, long limit) {
		return (limit != -1) && (value >= limit);
	}

	@Override
	public void checkRateLimitAndQuota(ApiCallStatus apiCallStatus, RateLimitParams params) {
		GatewayRate gr = params.gr;
		Limit limit = params.limit;
		
		if (isValueOverLimit(gr.localApiCallsSinceOneSecond, gr.localRateLimitSecond)) {
			apiCallStatus.apiCallIsSuccess = false;
			apiCallStatus.apiCallAction = (limit.getRateLimitPerSecond().getAction() != null) ? limit.getRateLimitPerSecond().getAction() : E3Constant.DEFAULT_ERROR_ACTION;
		}
		else if (isValueOverLimit(gr.localApiCallsSinceOneMinute, gr.localRateLimitMinute)) {
			apiCallStatus.apiCallIsSuccess = false;
			apiCallStatus.apiCallAction = (limit.getRateLimitPerMinute().getAction() != null) ? limit.getRateLimitPerMinute().getAction() : E3Constant.DEFAULT_ERROR_ACTION;
		}
		else if (isValueOverLimit(gr.localApiCallsSinceOneDay, gr.localQuotaLimitDay)) {
			apiCallStatus.apiCallIsSuccess = false;
			apiCallStatus.apiCallAction = (limit.getQuotaPerDay().getAction() != null) ? limit.getQuotaPerDay().getAction() : E3Constant.DEFAULT_ERROR_ACTION;
		}
		else if (isValueOverLimit(gr.localApiCallsSinceOneWeek, gr.localQuotaLimitWeek)) {
			apiCallStatus.apiCallIsSuccess = false;
			apiCallStatus.apiCallAction = (limit.getQuotaPerWeek().getAction() != null) ? limit.getQuotaPerWeek().getAction() : E3Constant.DEFAULT_ERROR_ACTION;
		}
		else if (isValueOverLimit(gr.localApiCallsSinceOneMonth, gr.localQuotaLimitMonth)) {
			apiCallStatus.apiCallIsSuccess = false;
			apiCallStatus.apiCallAction = (limit.getQuotaPerMonth().getAction() != null) ? limit.getQuotaPerMonth().getAction() : E3Constant.DEFAULT_ERROR_ACTION;
		}		
	}

	@Override
	public void updateRateWithStatus(GatewayRate gatewayRate, ApiCallStatus apiCallStatus) {
		// nothing to do
	}

	@Override
	public void entryAdded(DataEntryEvent<String, ArrayList<Instance>> event) {
		if (E3Constant.E3GATEWAY_ACTIVE.equals(event.getKey())) {
			updateGatewaysCount();
		}
	}

	@Override
	public void entryUpdated(DataEntryEvent<String, ArrayList<Instance>> event) {
		if (E3Constant.E3GATEWAY_ACTIVE.equals(event.getKey())) {
			updateGatewaysCount();
		}
	}

	@Override
	public void entryRemoved(DataEntryEvent<String, ArrayList<Instance>> event) {
		if (E3Constant.E3GATEWAY_ACTIVE.equals(event.getKey())) {
			updateGatewaysCount();
		}
	}

	private void updateGatewaysCount() {
		List<Instance> l = this.topologyClient.getAllInstancesOfType(E3Constant.E3GATEWAY_ACTIVE);
		if ((l!=null) )
		{
			this.gatewaysCount = l.size();

			if (this.gatewaysCount == 0) {
				logger.error("updateGatewaysCount: gateway count is 0");
				this.gatewaysCount = 1;
			}
		}
	}
}
