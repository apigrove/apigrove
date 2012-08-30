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

import com.alu.e3.data.model.ExtractFromType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dynamicTDR", propOrder = {
		"types"
})
public class DynamicTdr {

	protected TdrType types;
	@XmlAttribute(name = "tdrPropName", required = true)
	protected String tdrPropName;
	@XmlAttribute(name = "httpHeaderName", required = true)
	protected String httpHeaderName;

	@XmlAttribute(name = "extractFrom", required = false)
	protected ExtractFromType extractFrom = ExtractFromType.Either;

	@Override
	public String toString() {
		String v = "types = " + types + ", ";

		v += "tdrPropName = " + tdrPropName + ", ";

		v += "httpHeaderName = " + httpHeaderName + ", ";

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
	 * Gets the value of the httpHeaderName property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public String getHttpHeaderName() {
		return httpHeaderName;
	}

	/**
	 * Sets the value of the httpHeaderName property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public void setHttpHeaderName(String value) {
		this.httpHeaderName = value;
	}

	public ExtractFromType getExtractFrom() {
		return extractFrom;
	}

	public void setExtractFrom(ExtractFromType extractFrom) {
		this.extractFrom = extractFrom;
	}

}
