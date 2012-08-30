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
package com.alu.e3.gateway.loadbalancer;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.http4.HttpComponent;
import org.apache.camel.component.http4.HttpEndpoint;
import org.apache.camel.component.http4.HttpProducer;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.service.importer.OsgiServiceLifecycleListener;

import com.alu.e3.common.osgi.api.ITopologyClient;
import com.alu.e3.data.model.sub.ConnectionParameters;
import com.alu.e3.data.model.sub.TargetHost;
import com.alu.e3.gateway.common.camel.exception.GatewayException;
import com.alu.e3.gateway.common.camel.exception.GatewayExceptionCode;

public class HttpLoadBalancerProcessor implements Processor, OsgiServiceLifecycleListener {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpLoadBalancerProcessor.class);
	
	HttpEndpoint httpEndpoint;
	
	private String area;
	private boolean failedOver;
	private int failedOverErrorCode;
	private ITargetHostManager targetHostManager;
	private String apiId;
	private String contextId;
	
	private boolean isProvisioned;
	
	private int maxTotalConnection;
	private static final int DEFAULT_CONNECTIONS_PER_ROUTE = 400;
	private static final int DEFAULT_MAX_CONNECTIONS = 2000;
	
	private ReentrantLock lbProvisioningLock;
	private ReentrantLock endpointLock;
	
	// Primary: load balance only on targets of the same site
	private RoundrobinLoadBalancer primaryLoadBalancer; 
	
	// Alternative: load balance on targets of different sites or without site
	private RoundrobinLoadBalancer alternativeLoadBalancer;

	private final static String AREA_UNKNOWN = "unknown";
	private final static String AREA_ALTERNATIVE = "external";
		
	public HttpLoadBalancerProcessor() {
		this.area = AREA_UNKNOWN;
		this.isProvisioned = false;
		lbProvisioningLock = new ReentrantLock();
		endpointLock = new ReentrantLock();
	}
	
	public void setApiId(String apiId) {
		this.apiId = apiId;
	}
	
	public void setContextId(String contextId) {
		this.contextId = contextId;
	}
	
	public void setTopologyClient(ITopologyClient topologyClient) {
		area = topologyClient.getMyArea();
	}
	
	public void setTargetHostManager(ITargetHostManager targetHostManager) {
		this.targetHostManager = targetHostManager;		
	}	
	
	public void setFailedOver(boolean failedOver) {
		this.failedOver = failedOver;
	}
	
	public void setFailedOverErrorCode(int failedOverErrorCode) {
		this.failedOverErrorCode = failedOverErrorCode;
	}
	
	public RoundrobinLoadBalancer getPrimaryLoadBalancer() {
		if(primaryLoadBalancer == null) {
			LOGGER.debug("Adding new primary load balancer for area '" +area+ "' failover '" +failedOver+ "' and fail over code '" +failedOverErrorCode+ "'");
			primaryLoadBalancer = new RoundrobinLoadBalancer(area, failedOver, failedOverErrorCode, targetHostManager);
		}
		
		return primaryLoadBalancer;
	}
	

	public RoundrobinLoadBalancer getAlternativeLoadBalancer() {
		if(alternativeLoadBalancer == null) {
			LOGGER.debug("Adding new alternative load balancer for area '" +AREA_ALTERNATIVE+ "' failover '" +failedOver+ "' and fail over code '" +failedOverErrorCode+ "'");
			alternativeLoadBalancer = new RoundrobinLoadBalancer(AREA_ALTERNATIVE, failedOver, failedOverErrorCode, targetHostManager);
		}
		
		return alternativeLoadBalancer;
	}
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		boolean success = false;
	
		// Provision load balancer with the list of targets
		provisionLoadBalancer();
		
		// Initialize endpoint
		createHttpEndpoint(exchange);
		
		// First, try with primary targets if we have some
		if(this.primaryLoadBalancer != null) {
			LOGGER.debug("Processing load balancing on primary load balancer with apiId {}", apiId);
			success = this.primaryLoadBalancer.process(exchange);
		}
		
		// Now try on alternative targets
		if(!success && this.alternativeLoadBalancer != null) {
			LOGGER.debug("Processing load balancing on alternate load balancer with apiId {}", apiId);
			success = this.alternativeLoadBalancer.process(exchange);
		}
		
		if(!success) {
			GatewayException exception = new GatewayException(GatewayExceptionCode.LOAD_BALANCER, "Issue to call the target host");
			throw exception;
		}
	}
	
	private void createHttpEndpoint(Exchange exchange) throws Exception {
		if(httpEndpoint == null) {
		
			LOGGER.debug(">>>>>>>>>>>>> Creating HTTP Endpoint");
			
			initHttpEndpoint(exchange);
			
			LOGGER.debug("Creating new HTTP producer");
			HttpProducer httpProducer = (HttpProducer) httpEndpoint.createProducer();
			
			setDefaultHttpConnectionParams(httpProducer);
			
			if(this.primaryLoadBalancer != null) {
				LOGGER.debug("Setting HTTP producer on primary load balancer");
				this.primaryLoadBalancer.setHttpProducer(httpProducer);
			}
			if(alternativeLoadBalancer != null) {
				LOGGER.debug("Setting HTTP producer on alternate load balancer");
				this.alternativeLoadBalancer.setHttpProducer(httpProducer);
			}
		
		}
	}
	
	void initHttpEndpoint(Exchange exchange) throws Exception {
		endpointLock.lock();
		try {
			if(httpEndpoint == null) {
				LOGGER.debug("Initializing a new HttpEndpoint ...");
				CamelContext context = exchange.getContext();
				HttpComponent httpComponent = context.getComponent("http4", HttpComponent.class);
	
				if(maxTotalConnection < 1) {
					LOGGER.warn("Unable to determine the number of HTTP connections needed to support the API {}", apiId);
					maxTotalConnection = DEFAULT_MAX_CONNECTIONS;
					LOGGER.warn("The default number of HTTP connections to support the API {} is set to {}", apiId, maxTotalConnection);
				} 
				
				/*
				String endpointParameters = "?throwExceptionOnFailure=false&maxTotalConnections=" + maxTotalConnection + "&connectionsPerRoute="
						+ DEFAULT_CONNECTIONS_PER_ROUTE 
						+ "&httpClient.handleRedirects=false";
				
				String uriHttpEndpoint = "http4://host" + endpointParameters;
				*/
				
				String endpointParameters = "?throwExceptionOnFailure=false&httpClient.handleRedirects=false";
				String uriHttpEndpoint = "http4://host" + endpointParameters;
				
				LOGGER.debug("Init the HTTP EndPoint for API {} with [{}]", apiId, uriHttpEndpoint);
				
				httpEndpoint = (HttpEndpoint) httpComponent.createEndpoint(uriHttpEndpoint);
				// remove the parameters from the target URI
				httpEndpoint.setHttpUri(new URI("http4://host"));
			} else {
				LOGGER.debug("HTTP endpoint already referenced); doing nothing");
			}
		} finally {
			endpointLock.unlock();
		}
		
	}

	/**
	 * Provisions the LoadBalancer with TargetHosts returned by TargethostManager.
	 */
	protected void provisionLoadBalancer() {
		
		if(!isProvisioned) { 
			// Locking...
			lbProvisioningLock.lock();
			
			try {
				
				if(!isProvisioned) {
					
					if(apiId != null) {
						LOGGER.debug("Initialiazing LoadBalancer for API {} / Context {}", apiId, contextId);
				
						// Get the list of TargetReference for this ApiContext
						List<TargetReference> targets = targetHostManager.getTargetReferences(apiId, contextId);
						if(targets != null) {
							LOGGER.debug("Got {} references from TargetTostManager", targets.size());
							
							if(targets.size() > 0) {
								// Dispatching targets on load balancers depending on their site								
								for(TargetReference target : targets) {
									
									computeMaxTotalConnection(target);
									
									// ManagedTargethost is on same site has gateway's site, adding target to primaryLoadBalancer
									if(target.getTargetHost().getSite() != null && target.getTargetHost().getSite().equals(area)) {
										getPrimaryLoadBalancer().addTargetHostReference(target);
										LOGGER.debug("Added TargetTost {} to primary LB", target.getReference());
										
									// ManagedTargethost is on a different site than the gateway (or has no site), adding target to alternativeLoadBalancer
									} else {
										getAlternativeLoadBalancer().addTargetHostReference(target);
										LOGGER.debug("Added TargetTost {} to alternative LB", target.getReference());
									}
								}
								isProvisioned = true;
							} else {
								LOGGER.error("Recovered empty target endpoint list for API {} / Context {}", apiId, contextId);
							}
						} else {
							LOGGER.warn("Returned null for API {} / Context {}", apiId, contextId);
						} // End of Targets != null
						
					} else {
						LOGGER.error("ApiId is null on provisionLoadBalancer");
						
					} // End of apiId != null
					
				} // End of !isProvisioned
				else {
					LOGGER.debug("Load balancer already provisionned for api {}", apiId);
				}
			} finally {
				// Unlocking
				lbProvisioningLock.unlock();
			}
		}
		
	}
	

	private void computeMaxTotalConnection(TargetReference targetReference) {
		
		Integer connectionsPerRoute = DEFAULT_CONNECTIONS_PER_ROUTE;
		
		TargetHost targetHost = targetReference.getTargetHost();
		ConnectionParameters connectionParameters = targetHost.getConnectionParameters();
		
		if(connectionParameters != null) {
			Integer connectionsPerRouteFound = connectionParameters.getMaxConnections();
			
			if(connectionsPerRouteFound != null) {
				connectionsPerRoute = connectionsPerRouteFound;
			} else {
				LOGGER.debug("No HTTP connectionsPerRoute for the target host {}", targetReference.getTargetHost().getUrl());
				LOGGER.debug("Set the default HTTP connectionsPerRoute for the target host {}", targetReference.getTargetHost().getUrl());
			}
			
		} else {
			LOGGER.debug("No HTTP connectinoParameters for the target host {}",  targetReference.getTargetHost().getUrl());
		}
		
		maxTotalConnection = maxTotalConnection + connectionsPerRoute;		
		
		LOGGER.debug("The HTTP connectionsPerRoute is {} for the target host {}", connectionsPerRoute, targetReference.getTargetHost().getUrl());
		LOGGER.debug("Remaning maxTotalConnection is {} for API {}", maxTotalConnection, apiId);
	}
	
	@Override
	public void bind(Object service, Map properties) throws Exception {
		// Nothing to do on bind service
	}
	
	/**
	 * Call on osgi sslJetty component gone.
	 */
	@Override
	public void unbind(Object service, Map properties) throws Exception {
		LOGGER.debug("Cleaning HttpEndpoint ...");
		httpEndpoint=null;
	}
	
	private void setDefaultHttpConnectionParams(HttpProducer httpProducer) {
		
		DefaultHttpClient client = (DefaultHttpClient) httpProducer.getHttpClient();
			
		ClientConnectionManager ccm = client.getConnectionManager();
		if(ccm instanceof ThreadSafeClientConnManager){
			ThreadSafeClientConnManager tsccm = (ThreadSafeClientConnManager) ccm;
			tsccm.setDefaultMaxPerRoute(DEFAULT_CONNECTIONS_PER_ROUTE);
			tsccm.setMaxTotal(this.maxTotalConnection);
		}
		
	}
}
