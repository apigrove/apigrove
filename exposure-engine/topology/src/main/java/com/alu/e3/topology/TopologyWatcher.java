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
package com.alu.e3.topology;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alu.e3.common.E3Constant;
import com.alu.e3.common.osgi.api.IHealthCheckFactory;
import com.alu.e3.common.osgi.api.ITopologyClient;
import com.alu.e3.data.IHealthCheckService;
import com.alu.e3.data.model.Instance;
import com.alu.e3.data.topology.IInstanceListener;
import com.alu.e3.data.topology.InstanceEvent;

/**
 * Keeps track of the topology using health check mechanism and update the TopologyClient accordingly.
 */
public class TopologyWatcher implements Runnable, IInstanceListener {
	
	private static final Logger logger = LoggerFactory.getLogger(TopologyWatcher.class);
	
	private ITopologyClient topologyClient;
	private IHealthCheckFactory healthCheckFactory;
		
	private List<Instance> upGateways = new LinkedList<Instance>();
	private List<Instance> upGatewaysActive = new LinkedList<Instance>();
	private List<Instance> downGateways = new LinkedList<Instance>();

	private List<Instance> upSpeakers = new LinkedList<Instance>();
	private List<Instance> upSpeakersActive = new LinkedList<Instance>();
	
	private int pollingInterval = E3Constant.HEALTH_CHECK_POLLING_INTERVAL;
	
	private volatile boolean running;

	private Thread localThread;
	
	public TopologyWatcher() {}
	
	/**
	 * Sets the polling interval.
	 */
	public void setPollingInterval(int pollingInterval) {
		this.pollingInterval = pollingInterval;
	}
	
	/**
	 * Sets the healthCheckFactory object
	 */
	public void setHealthCheckFactory(IHealthCheckFactory healthCheckFactory) {
		this.healthCheckFactory = healthCheckFactory;
	}
	
	/**
	 * Set the topologyClient object
	 */	
	public void setTopologyClient(ITopologyClient topologyClient) {
		this.topologyClient = topologyClient;
	}
	
	/**
	 * Starts watching of the nodes
	 */
	public void init() {
		topologyClient.addInstanceListener(this);
		
		// get the list of gateways + speaker to monitor
		List<Instance> nodeList;

		synchronized (this) {
			nodeList = topologyClient.getAllInstancesOfType(E3Constant.E3GATEWAY);
			if (nodeList != null) {
				upGateways.addAll(nodeList);
			}

			nodeList = topologyClient.getAllInstancesOfType(E3Constant.E3GATEWAY_ACTIVE);
			if (nodeList != null) {
				upGatewaysActive.addAll(nodeList);
			}

			nodeList = topologyClient.getAllInstancesOfType(E3Constant.E3SPEAKER);
			if (nodeList != null) {
				upSpeakers.addAll(nodeList);
			}

			nodeList = topologyClient.getAllInstancesOfType(E3Constant.E3SPEAKER_ACTIVE);
			if (nodeList != null) {
				upSpeakersActive.addAll(nodeList);
			}
		}
		
		// start the watching of nodes
		setRunning(true);
		
		// TODO what is doing the passive manager? watching only the active manager? how do we know active or passive?		
		localThread = new Thread(this);
		localThread.start();
	}
	
	/**
	 * Stops watching the nodes
	 */
	public void destroy() {
		logger.debug("Stopping TopologyWatcher ...");
		setRunning(false);

		logger.debug("Clearing TopologyWatcher list ...");
		synchronized (this) {
			upGateways.clear();
			upGatewaysActive.clear();
			upSpeakers.clear();
			upSpeakersActive.clear();
		}

		logger.debug("Clearing TopologyWatcher listeners ...");
		topologyClient.removeInstanceListener(this);

		logger.debug("TopologyWatcher waiting for its local thread (join) ...");
		try {
			localThread.join();
		} catch (InterruptedException e) {
			logger.error("Unable to join TopologyWatcher local thread", e);
		}
		logger.debug("TopologyWatcher destroyed.");
	}

	/**
	 * Runnable implementation
	 */
	@Override
	public void run() {
		do {
			try {
				synchronized (this) {
					// check the active gateways which are up
					// if an active gateways is down, it is removed from the list of E3GATEWAY_ACTIVE in the TopologyClient
					checkUpNodes(upGatewaysActive, null, E3Constant.E3GATEWAY_ACTIVE);
					
					// check the gateways which are up
					// if a gateway is down, it is removed from the list of E3GATEWAY in the TopologyClient
					checkUpNodes(upGateways, downGateways, E3Constant.E3GATEWAY);
					
					// check the active speakers which are up
					// if an active speaker is down, it is removed from the list of E3SPEAKER_ACTIVE in the TopologyClient
					checkUpNodes(upSpeakersActive, null, E3Constant.E3SPEAKER_ACTIVE);
					
					// check the gateways which are down
					// if a gateway becomes up, it is added to the list of E3GATEWAY in the TopologyClient
					checkDownNodes(upGateways, downGateways, E3Constant.E3GATEWAY);
					
					// check the gateways that are becoming active
					// if a gateway becomes active, it is added to the list of E3GATEWAY_ACTIVE in the TopologyClient
					checkForActiveGateways();
					
					// check if there's a new speaker
					// if a speaker becomes active, it is added to the list of E3SPEAKER_ACTIVE in the TopologyClient
					checkForNewSpeakers();
				}
				Thread.sleep(pollingInterval);
			} catch (InterruptedException e) {
				logger.warn("TopologyWatcher thread interrupted", e);
				running = false;
			}
		} while (running);
	}
	
	/**
	 * Return the correct IP depending on the area
	 */
	private String getIPWithArea(Instance node) {
		String myArea = topologyClient.getMyArea();
		
		if (myArea == null || myArea.equals(node.getArea())) {
			return node.getInternalIP();
		}
		else {
			return node.getExternalIP();
		}		
	}
	
	/**
	 * Check if a gateway is becoming active
	 */
	private void checkForActiveGateways() {
		Instance[] nodes = new Instance[0];
		
		synchronized (this) {
			nodes = upGateways.toArray(nodes);
		}
		
		for (int i = 0; i < nodes.length; i++) {
			Instance node = nodes[i];
			
			if (!upGatewaysActive.contains(node) && checkIfUp(getIPWithArea(node), E3Constant.E3GATEWAY_ACTIVE)) {
				upGatewaysActive.add(node); // keep the node
				Instance newNode = new Instance(node);
				newNode.setType(E3Constant.E3GATEWAY_ACTIVE);
				topologyClient.addInstance(newNode);
			}
		}
	}
	
	/**
	 * Check if a new speaker is up
	 */
	private void checkForNewSpeakers() {
		// iterate over the speakers and check if the Speaker heart beat is up 
		Instance[] nodes = new Instance[0];
		
		nodes = upSpeakers.toArray(nodes);
		
		for (int i = 0; i < nodes.length; i++) {
			Instance node = nodes[i];
			if (!upSpeakersActive.contains(node) && checkIfUp(getIPWithArea(node), E3Constant.E3SPEAKER_ACTIVE)) {
				upSpeakersActive.add(node); // keep the node
				Instance newNode = new Instance(node);
				newNode.setType(E3Constant.E3SPEAKER_ACTIVE);
				topologyClient.addInstance(newNode);
			}
		}
	}
	
	/**
	 * Check if the upNodes are still up
	 */
	private void checkUpNodes(List<Instance> upNodes, List<Instance> downNodes, String type) {
		Instance[] nodes = new Instance[0];
		
		nodes = upNodes.toArray(nodes);
		
		for (int i = 0; i < nodes.length; i++) {
			Instance node = nodes[i];
			if (!checkIfUp(getIPWithArea(node), type)) {

				upNodes.remove(node);
				
				if (downNodes != null) {
					downNodes.add(node);
				}
				
				Instance newNode = new Instance(node);
				newNode.setType(type);
				topologyClient.deleteInstance(newNode);
			}
		}
	}
	
	/**
	 * Check if the downNodes are still down
	 */
	private void checkDownNodes(List<Instance> upNodes, List<Instance> downNodes, String type) {
		Instance[] nodes = new Instance[0];
		
		nodes = downNodes.toArray(nodes);
		
		for (int i = 0; i < nodes.length; i++) {
			Instance node = nodes[i];
			if (checkIfUp(getIPWithArea(node), type)) {

				downNodes.remove(node);
				upNodes.add(node);
				
				topologyClient.addInstance(node);
			}
		}
	}
	
	/**
	 * Check if the node is up
	 */
	private boolean checkIfUp(String node, String type) {
		
		if (healthCheckFactory == null) {
			// TODO log error
			return false;
		}
		
		String instanceType;
		
		if (E3Constant.E3GATEWAY.equals(type)) {
			instanceType = IHealthCheckFactory.GATEWAY_INTERNAL_TYPE;
		}
		else if (E3Constant.E3GATEWAY_ACTIVE.equals(type)) {
			instanceType = IHealthCheckFactory.GATEWAY_TYPE;
		}
		else if (E3Constant.E3SPEAKER_ACTIVE.equals(type)) {
			instanceType = IHealthCheckFactory.SPEAKER_TYPE;
		}
		else {
			// TODO log error
			return false;
		}
		
		IHealthCheckService healthCheckService;
		
		healthCheckService = healthCheckFactory.getHealthCheckService(instanceType);
		
		return healthCheckService.check(node);
	}

	private boolean checkIfExist(Instance instance, List<Instance> list)
	{
		for (Instance i : list)
		{
			if (i.getName().equals(instance.getName()) )
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * IInstanceListener implementation
	 */
	@Override
	public synchronized void instanceAdded(InstanceEvent event) {
		if (E3Constant.E3GATEWAY.equals(event.getType())) {
			if (!checkIfExist(event.getInstance(), upGateways))
			{
				upGateways.add(event.getInstance());
			}
			logger.debug("E3GATEWAY added: {}", event.getInstance());
		}
		
		else if (E3Constant.E3SPEAKER.equals(event.getType())) {
			if (!checkIfExist(event.getInstance(), upSpeakers))
			{
				upSpeakers.add(event.getInstance());
			}
			logger.debug("E3SPEAKER added: {}", event.getInstance());
		}
		
		else if (E3Constant.E3GATEWAY_ACTIVE.equals(event.getType())) {
			logger.debug("E3GATEWAY_ACTIVE added: {}", event.getInstance());
		}
		else if (E3Constant.E3SPEAKER_ACTIVE.equals(event.getType())) {
			logger.debug("E3SPEAKER_ACTIVE added: {}", event.getInstance());
		}
	}

	/**
	 * IInstanceListener implementation
	 */
	@Override
	public synchronized void instanceRemoved(InstanceEvent event) {
		if (E3Constant.E3GATEWAY.equals(event.getType())) {
			upGateways.remove(event.getInstance());
			logger.debug("E3GATEWAY removed: {}", event.getInstance());
		}
		else if (E3Constant.E3SPEAKER.equals(event.getType())) {
			upSpeakers.remove(event.getInstance());
			logger.debug("E3SPEAKER removed: {}", event.getInstance());
		}
		else if (E3Constant.E3GATEWAY_ACTIVE.equals(event.getType())) {
			logger.debug("E3GATEWAY_ACTIVE removed: {}", event.getInstance());
		}
		else if (E3Constant.E3SPEAKER_ACTIVE.equals(event.getType())) {
			logger.debug("E3SPEAKER_ACTIVE removed: {}", event.getInstance());
		}
	}

	/**
	 * General Setters
	 */
	public void setUpGateways(List<Instance> upGateways) {
		synchronized (this) {
			this.upGateways = upGateways;
		}
	}

	public void setUpGatewaysActive(List<Instance> upGatewaysActive) {
		this.upGatewaysActive = upGatewaysActive;
	}

	public void setDownGateways(List<Instance> downGateways) {
		this.downGateways = downGateways;
	}

	public void setUpSpeakers(List<Instance> upSpeakers) {
		synchronized (this) {
			this.upSpeakers = upSpeakers;
		}
	}

	public void setUpSpeakersActive(List<Instance> upSpeakersActive) {
		this.upSpeakersActive = upSpeakersActive;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
}
