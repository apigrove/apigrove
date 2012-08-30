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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>
 * Validation types given at provision of an API.
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ValidationType", propOrder = {

})
public class Validation {

	protected Validation.Schema schema;
	protected Validation.Xml xml;
	protected Validation.Soap soap;

	/**
	 * Gets the value of the schema property.
	 * 
	 * @return possible object is {@link Validation.Schema }
	 * 
	 */
	public Validation.Schema getSchema() {
		return schema;
	}

	/**
	 * Sets the value of the schema property.
	 * 
	 * @param value
	 *            allowed object is {@link Validation.Schema }
	 * 
	 */
	public void setSchema(Validation.Schema value) {
		this.schema = value;
	}

	/**
	 * Gets the value of the xml property.
	 * 
	 * @return possible object is {@link Validation.Xml }
	 * 
	 */
	public Validation.Xml getXml() {
		return xml;
	}

	/**
	 * Sets the value of the xml property.
	 * 
	 * @param value
	 *            allowed object is {@link Validation.Xml }
	 * 
	 */
	public void setXml(Validation.Xml value) {
		this.xml = value;
	}

	/**
	 * Gets the value of the soap property.
	 * 
	 * @return possible object is {@link Validation.Soap }
	 * 
	 */
	public Validation.Soap getSoap() {
		return soap;
	}

	/**
	 * Sets the value of the soap property.
	 * 
	 * @param value
	 *            allowed object is {@link Validation.Soap }
	 * 
	 */
	public void setSoap(Validation.Soap value) {
		this.soap = value;
	}

	/**
     * 
     */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = {

	})
	/**
	 * Schema validation type for XSD or WSDL 
	 */
	public static class Schema {

		public Schema(SchemaValidationEnum type) {
			super();
			this.type = type;
		}

		public Schema() {
			super();
		}

		/**
		 * xsd or wsdl type
		 */
		@XmlElement(required = true)
		protected SchemaValidationEnum type;
		/**
		 * List of resources for the schema element.
		 */
		@XmlElementWrapper(name = "resources", required = true)
		@XmlElement(name = "resource", required = true)
		protected List<ResourceItem> resourcesList;

		/**
		 * Gets the value of the type property.
		 * 
		 * @return possible object is {@link SchemaValidationEnum }
		 * 
		 */
		public SchemaValidationEnum getType() {
			return type;
		}

		/**
		 * Sets the value of the type property.
		 * 
		 * @param value
		 *            allowed object is {@link SchemaValidationEnum }
		 * 
		 */
		public void setType(SchemaValidationEnum value) {
			this.type = value;
		}

		public List<ResourceItem> getResourcesList() {
			if (resourcesList == null) {
				resourcesList = new ArrayList<ResourceItem>();
			}
			return this.resourcesList;
		}

		public void setResourcesList(List<ResourceItem> resourcesList) {
			this.resourcesList = resourcesList;
		}

	}

	/**
     * Soap validation type
     * 
     */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = {

	})
	public static class Soap {
		/**
		 * Soap version 1.1 or 1.2
		 */
		@XmlElement(required = true)
		protected SoapVersionEnum version;

		public Soap(SoapVersionEnum version) {
			super();
			this.version = version;
		}

		public Soap() {
			super();
		}

		/**
		 * Gets the value of the version property.
		 * 
		 * @return possible object is {@link String }
		 * 
		 */
		public SoapVersionEnum getVersion() {
			return version;
		}

		/**
		 * Sets the value of the version property.
		 * 
		 * @param value
		 *            allowed object is {@link String }
		 * 
		 */
		public void setVersion(SoapVersionEnum value) {
			this.version = value;
		}

	}

	/**
	 * <p>
	 * Xml validation type. 
	 * When specified, this element must be empty. 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "")
	public static class Xml {

	}

}
