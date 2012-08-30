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

import java.io.IOException;
import java.io.Writer;

/**
 * Implements {@link WriterProvider}, providing a {@link Writer} (character output stream)
 * associated with a file, which is created and rotated (renamed and replaced with a new one)
 * over time. This class also implements RotatingWriter.Rotator, allowing it to be notified
 * when it's time to rotate the output file.
 * <p>
 * Note that file specific functionality is encapsulated in the {@link RotatableFileWriterProvider}
 * subclass.
 * 
 * @see {@link RotatableWriter}
 */
public abstract class RotatableWriterProvider implements WriterProvider, RotatableWriter.Rotator {

	/**
	 * Creates a new {@link Writer}, along with an underlying data store (such as a file).
	 * This method must be provided by a subclass.
	 * 
	 * @return the Writer for the new file
	 * @throws IOException
	 */
	public abstract Writer newWriter() throws IOException;

	// single synchronization lock object used to protect both writing and rotation
	private final Object synchroLock = new Object();

	// the current Writer being used to output characters (changes with each rotate operation)
	private RotatableWriter currentWriter = null;

	// number of characters written to this Writer that will trigger a rotate operation
	private final long characterCountThreshold;
	
	// number of milliseconds from initial write to this Writer that will trigger a rotate operation
	private final long millisecondsThreshold;

	public RotatableWriterProvider(long characterCountThreshold, long millisecondsThreshold) {
		this.characterCountThreshold = characterCountThreshold;
		this.millisecondsThreshold = millisecondsThreshold;
	}

	/**
	 * Returns the synchronization lock for this OutputWriterProvider & RotatingWriter.Rotator.
	 */
	@Override
	public Object getSynchroLock() {
		return synchroLock;
	}

	/**
	 * Returns the current Writer.
	 * <p>
	 * Repeated invocations of this method may return the same Writer until such time
	 * as the Writer gets closed, at which time this method may obtain a new Writer.
	 * <p>
	 * This implementation makes use of the subclass's newWriter() method to obtain
	 * a new Writer as needed, wrapping it in a RotatableWriter.
	 * <p>
	 * This implementation also checks the length (number of characters written so far),
	 * which may precipitate a rotate operation.
	 */
	@Override
	public Writer getWriter() throws IOException {
		synchronized (getSynchroLock()) {
			if (currentWriter != null) {
				currentWriter.checkLength();   // may precipitate invocation of rotate()
			}
			if (currentWriter == null) {
				// create a new Writer (character output stream) and wrap it
				currentWriter = new RotatableWriter(this, newWriter(),
						characterCountThreshold, millisecondsThreshold);
				openWriter(currentWriter);
			}
			return currentWriter;
		}
	}
	
	@Override
	public void stop() {
		rotate();   // make sure any pending output/rename is done
	}

	/**
	 * Invoked when the output file needs to be rotated (closed/renamed). A subclass
	 * should invoke this superclass method first, then do whatever is necessary to
	 * persist the data that got written by the Writer (such as rename files).
	 */
	public void rotate() {
		synchronized (getSynchroLock()) {
			Writer writer = currentWriter;
			if (writer != null) {
				currentWriter = null;
				try {
					closeWriter(writer);
					writer.close();   // may already be closed, but let's be sure
				} catch (IOException e) {
					// do nothing
				}
			}
		}
	}
	
	protected void openWriter(Writer writer) throws IOException {
	}
	
	protected void closeWriter(Writer writer) throws IOException {
	}
}
