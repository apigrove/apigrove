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


import static org.junit.Assert.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.alu.e3.data.model.Api;
import com.alu.e3.data.model.sub.APIContext;
import com.alu.e3.data.model.sub.ForwardProxy;
import com.alu.e3.data.model.sub.IForwardProxy;
import com.alu.e3.gateway.loadbalancer.TargetHostManager.TargetStatus;
import com.alu.e3.gateway.targethealthcheck.ITargetHealthCheckService;

public class TargetHostManagerTest extends TargetHostManagerBase {

	@Test
	public void simpleNoHealthCheck() {
		Api api = newApi(null);
		
		dataManager.addApi(api);
		targetHostManager.registerAPI(api.getId());
		
		for(APIContext context : api.getApiDetail().getContexts()) {
			List<TargetReference> references = targetHostManager.getTargetReferences(api.getId(), context.getId());
				
			// We have same as many targetreference as targethost in the context
			assertTrue("Number of target references == than number of target hosts", references.size() == context.getTargetHosts().size());

			for(TargetReference target : references) {
				// Assert that all targets are Available (their default status)
				assertTrue(targetHostManager.isAvailable(target.getReference()));
				
				// No proxy associated to the API
				assertNull(target.getTargetHost().getForwardProxy());
				
				// Assert that the TargetHost referenced by the TargetReference obejct returned by TargetHostManager is really part of API Context's TargetHost
				assertTrue("TargetHost in TargetReference is part of API Context's TargetHosts", context.getTargetHosts().contains(target.getTargetHost()));
			}
			
			// Testing that notifying a failed target to targetHostManager does not change its status as this target is not healthchecked
			targetHostManager.notifyFailed(references.get(0).getReference());
			assertTrue(targetHostManager.isAvailable(references.get(0).getReference()));
		}
	}
	
	@Test
	public void noProxySet() {
		Api api = newApi(null);
		
		dataManager.addApi(api);
		targetHostManager.registerAPI(api.getId());
		
		for(APIContext context : api.getApiDetail().getContexts()) {
			List<TargetReference> references = targetHostManager.getTargetReferences(api.getId(), context.getId());
				
			for(TargetReference target : references) {
				
				// No proxy associated to the API
				assertNull(target.getTargetHost().getForwardProxy());
			}
			
		}
	}
	
	@Test
	public void localProxySet() {
		Api api = newApi(null);
		
		ForwardProxy localProxy = new ForwardProxy();
		localProxy.setProxyHost("host1");
		localProxy.setProxyPass("pass1");
		localProxy.setProxyPort("port1");
		localProxy.setProxyUser("user1");
		api.setForwardProxy(localProxy);
		
		dataManager.addApi(api);
		targetHostManager.registerAPI(api.getId());
		
		for(APIContext context : api.getApiDetail().getContexts()) {
			List<TargetReference> references = targetHostManager.getTargetReferences(api.getId(), context.getId());
			
			IForwardProxy refProxy;
			for(TargetReference target : references) {
				
				refProxy = target.getTargetHost().getForwardProxy();
				assertEquals("host1", refProxy.getProxyHost());
				assertEquals("pass1", refProxy.getProxyPass());
				assertEquals("port1", refProxy.getProxyPort());
				assertEquals("user1", refProxy.getProxyUser());
			}
			
		}
	}
	
	@Test
	public void globalProxyPreSetThenUpdated() {
		dataManager.addGlobalProxyListener3(targetHostManager.getGlobalForwardProxy());
		
		dataManager.addProxy("host2#port2#user2#pass2");

		Api api = newApi(null);
		api.setUseGlobalProxy(true);
		
		dataManager.addApi(api);
		targetHostManager.registerAPI(api.getId());
		
		for(APIContext context : api.getApiDetail().getContexts()) {
			List<TargetReference> references = targetHostManager.getTargetReferences(api.getId(), context.getId());
			
			IForwardProxy refProxy;
			for(TargetReference target : references) {
				
				refProxy = target.getTargetHost().getForwardProxy();
				assertEquals("host2", refProxy.getProxyHost());
				assertEquals("pass2", refProxy.getProxyPass());
				assertEquals("port2", refProxy.getProxyPort());
				assertEquals("user2", refProxy.getProxyUser());
			}
		}
		
		dataManager.addProxy("host3#port3#user3#pass3");

		for(APIContext context : api.getApiDetail().getContexts()) {
			List<TargetReference> references = targetHostManager.getTargetReferences(api.getId(), context.getId());
			
			IForwardProxy refProxy;
			for(TargetReference target : references) {
				
				refProxy = target.getTargetHost().getForwardProxy();
				assertEquals("host3", refProxy.getProxyHost());
				assertEquals("pass3", refProxy.getProxyPass());
				assertEquals("port3", refProxy.getProxyPort());
				assertEquals("user3", refProxy.getProxyUser());
			}
		}
		
	}
	
	@Test
	/**
	 * Will test that an API with a HealthCheckService is correctly added to the Service.
	 */
	public void simpleHealthCheck() {
		
		// Mimic Spring instantiation of HealthCheckService
		HealthCheckServiceMock healthCheckService = new HealthCheckServiceMock();
		healthCheckService.init();
		
		// imic Spring instantiation of THManager
		targetHostManager.registerHealthCheckService(healthCheckService);
		
		Api api = newApi(healthCheckService);
		
		// Provision the API
		dataManager.addApi(api);
		
		// Register the APi on the THMgr: all targets should be monitored by the healthcheck service too
		targetHostManager.registerAPI(api.getId());
		
		for(APIContext context : api.getApiDetail().getContexts()) {
			// These are targetReferences returned by the THMgr to the LoadBalancer
			List<TargetReference> references = targetHostManager.getTargetReferences(api.getId(), context.getId());
				
			// We have same as many targetreference as targethost in the context
			assertTrue("Number of target references == than number of target hosts", references.size() == context.getTargetHosts().size());

			for(TargetReference target : references) {
				// Assert that all targets are Available (their default status)
				assertTrue(targetHostManager.isAvailable(target.getReference()));
				
				// Assert that the TargetHost referenced by the TargetReference obejct returned by TargetHostManager is really part of API Context's TargetHost
				assertTrue("TargetHost in TargetReference is part of API Context's TargetHosts", context.getTargetHosts().contains(target.getTargetHost()));
				
				// Assert that the target has been added to the HealthCheckServiceMock
				assertTrue("TargetReference is monitored by HealthCheckService", healthCheckService.isMonitored(target.getReference()));
			}
			
			// Testing that notifying a failed target to targetHostManager CHANGES its status as this target IS healthchecked
			targetHostManager.notifyFailed(references.get(0).getReference());
			assertFalse(targetHostManager.isAvailable(references.get(0).getReference()));
			
			// Reset target0 
			healthCheckService.setStatus(references.get(0).getReference(), TargetStatus.AVAILABLE);
			assertTrue(targetHostManager.isAvailable(references.get(0).getReference()));
			
			// Check that THMgr returns false for isAvailable(UNAVAILABLE)
			healthCheckService.setStatus(references.get(1).getReference(), TargetStatus.UNAVAILABLE);
			assertFalse(targetHostManager.isAvailable(references.get(1).getReference()));
			
			// Check that THMgr returns false for isAvailable(OVERLOADED)
			healthCheckService.setStatus(references.get(2).getReference(), TargetStatus.OVERLOADED);
			assertFalse(targetHostManager.isAvailable(references.get(2).getReference()));
		}
		
	}
	
	class HealthCheckServiceMock implements ITargetHealthCheckService {

		public static final String NAME = "HealthCheckMock";
		protected Map<String, ManagedTargetHost> targets;
		
		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public void init() {
			targets = new HashMap<String, ManagedTargetHost>();
		}

		@Override
		public void start() {
			// Do nothing here
		}

		@Override
		public void stop() {
			// Do nothing here
		}

		@Override
		public void destroy() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void registerTarget(ManagedTargetHost target) {
			targets.put(target.getReference(), target);
		}

		@Override
		public void unregisterTarget(ManagedTargetHost target) {
			targets.remove(target.getReference());
		}
		
		
		public void setStatus(String reference, TargetStatus status) {
			ManagedTargetHost target = targets.get(reference);
			target.setStatus(status);
		}
		
		public boolean isMonitored(String reference) {
			return targets.containsKey(reference);
		}
	}
}
