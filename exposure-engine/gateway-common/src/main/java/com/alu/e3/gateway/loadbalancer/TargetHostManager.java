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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alu.e3.common.caching.IEntryListener;
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.data.DataEntryEvent;
import com.alu.e3.data.IDataManagerListener;
import com.alu.e3.data.model.Api;
import com.alu.e3.data.model.sub.APIContext;
import com.alu.e3.data.model.sub.ApiIds;
import com.alu.e3.data.model.sub.LoadBalancing;
import com.alu.e3.data.model.sub.TargetHost;
import com.alu.e3.gateway.targethealthcheck.ITargetHealthCheckService;

/**
 * Acts as ManagedTargetHost Database and used to maintain status of TargetHosts.
 * HttpLoadBalancer interacts with this TargetHostManager to get all Targets on which it must loadbalance requests for a given API.
 * 
 * TargetHostManager listens on the DataManager for any create/update/delete API operations and populate an internal list of ManagedTargetHost.
 * 
 * A ManagedTargetHost is a wrapper object that encapsulate a TargetHost (from E3's DataModel) and other data used by LoadBalancing (status, ...).
 * There can have one ManagedTargetHost for several TargetHost objects, if they have the same protocol+host+port+healthcheck.  
 * Ex: API #1 (http://www.apple.com|Ping), API #2 (http://www.apple.com|Telnet), API #3 (http://www.apple.com|Ping) where Ping and Telnet are
 * TargetHealthCheck types.
 * API #1 and #3 must have the same "Managed target" and #2 another one.
 *
 */
public class TargetHostManager implements ITargetHostManager, IEntryListener<String, Api>, IDataManagerListener {

	protected IDataManager dataManager;

	private static final Logger LOGGER = LoggerFactory.getLogger(TargetHostManager.class);

	public enum TargetStatus {
		AVAILABLE, OVERLOADED, UNAVAILABLE
	}

	// Map of ManagedTargetHosts (mapped by their reference)
	protected Map<String, ManagedTargetHost> targets;
	
	// Map<ApiID, Map<ApiContextID, List<TargetReference>>>
	protected Map<String, Map<String, List<TargetReference>>> targetReferencesMap;
	
	// Map of HealthChech services (mapped by their name, the same one to be used in API provisioning)
	protected Map<String, ITargetHealthCheckService> targetHealthCheckServices;

	// Internal list to postpone start of services in bean initialization
	private List<ITargetHealthCheckService> servicesToStart;

	public TargetHostManager() {
		targetHealthCheckServices = Collections.synchronizedMap(new HashMap<String, ITargetHealthCheckService>());
		targets = Collections.synchronizedMap(new HashMap<String, ManagedTargetHost>());
		targetReferencesMap = Collections.synchronizedMap(new HashMap<String, Map<String, List<TargetReference>>>());
	}

	/**
	 * Called by init-method in spring bean instantiation.
	 * Will register and start all TargetHealthCheck services.
	 */
	public void init() {
		LOGGER.debug("Initializing TargethostManager");
		
		// Registering as Listener on DataManager
		if(this.dataManager != null) {
			this.dataManager.addListener(this);
		}

		// Starts the list of HealthCheckServices
		if (servicesToStart != null) {
			for (ITargetHealthCheckService service : servicesToStart) {
				registerHealthCheckService(service);
			}
			servicesToStart.clear();
			servicesToStart = null;
		}
	}
	
	/**
	 * Called by destroy-method of spring bean.
	 * Stops listening on API create/update/delete operations.
	 */
	public void destroy() {
		LOGGER.debug("Destroying TargethostManager");
		
		if(this.dataManager != null) {
			this.dataManager.removeApiListener(this);
		}
	}
	
	/**
	 * Spring setter for DataManager
	 * @param dataManager The DataManager to set
	 */
	public void setDataManager(IDataManager dataManager) {
		this.dataManager = dataManager;
	}

	/**
	 * Spring setter for the list of ITargetHealthCheckService.
	 * 
	 * @param services The list of ITargetHealthCheckService to register and start.
	 */
	public void setHealthCheckServices(List<ITargetHealthCheckService> services) {
		LOGGER.debug("Registering {} HealthCheck services", services.size());
		this.servicesToStart = services;
	}

	/**
	 * Registers and starts a ITargetHealthCheckService.
	 * Internal method that put the service in it's internal map, mapped by service's name.
	 * The service is then started.
	 * 
	 * @param service The ITargetHealthCheckService to register and start.
	 */
	protected void registerHealthCheckService(ITargetHealthCheckService service) {
		if(service.getName() != null) {
			LOGGER.debug("Registering HealthCheck service {} ({})", service.getName(), service.getClass().getName());
			
			if(!this.targetHealthCheckServices.containsKey(service.getName())) {
				this.targetHealthCheckServices.put(service.getName(), service);
				service.start();
			} else {
				LOGGER.warn("There is already a HealthCheck service for the name {}", service.getName());
			}
		} else {
			LOGGER.warn("HealthCheck service {} does not have a name, ignoring it.", service.getClass().getName());
		}
	}

	/**
	 * Returns list of TargetReference objects for a given pair ApiID/ApiContextID.
	 * A TargetReference contains a TargetHost from E3's DataModel (to get url, site, ...) + a reference string.
	 * 
	 * @param apiId The Api ID
	 * @param contextId The Api Context ID
	 * @return The list of TargetReference objects for a given pair ApiID/ApiContextID.
	 */
	@Override
	public List<TargetReference> getTargetReferences(String apiId, String contextId) {
		List<TargetReference> targetReferences = getContextList(apiId, contextId);
		
		return targetReferences;
	}
	
	/**
	 * Internal getter for the map of Api Contexts.
	 * Creates the Map if it does not exists, returns the existing one otherwise.
	 * 
	 * @param apiId The ApiId of Api Contexts to retrieve.
	 * @return The Map of APIContext for this Api
	 */
	protected synchronized Map<String, List<TargetReference>> getApiMap(String apiId) {
		Map<String, List<TargetReference>> apis = targetReferencesMap.get(apiId);
		if(apis == null) {
			apis = Collections.synchronizedMap(new HashMap<String, List<TargetReference>>());
			this.targetReferencesMap.put(apiId, apis);
		} else {
			LOGGER.debug("Apis already initialized for api id {}; nothing to do", apiId);
		}
		
		return apis;
	}
	
	/**
	 * Internal getter for the List of TargetReference, for a given ApiContext.
	 * Creates the List if it does not exist, returns the existing one otherwise.
	 * @param apiId The ApiId
	 * @param contextId The Api Context Id
	 * @return The List of TargetReference for the corresponding Api Context.
	 */
	protected synchronized List<TargetReference> getContextList(String apiId, String contextId) {
		Map<String, List<TargetReference>> apis = getApiMap(apiId);
		List<TargetReference> contexts = apis.get(contextId);

		if(contexts == null) {
			contexts = Collections.synchronizedList(new ArrayList<TargetReference>());
			apis.put(apiId, contexts);
		} else {
			LOGGER.debug("Contexts already initialized for api id {} and context id {}; nothing to do", apiId, contextId);
		}

		return contexts;
	}
	
	
	/**
	 * Registers an API on the TargetHostManager.
	 * 
	 * For each API Contexts of the API,
	 *  * For each TargetHost of the API Context,
	 *    * Populate its ManagedTargetHost map
	 *    * Prepare the list of TargetReference that will be requested by the HttpLoadBalancer later on
	 * 
	 * This method is triggered by listeners on DataManager, when an API is created or updated.
	 * 
	 * @param apiId The API Id to register.
	 */
	protected void registerAPI(String apiId) {
		LOGGER.debug("Registering API {}", apiId);
		
		// Gets the API
		Api api = dataManager.getApiById(apiId, true);

		// Get context Ids (because apis are incomplete on Gateway only machines)
		List<ApiIds> ids = api.getContextIds();
		LOGGER.debug("Browsing {} contexts for API #{}", ids.size(), apiId);
		
		// Get or create the Api Context map for this API
		Map<String, List<TargetReference>> map = getApiMap(apiId);
		LOGGER.debug("Size of api context map for api id {}: {}", apiId, map.size());
		
		// Browsing API Contexts
		for(ApiIds id : ids) {
			// Get the ApiContext by its ID (this map is populated on all gateways)
			LOGGER.debug("Getting api context for api id {}", apiId);
			APIContext context = dataManager.getApiContextById(id.getApiContextId());
			
			// Shouldn't be null...
			if(context != null) {
				// Prepare the list of TargetReference
				List<TargetReference> targetReferences = new ArrayList<TargetReference>();
				LOGGER.debug("Registering Context {} ({} TargetHosts)", context.getId(), context.getTargetHosts().size());
				
				// Store requested HealthCheck service for later use
				ITargetHealthCheckService healthCheckService = null;
				String healthCheckServiceName = null;
				
				LoadBalancing lbConfig = context.getLoadBalancing();
				if(lbConfig.getTargetHealthCheck() != null) {
					healthCheckServiceName = lbConfig.getTargetHealthCheck().getType();
				}
				
				if(healthCheckServiceName != null) {
					LOGGER.debug("Will use HealthCheck service ", healthCheckServiceName);
					
					// Getting corresponding HealthCheck Service
					healthCheckService = targetHealthCheckServices.get(healthCheckServiceName);
					if(healthCheckService != null) {
						LOGGER.debug("Found a HealthCheckService ({}) for this name: {}", healthCheckService.getClass().getName(), healthCheckServiceName);
					} else {
						LOGGER.debug("No HealthCheckService found for this name: {}", healthCheckServiceName);
					}
				} else {
					LOGGER.debug("Will NOT use HealthCheck service for following targets");
				}
				
				
				// For each TargetHost, check if there is already a corresponding ManagedTargetHost
				for(TargetHost targetHost : context.getTargetHosts()) {
					
					// Using a "hash differentiation string":
					// We need to take into account the HealthCheckService associated to a TargetHost
					// Ex: API #1 (http://www.apple.com|Ping), API #2 (http://www.apple.com|Telnet), API #3 (http://www.apple.com|Ping)
					// API #1 and #3 must have the same "Managed target" and #2 another one
					// (One HealthCheck service may check for a specific functionality status)
					String hashDifferentiationString = healthCheckServiceName == null ? "" : healthCheckServiceName;
					String managedReference = ManagedTargetHost.computeTargetHostHash(targetHost, hashDifferentiationString);
									
					ManagedTargetHost target = targets.get(managedReference);		
					if(target == null) {
						LOGGER.debug("No corresponding ManagedTarget, creating a new one");
						
						// Instantiating and remembering managed target
						target = new ManagedTargetHost(targetHost);
						target.setReference(managedReference);
						targets.put(managedReference, target);
						
						// Registering managed target on health check service
						if(healthCheckService != null) {
							LOGGER.debug("Target #{} registered on HealthCheck service {}", managedReference, healthCheckService.getName());
							
							healthCheckService.registerTarget(target);
							target.setHealthCheckService(healthCheckService);
						} else {
							LOGGER.debug("Target #{} not registered on HealthCheck service");
						}
					} else {
						// We already have a ManagedTarget for this protocol+host+port+healthcheck
						LOGGER.debug("There is already a corresponding ManagedTarget: #{} ", managedReference);
					}
					
					// Incrementing counter of use (used for unregister method)
					target.getNumberOfUse().incrementAndGet();
					LOGGER.debug("New usage for target #{}: {}", managedReference, target.getNumberOfUse());
					
					// Adding this reference to the list that will be returned to the LoadBalancer later on
					TargetReference targetReference = new TargetReference();
					targetReference.setReference(managedReference);
					targetReference.setTargetHost(targetHost);
					targetReferences.add(targetReference);
				}
				
				// Put the list of TargetReferences in the appropriate map for this Api Context.
				map.put(context.getId(), targetReferences);
				
			} else {
				// Context null
				LOGGER.warn("No APIContext with that id {}, ignoring", id.getApiContextId());
			}
			
		} // End of for loop apiId / context
	}
	
	/**
	 * Unregisters a TargetHost from the TargetHostManager.
	 * The ManagedTargetHost usage will be decremented, 
	 * The object will be removed from the map only if it's not used by an API anymore and it will be unregistered from it's HealthCheck Service.
	 * 
	 * @param targetHostReference The reference String of the ManagedTargetHost to unregister. 
	 */
	protected void unregisterTargetHost(String targetHostReference) {
		LOGGER.debug("Unregistering target with reference {}", targetHostReference);
		ManagedTargetHost target = targets.get(targetHostReference);
		if(target != null) {
			int usage = target.getNumberOfUse().decrementAndGet();
			LOGGER.debug("Target #{} found, new usage: {}", targetHostReference, usage);
			
			// ManagedTarget is no longer used
			if(usage == 0) {
				LOGGER.debug("Usage == 0, removing target #{}", targetHostReference);
				// Unregistering it from the HealthCheck service
				ITargetHealthCheckService healthCheckService = target.getHealthCheckService();
				if(healthCheckService != null) {
					LOGGER.debug("Unregistering target #{} from HealthCheckService {}", targetHostReference, healthCheckService.getName());
					healthCheckService.unregisterTarget(target);
					target.setHealthCheckService(null);
				}

				// Removing it from the local targets map
				targets.remove(targetHostReference);
				LOGGER.debug("Target #{} removed from TargetHostManager DB", targetHostReference);
			}
		}
	}

	/**
	 * Marks a TargetHost as UNAVAILABLE (only if the target is monitored by a HealthCheck service, ignored otherwise).
	 * This method is called by the HttpLoadBalancer when sending a request to this targetHost has failed.
	 * 
	 * The LoadBalancer will not try this target anymore, till the HealthCheck service restored it's state to AVAILABLE.
	 * 
	 * @param targetHostReference The reference of ManagedTargetHost to mark as UNAVAILABLE.
	 */
	@Override
	public void notifyFailed(String targetHostReference) {
		ManagedTargetHost target = targets.get(targetHostReference);
		if (target != null && target.getHealthCheckService() != null) {
			LOGGER.debug("TargetHost {} has been notified as UNAVAILABLE, marking it");
			target.setStatus(TargetStatus.UNAVAILABLE);
		} else {
			LOGGER.debug("TargetHost {} has been notified as UNAVAILABLE, but is not healthChecked, so ignoring.");
		}
	}

	/**
	 * Returns if a ManagedTargetHost is AVAILABLE.
	 * Called by the LoadBalancer at each request.
	 * 
	 * @param targetReference The reference of the TargetHost to retrieve.
	 * @return true if the ManagedTargetHost is considered as AVAILABLE, false otherwise.
	 */
	@Override
	public boolean isAvailable(String targetReference) {
		ManagedTargetHost target = targets.get(targetReference);
		if(target != null) {
			return target.isAvailable();
		} else {
			LOGGER.warn("Asked invalid reference {}", targetReference);
			return false;
		}
	}


	/**
	 * Called when the DataManager is ready
	 */
	@Override
	public void dataManagerReady() {
		LOGGER.debug("Adding TargethostManager as listener on DataManager");
		if(this.dataManager != null) {
			// Removing from DataManagerListener
			this.dataManager.removeListener(this);
			
			// Adding as Listener on API Tables.
			this.dataManager.addApiListener(this);
		}
	}
	
	/**
	 * Removes all TargetReference from the TargetHostManager for an API.
	 * @param apiId The Api ID of the API to clean.
	 */
	private void cleanApi(String apiId) {
		Map<String, List<TargetReference>> map = getApiMap(apiId);
		
		if (map != null) {
			for(List<TargetReference> references : map.values()) {
				for(TargetReference reference : references) {
					unregisterTargetHost(reference.getReference());
				}
			}
			map.remove(apiId);
		}
	}

	@Override
	public void entryAdded(DataEntryEvent<String, Api> event) {
		// populate the DB
		registerAPI(event.getKey());
	}

	@Override
	public void entryUpdated(DataEntryEvent<String, Api> event) {
		// Update of an API
		// clean the database...
		cleanApi(event.getKey());
		
		// ... and repopulate it
		registerAPI(event.getKey());
	}

	@Override
	public void entryRemoved(DataEntryEvent<String, Api> event) {
		// Clean the DB
		cleanApi(event.getKey());		
	}



}
