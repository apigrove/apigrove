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
package com.alu.e3.prov;

/**
 * !!! Please do not use these Constants !!!
 * The Exchange constants are now part of the common project.
 * 
 * !!! Please use com.alu.e3.common.camel.ExchangeConstantKeys <<common project>> !!!
 */
@Deprecated
public class Constants {
	public static final String E3_PROVISION_ID = "E3_PROVISION_ID";
	public static final String E3_PROVISION_ID_ENCODED = "E3_PROVISION_ID_ENCODED";
	public static final String E3_API_ID = "E3_API_ID";
	public static final String E3_API_ID_ENCODED = "E3_API_ID_ENCODED";
	public static final String E3_OPERATION_NAME = "E3_OPERATION_NAME";
	public static final String E3_REQUEST_PAYLOAD = "E3_REQUEST_PAYLOAD";
	public static final String E3_API_ID_CREATION_MODE = "E3_API_ID_CREATION_MODE";	
	
	// values 
	public static final String E3_API_ID_CREATION_MODE_GENERATED = "GENERATED";	
	public static final String E3_API_ID_CREATION_MODE_PROVIDED = "PROVIDED";
}
