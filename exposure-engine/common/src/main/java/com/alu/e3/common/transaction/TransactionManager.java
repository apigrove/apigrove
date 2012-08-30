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

import java.util.concurrent.atomic.AtomicInteger;

public class TransactionManager implements ITransactionManager {

	 //  Using a final static counter is neither a good choice if we want a cluster-wide counter 
	private static final AtomicInteger counter = new AtomicInteger(0);

	private static final ThreadLocal<TransactionContext> threadLocalTransactionContext = new ThreadLocal<TransactionContext>() {
		@Override 
		protected TransactionContext initialValue() {
			return null;
		}
	};

	@Override
	public ITransaction getNewTransaction() throws TransactionLifecyleException {
		return new Transaction(this);
	}

	@Override
	public TransactionContext getTransactionContext() {
		return threadLocalTransactionContext.get(); 
	}
	
	public void attachTransaction(ITransaction transaction) throws TransactionLifecyleException {
		if (threadLocalTransactionContext.get() != null)
			throw new TransactionLifecyleException ("Can not start 2 transactions at the same time.");

		if (transaction.getTransactionId() != null)
			throw new TransactionLifecyleException ("Can not add a transaction that is already in a transaction context.");

		Integer newTransactionId = counter.getAndIncrement();
		
		transaction.setTransactionId(newTransactionId);
		TransactionContext transactionContext =  new TransactionContext(newTransactionId);

		threadLocalTransactionContext.set(transactionContext);
	}

	public void detachTransaction(ITransaction transaction) throws TransactionLifecyleException {
		if (threadLocalTransactionContext.get() == null)
			throw new TransactionLifecyleException ("No started transaction");

		threadLocalTransactionContext.set(null);
		transaction.setTransactionId(null);
	}
	
	public void rollback() {
		TransactionContext transactionContext = threadLocalTransactionContext.get();
		if (transactionContext != null)
			transactionContext.rollback();
	}

}
