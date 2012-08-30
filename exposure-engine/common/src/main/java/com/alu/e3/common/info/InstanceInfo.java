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
 * 
 */
package com.alu.e3.common.info;

import com.alu.e3.common.osgi.api.IInstanceInfo;

public class InstanceInfo implements IInstanceInfo {

	protected boolean isGateway = false;
	protected boolean isSpeaker = false;
	protected boolean isManager = false;
	protected IGatewayInfo gatewayInfo;
	protected ISpeakerInfo speakerInfo;
	protected IManagerInfo managerInfo;
	
	public InstanceInfo() {}
	
	@Override
	public boolean isGateway() {
		return isGateway;
	}

	@Override
	public void setGateway(boolean isGateway) {
		this.isGateway = isGateway;
	}
	

	@Override
	public boolean isSpeaker() {
		return isSpeaker;
	}

	@Override
	public void setSpeaker(boolean isSpeaker) {
		this.isSpeaker = isSpeaker;
	}
	
	@Override
	public boolean isManager() {
		return isManager;
	}

	@Override
	public void setManager(boolean isManager) {
		this.isManager = isManager;
	}

	@Override
	public IGatewayInfo getGatewayInfo() {
		return gatewayInfo;
	}

	@Override
	public void setGatewayInfo(IGatewayInfo gatewayInfo) {
		this.gatewayInfo = gatewayInfo;
	}

	@Override
	public ISpeakerInfo getSpeakerInfo() {
		return speakerInfo;
	}

	@Override
	public void setSpeakerInfo(ISpeakerInfo speakerInfo) {
		this.speakerInfo = speakerInfo;
	}

	@Override
	public IManagerInfo getManagerInfo() {
		return managerInfo;
	}

	@Override
	public void setManagerInfo(IManagerInfo managerInfo) {
		this.managerInfo = managerInfo;
	}
}
