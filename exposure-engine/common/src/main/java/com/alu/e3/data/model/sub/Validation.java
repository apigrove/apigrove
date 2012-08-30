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
import java.util.ArrayList;
import java.util.List;

import com.alu.e3.data.model.enumeration.SchemaValidationEnum;
import com.alu.e3.data.model.enumeration.SoapVersionEnum;

public class Validation implements Serializable {

	private static final long serialVersionUID = 1122965495210674643L;
	/**
	 * 
	 */
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

	public static class Schema implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5088762570666929286L;

		public Schema(SchemaValidationEnum type) {
			super();
			this.type = type;
		}

		public Schema() {
			super();
		}

		protected SchemaValidationEnum type;
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

	public static class Soap implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5085689793572938225L;
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

	public static class Xml implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4709611049934805035L;

	}

}
