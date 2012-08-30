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
package com.alu.e3.tdr;

public class TDRConstant {
	
	// TDR fields name
	public static final String TIMESTAMP = "Timestamp";
	public static final String TRANSACTION = "TransactionID";
	public static final String SYSTEM = "SystemID";
	public static final String CLIENT = "ClientIP";
	public static final String ENDPOINT = "ClientEndpoint";
	public static final String ENDPOINT_ACTION = "APIAction";
	public static final String CLIENT_REQ_TIME = "ClientRequestTimestamp";
	public static final String TARGET_REQ_TIME = "APITargetRequestTimestamp";
	public static final String TARGET_RESP_TIME = "APITargetResponseTimestamp";
	public static final String CLIENT_RESP_TIME = "ClientResponseTimestamp";
	public static final String HTTP_CODE = "HTTPStatusCode";
	public static final String HTTP_METHOD = "HTTPMethod";
	public static final String RESP_SIZE = "PayloadSize";
	public static final String TARGET_URL = "TargetEndPoint";
	public static final String TARGET_IP = "TargetIP";
	public static final String AUTHENTICATION = "AuthMethod";
	public static final String SOAP_ACTION = "SOAPAction";
	
	public static final String OVER_QUOTA = "OverQuota";
	public static final String OVER_QUOTA_SEC = "OverSecondRateLimit";
	public static final String OVER_QUOTA_MIN = "OverMinuteRateLimit";
	public static final String OVER_QUOTA_DAY = "OverDayQuota";
	public static final String OVER_QUOTA_WEEK = "OverWeekQuota";
	public static final String OVER_QUOTA_MONTH = "OverMonthQuota";
	public static final String OVER_QUOTA_ACTION = "QuotaAction";
	
	// EventType TDR enum
	public static final String EVENT_TYPE = "EventType";
	public static final String EVENT_TYPE_OK = "Response";
	public static final String EVENT_TYPE_INTERNALERROR = "Error";
	public static final String EVENT_TYPE_TARGETTIMEOUT = "APITargetTimeout";
	public static final String EVENT_TYPE_TARGETERROR = "APITargetError";
	
	// Rate TDR fields name
	public static final String RATE_TYPE = "Type";
	public static final String RATE_LIMIT = "Threshold";
	public static final String RATE_WARNING = "Warning";
	public static final String RATE_EXPIRATION = "Expiration";
	public static final String CALL_COUNT = "Count";
	public static final String USAGE_PERCENT = "UsagePercent";
	
	// TDR Specific fields name
	public static final String CORRELATION = "CorrelationID";

}
