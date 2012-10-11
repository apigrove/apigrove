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
package com.alu.e3.auth.access.file;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import com.alu.e3.common.tools.CanonicalizedIpAddress;
import com.alu.e3.data.model.Api;

public class FileDataTest {
	
	/* members */
	private FileData fileData = null;
	private static final String DB_PATH = "/TextData.db";;

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test1() throws Exception {
		Api api = new Api();
		api.setId("test");

		fileData = new FileData("/notAValidFile.db");
		assertNotNull(fileData);
		assertNull(fileData.checkAllowed(api, "test").getAuthIdentity());
	}
	
	@Test
	public void testIsAssociated() throws Exception {
		
		Api api = new Api();
		api.setId("api1234");

		URL fileURL = getClass().getResource(DB_PATH);
		
		fileData = new FileData(fileURL.getPath());
		
		assertNotNull(fileData.checkAllowed(api, "appKey1234").getAuthIdentity());
		assertNotNull(fileData.checkAllowed(api, "user", "pass").getAuthIdentity());
		assertNotNull(fileData.checkAllowed(api, new CanonicalizedIpAddress("127.0.0.1")).getAuthIdentity());
		assertNotNull(fileData.checkAllowed(api).getAuthIdentity());
		
		api.setId("apiASDF");
		
		assertNull(fileData.checkAllowed(api, "badbad").getAuthIdentity());
		assertNull(fileData.checkAllowed(api, "bad", "bad").getAuthIdentity());
		assertNull(fileData.checkAllowed(api, new CanonicalizedIpAddress("10.0.0.1")).getAuthIdentity());
		assertNull(fileData.checkAllowed(api, "badbad", "badbad").getAuthIdentity());
		assertNull(fileData.checkAllowed(api).getAuthIdentity());
	}
}
