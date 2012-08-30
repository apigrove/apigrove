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

/**
 * Class Instance
 * 
 */
public class Instance implements Serializable {

	/**
	 * Generated serial version UID
	 */
	private static final long serialVersionUID = 8190090033750980700L;
	
	
	private String externalIP, internalIP, externalDNS;
	private String type;
	private String name;
	private String sshKeyName;
	private String user;
	private String password;
	private String port;
	private String area;
	private int sshPort = 22;
	private SSHKey sshKey;

	public Instance() {
	}

	public Instance(Instance inst) {
		this.externalIP = inst.externalIP;
		this.internalIP = inst.internalIP;
		this.externalDNS = inst.externalDNS;
		this.type = inst.type;
		this.name = inst.name;
		this.sshKeyName = inst.sshKeyName;
		this.user = inst.user;
		this.password = inst.password;
		this.port = inst.port;
		this.area = inst.area;
		this.sshPort = inst.sshPort;

		SSHKey instSSHKey = inst.getSSHKey();

		if (instSSHKey != null) {
			this.sshKey = new SSHKey(instSSHKey.getName(),
					instSSHKey.getPrivateKey(), instSSHKey.getPublicKey());
		}
	}

	public int getSSHPort() {
		return sshPort;
	}

	public void setSSHPort(int sshPort) {
		this.sshPort = sshPort;
	}

	public SSHKey getSSHKey() {
		return sshKey;
	}

	public void setSSHKey(SSHKey sshKey) {
		this.sshKey = sshKey;
	}

	public String getExternalDNS() {
		return externalDNS;
	}

	public void setExternalDNS(String externalDNS) {
		this.externalDNS = externalDNS;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExternalIP() {
		return externalIP;
	}

	public void setExternalIP(String externalIP) {
		this.externalIP = externalIP;
	}

	public String getInternalIP() {
		return internalIP;
	}

	public void setInternalIP(String internalIP) {
		this.internalIP = internalIP;
	}

	public String getSSHKeyName() {
		return sshKeyName;
	}

	public void setSSHKeyName(String sshKeyName) {
		this.sshKeyName = sshKeyName;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	@Override
	public String toString() {
		return "[Instance: " + name + " " + type + " " + internalIP + " "
				+ externalIP + " " + area + "]";
	}
}
