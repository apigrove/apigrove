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
package com.alu.e3.gateway.loadbalancer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

import org.apache.camel.CamelExchangeException;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.http4.HttpEndpoint;
import org.apache.camel.component.http4.HttpProducer;
import org.apache.camel.spi.HeaderFilterStrategy;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.params.HttpConnectionParamBean;
import org.apache.http.params.HttpParams;

import com.alu.e3.common.E3Constant;
import com.alu.e3.common.camel.ExchangeConstantKeys;

public class HttpLoadBalancerProducer extends HttpProducer {

	public HttpLoadBalancerProducer(HttpEndpoint endpoint) {
		super(endpoint);
	}

	protected HttpRequestBase createMethod(Exchange exchange) throws URISyntaxException, CamelExchangeException {
		HttpRequestBase httpRequest = super.createMethod(exchange);
		HttpParams params = httpRequest.getParams();

		Integer connectionTimeout = exchange.getProperty(ExchangeConstantKeys.E3_HTTP_CONNECTION_TIMEOUT.toString(), E3Constant.DEFAULT_HTTP_CONNECTION_TIMETOUT, Integer.class);
		Integer socketTimeout = exchange.getProperty(ExchangeConstantKeys.E3_HTTP_SOCKET_TIMEOUT.toString(), E3Constant.DEFAULT_HTTP_SOCKET_TIMEOUT, Integer.class);

		HttpConnectionParamBean httpConnectionParamBean = new HttpConnectionParamBean(params);
		
		httpConnectionParamBean.setConnectionTimeout(connectionTimeout);
		
		httpConnectionParamBean.setSoTimeout(socketTimeout);

		return httpRequest;
	}

	@Override
	protected void populateResponse(Exchange exchange, HttpRequestBase httpRequest, HttpResponse httpResponse, Message in, HeaderFilterStrategy strategy, int responseCode) throws IOException,
			ClassNotFoundException {

		Message answer = exchange.getOut();
		
		// propagate HTTP 'in' headers
		// Old method: answer.setHeaders(in.getHeaders());
		// Uses the same strategy (if any) to only add 'technical' in.headers to answer
		// (The default used strategy is camel technical header aware)
		if (strategy != null) {
			Map<String, Object> headers = in.getHeaders();
			Set<String> headersSet = headers.keySet();
			for (String inHeader : headersSet) {
				Object value = headers.get(inHeader);
				if(strategy.applyFilterToCamelHeaders(inHeader, value, exchange)) {
					answer.setHeader(inHeader, value);
				}
			}
		}

		answer.setHeader(Exchange.HTTP_RESPONSE_CODE, responseCode);
		answer.setBody(extractResponseBody(httpRequest, httpResponse, exchange));

		// propagate HTTP response headers
		if (strategy != null) {
			Header[] headers = httpResponse.getAllHeaders();
			for (Header header : headers) {
				String name = header.getName();
				String value = header.getValue();
				if (name.toLowerCase().equals("content-type")) {
					name = Exchange.CONTENT_TYPE;
				}
				if (!strategy.applyFilterToExternalHeaders(name, value, exchange)) {
					answer.setHeader(name, value);
				}
			}
		}
	}

}