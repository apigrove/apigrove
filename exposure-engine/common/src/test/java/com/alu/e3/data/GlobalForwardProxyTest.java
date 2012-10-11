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
package com.alu.e3.data;

import org.junit.Test;

import com.alu.e3.common.E3Constant;
import com.alu.e3.common.caching.IEntryListener;
import com.alu.e3.data.model.sub.GlobalForwardProxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class GlobalForwardProxyTest {

	@Test
	public void test() {
		GlobalForwardProxy proxy = new GlobalForwardProxy();
		
		DataManagerMock dataManager = new DataManagerMock(); 
		
		proxy.setDataManager(dataManager);
		proxy.init();
		proxy.dataManagerReady();
		
		assertNull(proxy.getProxyHost());
		assertNull(proxy.getProxyPort());
		assertNull(proxy.getProxyUser());
		assertNull( proxy.getProxyPass());
		
		dataManager.addProxy("host1#port1#user1#pass1");
		assertEquals("host1", proxy.getProxyHost());
		assertEquals("port1", proxy.getProxyPort());
		assertEquals("user1", proxy.getProxyUser());
		assertEquals("pass1", proxy.getProxyPass());
		
		dataManager.updateProxy("host2#port2#user2#pass2");
		assertEquals("host2", proxy.getProxyHost());
		assertEquals("port2", proxy.getProxyPort());
		assertEquals("user2", proxy.getProxyUser());
		assertEquals("pass2", proxy.getProxyPass());
		
		dataManager.removeProxy("host3#port3#user3#pass3");
		assertNull(proxy.getProxyHost());
		assertNull(proxy.getProxyPort());
		assertNull(proxy.getProxyUser());
		assertNull( proxy.getProxyPass());
	}

	
	public class DataManagerMock extends DataManager {
		IEntryListener<String, String> listener;
		
		
		public void addProxy(String proxy) {
			listener.entryAdded(new DataEntryEvent<String, String> (E3Constant.GLOBAL_PROXY_SETTINGS, proxy));
		}
		
		public void updateProxy(String proxy) {
			listener.entryUpdated(new DataEntryEvent<String, String> (E3Constant.GLOBAL_PROXY_SETTINGS, proxy));
		}
		
		public void removeProxy(String proxy) {
			listener.entryRemoved(new DataEntryEvent<String, String> (E3Constant.GLOBAL_PROXY_SETTINGS, proxy));
		}
	
		@Override
		public void addGlobalProxyListener3(IEntryListener<String, String> listener) {
			this.listener = listener;
		}

		@Override
		public void removeGlobalProxyListener3(IEntryListener<String, String> listener) {
			this.listener = null;
		}

		
		@Override
		public String getSettingString(String key) {
			return null;
		}
		
	}
}
