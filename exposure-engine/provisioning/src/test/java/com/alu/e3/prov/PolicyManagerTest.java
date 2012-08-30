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
import com.alu.e3.prov.restapi.model.BasicResponse;
import com.alu.e3.prov.restapi.model.Context;
import com.alu.e3.prov.restapi.model.Policy;
import com.alu.e3.prov.restapi.model.Status;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.mapper.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { 
		"classpath:/spring/provisioning.osgi-context-test.xml", 
		"classpath:/spring/provisioning.provision-beans-test.xml", 
		"classpath:/spring/provisioning.rest-declaration-test.xml"
		 })
public class PolicyManagerTest {
	
	private String apiVersion = E3Constant.REST_API_VERSION;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
					
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
		RestAssured.baseURI = "http://localhost"; RestAssured.port = 2666; 
		RestAssured.basePath = "/cxf/e3/prov/"+apiVersion+"/policies";

		RestAssured.rootPath = "response";	
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	private void baseTestCreateUpdateDeletePolicy(String id) throws Exception {
		// CREATE
		Policy policy = new Policy();
		
		policy.setId(id);
		
		Context context = new Context();
		
		context.setId("anid");
		context.setStatus(Status.ACTIVE);
		
		policy.getContexts().add(context);
		
		BasicResponse response = given()
		.contentType("application/xml")
		.body(policy, ObjectMapper.JAXB)
		.expect()
		.statusCode(200)
		.rootPath("response")
		.body("status", equalTo("SUCCESS"))
		.log().ifError()
		.when()
		.post("")
		.andReturn()
		.as(BasicResponse.class, ObjectMapper.JAXB);

		assertNotNull(response);
		assertEquals("SUCCESS", response.getStatus());
		
		if (id != null)
		{
			assertEquals(id, response.getId());
		}
		else
		{
			assertNotNull(response.getId());
			id = response.getId();
		}

		// UPDATE
		context = new Context();
		
		context.setId("anotherid");
		context.setStatus(Status.ACTIVE);
		policy.getContexts().add(context);
		
		response = given()
		.contentType("application/xml")
		.body(policy, ObjectMapper.JAXB)
		.expect()
		.statusCode(200)
		.rootPath("response")
		.body("status", equalTo("SUCCESS"))
		.log().ifError()
		.when()
		.put("/" + id)
		.andReturn()
		.as(BasicResponse.class, ObjectMapper.JAXB);

		assertNotNull(response);
		assertEquals("SUCCESS", response.getStatus());			

		// DELETE
		response = given()
		.contentType("application/xml")
		.body(policy, ObjectMapper.JAXB)
		.expect()
		.statusCode(200)
		.rootPath("response")
		.body("status", equalTo("SUCCESS"))
		.log().ifError()
		.when()
		.delete("/" + id)
		.andReturn()
		.as(BasicResponse.class, ObjectMapper.JAXB);

		assertNotNull(response);
		assertEquals("SUCCESS", response.getStatus());	
	}

	@Test
	public void testCreateUpdateDeletePolicy() throws Exception {
		baseTestCreateUpdateDeletePolicy("12345");
	}
	
	@Test
	public void testCreatePolicyNoID() throws Exception {
		baseTestCreateUpdateDeletePolicy(null);
	}

}
