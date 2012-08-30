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
package com.alu.e3.tdr.camel.producer;

import java.lang.reflect.Method;
import java.net.InetAddress;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.apache.log4j.Logger;

import com.alu.e3.tdr.TDRConstant;
import com.alu.e3.tdr.TDRDataService;

public class TdrCommonRuleProducer extends DefaultProducer {
	private static Logger logger = Logger.getLogger(TdrCommonRuleProducer.class);
	private static String SYSTEM_ID = "";

	// The name of the tdr that gets generated on every transaction.  Default to txTDR
	private String txTDRName = "txTDR";

	static {
		String hostname;
		try { 
			hostname = ":"+InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			logger.error("Unable to initialize SYSTEM_ID", e);
			hostname = "";
		}
		SYSTEM_ID = new StringBuilder("E3:GATEWAY").append(hostname).toString();
	}


	public TdrCommonRuleProducer(Endpoint endpoint, String txTDRName) {
		super(endpoint);
		this.txTDRName = txTDRName;
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		TDRDataService.setTxTDRName(txTDRName, exchange);
		long firstInTimestamp = System.currentTimeMillis();
		TDRDataService.addCommonProperty(exchange, TDRConstant.TIMESTAMP, firstInTimestamp);
		TDRDataService.addCommonProperty(exchange, TDRConstant.TRANSACTION, exchange.getExchangeId());
		TDRDataService.addTxTDRProperty(exchange, TDRConstant.HTTP_METHOD, exchange.getIn().getHeader(Exchange.HTTP_METHOD));
		TDRDataService.addTxTDRProperty(exchange, TDRConstant.CLIENT_REQ_TIME, firstInTimestamp);

		/**
		 * This done to avoid adding a dependency on the servlet spec
		 */
		try{
			Object request =  exchange.getIn().getHeader(Exchange.HTTP_SERVLET_REQUEST);
			Method method = request.getClass().getMethod("getRemoteAddr");
			String clientIp = (String) method.invoke(request);
			TDRDataService.addCommonProperty(exchange, TDRConstant.CLIENT, clientIp);
		} catch(Exception e){
			logger.debug("Could not obtain clientIP");
		}

		String endpoint = exchange.getIn().getHeader(Exchange.HTTP_URL, String.class);
		TDRDataService.addTxTDRProperty(exchange, TDRConstant.ENDPOINT, endpoint);
		TDRDataService.addTxTDRProperty(exchange, TDRConstant.ENDPOINT_ACTION, exchange.getIn().getHeader(Exchange.HTTP_URI));
		TDRDataService.addCommonProperty(exchange, TDRConstant.SYSTEM, SYSTEM_ID);
	}

}
