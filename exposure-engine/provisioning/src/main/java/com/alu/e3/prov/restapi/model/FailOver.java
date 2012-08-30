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
 * The Fail over for this load balancing 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "failOver", propOrder = {
    "onResponseCode"
})
public class FailOver {
	 
    @XmlElement(required = true)
	protected String onResponseCode;



	/**
	 * Gets the value of the onResponseCode property.
	 */
	public String getOnResponseCode() {
		return this.onResponseCode;
	}
	
	/**
	 * Sets the value of the onResponseCode property.
	 * @param onResponseCode the onResponseCode to set
	 */
	public void setOnResponseCode(String onResponseCode) {
		this.onResponseCode = onResponseCode;
	}
 
    
}
