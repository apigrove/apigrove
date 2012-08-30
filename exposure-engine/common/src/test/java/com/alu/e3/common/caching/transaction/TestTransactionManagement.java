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
package com.alu.e3.common.caching.transaction;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import com.alu.e3.common.transaction.ITransaction;
import com.alu.e3.common.transaction.ITransactionManager;
import com.alu.e3.common.transaction.TransactionManager;
import com.alu.e3.common.transaction.TransactionContext;

public class TestTransactionManagement {

	@Test
	public void testTransactionInCurrentThread() throws Exception {
		ITransactionManager txMnger = new TransactionManager();
		assertNull(txMnger.getTransactionContext());

		// create, begin, commit, end
		ITransaction tx = txMnger.getNewTransaction();
		assertNotNull(tx);

		// Not transaction id until the transaction has begun
		assertNull(tx.getTransactionId());
		// Not transaction context until the transaction has begun
		assertNull(txMnger.getTransactionContext());


		tx.begin();
		assertNotNull (tx.getTransactionId());
		assertNotNull (txMnger.getTransactionContext().getTransactionId());
		assertEquals (txMnger.getTransactionContext().getTransactionId(), tx.getTransactionId());

		tx.commit();
		assertNotNull (tx.getTransactionId());
		assertNotNull (txMnger.getTransactionContext().getTransactionId());
		assertEquals (txMnger.getTransactionContext().getTransactionId(), tx.getTransactionId());

		tx.end();
		assertNull(tx.getTransactionId());
		assertNull(txMnger.getTransactionContext());

		// create, begin, rollback
		tx = txMnger.getNewTransaction();
		assertNull(tx.getTransactionId());
		assertNull(txMnger.getTransactionContext());

		tx.begin();
		assertNotNull (tx.getTransactionId());
		assertNotNull (txMnger.getTransactionContext().getTransactionId());
		assertEquals (txMnger.getTransactionContext().getTransactionId(), tx.getTransactionId());

		tx.rollback();
		assertNull(tx.getTransactionId());
		assertNull(txMnger.getTransactionContext());

		// begin, commit, rollback
		tx = txMnger.getNewTransaction();
		assertNull(tx.getTransactionId());
		assertNull(txMnger.getTransactionContext());

		tx.begin();
		assertNotNull (tx.getTransactionId());
		assertNotNull (txMnger.getTransactionContext().getTransactionId());
		assertEquals (txMnger.getTransactionContext().getTransactionId(), tx.getTransactionId());

		tx.commit();
		assertNotNull (tx.getTransactionId());
		assertNotNull (txMnger.getTransactionContext().getTransactionId());
		assertEquals (txMnger.getTransactionContext().getTransactionId(), tx.getTransactionId());

		tx.rollback();
		assertNull(tx.getTransactionId());
		assertNull(txMnger.getTransactionContext());
	}

	@Test
	public void testTransactionStateOverThreads() throws Exception {

		ExecutorService executor1 = Executors.newSingleThreadScheduledExecutor();

		// Create, then begin a transaction
		TransactionContext transactionState1 = executor1.submit( new Callable<TransactionContext>() {
			@Override
			public TransactionContext call() throws Exception {
				ITransactionManager txMnger = new TransactionManager();
				ITransaction tx = txMnger.getNewTransaction();
				tx.begin();
				return txMnger.getTransactionContext();
			}
		}).get();

		assertNotNull(transactionState1);
		assertNotNull(transactionState1.getTransactionId());
		
		// Later, a method in the same thread stack should be able to deal with the same transaction context 
		TransactionContext transactionState2 = executor1.submit( new Callable<TransactionContext>() {
			@Override
			public TransactionContext call() throws Exception {
				ITransactionManager txMnger = new TransactionManager();
				return txMnger.getTransactionContext();
			}
		}).get();
		
		assertNotNull(transactionState2);
		assertNotNull(transactionState2.getTransactionId());
		assertEquals(transactionState1.getTransactionId(), transactionState2.getTransactionId());

		// The transaction in a thread must not be viewable from another thread 
		ExecutorService executor2 = Executors.newSingleThreadScheduledExecutor();
		TransactionContext transactionState3 = executor2.submit( new Callable<TransactionContext>() {
			@Override
			public TransactionContext call() throws Exception {
				ITransactionManager txMnger = new TransactionManager();
				return txMnger.getTransactionContext();
			}
		}).get();
		assertNull (transactionState3);
		
		transactionState3 = executor2.submit( new Callable<TransactionContext>() {
			@Override
			public TransactionContext call() throws Exception {
				ITransactionManager txMnger = new TransactionManager();
				ITransaction tx = txMnger.getNewTransaction();
				tx.begin();
				return txMnger.getTransactionContext();
			}
		}).get();
		
		assertNotNull (transactionState3);
		assertTrue(transactionState1.getTransactionId() != transactionState3.getTransactionId());
	}
}
