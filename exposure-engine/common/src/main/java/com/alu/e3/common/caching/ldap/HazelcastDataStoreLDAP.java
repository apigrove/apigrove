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
package com.alu.e3.common.caching.ldap;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.directory.InitialDirContext;

import com.hazelcast.core.MapStore;

@SuppressWarnings("rawtypes")
public class HazelcastDataStoreLDAP implements MapStore {
	
	private final static String LDAP_ROOT_PATH = ",ou=e3,dc=exEngine,dc=com"; 

	private String basePath;
	
	// shared amongst all data store
	private static InitialDirContext dirContext;
	
	// TODO: add logs
	public HazelcastDataStoreLDAP(String basePath, String ip, String port, String principal, String credentials) {
		
		try {
			this.basePath = basePath + LDAP_ROOT_PATH;
			
			if (dirContext == null) {
				String url = "ldap://" + ip + ":" + port;
				
				Hashtable<String, String> env = new Hashtable<String, String>();
				
				env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
				env.put(Context.PROVIDER_URL, url);
				env.put(Context.SECURITY_AUTHENTICATION, "Simple");
				env.put(Context.SECURITY_PRINCIPAL, principal);
				env.put(Context.SECURITY_CREDENTIALS, credentials);
		
				dirContext = new InitialDirContext(env);
			}
		}
		catch (Exception e) {
			System.out.println("HazelcastDataStoreLDAP failure");
			e.printStackTrace();
		}
	}
	
	@Override
	public Object load(Object key) {
		Object obj = null;
		
		try {
			synchronized (dirContext) {
				obj = dirContext.lookup("cn="+key+","+basePath);				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return obj;
	}

	@Override
	public Map loadAll(Collection keys) {
		Map<Object, Object> map = new HashMap<Object, Object>();
		
		for (Object key : keys) {
			Object value = load(key);			
			map.put(key, value);
		}

		return map;
	}

	@Override
	public Set loadAllKeys() {
		Set<Object> keys = new HashSet<Object>();
		
		try {
			synchronized (dirContext) {
				NamingEnumeration list = dirContext.list(basePath);
				
				while (list.hasMore()) {
				    NameClassPair nc = (NameClassPair)list.next();
				    
				    // remove the "cn="
				    keys.add(nc.getName().substring(3));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return keys;
	}

	@Override
	public void delete(Object key) {
		try {
			synchronized (dirContext) {
				dirContext.unbind("cn="+key+","+basePath);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deleteAll(Collection keys) {
		for (Object key : keys) {
			delete(key);
		}
	}

	@Override
	public void store(Object key, Object value) {
		try {
			synchronized (dirContext) {
				dirContext.rebind("cn="+key+","+basePath, value);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void storeAll(Map map) {
		if (map != null) {
			// Weird error if using map and not map2 in the loop
			@SuppressWarnings("unchecked")
			Map<Object, Object> map2 = (Map<Object, Object>) map;
			for (Map.Entry<Object, Object> entry : map2.entrySet()) {
				store(map.get(entry.getKey()), map.get(entry.getValue()));
			}		
		}
	}
}
