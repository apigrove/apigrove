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
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "staticTDR", propOrder = {
		"types"
})
public class StaticTdr {

	protected TdrType types;
	@XmlAttribute(name = "tdrPropName", required = true)
	protected String tdrPropName;
	@XmlAttribute(name = "value", required = false)
	protected String value;
	@XmlAttribute(name = "property", required = false)
	protected String propertyName;

	@Override
	public String toString() {
		String v = "types = " + types + ", ";

		v += "tdrPropName = " + tdrPropName + ", ";

		v += "value = " + value + ", ";
		v += "property = " + propertyName + ", ";

		return v;
	}

	/**
	 * Gets the value of the types property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link TdrType }
	 *     
	 */
	public TdrType getTypes() {
		return types;
	}

	/**
	 * Sets the value of the types property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link TdrType }
	 *     
	 */
	public void setTypes(TdrType value) {
		this.types = value;
	}

	/**
	 * Gets the value of the tdrPropName property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public String getTdrPropName() {
		return tdrPropName;
	}

	/**
	 * Sets the value of the tdrPropName property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public void setTdrPropName(String value) {
		this.tdrPropName = value;
	}

	/**
	 * Gets the value of the value property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the value of the value property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public void setValue(String value) {
		this.value = value;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String property) {
		this.propertyName = property;
	}

}
