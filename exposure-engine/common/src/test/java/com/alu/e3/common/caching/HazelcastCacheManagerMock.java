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
package com.alu.e3.common.caching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alu.e3.common.E3Constant;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;

public class HazelcastCacheManagerMock extends HazelcastCacheManager {

	private static Logger logger = LoggerFactory.getLogger(HazelcastCacheManager.class);
	
	@Override
	public void init(){
		
		// check if an Hazelcast instance is already running
		setHazelcastInstance(Hazelcast.getHazelcastInstanceByName(E3Constant.HAZELCAST_NAME));
		
		if (this.hazelcastInstance  != null) {
			// should not happen except when running the JUnit
			if (logger.isWarnEnabled()) {
				logger.warn("WARNING: HazelcastCacheManager: HazelcastInstance is already running");
			}
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Normal start of new HazelcastInstance");
			}
			// create the instance
			Config cfg = new Config();
			cfg.setPort(E3Constant.HAZELCAST_PORT);
			cfg.setPortAutoIncrement(true);
			
			cfg.setInstanceName(E3Constant.HAZELCAST_NAME);

			cfg.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);

			setHazelcastInstance(Hazelcast.newHazelcastInstance(cfg));

			if (logger.isDebugEnabled()) {
				logger.debug("Normal start: done.");
			}
		}
	}

}
