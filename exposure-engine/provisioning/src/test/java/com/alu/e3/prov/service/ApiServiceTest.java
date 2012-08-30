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

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alu.e3.common.InvalidIDException;
import com.alu.e3.data.DataManager;
import com.alu.e3.prov.ApplicationCodeConstants;
import com.alu.e3.prov.ProvisionException;
import com.alu.e3.prov.deployment.MockDeploymentManager;
import com.alu.e3.prov.deployment.RollbackException;
import com.alu.e3.prov.restapi.ExchangeData;
import com.alu.e3.prov.restapi.model.Api;
import com.alu.e3.prov.restapi.model.ApiContext;
import com.alu.e3.prov.restapi.model.ApiType;
import com.alu.e3.prov.restapi.model.AuthType;
import com.alu.e3.prov.restapi.model.Authentication;
import com.alu.e3.prov.restapi.model.Authkey;
import com.alu.e3.prov.restapi.model.Data;
import com.alu.e3.prov.restapi.model.DynamicTdr;
import com.alu.e3.prov.restapi.model.HTTPSType;
import com.alu.e3.prov.restapi.model.Key;
import com.alu.e3.prov.restapi.model.ProvisionAuthentication;
import com.alu.e3.prov.restapi.model.StaticTdr;
import com.alu.e3.prov.restapi.model.Status;
import com.alu.e3.prov.restapi.model.TargetHost;
import com.alu.e3.prov.restapi.model.TdrData;
import com.alu.e3.prov.restapi.model.TdrEnabled;
import com.alu.e3.prov.restapi.model.TdrType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:/spring/provisioning.osgi-context-test.xml", 
		"classpath:/spring/provisioning.provision-beans-test.xml"
		})
public class ApiServiceTest {

	private ApiService apiService;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void testApiService() throws Exception {
		Api api = newApi();
		try {
			apiService.create(api);
			apiService.update(api);
			Api getApi  = apiService.get(api.getId());
			assertNotNull(getApi);

			List<String> ids = apiService.getAll();
			assertNotNull(ids);
			assertFalse(ids.isEmpty());

			apiService.delete(api.getId());

			boolean isInvalidID = false;

			try {
				apiService.get(api.getId());
			} catch (InvalidIDException e) {
				isInvalidID = true;
			}

			assertTrue(isInvalidID);
			
		} catch (ProvisionException e) {
			fail("Update Api failure");
			e.printStackTrace();

		}
	}





	@Test
	public void testDeploy() throws Exception {
		Api api = newApi();
		try {
			apiService.setDeploymentManager(new MockDeploymentManager.DeployOk());

			apiService.deployApi(api, ApiService.createExchange(api));

		} catch (ProvisionException e) {
			fail("Create Api failure");
			e.printStackTrace();

		}
	}

	@Test
	public void testRollback_DeployKo() throws Exception {
		Api api = newApi();
		try {
			// setup old jar in dataManager
			byte[] oldJar = new byte[] { 0, 1 };			
			((DataManager) apiService.getDataManager()).deployApi(api.getId(), oldJar);

			// mock deploymentManager scenario
			apiService.setDeploymentManager(new MockDeploymentManager.DeployKO());

			ExchangeData exchange = ApiService.createExchange(api);
			apiService.deployApi(api, exchange);

		} catch (RollbackException e) {
			assertEquals(ApplicationCodeConstants.ROLLBACK_FAILED, e.getErrorCode());

		} catch (ProvisionException e) {
			fail("Rollback Api failure");
			e.printStackTrace();

		}
	}


	@Test
	public void testRollback_UndeployKo() throws Exception {
		Api api = newApi();
		try {
			// setup
			byte[] oldJar = new byte[] { 0, 1 };			
			((DataManager) apiService.getDataManager()).deployApi(api.getId(), oldJar);

			apiService.setDeploymentManager(new MockDeploymentManager.FirstDeployKoSecondDeployOkUndeployKo());

			ExchangeData exchange = ApiService.createExchange(api);
			apiService.deployApi(api, exchange);

		} catch (ProvisionException e) {
			assertEquals(1, e.getErrorCode());

		} catch (Exception e) {
			fail("Rollback Api failure");
			e.printStackTrace();
		}
	}

	@Test
	public void testRollback_FirstDeployOkSecondDeployKoUndeployOk() throws Exception {
		Api api = newApi();
		try {
			// setup
			byte[] oldJar = new byte[] { 0, 1 };			
			((DataManager) apiService.getDataManager()).deployApi(api.getId(), oldJar);

			apiService.setDeploymentManager(new MockDeploymentManager.FirstDeployOkSecondDeployKoUndeployOk());

			ExchangeData exchange = ApiService.createExchange(api);
			apiService.deployApi(api, exchange);

		} catch (ProvisionException e) {
			assertEquals(1, e.getErrorCode());

		} catch (Exception e) {
			fail("Rollback Api failure");
			e.printStackTrace();
		}
	}



	@Test
	public void testRollback_FirstDeployKoUndeployOk_NoOldJar() throws Exception {
		Api api = newApi();
		try {

			apiService.setDeploymentManager(new MockDeploymentManager.DeployKoUndeployKo());

			ExchangeData exchange = ApiService.createExchange(api);
			apiService.deployApi(api, exchange);

		} catch (ProvisionException e) {
			assertEquals(1, e.getErrorCode());

		} catch (Exception e) {
			fail("Rollback Api failure");
			e.printStackTrace();
		}
	}

	@Autowired
	public void setApiService(ApiService apiService) {
		this.apiService = apiService;
	}

	private Api newApi() {

		Api api = new Api();

		ApiContext env = new ApiContext();
		env.setDefaultContext(true);
		env.setId("test");
		api.getContexts().add(env);

		env.setStatus(Status.ACTIVE);
		/*
		 * env.setMaxRateLimitTPMThreshold(1); env.setMaxRateLimitTPMWarning(1);
		 * env.setMaxRateLimitTPSThreshold(1); env.setMaxRateLimitTPSWarning(1);
		 */

		api.setId("getLocation" + (new Random().nextLong()));
		api.setDisplayName("test");
		api.setType(ApiType.PASS_THROUGH);
		api.setVersion("1.0");
		api.setEndpoint("AEndpointURL");

		api.setStatus(Status.ACTIVE);

		ProvisionAuthentication pauth = new ProvisionAuthentication();
		Authkey authKey = new Authkey();
		authKey.setKeyName("key");
		pauth.setAuthKey(authKey);

		api.setAuthentication(pauth);

		pauth.getAuths().add(AuthType.AUTHKEY);
		pauth.getAuths().add(AuthType.BASIC);
		pauth.getAuths().add(AuthType.IP_WHITE_LIST);

		TargetHost th = new TargetHost();
		th.setUrl("http://www.yahoo.com");

		TargetHost th2 = new TargetHost();
		th2.setUrl("http://www.google.com");

		Authentication auth = new Authentication();
		auth.setType("NoAuth");
		Data d = new Data();
		Key k = new Key();
		k.setName("aKey00");
		k.setValue("key000Val");
		d.setKey(Arrays.asList(k));
		auth.setData(d);
		th.setAuthentication(auth);
		th2.setAuthentication(auth);

		env.setTargetHosts(Arrays.asList(th, th2));
		api.setTdrEnabled(new TdrEnabled());
		api.getTdrEnabled().setEnabled("true");

		HTTPSType httpsType = new HTTPSType();
		httpsType.setEnabled(true);
		httpsType.setTlsMode(com.alu.e3.prov.restapi.model.TLSMode.ONE_WAY);
		api.setHttps(httpsType);

		TdrData tdrData = new TdrData();

		TdrType tdrType = new TdrType();
		tdrType.getType().add("apiRateLimit");

		DynamicTdr dt = new DynamicTdr();
		dt.setHttpHeaderName("HTTP_HEADER");
		dt.setTdrPropName("propname");
		dt.setTypes(tdrType);

		tdrData.getDynamic().add(dt);

		StaticTdr st = new StaticTdr();
		st.setValue("staticValue");
		st.setTdrPropName("staticName");

		st.setTypes(tdrType);

		tdrData.getStatic().add(st);
		api.setTdr(tdrData);

		return api;
	}
}
