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
package com.alu.e3.data;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alu.e3.common.caching.ICacheManager;
import com.alu.e3.common.caching.ICacheTable;
import com.alu.e3.common.caching.IEntryListener;
import com.alu.e3.common.osgi.api.ITopologyClient;
import com.alu.e3.common.tools.CanonicalizedIpAddress;
import com.alu.e3.common.tools.CommonTools;
import com.alu.e3.data.model.Instance;
import com.alu.e3.data.topology.IInstanceListener;
import com.alu.e3.data.topology.InstanceEvent;

/**
 * Class to access/set topology information in cache
 */
public class TopologyClient implements ITopologyClient {

	private static final Logger logger = LoggerFactory.getLogger(TopologyClient.class);
	
	private ICacheTable<String, ArrayList<Instance>> cachingTableTopology;

	/* area of this instance. */
	private String currentArea;
	private Set<String> currentAreaList;
	
	private ICacheManager cacheManager;

	private Map<String,Instance> whoIAm = new HashMap<String,Instance>();

	public TopologyClient() {}
	
	/**
	 * Set the cache manager
	 */
	public void setCacheManager(ICacheManager cacheManager) {
		logger.debug("Set ICacheManager on TopologyClient");
		this.cacheManager = cacheManager;
	}
	
	public void init() {
		logger.debug("TopologyClient initialization");
		cachingTableTopology = cacheManager.createTable("cachingTableTopology", true, null);
	}

	/**
	 * Add an instance
	 */
	public void addInstance(Instance inst) {
		logger.debug("Adding instance: {}", inst);
		
		if (inst == null || inst.getType().isEmpty() || inst.getInternalIP().isEmpty())
			throw new IllegalArgumentException("Invalid type or ip");

		ArrayList<Instance> list = cachingTableTopology.get(inst.getType());
		if (list == null)
		{
			logger.debug("Creating cachingTableTopology element for type: {}", inst.getType());
			cachingTableTopology.set(inst.getType(), new ArrayList<Instance>());
			list = cachingTableTopology.get(inst.getType());
		}

		for (Instance i : list)
		{
			if ( i.getName().equals(inst.getName()) )
			{
				deleteInstance(i);
				break;
			}
		}
		
		list.add(inst);
		cachingTableTopology.set(inst.getType(), list);
		
		fireInstanceAdded(new InstanceEvent(inst));
		
		// This is to clean the current cache
		synchronized (this) {
			currentAreaList = null;
		}

		logger.debug("Instance added: {}", inst);
	}

	/**
	 * Deletes an instance
	 */
	public boolean deleteInstance(Instance inst) {
		ArrayList<Instance> list = cachingTableTopology.get(inst.getType());
		if (list == null)
			return false;

		Iterator<Instance> itr = list.iterator();
		while (itr.hasNext()) {
			if (itr.next().getName().equals(inst.getName())) {
				itr.remove();
				cachingTableTopology.set(inst.getType(), list);
				fireInstanceRemoved(new InstanceEvent(inst));
				ArrayList<Instance> list22 = cachingTableTopology.get(inst.getType());
				// This is to clean the current cache
				currentAreaList = null;
				
				return true;
			}
		}
		return false;
	}

	/**
	 * Get all instances info of a type.
	 */
	@Override
	public List<Instance> getAllInstancesOfType(String type) {

		if (type == null)
			throw new IllegalArgumentException("Invalid type");

		return cachingTableTopology.get(type);
	}

	/**
	 * Get all instances info of a type.
	 */
	@Override
	public Set<String> getAllInternalIPsOfType(String type) {

		return this.getInternalIPsOfType(type, null);
	}

	/**
	 * Get all instances info of a type in a given area
	 */
	@Override
	public Set<String> getInternalIPsOfType(String type, String area) {

		if (type == null)
			throw new IllegalArgumentException("Invalid type");

		Set<String> set = new HashSet<String>();

		ArrayList<Instance> list = cachingTableTopology.get(type);
		if (list != null)
		{
			/* copy now the array to avoid concurency problems. */
			Instance[] tabInstances = new Instance[0];
			tabInstances = list.toArray(tabInstances);

			for (int i = 0; i < tabInstances.length; i++) {
				Instance inst = tabInstances[i];

				if (area == null || area.equals(inst.getArea()))
					set.add(inst.getInternalIP());
			}
		}

		return set;
	}

	/**
	 * Get all instances info of a type.
	 */
	@Override
	public Set<String> getAllExternalIPsOfType(String type) {

		return this.getExternalIPsOfType(type, null);
	}

	/**
	 * Get all instances info of a type in a given area
	 */
	@Override
	public Set<String> getExternalIPsOfType(String type, String area) {

		if (type == null)
			throw new IllegalArgumentException("Invalid type");

		Set<String> set = new HashSet<String>();

		ArrayList<Instance> list = cachingTableTopology.get(type);
		if (list != null)
		{
			/* copy now the array to avoid concurency problems. */
			Instance[] tabInstances = new Instance[0];
			tabInstances = list.toArray(tabInstances);

			for (int i = 0; i < tabInstances.length; i++) {
				Instance inst = tabInstances[i];

				if (area == null || area.equals(inst.getArea()))
					set.add(inst.getExternalIP());
			}
		}

		return set;
	}

	/**
	 * Get all instances info of a type in a given area.
	 */
	@Override
	public List<Instance> getInstancesOfType(String type, String area) {

		if (type == null)
			throw new IllegalArgumentException("Invalid type");

		/* copy now the array to avoid concurency problems. */
		Instance[] tabInstances = new Instance[0];
		
		ArrayList<Instance> tmp = cachingTableTopology.get(type);
		if(tmp != null)
			tabInstances = tmp.toArray(tabInstances);
		
		
		ArrayList<Instance> list = new ArrayList<Instance>();

		for (int i = 0; i < tabInstances.length; i++) {
			Instance inst = tabInstances[i];

			if (area == null || area.equals(inst.getArea()))
				list.add(inst);
		}

		return list;
	}

	/**
	 * Get the current area.
	 */
	@Override
	public String getMyArea() {

		if (currentArea != null)
			return currentArea;

		Iterator<String> itKeys = cachingTableTopology.getAllKeys().iterator();
		
		while (itKeys.hasNext()) {
			Instance[] tabInstances = new Instance[0];
			tabInstances = cachingTableTopology.get(itKeys.next()).toArray(tabInstances);

			for (int i = 0; i < tabInstances.length; i++) {
				Instance inst = tabInstances[i];

				if (CommonTools.isLocal(inst.getExternalIP()) || CommonTools.isLocal(inst.getInternalIP())) {
					currentArea = inst.getArea();
					return currentArea;
				}
			}
		}

		/* Area not found or error while fetching ip address. */
		return null;
	}

	/**
	 * Get all the area available
	 */
	@Override
	public synchronized Set<String> getAllAreas() {

		if (currentAreaList != null)
			return currentAreaList;

			currentAreaList = new HashSet<String>();

			Iterator<String> itKeys = cachingTableTopology.getAllKeys().iterator();
			while (itKeys.hasNext()) {

				Instance[] tabInstances = new Instance[0];
				tabInstances = cachingTableTopology.get(itKeys.next()).toArray(tabInstances);

				for (int i = 0; i < tabInstances.length; i++) {
					String area = tabInstances[i].getArea();
					if (area != null && !area.isEmpty())
						currentAreaList.add(area);
				}
			}
		return currentAreaList;
	}

	/**
	 * Get all the other areas available
	 */
	@Override
	public Set<String> getAllOtherAreas() {

		Set<String> allAreas = this.getAllAreas();
		Set<String> tmp = new HashSet<String>(allAreas);
		String myArea = this.getMyArea();
		tmp.remove(myArea);

		return tmp;
	}

	/**
	 * Add listener to any change of the topology in cache
	 */
	public void addInstanceTypeListener(IEntryListener<String, ArrayList<Instance>> listener) {
		cachingTableTopology.addEntryListener(listener);
	}

	/**
	 * Remove listener.
	 */
	public void removeInstanceTypeListener(IEntryListener<String, ArrayList<Instance>> listener) {
		cachingTableTopology.removeEntryListener(listener);
	}

	/**
	 * The list of instance listeners
	 */
	private LinkedList<IInstanceListener> instanceListeners = new LinkedList<IInstanceListener>();

	/**
	 * Add an instance listener
	 */
	@Override
	public void addInstanceListener(IInstanceListener listener) {
		instanceListeners.add(listener);
	}

	/**
	 * Remove an instance listener
	 */
	@Override
	public void removeInstanceListener(IInstanceListener listener) {
		instanceListeners.remove(listener);
	}

	/**
	 * Fire the instanceAdded event
	 */
	private void fireInstanceAdded(final InstanceEvent event) {
		logger.debug("Firing instance added event for instance:{}", event.getInstance());

		IInstanceListener[] currentListOfListeners = instanceListeners.toArray(new IInstanceListener[]{});
		
		for(IInstanceListener listener : currentListOfListeners) {
			listener.instanceAdded(event);
		}
	}

	/**
	 * Fire the instanceRemoved event
	 */
	private void fireInstanceRemoved(final InstanceEvent event) {
		logger.debug("Firing instance removed event for instance:{}", event.getInstance());
		
		IInstanceListener[] currentListOfListeners = instanceListeners.toArray(new IInstanceListener[]{});
		
		for(IInstanceListener listener : currentListOfListeners) {
			listener.instanceRemoved(event);
		}
	}

	@Override
	public synchronized void addAreas(Set<String> areas) {					
		if(currentAreaList == null){
			currentAreaList = new HashSet<String>();			
		}
		this.currentAreaList.addAll(areas);
	}
	
	public synchronized void setMyArea(String area) {					
		
		this.currentArea = area;
	}
	
	// If the topology hasn't entered the data cache, this function will
	// NOT return whoIAm, it will return null.  So, if you use this, you
	// MUST include some sort of failover ID for this case
	public Instance whoAmI(String type) {
		if (whoIAm.get(type) != null) {
			return whoIAm.get(type);
		}
		logger.debug("Trying to find my Instance for type: " + type);

		List<Instance> instances;
		instances = getAllInstancesOfType(type);
		if (instances == null) {
			logger.debug("Found no Instances of type.");
			return null;
		}
		//add all possible IPs (and hostname) to this map for easy retrieval
		Map<String,Instance> ipToInstance = new HashMap<String,Instance>();
		for (Instance inst : instances) {
			String internalIP = inst.getInternalIP();
			if (!CanonicalizedIpAddress.isValidIp(internalIP)) {
				try {
					internalIP = InetAddress.getByName(internalIP).getHostAddress();
				} catch (UnknownHostException e) {;}
			}
			if (!internalIP.isEmpty()) ipToInstance.put(internalIP, inst);
			
			String externalIP = inst.getExternalIP();
			// if externalIP is a hostname, try to resolve it
			if (!CanonicalizedIpAddress.isValidIp(externalIP)) {
				try {
					externalIP = InetAddress.getByName(externalIP).getHostAddress();
				} catch (UnknownHostException e) {;}
			}
			if (!externalIP.isEmpty()) ipToInstance.put(externalIP, inst);
			
			String externalDNS = inst.getExternalDNS();
			if (!externalDNS.isEmpty()) ipToInstance.put(externalDNS, inst);
		}
		
		//check network interfaces
		Enumeration<NetworkInterface> nets;
		try {
			nets = NetworkInterface.getNetworkInterfaces();
			for (NetworkInterface netint : Collections.list(nets)) {
				logger.debug("Checking Interface " + netint.getDisplayName());
				
				//check ips for each interface
		        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
		        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
		        	String myIp = inetAddress.getHostAddress();
		        	logger.debug("Checking IP : " + myIp);
		        	
		        	//if there is an instance associated with myIP, that is me
		        	Instance instance = ipToInstance.get(myIp);
		        	if (instance != null) {
			        	whoIAm.put(type, instance);
			        	logger.debug("Found an IP match with Instance " + instance.getName());
		        		return instance;
		        	}
		        }
	        }
		} catch (SocketException e) {
			logger.error("Got an exception checking my Network Interfaces for matching IPs: " +
					e.getMessage());
			logger.warn(e.getMessage(), e);
		}
		logger.debug("Could not find an Instance with a matching IP");
		return null;
	}
}
