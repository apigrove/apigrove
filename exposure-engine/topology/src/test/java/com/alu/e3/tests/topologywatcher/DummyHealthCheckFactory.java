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
package com.alu.e3.tests.topologywatcher;

import java.util.HashSet;
import java.util.Set;

import com.alu.e3.common.osgi.api.IHealthCheckFactory;
import com.alu.e3.data.IHealthCheckService;

public class DummyHealthCheckFactory implements IHealthCheckFactory {
	
	private Set<String> gatewaysActive = new HashSet<String>();
	private Set<String> gateways = new HashSet<String>();
	private Set<String> speakers = new HashSet<String>();

	public void setGatewaysActive(Set<String> gateways) {
		this.gatewaysActive = gateways;
	}
	
	public void setGateways(Set<String> gateways) {
		this.gateways = gateways;
	}
	
	public void setSpeakers(Set<String> speakers) {
		this.speakers = speakers;
	}

	@Override
	public IHealthCheckService getHealthCheckService(String instanceType) {
		return new DummyHealCheckService(instanceType, this);
	}
	
	public boolean check(String instanceType, String ip) {
		if (instanceType.equals(IHealthCheckFactory.GATEWAY_INTERNAL_TYPE)) {
			return gateways.contains(ip);
		}
		else if (instanceType.equals(IHealthCheckFactory.GATEWAY_TYPE)) {
			return gatewaysActive.contains(ip);
		}
		else if (instanceType.equals(IHealthCheckFactory.SPEAKER_TYPE)) {
			return speakers.contains(ip);
		}
		
		return false;
	}
}
