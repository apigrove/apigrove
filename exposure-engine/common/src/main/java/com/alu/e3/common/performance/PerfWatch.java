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
package com.alu.e3.common.performance;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PerfWatch {

	
	private  AtomicLong elapsedTime = new AtomicLong(0);
	private  AtomicInteger iterationCount = new AtomicInteger(0); 
	private static Logger logger = LoggerFactory.getLogger(PerfWatch.class);
	
	/**
	 * @return the iterationCount
	 */
	public AtomicInteger getIterationCount() {
		return iterationCount;
	}

	/**
	 * @return the elapsedTime
	 */
	public AtomicLong getElapsedTime() {
		return elapsedTime;
	}

	
	public void log(String str)
	{

		
		if (iterationCount.intValue()>10000)
		{
			if (logger.isInfoEnabled())
			{
				logger.info(" PerfWatch : " + str + " : " + iterationCount + " iteration in " + (elapsedTime.longValue()/1000000.0) + " ms");
			}
			
			iterationCount.set(0);
			elapsedTime.set(0);
			

		}
		
	}
	
}
