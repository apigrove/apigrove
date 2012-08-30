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

public class GatewayQueueRate  implements Serializable {

	private static final long serialVersionUID = -3108794664269206723L;
	
	public int bucketID;
	
	public short localApiCallsInFirstPeriod; // all call OK
	public short localApiSpeedInFirstPeriod; // all calls OK + KO
	
	public short localApiCallsInLastPeriodSecond;
	public int localApiCallsInLastPeriodMinute;
		
	public GatewayQueueRate()
	{
	
	}
	
	public GatewayQueueRate(int bucketID)
	{
		this.bucketID=bucketID;
		
		this.localApiCallsInFirstPeriod = 0;
		this.localApiSpeedInFirstPeriod = 0;
		this.localApiCallsInLastPeriodSecond = 0;
		this.localApiCallsInLastPeriodMinute = 0;
	}
	
	public boolean isReadyToRemove()
	{
		return (   (this.localApiCallsInFirstPeriod == 0)
			    && (this.localApiSpeedInFirstPeriod == 0)
			    && (this.localApiCallsInLastPeriodSecond == 0)
			    && (this.localApiCallsInLastPeriodMinute == 0)
			   );
	}
	
	/**
	 * @return the bucketID
	 */
	public int getBucketID() {
		return bucketID;
	}

	/**
	 * @param bucketID the bucketID to set
	 */
	public void setBucketID(int bucketID) {
		this.bucketID = bucketID;
	}

	/**
	 * @return the localApiCallsInFirstPeriod
	 */
	public short getLocalApiCallsInFirstPeriod() {
		return localApiCallsInFirstPeriod;
	}

	/**
	 * @param localApiCallsInFirstPeriod the localApiCallsInFirstPeriod to set
	 */
	public void setLocalApiCallsInFirstPeriod(short localApiCallsInFirstPeriod) {
		this.localApiCallsInFirstPeriod = localApiCallsInFirstPeriod;
	}

	/**
	 * @return the localApiSpeedInFirstPeriod
	 */
	public short getLocalApiSpeedInFirstPeriod() {
		return localApiSpeedInFirstPeriod;
	}

	/**
	 * @param localApiSpeedInFirstPeriod the localApiSpeedInFirstPeriod to set
	 */
	public void setLocalApiSpeedInFirstPeriod(short localApiSpeedInFirstPeriod) {
		this.localApiSpeedInFirstPeriod = localApiSpeedInFirstPeriod;
	}

	/**
	 * @return the localApiCallsInLastPeriodSecond
	 */
	public short getLocalApiCallsInLastPeriodSecond() {
		return localApiCallsInLastPeriodSecond;
	}

	/**
	 * @param localApiCallsInLastPeriodSecond the localApiCallsInLastPeriodSecond to set
	 */
	public void setLocalApiCallsInLastPeriodSecond(
			short localApiCallsInLastPeriodSecond) {
		this.localApiCallsInLastPeriodSecond = localApiCallsInLastPeriodSecond;
	}

	/**
	 * @return the localApiCallsInLastPeriodMinute
	 */
	public int getLocalApiCallsInLastPeriodMinute() {
		return localApiCallsInLastPeriodMinute;
	}

	/**
	 * @param localApiCallsInLastPeriodMinute the localApiCallsInLastPeriodMinute to set
	 */
	public void setLocalApiCallsInLastPeriodMinute(
			int localApiCallsInLastPeriodMinute) {
		this.localApiCallsInLastPeriodMinute = localApiCallsInLastPeriodMinute;
	}
}
