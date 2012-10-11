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
package com.alu.e3.auth.camel.endpoint;

import java.util.List;

import org.apache.camel.CamelException;
import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;

import com.alu.e3.auth.camel.producer.AuthProducer;
import com.alu.e3.auth.executor.IAuthExecutor;
import com.alu.e3.common.osgi.api.IDataManager;

public class AuthEndpoint extends DefaultEndpoint {

	private String uri;
	private String apiId;
	private IDataManager dataManager;
	
	private List<IAuthExecutor> executors;
	
	public AuthEndpoint(String uri, Component component, List<IAuthExecutor> executors, IDataManager dataManager, String apiId) throws CamelException {
		super(uri, component);
		this.uri = uri;
		this.dataManager = dataManager;
		this.apiId = apiId;
		this.executors = executors;
	}
	
	@Override
	public String getEndpointUri() {
		return uri;
	}
	 
	@Override  
	public Producer createProducer() throws Exception {
		return new AuthProducer(this, executors, dataManager, apiId);
	}

	@Override
	public Consumer createConsumer(Processor processor) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSingleton() {
		// TODO Auto-generated method stub
		return false;
	}

}
