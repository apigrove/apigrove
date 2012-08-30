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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Extends {@link RotatableWriterProvider} and encapsulates File specific functionality.
 * A subclass may extend this to implement specific file names.
 *
 */
public class RotatableFileWriterProvider extends RotatableWriterProvider {

	// directory in which the files are to be maintained
	private final File dir;
	
	public RotatableFileWriterProvider(File dir,
			long characterCountThreshold, long millisecondsThreshold) {
		super(characterCountThreshold, millisecondsThreshold);
		this.dir = dir;
	}
	
	/**
	 * Creates a new file and {@link Writer}.
	 * 
	 * @return the Writer for the new file
	 * @throws IOException
	 */
	public Writer newWriter() throws IOException {
		return new BufferedWriter(new FileWriter(new File(dir, getTempFileName())));
	}

	/**
	 * Renames the current file.
	 */
	public void rotate() {
		synchronized (getSynchroLock()) {
			super.rotate();
			new File(dir, getTempFileName()).renameTo(new File(dir, getFileName()));
		}
	}

	protected String getTempFileName() {
		return "temp";
	}

	protected String getFileName() {
		return "log." + System.currentTimeMillis() + ".txt";
	}
}
