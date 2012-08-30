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
package com.alu.e3.data.model.sub;

import java.io.Serializable;


public class TargetHost implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1781721810022518456L;

	private String url;
	private String site;
	private SBAuthentication authentication; 
	private ConnectionParameters connectionParameter;
	private ForwardProxy forwardProxy;

	// Authentifcation per targetHost !

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public SBAuthentication getAuthentication() {
		return authentication;
	}
	public void setAuthentication(SBAuthentication authentication) {
		this.authentication = authentication;
	}

	public ConnectionParameters getConnectionParameters() {
		return connectionParameter;
	}
	public void setConnectionParameters(ConnectionParameters connectionParameter) {
		this.connectionParameter = connectionParameter;
	}
	public ForwardProxy getForwardProxy() {
		return forwardProxy;
	}
	public void setForwardProxy(ForwardProxy forwardProxy) {
		this.forwardProxy = forwardProxy;
	}
}
