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

import com.alu.e3.data.model.ExtractFromType;


public class TdrDynamicRule implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6618904536647856203L;

	private List<String> types;
	private String tdrPropName;
	private String httpHeaderName;
	private ExtractFromType extractFrom;

	public List<String> getTypes() {
		if (types==null) types = new ArrayList<String>();
		return types;
	}

	public String getTdrPropName() {
		return tdrPropName;
	}
	public void setTdrPropName(String tdrPropName) {
		this.tdrPropName = tdrPropName;
	}

	public String getHttpHeaderName() {
		return httpHeaderName;
	}
	public void setHttpHeaderName(String httpHeaderName) {
		this.httpHeaderName = httpHeaderName;
	}

	public ExtractFromType getExtractFrom() {
		return extractFrom;
	}

	public void setExtractFrom(ExtractFromType extractFrom) {
		this.extractFrom = extractFrom;
	}



}
