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

import org.springframework.beans.factory.annotation.Autowired;

import com.alu.e3.common.camel.ExchangeConstantKeys;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.prov.ApplicationCodeConstants;
import com.alu.e3.prov.LogUtil;
import com.alu.e3.prov.restapi.ExchangeData;

public class DeploymentManager implements IDeploymentManager {
	private static final CategoryLogger logger = CategoryLoggerFactory.getLogger(DeploymentManager.class);
	
	@Autowired
	protected IDataManager dataManager;

	public DeploymentManager() {}

		
	
	/* (non-Javadoc)
	 * @see com.alu.e3.prov.deployment.IDeploymentManager#deployApi(com.alu.e3.prov.restapi.ExchangeData, byte[])
	 */
	@Override
	public void deployApi(ExchangeData exchange, byte[] jarData) throws DeploymentException {

		LogUtil.log(logger, exchange, LogUtil.Phase.DEPLOY, "Deploy Jar Data procedure started");
	
		
		String apiId = (String) exchange.getProperty(ExchangeConstantKeys.E3_API_ID.toString());
		if(apiId == null)
			throw new DeploymentException(ApplicationCodeConstants.DEPLOYMENT_FAILED, "No API ID in exchange");
		
		boolean result = false;	
		
		if(jarData != null) {
			result = dataManager.deployApi(apiId, jarData);
		}
		
		if(!result) {
			if(logger.isErrorEnabled()) {
				logger.error("Deployment failed");
			}
			throw new DeploymentException(ApplicationCodeConstants.DEPLOYMENT_FAILED, "Deployment failed");
		}

		LogUtil.log(logger, exchange, LogUtil.Phase.DEPLOY, "Deploy Jar Data procedure finished");

	}
	
	/* (non-Javadoc)
	 * @see com.alu.e3.prov.deployment.IDeploymentManager#undeployApi(com.alu.e3.prov.restapi.ExchangeData, java.lang.String)
	 */
	@Override
	public void undeployApi(ExchangeData exchange, String apiId) throws DeploymentException {
		LogUtil.log(logger, exchange, LogUtil.Phase.UNDEPLOY, "Undeploy Jar Data procedure started");

		if (logger.isDebugEnabled())
			logger.debug("Undeploy start for API with Id: " + apiId);
		
		boolean result = dataManager.undeployApi(apiId);
		
		if(!result) {
			if(logger.isErrorEnabled()) {
				logger.error("UnDeployment failed");
			}
			throw new DeploymentException(ApplicationCodeConstants.DEPLOYMENT_FAILED, "Undeployment failed");
		}

		LogUtil.log(logger, exchange, LogUtil.Phase.UNDEPLOY, "Undeploy Jar Data procedure finished");
	}

}
