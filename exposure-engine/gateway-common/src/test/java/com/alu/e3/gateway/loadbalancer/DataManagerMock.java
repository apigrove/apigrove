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

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.alu.e3.common.E3Constant;
import com.alu.e3.common.caching.ICacheManager;
import com.alu.e3.common.caching.IEntryListener;
import com.alu.e3.common.logging.Category;
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.data.DataEntryEvent;
import com.alu.e3.data.IAuthMatcher;
import com.alu.e3.data.IDataManagerListener;
import com.alu.e3.data.IDataManagerUsedBucketIdsListener;
import com.alu.e3.data.model.Api;
import com.alu.e3.data.model.ApiJar;
import com.alu.e3.data.model.Auth;
import com.alu.e3.data.model.CallDescriptor;
import com.alu.e3.data.model.Certificate;
import com.alu.e3.data.model.Key;
import com.alu.e3.data.model.Limit;
import com.alu.e3.data.model.LogLevel;
import com.alu.e3.data.model.Policy;
import com.alu.e3.data.model.SSLCRL;
import com.alu.e3.data.model.sub.APIContext;
import com.alu.e3.data.model.sub.ApiIds;
import com.alu.e3.data.model.sub.Context;
import com.alu.e3.data.model.sub.ForwardProxy;
import com.alu.e3.data.model.sub.QuotaRLBucket;


public class DataManagerMock implements IDataManager {
	
	protected Map<String, Api> apis;
	protected Map<Integer, APIContext> contexts;
	
	public DataManagerMock() {
		apis = new HashMap<String, Api>();
		contexts = new HashMap<Integer, APIContext>();
	}
	
	@Override
	public void addListener(IDataManagerListener listener) {		
	}

	
	
	@Override
	public void removeListener(IDataManagerListener listener) {
		// Nothing to do			
	}

	
	
	@Override
	public Set<String> getAllApiIds() {
		throw new RuntimeException("Not implemented");
		
	}

		
	@Override
	public void addApi(Api api) {
		apis.put(api.getId(), api);
		
		for(APIContext context : api.getApiDetail().getContexts()) {
			Random r = new Random();
			
			Integer id = new Integer(r.nextInt());
			context.setContextId(id);
			contexts.put(id, context);
			ApiIds ids = new ApiIds(context.getId(), context.getContextId(), 0, true);
			api.getContextIds().add(ids);
		}
	}

	
	
	@Override
	public boolean endpointExists(String endpoint) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void updateApi(Api api) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void removeApi(String id) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public Api getApiById(String id) {
		return apis.get(id);
	}

	
	
	@Override
	public Api getApiById(String id, boolean getFullDetails) {
		return apis.get(id);
	}

	
	
	@Override
	public Set<String> getAllAuthIds() {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void addAuth(Auth auth) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void updateAuth(Auth auth) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void removeAuth(String id) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public Auth getAuthById(String id) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public Auth getAuthById(String id, boolean getFullDetails) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public Auth getAuthByAuthKey(String authKey) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public Auth getAuthByOAuth(String clientId, String clientSecret) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public Auth getAuthByUserPass(String username, String password) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public Auth getAuthByIP(String IP) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public Auth getAuthMatching(IAuthMatcher authMatcher) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public Set<String> getAllPolicy() {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void addPolicy(Policy policy) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void updatePolicy(Policy policy) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void removePolicy(String id) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public Policy getPolicyById(String id) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public Policy getPolicyById(String id, boolean getFullDetails) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void addAuthsToBucket(String policyId, String bucketId,
			QuotaRLBucket authIds) throws InvalidParameterException {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void addAuthsToBucket(List<String> policyId, String bucketId,
			QuotaRLBucket authIds) throws InvalidParameterException {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void appendAuthsToBucket(List<String> policyIds, String bucketId,
			QuotaRLBucket authIds) throws IllegalArgumentException {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void removeAuthFromBucket(String policyId, String bucketId,
			String authId) throws InvalidParameterException {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void createBucket(String policyId, QuotaRLBucket authIds)
			throws InvalidParameterException {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void removeBucket(String policyId, String bucketId)
			throws InvalidParameterException {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void removeBucket(List<String> policyIds, String bucketId)
			throws InvalidParameterException {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public List<CallDescriptor> getMatchingPolicies(Api api) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public List<CallDescriptor> getMatchingPolicies(Api api, Auth auth) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void fillLimitsById(Integer contextId, Limit limit) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public Context getPolicyContextById(Integer id) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public APIContext getApiContextById(Integer id) {
		APIContext context = contexts.get(id);
		return context;
	}

	
	
	@Override
	public void putSettingString(String key, String value) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void clearSettingString(String key) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public String getSettingString(String key) {
		return null;		
	}

	
	
	@Override
	public void addKey(Key key) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public Set<String> getAllKeyIds() {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public Key getKeyById(String id) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public Key getKeyById(String id, boolean getFullDetails) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void updateKey(Key key) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void removeKey(String id) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void addCert(Certificate cert) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public Set<String> getAllCertIds() {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public Set<String> getAllCertIdsForKeyId(String keyId) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public Certificate getCertById(String id) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public Certificate getCertById(String id, boolean getFullDetails) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void updateCert(Certificate certificate) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void removeCert(String id) {
		throw new RuntimeException("Not implemented");
		
	}

	@Override
	public void removeKeyListener(IEntryListener<String, Key> listener) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void addKeyListener(IEntryListener<String, Key> listener) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void setCacheManager(ICacheManager cacheManager) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public boolean deployApi(String apiId, byte[] jarData) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public boolean undeployApi(String apiId) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void addApiDeploymentListener(IEntryListener<String, ApiJar> listener) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void removeApiDeploymentListener(
			IEntryListener<String, ApiJar> listener) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void addLogLevelListener(IEntryListener<String, LogLevel> listener) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void removeLogLevelListener(IEntryListener<String, LogLevel> listener) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void setLogLevel(LogLevel logLevel) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public LogLevel getLogLevel() {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void setSMXLogLevel(LogLevel logLevel) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public LogLevel getSMXLogLevel() {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void setSyslogLevel(LogLevel logLevel) {
		throw new RuntimeException("Not implemented");
		
	}

		
	@Override
	public LogLevel getSyslogLevel() {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void reloadGateway(String ip) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void addCA(Certificate cert) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public Set<String> getAllCA() {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public Certificate getCAById(String id) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void updateCA(Certificate cert) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void removeCA(String id) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void addCAListener(IEntryListener<String, Certificate> listener) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void removeCAListener(IEntryListener<String, Certificate> listener) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void addCRL(SSLCRL clr) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public SSLCRL getCRLById(String id) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void updateCRL(SSLCRL crl) {
		throw new RuntimeException("Not implemented");
		
	}

		
	@Override
	public void removeCRL(String id) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public Set<String> getAllCRL() {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public Collection<SSLCRL> getAllCRLValues() {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void addCrlListener(IEntryListener<String, SSLCRL> listener) {
		throw new RuntimeException("Not implemented");
		
	}

	
	
	@Override
	public void removeCrlListener(IEntryListener<String, SSLCRL> listener) {
		throw new RuntimeException("Not implemented");
		
	}

		
	@Override
	public boolean isApiExist(String apiId) {
		throw new RuntimeException("Not implemented");
		
	}

		
	@Override
	public ApiJar getApiJar(String apiId) {
		throw new RuntimeException("Not implemented");
		
	}


	@Override
	public void addApiListener(IEntryListener<String, Api> listener) {
		throw new RuntimeException("Not implemented");
		
	}
	
	@Override
	public void removeApiListener(IEntryListener<String, Api> listener) {
		// throw new RuntimeException("Not implemented");
		// Commented because called by tearDown method
		
	}

	@Override
	public Auth getWsseAuth(String username, String passwordDigest, boolean isPasswordText, String nonce, String created) {
		throw new RuntimeException("Not implemented");
		
	}

	/* (non-Javadoc)
	 * @see com.alu.e3.common.osgi.api.IDataManager#addListener(com.alu.e3.data.IDataManagerUsedBucketIdsListener)
	 */
	@Override
	public void addListener(IDataManagerUsedBucketIdsListener listener) {
		throw new RuntimeException("Not implemented");
		
	}

	/* (non-Javadoc)
	 * @see com.alu.e3.common.osgi.api.IDataManager#removeListener(com.alu.e3.data.IDataManagerUsedBucketIdsListener)
	 */
	@Override
	public void removeListener(IDataManagerUsedBucketIdsListener listener) {
		throw new RuntimeException("Not implemented");
		
	}

	/* (non-Javadoc)
	 * @see com.alu.e3.common.osgi.api.IDataManager#removeAuthsFromBucket(java.util.List, java.lang.String, java.util.List)
	 */
	@Override
	public void removeAuthsFromBucket(List<String> policyIds, String bucketId,
			List<String> authIds) throws IllegalArgumentException {
		throw new RuntimeException("Not implemented");
		
	}

	@Override
	public void setLoggingCategory(Category category, boolean enabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getLoggingCategory(Category category) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isIpAllowed(Api api, String ip) {
		// TODO Auto-generated method stub
		return false;
	}

	protected IEntryListener<String, String> listenerX;
	
	@Override
	public void removeGlobalProxyListener3(IEntryListener<String, String> listener) {
		this.listenerX = null;
	}

	@Override
	public void addGlobalProxyListener3(IEntryListener<String, String> listener) {
		this.listenerX = listener;
	}

	protected ForwardProxy proxy;
	
	public void addGlobalProxy (ForwardProxy proxy) {
		this.proxy = proxy;
		if (listenerX != null) {
			DataEntryEvent<String, String> event = new DataEntryEvent<String, String>(E3Constant.GLOBAL_PROXY_SETTINGS, proxy.serialize());
			listenerX.entryAdded(event);
		}
	}
	public void updateGlobalProxy (ForwardProxy proxy) {
		this.proxy = proxy;
		if (listenerX != null) {
			DataEntryEvent<String, String> event = new DataEntryEvent<String, String>(E3Constant.GLOBAL_PROXY_SETTINGS, proxy.serialize());
			listenerX.entryUpdated(event);
		}
	}
	public void removeGlobalProxy () {
		this.proxy = null;
		if (listenerX != null) {
			DataEntryEvent<String, String> event = new DataEntryEvent<String, String>(E3Constant.GLOBAL_PROXY_SETTINGS, null);
			listenerX.entryAdded(event);
		}
	}

	
	public void addProxy(String proxy) {
		listenerX.entryAdded(new DataEntryEvent<String, String> (E3Constant.GLOBAL_PROXY_SETTINGS, proxy));
	}
	
	public void updateProxy(String proxy) {
		listenerX.entryUpdated(new DataEntryEvent<String, String> (E3Constant.GLOBAL_PROXY_SETTINGS, proxy));
	}
	
	public void removeProxy(String proxy) {
		listenerX.entryRemoved(new DataEntryEvent<String, String> (E3Constant.GLOBAL_PROXY_SETTINGS, proxy));
	}

}
