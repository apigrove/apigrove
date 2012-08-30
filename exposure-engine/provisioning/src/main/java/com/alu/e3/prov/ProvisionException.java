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

public class ProvisionException extends Exception {

	/**
	 * Generated serial version UID 
	 */
	private static final long serialVersionUID = 1495836926660177572L;
	
	int functionalErrorCode;

	public ProvisionException() {
		super();
	}

	public ProvisionException(int code, String message) {
		super(message);
		functionalErrorCode = code;
	}

	public ProvisionException(int code, String message, Throwable cause) {
		super(message, cause);
		functionalErrorCode = code;
	}

	public ProvisionException(int code, Throwable cause) {
		super(cause);
		functionalErrorCode = code;
	}

	public int getErrorCode() {
		return functionalErrorCode;
	}

	public void setErrorCode(int functionalErrorCode) {
		this.functionalErrorCode = functionalErrorCode;
	}
}
