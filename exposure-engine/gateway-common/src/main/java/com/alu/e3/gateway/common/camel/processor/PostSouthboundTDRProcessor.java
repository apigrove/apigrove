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

import java.net.InetAddress;
import java.net.URL;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.alu.e3.tdr.TDRConstant;
import com.alu.e3.tdr.TDRDataService;

/**
 * This processor will get run after the response from the Southbound endpoint
 *
 */
public class PostSouthboundTDRProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		TDRDataService.setTxTDRProperty(TDRConstant.TARGET_RESP_TIME, System.currentTimeMillis(), exchange);
		TDRDataService.setTxTDRProperty(TDRConstant.HTTP_CODE, exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE), exchange);
		TDRDataService.setTxTDRProperty(TDRConstant.RESP_SIZE, exchange.getIn().getHeader("Content-Length", String.class), exchange);

		// TDRConstant.TARGET_URL is null only on notification route
		// TDRConstant.TARGET_URL test used to not set two times value
		if(TDRDataService.getTxTDRProperty(TDRConstant.TARGET_URL, exchange) == null){
			URL targetURL = new URL(exchange.getIn().getHeader(Exchange.HTTP_URI, String.class));
			TDRDataService.setTxTDRProperty(TDRConstant.TARGET_URL, targetURL.toString(), exchange);
			TDRDataService.setTxTDRProperty(TDRConstant.TARGET_IP, InetAddress.getByName(targetURL.getHost()).getHostAddress(), exchange);
		}else{
			URL targetURL = new URL((String) TDRDataService.getTxTDRProperty(TDRConstant.TARGET_URL, exchange));
			TDRDataService.setTxTDRProperty(TDRConstant.TARGET_IP, InetAddress.getByName(targetURL.getHost()).getHostAddress(), exchange);
		}
		
		if(exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class) < 400){
			TDRDataService.setTxTDRProperty(TDRConstant.EVENT_TYPE, TDRConstant.EVENT_TYPE_OK, exchange);
		}else{
			TDRDataService.setTxTDRProperty(TDRConstant.EVENT_TYPE, TDRConstant.EVENT_TYPE_TARGETERROR, exchange);
		}
	}

}
