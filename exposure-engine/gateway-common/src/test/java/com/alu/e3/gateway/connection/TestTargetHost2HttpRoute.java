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
package com.alu.e3.gateway.connection;

import junit.framework.Assert;

import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.junit.Test;

import com.alu.e3.data.model.sub.ForwardProxy;
import com.alu.e3.data.model.sub.TargetHost;
import com.alu.e3.gateway.TargetHost2HttpRouteUtil;

public class TestTargetHost2HttpRoute {

	/**
	 * Tests the basline functionality that we need to confirm:
	 * That given two "equal" routes they will return the same maxConnections value
	 */
	@Test
	public void testBaseline(){
		HttpRoute route = new HttpRoute(new HttpHost("google.com"));
		HttpRoute route2 = new HttpRoute(new HttpHost("google.com"));
		doMaxConnTests(route, route2);
	}

	@Test
	public void testNonSecureRoutes(){
		String url = "http://google.com";
		TargetHost th1 = new TargetHost();
		th1.setUrl(url);
		TargetHost th2 = new TargetHost();
		th2.setUrl(url);

		HttpRoute route1 = TargetHost2HttpRouteUtil.fromTargetHost(th1);
		HttpRoute route2 = TargetHost2HttpRouteUtil.fromTargetHost(th2);

		Assert.assertFalse(route1.isSecure());
		Assert.assertNull(route1.getProxyHost());

		doMaxConnTests(route1, route2);
	}

	@Test
	public void testSecureRoutes(){
		String url = "https://google.com";
		TargetHost th1 = new TargetHost();
		th1.setUrl(url);
		TargetHost th2 = new TargetHost();
		th2.setUrl(url);

		HttpRoute route1 = TargetHost2HttpRouteUtil.fromTargetHost(th1);
		HttpRoute route2 = TargetHost2HttpRouteUtil.fromTargetHost(th2);

		Assert.assertTrue(route1.isSecure());
		Assert.assertNull(route1.getProxyHost());

		doMaxConnTests(route1, route2);
	}

	@Test
	public void testProxiedRoutes(){
		String url = "http://google.com";

		ForwardProxy fp = new ForwardProxy();
		fp.setProxyHost("alu-proxy.com");
		fp.setProxyPort("1234");

		TargetHost th1 = new TargetHost();
		th1.setUrl(url);
		th1.setForwardProxy(fp);

		TargetHost th2 = new TargetHost();
		th2.setUrl(url);
		th2.setForwardProxy(fp);

		HttpRoute route1 = TargetHost2HttpRouteUtil.fromTargetHost(th1);
		HttpRoute route2 = TargetHost2HttpRouteUtil.fromTargetHost(th2);

		Assert.assertFalse(route1.isSecure());
		Assert.assertNotNull(route1.getProxyHost());

		doMaxConnTests(route1, route2);
	}

	@Test
	public void testSecuredProxied(){
		String url = "https://google.com";

		ForwardProxy fp = new ForwardProxy();
		fp.setProxyHost("alu-proxy.com");
		fp.setProxyPort("1234");

		TargetHost th1 = new TargetHost();
		th1.setUrl(url);
		th1.setForwardProxy(fp);

		TargetHost th2 = new TargetHost();
		th2.setUrl(url);
		th2.setForwardProxy(fp);

		HttpRoute route1 = TargetHost2HttpRouteUtil.fromTargetHost(th1);
		HttpRoute route2 = TargetHost2HttpRouteUtil.fromTargetHost(th2);

		Assert.assertTrue(route1.isSecure());
		Assert.assertNotNull(route1.getProxyHost());

		doMaxConnTests(route1, route2);
	}




	/**
	 * Private helper function
	 * @param route1
	 * @param route2
	 */
	private void doMaxConnTests(HttpRoute route1, HttpRoute route2){
		ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager();

		int originalMax = manager.getMaxForRoute(route1);

		int originalMax2 = manager.getMaxForRoute(route2);

		Assert.assertEquals(originalMax, originalMax2);

		int newMax = originalMax+1;

		manager.setMaxForRoute(route1, newMax);

		int newMax2 = manager.getMaxForRoute(route2);

		Assert.assertEquals(newMax, newMax2);
	}

}
