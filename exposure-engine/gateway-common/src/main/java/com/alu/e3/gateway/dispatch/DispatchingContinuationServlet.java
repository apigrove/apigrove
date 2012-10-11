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
package com.alu.e3.gateway.dispatch;

import javax.servlet.http.HttpServletRequest;

import org.apache.camel.component.http.HttpConsumer;
import org.apache.camel.component.jetty.CamelContinuationServlet;

/**
 * Extension of CamelContinuationServlet using a provided dispatcher to resolve requests.
 */
public class DispatchingContinuationServlet extends CamelContinuationServlet {
	private static final long serialVersionUID = 6452344719002961461L;

	private Dispatcher<HttpConsumer> dispatcher;
	
	/**
	 * Sets the dispatcher.
	 * @param dispatcher
	 */
	public void setDispatcher(Dispatcher<HttpConsumer> dispatcher) {
		this.dispatcher = dispatcher;
	}

	@Override
	protected HttpConsumer resolve(final HttpServletRequest request) {
        final String path = request.getPathInfo();
        if (path == null) {
            return null;
        }
    	final HttpConsumer exact = dispatcher.findExactMatch(path);
    	if (exact != null) {
    		return exact;
    	}
    	for (final HttpConsumer prefix : dispatcher.findPrefixMatches(path)) {
    		if (prefix.getEndpoint().isMatchOnUriPrefix()) {
    			return prefix;
    		}
    	}
        return null;
	}

	@Override
	public void connect(HttpConsumer consumer) {
		dispatcher.addEndpoint(consumer.getPath(), consumer);
	}

	@Override
	public void disconnect(HttpConsumer consumer) {
		dispatcher.removeEndpoint(consumer.getPath());
	}
}
