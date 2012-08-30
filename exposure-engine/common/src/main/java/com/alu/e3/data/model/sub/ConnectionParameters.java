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

public class ConnectionParameters implements Serializable {
	
	private static final long serialVersionUID = 8854660312017153502L;

    private static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 20; // Taken from org.apache.camel.component.http4.HttpComponent
	private static final int DEFAULT_CONNECTION_TIMEOUT = 0; //taken from org.apache.http.params.HttpConnectionParams
	private static final int DEFAULT_SOCKET_TIMEOUT = 0; //taken from org.apache.http.params.HttpConnectionParams
	
	private Integer maxConnections = null;
	private Integer connectionTimeout = null;
	private Integer socketTimeout = null;
	
	public ConnectionParameters(){
//		this.maxConnections = DEFAULT_MAX_CONNECTIONS_PER_ROUTE;
//		this.connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
//		this.socketTimeout = DEFAULT_SOCKET_TIMEOUT;
	}

	public Integer getMaxConnections() {
		return maxConnections;
	}
	public void setMaxConnections(Integer maxConnections) {
		this.maxConnections = maxConnections;
	}

	public Integer getConnectionTimeout() {
		return connectionTimeout;
	}
	public void setConnectionTimeout(Integer connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public Integer getSocketTimeout() {
		return socketTimeout;
	}
	public void setSocketTimeout(Integer socketTimeout) {
		this.socketTimeout = socketTimeout;
	}
	
}
