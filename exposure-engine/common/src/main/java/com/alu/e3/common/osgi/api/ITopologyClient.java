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
/**
 * ITopologyClient
 */
package com.alu.e3.common.osgi.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.alu.e3.common.caching.ICacheManager;
import com.alu.e3.common.caching.IEntryListener;
import com.alu.e3.data.model.Instance;
import com.alu.e3.data.topology.IInstanceListener;

public interface ITopologyClient {

	void setCacheManager(ICacheManager cacheManager);

	void addInstance(Instance inst);	
	boolean deleteInstance(Instance inst);
	
	Set<String> getAllExternalIPsOfType(String type);
	Set<String> getAllInternalIPsOfType(String type);
	List<Instance> getAllInstancesOfType(String type);
	
	Set<String> getExternalIPsOfType(String type, String area);
	Set<String> getInternalIPsOfType(String type, String area);
	List<Instance> getInstancesOfType(String type, String area);
	
	void addAreas(Set<String> areas);
	public void setMyArea(String area);				

	String getMyArea();
	Set<String> getAllAreas();
	Set<String> getAllOtherAreas();
	
	void addInstanceTypeListener(IEntryListener<String, ArrayList<Instance>> listener);
	void removeInstanceTypeListener(IEntryListener<String, ArrayList<Instance>> listener);

	void addInstanceListener(IInstanceListener listener);
	void removeInstanceListener(IInstanceListener listener);
	
	public Instance whoAmI(String type);
}
