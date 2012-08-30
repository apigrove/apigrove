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

import com.alu.e3.data.IHealthCheckService;

public class DummyHealCheckService implements IHealthCheckService {
	
	private String type;
	private DummyHealthCheckFactory factory;
	
	public DummyHealCheckService(String type, DummyHealthCheckFactory factory) {
		this.type = type;
		this.factory = factory;
	}

	@Override
	public void start() {
		// must not be called
		throw new RuntimeException();
	}

	@Override
	public void stop() {
		// must not be called
		throw new RuntimeException();
	}

	@Override
	public boolean check(String ip) {
		return factory.check(type, ip);
	}
	
}
