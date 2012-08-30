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
package com.alu.e3.tdr.rotator;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests RotatableWriterProvider.
 * 
 */
public class RotatableWriterProviderTest {
	
	private static final int MILLISECOND_THRESHOLD = 1 * 1000;   // short timeout, for testing
	private static final int CHARACTER_COUNT_THRESHOLD = 3;   // short length, for testing

	private static final String[] indexedTestStrings = {
		"the first test string",
		"the second test string",
		"the third test string"
	};


	// handy test class
	private class RotateableCharArryWriterProvider extends RotatableWriterProvider {
		
		int rotateCount = 0;   // to determine which test string is expected
		private CharArrayWriter currentCharArrayWriter;   // for verifying content

		public RotateableCharArryWriterProvider(long characterCountThreshold, long millisecondsThreshold) {
			super(characterCountThreshold, millisecondsThreshold);
		}

		@Override
		public Writer newWriter() throws IOException {
			return currentCharArrayWriter = new CharArrayWriter();
		}

		@Override
		public void rotate() {
			synchronized (getSynchroLock()) {
				super.rotate();
				// with each rotate operation, we expect to find the corresponding test string
				assertEquals(indexedTestStrings[rotateCount++], currentCharArrayWriter.toString());
			}
		}
	}

	@Test
	public void testConstuctor() throws IOException, InterruptedException {
		WriterProvider wp = new RotateableCharArryWriterProvider(1000, 3 * 1000);
		assertNotNull(wp);
	}

	@Test
	public void testTimeout() throws IOException, InterruptedException {
		// create a WriterProvider with a low timeout value
		WriterProvider wp = new RotateableCharArryWriterProvider(1000, MILLISECOND_THRESHOLD);
		assertNotNull(wp);
		for (int i = 0; i < indexedTestStrings.length; i++) {
			synchronized (wp.getSynchroLock()) {
				wp.getWriter().write(indexedTestStrings[i]);
			}
			Thread.sleep(MILLISECOND_THRESHOLD * 2);   // sleep long enough for a rotate to occur
		}
	}

	@Test
	public void testLength() throws IOException, InterruptedException {
		// create a WriterProvider with a low character count threshold
		WriterProvider wp = new RotateableCharArryWriterProvider(CHARACTER_COUNT_THRESHOLD, 30 * 1000);
		assertNotNull(wp);
		for (int i = 0; i < indexedTestStrings.length; i++) {
			synchronized (wp.getSynchroLock()) {
				wp.getWriter().write(indexedTestStrings[i]);   // write enough chars to trigger a rotate
			}
		}
	}
}
