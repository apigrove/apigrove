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
import java.util.Map;

import org.apache.camel.Exchange;

import com.alu.e3.common.camel.AuthIdentity;
import com.alu.e3.common.camel.ExchangeConstantKeys;
import com.alu.e3.data.model.Api;
import com.alu.e3.data.model.Auth;
import com.alu.e3.data.model.CallDescriptor;
import com.alu.e3.data.model.ExtractFromType;
import com.alu.e3.data.model.Policy;
import com.alu.e3.data.model.sub.TdrDynamicRule;
import com.alu.e3.data.model.sub.TdrGenerationRule;
import com.alu.e3.data.model.sub.TdrStaticRule;
import com.alu.e3.tdr.TDRDataService;

public class TdrProcessorHelper {
	public static void processTdrRules(Exchange exchange, ExtractFromType efType, boolean doStatic){
		AuthIdentity identity = (AuthIdentity) exchange.getProperty(ExchangeConstantKeys.E3_AUTH_IDENTITY.toString());
		if (identity == null) return;
		@SuppressWarnings("unchecked")
		Map<String, String> properties = (Map<String,String>) exchange.getProperty(ExchangeConstantKeys.E3_MODEL_PROPERTIES.toString());
		if(properties == null)
			properties = new HashMap<String, String>();

		// First add the TDRs from the API
		Api api = identity.getApi();
		if (api != null)
			if(doStatic)
				processTdrGenerationRuleStatic(api.getTdrGenerationRule(), exchange, properties);
			else
				processTdrGenerationRuleDynamic(api.getTdrGenerationRule(), exchange, properties, efType);

		// Next add all of the tdr values for the Policies
		Iterator<CallDescriptor> it = identity.getCallDescriptors().iterator();
		while(it.hasNext()){
			CallDescriptor cd = it.next();
			Policy policy = cd.getPolicy();
			if(policy != null){
				if(doStatic)
					processTdrGenerationRuleStatic(policy.getTdrGenerationRule(), exchange, properties);
				else
					processTdrGenerationRuleDynamic(policy.getTdrGenerationRule(), exchange, properties, efType);
			}
		}

		// Finally add the values from the Auth
		Auth auth = identity.getAuth();
		if(auth != null)
			if(doStatic)
				processTdrGenerationRuleStatic(auth.getTdrGenerationRule(), exchange, properties);
			else
				processTdrGenerationRuleDynamic(auth.getTdrGenerationRule(), exchange, properties, efType);
	}

	/**
	 * Inner helper function to help iterate through all the TdrGenerationRules from all
	 * of the relevant objects, adding the TDR values to the TDRDataService. 
	 * 
	 * @param genRule
	 * @param exchange
	 */
	private static void processTdrGenerationRuleDynamic(TdrGenerationRule genRule, Exchange exchange, Map<String, String> properties, ExtractFromType efType){
		// If the genRule is null then we don't want to continue
		if(genRule == null) return;

		// First go through the dynamic rules
		if(genRule.getDynamicRules() != null){
			for (TdrDynamicRule rule : genRule.getDynamicRules()) {
				if(rule.getExtractFrom().equals(efType) || rule.getExtractFrom().equals(ExtractFromType.Either)){
					for(String type : rule.getTypes()){
						TDRDataService.setPropertyForType(type, 
								rule.getTdrPropName(), 
								TDRDataService.evalDynamicProperty(rule.getHttpHeaderName(), exchange), 
								exchange);
					}

					if(rule.getTypes() == null || rule.getTypes().size() <= 0){
						TDRDataService.addCommonProperty(exchange, rule.getTdrPropName(), 
								TDRDataService.evalDynamicProperty(rule.getHttpHeaderName(), exchange));
					}
				}
			}
		}
	}

	private static void processTdrGenerationRuleStatic(
			TdrGenerationRule genRule, Exchange exchange,
			Map<String, String> properties) {
		if(genRule == null) return;
		// If we are processing the response and there are static rules
		for (TdrStaticRule rule : genRule.getStaticRules()) {
			for(String type : rule.getTypes()){
				// If truly a static value
				if(rule.getValue() != null && !rule.equals(""))
					TDRDataService.setPropertyForType(type, 
							rule.getTdrPropName(), 
							rule.getValue(), 
							exchange);
				// If a property value
				else if(rule.getPropertyName() != null && !rule.getPropertyName().equals("")){
					String value = ""; // Default
					if(properties.containsKey(rule.getPropertyName())){
						value = properties.get(rule.getPropertyName());	
					}

					TDRDataService.setPropertyForType(type.toString(), 
							rule.getTdrPropName(), 
							value, 
							exchange);
				}
			}

			// If not types are specified then we'll add the property to the common Map, so it will
			// be appended to ALL TDRs generated in this transaction
			if(rule.getTypes() == null || rule.getTypes().size() <= 0){
				if(rule.getValue() != null && !rule.equals(""))
					TDRDataService.addCommonProperty(exchange, rule.getTdrPropName(), rule.getValue());
				else if(rule.getPropertyName() != null && !rule.getPropertyName().equals(""))
					TDRDataService.addCommonProperty(exchange, rule.getTdrPropName(), properties.get(rule.getPropertyName()));
			}
		}
	}


}
