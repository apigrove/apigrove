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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 /**
  * Class LocalCommand
  */
public class LocalCommand implements ICommand {
 
	/* slf4j logger */
	private static final Logger logger = LoggerFactory.getLogger(LocalCommand.class);
	
	/**
	 * Constructor
	 */
	public LocalCommand()
	{
	}
	
	/**
	 * Execute a shell command on the current system
	 * @param strCommand
	 * @param strAnswer
	 * @return Exit status of the command executed on the remote host
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	@Override
	public ShellCommandResult execShellCommand(String strCommand) throws IOException, InterruptedException {
		return execShellCommand(strCommand, null);
	}
	
	@Override
	public ShellCommandResult execShellCommand(String strCommand, String workingDir) throws IOException, InterruptedException {
		
		/* Init answer. */
		logger.debug("local execShellCommand " + strCommand + "(dir:" + workingDir + ")");
		int nExitStatus = 0;
		StringBuilder strAnswer = new StringBuilder();

		InputStream inStream = null;
		try {
			/* Execute command */
			Process aProcess;
			if(workingDir == null)
				 aProcess = Runtime.getRuntime().exec(strCommand);
			else
				 aProcess = Runtime.getRuntime().exec(strCommand, null, new File(workingDir));
			
			aProcess.waitFor();

			/* Get output */
			inStream = aProcess.getInputStream();
			nExitStatus = aProcess.exitValue();

			/* Read answer */
			byte buffer[] = new byte[1024];
			int nRead = inStream.read(buffer);
			while (nRead >= 0) {
				strAnswer.append(new String(buffer, 0, nRead));
				nRead = inStream.read(buffer);
			}
		} finally {
			if (inStream != null) {
				inStream.close();
			}
		}

		String answer = strAnswer.toString();
		/* return answer. */
		logger.debug("local execShellCommand returned: exit status" + nExitStatus + "\n" + answer);
		return new ShellCommandResult(answer, nExitStatus);
	}

	/**
	 * Copy file
	 * 
	 * @param sourcePath
	 * @param targetPath
	 * @throws IOException
	 */
	@Override
	public void copy(String sourcePath, String targetPath) throws IOException  {

		/* Fast java copy file */
		logger.debug("local copy from " + sourcePath + " to " + targetPath);
		File tDestFile = new File(targetPath);
		File tSourceFile = new File(sourcePath);

		if (!tDestFile.exists()) {
			tDestFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(tSourceFile).getChannel();
			destination = new FileOutputStream(tDestFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
		
		logger.debug("local copy done ");
	}

	/**
	 * 
	 */
	@Override
	public String getImplementationType() {
		
		return "local";
	}

	@Override
	public void disconnect() {
		
	}
}