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

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.component.http4.HttpProducer;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRouteParamBean;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParamBean;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alu.e3.data.model.sub.ConnectionParameters;
import com.alu.e3.data.model.sub.ForwardProxy;
import com.alu.e3.data.model.sub.TargetHost;
import com.alu.e3.gateway.TargetHost2HttpRouteUtil;
import com.alu.e3.tdr.TDRConstant;
import com.alu.e3.tdr.TDRDataService;

public class RoundrobinLoadBalancer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RoundrobinLoadBalancer.class);
	
	private String name;
	
	private boolean failedOver;
	private int failedOverErrorCode;
	
	HttpProducer httpProducer;
	
	private ITargetHostManager targetHostManager;
	
	private List<TargetReference> targetReferences;
	
	private int globalIndex;
	private int maxAttemptsExceeded;
	private int numberOfReferences;	
	
	public RoundrobinLoadBalancer(String name, boolean failedover, int failedOverErrorCode, ITargetHostManager targetHostManager) {
		this.name = name;
		this.failedOver = failedover;
		this.failedOverErrorCode = failedOverErrorCode;
		this.targetReferences = new ArrayList<TargetReference>();
		this.targetHostManager = targetHostManager;
		this.maxAttemptsExceeded = 1;
		this.globalIndex = 0;
	}
	
	public void addTargetHostReference(TargetReference targetReference) {
		
		LoadBalancerDisplay.logDebug(LOGGER, name, "Add and register the target host " + targetReference);
		
		this.targetReferences.add(targetReference);
		this.numberOfReferences++;
		if(failedOver) {
			maxAttemptsExceeded = numberOfReferences;
		}
	}
	
	public List<TargetReference> getTargetReferences() {
		return targetReferences;
	}
	
	public void setHttpProducer(HttpProducer httpProducer) {
		LOGGER.debug("Setting a new HttpProducer ...");
		this.httpProducer = httpProducer;
		LoadBalancerDisplay.displayConfig(LOGGER, name, maxAttemptsExceeded, failedOver, failedOverErrorCode, numberOfReferences);
	}
	
	
	public boolean process(Exchange exchange) throws Exception {
		
		boolean success = false;
		
		// get current 
		int localIndex = getNextGlobalIndex();
		LoadBalancerDisplay.logDebug(LOGGER, name, "Local index served = "+localIndex);
		
		TargetReference targetReference = null;
				
		for(int attempt = 0; attempt < maxAttemptsExceeded; attempt++) {
			
			LoadBalancerDisplay.logDebug(LOGGER, name,"Try "+ (attempt+1) +"/"+ maxAttemptsExceeded +" to get a valid target host"); 
			
			targetReference = targetReferences.get(localIndex);
			LoadBalancerDisplay.logDebug(LOGGER, name, "Recovered target ref = '"+targetReference.getReference()+ "'. Will check if available");
			
			boolean isAvailable = targetHostManager.isAvailable(targetReference.getReference());
			
			if(isAvailable) {
				LoadBalancerDisplay.logDebug(LOGGER, name, "Target host with reference ["+targetReference.getReference()+"] is available");
				boolean isOk = send(exchange, targetReference);
				if(isOk) {	
					success = isOk;
					LoadBalancerDisplay.logDebug(LOGGER, name, "Succss with a try "+ (attempt+1) +"/"+ maxAttemptsExceeded); 
					break;
				} else {
					LoadBalancerDisplay.logWarn(LOGGER, name, "Unable to call the target host while it is supposed to be available, notify that the target host ["+targetReference.getTargetHost().getUrl()+"] is invalid");
					targetHostManager.notifyFailed(targetReference.getReference());
				}
			} else {
				LoadBalancerDisplay.logDebug(LOGGER, name, "Target host with reference ["+targetReference.getReference()+"] is NOT available");
			}
			++localIndex;
			if(localIndex > numberOfReferences - 1) {
				LoadBalancerDisplay.logDebug(LOGGER, name, "End of reference list; setting index to first element");
				localIndex = 0;
			}			
		}
		
		if(targetReference != null){
			// Put some TDR stuff that we get from HTTP 
			TDRDataService.setTxTDRProperty(TDRConstant.TARGET_URL, targetReference.getTargetHost().getUrl(), exchange);
		}
		
		return success;
	}
	
	
	private synchronized int getNextGlobalIndex() {
		
		LoadBalancerDisplay.logDebug(LOGGER, name, "Global index found = " + globalIndex);
		
		int nextGlobalIndex = globalIndex;
		
		// compute next
		++globalIndex;

		// reset index
		if(globalIndex  > numberOfReferences - 1) {
			globalIndex = 0;
		} 
		
		LoadBalancerDisplay.logDebug(LOGGER, name, "Next global index computed = " + globalIndex);
		
		return nextGlobalIndex;
	}
	
	private boolean send(Exchange exchange, TargetReference targetReference) throws Exception {
		
		boolean isOk = false;
		
		LoadBalancerDisplay.logDebug(LOGGER, name, "Set target host [" + targetReference.getTargetHost().getUrl() + "]");
		
		String url = targetReference.getTargetHost().getUrl();
		String queryParams = null;
		
		int delim = url.indexOf('?');
		
		if (delim != -1) {
			queryParams = url.substring(delim+1, url.length());
			url = url.substring(0, delim);
		}
		
		exchange.getIn().setHeader(Exchange.HTTP_URI, url);
		
		if (queryParams != null) {
			String oldQueryParams = (String)exchange.getIn().getHeader(Exchange.HTTP_QUERY);
			
			if (oldQueryParams != null) {
				queryParams = oldQueryParams + "&" + queryParams;
			}
			
			exchange.getIn().setHeader(Exchange.HTTP_QUERY, queryParams);
		}
		
		config(targetReference.getTargetHost());
		
		LoadBalancerDisplay.displayExchange(LOGGER, name, exchange, " IN");

		LoadBalancerDisplay.logDebug(LOGGER, name, "Send HTTP request to target reference '" +targetReference.getReference()+ "'");
		
		try {
		
			httpProducer.process(exchange);
			LoadBalancerDisplay.logDebug(LOGGER, name, "Successfully sent request to target reference '" +targetReference.getReference()+ "'");
			isOk = checkResponse(exchange);
			
		} catch(Exception e) {

			TDRDataService.setTxTDRProperty(TDRConstant.EVENT_TYPE, TDRConstant.EVENT_TYPE_TARGETTIMEOUT, exchange);
			LoadBalancerDisplay.logError(LOGGER, name, "GRAVE: Issue during the call", e);
			
		}
		
		LoadBalancerDisplay.displayExchange(LOGGER, name, exchange, " OUT");
		
		return isOk;
	}
	
	protected void config(TargetHost targetHost){
		if(targetHost == null){
			throw new RuntimeException("Targethost cannot be null!");
		}
		
		DefaultHttpClient client = (DefaultHttpClient) httpProducer.getHttpClient();
		HttpParams params = client.getParams();
		
		ConnectionParameters connParams = targetHost.getConnectionParameters();
		if(connParams != null){
			HttpConnectionParamBean httpConnectionParamBean = new HttpConnectionParamBean(params);
			if(connParams.getConnectionTimeout() != null)
				httpConnectionParamBean.setConnectionTimeout(connParams.getConnectionTimeout());
			if(connParams.getSocketTimeout() != null)
				httpConnectionParamBean.setSoTimeout(connParams.getSocketTimeout());
			
			ClientConnectionManager ccm = client.getConnectionManager();
			if(ccm instanceof ThreadSafeClientConnManager){
				ThreadSafeClientConnManager tsccm = (ThreadSafeClientConnManager) ccm;
				HttpRoute route = TargetHost2HttpRouteUtil.fromTargetHost(targetHost); 
				if(connParams.getMaxConnections() != null)
					tsccm.setMaxForRoute(route, targetHost.getConnectionParameters().getMaxConnections());
			}
			
			
		} else {
			LOGGER.debug("Connection parameters for target host {} are NULL", targetHost.getUrl());
		}
        
		ForwardProxy forwardProxy = targetHost.getForwardProxy();
		if(forwardProxy != null){
			String host = forwardProxy.getProxyHost();
			String port = forwardProxy.getProxyPort();
			if(host != null && port != null){
		        ConnRouteParamBean connRouteParamBean = new ConnRouteParamBean(params);
				int iport = new Integer(port);
				HttpHost proxy = new HttpHost(host, iport);
				connRouteParamBean.setDefaultProxy(proxy);
				
				String user = forwardProxy.getProxyUser();
				String pass = forwardProxy.getProxyPass();
				if(user != null && pass != null){
					Credentials credentials = null;
					
					String ntdomain = null; //targetHost.getForwardProxy().getProxyNtDomain;
					String ntworkstation = null; //targetHost.getForwardProxy().getProxyNtWorkstation();
					if(ntdomain != null || ntworkstation != null){
						credentials = new NTCredentials(user, pass, ntworkstation, ntdomain);
					} else {
						credentials = new UsernamePasswordCredentials(user,pass);
					}
					AuthScope authscope = new AuthScope(host, iport);
					client.getCredentialsProvider().setCredentials(authscope, credentials);
				}
			} else {
				LOGGER.debug("Host or port for target host {} forward proxy are NULL", targetHost.getUrl());
			}
		} else {
			LOGGER.debug("Forward proxy for target host {} is NULL", targetHost.getUrl());
		}
		
		
	}
	
	private boolean checkResponse(Exchange exchange) {
		// isOK = false : launch Failover process for trying another target
		boolean isOk = false;
		
		int httpCode = (Integer) exchange.getOut().getHeader(Exchange.HTTP_RESPONSE_CODE);
		LoadBalancerDisplay.logDebug(LOGGER, name, "HTTP request response code '" +httpCode+ "'");
		
		if (httpCode != this.failedOverErrorCode) {
			LoadBalancerDisplay.logDebug(LOGGER, name, "HTTP request response code '" +httpCode+ "' is not failover code");
			isOk = true;
		} else {
			LoadBalancerDisplay.logWarn(LOGGER, name, "Failover http error code : " + this.failedOverErrorCode + " detected, Failover process launched.");
		}
		
		return isOk;		
	}
	


	
		
	
}
