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
/**
 * 
 */
package com.alu.e3.installer.command;

import java.net.UnknownHostException;

//import com.alu.e3.common.tools.CommonTools;
import com.alu.e3.data.model.SSHKey;

public class Command {
	
	/**
	 * Returns either a LocalCommand or SSHCommand instance, depending on hosts to connect from/to.
	 * @param  host
	 * @return A LocalCommand if remoteHost is the same machine as the one running the code, SSHCommand otherwise. 
	 * @throws UnknownHostException
	 */
	public static ICommand getCommand(String host) throws UnknownHostException {
		ICommand command = null;
		
		// We need to be root to add iptables rules... so only use SSH
//		if(CommonTools.isLocal(host)) {
//			command = new LocalCommand();
//		} else {
			command = new SSHCommand();
//		}
		
		return command;
		
	}
	
	/**
	 * Returns either a LocalCommand or SSHCommand connected instance, depending on hosts to connect from/to.
	 * @param SSHKey
	 * @param host
	 * @param SSHPort
	 * @param user
	 * @param password
	 * @return
	 * @throws UnknownHostException
	 */
	public static ICommand getCommandAndConnect(SSHKey SSHKey, String host, int SSHPort, String user, String password) throws UnknownHostException{
		
		ICommand command = null;
		
		// We need to be root to add iptables rules... so only use SSH
//		if(CommonTools.isLocal(host)) {
//			command = new LocalCommand();
//		} else {
			command = new SSHCommand();
			try{
				((SSHCommand) command).connect(SSHKey, host, SSHPort, user, password);
	
				if (!((SSHCommand) command).isConnected())
				{
					throw new Exception("SSH connection to " + host + " failed");
				}
			}catch(Exception e){
				
			}
//		}
		
		return command;
	}
	
}
