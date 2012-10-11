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
package com.alu.e3.gateway;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceReference;

import org.springframework.beans.factory.annotation.Autowired;

import com.alu.e3.common.caching.IEntryListener;
import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.data.DataEntryEvent;
import com.alu.e3.data.IDataManagerListener;
import com.alu.e3.data.model.ApiJar;

public class ApiDeploymentManager implements IDataManagerListener, IEntryListener<String, ApiJar>, BundleListener {

	private static final CategoryLogger logger = CategoryLoggerFactory.getLogger(ApiDeploymentManager.class, Category.PROV);

	protected static int SERVICES_LOOP_ATTEMPTS = 20;
	protected static int SERVICES_LOOP_DURATION = 500; // in milliseconds

	// Prefix for bundle's location
	protected static String BUNDLE_LOCATION_URI_PREFIX = "api://";
	public static String getBundleLocationURIPrefix() {
		return BUNDLE_LOCATION_URI_PREFIX;
	}

	protected IDataManager dataManager;
	
	@Autowired
	protected BundleContext bundleContext;

	// Used to associate a BundleEvent with an ack queue, via Bundle's location
	protected Map<String, ApiJar> bundleBeeingUpdated;
	protected Map<String, Semaphore> waitingActions;

	public ApiDeploymentManager() {}

	public void setDataManager(IDataManager dataManager) {
		if(logger.isDebugEnabled()) {
			logger.debug("Setting DataManager");	
		}
		// set dataManager before adding as listener, otherwise, dataManagerReady might fail!
		this.dataManager = dataManager;
	}
	
	@Override
	/**
	 * Called by DataManager when its ready (has loaded all it's tables)
	 */
	public void dataManagerReady() {
		if(logger.isDebugEnabled()) {
			logger.debug("DataManager ready, registering as Api Deployment Listener");
		}
		dataManager.addApiDeploymentListener(this);
	}
	
	public void init() {
		if(logger.isDebugEnabled()) {
			logger.debug("Initializing ApiDeploymentManager");
		}
		bundleBeeingUpdated = Collections.synchronizedMap(new HashMap<String, ApiJar>());
		waitingActions = Collections.synchronizedMap(new HashMap<String, Semaphore>());

		if(logger.isDebugEnabled()) {
			logger.debug("Setting as listener on BundleContext");
		}
		bundleContext.addBundleListener(this);
		
		dataManager.addListener(this);
	}
	
	/**
	 * Called by Spring magic 
	 */
	public void destroy() {
		if(logger.isDebugEnabled()) {
			logger.debug("Destroying, removing as listener");
		}
		if(bundleContext != null) {
			bundleContext.removeBundleListener(this);
		}
		if(dataManager != null) {
			dataManager.removeListener(this);
			dataManager.removeApiDeploymentListener(this);
		}
	}

	@Override
	/**
	 * A route has been added. Installing the bundle.
	 */
	public void entryAdded(DataEntryEvent<String, ApiJar> event) {
		if(logger.isDebugEnabled()) {
			logger.debug("[EntryAdded] ApiId: " + event.getKey());
		}
		
		// A location which will identify the bundle in the bundle context
		String location = BUNDLE_LOCATION_URI_PREFIX + event.getKey();
		
		// The ApiJar object extracted from the event, containing the queueName to post ACKs and bundle's data
		ApiJar jar = event.getValue();
		
		// Start bundle installation
		handleAddOrUpdateEvent(location, jar, event.getKey());
		if(logger.isDebugEnabled()) {	
			logger.debug("[EntryAdded] Finished handling event for apiId " + event.getKey());
		}
	}


	@Override
	/**
	 * A route has been updated, uninstalling previous one and installing new one
	 */
	public void entryUpdated(DataEntryEvent<String, ApiJar> event) {
		if(logger.isDebugEnabled()) {
			logger.debug("[EntryUpdated] for apiId: " + event.getKey());
		}
		
		// A location which will identify the bundle in the bundle context
		String location = BUNDLE_LOCATION_URI_PREFIX + event.getKey();
		
		// The ApiJar object extracted from the event, containing the queueName to post ACKs and bundle's data
		ApiJar jar = event.getValue();
		
		// Start bundle update
		handleAddOrUpdateEvent(location, jar, event.getKey());
		
		if(logger.isDebugEnabled()) {
			logger.debug("[EntryUpdated] Finished handling event for apiId " + event.getKey());
		}
	}


	@Override
	/**
	 * A route has been removed
	 */
	public void entryRemoved(DataEntryEvent<String, ApiJar> event) {
		if(logger.isDebugEnabled()) {
			logger.debug("[EntryRemoved] Handling event for apiId: " + event.getKey());
		}
		String apiId = event.getKey();

		// A location which will identify the bundle in the bundle context
		String location = BUNDLE_LOCATION_URI_PREFIX + apiId;

		// The bundle to remove
		Bundle bundle = getBundle(location);

		if(bundle != null) {
			try {
				Semaphore stopActionSemaphore = new Semaphore(0);
				waitingActions.put(location, stopActionSemaphore);
				
				// Stopping the bundle
				if(logger.isDebugEnabled()) {
					logger.debug("Stopping bundle for apiId " + apiId + ": " + bundle.getSymbolicName());
				}
				bundle.stop();
				stopActionSemaphore.tryAcquire(10L, TimeUnit.SECONDS);
				if(logger.isDebugEnabled()) {
					logger.debug("Stop unlocked for apiId " + apiId);
				}

				// Uninstalling the bundle
				// The ACK will be post in "bundleChanged" method, when the event "UNINSTALLED" will be fired for this bundle
				if(logger.isDebugEnabled()) {
					logger.debug("Uninstalling bundle for apiId " + apiId + ": " + bundle.getSymbolicName());
				}
				bundle.uninstall();
				stopActionSemaphore.tryAcquire(10L, TimeUnit.SECONDS);
				if(logger.isDebugEnabled()) {
					logger.debug("Uninstall unlocked for apiId " + apiId);
				}
				
			} catch (Exception e) {
				logger.error("Error stopping/uninstalling bundle " + bundle.getSymbolicName() + " after update ", e);				

				// post back the error in the queue
				throw new RuntimeException("Error stopping/uninstalling bundle " + bundle.getSymbolicName() + " after update ");
			} finally {
				// removing the queueName to prevent any later BundleEvent handling
				waitingActions.remove(location);
			}
		} else {
			// No bundle found, ignoring the request and sending an OK ack.
			logger.warn("Bundle " + location + " not found in current context");
		}
		if(logger.isDebugEnabled()) {
			logger.debug("[EntryRemoved] Finished handling event for apiId " + apiId);
		}
	}

	/**
	 * (Re)Installs a bundle
	 * @param location A string identifying the bundle
	 * @param jar The ApiJar object containing the bundle to (re)install
	 */
	private void handleAddOrUpdateEvent(String location, ApiJar jar, String apiId) {
		Bundle existingBundle = getBundle(location);
		try {
			if(existingBundle != null) {
				Semaphore stopActionSemaphore = new Semaphore(0);
				waitingActions.put(location, stopActionSemaphore);
				
				// Stopping the bundle
				if(logger.isDebugEnabled()) {
					logger.debug("Stopping bundle for apiId " + apiId + ": " + existingBundle.getSymbolicName());
				}
				existingBundle.stop();
				stopActionSemaphore.tryAcquire(10L, TimeUnit.SECONDS);

				// Uninstalling the bundle
				// The ACK will be post in "bundleChanged" method, when the event "UNINSTALLED" will be fired for this bundle
				if(logger.isDebugEnabled()) {
					logger.debug("Uninstalling bundle for apiId " + apiId + ": " + existingBundle.getSymbolicName());
				}
				existingBundle.uninstall();
				stopActionSemaphore.tryAcquire(10L, TimeUnit.SECONDS);
				
				waitingActions.remove(location);
			}
			
			// no previous bundle, it's a fresh install so let's do it
			installApiJar(location, jar, apiId);


		} catch(Exception e) {
			if(logger.isDebugEnabled()) {
				logger.error("An error occured while installing the bundle", e);
			}
			throw new RuntimeException("An error occured while installing the bundle");
		} finally {
			waitingActions.remove(location);
		}
	}

	private void installApiJar(String location, ApiJar jar, String apiId) throws BundleException {
		byte[] data = jar.getData();
		ByteArrayInputStream in = new ByteArrayInputStream(data);

		Bundle bundle = bundleContext.installBundle(location, in);

		try {
			Semaphore startActionSemaphore = new Semaphore(0);
			waitingActions.put(location, startActionSemaphore);
			
			// Starting the bundle
			if(logger.isDebugEnabled()) {
				logger.debug("Starting bundle for apiId " + apiId + ": " + bundle.getSymbolicName());
			}
			bundle.start();
			if ( ! startActionSemaphore.tryAcquire(2L, TimeUnit.MINUTES) ) { // watch out this timeout can lead to rejected bundle if GW is overloaded 
				throw new BundleException ("bundle start failed");
			}
			waitingActions.remove(location);
		} catch(Exception e) {
			if(logger.isDebugEnabled()) {
				logger.error("An error occured while starting the bundle", e);
			}
			throw new RuntimeException("An error occured while starting the bundle");
		} finally {
			waitingActions.remove(location);
		}
	}

	/**
	 * Browse the list of bundles to find the one matching the location parameter.
	 * Not optimized at all...
	 * @param location The location to look bundle for.
	 * @return Matching bundle, or null if no bundle is foudn for this location
	 */
	private synchronized Bundle getBundle(String location) {
		// TODO optimize this
		Bundle[] bundles = bundleContext.getBundles();
		Bundle foundBundle = null;
		for(Bundle bundle : bundles) {
			if(bundle.getLocation().equals(location)) {
				foundBundle = bundle;
				break;
			}
		}

		return foundBundle;
	}


	@Override
	/**
	 * BundleListener
	 */
	public void bundleChanged(BundleEvent event) {
		Bundle bundle = event.getBundle();
		String location = bundle.getLocation();
		Semaphore actionSemaphore = waitingActions.get(location);

		switch(event.getType()) {			
		case BundleEvent.STARTED:
			if(logger.isDebugEnabled()) {
				logger.debug("Bundle " + bundle.getSymbolicName() + " STARTED");
			}

			// Starting a loop to check number of registered services, this allow to wait for the route to be UP
			// Previously using ServiceListener (filtered with the two services CamelContext + another one)
			// but took up to 20s to be notified for route startup (really random time)
			boolean isBundleStarted = waitForBundleStarted(bundle);
			if(isBundleStarted) {
				if (actionSemaphore!=null) actionSemaphore.release();
			} else {
				logger.error("Bundle " + bundle.getSymbolicName() + " not started in time");
			}
			break;
		case BundleEvent.UNINSTALLED:
			if(logger.isDebugEnabled()) {
				logger.debug("Bundle " + bundle.getSymbolicName() + " UNINSTALLED");
			}
			
			if (actionSemaphore!=null) actionSemaphore.release();
			
			break;

		case BundleEvent.INSTALLED:
			if(logger.isDebugEnabled()) {
				logger.debug("Bundle " + bundle.getSymbolicName() + " INSTALLED");
			}
			break;
		case BundleEvent.RESOLVED:
			if(logger.isDebugEnabled()) {
				logger.debug("Bundle " + bundle.getSymbolicName() + " RESOLVED");
			}
			break;
		case BundleEvent.STARTING:
			if(logger.isDebugEnabled()) {
				logger.debug("Bundle " + bundle.getSymbolicName() + " STARTING");
			}
			break;
		case BundleEvent.STOPPED:
			if (actionSemaphore!=null) actionSemaphore.release();
			if(logger.isDebugEnabled()) {
				logger.debug("Bundle " + bundle.getSymbolicName() + " STOPPED");
			}
			break;
		case BundleEvent.STOPPING:
			if(logger.isDebugEnabled()) {
				logger.debug("Bundle " + bundle.getSymbolicName() + " STOPPING");
			}
			break;
		case BundleEvent.UNRESOLVED:
			if(logger.isDebugEnabled()) {
				logger.debug("Bundle " + bundle.getSymbolicName() + " UNRESOLVED");
			}
			break;
		case BundleEvent.UPDATED:
			logger.trace("Bundle " + bundle.getSymbolicName() + " UPDATED");
			break;

		default:
			logger.trace("Unknown BundleEvent type");
			break;
		}
	}


	private boolean isBundleStarted(Bundle bundle) {
		boolean isStarted = false;
		ServiceReference[] services = bundle.getRegisteredServices();
		if (services != null && services.length>1) {
			isStarted = true;
		}
		return isStarted;
	}

	private boolean waitForBundleStarted(Bundle bundle) {
		int attempts = SERVICES_LOOP_ATTEMPTS;
		boolean bundleStarted = false;

		try {
			while (!isBundleStarted(bundle) && attempts > 0) {
				Thread.sleep(SERVICES_LOOP_DURATION);
				if (logger.isDebugEnabled())
					logger.debug("Number of " + SERVICES_LOOP_DURATION + "ms attempt for route started test: " + attempts);
				attempts--;
			}

			bundleStarted = isBundleStarted(bundle);

		} catch(InterruptedException e) {
			logger.error("Error while waiting for bundle start", e);
		}

		return bundleStarted;
	}


}
