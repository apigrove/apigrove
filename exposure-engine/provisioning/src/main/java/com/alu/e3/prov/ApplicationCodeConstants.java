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
 * Application specific return codes.
 * 
 * For Http return codes, @see org.springframework.http.HttpStatus
 */
public class ApplicationCodeConstants {
	
	public static final int RESPONSE_OK = 100;
	
	public static final int BODY_NOT_PROVIDED = 101;
	public static final int INVALID_XML = 102;
	public static final int API_ID_NOT_PROVIDED = 103;
	public static final int OPERATION_FAILED = 104;
	public static final int API_ID_NOT_FOUND = 105;
	public static final int ROLLBACK_FAILED = 106;
	public static final int API_ID_MISMATCH = 107;	
	public static final int NOTHING_TO_DELETE = 108;
	public static final int ROUTE_CREATION_FAILED_ROLLBACK_KO = 109;
	public static final int ROUTE_CREATION_FAILED_ROLLBACK_OK = 110;	
	public static final int DEPLOYMENT_FAILED = 111;
	public static final int UNDEPLOYMENT_FAILED = 112;	
	public static final int NOTHING_TO_ROLLBACK = 113;
	public static final int ID_NOT_FOUND = 114;	
	
	public static final int API_ID_ALREADY_EXIST = 115;	

	public static final int AUTHENTICATION_ENABLED_AND_FAILED = 403;
	
	public static final int UNAUTHORIZED = 401;
	
	public static final int INTERNAL_APPLICATION_ERROR = 500;

	public static final int NOT_IMPLEMENTED = 501;

}
