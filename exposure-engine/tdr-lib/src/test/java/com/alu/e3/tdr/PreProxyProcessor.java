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
package com.alu.e3.tdr;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class PreProxyProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		//String id = exchange.getIn().getHeader("id", String.class);
		
		// Store the exchangeId at the beginning so we can compare at the end to make sure it 
		// is persistant
		exchange.getIn().setBody("ID=123");
		TDRDataService.setTxTDRProperty("paramId", "123", exchange);
	}

}
