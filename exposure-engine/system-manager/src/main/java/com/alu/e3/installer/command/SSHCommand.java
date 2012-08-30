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
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alu.e3.data.model.SSHKey;
import com.jcraft.jsch.*;

/**
 * Class SSHCommand
 */
public class SSHCommand implements ICommand {

	/* slf4j logger */
	private static final Logger logger = LoggerFactory.getLogger(SSHCommand.class);

	/* members */
	private Session m_tSession;

	/**
	 * Constructor
	 */
	public SSHCommand()
	{
		m_tSession = null;
	}

	/**
	 * Finalize
	 */
	protected void finalize()
	{
		/* Make sure to disconnect session. */
		//if (m_tSession != null && m_tSession.isConnected())
		//	m_tSession.disconnect();
	} 

	/**
	 * Connect via ssh
	 * @param strPrivateKeyPath
	 * @param strTargetHostName
	 * @param nPortNumber
	 * @param strUser
	 * @param strPassword
	 * @throws JSchException
	 */
	public void connect(SSHKey key, String strTargetHostName, int nPortNumber, String strUser, String strPassword) throws JSchException
	{
		connect(key, strTargetHostName, nPortNumber, strUser, strPassword, 0);
	}

	/**
	 * Connect via ssh
	 * @param strPrivateKeyPath
	 * @param strTargetHostName
	 * @param nPortNumber
	 * @param strUser
	 * @param strPassword
	 * @throws JSchException
	 */
	public void connect(SSHKey key, String strTargetHostName, int nPortNumber, String strUser, String strPassword, int timeout) throws JSchException
	{
		/* Init ssh object with the private key. */
		JSch jsch = new JSch();
		if(key != null){
			jsch.addIdentity(key.getName(), key.getPrivateKey() == null ? null : key.getPrivateKey().clone(), key.getPublicKey(), null);
		}

		/* Init session, set password if needed. */
		m_tSession = jsch.getSession(strUser, strTargetHostName, nPortNumber);
		if (strPassword != null) {
			m_tSession.setPassword(strPassword);
		}

		/* Disable strict host checking. */
		java.util.Properties config = new java.util.Properties(); 
		config.put("StrictHostKeyChecking", "no");
		m_tSession.setConfig(config);

		/* Connect */
		if (timeout > 0) {
			m_tSession.connect(timeout);
		} else {
			m_tSession.connect();
		}
	}

	/**
	 * Disconnect the session.
	 */
	public void disconnect()
	{
		/* Disconnect session. */
		if (m_tSession != null)
			m_tSession.disconnect();
	}

	/**
	 * Reconnect with the settings used previously to connect.
	 * @throws JSchException
	 */
	public void reconnect() throws JSchException
	{
		/* Sanity check. */
		if (m_tSession == null)
			throw new JSchException("Session not initializad, use connect() first");

		/* connect session. */
		m_tSession.connect();
	}

	/**
	 * Is connected
	 */
	public boolean isConnected()
	{
		return m_tSession != null && m_tSession.isConnected();
	}

	/**
	 * Execute a shell command on the remote location.
	 * @param strCommand
	 * @param strAnswer
	 * @return Exit status of the command executed on the remote host
	 * @throws JSchException
	 * @throws IOException
	 */

	@Override
	public ShellCommandResult execShellCommand(String strCommand) throws IOException, JSchException {
		return execShellCommand(strCommand, null);
	}

	@Override
	public ShellCommandResult execShellCommand(String strCommand, String workingDir)
			throws JSchException, IOException {

		/* Sanity check. */
		if (m_tSession == null || !m_tSession.isConnected())
			throw new JSchException("Session not initializad, use connect() first");

		/* Change directory before executing the command. */
		if(workingDir != null)
			strCommand = "cd " + workingDir + "; " + strCommand;

		/* execute command */
		logger.debug("remote execShellCommand " + strCommand);
		Channel channel = m_tSession.openChannel("exec");
		((ChannelExec) channel).setCommand(strCommand);

		InputStream in = channel.getInputStream(); // Data coming from remote host
		((ChannelExec) channel).setErrStream(System.err);
		channel.connect();

		/* Get answer. */
		int nExitStatus = 0;
		StringBuilder strAnswer = new StringBuilder();
		byte[] buffer = new byte[1024];
		while (true) {
			while (in.available() > 0) {
				int i = in.read(buffer, 0, 1024);
				if (i < 0)
					break;
				strAnswer.append(new String(buffer, 0, i));
			}
			if (channel.isClosed()) {
				nExitStatus = channel.getExitStatus();
				break;
			}
			try {
				Thread.sleep(100);
			} catch (Exception ee) {
			}
		}
		channel.disconnect();

		/* return answer. */
		logger.debug("remote execShellCommand returned:\nexit status" + nExitStatus + "\n" + strAnswer);
		return new ShellCommandResult(strAnswer.toString(), nExitStatus);
	}

	/**
	 * Secure remote copy file
	 * 
	 * @param sourcePath
	 * @param targetPath
	 * @throws Exception
	 */
	public void copy(String sourcePath, String targetPath) throws JSchException, IOException {

		/* Sanity check. */
		if (m_tSession == null || !m_tSession.isConnected())
			throw new JSchException(
					"Session not initializad, use connect() first");

		logger.debug("remote copy from " + sourcePath + " to " + targetPath);
		String command = "scp -p -t  " + targetPath;
		Channel channel = m_tSession.openChannel("exec");
		((ChannelExec) channel).setCommand(command);

		// get I/O streams for remote scp
		OutputStream out = channel.getOutputStream();
		FileInputStream fis = null;
		try {
			channel.connect();

			// send "C0644 filesize filename", where filename should not include '/'
			long filesize = (new File(sourcePath)).length();
			logger.debug("scp filesize = " + filesize);
			command = "C0644 " + filesize + " ";
			if (sourcePath.lastIndexOf('/') > 0) {
				command += sourcePath.substring(sourcePath.lastIndexOf('/') + 1);
			}else if (sourcePath.lastIndexOf('\\') > 0){
				command += sourcePath.substring(sourcePath.lastIndexOf('\\') + 1);

			} else {
				command += sourcePath;
			}
			command += "\n";
			out.write(command.getBytes());
			out.flush();

			fis = new FileInputStream(sourcePath);
			byte[] buf = new byte[1024];

			while (true) {
				int len = fis.read(buf, 0, buf.length);
				if (len <= 0)
					break;
				out.write(buf, 0, len);
			}
			fis = null;
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();

		} finally {
			if (fis != null)
				fis.close();
			out.close();
		}

		// Wait for the end of the actual copy
		while (! channel.isEOF()) {
			logger.trace("Finishing the copy of " + sourcePath + " to " + targetPath);
		}

		channel.disconnect();

		logger.debug("scp done");
	}

	// From: http://www.jcraft.com/jsch/examples/ScpFrom.java.html
	public long copyFrom(String remotePath, String localPath) throws JSchException, IOException
	{
		/* Sanity check. */
		if ((m_tSession == null) || !m_tSession.isConnected()) {
			throw new JSchException("Session not initializad, use connect() first");
		}

		logger.debug("remote copy from {} to {}", remotePath, localPath);

		long copiedByteCount = 0L;
		FileOutputStream fos = null;

		String prefix = null;
		if (new File(localPath).isDirectory()) {
			prefix = localPath + File.separator;
		}

		// exec 'scp -f rfile' remotely
		String command="scp -f "+ remotePath;
		Channel channel = m_tSession.openChannel("exec");
		((ChannelExec)channel).setCommand(command);

		// get I/O streams for remote scp
		OutputStream out = channel.getOutputStream();
		InputStream in = channel.getInputStream();

		channel.connect();

		byte[] buf = new byte[1024];


		try {
			// send '\0'
			buf[0] = 0; 
			out.write(buf, 0, 1); 
			out.flush();

			while (true) {
				int c = checkAck(in);
				if (c != 'C') {
					break;
				}

				// read '0644 '
				in.read(buf, 0, 5);

				long filesize = 0L;
				while (true) {
					if (in.read(buf, 0, 1) < 0) {
						// error
						break; 
					}
					if (buf[0] == ' ') break;
					filesize = filesize*10L + (long)(buf[0] - '0');
				}

				String file = null;
				for (int i = 0; ; i++) {
					in.read(buf, i, 1);
					if (buf[i] == (byte)0x0a) {
						file = new String(buf, 0, i);
						break;
					}
				}

				//System.out.println("filesize="+filesize+", file="+file);

				// send '\0'
				buf[0] = 0; 
				out.write(buf, 0, 1); 
				out.flush();

				// read a content of lfile
				fos = new FileOutputStream(prefix == null ? localPath : prefix+file);
				int foo;
				while (true) {
					if (buf.length < filesize) foo=buf.length;
					else foo = (int)filesize;
					foo = in.read(buf, 0, foo);
					if (foo < 0) {
						// error 
						break;
					}
					fos.write(buf, 0, foo);
					filesize -= foo;
					copiedByteCount += foo;
					if (filesize == 0L) break;
				}
				fos.close();
				fos = null;

				if (checkAck(in) != 0) {
					System.exit(0);
				}

				// send '\0'
				buf[0] = 0; 
				out.write(buf, 0, 1); 
				out.flush();
			}

			channel.disconnect();
			logger.debug("scp done");

		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			try { if (fos != null) fos.close(); } catch(Exception ee){}
		}
		return copiedByteCount;
	}

	// For symmetry ...
	public void copyTo(String localPath, String remotePath) throws JSchException, IOException
	{
		copy(localPath, remotePath);
	}

	static int checkAck(InputStream in) throws IOException{
		int b=in.read();
		// b may be 0 for success,
		//          1 for error,
		//          2 for fatal error,
		//          -1
		if(b==0) return b;
		if(b==-1) return b;

		if(b==1 || b==2){
			StringBuffer sb=new StringBuffer();
			int c;
			do {
				c=in.read();
				sb.append((char)c);
			}
			while(c!='\n');
			if(b==1){ // error
				System.out.print(sb.toString());
			}
			if(b==2){ // fatal error
				System.out.print(sb.toString());
			}
		}
		return b;
	}

	@Override
	public String getImplementationType() {
		return "ssh";
	}

	public int getSessionTimeout()
	{
		return m_tSession.getTimeout();
	}

	public void setSessionTimeout(int timeout) throws JSchException
	{
		m_tSession.setTimeout(timeout);
	}

	public String getSessionUsername()
	{
		return m_tSession.getUserName();
	}

}