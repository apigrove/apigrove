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

import java.io.Serializable;

public class ProvisionRouteTask implements Serializable {
	private static final long serialVersionUID = -7018485375170353176L;

	protected long id;
	protected byte[] bundle;

	public long getId() {
		return id;
	}

	public byte[] getBundle() {
		return bundle;
	}

	public ProvisionRouteTask(long id, byte[] bundle) {
		super();
		this.id = id;
		this.bundle = bundle;
	}
	
	// This constructor can be used only when all the instances are created on the same machine 
	public ProvisionRouteTask(byte[] bundle) {
		super();
		id = System.nanoTime();
		this.bundle = bundle;
	}
}
