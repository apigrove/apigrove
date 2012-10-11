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

import java.net.URISyntaxException;

import org.apache.camel.CamelExchangeException;
import org.apache.camel.Exchange;
import org.apache.camel.component.http4.HttpEndpoint;
import org.apache.camel.component.http4.HttpProducer;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.params.HttpConnectionParamBean;
import org.apache.http.params.HttpParams;

import com.alu.e3.common.camel.ExchangeConstantKeys;

public class HttpLoadBalancerProducer extends HttpProducer {

	public HttpLoadBalancerProducer(HttpEndpoint endpoint) {
		super(endpoint);
		// TODO Auto-generated constructor stub
	}

    protected HttpRequestBase createMethod(Exchange exchange) throws URISyntaxException, CamelExchangeException {
    	HttpRequestBase httpRequest = super.createMethod(exchange);
    	HttpParams params = httpRequest.getParams();
    	
    	Integer connectionTimeout = exchange.getProperty(ExchangeConstantKeys.E3_HTTP_CONNECTION_TIMEOUT.toString(), Integer.class);
    	Integer socketTimeout = exchange.getProperty(ExchangeConstantKeys.E3_HTTP_SOCKET_TIMEOUT.toString(), Integer.class);

        HttpConnectionParamBean httpConnectionParamBean = new HttpConnectionParamBean(params);
		if(connectionTimeout != null)
			httpConnectionParamBean.setConnectionTimeout(connectionTimeout);
		if(socketTimeout != null)
			httpConnectionParamBean.setSoTimeout(socketTimeout);

		return httpRequest;
    }
    
}
