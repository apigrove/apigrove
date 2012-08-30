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
package com.alu.e3.gateway.common.camel.exception;

public class GatewayException extends Exception {

	private static final long serialVersionUID = 7743794919826042257L;
	
	private GatewayExceptionCode code;
	
	private String action;
	
	public GatewayException(GatewayExceptionCode code, String message, Throwable cause) {
		super(message, cause);
		this.setCode(code);
	}

	public GatewayException(GatewayExceptionCode code, String message) {
		super(message);
		this.setCode(code);
	}

	public GatewayException(GatewayExceptionCode code, String message, String action) {
		this(code, message);
		this.setAction(action);
	}

	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * @return the code
	 */
	public GatewayExceptionCode getCode() {
		return code;
	}
	

	/**
	 * @param code the code to set
	 */
	public void setCode(GatewayExceptionCode code) {
		this.code = code;
	}

}
