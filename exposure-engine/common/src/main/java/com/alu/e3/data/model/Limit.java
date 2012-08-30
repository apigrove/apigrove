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
package com.alu.e3.data.model;

import java.util.Date;

import com.alu.e3.data.model.sub.Counter;

public class Limit {

	private Counter quotaPerDay;
	private Counter quotaPerWeek;
	private Counter quotaPerMonth;
	private Counter rateLimitPerSecond;
	private Counter rateLimitPerMinute;
	private Date createdDate;
	
	public Counter getQuotaPerDay() {
		return quotaPerDay;
	}
	
	public Counter getQuotaPerWeek() {
		return quotaPerWeek;
	}
	
	public Counter getQuotaPerMonth() {
		return quotaPerMonth;
	}
	
	public Counter getRateLimitPerMinute() {
		return rateLimitPerMinute;
	}
	
	public Counter getRateLimitPerSecond() {
		return rateLimitPerSecond;
	}
	
	public void setQuotaPerDay(Counter quotaPerDay) {
		this.quotaPerDay = quotaPerDay;
	}
	
	public void setQuotaPerWeek(Counter quotaPerWeek) {
		this.quotaPerWeek = quotaPerWeek;
	}
	
	public void setQuotaPerMonth(Counter quotaPerMonth) {
		this.quotaPerMonth = quotaPerMonth;
	}
	public void setRateLimitPerMinute(Counter rateLimitPerMinute) {
		this.rateLimitPerMinute = rateLimitPerMinute;
	}
	
	public void setRateLimitPerSecond(Counter rateLimitPerSecond) {
		this.rateLimitPerSecond = rateLimitPerSecond;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
}
