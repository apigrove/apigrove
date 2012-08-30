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
package com.alu.e3.gateway;

import java.net.URL;

import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.log4j.Logger;

import com.alu.e3.data.model.sub.TargetHost;

public class TargetHost2HttpRouteUtil {
	private static Logger logger = Logger.getLogger(TargetHost2HttpRouteUtil.class);

	public static HttpRoute fromTargetHost(TargetHost th){
		HttpRoute route = null;
		try{
			URL url = new URL(th.getUrl());
			boolean secured = url.getProtocol().equalsIgnoreCase("HTTPS");

			if(th.getForwardProxy() != null){
				route = new HttpRoute(
						new HttpHost(url.getHost(), url.getPort()), 
						null, 
						new HttpHost(th.getForwardProxy().getProxyHost(), 
								Integer.parseInt(th.getForwardProxy().getProxyPort())),
								secured);
			}
			else{
				route = new HttpRoute(
						new HttpHost(url.getHost(), url.getPort()), 
						null,  
						secured);
			}

		} catch(Exception e){
			logger.error(e.getMessage(), e);
		}

		return route;
	}
}
