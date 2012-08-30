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
package com.alu.e3.tdr.camel.endpoint;

import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;

import com.alu.e3.tdr.camel.producer.TdrCommonRuleProducer;
import com.alu.e3.tdr.camel.producer.TdrDynamicRuleProducer;
import com.alu.e3.tdr.camel.producer.TdrEmitProducer;
import com.alu.e3.tdr.camel.producer.TdrStaticRuleProducer;
import com.alu.e3.tdr.service.ITdrQueueService;

public class TdrRuleEndpoint extends DefaultEndpoint {

	public static enum Type {
		COMMON,
		STATIC,
		DYNAMIC,
		EMIT
	}

	private Type type;
	private String tdrTypeName;
	private String propName;
	private String staticValue;
	private String headerName;
	private String txTDRName;
	private ITdrQueueService tdrQueueService;

	public TdrRuleEndpoint(String uri, Component component, String type, ITdrQueueService tdrQueueService) {
		super(uri, component);

		if (type == null) throw new IllegalArgumentException("TdrRuleEndpoint<init>(type) must not be null");
		if (tdrQueueService == null) throw new IllegalArgumentException("TdrQueueService must not be null");
		this.type = Type.valueOf(type);

		this.tdrQueueService = tdrQueueService;
	}

	@Override
	public Producer createProducer() throws Exception {
		if (Type.COMMON == type)
			return new TdrCommonRuleProducer(this, txTDRName);
		else if (Type.STATIC == type)
			return new TdrStaticRuleProducer(this, propName, staticValue, tdrTypeName);
		else if (Type.DYNAMIC == type)
			return new TdrDynamicRuleProducer(this, propName, headerName, tdrTypeName);
		else if (Type.EMIT == type)
			return new TdrEmitProducer(this, tdrQueueService);
		else
			throw new UnsupportedOperationException("No producer of type: "+type);
	}

	@Override
	public Consumer createConsumer(Processor processor) throws Exception {
		throw new UnsupportedOperationException("TdrRuleComponent does not support instance as consumer");
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

	public void setPropName(String propName) {
		this.propName = propName;
	}

	public void setStaticValue(String staticValue) {
		this.staticValue = staticValue;
	}

	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}

	public void setTdrTypeName(String tdrTypeName){
		this.tdrTypeName = tdrTypeName;
	}

	public void setTxTDRName(String name){
		this.txTDRName = name;
	}
}
