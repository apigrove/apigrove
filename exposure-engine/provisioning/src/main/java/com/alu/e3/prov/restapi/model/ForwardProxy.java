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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The forwardProxy data model.
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "forwardProxy", propOrder = {
    "proxyHost",
    "proxyPort",
    "proxyUser",
    "proxyPass"
})
public class ForwardProxy {
	
    @XmlElement(required = true)
    protected String proxyHost;

    @XmlElement(required = true)
    protected String proxyPort;
    
    @XmlElement(required = true)
    protected String proxyUser;

    @XmlElement(required = true)
    protected String proxyPass;

    /**
     * Gets the value of the proxyHost property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProxyHost() {
        return proxyHost;
    }

    /**
     * Sets the value of the proxyHost property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProxyHost(String value) {
        this.proxyHost = value;
    }

    
    /**
     * Gets the value of the proxyPort property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProxyPort() {
        return proxyPort;
    }

    /**
     * Sets the value of the proxyPort property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProxyPort(String value) {
        this.proxyPort = value;
    }
   

    /**
     * Gets the value of the proxyUser property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProxyUser() {
        return proxyUser;
    }

    /**
     * Sets the value of the proxyUser property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProxyUser(String value) {
        this.proxyUser = value;
    }
    
    
    /**
     * Gets the value of the proxyPass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProxyPass() {
        return proxyPass;
    }

    /**
     * Sets the value of the proxyPass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProxyPass(String value) {
        this.proxyPass = value;
    }
}
