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

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.alu.e3.common.camel.ExchangeConstantKeys;
import com.alu.e3.gateway.common.camel.exception.GatewayException;
import com.alu.e3.gateway.common.camel.exception.GatewayExceptionCode;

public class GatewayErrorProcessor implements Processor {

	private static Logger logger = Logger.getLogger(GatewayErrorProcessor.class);

	@Override
	public void process(Exchange exchange) throws Exception {

		boolean isNoteSpecialized = true;

		Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);

		if (exception instanceof GatewayException) {

			GatewayException gatewayException = (GatewayException) exception;

			if (gatewayException.getCode() == GatewayExceptionCode.AUTHORIZATION) {
				String body = "Issue: " + gatewayException.getMessage();
				createHttpErrorResponse(exchange, 401, body); 
				isNoteSpecialized = false;

			} else if(gatewayException.getCode() == GatewayExceptionCode.AUTHORIZATION_BASIC) {
				exchange.getOut().setHeader("WWW-Authenticate", "Basic realm=\"Secure Service\"");
				String body = "Issue: " + gatewayException.getMessage(); 
				createHttpErrorResponse(exchange, 401, body);
				isNoteSpecialized = false;
				
			} else if(gatewayException.getCode() == GatewayExceptionCode.API_NOT_ACTIVATED) {				
				String body = "Issue: " + gatewayException.getMessage(); 
				createHttpErrorResponse(exchange, 403, body);
				isNoteSpecialized = false;		
				
			} else if(gatewayException.getCode() == GatewayExceptionCode.VALIDATION) {
				String body = "Issue: " + gatewayException.getMessage(); 
				createHttpErrorResponse(exchange, 400, body);
				isNoteSpecialized = false;
			}	
			if (gatewayException.getCode() == GatewayExceptionCode.RATEORQUOTA) {
				String body = "Issue: " + gatewayException.getMessage();
				// Based on draft "Additional HTTP Status Codes; draft-nottingham-http-new-status-02"
				// http://tools.ietf.org/html/draft-nottingham-http-new-status-02#page-4
				createHttpErrorResponse(exchange, 429, "Too Many Requests", body);
				isNoteSpecialized = false;
			}
			if (gatewayException.getCode() == GatewayExceptionCode.HTTP_METHOD) {
				String body = "Issue: " + gatewayException.getMessage();
				createHttpErrorResponse(exchange, 405, body);
				isNoteSpecialized = false;
			}
		}

		if (isNoteSpecialized) {
			int errorCode = 500;
			String body = "Issue: Internal Server Error\n" + exception.getMessage();
			createHttpErrorResponse(exchange, errorCode, body);
			isNoteSpecialized = false;
			logger.debug(exception.getMessage(), exception);
		}
	}

	private void createHttpErrorResponse(Exchange exchange, int errorCode, String body) {
		exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, errorCode);
		exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "text/plain");
		exchange.getOut().setBody(body);
	}
	
	private void createHttpErrorResponse(Exchange exchange, int errorCode, String errorMessage, String body) {
		exchange.setProperty(ExchangeConstantKeys.E3_HTTP_STATUS_LINE_REASON_PHRASE.toString(), errorMessage);
		createHttpErrorResponse(exchange, errorCode, body);
	}
}
