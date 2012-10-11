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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Level;

import com.alu.e3.common.E3Constant;
import com.alu.e3.common.InvalidIDException;
import com.alu.e3.common.NullHazelcastTableException;
import com.alu.e3.common.caching.ICacheManager;
import com.alu.e3.common.caching.ICacheTable;
import com.alu.e3.common.caching.IEntryListener;
import com.alu.e3.common.caching.internal.HandlerPool;
import com.alu.e3.common.caching.internal.MapHandler;
import com.alu.e3.common.info.GatewayStatus;
import com.alu.e3.common.info.RemoteInstanceInfo;
import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.common.osgi.api.ITopologyClient;
import com.alu.e3.common.tools.ExpiringSet;
import com.alu.e3.common.tools.WsseTools;
import com.alu.e3.data.model.Api;
import com.alu.e3.data.model.ApiDetail;
import com.alu.e3.data.model.ApiJar;
import com.alu.e3.data.model.Auth;
import com.alu.e3.data.model.AuthDetail;
import com.alu.e3.data.model.CallDescriptor;
import com.alu.e3.data.model.Certificate;
import com.alu.e3.data.model.CertificateDetail;
import com.alu.e3.data.model.CertificateRequest;
import com.alu.e3.data.model.Instance;
import com.alu.e3.data.model.Key;
import com.alu.e3.data.model.KeyDetail;
import com.alu.e3.data.model.Limit;
import com.alu.e3.data.model.LogLevel;
import com.alu.e3.data.model.Policy;
import com.alu.e3.data.model.SSLCRL;
import com.alu.e3.data.model.enumeration.NBAuthType;
import com.alu.e3.data.model.enumeration.StatusType;
import com.alu.e3.data.model.sub.APIContext;
import com.alu.e3.data.model.sub.ApiIds;
import com.alu.e3.data.model.sub.AuthIds;
import com.alu.e3.data.model.sub.Context;
import com.alu.e3.data.model.sub.ContextWrapper;
import com.alu.e3.data.model.sub.Counter;
import com.alu.e3.data.model.sub.QuotaRLBucket;
import com.alu.e3.data.topology.IInstanceListener;
import com.alu.e3.data.topology.InstanceEvent;

public class DataManager implements IDataManager, IInstanceListener {

	private static final CategoryLogger logger = CategoryLoggerFactory.getLogger(DataManager.class, Category.DMGR);

	// Is tables already initialized or not.
	private boolean isHazelCastTablesInitialized = false;

	// On SystemManager only
	private ICacheTable<String, ApiDetail> cachingTableApiDetails;
	private ICacheTable<String, AuthDetail> cachingTableAuthDetails;
	private ICacheTable<String, String> cachingTableAuthIdToAuthToken;
	private ICacheTable<String, KeyDetail> cachingTableKeyDetails;
	private ICacheTable<String, CertificateDetail> cachingTableCertificateDetails;
	private ICacheTable<String, CertificateDetail> cachingTableCADetails;

	// On SystemManager only, recreated at startup

	// => At startup, check all policies and all buckets, recreate Set<K>
	private Set<Integer> usedBucketIds = new HashSet<Integer>();
	private Set<String> usedEndpoints = new HashSet<String>();

	// On Gateway and SystemManager
	private ICacheTable<String, Api> cachingTableApi;
	private ICacheTable<String, Auth> cachingTableAuth;
	private ICacheTable<String, String> cachingTableAuthIpAddress;
	private ICacheTable<String, Policy> cachingTablePolicy;
	private ICacheTable<Integer, ContextWrapper> cachingTableContext;
	private ICacheTable<String, String> cachingTableSettings;
	private ICacheTable<String, Key> cachingTableKey;
	private ICacheTable<String, Certificate> cachingTableCertificate;
	private ICacheTable<String, LogLevel> cachingTableLogLevel;
	private ICacheTable<String, Certificate> cachingTableCA;
	private ICacheTable<String, SSLCRL> cachingTableCRL;

	private ICacheTable<String, ApiJar> cachingTableApiJars; // table with acknowledgement service

	private ICacheManager cacheManager;
	private ITopologyClient topologyClient;

	protected boolean manager;

	private ReentrantLock dataManagerReadyLock = new ReentrantLock();

	// DataManagerListeners
	private Set<IDataManagerListener> listeners = new HashSet<IDataManagerListener>();
	private Set<IDataManagerUsedBucketIdsListener> usedBucketIdslisteners = new HashSet<IDataManagerUsedBucketIdsListener>();

	protected HandlerPool<String, MapHandler<String, ApiJar>> mapHandlerPool = new HandlerPool<String, MapHandler<String, ApiJar>>(E3Constant.HAZELCAST_HANDLER_POOL_MAX_SIZE);


	public DataManager() {

		/* Create fake table objects. */
		cachingTableApiDetails = new SanityCheckCacheTable<String, ApiDetail>("cachingTableApiDetails");
		cachingTableAuthDetails  = new SanityCheckCacheTable<String, AuthDetail>("cachingTableAuthDetails");
		cachingTableAuthIdToAuthToken = new SanityCheckCacheTable<String, String>("cachingTableAuthIdToAuthToken");
		cachingTableKeyDetails = new SanityCheckCacheTable<String, KeyDetail>("cachingTableKeyDetails");
		cachingTableCertificateDetails = new SanityCheckCacheTable<String, CertificateDetail>("cachingTableCertificateDetails");
		cachingTableCADetails = new SanityCheckCacheTable<String, CertificateDetail>("cachingTableCADetails");

		cachingTableApi = new SanityCheckCacheTable<String, Api>("cachingTableApi");
		cachingTableAuth = new SanityCheckCacheTable<String, Auth>("cachingTableAuth");
		cachingTableAuthIpAddress = new SanityCheckCacheTable<String, String>("cachingTableAuthIpAddress");
		cachingTablePolicy = new SanityCheckCacheTable<String, Policy>("cachingTablePolicy");
		cachingTableContext = new SanityCheckCacheTable<Integer, ContextWrapper>("cachingTableContext");
		cachingTableSettings = new SanityCheckCacheTable<String, String>("cachingTableSettings");
		cachingTableKey = new SanityCheckCacheTable<String, Key>("cachingTableKey");
		cachingTableCertificate = new SanityCheckCacheTable<String, Certificate>("cachingTableCertificate");
		cachingTableLogLevel = new SanityCheckCacheTable<String, LogLevel>("cachingTableLogLevel");
		cachingTableCA = new SanityCheckCacheTable<String, Certificate>("cachingTableCA");
		cachingTableCRL = new SanityCheckCacheTable<String, SSLCRL>("cachingTableCRL");

		cachingTableApiJars = new SanityCheckCacheTable<String, ApiJar>("cachingTableApiJars");
	}

	@Override
	public void addListener(IDataManagerListener listener) {

		dataManagerReadyLock.lock();

		try {
			this.listeners.add(listener);

			// assume that the DataManager is already ready at bean instantiation when we aren't a manager...
			if(isHazelCastTablesInitialized || !isManager()) {
				listener.dataManagerReady();
			}
		}
		finally {
			dataManagerReadyLock.unlock();
		}

	}

	@Override
	public void removeListener(IDataManagerListener listener) {
		this.listeners.remove(listener);
	}	


	@Override
	public void addListener(IDataManagerUsedBucketIdsListener listener) {
		this.usedBucketIdslisteners.add(listener);
	}


	@Override
	public void removeListener(IDataManagerUsedBucketIdsListener listener) {
		this.usedBucketIdslisteners.remove(listener);
	}

	private void populateBucketAndAPIIds() {

		for (ContextWrapper context : cachingTableContext.getAllValues()) {
			APIContext apiContext = context.getApiContext();

			if (apiContext != null) {
				usedBucketIds.add(apiContext.getBucketId());
			}
		}

		for (Policy policy : cachingTablePolicy.getAllValues()) {
			for (QuotaRLBucket bucket : policy.getAuthIds()) {
				usedBucketIds.add(bucket.getBucketId());
			}
		}

		for (Api api: cachingTableApi.getAllValues()) {
			usedBucketIds.add(api.getApiId());
		}

	}

	private void populateUsedEndpoints() {
		for(ApiDetail ad : cachingTableApiDetails.getAllValues()){
			usedEndpoints.add(ad.getEndpoint());
		}
	}

	public void setManager(boolean manager) {
		this.manager = manager;
	}

	private boolean isManager() {
		return manager;
	}

	public void init() {
		if (isManager()) {
			// register to know when the IP of DataStorage is changing
			this.topologyClient.addInstanceListener(this);

			createDataStoreTables();
		} else {
			// on the gateway, there is no data store and no replication
			setCachingTableApiDetails(cacheManager.createTable("cachingTableApiDetails", false, null));
			setCachingTableAuthDetails(cacheManager.createTable("cachingTableAuthDetails", false, null));
			setCachingTableAuthIdToAuthToken(cacheManager.createTable("cachingTableAuthIdToAuthToken", false, null));
			setCachingTableKeyDetails(cacheManager.createTable("cachingTableKeyDetails", false, null));
			setCachingTableCertificateDetails(cacheManager.createTable("cachingTableCertificateDetails", false, null));
			setCachingTableCADetails(cacheManager.createTable("cachingTableCADetails", false, null));

			setCachingTableApi(cacheManager.createTable("cachingTableApi", false, null));
			setCachingTableAuth(cacheManager.createTable("cachingTableAuth", false, null));
			setCachingTableAuthIpAddress(cacheManager.createTable("cachingTableAuthIpAddress", false, null));
			setCachingTablePolicy(cacheManager.createTable("cachingTablePolicy", false, null));
			setCachingTableKey(cacheManager.createTable("cachingTableKey", false, null));
			setCachingTableCertificate(cacheManager.createTable("cachingTableCertificate", false, null));
			setCachingTableCA(cacheManager.createTable("cachingTableCA", false, null));
			setCachingTableCRL(cacheManager.createTable("cachingTableCRL", false, null));

			setCachingTableContext(cacheManager.createTable("cachingTableContext", false, null));
			setCachingTableSettings(cacheManager.createTable("cachingTableSettings", false, null));

			setCachingTableApiJars(cacheManager.createTable("cachingTableApiJars", false, null));
			setCachingTableLogLevel(cacheManager.createTable("cachingTableLogLevel", false, null));

			isHazelCastTablesInitialized = true;

			// Inform listeners for DataManager Ready
			fireDataManagerReady();
		}
	}

	public void destroy() {
		if (this.topologyClient != null) {
			this.topologyClient.removeInstanceListener(this);
		}
	}

	private void fireDataManagerReady() {
		dataManagerReadyLock.lock();

		try {
			// do it on a copy of the listener in case of the listener list is modified during the loop
			IDataManagerListener[] currentListOfListeners = listeners.toArray(new IDataManagerListener[]{});

			for(IDataManagerListener listener : currentListOfListeners) {
				listener.dataManagerReady();
			}
		}
		finally {
			dataManagerReadyLock.unlock();
		}
	}

	private synchronized void createDataStoreTables() {

		// protect for multiple instantiation
		if (!isHazelCastTablesInitialized) {

			// create the tables if the IP of DataStorage is known
			List<Instance> dsInfoList = topologyClient.getAllInstancesOfType(E3Constant.DATA_STORAGE);
			if (dsInfoList != null && dsInfoList.size() > 0) {

				if(logger.isDebugEnabled()){
					logger.debug("DataManager initializing cache tables");
				}

				Instance dsInfo = dsInfoList.get(0);

				Map<String, String> properties = new HashMap<String, String>();

				properties.put("map-store-ip", dsInfo.getInternalIP());
				properties.put("map-store-port", dsInfo.getPort());
				properties.put("map-store-user", dsInfo.getUser());
				properties.put("map-store-password", dsInfo.getPassword());

				properties.put("map-store-name", "apiDetails");
				setCachingTableApiDetails(cacheManager.createTable("cachingTableApiDetails", false, properties));
				properties.put("map-store-name", "authDetails");
				setCachingTableAuthDetails(cacheManager.createTable("cachingTableAuthDetails", false, properties));
				properties.put("map-store-name", "authIdToAuthToken");
				setCachingTableAuthIdToAuthToken(cacheManager.createTable("cachingTableAuthIdToAuthToken", false, properties));
				properties.put("map-store-name", "keyDetails");
				setCachingTableKeyDetails(cacheManager.createTable("cachingTableKeyDetails", false, properties));
				properties.put("map-store-name", "certificateDetails");
				setCachingTableCertificateDetails(cacheManager.createTable("cachingTableCertificateDetails", false, properties));
				properties.put("map-store-name", "caDetails");
				setCachingTableCADetails(cacheManager.createTable("cachingTableCADetails", false, properties));

				properties.put("map-store-name", "api");
				setCachingTableApi(cacheManager.createTable("cachingTableApi", true, properties));
				properties.put("map-store-name", "auth");
				setCachingTableAuth(cacheManager.createTable("cachingTableAuth", true, properties));
				properties.put("map-store-name", "authIpAddress");
				setCachingTableAuthIpAddress(cacheManager.createTable("cachingTableAuthIpAddress", true, properties));
				properties.put("map-store-name", "policy");
				setCachingTablePolicy(cacheManager.createTable("cachingTablePolicy", true, properties));
				properties.put("map-store-name", "key");
				setCachingTableKey(cacheManager.createTable("cachingTableKey", true, properties));
				properties.put("map-store-name", "certificate");
				setCachingTableCertificate(cacheManager.createTable("cachingTableCertificate", true, properties));
				properties.put("map-store-name", "ca");
				setCachingTableCA(cacheManager.createTable("cachingTableCA", true, properties));
				properties.put("map-store-name", "crl");
				setCachingTableCRL(cacheManager.createTable("cachingTableCRL", true, properties));

				properties.put("map-store-name", "context");
				setCachingTableContext(cacheManager.createTable("cachingTableContext", true, properties));
				setCachingTableSettings(cacheManager.createTable("cachingTableSettings", true, null));

				properties.put("map-store-name", "jars");
				setCachingTableApiJars(cacheManager.createTable("cachingTableApiJars", true, properties));

				properties.put("map-store-name", "logLevel");
				setCachingTableLogLevel(cacheManager.createTable("cachingTableLogLevel", true, properties));

				populateBucketAndAPIIds();
				populateUsedEndpoints();

				isHazelCastTablesInitialized = true;

				// Inform listeners for DataManager Ready
				fireDataManagerReady();

				// no need to be notified on the topology again
				this.topologyClient.removeInstanceListener(this);
				// TODO: Clean 
				//this.topologyClient = null;
			}
		}
	}

	@Override
	public void setCacheManager(ICacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Override
	public void reloadGateway(String ip) {
		// Launch the provisioning of a gateway in a dedicated thread
		(new Thread (new RunnableProvisionGateway(ip))).start();
	}

	public void setTopologyClient(ITopologyClient topologyClient) {
		this.topologyClient = topologyClient;
	}

	@Override
	public Set<String> getAllApiIds() {		
		HashSet<String> ks = new HashSet<String>();

		ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(DataManager.class.getClassLoader());

		for (Api api : cachingTableApi.getAllValues()) {
			if (!api.getInternal()) {
				ks.add(api.getId());
			}
		}

		Thread.currentThread().setContextClassLoader(previousClassLoader);

		return ks;
	}

	@Override
	public boolean isApiExist (String apiId) {
		return cachingTableApi.containsKey(apiId);
	}

	@Override
	public void addApi(Api api) {

		if (cachingTableApi.containsKey(api.getId()))
			throw new IllegalArgumentException("API already exists with that ID [" + api.getId() + "]");

		internalSaveApiDetails(api);

		boolean ok = false;

		while (!ok) {
			int idx = (int)(Math.random() * Integer.MAX_VALUE);
			Integer apiId = Integer.valueOf(idx);
			if (!usedBucketIds.contains(apiId)) {
				api.setApiId(apiId);
				usedBucketIds.add(apiId);
				ok = true;
			}
		}

		cachingTableApi.set(api.getId(), api);
		usedEndpoints.add(api.getApiDetail().getEndpoint());
	}

	@Override
	public boolean endpointExists(String endpoint){
		return usedEndpoints.contains(endpoint);
	}

	private void internalSaveApiDetails(Api api)
	{
		/* if no details, nothing to do. */
		if (api.getApiDetail() == null)
			return;

		api.setStatus(api.getApiDetail().getStatus());

		cachingTableApiDetails.set(api.getId(), api.getApiDetail());

		// Store all contexts aside, indexed by an int
		for (APIContext ctx : api.getApiDetail().getContexts()) {
			boolean ok = false;

			while (!ok) {
				int idx = (int)(Math.random() * Integer.MAX_VALUE);
				Integer contextId = Integer.valueOf(idx);
				if (!cachingTableContext.containsKey(contextId)) {
					ctx.setContextId(contextId);

					// API context should also be seen as a Bucket
					boolean ok2 = false;
					while (!ok2) {
						int idx2 = (int)(Math.random() * Integer.MAX_VALUE);
						Integer bucketId = Integer.valueOf(idx2);
						if (!usedBucketIds.contains(bucketId)) {
							ctx.setBucketId(bucketId);
							usedBucketIds.add(bucketId);
							ok2 = true;
						}
					}

					cachingTableContext.set(contextId, new ContextWrapper(ctx));
					// If this APIContext is the default one, adding it at index 0 ; this is the place where we're
					// looking for it in getMatchingPolicies
					ApiIds apiIds = new ApiIds(ctx.getId(), ctx.getContextId(), ctx.getBucketId(), ctx.getStatus()==StatusType.ACTIVE);
					if(ctx.isDefaultContext())
						api.getContextIds().add(0, apiIds);
					else
						api.getContextIds().add(apiIds);

					ok = true;
				}
			}
		}
	}

	@Override
	public void updateApi(Api api) throws InvalidIDException {

		Api oldApi = cachingTableApi.get(api.getId());
		if (oldApi == null)
			throw new InvalidIDException("An API with that ID [" + api.getId() + "] doesn't exist");

		// Remove all contexts and bucket ids
		for (ApiIds apiIds : oldApi.getContextIds()) {
			cachingTableContext.remove(Integer.valueOf(apiIds.getApiContextId()));
			usedBucketIds.remove(Integer.valueOf(apiIds.getApiBucketId()));
			for (IDataManagerUsedBucketIdsListener listner : this.usedBucketIdslisteners)
			{
				listner.usedBucketIdsRemoved(new Integer(apiIds.getApiBucketId()));
			}
		}

		// Remove endpoint from the used endpoints
		ApiDetail oldDetail = cachingTableApiDetails.get(api.getId());
		usedEndpoints.remove(oldDetail.getEndpoint());

		// add updated contexts
		internalSaveApiDetails(api);

		api.setPolicyIds(oldApi.getPolicyIds());

		cachingTableApi.set(api.getId(), api);
		usedEndpoints.add(api.getApiDetail().getEndpoint());
	}

	@Override
	public void removeApi(String id) throws InvalidIDException {
		Api api = cachingTableApi.remove(id);

		ApiDetail detail = cachingTableApiDetails.remove(id);

		if (api == null) {
			throw new InvalidIDException("An API with that ID [" + id + "] doesn't exist");
		}

		// Find the policies associated, and remove this API
		for (String policyId : api.getPolicyIds()) {
			Policy policy = getPolicyById(policyId);
			if (policy != null) {
				policy.getApiIds().remove(api.getId());
				cachingTablePolicy.set(policyId, policy);
			}
		}

		// Remove the contexts
		for (ApiIds apiIds : api.getContextIds()) {
			cachingTableContext.remove(Integer.valueOf(apiIds.getApiContextId()));
			usedBucketIds.remove(new Integer(apiIds.getApiBucketId()));
			for (IDataManagerUsedBucketIdsListener listner : this.usedBucketIdslisteners)
			{
				listner.usedBucketIdsRemoved(new Integer(apiIds.getApiBucketId()));
			}
		}
		// Remove endpoint from the used endpoints
		usedEndpoints.remove(detail.getEndpoint());

		usedBucketIds.remove(api.getApiId());
		for (IDataManagerUsedBucketIdsListener listner : this.usedBucketIdslisteners)
		{
			listner.usedBucketIdsRemoved(new Integer(api.getApiId()));
		}
	}

	@Override
	public Api getApiById(String id) {
		return getApiById(id, false);
	}


	@Override
	public Api getApiById(String id, boolean getFullDetails) throws InvalidIDException {

		Api api = cachingTableApi.get(id);

		if (api == null) {
			throw new InvalidIDException("An API with that ID [" + id + "] doesn't exist");
		}

		if (getFullDetails) {

			ApiDetail details = cachingTableApiDetails.get(id);

			if (details != null) {

				api.setApiDetail(details);
				// It's not because List<Context> is transient on ApiDetail
				// that it isn't stored in hazelcast memory locally.
				// For sure, it will not be replicated
				details.getContexts().clear();
				// We can't be sure here to reuse existing List<APIContext>
				// it may be not up to date.

				for (ApiIds ids : api.getContextIds()) {
					details.getContexts().add(cachingTableContext.get(ids.getApiContextId()).getApiContext());
				}
			}
		}
		else
			api.setApiDetail(null);

		return api;
	}

	private String byteToString(byte[] data) {

		String result = null;

		if (data != null) {
			result = new String(data).toString();
		} else {
			result = new String();
		}

		return result;
	}
	
	/**
	 * Uses SHA1 algorithm to hash authId.
	 * @param auth
	 * @return The hexa conversion of SHA-1(auth)
	 */
	private String hashAuthId(String auth){
		return DigestUtils.shaHex(auth);
	}

	private String createTokenFromAuth(Auth auth) {
		if (auth.getAuthDetail() == null)
			return null;

		if (auth.getAuthDetail().getType() == NBAuthType.AUTHKEY)
			return createTokenFromAuthKey(auth.getAuthDetail().getAuthKeyValue());
		if (auth.getAuthDetail().getType() == NBAuthType.OAUTH)
			return createTokenFromOAuth(auth.getAuthDetail().getClientId(), auth.getAuthDetail().getClientSecret());
		if (auth.getAuthDetail().getType() == NBAuthType.BASIC)
			return createTokenFromUserPass(auth.getAuthDetail().getUsername(), byteToString(auth.getAuthDetail().getPassword()));
		if (auth.getAuthDetail().getType() == NBAuthType.WSSE)
			return createWsseTokenFromUser(auth.getAuthDetail().getUsername());
		if (auth.getAuthDetail().getType() == NBAuthType.IP_WHITE_LIST)
			return auth.getId();

		throw new IllegalArgumentException("Unknown authentication type [" + auth.getAuthDetail().getType() + "]");
	}

	private String createTokenFromAuthKey(String authKey) {
		return authKey;
	}

	private String createTokenFromOAuth(String clientId, String clientSecret) {
		return clientId + ";"/* + clientSecret*/;
	}

	private String createTokenFromUserPass(String username, String password) {
		// Encrypt
		return username + ":" + hashAuthId(password);
	}

	private String createWsseTokenFromUser(String username) {
		return username + ":";
	}

	@Override
	public Set<String> getAllAuthIds() {
		return cachingTableAuthIdToAuthToken.getAllKeys();
	}

	@Override
	public void addAuth(Auth auth) {

		if (cachingTableAuthIdToAuthToken.containsKey(auth.getId()))
			throw new IllegalArgumentException("Auth already exists with that ID [" + auth.getId() + "]");

		String authToken = createTokenFromAuth(auth);

		Auth auth2 = cachingTableAuth.get(authToken);
		if (auth2 != null)
			throw new IllegalArgumentException("An authorization with the same credentials already exist");

		// For each IP address, check if the IP already exist
		if (auth.getAuthDetail() != null)  {

			if (auth.getAuthDetail().getType() == NBAuthType.IP_WHITE_LIST) {
				for (String ip : auth.getAuthDetail().getWhiteListedIps()) {
					if (cachingTableAuthIpAddress.containsKey(ip))
						throw new IllegalArgumentException("An authorization with the same IP address already exist");
				}
			}
		}

		cachingTableAuthIdToAuthToken.set(auth.getId(), authToken);
		cachingTableAuth.set(authToken, auth);

		if (auth.getAuthDetail() != null) {
			cachingTableAuthDetails.set(authToken, auth.getAuthDetail());

			if (auth.getAuthDetail().getType() == NBAuthType.IP_WHITE_LIST) {
				for (String ip : auth.getAuthDetail().getWhiteListedIps()) {
					cachingTableAuthIpAddress.set(ip, authToken);
				}
			}
		}
	}

	@Override
	public void updateAuth(Auth auth) throws InvalidIDException {

		// Get old Auth
		String oldAuthToken = cachingTableAuthIdToAuthToken.get(auth.getId());
		if (oldAuthToken == null)
			throw new InvalidIDException("An authorization with that ID [" + auth.getId() + "] doesn't exist");

		// For each IP address, check if the IP already exist
		if (auth.getAuthDetail() != null)  {

			if (auth.getAuthDetail().getType() == NBAuthType.IP_WHITE_LIST) {
				for (String ip : auth.getAuthDetail().getWhiteListedIps()) {
					String authToken = cachingTableAuthIpAddress.get(ip);
					if ((authToken != null) && (authToken.equals(oldAuthToken) == false))
						throw new IllegalArgumentException("An authorization with the same IP address already exist");
				}
			}
		}

		AuthDetail authDetail = cachingTableAuthDetails.get(oldAuthToken);
		if (authDetail != null) {

			// Remove all IPs from the table, and add the new one
			if (authDetail.getType() == NBAuthType.IP_WHITE_LIST) {
				for (String ip : authDetail.getWhiteListedIps()) {
					cachingTableAuthIpAddress.remove(ip);
				}
			}
		}

		String authToken = createTokenFromAuth(auth);
		Auth auth2;

		// Check that the token has changed or not
		if (oldAuthToken.equals(authToken) == false) {

			// Check that new token doesn't exist
			auth2 = cachingTableAuth.get(authToken);
			if (auth2 != null)
				throw new IllegalArgumentException("An authorization with the same credentials already exist");

			// If token has changed, update association table
			cachingTableAuthIdToAuthToken.set(auth.getId(), authToken);

			// Remove old auth
			auth2 = cachingTableAuth.remove(oldAuthToken);

			// Remove old details
			cachingTableAuthDetails.remove(oldAuthToken);

		} else {

			auth2 = cachingTableAuth.get(authToken);
			if (auth2 == null)
				throw new IllegalArgumentException("An Authorization with that token doesn't exist");

		}

		auth.setPolicyContexts(auth2.getPolicyContexts());

		if (authDetail != null) {

			// Remove all IPs from the table, and add the new one
			if (authDetail.getType() == NBAuthType.IP_WHITE_LIST) {
				for (String ip : authDetail.getWhiteListedIps()) {
					cachingTableAuthIpAddress.remove(ip);
				}
			}
		}

		// Update the status
		for (AuthIds authId : auth.getPolicyContexts()) {
			authId.setStatusActive(auth.getStatus().isActive());
			authId.setPolicyContextId(getPolicyContextId(auth, getPolicyById(authId.getPolicyId())));
		}

		cachingTableAuth.set(authToken, auth);

		if (auth.getAuthDetail() != null) {
			cachingTableAuthDetails.set(authToken, auth.getAuthDetail());

			if (auth.getAuthDetail().getType() == NBAuthType.IP_WHITE_LIST) {
				for (String ip : auth.getAuthDetail().getWhiteListedIps()) {
					cachingTableAuthIpAddress.set(ip, authToken);
				}
			}
		}
	}

	@Override
	public void removeAuth(String id) throws InvalidIDException {
		String authToken = cachingTableAuthIdToAuthToken.get(id);
		if (authToken == null)
			throw new InvalidIDException("An Authorization with that ID [" + id + "] doesn't exist");

		cachingTableAuthIdToAuthToken.remove(id);
		Auth auth = cachingTableAuth.remove(authToken);
		AuthDetail authDetail = cachingTableAuthDetails.remove(authToken);

		if (auth == null) {
			throw new InvalidIDException("An Authorization with that token doesn't exist");
		}

		if (authDetail != null) {
			if (authDetail.getType() == NBAuthType.IP_WHITE_LIST) {
				for (String ip : authDetail.getWhiteListedIps()) {
					cachingTableAuthIpAddress.remove(ip);
				}
			}
		}

		// Find the policies associated, and remove this Auth
		for (AuthIds authCtx : auth.getPolicyContexts()) {
			String policyId = authCtx.getPolicyId();

			Policy policy = getPolicyById(policyId);
			if (policy != null) {
				for(QuotaRLBucket authIds : policy.getAuthIds()) {
					authIds.getAuthIds().remove(auth.getId());
					cachingTablePolicy.set(policyId, policy);
				}
			}
		}
	}

	@Override
	public Auth getAuthById(String id) {
		return getAuthById(id, false);
	}

	@Override
	public Auth getAuthById(String id, boolean getFullDetails) throws InvalidIDException {

		String authToken = cachingTableAuthIdToAuthToken.get(id);
		if (authToken == null)
			throw new InvalidIDException("An Authorization with that ID [" + id + "] doesn't exist");

		return getAuthByToken(authToken, getFullDetails);
	}

	private Auth getAuthByToken(String authToken, boolean getFullDetails) {

		Auth auth = cachingTableAuth.get(authToken);
		if (auth == null)
			return null;

		if (getFullDetails)
			auth.setAuthDetail(cachingTableAuthDetails.get(authToken));
		else
			auth.setAuthDetail(null);

		return auth;
	}

	@Override
	public Auth getAuthByAuthKey(String authKey) {
		if(authKey == null) return null;
		else return getAuthByToken(createTokenFromAuthKey(authKey), false);
	}

	@Override
	public Auth getAuthByOAuth(String clientId, String clientSecret) {
		if(clientId == null || clientSecret == null) return null;
		else return getAuthByToken(createTokenFromOAuth(clientId, clientSecret), false);
	}

	@Override
	public Auth getAuthByUserPass(String username, String password) {
		if(username == null || password == null) return null;
		else return getAuthByToken(createTokenFromUserPass(username, password), false);
	}

	private static final int WSSE_AGE_SECONDS = 5 * 60;

	private ExpiringSet<String> liveNonces = new ExpiringSet<String>(WSSE_AGE_SECONDS + 30);   // 30 seconds extra

	@Override
	public Auth getWsseAuth(String username, String password, boolean isPasswordText, String nonce, String created) {
		if(username == null || password == null) {
			return null;
		}
		Auth auth = getAuthByToken(createWsseTokenFromUser(username), false);
		if (auth != null) {
			
			// PasswordText
			if(isPasswordText) {
				String storedPassword = new String(auth.getWssePassword());
				if(storedPassword.equals(password)) {
					return auth;
				} else {
					return null;
				}
				
			// Digest password
			} else {
				if (created != null) {
					long then;
					try {
						then = WsseTools.getCreatedDate(created).getTime();
					} catch (ParseException e) {
						if (logger.isDebugEnabled()) {
							logger.debug("invalid WSSE: created parse error: " + created);
						}
						return null;
					}
					long now = System.currentTimeMillis();
					if (then > now + (WSSE_AGE_SECONDS * 1000)) {   // allow client to be up to 5 minutes in the future
						if (logger.isDebugEnabled()) {
							logger.debug("invalid WSSE: created in the future: " + created);
						}
						return null;
					} else if (then < now - (WSSE_AGE_SECONDS * 1000)) {
						if (logger.isDebugEnabled()) {
							logger.debug("invalid WSSE: old created: " + created);
						}
						return null;
					}
				}
				if (WsseTools.isValid(password, nonce, created, auth.getWssePassword())) {
					if (nonce != null && liveNonces.contains(nonce)) {
						if (logger.isDebugEnabled()) {
							logger.debug("invalid WSSE: familiar nonce: " + nonce + " (created " + created + ")");
						}
						return null;
					}
					if (logger.isDebugEnabled()) {
						logger.debug("valid WSSE");
					}
					return auth;
				}
				if (logger.isDebugEnabled()) {
					logger.debug("invalid WSSE: digest validation failed");
				}
			}
		}
		return null;
	}

	@Override
	public Auth getAuthByIP(String ip) {

		String authToken = cachingTableAuthIpAddress.get(ip);
		if (authToken == null)
			return null;

		return getAuthByToken(authToken, false);
	}

	@Override
	public Auth getAuthMatching(IAuthMatcher authMatcher) {

		Auth auth = null;

		ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();

		Thread.currentThread().setContextClassLoader(DataManager.class.getClassLoader());

		for (Auth authFound : cachingTableAuth.getAllValues()) {

			if(authMatcher.isAuth(authFound)) {
				auth = authFound;
				break;
			}
		}

		Thread.currentThread().setContextClassLoader(previousClassLoader);

		return auth;
	}


	@Override
	public Set<String> getAllPolicy() {
		return cachingTablePolicy.getAllKeys();
	}

	@Override
	public void addPolicy(Policy policy) {

		if (cachingTablePolicy.containsKey(policy.getId()))
			throw new IllegalArgumentException("Policy already exists with that ID [" + policy.getId() + "]");

		updatePolicy(policy, false);
	}

	private int getPolicyContextId(Auth auth, Policy policy) {
		if (logger.isDebugEnabled()) {
			logger.debug("getPolicyContextId({}, {})", auth.getId(), policy.getId());
		}
		
		for (Context ctx : policy.getContexts()) {
			if ((ctx.getId() != null) && (ctx.getId().equals(auth.getPolicyContext()))) {
				if (logger.isDebugEnabled()) {
					logger.debug("Found a policy context matching auth's policyContext: {}", auth.getPolicyContext());
				}
				return ctx.getContextId();
			}
		}

		for (Context ctx : policy.getContexts()) {
			// * is the default context to be used when context specified by auth cannot be found
			if ((ctx.getId() != null ) && ctx.getId().equals("*")) {
				if (logger.isDebugEnabled()) {
					logger.debug("No explicit policy context matching but there is a default context, using it");
				}
				return ctx.getContextId();
			}
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("getPolicyContextId didn't find in policy any default or matching context withauth's policyContext {}", auth.getPolicyContext());
		}

		return -1;
	}

	@Override
	public void updatePolicy(Policy policy) {
		updatePolicy(policy, true);
	}

	/**
	 * 
	 * @param policy
	 * @param update specifies if the policy is a new one or replace an existing one ; used to determine if we should check for existence or not.
	 */
	private void updatePolicy(Policy policy, boolean update) throws InvalidIDException {

		// First, check that the apis exist
		for (String apiId : policy.getApiIds()) {
			if (! isApiExist(apiId)) {
				throw new InvalidIDException("An API with that ID [" + apiId + "] doesn't exist");
			}
		}

		// Then, check that the auths exist
		for (QuotaRLBucket bucket : policy.getAuthIds()) {
			for(String authId : bucket.getAuthIds()) {
				String authToken = cachingTableAuthIdToAuthToken.get(authId);
				if (authToken == null)
					throw new IllegalArgumentException("Auth ID [" + authId + "] not found");

				Auth auth = cachingTableAuth.get(authToken);
				if (auth == null)
					throw new IllegalArgumentException("Auth token not found");
			}
		}

		for (QuotaRLBucket bucket : policy.getAuthIds()) {
			checkIfAuthAlreadyInPolicy(policy.getId(), bucket.getAuthIds(), bucket.getId());
		}

		Policy cachedPolicy = null;

		if(update) {
			cachedPolicy = getPolicyById(policy.getId(), false);

			// Remove existing contexts
			for (Integer contextId : cachedPolicy.getContextIds()) {
				cachingTableContext.remove(contextId);
			}
		}

		policy.getContextIds().clear();

		// TODO: Check value of quota, if changed, then reset the created date, otherwise, keep old value
		//=> Speaker needs to store the created date

		// Store all contexts aside, indexed by an int
		for (Context ctx : policy.getContexts()) {
			boolean ok = false;

			while (!ok) {
				int idx = (int)(Math.random() * Integer.MAX_VALUE);
				Integer contextId = Integer.valueOf(idx);
				if (!cachingTableContext.containsKey(contextId)) {
					ctx.setContextId(contextId);
					ctx.setCreatedDate(new Date());
					cachingTableContext.set(contextId, new ContextWrapper(ctx));
					policy.getContextIds().add(contextId);
					ok = true;
				}
			}
		}

		// Store all contexts aside, indexed by an int
		for (QuotaRLBucket bucket : policy.getAuthIds()) {
			boolean ok = false;

			if ((cachedPolicy != null) && ((bucket.getId() == null) || (bucket.getId().equals("")))) {
				// First, try to find the current bucket in the old policy
				for (QuotaRLBucket cachedBucket : cachedPolicy.getAuthIds()) {
					if (cachedBucket.getId().equals(bucket.getId())) {
						// Bucket was already in old policy
						bucket.setBucketId(cachedBucket.getBucketId());
						cachedPolicy.getAuthIds().remove(cachedBucket);
						ok = true;
						break;
					}
				}
			}

			while (!ok) {
				int idx = (int)(Math.random() * Integer.MAX_VALUE);
				Integer bucketId = Integer.valueOf(idx);
				if (!usedBucketIds.contains(bucketId)) {
					bucket.setBucketId(bucketId);
					usedBucketIds.add(bucketId);

					if ((bucket.getId() == null) || (bucket.getId().equals(""))) {
						bucket.setId(bucketId.toString());
					}

					ok = true;
				}
			}
		}

		if (cachedPolicy != null) {
			// Now remove all buckets which are not in the new policy
			for (QuotaRLBucket cachedBucket : cachedPolicy.getAuthIds()) {
				usedBucketIds.remove(cachedBucket.getBucketId());
				for (IDataManagerUsedBucketIdsListener listner : this.usedBucketIdslisteners)
				{
					listner.usedBucketIdsRemoved(cachedBucket.getBucketId());
				}
			}
		}

		cachingTablePolicy.set(policy.getId(), policy);

		// Now, iterate through all APIs and Auth in this policy and update them
		for (String apiId : policy.getApiIds()) {
			Api api = getApiById(apiId);
			if (api.getPolicyIds().indexOf(policy.getId()) < 0)
			{
				api.getPolicyIds().add(policy.getId());
				cachingTableApi.set(api.getId(), api);
			}
		}

		for (QuotaRLBucket bucket : policy.getAuthIds()) {
			for(String authId : bucket.getAuthIds()) {
				String authToken = cachingTableAuthIdToAuthToken.get(authId);
				if (authToken == null)
					throw new IllegalArgumentException("Auth ID [" + authId + "] not found");

				Auth auth = cachingTableAuth.get(authToken);
				if (auth == null)
					throw new IllegalArgumentException("Auth token not found");

				//Add policy: add a AuthContext object with PolicyId, LimitId, BucketId (instead of just PolicyId) => LimitId should be crossed with "policyContext" on Auth

				boolean found = false;
				for (AuthIds authCtx : auth.getPolicyContexts()) {
					if (authCtx.getPolicyId().equals(policy.getId())) { 
						//update Auth with good Policy context and bucket
						authCtx.setPolicyContextId(getPolicyContextId(auth, policy));
						authCtx.setPolicyBucketId(bucket.getBucketId());
						cachingTableAuth.set(authToken, auth);//updating auth
						found = true;
						break;
					}
				}

				if (!found) {
					auth.getPolicyContexts().add(new AuthIds(policy.getId(), bucket.getId(), getPolicyContextId(auth, policy), bucket.getBucketId(), auth.getStatus().isActive()));
					cachingTableAuth.set(authToken, auth);
				}
			}
		}
	}

	@Override
	public void removePolicy(String id) throws InvalidIDException {

		Policy policy = cachingTablePolicy.remove(id);
		if (policy == null)
			throw new InvalidIDException("A Policy with that ID [" + id + "] doesn't exist");

		// Now, iterate through all APIs and Auth in this policy and update them
		for (String apiId : policy.getApiIds()) {
			Api api = getApiById(apiId);
			if (api != null) {
				api.getPolicyIds().remove(policy.getId());
				cachingTableApi.set(api.getId(), api);
			}
		}

		for (QuotaRLBucket bucket : policy.getAuthIds()) {
			for(String authId : bucket.getAuthIds()) {
				String authToken = cachingTableAuthIdToAuthToken.get(authId);
				if (authToken == null)
					continue;

				Auth auth = cachingTableAuth.get(authToken);
				if (auth == null)
					continue;

				for (AuthIds authCtx : auth.getPolicyContexts()) {
					if (authCtx.getPolicyId().equals(policy.getId())) {
						auth.getPolicyContexts().remove(authCtx);
						break;
					}
				}
				cachingTableAuth.set(authToken, auth);
			}
			usedBucketIds.remove(bucket.getBucketId());
			for (IDataManagerUsedBucketIdsListener listner : this.usedBucketIdslisteners)
			{
				listner.usedBucketIdsRemoved(bucket.getBucketId());
			}
		}

		// Remove the contexts
		for (Integer contextId : policy.getContextIds()) {
			cachingTableContext.remove(contextId);
		}
	}

	@Override
	public Policy getPolicyById(String id) {
		return getPolicyById(id, true);
	}

	@Override
	public Policy getPolicyById(String id, boolean getFullDetails) throws InvalidIDException {

		Policy policy = cachingTablePolicy.get(id);
		if (policy == null)
			throw new InvalidIDException("A Policy with that ID [" + id + "] doesn't exist");

		if (getFullDetails) {
			// It's not because List<Context> is transient on Policy
			// that it is not stored in hazelcast memory _locally_.
			// For sure, it will not be replicated
			policy.getContexts().clear();
			// We can't be sure here to reuse existing List<Context>
			// it may be not up to date.
			for (Integer contextId : policy.getContextIds()) {
				policy.getContexts().add(cachingTableContext.get(contextId).getPolicyContext());
			}
		}

		return policy;
	}


	@Override
	public void createBucket(String policyId, QuotaRLBucket bucket) throws IllegalArgumentException {

		Policy policy = getPolicyById(policyId);

		if(getBucketWithIdForPolicy(policy, bucket.getId()) != null) {
			throw new IllegalArgumentException("A Bucket with this ID already exists for this policy");
		}

		policy.getAuthIds().add(bucket);
		cachingTablePolicy.set(policy.getId(), policy);

		checkIfAuthAlreadyInPolicy(policyId, bucket.getAuthIds(), bucket.getId());

		for(String authId : bucket.getAuthIds()) {
			String authToken = cachingTableAuthIdToAuthToken.get(authId);
			if (authToken == null)
				throw new IllegalArgumentException("Auth ID [" + authId + "] not found");

			Auth auth = cachingTableAuth.get(authToken);
			if (auth == null)
				throw new IllegalArgumentException("Auth token not found");

			// Add auth to policy
			if (bucket.getAuthIds().indexOf(auth.getId()) < 0) {
				bucket.getAuthIds().add(auth.getId());
				cachingTablePolicy.set(policy.getId(), policy);
			}

			// Add policy to auth
			boolean found = false;
			for (AuthIds authCtx : auth.getPolicyContexts()) {
				if (authCtx.getPolicyId().equals(policy.getId())) {
					//update Auth with good Policy context and bucket
					authCtx.setPolicyContextId(getPolicyContextId(auth, policy));
					authCtx.setPolicyBucketId(bucket.getBucketId());
					cachingTableAuth.set(authToken, auth);//updating auth
					found = true;
					break;
				}
			}

			if (!found) {
				auth.getPolicyContexts().add(new AuthIds(policyId, bucket.getId(), getPolicyContextId(auth, policy), bucket.getBucketId(), auth.getStatus().isActive()));
				cachingTableAuth.set(authToken, auth);
			}
		}
	}

	private void checkIfAuthAlreadyInPolicy(String policyId, List<String> authIds, String bucketId)
	{
		for(String authId : authIds) {

			String authToken = cachingTableAuthIdToAuthToken.get(authId);
			if (authToken == null)
				throw new IllegalArgumentException("Auth ID not found");

			Auth auth = cachingTableAuth.get(authToken);
			if (auth == null)
				throw new IllegalArgumentException("Auth token not found");

			boolean found = false;
			for (AuthIds authCtx : auth.getPolicyContexts()) {
				if (authCtx.getPolicyId().equals(policyId)) {
					if(!authCtx.getBucketId().equals(bucketId)){
						// Auth should not exist in another bucket 
						found = true;
					}
					break;
				}
			}

			if (found) {
				throw new IllegalArgumentException("Auth " + authId +" already used on this policy by another bucket");
			}
		}
	}

	@Override
	public void addAuthsToBucket(String policyId, String bucketId, QuotaRLBucket bucket) throws IllegalArgumentException {

		Policy policy = getPolicyById(policyId);

		// Find the correct bucket in this policy
		QuotaRLBucket pAuthIds = getBucketWithIdForPolicy(policy, bucketId);
		if(pAuthIds == null) {
			throw new IllegalArgumentException("A Bucket with that ID [" + bucketId + "] doesn't exist for this Policy [" + policyId + "]");
		}

		checkIfAuthAlreadyInPolicy(policyId, bucket.getAuthIds(), bucketId);

		for(String authId : bucket.getAuthIds()) {

			String authToken = cachingTableAuthIdToAuthToken.get(authId);
			if (authToken == null)
				throw new IllegalArgumentException("Auth ID [" + authId + "] not found");

			Auth auth = cachingTableAuth.get(authToken);
			if (auth == null)
				throw new IllegalArgumentException("Auth token not found");

			// Add auth to the bucket
			if (pAuthIds.getAuthIds().indexOf(auth.getId()) < 0)
			{
				pAuthIds.getAuthIds().add(auth.getId());
				cachingTablePolicy.set(policy.getId(), policy);
			}

			// Add policy to auth
			boolean found = false;
			for (AuthIds authCtx : auth.getPolicyContexts()) {
				if (authCtx.getPolicyId().equals(policy.getId())) {
					//update Auth with good Policy context and bucket
					authCtx.setPolicyContextId(getPolicyContextId(auth, policy));
					authCtx.setPolicyBucketId(bucket.getBucketId());
					cachingTableAuth.set(authToken, auth);//updating auth
					found = true;
					break;
				}
			}

			if (!found) {
				auth.getPolicyContexts().add(new AuthIds(policyId, bucketId, getPolicyContextId(auth, policy), pAuthIds.getBucketId(), auth.getStatus().isActive()));
				cachingTableAuth.set(authToken, auth);
			}
		}
	}

	@Override
	public void addAuthsToBucket(List<String> policyIds, String bucketId, QuotaRLBucket authIds) throws IllegalArgumentException {
		//cacheManager.beginTransaction();

		for(String policyId : policyIds){
			createBucket(policyId, authIds);
		}
		//cacheManager.endTransaction();

	}

	@Override
	public void appendAuthsToBucket(List<String> policyIds, String bucketId, QuotaRLBucket authIds) throws IllegalArgumentException {
		//cacheManager.beginTransaction();

		for(String policyId : policyIds){
			addAuthsToBucket(policyId, bucketId, authIds);
		}
		//cacheManager.endTransaction();

	}

	@Override
	public void removeAuthFromBucket(String policyId, String bucketId, String authId) throws IllegalArgumentException {

		Policy policy = getPolicyById(policyId);

		String authToken = cachingTableAuthIdToAuthToken.get(authId);
		if (authToken == null)
			throw new InvalidIDException("An Authorization with that ID [" + authId + "] doesn't exist");

		Auth auth = cachingTableAuth.get(authToken);
		if (auth == null)
			throw new InvalidIDException("An Authorization with that token doesn't exist");

		QuotaRLBucket pAuthIds = getBucketWithIdForPolicy(policy, bucketId);
		if(pAuthIds == null) {
			throw new InvalidIDException("A Bucket with that ID [" + bucketId + "] doesn't exist for this Policy [" + policyId + "]");
		}

		// Remove auth from the bucket
		pAuthIds.getAuthIds().remove(auth.getId());
		cachingTablePolicy.set(policy.getId(), policy);

		// Remove policy from auth
		for (AuthIds authCtx : auth.getPolicyContexts()) {
			if (authCtx.getPolicyId().equals(policy.getId())) {
				auth.getPolicyContexts().remove(authCtx);
				cachingTableAuth.set(authToken, auth);
				break;
			}
		}
	}

	@Override
	public void removeAuthsFromBucket(List<String> policyIds, String bucketId, List<String> authIds) throws IllegalArgumentException {
		for(String authId : authIds){
			for(String policyId : policyIds){
				removeAuthFromBucket(policyId, bucketId, authId);
			}
		}
	}

	@Override
	public void removeBucket(String policyId, String bucketId) throws IllegalArgumentException {

		Policy policy = getPolicyById(policyId);

		QuotaRLBucket pAuthIds = getBucketWithIdForPolicy(policy, bucketId);
		if(pAuthIds == null) {
			throw new InvalidIDException("A Bucket with that ID [" + bucketId + "] doesn't exist for this Policy [" + policyId + "]");
		}

		// Iterating over Auth Ids in the bucket to remove policy from the auth
		for(String authId : pAuthIds.getAuthIds()) {

			String authToken = cachingTableAuthIdToAuthToken.get(authId);
			if (authToken == null)
				continue;

			Auth auth = cachingTableAuth.get(authToken);
			if (auth == null)
				continue;

			// Remove policy from auth
			for (AuthIds authCtx : auth.getPolicyContexts()) {
				if (authCtx.getPolicyId().equals(policy.getId())) {
					auth.getPolicyContexts().remove(authCtx);
					cachingTableAuth.set(authToken, auth);
					break;
				}
			}
		}

		policy.getAuthIds().remove(pAuthIds);
		cachingTablePolicy.set(policy.getId(), policy);
	}

	@Override
	public void removeBucket(List<String> policyIds, String bucketId) throws IllegalArgumentException {
		//cacheManager.beginTransaction();

		for(String policyId : policyIds){
			removeBucket(policyId, bucketId);
		}
		//cacheManager.endTransaction();

	}

	/**
	 * Utility method that iterate over policy's buckets to find a bucket for a given bucketId.
	 * @param policy Policy to iterate on
	 * @param bucketId Id of the bucket to get
	 * @return The corresponding bucket (AuthIds object)
	 */
	private QuotaRLBucket getBucketWithIdForPolicy(Policy policy, String bucketId) {
		QuotaRLBucket pAuthIds = null;
		for(QuotaRLBucket cAuthIds : policy.getAuthIds()) {
			if(cAuthIds.getId().equalsIgnoreCase(bucketId)) {
				pAuthIds = cAuthIds;
				break;
			}
		}
		return pAuthIds;
	}

	@Override
	public List<CallDescriptor> getMatchingPolicies(Api api) {
		return getMatchingPolicies(api, null);
	}

	@Override
	public List<CallDescriptor> getMatchingPolicies(Api api, Auth auth) {

		if (logger.isDebugEnabled()) {
			logger.debug("getMatchingPolicies({}, {})", 
					api != null ? api.getId() : null, 
					auth != null ? auth.getId() : null
					);
		}
		
		List<CallDescriptor> result = null;

		// If no Auth, check if we have an API
		if (auth == null) {

			// No Auth and no API ? What are we doing here ?!
			if (api != null) {
				
				// No Auth, but an API: return all Policies with no Auth associated
				// => API doesn't have any authentification method
				for (String policyIdInApi : api.getPolicyIds()) {

					Policy policy = getPolicyById(policyIdInApi);

					// Add only if no auth in policy's buckets
					boolean add = true;
					for(QuotaRLBucket authIds : policy.getAuthIds()) {
						if (!authIds.getAuthIds().isEmpty()) {
							add = false;
						}
					}
					if(add) {
						if (logger.isDebugEnabled()) {
							logger.debug("Adding policy {} as it does not have any auth in it's contexts", policy.getId());
						}
						if (result == null)
							result = new ArrayList<CallDescriptor>();

						int policyIdx = -1;
						if (policy != null && policy.getContextIds().size() > 0) {
							policyIdx = policy.getContextIds().get(0);
						}
						
						result.add(new CallDescriptor(policy, policyIdx , -1));

					}
				}

				// Now, add the CallDescriptor of the "default" context
				if (api.getContextIds().isEmpty() == false) {
					if (result == null)
						result = new ArrayList<CallDescriptor>();

					// Looking for default context, placed at index 0 by addApi
					ApiIds ctx = api.getContextIds().get(0);
					if (ctx.isStatusActive()) {
						if (logger.isDebugEnabled()) {
							logger.debug("Adding CallDescriptor({}, {}, {})", new String[] {
									null, 
									""+ctx.getApiContextId(), 
									""+ctx.getApiBucketId()
									});
						}
						result.add(new CallDescriptor(null, ctx.getApiContextId(), ctx.getApiBucketId()));
					}
				}
			}
		} else {

			// We have an Auth and an API, check matching policies
			for (AuthIds authCtx : auth.getPolicyContexts()) {

				String policyIdInAuth = authCtx.getPolicyId();

				boolean policyAdded = false;

				// API may be null if Auth is for "Company"
				if (api != null) {
					for (String policyIdInApi : api.getPolicyIds()) {

						// Matching means that the policy is in Auth and API
						if (policyIdInApi.equals(policyIdInAuth)) {
							Policy policy = getPolicyById(policyIdInAuth);
							if (result == null)
								result = new ArrayList<CallDescriptor>();
							if (authCtx.isStatusActive()) {
								if (logger.isDebugEnabled()) {
									logger.debug("Adding CallDescriptor({}, {}, {})", new String[] {
											policy.getId(), 
											""+authCtx.getPolicyContextId(), 
											""+authCtx.getPolicyBucketId()
											});
								}
								result.add(new CallDescriptor(policy, authCtx.getPolicyContextId(), authCtx.getPolicyBucketId()));
							}
							policyAdded = true;
						}
					}
				}

				// If the policy wasn't added...
				if (!policyAdded) {
					// Slow, but need to check if that policy has some API attached
					Policy policy = getPolicyById(policyIdInAuth, false);
					if ((policy.getApiIds() == null) || policy.getApiIds().isEmpty()) {
						if (result == null)
							result = new ArrayList<CallDescriptor>();
						if (authCtx.isStatusActive()) {
							result.add(new CallDescriptor(policy, authCtx.getPolicyContextId(), authCtx.getPolicyBucketId()));
						}
					}
				}
			}

			// Now check the apiContext on the auth which is in the list of API
			if (api != null) {
				for (ApiIds ctx : api.getContextIds()) {
					if (ctx.getApiContextName().equals(auth.getApiContext())) {
						if (result == null)
							result = new ArrayList<CallDescriptor>();
						if (ctx.isStatusActive()) {
							if (logger.isDebugEnabled()) {
								logger.debug("Adding CallDescriptor({}, {}, {})", new String[] {
										null, 
										""+ctx.getApiContextId(), 
										""+ctx.getApiBucketId()
										});
							}
							result.add(new CallDescriptor(null, ctx.getApiContextId(), ctx.getApiBucketId()));
						}
					}
				}
			}
		}

		return result;
	}

	@Override
	public APIContext getApiContextById(Integer id) {

		ContextWrapper ctx = cachingTableContext.get(id);

		if (ctx == null)
			throw new IllegalArgumentException("There is no Context available with that ID");

		return ctx.getApiContext();
	}

	@Override
	public Context getPolicyContextById(Integer id) {

		ContextWrapper ctx = cachingTableContext.get(id);

		if (ctx == null)
			throw new IllegalArgumentException("There is no Context available with that ID");

		return ctx.getPolicyContext();
	}

	@Override
	public void fillLimitsById(Integer contextId, Limit limit) {

		if (limit == null)
			throw new IllegalArgumentException("Limit must not be null");

		ContextWrapper ctx = cachingTableContext.get(contextId);

		if (ctx == null)
			throw new IllegalArgumentException("There is no limit available with that ID");

		if (ctx.getApiContext() != null) {
			// It's an API Context
			limit.setQuotaPerDay(null);
			limit.setQuotaPerWeek(null);
			limit.setQuotaPerMonth(null);
			limit.setRateLimitPerMinute(new Counter(ctx.getApiContext().getMaxRateLimitTPMWarning(), ctx.getApiContext().getMaxRateLimitTPMThreshold()));
			limit.setRateLimitPerSecond(new Counter(ctx.getApiContext().getMaxRateLimitTPSWarning(), ctx.getApiContext().getMaxRateLimitTPSThreshold()));
		}

		if (ctx.getPolicyContext() != null) {
			// It's a Policy Context
			limit.setQuotaPerDay(ctx.getPolicyContext().getQuotaPerDay());
			limit.setQuotaPerWeek(ctx.getPolicyContext().getQuotaPerWeek());
			limit.setQuotaPerMonth(ctx.getPolicyContext().getQuotaPerMonth());
			limit.setRateLimitPerMinute(ctx.getPolicyContext().getRateLimitPerMinute());
			limit.setRateLimitPerSecond(ctx.getPolicyContext().getRateLimitPerSecond());
			limit.setCreatedDate(ctx.getPolicyContext().getCreatedDate());
		}
	}

	@Override
	public void putSettingString(String key, String value) {
		cachingTableSettings.set(key, value);
	}

	@Override
	public void clearSettingString(String key) {
		cachingTableSettings.remove(key);
	}

	@Override
	public String getSettingString(String key) {
		return cachingTableSettings.get(key);
	}

	@Override
	public void addKey(Key key){
		if (cachingTableKey.containsKey(key.getId()))
			throw new IllegalArgumentException("Key already exists with that ID [" + key.getId() + "]");

		if( key.getData() == null || key.getData().isEmpty() )
			throw new IllegalArgumentException("New Keys must have key data");

		cachingTableKeyDetails.set(key.getId(), key.getKeyDetail());

		cachingTableKey.set(key.getId(), key);

	}

	@Override
	public Set<String> getAllKeyIds() {
		return cachingTableKey.getAllKeys();
	}

	@Override
	public Key getKeyById(String id) {
		return getKeyById(id, false);
	}

	@Override
	public Key getKeyById(String id, boolean getFullDetails) throws InvalidIDException {

		if( ! cachingTableKey.containsKey(id))
			throw new InvalidIDException("A Key with that ID [" + id + "] doesn't exist");

		Key key = cachingTableKey.get(id);

		if (getFullDetails) 
			key.setKeyDetail(cachingTableKeyDetails.get(id));
		else
			key.setKeyDetail(null);

		return key;
	}

	@Override
	public void updateKey(Key key) throws InvalidIDException {

		if( ! cachingTableKey.containsKey(key.getId()))
			throw new InvalidIDException("A Key with that ID [" + key.getId() + "] doesn't exist");

		if( key.getData() != null && !key.getData().isEmpty() )
			throw new IllegalArgumentException("Key data cannot be changed with an update");

		Key knownKey = getKeyById(key.getId(), true);
		KeyDetail knownKeyDetail = knownKey.getKeyDetail();

		if(key.getActiveCertId() != null){
			knownKey.setActiveCertId(key.getActiveCertId());
		}
		
		knownKey.setKeyPassphrase(key.getKeyPassphrase());

		if(key.getKeyDetail().getName() != null){
			knownKeyDetail.setName(key.getKeyDetail().getName());
		}

		boolean success = cachingTableKey.set(key.getId(), knownKey);
		
		if (!success) {
			throw new IllegalArgumentException("Error while updating the key");			
		}
		
		cachingTableKeyDetails.set(key.getId(), knownKeyDetail);
	}

	@Override
	public void removeKey(String id) throws InvalidIDException {

		if( ! cachingTableKey.containsKey(id))
			throw new InvalidIDException("A Key with that ID [" + id + "] doesn't exist");

		ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(DataManager.class.getClassLoader());

		// Remove all certificates for this key
		for(CertificateDetail cd : cachingTableCertificateDetails.getAllValues()){
			if(cd.getKeyId().equals(id)){
				cachingTableCertificate.remove(cd.getId());
				cachingTableCertificateDetails.remove(cd.getId());
			}
		}

		Thread.currentThread().setContextClassLoader(previousClassLoader);

		// Remove the key
		cachingTableKey.remove(id);
		cachingTableKeyDetails.remove(id);
	}

	@Override
	public void addCert(Certificate cert){
		if(cachingTableCertificate.containsKey(cert.getId()))
			throw new IllegalArgumentException("Cert already exists with that ID [" + cert.getId() + "]");

		if( cert.getData() == null || cert.getData().isEmpty() )
			throw new IllegalArgumentException("New Certificates must have data");

		// Invalid key
		if( !cachingTableKey.containsKey(cert.getCertDetail().getKeyId())){
			throw new IllegalArgumentException("The Key with that ID [" + cert.getCertDetail().getKeyId() + "] does not exist");
		}

		cachingTableCertificateDetails.set(cert.getId(), cert.getCertDetail());
		cachingTableCertificate.set(cert.getId(), cert);
	}

	@Override
	public Set<String> getAllCertIds(){
		return cachingTableCertificate.getAllKeys();
	}

	@Override
	public Set<String> getAllCertIdsForKeyId(String keyId){

		ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(DataManager.class.getClassLoader());

		Set<String> result = new HashSet<String>();
		for(CertificateDetail cd : cachingTableCertificateDetails.getAllValues()){
			if(cd.getKeyId().equals(keyId)){
				result.add(cd.getId());
			}
		}

		Thread.currentThread().setContextClassLoader(previousClassLoader);

		return result;
	}

	@Override
	public Certificate getCertById(String id){
		return getCertById(id, false);
	}

	@Override
	public Certificate getCertById(String id, boolean getFullDetails) throws InvalidIDException {
		if(!cachingTableCertificate.containsKey(id))
			throw new InvalidIDException("A Certificate with that ID [" + id + "] doesn't exist");
		Certificate cert = cachingTableCertificate.get(id);	

		if(getFullDetails)
			cert.setCertDetail(cachingTableCertificateDetails.get(id));
		else
			cert.setCertDetail(null);

		return cert;
	}

	@Override
	public void updateCert(Certificate cert) throws InvalidIDException {
		if( ! cachingTableCertificate.containsKey(cert.getId()))
			throw new InvalidIDException("A Certificate with that ID [" + cert.getId() + "] doesn't exist");

		if( cert.getData() != null && !cert.getData().isEmpty() )
			throw new IllegalArgumentException("Certificate data cannot be changed with an update");


		Certificate knownCert = getCertById(cert.getId(), true);
		CertificateDetail knownCertDetail = knownCert.getCertDetail();

		if(cert.getCertDetail().getName() != null){
			knownCertDetail.setName(cert.getCertDetail().getName());
		}

		if(cert.getCertDetail().getKeyId() != null){
			if( !cachingTableKey.containsKey(cert.getCertDetail().getKeyId())){
				throw new IllegalArgumentException("The Key with that ID [" + cert.getCertDetail().getKeyId() + "] does not exist");
			}
			knownCertDetail.setKeyId(cert.getCertDetail().getKeyId());
		}

		cachingTableCertificate.set(cert.getId(), knownCert);
		cachingTableCertificateDetails.set(cert.getId(), knownCertDetail);
	}

	@Override
	public void removeCert(String id) throws InvalidIDException {
		if( ! cachingTableCertificate.containsKey(id))
			throw new InvalidIDException("A Certificate with that ID [" + id + "] doesn't exist");

		// If the cert is active, disallow removal
		if( cachingTableKey.get(cachingTableCertificateDetails.get(id).getKeyId()).getActiveCertId().equals(id))
			throw new IllegalArgumentException("Cannot remove active certificate");

		cachingTableCertificate.remove(id);
		cachingTableCertificateDetails.remove(id);
	}

	/**
	 * Generate a new Key with BouncyCastle
	 */
	@Override
	public Key generateKey(String id) {
		//TODO implement bouncycastle key generation
		return null;
	}

	/**
	 * Generate a CSR with bouncycastle
	 */
	@Override
	public CertificateRequest getCSR(String keyId){
		//TODO: implement bouncycastle csr generation
		return null;
	}

	/**
	 * Self-Sign a key with bouncycastle
	 */
	@Override
	public Certificate selfSignKey(String keyId, CertificateRequest csr){
		//TODO: implement bouncycastle self-signing
		return null;
	}

	@Override
	public void addKeyListener(IEntryListener<String, Key> listener) {
		cachingTableKey.addEntryListener(listener);
	}

	@Override
	public void removeKeyListener(IEntryListener<String, Key> listener) {
		cachingTableKey.removeEntryListener(listener);
	}

	public void setCachedLogLevel(String key, LogLevel level) {
		cachingTableLogLevel.set(key, level);
	}

	public void clearCachedLogLevel(String key) {
		cachingTableLogLevel.remove(key);
	}

	public LogLevel getCachedLogLevel(String key) {
		return cachingTableLogLevel.get(key);
	}

	@Override
	public void addLogLevelListener(IEntryListener<String, LogLevel> listener) {
		cachingTableLogLevel.addEntryListener(listener);
	}

	@Override
	public void removeLogLevelListener(IEntryListener<String, LogLevel> listener) {
		cachingTableLogLevel.removeEntryListener(listener);
	}

	public void setCachedLoggingCategory(Category category, boolean enabled) {
		// Use log4j level OFF to indicated disabled category, ALL for enabled
		Level level = enabled ? Level.ALL : Level.OFF;
		cachingTableLogLevel.set(category.toString(), new LogLevel(level));
	}

	public void clearCachedLoggingCategory(Category category) {
		cachingTableLogLevel.remove(category.toString());
	}

	public boolean getCachedLoggingCategory(Category category) {
		LogLevel level = cachingTableLogLevel.get(category.toString());
		if ((level != null) && level.equals(Level.OFF)) {
			return false;
		}
		return true;
	}


	//----------------------------------------------------------------
	// *** Route deployment ***
	// 
	// Gateways are listening on cachingTableApiJars map to be 
	// notified of bundle updates through cache mechanism
	//----------------------------------------------------------------
	@Override
	public boolean deployApi(String apiId, byte[] jarData) {
		ApiJar data = new ApiJar();
		data.setId(apiId);
		data.setData(jarData);
		boolean result = cachingTableApiJars.set(apiId, data);
		return result;
	}

	@Override
	public ApiJar getApiJar(String apiId) {		
		return cachingTableApiJars.get(apiId);
	}

	@Override
	public boolean undeployApi(String apiId) {
		ApiJar undeployed = cachingTableApiJars.remove(apiId);
		return undeployed != null;
	}

	@Override
	public void addApiDeploymentListener(IEntryListener<String, ApiJar> listener) {
		cachingTableApiJars.addEntryListener(listener);
	}

	@Override
	public void removeApiDeploymentListener(IEntryListener<String, ApiJar> listener) {
		cachingTableApiJars.removeEntryListener(listener);
	}

	@Override
	public void instanceAdded(InstanceEvent event) {
		if (!isManager()) return;

		if (E3Constant.DATA_STORAGE.equals(event.getType())) {
			// on the manager, we need replication and data store
			// Trying to create them all
			// if condition are met
			createDataStoreTables();
		}
	}

	@Override
	public void instanceRemoved(InstanceEvent event) {
		// Nothing to do about data management on instance removed.
	}

	@Override
	public void setLogLevel(LogLevel level) {
		this.setCachedLogLevel(LogLevel.logLevelKey, level);
	}

	@Override
	public LogLevel getLogLevel() {
		return this.getCachedLogLevel(LogLevel.logLevelKey);
	}

	@Override
	public void setSMXLogLevel(LogLevel level) {
		this.setCachedLogLevel(LogLevel.smxlogLevelKey, level);
	}

	@Override
	public LogLevel getSMXLogLevel() {
		return this.getCachedLogLevel(LogLevel.smxlogLevelKey);
	}

	@Override
	public void setSyslogLevel(LogLevel level) {
		this.setCachedLogLevel(LogLevel.syslogLevelKey, level);
	}

	@Override
	public LogLevel getSyslogLevel() {
		return this.getCachedLogLevel(LogLevel.syslogLevelKey);
	}

	@Override
	public void setLoggingCategory(Category category, boolean enabled) {
		this.setCachedLoggingCategory(category, enabled);
	}
	
	@Override
	public boolean getLoggingCategory(Category category) {
		Boolean enabled = this.getCachedLoggingCategory(category);
		return enabled != null ? enabled.booleanValue() : true;
	}
	
	public class RunnableProvisionGateway implements Runnable {

		protected String ip;

		public RunnableProvisionGateway(String ip) {
			super();
			this.ip = ip;
		}

		@Override
		public void run() {
			logger.info("Running ProvisionGateway(ip:{}) ...", ip);
			RemoteInstanceInfo.setGatewayStatus(ip, GatewayStatus.PROVISIONING);
			
			logger.debug("Reloading topology to ip:{} ...", ip);
			topologyClient.reloadInstanceTopology(ip);
			
			logger.debug("Loading CA/CRL for ip:{} ...", ip);
			cachingTableCA.reloadSlave(ip);
			cachingTableCRL.reloadSlave(ip);

			// TODO: Clean
			// This table is not flagged as 'replicated'
			//logger.debug("Loading ApiDetails for ip:{} ...", ip);
			//cachingTableApiDetails.reloadSlave(ip);
			
			// Missing tables flagged as 'replicated'
			// Becareful, load Certificate -before- Keys
			logger.debug("Loading Certificate for ip:{} ...", ip);
			cachingTableCertificate.reloadSlave(ip);
			
			logger.debug("Loading Key for ip:{} ...", ip);
			cachingTableKey.reloadSlave(ip);

			logger.debug("Loading LogLevel for ip:{} ...", ip);
			cachingTableLogLevel.reloadSlave(ip);

			// TODO: Clean
			// This table is not flagged as 'replicated'
			//logger.debug("Loading AuthDetails for ip:{} ...", ip);
			//cachingTableAuthDetails.reloadSlave(ip);
			
			// TODO: Clean
			// This table is not flagged as 'replicated'
			//logger.debug("Loading AuthIdToAuthToken for ip:{} ...", ip);
			//cachingTableAuthIdToAuthToken.reloadSlave(ip);
			
			logger.debug("Loading Auth for ip:{} ...", ip);
			cachingTableAuth.reloadSlave(ip);
			
			logger.debug("Loading AuthIpAddress for ip:{} ...", ip);
			cachingTableAuthIpAddress.reloadSlave(ip);
			
			logger.debug("Loading Policy for ip:{} ...", ip);
			cachingTablePolicy.reloadSlave(ip);

			logger.debug("Loading Context for ip:{} ...", ip);
			cachingTableContext.reloadSlave(ip);

			logger.debug("Loading Api for ip:{} ...", ip);
			cachingTableApi.reloadSlave(ip);
			
			logger.debug("Loading Settings for ip:{} ...", ip);
			cachingTableSettings.reloadSlave(ip);
			
			// All the routes were previously removed from the gateway
			for (ApiJar apiJar: cachingTableApiJars.getAllValues()) {
				logger.debug("Deploying api:{} for ip:{} ...", apiJar.getId(), ip);
				reDeployApi(ip, apiJar);
			}

			// Notify the gateway that its tables were all provisioned
			RemoteInstanceInfo.setGatewayStatus(ip, GatewayStatus.PROVISIONED);
			logger.info("ProvisionGateway(ip:{}) done.", ip);
		}

		protected boolean reDeployApi(String gatewayIP, ApiJar apiJar) {

			if (apiJar == null) {
				logger.warn("deploying a jar - parameter apiJar is null");
				return false;
			}

			return cachingTableApiJars.set(apiJar.getId(), apiJar, gatewayIP);
		}

	}

	@Override
	public Set<String> getAllCA() {
		return cachingTableCA.getAllKeys();
	}

	@Override
	public void addCA(Certificate cert) {
		if(cachingTableCA.containsKey(cert.getId()))
			throw new IllegalArgumentException("CA already exists with that ID [" + cert.getId() + "]");

		if( cert.getData() == null || cert.getData().isEmpty() )
			throw new IllegalArgumentException("New CA must have data");

		cachingTableCADetails.set(cert.getId(), cert.getCertDetail());
		cachingTableCA.set(cert.getId(), cert);
	}

	@Override
	public Certificate getCAById(String id) throws InvalidIDException {
		if(!cachingTableCA.containsKey(id))
			throw new InvalidIDException("A CA with that ID [" + id + "] doesn't exist");
		Certificate cert = cachingTableCA.get(id);	
		cert.setCertDetail(cachingTableCADetails.get(id));

		return cert;
	}

	@Override
	public void updateCA(Certificate cert) throws InvalidIDException {
		if( ! cachingTableCA.containsKey(cert.getId()))
			throw new InvalidIDException("A CA with that ID [" + cert.getId() + "] doesn't exist");

		if( cert.getData() != null && !cert.getData().isEmpty() )
			throw new IllegalArgumentException("CA data cannot be changed with an update");


		Certificate knownCert = getCAById(cert.getId());
		CertificateDetail knownCertDetail = knownCert.getCertDetail();

		if(cert.getCertDetail().getName() != null){
			knownCertDetail.setName(cert.getCertDetail().getName());
		}

		cachingTableCA.set(cert.getId(), knownCert);
		cachingTableCADetails.set(cert.getId(), knownCertDetail);
	}

	@Override
	public void removeCA(String id) throws InvalidIDException {
		if( ! cachingTableCA.containsKey(id))
			throw new InvalidIDException("A CA with that ID [" + id + "] doesn't exist");

		cachingTableCA.remove(id);
		cachingTableCADetails.remove(id);
	}

	@Override
	public void addCAListener(IEntryListener<String, Certificate> listener) {
		cachingTableCA.addEntryListener(listener);
	}

	@Override
	public void removeCAListener(IEntryListener<String, Certificate> listener) {
		cachingTableCA.removeEntryListener(listener);
	}

	@Override
	public void addCRL(SSLCRL clr) {
		if(cachingTableCRL.containsKey(clr.getId()))
			throw new IllegalArgumentException("CRL already exists with that ID [" + clr.getId() + "]");

		if( clr.getContent() == null || clr.getContent().length() == 0)
			throw new IllegalArgumentException("New CRL must have data");

		cachingTableCRL.set(clr.getId(), clr);
	}

	@Override
	public SSLCRL getCRLById(String id) throws InvalidIDException {
		if(!cachingTableCRL.containsKey(id))
			throw new InvalidIDException("A CRL with that ID [" + id + "] doesn't exist");
		SSLCRL crl = cachingTableCRL.get(id);	

		return crl;
	}

	@Override
	public void updateCRL(SSLCRL crl) throws InvalidIDException {
		SSLCRL knownCRL = getCRLById(crl.getId());

		if( crl.getContent() != null && crl.getContent().length() > 0)
			throw new IllegalArgumentException("CRL data cannot be changed with an update");

		if(crl.getDisplayName() != null){
			knownCRL.setDisplayName(crl.getDisplayName());
		}

		cachingTableCRL.set(crl.getId(), knownCRL);
	}

	@Override
	public void removeCRL(String id) throws InvalidIDException {
		if( ! cachingTableCRL.containsKey(id))
			throw new InvalidIDException("A CRL with that ID [" + id + "] doesn't exist");

		cachingTableCRL.remove(id);
	}

	@Override
	public Collection<SSLCRL> getAllCRLValues() {
		return cachingTableCRL.getAllValues();
	}

	@Override
	public Set<String> getAllCRL() {
		return cachingTableCRL.getAllKeys();
	}

	@Override
	public void addCrlListener(IEntryListener<String,SSLCRL> listener) {
		cachingTableCRL.addEntryListener(listener);
	}

	@Override
	public void removeCrlListener(IEntryListener<String,SSLCRL> listener) {
		cachingTableCRL.removeEntryListener(listener);
	}


	/**
	 * Public Setters
	 */

	@SuppressWarnings("unchecked")
	public <K, V> void setCachingTableApiDetails(ICacheTable<K, V> iCacheTable) {
		if (iCacheTable == null)
			throw new RuntimeException("Error: table cachingTableApiDetails already created.");
		cachingTableApiDetails = (ICacheTable<String, ApiDetail>) iCacheTable;
	}

	@SuppressWarnings("unchecked")
	public <K, V> void setCachingTableAuthDetails(ICacheTable<K, V> iCacheTable) {
		if (iCacheTable == null)
			throw new RuntimeException("Error: table cachingTableAuthDetails already created.");
		this.cachingTableAuthDetails = (ICacheTable<String, AuthDetail>) iCacheTable;
	}

	@SuppressWarnings("unchecked")
	public <K, V> void setCachingTableAuthIdToAuthToken(ICacheTable<K, V> iCacheTable) {
		if (iCacheTable == null)
			throw new RuntimeException("Error: table cachingTableAuthIdToAuthToken already created.");
		this.cachingTableAuthIdToAuthToken = (ICacheTable<String, String>) iCacheTable;
	}

	@SuppressWarnings("unchecked")
	public <K, V> void setCachingTableKeyDetails(ICacheTable<K, V> iCacheTable) {
		if (iCacheTable == null)
			throw new RuntimeException("Error: table cachingTableKeyDetails already created.");
		this.cachingTableKeyDetails = (ICacheTable<String, KeyDetail>) iCacheTable;
	}

	@SuppressWarnings("unchecked")
	public <K, V> void setCachingTableCertificateDetails(ICacheTable<K, V> iCacheTable) {
		if (iCacheTable == null)
			throw new RuntimeException("Error: table cachingTableCertificateDetails already created.");
		this.cachingTableCertificateDetails = (ICacheTable<String, CertificateDetail>) iCacheTable;
	}

	@SuppressWarnings("unchecked")
	public <K, V> void setCachingTableCADetails(ICacheTable<K, V> iCacheTable) {
		if (iCacheTable == null)
			throw new RuntimeException("Error: table cachingTableCADetails already created.");
		this.cachingTableCADetails = (ICacheTable<String, CertificateDetail>) iCacheTable;
	}

	@SuppressWarnings("unchecked")
	public <K, V> void setCachingTableApi(ICacheTable<K, V> iCacheTable) {
		if (iCacheTable == null)
			throw new RuntimeException("Error: table cachingTableApi already created.");
		this.cachingTableApi = (ICacheTable<String, Api>) iCacheTable;
	}

	@SuppressWarnings("unchecked")
	public <K, V> void setCachingTableAuth(ICacheTable<K, V> iCacheTable) {
		if (iCacheTable == null)
			throw new RuntimeException("Error: table cachingTableAuth already created.");
		this.cachingTableAuth = (ICacheTable<String, Auth>) iCacheTable;
	}

	@SuppressWarnings("unchecked")
	public <K, V> void setCachingTableAuthIpAddress(ICacheTable<K, V> iCacheTable) {
		if (iCacheTable == null)
			throw new RuntimeException("Error: table cachingTableAuthIpAddress already created.");
		this.cachingTableAuthIpAddress = (ICacheTable<String, String>) iCacheTable;
	}

	@SuppressWarnings("unchecked")
	public <K, V> void setCachingTablePolicy(ICacheTable<K, V> iCacheTable) {
		if (iCacheTable == null)
			throw new RuntimeException("Error: table cachingTablePolicy already created.");
		this.cachingTablePolicy = (ICacheTable<String, Policy>) iCacheTable;
	}

	@SuppressWarnings("unchecked")
	public <K, V> void setCachingTableContext(ICacheTable<K, V> iCacheTable) {
		if (iCacheTable == null)
			throw new RuntimeException("Error: table cachingTableContext already created.");
		this.cachingTableContext = (ICacheTable<Integer, ContextWrapper>) iCacheTable;
	}

	@SuppressWarnings("unchecked")
	public <K, V> void setCachingTableSettings(ICacheTable<K, V> iCacheTable) {
		if (iCacheTable == null)
			throw new RuntimeException("Error: table cachingTableSettings already created.");
		this.cachingTableSettings = (ICacheTable<String, String>) iCacheTable;
	}

	@SuppressWarnings("unchecked")
	public <K, V> void setCachingTableKey(ICacheTable<K, V> iCacheTable) {
		if (iCacheTable == null)
			throw new RuntimeException("Error: table cachingTableKey already created.");
		this.cachingTableKey = (ICacheTable<String, Key>) iCacheTable;
	}

	@SuppressWarnings("unchecked")
	public <K, V> void setCachingTableCertificate(ICacheTable<K, V> iCacheTable) {
		if (iCacheTable == null)
			throw new RuntimeException("Error: table cachingTableCertificate already created.");
		this.cachingTableCertificate = (ICacheTable<String, Certificate>) iCacheTable;
	}

	@SuppressWarnings("unchecked")
	public <K, V> void setCachingTableLogLevel(ICacheTable<K, V> iCacheTable) {
		if (iCacheTable == null)
			throw new RuntimeException("Error: table cachingTableLogLevel already created.");
		this.cachingTableLogLevel = (ICacheTable<String, LogLevel>) iCacheTable;
	}

	@SuppressWarnings("unchecked")
	public <K, V> void setCachingTableCA(ICacheTable<K, V> iCacheTable) {
		if (iCacheTable == null)
			throw new RuntimeException("Error: table cachingTableCA already created.");
		this.cachingTableCA = (ICacheTable<String, Certificate>) iCacheTable;
	}

	@SuppressWarnings("unchecked")
	public <K, V> void setCachingTableCRL(ICacheTable<K, V> iCacheTable) {
		if (iCacheTable == null)
			throw new RuntimeException("Error: table cachingTableCRL already created.");
		this.cachingTableCRL = (ICacheTable<String, SSLCRL>) iCacheTable;
	}

	@SuppressWarnings("unchecked")
	public <K, V> void setCachingTableApiJars(ICacheTable<K, V> iCacheTable) {
		if (iCacheTable == null)
			throw new RuntimeException("Error: table cachingTableApiJars already created.");
		this.cachingTableApiJars = (ICacheTable<String, ApiJar>) iCacheTable;
	}

	public void setListeners(Set<IDataManagerListener> listeners) {
		this.listeners = listeners;
	}

	public void setUsedBucketIds(Set<Integer> usedBucketIds) {
		this.usedBucketIds = usedBucketIds;
	}


	@Override
	public void addApiListener(IEntryListener<String, Api> listener) {
		this.cachingTableApi.addEntryListener(listener);
	}

	@Override
	public void removeApiListener(IEntryListener<String, Api> listener) {
		this.cachingTableApi.removeEntryListener(listener);
	}

	/* private class that stubs a CacheTable to forbid its uses if not initialized. */
	private class SanityCheckCacheTable<K, V> implements ICacheTable<K, V> {

		// Stubs
		@Override
		public boolean containsKey(K key) 								{ throwNullTableException(); return false; }
		@Override
		public boolean set(K key, V value, String instanceIP) 			{ throwNullTableException(); return false; }
		@Override
		public boolean set(K key, V value) 								{ throwNullTableException(); return false; }
		@Override
		public V get(K key) 											{ throwNullTableException(); return null; }
		@Override
		public V remove(K key)											{ throwNullTableException(); return null; }
		@Override
		public String getName() 										{ throwNullTableException(); return null; }
		@Override
		public Set<K> getAllKeys()										{ throwNullTableException(); return null; }
		@Override
		public Collection<V> getAllValues() 							{ throwNullTableException(); return null; }
		@Override
		public void clear()												{ throwNullTableException(); }
		@Override
		public void lock(K key) 										{ throwNullTableException(); }
		@Override
		public void unlock(K key) 										{ throwNullTableException(); }
		@Override
		public void addEntryListener(IEntryListener<K, V> listener) 	{ throwNullTableException(); }
		@Override
		public void removeEntryListener(IEntryListener<K, V> listener)	{ throwNullTableException(); }
		@Override
		public void reloadSlave(String ip) 								{ throwNullTableException(); }
		@Override
		public void addEntryListener(IEntryListener<K, V> listener, K key) { throwNullTableException(); }
		@Override
		public void removeEntryListener(IEntryListener<K, V> listener, K key) { throwNullTableException(); }

		// constructor
		public SanityCheckCacheTable(String tableName) {  
			this.tableName = tableName;
		}

		// throw exception because table is not initalized
		private String tableName;
		private void throwNullTableException()
		{
			throw new NullHazelcastTableException("Hazelcast table:" + tableName + " hasn't been intialized. DataManager is not ready yet.");
		}
	}

	@Override
	public boolean isIpAllowed(Api api, String ip) {
		boolean result = false;
		if(api != null) {
			List<String> ipWhiteList = api.getWhiteListedIps();
			Iterator<String> it = ipWhiteList.iterator();			
			while(!result && it.hasNext()) {
				String ip2 = it.next();
				result = ip2.equals(ip);
			}
		}
		return result;
	}

	@Override
	public void addGlobalProxyListener3(IEntryListener<String, String> listener) {
		// A better approach consists in pre-filtering based on the key "addEntryListener(listener, E3Constant.GLOBAL_PROXY_SETTINGS)". Unfortunatly, it's buggy
		this.cachingTableSettings.addEntryListener(listener);
	}

	@Override
	public void removeGlobalProxyListener3(IEntryListener<String, String> listener) {
		this.cachingTableSettings.removeEntryListener(listener);
	}
}


