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

public class Certificate implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7547669134719015486L;
	
	private String id;
	
	private String data;
	private String password;
	private transient CertificateDetail certDetail;

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CertificateDetail getCertDetail() {
		return certDetail;
	}

	public void setCertDetail(CertificateDetail certDetail) {
		this.certDetail = certDetail;
	}
	
}
