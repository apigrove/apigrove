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

import java.io.File;

import com.alu.e3.common.tools.BundleTools;
import com.hazelcast.core.IQueue;

public class BundlesToDeployListener implements Runnable {
	protected IQueue<ProvisionRouteResult> deploymentResultQueue;
	protected IQueue<ProvisionRouteTask> provisionRouteQueue;

	public BundlesToDeployListener(IQueue<ProvisionRouteTask> provisionRouteQueue, IQueue<ProvisionRouteResult> deploymentResultQueue) {
		super();
		this.deploymentResultQueue = deploymentResultQueue;
		this.provisionRouteQueue = provisionRouteQueue;

		Thread thread = new Thread(this);
		thread.start();
	}

	protected void consume() throws Exception {
		try {
			// Wait for the next task to process
			ProvisionRouteTask provisionRouteTask = provisionRouteQueue.take();

			// A task was received
			if (deploymentResultQueue != null) {
				// populate the cache with the new rates, ...
				// persist the jar
				String newBundlePath = "c:\\tmp\\" + Long.toString(System.nanoTime()) + ".jar";
				BundleTools.byteArray2File(provisionRouteTask.getBundle(), new File(newBundlePath));

				// deploy the bundle

				// And post the result of the provisioning
				ProvisionRouteResult provisionRouteResult = new ProvisionRouteResult(provisionRouteTask, TaskResult.OK);
				provisionRouteResult.setBundlePath(newBundlePath);
				deploymentResultQueue.add(provisionRouteResult);			
			}
		} catch (InterruptedException e) {

		}
		// Wait for the next task to process
		consume();
	}

	@Override
	public void run() {
		try {
			consume();
		} catch (Exception e) {

		}
	}

}
