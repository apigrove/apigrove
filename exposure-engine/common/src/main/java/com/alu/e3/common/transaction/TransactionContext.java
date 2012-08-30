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

import java.util.ArrayList;
import java.util.List;


public class TransactionContext {
	protected Integer transactionId;
	protected List<Operation<?, ?>> operations;
	protected boolean isRollbackOnly, isRollbacking;
	protected Operation<?, ?> rollbackingOperation;

	public Operation<?, ?> getRollbackingOperation() {
		return rollbackingOperation;
	}

	public void setRollbackingOperation(Operation<?, ?> rollbackingOperation) {
		this.rollbackingOperation = rollbackingOperation;
	}

	public boolean isRollbacking() {
		return isRollbacking;
	}

	public void setRollbacking(boolean isRollbacking) {
		this.isRollbacking = isRollbacking;
	}

	public boolean isRollbackOnly() {
		return isRollbackOnly;
	}

	public void setRollbackOnly(boolean isRollbackOnly) {
		this.isRollbackOnly = isRollbackOnly;
	}

	public TransactionContext(Integer transactionId) {
		super();
		this.transactionId = transactionId;
		
		operations = new ArrayList<Operation<?, ?>>();
		isRollbackOnly = false;
		isRollbacking = false;
		rollbackingOperation = null;
	}

	public Integer getTransactionId() {
		return transactionId;
	}

	public void addOperation(Operation<?, ?> operation) {
		operations.add(operation);
	}

	public void rollback() {
		isRollbackOnly = false;
		isRollbacking = true;
		for (int i = operations.size()-1; i >= 0; i--) {
			rollbackingOperation = operations.get(i);
			operations.get(i).rollback();
		} 
		rollbackingOperation = null;
		isRollbacking = false;
	}
	
}
