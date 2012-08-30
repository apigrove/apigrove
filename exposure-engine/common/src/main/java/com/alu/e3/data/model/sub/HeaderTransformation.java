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

import com.alu.e3.data.model.enumeration.HeaderTransformationAction;
import com.alu.e3.data.model.enumeration.HeaderTransformationType;

public class HeaderTransformation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1564338343179213740L;
	
	protected String name;
    protected String property;
    protected String value;
    protected HeaderTransformationType type;
    protected HeaderTransformationAction action;
    
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}
	public HeaderTransformationType getType() {
		return type;
	}
	public void setType(HeaderTransformationType type) {
		this.type = type;
	}
	public HeaderTransformationAction getAction() {
		return action;
	}
	public void setAction(HeaderTransformationAction action) {
		this.action = action;
	}
	public String getValue() {
		return this.value;
	}
	public void setValue(String value2) {
		this.value = value2;
	}
    
    
}
