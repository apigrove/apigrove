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
package com.alu.e3.common.info;

import com.alu.e3.common.caching.internal.MapHandler;

public class RemoteInstanceInfo {
	
	public static String getGatewayStatus(String ip) {
		MapHandler<String,String> mapHandler = new MapHandler<String,String>(ip, IGatewayInfo.GATEWAY_STATUS_TABLE_NAME);
		
		return (mapHandler.getMap() == null ? GatewayStatus.DOWN : mapHandler.getMap().get(IGatewayInfo.GATEWAY_STATUS_CACHE_ENTRY));
	}
	
	public static void setGatewayStatus(String ip, String status) {
		MapHandler<String, String> mapHandler = new MapHandler<String, String>(ip, IGatewayInfo.GATEWAY_STATUS_TABLE_NAME);
				
		if (mapHandler != null && mapHandler.getMap() != null) {
			mapHandler.getMap().put(IGatewayInfo.GATEWAY_STATUS_CACHE_ENTRY, status);
		}
	}
	
	
	public static String getSpeakerStatus(String ip) {
		MapHandler<String,String> mapHandler = new MapHandler<String,String>(ip, ISpeakerInfo.SPEAKER_STATUS_TABLE_NAME);
		
		return (mapHandler.getMap() == null ? SpeakerStatus.DOWN : mapHandler.getMap().get(ISpeakerInfo.SPEAKER_STATUS_CACHE_ENTRY));
	}
	
	public static void setSpeakerStatus(String ip, String status) {
		MapHandler<String,String> mapHandler = new MapHandler<String,String>(ip, ISpeakerInfo.SPEAKER_STATUS_TABLE_NAME);
						
		if (mapHandler.getMap() != null) {
			mapHandler.getMap().put(ISpeakerInfo.SPEAKER_STATUS_CACHE_ENTRY, status);
		}
	}
}
