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
package com.alu.e3.installer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import com.alu.e3.Utilities;
import com.alu.e3.common.E3Constant;
import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.common.spring.SpringContextBootstrapper;
import com.alu.e3.common.tools.CommonTools;
import com.alu.e3.data.model.Instance;
import com.alu.e3.data.model.SSHKey;
import com.alu.e3.installer.command.Command;
import com.alu.e3.installer.command.ICommand;
import com.alu.e3.installer.command.SSHCommand;
import com.alu.e3.installer.command.ShellCommandResult;
import com.alu.e3.installer.model.Configuration;
import com.alu.e3.installer.model.InstallerDeployException;
import com.alu.e3.installer.parsers.InstallerConfigurationParser;
import com.alu.e3.installer.parsers.InstallerParserException;
import com.alu.e3.topology.model.Topology;
import com.jcraft.jsch.JSchException;

/**
 * Class Installer
 */
public class Installer {

	/* Members */
	private Map<String, List<Configuration>> configurations;
	
	private static final CategoryLogger logger = CategoryLoggerFactory.getLogger(Installer.class, Category.SYS);
	
	private Topology topology;

	private SpringContextBootstrapper springContextBoostrapper;
	
	/**
	 * Constructor
	 * @param configFilePath
	 * @throws Exception 
	 */
	public Installer(String configFilePath, Topology topology, SpringContextBootstrapper springCtxBootstrapper) throws Exception {
		try {
			this.topology = topology;
			this.springContextBoostrapper = springCtxBootstrapper;
			
			/* Parsing Configuration File */
			if(logger.isDebugEnabled()) {
				logger.debug("new instance Installer");
				logger.debug("Parsing xml: " + configFilePath);
			}
			InstallerConfigurationParser configParser = new InstallerConfigurationParser();
			this.configurations = configParser.parse(configFilePath);
		}catch (Exception e){
			if(logger.isErrorEnabled()) {
				logger.error("Exception: " + e.getMessage() + "\n" + Utilities.getStackTrace(e));
			}
			e.printStackTrace();
			throw new Exception("Xml parsing failed", e); 
		}
	}
	
	/**
	 * Constructor
	 * @param configInput
	 * @throws Exception 
	 */
	public Installer(InputStream configInput) throws Exception {
		try {
			if(logger.isDebugEnabled()) {
				logger.debug("new instance Installer");
				/* Parsing Configuration InputStream */
				logger.debug("Parsing xml: configInput stream");
			}
			InstallerConfigurationParser configParser = new InstallerConfigurationParser();
			this.configurations = configParser.parse(configInput);
		}catch (Exception e){
			if(logger.isErrorEnabled()) {
				logger.error("Exception: " + e.getMessage() + "\n" + Utilities.getStackTrace(e));
			}
			e.printStackTrace();
			throw new Exception("Xml parsing failed", e); 
		}
	}
	
	/**
	 * Deploy EE as specified in the configuration files.
	 * @throws UnknownHostException 
	 * @throws IOException 
	 * @throws JSchException 
	 * @throws InstallerParserException 
	 */
	public void deploy() throws Exception {
		
		/* Get local ip and host name. */
		String managerIP = CommonTools.getLocalAddress();
		if(logger.isDebugEnabled()) {
			logger.debug("call Installer.deploy() on machine " + CommonTools.getLocalHostname() + "(IP:" + managerIP + ")");
		}
		
		//retrieve manager area
		String managerArea = getManagerAreaByIP(CommonTools.getLocalHostname(), managerIP);
	
		/* Loop on all the configurations to install */
		for (String configName : configurations.keySet())
		{
			List<Instance> instanceList = topology.getInstancesByType(configName);
			
			for(Instance instance : instanceList)
			{
				try {					
					String localizedGatewayIP = getLocalizedGatewayIP(instance, managerArea);
					if(logger.isDebugEnabled()) {
						logger.debug("deploying: '" + instance.toString() + "' using ip '" + localizedGatewayIP + "'");
					}
					
					/* copy and launch installers */
					List<Configuration> instanceConfigurations = configurations.get(instance.getType());
					if(instanceConfigurations != null) {
						for(Configuration config : instanceConfigurations) {
							if(logger.isDebugEnabled()) {
								logger.debug("installing: " + config);
							}
							
							/* copy and start installer setup script. */
							ICommand cmd = Command.getCommand(localizedGatewayIP);
							
							if (!CommonTools.isLocal(localizedGatewayIP))
							{
								/* Get the ssh key. TODO: handle case without sshkey (eg. if user/password provided) */
								// return manager can not be null
								Instance manager = Utilities.getManagerByIP(CommonTools.getLocalHostname(), CommonTools.getLocalAddress(),  topology.getInstancesByType(E3Constant.E3MANAGER), logger);
								SSHKey key = manager.getSSHKey();
								if (key != null) {
									if(logger.isDebugEnabled()) {
										logger.debug("using key: " + key.getName());
									}
								}
								
								/* Connect via ssh. */
								SSHCommand sshCommand = (SSHCommand) cmd;
								sshCommand.connect(key, localizedGatewayIP, 22, instance.getUser(), instance.getPassword());
								
								if (!sshCommand.isConnected())
								{
									String errorSshMsg = "Error: ssh connection to " + localizedGatewayIP+  " failed (sshkey=";
									errorSshMsg += (key == null) ? "not defined " : key.getName();
									errorSshMsg += "user=" + instance.getUser() + ")";
									throw new InstallerDeployException(errorSshMsg);
								}
								
								if(logger.isDebugEnabled()) {
									logger.debug("command type: " + cmd.getImplementationType());
								}
								
								/* check if the destination directory already exists. */
								ShellCommandResult dirExistResult = cmd.execShellCommand("ls "+ config.getRemotePath());
								if (dirExistResult.getExitStatus() != 0)
								{
									/* Create the destination directory */
									ShellCommandResult dirCreateResult = cmd.execShellCommand("mkdir -p -m 755 "+ config.getRemotePath());
									if (dirCreateResult.getExitStatus() != 0)
									{
										throw new InstallerDeployException("Unable to create remote destination directory "+ config.getRemotePath() + ".");
									}
								
									/* Remote copy the package. */
									if(logger.isDebugEnabled()) {
										logger.debug("package url: " + config.getPackageUrl());
									}
									URL urlPackage = new URL(config.getPackageUrl());
									String strFilename;
									if (urlPackage.getProtocol().equals("file")) 
									{
										strFilename = new File(urlPackage.getFile()).getName();
										cmd.copy(urlPackage.getFile(), config.getRemotePath() + "/" + strFilename);
										
									} else {
										/* TODO: handle HTTP package URL ? */
										if(logger.isDebugEnabled()) {
											logger.debug("URL type " + urlPackage.getProtocol() + " is not supported yet.");
										}
										continue;
									}
								
									/* Unzip TODO: instaler filename and install location in config */
									ShellCommandResult cmdRes = cmd.execShellCommand("tar xfz " + strFilename, config.getRemotePath());
									if (cmdRes.getExitStatus() != 0)
									{
										/* unzip has failed, display output. TODO: handle failure */
										throw new InstallerDeployException("Unzip archive" + strFilename + " failed (returned code: "+ cmdRes + ")");
									}
								}
								
								String fullyQualifiedInstallerCmd = replaceManagerIPPattern(config.getInstallerCmd(), managerIP);
								if(logger.isDebugEnabled()) {
									logger.debug("Executing shell command '" + fullyQualifiedInstallerCmd + "'");
								}
								
								/* Launch Install. */
								ShellCommandResult cmdResInstallation = cmd.execShellCommand(fullyQualifiedInstallerCmd, config.getRemotePath());
								if (cmdResInstallation.getExitStatus() != 0)
								{
									/* remote installation has failed, display output. */
									throw new InstallerDeployException("Installation has failed while executing command [" + fullyQualifiedInstallerCmd + "]\ndetails:"+ cmdResInstallation);
								}
									
								/* Launch sanity check. */
								ShellCommandResult cmdResSanityCheck = cmd.execShellCommand(config.getSanityCheckCmd(), config.getRemotePath());
								if (cmdResSanityCheck.getExitStatus() != 0)
								{
									/* remote installation has failed, display output. TODO: handle failure */
									throw new InstallerDeployException("Sanity check has failed  while executing command ["+ config.getSanityCheckCmd() + "] in the folder [" + config.getRemotePath() + "]\nDetails:"+ cmdResSanityCheck);
								}
							}
							else { // Local 
								//if ("E3Gateway".equals(instance.getType())) {
								
								String generateNatureCmd = config.getGenerateNatureCmd();
								if (generateNatureCmd != null && !generateNatureCmd.isEmpty())
								{
								
									String fullyQualifiedGenerateNatureCmd = replaceManagerIPPattern(generateNatureCmd, managerIP);
									if(logger.isDebugEnabled()) {
										logger.debug("Executing shell command '" + fullyQualifiedGenerateNatureCmd + "'");
									}
									
									/* Get the ssh key. TODO: handle case without sshkey (eg. if user/password provided) */
									SSHKey key = instance.getSSHKey();
									if (key != null) {
										if(logger.isDebugEnabled()) {
											logger.debug("using key: " + key.getName());
										}
									}
									/* Connect via ssh. */
									SSHCommand sshCommand = (SSHCommand) cmd;
									sshCommand.connect(key, localizedGatewayIP, 22, instance.getUser(), instance.getPassword());
									
									if (!sshCommand.isConnected())
									{
										String errorSshMsg = "Error: ssh connection to" + localizedGatewayIP+  " failed (sshkey=";
										errorSshMsg += (key == null) ? "not defined " : key.getName();
										errorSshMsg += "user=" + instance.getUser() + ")";
										throw new InstallerDeployException(errorSshMsg);
									}
									
									// And we need to update the configuration.properties file directly
									ShellCommandResult cmdGenerateNature = cmd.execShellCommand(fullyQualifiedGenerateNatureCmd, config.getRemotePath());
									if (cmdGenerateNature.getExitStatus() != 0)
									{
										
										/* Update nature failed */
										throw new InstallerDeployException("Update nature has failed while executing command ["+ fullyQualifiedGenerateNatureCmd + "] in the folder [" + config.getRemotePath() + "]\nDetails:"+ cmdGenerateNature);
									}
									
									/* reload context. */
									springContextBoostrapper.reloadNatureProperties();
								}
							}
						}
					} else {
						if(logger.isDebugEnabled()) {
							logger.debug("Nothing to install for type " + instance.getType());
						}
					}
				
				}
				catch (Exception e)
				{
					if(logger.isErrorEnabled()) {
						logger.error("Exception: " + e.getMessage() + "\n" + Utilities.getStackTrace(e));
					}
					e.printStackTrace();
					if(logger.isDebugEnabled()) {
						logger.debug("Install for type " + instance.getType() + " has failed. (" + e.getMessage() + ")");
					}
					throw e;
				}
			
			}
		}		
	}
	
	/**
	 * Method: replaceManagerIPPattern
	 * Replaces the pattern for manager IP in template-installer-config with the computed manager ip
	 * Aug 7, 2012 - 11:52:01 AM
	 * @param originalCmd the original command line retrieved from the config file
	 * @param managerIP the manager ip replacement
	 * @return String the command line after replacement
	 */
	private String replaceManagerIPPattern(String originalCmd, String managerIP) {
		return originalCmd.replace(E3Constant.MANAGER_IP_REPLACE_PATTERN, managerIP);
	}

	
	
	/**
	 * Method getManagerAreaByIP
	 * @param managerHostName the manager's host name
	 * @param managerIP the manager's ip adress
	 * @return String the area of that ip if existing
	 * @throws NonExistingManagerException thrown if parameter is no valid ip
	 */
	private String getManagerAreaByIP(String managerHostName, String managerIP) throws NonExistingManagerException {
		// getManagerByIP(..) either return the manager or throw an exception if not found => can not have a NPE
		return Utilities.getManagerByIP(managerHostName, managerIP,  topology.getInstancesByType(E3Constant.E3MANAGER), logger).getArea();
	}
	
	/**
	 * Method getLocalizedGatewayIP
	 * @param instance the gateway instance
	 * @param managerArea the manager's area
	 * @return String the localized ip to use for that gateway
	 */
	private String getLocalizedGatewayIP(Instance instance, String managerArea) {
		String localizedGatewayIP = null;
		if (managerArea.equals(instance.getArea())) { //gateway has same area than the manager -> use internal ip
			if(logger.isDebugEnabled()) {
				logger.debug("Instance '" + instance.getName() + "' uses same area than the manager; using internal IP");
			}
			localizedGatewayIP = instance.getInternalIP();
		} else { //gateway has different area than the manager -> use external ip
			if(logger.isDebugEnabled()) {
				logger.debug("Instance '" + instance.getName() + "' uses different area than the manager; using external IP");
			}
			localizedGatewayIP = instance.getExternalIP();
		}
		
		return localizedGatewayIP;
	}
	
}
