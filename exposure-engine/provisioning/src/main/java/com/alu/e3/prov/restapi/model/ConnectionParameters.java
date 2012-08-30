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
package com.alu.e3.prov.restapi.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * The connection parameters for API target hosts. 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "connectionParameters", propOrder = {
    "maxConnections",
    "connectionTimeout",
    "socketTimeout"
})
public class ConnectionParameters {

    /**
     * The maximum number of connections for this target host.
     */
    @XmlElement(required = false)
    protected Integer maxConnections = null;
    
    
    /**
     * The connection timeout for this target host.
     */
    @XmlElement(required = false)
    protected Integer connectionTimeout = null;
    
    
    /**
     * The socket timeout for this target host.
     */
    @XmlElement(required = false)
    protected Integer socketTimeout = null;

	/**
	 * Get the maximum number of connections for this target host.
	 * @return the maximum number of connections
	 */
	public Integer getMaxConnections() {
		return maxConnections;
	}

	/**
	 * Set the maximum number of connections for this target host.
	 * @param maxConnections the maximum number of connections to set
	 */
	public void setMaxConnections(Integer maxConnections) {
		this.maxConnections = maxConnections;
	}

	/**
	 * Get the connection  timeout for this target host.
	 * @return the connection  Timeout
	 */
	public Integer getConnectionTimeout() {
		return connectionTimeout;
	}

	/**
	 * Set the connection  timeout for this target host.
	 * @param connectionTimeout the connection  Timeout to set
	 */
	public void setConnectionTimeout(Integer connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	/**
	 * Get the socket timeout for this target host.
	 * @return the socket Timeout
	 */
	public Integer getSocketTimeout() {
		return socketTimeout;
	}

	/**
	 * Set the socket timeout for this target host.
	 * @param socketTimeout the socket Timeout to set
	 */
	public void setSocketTimeout(Integer socketTimeout) {
		this.socketTimeout = socketTimeout;
	}
    
}
