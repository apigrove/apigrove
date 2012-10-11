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

public class Key implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2510943073668382224L;

	private String id;
	
	private String data;
	private String activeCertId;
	private String keyPassphrase;
	transient private Certificate activeCert;
	transient private KeyDetail keyDetail;
	
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getActiveCertId() {
		return activeCertId;
	}

	public void setActiveCertId(String activecert) {
		this.activeCertId = activecert;
	}
	
	public void setKeyPassphrase(String keyPassphrase) {
		this.keyPassphrase = keyPassphrase;
	}
	
	public String getKeyPassphrase() {
		return keyPassphrase;
	}

	public KeyDetail getKeyDetail() {
		return keyDetail;
	}

	public void setKeyDetail(KeyDetail keyDetail) {
		this.keyDetail = keyDetail;
	}

	public Certificate getActiveCert() {
		return activeCert;
	}

	public void setActiveCert(Certificate activeCert) {
		this.activeCert = activeCert;
	}
}
