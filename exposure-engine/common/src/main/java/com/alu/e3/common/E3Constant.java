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
package com.alu.e3.common;

import com.alu.e3.data.model.enumeration.ActionType;

public class E3Constant {

	public static final String E3GATEWAY = "E3Gateway"; // represents a gateway ready to be provisioned but not yet active
	public static final String E3SPEAKER = "E3Speaker"; // represents a speaker ready for election but not yet active
	public static final String E3GATEWAY_ACTIVE = "E3GatewayA"; // represents an active gateway, i.e. ready to receive external connections
	public static final String E3SPEAKER_ACTIVE = "E3SpeakerA"; // represents an active speaker
	public static final String E3MANAGER = "E3Manager"; // represents the active manager
	public static final String TDR_COLLECTOR = "TDRCollector"; // the downstream tdr collection machine
	public static final String localhost = "localhost";		// the ip-address we use for the localhost

	public static final String DATA_STORAGE = "DataStorage";
	public static final String HAZELCAST_NAME = "E3Hz";
	public static final int HAZELCAST_PORT = 15701;
	public static final int HAZELCAST_HANDLER_POOL_MAX_SIZE = 150;
	public static final String SETTING_GATEWAY_NUMBER = "GatewayNumber";
	public static final int CACHE_ACK_TIMEOUT = 300000;
	
	public static final String GLOBAL_PROXY_SETTINGS = "GlobalProxySettings";
	
	public static final int HEALTH_CHECK_POLLING_INTERVAL = 10000; // 10s
	
	public static final int GATEWAY_INTERNAL_DEFAULT_HEALTH_CHECK_PORT = 8082;
	public static final int GATEWAY_DEFAULT_HEALTH_CHECK_PORT = 8083;
	public static final int SPEAKER_DEFAULT_HEALTH_CHECK_PORT = 8084;
	public static final int MANAGER_DEFAULT_HEALTH_CHECK_PORT = 8085;

    public static final String AGGREGATE_LOG_LOCATION = "/tmp/aggregate.log";
    
    public final static ActionType DEFAULT_ERROR_ACTION = ActionType.REJECT;
    
    // Headers
	public final static String CORRELATION_ID_HEADER_NAME = "X-LogCorrelationID";
	public final static String SUBSCRIBER_ID_HEADER_NAME = "X-SubscriberID";
	public final static String NOTIFY_URL_HEADER_NAME = "notifyUrl";
	public final static String DEFAULT_APP_AUTHKEY_HEADER_NAME = "X-App-AuthKey";
	public final static String HEADER_X_FORWARDED_FOR = "X-FORWARDED-FOR";
	
	
	// TDR Collection Locations
	public static final String TDR_BASE_PATH = "data/TDR";
	// These are reflected in the TDR Transfer Script.  Do not modify unless the script's config is also modified
	public static final String TDR_TRANSFER_CONFIG_PATH = "/home/e3/TDR_ProcessScript/config";
	public static final String TDR_TRANSFER_CONFIG_FILE = "dynamic_config";
	public static final String TDR_TRANSFER_CONFIG_KEY = "dynamic_key.pem";
	
	public static final String REST_API_VERSION = "v1";
	
	public static final String IS_MANAGER_PROPERTY_ENTRY = "e3.manager";
	public static final String IS_GATEWAY_PROPERTY_ENTRY = "e3.gateway";
	
	public static final String MANAGER_IP_REPLACE_PATTERN = "%IP%";
	
	public static final int DEFAULT_HTTP_CONNECTION_TIMETOUT = 5000;
	public static final int DEFAULT_HTTP_SOCKET_TIMEOUT = 5000;
}
