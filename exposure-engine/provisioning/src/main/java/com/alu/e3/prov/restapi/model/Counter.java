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
 * Defines a counter.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CounterType", propOrder = {
    "status",
    "action",
    "warning",
    "threshold"
})
public class Counter {

    @XmlElement(required = true)
    protected Status status;
    @XmlElement(required = true)
    protected Action action;
    protected float warning;
    protected long threshold;

    public String toString() {
    	String v = "status = " + status;
    	v += ", action = " + action;
    	v += ", warning = " + warning;
    	v += ", threshold = " + threshold;
    	
    	return v;
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
     * Gets the value of the action property.
     * 
     * @return
     *     possible object is
     *     {@link Action }
     *     
     */
    public Action getAction() {
        return action;
    }

    /**
     * Sets the value of the action property.
     * 
     * @param value
     *     allowed object is
     *     {@link Action }
     *     
     */
    public void setAction(Action value) {
        this.action = value;
    }

    /**
     * Gets the value of the warning property.
     * 
     */
    public float getWarning() {
        return warning;
    }

    /**
     * Sets the value of the warning property.
     * 
     */
    public void setWarning(float value) {
        this.warning = value;
    }

    /**
     * Gets the value of the threshold property.
     * 
     */
    public long getThreshold() {
        return threshold;
    }

    /**
     * Sets the value of the threshold property.
     * 
     */
    public void setThreshold(long value) {
        this.threshold = value;
    }

}
