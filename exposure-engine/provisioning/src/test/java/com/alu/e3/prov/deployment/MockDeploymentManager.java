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
package com.alu.e3.prov.deployment;

import com.alu.e3.prov.restapi.ExchangeData;

public class MockDeploymentManager implements IDeploymentManager {

	public MockDeploymentManager() {
	}

	@Override
	public void deployApi(ExchangeData exchange, byte[] jarData) throws DeploymentException {

	}

	@Override
	public void undeployApi(ExchangeData exchange, String apiId) throws DeploymentException {

	}

	public static class FirstDeployOkSecondDeployKoUndeployOk extends MockDeploymentManager {
		boolean firstTime = true;

		@Override
		public void deployApi(ExchangeData exchange, byte[] jarData) throws DeploymentException {
			if (firstTime) {
				firstTime = false;
				// ok
			} else {
				throw new DeploymentException(1, "Deployment failure testing");
			}

		}

	}

	public static class FirstDeployKoSecondDeployOkUndeployKo extends MockDeploymentManager {
		boolean firstTime = true;

		@Override
		public void deployApi(ExchangeData exchange, byte[] jarData) throws DeploymentException {
			if (firstTime) {
				firstTime = false;
				throw new DeploymentException(1, "Deployment failure testing");
			} else {
				// Ok
			}

		}

		@Override
		public void undeployApi(ExchangeData exchange, String apiId) throws DeploymentException {
			throw new DeploymentException(1, "UnDeployment failure testing");
		}
	}
	
	public static class DeployOk extends MockDeploymentManager {
		
	}
	
	public static class DeployKO extends MockDeploymentManager {
		@Override
		public void deployApi(ExchangeData exchange, byte[] jarData) throws DeploymentException {
			throw new DeploymentException(1, "Deployment failure testing");
		}

	}

	public static class DeployOkUndeployOk extends MockDeploymentManager {

	}
	
	public static class DeployKoUndeployKo extends MockDeploymentManager {
		@Override
		public void deployApi(ExchangeData exchange, byte[] jarData) throws DeploymentException {
			throw new DeploymentException(1, "Deployment failure testing");
		}

		@Override
		public void undeployApi(ExchangeData exchange, String apiId) throws DeploymentException {
			throw new DeploymentException(1, "UnDeployment failure testing");
		}
	}
}
