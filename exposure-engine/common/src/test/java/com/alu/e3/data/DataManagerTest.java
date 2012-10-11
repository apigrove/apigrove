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
package com.alu.e3.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alu.e3.common.InvalidIDException;
import com.alu.e3.common.caching.HazelcastCacheManager;
import com.alu.e3.data.model.Api;
import com.alu.e3.data.model.ApiDetail;
import com.alu.e3.data.model.Auth;
import com.alu.e3.data.model.AuthDetail;
import com.alu.e3.data.model.CallDescriptor;
import com.alu.e3.data.model.Policy;
import com.alu.e3.data.model.enumeration.NBAuthType;
import com.alu.e3.data.model.enumeration.StatusType;
import com.alu.e3.data.model.sub.QuotaRLBucket;

public class DataManagerTest {

	private DataManager dataManager;
	private HazelcastCacheManager cacheManager;

	public DataManagerTest() {
	}

	@Before
	public void setUp() throws Exception {
		dataManager = new DataManager();
		TopologyClient topologyClient = new TopologyClient();
		dataManager.setTopologyClient(topologyClient);
		cacheManager = new HazelcastCacheManager();
		cacheManager.setTopologyClient(topologyClient);
		cacheManager.init(true);
		dataManager.setCacheManager(cacheManager);
		dataManager.init();
	}

	@After
	public void tearDown() throws Exception {
		cacheManager.destroy();
		dataManager.destroy();
	}

	@Test
	public void testDataManager() {
		assertNotNull("Check that DataManager is not null", dataManager);
	}

	@Test
	public void testAddGetRemoveApi() {

		// Add a new API
		Api api = new Api();
		api.setId("id1");
		api.setApiDetail(new ApiDetail());
		api.getApiDetail().setEndpoint("http://www.google.com");
		dataManager.addApi(api);

		// Now, get the "simple" API and check that it's OK
		Api api2 = dataManager.getApiById("id1");
		assertNotNull("API was added/returned", api2);
		assertNull("API details is null", api2.getApiDetail());

		// Now, get the "full" API and check that it's OK
		Api api3 = dataManager.getApiById("id1", true);
		assertNotNull("API was added/returned", api3);
		assertEquals("API values are correct", api.getApiDetail().getEndpoint(), api3.getApiDetail().getEndpoint());

		dataManager.removeApi("id1");

		boolean isInvalidID = false;

		try {
			dataManager.getApiById("id1");
		}
		catch (InvalidIDException e) {
			isInvalidID = true;
		}

		assertTrue("API was found and removed", isInvalidID);
	}

	@Test
	public void testAddTwiceApi() {

		// Add a new API
		Api api = new Api();
		api.setId("id1");
		api.setApiDetail(new ApiDetail());
		api.getApiDetail().setEndpoint("http://www.google.com");
		dataManager.addApi(api);

		boolean exceptionRaised = false;
		try
		{
			dataManager.addApi(api);
		}
		catch(Exception e)
		{
			exceptionRaised = true;
		}

		assertTrue("Exception raised", exceptionRaised);

		// cleanup
		dataManager.removeApi(api.getId());
	}

	@Test
	public void testGetAllApi() {

		// Add a new API
		Api api = new Api();
		api.setId("id10");
		api.setApiDetail(new ApiDetail());
		api.getApiDetail().setEndpoint("http://www.google.com");
		dataManager.addApi(api);

		// Add a new API
		Api api2 = new Api();
		api2.setId("id2");
		api2.setApiDetail(new ApiDetail());
		api2.getApiDetail().setEndpoint("http://www.yahoo.com");
		dataManager.addApi(api2);

		Set<String> apis = dataManager.getAllApiIds();
		assertNotNull("No apis found", apis);

		List<String> apisList = new ArrayList<String>();

		for (String apiStr : apis) {
			apisList.add(apiStr);
		}

		assertEquals("API count is correct", 2, apisList.size());
		assertTrue("Both APIs are found", (api.getId().equals(apisList.get(0)) || api2.getId().equals(apisList.get(0))) && (api.getId().equals(apisList.get(1))  || api2.getId().equals(apisList.get(1))));

		// cleanup
		dataManager.removeApi(api.getId());
		dataManager.removeApi(api2.getId());
	}

	@Test
	public void testAddGetRemoveAuth() {

		// Add a new Auth
		Auth auth = new Auth();
		auth.setId("id2");
		auth.setAuthDetail(new AuthDetail());
		auth.getAuthDetail().setUsername("username");
		auth.getAuthDetail().setType(NBAuthType.AUTHKEY);
		auth.getAuthDetail().setAuthKeyValue("authKey");
		dataManager.addAuth(auth);

		// Now, get the "simple" Auth and check that it's OK
		Auth auth2 = dataManager.getAuthById("id2");
		assertNotNull("Auth was added/returned", auth2);
		assertNull("Auth  details is null", auth2.getAuthDetail());

		// Now, get the "full" Auth and check that it's OK
		Auth auth3 = dataManager.getAuthById("id2", true);
		assertNotNull("Auth was added/returned", auth3);
		assertEquals("Auth values are correct", auth.getAuthDetail().getUsername(), auth3.getAuthDetail().getUsername());

		dataManager.removeAuth("id2");

		boolean isInvalidID = false;

		try {
			dataManager.getAuthById("id2");
		}
		catch (InvalidIDException e) {
			isInvalidID = true;
		}

		assertTrue("Auth was found and removed", isInvalidID);
	}

	@Test
	public void testAddAuthThenUpdate() {

		// Add a new Auth
		Auth auth = new Auth();
		auth.setId("id2");
		auth.setAuthDetail(new AuthDetail());
		auth.getAuthDetail().setUsername("username");
		auth.getAuthDetail().setType(NBAuthType.AUTHKEY);
		auth.getAuthDetail().setAuthKeyValue("authKey");
		dataManager.addAuth(auth);

		// Now, get the "simple" Auth and check that it's OK
		Auth auth2 = dataManager.getAuthById("id2", true);
		assertNotNull("Auth was added/returned", auth2);

		auth2.getAuthDetail().setAuthKeyValue("authKey2");
		dataManager.updateAuth(auth2);

		// Now, get the "full" Auth and check that it's OK
		Auth auth3 = dataManager.getAuthById("id2", true);
		assertNotNull("Auth was added/returned", auth3);
		assertEquals("Auth values are correct", auth2.getAuthDetail().getAuthKeyValue(), auth3.getAuthDetail().getAuthKeyValue());

		dataManager.removeAuth("id2");

		boolean isInvalidID = false;

		try {
			dataManager.getAuthById("id2");
		}
		catch (InvalidIDException e) {
			isInvalidID = true;
		}

		assertTrue("Auth was found and removed", isInvalidID);
	}

	@Test
	public void testAddTwiceAuthSameId() {

		// Add a new Auth
		Auth auth = new Auth();
		auth.setId("id2");
		auth.setAuthDetail(new AuthDetail());
		auth.getAuthDetail().setType(NBAuthType.AUTHKEY);
		auth.getAuthDetail().setAuthKeyValue("authKey");
		dataManager.addAuth(auth);

		// Add a new Auth
		Auth auth2 = new Auth();
		auth2.setId("id2");
		auth2.setAuthDetail(new AuthDetail());
		auth2.getAuthDetail().setType(NBAuthType.AUTHKEY);
		auth2.getAuthDetail().setAuthKeyValue("authKey2");

		boolean exceptionRaised = false;
		try
		{
			dataManager.addAuth(auth2);
		}
		catch(Exception e)
		{
			exceptionRaised = true;
		}

		assertTrue("Exception raised", exceptionRaised);

		// cleanup
		dataManager.removeAuth(auth.getId());
	}

	@Test
	public void testAddTwiceAuthSameToken() {

		// Add a new Auth
		Auth auth = new Auth();
		auth.setId("id2");
		auth.setAuthDetail(new AuthDetail());
		auth.getAuthDetail().setUsername("username");
		auth.getAuthDetail().setType(NBAuthType.AUTHKEY);
		auth.getAuthDetail().setAuthKeyValue("authKey");
		dataManager.addAuth(auth);

		Auth auth2 = new Auth();
		auth2.setId("id3");
		auth2.setAuthDetail(new AuthDetail());
		auth2.getAuthDetail().setUsername("username");
		auth2.getAuthDetail().setType(NBAuthType.AUTHKEY);
		auth2.getAuthDetail().setAuthKeyValue("authKey");

		boolean wasException = false;
		try
		{
			dataManager.addAuth(auth2);
		}
		catch (Exception e)
		{
			wasException = true;
		}

		assertTrue("Second auth with same API key was refused", wasException);

		// cleanup for next test
		dataManager.removeAuth(auth.getId());
	}

	@Test
	public void testAddTwiceAuthSameIP() {

		// Add a new Auth
		Auth auth = new Auth();
		auth.setId("id2");
		auth.setAuthDetail(new AuthDetail());
		auth.getAuthDetail().setType(NBAuthType.IP_WHITE_LIST);
		auth.getAuthDetail().getWhiteListedIps().add("12.34.56.78");
		dataManager.addAuth(auth);

		Auth auth2 = new Auth();
		auth2.setId("id3");
		auth2.setAuthDetail(new AuthDetail());
		auth2.getAuthDetail().setType(NBAuthType.IP_WHITE_LIST);
		auth2.getAuthDetail().getWhiteListedIps().add("12.34.56.78");

		boolean wasException = false;
		try
		{
			dataManager.addAuth(auth2);
		}
		catch (Exception e)
		{
			wasException = true;
		}

		assertTrue("Second auth with same API key was refused", wasException);

		// cleanup for next test
		dataManager.removeAuth(auth.getId());



		// Add a new Auth
		auth = new Auth();
		auth.setId("id2");
		auth.setAuthDetail(new AuthDetail());
		auth.getAuthDetail().setType(NBAuthType.IP_WHITE_LIST);
		auth.getAuthDetail().getWhiteListedIps().add("12.34.56.78");
		dataManager.addAuth(auth);

		auth2 = new Auth();
		auth2.setId("id3");
		auth2.setAuthDetail(new AuthDetail());
		auth2.getAuthDetail().setType(NBAuthType.IP_WHITE_LIST);
		auth2.getAuthDetail().getWhiteListedIps().add("112.34.56.78");
		dataManager.addAuth(auth2);

		auth2.getAuthDetail().getWhiteListedIps().clear();
		auth2.getAuthDetail().getWhiteListedIps().add("12.34.56.78");

		wasException = false;
		try
		{
			dataManager.updateAuth(auth2);
		}
		catch (Exception e)
		{
			wasException = true;
		}

		assertTrue("Second auth with same API key was refused", wasException);

		// cleanup for next test
		dataManager.removeAuth(auth.getId());
		dataManager.removeAuth(auth2.getId());

	}

	@Test
	public void testGetAllAuth() {

		// Add a new Auth
		Auth auth = new Auth();
		auth.setId("id1");
		auth.setAuthDetail(new AuthDetail());
		auth.getAuthDetail().setUsername("username1");
		auth.getAuthDetail().setType(NBAuthType.AUTHKEY);
		auth.getAuthDetail().setAuthKeyValue("authKey1");
		dataManager.addAuth(auth);

		Auth auth2 = new Auth();
		auth2.setId("id2");
		auth2.setAuthDetail(new AuthDetail());
		auth2.getAuthDetail().setUsername("username");
		auth2.getAuthDetail().setType(NBAuthType.AUTHKEY);
		auth2.getAuthDetail().setAuthKeyValue("authKey2");
		dataManager.addAuth(auth2);

		Set<String> auths = dataManager.getAllAuthIds();
		assertNotNull("No auth found", auths);

		List<String> authList = new ArrayList<String>();

		for (String authStr : auths) {
			authList.add(authStr);
		}

		assertEquals("Auth count is correct", 2, authList.size());
		assertTrue("Both Auths are found", (authList.get(0).equals(auth.getId()) || authList.get(0).equals(auth2.getId())) && (authList.get(1).equals(auth.getId()) || authList.get(1).equals(auth2.getId())));

		// cleanup for next test
		dataManager.removeAuth(auth.getId());
		dataManager.removeAuth(auth2.getId());
	}

	@Test
	public void testAddGetRemovePolicy() {

		// Add a new Policy
		Policy policy = new Policy();
		policy.setId("id3");
		dataManager.addPolicy(policy);

		// Now, get the API and check that it's OK
		Policy policy2 = dataManager.getPolicyById("id3");
		assertNotNull("Policy was added/returned", policy2);

		dataManager.removePolicy("id3");

		boolean isInvalidID = false;

		try {
			dataManager.getPolicyById("id3");
		}
		catch (InvalidIDException e) {
			isInvalidID = true;
		}

		assertTrue("Policy was found and removed", isInvalidID);
	}

	@Test
	public void testAddTwicePolicy() {

		// Add a new API
		Policy policy = new Policy();
		policy.setId("id3");
		dataManager.addPolicy(policy);

		boolean exceptionRaised = false;
		try
		{
			dataManager.addPolicy(policy);
		}
		catch(Exception e)
		{
			exceptionRaised = true;
		}

		assertTrue("Exception raised", exceptionRaised);

		// cleanup
		dataManager.removePolicy(policy.getId());
	}

	@Test
	public void testGetAllPolicy() {

		// Add a new Policy
		Policy policy = new Policy();
		policy.setId("id1");
		dataManager.addPolicy(policy);

		// Add a new Policy
		Policy policy2 = new Policy();
		policy2.setId("id2");
		dataManager.addPolicy(policy2);

		Set<String> policies = dataManager.getAllPolicy();
		assertNotNull("No policy found", policies);

		List<String> policyList = new ArrayList<String>();

		for (String policyStr : policies) {
			policyList.add(policyStr);
		}

		assertEquals("Auth count is correct", 2, policyList.size());
		assertTrue("Both Auths are found", (policyList.get(0).equals(policy.getId()) || policyList.get(0).equals(policy2.getId())) && (policyList.get(1).equals(policy.getId()) || policyList.get(1).equals(policy2.getId())));

		// cleanup
		dataManager.removePolicy(policy.getId());
		dataManager.removePolicy(policy2.getId());
	}

	@Test
	public void testAddPolicyAndUpdateApiAuthThenRemovePolicy() {

		// Add a new API
		Api api = new Api();
		api.setId("id1");
		api.setApiDetail(new ApiDetail());
		dataManager.addApi(api);

		// Add a new Auth
		Auth auth = new Auth();
		auth.setId("id2");
		auth.setStatus(StatusType.ACTIVE);
		auth.setAuthDetail(new AuthDetail());
		auth.getAuthDetail().setType(NBAuthType.AUTHKEY);
		auth.getAuthDetail().setAuthKeyValue("authKey10");
		dataManager.addAuth(auth);

		// Add a new Policy
		Policy policy = new Policy();
		policy.setId("id3");
		policy.getApiIds().add(api.getId());
		dataManager.addPolicy(policy);

		// Create bucket
		QuotaRLBucket authIds = new QuotaRLBucket();
		authIds.setId("id3");
		authIds.getAuthIds().add(auth.getId());
		dataManager.createBucket(policy.getId(), authIds);

		// Now, get the API and check that it's OK
		Api api2 = dataManager.getApiById("id1");
		assertEquals("API has new policy", policy.getId(), api2.getPolicyIds().get(0));

		// Now, get the Auth and check that it's OK
		Auth auth2 = dataManager.getAuthById("id2");
		assertEquals("Auth has new policy", policy.getId(), auth2.getPolicyContexts().get(0).getPolicyId());

		// Check that bucket is added correctly to policy
		policy = dataManager.getPolicyById(policy.getId());
		assertEquals("Correct number of buckets for policy", 1, policy.getAuthIds().size());
		assertEquals("Policy has new bucket", authIds.getId(), policy.getAuthIds().get(0).getId());		

		dataManager.removePolicy(policy.getId());

		// Now, get the API and check that it's OK
		Api api3 = dataManager.getApiById("id1");
		assertEquals("API has no policy", 0, api3.getPolicyIds().size());

		// Now, get the API and check that it's OK
		Auth auth3 = dataManager.getAuthById("id2");
		assertEquals("Auth has no policy", 0, auth3.getPolicyContexts().size());

		// cleanup
		dataManager.removeApi(api.getId());
		dataManager.removeAuth(auth.getId());
	}

	@Test
	public void testAddPolicyAndUpdateApiAuthThenRemoveApi() {

		// Add a new API
		Api api = new Api();
		api.setApiDetail(new ApiDetail());
		api.setId("id51");
		dataManager.addApi(api);

		// Add a new Auth
		Auth auth = new Auth();
		auth.setId("id52");
		auth.setStatus(StatusType.ACTIVE);
		auth.setAuthDetail(new AuthDetail());
		auth.getAuthDetail().setType(NBAuthType.AUTHKEY);
		auth.getAuthDetail().setAuthKeyValue("authKey20");
		dataManager.addAuth(auth);

		// Add a new Policy
		Policy policy = new Policy();
		policy.setId("id53");
		policy.getApiIds().add(api.getId());
		QuotaRLBucket authIds = new QuotaRLBucket();
		authIds.setId("bucketId");
		authIds.getAuthIds().add(auth.getId());
		policy.getAuthIds().add(authIds);
		dataManager.addPolicy(policy);

		dataManager.removeApi(api.getId());

		// Now, get the Policy and check that it's OK
		Policy policy2 = dataManager.getPolicyById(policy.getId());
		assertEquals("Policy has no API id", 0, policy2.getApiIds().size());

		// cleanup
		dataManager.removeAuth(auth.getId());
	}

	@Test
	public void testAddPolicyAndUpdateApiAuthThenRemoveAuth() {

		// Add a new API
		Api api = new Api();
		api.setId("id61");
		api.setApiDetail(new ApiDetail());
		dataManager.addApi(api);

		// Add a new Auth
		Auth auth = new Auth();
		auth.setId("id62");
		auth.setStatus(StatusType.ACTIVE);
		auth.setAuthDetail(new AuthDetail());
		auth.getAuthDetail().setType(NBAuthType.AUTHKEY);
		auth.getAuthDetail().setAuthKeyValue("authKey30");
		dataManager.addAuth(auth);

		// Add a new Policy
		QuotaRLBucket authIds = new QuotaRLBucket();
		authIds.setId("id63");
		authIds.getAuthIds().add(auth.getId());

		Policy policy = new Policy();
		policy.setId("id64");
		policy.getApiIds().add(api.getId());
		policy.getAuthIds().add(authIds);
		dataManager.addPolicy(policy);

		dataManager.removeAuth(auth.getId());

		// Now, get the Policy and check that it's OK
		Policy policy2 = dataManager.getPolicyById(policy.getId());
		assertEquals("Policy has no Auth id", 0, policy2.getAuthIds().get(0).getAuthIds().size());

		// cleanup
		dataManager.removeApi(api.getId());
	}

	@Test
	public void testGetMatchingPolicies() {

		// Add a new API
		Api api = new Api();
		api.setApiDetail(new ApiDetail());
		api.setId("id31");
		dataManager.addApi(api);

		// Add a new API
		Api api2 = new Api();
		api2.setApiDetail(new ApiDetail());
		api2.setId("id32");
		dataManager.addApi(api2);

		// Add a new API
		Api api3 = new Api();
		api3.setApiDetail(new ApiDetail());
		api3.setId("id33");
		dataManager.addApi(api3);

		// Add a new API
		Api api4 = new Api();
		api4.setApiDetail(new ApiDetail());
		api4.setId("id34");
		dataManager.addApi(api4);

		// Add a new Auth
		Auth auth = new Auth();
		auth.setId("id31");
		auth.setStatus(StatusType.ACTIVE);
		AuthDetail detail = new AuthDetail();
		auth.setAuthDetail(detail);
		auth.getAuthDetail().setType(NBAuthType.AUTHKEY);
		auth.getAuthDetail().setAuthKeyValue("authKey31");
		dataManager.addAuth(auth);

		// Add a new Auth
		Auth auth2 = new Auth();
		auth2.setId("id32");
		auth2.setStatus(StatusType.ACTIVE);
		AuthDetail detail2 = new AuthDetail();
		auth2.setAuthDetail(detail2);
		auth2.getAuthDetail().setType(NBAuthType.AUTHKEY);
		auth2.getAuthDetail().setAuthKeyValue("authKey32");
		dataManager.addAuth(auth2);

		// Add a new Auth
		Auth auth3 = new Auth();
		auth3.setId("id33");
		auth3.setStatus(StatusType.ACTIVE);
		AuthDetail detail3 = new AuthDetail();
		auth3.setAuthDetail(detail3);
		auth3.getAuthDetail().setType(NBAuthType.AUTHKEY);
		auth3.getAuthDetail().setAuthKeyValue("authKey33");
		dataManager.addAuth(auth3);

		// Add a new Auth
		Auth auth4 = new Auth();
		auth4.setId("id34");
		auth4.setStatus(StatusType.ACTIVE);
		AuthDetail detail4 = new AuthDetail();
		auth4.setAuthDetail(detail4);
		auth4.getAuthDetail().setType(NBAuthType.BASIC);
		auth4.getAuthDetail().setUsername("user1");
		auth4.getAuthDetail().setPassword(new byte[]{'p', 'a', 's', 's', '1'});
		dataManager.addAuth(auth4);

		// Add a new Policy: Associated to API id1 and Auth id1
		Policy policy = new Policy();
		policy.setId("id31");
		policy.getApiIds().add(api.getId());

		// Create Bucket authIds of auth ids
		QuotaRLBucket authIds = new QuotaRLBucket();
		authIds.setId("bucketId");
		authIds.getAuthIds().add(auth.getId());
		// Add bucket authIds to policy
		policy.getAuthIds().add(authIds);

		dataManager.addPolicy(policy);

		// Add a new Policy: Associated to API id2, API id3 and Auth id2, id4
		Policy policy2 = new Policy();
		policy2.setId("id32");
		policy2.getApiIds().add(api2.getId());
		policy2.getApiIds().add(api3.getId());

		// Create Bucket authIds2 of auth ids
		QuotaRLBucket authIds2 = new QuotaRLBucket();
		authIds.setId("bucketId2");
		authIds2.getAuthIds().add(auth2.getId());
		authIds2.getAuthIds().add(auth4.getId());
		// Add bucket authIds2 to the policy
		policy2.getAuthIds().add(authIds2);

		dataManager.addPolicy(policy2);


		// Add a new Policy: Associatd to API id4 and no Auth
		Policy policy3 = new Policy();
		policy3.setId("id33");
		policy3.getApiIds().add(api4.getId());
		dataManager.addPolicy(policy3);


		// Add a new Policy: Associatd to no API and Auth id3
		Policy policy4 = new Policy();
		policy4.setId("id34");

		// Create Bucket authIds4 of auth ids
		QuotaRLBucket authIds4 = new QuotaRLBucket();
		authIds.setId("bucketId4");
		authIds4.getAuthIds().add(auth3.getId());
		// Add bucket authIds4 to the policy
		policy4.getAuthIds().add(authIds4);

		dataManager.addPolicy(policy4);


		// Add a new Policy: Associatd to API id3 and no Auth
		Policy policy5 = new Policy();
		policy5.setId("id35");
		policy5.getApiIds().add(api3.getId());
		dataManager.addPolicy(policy5);


		// Add a new Policy: Associatd to API id3 and Auth id3
		Policy policy6 = new Policy();
		policy6.setId("id36");
		policy6.getApiIds().add(api3.getId());

		// Create Bucket authIds6  of auth ids
		QuotaRLBucket authIds6 = new QuotaRLBucket();
		authIds.setId("bucketId6");
		authIds6.getAuthIds().add(auth3.getId());
		// Add bucket authIds6 to the policy
		policy6.getAuthIds().add(authIds6);

		dataManager.addPolicy(policy6);


		// Add a new Policy: Associatd to no API and Auth id2
		Policy policy7 = new Policy();
		policy7.setId("id37");

		// Create Bucket authIds7  of authId2
		QuotaRLBucket authIds7 = new QuotaRLBucket();
		authIds.setId("bucketId7");
		authIds7.getAuthIds().add(auth2.getId());
		// Add bucket authIds7 to the policy
		policy7.getAuthIds().add(authIds7);

		dataManager.addPolicy(policy7);

		// Fetch updated values from cache
		api = dataManager.getApiById(api.getId());
		api2 = dataManager.getApiById(api2.getId());
		api3 = dataManager.getApiById(api3.getId());
		api4 = dataManager.getApiById(api4.getId());
		auth = dataManager.getAuthById(auth.getId());
		auth2 = dataManager.getAuthById(auth2.getId());
		auth3 = dataManager.getAuthById(auth3.getId());
		auth4 = dataManager.getAuthById(auth4.getId());

		List<CallDescriptor> list1 = dataManager.getMatchingPolicies(api, auth);
		assertEquals("List1 has correct size", 1, list1.size());
		assertEquals("Matching policy is correct", policy.getId(), list1.get(0).getPolicy().getId());

		List<CallDescriptor> list2 = dataManager.getMatchingPolicies(api2, auth2);
		assertEquals("List2 has correct size", 2, list2.size());
		assertEquals("Matching policy is correct", policy2.getId(), list2.get(0).getPolicy().getId());
		assertEquals("Matching policy is correct", policy7.getId(), list2.get(1).getPolicy().getId());

		List<CallDescriptor> list3 = dataManager.getMatchingPolicies(api3, null);
		assertEquals("List3 has correct size", 1, list3.size());
		assertEquals("Matching policy is correct", policy5.getId(), list3.get(0).getPolicy().getId());

		List<CallDescriptor> list4 = dataManager.getMatchingPolicies(null, auth3);
		assertEquals("List4 has correct size", 1, list4.size());
		assertEquals("Matching policy is correct", policy4.getId(), list4.get(0).getPolicy().getId());

		List<CallDescriptor> list5 = dataManager.getMatchingPolicies(api4, auth3);
		assertEquals("List5 has correct size", 1, list5.size());
		assertEquals("Matching policy is correct", policy4.getId(), list5.get(0).getPolicy().getId());

		List<CallDescriptor> list6 = dataManager.getMatchingPolicies(api4, auth4);
		assertNull("List6 has correct size", list6);

		List<CallDescriptor> list7 = dataManager.getMatchingPolicies(null, null);
		assertNull("List7 has correct size", list7);

		// cleanup
		dataManager.removePolicy(policy.getId());
		dataManager.removePolicy(policy2.getId());
		dataManager.removePolicy(policy3.getId());
		dataManager.removePolicy(policy4.getId());
		dataManager.removePolicy(policy5.getId());
		dataManager.removePolicy(policy6.getId());
		dataManager.removePolicy(policy7.getId());
		dataManager.removeApi(api.getId());
		dataManager.removeApi(api2.getId());
		dataManager.removeApi(api3.getId());
		dataManager.removeApi(api4.getId());
		dataManager.removeAuth(auth.getId());
		dataManager.removeAuth(auth2.getId());
		dataManager.removeAuth(auth3.getId());
		dataManager.removeAuth(auth4.getId());
	}

	@Test
	public void testAddPolicyAndUpdateApiAuthWithCreateBucketThenRemovePolicy() {

		// Add a new API
		Api api = new Api();
		api.setApiDetail(new ApiDetail());
		api.setId("apiId1");
		dataManager.addApi(api);

		// Add a new Auth
		Auth auth = new Auth();
		auth.setId("authId1");
		auth.setStatus(StatusType.ACTIVE);
		auth.setAuthDetail(new AuthDetail());
		auth.getAuthDetail().setType(NBAuthType.AUTHKEY);
		auth.getAuthDetail().setAuthKeyValue("authKey");
		dataManager.addAuth(auth);

		// Add a new Policy
		Policy policy = new Policy();
		policy.setId("policyId1");
		policy.getApiIds().add(api.getId());
		dataManager.addPolicy(policy);

		// Create bucket to add new Auth
		QuotaRLBucket authIds = new QuotaRLBucket();
		authIds.setId("bucketId1");
		authIds.getAuthIds().add(auth.getId());
		dataManager.createBucket(policy.getId(), authIds);
		// fetch updated policy from the cache
		policy = dataManager.getPolicyById(policy.getId());

		// Now, get the API and check that it's OK
		Api api2 = dataManager.getApiById(api.getId());
		assertEquals("API has new policy", policy.getId(), api2.getPolicyIds().get(0));

		// Now, get the Auth and check that it's OK
		Auth auth2 = dataManager.getAuthById(auth.getId());
		assertEquals("Auth has new policy", policy.getId(), auth2.getPolicyContexts().get(0).getPolicyId());

		// Check that bucket is added correctly to policy
		assertEquals("Correct number of buckets for policy", 1, policy.getAuthIds().size());
		assertEquals("Policy has new bucket", authIds.getId(), policy.getAuthIds().get(0).getId());		

		dataManager.removePolicy(policy.getId());

		// Now, get the API and check that it's OK
		Api api3 = dataManager.getApiById(api.getId());
		assertEquals("API has no policy", 0, api3.getPolicyIds().size());

		// Now, get the API and check that it's OK
		Auth auth4 = dataManager.getAuthById(auth.getId());
		assertEquals("Auth has no policy", 0, auth4.getPolicyContexts().size());

		// cleanup
		dataManager.removeApi(api.getId());
		dataManager.removeAuth(auth.getId());
	}

	@Test
	public void testAddPolicyWithBucketAndAppendAuthsThenRemovePolicy() {

		// Add a new API
		Api api = new Api();
		api.setApiDetail(new ApiDetail());
		api.setId("apiId1");
		dataManager.addApi(api);

		// Prepare Auths
		Auth auth = new Auth();
		auth.setId("authId1");
		auth.setStatus(StatusType.ACTIVE);
		auth.setAuthDetail(new AuthDetail());
		auth.getAuthDetail().setType(NBAuthType.AUTHKEY);
		auth.getAuthDetail().setAuthKeyValue("authKey1");
		dataManager.addAuth(auth);

		Auth auth2 = new Auth();
		auth2.setId("authId2");
		auth2.setStatus(StatusType.ACTIVE);
		auth2.setAuthDetail(new AuthDetail());
		auth2.getAuthDetail().setType(NBAuthType.AUTHKEY);
		auth2.getAuthDetail().setAuthKeyValue("authKey2");
		dataManager.addAuth(auth2);

		// Add a new Policy
		Policy policy = new Policy();
		policy.setId("policyId1");
		policy.getApiIds().add(api.getId());
		dataManager.addPolicy(policy);

		// Create bucket to add new Auth
		QuotaRLBucket authIds = new QuotaRLBucket();
		authIds.setId("bucketId1");
		authIds.getAuthIds().add(auth.getId());
		dataManager.createBucket(policy.getId(), authIds);
		// fetch updated policy from the cache
		policy = dataManager.getPolicyById(policy.getId());

		// Now, get the API and check that it's OK
		Api gApi1 = dataManager.getApiById(api.getId());
		assertEquals("API has new policy", policy.getId(), gApi1.getPolicyIds().get(0));

		// Now, get the Auth and check that it's OK
		Auth gAuth1 = dataManager.getAuthById(auth.getId());
		assertEquals("Auth has new policy", policy.getId(), gAuth1.getPolicyContexts().get(0).getPolicyId());

		// Check that bucket is added correctly to policy
		assertEquals("Correct number of buckets for policy", 1, policy.getAuthIds().size());
		assertEquals("Policy has new bucket", authIds.getId(), policy.getAuthIds().get(0).getId());	

		// Append a new Auth to the bucket
		QuotaRLBucket authIdsToAppend = new QuotaRLBucket();
		authIdsToAppend.getAuthIds().add(auth2.getId());
		dataManager.addAuthsToBucket(policy.getId(), authIds.getId(), authIdsToAppend);
		// fetch updated policy from the cache
		policy = dataManager.getPolicyById(policy.getId());

		// Check that auth2 has been appended to original bucket
		Auth gAuth2 = dataManager.getAuthById(auth2.getId());
		assertEquals("Auth has new policy", policy.getId(), gAuth2.getPolicyContexts().get(0).getPolicyId());
		assertEquals("Correct number of buckets for policy", 1, policy.getAuthIds().size());
		assertEquals("Correct number of auths in the bucket", 2, policy.getAuthIds().get(0).getAuthIds().size());

		// Retry appending the same auth to the same bucket
		dataManager.addAuthsToBucket(policy.getId(), authIds.getId(), authIdsToAppend);

		// fetch updated policy from the cache
		policy = dataManager.getPolicyById(policy.getId());

		// Check that auth hasn't been added twice
		assertEquals("Correct number of auths in the bucket", 2, policy.getAuthIds().get(0).getAuthIds().size());

		dataManager.removePolicy(policy.getId());

		// Now, get the API and check that it's OK
		Api gApi1_2 = dataManager.getApiById(api.getId());
		assertEquals("API has no policy", 0, gApi1_2.getPolicyIds().size());

		// Now, get the Auth and check that it's OK
		Auth gAuth1_2 = dataManager.getAuthById(auth.getId());
		assertEquals("Auth has no policy", 0, gAuth1_2.getPolicyContexts().size());

		// Now, get the Auth and check that it's OK
		Auth gAuth2_2 = dataManager.getAuthById(auth2.getId());
		assertEquals("Auth has no policy", 0, gAuth2_2.getPolicyContexts().size());

		// cleanup
		dataManager.removeApi(api.getId());
		dataManager.removeAuth(auth.getId());
		dataManager.removeAuth(auth2.getId());

	}

	@Test
	public void testAddPolicyAppendAuthsBucketsRemoveBucketsRemovePolicy() {

		// Add a new API
		Api api = new Api();
		api.setId("apiId1");
		api.setApiDetail(new ApiDetail());
		dataManager.addApi(api);

		// Prepare Auths
		Auth auth = new Auth();
		auth.setId("authId1");
		auth.setStatus(StatusType.ACTIVE);
		auth.setAuthDetail(new AuthDetail());
		auth.getAuthDetail().setType(NBAuthType.AUTHKEY);
		auth.getAuthDetail().setAuthKeyValue("authKey1");
		dataManager.addAuth(auth);

		Auth auth2 = new Auth();
		auth2.setId("authId2");
		auth2.setStatus(StatusType.ACTIVE);
		auth2.setAuthDetail(new AuthDetail());
		auth2.getAuthDetail().setType(NBAuthType.AUTHKEY);
		auth2.getAuthDetail().setAuthKeyValue("authKey2");
		dataManager.addAuth(auth2);

		Auth auth3 = new Auth();
		auth3.setId("authId3");
		auth3.setStatus(StatusType.ACTIVE);
		auth3.setAuthDetail(new AuthDetail());
		auth3.getAuthDetail().setType(NBAuthType.AUTHKEY);
		auth3.getAuthDetail().setAuthKeyValue("authKey3");
		dataManager.addAuth(auth3);

		// Add a new Policy
		Policy policy = new Policy();
		policy.setId("policyId1");
		policy.getApiIds().add(api.getId());
		dataManager.addPolicy(policy);

		// Create bucket to add new Auth
		QuotaRLBucket authIds = new QuotaRLBucket();
		authIds.setId("bucketId1");
		authIds.getAuthIds().add(auth.getId());
		dataManager.createBucket(policy.getId(), authIds);

		// fetch policy from the cache
		policy = dataManager.getPolicyById(policy.getId());

		// Now, get the API and check that it's OK
		Api gApi1 = dataManager.getApiById(api.getId());
		assertEquals("API has new policy", policy.getId(), gApi1.getPolicyIds().get(0));

		// Now, get the Auth and check that it's OK
		Auth gAuth1 = dataManager.getAuthById(auth.getId());
		assertEquals("Auth has new policy", policy.getId(), gAuth1.getPolicyContexts().get(0).getPolicyId());

		// Check that bucket is added correctly to policy
		assertEquals("Correct number of buckets for policy", 1, policy.getAuthIds().size());
		assertEquals("Policy has new bucket", authIds.getId(), policy.getAuthIds().get(0).getId());	

		// Append a new Auth to the bucket
		QuotaRLBucket authIdsToAppend = new QuotaRLBucket();
		authIdsToAppend.getAuthIds().add(auth2.getId());
		dataManager.addAuthsToBucket(policy.getId(), authIds.getId(), authIdsToAppend);

		// fetch policy from the cache
		policy = dataManager.getPolicyById(policy.getId());

		// Check that auth2 has been appended to original bucket
		Auth gAuth2 = dataManager.getAuthById(auth2.getId());
		assertEquals("Auth has new policy", policy.getId(), gAuth2.getPolicyContexts().get(0).getPolicyId());
		assertEquals("Correct number of buckets for policy", 1, policy.getAuthIds().size());
		assertEquals("Correct number of auths in the bucket", 2, policy.getAuthIds().get(0).getAuthIds().size());

		// Retry appending the same auth to the same bucket
		dataManager.addAuthsToBucket(policy.getId(), authIds.getId(), authIdsToAppend);

		// fetch policy from the cache
		policy = dataManager.getPolicyById(policy.getId());

		// Check that auth hasn't been added twice
		assertEquals("Correct number of auths in the bucket", 2, policy.getAuthIds().get(0).getAuthIds().size());

		// Create a new bucket containing a third auth
		QuotaRLBucket authIds2 = new QuotaRLBucket();
		authIds2.setId("bucketId2");
		authIds2.getAuthIds().add(auth3.getId());
		dataManager.createBucket(policy.getId(), authIds2);

		// fetch policy from the cache
		policy = dataManager.getPolicyById(policy.getId());

		// Check that bucket has been added correctly
		assertEquals("Correct number of buckets for policy", 2, policy.getAuthIds().size());

		// Just to be sure that order is maintained
		QuotaRLBucket bucket1, bucket2;
		if(policy.getAuthIds().get(0).getId().equals(authIds.getId())) {
			bucket1 = policy.getAuthIds().get(0);
			bucket2 = policy.getAuthIds().get(1);
		} else {
			bucket1 = policy.getAuthIds().get(1);
			bucket2 = policy.getAuthIds().get(0);
		}

		assertEquals("Correct number of auths in the bucket 1", 2, bucket1.getAuthIds().size());
		assertEquals("Correct number of auths in the bucket 2", 1, bucket2.getAuthIds().size());

		// removing auth3 from bucket2
		dataManager.removeAuthFromBucket(policy.getId(), bucket2.getId(), auth3.getId());

		// fetch policy from the cache
		policy = dataManager.getPolicyById(policy.getId());

		assertEquals("Correct number of buckets for policy", 2, policy.getAuthIds().size());
		assertEquals("Correct number of auths in the bucket 1", 2, bucket1.getAuthIds().size());
		assertEquals("Correct number of auths in the bucket 2", 0, bucket2.getAuthIds().size());

		// Now, get auth3 and check that it's no more associated with the policy
		Auth gAuth3_2 = dataManager.getAuthById(auth3.getId());
		assertEquals("Auth has no policy", 0, gAuth3_2.getPolicyContexts().size());

		// Removing bucket2
		dataManager.removeBucket(policy.getId(), authIds2.getId());
		assertEquals("Correct number of buckets for policy", 1, policy.getAuthIds().size());
		assertEquals("Correct number of auths in the bucket 1", 2, policy.getAuthIds().get(0).getAuthIds().size());

		// fetch policy from the cache
		policy = dataManager.getPolicyById(policy.getId());		

		// Removing bucket1
		dataManager.removeBucket(policy.getId(), authIds.getId());
		assertEquals("Correct number of buckets for policy", 0, policy.getAuthIds().size());

		dataManager.removePolicy(policy.getId());

		// Now, get the API and check that it's OK
		Api gApi1_2 = dataManager.getApiById(api.getId());
		assertEquals("API has no policy", 0, gApi1_2.getPolicyIds().size());

		// Now, get the Auth and check that it's OK
		Auth gAuth1_2 = dataManager.getAuthById(auth.getId());
		assertEquals("Auth has no policy", 0, gAuth1_2.getPolicyContexts().size());

		// Now, get the Auth and check that it's OK
		Auth gAuth2_2 = dataManager.getAuthById(auth2.getId());
		assertEquals("Auth has no policy", 0, gAuth2_2.getPolicyContexts().size());

		// cleanup
		dataManager.removeApi(api.getId());
		dataManager.removeAuth(auth.getId());
		dataManager.removeAuth(auth2.getId());
		dataManager.removeAuth(auth3.getId());
	}






	@Test
	public void testGetMatchingPoliciesWithSeveralAuth() {

		// Add a new API
		Api api = new Api();
		api.setApiDetail(new ApiDetail());
		api.setId("id31");
		dataManager.addApi(api);

		// Add a new Auth
		Auth auth = new Auth();
		auth.setId("id31");
		auth.setStatus(StatusType.ACTIVE);
		AuthDetail detail = new AuthDetail();
		auth.setAuthDetail(detail);
		auth.getAuthDetail().setType(NBAuthType.AUTHKEY);
		auth.getAuthDetail().setAuthKeyValue("authKey31");
		dataManager.addAuth(auth);

		// Add a new Auth
		Auth auth2 = new Auth();
		auth2.setId("id32");
		auth2.setStatus(StatusType.ACTIVE);
		auth2.setAuthDetail(new AuthDetail());
		auth2.getAuthDetail().setType(NBAuthType.BASIC);
		auth2.getAuthDetail().setUsername("username");
		auth2.getAuthDetail().setPassword("password".getBytes());
		dataManager.addAuth(auth2);

		// Add a new Auth
		Auth auth3 = new Auth();
		auth3.setId("id33");
		auth3.setStatus(StatusType.ACTIVE);
		auth3.setAuthDetail(new AuthDetail());
		auth3.getAuthDetail().setType(NBAuthType.IP_WHITE_LIST);
		auth3.getAuthDetail().getWhiteListedIps().add("12.23.45.56");
		auth3.getAuthDetail().getWhiteListedIps().add("45.45.89.65");
		dataManager.addAuth(auth3);

		// Add a new Policy: Associated to API id1 and Auth id1
		Policy policy = new Policy();
		policy.setId("id31");
		policy.getApiIds().add(api.getId());

		// Create Bucket authIds of auth ids
		QuotaRLBucket authIds = new QuotaRLBucket();
		authIds.setId("bucketId");
		authIds.getAuthIds().add(auth.getId());
		authIds.getAuthIds().add(auth2.getId());
		authIds.getAuthIds().add(auth3.getId());

		// Add bucket authIds to policy
		policy.getAuthIds().add(authIds);

		dataManager.addPolicy(policy);

		Api api_ = dataManager.getApiById(api.getId());
		Auth auth_ = dataManager.getAuthById(auth.getId());

		List<CallDescriptor> list1 = dataManager.getMatchingPolicies(api_, auth_);
		assertEquals("List1 has correct size", 1, list1.size());
		assertEquals("Matching policy is correct", policy.getId(), list1.get(0).getPolicy().getId());

		// cleanup
		dataManager.removePolicy(policy.getId());
		dataManager.removeApi(api.getId());
		dataManager.removeAuth(auth.getId());
		dataManager.removeAuth(auth2.getId());
		dataManager.removeAuth(auth3.getId());
	}

	@Test
	public void testSettings() {

		dataManager.putSettingString("testValue", "str");
		assertEquals("Setting has correctly been put/get", "str", dataManager.getSettingString("testValue"));
		dataManager.clearSettingString("testValue");
		assertEquals("Setting has correctly been removed", null, dataManager.getSettingString("testValue"));

	}

}
