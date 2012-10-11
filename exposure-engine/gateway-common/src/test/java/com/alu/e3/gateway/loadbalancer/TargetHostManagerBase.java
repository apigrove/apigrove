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
/**
 * 
 */
package com.alu.e3.gateway.loadbalancer;


import org.junit.After;
import org.junit.Before;

import com.alu.e3.data.model.Api;
import com.alu.e3.data.model.ApiDetail;
import com.alu.e3.data.model.sub.APIContext;
import com.alu.e3.data.model.sub.TargetHealthCheck;
import com.alu.e3.data.model.sub.TargetHost;
import com.alu.e3.gateway.targethealthcheck.ITargetHealthCheckService;

public class TargetHostManagerBase {
	
	protected DataManagerMock dataManager;
	protected TargetHostManager targetHostManager;

	public TargetHostManagerBase() {
	}
	
	@Before
	public void setUp() throws Exception {
		dataManager = new DataManagerMock();

		targetHostManager = new TargetHostManager();
		targetHostManager.setDataManager(dataManager);
		targetHostManager.init();

	}

	@After
	public void tearDown() throws Exception {
		targetHostManager.destroy();
	}
	

	public Api newApi(ITargetHealthCheckService healthCheckService) {		
		TargetHealthCheck targetHealthCheck = null;
		if(healthCheckService != null) {
			targetHealthCheck = new TargetHealthCheck();
			targetHealthCheck.setType(healthCheckService.getName());
		}
		
		APIContext context1 = new APIContext();
		context1.setId("test");
		context1.setDefaultContext(true);
		context1.getTargetHosts().add(targetHostWithUrlAndSite("http://www.google.fr", "LOCAL"));
		context1.getTargetHosts().add(targetHostWithUrlAndSite("http://www.google.com", "US"));
		context1.getTargetHosts().add(targetHostWithUrlAndSite("http://www.google.co.uk", "UK"));
		
		if(targetHealthCheck != null)
			context1.getLoadBalancing().setTargetHealthCheck(targetHealthCheck);
		
		
		
		APIContext context2 = new APIContext();
		context2.setId("production");
		context2.setDefaultContext(false);
		context2.getTargetHosts().add(targetHostWithUrlAndSite("http://www.apple.fr", "LOCAL"));
		context2.getTargetHosts().add(targetHostWithUrlAndSite("http://www.apple.com", "US"));
		context2.getTargetHosts().add(targetHostWithUrlAndSite("http://www.apple.co.uk", "UK"));
		
		if(targetHealthCheck != null)
			context2.getLoadBalancing().setTargetHealthCheck(targetHealthCheck);
		
		Api api = new Api();
		api.setApiDetail(new ApiDetail());
		api.getApiDetail().setEndpoint("/endpoint");
		api.getApiDetail().getContexts().add(context1);
		api.getApiDetail().getContexts().add(context2);
		return api;
	}
	
	protected TargetHost targetHostWithUrlAndSite(String url, String site) {
		TargetHost target = new TargetHost();
		target.setUrl(url);
		target.setSite(site);
		
		return target;
	}

}
