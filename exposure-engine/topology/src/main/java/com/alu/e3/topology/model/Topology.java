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
package com.alu.e3.topology.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alu.e3.data.model.Instance;
import com.alu.e3.data.model.SSHKey;


/**
 * Maintains a list of instances
 *
 */
public class Topology {

	private List<Instance> instances;
	private Map<String, SSHKey> sshKeys;
	private Set<String> areas = new HashSet<String>();
	public Topology() {
		instances = new ArrayList<Instance>();
		sshKeys = new HashMap<String, SSHKey>();
	}
	
	public List<Instance> getInstances() {
		return instances;
	}
	
	public void addInstance(Instance instance) {
		instances.add(instance);
	}
	
	public Map<String, SSHKey> getKeys() {
		return sshKeys;
	}
	
	public void addKey(SSHKey key) {
		sshKeys.put(key.getName(), key);
	}
	
	/**
	 * Extract the instance list by type 'instanceType'.
	 * @param instanceType the instance type query
	 * @return the list of found instances
	 */
	public List<Instance> getInstancesByType(String instanceType) {
		List<Instance> tmpInstances = new ArrayList<Instance>();

		if (instanceType != null)
		{
			if (this.instances != null)
			{
				for (Instance instance : this.instances) 
				{
					if (instanceType.equals(instance.getType()))
					{
						tmpInstances.add(instance);
					}
				}
			}
		}
		
		return tmpInstances;
	}

	public Set<String> getAreas() {
		return areas;
	}

	public void setAreas(Set<String> areas) {
		this.areas = areas;
	}
	
}
