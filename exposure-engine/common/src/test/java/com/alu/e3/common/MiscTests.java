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
package com.alu.e3.common;

import static org.hamcrest.CoreMatchers.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alu.e3.common.tools.CommonTools;
import com.alu.e3.data.model.CallDescriptor;
import com.alu.e3.data.model.Policy;

import static org.junit.Assert.*;

public class MiscTests {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSplitUrl() {
		validateUrl("http://www.apple.com/test.html", "http://www.apple.com/test.html", null);
		validateUrl("http://www.apple.com/path/test.html", "http://www.apple.com/path/test.html", null);
		validateUrl("http://www.apple.com/test.html?key=value&key2=value2", "http://www.apple.com/test.html", "key=value&key2=value2");
		validateUrl("http://www.apple.com/path/test/test.html?key=value&key2=value2", "http://www.apple.com/path/test/test.html", "key=value&key2=value2");
	}
	
	private void validateUrl(String url, String part1, String part2) {
		String[] parts = CommonTools.splitUrl(url);
		
		assertEquals(part1, parts[0]);
		assertEquals(part2, parts[1]);
	}
	
	@Test
	public void testCallDescriptorsEquals() {
		Policy p1 = new Policy();
		p1.setId("abcd");

		
		// same cd
		CallDescriptor cd_p1 = new CallDescriptor(p1, -1, -1);
		assertThat(cd_p1, is(cd_p1));
		
		// different cd with same non-null policy
		CallDescriptor cd2_p1 = new CallDescriptor(p1, -1, -1);
		assertThat(cd2_p1, is(cd_p1));
		
		// same non-null policy, different bucket id
		CallDescriptor cd_p1_bucket = new CallDescriptor(p1, 0, -1);
		assertThat(cd_p1_bucket, is(not(cd_p1)));
		
		// same non-null policy, different context id
		CallDescriptor cd_p1_context = new CallDescriptor(p1, -1, 0);
		assertThat(cd_p1_context, is(not(cd_p1)));
		
		// same non-null policy, different bucket & bucket id
		CallDescriptor cd_p1_ctx_buck = new CallDescriptor(p1, 0, 0);
		assertThat(cd_p1, is(not(cd_p1_ctx_buck)));
		
		// comparing with non null policy but null policy id
		Policy p_idn = new Policy();
		CallDescriptor cd_pn_idn = new CallDescriptor(p_idn, -1, -1);
		assertThat(cd_p1, is(not(cd_pn_idn)));
		
		// comparing with non null policy but null policy id
		CallDescriptor cd_pn_idn_bucket = new CallDescriptor(p_idn, 0, -1);
		assertThat(cd_p1, is(not(cd_pn_idn_bucket)));
		
		// comparing with non null policy but null policy id
		CallDescriptor cd_pn_idn_context = new CallDescriptor(p_idn, -1, 0);
		assertThat(cd_p1, is(not(cd_pn_idn_context)));

		// comparing with null policy
		CallDescriptor cd_pn = new CallDescriptor(null, -1, -1);
		assertThat(cd_p1, is(not(cd_pn)));
		assertThat(cd_pn, is(not(cd_p1)));
		
		// comparing 2 null policies
		CallDescriptor cd2_pn = new CallDescriptor(null, -1, -1);
		assertThat(cd_pn, is(cd2_pn));

		
		// comparing with different policy but same policy id
		Policy p2 = new Policy();
		p2.setId("abcd");
		
		// different policy with same id
		CallDescriptor cd2_p2 = new CallDescriptor(p2, -1, -1);
		assertThat(cd2_p2, is(cd_p1));
		
		// different policy with same id + different bucket id
		CallDescriptor cd2_p2_bucket = new CallDescriptor(p1, 0, -1);
		assertThat(cd2_p2_bucket, is(not(cd2_p2)));
		
		// different policy with same id + different context id
		CallDescriptor cd2_p2_context = new CallDescriptor(p1, -1, 0);
		assertThat(cd2_p2_context, is(not(cd2_p2)));
		
		// different policy with same id + different context & bucket id
		CallDescriptor cd2_p2_ctx_buck = new CallDescriptor(p1, 0, 0);
		assertThat(cd2_p2_ctx_buck, is(not(cd2_p2)));

	}
}
