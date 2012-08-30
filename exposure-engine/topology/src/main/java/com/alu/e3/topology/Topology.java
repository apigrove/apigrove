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
package com.alu.e3.topology;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.alu.e3.common.E3Constant;
import com.alu.e3.common.osgi.api.ITopologyClient;
import com.alu.e3.common.tools.CommonTools;
import com.alu.e3.data.model.Instance;
import com.alu.e3.osgi.api.ITopology;
import com.alu.e3.topology.model.ITopologyListener;
import com.alu.e3.topology.parsers.TopologyDescriptorParser;

public class Topology implements ITopology 
{
	private com.alu.e3.topology.model.Topology topology;
	private com.alu.e3.topology.model.Topology systemTopology;
	
	private LinkedList<ITopologyListener> listeners = new LinkedList<ITopologyListener>();
	
	private ITopologyClient topologyClient;
	
	/**
	 * setTopologyClient
	 */
	
	public void setTopologyClient(ITopologyClient topologyClient) {
		this.topologyClient = topologyClient;

		if (topology != null) {
			saveTopologyInCache(topology.getInstances());
		}
		
		if (systemTopology != null) {
			saveTopologyInCache(systemTopology.getInstances());
		}

	}
	
	@Override
	public List<String> getNodeAccessDescriptor(String nodeType) 
	{
		if (nodeType == null)
		{
			return null;
		}
		
		List<String> result = null;
		
		if (systemTopology != null)
		{
			result = getNodeAccessDescriptor_internal(nodeType, systemTopology.getInstances());			
		}
		
		if (result == null || result.isEmpty())
		{
			if (topology != null)
			{
				result = getNodeAccessDescriptor_internal(nodeType,	topology.getInstances());				
			}
		}

		return result;
	}

	@Override
	public List<Instance> getInstancesByType(String instanceType) 
	{
		List<Instance> instances = new ArrayList<Instance>();
		
		if (systemTopology != null) {
			instances.addAll(systemTopology.getInstancesByType(instanceType));
		}
		if (topology != null) {
			instances.addAll(topology.getInstancesByType(instanceType));
		}

		return instances;
	}
	
	@Override
	public boolean setTopology(String path) 
	{
		if (path.contains("null"))
		{
			path = System.getProperty("user.home") + File.separator + "topology.xml";
		}
		
		com.alu.e3.topology.model.Topology tmpTopology = null;
		TopologyDescriptorParser parser = new TopologyDescriptorParser();
		try 
		{
			tmpTopology = parser.parse(path);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return setTopology(tmpTopology);
	}

	@Override
	public synchronized boolean setTopology(com.alu.e3.topology.model.Topology tmpTopology) {
		if (tmpTopology == null)
		{
			return false;
		}
		
		this.topology = tmpTopology;
		
		
		if (systemTopology != null)
		{
			// Store in cache
			saveTopologyInCache();

			// both topology and systemTopology are set, notify listeners
			notifyReady();
		}
		
		return true;
	}
	
	@Override
	public synchronized boolean setSystemTopology(String path) 
	{
		if (systemTopology != null)
		{
			return false;
		}

		if (path.contains("null"))
		{
			path = System.getProperty("user.home") + File.separator + "system_topology.xml";
		}

		TopologyDescriptorParser parser = new TopologyDescriptorParser();
		try 
		{
			systemTopology = parser.parse(path);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (systemTopology == null)
		{
			return false;
		}
		
		
		if (topology != null)
		{
			// Store in cache
			saveTopologyInCache();

			// both topology and systemTopology are set, notify listeners			
			notifyReady();
		}
		
		return true;
	}
	
	/**
	 * Save a topology in the cache of data manager.
	 */
	private void saveTopologyInCache() {
		if (topologyClient == null) {
			return;
		}
		
		// my area + all areas		
		List<Instance> instances = topology.getInstances();
		Set<String> areas = new HashSet<String>();
		
		List<Instance> gateways = new ArrayList<Instance>();
		List<Instance> others = new ArrayList<Instance>();
		for (Instance instance : instances){
			areas.add(instance.getArea());
			if(E3Constant.E3GATEWAY.equals(instance.getType())){
				gateways.add(instance);
			}else{
				others.add(instance);
			}
		}
		topologyClient.addAreas(areas);
		String myArea = initMyArea(instances);
		topologyClient.setMyArea(myArea);
		
		// first gateways 
		saveTopologyInCache(gateways);
		// machines inside E3 (managers, speakers ...)
		saveTopologyInCache(others);
		// machines outside of E3 (DataStorage ...)
		saveTopologyInCache(systemTopology.getInstances());
	}
	
	/**
	 * Save a topology in the cache of data manager.
	 */
	private void saveTopologyInCache(List<Instance> instances) {

		if (topologyClient == null) {
			return;
		}
		
		//topologyClient.addAreas(topology.getAreas());
		for (Instance instance : instances) 
			topologyClient.addInstance(instance);
	}

	private List<String> getNodeAccessDescriptor_internal(String nodeType, List<Instance> instances) 
	{
		ArrayList<String> nodes = new ArrayList<String>();

		for (Instance instance : instances) 
		{
			if (nodeType.equals(instance.getType()))
			{
				String nd = "";

				if (instance.getExternalDNS() != null)
				{
					nd += "ip=" + instance.getExternalDNS();
				}
				else
				{
					nd += "ip=" + instance.getExternalIP();
				}

				if (instance.getPort() != null)
				{
					nd += "&port=" + instance.getPort();
				}

				if (instance.getUser() != null)
				{
					nd += "&user="
							+ org.apache.cxf.jaxrs.utils.HttpUtils
									.urlEncode(instance.getUser());
				}

				if (instance.getPassword() != null)
				{
					nd += "&password="
							+ org.apache.cxf.jaxrs.utils.HttpUtils
									.urlEncode(instance.getPassword());
				}

				nodes.add(nd);
			}
		}

		return nodes;
	}

	@Override
	public void addTopologyListener(ITopologyListener listener)
	{
		listeners.add(listener);
		
		if (topology != null && systemTopology != null)
		{
			listener.onReady();
		}
	}

	@Override
	public void removeTopologyListener(ITopologyListener listener)
	{
		listeners.remove(listener);
	}
	
	@Override
	public com.alu.e3.topology.model.Topology getTopologyFromFile(String absolutePath) {
		TopologyDescriptorParser parser = new TopologyDescriptorParser();
		try 
		{
			return parser.parse(absolutePath);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException("Unable to parse the given topology file:"+absolutePath, e);
		}
	}
	
	private void notifyReady()
	{
		for (ITopologyListener listener : listeners)
		{
			listener.onReady();
		}
	}
	
	private String initMyArea(List<Instance> instances){
		String currentArea = null;

		for (Instance inst : instances) {
			if (CommonTools.isLocal(inst.getExternalIP()) || CommonTools.isLocal(inst.getInternalIP())) {
				currentArea = inst.getArea();
				return currentArea;						
			}
		}

		return currentArea;
	}
	
}
