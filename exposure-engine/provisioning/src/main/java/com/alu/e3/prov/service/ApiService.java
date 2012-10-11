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
package com.alu.e3.prov.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.alu.e3.common.InvalidIDException;
import com.alu.e3.common.camel.ExchangeConstantKeys;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.data.model.ApiJar;
import com.alu.e3.data.wrapper.BeanConverterUtil;
import com.alu.e3.prov.ApplicationCodeConstants;
import com.alu.e3.prov.LogUtil;
import com.alu.e3.prov.ProvisionException;
import com.alu.e3.prov.deployment.DeploymentException;
import com.alu.e3.prov.deployment.IDeploymentManager;
import com.alu.e3.prov.deployment.RollbackException;
import com.alu.e3.prov.lifecycle.IDHelper;
import com.alu.e3.prov.restapi.ExchangeData;
import com.alu.e3.prov.restapi.model.Api;
import com.alu.e3.prov.restapi.model.HTTPSType;
import com.alu.e3.prov.restapi.model.Key;
import com.alu.e3.prov.restapi.model.TLSMode;
import com.alu.e3.prov.restapi.model.TdrData;

public class ApiService implements IApiService {
	private static final CategoryLogger LOG = CategoryLoggerFactory.getLogger(ApiService.class);

	private IDataManager dataManager;
	private IApiJarBuilder apiBuilder;
	private IDeploymentManager deploymentManager;
	private ICommonApiCheck apiChecker;
	private IFeaturedApiCheck featureChecker;

	public void setCommonApiCheck(ICommonApiCheck apiChecker) {
		this.apiChecker = apiChecker;
	}

	public void setFeaturedApiCheck(IFeaturedApiCheck featureChecker) {
		this.featureChecker = featureChecker;
	}

	/**
	 * 
	 * @param dataManager
	 */
	public void setDeploymentManager(IDeploymentManager deploymentManager) {
		this.deploymentManager = deploymentManager;
	}

	/**
	 * 
	 * @param dataManager
	 */
	public void setApiJarBuilder(IApiJarBuilder apiBuilder) {
		this.apiBuilder = apiBuilder;
	}

	/**
	 * 
	 * @param dataManager
	 */
	public void setDataManager(IDataManager dataManager) {
		this.dataManager = dataManager;
	}

	@Override
	public void create(Api api) throws ProvisionException {

		boolean canCreateJar;

		// Checks before create
		canCreateJar = this.checkCreate(api);

		// Setting default value of HTTPS
		this.setDefaultHttpsValue(api);

		// Set default TdrData
		this.setDefaultTdrValue(api);

		ExchangeData exchange = createExchange(api);

		if (canCreateJar) {
			// Deploy the api
			this.deployApi(api, exchange);
		}

		// Store the model
		com.alu.e3.data.model.Api model = BeanConverterUtil.toDataModel(api);
		model.getApiDetail().setIsApiDeployed(canCreateJar);
		dataManager.addApi(model);

		LogUtil.log(LOG, exchange, LogUtil.Phase.PROCESS, "Create Done");

		return;

	}

	@Override
	public void update(Api api) throws ProvisionException {

		boolean canCreateJar;

		// checks before create
		canCreateJar = this.checkUpdate(api);

		// Setting default value of HTTPS
		this.setDefaultHttpsValue(api);

		// Set default TdrData
		this.setDefaultTdrValue(api);

		ExchangeData exchange = createExchange(api);

		if (canCreateJar) {
			this.deployApi(api, exchange);
		}
		else {
			// remove the previously deployed jar if needed
			com.alu.e3.data.model.Api previousApi = dataManager.getApiById(api.getId(), true);

			if (previousApi.getApiDetail().isApiDeployed()) {
				this.deploymentManager.undeployApi(exchange, api.getId());
			}
		}

		// store the model
		com.alu.e3.data.model.Api model = BeanConverterUtil.toDataModel(api);
		model.getApiDetail().setIsApiDeployed(canCreateJar);
		dataManager.updateApi(model);

		LogUtil.log(LOG, exchange, LogUtil.Phase.PROCESS, "Update Done");

		return;

	}

	@Override
	public void delete(String apiId) throws ProvisionException {
		if (apiId == null) {
			throw new ProvisionException(ApplicationCodeConstants.API_ID_NOT_PROVIDED, "Missing API ID in the delete request");
		}

		com.alu.e3.data.model.Api api = dataManager.getApiById(apiId, true);

		dataManager.removeApi(apiId); 

		ExchangeData exchange = createExchange(apiId);

		if (api.getApiDetail().isApiDeployed()) {
			this.deploymentManager.undeployApi(exchange, apiId);
		}

		LogUtil.log(LOG, exchange, LogUtil.Phase.PROCESS, "Delete Done");

		return;

	}

	@Override
	public Api get(String apiId) throws ProvisionException {

		if (apiId == null) {
			LOG.error("Missing API ID in the get request");
			throw new ProvisionException(ApplicationCodeConstants.API_ID_NOT_PROVIDED, "Missing API ID in the get request");
		}

		com.alu.e3.data.model.Api api = dataManager.getApiById(apiId, true);

		Api apiWs = BeanConverterUtil.fromDataModel(api);

		ExchangeData exchange = createExchange(apiId);
		LogUtil.log(LOG, exchange, LogUtil.Phase.GET, "N/A");

		return apiWs;
	}

	@Override
	public List<String> getAll() throws ProvisionException {
		List<String> apiIdsList = new ArrayList<String>();

		Set<String> apiIds = dataManager.getAllApiIds();
		if (apiIds != null)
			apiIdsList.addAll(apiIds);

		ExchangeData exchange = new ExchangeData();
		LogUtil.log(LOG, exchange, LogUtil.Phase.GETALL, "N/A");

		return apiIdsList;
	}

	private boolean checkCreate(Api api) throws ProvisionException {
		// check api ID does not already exist
		if (api.getId() != null && dataManager.isApiExist(api.getId())) {
			throw new ProvisionException(ApplicationCodeConstants.API_ID_ALREADY_EXIST, "An API with that ID already exist");
		}
		
		//call checkCreateUpdate to perform the common checks
		boolean canCreateJarFile = checkCreateUpdate(api);
		
		//and perform additional check
		if(dataManager.endpointExists(api.getEndpoint())){
			throw new IllegalArgumentException("An API with that endpoint already exists");
		}
		
		return canCreateJarFile;
	}

	private boolean checkUpdate(Api api) {
		// check api ID already exist
		if (api.getId() == null || !dataManager.isApiExist(api.getId())) {
			throw new InvalidIDException("An API with that ID does not exist");
		}
		
		//call checkCreateUpdate to perform the common checks
		boolean canCreateJarFile = checkCreateUpdate(api);

		//and perform additional check
		com.alu.e3.data.model.Api oldApi = dataManager.getApiById(api.getId(), true);
		if(dataManager.endpointExists(api.getEndpoint()) && !oldApi.getApiDetail().getEndpoint().equals(api.getEndpoint())){
			throw new IllegalArgumentException("API endpoint name should be the same as the one in the API to update");
		}

		return canCreateJarFile;
	}
	
	private boolean checkCreateUpdate(Api api){
		//perform common checks between checkCreate and checkUpdate
		featureChecker.assertIsAvailableFeature(api);
		
		// Checking if the API has one default context, throw an exception
		// otherwise.
		apiChecker.assertHasDefaultContext(api);
		
		//checking that given XML grammar stream is ok
		apiChecker.assertValidatioSchemaConsystency(api);

		// Checking PassThrough/Composite/Notification/Subscription consistency
		boolean canCreateJarFile = apiChecker.assertCompositionApiConsistency(api);

		if(api.getEndpoint() == null || api.getEndpoint().isEmpty()){
			throw new IllegalArgumentException("The endpoint is required");
		} else if (!api.getEndpoint().matches("[a-zA-Z0-9-_./]+")){
			throw new IllegalArgumentException("The endpoint is not well-formed : authorized characters are alphanumerics and -_./");
		}

		for(Key key : api.getProperties()){
			if(key.getName() == null || key.getName().isEmpty()) {
				throw new IllegalArgumentException("All properties must have a name");
			}
		}

		if(api.getAuthentication().getUseAuthKey() && 
				(api.getAuthentication().getAuthKey() == null || 
				api.getAuthentication().getAuthKey().getKeyName() == null ||
				api.getAuthentication().getAuthKey().getKeyName().isEmpty())){
			throw new IllegalArgumentException("APIs with AuthKey authentication require a keyName");
		}

		return canCreateJarFile;
	}

	private void setDefaultHttpsValue(Api api) {
		// Setting default value of HTTPS
		HTTPSType httpsType = api.getHttps();
		if (httpsType == null) {
			httpsType = new HTTPSType();
			httpsType.setEnabled(false);
		} else if (httpsType.isEnabled() && httpsType.getTlsMode() == null) {
			httpsType.setTlsMode(TLSMode.ONE_WAY);
		}
		api.setHttps(httpsType);
	}

	private void setDefaultTdrValue(Api api){
		if(api.getTdr() == null){
			api.setTdr(new TdrData());
		}
	}

	protected void deployApi(Api api, ExchangeData exchange) throws RollbackException, ProvisionException {
		ApiJar oldApiJar = null;
		try {
			oldApiJar = dataManager.getApiJar(api.getId());

			byte[] apiNewJarBytes = apiBuilder.build(api, exchange);

			if (apiNewJarBytes != null) {
				deploymentManager.deployApi(exchange, apiNewJarBytes);
			} else {
				LOG.error("No Jar data created for for Api ID: " + api.getId());
			}
		} catch (DeploymentException e) {
			LOG.error("Deployment failed for Api ID: " + api.getId(), e);

			deploymentManager.undeployApi(exchange, api.getId());

			// Roll back old API
			if (oldApiJar != null) {
				try {
					// Deploy N-1 jar
					deploymentManager.deployApi(exchange, oldApiJar.getData());
					// throw this status for error mgmt  
					throw new RollbackException(ApplicationCodeConstants.ROUTE_CREATION_FAILED_ROLLBACK_OK, "Route edition failed, rollback ok.");

				} catch (DeploymentException e1) {					
					throw new RollbackException(ApplicationCodeConstants.ROLLBACK_FAILED, "Second try deployment, give up", e1);
				}
			} else {
				// Nothing to roll-back
				throw new RollbackException(ApplicationCodeConstants.ROUTE_CREATION_FAILED_ROLLBACK_KO, "Route creation failed, and no rollback could be done.");
			}

		}

	}

	//
	// Utility methods for logging
	//

	protected static ExchangeData createExchange(String apiId) {
		ExchangeData exchange = new ExchangeData();
		String provID = IDHelper.generateUID();
		String encodedApiID = IDHelper.encode(apiId);

		exchange.getProperties().put(ExchangeConstantKeys.E3_API_ID.toString(), apiId);
		exchange.getProperties().put(ExchangeConstantKeys.E3_API_ID_ENCODED.toString(), encodedApiID);
		exchange.getProperties().put(ExchangeConstantKeys.E3_PROVISION_ID.toString(), provID);

		return exchange;
	}

	protected static ExchangeData createExchange(Api api) {
		ExchangeData exchange = new ExchangeData();

		String provID = IDHelper.generateUID();

		String encodedApiID = null;
		String apiID = api.getId();
		if (apiID != null) {
			exchange.getProperties().put(ExchangeConstantKeys.E3_API_ID_CREATION_MODE.toString(), ExchangeConstantKeys.E3_API_ID_CREATION_MODE_PROVIDED.toString());
			encodedApiID = IDHelper.encode(apiID);

			if (LOG.isDebugEnabled())
				LOG.debug("Found API ID: {}", apiID);

		} else {
			apiID = IDHelper.generateUID();
			encodedApiID = IDHelper.encode(apiID);

			api.setId(apiID);

			exchange.getProperties().put(ExchangeConstantKeys.E3_API_ID_CREATION_MODE.toString(), ExchangeConstantKeys.E3_API_ID_CREATION_MODE_GENERATED.toString());

			if (LOG.isDebugEnabled())
				LOG.debug("Generating API ID is set to: {}", apiID);

		}

		exchange.getProperties().put(ExchangeConstantKeys.E3_API_ID.toString(), api.getId());
		exchange.getProperties().put(ExchangeConstantKeys.E3_API_ID_ENCODED.toString(), encodedApiID);
		exchange.getProperties().put(ExchangeConstantKeys.E3_REQUEST_PAYLOAD.toString(), api);
		exchange.getProperties().put(ExchangeConstantKeys.E3_PROVISION_ID.toString(), provID);

		return exchange;
	}

	public IDeploymentManager getDeploymentManager() {
		return deploymentManager;
	}

	public IDataManager getDataManager() {
		return dataManager;
	}

}
