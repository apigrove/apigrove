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
import org.apache.log4j.Logger;

import com.alu.e3.common.camel.ExchangeConstantKeys;
import com.alu.e3.data.model.ExtractFromType;
import com.alu.e3.gateway.TdrProcessorHelper;

/**
 * This processor is meant to do the work of pulling the Provisioned TDR values
 * out of the data model and inserting them into the TDR emission structure.
 * 
 *
 */
public class TDRResponseProcessor implements Processor {
	private static Logger logger = Logger.getLogger(TDRResponseProcessor.class);

	// The key to our Exchange property that indicates whether this has already been run.
	// We only want to run it once and it could run twice if there is an error on the route.
	private static String TDR_RES_PROC_RUN_KEY = "TDR_RES_PROC_RUN";

	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			Boolean alreadyRan = exchange.getProperty(TDR_RES_PROC_RUN_KEY, Boolean.FALSE, Boolean.class); 
			Boolean isResponse = exchange.getProperty(ExchangeConstantKeys.E3_GOT_SB_RESPONSE.toString(), Boolean.FALSE, Boolean.class);

			if(!alreadyRan && isResponse){
				TdrProcessorHelper.processTdrRules(exchange, ExtractFromType.Response, false);
				exchange.setProperty(TDR_RES_PROC_RUN_KEY, Boolean.TRUE);
			}
		} catch(Exception e){
			if (logger.isErrorEnabled()) {
				logger.error(e.getMessage(), e);
			}
		}

	}
}
