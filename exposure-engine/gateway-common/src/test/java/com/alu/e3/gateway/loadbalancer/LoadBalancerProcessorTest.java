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
package com.alu.e3.gateway.loadbalancer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.apache.log4j.PropertyConfigurator;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:gateway-common.test-lb-context.xml"} )
@Ignore
public class LoadBalancerProcessorTest {

	static {
		Properties props = new Properties(); 
		
		props.setProperty("log4j.rootLogger","DEBUG, myConsoleAppender");
		props.setProperty("log4j.appender.myConsoleAppender","org.apache.log4j.ConsoleAppender");
		props.setProperty("log4j.appender.myConsoleAppender.layout","org.apache.log4j.PatternLayout");
		props.setProperty("log4j.appender.myConsoleAppender.layout.ConversionPattern","%-4r [%t] %-5p %c %x - %m%n");

		PropertyConfigurator.configure(props);
	}
	
	
	@Autowired
    protected CamelContext camelContext;
	
	@EndpointInject(uri = "mock:result")
    protected MockEndpoint resultEndpoint;

	@EndpointInject(uri = "mock:resultError")
    protected MockEndpoint resultErrorEndpoint;
	
	@Produce(uri = "direct:testCallApi")
    protected ProducerTemplate producerTemplate;
	
	@Test
	@DirtiesContext
	@Ignore
	public void testCallApiSuccess() throws InterruptedException {
		
		String body = "200";		
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("MyHeader", "true");
		
		producerTemplate.sendBodyAndHeaders(body, headers);
		resultEndpoint.message(0).header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(200);
		resultEndpoint.assertIsSatisfied();
	
		
	}
	
	@Test
	@DirtiesContext
	@Ignore
	public void testCallApiFails() throws InterruptedException {
		
		String body = "503";		
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("MyHeader", "true");
		
		producerTemplate.sendBodyAndHeaders(body, headers);
		resultErrorEndpoint.message(0).header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(500);
		resultErrorEndpoint.assertIsSatisfied();
	}
	
	@Test
	@DirtiesContext
	@Ignore
	public void testCallApiNotFound() throws InterruptedException {
		
		String body = "404";		
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("MyHeader", "true");
		
		producerTemplate.sendBodyAndHeaders(body, headers);
		resultEndpoint.message(0).header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(404);
		resultEndpoint.assertIsSatisfied();
	}
}
