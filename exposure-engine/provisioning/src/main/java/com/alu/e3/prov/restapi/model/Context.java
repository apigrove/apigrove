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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Defines an environment.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ContextType", propOrder = {
    "status",
    "quotaPerDay",
    "quotaPerWeek",
    "quotaPerMonth",
    "rateLimitPerSecond",
    "rateLimitPerMinute"
})
public class Context {

    @XmlAttribute(required = true)
    protected String id;
    @XmlElement(required = true)
    protected Status status;
    protected Counter quotaPerDay;
    protected Counter quotaPerWeek;
    protected Counter quotaPerMonth;
    protected Counter rateLimitPerSecond;
    protected Counter rateLimitPerMinute;
    
    public String toString() {
    	String v = "id = " + id;
    	v += ", status = " + status;
    	v += ", quotaPerDay = [" + quotaPerDay + "]";
    	v += ", quotaPerWeek = [" + quotaPerWeek + "]";
    	v += ", quotaPerMonth = [" + quotaPerMonth + "]";
    	v += ", rateLimitPerSecond = [" + rateLimitPerSecond + "]";
    	v += ", rateLimitPerMinute = [" + rateLimitPerMinute + "]";
    	
    	return v;
    }

    /**
     * Gets the value of the id attribute.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id attribute.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link Status }
     *     
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link Status }
     *     
     */
    public void setStatus(Status value) {
        this.status = value;
    }

    /**
     * Gets the value of the quotaPerDay property.
     * 
     * @return
     *     possible object is
     *     {@link Counter }
     *     
     */
    public Counter getQuotaPerDay() {
        return quotaPerDay;
    }

    /**
     * Sets the value of the quotaPerDay property.
     * 
     * @param value
     *     allowed object is
     *     {@link Counter }
     *     
     */
    public void setQuotaPerDay(Counter value) {
        this.quotaPerDay = value;
    }

    /**
     * Gets the value of the quotaPerWeek property.
     * 
     * @return
     *     possible object is
     *     {@link Counter }
     *     
     */
    public Counter getQuotaPerWeek() {
        return quotaPerWeek;
    }

    /**
     * Sets the value of the quotaPerWeek property.
     * 
     * @param value
     *     allowed object is
     *     {@link Counter }
     *     
     */
    public void setQuotaPerWeek(Counter value) {
        this.quotaPerWeek = value;
    }

    /**
     * Gets the value of the quotaPerMonth property.
     * 
     * @return
     *     possible object is
     *     {@link Counter }
     *     
     */
    public Counter getQuotaPerMonth() {
        return quotaPerMonth;
    }

    /**
     * Sets the value of the quotaPerMonth property.
     * 
     * @param value
     *     allowed object is
     *     {@link Counter }
     *     
     */
    public void setQuotaPerMonth(Counter value) {
        this.quotaPerMonth = value;
    }

    /**
     * Gets the value of the rateLimitPerSecond property.
     * 
     * @return
     *     possible object is
     *     {@link Counter }
     *     
     */
    public Counter getRateLimitPerSecond() {
        return rateLimitPerSecond;
    }

    /**
     * Sets the value of the rateLimitPerSecond property.
     * 
     * @param value
     *     allowed object is
     *     {@link Counter }
     *     
     */
    public void setRateLimitPerSecond(Counter value) {
        this.rateLimitPerSecond = value;
    }

    /**
     * Gets the value of the rateLimitPerMinute property.
     * 
     * @return
     *     possible object is
     *     {@link Counter }
     *     
     */
    public Counter getRateLimitPerMinute() {
        return rateLimitPerMinute;
    }

    /**
     * Sets the value of the rateLimitPerMinute property.
     * 
     * @param value
     *     allowed object is
     *     {@link Counter }
     *     
     */
    public void setRateLimitPerMinute(Counter value) {
        this.rateLimitPerMinute = value;
    }

}
