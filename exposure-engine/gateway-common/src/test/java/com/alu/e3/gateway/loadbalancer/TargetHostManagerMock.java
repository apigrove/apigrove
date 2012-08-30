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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alu.e3.data.model.sub.TargetHost;

public class TargetHostManagerMock implements ITargetHostManager {

	private Map<String, TargetReference> targets;
	private int index;
	
	public TargetHostManagerMock() {
		targets = new HashMap<String,TargetReference>();
		index = 0;
	}

	@Override
	public void notifyFailed(String targetHostReference) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isAvailable(String targetReference) {
		
		TargetReference targetRef = targets.get(targetReference);
		boolean available = true;
		if(targetRef != null) {
			String url = targetRef.getTargetHost().getUrl();
			if(url.contains("not_available")) {
				available = false; // simulate not available
			}
		}
		
		return available;
	}

	public String registerTargetHost(TargetHost targetHost) {
		
		String reference = Integer.toString(index);
		index++;
		
		TargetReference targetRef = new TargetReference();
		targetRef.setReference(reference);
		targetRef.setTargetHost(targetHost);
		
		targets.put(reference, targetRef);
		
		return reference;
	}


	@Override
	public List<TargetReference> getTargetReferences(String apiId,
			String contextId) {
		// TODO Auto-generated method stub
		return null;
	}



}
