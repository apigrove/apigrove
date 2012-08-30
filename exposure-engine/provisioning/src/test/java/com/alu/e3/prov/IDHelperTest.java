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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.alu.e3.prov.lifecycle.IDHelper;

public class IDHelperTest {

	@Test
	public void testExtractApiIdFromFileName() {
		String fileName1 = "41504969443a31363634-41504969443a31363634.xml";
		
		String apiId1 = IDHelper.extractApiIdFromFileName(fileName1);
		assertNotNull("Extracted apiId must be not null", apiId1);
		assertTrue("apiId must be 'APIiD:1664'", "APIiD:1664".equals(apiId1));
		
		String fileName2 = "41504969443a31363634-41504969443a31363634.jar";
		
		String apiId2 = IDHelper.extractApiIdFromFileName(fileName2);
		assertNotNull("Extracted apiId must be not null", apiId1);
		assertTrue("apiId must be 'APIiD:1664'", "APIiD:1664".equals(apiId2));
		
	}

	@Test
	public void testExtractAllFromFileName() {
		String fileName1 = "41504969443a31363634-41504969443a31363634.xml";
		
		String[] datas1 = IDHelper.extractAllFromFileName(fileName1);
		assertNotNull("Extracted datas must be not null", datas1);
		assertTrue("Extracted datas must contains 3 value", datas1.length == 3);
		assertTrue("datas[0] must be '41504969443a31363634'", "41504969443a31363634".equals(datas1[0]));
		assertTrue("datas[1] must be 'APIiD:1664'", "APIiD:1664".equals(datas1[1]));
		assertTrue("datas[2] must be '41504969443a31363634'", "41504969443a31363634".equals(datas1[2]));
	}

	@Test
	public void testGenerateUID() {
		String uid1 = IDHelper.generateUID();
		assertNotNull("Generated UID must not be null", uid1);
		
		String uid2 = IDHelper.generateUID();
		assertNotNull("Generated UID must not be null", uid1);
		
		assertFalse("Two generated UID must be different", uid1.equals(uid2));
	}

	@Test
	public void testEncode() {
		String uid = "APIiD:1664";
		String uidEncoded = IDHelper.encode(uid);
		
		assertTrue("Encoded UID must be '41504969443a31363634'", "41504969443a31363634".equals(uidEncoded));
	}

	@Test
	public void testDecode() {
		String uidEncoded = "41504969443a31363634";
		String uid = IDHelper.decode(uidEncoded);
		
		assertTrue("Decoded UID must be 'APIiD:1664'", "APIiD:1664".equals(uid));
	}

}
