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
package com.alu.e3.auth.camel;

import org.apache.camel.CamelException;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Before;
import org.junit.Test;

import com.alu.e3.auth.camel.component.AuthComponent;
import com.alu.e3.auth.executor.MockAppKeyExecutorFactory;
import com.alu.e3.auth.executor.MockHttpBasicExecutorFactory;
import com.alu.e3.auth.executor.MockIpWhitelistExecutorFactory;
import com.alu.e3.auth.executor.MockNoAuthExecutorFactory;

/**
 * This class should cover all functionality contained in the AuthEndpoint and AuthProducer classes.
 * All supporting classes are mocked to isolate the functionality.
 * 
 *
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = { "classpath:/META-INF/spring/auth-component-test.xml" })
public class AuthComponentConfigTest {
	
	private AuthComponent component;
	
	@Before
	public void before(){
		component = new AuthComponent(new DefaultCamelContext());
		component.registerExecutorFactory("authKey", new MockAppKeyExecutorFactory());
		component.registerExecutorFactory("basic", new MockHttpBasicExecutorFactory());
		component.registerExecutorFactory("ipList", new MockIpWhitelistExecutorFactory());
		component.registerExecutorFactory("noAuth", new MockNoAuthExecutorFactory());
	}

	/**
	 * This tests the case where HttpBasic auth is specified but no HttpBasicExecutorFactory is provided.
	 * In this case we expect a RuntimeException to be thrown.
	 * @throws Exception
	 */
	@Test(expected=CamelException.class)
	public void testFailNoApiIdConfig() throws Exception{
		component.createEndpoint("blah?basic=true&keyName=appkey&ipList=true");
	}
	
	/**
	 * This tests the case where HttpBasic auth is specified but no HttpBasicExecutorFactory is provided.
	 * In this case we expect a RuntimeException to be thrown.
	 * @throws Exception
	 */
	@Test(expected=RuntimeException.class)
	public void testFailHttpBasicConfig() throws Exception{
		component.unregisterExecutorFactory("basic");
		component.createEndpoint("blah?apiId=1234&basic=true");
	}
	
	/**
	 * This tests the case where AppKey auth is specified but no AppKeyExecutorFactory is provided.
	 * In this case we expect a RuntimeException to be thrown.
	 * @throws Exception
	 */
	@Test(expected=RuntimeException.class)
	public void testFailAppKeyConfig() throws Exception{
		component.unregisterExecutorFactory("authKey");
		component.createEndpoint("blah?apiId=1234&keyName=appkey");
	}
	
	/**
	 * This tests the case where Whitelist auth is specified but no WhitelistExecutorFactory is provided.
	 * In this case we expect a RuntimeException to be thrown.
	 * @throws Exception
	 */
	@Test(expected=RuntimeException.class)
	public void testFailWhitelistConfig() throws Exception{
		component.unregisterExecutorFactory("ipList");
		component.createEndpoint("blah?apiId=1234&ipList=true");
	}
	
	
	/**
	 * This tests the case where Whitelist auth is specified but no WhitelistExecutorFactory is provided.
	 * In this case we expect a RuntimeException to be thrown.
	 * @throws Exception
	 */
	@Test(expected=RuntimeException.class)
	public void testFailNoAuthConfig() throws Exception{
		component.unregisterExecutorFactory("noAuth");
		component.createEndpoint("blah?apiId=1234&noAuth=true");
	}
}
