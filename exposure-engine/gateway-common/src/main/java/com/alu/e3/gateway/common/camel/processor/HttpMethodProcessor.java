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
package com.alu.e3.gateway.common.camel.processor;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;

import com.alu.e3.gateway.common.camel.exception.GatewayException;
import com.alu.e3.gateway.common.camel.exception.GatewayExceptionCode;

public class HttpMethodProcessor implements Processor {

	private List<String> allowedHttpMethods;
	
	@Autowired
	public void setAllowedHttpMethods(String csv){
		allowedHttpMethods = new ArrayList<String>();
		if(csv != null){
			for(String m : csv.split(",")){
				if(m == null || m.isEmpty())
					continue;
				allowedHttpMethods.add(m.toLowerCase());
			}
		}
	}
	
	@Override
	public void process(Exchange exchange) throws Exception {
		if(allowedHttpMethods==null || allowedHttpMethods.isEmpty())
			return;

		Object methodheader = exchange.getIn().getHeader(Exchange.HTTP_METHOD);
		if(methodheader == null){
			throw new GatewayException(GatewayExceptionCode.HTTP_METHOD, "No HTTP Method");
		}
		
		for(String m : allowedHttpMethods){
			if(m.equals(methodheader.toString().toLowerCase())){
				return;
			}
		}
		
		throw new GatewayException(GatewayExceptionCode.HTTP_METHOD, "Method "+methodheader.toString()+" not allowed");
	}
}
