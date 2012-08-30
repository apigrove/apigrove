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
package com.alu.e3.installer.parsers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.alu.e3.installer.model.Configuration;

/**
 * Test for InstallerConfigurationParser.
 *
 */
public class InstallerConfigurationTest {

	private static String CONFIG_PATH = "/installer/installer-config.xml";
	
	private InstallerConfigurationParser parser;
	
	@Before
	public void setUp() throws Exception {
		parser = new InstallerConfigurationParser();
	}
	
	@Test
	public void test() throws InstallerParserException {
		URL fileURL = getClass().getResource(CONFIG_PATH);
		
		Map<String, List<Configuration>> configurations = parser.parse(fileURL.getPath());
		
		// this configuration file should contain 3 types of machine
		assertEquals(3, configurations.size());
		assertNotNull(configurations.get("E3ManagerMaster"));
		assertNotNull(configurations.get("E3ManagerSlave"));
		assertNotNull(configurations.get("E3Gateway"));
		
		Configuration cfg = null;
		
		// E3Manager Master dummy configurations
		List<Configuration> e3MgrMstrConfigs = configurations.get("E3ManagerMaster");
		assertEquals(2, e3MgrMstrConfigs.size());
		
		cfg = e3MgrMstrConfigs.get(0);
		assertNotNull(null, cfg.getName());
		assertEquals("file:///tmp/E3.zip", cfg.getPackageUrl());
		assertEquals("bin/install.sh manager-master", cfg.getInstallerCmd());
		assertEquals("bin/sanitycheck.sh manager", cfg.getSanityCheckCmd());
		
		cfg = e3MgrMstrConfigs.get(1);
		assertNotNull(null, cfg.getName()); 
		assertEquals("file:///tmp/E3ManagerAddOn.zip", cfg.getPackageUrl());
		assertEquals("bin/install.sh", cfg.getInstallerCmd());
		assertEquals("bin/sanitycheck.sh", cfg.getSanityCheckCmd());

		// E3Manager Slave dummy configurations
		List<Configuration> e3MgrSlaveConfigs = configurations.get("E3ManagerSlave");
		cfg = e3MgrSlaveConfigs.get(0);
		assertNotNull(null, cfg.getName()); 
		assertEquals("file:///tmp/E3.zip", cfg.getPackageUrl());
		assertEquals("bin/install.sh manager-slave", cfg.getInstallerCmd());
		assertEquals("bin/sanitycheck.sh manager", cfg.getSanityCheckCmd());
		
		// E3Gateway dummy configurations
		List<Configuration> e3GtwConfigs = configurations.get("E3Gateway");
		cfg = e3GtwConfigs.get(0);
		assertNotNull(null, cfg.getName()); 
		assertEquals("file:///tmp/E3.zip", cfg.getPackageUrl());
		assertEquals("bin/install.sh gateway", cfg.getInstallerCmd());
		assertEquals("bin/sanitycheck.sh gateway", cfg.getSanityCheckCmd());
		
	}

}
