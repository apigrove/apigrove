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
package com.alu.e3.data.topology;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alu.e3.common.E3Constant;
import com.alu.e3.common.caching.IEntryListener;
import com.alu.e3.common.osgi.api.ITopologyClient;
import com.alu.e3.data.DataEntryEvent;
import com.alu.e3.data.model.Instance;
import com.alu.e3.data.model.SSHKey;

public class TDRCollectorInstanceWatcher implements IEntryListener<String, ArrayList<Instance>> {
	public static final String HOST_CFG = "ra_server";
	public static final String KEY_CFG = "ssh_key";
	public static final String USER_CFG = "ssh_key_user";
	public static final String ID_CFG = "e3_id";
	public static final String CONFIG_KEYVAL_CHAR = ":";
	
	private static final Logger logger = 
		LoggerFactory.getLogger(TDRCollectorInstanceWatcher.class);
	
	private ITopologyClient topologyClient;
	private List<Instance> TDRCollectors = new LinkedList<Instance>();
	
	private SSHKey key;
	private String user;
	private String myName = null;

	protected String writeLocation = E3Constant.TDR_TRANSFER_CONFIG_PATH;
	protected String configName = E3Constant.TDR_TRANSFER_CONFIG_FILE;
	protected String keyName = E3Constant.TDR_TRANSFER_CONFIG_KEY;
	
//	@Override
//	public void instanceAdded(InstanceEvent event) {
//		Instance instance = event.getInstance();
//		onInstanceAdded(instance);
//	}

	private void onInstanceAdded(Instance instance) {
		if (myName == null && E3Constant.E3GATEWAY.equals(instance.getType())) {
			getMyName();
			if (myName != null) {
				writeConfigFile();
			}
		} else if (E3Constant.E3MANAGER.equals(instance.getType())) {
			verifyCredentials(instance);
		} else if (E3Constant.TDR_COLLECTOR.equals(instance.getType())) {
			logger.debug("Adding instance " + instance.getName() + " (" + instance.getInternalIP() + ")");
			TDRCollectors.add(instance);
			writeConfigFile();
		}
	}

	//if more instances are registered with the same InternalIP, only the first is removed
//	@Override
//	public void instanceRemoved(InstanceEvent event) {
//		Instance removedInstance = event.getInstance();
//		onInstanceRemoved(removedInstance);
//	}

	private void onInstanceRemoved(Instance removedInstance) {
		if (!E3Constant.TDR_COLLECTOR.equals(removedInstance.getType())) return;
		Instance toRemove = null;
		for (Instance currentInstance : TDRCollectors) {
			if (currentInstance.getInternalIP().equals(removedInstance.getInternalIP())) {
				toRemove = currentInstance;
			}
		}
		if (toRemove != null) {
			logger.debug("Removing instance " + removedInstance.getName() + 
					" (" + removedInstance.getInternalIP() + ")");
			TDRCollectors.remove(toRemove);
			writeConfigFile();
		} else {
			logger.warn("Tried to remove unknown instance " + removedInstance.getName() + 
					" (" + removedInstance.getInternalIP() + ")");
		}
	}
	
	protected void getMyName() {
		Instance inst = topologyClient.whoAmI(E3Constant.E3GATEWAY);
		if (inst != null) {
			myName = inst.getName();
			logger.debug("I found my name: " + myName);
		}
	}
	
	//records: where the key is, what the user is, what the TDRCollectors are, and what the ID is
	protected void writeConfigFile() {
		logger.debug("Writing new TDR Transfer Config File");
		if (keyName == null || user == null || TDRCollectors.isEmpty()) {
			logger.debug("Missing required data for writing TDR Transfer Config File");
			return;
		}
		if (myName == null) getMyName();
		Writer writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(
					new File(new File(writeLocation), configName)));
			if (myName != null) writer.write(ID_CFG + CONFIG_KEYVAL_CHAR + myName + "\n");
			writer.write(KEY_CFG + CONFIG_KEYVAL_CHAR + writeLocation + "/" + keyName + "\n");
			writer.write(USER_CFG + CONFIG_KEYVAL_CHAR + user + "\n");
			for (Instance instance : TDRCollectors) {
				writer.write(HOST_CFG + CONFIG_KEYVAL_CHAR + instance.getInternalIP() + "\n");
				
			}
		} catch (IOException e) {
			logger.error("Could not write config file for TDR Transfer Script!");
			logger.error(e.getMessage(), e.getStackTrace());
			return;
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					logger.error("Could not close TDR Transfer Key writer.");
					e.printStackTrace();
					return;
				}
			}
		}

		logger.debug("TDR Transfer Config File written.");
	}
	
	protected boolean writeKeyFile() {
		logger.debug("Writing new TDR Transfer Key File.");
		File keyfile = new File(new File(writeLocation), keyName);
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(
					keyfile));
			out.write(this.key.getPrivateKey());
		} catch (IOException e) {
			logger.error("Could not write config file for TDR Transfer Script!");
			logger.error(e.getMessage(), e.getStackTrace());
			return false;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logger.error("Could not close TDR Transfer Key writer.");
					e.printStackTrace();
				}
			}
			keyfile.setWritable(false, false);
			keyfile.setReadable(false, false);
			keyfile.setExecutable(false, false);
			keyfile.setWritable(true, true);
			keyfile.setReadable(true, true);
		}
		return true;
	}

	public void init() {
		logger.debug("Starting TDRCollectorInstanceWatcher ...");
		logger.debug("Getting TDRCollectorInstance list ...");
		List<Instance> nodeList;
		
		nodeList = topologyClient.getAllInstancesOfType(E3Constant.E3MANAGER);
		if (nodeList != null && nodeList.size() != 0) {
			for (Instance instance : nodeList) {
				verifyCredentials(instance);
			}
			if (this.key == null || this.user == null) {
				logger.warn("No credentials were set for instances of type " + E3Constant.TDR_COLLECTOR + 
				".  There will likely be problems with downstream TDR collection.  ");
			}
		}
			
		nodeList = topologyClient.getAllInstancesOfType(E3Constant.TDR_COLLECTOR);
		if (nodeList != null && nodeList.size() != 0) {
			TDRCollectors.addAll(nodeList);
			logger.debug("Writing TDR Transfer Script configuration file.");
			writeConfigFile();
		} else {
			logger.debug("No instances of type " + E3Constant.TDR_COLLECTOR + " were found.");
		}
		listenInstanceTypeListener();
		getMyName();
		logger.debug("TDRCollectorInstanceWatcher init'd.");
	}
	
	public void destroy() {
		logger.debug("Stopping TDRCollectorInstanceWatcher ...");
		logger.debug("Clearing TDRCollectorInstance list ...");
		TDRCollectors.clear();
		logger.debug("Clearing TDRCollectorInstanceWatcher listeners ...");
//		topologyClient.removeInstanceListener(this);
		topologyClient.removeInstanceTypeListener(this);
		logger.debug("TDRCollectorInstanceWatcher destroyed.");
	}
	
	// This function creates the key file if one doesn't exist, and monitors
	// whether multiple keys are registered.
	// TDR Transfer Script only supports one key for all destinations instances, so
	// only the first key registered is ever used.  If there are no credentials
	// or different credentials, an error is logged.
	protected boolean verifyCredentials(Instance instance) {
		boolean hasConsistentCredentials = true;
		
		SSHKey instKey = instance.getSSHKey();
		if (instKey != null) {
			if (this.key == null) {
				this.key = instKey;
				this.user = instance.getUser();
				if (!writeKeyFile()) {
					this.key = null;
					this.user = null;
					return false;
				}
				return true;
			}  
			if (!instKey.isSameKey(this.key)) {
				hasConsistentCredentials = false;
				logger.error("Instance " + instance.getName() + " (" + instance.getInternalIP() +
					") uses a different key than another " + E3Constant.TDR_COLLECTOR + " instance!");
			} 
			if (instance.getUser() != this.user) {
				hasConsistentCredentials = false;
				logger.error("Instance " + instance.getName() + " (" + instance.getInternalIP() +
						") has a different user than another " + E3Constant.TDR_COLLECTOR + " instance!");
			}
		} else {
			logger.warn("Instance " + instance.getName() + " (" + instance.getInternalIP() +
					") has no key specified!");
		}
		
		return hasConsistentCredentials;
	}


	public void setWriteLocation(String value) {
		writeLocation = value;
	}
	public String getWriteLocation() {
		return writeLocation;
	}

	public void setConfigName(String value) {
		configName = value;
	}
	public String getConfigName() {
		return configName;
	}

	public void setKeyName(String value) {
		keyName = value;
	}
	public String getKeyName() {
		return keyName;
	}

	public void setTopologyClient(ITopologyClient topologyClient) {
		this.topologyClient = topologyClient;
	}
	
//	public void listenInstanceListener() {
//		topologyClient.addInstanceListener(this);
//	}
	
	public void listenInstanceTypeListener() {
		topologyClient.addInstanceTypeListener(this);
	}
	
	public ITopologyClient getTopologyClient() {
		return topologyClient;		
	}
	
	///
	
	@Override
	public void entryAdded(DataEntryEvent<String, ArrayList<Instance>> event) {
		if (event == null) {
			logger.warn("Event received by the listener cannot be null");
			return;
		}
		if (event.getValue() == null) {
			logger.warn("List of added instances cannot be null");
			return;
		}
		
		for (Instance instance: event.getValue()) {
			onInstanceAdded(instance);
		}
	}

	@Override
	public void entryUpdated(DataEntryEvent<String, ArrayList<Instance>> event) {
		if (event == null) {
			logger.warn("Event received by the listener cannot be null");
			return;
		}
		if (event.getValue() == null) {
			logger.warn("List of updated instances cannot be null");
			return;
		}
		
		for (Instance instance: event.getValue()) {
			onInstanceAdded(instance);
		}	
	}
	
	@Override
	public void entryRemoved(DataEntryEvent<String, ArrayList<Instance>> event) { 
		if (event == null) {
			logger.warn("Event received by the listener cannot be null");
			return;
		}
		if (event.getValue() == null) {
			logger.warn("List of removed instances cannot be null");
			return;
		}
		
		for (Instance instance: event.getValue()) {
			onInstanceRemoved(instance);
		}	
	}
	
}
