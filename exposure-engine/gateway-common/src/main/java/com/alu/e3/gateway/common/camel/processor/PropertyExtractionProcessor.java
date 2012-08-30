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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.alu.e3.common.camel.AuthIdentity;
import com.alu.e3.common.camel.ExchangeConstantKeys;
import com.alu.e3.data.model.CallDescriptor;
import com.alu.e3.data.model.Policy;

/**
 * This processor is meant to go through all of the applicable model to extract
 * the properties for use by Header Transformations, TDRs, etc.
 *
 */
public class PropertyExtractionProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		AuthIdentity identity = (AuthIdentity) exchange.getProperty(ExchangeConstantKeys.E3_AUTH_IDENTITY.toString());
		Map<String, String> props = resolveProperties(identity);

		exchange.setProperty(ExchangeConstantKeys.E3_MODEL_PROPERTIES.toString(), props);
	}

	/**
	 * Inner function to help do the work of merging all of the property maps
	 * By design we have decided that properties that have naming conflicts will be resolved by the following rules
	 * 
	 * auth > policy > api
	 * conflicts between policies will be resolved in an undefined way.
	 * 
	 * @param identity
	 * @return
	 */
	private Map<String, String> resolveProperties(AuthIdentity identity){
		Map<String, String> props = new HashMap<String,String>();

		props.putAll(identity.getApi().getProperties());
		Iterator<CallDescriptor> it = identity.getCallDescriptors().iterator();
		while(it.hasNext()){
			CallDescriptor cd = it.next();
			Policy policy = cd.getPolicy();
			if(policy != null){
				props.putAll(policy.getProperties());
			}
		}

		if(identity.getAuth() != null)
			props.putAll(identity.getAuth().getProperties());


		return props;
	}

}
