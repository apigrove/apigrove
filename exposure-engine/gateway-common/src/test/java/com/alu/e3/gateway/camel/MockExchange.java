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
package com.alu.e3.gateway.camel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.Message;
import org.apache.camel.spi.Synchronization;
import org.apache.camel.spi.UnitOfWork;

/**
 * Built to support the properties management stuff to test the TDRDataService
 *
 */
public class MockExchange implements Exchange {
	private Map<String, Object> properties = new HashMap<String,Object>();

	private Message in = new Message(){
		private Map<String, Object> headers = new HashMap<String, Object>();
		@Override
		public String getMessageId() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setMessageId(String messageId) {
			// TODO Auto-generated method stub

		}

		@Override
		public Exchange getExchange() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isFault() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setFault(boolean fault) {
			// TODO Auto-generated method stub

		}

		@Override
		public Object getHeader(String name) {
			return headers.get(name);
		}

		@Override
		public Object getHeader(String name, Object defaultValue) {
			// TODO Auto-generated method stub
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getHeader(String name, Class<T> type) {
			return (T) headers.get(name);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getHeader(String name, Object defaultValue, Class<T> type) {
			T value = getHeader(name, type);
			if(value == null)
				value = (T) defaultValue;

			return value;
		}

		@Override
		public void setHeader(String name, Object value) {
			headers.put(name, value);
		}

		@Override
		public Object removeHeader(String name) {
			Object result = headers.remove(name);
			return result;
		}

		@Override
		public boolean removeHeaders(String pattern) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean removeHeaders(String pattern,
				String... excludePatterns) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Map<String, Object> getHeaders() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setHeaders(Map<String, Object> headers) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean hasHeaders() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Object getBody() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object getMandatoryBody() throws InvalidPayloadException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <T> T getBody(Class<T> type) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <T> T getMandatoryBody(Class<T> type)
				throws InvalidPayloadException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setBody(Object body) {
			// TODO Auto-generated method stub

		}

		@Override
		public <T> void setBody(Object body, Class<T> type) {
			// TODO Auto-generated method stub

		}

		@Override
		public Message copy() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void copyFrom(Message message) {
			// TODO Auto-generated method stub

		}

		@Override
		public DataHandler getAttachment(String id) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<String> getAttachmentNames() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void removeAttachment(String id) {
			// TODO Auto-generated method stub

		}

		@Override
		public void addAttachment(String id, DataHandler content) {
			// TODO Auto-generated method stub

		}

		@Override
		public Map<String, DataHandler> getAttachments() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setAttachments(Map<String, DataHandler> attachments) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean hasAttachments() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public String createExchangeId() {
			// TODO Auto-generated method stub
			return null;
		}

	};

	@Override
	public ExchangePattern getPattern() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPattern(ExchangePattern pattern) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getProperty(String name) {
		return properties.get(name);
	}

	@Override
	public Object getProperty(String name, Object defaultValue) {
		Object ob = properties.get(name);
		if(ob == null){
			ob = defaultValue;
		}
		return ob;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getProperty(String name, Class<T> type) {
		return (T) properties.get(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getProperty(String name, Object defaultValue, Class<T> type) {
		T ob = (T) properties.get(name);
		if(ob == null){
			ob = (T) defaultValue;
		}
		return ob;
	}

	@Override
	public void setProperty(String name, Object value) {
		properties.put(name, value);
	}

	@Override
	public Object removeProperty(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> getProperties() {
		return properties;
	}

	@Override
	public boolean hasProperties() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Message getIn() {
		return in;
	}

	@Override
	public <T> T getIn(Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setIn(Message in) {
		// TODO Auto-generated method stub

	}

	@Override
	public Message getOut() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getOut(Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasOut() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setOut(Message out) {
		// TODO Auto-generated method stub

	}

	@Override
	public Exception getException() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getException(Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void setException(Throwable t) {
		// TODO Auto-generated method stub

	}


	@Override
	public boolean isFailed() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean isTransacted() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean isRollbackOnly() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public CamelContext getContext() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Exchange copy() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Endpoint getFromEndpoint() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void setFromEndpoint(Endpoint fromEndpoint) {
		// TODO Auto-generated method stub

	}


	@Override
	public String getFromRouteId() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void setFromRouteId(String fromRouteId) {
		// TODO Auto-generated method stub

	}


	@Override
	public UnitOfWork getUnitOfWork() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void setUnitOfWork(UnitOfWork unitOfWork) {
		// TODO Auto-generated method stub

	}


	@Override
	public String getExchangeId() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void setExchangeId(String id) {
		// TODO Auto-generated method stub

	}


	@Override
	public void addOnCompletion(Synchronization onCompletion) {
		// TODO Auto-generated method stub

	}


	@Override
	public void handoverCompletions(Exchange target) {
		// TODO Auto-generated method stub

	}


	@Override
	public List<Synchronization> handoverCompletions() {
		// TODO Auto-generated method stub
		return null;
	}

}
