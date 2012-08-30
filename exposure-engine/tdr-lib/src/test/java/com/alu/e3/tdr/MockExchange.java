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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.spi.Synchronization;
import org.apache.camel.spi.UnitOfWork;

/**
 * Built to support the properties management stuff to test the TDRDataService
 *
 */
public class MockExchange implements Exchange {
	private Map<String, Object> properties = new HashMap<String,Object>();
	
	public ExchangePattern getPattern() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setPattern(ExchangePattern pattern) {
		// TODO Auto-generated method stub

	}

	public Object getProperty(String name) {
		return properties.get(name);
	}

	public Object getProperty(String name, Object defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T getProperty(String name, Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T getProperty(String name, Object defaultValue, Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setProperty(String name, Object value) {
		properties.put(name, value);
	}

	public Object removeProperty(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public boolean hasProperties() {
		// TODO Auto-generated method stub
		return false;
	}

	public Message getIn() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T getIn(Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setIn(Message in) {
		// TODO Auto-generated method stub

	}

	public Message getOut() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T getOut(Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasOut() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setOut(Message out) {
		// TODO Auto-generated method stub

	}

	public Exception getException() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T getException(Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void setException(Throwable t) {
		// TODO Auto-generated method stub

	}

	
	public boolean isFailed() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean isTransacted() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean isRollbackOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public CamelContext getContext() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Exchange copy() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Endpoint getFromEndpoint() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void setFromEndpoint(Endpoint fromEndpoint) {
		// TODO Auto-generated method stub

	}

	
	public String getFromRouteId() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void setFromRouteId(String fromRouteId) {
		// TODO Auto-generated method stub

	}

	
	public UnitOfWork getUnitOfWork() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void setUnitOfWork(UnitOfWork unitOfWork) {
		// TODO Auto-generated method stub

	}

	
	public String getExchangeId() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void setExchangeId(String id) {
		// TODO Auto-generated method stub

	}

	
	public void addOnCompletion(Synchronization onCompletion) {
		// TODO Auto-generated method stub

	}

	
	public void handoverCompletions(Exchange target) {
		// TODO Auto-generated method stub

	}

	
	public List<Synchronization> handoverCompletions() {
		// TODO Auto-generated method stub
		return null;
	}

}
