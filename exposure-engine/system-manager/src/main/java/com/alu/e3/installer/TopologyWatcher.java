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

import org.apache.camel.Exchange;

import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.osgi.api.ITopology;

public class TopologyWatcher
{
	private static final CategoryLogger logger = CategoryLoggerFactory.getLogger(TopologyWatcher.class, Category.SYS);
	
	private ITopology topology;
	boolean topologyProcessed = false;
	boolean systemTopologyProcessed = false;

	public void setTopology(ITopology topology) {
		this.topology = topology;
	}
	
	public TopologyWatcher() {}
	
	public void processTopology(Exchange exchange) throws Exception 
	{
		logger.debug("New topology.xml file detected");
		
		if(topologyProcessed) {
			logger.debug("Topology.xml already processed. Ignoring.");
			return;
		}
		
		topologyProcessed = true;
		
		if (topology != null)
		{
			logger.debug("Setting new topology.xml file on topology service.");
			topology.setTopology("null");
		}
		else
			logger.warn("Topology file is null. Ignoring.");
	}

	public void processSystemTopology(Exchange exchange) throws Exception 
	{
		logger.debug("New system-topology.xml file detected");
		
		if(systemTopologyProcessed) {
			logger.debug("system-topology.xml already processed. Ignoring.");
			return;
		}
		
		systemTopologyProcessed = true;
		
		if (topology != null)
		{
			logger.debug("Setting new system-topology.xml file on topology service.");
			topology.setSystemTopology("null");
		}
		else
			logger.warn("system-topology file is null. Ignoring.");
	}

	/**
	 * General Setters
	 */
	public void setTopologyProcessed(boolean topologyProcessed) {
		this.topologyProcessed = topologyProcessed;
	}

	public void setSystemTopologyProcessed(boolean systemTopologyProcessed) {
		this.systemTopologyProcessed = systemTopologyProcessed;
	}

}
