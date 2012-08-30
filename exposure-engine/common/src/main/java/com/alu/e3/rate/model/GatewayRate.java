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

import java.io.Serializable;
import java.util.Date;

public class GatewayRate implements Serializable {
	
	private static final long serialVersionUID = 2524122037873871500L;

	public int bucketID;
	
	public int localRateLimitSecond;
	public int localRateLimitMinute;
	public long localQuotaLimitDay;
	public long localQuotaLimitWeek;
	public long localQuotaLimitMonth;
	
	// TODO: use constant values instead of properties?
	public short lifeTimeForlocalRateLimitSecond;
	public int lifeTimeForlocalRateLimitMinute;
	public int lifeTimeForlocalDailyQuota;
	public int lifeTimeForlocalWeeklyQuota;
	public long lifeTimeForlocalMonthlyQuota;
	
	public transient Date createdDate;
	public transient Date nextMonthUpdate;
	public transient Date nextWeekUpdate;

	public short localApiCallsSinceOneSecond;
	public int localApiCallsSinceOneMinute;
	public int localApiCallsSinceOneDay;
	public int localApiCallsSinceOneWeek;
	public long localApiCallsSinceOneMonth;
	
	public boolean initialized; // default limit has been set

	public GatewayRate(int bucketID) {
		this.bucketID=bucketID;
		
		this.localRateLimitSecond=-1;
		this.localRateLimitMinute=-1;
		this.localQuotaLimitDay=-1;
		this.localQuotaLimitWeek=-1;
		this.localQuotaLimitMonth=-1;
	}
	
	public GatewayRate(int bucketID, Date createdDate) {
		this(bucketID);
		this.createdDate = createdDate;
	}
}
