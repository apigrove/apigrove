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
package com.alu.e3.topology.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.alu.e3.data.model.Instance;
import com.alu.e3.data.model.SSHKey;
import com.alu.e3.topology.model.Topology;

public class TopologyDescriptorParser {
	
	private static final Logger logger = LoggerFactory.getLogger(TopologyDescriptorParser.class);
	
	
	public Topology parse(String topologyFilePath) throws TopologyParserException, UnsupportedEncodingException {
		logger.debug("Parsing file at path: " + topologyFilePath);
		
		FileInputStream fin = null;
		
		try {
			fin = new FileInputStream(topologyFilePath);
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
			throw new TopologyParserException(e.getMessage());
		}
		
		return parse(fin);
	}
	
	public Topology parse(InputStream topologyInputStream) throws TopologyParserException, UnsupportedEncodingException {
		Topology topology = null;
		Element rootElement = getRootElement(topologyInputStream);
		
		String type = rootElement.getAttribute("type");
		logger.debug("Topology type: " + type);
		
		TopologyParser parser = getParser(type);
		
		topology = parser.parseElement(rootElement);
		
		return topology;
	}
	
	
	private Element getRootElement(InputStream inputStream) throws TopologyParserException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
	
			Document labDoc = builder.parse(inputStream);
			Element rootElement = labDoc.getDocumentElement();
			
			return rootElement;
		
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new TopologyParserException(e.getMessage());
		}

	}
	
	private TopologyParser getParser(String type) throws TopologyParserException {
		TopologyParser parser = null;

		parser = new CommonTopologyParser();		
		return parser;
	}
	
	abstract class TopologyParser {
		abstract Topology parseElement(Element rootElement) throws TopologyParserException, UnsupportedEncodingException;
	}
	
	class CommonTopologyParser extends TopologyParser {
		
		@Override
		Topology parseElement(Element rootElement) throws TopologyParserException, UnsupportedEncodingException {
			Topology topology = new Topology();
			
			// Finding keypair to use
			NodeList keyNodes = rootElement.getElementsByTagName("Key");
			logger.debug("Found " + keyNodes.getLength() + " SSH keys");
			for(int i = 0; i < keyNodes.getLength(); i++)
			{
				Element keyElement = (Element) keyNodes.item(i);
				if(keyElement == null) {
					continue;
				}
	
				String strPrivateKey = "", strPublicKey = "";
				String strKeyName = keyElement.getAttribute("name");
				String privateKeyPath = keyElement.getAttribute("privatekeypath");
				if (privateKeyPath != null && !privateKeyPath.isEmpty())
				{
					String strKeyFormat = keyElement.getAttribute("format");
					privateKeyPath += File.separatorChar + strKeyName + "." + strKeyFormat;
					
					try {
						StringBuffer fileContent = new StringBuffer(1000);
				        BufferedReader br = new BufferedReader(new FileReader(privateKeyPath));
				        char[] buf = new char[1024];
				        int nRead=0;
				        while((nRead=br.read(buf)) != -1){
				        	fileContent.append(buf, 0, nRead);
				        }
				        br.close();
				        strPrivateKey = fileContent.toString();
			        		
			        		
					} catch (Exception e) {
						logger.error(e.getMessage());
						throw new TopologyParserException(e.getMessage());
					}
				}	
				else
				{
					NodeList privateKeyNode = keyElement.getElementsByTagName("PrivateKey");
					strPublicKey = keyElement.getElementsByTagName("PublicKey").item(0).getTextContent();
					strPrivateKey = (privateKeyNode == null || privateKeyNode.getLength() == 0) ? null : privateKeyNode.item(0).getTextContent();
				}

				SSHKey key = new SSHKey(strKeyName, strPrivateKey == null ? null : strPrivateKey.getBytes("UTF-8"), strPublicKey.getBytes("UTF-8"));
				topology.addKey(key);
				
				logger.debug(key.toString());
			}
			

			NodeList labNodes = rootElement.getElementsByTagName("Node");
			logger.debug("Found " + labNodes.getLength() + " nodes in this topology");
			
			HashSet<String> instancesNameAlreadyAdded = new HashSet<String>();
			
			for(int i = 0; i < labNodes.getLength(); i++)
			{
				Instance instance = new Instance();
				Node node = labNodes.item(i);

				instance.setName(node.getAttributes().getNamedItem("name").getTextContent());
				
				if (instancesNameAlreadyAdded.contains(instance.getName()))
				{
					throw new TopologyParserException("Each node name in the topology configuration file must be unique. The name " + instance.getName() + "is set to more than one node");
				}

				for(int j = 0; j < node.getChildNodes().getLength(); j++)
				{
					Node child = node.getChildNodes().item(j);
					if(child.getNodeName().equals("Type"))
					{
						instance.setType(child.getAttributes().getNamedItem("type").getTextContent());
					}
					else if(child.getNodeName().equals("ExternalDNS"))
					{
						instance.setExternalDNS(child.getAttributes().getNamedItem("url").getTextContent());
					}
					else if(child.getNodeName().equals("ExternalIP"))
					{
						instance.setExternalIP(child.getAttributes().getNamedItem("ip").getTextContent());
					}
					else if(child.getNodeName().equals("InternalIP"))
					{
						instance.setInternalIP(child.getAttributes().getNamedItem("ip").getTextContent());
					}
					else if(child.getNodeName().equals("Port"))
					{
						instance.setPort(child.getAttributes().getNamedItem("port").getTextContent());
					}
					else if(child.getNodeName().equals("Area"))
					{
						instance.setArea(child.getAttributes().getNamedItem("name").getTextContent());
						topology.getAreas().add(child.getAttributes().getNamedItem("name").getTextContent());
					}	
					else if(child.getNodeName().equals("Credentials"))
					{
						if (child.getAttributes().getNamedItem("key") != null) {
							instance.setSSHKeyName(child.getAttributes().getNamedItem("key").getTextContent());
							instance.setSSHKey(topology.getKeys().get(instance.getSSHKeyName()));
						}
						if (child.getAttributes().getNamedItem("user") != null) {
							instance.setUser(child.getAttributes().getNamedItem("user").getTextContent());
						}
						if (child.getAttributes().getNamedItem("password") != null) {
							instance.setPassword(child.getAttributes().getNamedItem("password").getTextContent());
						}
					}
					else if(child.getNodeName().equals("KeyUsed"))
					{
						if (child.getAttributes().getNamedItem("key") != null) {
							instance.setSSHKeyName(child.getAttributes().getNamedItem("key").getTextContent());
							instance.setSSHKey(topology.getKeys().get(instance.getSSHKeyName()));
						}
						if (child.getAttributes().getNamedItem("user") != null) {
							instance.setUser(child.getAttributes().getNamedItem("user").getTextContent());
						}
						if (child.getAttributes().getNamedItem("password") != null) {
							instance.setPassword(child.getAttributes().getNamedItem("password").getTextContent());
						}
					}
					
				}

				logger.debug(instance.toString());
				
				topology.addInstance(instance);
			}
			
			return topology;
		}
	}
}
