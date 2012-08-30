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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alu.e3.tdr.TDRDataService;
import com.alu.e3.tdr.service.ITdrQueueService;
import com.alu.e3.tdr.service.impl.TdrQueueService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { 
		"classpath:tdr.osgi-context-test.xml"
})
public class TdrRuleTest {

	@Autowired
	@Qualifier("tdrRuleContext")
	CamelContext camelContext;

	@Autowired
	ITdrQueueService tdrQueueService;


	@Test
	public void enableTestEnvironment() throws Exception {
		MockEndpoint.assertIsSatisfied(camelContext);
	}

	@Test
	public void staticRuleTest() throws InterruptedException {
		ProducerTemplate producer;
		MockEndpoint mockEndpoint;
		Exchange exchange;
		Exchange receivedExchange;
		Map<String, Object> tdrProperties;

		//
		// Test route 1
		producer = camelContext.createProducerTemplate();
		assertNotNull("producer for test purpose is null", producer);
		mockEndpoint = camelContext.getEndpoint("mock:tdrRuleResult1", MockEndpoint.class);
		assertNotNull("mockEndpoint for test purpose is null", mockEndpoint);

		mockEndpoint.expectedMessageCount(1);
		exchange = new DefaultExchange(camelContext);

		producer.send("direct:tdrRuleTest1", exchange);

		assertEquals("Wrong expected message count", 1, mockEndpoint.getReceivedCounter());

		receivedExchange = mockEndpoint.getExchanges().get(0);
		assertNotNull("receivedExchange is null", receivedExchange);

		tdrProperties = TDRDataService.getTxTDRProperties(receivedExchange);
		assertNotNull("tdrProperties map is null", tdrProperties);

		assertTrue("PropertyOne missing", tdrProperties.containsKey("PropertyOne"));
		assertEquals("PropertyOne wrong value", "Static", tdrProperties.get("PropertyOne"));
		assertTrue("PropertyOneTwo missing", tdrProperties.containsKey("PropertyOneTwo"));
		assertEquals("PropertyOneTwo wrong value", "OtherValue", tdrProperties.get("PropertyOneTwo"));

		mockEndpoint.reset();


		//
		// Test route 1 - 10 messages
		mockEndpoint.expectedMessageCount(10);

		for(int i=0; i<10; i++) {
			exchange = new DefaultExchange(camelContext);
			producer.send("direct:tdrRuleTest1", exchange);
		}

		assertEquals("Wrong expected message count", 10, mockEndpoint.getReceivedCounter());

		Exchange lastExchange = null;
		for(int i=0; i<10; i++) {
			receivedExchange = mockEndpoint.getExchanges().get(i);
			assertNotSame("Exchanges are identics", receivedExchange, lastExchange);
			assertNotNull("receivedExchange is null", receivedExchange);

			tdrProperties = TDRDataService.getTxTDRProperties(receivedExchange);
			assertNotNull("tdrProperties map is null", tdrProperties);

			assertTrue("PropertyOne missing", tdrProperties.containsKey("PropertyOne"));
			assertEquals("PropertyOne wrong value", "Static", tdrProperties.get("PropertyOne"));
			assertTrue("PropertyOneTwo missing", tdrProperties.containsKey("PropertyOneTwo"));
			assertEquals("PropertyOneTwo wrong value", "OtherValue", tdrProperties.get("PropertyOneTwo"));

			lastExchange = receivedExchange;
		}

		mockEndpoint.reset();


		//
		// Test route 2 - Complex static value
		producer = camelContext.createProducerTemplate();
		assertNotNull("producer for test purpose is null", producer);
		mockEndpoint = camelContext.getEndpoint("mock:tdrRuleResult2", MockEndpoint.class);
		assertNotNull("mockEndpoint for test purpose is null", mockEndpoint);

		mockEndpoint.expectedMessageCount(1);
		exchange = new DefaultExchange(camelContext);

		producer.send("direct:tdrRuleTest2", exchange);

		assertEquals("Wrong expected message count", 1, mockEndpoint.getReceivedCounter());

		receivedExchange = mockEndpoint.getExchanges().get(0);
		assertNotNull("receivedExchange is null", receivedExchange);

		tdrProperties = TDRDataService.getTxTDRProperties(receivedExchange);
		assertNotNull("tdrProperties map is null", tdrProperties);

		assertTrue("PropertyTwo missing", tdrProperties.containsKey("PropertyTwo"));
		assertEquals("PropertyTwo wrong value", "Static value with space", tdrProperties.get("PropertyTwo"));
		assertTrue("PropertyTwoTwo missing", tdrProperties.containsKey("PropertyTwoTwo"));
		assertEquals("PropertyTwoTwo wrong value", "OtherValue", tdrProperties.get("PropertyTwoTwo"));

		mockEndpoint.reset();


		//
		// Test route 3 - Complex static value with special character
		producer = camelContext.createProducerTemplate();
		assertNotNull("producer for test purpose is null", producer);
		mockEndpoint = camelContext.getEndpoint("mock:tdrRuleResult3", MockEndpoint.class);
		assertNotNull("mockEndpoint for test purpose is null", mockEndpoint);

		mockEndpoint.expectedMessageCount(1);
		exchange = new DefaultExchange(camelContext);

		producer.send("direct:tdrRuleTest3", exchange);

		assertEquals("Wrong expected message count", 1, mockEndpoint.getReceivedCounter());

		receivedExchange = mockEndpoint.getExchanges().get(0);
		assertNotNull("receivedExchange is null", receivedExchange);

		tdrProperties = TDRDataService.getTxTDRProperties(receivedExchange);
		assertNotNull("tdrProperties map is null", tdrProperties);

		assertTrue("PropertyThree missing", tdrProperties.containsKey("PropertyThree"));
		assertEquals("PropertyThree wrong value", "What kind of complex value we should have ?", tdrProperties.get("PropertyThree"));
		assertTrue("PropertyThreeTwo missing", tdrProperties.containsKey("PropertyThreeTwo"));
		assertEquals("PropertyThreeTwo wrong value", "OtherValue", tdrProperties.get("PropertyThreeTwo"));

		mockEndpoint.reset();
	}

	public void dynamicRuleTest() {
		ProducerTemplate producer;
		MockEndpoint mockEndpoint;
		Exchange exchange;
		Exchange receivedExchange;
		Map<String, Object> tdrProperties;


		//
		// Test route 4 - Dynamic tdr rule
		producer = camelContext.createProducerTemplate();
		assertNotNull("producer for test purpose is null", producer);
		mockEndpoint = camelContext.getEndpoint("mock:tdrRuleResult4", MockEndpoint.class);
		assertNotNull("mockEndpoint for test purpose is null", mockEndpoint);

		mockEndpoint.expectedMessageCount(1);
		exchange = new DefaultExchange(camelContext);
		exchange.getIn().setHeader("Header1", "Value header 1");

		producer.send("direct:tdrRuleTest4", exchange);

		assertEquals("Wrong expected message count", 1, mockEndpoint.getReceivedCounter());

		receivedExchange = mockEndpoint.getExchanges().get(0);
		assertNotNull("receivedExchange is null", receivedExchange);

		tdrProperties = TDRDataService.getTxTDRProperties(receivedExchange);
		assertNotNull("tdrProperties map is null", tdrProperties);

		assertTrue("PropertyFour missing", tdrProperties.containsKey("PropertyFour"));
		assertEquals("PropertyFour wrong value", "Value header 1", tdrProperties.get("PropertyFour"));
		assertTrue("PropertyFourTwo missing", tdrProperties.containsKey("PropertyFourTwo"));
		assertEquals("PropertyFourTwo wrong value", "OtherValue", tdrProperties.get("PropertyFourTwo"));


		mockEndpoint.reset();
	}

	@Test
	public void queueServiceTest() throws InterruptedException {
		ProducerTemplate producer;
		MockEndpoint mockEndpoint;
		Exchange exchange;
		Map<String, List<Map<String, Object>>> tdrData;

		//
		// Test route 5 - Queue Service
		producer = camelContext.createProducerTemplate();
		assertNotNull("producer for test purpose is null", producer);
		mockEndpoint = camelContext.getEndpoint("mock:tdrRuleResult5", MockEndpoint.class);
		assertNotNull("mockEndpoint for test purpose is null", mockEndpoint);
		assertNotNull("tdrQueueService for test purpose is null", tdrQueueService);

		mockEndpoint.expectedMessageCount(1);
		exchange = new DefaultExchange(camelContext);
		exchange.getIn().setHeader("DynamicHeader", "DynamicValue");

		producer.send("direct:tdrRuleTest5", exchange);
		assertEquals("Wrong expected message count", 1, mockEndpoint.getReceivedCounter());
		assertEquals("Wrong tdrQueueService size", 1, tdrQueueService.getQueueSize());

		tdrData = tdrQueueService.getOrWait();
		// Now the queue is empty
		assertEquals("Wrong tdrQueueService size", 0, tdrQueueService.getQueueSize());
		// Check tdrData values
		assertNotNull("tdrDatas is null", tdrData);
		assertTrue("tdrDatas does not have a txTDR",
				tdrData.containsKey("txTDR"));
		assertTrue("txTDR list has not members",
				tdrData.get("txTDR").size() > 0);
		Map<String, Object> txTdr = tdrData.get("txTDR").get(0);
		assertTrue("PropertyFive missing", txTdr.containsKey("PropertyFive"));
		assertEquals("PropertyFive wrong value", "OtherValue",
				txTdr.get("PropertyFive"));
		assertTrue("DynamicHeader missing", txTdr.containsKey("DynamicHeader"));
		assertEquals("DynamicHeader wrong value", "DynamicValue",
				txTdr.get("DynamicHeader"));

		// Reset
		mockEndpoint.reset();

		// Sends 10 message
		for(int i=0; i<10; i++) {
			exchange = new DefaultExchange(camelContext);
			exchange.getIn().setHeader("DynamicHeader", "DynamicValue"+i);

			producer.send("direct:tdrRuleTest5", exchange);
		}
		assertEquals("Wrong expected message count", 10,
				mockEndpoint.getReceivedCounter());
		tdrData = tdrQueueService.getOrWait();
		// Now the queue is 9
		assertEquals("Wrong tdrQueueService size", 9, tdrQueueService.getQueueSize());
		// Check tdrData values
		assertNotNull("tdrDatas is null", tdrData);
		assertNotNull("tdrDatas is null", tdrData);
		assertTrue("tdrDatas does not have a txTDR",
				tdrData.containsKey("txTDR"));
		assertTrue("txTDR list has not members",
				tdrData.get("txTDR").size() > 0);
		txTdr = tdrData.get("txTDR").get(0);
		assertTrue("PropertyFive missing", txTdr.containsKey("PropertyFive"));
		assertEquals("PropertyFive wrong value", "OtherValue", txTdr.get("PropertyFive"));
		assertTrue("DynamicHeader missing", txTdr.containsKey("DynamicHeader"));
		assertEquals("DynamicHeader wrong value", "DynamicValue0",
				txTdr.get("DynamicHeader"));
		// Check others tdrData
		for (int i = 1; i < 10; i++) {
			tdrData = tdrQueueService.getOrWait();
			assertEquals("Wrong tdrQueueService size", 9 - i,
					tdrQueueService.getQueueSize());
			// Check tdrData values

			assertNotNull("tdrDatas is null", tdrData);
			assertTrue("tdrDatas does not have a txTDR",
					tdrData.containsKey("txTDR"));
			assertTrue("txTDR list has not members", tdrData.get("txTDR")
					.size() > 0);
			txTdr = tdrData.get("txTDR").get(0);
			assertTrue("PropertyFive missing",
					txTdr.containsKey("PropertyFive"));
			assertEquals("PropertyFive wrong value", "OtherValue",
					txTdr.get("PropertyFive"));
			assertTrue("DynamicHeader missing",
					txTdr.containsKey("DynamicHeader"));
			assertEquals("DynamicHeader wrong value", "DynamicValue" + i,
					txTdr.get("DynamicHeader"));
		}

		// Reset
		mockEndpoint.reset();

		// Bulk tdrData get
		// Sends 10 message
		for (int i = 0; i < 10; i++) {
			exchange = new DefaultExchange(camelContext);
			exchange.getIn().setHeader("DynamicHeader", "DynamicValue" + i);

			producer.send("direct:tdrRuleTest5", exchange);
		}
		assertEquals("Wrong expected message count", 10,
				mockEndpoint.getReceivedCounter());
		assertEquals("Wrong tdrQueueService size", 10,
				tdrQueueService.getQueueSize());
		// Get 5 of them
		List<Map<String, List<Map<String, Object>>>> tdrDatas = tdrQueueService
				.getMultiple(5);
		assertEquals("Wrong tdrQueueService size", 5,
				tdrQueueService.getQueueSize());
		assertEquals("Wrong tdrDatas size", 5, tdrDatas.size());
		tdrDatas.clear();
		// Get 10 but there is only 5 in queue
		tdrDatas = tdrQueueService.getMultiple(10);
		assertEquals("Wrong tdrQueueService size", 0,
				tdrQueueService.getQueueSize());
		// So we must have 5
		assertEquals("Wrong tdrDatas size", 5, tdrDatas.size());
		tdrDatas.clear();


		// Reset
		mockEndpoint.reset();
	}

	@Test
	public void blockingQueueServiceTest() throws InterruptedException {
		final ProducerTemplate producer;
		MockEndpoint mockEndpoint;
		Exchange exchange;
		Map<String, List<Map<String, Object>>> tdrData;

		// Test - Blocking tdr queue service
		producer = camelContext.createProducerTemplate();
		assertNotNull("producer for test purpose is null", producer);
		mockEndpoint = camelContext.getEndpoint("mock:tdrRuleResult5",
				MockEndpoint.class);
		assertNotNull("mockEndpoint for test purpose is null", mockEndpoint);
		assertNotNull("tdrQueueService for test purpose is null",
				tdrQueueService);

		TdrQueueService tdrQueueServiceImpl = (TdrQueueService) tdrQueueService;
		// Force the queue size to have only 5 slot.
		// If a sixth exchange come, the thread will wait until someone
		// consume at least one message from the queue.
		tdrQueueServiceImpl.setQueueWaitingSize(5);

		// Each exchange are sent in a separate thread
		for (int i = 0; i < 10; i++) {
			exchange = new DefaultExchange(camelContext);
			exchange.getIn().setHeader("DynamicHeader", "DynamicValue" + i);

			new Thread(
					new Runnable() {
						private Exchange exchange;
						public Runnable setExchange(Exchange exchange) {
							this.exchange = exchange;
							return this;
						}
						@Override
						public void run() {
							producer.send("direct:tdrRuleTest5", exchange);
						}
					}.setExchange(exchange)
					).start();
		}
		// Wait a bit to be sure
		Thread.sleep(100);
		// We had 10 thread sending exchange
		// but the tdrData todo queue is 5 slot
		// We have only received 5 message:
		assertEquals("Wrong expected message count", 5, mockEndpoint.getReceivedCounter());

		// Pop the first one from the queue
		tdrData = tdrQueueService.getOrWait();
		assertNotNull("tdrDatas is null", tdrData);
		assertTrue("tdrDatas does not have a txTDR",
				tdrData.containsKey("txTDR"));
		assertTrue("txTDR list has not members",
				tdrData.get("txTDR").size() > 0);
		Map<String, Object> txTdr = tdrData.get("txTDR").get(0);
		assertTrue("PropertyFive missing", txTdr.containsKey("PropertyFive"));
		assertEquals("PropertyFive wrong value", "OtherValue",
				txTdr.get("PropertyFive"));
		// Wait a bit to be sure
		Thread.sleep(100);
		// Now the queue is still 5, cause a route thread has been released
		assertEquals("Wrong tdrQueueService size", 5,
				tdrQueueService.getQueueSize());
		// Only one more route thread has finished
		assertEquals("Wrong expected message count", 6,
				mockEndpoint.getReceivedCounter());
		// Pop .=*=. 2
		tdrData = tdrQueueService.getOrWait();
		// Pop .=*=. 3
		tdrData = tdrQueueService.getOrWait();
		// Pop .=*=. 4
		tdrData = tdrQueueService.getOrWait();
		// Pop .=*=. 5
		tdrData = tdrQueueService.getOrWait();
		// Wait a bit to be sure
		Thread.sleep(100);
		// All route have finished
		assertEquals("Wrong expected message count", 10,
				mockEndpoint.getReceivedCounter());
		// But tdr data to treat are still 5
		assertEquals("Wrong tdrQueueService size", 5,
				tdrQueueService.getQueueSize());
		// Pop them all
		List<Map<String, List<Map<String, Object>>>> tdrDatas = tdrQueueService
				.getMultiple(10);
		// Queue size is empty
		assertEquals("Wrong tdrQueueService size", 0,
				tdrQueueService.getQueueSize());
		assertEquals("Wrong tdrDatas size", 5, tdrDatas.size());
		// No more data to treat
		tdrDatas = tdrQueueService.getMultiple(10);
		assertEquals("Wrong tdrDatas size", 0, tdrDatas.size());


		// Reset
		mockEndpoint.reset();
	}

	@Test
	public void commonTdrRuleTest() throws InterruptedException {
		ProducerTemplate producer;
		MockEndpoint mockEndpoint;
		Exchange exchange;
		Exchange receivedExchange;
		Map<String, List<Map<String, Object>>> tdrProperties;

		//
		// Test route 6 - Common tdr rule
		producer = camelContext.createProducerTemplate();
		assertNotNull("producer for test purpose is null", producer);
		mockEndpoint = camelContext.getEndpoint("mock:tdrRuleResult6",
				MockEndpoint.class);
		assertNotNull("mockEndpoint for test purpose is null", mockEndpoint);
		assertNotNull("tdrQueueService for test purpose is null",
				tdrQueueService);

		mockEndpoint.expectedMessageCount(1);
		exchange = new DefaultExchange(camelContext);
		exchange.getIn().setHeader("DynamicHeader7",
				"The value header 7, but will not be extracted");
		exchange.getIn().setHeader("DynamicHeader8", "The value header 8");

		producer.send("direct:tdrRuleTest6", exchange);

		assertEquals("Wrong expected message count", 1,
				mockEndpoint.getReceivedCounter());

		receivedExchange = mockEndpoint.getExchanges().get(0);
		assertNotNull("receivedExchange is null", receivedExchange);

		tdrProperties = tdrQueueService.getOrWait();
		assertTrue("tdr Properties doesn't have a txTDR",
				tdrProperties.containsKey("txTDR"));
		assertTrue("txTDR list has no elements", tdrProperties.get("txTDR")
				.size() > 0);

		Map<String, Object> txTdr = tdrProperties.get("txTDR").get(0);

		assertTrue("PropertySeven missing", txTdr.containsKey("PropertySeven"));
		assertEquals("PropertySeven wrong value", "SevenValue",
				txTdr.get("PropertySeven"));
		assertTrue("Property8 missing", txTdr.containsKey("Property8"));
		assertEquals("Property8 wrong value", "The value header 8",
				txTdr.get("Property8"));
		assertTrue("id missing", txTdr.containsKey("TransactionID"));
		assertNotNull("id is null", txTdr.get("TransactionID"));

		mockEndpoint.reset();
	}
}