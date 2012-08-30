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
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.alu.e3.data.model.Api;
import com.alu.e3.gateway.common.camel.exception.GatewayException;
import com.alu.e3.gateway.common.camel.exception.GatewayExceptionCode;
import com.alu.e3.gateway.common.camel.processor.HttpMethodProcessor;

public class HttpMethodProcessorTest {

	static String[] ALLOWED_ARR = {"yes","good","POSITIVE","SuperSweet","All","gOOd","1234"};
	static String[] NOTALLOWED_ARR = {"no","NotAllowed","BAD","terrible","CRAZY","paTHEtic","6789"};

	static List<String> ALLOWED;
	static List<String> NOTALLOWED;
	
	Exchange exchange;

	HttpMethodProcessor proc;

	Api api;
	
	@Before 
	public void setup(){
		proc = new HttpMethodProcessor();
		
		ALLOWED = Arrays.asList(ALLOWED_ARR);
		NOTALLOWED = Arrays.asList(NOTALLOWED_ARR);
	}
	
	@Test
	public void testPermutations() throws Exception{
		proc.setAllowedHttpMethods(StringUtils.join(ALLOWED.iterator(), ","));
		
		checkAllowedMethods(ALLOWED);
		
		checkNotAllowedMethods(NOTALLOWED);
	}
	
	@Test
	public void testNullEdgeCases() throws Exception {
		//empty list implies all are allowed
		proc.setAllowedHttpMethods("");
		checkAllowedMethods(ALLOWED);
		checkAllowedMethods(NOTALLOWED);

		//empty list implies all are allowed
		proc.setAllowedHttpMethods(null);
		checkAllowedMethods(ALLOWED);
		checkAllowedMethods(NOTALLOWED);
		
		//check that when too many commas are added, they are ignored
		List<String> extracommas = new ArrayList<String>();
		extracommas.addAll(ALLOWED);
		extracommas.add(0, ",");
		extracommas.add(2,",,");
		extracommas.add(",,");
		proc.setAllowedHttpMethods(StringUtils.join(extracommas.iterator(), ","));
		checkAllowedMethods(ALLOWED);
		checkNotAllowedMethods(NOTALLOWED);
		checkNotAllowedMethods(Arrays.asList("", ",", ",,", null));
		
		
	}
	
	private void checkAllowedMethods(Collection<String> methods) throws Exception {
		for(String method : methods){
			exchange = TestHelper.setupExchange();
			exchange.getIn().setHeader(Exchange.HTTP_METHOD, method);
			proc.process(exchange);
			//no exception means OK
		}
	}

	private void checkNotAllowedMethods(Collection<String> methods) throws Exception {
		for(String method : methods){
			exchange = TestHelper.setupExchange();
			exchange.getIn().setHeader(Exchange.HTTP_METHOD, method);
			try{
				proc.process(exchange);
				fail("Should not succeed");
			}catch(GatewayException e){
				assertEquals("Exception should be an HttpMethod error", GatewayExceptionCode.HTTP_METHOD, e.getCode());
			}
		}
	}
	
}
