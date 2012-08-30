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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.alu.e3.common.E3Constant;
import com.alu.e3.common.caching.ICacheManager;
import com.alu.e3.common.caching.IEntryListener;
import com.alu.e3.common.osgi.api.ITopologyClient;
import com.alu.e3.data.DataEntryEvent;
import com.alu.e3.data.model.Instance;

// This file is copied from the topology project where it was used
// to test TopologyWatcher
public class DummyTopologyClient implements ITopologyClient {
	
	private List<Instance> gateways = new LinkedList<Instance>();
	private List<Instance> managers = new LinkedList<Instance>();
	private List<Instance> tdrCollectors = new LinkedList<Instance>();
	
	public void setGateways(List<Instance> gateways) {
		this.gateways = gateways;
	}
	
	public void setManagers(List<Instance> managers) {
		this.managers = managers;
	}
	
	public void setTdrCollectors(List<Instance> tdrCollectors) {
		this.tdrCollectors = tdrCollectors;
	}

	@Override
	public void addInstance(Instance inst) {
		if (inst.getType().equals(E3Constant.E3GATEWAY)) {
			gateways.add(inst);
		}
		else if (inst.getType().equals(E3Constant.E3MANAGER)) {
			managers.add(inst);
		}
		else if (inst.getType().equals(E3Constant.TDR_COLLECTOR)) {
			tdrCollectors.add(inst);
		}
		
		fireInstanceAdded(new InstanceEvent(inst));
		
		ArrayList<Instance> instanceList = new ArrayList<Instance>(1);
		instanceList.add(inst);
		fireInstanceTypeAdded(new DataEntryEvent<String, ArrayList<Instance>>("", instanceList));
		
	}

	@Override
	public boolean deleteInstance(Instance inst) {
		List<Instance> list = null;

		if (inst.getType().equals(E3Constant.E3GATEWAY)) {
			list = gateways;
		}
		else if (inst.getType().equals(E3Constant.E3MANAGER)) {
			list = managers;
		}
		else if (inst.getType().equals(E3Constant.TDR_COLLECTOR)) {
			list = tdrCollectors;
		}

		if (list == null)
			return false;

		Iterator<Instance> itr = list.iterator();
		while (itr.hasNext()) {
			if (itr.next().getInternalIP().equals(inst.getInternalIP())) {
				itr.remove();
				fireInstanceRemoved(new InstanceEvent(inst));
				ArrayList<Instance> instanceList = new ArrayList<Instance>(1);
				instanceList.add(inst);
				fireInstanceTypeRemoved(new DataEntryEvent<String, ArrayList<Instance>>("", instanceList));
				return true;
			}
		}
		return false;		

	}

	private LinkedList<IInstanceListener> listeners = new LinkedList<IInstanceListener>();
	private LinkedList<IEntryListener<String, ArrayList<Instance>>> entryListeners = new LinkedList<IEntryListener<String, ArrayList<Instance>>>();

	@Override
	public void addInstanceListener(IInstanceListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeInstanceListener(IInstanceListener listener) {
		listeners.remove(listener);
	}	
	
	private void fireInstanceAdded(InstanceEvent event) {
		for (IInstanceListener listener : listeners) {
			listener.instanceAdded(event);
		}
	}

	
	private void fireInstanceTypeAdded(DataEntryEvent<String, ArrayList<Instance>> event) {
		for (IEntryListener<String, ArrayList<Instance>> listener : entryListeners) {
			listener.entryAdded(event);
		}
	}

	private void fireInstanceTypeRemoved(DataEntryEvent<String, ArrayList<Instance>> event) {
		for (IEntryListener<String, ArrayList<Instance>> listener : entryListeners) {
			listener.entryRemoved(event);
		}
	}
	
	private void fireInstanceRemoved(InstanceEvent event) {
		for (IInstanceListener listener : listeners) {
			listener.instanceRemoved(event);
		}
	}

	@Override
	public Set<String> getAllExternalIPsOfType(String type) {
		List<Instance> instances;
		
		if (type.equals(E3Constant.E3GATEWAY)) {
			instances = gateways;
		}
		else if (type.equals(E3Constant.E3MANAGER)) {
			instances = managers;
		}
		else if (type.equals(E3Constant.TDR_COLLECTOR)) {
			instances = tdrCollectors;
		}
		else {
			return null;
		}
		
		Set<String> ips = new HashSet<String>();
		
		for (Instance instance : instances) {
			ips.add(instance.getExternalIP());
		}
		
		return ips;
	}

	@Override
	public List<Instance> getAllInstancesOfType(String type) {
		if (type.equals(E3Constant.E3GATEWAY)) {
			return gateways;
		}
		else if (type.equals(E3Constant.E3MANAGER)) {
			return managers;
		}
		else if (type.equals(E3Constant.TDR_COLLECTOR)) {
			return tdrCollectors;
		}
		return null;
	}

	@Override
	public String getMyArea() {
		return "myArea";
	}

	@Override
	public void setCacheManager(ICacheManager cacheManager) {
		// must not be called
		throw new RuntimeException();
	}

	@Override
	public void addInstanceTypeListener(IEntryListener<String, ArrayList<Instance>> listener) {
		entryListeners.add (listener);
	}

	@Override
	public void removeInstanceTypeListener(IEntryListener<String, ArrayList<Instance>> listener) {
		// must not be called
		throw new RuntimeException();
	}
	

	@Override
	public Set<String> getAllInternalIPsOfType(String type) {
		// must not be called
		throw new RuntimeException();
	}

	@Override
	public Set<String> getExternalIPsOfType(String type, String area) {
		// must not be called
		throw new RuntimeException();
	}

	@Override
	public Set<String> getInternalIPsOfType(String type, String area) {
		// must not be called
		throw new RuntimeException();
	}

	@Override
	public List<Instance> getInstancesOfType(String type, String area) {
		// must not be called
		throw new RuntimeException();
	}

	@Override
	public Set<String> getAllAreas() {
		// must not be called
		throw new RuntimeException();
	}

	@Override
	public Set<String> getAllOtherAreas() {
		// must not be called
		throw new RuntimeException();
	}

	@Override
	public void addAreas(Set<String> areas) {
		// TODO Auto-generated method stub
		throw new RuntimeException();
	}

	@Override
	public void setMyArea(String area) {
		// TODO Auto-generated method stub
		throw new RuntimeException();
	}

	@Override
	public Instance whoAmI(String type) {
		// TODO Auto-generated method stub
		return null;
	}
}