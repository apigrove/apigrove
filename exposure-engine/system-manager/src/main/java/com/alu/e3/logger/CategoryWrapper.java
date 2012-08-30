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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.alu.e3.common.logging.Category;


/**
 * A Category wrapper class for use by the REST logging-management methods
 * in LoggingManager.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="loggingCategory", propOrder = {
		"name",
		"fullname", 
		"description",
		"enabled"})
		
public class CategoryWrapper {
	@XmlElement(name="name", required = true)
	protected String name;
	@XmlElement(name="fullname", required = false)
	protected String fullname;
	@XmlElement(name="description", required = false)
	protected String description;
	@XmlElement(name="enabled", required = true)
	protected boolean enabled;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getFullname() 
	{
		return fullname;
	}
	
	public void setFullname(String fullname) 
	{
		this.fullname = fullname;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public void setDescription(String description)
	{
		this.description = description;
	}
	
	public boolean getEnabled()
	{
		return enabled;
	}
	
	public void setEnabled(boolean enabled) 
	{
		this.enabled = enabled;
	}
	
	public static final Category toDataModel(CategoryWrapper categoryXML)
	{
		if (categoryXML == null) {
			throw new IllegalArgumentException("CategoryWrapper must not be null");
		}
		Category category = Category.fromString(categoryXML.getName());
		if (category == null) {
			throw new IllegalArgumentException("CategoryWrapper.name must be a valid Category value: " + Category.valuesList);
		}
		Category.enableCategory(category, categoryXML.getEnabled());
		return category;
	}
	
	public static final CategoryWrapper fromDataModel(Category category) 
	{
		if (category == null) {
			throw new IllegalArgumentException("Category must not be null");
		}
		CategoryWrapper c = new CategoryWrapper();
		c.setName(category.name());
		c.setFullname(category.fullname());
		c.setDescription(category.description());
		c.setEnabled(Category.isCategoryEnabled(category));
		return c;
	}

}
