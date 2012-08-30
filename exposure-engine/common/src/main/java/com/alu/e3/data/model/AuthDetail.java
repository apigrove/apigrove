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
package com.alu.e3.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.alu.e3.data.model.enumeration.NBAuthType;
import com.alu.e3.data.model.enumeration.StatusType;

public class AuthDetail implements Serializable {
	/**
	 * USID - Unique Serial ID
	 */
	private static final long serialVersionUID = 1263666739020248605L;

	private StatusType status;
	private NBAuthType type;
	private String authKeyValue;
	private String clientId;
	private String clientSecret;
	private String username;
	private byte[] password;
	private List<String> whiteListedIps;

	public StatusType getStatus() {
		return status;
	}
	public void setStatus(StatusType status) {
		this.status = status;
	}
	public NBAuthType getType() {
		return type;
	}
	public void setType(NBAuthType type) {
		this.type = type;
	}
	public String getAuthKeyValue() {
		return authKeyValue;
	}
	public void setAuthKeyValue(String authKeyValue) {
		this.authKeyValue = authKeyValue;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getClientSecret() {
		return clientSecret;
	}
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
	public byte[] getPassword() {
		return password;
	}
	public void setPassword(byte[] password) {
		this.password = password;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public List<String> getWhiteListedIps() {
		if (whiteListedIps==null) whiteListedIps = new ArrayList<String>();
		return whiteListedIps;
	}
}
