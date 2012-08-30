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
import static org.hamcrest.CoreMatchers.notNullValue;
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
import com.alu.e3.prov.restapi.model.AuthType;
import com.alu.e3.prov.restapi.model.BasicAuth;
import com.alu.e3.prov.restapi.model.BasicResponse;
import com.alu.e3.prov.restapi.model.Status;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.internal.mapping.ObjectMapping;
import com.jayway.restassured.mapper.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { 
		"classpath:/spring/provisioning.osgi-context-test.xml", 
		"classpath:/spring/provisioning.provision-beans-test.xml", 
		"classpath:/spring/provisioning.rest-declaration-test.xml"
		 })
public class AuthManagerRestApiTest {
	
	private String apiVersion = E3Constant.REST_API_VERSION;

	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		//RestAssured.baseURI = "http://192.168.84.51";RestAssured.port = 8181;

		
				
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
		RestAssured.basePath = "/cxf/e3/prov/"+apiVersion+"/auths";
		//RestAssured.authentication = basic("username", "password");
		RestAssured.rootPath = "response";
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testCreateAuth() throws Exception {

		Auth data = newAuthProvision("usedId");
		
		
		BasicResponse response = given()
		.contentType("application/xml")
		.body(data, ObjectMapper.JAXB)
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
		
	}
	
	@Test
	public void testCreateAndDeleteAuth() throws Exception {
		
		Auth data = newAuthProvision("reusableId");		
		
		// Create step
		BasicResponse response = given()
		.contentType("application/xml")
		.body(data, ObjectMapper.JAXB)
		.expect()
		.statusCode(200)
		.rootPath("response")
		.body("status", equalTo("SUCCESS"))
		.body("apiID", notNullValue())
		.log().ifError()
		.when()
		.post("")
		.andReturn()
		.as(BasicResponse.class, ObjectMapper.JAXB);
		
		
		assertNotNull(response);
		assertEquals("SUCCESS", response.getStatus());
		
		
		// Delete step
		response = given()
		.contentType("application/xml")
		.expect()
		.statusCode(200)
		.rootPath("response")
		.body("status", equalTo("SUCCESS"))
		.log().ifError()
		.when()
		.delete("/" + data.getId())
		.andReturn()
		.as(BasicResponse.class, ObjectMapper.JAXB);

		assertNotNull(response);
		assertEquals("SUCCESS", response.getStatus());
					
	}
	
	@Test
	public void testSecondCreateAndDeleteAuth() throws Exception {
		
		Auth data = newAuthProvision("reusableId");		
		
		// Create step
		BasicResponse response = given()
		.contentType("application/xml")
		.body(data, ObjectMapper.JAXB)
		.expect()
		.statusCode(200)
		.rootPath("response")
		.body("status", equalTo("SUCCESS"))
		.body("apiID", notNullValue())
		.log().ifError()
		.when()
		.post("")
		.andReturn()
		.as(BasicResponse.class, ObjectMapper.JAXB);
		
		
		assertNotNull(response);
		assertEquals("SUCCESS", response.getStatus());
		
		
		// Delete step
		response = given()
		.contentType("application/xml")
		.expect()
		.statusCode(200)
		.rootPath("response")
		.body("status", equalTo("SUCCESS"))
		.log().ifError()
		.when()
		.delete("/" + data.getId())
		.andReturn()
		.as(BasicResponse.class, ObjectMapper.JAXB);

		assertNotNull(response);
		assertEquals("SUCCESS", response.getStatus());
					
	}
	
	@Test
	public void testCreateAndUpdateAuth() throws Exception {
		String id = ""+(System.currentTimeMillis());

		Auth data = newAuthProvision(id);		

		BasicResponse response = given()
		.contentType("application/xml")
		.body(data, ObjectMapper.JAXB)
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
		
				
		data.setStatus(Status.INACTIVE);
		
		
		response = given()
		.contentType("application/xml")
		.body(data, ObjectMapper.JAXB)
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
}
