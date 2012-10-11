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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.cxf.Bus;
import org.apache.cxf.common.injection.NoJSR250Annotations;
import org.apache.cxf.configuration.ConfiguredBeanLocator;
import org.apache.cxf.transports.http.QueryHandler;
import org.apache.cxf.transports.http.QueryHandlerRegistry;

/**
 * Manages E3 provisioning specific queries. It typically adds a custom WSDL
 * query handle for rejecting WSDL queries.
 * 
 * @author bonfroy
 * 
 */
@NoJSR250Annotations(unlessNull = "bus")
public class E3QueryHandlerRegistry implements QueryHandlerRegistry {
	private List<QueryHandler> queryHandlers;
	private Bus bus;

	public E3QueryHandlerRegistry() {
		queryHandlers = new CopyOnWriteArrayList<QueryHandler>();
	}

	public E3QueryHandlerRegistry(Bus b) {
		queryHandlers = new CopyOnWriteArrayList<QueryHandler>();
		setBus(b);
	}

	public E3QueryHandlerRegistry(Bus b, List<QueryHandler> handlers) {
		queryHandlers = new CopyOnWriteArrayList<QueryHandler>(handlers);
		setBus(b);
	}

	public void setQueryHandlers(List<QueryHandler> handlers) {
		this.queryHandlers = new CopyOnWriteArrayList<QueryHandler>(handlers);
	}

	public Bus getBus() {
		return bus;
	}

	public final void setBus(Bus b) {
		bus = b;
		if (queryHandlers == null) {
			queryHandlers = new CopyOnWriteArrayList<QueryHandler>();
		}
		if (null != bus) {
			bus.setExtension(this, QueryHandlerRegistry.class);

			// Inject our WSDL query handler
			WSDLQueryHandler wsdlQueryHandler = new WSDLQueryHandler();
			wsdlQueryHandler.setBus(bus);
			queryHandlers.add(wsdlQueryHandler);

			ConfiguredBeanLocator c = bus
					.getExtension(ConfiguredBeanLocator.class);
			if (c != null) {
				for (QueryHandler handler : c
						.getBeansOfType(QueryHandler.class)) {
					if (!queryHandlers.contains(handler)) {
						queryHandlers.add(handler);
					}
				}
			}
		}
	}

	public List<QueryHandler> getHandlers() {
		return queryHandlers;
	}

	public void registerHandler(QueryHandler handler) {
		queryHandlers.add(handler);
	}

	public void registerHandler(QueryHandler handler, int position) {
		queryHandlers.add(position, handler);
	}

}
