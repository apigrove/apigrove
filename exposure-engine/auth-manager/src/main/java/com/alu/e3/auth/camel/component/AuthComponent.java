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
package com.alu.e3.auth.camel.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelException;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.alu.e3.auth.camel.endpoint.AuthEndpoint;
import com.alu.e3.auth.executor.IAuthExecutor;
import com.alu.e3.auth.executor.IAuthExecutorFactory;

@Component
@Scope("singleton")
public class AuthComponent extends DefaultComponent {

	private Map<String, IAuthExecutorFactory> factories = new HashMap<String, IAuthExecutorFactory>();
	
	public AuthComponent() {}
	
	public AuthComponent(CamelContext camelContext) {
		super(camelContext);
	}
	
	@Override
	protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
		if(!parameters.containsKey("apiId"))
			throw new CamelException("apiId parameter is missing");
		
		String apiId = getAndRemoveParameter(parameters, "apiId", String.class);

		List<IAuthExecutor> executors = new ArrayList<IAuthExecutor>();
		
		Iterator<Entry<String, IAuthExecutorFactory>> it = factories.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, IAuthExecutorFactory> pairs = it.next();
			if(parameters.containsKey(pairs.getKey())){
				Boolean key = getAndRemoveParameter(parameters, pairs.getKey(), Boolean.class);
				if(key != null && key.equals(Boolean.TRUE)){
					IAuthExecutor executor = pairs.getValue().getExecutor(this, apiId, parameters);
					executors.add(executor);
				}
			}
	    }

		return new AuthEndpoint(uri, this, executors);
	}
	
	public void registerExecutorFactory(String name, IAuthExecutorFactory executor) {
		factories.put(name, executor);
	}
	
	public void unregisterExecutorFactory(String name) {
		factories.remove(name);
	}
}
