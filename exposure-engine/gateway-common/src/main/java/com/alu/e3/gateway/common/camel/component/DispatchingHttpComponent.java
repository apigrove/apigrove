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

import java.util.List;

import org.apache.camel.component.http.CamelServlet;
import org.apache.camel.component.http.HttpConsumer;
import org.apache.camel.component.jetty.JettyHttpComponent;
import org.apache.camel.component.jetty.JettyHttpEndpoint;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.alu.e3.gateway.dispatch.DispatchingContinuationServlet;
import com.alu.e3.gateway.dispatch.TreeDispatcher;

/**
 * Extension of JettyHttpComponent using DispatchingContinuationServlet.
 */
public class DispatchingHttpComponent extends JettyHttpComponent {

	@Override
	protected CamelServlet createServletForConnector(Server server, Connector connector,
			List<Handler> handlers, JettyHttpEndpoint endpoint) throws Exception
	{
		ServletContextHandler context = new ServletContextHandler(server, "/",
				ServletContextHandler.NO_SECURITY | ServletContextHandler.NO_SESSIONS);
		context.setConnectorNames(new String[] {connector.getName()});

		DispatchingContinuationServlet servlet = new DispatchingContinuationServlet();
		servlet.setDispatcher(new TreeDispatcher<HttpConsumer>());
		Long timeout = endpoint.getContinuationTimeout() != null ? endpoint
				.getContinuationTimeout() : getContinuationTimeout();
		if (timeout != null) {
			servlet.setContinuationTimeout(timeout);
		}

		ServletHolder holder = new ServletHolder();
		holder.setServlet(servlet);
		context.addServlet(holder, "/*");

		return servlet;
	}
}
