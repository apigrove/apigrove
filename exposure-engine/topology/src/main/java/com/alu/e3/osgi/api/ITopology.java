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
package com.alu.e3.osgi.api;

import java.util.List;

import com.alu.e3.data.model.Instance;
import com.alu.e3.topology.model.ITopologyListener;
import com.alu.e3.topology.model.Topology;

public interface ITopology {
	
	/**
	 * 
	 * @param nodeType
	 * @return
	 */
	List<String> getNodeAccessDescriptor(String nodeType);

	/**
	 * Get the instance list by type 'instanceType'.
	 * @param instanceType the instance type query
	 * @return a list of found instances
	 */
	List<Instance> getInstancesByType(String instanceType);
	
	/**
	 * Override or set the topology.
	 * @param path, the fullpath string to the topology to set.
	 * @return true if success, false otherwise
	 */
	boolean setTopology(String path);

	/**
	 * Override or set the topology.
	 * @param tmpTopology, the Topology to set.
	 * @return true if success, false otherwise
	 */
	boolean setTopology(Topology tmpTopology);
	
	/**
	 * Override or set the system topology.
	 * @param path the fullpath string to the system topology to set.
	 * @return true if success, false otherwise
	 */
	boolean setSystemTopology(String path);
	
	/**
	 * Register a topology listener against the Topology service.
	 * @param listener the listener to register and notify
	 */
	void addTopologyListener(ITopologyListener listener);

	/**
	 * Unregister a topology listener against the Topology service.
	 * @param listener the listener to unregister
	 */
	void removeTopologyListener(ITopologyListener listener);

	/**
	 * Parse a topology file and return Topology object.
	 * @param absolutePath the fullpath string of the topology file to parse
	 * @return the constructed topology object
	 */
	Topology getTopologyFromFile(String absolutePath);		
}
