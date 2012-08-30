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
package com.alu.e3.gateway.common.camel.component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.http.DefaultHttpBinding;
import org.apache.camel.component.http.HttpEndpoint;
import org.apache.camel.component.http.HttpMessage;
import org.apache.camel.spi.HeaderFilterStrategy;
import org.eclipse.jetty.http.HttpHeaderValues;
import org.eclipse.jetty.http.HttpHeaders;

import com.alu.e3.common.camel.ExchangeConstantKeys;
import com.alu.e3.gateway.common.camel.exception.GatewayException;

/**
 * HTTP binding that doesn't write request parameters to the input headers.
 * 
 *
 */
public class E3HttpBinding extends DefaultHttpBinding {

	@Deprecated
	public E3HttpBinding() {
		super();
	}

	@Deprecated
	public E3HttpBinding(HeaderFilterStrategy headerFilterStrategy) {
		super(headerFilterStrategy);
	}

	public E3HttpBinding(HttpEndpoint endpoint) {
		super(endpoint);
	}

	@Override
	protected void populateRequestParameters(HttpServletRequest request, HttpMessage message)
			throws UnsupportedEncodingException {
		
		// Save the headers and set a new map for the request parameters
		Map<String, Object> originalHeaders = message.getHeaders();
		message.setHeaders(new HashMap<String, Object>());
		
		super.populateRequestParameters(request, message);
		
		// Set the request parameters map as a new exchange property, and restore
		// the original headers.
		message.getExchange().setProperty(
				ExchangeConstantKeys.E3_REQUEST_PARAMETERS.toString(), message.getHeaders());
		message.setHeaders(originalHeaders);
	}
	
	@SuppressWarnings("deprecation")
	@Override
    public void doWriteResponse(Message message, HttpServletResponse response, Exchange exchange) throws IOException {	
        Exception e = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        if (e instanceof GatewayException) {
        	switch (((GatewayException) e).getCode()) {
        	case AUTHORIZATION:
        	case AUTHORIZATION_BASIC:
        	case AUTHORIZATION_WSSE:
        	case RATEORQUOTA:
        	case VALIDATION:
                // Causes the connection will be closed.
        		response.setHeader(HttpHeaders.CONNECTION, HttpHeaderValues.CLOSE);
        	}
        }

        // set the status code in the response. Default is 200.
        if ((message.getHeader(Exchange.HTTP_RESPONSE_CODE) != null) &&
        		(exchange.getProperty(ExchangeConstantKeys.E3_HTTP_STATUS_LINE_REASON_PHRASE.toString()) != null)){
            int code = message.getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
            String reason = exchange.getProperty(ExchangeConstantKeys.E3_HTTP_STATUS_LINE_REASON_PHRASE.toString(), String.class);
            response.setStatus(code, reason);

            try {
            	// Remove HTTP_RESPONSE_CODE header in order to not overwrite 
            	// status line reason part.
            	message.removeHeader(Exchange.HTTP_RESPONSE_CODE);
            	exchange.removeProperty(ExchangeConstantKeys.E3_HTTP_STATUS_LINE_REASON_PHRASE.toString());
            	super.doWriteResponse(message, response, exchange);
            } finally {
            	// Restore HTTP_RESPONSE_CODE
            	message.setHeader(Exchange.HTTP_RESPONSE_CODE, code);
            }
        } else {
        	super.doWriteResponse(message, response, exchange);
        }
    }

}
