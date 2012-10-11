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

import java.util.HashSet;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alu.e3.common.E3Constant;
import com.alu.e3.common.camel.AuthIdentity;
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.data.IDataManagerUsedBucketIdsListener;
import com.alu.e3.data.model.CallDescriptor;
import com.alu.e3.data.model.Limit;
import com.alu.e3.rate.manager.GatewayDataManager.LockCounter;
import com.alu.e3.rate.manager.IGatewayRateValidator.ApiCallStatus;
import com.alu.e3.rate.manager.IGatewayRateValidator.RateLimitParams;
import com.alu.e3.rate.model.ApiCall;
import com.alu.e3.rate.model.GatewayQueueRate;
import com.alu.e3.rate.model.GatewayRate;
import com.alu.e3.rate.model.LimitCheckResult;

public class GatewayRateManager implements IGatewayRateManager {

	private static final Logger logger = LoggerFactory.getLogger(GatewayRateManager.class);
	
	private IDataManager dataManager;
	private IGatewayRateValidator gatewayRateValidator;
	
	private GatewayDataManager gdm;
	
	private Object lockUpdateSecondQueues = new Object();
	private Object lockUpdateMinuteQueues = new Object();
	
	public void init() {
		this.dataManager.addListener(this.gdm);
	}
	
	public void destroy(){
		this.dataManager.removeListener(this.gdm);
	}

	public void setDataManager(IDataManager dataManager) {
		this.dataManager = dataManager;
	}
	
	public void setGdm(GatewayDataManager gdm) {
		this.gdm = gdm;
	}
	
	public void setGatewayRateValidator(IGatewayRateValidator gatewayRateValidator) {
		this.gatewayRateValidator = gatewayRateValidator;
	}
	
	public void updateQueues(long currentTime) {
		ApiCall oldestCall = null;

		// update this.apiCallsPerMinuteQueue to remove call older than 1 minute	
		boolean removed = true;
		while (removed) {	
			removed = false;
			synchronized(lockUpdateMinuteQueues) {
				oldestCall = this.gdm.peekCallFromMinuteQueue();
				if  ((oldestCall!=null) && (currentTime - oldestCall.callTime > 60000)) {
					removed = this.gdm.removeCallFromMinuteQueue(oldestCall);
				}
			}
			
			// if the call was failed then it was not counted so no need to decrease the counter
			if (removed && oldestCall.callSuccess) {
				LockCounter lock = this.gdm.getLockForBucket(oldestCall.bucketID);

				synchronized (lock) {
					GatewayRate gr =  gatewayRateValidator.getRateForBucket(oldestCall.bucketID);
					gr.localApiCallsSinceOneMinute--;
					if (!oldestCall.countedInLastPeriodMinute) {
						// if the call was after the last collect and older than 1 minute
						// then we must count it
						GatewayQueueRate gqr = gatewayRateValidator.getQueueRateForBucket(oldestCall.bucketID);
						gqr.localApiCallsInLastPeriodMinute++;
					}
					this.gdm.releaseLockForBucket(oldestCall.bucketID, lock);
				}
			}
		}	

		// update this.apiCallsPerSecondQueue to remove call older than 1s
		removed = true;
		while (removed) {	
			removed = false;
			synchronized(lockUpdateSecondQueues) {
				oldestCall = this.gdm.peekCallFromSecondQueue();
				if  ((oldestCall!=null) && (currentTime - oldestCall.callTime > 1000)) {
					removed = this.gdm.removeCallFromSecondQueue(oldestCall);
				}
			}
			if (removed && oldestCall.callSuccess) {
				LockCounter lock = this.gdm.getLockForBucket(oldestCall.bucketID);

				synchronized (lock) {
					GatewayRate gr =  gatewayRateValidator.getRateForBucket(oldestCall.bucketID);
					gr.localApiCallsSinceOneSecond--;
					if (!oldestCall.countedInLastPeriodSecond) {
						// if the call was after the last collect and older than 1 second
						// then we must count it
						GatewayQueueRate gqr = gatewayRateValidator.getQueueRateForBucket(oldestCall.bucketID);
						gqr.localApiCallsInLastPeriodSecond++;
					}
					this.gdm.releaseLockForBucket(oldestCall.bucketID, lock);
				}
			}
		}	
	}

	
	
	

	@Override
	public LimitCheckResult isAllowed(AuthIdentity authIdentity, boolean isTDREnabled) {
		long currentTime = System.currentTimeMillis();
		
		updateQueues(currentTime);

		ApiCallStatus apiCallStatus = new ApiCallStatus();
		apiCallStatus.apiCallIsSuccess = true;
		apiCallStatus.apiCallAction = null;
		
		RateLimitParams params = new RateLimitParams();
		params.isTDREnabled = isTDREnabled;
		params.result = new LimitCheckResult();
		params.authIdentity = authIdentity;
		
		boolean mustResetRateLimit = gatewayRateValidator.isRateLimitInvalid(currentTime);
		
		// contain the id of the lock to unlocks locks in case of throw exception
		HashSet<Integer> lockedSet = new HashSet<Integer>();
		
		try {
			// step1 : test if call is success
			Iterator<CallDescriptor> it = authIdentity.getCallDescriptors().iterator();
			Limit limit = new Limit();

			while (it.hasNext()) {
				CallDescriptor callDescriptor = it.next();
				Integer bucketID = callDescriptor.getBucketId();
				Integer contextId = callDescriptor.getContextId();

				this.dataManager.fillLimitsById(contextId, limit);

				if ((limit.getQuotaPerDay()!=null) || (limit.getQuotaPerWeek()!=null) || (limit.getQuotaPerMonth()!=null) 
						|| (limit.getRateLimitPerMinute()!=null) || (limit.getRateLimitPerSecond()!=null)) {			
					
					LockCounter lock = this.gdm.getLockForBucket(bucketID);
					
					synchronized (lock) {
					
						lockedSet.add(bucketID);

						GatewayRate gr = gatewayRateValidator.getRateForBucket(bucketID, limit, mustResetRateLimit);
						
						gatewayRateValidator.updateRateLimitAndQuotaValues(gr, limit, currentTime);
						
						params.gr = gr;
						params.limit = limit;
						params.callDescriptor = callDescriptor;
						
						gatewayRateValidator.checkRateLimitAndQuota(apiCallStatus, params);
						
						this.gdm.releaseLockForBucket(bucketID, lock);
						lockedSet.remove(bucketID);
					}
				}
			}
			
			// TODO: maybe increment always in previous loop and decrement all gr only if call was failed 
			// step2: save data for call success or fail
			it = authIdentity.getCallDescriptors().iterator();
			while (it.hasNext())
			{
				CallDescriptor callDescriptor = it.next();
				Integer bucketID = callDescriptor.getBucketId();
				Integer contextId = callDescriptor.getContextId();
				this.dataManager.fillLimitsById(contextId, limit);

				if ((limit.getQuotaPerDay()!=null) || (limit.getQuotaPerWeek()!=null) || (limit.getQuotaPerMonth()!=null) 
						|| (limit.getRateLimitPerMinute()!=null) || (limit.getRateLimitPerSecond()!=null))
				{	
					LockCounter lock = this.gdm.getLockForBucket(bucketID);

					synchronized (lock) {
						
						GatewayRate gr = gatewayRateValidator.getRateForBucket(bucketID);
						GatewayQueueRate gqr = gatewayRateValidator.getQueueRateForBucket(bucketID);			
	
						if (apiCallStatus.apiCallIsSuccess)
						{
							gr.localApiCallsSinceOneMinute++;
							gr.localApiCallsSinceOneSecond++;
							gr.localApiCallsSinceOneDay++;
							gr.localApiCallsSinceOneWeek++;
							gr.localApiCallsSinceOneMonth++;
							gqr.localApiCallsInFirstPeriod++;
						}
						
						gatewayRateValidator.updateRateWithStatus(gr, apiCallStatus);
	
						gqr.localApiSpeedInFirstPeriod++;
						ApiCall call = new ApiCall(gr.bucketID, currentTime);
						call.callSuccess = apiCallStatus.apiCallIsSuccess;
						this.gdm.addCallToMinuteQueue(call);
						this.gdm.addCallToSecondQueue(call);
	
						this.gdm.releaseLockForBucket(bucketID, lock);
						lockedSet.remove(bucketID);
					}
				}
			}		
		}
		catch (Exception e) {
			if(logger.isErrorEnabled()) {
				logger.error("Error checking if a route isAllowed in GatewayRateManager due to an exception", e);
			}
			apiCallStatus.apiCallAction = E3Constant.DEFAULT_ERROR_ACTION;
			apiCallStatus.apiCallIsSuccess = false;
			params.result.setActionTypeMessage(e.toString());
			Iterator<Integer> it = lockedSet.iterator();
			while (it.hasNext())
			{
				Integer id = it.next();
				this.gdm.releaseLockForBucket(id);
			}
		}
		
		params.result.setActionType(apiCallStatus.apiCallAction);
		
		return params.result;
	}
}