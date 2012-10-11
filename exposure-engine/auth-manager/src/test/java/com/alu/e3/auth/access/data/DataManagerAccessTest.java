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
package com.alu.e3.auth.access.data;

import org.junit.Test;

import com.alu.e3.common.camel.AuthIdentity;
import com.alu.e3.common.camel.AuthReport;
import com.alu.e3.common.tools.CanonicalizedIpAddress;
import com.alu.e3.data.model.Api;
import com.alu.e3.data.model.Auth;
import com.alu.e3.data.model.CallDescriptor;
import com.alu.e3.data.model.Policy;
import com.alu.e3.data.model.enumeration.StatusType;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DataManagerAccessTest {
	
	@Test 
	public void testDataManagerAccessApi() {
		 
		DataManagerAccess dataAccess = new DataManagerAccess();
		MockDataManager mockDataManager = new MockDataManager();
		dataAccess.setDataManager(mockDataManager);
		
		Api api = new Api();
		api.setId("api");
		api.setStatus(StatusType.ACTIVE);
		
		// no API found
		assertNull(dataAccess.checkAllowed(api).getAuthIdentity());
		
		mockDataManager.setApi(api);
		
		// No policy found for the API
		// assertNull(dataAccess.isAllowed("api"));
		
		Policy policy = new Policy();
		mockDataManager.getCallDescriptors().add(new CallDescriptor(policy, 1, 2));
		
		
		AuthReport authReport = dataAccess.checkAllowed(api);
		AuthIdentity authIdentity = authReport.getAuthIdentity();
		assertNotNull(authIdentity);
		
		assertNotNull(authIdentity.getApi() == api); // compare memory reference
		assertNull(authIdentity.getAuth());
		assertNotNull(authIdentity.getCallDescriptors().get(0).getPolicy() == policy); // compare memory reference
		
	}
	
	@Test
	public void testDataManagerAccessApiAuth() {
		
		DataManagerAccess dataAccess = new DataManagerAccess();
		MockDataManager mockDataManager = new MockDataManager();
		dataAccess.setDataManager(mockDataManager);
		
		Api api = new Api();
		api.setId("api");
		api.setStatus(StatusType.ACTIVE);
		mockDataManager.setApi(api);
		
		// no auth found
		assertNull(dataAccess.checkAllowed(api, "authKey").getAuthIdentity());
		assertNull(dataAccess.checkAllowed(api, "username", "password").getAuthIdentity());
		assertNull(dataAccess.checkAllowed(api, new CanonicalizedIpAddress("127.0.0.1")).getAuthIdentity());
		
		Auth auth = new Auth();
		auth.setStatus(StatusType.ACTIVE);
		mockDataManager.setAuth(auth);
		
		// No policy found
		assertNull(dataAccess.checkAllowed(api, "authKey").getAuthIdentity());
		assertNull(dataAccess.checkAllowed(api, "username", "password").getAuthIdentity());
		assertNull(dataAccess.checkAllowed(api, new CanonicalizedIpAddress("127.0.0.1")).getAuthIdentity());
		
		Policy policy = new Policy();
		mockDataManager.getCallDescriptors().add(new CallDescriptor(policy, 1, 2));
		
		AuthIdentity authIdentity = dataAccess.checkAllowed(api, "authKey").getAuthIdentity();
		assertNotNull(authIdentity);		
		assertNotNull(authIdentity.getApi() == api); // compare memory reference
		assertNotNull(authIdentity.getAuth() == auth); // compare memory reference
		assertNotNull(authIdentity.getCallDescriptors().get(0).getPolicy() == policy); // compare memory reference

		authIdentity = dataAccess.checkAllowed(api, "username", "password").getAuthIdentity();
		assertNotNull(authIdentity);		
		assertNotNull(authIdentity.getApi() == api); // compare memory reference
		assertNotNull(authIdentity.getAuth() == auth); // compare memory reference
		assertNotNull(authIdentity.getCallDescriptors().get(0).getPolicy() == policy); // compare memory reference
		
		
		authIdentity = dataAccess.checkAllowed(api, new CanonicalizedIpAddress("127.0.0.1")).getAuthIdentity();
		assertNotNull(authIdentity);	
		assertNotNull(authIdentity.getApi() == api); // compare memory reference
		assertNotNull(authIdentity.getAuth() == auth); // compare memory reference
		assertNotNull(authIdentity.getCallDescriptors().get(0).getPolicy() == policy); // compare memory reference
		
	}
	
}
