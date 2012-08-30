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

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.apache.log4j.Logger;

import com.alu.e3.tdr.TDRConstant;
import com.alu.e3.tdr.TDRDataService;
import com.alu.e3.tdr.service.ITdrQueueService;

public class TdrEmitProducer extends DefaultProducer {

	private static Logger logger = Logger.getLogger(TdrEmitProducer.class);
	private ITdrQueueService tdrQueueService;

	public TdrEmitProducer(Endpoint endpoint, ITdrQueueService tdrQueueService) {
		super(endpoint);
		if(tdrQueueService == null){
			throw new RuntimeException("Cannot initialize TdrEmitProducer, TdrQueueService is null");
		}

		this.tdrQueueService = tdrQueueService;
	}

	@Override 
	public void process(Exchange exchange) throws Exception {
		try{
			// Add the last timestamp
			TDRDataService.addTxTDRProperty(exchange, TDRConstant.CLIENT_RESP_TIME, System.currentTimeMillis());
			
			if(TDRDataService.getTxTDRProperty(TDRConstant.EVENT_TYPE, exchange) == null)
				TDRDataService.setTxTDRProperty(TDRConstant.EVENT_TYPE, TDRConstant.EVENT_TYPE_INTERNALERROR, exchange);
			
			// Add the TDRs to the queue to be written to disk
			tdrQueueService.putOrWait(TDRDataService.getTdrs(exchange));
			TDRDataService.clean(exchange);
		} catch(Exception e){
			logger.error(e.getMessage(), e);
		}
	}
}
