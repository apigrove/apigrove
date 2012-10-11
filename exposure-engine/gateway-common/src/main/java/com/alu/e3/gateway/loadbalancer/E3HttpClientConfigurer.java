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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.camel.component.http4.HttpClientConfigurer;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRouteParamBean;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParamBean;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alu.e3.data.model.sub.IForwardProxy;
import com.alu.e3.data.model.sub.TargetHost;

public class E3HttpClientConfigurer implements HttpClientConfigurer {

	// logger
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientConfigurer.class);
	
	// HTTP protocols
	private static final String HTTP_SECURED_PROTOCOL = "HTTPS";
	
	// default connection pool settings
	public static final int DEFAULT_CONNECTIONS_PER_ROUTE = 400;
	public static final int DEFAULT_MAX_CONNECTIONS_POOL = 2000;
	
	// HttpClient : connection pool settings
	private Integer connectionsPerRoute;
	private Integer maxConnectionsPool;
	private Map<TargetHost, Integer> connectionsPerRoutes;

	// HttpClient setting
	private Integer socketTimeOut;
	private Integer connectionTimeOut;
	private IForwardProxy forwardProxy;
	
	
	@Override
	public void configureHttpClient(HttpClient httpClient) {
		
		ClientConnectionManager clientConnectionManager = httpClient.getConnectionManager();
		HttpParams params = httpClient.getParams();
		
		// set Connection Pool Manager
		if(clientConnectionManager instanceof ThreadSafeClientConnManager) {
			setThreadSafeConnectionManager(clientConnectionManager);
		} else {
			LOGGER.error("The given settings for the HttpClient connection pool will be ignored: Unsupported implementation");
		}
		
		// set HttpClient global setting
		HttpConnectionParamBean httpConnectionParamBean = new HttpConnectionParamBean(params);		
		if(getSocketTimeOut() != null) {
			httpConnectionParamBean.setSoTimeout(getSocketTimeOut());
		}		
		// set HttpClient global connection timeout		
		if(getConnectionTimeOut() != null) {
			httpConnectionParamBean.setConnectionTimeout(getConnectionTimeOut());
		}
		
		// set HttpClient Proxy settings
		if(getForwardProxy() != null) {
			setForwardProxy(httpClient, params);
		}
	}


	/**
	 * Configures the ClientConnectionManager
	 * @param clientConnectionManager to be set
	 */
	private void setThreadSafeConnectionManager(ClientConnectionManager clientConnectionManager) {
		
		ThreadSafeClientConnManager threadSafeclientConnectionManager = (ThreadSafeClientConnManager) clientConnectionManager;
		
		// set HttpClient connection pool : global and default values
		threadSafeclientConnectionManager.setMaxTotal(getMaxConnectionsPool());
		threadSafeclientConnectionManager.setDefaultMaxPerRoute(getConnectionsPerRoute());
		
		// set HttpClient connection pool : max connection per route
		if(connectionsPerRoutes != null) {			
			Set<TargetHost> targetHosts = connectionsPerRoutes.keySet();
			
			for(TargetHost targetHost : targetHosts) {			
				try {
					HttpRoute route = getHttpRoute(targetHost);
					threadSafeclientConnectionManager.setMaxForRoute(route, connectionsPerRoutes.get(targetHost));
				} catch (MalformedURLException e) {
					LOGGER.warn("Unable to set a max connection for route [" + targetHost.getUrl() + "]" , e);
				}
			}
		}
	}
	
	
	/**
	 * Sets global PROXY for HttpClient
	 * @param client HttpClient
	 * @param params of the PROXY
	 */
	private void setForwardProxy(HttpClient client, HttpParams params){
			
		if(forwardProxy != null) {
			
			if(client instanceof DefaultHttpClient) {
			
				String host = forwardProxy.getProxyHost();
				Integer port = new Integer(forwardProxy.getProxyPort());
				
				if(host != null && port != null){
	
					HttpHost httpHost = new HttpHost(host, port);
					
			        ConnRouteParamBean connRouteParamBean = new ConnRouteParamBean(params);
					connRouteParamBean.setDefaultProxy(httpHost);
					
					String user = forwardProxy.getProxyUser();
					String pass = forwardProxy.getProxyPass();
					
					if(user != null && pass != null) {
						
						Credentials credentials = null;
						
						String ntdomain = null; //targetHost.getForwardProxy().getProxyNtDomain;
						String ntworkstation = null; //targetHost.getForwardProxy().getProxyNtWorkstation();
						
						if(ntdomain != null || ntworkstation != null) {
							credentials = new NTCredentials(user, pass, ntworkstation, ntdomain);
						} else {
							credentials = new UsernamePasswordCredentials(user,pass);
						}
						
						AuthScope authscope = new AuthScope(host, port);
						
						DefaultHttpClient defaultHttpClient = (DefaultHttpClient) client;
						defaultHttpClient.getCredentialsProvider().setCredentials(authscope, credentials);
						
					} else {
						if(LOGGER.isDebugEnabled()) {
							LOGGER.debug("The proxy is set with no user or password information");
						}
					}
					
				} else {
					LOGGER.error("Unable to set proxy settings: Host or Port are NULL");
				}	
			
			} else {
				LOGGER.error("Unable to set proxy settings: Unsupported HttpClient implementation");
			}
		} 
	}
	
	
	/**
	 * Sets the global  max connection pool
	 * @param maxConnectionsPool max connection pool
	 */
	public void setMaxConnectionsPool(int maxConnectionsPool) {
		this.maxConnectionsPool = new Integer(maxConnectionsPool);
	}
		
	
	/**
	 * Gets the global max connection pool
	 * @return max connection pool
	 */
	public int getMaxConnectionsPool() {
		if(maxConnectionsPool != null) {
			return maxConnectionsPool;
		} else {
			return DEFAULT_MAX_CONNECTIONS_POOL;
		}
	}
	
	/**
	 * Gets the global socket time out
	 * @return socket time out
	 */
	public Integer getSocketTimeOut() {
		return socketTimeOut;
	}
	
	/**
	 * Sets the global socket time out  
	 * @param socketTimeOut socket time out
	 */
	public void setSocketTimeOut(int socketTimeOut) {
		this.socketTimeOut = socketTimeOut;
	}
	

	/**
	 * Gets the global connection time out
	 * @return connection time out
	 */
	public Integer getConnectionTimeOut() {
		return connectionTimeOut;
	}


	/**
	 * Sets the global connection time out
	 * @param connectionTimeOut connection time out
	 */
	public void setConnectionTimeOut(int connectionTimeOut) {
		this.connectionTimeOut = connectionTimeOut;
	}	
	
	/**
	 * Sets the global PROXY settings
	 * @param forwardProxy
	 */
	public void setForwardProxy(IForwardProxy forwardProxy) {
		this.forwardProxy = forwardProxy;
	}

	/**
	 * Gets the global PROXY settings
	 * @return
	 */
	public IForwardProxy getForwardProxy() {
		return forwardProxy;
	}	

	/**
	 * Sets the default max connection pool per route
	 * @param connectionsPerRoute max connection pool for a route
	 */
	public void setConnectionsPerRoute(int connectionsPerRoute) {
		this.connectionsPerRoute = new Integer(connectionsPerRoute);
	}		

	/**
	 * Gets the default max connections per route
	 * @return  max connection pool for a route
	 */
	public int getConnectionsPerRoute() {
		if(connectionsPerRoute != null) {
			return connectionsPerRoute;
		} else {
			return DEFAULT_CONNECTIONS_PER_ROUTE;
		}
	}	
	
	/**
	 * Gets the max connections per route in the pool
	 * @return
	 */
	public Map<TargetHost, Integer> getConnectionsPerRoutes() {
		if(connectionsPerRoutes == null) {
			connectionsPerRoutes = new HashMap<TargetHost, Integer>();
		}
		return connectionsPerRoutes;
	}
	
	/**
	 * Converts a TargetHost to an HttpRoute 
	 * @param targetHost the target host to transform
	 * @return HttpRoute HTTP route
	 * @throws MalformedURLException
	 */
	private HttpRoute getHttpRoute(TargetHost targetHost) throws MalformedURLException {
		
		HttpRoute route = null;

		URL url = new URL(targetHost.getUrl());
		
		boolean secured = url.getProtocol().equalsIgnoreCase(HTTP_SECURED_PROTOCOL);

		if(forwardProxy != null) {
			
			Integer proxyPort = Integer.parseInt(forwardProxy.getProxyPort()); 
			String proxyHost = forwardProxy.getProxyHost();
			
			route = new HttpRoute (
					new HttpHost(url.getHost(), url.getPort()), 
					null, 
					new HttpHost(proxyHost, proxyPort), secured
					);
		} else {
			
			route = new HttpRoute(
					new HttpHost(url.getHost(), url.getPort()), 
					null,  
					secured
					);
		}

		return route;		
	}
	
}
