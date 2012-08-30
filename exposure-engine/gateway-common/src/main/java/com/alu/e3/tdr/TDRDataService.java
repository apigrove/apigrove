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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Exchange;

import com.alu.e3.data.model.sub.TdrDynamicRule;
import com.alu.e3.data.model.sub.TdrGenerationRule;
import com.alu.e3.data.model.sub.TdrStaticRule;

/**
 * Class TDRDataService A thin facade that will help read and write TDR specific
 * properties on the Camel Exchange.
 * 
 * 
 */
public class TDRDataService {

	private static final String tdrDataKey = "__TDRDATA__";
	private static final String txTdrNameKey = "__TXTDRNAME__";

	private static final Map<String, Object> emptyMap = Collections.emptyMap();

	public static void setTxTDRName(String name, Exchange exchange) {
		exchange.setProperty(txTdrNameKey, name);
	}

	public static String getTxTDRName(Exchange exchange) {
		String txTDRName = (String) exchange.getProperty(txTdrNameKey);

		// Make sure that the name has some value... default to txTDR
		if (txTDRName == null || txTDRName.equals("")) {
			txTDRName = "txTDR";
		}

		return txTDRName; 
	}

	/**
	 * Adds a new property (name/value pair) which will be shared by all the
	 * TDRs on this exchange.
	 * 
	 * @param exchange
	 * @param name
	 * @param value
	 */
	public static void addCommonProperty(Exchange exchange, String name,
			Object value) {
		addToUniquelyNamedTdr(exchange, "common", name, value);
	}

	/**
	 * Adds a new property (name/value pair) to the tx TDR on this exchange.
	 * 
	 * @param exchange
	 * @param name
	 * @param value
	 */
	public static void addTxTDRProperty(Exchange exchange, String name,
			Object value) {
		String txTDRName = getTxTDRName(exchange);
		addToUniquelyNamedTdr(exchange, txTDRName, name, value);
	}

	/**
	 * Adds a new TDR (map of name/value pairs) of the specified tdrTypeName to
	 * this exchange.
	 * 
	 * @param exchange
	 * @param tdr
	 * @param tdrTypeName
	 */
	public static void addNewTdrMap(Exchange exchange, Map<String, Object> tdr,
			String tdrTypeName) {
		Map<String, List<Map<String, Object>>> tdrData = getTdrData(exchange);
		synchronized (tdrData) {
			List<Map<String, Object>> list = tdrData.get(tdrTypeName);
			if (list == null) {
				list = new ArrayList<Map<String, Object>>();
				tdrData.put(tdrTypeName, list);
			}
			list.add(tdr);
		}
	}

	/**
	 * Use this function to retrieve all TDR properties set on the exchange.
	 * <p>
	 * This method was useful when there was only a single TDR for an exchange,
	 * but now that there can be multiple TDRs per exchange this method doesn't
	 * make much sense. For backwards compatibility, this method now returns the
	 * tx properties.
	 * 
	 * @deprecated use getTxTDRProperties
	 * @param exchange
	 * @return Map<String, Object>
	 */
	@Deprecated
	public static Map<String, Object> getProperties(Exchange exchange) {
		return getTxTDRProperties(exchange);
	}

	/**
	 * Use this function to retrieve all tx TDR properties set on the exchange.
	 * <p>
	 * Note that the map returned by this method does not include the common
	 * properties.
	 * 
	 * @param exchange
	 * @return Map<String, Object>
	 */
	public static Map<String, Object> getTxTDRProperties(Exchange exchange) {
		String txTDRName = getTxTDRName(exchange);
		Map<String, Object> map = getFirstNamedTdr(exchange, txTDRName);
		return (map == null) ? emptyMap : map;
	}

	/**
	 * Returns a value string for a given property name. Returns NULL if name
	 * not found.
	 * <p>
	 * This method was useful when there was only a single TDR for an exchange,
	 * but now that there can be multiple TDRs per exchange this method doesn't
	 * make much sense. For backwards compatibility, this method now returns the
	 * tx property with the specified name.
	 * 
	 * @deprecated use getTxTDRProperty
	 * @param name
	 * @param exchange
	 * @return String
	 */
	@Deprecated
	public static Object getProperty(String name, Exchange exchange) {
		return getTxTDRProperty(name, exchange);
	}

	/**
	 * Returns a value string for a given tx property name. Returns NULL if name
	 * not found.
	 * 
	 * @param name
	 * @param exchange
	 * @return String
	 */
	public static Object getTxTDRProperty(String name, Exchange exchange) {
		return getTxTDRProperties(exchange).get(name);
	}

	/**
	 * Will set a name value pair on the Camel Exchange.
	 * 
	 * @deprecated use setTxTDRProperty
	 * @param String
	 *            name
	 * @param Object
	 *            value - this object should
	 * @param exchange
	 */
	@Deprecated
	public static void setProperty(String name, Object value, Exchange exchange) {
		setTxTDRProperty(name, value, exchange);
	}

	public static void setPropertyForType(String tdrTypeName, String name,
			String value, Exchange exchange) {

		if (tdrTypeName == null || tdrTypeName.equals("") || tdrTypeName.equals(getTxTDRName(exchange).toString())) {
			setTxTDRProperty(name, value, exchange);
		} else {
			Map<String, List<Map<String, Object>>> tdrData = getTdrData(exchange);
			
			if (tdrData != null) {
				List<Map<String, Object>> tdrList = tdrData.get(tdrTypeName);
				if (tdrList != null) {
					for (Map<String, Object> tdr : tdrList) {
						// append this name-value to each tdr
						tdr.put(name, value);
					}
				}
			}
		}
	}

	/**
	 * Will set a name value pair on the Camel Exchange.
	 * 
	 * @param String
	 *            name
	 * @param Object
	 *            value - this object should
	 * @param exchange
	 */
	public static void setTxTDRProperty(String name, Object value,
			Exchange exchange) {
		addTxTDRProperty(exchange, name, value);
	}

	/**
	 * Returns all the TDRs from this exchange, with common values replicated in
	 * each. TDR specific values take precedence over any common values with the
	 * same name.
	 * 
	 * @param exchange
	 * @return map (keyed on tdrTypeName) of list of TDR property maps
	 *         (name/value pairs)
	 */
	public static Map<String, List<Map<String, Object>>> getTdrs(
			Exchange exchange) {
		Map<String, List<Map<String, Object>>> resolvedTdrs = new HashMap<String, List<Map<String, Object>>>();
		Map<String, List<Map<String, Object>>> tdrData = getTdrData(exchange);

		synchronized (tdrData) {
			List<Map<String, Object>> commonList = tdrData.get("common");
			Map<String, Object> commonMap = (commonList != null) ? commonList.get(0) : emptyMap;

			for (Entry<String, List<Map<String, Object>>> tdrDataEntry: tdrData.entrySet()) {
				if ("common".equals(tdrDataEntry.getKey())) {
					continue; // not a real TDR
				}
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				for (Map<String, Object> tdrMap : tdrDataEntry.getValue()) {
					Map<String, Object> map = new HashMap<String, Object>(commonMap);
					map.putAll(tdrMap);
					list.add(map);
				}
				resolvedTdrs.put(tdrDataEntry.getKey(), list);
			}

			return resolvedTdrs;
		}
	}

	/**
	 * Clean the given Exchange about tdr datas
	 * 
	 * @param exchangeToClean
	 */
	public static void clean(Exchange exchangeToClean) {
		exchangeToClean.getProperties().remove(tdrDataKey);
	}

	/**
	 * Helper function to help do dynamic TDR value evaluation
	 * 
	 * @param name
	 * @param header
	 * @param exchange
	 */
	public static void setDynamicTxTDRProperty(String name, String header,
			Exchange exchange) {
		String resolvedValue = evalDynamicProperty(header, exchange);
		addTxTDRProperty(exchange, name, resolvedValue);
	}

	public static String evalDynamicProperty(String header, Exchange exchange) {
		String resolvedValue = exchange.getIn().getHeader(header, String.class);

		if (resolvedValue == null) {
			resolvedValue = exchange.getProperty(header, String.class);
		}
		
		if (resolvedValue == null) {
			resolvedValue = "";
		}

		return resolvedValue;
	}

	/**
	 * Helper function... simply converts the TdrGenerationRule into a
	 * Map<String, Object> and then calls the addNewTdrMap function above
	 * 
	 * @param exchange
	 * @param genRule
	 * @param tdrTypeName
	 */
	public static void addNewTdrGenerationRule(Exchange exchange,
			TdrGenerationRule genRule, String tdrTypeName) {
		Map<String, Object> map = new HashMap<String, Object>();
		for (TdrDynamicRule rule : genRule.getDynamicRules()) {
			map.put(rule.getTdrPropName(),
					evalDynamicProperty(rule.getHttpHeaderName(), exchange));
		}
		for (TdrStaticRule rule : genRule.getStaticRules()) {
			map.put(rule.getTdrPropName(), rule.getValue());
		}

		addNewTdrMap(exchange, map, tdrTypeName);
	}

	private static void addToUniquelyNamedTdr(Exchange exchange,
			String tdrTypeName, String name, Object value) {

		Map<String, List<Map<String, Object>>> tdrData = getTdrData(exchange);
		synchronized (tdrData) {
			List<Map<String, Object>> list = tdrData.get(tdrTypeName);
			if (list == null) {
				list = new ArrayList<Map<String, Object>>();
				list.add(new HashMap<String, Object>());
				tdrData.put(tdrTypeName, list);
			}
			Map<String, Object> map = list.get(0);
			map.put(name, value);
		}
	}

	private static Map<String, Object> getFirstNamedTdr(Exchange exchange,
			String tdrTypeName) {
		Map<String, List<Map<String, Object>>> tdrData = getTdrData(exchange);
		synchronized (tdrData) {
			List<Map<String, Object>> list = tdrData.get(tdrTypeName);
			return (list == null) ? null : list.get(0);
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<String, List<Map<String, Object>>> getTdrData(
			Exchange exchange) {
		synchronized (exchange) {
			Map<String, List<Map<String, Object>>> tdrData = (Map<String, List<Map<String, Object>>>) exchange
					.getProperties().get(tdrDataKey);
			if (tdrData == null) {
				tdrData = new HashMap<String, List<Map<String, Object>>>();
				exchange.setProperty(tdrDataKey, tdrData);
			}
			return tdrData;
		}
	}
}
