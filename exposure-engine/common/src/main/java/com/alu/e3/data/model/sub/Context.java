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
package com.alu.e3.data.model.sub;

import java.io.Serializable;
import java.util.Date;

import com.alu.e3.data.model.enumeration.StatusType;

public class Context implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5290652101508735198L;
	
	private String id;
	private StatusType status;
	private Counter quotaPerDay;
	private Counter quotaPerWeek;
	private Counter quotaPerMonth;
	private Counter rateLimitPerSecond;
	private Counter rateLimitPerMinute;
	private Date createdDate;
	
	private int contextId;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public StatusType getStatus() {
		return status;
	}
	public void setStatus(StatusType status) {
		this.status = status;
	}
	public Counter getQuotaPerDay() {
		return quotaPerDay;
	}
	public void setQuotaPerDay(Counter quotaPerDay) {
		this.quotaPerDay = quotaPerDay;
	}
	public Counter getQuotaPerMonth() {
		return quotaPerMonth;
	}
	public void setQuotaPerMonth(Counter quotaPerMonth) {
		this.quotaPerMonth = quotaPerMonth;
	}
	public Counter getQuotaPerWeek() {
		return quotaPerWeek;
	}
	public void setQuotaPerWeek(Counter quotaPerWeek) {
		this.quotaPerWeek = quotaPerWeek;
	}
	public Counter getRateLimitPerMinute() {
		return rateLimitPerMinute;
	}
	public void setRateLimitPerMinute(Counter rateLimitPerMinute) {
		this.rateLimitPerMinute = rateLimitPerMinute;
	}
	public Counter getRateLimitPerSecond() {
		return rateLimitPerSecond;
	}
	public void setRateLimitPerSecond(Counter rateLimitPerSecond) {
		this.rateLimitPerSecond = rateLimitPerSecond;
	}
	
	public void setContextId(int contextId) {
		this.contextId = contextId;
	}
	
	public int getContextId() {
		return contextId;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
}
