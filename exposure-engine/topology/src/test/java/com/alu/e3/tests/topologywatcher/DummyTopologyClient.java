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
package com.alu.e3.tests.topologywatcher;

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
import com.alu.e3.data.model.Instance;
import com.alu.e3.data.topology.IInstanceListener;
import com.alu.e3.data.topology.InstanceEvent;

public class DummyTopologyClient implements ITopologyClient {
	
	private List<Instance> gateways = new LinkedList<Instance>();
	private List<Instance> gatewaysActive = new LinkedList<Instance>();
	private List<Instance> speakers = new LinkedList<Instance>();
	private List<Instance> speakersActive = new LinkedList<Instance>();
	
	public void setGateways(List<Instance> gateways) {
		this.gateways = gateways;
	}
	
	public void setSpeakers(List<Instance> speakers) {
		this.speakers = speakers;
	}

	@Override
	public void setCacheManager(ICacheManager cacheManager) {
		// must not be called
		throw new RuntimeException();
	}

	@Override
	public void addInstance(Instance inst) {
		if (inst.getType().equals(E3Constant.E3GATEWAY)) {
			gateways.add(inst);
		}
		else if (inst.getType().equals(E3Constant.E3GATEWAY_ACTIVE)) {
			gatewaysActive.add(inst);
		}
		else if (inst.getType().equals(E3Constant.E3SPEAKER)) {
			speakers.add(inst);
		}
		else if (inst.getType().equals(E3Constant.E3SPEAKER_ACTIVE)) {
			speakersActive.add(inst);
		}
		
		fireInstanceAdded(new InstanceEvent(inst));
	}

	@Override
	public boolean deleteInstance(Instance inst) {
		List<Instance> list = null;
		
		if (inst.getType().equals(E3Constant.E3GATEWAY)) {
			list = gateways;
		}
		else if (inst.getType().equals(E3Constant.E3GATEWAY_ACTIVE)) {
			list = gatewaysActive;
		}
		else if (inst.getType().equals(E3Constant.E3SPEAKER)) {
			list = speakers;
		}
		else if (inst.getType().equals(E3Constant.E3SPEAKER_ACTIVE)) {
			list = speakersActive;
		}
		
		if (list == null)
			return false;
		
		Iterator<Instance> itr = list.iterator();
		while (itr.hasNext()) {
			if (itr.next().getInternalIP().equals(inst.getInternalIP())) {
				itr.remove();
		fireInstanceRemoved(new InstanceEvent(inst));
				return true;
			}
		}
		return false;		
		
	}

	@Override
	public void addInstanceTypeListener(IEntryListener<String, ArrayList<Instance>> listener) {
		// must not be called
		throw new RuntimeException();
	}

	@Override
	public void removeInstanceTypeListener(IEntryListener<String, ArrayList<Instance>> listener) {
		// must not be called
		throw new RuntimeException();
	}
	
	private LinkedList<IInstanceListener> listeners = new LinkedList<IInstanceListener>();

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
		else if (type.equals(E3Constant.E3SPEAKER)) {
			instances = speakers;
		}
		else if (type.equals(E3Constant.E3GATEWAY_ACTIVE)) {
			instances = gatewaysActive;
		}
		else if (type.equals(E3Constant.E3SPEAKER_ACTIVE)) {
			instances = speakersActive;
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
	public Set<String> getAllInternalIPsOfType(String type) {
		// must not be called
		throw new RuntimeException();
	}

	@Override
	public List<Instance> getAllInstancesOfType(String type) {
		if (type.equals(E3Constant.E3GATEWAY)) {
			return gateways;
		}
		else if (type.equals(E3Constant.E3SPEAKER)) {
			return speakers;
		}
		else if (type.equals(E3Constant.E3GATEWAY_ACTIVE)) {
			return gatewaysActive;
		}
		else if (type.equals(E3Constant.E3SPEAKER_ACTIVE)) {
			return speakersActive;
		}
		return null;
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
	public String getMyArea() {
		return "myArea";
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