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

import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.Description;
import org.springframework.stereotype.Controller;

import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.data.wrapper.BeanConverterUtil;
import com.alu.e3.prov.ApplicationCodeConstants;
import com.alu.e3.prov.ProvisionException;
import com.alu.e3.prov.restapi.model.AuthIdsNoIdType;
import com.alu.e3.prov.restapi.model.BulkPolicyQuotaRLBucketType;
import com.alu.e3.prov.restapi.model.PolicyIdsType;
import com.alu.e3.prov.restapi.model.PolicyResponse;

/**
 * The web service to create or delete a bucket of auths associated to a list of
 * policies.
 */
@Controller
@Path("/bulk/policies/quotaRLBuckets")
@Description(value = " E3 REST API to create or delete a bucket of auths associated to a list of policies")
public class BulkPolicyQuotaRLBucketsManager extends BasicManager {

	private static final CategoryLogger LOG = CategoryLoggerFactory.getLogger(BulkPolicyQuotaRLBucketsManager.class, Category.PROV);

	private IDataManager dataManager;

	public BulkPolicyQuotaRLBucketsManager() {
	}

	/**
	 * 
	 * @param dataManager
	 */
	public void setDataManager(IDataManager dataManager) {
		this.dataManager = dataManager;
	}
	
	/**
	 * This REST API is used to create a bulk to associate auths to policies
	 * @param request
	 * @return Response
	 * @throws ProvisionException
	 * 
	 * @inputWrapped com.alu.e3.prov.restapi.model.BulkPolicyQuotaRLBucketType
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.PolicyResponse
	 * 
	 * @HTTP 200 for success
	 * 
	 * @HTTP 500 for E3 internal errors
	 * 
	 * @RequestHeader N/A No specific request header needed
	 * 
	 * @ResponseHeader N/A No specific response header needed
	 */
	@POST
	@Path("")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	@Description(value = "This REST API is used to create a bucket linked to a list of policies.")
	public Response createBucket(final BulkPolicyQuotaRLBucketType request) throws ProvisionException {

		checkCreate(request);

		Action addBucket = new Action() {

			protected Object doAction(Object... params) {
				PolicyIdsType policies = request.getPolicies();
				AuthIdsNoIdType authIds = request.getQuotaRLBucket();

				if (authIds.getId() == null || authIds.getId().equals("")) {
					// create the id
					authIds.setId(UUID.randomUUID().toString());
				}

				if(LOG.isDebugEnabled()) {
					LOG.debug("Add auths to policies:" + policies + " on bucket:" + authIds);
				}

				com.alu.e3.data.model.sub.QuotaRLBucket authIdsDataModel = BeanConverterUtil.toDataModel(authIds);
				dataManager.addAuthsToBucket(policies.getId(), authIds.getId(), authIdsDataModel);

				return new PolicyResponse(PolicyResponse.SUCCESS, authIds.getId());
			}
		};

		return execute(addBucket, request);
	}
	
	
	/**
	 * This REST API is used to append auths to a bucket for several policies
	 * @param request
	 * @return Response
	 * @throws ProvisionException
	 * 
	 * @inputWrapped com.alu.e3.prov.restapi.model.BulkPolicyQuotaRLBucketType
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.PolicyResponse
	 * 
	 * @HTTP 200 for success
	 * 
	 * @HTTP 500 for E3 internal errors
	 * 
	 * @RequestHeader N/A No specific request header needed
	 * 
	 * @ResponseHeader N/A No specific response header needed
	 */
	@PUT
	@Path("/{BUCKET_ID}")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	@Description(value = "This REST API is used to append auths to a bucket for several policies.")
	public Response appendBucket(final BulkPolicyQuotaRLBucketType request, final @PathParam("BUCKET_ID") String bucketID) throws ProvisionException {

		checkCreate(request);
		
		AuthIdsNoIdType authIds = request.getQuotaRLBucket();
		
		if (authIds.getId() != null) {
			if(!authIds.getId().equals(bucketID)){
				throw new WebApplicationException(new ProvisionException(ApplicationCodeConstants.INVALID_XML, "Bucket ID not the same in payload as in URL:" + authIds.getId() +" >> " + bucketID));
			}
			
		}
		
		Action appendBucket = new Action() {

			protected Object doAction(Object... params) {
				PolicyIdsType policies = request.getPolicies();
				
				AuthIdsNoIdType authIds = request.getQuotaRLBucket();

				if(LOG.isDebugEnabled()) {
					LOG.debug("Add auths to policies:" + policies + " on bucket:" + authIds);
				}

				com.alu.e3.data.model.sub.QuotaRLBucket authIdsDataModel = BeanConverterUtil.toDataModel(authIds);
				dataManager.appendAuthsToBucket(policies.getId(), authIds.getId(), authIdsDataModel);

				return new PolicyResponse(PolicyResponse.SUCCESS, authIds.getId());
			}
		};

		return execute(appendBucket, request);
	}
	
	/**
	 * Deletes a QuotaRLBucket from a list of policies.
	 * 
 	 * @inputWrapped com.alu.e3.prov.restapi.model.BulkPolicyQuotaRLBucketType
	 *
	 * @returnWrapped com.alu.e3.prov.restapi.model.PolicyResponse 
	 * 
	 * @HTTP 200 for success
	 * 
	 * @HTTP 500 for E3 internal errors
	 * 
	 * @RequestHeader N/A No specific request header needed
	 * 
	 * @ResponseHeader N/A No specific response header needed
	 */
	@PUT
	@Path("/{BUCKET_ID}/deleteBucket")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	@Description(value = "This REST API is used to remove a bucket from a list of policies.")
	public Response deleteBucket(final BulkPolicyQuotaRLBucketType request, final @PathParam("BUCKET_ID") String bucketID) throws ProvisionException {

		checkDelete(request);

		Action deleteBucket = new Action() {

			protected Object doAction(Object... params) {
				PolicyIdsType policies = request.getPolicies();

				if(LOG.isDebugEnabled()) {
					LOG.debug("Remove bucket ID:[" + bucketID + "] on policy:"+ policies.getId());
				}

				dataManager.removeBucket(policies.getId(), bucketID);

				return new PolicyResponse(PolicyResponse.SUCCESS);
			}
		};
		return execute(deleteBucket, bucketID);
	}
	
	
	/**
	 * This REST API is used to remove auths from a bucket for several policies
	 * @param request
	 * @return Response
	 * @throws ProvisionException
	 * 
	 * @inputWrapped com.alu.e3.prov.restapi.model.BulkPolicyQuotaRLBucketType
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.PolicyResponse
	 * 
	 * @HTTP 200 for success
	 * 
	 * @HTTP 500 for E3 internal errors
	 * 
	 * @RequestHeader N/A No specific request header needed
	 * 
	 * @ResponseHeader N/A No specific response header needed
	 */
	@PUT
	@Path("/{BUCKET_ID}/deleteAuth")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	@Description(value = "This REST API is used to remove auths from a bucket for several policies.")
	public Response deleteAuth(final BulkPolicyQuotaRLBucketType request, final @PathParam("BUCKET_ID") String bucketID) throws ProvisionException {

		checkCreate(request);
		
		AuthIdsNoIdType authIds = request.getQuotaRLBucket();
		
		if (authIds.getId() != null) {
			if(!authIds.getId().equals(bucketID)){
				throw new WebApplicationException(new ProvisionException(ApplicationCodeConstants.INVALID_XML, "Bucket ID not the same in payload as in URL:" + authIds.getId() +" >> " + bucketID));
			}
		}
		
		Action removeAuths = new Action() {
			protected Object doAction(Object... params) {
				PolicyIdsType policies = request.getPolicies();
				AuthIdsNoIdType authIds = request.getQuotaRLBucket();	
				dataManager.removeAuthsFromBucket(policies.getId(), bucketID, authIds.getAuthIds());
				if(LOG.isDebugEnabled()) {
					LOG.debug("Remove auths from policies:" + policies + " on bucket:" + authIds);
				}
				return new PolicyResponse(PolicyResponse.SUCCESS);
			}
		};

		return execute(removeAuths, request);
	}
	
	
	private boolean checkCreate(BulkPolicyQuotaRLBucketType request) throws ProvisionException {
		boolean result = check(request);
		
		if(request.getQuotaRLBucket() == null){
			throw new WebApplicationException(new ProvisionException(ApplicationCodeConstants.INVALID_XML, "Bulk validation failure: bucket of auths missing -> tag <quotaRLBucket>"));
		}
		
		return result;
	}

	private boolean checkDelete(BulkPolicyQuotaRLBucketType request) throws ProvisionException {
		boolean result = check(request);

		return result;

	}

	private boolean check(BulkPolicyQuotaRLBucketType request) throws ProvisionException {
		boolean restult = (request != null);

		if (!restult) {
			throw new WebApplicationException(new ProvisionException(ApplicationCodeConstants.INVALID_XML, "Bulk validation failure: request empty"));
		}

		return restult;

	}
}
