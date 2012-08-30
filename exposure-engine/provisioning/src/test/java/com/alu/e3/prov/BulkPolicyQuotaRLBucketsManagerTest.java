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
package com.alu.e3.prov;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alu.e3.common.E3Constant;
import com.alu.e3.prov.restapi.model.Auth;
import com.alu.e3.prov.restapi.model.AuthIdsNoIdType;
import com.alu.e3.prov.restapi.model.AuthType;
import com.alu.e3.prov.restapi.model.BasicAuth;
import com.alu.e3.prov.restapi.model.BasicResponse;
import com.alu.e3.prov.restapi.model.BulkPolicyQuotaRLBucketType;
import com.alu.e3.prov.restapi.model.Context;
import com.alu.e3.prov.restapi.model.Policy;
import com.alu.e3.prov.restapi.model.PolicyIdsType;
import com.alu.e3.prov.restapi.model.Status;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.internal.mapping.ObjectMapping;
import com.jayway.restassured.mapper.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { 
		"classpath:/spring/provisioning.osgi-context-test.xml", 
		"classpath:/spring/provisioning.provision-beans-test.xml", 
		"classpath:/spring/provisioning.rest-declaration-test.xml" })		
public class BulkPolicyQuotaRLBucketsManagerTest {

	private String apiVersion = E3Constant.REST_API_VERSION;

	private String baseBulkPath = "/cxf/e3/prov/" + apiVersion + "/bulk/policies/quotaRLBuckets";

	private String basePoliciesPath = "/cxf/e3/prov/" + apiVersion + "/policies";

	private String baseAuthPath = "/cxf/e3/prov/" + apiVersion + "/auths";

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		// RestAssured.baseURI = "http://192.168.84.51";RestAssured.port = 8181;

	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 2666;		
		RestAssured.rootPath = "response";
		
		RestAssured.basePath = "";
		
		baseBulkPath = "/cxf/e3/prov/" + apiVersion + "/bulk/policies/quotaRLBuckets";
		basePoliciesPath = "/cxf/e3/prov/" + apiVersion + "/policies";
		baseAuthPath = "/cxf/e3/prov/" + apiVersion + "/auths";
		
		createPolicy("p_1");
		createPolicy("p_2");
		createPolicy("p_3");

		createAuth("a_1");
		createAuth("a_2");
		createAuth("a_3");
		createAuth("a_4");
		
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		deletePolicy("p_1");
		deletePolicy("p_2");
		deletePolicy("p_3");

		deleteAuth("a_1");
		deleteAuth("a_2");
		deleteAuth("a_3");
		deleteAuth("a_4");
		
	}

	@Test
	public void testCreateDeleteBulkBucket() throws Exception {
		String bucketId = "bucket_1";

		BulkPolicyQuotaRLBucketType bulk = newBulkProvision(bucketId);

		BasicResponse response = given().contentType("application/xml").body(bulk, ObjectMapper.JAXB).expect().statusCode(200).rootPath("response").body("status", equalTo("SUCCESS")).log().ifError()
				.when().post(baseBulkPath).andReturn().as(BasicResponse.class, ObjectMapper.JAXB);

		assertNotNull(response);
		assertEquals("SUCCESS", response.getStatus());
		
		
		bulk.getQuotaRLBucket().getAuthIds().add("a_3");
		bulk.getQuotaRLBucket().getAuthIds().add("a_4");
		
		// append 
		
		response = given().contentType("application/xml").body(bulk, ObjectMapper.JAXB).expect().statusCode(500).rootPath("response").body("status", equalTo("FAILURE")).log().ifError()
		.when().put(baseBulkPath  + "/" + "wrongBucketID").andReturn().as(BasicResponse.class, ObjectMapper.JAXB);
		
		response = given().contentType("application/xml").body(bulk, ObjectMapper.JAXB).expect().statusCode(200).rootPath("response").body("status", equalTo("SUCCESS")).log().ifError()
		.when().put(baseBulkPath  + "/" + bulk.getQuotaRLBucket().getId()).andReturn().as(BasicResponse.class, ObjectMapper.JAXB);

		assertNotNull(response);
		assertEquals("SUCCESS", response.getStatus());
		
		// Delete step
		response = given().contentType("application/xml").body(bulk).expect().statusCode(200).rootPath("response").body("status", equalTo("SUCCESS")).log().ifError().when()
				.put(baseBulkPath + "/" + bulk.getQuotaRLBucket().getId() +"/deleteBucket").andReturn().as(BasicResponse.class, ObjectMapper.JAXB);

		assertNotNull(response);
		assertEquals("SUCCESS", response.getStatus());
	}

	private void createPolicy(String id) throws Exception {
		// CREATE
		Policy policy = new Policy();

		policy.setId(id);

		Context context = new Context();

		context.setId("anid");
		context.setStatus(Status.ACTIVE);

		policy.getContexts().add(context);

		BasicResponse response = given().contentType("application/xml").body(policy, ObjectMapper.JAXB).expect().statusCode(200).rootPath("response").body("status", equalTo("SUCCESS")).log()
				.ifError().when().post(basePoliciesPath).andReturn().as(BasicResponse.class, ObjectMapper.JAXB);

		assertNotNull(response);
		assertEquals("SUCCESS", response.getStatus());

		if (id != null) {
			assertEquals(id, response.getId());
		} else {
			assertNotNull(response.getId());
			id = response.getId();
		}

	}

	private void deletePolicy(String id) {
		BasicResponse response = given().contentType("application/xml").expect().statusCode(200).rootPath("response").body("status", equalTo("SUCCESS")).log().ifError().when()
				.delete(basePoliciesPath + "/" + id).andReturn().as(BasicResponse.class, ObjectMapper.JAXB);

		assertNotNull(response);
		assertEquals("SUCCESS", response.getStatus());
	}

	public void createAuth(String id) throws Exception {

		Auth data = newAuthProvision(id);

		BasicResponse response = given()
		.contentType("application/xml")
		.body(data, ObjectMapper.JAXB)
		.expect()
		.statusCode(200).rootPath("response").body("status", equalTo("SUCCESS"))
		.log().ifError()
		.when().post(baseAuthPath)
		.andReturn().as(BasicResponse.class, ObjectMapper.JAXB);

		assertNotNull(response);
		assertEquals("SUCCESS", response.getStatus());

	}

	private void deleteAuth(String id) {
		BasicResponse response = given().contentType("application/xml").expect().statusCode(200).rootPath("response").body("status", equalTo("SUCCESS")).log().ifError().when()
				.delete(baseAuthPath + "/" + id).andReturn().as(BasicResponse.class, ObjectMapper.JAXB);

		assertNotNull(response);
		assertEquals("SUCCESS", response.getStatus());

	}

	private Auth newAuthProvision(String authId) {

		Auth data = new Auth();

		data.setId(authId);
		data.setType(AuthType.BASIC);
		data.setStatus(Status.ACTIVE);
		data.setApiContext("apiCtx");
		data.setPolicyContext("policyCtx");

		data.setBasicAuth(new BasicAuth());
		data.getBasicAuth().setUsername("username0");
		data.getBasicAuth().setPassword(("password0" + authId).getBytes());

		//data.setIpWhiteListAuth(new IpWhiteListAuth());
		//data.getIpWhiteListAuth().getIp().add("192.168.84.67");

		//data.setAuthKeyAuth(new AuthKeyAuth());
		//data.getAuthKeyAuth().setKeyValue("keyvaluesgfhjzghjzeg");

		System.out.println(ObjectMapping.serialize(data, "xml", ObjectMapper.JAXB));

		return data;
	}

	private BulkPolicyQuotaRLBucketType newBulkProvision(String bucketId) {

		BulkPolicyQuotaRLBucketType bulk = new BulkPolicyQuotaRLBucketType();
		PolicyIdsType policies = new PolicyIdsType();

		bulk.setPolicies(policies);

		policies.getId().add("p_1");
		policies.getId().add("p_2");
		policies.getId().add("p_3");

		AuthIdsNoIdType authBucket = new AuthIdsNoIdType();
		bulk.setQuotaRLBucket(authBucket);

		authBucket.getAuthIds().add("a_1");
		authBucket.getAuthIds().add("a_2");
		//authBucket.getAuthIds().add("a_3");
		//authBucket.getAuthIds().add("a_4");

		authBucket.setId(bucketId);

		System.out.println(ObjectMapping.serialize(bulk, "xml", ObjectMapper.JAXB));

		return bulk;
	}
}
