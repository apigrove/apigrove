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
package com.alu.e3.gateway.loadbalancer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.alu.e3.common.caching.ICacheManager;
import com.alu.e3.common.caching.IEntryListener;
import com.alu.e3.common.osgi.api.ITopologyClient;
import com.alu.e3.data.model.Instance;
import com.alu.e3.data.topology.IInstanceListener;

public class TopologyClientMock implements ITopologyClient {

	
	private String myArea;
	
	
	@Override
	public void setCacheManager(ICacheManager cacheManager) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addInstance(Instance inst) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean deleteInstance(Instance inst) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<String> getAllExternalIPsOfType(String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getAllInternalIPsOfType(String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Instance> getAllInstancesOfType(String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getExternalIPsOfType(String type, String area) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getInternalIPsOfType(String type, String area) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Instance> getInstancesOfType(String type, String area) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addAreas(Set<String> areas) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMyArea(String area) {
		this.myArea = area;		
	}

	@Override
	public String getMyArea() {
		return myArea;
	}

	@Override
	public Set<String> getAllAreas() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getAllOtherAreas() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addInstanceTypeListener(
			IEntryListener<String, ArrayList<Instance>> listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeInstanceTypeListener(
			IEntryListener<String, ArrayList<Instance>> listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addInstanceListener(IInstanceListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeInstanceListener(IInstanceListener listener) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.alu.e3.common.osgi.api.ITopologyClient#whoAmI(java.lang.String)
	 */
	@Override
	public Instance whoAmI(String type) {
		// TODO Auto-generated method stub
		return null;
	}
	

}
