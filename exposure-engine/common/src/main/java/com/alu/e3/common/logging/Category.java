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
package com.alu.e3.common.logging;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * The Category class is an enum describing the types of events to be logged in the system.
 * The CategoryLogger class accepts a Category parameter for classifying a logging 
 * message for control (enable/disable) and possible filtering.
 */
public enum Category {
	
	SYS("System", "System events"),
	AUTH("Authentication", "Authentication events"),
	PROV("Provisioning", "Provisioning events"),
	SPKR("Speaker", "Speaker-generated events"),
	DMGR("DataManager", "Data-manager events"),
	LOG("Logging", "Logging events");

	static private final Set<Category> disabledCategories = Collections.synchronizedSet(EnumSet.noneOf(Category.class));

	private final String fullname;
	private final String description;

	Category(String fullname, String description) 
	{
		this.fullname = fullname;
		this.description = description;
	}

	public String fullname() 
	{
		return fullname;
	}
	
	public String description() 
	{
		return description;
	}
	
	public boolean enabled() 
	{
		return Category.isCategoryEnabled(this);
	}
	
	static public Category fromString(String name) 
	{
		try {
			return Category.valueOf(name.trim().toUpperCase());
		} catch (Exception ex) {
			return null;
		}
	}
	
	public static final String valuesList = Arrays.asList(values()).toString();

	/**
	 * Enable or disable logging output for the specified Category.  Note
	 * that the <code>null</code> Category is always enabled and cannot
	 * be disabled.
	 * 
	 * @param category	The Category to enable or disable
	 * @param enabled	If <code>false</code>, subsequent log messages classified 
	 * with <code>category</code> will not be output.
	 * @return <code>true</code> if category status is actually changed
	 */
	static public boolean enableCategory(Category category, boolean enabled) 
	{	
		if (category == null) {
			return false;
		}
		if (enabled) {
			return disabledCategories.remove(category);
		} else {
			return disabledCategories.add(category);
		} 
	}
	
	static public boolean isCategoryEnabled(Category category) 
	{
		return ((category == null) || !disabledCategories.contains(category));
	}

}
