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
import junit.framework.TestCase;

import org.apache.cxf.common.util.Base64Utility;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alu.e3.common.E3Constant;
import com.jayway.restassured.RestAssured;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/provisioning.osgi-context-test.xml", "classpath:/spring/provisioning.provision-beans-test.xml",
		"classpath:/spring/provisioning.rest-declaration-test.xml" })
public class SecuredRestApiTest extends TestCase {

	private String apiVersion = E3Constant.REST_API_VERSION;

	private String username = "changeit";
	private String password = "changeit";

	private String basicAuthCredentials = null;

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

		// Security.setProperty("keystore.type", "jks");
		// RestAssured.keystore(resources.getJksTrustStore().getFile(),
		// truststorePassword);

		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 2667;
		RestAssured.basePath = "/cxf/e3/prov/" + apiVersion + "/apis";

		basicAuthCredentials = Base64Utility.encode((username + ":" + password).getBytes());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRestassured() throws Exception {
		try {
			given().header("Authorization", "Basic " + basicAuthCredentials).get("/");
		} finally {
			RestAssured.reset();
		}
	}

	@Test
	public void testRestassured_Failure() throws Exception {
		try {

			given().header("Authorization", "Basic wrongBasicAuthCredentials").expect().statusCode(401).get("/");
		} 
		catch (Exception e) {
			
		}
		finally {
			RestAssured.reset();
		}
	}

}
