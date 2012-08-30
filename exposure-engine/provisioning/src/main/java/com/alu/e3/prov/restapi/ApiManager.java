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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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
import com.alu.e3.prov.ApplicationCodeConstants;
import com.alu.e3.prov.ProvisionException;
import com.alu.e3.prov.restapi.model.Api;
import com.alu.e3.prov.restapi.model.ApiResponse;
import com.alu.e3.prov.service.IApiService;

/**
 * This class exposes the REST API to create, update or delete an API on E3
 * system.
 */
@Controller
@Path("/apis")
@Description(value = " E3 REST API to create, update or delete an API")
public class ApiManager extends BasicManager {
	private static final CategoryLogger LOG = CategoryLoggerFactory.getLogger(ApiManager.class, Category.PROV);

	private IApiService apiService;

	/**
	 * 
	 * @param dataManager
	 */
	public void setApiService(IApiService apiService) {
		this.apiService = apiService;
	}

	public ApiManager() {

	}

	/**
	 * This REST API is used to create an API.
	 * 
	 * @param request
	 * @return Response
	 * 
	 * @inputWrapped com.alu.e3.prov.restapi.model.Api
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.ApiResponse
	 * 
	 * @HTTP 200 for success
	 * 
	 * @HTTP 500 for E3 internal errors
	 * 
	 * @RequestHeader N/A No specific request header needed
	 * 
	 * @ResponseHeader X-E3-Application-Error-Code 10x E3 Application Error Code
	 */
	@POST
	@Path("")
	@Produces({ MediaType.APPLICATION_XML /* , MediaType.APPLICATION_JSON */})
	@Consumes({ MediaType.APPLICATION_XML /* , MediaType.APPLICATION_JSON */})
	@Description(value = "This REST API is used to create an API.")
	public Response create(final Api api) {

		if (LOG.isDebugEnabled())
			LOG.debug("Create API ID: {}", api.getId());

		Action action = new Action() {
			protected Object doAction(Object... params) {
				try {
					apiService.create(api);
				} catch (ProvisionException e) {
					throw new WebApplicationException(e);
				}
				return new ApiResponse(ApiResponse.SUCCESS, api.getId());
			}
		};

		return execute(action, (Object) null);

	}

	/**
	 * This REST API is used to update an API.
	 * 
	 * @param request
	 * @param apiID
	 *            the API ID
	 * @return
	 * 
	 * @inputWrapped com.alu.e3.prov.restapi.model.Api
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.ApiResponse
	 * 
	 * @HTTP 200 for success
	 * 
	 * @HTTP 500 for E3 internal errors
	 * 
	 * @RequestHeader N/A No specific request header needed
	 * 
	 * @ResponseHeader X-E3-Application-Error-Code 10x E3 Application Error Code
	 */
	@PUT
	@Path("/{API_ID}")
	@Produces({ MediaType.APPLICATION_XML /* , MediaType.APPLICATION_JSON */})
	@Consumes({ MediaType.APPLICATION_XML })
	@Description(value = "This REST API is used to update an API.")
	public Response update(final Api api, final @PathParam("API_ID") String apiId) {

		if (LOG.isDebugEnabled())
			LOG.debug("Update API ID: {}", apiId);

		// check API ID same in Pay-load/URL
		if (api == null || api.getId() == null)
			throw new WebApplicationException(new ProvisionException(ApplicationCodeConstants.API_ID_NOT_PROVIDED, "API ID missing in the body for Update operation"));

		if (!api.getId().equals(apiId))
			throw new WebApplicationException(new ProvisionException(ApplicationCodeConstants.API_ID_MISMATCH, "API ID not the same in URL vs Body for Update operation: +" + apiId + "/" + apiId));

		Action action = new Action() {
			protected Object doAction(Object... params) {
				try {
					apiService.update(api);
					return new ApiResponse(ApiResponse.SUCCESS, apiId);

				} catch (ProvisionException e) {
					throw new WebApplicationException(e);
				}
			}
		};
		return execute(action, (Object) null);

	}

	/**
	 * This REST API is used to delete an API.
	 * 
	 * @param apiID
	 * @return
	 * @returnWrapped com.alu.e3.prov.restapi.model.ApiResponse
	 * 
	 * @HTTP 200 for success
	 * 
	 * @HTTP 500 for E3 internal errors
	 * 
	 * @RequestHeader N/A No specific request header needed
	 * 
	 * @ResponseHeader X-E3-Application-Error-Code 10x E3 Application Error Code
	 */
	@DELETE
	@Path("/{API_ID}")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "This REST API is used to delete an API.")
	public Response delete(final @PathParam("API_ID") String apiId) throws Exception {

		if (LOG.isDebugEnabled())
			LOG.debug("Delete API ID: {}", apiId);

		Action action = new Action() {

			protected Object doAction(Object... params) {
				try {
					apiService.delete(apiId);
				} catch (ProvisionException e) {
					throw new WebApplicationException(e);
				}
				return new ApiResponse(ApiResponse.SUCCESS, apiId);
			}
		};
		return execute(action, (Object) null);

	}

	/**
	 * Gets an API by it's ID from E3 system.
	 * 
	 * @param apiId
	 * @return
	 * @throws ProvisionException
	 */
	@GET
	@Path("/{API_ID}")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "Used to get an API by this ID.")
	public Response get(final @PathParam("API_ID") String apiId) {

		if (LOG.isDebugEnabled())
			LOG.debug("Get API ID: {}", apiId);

		Action action = new Action() {

			protected Object doAction(Object... params) {
				try {
					Api api = apiService.get(apiId);
					return new ApiResponse(ApiResponse.SUCCESS, api);

				} catch (ProvisionException e) {
					throw new WebApplicationException(e);
				}
			}
		};
		return execute(action, (Object) null);

	}

	/**
	 * Get all APIs created on E3 system
	 * 
	 * @return
	 * @throws ProvisionException
	 */
	@GET
	@Path("")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "Used to get all API IDs.")
	public Response getAll() {

		if (LOG.isDebugEnabled())
			LOG.debug("GetAll APIs");

		Action action = new Action() {

			protected Object doAction(Object... params) {
				try {
					List<String> apiIdsList = apiService.getAll();
					return new ApiResponse(ApiResponse.SUCCESS, apiIdsList);

				} catch (ProvisionException e) {
					throw new WebApplicationException(e);
				}
			}

		};

		return execute(action, (Object) null);
	}

}
