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
package com.alu.e3.installer.command;

/**
 * Class ShellCommandResult
 *
 */
public class ShellCommandResult {
 
	/* members */
	private String m_strResult;
	private int m_nExitStatus;
	
	/**
	 * Constructor
	 * @param m_strResult
	 * @param m_nExitStatus
	 */
	public ShellCommandResult(String m_strResult, int m_nExitStatus) {
		this.m_strResult = m_strResult;
		this.m_nExitStatus = m_nExitStatus;
	}

	/**
	 * Return a string of the output of the command
	 * @return output of the command
	 */
	public String getResult() {
		return m_strResult;
	}

	/**
	 * Return a string of the status of the command
	 * @return exit status of the command
	 */
	public int getExitStatus() {
		return m_nExitStatus;
	}

	@Override
	public String toString() {
		return "[Result=" + m_strResult
				+ ", ExitStatus=" + m_nExitStatus + "]";
	} 
  }