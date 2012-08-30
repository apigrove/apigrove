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
package com.alu.e3.tests;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.alu.e3.common.E3Constant;
import com.alu.e3.common.osgi.api.ITopologyClient;
import com.alu.e3.data.model.Instance;
import com.alu.e3.tests.topologywatcher.DummyHealthCheckFactory;
import com.alu.e3.tests.topologywatcher.DummyTopologyClient;
import com.alu.e3.topology.TopologyWatcher;

public class TopologyWatcherTest {
	
	private static final int POLLING_INTERVAL = 10;
	
	private TopologyWatcher topologyWatcher;
	private ITopologyClient topologyClient;
	private DummyHealthCheckFactory healthCheckFactory;
	
	private void setHealthCheckGatewaysActive(String[] gatewayList) {
		Set<String> gateways = new HashSet<String>();
		for (int i = 0; i < gatewayList.length; i++) {
			gateways.add(gatewayList[i]);
		}

		healthCheckFactory.setGatewaysActive(gateways);		
	}
	
	private void setHealthCheckGateways(String[] gatewayList) {
		Set<String> gateways = new HashSet<String>();
		for (int i = 0; i < gatewayList.length; i++) {
			gateways.add(gatewayList[i]);
		}

		healthCheckFactory.setGateways(gateways);		
	}
	
	private void setHealthCheckSpeakers(String[] speakerList) {
		Set<String> speakers = new HashSet<String>();
		for (int i = 0; i < speakerList.length; i++) {
			speakers.add(speakerList[i]);
		}
		
		healthCheckFactory.setSpeakers(speakers);
	}
	
	private Instance createInstance(String type, String intIP, String extIP) {
		Instance instance = new Instance();
		
		instance.setType(type);
		instance.setArea("myArea");
		instance.setInternalIP(intIP);
		instance.setExternalIP(extIP);
		
		return instance;
	}
	
	private void createTestResources(String[] gatewayList, String[] gatewayActiveList, String[] speakerList) {
		topologyWatcher = new TopologyWatcher();
		
		topologyWatcher.setPollingInterval(POLLING_INTERVAL);
		
		topologyClient = new DummyTopologyClient();
		
		healthCheckFactory = new DummyHealthCheckFactory();
		
		setHealthCheckGateways(gatewayList);
		setHealthCheckGatewaysActive(gatewayActiveList);
		setHealthCheckSpeakers(speakerList);
		
		topologyClient.addInstance(createInstance(E3Constant.E3GATEWAY, "1.1.1.1", "1.1.1.1"));
		topologyClient.addInstance(createInstance(E3Constant.E3GATEWAY, "1.1.1.2", "1.1.1.2"));
		topologyClient.addInstance(createInstance(E3Constant.E3GATEWAY, "1.1.1.3", "1.1.1.3"));
		
		topologyClient.addInstance(createInstance(E3Constant.E3SPEAKER, "1.1.1.4", "1.1.1.4"));

		topologyWatcher.setHealthCheckFactory(healthCheckFactory);
		topologyWatcher.setTopologyClient(topologyClient);	
	}
	
	private boolean areEquals(Set<String> v1, String[] v2) {
		
		if (v1 == null || v2 == null) return false;
		
		for (int i = 0; i < v2.length; i++) {
			if (!v1.contains(v2[i])) return false;
		}
		
		return v1.size() == v2.length;
	}
	
	@Test
	public void testTopologyNotChanged() {
		String[] gatewayList = {"1.1.1.1", "1.1.1.2", "1.1.1.3"};
		String[] gatewayActiveList = {"1.1.1.1", "1.1.1.2", "1.1.1.3"};
		String[] speakerList = {"1.1.1.4"};

		createTestResources(gatewayList, gatewayActiveList, speakerList);
		
		// run 2 cycles of topology watcher
		topologyWatcher.init();
		
		try {
			Thread.sleep(POLLING_INTERVAL * 2 + POLLING_INTERVAL / 2);
			
			topologyWatcher.destroy();
			
			// wait until it's stopped
			Thread.sleep(POLLING_INTERVAL + POLLING_INTERVAL / 2);
		}
		catch (InterruptedException e) {
			assertTrue("interrupted", false);
		}
	
		// the list of gateways and speakers must not have changed
		boolean check;
		
		Set<String> outGateways = topologyClient.getAllExternalIPsOfType(E3Constant.E3GATEWAY);		
		check = areEquals(outGateways, gatewayList);
		assertTrue("The list of gateways must not have changed", check);
		
		Set<String> outGatewaysActive = topologyClient.getAllExternalIPsOfType(E3Constant.E3GATEWAY_ACTIVE);		
		check = areEquals(outGatewaysActive, gatewayActiveList);
		assertTrue("The list of active gateways must not have changed", check);
		
		Set<String> outSpeakers = topologyClient.getAllExternalIPsOfType(E3Constant.E3SPEAKER);		
		check = areEquals(outSpeakers, speakerList);
		assertTrue("The list of speakers is not correct", check);
		
		Set<String> outSpeakersActive = topologyClient.getAllExternalIPsOfType(E3Constant.E3SPEAKER_ACTIVE);		
		check = areEquals(outSpeakersActive, speakerList);
		assertTrue("The list of active speakers must not have changed", check);
	}
	
	@Test
	public void testTopologyGatewayDownAndUp() {
		// from the beginning there's one gateway down
		String[] gatewayList = {"1.1.1.1", "1.1.1.3"};
		String[] gatewayActiveList = {"1.1.1.1", "1.1.1.3"};
		String[] speakerList = {"1.1.1.4"};

		createTestResources(gatewayList, gatewayActiveList, speakerList);

		// run 2 cycles of topology watcher
		topologyWatcher.init();
		
		try {
			Thread.sleep(POLLING_INTERVAL * 2 + POLLING_INTERVAL / 2);

			topologyWatcher.destroy();
			
			// wait until it's stopped
			Thread.sleep(POLLING_INTERVAL + POLLING_INTERVAL / 2);
		}
		catch (InterruptedException e) {
			assertTrue("interrupted", false);
		}
	
		// there must be one gateway down
		boolean check;
		
		Set<String> outGateways = topologyClient.getAllExternalIPsOfType(E3Constant.E3GATEWAY);
		check = !outGateways.contains("1.1.1.2");
		assertTrue("The list of gateways must not contain the down gateway", check);
		check = outGateways.contains("1.1.1.1") && outGateways.contains("1.1.1.3") && outGateways.size() == 2;
		assertTrue("The list of gateways is not correct", check);
		
		Set<String> outActiveGateways = topologyClient.getAllExternalIPsOfType(E3Constant.E3GATEWAY_ACTIVE);
		check = !outActiveGateways.contains("1.1.1.2");
		assertTrue("The list of active gateways must not contain the down gateway", check);
		check = outActiveGateways.contains("1.1.1.1") && outActiveGateways.contains("1.1.1.3") && outActiveGateways.size() == 2;
		assertTrue("The list of active gateways is not correct", check);
		
		Set<String> outSpeakers = topologyClient.getAllExternalIPsOfType(E3Constant.E3SPEAKER);		
		check = areEquals(outSpeakers, speakerList);
		assertTrue("The list of speakers is not correct", check);
		
		Set<String> outSpeakersActive = topologyClient.getAllExternalIPsOfType(E3Constant.E3SPEAKER_ACTIVE);		
		check = areEquals(outSpeakersActive, speakerList);
		assertTrue("The list of active speakers must not have changed", check);
		
		// add one gateway
		String[] newGatewayList = {"1.1.1.1", "1.1.1.2", "1.1.1.3"};
		setHealthCheckGateways(newGatewayList);
		
		// run 1 cycle of topology watcher
		topologyWatcher.init();
		
		try {
			Thread.sleep(POLLING_INTERVAL * 2 + POLLING_INTERVAL / 2);

			topologyWatcher.destroy();
			
			// wait until it's stopped
			Thread.sleep(POLLING_INTERVAL + POLLING_INTERVAL / 2);
		}
		catch (InterruptedException e) {
			assertTrue("interrupted", false);
		}
	
		// all gateways must be up
		outGateways = topologyClient.getAllExternalIPsOfType(E3Constant.E3GATEWAY);		
		check = areEquals(outGateways, newGatewayList);
		assertTrue("The list of gateways is not correct", check);
		
		outActiveGateways = topologyClient.getAllExternalIPsOfType(E3Constant.E3GATEWAY_ACTIVE);
		check = !outActiveGateways.contains("1.1.1.2");
		assertTrue("The list of active gateways must not contain the down gateway", check);
		check = outActiveGateways.contains("1.1.1.1") && outActiveGateways.contains("1.1.1.3") && outActiveGateways.size() == 2;
		assertTrue("The list of active gateways is not correct", check);
		
		outSpeakers = topologyClient.getAllExternalIPsOfType(E3Constant.E3SPEAKER);		
		check = areEquals(outSpeakers, speakerList);
		assertTrue("The list of speakers is not correct", check);
		
		outSpeakersActive = topologyClient.getAllExternalIPsOfType(E3Constant.E3SPEAKER_ACTIVE);		
		check = areEquals(outSpeakersActive, speakerList);
		assertTrue("The list of active speakers must not have changed", check);
		
		// make the gateway active
		String[] newGatewayActiveList = {"1.1.1.1", "1.1.1.2", "1.1.1.3"};
		setHealthCheckGatewaysActive(newGatewayActiveList);

		// run 1 cycle of topology watcher
		topologyWatcher.init();
		
		try {
			Thread.sleep(POLLING_INTERVAL * 2 + POLLING_INTERVAL / 2);

			topologyWatcher.destroy();
			
			// wait until it's stopped
			Thread.sleep(POLLING_INTERVAL + POLLING_INTERVAL / 2);
		}
		catch (InterruptedException e) {
			assertTrue("interrupted", false);
		}
	
		// all gateways must be up
		outGateways = topologyClient.getAllExternalIPsOfType(E3Constant.E3GATEWAY);		
		check = areEquals(outGateways, newGatewayList);
		assertTrue("The list of gateways is not correct", check);
		
		outActiveGateways = topologyClient.getAllExternalIPsOfType(E3Constant.E3GATEWAY_ACTIVE);
		check = areEquals(outActiveGateways, newGatewayActiveList);
		assertTrue("The list of gateways is not correct", check);
		
		outSpeakers = topologyClient.getAllExternalIPsOfType(E3Constant.E3SPEAKER);		
		check = areEquals(outSpeakers, speakerList);
		assertTrue("The list of speakers is not correct", check);

		outSpeakersActive = topologyClient.getAllExternalIPsOfType(E3Constant.E3SPEAKER_ACTIVE);		
		check = areEquals(outSpeakersActive, speakerList);
		assertTrue("The list of active speakers must not have changed", check);
	}
	
	@Test
	public void testTopologySpeakerDownAndUp() {
		// from the beginning there's one speaker down
		String[] gatewayList = {"1.1.1.1", "1.1.1.2", "1.1.1.3"};
		String[] gatewayActiveList = {"1.1.1.1", "1.1.1.2", "1.1.1.3"};
		String[] speakerBaseList = {"1.1.1.4"};
		String[] speakerList = {};

		createTestResources(gatewayList, gatewayActiveList, speakerList);

		// run 1 cycle of topology watcher
		topologyWatcher.init();
		
		try {
			Thread.sleep(POLLING_INTERVAL * 2 + POLLING_INTERVAL / 2);

			topologyWatcher.destroy();
			
			// wait until it's stopped
			Thread.sleep(POLLING_INTERVAL + POLLING_INTERVAL / 2);
		}
		catch (InterruptedException e) {
			assertTrue("interrupted", false);
		}
	
		// there must be one speaker down
		boolean check;
		
		Set<String> outGateways = topologyClient.getAllExternalIPsOfType(E3Constant.E3GATEWAY);
		check = areEquals(outGateways, gatewayList);
		assertTrue("The list of gateways is not correct", check);
		
		Set<String> outActiveGateways = topologyClient.getAllExternalIPsOfType(E3Constant.E3GATEWAY_ACTIVE);
		check = areEquals(outActiveGateways, gatewayActiveList);
		assertTrue("The list of active gateways is not correct", check);
		
		Set<String> outSpeakers = topologyClient.getAllExternalIPsOfType(E3Constant.E3SPEAKER);		
		check = areEquals(outSpeakers, speakerBaseList);
		assertTrue("The list of speakers is not correct", check);
		
		Set<String> outSpeakersActive = topologyClient.getAllExternalIPsOfType(E3Constant.E3SPEAKER_ACTIVE);		
		check = outSpeakersActive.size() == 0;
		assertTrue("The list of active speakers must be empty", check);
		
		// add one speaker
		String[] newSpeakerList = {"1.1.1.4"};
		setHealthCheckSpeakers(newSpeakerList);
		
		// run 1 cycle of topology watcher
		topologyWatcher.init();
		
		try {
			Thread.sleep(POLLING_INTERVAL * 2 + POLLING_INTERVAL / 2);

			topologyWatcher.destroy();
			
			// wait until it's stopped
			Thread.sleep(POLLING_INTERVAL + POLLING_INTERVAL / 2);
		}
		catch (InterruptedException e) {
			assertTrue("interrupted", false);
		}
	
		// there must be one new speaker
		outGateways = topologyClient.getAllExternalIPsOfType(E3Constant.E3GATEWAY);
		check = areEquals(outGateways, gatewayList);
		assertTrue("The list of gateways is not correct", check);
		
		outActiveGateways = topologyClient.getAllExternalIPsOfType(E3Constant.E3GATEWAY_ACTIVE);
		check = areEquals(outActiveGateways, gatewayActiveList);
		assertTrue("The list of active gateways is not correct", check);
		
		outSpeakers = topologyClient.getAllExternalIPsOfType(E3Constant.E3SPEAKER);		
		check = areEquals(outSpeakers, speakerBaseList);
		assertTrue("The list of speakers is not correct", check);
		
		outSpeakers = topologyClient.getAllExternalIPsOfType(E3Constant.E3SPEAKER_ACTIVE);		
		check = areEquals(outSpeakers, newSpeakerList);
		assertTrue("The list of speakers is not correct", check);
		
		// remove the speaker
		setHealthCheckSpeakers(speakerList);

		// run 1 cycle of topology watcher
		topologyWatcher.init();
		
		try {
			Thread.sleep(POLLING_INTERVAL * 2 + POLLING_INTERVAL / 2);

			topologyWatcher.destroy();
			
			// wait until it's stopped
			Thread.sleep(POLLING_INTERVAL + POLLING_INTERVAL / 2);
		}
		catch (InterruptedException e) {
			assertTrue("interrupted", false);
		}
		
		outGateways = topologyClient.getAllExternalIPsOfType(E3Constant.E3GATEWAY);
		check = areEquals(outGateways, gatewayList);
		assertTrue("The list of gateways is not correct", check);
		
		outActiveGateways = topologyClient.getAllExternalIPsOfType(E3Constant.E3GATEWAY_ACTIVE);
		check = areEquals(outActiveGateways, gatewayActiveList);
		assertTrue("The list of active gateways is not correct", check);
		
		outSpeakers = topologyClient.getAllExternalIPsOfType(E3Constant.E3SPEAKER);		
		check = areEquals(outSpeakers, speakerBaseList);
		assertTrue("The list of speakers is not correct", check);
		
		outSpeakersActive = topologyClient.getAllExternalIPsOfType(E3Constant.E3SPEAKER_ACTIVE);		
		check = outSpeakersActive.size() == 0;
		assertTrue("The list of active speakers must be empty", check);
	}
}


