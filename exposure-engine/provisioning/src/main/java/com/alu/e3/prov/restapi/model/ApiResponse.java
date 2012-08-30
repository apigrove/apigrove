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

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Api REST service response model
 * 
 */
@XmlRootElement(name = "response")
public class ApiResponse extends BasicResponse {
	protected Api api;
	
	public ApiResponse() {
		super();
	}
	
	public ApiResponse(String status) {
		super(status);
	}
	
	public ApiResponse(String status, Api api) {
		this(status);
		setApi(api);
	}
	
	public ApiResponse(String status, String apiId) {
		this(status);
		setId(apiId);
	}
	
	public ApiResponse(String status, List<String> apiIdsList) {
		this(status);
		setIds(apiIdsList);
	}
	
	public Api getApi() {
		return api;
	}

	public void setApi(Api api) {
		this.api = api;
	}

}
