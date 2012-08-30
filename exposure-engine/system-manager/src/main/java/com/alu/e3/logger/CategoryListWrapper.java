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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.alu.e3.common.logging.Category;

/**
 * A JAXB wrapper class to encapsulate the Category list sent by
 * requesters to the LoggingManager REST API.
 */
@XmlRootElement(name="loggingCategories")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="loggingCategoryList", propOrder = {"categories"})
public class CategoryListWrapper {

	@XmlElement(name = "loggingCategory", required = true)
	protected List<CategoryWrapper> categories;

	public List<CategoryWrapper> getCategories()
	{
		return categories;
	}
	
	public void setCategories(List<CategoryWrapper> categories)
	{
		this.categories = categories;
	}
	
	public static final List<Category> toDataModel(CategoryListWrapper categoryListWrapper) 
	{
		return toDataModel(categoryListWrapper.getCategories());
	}
	
	public static final List<Category> toDataModel(List<CategoryWrapper> wrappers) 
	{
    	List<Category> categoryList = new ArrayList<Category>();
    	for (CategoryWrapper c : wrappers) {
    		categoryList.add(CategoryWrapper.toDataModel(c));
    	}
		return categoryList;
	}

	public static final List<CategoryWrapper> fromDataModel(List<Category> categories) 
	{
    	List<CategoryWrapper> wrapperList = new ArrayList<CategoryWrapper>();
    	for (Category c : categories) {
    		wrapperList.add(CategoryWrapper.fromDataModel(c));
    	}
		return wrapperList;
	}

}
