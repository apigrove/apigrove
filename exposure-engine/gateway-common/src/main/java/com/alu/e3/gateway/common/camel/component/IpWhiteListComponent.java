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
package com.alu.e3.gateway.common.camel.component;

import java.util.Map;

import org.apache.camel.CamelException;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.gateway.common.camel.endpoint.IpWhiteListEndpoint;

public class IpWhiteListComponent extends DefaultComponent {
	private IDataManager dataManager;

	public void setDataManager(IDataManager dataManager) {
		this.dataManager = dataManager;
	}

	@Override
	protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
		if(!parameters.containsKey("apiId"))
			throw new CamelException("apiId parameter is missing");
		
		String apiId = getAndRemoveParameter(parameters, "apiId", String.class);
		return new IpWhiteListEndpoint(uri, this, dataManager, apiId);
	}

}
