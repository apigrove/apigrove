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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.installer.model.Configuration;

public class InstallerConfigurationParser {

	private static final CategoryLogger logger = CategoryLoggerFactory.getLogger(InstallerConfigurationParser.class, Category.SYS);
	
	public InstallerConfigurationParser() {

	}
	
	
	public Map<String, List<Configuration>> parse(String configFilePath) throws InstallerParserException {
		if(logger.isDebugEnabled()) {
			logger.debug("Parsing file at path: " + configFilePath);
		}
		
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(configFilePath);
		} catch (FileNotFoundException e) {
			if(logger.isErrorEnabled()) {
				logger.error(e.getMessage());
			}
			throw new InstallerParserException(e);
		}
		return parse(fin);
	}
	
	public Map<String, List<Configuration>> parse(InputStream inputStream) throws InstallerParserException {
		if(inputStream == null)
			throw new InstallerParserException("InputStream null");
		
		Map<String, List<Configuration>> configurations = new HashMap<String, List<Configuration>>();
		try
		{
			// Parsing installers configuration inputstream
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			Document doc = builder.parse(inputStream);
			Element rootElement = doc.getDocumentElement();
	
			NodeList nodes = rootElement.getElementsByTagName("Configuration");
			if(logger.isDebugEnabled()) {
				logger.debug("Found " + nodes.getLength() + " configurations");
			}
			for(int i = 0; i < nodes.getLength(); i++)
			{
				Element el = (Element) nodes.item(i);

				Configuration config = new Configuration();
				config.setPackageUrl(el.getAttribute("packageURL"));
				config.setInstallerCmd(el.getAttribute("installerCmd"));
				config.setSanityCheckCmd(el.getAttribute("sanityCheckCmd"));
				config.setGenerateNatureCmd(el.getAttribute("generateNatureCmd"));
				config.setType(el.getAttribute("type"));
				config.setName(el.getAttribute("name"));
				config.setVersion(el.getAttribute("version"));
				
				// stores this new configuration in the configuration map to be returned
				storeConfiguration(configurations, config);
				
				if(logger.isDebugEnabled()) {
					logger.debug(config.toString());
				}
			} 
		}
		catch (Exception e)
		{
			if(logger.isErrorEnabled()) {
				logger.error(e.getMessage());
			}
			throw new InstallerParserException(e);
		}
		
		return configurations;
	}
	
	
	/**
	 * Store a configuration for a type. A given type can have several configurations.
	 */
	private void storeConfiguration(Map<String, List<Configuration>> configurationsMap, Configuration config) {
		
		List<Configuration> configurationsList = configurationsMap.get(config.getType());
		
		if(configurationsList == null) {
			configurationsList = new ArrayList<Configuration>();
			configurationsMap.put(config.getType(), configurationsList);
		}
		
		configurationsList.add(config);
	}
	
	
}
