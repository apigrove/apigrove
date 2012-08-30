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
import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.builder.xml.XPathBuilder;

public class NotifyUrlWSAProcessor extends AbstractNotifyUrlProcessor {

	@Override
	public void process(Exchange exchange) throws Exception {

		String notifyUrl = XPathBuilder.xpath("//To", String.class).evaluate(exchange, String.class);
		
		if(notifyUrl.isEmpty()) {
			Namespaces ns = new Namespaces("wsa", "http://www.w3.org/2005/08/addressing");
			notifyUrl = ns.xpath("//wsa:To", String.class).evaluate(exchange, String.class);
		}

		injectUriAndQueryString(notifyUrl, exchange);
	}
	
}
