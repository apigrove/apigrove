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
package com.alu.e3.common.osgi.api;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.alu.e3.common.caching.ICacheManager;
import com.alu.e3.common.caching.IEntryListener;
import com.alu.e3.common.logging.Category;
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
import com.alu.e3.data.model.sub.Context;
import com.alu.e3.data.model.sub.QuotaRLBucket;

public interface IDataManager {

	void addListener(IDataManagerListener listener);
	void removeListener(IDataManagerListener listener);
	
	void addListener(IDataManagerUsedBucketIdsListener listener);
	void removeListener(IDataManagerUsedBucketIdsListener listener);

	Set<String> getAllApiIds();

	void addApi(Api api);

	boolean endpointExists(String endpoint);

	void updateApi(Api api);

	void removeApi(String id);

	Api getApiById(String id);

	Api getApiById(String id, boolean getFullDetails);

	Set<String> getAllAuthIds();

	void addAuth(Auth auth);

	void updateAuth(Auth auth);

	void removeAuth(String id);

	Auth getAuthById(String id);

	Auth getAuthById(String id, boolean getFullDetails);

	Auth getAuthByAuthKey(String authKey);

	Auth getAuthByOAuth(String clientId, String clientSecret);

	Auth getAuthByUserPass(String username, String password);
	
	Auth getWsseAuth(String username, String passwordDigest, boolean isPasswordText, String nonce, String created);

	Auth getAuthByIP(String IP);
	
	Auth getAuthMatching(IAuthMatcher authMatcher);

	Set<String> getAllPolicy();

	void addPolicy(Policy policy);

	void updatePolicy(Policy policy);

	void removePolicy(String id);

	Policy getPolicyById(String id);

	Policy getPolicyById(String id, boolean getFullDetails);

	void addAuthsToBucket(String policyId, String bucketId, QuotaRLBucket authIds) throws InvalidParameterException;

	void addAuthsToBucket(List<String> policyId, String bucketId, QuotaRLBucket authIds) throws InvalidParameterException;

	void appendAuthsToBucket(List<String> policyIds, String bucketId, QuotaRLBucket authIds) throws IllegalArgumentException;

	void removeAuthFromBucket(String policyId, String bucketId, String authId) throws InvalidParameterException;
	
	void removeAuthsFromBucket(List<String> policyIds, String bucketId, List<String> authIds) throws IllegalArgumentException;

	void createBucket(String policyId, QuotaRLBucket authIds) throws InvalidParameterException;

	void removeBucket(String policyId, String bucketId) throws InvalidParameterException;

	void removeBucket(List<String> policyIds, String bucketId) throws InvalidParameterException;

	List<CallDescriptor> getMatchingPolicies(Api api);

	List<CallDescriptor> getMatchingPolicies(Api api, Auth auth);

	void fillLimitsById(Integer contextId, Limit limit);

	Context getPolicyContextById(Integer id);

	APIContext getApiContextById(Integer id);

	void putSettingString(String key, String value);

	void clearSettingString(String key);

	String getSettingString(String key);

	void addKey(Key key);
	Set<String> getAllKeyIds();
	Key getKeyById(String id);
	Key getKeyById(String id, boolean getFullDetails);
	void updateKey(Key key);
	void removeKey(String id);

	void addCert(Certificate cert);
	Set<String> getAllCertIds();
	Set<String> getAllCertIdsForKeyId(String keyId);
	Certificate getCertById(String id);
	Certificate getCertById(String id, boolean getFullDetails);
	void updateCert(Certificate certificate);
	void removeCert(String id);

	void removeKeyListener(IEntryListener<String, Key> listener);
	void addKeyListener(IEntryListener<String, Key> listener);

	void setCacheManager(ICacheManager cacheManager);

	boolean deployApi(String apiId, byte[] jarData);

	boolean undeployApi(String apiId);

	void addApiDeploymentListener(IEntryListener<String, ApiJar> listener);

	void removeApiDeploymentListener(IEntryListener<String, ApiJar> listener);

	void addLogLevelListener(IEntryListener<String, LogLevel> listener);	
	void removeLogLevelListener(IEntryListener<String, LogLevel> listener);
	void setLogLevel(LogLevel logLevel);
	LogLevel getLogLevel();
	void setSMXLogLevel(LogLevel logLevel);
	LogLevel getSMXLogLevel();
	void setSyslogLevel(LogLevel logLevel);
	LogLevel getSyslogLevel();
	void setLoggingCategory(Category category, boolean enabled);
	public boolean getLoggingCategory(Category category);

	void reloadGateway(String ip);

	void addCA(Certificate cert);
	Set<String> getAllCA();
	Certificate getCAById(String id);
	void updateCA(Certificate cert);
	void removeCA(String id);
	void addCAListener(IEntryListener<String, Certificate> listener);
	void removeCAListener(IEntryListener<String, Certificate> listener);

	void addCRL(com.alu.e3.data.model.SSLCRL clr);
	com.alu.e3.data.model.SSLCRL getCRLById(String id);
	void updateCRL(com.alu.e3.data.model.SSLCRL crl);
	void removeCRL(String id);
	Set<String> getAllCRL();
	Collection<SSLCRL> getAllCRLValues();
	void addCrlListener(IEntryListener<String, SSLCRL> listener);
	void removeCrlListener(IEntryListener<String, SSLCRL> listener);

	boolean isApiExist(String apiId);
	ApiJar getApiJar(String apiId);
	
	void addApiListener(IEntryListener<String, Api> listener);
	void removeApiListener(IEntryListener<String, Api> listener);
	boolean isIpAllowed(Api api, String ip);

	// Global Proxy settings
	void removeGlobalProxyListener3(IEntryListener<String, String> listener);
	void addGlobalProxyListener3(IEntryListener<String, String> listener);
}
