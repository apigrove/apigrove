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
package com.alu.e3.auth.access.data;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.alu.e3.common.caching.ICacheManager;
import com.alu.e3.common.caching.IEntryListener;
import com.alu.e3.common.logging.Category;
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.data.IAuthMatcher;
import com.alu.e3.data.IDataManagerListener;
import com.alu.e3.data.IDataManagerUsedBucketIdsListener;
import com.alu.e3.data.model.Api;
import com.alu.e3.data.model.ApiJar;
import com.alu.e3.data.model.Auth;
import com.alu.e3.data.model.CallDescriptor;
import com.alu.e3.data.model.Certificate;
import com.alu.e3.data.model.CertificateRequest;
import com.alu.e3.data.model.Key;
import com.alu.e3.data.model.Limit;
import com.alu.e3.data.model.LogLevel;
import com.alu.e3.data.model.Policy;
import com.alu.e3.data.model.SSLCRL;
import com.alu.e3.data.model.enumeration.StatusType;
import com.alu.e3.data.model.sub.APIContext;
import com.alu.e3.data.model.sub.Context;
import com.alu.e3.data.model.sub.QuotaRLBucket;

public class MockDataManager implements IDataManager {

	private Api api = null;
	private Auth auth = null;
	private List<CallDescriptor> callDescriptors = null;

	public MockDataManager() {		
	}

	public MockDataManager(boolean isAlwaysHappy) {
		Api api = new Api();
		api.setStatus(StatusType.ACTIVE);
		this.setApi(api);		
		if(isAlwaysHappy) {
			this.setAuth(new Auth());
			this.getCallDescriptors().add(new CallDescriptor(new Policy(), 1, 2));
		} 
	}

	@Override
	public Set<String> getAllApiIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addApi(Api api) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateApi(Api api) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeApi(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public Api getApiById(String id) {
		return this.api;
	}

	@Override
	public Api getApiById(String id, boolean getFullDetails) {
		// TODO Auto-generated method stub
		return this.api;
	}

	@Override
	public Set<String> getAllAuthIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addAuth(Auth auth) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateAuth(Auth auth) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAuth(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public Auth getAuthById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Auth getAuthById(String id, boolean getFullDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getAllPolicy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addPolicy(Policy policy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updatePolicy(Policy policy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePolicy(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public Policy getPolicyById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addAuthsToBucket(String policyId, String bucketId,
			QuotaRLBucket authIds) throws InvalidParameterException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAuthFromBucket(String policyId, String bucketId,
			String authId) throws InvalidParameterException {
		// TODO Auto-generated method stub

	}

	@Override
	public void createBucket(String policyId, QuotaRLBucket authIds)
			throws InvalidParameterException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeBucket(String policyId, String bucketId)
			throws InvalidParameterException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<CallDescriptor> getMatchingPolicies(Api api) {
		return this.callDescriptors;
	}

	@Override
	public List<CallDescriptor> getMatchingPolicies(Api api, Auth auth) {
		return this.callDescriptors;
	}

	/**
	 * @param api the api to set
	 */
	public void setApi(Api api) {
		this.api = api;
	}

	/**
	 * @param auth the auth to set
	 */
	public void setAuth(Auth auth) {
		this.auth = auth;
	}

	/**
	 * @param policies the policies to set
	 */
	public List<CallDescriptor> getCallDescriptors() {

		if(callDescriptors == null) {
			callDescriptors = new ArrayList<CallDescriptor>();
		}

		return this.callDescriptors;
	}

	@Override
	public Policy getPolicyById(String id, boolean getFullDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fillLimitsById(Integer contextId, Limit limit) {
		// TODO Auto-generated method stub

	}

	@Override
	public Context getPolicyContextById(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public APIContext getApiContextById(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Auth getAuthByAuthKey(String authKey) {
		// TODO Auto-generated method stub
		return this.auth;
	}

	@Override
	public Auth getAuthByUserPass(String username, String password) {
		// TODO Auto-generated method stub
		return this.auth;
	}

	@Override
	public Auth getWsseAuth(String username, String passwordDigest, boolean isPasswordText, String nonce, String created) {
		// TODO:
		return this.auth;
	}

	@Override
	public Auth getAuthByIP(String IP) {
		// TODO Auto-generated method stub
		return this.auth;
	}

	@Override
	public void addAuthsToBucket(List<String> policyId, String bucketId, QuotaRLBucket authIds) throws InvalidParameterException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeBucket(List<String> policyIds, String bucketId) throws InvalidParameterException {
		// TODO Auto-generated method stub

	}

	@Override
	public void putSettingString(String key, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearSettingString(String key) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getSettingString(String key) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public	void setCacheManager(ICacheManager cacheManager) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean deployApi(String apiId, byte[] jarData) {
		return false;
	}

	@Override
	public boolean undeployApi(String apiId) {
		return false;
	}

	@Override
	public void addApiDeploymentListener(IEntryListener<String, ApiJar> listener) {

	}

	@Override
	public void removeApiDeploymentListener(IEntryListener<String, ApiJar> listener) {

	}

	@Override
	public void addListener(IDataManagerListener listener) {	

	}

	@Override
	public void removeListener(IDataManagerListener listener) {

	}

	@Override
	public void removeListener(IDataManagerUsedBucketIdsListener listener) {

	}	
	
	@Override
	public void addKey(Key key) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<String> getAllKeyIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Key getKeyById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Key getKeyById(String id, boolean getFullDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateKey(Key key) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeKey(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addCert(Certificate cert) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<String> getAllCertIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Certificate getCertById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Certificate getCertById(String id, boolean getFullDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeCert(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateCert(Certificate certificate) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<String> getAllCertIdsForKeyId(String keyId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeKeyListener(IEntryListener<String, Key> listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addKeyListener(IEntryListener<String, Key> listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void appendAuthsToBucket(List<String> policyIds, String bucketId,
			QuotaRLBucket authIds) throws IllegalArgumentException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLogLevel(LogLevel logLevel) {
		// TODO Auto-generated method stub
	}

	@Override
	public LogLevel getLogLevel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSMXLogLevel(LogLevel logLevel) {
		// TODO Auto-generated method stub
	}

	@Override
	public LogLevel getSMXLogLevel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSyslogLevel(LogLevel logLevel) {
		// TODO Auto-generated method stub
	}

	@Override
	public LogLevel getSyslogLevel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reloadGateway(String ip) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addCA(Certificate cert) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<String> getAllCA() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Certificate getCAById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateCA(Certificate cert) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeCA(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addCRL(SSLCRL clr) {
		// TODO Auto-generated method stub

	}

	@Override
	public SSLCRL getCRLById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateCRL(SSLCRL crl) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeCRL(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<String> getAllCRL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isApiExist(String apiId) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Collection<SSLCRL> getAllCRLValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addCrlListener(IEntryListener<String, SSLCRL> listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeCrlListener(IEntryListener<String, SSLCRL> listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addCAListener(IEntryListener<String, Certificate> listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeCAListener(IEntryListener<String, Certificate> listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addLogLevelListener(IEntryListener<String, LogLevel> listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeLogLevelListener(IEntryListener<String, LogLevel> listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public ApiJar getApiJar(String apiId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Auth getAuthByOAuth(String clientId, String clientSecret) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean endpointExists(String endpoint) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Auth getAuthMatching(IAuthMatcher authMatcher) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addApiListener(IEntryListener<String, Api> listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeApiListener(IEntryListener<String, Api> listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListener(IDataManagerUsedBucketIdsListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeAuthsFromBucket(List<String> policyIds, String bucketId,
			List<String> authIds) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		
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

	@Override
	public void addGlobalProxyListener3(IEntryListener<String, String> listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeGlobalProxyListener3(
			IEntryListener<String, String> listener) {
		// TODO Auto-generated method stub
		
	}

}
