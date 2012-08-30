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
package com.alu.e3.common.camel;

public enum ExchangeConstantKeys {

	// PROVIONING
	E3_PROVISION_ID,
	E3_PROVISION_ID_ENCODED,
	E3_API_ID, 
	E3_API_ID_ENCODED,
	E3_OPERATION_NAME,
	E3_REQUEST_PAYLOAD,
	E3_API_ID_CREATION_MODE,
	E3_API_ID_CREATION_MODE_GENERATED,
	E3_API_ID_CREATION_MODE_PROVIDED,

	// AUTH
	E3_AUTH_IDENTITY, 
	E3_AUTH_METHOD,

	// Rate Limit / Quota
	E3_RATELIMIT_ACTION,
	E3_RATELIMIT_OVER_QUOTA,
	E3_RATELIMIT_OVER_SEC,
	E3_RATELIMIT_OVER_MIN,
	E3_RATELIMIT_OVER_DAY,
	E3_RATELIMIT_OVER_WK,
	E3_RATELIMIT_OVER_MON, 

	// TDR
	E3_TDR_ENABLED,
	E3_GOT_SB_RESPONSE,
	
	// HTTP
	E3_REQUEST_PARAMETERS,

	// SOAP
	E3_SOAP_ACTION,

	// Model Properties
	E3_MODEL_PROPERTIES,
	
	E3_OAUTH_REVOKE_TOKEN,
	
	// TODO: To be renamed to E3_API_ID_CREATION_MODE_GENERATED
	GENERATED,
	// TODO: To be renamed to E3_API_ID_CREATION_MODE_PROVIDED
	PROVIDED,
	
	// HTTP status line reason phrase 
	E3_HTTP_STATUS_LINE_REASON_PHRASE,
}
