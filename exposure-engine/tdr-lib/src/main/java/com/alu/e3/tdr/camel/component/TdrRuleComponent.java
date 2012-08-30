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
package com.alu.e3.tdr.camel.component;

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

import com.alu.e3.tdr.camel.endpoint.TdrRuleEndpoint;
import com.alu.e3.tdr.service.ITdrQueueService;

/**
 * TDRRuleComponent is used to add extracted tdr data from exchange along a route
 * to an internal TDR map.
 */
public class TdrRuleComponent extends DefaultComponent {

	private ITdrQueueService tdrQueueService;

	public TdrRuleComponent() {}
	
	@Override
	protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
		TdrRuleEndpoint tdrre = new TdrRuleEndpoint(uri, this, remaining, tdrQueueService);
		setProperties(tdrre, parameters);
		return tdrre;
	}

	public void setTdrQueueService(ITdrQueueService tdrQueueService) {
		if(tdrQueueService == null){
			throw new RuntimeException("Cannot initialize TdrRuleComponent, tdrQueueService is null");
		}

		this.tdrQueueService = tdrQueueService;
	}

}
