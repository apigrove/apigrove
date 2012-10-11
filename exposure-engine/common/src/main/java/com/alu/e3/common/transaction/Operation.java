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
package com.alu.e3.common.transaction;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.alu.e3.common.caching.ICacheTable;

public class Operation <K, V> {
	private final static Logger log = LoggerFactory.getLogger(Operation.class);
	
	protected OperationType operationType;
	protected K key;
	protected V previousValue;
	protected ICacheTable<K, V> cacheTable;
	protected List<String> successfulLocalOperations ;

	public Operation(OperationType operationType, K key, V previousValue, ICacheTable<K, V> cacheTable) throws InvalidParameterException {
		super();

		if (cacheTable == null) 
			throw new InvalidParameterException("the parameter 'cacheTable' cannot be null");
		
		if (key == null) 
			throw new InvalidParameterException("the parameter 'key' cannot be null");
		
		if (operationType == OperationType.CREATE && previousValue != null)
			if (log.isWarnEnabled()) {
				log.warn("An entry that is to be created but seem to have a previous value (key: " + key + ", map: " + cacheTable.getName() + ")");
			}
		
		if (operationType == OperationType.UPDATE && previousValue == null)
			if (log.isWarnEnabled()) {
				log.warn("An entry that is to be updated does not already exist (key: " + key + ", map: " + cacheTable.getName() + ")");
			}

		this.operationType = operationType;
		this.key = key;
		this.previousValue = previousValue;
		this.cacheTable = cacheTable;
		this.successfulLocalOperations = new ArrayList<String>();
	}

	public void addSuccessfulLocalOperation (String instance) {
		successfulLocalOperations.add(instance);
	}
	
	public void removeSuccessfulLocalOperation (String instance) {
		successfulLocalOperations.remove(instance);
	}
	
	public List<String> getSuccessfulLocalOperations () {
		return successfulLocalOperations;
	}
	
	public OperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}

	public K getKey() {
		return key;
	}

	public void setKey(K key) {
		this.key = key;
	}

	public V getPreviousValue() {
		return previousValue;
	}

	public void setPreviousValue(V previousValue) {
		this.previousValue = previousValue;
	}

	public ICacheTable<K, V> getCacheTable() {
		return cacheTable;
	}

	public void setCacheTable(ICacheTable<K, V> cacheTable) {
		this.cacheTable = cacheTable;
	}
	
	public void rollback() {
		if (operationType ==  OperationType.CREATE) {
			// A new data was inserted, try to remove
			cacheTable.remove(key);
		} else if (operationType ==  OperationType.UPDATE) {
			// A data was update, try to restore the previous value
			cacheTable.set(key, previousValue);
		} else if (operationType ==  OperationType.DELETE) {
			// A data was update, try to restore the previous value
			cacheTable.set(key, previousValue);
		} 			
	}
}
