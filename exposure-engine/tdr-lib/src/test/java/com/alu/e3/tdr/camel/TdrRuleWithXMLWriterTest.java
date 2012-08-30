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
package com.alu.e3.tdr.camel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alu.e3.tdr.service.ITdrQueueService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { 
		"classpath:tdr.osgi-context-writer-test.xml"
})
public class TdrRuleWithXMLWriterTest {

	@Autowired
	@Qualifier("tdrWriterContext")
	CamelContext camelContext;

	@Autowired
	ITdrQueueService tdrQueueService;

	@BeforeClass
	public static void cleanTdrs() {
		File outputDir = new File("target/tdrs");
		outputDir.mkdirs();
		for(File f : outputDir.listFiles()) {
			f.delete();
		}
	}

	@Test
	public void enableTestEnvironment() throws Exception {
		MockEndpoint.assertIsSatisfied(camelContext);
	}

	@Test
	public void xmlWriterTest() throws InterruptedException {
		// Waiting xmlWriter service launch
		Thread.sleep(1010);

		// Ok, the writer should have been started
		//TODO: Test it up


		final ProducerTemplate producer;
		MockEndpoint mockEndpoint;

		//
		// Test - Blocking tdr queue service 
		producer = camelContext.createProducerTemplate();
		assertNotNull("producer for test purpose is null", producer);
		mockEndpoint = camelContext.getEndpoint("mock:tdrWriter", MockEndpoint.class);
		assertNotNull("mockEndpoint for test purpose is null", mockEndpoint);
		assertNotNull("tdrQueueService for test purpose is null", tdrQueueService);

		long start = System.currentTimeMillis();

		// Each thread sends 100 message
		// There are 100 threads ...
		// So : finally 10000 tdrs
		for(int i=0; i<100; i++) {

			new Thread(
					new Runnable() {
						@Override
						public void run() {
							for(int i=0; i<100; i++) {
								Exchange exchange = new DefaultExchange(camelContext);
								exchange.getIn().setHeader("DynamicHeader", "The dynamic value message:"+i);
								producer.send("direct:tdrWriterTest", exchange);
							}
						}
					}
					).start();
		}
		// Wait a bit to be sure
		int attempt = 1000; // We're not going to wait indefinitely ...
		do {
			Thread.sleep(100);
			System.out.println("  Remaining route to finish:"+mockEndpoint.getReceivedCounter()+"/10000");
		} while( mockEndpoint.getReceivedCounter()<10000 && --attempt > 0);

		assertEquals("Wrong expected message count", 10000, mockEndpoint.getReceivedCounter());

		attempt = 1000;
		do {
			Thread.sleep(100);
			System.out.println("  Remaining tdr to write:"+tdrQueueService.getQueueSize());
		} while(tdrQueueService.getQueueSize()>0 && --attempt > 0);

		assertEquals("Wrong queue size", 0, tdrQueueService.getQueueSize());

		long stop = System.currentTimeMillis();

		System.out.println("Writing 10000 tdr took: "+(stop-start)+"millis");

		// The queue should be empty
		Assert.assertTrue("Size should be 0", tdrQueueService.getQueueSize() == 0);

		// Reset
		mockEndpoint.reset();
	}
}