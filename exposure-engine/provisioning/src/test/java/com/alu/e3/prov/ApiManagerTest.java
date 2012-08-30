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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alu.e3.common.E3Constant;
import com.alu.e3.prov.restapi.model.Api;
import com.alu.e3.prov.restapi.model.ApiContext;
import com.alu.e3.prov.restapi.model.ApiType;
import com.alu.e3.prov.restapi.model.AuthType;
import com.alu.e3.prov.restapi.model.Authentication;
import com.alu.e3.prov.restapi.model.Authkey;
import com.alu.e3.prov.restapi.model.BasicResponse;
import com.alu.e3.prov.restapi.model.Data;
import com.alu.e3.prov.restapi.model.DynamicTdr;
import com.alu.e3.prov.restapi.model.HTTPSType;
import com.alu.e3.prov.restapi.model.Key;
import com.alu.e3.prov.restapi.model.NotificationFormat;
import com.alu.e3.prov.restapi.model.ProvisionAuthentication;
import com.alu.e3.prov.restapi.model.StaticTdr;
import com.alu.e3.prov.restapi.model.Status;
import com.alu.e3.prov.restapi.model.SubscriptionStep;
import com.alu.e3.prov.restapi.model.TargetHost;
import com.alu.e3.prov.restapi.model.TdrData;
import com.alu.e3.prov.restapi.model.TdrEnabled;
import com.alu.e3.prov.restapi.model.TdrType;
import com.alu.e3.prov.restapi.model.Validation;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.mapper.ObjectMapper;
import com.jayway.restassured.response.Response;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { 
		"classpath:/spring/provisioning.osgi-context-test.xml", 
		"classpath:/spring/provisioning.provision-beans-test.xml", 
		"classpath:/spring/provisioning.rest-declaration-test.xml"
})

public class ApiManagerTest {
	private String apiVersion = E3Constant.REST_API_VERSION;


	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//RestAssured.baseURI = "http://192.168.84.51";RestAssured.port = 8181;

		//A little bit of cleanup
		File archiveDir = new File("target/PROVISIONING_WORKING_DIR/DEPLOYED");
		if (archiveDir.exists()) {
			for(File toDelete : archiveDir.listFiles())
				toDelete.delete();
		}

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
		RestAssured.basePath = "/cxf/e3/prov/"+apiVersion+"/apis";
		//RestAssured.authentication = basic("username", "password");
		RestAssured.rootPath = "response";
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {		
	}	

	/**
	 * Strangly does not succeed if this test is put at bottom 
	 * of this test suite.
	 * TODO: Need to be check why.
	 */
	@Test
	public void testCreateAndGetAllApi() {
		// GetAll step
		BasicResponse response = given()
				.contentType("application/xml")
				.expect()
				.statusCode(200)
				.rootPath("response")
				.body("status", equalTo("SUCCESS"))

				.log().ifError()
				.when()
				.get("/")
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getIds());
		for(String idToDelete : response.getIds()) {
			// Delete step
			BasicResponse deleteResponse = given()
					.contentType("application/xml")
					.expect()
					.statusCode(200)
					.rootPath("response")
					.body("status", equalTo("SUCCESS"))

					.log().ifError()
					.when()
					.delete("/" + idToDelete)
					.andReturn()
					.as(BasicResponse.class, ObjectMapper.JAXB);

			Assert.assertNotNull(deleteResponse);
			Assert.assertEquals("SUCCESS", deleteResponse.getStatus());
		}

		List<String> createdApis = new ArrayList<String>();

		Api data;
		for(int i=0; i<2; i++) {
			data = newApi();
			String apiID = ""+(new Random().nextLong());
			createdApis.add(apiID);
			data.setId(apiID);

			// Create step
			BasicResponse createResponse = given()
					.contentType("application/xml")
					.body(data, ObjectMapper.JAXB)
					.expect()
					.statusCode(200)
					.rootPath("response")
					.body("status", equalTo("SUCCESS"))
					.body("id", notNullValue())
					.log().ifError()
					.when()
					.post("")
					.andReturn()
					.as(BasicResponse.class, ObjectMapper.JAXB);

			String apiIDReturned = createResponse.getId();

			Assert.assertNotNull(createResponse);
			Assert.assertNotNull(apiIDReturned);
			Assert.assertEquals(apiID, apiIDReturned);

			Assert.assertEquals("SUCCESS", createResponse.getStatus());
		}


		// GetAll step
		BasicResponse getAllResponse = given()
				.contentType("application/xml")
				.expect()
				.statusCode(200)
				.rootPath("response")
				.body("status", equalTo("SUCCESS"))

				.log().ifError()
				.when()
				.get("/")
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		Assert.assertNotNull(getAllResponse);
		Assert.assertNotNull(getAllResponse.getIds());
		// sometimes other junits create apis 
		Assert.assertTrue(createdApis.size() <= getAllResponse.getIds().size());
		for(int i=0; i<createdApis.size(); i++) {
			String idReturned = createdApis.get(i);
			Assert.assertTrue(getAllResponse.getIds().contains(idReturned));
		}
	}

	@Test
	public void testCreateWithNoApiID() throws Exception {
		Api data = newApi();

		BasicResponse response = given()
				.contentType("application/xml")
				.body(data, ObjectMapper.JAXB)
				.expect()
				.statusCode(200)
				.rootPath("response")
				.body("status", equalTo("SUCCESS"))
				.body("id", notNullValue())
				.log().ifError()
				.when()
				.post("")
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getId());
		Assert.assertEquals("SUCCESS", response.getStatus());

	}

	@Test
	public void testCreateWithApiID() throws Exception {

		String apiID = ""+(new Random().nextLong());
		Api data = newApi();
		data.setEndpoint(new Random().nextLong()+"");
		data.setId(apiID);

		BasicResponse response = given()
				.contentType("application/xml")
				.body(data, ObjectMapper.JAXB)
				.expect()
				.statusCode(200)
				.rootPath("response")
				.body("status", equalTo("SUCCESS"))
				.body("id", notNullValue())
				.body("id", equalTo(apiID))
				.log().ifError()
				.when()
				.post("")
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getId());
		Assert.assertEquals(apiID, response.getId());
		Assert.assertEquals("SUCCESS", response.getStatus());

	}

	@Test
	public void testCreateWithNoApiIDAndUpdate() throws Exception {
		Api data = newApi();
		data.setId(null);

		BasicResponse response = given()
				.contentType("application/xml")
				.body(data, ObjectMapper.JAXB)
				.expect()
				.statusCode(200)
				.rootPath("response")
				.body("status", equalTo("SUCCESS"))
				.body("id", notNullValue())
				.log().ifError()
				.when()
				.post("")
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getId());
		Assert.assertEquals("SUCCESS", response.getStatus());

		// do an update for this newly created API
		String createdApiId = response.getId();

		data.setId(createdApiId);
		data.setVersion("2.0");
		data.setEndpoint("newEndpointURL");


		response = given()
				.contentType("application/xml")
				.body(data, ObjectMapper.JAXB)
				.expect()
				.statusCode(200)
				.rootPath("response")
				.body("status", equalTo("SUCCESS"))
				.body("id", notNullValue())
				.body("id", equalTo(createdApiId))
				.log().ifError()
				.when()
				.put("/" + createdApiId)
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getId());
		Assert.assertEquals("SUCCESS", response.getStatus());
		Assert.assertEquals(createdApiId, response.getId());


	}

	@Test
	public void testCreateWithDuplicateEndpoint() throws Exception {
		String apiID = ""+(new Random().nextLong());

		Api data = newApi();
		data.setId(apiID);

		BasicResponse response = given()
				.contentType("application/xml")
				.body(data, ObjectMapper.JAXB)
				.expect()
				.statusCode(200)
				.rootPath("response")
				.body("status", equalTo("SUCCESS"))
				.body("id", notNullValue())
				.log().ifError()
				.when()
				.post("")
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getId());
		Assert.assertEquals(apiID, response.getId());

		Assert.assertEquals("SUCCESS", response.getStatus());

		// Add another with the same endpoint

		data.setId(new Random().nextLong()+"");

		response = given()
				.contentType("application/xml")
				.body(data, ObjectMapper.JAXB)
				.expect()
				.statusCode(500)
				.rootPath("response")
				.body("status", equalTo("FAILURE"))
				.log().ifError()
				.when()
				.post("")
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		Assert.assertNotNull(response);
	}

	/**
	 * This tests the case that we add, delete and then re-add
	 * to make sure it succeeds
	 * @throws Exception
	 */
	@Test
	public void testCreateDeleteCreteDuplicateEndpoint() throws Exception {
		String apiID = ""+(new Random().nextLong());

		Api data = newApi();
		data.setId(apiID);

		BasicResponse response = given()
				.contentType("application/xml")
				.body(data, ObjectMapper.JAXB)
				.expect()
				.statusCode(200)
				.rootPath("response")
				.body("status", equalTo("SUCCESS"))
				.body("id", notNullValue())
				.log().ifError()
				.when()
				.post("")
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getId());
		Assert.assertEquals(apiID, response.getId());

		Assert.assertEquals("SUCCESS", response.getStatus());

		// Add another with the same endpoint

		response = given()
				.contentType("application/xml")
				.body(data, ObjectMapper.JAXB)
				.expect()
				.statusCode(200)
				.rootPath("response")
				.body("status", equalTo("SUCCESS"))
				.log().ifError()
				.when()
				.delete("/"+apiID)
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		Assert.assertNotNull(response);

		response = given()
				.contentType("application/xml")
				.body(data, ObjectMapper.JAXB)
				.expect()
				.statusCode(200)
				.rootPath("response")
				.body("status", equalTo("SUCCESS"))
				.body("id", notNullValue())
				.log().ifError()
				.when()
				.post("")
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getId());
		Assert.assertEquals(apiID, response.getId());

		Assert.assertEquals("SUCCESS", response.getStatus());

	}

	@Test
	public void testUpdateWithDuplicateEndpoint() throws Exception {
		String apiID = ""+(new Random().nextLong());

		Api data = newApi();
		data.setId(apiID);
		String origEndpoint = data.getEndpoint();

		BasicResponse response = given()
				.contentType("application/xml")
				.body(data, ObjectMapper.JAXB)
				.expect()
				.statusCode(200)
				.rootPath("response")
				.body("status", equalTo("SUCCESS"))
				.body("id", notNullValue())
				.log().ifError()
				.when()
				.post("")
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getId());
		Assert.assertEquals(apiID, response.getId());

		Assert.assertEquals("SUCCESS", response.getStatus());

		// Add another with a different endpoint

		apiID = new Random().nextLong()+"";
		data.setId(apiID);
		data.setEndpoint(new Random().nextLong()+"");

		response = given()
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

		Assert.assertNotNull(response);

		data.setEndpoint(origEndpoint);
		response = given()
				.contentType("application/xml")
				.body(data, ObjectMapper.JAXB)
				.expect()
				.statusCode(500)
				.rootPath("response")
				.body("status", equalTo("FAILURE"))
				.log().ifError()
				.when()
				.put("/" + apiID)
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		Assert.assertNotNull(response);

	}

	/**
	 * This tests the case where we update an api with an endpoint that
	 * used to belong an API that has been deleted and see that it succeeds
	 * @throws Exception
	 */
	@Test
	public void testUpdateWithDeletedDuplicateEndpoint() throws Exception {
		String apiID = ""+(new Random().nextLong());

		Api data = newApi();
		data.setId(apiID);
		String origEndpoint = data.getEndpoint();

		BasicResponse response = given()
				.contentType("application/xml")
				.body(data, ObjectMapper.JAXB)
				.expect()
				.statusCode(200)
				.rootPath("response")
				.body("status", equalTo("SUCCESS"))
				.body("id", notNullValue())
				.log().ifError()
				.when()
				.post("")
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getId());
		Assert.assertEquals(apiID, response.getId());

		Assert.assertEquals("SUCCESS", response.getStatus());

		// Delete
		response = given()
				.contentType("application/xml")
				.body(data, ObjectMapper.JAXB)
				.expect()
				.statusCode(200)
				.rootPath("response")
				.body("status", equalTo("SUCCESS"))
				.log().ifError()
				.when()
				.delete("/"+apiID)
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		Assert.assertNotNull(response);

		// Add another with a different endpoint

		apiID = new Random().nextLong()+"";
		data.setId(apiID);
		data.setEndpoint(new Random().nextLong()+"");

		response = given()
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

		Assert.assertNotNull(response);

		data.setEndpoint(origEndpoint);
		response = given()
				.contentType("application/xml")
				.body(data, ObjectMapper.JAXB)
				.expect()
				.statusCode(200)
				.rootPath("response")
				.body("status", equalTo("SUCCESS"))
				.log().ifError()
				.when()
				.put("/" + apiID)
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		Assert.assertNotNull(response);

	}

	@Test
	public void testCreateWithApiIDAndUpdate() throws Exception {
		String apiID = ""+(new Random().nextLong());

		Api data = newApi();
		data.setId(apiID);

		BasicResponse response = given()
				.contentType("application/xml")
				.body(data, ObjectMapper.JAXB)
				.expect()
				.statusCode(200)
				.rootPath("response")
				.body("status", equalTo("SUCCESS"))
				.body("id", notNullValue())
				.log().ifError()
				.when()
				.post("")
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getId());
		Assert.assertEquals(apiID, response.getId());

		Assert.assertEquals("SUCCESS", response.getStatus());

		// do an update for this API ID

		data.setId(apiID);
		data.setVersion("2.0");

		response = given()
				.contentType("application/xml")
				.body(data, ObjectMapper.JAXB)
				.expect()
				.statusCode(200)
				.rootPath("response")
				.body("status", equalTo("SUCCESS"))
				.body("id", notNullValue())
				.body("id", equalTo(apiID))
				.log().ifError()
				.when()
				.put("/" + apiID)
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getId());
		Assert.assertEquals("SUCCESS", response.getStatus());
		Assert.assertEquals(apiID, response.getId());


	}

	@Test
	public void testCreateAndDelete() throws Exception {

		Api data = newApi();
		String apiID = ""+(new Random().nextLong());
		data.setId(apiID);

		// Create step
		BasicResponse response = given()
				.contentType("application/xml")
				.body(data, ObjectMapper.JAXB)
				.expect()
				.statusCode(200)
				.rootPath("response")
				.body("status", equalTo("SUCCESS"))
				.body("id", notNullValue())
				.log().ifError()
				.when()
				.post("")
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		String apiIDReturned = response.getId();

		Assert.assertNotNull(response);
		Assert.assertNotNull(apiIDReturned);
		Assert.assertEquals(apiID, apiIDReturned);

		Assert.assertEquals("SUCCESS", response.getStatus());


		// Delete step
		response = given()
				.contentType("application/xml")
				.expect()
				.statusCode(200)
				.rootPath("response")
				.body("status", equalTo("SUCCESS"))

				.log().ifError()
				.when()
				.delete("/" + apiID)
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		Assert.assertNotNull(response);
		Assert.assertEquals("SUCCESS", response.getStatus());

	}

	@Test
	@Ignore
	public void testDeleteUnknownApiID() throws Exception {
		String apiID = ""+System.currentTimeMillis();

		// Delete step
		BasicResponse response = given()
				.contentType("application/xml")
				.expect()
				.statusCode(500)
				.header("X-Application-Error-Code", equalTo("500"))
				.rootPath("response")
				.body("status", equalTo("FAILURE"))
				//.log().ifError()
				.when()
				.delete("/" + apiID)
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		Assert.assertNotNull(response);
		Assert.assertEquals("FAILURE", response.getStatus());

	}


	private Api newApi() {

		Api api = new Api();


		ApiContext env = new ApiContext();
		env.setDefaultContext(true);
		env.setId("test");
		api.getContexts().add(env);

		env.setStatus(Status.ACTIVE);
		/*env.setMaxRateLimitTPMThreshold(1);
		env.setMaxRateLimitTPMWarning(1);
		env.setMaxRateLimitTPSThreshold(1);
		env.setMaxRateLimitTPSWarning(1);*/

		api.setId("getLocation" + (new Random().nextLong()));
		api.setDisplayName("test");
		api.setType(ApiType.PASS_THROUGH);
		api.setVersion("1.0");
		api.setEndpoint(new Random().nextLong()+"");

		api.setStatus(Status.ACTIVE);


		ProvisionAuthentication pauth = new ProvisionAuthentication();
		Authkey authKey = new Authkey(); authKey.setKeyName("key");
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

	@Test
	public void testCreateAndGetApi() {
		Api data = newApi();
		String apiID = ""+(new Random().nextLong());
		data.setId(apiID);

		// Create step
		BasicResponse response = given()
				.contentType("application/xml")
				.body(data, ObjectMapper.JAXB)
				.expect()
				.statusCode(200)
				.rootPath("response")
				.body("status", equalTo("SUCCESS"))
				.body("id", notNullValue())
				.log().ifError()
				.when()
				.post("")
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		String apiIDReturned = response.getId();

		Assert.assertNotNull(response);
		Assert.assertNotNull(apiIDReturned);
		Assert.assertEquals(apiID, apiIDReturned);

		Assert.assertEquals("SUCCESS", response.getStatus());


		// Delete step
		@SuppressWarnings("unused")
		Response apiResponse = given()
		.contentType("application/xml")
		.expect()
		.statusCode(200)
		.rootPath("response")
		.body("status", equalTo("SUCCESS"))
		.body("api", notNullValue())
		.body("api.id", notNullValue())
		.body("api.id", equalTo(apiID))

		.log().ifError()
		.when()
		.get("/" + apiID)
		.andReturn();
		//.as(ApiResponse.class, ObjectMapper.JAXB);

		// RestAssured does not want to cast the response to an ApiResponse type
		//ApiResponse apiReponseCasted = apiResponse.as(ApiResponse.class);
		//System.out.println(apiReponseCasted);

		//		Assert.assertNotNull(apiResponse);
		//		Assert.assertNotNull(apiResponse.getId());
		//		Assert.assertEquals(apiID, apiResponse.getId());
		//		Assert.assertEquals("SUCCESS", apiResponse.getStatus());
		//		Assert.assertNotNull(apiResponse.getApi());
		//		Assert.assertNotNull(apiResponse.getApi().getAuthentication());
		//		Assert.assertNotNull(apiResponse.getApi().getContexts());
		//		Assert.assertNotNull(apiResponse.getApi().getTdr());
		//		Assert.assertNotNull(apiResponse.getApi().getTdrEnabled());
		//		Assert.assertNotNull(apiResponse.getApi().getType());
	}

	@Test
	public void testCreateMultipleDefaultContexts() throws Exception {
		Api data = newApi();
		ApiContext anotherDefaultContext = new ApiContext();
		anotherDefaultContext.setId("test_anotherDefaultContext");
		anotherDefaultContext.setDefaultContext(true);
		data.getContexts().add(anotherDefaultContext);


		BasicResponse response = given()
				.contentType("application/xml")
				.body(data, ObjectMapper.JAXB)
				.expect()
				.statusCode(500)
				.rootPath("response")
				.body("status", equalTo("FAILURE"))
				.log().ifError()
				.when()
				.post("")
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		Assert.assertNotNull(response);
		Assert.assertEquals("FAILURE", response.getStatus());
	}

	@Test
	public void testCreateAndGetApiXmlValidation() {
		Api data = newApi();		

		Validation val = new Validation();
		data.setValidation(val);
		val.setXml(new Validation.Xml());
		//val.setSoap(new Validation.Soap(SoapVersionEnum.SOAP11));		
		//val.setSchema(new Validation.Schema(SchemaValidationEnum.WSDL, "hjhjsdhjsdhjsdh48647"));

		String apiID = ""+(new Random().nextLong());
		data.setId(apiID);

		// Create step
		BasicResponse response = given()
				.contentType("application/xml")
				.body(data, ObjectMapper.JAXB)
				.expect()
				.statusCode(200)
				.rootPath("response")
				.body("status", equalTo("SUCCESS"))
				.body("id", notNullValue())
				.log().ifError()
				.when()
				.post("")
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		String apiIDReturned = response.getId();

		Assert.assertNotNull(response);
		Assert.assertNotNull(apiIDReturned);
		Assert.assertEquals(apiID, apiIDReturned);

		Assert.assertEquals("SUCCESS", response.getStatus());


		// Delete step
		BasicResponse apiResponse = given()
				.contentType("application/xml")
				.expect()
				.statusCode(200)
				.rootPath("response")
				.body("status", equalTo("SUCCESS"))
				.body("api", notNullValue())
				.body("api.id", notNullValue())
				.body("api.id", equalTo(apiID))

				.log().ifError()
				.when()
				.get("/" + apiID)
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		// RestAssured does not want to cast the response to an ApiResponse type
		//ApiResponse apiReponseCasted = apiResponse.as(ApiResponse.class);
		System.out.println(apiResponse);


	}

	@Test
	public void testCreateSubscriptionApi() {
		Api newApi = newApi();

		String apiID = "Subscription"+(new Random().nextLong());
		newApi.setId(apiID);

		newApi.setNotificationFormat(NotificationFormat.HEADER);

		// Testing we can't have NotificationFormat tag on Non Notification type APIs
		BasicResponse response = given()
				.contentType("application/xml")
				.body(newApi, ObjectMapper.JAXB)
				.expect()
				.statusCode(500)
				//.rootPath("response")
				//.body("status", equalTo("SUCCESS"))
				//.body("id", notNullValue())
				.log().ifError()
				.when()
				.post("")
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		Assert.assertNotNull("No error message", response.getError());
		Assert.assertNotNull("No error message", response.getError().getErrorText());
		Assert.assertTrue("Wrong errorMsg value", response.getError().getErrorText().contains("can't have NotificationFormat"));

		// Testing Wrong : Subscription + Notification Format
		newApi.setSubscriptionStep(SubscriptionStep.SUBSCRIPTION);
		response = given()
				.contentType("application/xml")
				.body(newApi, ObjectMapper.JAXB)
				.expect()
				.statusCode(500)
				//.rootPath("response")
				//.body("status", equalTo("SUCCESS"))
				//.body("id", notNullValue())
				.log().ifError()
				.when()
				.post("")
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		Assert.assertNotNull("No error message", response.getError());
		Assert.assertNotNull("No error message", response.getError().getErrorText());
		Assert.assertTrue("Wrong errorMsg value", response.getError().getErrorText().contains("can't have a NotificationFormat in Subscription step mode"));

		// Testing Wrong : Notification + NotificationFormat + Target hosts
		newApi.setSubscriptionStep(SubscriptionStep.NOTIFICATION);

		response = given()
				.contentType("application/xml")
				.body(newApi, ObjectMapper.JAXB)
				.expect()
				.statusCode(500)
				//.rootPath("response")
				//.body("status", equalTo("SUCCESS"))
				//.body("id", notNullValue())
				.log().ifError()
				.when()
				.post("")
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		Assert.assertNotNull("No error message", response.getError());
		Assert.assertNotNull("No error message", response.getError().getErrorText());
		Assert.assertTrue("Wrong errorMsg value", response.getError().getErrorText().contains("Notification step can't have any target host"));

		// Testing Wrong : Notification - NotificationFormat - TargetHost
		for (ApiContext apiCtx : newApi.getContexts())
			apiCtx.setTargetHosts(null);

		newApi.setNotificationFormat(null);

		response = given()
				.contentType("application/xml")
				.body(newApi, ObjectMapper.JAXB)
				.expect()
				.statusCode(500)
				//.rootPath("response")
				//.body("status", equalTo("SUCCESS"))
				//.body("id", notNullValue())
				.log().ifError()
				.when()
				.post("")
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);

		Assert.assertNotNull("No error message", response.getError());
		Assert.assertNotNull("No error message", response.getError().getErrorText());
		Assert.assertTrue("Wrong errorMsg value", response.getError().getErrorText().contains("Notification must have a NotificationFormat"));

		// SUCCESS : Notification + NotificationFormat - TargetHost
		newApi.setNotificationFormat(NotificationFormat.HEADER);
		response = given()
				.contentType("application/xml")
				.body(newApi, ObjectMapper.JAXB)
				.expect()
				.statusCode(200)
				.rootPath("response")
				.body("status", equalTo("SUCCESS"))
				.body("id", notNullValue())
				.body("id", equalTo(newApi.getId()))
				.log().ifError()
				.when()
				.post("")
				.andReturn()
				.as(BasicResponse.class, ObjectMapper.JAXB);
	}
}
