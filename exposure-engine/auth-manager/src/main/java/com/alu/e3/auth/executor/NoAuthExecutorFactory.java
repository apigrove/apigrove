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
package com.alu.e3.auth.executor;

import java.util.Map;

import org.apache.camel.impl.DefaultComponent;

import com.alu.e3.auth.access.IAuthDataAccess;

public class NoAuthExecutorFactory extends BaseExecutorFactory {

	protected IAuthDataAccess dataAccess;
	
	public NoAuthExecutorFactory() {}
	
	public IAuthDataAccess getDataAccess() {
		return dataAccess;
	}

	public void setDataAccess(IAuthDataAccess dataAccess) {
		this.dataAccess = dataAccess;
	}
	
	@Override
	public IAuthExecutor getExecutor(DefaultComponent component, String apiId, Map<String, Object> parameters) {
		return new NoAuthExecutor(apiId, dataAccess);
	}

	@Override
	public String getName() {
		return "noAuth";
	}

}
