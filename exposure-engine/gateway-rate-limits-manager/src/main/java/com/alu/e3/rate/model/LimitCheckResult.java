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
package com.alu.e3.rate.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alu.e3.data.model.enumeration.ActionType;
import com.alu.e3.data.model.sub.TdrGenerationRule;

/**
 * This class is used as the result to the IGatewayRateManager::isAllowed function.  This enables us to get more data for TDRs
 * than simply returning the ActionType enum.
 * 
 *
 */
public class LimitCheckResult {
	/**
	 * The Action to be taken by the gateway
	 */
	private ActionType actionType;
	
	/**
	 * The messace linked to the ActionType
	 */
	private String actionTypeMessage = "";
		
	/**
	 * Whether or not a second ratelimit has been breached
	 */
	private boolean overSecond = false;
	
	/**
	 * Whether or not a minute ratelimit has been breached
	 */
	private boolean overMinute = false;
	
	/**
	 * Whether or not a daily quota has been breached
	 */
	private boolean overDay = false;
	
	/**
	 * whether or not a weekly quota has been breached 
	 */
	private boolean overWeek = false;
	
	/**
	 * Whether or not a monthly quota has been breached
	 */
	private boolean overMonth = false;
	
	/**
	 * The tdrValues that should be inserted into the TDR
	 */
	private Map<String, List<TdrGenerationRule>> tdrValues = new HashMap<String, List<TdrGenerationRule>>();
	
	public void addTdrValue(String tdrTypeName, TdrGenerationRule rule){
		if(!tdrValues.containsKey(tdrTypeName)){
			tdrValues.put(tdrTypeName, new ArrayList<TdrGenerationRule>());
		}
		
		tdrValues.get(tdrTypeName).add(rule);
	}
	
	public Map<String, List<TdrGenerationRule>> getTdrValues(){
		return this.tdrValues;
	}
	

	public boolean isOverQuota(){
		return overSecond || overMinute || overDay || overWeek || overMonth;
	}

	public ActionType getActionType() {
		return actionType;
	}

	public void setActionType(ActionType actionType) {
		this.actionType = actionType;
	}

	/**
	 * @return the actionTypeMessage
	 */
	public String getActionTypeMessage() {
		return actionTypeMessage;
	}

	/**
	 * @param actionTypeMessage the actionTypeMessage to set
	 */
	public void setActionTypeMessage(String actionTypeMessage) {
		this.actionTypeMessage = actionTypeMessage;
	}

	public boolean isOverSecond() {
		return overSecond;
	}

	public void setOverSecond(boolean overSecond) {
		this.overSecond = overSecond;
	}

	public boolean isOverMinute() {
		return overMinute;
	}

	public void setOverMinute(boolean overMinute) {
		this.overMinute = overMinute;
	}

	public boolean isOverDay() {
		return overDay;
	}

	public void setOverDay(boolean overDay) {
		this.overDay = overDay;
	}

	public boolean isOverWeek() {
		return overWeek;
	}

	public void setOverWeek(boolean overWeek) {
		this.overWeek = overWeek;
	}

	public boolean isOverMonth() {
		return overMonth;
	}

	public void setOverMonth(boolean overMonth) {
		this.overMonth = overMonth;
	}
}
