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
package com.alu.e3.common.tools;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

import com.alu.e3.common.E3Constant;

public class CommonTools {

	protected static InetAddress localInetAddress;

	
	static {
		if (localInetAddress == null) {
			try {
				localInetAddress = InetAddress.getLocalHost();
			}
			catch (UnknownHostException e) {
				localInetAddress = null;
			}
		}
	}
	
	protected static InetAddress getLocalInetAddress() {
		if (localInetAddress == null) {
			try {
				localInetAddress = InetAddress.getLocalHost();
			}
			catch (UnknownHostException e) {
				localInetAddress = null;
			}
		}
		return localInetAddress;
	}
	
	public static String getLocalHostname() {
		return getLocalInetAddress() == null ? "" : getLocalInetAddress().getHostName();
	}

	public static String getLocalAddress() {
		return getLocalInetAddress() == null ? "" : getLocalInetAddress().getHostAddress();
	}
	
	public static boolean isLocal(String ip) {

		boolean isLocal = "127.0.0.1".equals(ip) || "localhost".equalsIgnoreCase(ip);

		if (!isLocal && getLocalInetAddress() != null) {
			isLocal = ip.equals(getLocalInetAddress().getHostAddress()) || ip.equalsIgnoreCase(getLocalInetAddress().getHostName());
		}

		return isLocal;
	}
	
	public static String[] splitUrl(String urlToSplit) {
		try {
			URL url = new URL(urlToSplit);
	
			String strUrl = url.getProtocol() + "://" +  url.getHost();
			
			int port = url.getPort();
			if(port != -1)
				strUrl += ":" + port;
			
			String path = url.getPath();
			if(path != null && !path.isEmpty())
				strUrl += path;
			
			return new String[]{strUrl, url.getQuery()};
			
		} catch (MalformedURLException e) {
			return null;
		}
	}
	
	
	/**
	 * get the real remote address from a request header
	 * @return the real remote address
	 */
	public static String remoteAddr(HttpServletRequest request){
		String remoteAddr = null;

		if (request != null){
			
			//get the real remote address, if x-forwarded-for field not empty, retrieve IP address inside
			remoteAddr = request.getRemoteAddr();
			String x;
			if ((x = request.getHeader(E3Constant.HEADER_X_FORWARDED_FOR)) != null) {
				remoteAddr = x;
				int idx = remoteAddr.indexOf(',');
				if (idx > -1) {
					remoteAddr = remoteAddr.substring(0, idx);
				}
			}
		}

		return remoteAddr;
	}
	
}
