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
package com.alu.e3.logger;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * An XML/JAXB-wrapped response used by LoggingManager when responding
 * to logging-category management REST calls.  Based on the model 
 * used in Provisioning for Auths/Apis etc., except it doesn't
 * currently provide an <code>error</code> member.
 */
@XmlRootElement(name="response")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "loggingCategoryResponse", propOrder = { "categories", "status" })
public class CategoryResponse {
	public final static String SUCCESS = "SUCCESS";
	public final static String FAILURE = "FAILURE";

	@XmlElementWrapper(name = "loggingCategories")
	@XmlElement(name = "loggingCategory")
	protected List<CategoryWrapper> categories;
	protected String status;

	public CategoryResponse() {

	}

	public CategoryResponse(String status) {
		setStatus(status);
	}
		
	/**
	 * 
	 * @return
	 */
	public List<CategoryWrapper> getCategories() {
		if (categories == null) {
			categories = new ArrayList<CategoryWrapper>();
		}
		return this.categories;
	}

	/**
	 * 
	 * @return
	 */
	public void setCategories(List<CategoryWrapper> categories) {
		this.categories = categories;
	}

	/**
	 * Gets the value of the status property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets the value of the status property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setStatus(String value) {
		this.status = value;
	}

}
