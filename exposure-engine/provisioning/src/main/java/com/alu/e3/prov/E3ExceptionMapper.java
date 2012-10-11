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
package com.alu.e3.prov;

import java.util.ArrayList;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import org.apache.cxf.interceptor.security.AccessDeniedException;
import org.apache.cxf.interceptor.security.AuthenticationException;
import org.xml.sax.SAXParseException;

import com.alu.e3.common.InvalidIDException;
import com.alu.e3.common.NotImplementedException;
import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.prov.restapi.model.Error;
import com.alu.e3.prov.restapi.model.BasicResponse;

public class E3ExceptionMapper implements ExceptionMapper<Exception> {
	private static final CategoryLogger logger = CategoryLoggerFactory.getLogger(E3ExceptionMapper.class, Category.PROV);

	public Response toResponse(Exception ex) {
		String message = "";
		int applicationErrorCode = 0;
		int status;
		Throwable cause = ex;

		BasicResponse resp = new BasicResponse(BasicResponse.FAILURE);
		resp.setError(new Error());

		if (ex instanceof AccessDeniedException) {
			// 403 FORBIDDEN
			status = 403;

			applicationErrorCode = ApplicationCodeConstants.AUTHENTICATION_ENABLED_AND_FAILED;
			resp.getError().setErrorText("Access Denied");
			

		} else if (ex instanceof AuthenticationException) {
			//  UNAUTHORIZED
			status = 401;

			applicationErrorCode = ApplicationCodeConstants.UNAUTHORIZED;
			resp.getError().setErrorText("Unauthorized access");
			
		} else if (ex instanceof WebApplicationException) { // CXF raises this
															// kind of exception
			// 500 INTERNAL_SERVER_ERROR
			status = 500;
			applicationErrorCode = ApplicationCodeConstants.INTERNAL_APPLICATION_ERROR;

			if (ex.getCause() == null) {
				message = ex.toString();
			} else {
				cause = ex.getCause();
				if (cause instanceof SAXParseException) {
					applicationErrorCode = ApplicationCodeConstants.INVALID_XML;
					resp.getError().setErrorText("XML Validation Error: " + ex.getCause().getMessage());

				} else if (cause instanceof ProvisionException) {
					ProvisionException provEx = (ProvisionException) ex.getCause();
					applicationErrorCode = provEx.getErrorCode();
					resp.getError().setErrorText(ex.getCause().getMessage());
					
				} else if (cause instanceof InvalidIDException) {
					// This block is called in API WebService
					// Should do the same as other InvalidIDException catch
					// 404 NOT_FOUND
					status = 404;
					applicationErrorCode = ApplicationCodeConstants.ID_NOT_FOUND;
					resp.getError().setErrorText(ex.getCause().getMessage());
				} else {
					message = cause.getMessage();
					if (message == null) {
						message = ex.getCause().toString();
					}
					applicationErrorCode = ApplicationCodeConstants.INTERNAL_APPLICATION_ERROR;
					resp.getError().setErrorText(message);
				}
			}

		} else if (ex instanceof InvalidIDException) {
			// Should do the same as other InvalidIDException catch
			// 404 NOT_FOUND
			status = 404;
			applicationErrorCode = ApplicationCodeConstants.ID_NOT_FOUND;
			resp.getError().setErrorText(ex.getMessage());

		} else if (ex instanceof NotImplementedException) {
			// 501 NOT_IMPLEMENTED
			status = 501;
			applicationErrorCode = ApplicationCodeConstants.NOT_IMPLEMENTED;
			resp.getError().setErrorText(ex.getMessage());

		}  else if (ex instanceof ProvisionException) {
			// 500 INTERNAL_SERVER_ERROR
			status = 500;
			ProvisionException provEx = (ProvisionException) ex.getCause();
			applicationErrorCode = provEx.getErrorCode();
			resp.getError().setErrorText(ex.getCause().getMessage());
			
		} else {
			// 500 INTERNAL_SERVER_ERROR
			status = 500;
			message = ex.toString();

			applicationErrorCode = ApplicationCodeConstants.INTERNAL_APPLICATION_ERROR;
			resp.getError().setErrorText("Provisioning Server Error: "+message);

		}
		StackTraceElement[] stack = cause.getStackTrace();
		ArrayList<StackTraceElement> e3Stack = new ArrayList<StackTraceElement>();
		for (StackTraceElement elem : stack) {
			if (elem.getClassName().indexOf("com.alu.e3") != -1) {
				e3Stack.add(elem);
			}
		}

		cause.setStackTrace(e3Stack.toArray(new StackTraceElement[e3Stack.size()]));
		if(logger.isErrorEnabled()) {
			logger.error("E3 Runtime exception (HTTP status:" + status + ")", cause);
		}

		resp.getError().setErrorCode(String.valueOf(applicationErrorCode));
		ResponseBuilder builder = Response.status(status).type(MediaType.APPLICATION_XML).header("X-Application-Error-Code", applicationErrorCode).entity(resp);
		if(status == 401){
			builder.header("WWW-Authenticate", "Basic");
		}
		
		return builder.build();

	}

}
