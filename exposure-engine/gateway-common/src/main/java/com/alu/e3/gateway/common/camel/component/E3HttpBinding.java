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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.http.DefaultHttpBinding;
import org.apache.camel.component.http.HttpConverter;
import org.apache.camel.component.http.HttpEndpoint;
import org.apache.camel.component.http.HttpMessage;
import org.apache.camel.converter.stream.CachedOutputStream;
import org.apache.camel.spi.HeaderFilterStrategy;
import org.apache.camel.util.IOHelper;
import org.eclipse.jetty.http.HttpHeaderValues;
import org.eclipse.jetty.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alu.e3.common.camel.ExchangeConstantKeys;
import com.alu.e3.gateway.common.camel.converter.stream.MultipartStream;
import com.alu.e3.gateway.common.camel.exception.GatewayException;

/**
 * HTTP binding that doesn't write request parameters to the input headers.
 * 
 *
 */
public class E3HttpBinding extends DefaultHttpBinding {

	private static final Logger LOG = LoggerFactory.getLogger(E3HttpBinding.class);
	
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

	@Override
	public Object parseBody(HttpMessage httpMessage) throws IOException {
//		httpMessage.getExchange().getContext().getProperties().put(CachedOutputStream.THRESHOLD, "0");
		
        // lets assume the body is a reader
        HttpServletRequest request = httpMessage.getRequest();
        // Need to handle the GET Method which has no inputStream
        if ("GET".equals(request.getMethod())) {
            return null;
        }
        if (isUseReaderForPayload()) {
            // use reader to read the response body
            return request.getReader();
        } else {
        	Exchange exchange = httpMessage.getExchange();
            // read the response body from servlet request
            InputStream is = HttpConverter.toInputStream(request, exchange);

            if (is == null) {
                return null;
            }

            if (LOG.isDebugEnabled()) {
				LOG.debug("Content-Type[{}], Content-Length[{}]", httpMessage
						.getRequest().getContentType(), httpMessage
						.getRequest().getContentLength());
            }
            
            // convert the input stream to StreamCache if the stream cache is not disabled
            if (exchange.getProperty(Exchange.DISABLE_HTTP_STREAM_CACHE, Boolean.FALSE, Boolean.class)) {
            	return is;
            } else {
            	if (LOG.isDebugEnabled()) {
            		LOG.debug("Cache enabled");
            	}
            	
        		// IMPORTANT: contentType MUST be final! For some reason Axiom has an issue when it is not final
        		// and is unable to parse boundary in incoming message.
            	final String contentType = httpMessage.getRequest().getContentType();
            	if ((contentType != null)
            			&& (contentType.toLowerCase().startsWith("multipart/related") || 
            				contentType.toLowerCase().startsWith("multipart/form-data"))) {
            		/*
            		 * SOAP message with attachment(s) uses multipart/related content type.
            		 * multipart/form-data is being used by REST request with attachment(s).
            		 * Fortunately, Axiom's Attachments.getSOAPPartInputStream() for multipart/form-data
            		 * returns first part of multipart data which is XML payload we need to
            		 * validate against API's defined schema.
            		 * This is just workaround. The final solution will be provided by OAPEEE-294.
            		 */
            		if (LOG.isDebugEnabled()) {
            			LOG.debug("MultipartStream used");
            		}
            		return new MultipartStream(is, contentType);
            	} else {
            		if (LOG.isDebugEnabled()) {
            			LOG.debug("CachedOutputStream used");
            		}
            		CachedOutputStream cos = new CachedOutputStream(exchange);
            		IOHelper.copyAndCloseInput(is, cos);
            		return cos.getStreamCache();
            	}
            }
        }

	}


}
