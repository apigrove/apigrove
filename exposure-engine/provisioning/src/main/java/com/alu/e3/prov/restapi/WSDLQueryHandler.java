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
package com.alu.e3.prov.restapi;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.Bus;
import org.apache.cxf.service.model.EndpointInfo;

import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;

/**
 * Specifies a WSDL query handler for rejecting WSDL requests. Using default
 * query handler, WSDL request returns a stack trace.
 * 
 * @author bonfroy
 * 
 */
public class WSDLQueryHandler extends
		org.apache.cxf.transport.http.WSDLQueryHandler {
	
	private static final CategoryLogger LOGGER = CategoryLoggerFactory
			.getLogger(TrustStoreManager.class, Category.PROV);

	public WSDLQueryHandler() {
		super();
	}

	public WSDLQueryHandler(Bus b) {
		super(b);
	}

	public void writeResponse(String baseUri, String ctxUri,
			EndpointInfo endpointInfo, OutputStream os) {
		String response = Response.status(Status.SERVICE_UNAVAILABLE).build()
				.toString();
		try {
			os.write(response.getBytes());
		} catch (IOException e) {
			LOGGER.warn("Something wrong happens while rejecting WSDL request\n"
					+ e.getMessage());
		}
	}
}
