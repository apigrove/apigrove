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
package com.alu.e3.data.topology;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

import org.junit.After;
import org.junit.Test;

import com.alu.e3.common.E3Constant;
import com.alu.e3.common.osgi.api.ITopologyClient;
import com.alu.e3.data.model.Instance;
import com.alu.e3.data.model.SSHKey;

public class TDRCollectorInstanceWatcherTest {

	private static final byte[] PRIVATE_KEY = {45, 45, 45, 45, 45, 66, 69, 71, 73, 78, 32, 68, 83, 65, 32, 80, 82, 73, 86, 65, 84, 69, 32, 75, 69, 89, 45, 45, 45, 45, 45, 10, 77, 73, 73, 66, 118, 65, 73, 66, 65, 65, 75, 66, 103, 81, 67, 49, 122, 115, 54, 107, 50, 120, 66, 68, 104, 117, 57, 78, 76, 53, 104, 86, 84, 68, 118, 76, 80, 50, 108, 51, 112, 107, 105, 87, 109, 115, 113, 110, 67, 69, 69, 84, 89, 80, 43, 87, 119, 116, 70, 74, 67, 79, 97, 52, 10, 107, 113, 50, 87, 70, 120, 75, 75, 106, 98, 108, 87, 81, 107, 118, 77, 97, 109, 69, 97, 119, 72, 107, 103, 105, 55, 66, 87, 86, 105, 120, 119, 82, 113, 105, 70, 67, 102, 86, 56, 109, 54, 99, 117, 80, 51, 99, 108, 114, 89, 47, 117, 87, 81, 48, 66, 100, 113, 47, 70, 52, 82, 83, 80, 10, 83, 98, 116, 112, 76, 69, 79, 77, 86, 51, 74, 90, 119, 52, 116, 68, 122, 109, 108, 119, 120, 78, 78, 66, 118, 67, 55, 87, 101, 52, 104, 105, 48, 48, 83, 108, 70, 56, 90, 53, 110, 99, 43, 85, 70, 57, 53, 103, 117, 49, 77, 110, 65, 70, 71, 76, 103, 81, 73, 86, 65, 78, 54, 70, 10, 119, 48, 82, 82, 51, 112, 49, 66, 104, 113, 113, 83, 118, 48, 75, 98, 82, 75, 78, 102, 70, 57, 65, 76, 65, 111, 71, 65, 65, 106, 55, 112, 55, 83, 78, 122, 70, 110, 97, 117, 108, 80, 55, 103, 111, 100, 73, 55, 47, 54, 73, 120, 111, 56, 101, 73, 81, 71, 66, 74, 104, 47, 79, 85, 10, 85, 54, 107, 80, 99, 51, 104, 114, 47, 122, 82, 70, 100, 120, 52, 110, 100, 118, 120, 110, 75, 79, 84, 86, 104, 101, 113, 99, 74, 54, 84, 113, 111, 43, 57, 89, 77, 51, 104, 49, 105, 111, 90, 105, 100, 90, 121, 49, 98, 118, 71, 75, 107, 122, 81, 106, 118, 118, 119, 111, 117, 89, 76, 83, 10, 116, 55, 74, 76, 114, 75, 70, 69, 52, 111, 76, 90, 85, 115, 106, 78, 107, 98, 112, 114, 76, 77, 119, 53, 98, 111, 78, 109, 51, 51, 76, 90, 103, 90, 84, 102, 80, 118, 121, 113, 84, 112, 52, 101, 75, 51, 65, 97, 49, 98, 90, 50, 111, 99, 114, 50, 84, 103, 112, 98, 78, 110, 71, 112, 10, 122, 106, 89, 57, 87, 88, 115, 67, 103, 89, 69, 65, 106, 57, 73, 119, 74, 73, 90, 113, 56, 106, 103, 112, 102, 103, 52, 99, 81, 71, 82, 67, 111, 88, 103, 115, 111, 107, 85, 53, 116, 107, 80, 77, 51, 66, 73, 53, 105, 56, 80, 121, 118, 101, 114, 118, 70, 55, 80, 75, 105, 85, 108, 90, 10, 52, 80, 111, 103, 105, 55, 73, 102, 100, 111, 88, 97, 118, 73, 52, 67, 81, 78, 103, 99, 43, 72, 116, 54, 48, 50, 97, 110, 82, 57, 79, 117, 102, 81, 112, 66, 110, 51, 106, 98, 69, 120, 101, 86, 52, 78, 103, 111, 84, 117, 88, 119, 78, 101, 80, 68, 52, 77, 118, 111, 112, 108, 80, 111, 10, 78, 89, 82, 114, 82, 75, 56, 121, 67, 106, 88, 116, 79, 88, 83, 101, 87, 67, 52, 108, 74, 103, 67, 108, 74, 109, 55, 83, 72, 82, 111, 73, 51, 76, 111, 49, 47, 112, 109, 77, 88, 51, 120, 75, 119, 83, 71, 115, 111, 119, 99, 76, 71, 112, 107, 67, 70, 81, 67, 49, 121, 73, 116, 102, 10, 113, 84, 82, 84, 56, 104, 48, 51, 89, 105, 78, 74, 79, 109, 121, 51, 122, 73, 105, 105, 88, 119, 61, 61, 10, 45, 45, 45, 45, 45, 69, 78, 68, 32, 68, 83, 65, 32, 80, 82, 73, 86, 65, 84, 69, 32, 75, 69, 89, 45, 45, 45, 45, 45, 10, 9, 9};
	private static final byte[] PUBLIC_KEY = {115, 115, 104, 45, 100, 115, 115, 32, 65, 65, 65, 65, 66, 51, 78, 122, 97, 67, 49, 107, 99, 51, 77, 65, 65, 65, 67, 66, 65, 76, 88, 79, 122, 113, 84, 98, 69, 69, 79, 71, 55, 48, 48, 118, 109, 70, 86, 77, 79, 56, 115, 47, 97, 88, 101, 109, 83, 74, 97, 97, 121, 113, 99, 73, 81, 82, 78, 103, 47, 53, 98, 67, 48, 85, 107, 73, 53, 114, 105, 83, 114, 90, 89, 88, 69, 111, 113, 78, 117, 86, 90, 67, 83, 56, 120, 113, 89, 82, 114, 65, 101, 83, 67, 76, 115, 70, 90, 87, 76, 72, 66, 71, 113, 73, 85, 74, 57, 88, 121, 98, 112, 121, 52, 47, 100, 121, 87, 116, 106, 43, 53, 90, 68, 81, 70, 50, 114, 56, 88, 104, 70, 73, 57, 74, 117, 50, 107, 115, 81, 52, 120, 88, 99, 108, 110, 68, 105, 48, 80, 79, 97, 88, 68, 69, 48, 48, 71, 56, 76, 116, 90, 55, 105, 71, 76, 84, 82, 75, 85, 88, 120, 110, 109, 100, 122, 53, 81, 88, 51, 109, 67, 55, 85, 121, 99, 65, 85, 89, 117, 66, 65, 65, 65, 65, 70, 81, 68, 101, 104, 99, 78, 69, 85, 100, 54, 100, 81, 89, 97, 113, 107, 114, 57, 67, 109, 48, 83, 106, 88, 120, 102, 81, 67, 119, 65, 65, 65, 73, 65, 67, 80, 117, 110, 116, 73, 51, 77, 87, 100, 113, 54, 85, 47, 117, 67, 104, 48, 106, 118, 47, 111, 106, 71, 106, 120, 52, 104, 65, 89, 69, 109, 72, 56, 53, 82, 84, 113, 81, 57, 122, 101, 71, 118, 47, 78, 69, 86, 51, 72, 105, 100, 50, 47, 71, 99, 111, 53, 78, 87, 70, 54, 112, 119, 110, 112, 79, 113, 106, 55, 49, 103, 122, 101, 72, 87, 75, 104, 109, 74, 49, 110, 76, 86, 117, 56, 89, 113, 84, 78, 67, 79, 43, 47, 67, 105, 53, 103, 116, 75, 51, 115, 107, 117, 115, 111, 85, 84, 105, 103, 116, 108, 83, 121, 77, 50, 82, 117, 109, 115, 115, 122, 68, 108, 117, 103, 50, 98, 102, 99, 116, 109, 66, 108, 78, 56, 43, 47, 75, 112, 79, 110, 104, 52, 114, 99, 66, 114, 86, 116, 110, 97, 104, 121, 118, 90, 79, 67, 108, 115, 50, 99, 97, 110, 79, 78, 106, 49, 90, 101, 119, 65, 65, 65, 73, 69, 65, 106, 57, 73, 119, 74, 73, 90, 113, 56, 106, 103, 112, 102, 103, 52, 99, 81, 71, 82, 67, 111, 88, 103, 115, 111, 107, 85, 53, 116, 107, 80, 77, 51, 66, 73, 53, 105, 56, 80, 121, 118, 101, 114, 118, 70, 55, 80, 75, 105, 85, 108, 90, 52, 80, 111, 103, 105, 55, 73, 102, 100, 111, 88, 97, 118, 73, 52, 67, 81, 78, 103, 99, 43, 72, 116, 54, 48, 50, 97, 110, 82, 57, 79, 117, 102, 81, 112, 66, 110, 51, 106, 98, 69, 120, 101, 86, 52, 78, 103, 111, 84, 117, 88, 119, 78, 101, 80, 68, 52, 77, 118, 111, 112, 108, 80, 111, 78, 89, 82, 114, 82, 75, 56, 121, 67, 106, 88, 116, 79, 88, 83, 101, 87, 67, 52, 108, 74, 103, 67, 108, 74, 109, 55, 83, 72, 82, 111, 73, 51, 76, 111, 49, 47, 112, 109, 77, 88, 51, 120, 75, 119, 83, 71, 115, 111, 119, 99, 76, 71, 112, 107, 61, 32, 114, 111, 111, 116, 64, 118, 109, 45, 111, 97, 112, 45, 48};
	private static final String USER_NAME = "root";
	private static final String KEY_NAME = "test_key_name";
	private static final String CONFIG_NAME = "test_tdr_config";
	private static final String WRITE_LOCATION = ".";
	
	
	private TDRCollectorInstanceWatcher tdrCollectorInstanceWatcher;
	private ITopologyClient topologyClient;

	private Instance createInstance(String type, String intIP, String extIP, SSHKey key) {
		Instance instance = new Instance();
		
		instance.setType(type);
		instance.setArea("myArea");
		instance.setInternalIP(intIP);
		instance.setExternalIP(extIP);
		instance.setSSHKey(key);
		instance.setUser(USER_NAME);
		
		return instance;
	}
	
	private void addInstance(Instance instance) {
		topologyClient.addInstance(instance);
	}
	
	private void deleteInstance(Instance instance) {
		topologyClient.deleteInstance(instance);
	}
	
	private void initializeTestSetup(int gatewayCount, int managerCount,  int tdrCollectorCount, boolean useKey) {
		
		File oldConfig = new File(new File(WRITE_LOCATION), CONFIG_NAME);
		File oldKey = new File(new File(WRITE_LOCATION), KEY_NAME);
		if (oldConfig.exists()) oldConfig.delete();
		if (oldKey.exists()) oldKey.delete();			
		
		tdrCollectorInstanceWatcher = new TDRCollectorInstanceWatcher();
		tdrCollectorInstanceWatcher.setConfigName(CONFIG_NAME);
		tdrCollectorInstanceWatcher.setKeyName(KEY_NAME);
		tdrCollectorInstanceWatcher.setWriteLocation(WRITE_LOCATION);
		
		SSHKey key = null;
		if (useKey) {
			key = new SSHKey(KEY_NAME, PRIVATE_KEY, PUBLIC_KEY);
		}	
		
		topologyClient = new DummyTopologyClient();
		tdrCollectorInstanceWatcher.setTopologyClient(topologyClient);	

		for (int i = 0; i < gatewayCount; i++) {
			addInstance(createInstance(E3Constant.E3GATEWAY, "1.1.1." + i, "2.1.1." + i, null));
		}		
		for (int i = 0; i < managerCount; i++) {
			addInstance(createInstance(E3Constant.E3MANAGER, "1.1.2." + i, "2.1.2." + i, key));
		}
		for (int i = 0; i < tdrCollectorCount; i++) {
			addInstance(createInstance(E3Constant.TDR_COLLECTOR, "1.1.3." + i, "2.1.3." + i, null));
		}	
	}
	
	private boolean verifyKeyExists() {
		return new File(new File(WRITE_LOCATION), KEY_NAME).exists();
	}
	
	private boolean verifyConfigExists() {
		return new File(new File(WRITE_LOCATION), CONFIG_NAME).exists();
	}
	
	// when verifying the correct collectors, it must be so that the current
	// ips go from 0 to (correctCollectorCount-1)
	private boolean verifyValidConfig(int correctCollectorCount) {
		File configFile = new File(new File(WRITE_LOCATION), CONFIG_NAME);
		if (!configFile.exists()) return false;
		Scanner scan;
		try {
			scan = new Scanner(configFile);
		} catch (FileNotFoundException e) {
			return false;
		}
		try {
			boolean hasKey = false;
			boolean hasUser = false;
			boolean hasCollectorCount = false;
			HashMap<String, Boolean> collectorIndices = new HashMap<String, Boolean>();
			int collectorCount = 0;
			
			while (scan.hasNext()) {
				String line = scan.nextLine();
				if (line.equals(TDRCollectorInstanceWatcher.KEY_CFG + 
						TDRCollectorInstanceWatcher.CONFIG_KEYVAL_CHAR + WRITE_LOCATION + "/" + KEY_NAME)) {
					hasKey = true;
				} else if (line.equals(TDRCollectorInstanceWatcher.USER_CFG + 
						TDRCollectorInstanceWatcher.CONFIG_KEYVAL_CHAR + USER_NAME)) {
					hasUser = true;
				} else if (line.startsWith(TDRCollectorInstanceWatcher.HOST_CFG + 
						TDRCollectorInstanceWatcher.CONFIG_KEYVAL_CHAR + "1.1.3.")) {
					collectorCount++;
					hasCollectorCount = collectorCount == correctCollectorCount;
					collectorIndices.put(
							line.replace(TDRCollectorInstanceWatcher.HOST_CFG + 
									TDRCollectorInstanceWatcher.CONFIG_KEYVAL_CHAR + "1.1.3.", ""), 
							true);
				}
			}
			if (!hasKey || !hasUser || !hasCollectorCount) return false;
			
			for (int i = 0; i < correctCollectorCount; i++) {
				if (!collectorIndices.get("" + i)) return false;
			}
		} finally {
			scan.close();			
		}
		
		return true;
	}
	
	@Test
	public void testInitialCreation() {
		initializeTestSetup(1, 1, 1, true);
		tdrCollectorInstanceWatcher.init();
		assertTrue("Key file was not created.", verifyKeyExists());
		assertTrue("Config file was not created.", verifyConfigExists());
		assertTrue("Config file was not correctly built.", verifyValidConfig(1));
	}
	

	@Test
	public void testInitialCreationWithNoTdrCollectors() {
		initializeTestSetup(1, 1, 0, false);
		tdrCollectorInstanceWatcher.init();
		assertTrue("Key file was created.", !verifyKeyExists());
		assertTrue("Config file was created.", !verifyConfigExists());
	}
	
	@Test
	public void testNonTdrCollectorInstances() {
		initializeTestSetup(1, 1, 1, true);
		tdrCollectorInstanceWatcher.init();
		assertTrue("Key file was not created.", verifyKeyExists());
		assertTrue("Config file was not created.", verifyConfigExists());
		assertTrue("Config file was not correctly built.", verifyValidConfig(1));
		Instance gate = createInstance(E3Constant.E3GATEWAY, "1.1.1.1", "2.1.1.1", null);
		Instance manager = createInstance(E3Constant.E3MANAGER, "1.1.2.1", "2.1.2.1", null);
		addInstance(gate);
		addInstance(manager);
		assertTrue("Key file did not remain after adding non-collection instances.", verifyKeyExists());
		assertTrue("Config file did not remain after adding non-collection instances.", verifyConfigExists());
		assertTrue("Config file did not remain correctly built after adding non-collection instances.", verifyValidConfig(1));
		deleteInstance(gate);
		deleteInstance(manager);
		assertTrue("Key file did not remain after removing non-collection instances.", verifyKeyExists());
		assertTrue("Config file did not remain after removing non-collection instances.", verifyConfigExists());
		assertTrue("Config file did not remain correctly built after removing non-collection instances.", verifyValidConfig(1));
	}
	
	@Test
	public void testTdrCollectorInstances() {
		initializeTestSetup(1, 1, 2, true);
		tdrCollectorInstanceWatcher.init();

		assertTrue("Key file was not created.", verifyKeyExists());
		assertTrue("Config file was not created.", verifyConfigExists());
		assertTrue("Config file was not correctly built.", verifyValidConfig(2));
		Instance inst1 = createInstance(E3Constant.TDR_COLLECTOR, "1.1.3.2", "2.1.3.2", null);
		Instance inst2 = createInstance(E3Constant.TDR_COLLECTOR, "1.1.3.3", "2.1.3.3", null);
		addInstance(inst1);
		addInstance(inst2);
		assertTrue("Key file did not remain after adding collection instances.", verifyKeyExists());
		assertTrue("Config file did not remain after adding collection instances.", verifyConfigExists());
		assertTrue("Config file did not remain correctly built after adding collection instances.", verifyValidConfig(4));
		deleteInstance(inst2);
		assertTrue("Key file did not remain after removing collection instance.", verifyKeyExists());
		assertTrue("Config file did not remain after removing collection instance.", verifyConfigExists());
		assertTrue("Config file did not remain correctly built after removing collection instance.", verifyValidConfig(3));
	}
	
	@After
	public void tearDown() {
		File oldKey = new File(new File(WRITE_LOCATION), KEY_NAME);
		File oldConfig = new File(new File(WRITE_LOCATION), CONFIG_NAME);
		if (oldKey.exists()) {
			oldKey.delete();	
		}
		if (oldConfig.exists()) {
			oldConfig.delete();
		}
	}
	
}
