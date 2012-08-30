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

import com.alu.e3.common.camel.ExchangeConstantKeys;
import com.alu.e3.tdr.TDRConstant;
import com.alu.e3.tdr.TDRDataService;

/**
 * This processor will be executed just before the outgoing soutbound call.
 * 
 *
 */
public class PreSouthboundTDRProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		// Now put the TDR values into the TDRDataService whose values have been extracted and put into the header for use here.
		// We don't want these headers to go all the way to the southbound endpoint so we'll remove them after extraction.
		
		if(exchange.getProperty(ExchangeConstantKeys.E3_SOAP_ACTION.toString()) != null){
			TDRDataService.setTxTDRProperty(TDRConstant.SOAP_ACTION, exchange.getProperty(ExchangeConstantKeys.E3_SOAP_ACTION.toString(), String.class), exchange);
		}
		
		// This entry needs to be as close the request as possible... so it goes last here.
		TDRDataService.setTxTDRProperty(TDRConstant.TARGET_REQ_TIME, System.currentTimeMillis(), exchange);
	}
}
