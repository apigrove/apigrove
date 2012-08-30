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
 * Provides a {@link Writer} (character output stream), which may change over time.
 * 
 */
public interface WriterProvider {

	/**
	 * Returns the synchronization lock for this OutputWriterProvider.
	 * 
	 * @return the synchronization lock
	 */
	public Object getSynchroLock();

	/**
	 * Returns the current Writer (character output stream).
	 * 
	 * @return the current Writer
	 * @throws IOException
	 */
	public Writer getWriter() throws IOException;

	/**
	 * Flushes any pending output.
	 */
	public void stop();
}
