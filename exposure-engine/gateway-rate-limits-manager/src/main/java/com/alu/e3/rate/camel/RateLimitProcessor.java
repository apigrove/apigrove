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
package com.alu.e3.rate.camel;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alu.e3.common.camel.AuthIdentity;
import com.alu.e3.common.camel.ExchangeConstantKeys;
import com.alu.e3.data.model.enumeration.ActionType;
import com.alu.e3.data.model.sub.TdrGenerationRule;
import com.alu.e3.gateway.common.camel.exception.GatewayException;
import com.alu.e3.gateway.common.camel.exception.GatewayExceptionCode;
import com.alu.e3.rate.manager.IGatewayRateManager;
import com.alu.e3.rate.model.LimitCheckResult;
import com.alu.e3.tdr.TDRConstant;
import com.alu.e3.tdr.TDRDataService;

public class RateLimitProcessor implements Processor {
	protected IGatewayRateManager rateManager;	

	private final static Logger log = LoggerFactory.getLogger(RateLimitProcessor.class);

	public IGatewayRateManager getGtwRateMger() {
		return rateManager;
	}

	public void setGatewayRateMger(IGatewayRateManager gtwRateMger) {
		if(log.isDebugEnabled()) {
			log.debug("GatewayRateManager set in RateLimit processor");
		}
		this.rateManager = gtwRateMger;
	}

	@Override 
	public void process(Exchange exchange) throws Exception {

		if (rateManager == null) {
			throw new GatewayException(GatewayExceptionCode.RATEORQUOTA, "Rate Manager is null");
		}

		/* 
		 * TODO extract data required by the rate manager to compute the rate limits from the parameters inside exchange		
		 */
		Object property = exchange.getProperty(ExchangeConstantKeys.E3_AUTH_IDENTITY.toString());
		if (property == null || ! (property instanceof AuthIdentity)) {
			throw new GatewayException(GatewayExceptionCode.RATEORQUOTA, "The property " + ExchangeConstantKeys.E3_AUTH_IDENTITY.toString() + " is required to check the rate limits.");
		}

		boolean isTDREnabled = exchange.getProperty(ExchangeConstantKeys.E3_TDR_ENABLED.toString(), boolean.class);
		AuthIdentity authIdentity = (AuthIdentity) property;
		LimitCheckResult limitCheckResult = rateManager.isAllowed(authIdentity, isTDREnabled);

		if(isTDREnabled){
			/**
			 * Put some TDR data into into service
			 */
			TDRDataService.addTxTDRProperty(exchange, TDRConstant.OVER_QUOTA, limitCheckResult.isOverQuota());
			TDRDataService.addTxTDRProperty(exchange, TDRConstant.OVER_QUOTA_SEC, limitCheckResult.isOverSecond());
			TDRDataService.addTxTDRProperty(exchange, TDRConstant.OVER_QUOTA_MIN, limitCheckResult.isOverMinute());
			TDRDataService.addTxTDRProperty(exchange, TDRConstant.OVER_QUOTA_DAY, limitCheckResult.isOverDay());
			TDRDataService.addTxTDRProperty(exchange, TDRConstant.OVER_QUOTA_WEEK, limitCheckResult.isOverWeek());
			TDRDataService.addTxTDRProperty(exchange, TDRConstant.OVER_QUOTA_MONTH, limitCheckResult.isOverMonth());

			for(String tdrName : limitCheckResult.getTdrValues().keySet()){
				List<TdrGenerationRule> genRules = limitCheckResult.getTdrValues().get(tdrName);
				for(TdrGenerationRule genRule : genRules){
					TDRDataService.addNewTdrGenerationRule(exchange, genRule, tdrName);
				}
			}
		}


		// Route allowed if null - no action error defined
		if (limitCheckResult != null && limitCheckResult.getActionType() != null) {
			if(isTDREnabled) {
				exchange.setProperty(ExchangeConstantKeys.E3_RATELIMIT_ACTION.toString(), limitCheckResult.getActionType().toString());
				TDRDataService.addTxTDRProperty(exchange, TDRConstant.OVER_QUOTA_ACTION, limitCheckResult.getActionType().toString());
			}

			// Log - Will be based on the Logging Framework developed internally
			//log.debug("Route rate limit for API " + (authIdentity.getApi()==null ? "(no API)" : authIdentity.getApi().getId()) + " exeeded" + ((authIdentity.getAuth()==null) ? "": "by " + authIdentity.getAuth().getId()));
			// TDR Notification - not yet implemented 
			// HTTP Status Code 429 - to be return by the error processor of the route

			if(limitCheckResult.getActionType().equals(ActionType.REJECT)) {
				throw new GatewayException(GatewayExceptionCode.RATEORQUOTA, "Rate limit exceeded. " + limitCheckResult.getActionTypeMessage(), limitCheckResult.getActionType().toString());
			}
		}

	}

}
