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
package com.alu.e3.gateway.camel.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.junit.Before;
import org.junit.Test;

import com.alu.e3.common.camel.AuthIdentity;
import com.alu.e3.common.camel.ExchangeConstantKeys;
import com.alu.e3.data.model.Api;
import com.alu.e3.data.model.Auth;
import com.alu.e3.data.model.Policy;
import com.alu.e3.data.model.enumeration.HeaderTransformationAction;
import com.alu.e3.data.model.enumeration.HeaderTransformationType;
import com.alu.e3.data.model.sub.HeaderTransformation;
import com.alu.e3.gateway.common.camel.processor.HeaderTransRequestProcessor;
import com.alu.e3.gateway.common.camel.processor.HeaderTransResponseProcessor;

public class HeaderTransProcessorsTest {

	Exchange exchange;
	Auth auth;
	Api api;
	Policy policy1;
	Processor requestProcessor;
	Processor responseProcessor;

	/**
	 * Basic test to make sure that all types of header transformations are applied
	 * correctly from an Api.
	 * @throws Exception
	 */
	@Test
	public void testHeaderTransApi() throws Exception{
		api.setHeaderTransformation(getBasicTrans());

		testPrerequisites();
		requestProcessor.process(exchange);
		testPostRequestProcessor();
		responseProcessor.process(exchange);
		testPostResponseProcessor();
	}

	@Test
	public void testHeaderTransAuth() throws Exception{
		auth.setHeaderTransformation(getBasicTrans());

		testPrerequisites();
		requestProcessor.process(exchange);
		testPostRequestProcessor();
		responseProcessor.process(exchange);
		testPostResponseProcessor();
	}

	@Test
	public void testHeaderTransPolicy() throws Exception{
		policy1.setHeaderTransformation(getBasicTrans());

		testPrerequisites();
		requestProcessor.process(exchange);
		testPostRequestProcessor();
		responseProcessor.process(exchange);
		testPostResponseProcessor();
	}


	/**
	 * Helper method
	 */
	private void testPostResponseProcessor() {
		assertNotNull(exchange.getIn().getHeader("HEADER-VALUE"));
		assertEquals("RESPONSE-VALUE", exchange.getIn().getHeader("HEADER-VALUE"));
		assertNotNull(exchange.getIn().getHeader("HEADER-PROP"));
		assertEquals("5678", exchange.getIn().getHeader("HEADER-PROP"));

		// REMOVE1 and REMOVE2 should be gone
		assertNull(exchange.getIn().getHeader("HEADER-REMOVE1"));
		assertNull(exchange.getIn().getHeader("HEADER-REMOVE2"));

		// Should always be null
		assertNull(exchange.getIn().getHeader("HEADER-PROP-NULL"));
		assertNotNull(exchange.getIn().getHeader("HEADER-PREEXIST"));
		assertEquals("BLARG", exchange.getIn().getHeader("HEADER-PREEXIST"));

		assertNull(exchange.getIn().getHeader("HEADER-EMPTY"));
		assertNull(exchange.getIn().getHeader("HEADER-EMPTY2"));

		assertNull(exchange.getIn().getHeader("HEADER-REMOVE-NULL"));
		assertNull(exchange.getIn().getHeader("HEADER-REMOVE-NULL2"));
	}


	/**
	 * Helper method
	 */
	private void testPostRequestProcessor() {
		assertNotNull(exchange.getIn().getHeader("HEADER-VALUE"));
		assertEquals("REQUEST-VALUE", exchange.getIn().getHeader("HEADER-VALUE"));
		assertNotNull(exchange.getIn().getHeader("HEADER-PROP"));
		assertEquals("1234", exchange.getIn().getHeader("HEADER-PROP"));

		// REMOVE1 should be gone but REMOVE2 should still be there
		assertNull(exchange.getIn().getHeader("HEADER-REMOVE1"));
		assertNotNull(exchange.getIn().getHeader("HEADER-REMOVE2"));

		// Should always be null
		assertNull(exchange.getIn().getHeader("HEADER-PROP-NULL"));
		assertNotNull(exchange.getIn().getHeader("HEADER-PREEXIST"));
		assertEquals("BLARG", exchange.getIn().getHeader("HEADER-PREEXIST"));

		assertNull(exchange.getIn().getHeader("HEADER-EMPTY"));
		assertNull(exchange.getIn().getHeader("HEADER-EMPTY2"));

		assertNull(exchange.getIn().getHeader("HEADER-REMOVE-NULL"));
		assertNull(exchange.getIn().getHeader("HEADER-REMOVE-NULL2"));

	}


	/**
	 * Helper method
	 */
	private void testPrerequisites() {
		// Make sure the added headers are not there to start with
		assertNull(exchange.getIn().getHeader("HEADER-VALUE"));
		assertNull(exchange.getIn().getHeader("HEADER-PROP"));
		assertNull(exchange.getIn().getHeader("HEADER-PROP-NULL"));
		// Make sure these headers are there to start
		assertNotNull(exchange.getIn().getHeader("HEADER-REMOVE1"));
		assertNotNull(exchange.getIn().getHeader("HEADER-REMOVE2"));
		assertNull(exchange.getIn().getHeader("HEADER-EMPTY"));
		assertNull(exchange.getIn().getHeader("HEADER-EMPTY2"));
		assertNull(exchange.getIn().getHeader("HEADER-REMOVE-NULL"));
		assertNull(exchange.getIn().getHeader("HEADER-REMOVE-NULL2"));
	}



	/**
	 * Setup some properties, headers, exchange and the processors
	 */
	@Before 
	public void setup(){
		exchange = TestHelper.setupExchange();
		requestProcessor = new HeaderTransRequestProcessor();
		responseProcessor = new HeaderTransResponseProcessor();

		AuthIdentity id = (AuthIdentity) exchange.getProperty(ExchangeConstantKeys.E3_AUTH_IDENTITY.toString());
		auth = id.getAuth();
		api = id.getApi();
		policy1 = id.getCallDescriptors().get(0).getPolicy();

		// Setup some properties to reference
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("PROPERTY1","1234");
		properties.put("PROPERTY2","5678");
		exchange.setProperty(ExchangeConstantKeys.E3_MODEL_PROPERTIES.toString(), properties);

		// Add some pre built headers to mess with
		exchange.getIn().setHeader("HEADER-REMOVE1", "BLARG");
		exchange.getIn().setHeader("HEADER-REMOVE2", "BLARG");
		exchange.getIn().setHeader("HEADER-PREEXIST", "BLARG");


	}

	/**
	 * Setup a bunch of header transformations for use.
	 * @return
	 */
	private List<HeaderTransformation> getBasicTrans(){
		List<HeaderTransformation> list = new ArrayList<HeaderTransformation>();

		// Create a value, property and remove transformation for request
		HeaderTransformation ht = new HeaderTransformation();
		ht.setAction(HeaderTransformationAction.ADD);
		ht.setName("HEADER-VALUE");
		ht.setValue("REQUEST-VALUE");
		ht.setType(HeaderTransformationType.REQUEST);
		list.add(ht);

		ht = new HeaderTransformation();
		ht.setAction(HeaderTransformationAction.ADD);
		ht.setName("HEADER-PROP");
		ht.setProperty("PROPERTY1");
		ht.setType(HeaderTransformationType.REQUEST);
		list.add(ht);

		ht = new HeaderTransformation();
		ht.setAction(HeaderTransformationAction.ADD);
		ht.setName("HEADER-PROP-NULL");
		ht.setProperty("PROPERTY3");
		ht.setType(HeaderTransformationType.REQUEST);
		list.add(ht);

		ht = new HeaderTransformation();
		ht.setAction(HeaderTransformationAction.ADD);
		ht.setName("HEADER-EMPTY");
		ht.setType(HeaderTransformationType.REQUEST);
		list.add(ht);

		ht = new HeaderTransformation();
		ht.setAction(HeaderTransformationAction.REMOVE);
		ht.setName("HEADER-REMOVE1");
		ht.setType(HeaderTransformationType.REQUEST);
		list.add(ht);

		ht = new HeaderTransformation();
		ht.setAction(HeaderTransformationAction.REMOVE);
		ht.setName("HEADER-REMOVE-NULL");
		ht.setType(HeaderTransformationType.REQUEST);
		list.add(ht);

		// Create a value, property and remove transformation for the response
		ht = new HeaderTransformation();
		ht.setAction(HeaderTransformationAction.ADD);
		ht.setName("HEADER-VALUE");
		ht.setValue("RESPONSE-VALUE");
		ht.setType(HeaderTransformationType.RESPONSE);
		list.add(ht);

		ht = new HeaderTransformation();
		ht.setAction(HeaderTransformationAction.ADD);
		ht.setName("HEADER-PROP");
		ht.setProperty("PROPERTY2");
		ht.setType(HeaderTransformationType.RESPONSE);
		list.add(ht);

		ht = new HeaderTransformation();
		ht.setAction(HeaderTransformationAction.REMOVE);
		ht.setName("HEADER-REMOVE2");
		ht.setType(HeaderTransformationType.RESPONSE);
		list.add(ht);

		ht = new HeaderTransformation();
		ht.setAction(HeaderTransformationAction.REMOVE);
		ht.setName("HEADER-REMOVE-NULL2");
		ht.setType(HeaderTransformationType.RESPONSE);
		list.add(ht);

		ht = new HeaderTransformation();
		ht.setAction(HeaderTransformationAction.ADD);
		ht.setName("HEADER-PROP-NULL");
		ht.setProperty("PROPERTY3");
		ht.setType(HeaderTransformationType.RESPONSE);
		list.add(ht);

		ht = new HeaderTransformation();
		ht.setAction(HeaderTransformationAction.ADD);
		ht.setName("HEADER-EMPTY2");
		ht.setType(HeaderTransformationType.RESPONSE);
		list.add(ht);

		return list;
	}

}
