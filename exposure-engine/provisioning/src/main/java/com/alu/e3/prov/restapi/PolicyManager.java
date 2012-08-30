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
package com.alu.e3.prov.restapi;

import java.security.InvalidParameterException;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.Description;
import org.springframework.stereotype.Controller;

import com.alu.e3.common.InvalidIDException;
import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.data.wrapper.BeanConverterUtil;
import com.alu.e3.prov.restapi.model.AuthIdsNoIdType;
import com.alu.e3.prov.restapi.model.Key;
import com.alu.e3.prov.restapi.model.Policy;
import com.alu.e3.prov.restapi.model.PolicyResponse;

/**
 * The web service to create, update or delete a policy.
 */
@Controller
@Path("/policies")
@Description(value = " E3 REST API to create, update or delete a policy")
public class PolicyManager extends BasicManager{

	private static final CategoryLogger LOG = CategoryLoggerFactory.getLogger(PolicyManager.class, Category.PROV);

	private final Action create;
	private final Action update;
	private final Action delete;
	private final Action get;
	private final Action getAll;

	private final Action createBucket;
	private final Action deleteBucket;
	private final Action putAuthsToBucket;
	private final Action deleteAuthFromBucket;
	private final Action deleteAuthsFromBucket;

	private IDataManager dataManager;

	public PolicyManager() {
		create = newCreateAction();
		update = newUpdateAction();
		delete = newDeleteAction();
		get = newGetAction();
		getAll = newGetAllAction();
		createBucket = newCreateBucketAction();
		deleteBucket = newDeleteBucketAction();
		putAuthsToBucket = newPutAuthsToBucketAction();
		deleteAuthFromBucket = newDeleteAuthFromBucketAction();
		deleteAuthsFromBucket = newdeleteAuthsFromBucketAction();
	}

	/**
	 * 
	 * @param dataManager
	 */
	public void setDataManager(IDataManager dataManager) {
		this.dataManager = dataManager;
	}

	/**
	 * Gets all policies.
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.PolicyResponse
	 */
	@GET
	@Path("")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "This REST API is used to fetch all policy ids.")
	public Response getAll() {
		return execute(getAll);
	}

	/**
	 * Creates a policy.
	 * 
	 * @inputWrapped com.alu.e3.prov.restapi.model.PolicyDataType
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.PolicyResponse
	 */
	@POST
	@Path("")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	@Description(value = "This REST API is used to create a policy.")
	public Response create(Policy request) {
		return execute(create, request);
	}

	/**
	 * Deletes a policy.
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.PolicyResponse
	 */
	@DELETE
	@Path("/{POLICY_ID}")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "This REST API is used to delete a policy.")
	public Response delete(@PathParam("POLICY_ID") String policyID) {
		return execute(delete, policyID);
	}


	/**
	 * Gets a policy.
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.PolicyResponse
	 */
	@GET
	@Path("/{POLICY_ID}")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "This REST API is used to fetch policy data.")
	public Response get(@PathParam("POLICY_ID") String policyID) {
		return execute(get, policyID);
	}

	/**
	 * Updates a policy.
	 * 
	 * @inputWrapped com.alu.e3.prov.restapi.model.PolicyDataType
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.PolicyResponse
	 */
	@PUT
	@Path("/{POLICY_ID}")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	@Description(value = "This REST API is used to update a policy.")
	public Response put(Policy request, @PathParam("POLICY_ID") String policyID) {
		return execute(update, request, policyID);
	}

	/**
	 * Creates a QuotaRLBucket in a policy.
	 * 
	 * @inputWrapped com.alu.e3.prov.restapi.model.QuotaRLBucket
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.PolicyResponse
	 */
	@POST
	@Path("/{POLICY_ID}/quotaRLBuckets")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	@Description(value = "This REST API is used to create a QuotaRLBucket in a policy.")
	public Response add(AuthIdsNoIdType request, @PathParam("POLICY_ID") String policyID) {
		return execute(createBucket, policyID, request);
	}

	/**
	 * Appends Auths to a QuotaRLBucket.
	 * 
	 * @inputWrapped com.alu.e3.prov.restapi.model.QuotaRLBucket
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.PolicyResponse
	 */
	@PUT
	@Path("/{POLICY_ID}/quotaRLBuckets/{BUCKET_ID}")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	@Description(value = "This REST API is used to append auths to a bucket.")
	public Response addAuthsToBucket(AuthIdsNoIdType request, @PathParam("POLICY_ID") String policyID, @PathParam("BUCKET_ID") String bucketID) {
		return execute(putAuthsToBucket, policyID, bucketID, request);
	}

	/**
	 * Deletes a QuotaRLBucket from a policy.
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.PolicyResponse
	 */
	@DELETE
	@Path("/{POLICY_ID}/quotaRLBuckets/{BUCKET_ID}")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "This REST API is used to remove a bucket from a policy.")
	public Response removeBucket(@PathParam("POLICY_ID") String policyID, @PathParam("BUCKET_ID") String bucketID) {
		return execute(deleteBucket, policyID, bucketID);
	}

	/**
	 * Deletes a Auth from a QuotaRLBucket.
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.PolicyResponse
	 */
	@DELETE
	@Path("/{POLICY_ID}/quotaRLBuckets/{BUCKET_ID}/auths/{AUTH_ID}")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "This REST API is used to remove an auth from a bucket.")
	public Response removeAuthFromBucket(@PathParam("POLICY_ID") String policyID, @PathParam("BUCKET_ID") String bucketID, @PathParam("AUTH_ID") String authID) {
		return execute(deleteAuthFromBucket, policyID, bucketID, authID);
	}

	/**
	 * Deletes Auths from a QuotaRLBucket.
	 * 
	 * @inputWrapped com.alu.e3.prov.restapi.model.QuotaRLBucket
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.PolicyResponse
	 */
	@PUT
	@Path("/{POLICY_ID}/quotaRLBuckets/{BUCKET_ID}/delete")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	@Description(value = "This REST API is used to remove Auths from a QuotaRLBucket.")
	public Response removeAuthsFromBucket(AuthIdsNoIdType request, @PathParam("POLICY_ID") String policyID, @PathParam("BUCKET_ID") String bucketID) {
		return execute(deleteAuthsFromBucket, policyID, bucketID, request);
	}

	/**
	 * Action to create a Policy
	 * @return
	 */
	protected final Action newCreateAction() {
		return new Action() {

			@Override
			protected Object doAction(Object... params) {
				Policy policy = (Policy) params[0];

				if ((policy.getId() == null) || (policy.getId().equals("")))
				{
					// create the id
					policy.setId(UUID.randomUUID().toString());
				}

				/**
				 * Some validation
				 */
				for(Key key : policy.getProperties()){
					if(key.getName() == null || key.getName().isEmpty())
						throw new IllegalArgumentException("All properties must have a name");
				}

				LOG.debug("Creating Policy:", policy.getId());

				com.alu.e3.data.model.Policy policyDataModel = BeanConverterUtil.toDataModel(policy);
				dataManager.addPolicy(policyDataModel);

				PolicyResponse response = new PolicyResponse(PolicyResponse.SUCCESS);
				response.setId(policy.getId());

				return response;
			}
		};
	}

	/**
	 * Action to update a Policy
	 * @return
	 */
	protected final Action newUpdateAction() {
		return new Action() {

			@Override
			protected Object doAction(Object... params) {
				Policy policy = (Policy) params[0];
				String policyId = (String) params[1];

				/**
				 * Some validation
				 */
				for(Key key : policy.getProperties()){
					if(key.getName() == null || key.getName().isEmpty())
						throw new IllegalArgumentException("All properties must have a name");
				}

				LOG.debug("Updating Policy:", policyId);

				if(policy.getId() == null || policy.getId().equals(""))
					policy.setId(policyId);
				else if(policy.getId().equals(policyId) == false)
					throw new InvalidParameterException("Policy ID mismatch");

				com.alu.e3.data.model.Policy policyDataModel = BeanConverterUtil.toDataModel(policy);				
				dataManager.updatePolicy(policyDataModel);

				return new PolicyResponse(PolicyResponse.SUCCESS);
			}
		};
	}

	/**
	 * 
	 * @return
	 */
	protected final Action newDeleteAction() {
		return new Action() {

			@Override
			protected Object doAction(Object... params) {
				String policyId = (String) params[0];

				LOG.debug("Deleting Policy:", policyId);

				dataManager.removePolicy(policyId);

				return new PolicyResponse(PolicyResponse.SUCCESS);
			}
		};
	}

	/**
	 * 
	 * @return
	 */
	protected final Action newGetAction() {
		return new Action() {

			@Override
			protected Object doAction(Object... params) {
				String policyId = (String) params[0];

				LOG.debug("Getting Policy:", policyId);

				com.alu.e3.data.model.Policy policyDataModel = dataManager.getPolicyById(policyId);
				if(policyDataModel == null)
					throw new InvalidIDException("A Policy with that ID does not exist");

				Policy policy = BeanConverterUtil.fromDataModel(policyDataModel);

				PolicyResponse response = new PolicyResponse(PolicyResponse.SUCCESS);
				response.setPolicy(policy);

				return response;
			}
		};
	}

	/**
	 * 
	 * @return
	 */
	protected final Action newGetAllAction() {
		return new Action() {

			@Override
			protected Object doAction(Object... params) {

				LOG.debug("Get all policies");

				PolicyResponse response = new PolicyResponse(PolicyResponse.SUCCESS);

				// Get all policies from the store
				response.getIds().addAll(dataManager.getAllPolicy());

				return response;
			}
		};
	}

	/**
	 * 
	 * @return
	 */
	protected final Action newCreateBucketAction() {
		return new Action() {

			@Override
			protected Object doAction(Object... params) {
				String policyId = (String) params[0];
				AuthIdsNoIdType authIds = (AuthIdsNoIdType) params[1];

				if(authIds.getId() == null || authIds.getId().equals("")) {
					// create the id
					authIds.setId(UUID.randomUUID().toString());
				}

				LOG.debug("Create bucket:", authIds.getId());

				com.alu.e3.data.model.sub.QuotaRLBucket authIdsDataModel = BeanConverterUtil.toDataModel(authIds);
				dataManager.createBucket(policyId, authIdsDataModel);

				PolicyResponse response = new PolicyResponse(PolicyResponse.SUCCESS);
				response.setId(authIds.getId());

				return response;
			}
		};
	}

	/**
	 * 
	 * @return
	 */
	protected final Action newDeleteBucketAction() {
		return new Action() {

			@Override
			protected Object doAction(Object... params) {
				String policyId = (String) params[0];
				String bucketId = (String) params[1];

				LOG.debug("Remove bucket:" + bucketId + " on policy:", policyId);

				dataManager.removeBucket(policyId, bucketId);

				return new PolicyResponse(PolicyResponse.SUCCESS);
			}
		};
	}

	/**
	 * 
	 * @return
	 */
	protected final Action newPutAuthsToBucketAction() {
		return new Action() {

			@Override
			protected Object doAction(Object... params) {
				String policyId = (String) params[0];
				String bucketId = (String) params[1];
				AuthIdsNoIdType authIds = (AuthIdsNoIdType) params[2];

				LOG.debug("Add auths to policy:" + policyId + " on bucket:", bucketId);

				com.alu.e3.data.model.sub.QuotaRLBucket authIdsDataModel = BeanConverterUtil.toDataModel(authIds);
				dataManager.addAuthsToBucket(policyId, bucketId, authIdsDataModel);

				return new PolicyResponse(PolicyResponse.SUCCESS);
			}
		};
	}

	/**
	 * 
	 * @return
	 */
	protected final Action newDeleteAuthFromBucketAction() {
		return new Action() {

			@Override
			protected Object doAction(Object... params) {
				String policyId = (String) params[0];
				String bucketId = (String) params[1];
				String authId = (String) params[2];

				LOG.debug("Remove auth:" + authId + " from policy:" + policyId + "/bucket:" + bucketId);
				dataManager.removeAuthFromBucket(policyId, bucketId, authId);

				return new PolicyResponse(PolicyResponse.SUCCESS);
			}
		};
	}
	/**
	 * 
	 * @return
	 */

	protected final Action newdeleteAuthsFromBucketAction() {
		return new Action() {

			@Override
			protected Object doAction(Object... params) {
				String policyId = (String) params[0];
				String bucketId = (String) params[1];
				AuthIdsNoIdType authIds = (AuthIdsNoIdType) params[2];

				LOG.debug("Remove auths fromo policy:" + policyId + " on bucket:", bucketId);

				for (String authId : authIds.getAuthIds())
				{
					dataManager.removeAuthFromBucket(policyId, bucketId, authId);
				}

				return new PolicyResponse(PolicyResponse.SUCCESS);
			}
		};
	}

}
