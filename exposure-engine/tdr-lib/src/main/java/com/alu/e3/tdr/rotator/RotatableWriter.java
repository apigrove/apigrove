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

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A monitored {@link Writer} (character output stream). An associated {@link Rotator}
 * is notified when the size or age of this RotatingWriter exceeds a specified threshold.
 * <p>
 * A new RotatingWriter is created for each new log file.
 * <p>
 * Everything is synchronized on a lock object provided by the Rotator.
 * 
 */
public class RotatableWriter extends FilterWriter {

	public interface Rotator {
		public Object getSynchroLock();
		public void rotate();   // invoked when the size or age exceeds a threshold
	}

	private final Rotator rotator;
	private final long charCountThreshold;
	private final long millisecondsThreshold;
	
	private boolean isClosed = false;

	private int currentCharCount = 0;
	private TimerTask timerTask;

	// protects against FilterWriter that may implement write methods in terms of each other
	private boolean reentered = false;

	public RotatableWriter(Rotator rotator, Writer writer,
			long charCountThreshold, long millisecondsThreshold) {
		super(writer);
		this.rotator = rotator;
		this.charCountThreshold = charCountThreshold;
		this.millisecondsThreshold = millisecondsThreshold;
	}

	/**
	 * Writes the specified array of characters to the output stream.
	 */
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		synchronized (rotator.getSynchroLock()) {
			boolean wasReentered = reentered;
			reentered = true;
			try {
				super.write(cbuf, off, len);
				if (!wasReentered) {
					countCharactersWritten(len);
				}
			} finally {
				reentered = wasReentered;
			}
		}
	}

	/**
	 * Writes the specified String to the output stream.
	 */
	@Override
	public void write(String str, int off, int len) throws IOException {
		synchronized (rotator.getSynchroLock()) {
			boolean wasReentered = reentered;
			reentered = true;
			try {
				super.write(str, off, len);
				if (!wasReentered) {
					countCharactersWritten(len);
				}
			} finally {
				reentered = wasReentered;
			}
		}
	}

	@Override
	public void write(int c) throws IOException {
		synchronized (rotator.getSynchroLock()) {
			boolean wasReentered = reentered;
			reentered = true;
			try {
				super.write(c);
				if (!wasReentered) {
					countCharactersWritten(1);
				}
			} finally {
				reentered = wasReentered;
			}
		}
	}

	/**
	 * Common code that is invoked after characters have been written.
	 * 
	 * @param len number of characters just written
	 */
	private void countCharactersWritten(int len) {
		if ((currentCharCount == 0) && (len > 0)) {
			// first write to a new file: start the timer
			startTimer();
		}
		currentCharCount += len;
	}

	/**
	 * Closes this output stream, stopping the timer and notifying the Rotator.
	 * <p>
	 * Note that this implementation may invoke the Rotator's rotate() method,
	 * which in turn could invoke this close() method. Both methods protect
	 * themselves against runaway recursion.
	 */
	@Override
	public void close() {
		synchronized (rotator.getSynchroLock()) {
			if (isClosed) {
				return;   // once is enough
			}
			isClosed = true;
			rotator.rotate();   // end of the line for this RotatingWriter
			stopTimer();
			try {
				super.close();
			} catch (Exception e) {
				// do nothing
			}
		}
	}

	/**
	 * Checks the length (number of characters written) of the output stream.
	 * If it exceeds the threshold, closes the stream, which in turn causes
	 * the Rotator to be notified.
	 */
	public void checkLength() {
		synchronized (rotator.getSynchroLock()) {
			if (currentCharCount >= charCountThreshold) {
				close();
			}
		}
	}

	/**
	 * Starts the one-shot timer associated with this Writer. When the timer
	 * expires this Writer will be closed and the Rotator will be notified.
	 */
	private void startTimer() {
		timerTask = new TimerTask() {
			@Override
			public void run() {
				synchronized (rotator.getSynchroLock()) {
					close();   // time expired
				}
			}
		};
		new Timer("RotatingWriter", true).schedule(timerTask, millisecondsThreshold);
	}

	/**
	 * Stops (cancels) this timer.
	 */
	private void stopTimer() {
		if (timerTask != null) {
			timerTask.cancel();   // stop the timer
			timerTask = null;
		}
	}
}
