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
package com.alu.e3.gateway;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;

import com.alu.e3.common.camel.AuthIdentity;
import com.alu.e3.common.camel.ExchangeConstantKeys;
import com.alu.e3.data.model.Api;
import com.alu.e3.data.model.Auth;
import com.alu.e3.data.model.CallDescriptor;
import com.alu.e3.data.model.Policy;
import com.alu.e3.data.model.enumeration.HeaderTransformationType;
import com.alu.e3.data.model.sub.HeaderTransformation;


public class HeaderTransHelper {

	/**
	 * Private helper function to do the work of iterating all of the lists of headers and applying them to the request/response
	 * @param trans
	 * @param type
	 * @param properties
	 * @param exchange
	 */
	private static void applyHeaderTransforms(List<HeaderTransformation> trans, HeaderTransformationType type, Map<String, String> properties, Exchange exchange){
		if(trans != null)
			for(HeaderTransformation tran : trans){
				if(tran.getType().equals(type)){
					switch (tran.getAction()){
					case ADD:
						if(tran.getValue() != null && !tran.getValue().equals(""))
							exchange.getIn().setHeader(tran.getName(), tran.getValue());
						else if(tran.getProperty() != null && !tran.getProperty().equals(""))
							exchange.getIn().setHeader(tran.getName(), properties.get(tran.getProperty()));
						break;
					case REMOVE:
						exchange.getIn().removeHeader(tran.getName());
						break;
					default:
						break;
					}
				}
			}
	}

	/**
	 * Public static function to centralize the functionality of applying the transformations to the request/response
	 * @param type
	 * @param exchange
	 */
	public static void applyHeaderTransforms(HeaderTransformationType type, Exchange exchange){
		AuthIdentity identity = (AuthIdentity) exchange.getProperty(ExchangeConstantKeys.E3_AUTH_IDENTITY.toString());
		@SuppressWarnings("unchecked")
		Map<String, String> properties = (Map<String,String>) exchange.getProperty(ExchangeConstantKeys.E3_MODEL_PROPERTIES.toString());
		if(properties == null)
			properties = new HashMap<String, String>();

		// First add the TDRs from the API
		Api api = identity.getApi();
		applyHeaderTransforms(api.getHeaderTransformations(), type, properties, exchange);

		// Next add all of the tdr values for the Policies
		Iterator<CallDescriptor> it = identity.getCallDescriptors().iterator();
		while(it.hasNext()){
			CallDescriptor cd = it.next();
			Policy policy = cd.getPolicy();
			if(policy != null){
				HeaderTransHelper.applyHeaderTransforms(policy.getHeaderTransformations(), type, properties, exchange);
			}
		}

		// Finally add the values from the Auth
		Auth auth = identity.getAuth();
		if(auth != null)
			HeaderTransHelper.applyHeaderTransforms(auth.getHeaderTransformations(), type, properties, exchange);
	}
}
