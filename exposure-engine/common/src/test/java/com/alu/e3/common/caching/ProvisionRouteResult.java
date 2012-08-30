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

public class ProvisionRouteResult implements Serializable {
	private static final long serialVersionUID = 7834547114448233788L;

	protected long provisionRouteId;
	protected TaskResult taskResult;
	protected String bundlePath;
	
	public void setProvisionRouteId(long provisionRouteId) {
		this.provisionRouteId = provisionRouteId;
	}
	public void setTaskResult(TaskResult taskResult) {
		this.taskResult = taskResult;
	}
	public String getBundlePath() {
		return bundlePath;
	}
	public void setBundlePath(String bundlePath) {
		this.bundlePath = bundlePath;
	}
	public ProvisionRouteResult(ProvisionRouteTask provisionRouteTask, TaskResult taskResult) {
		super();
		this.provisionRouteId = provisionRouteTask.getId();
		this.taskResult = taskResult;
	}
	public long getProvisionRouteId() {
		return provisionRouteId;
	}
	public TaskResult getTaskResult() {
		return taskResult;
	}
}
