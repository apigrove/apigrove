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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.data.wrapper.BeanConverterUtil;
import com.alu.e3.prov.restapi.model.Auth;
import com.alu.e3.prov.restapi.model.AuthKeyAuth;
import com.alu.e3.prov.restapi.model.AuthResponse;
import com.alu.e3.prov.restapi.model.AuthType;
import com.alu.e3.prov.restapi.model.BasicAuth;
import com.alu.e3.prov.restapi.model.Error;
import com.alu.e3.prov.restapi.model.IpWhiteList;
import com.alu.e3.prov.restapi.model.Key;
import com.alu.e3.prov.restapi.model.WSSEAuth;

/**
 * REST API to provision authentications on E3 subsystem.
 */
@Controller
@Path("/auths")
@Description(value = " E3 REST API provision authentications on E3 subsystem")
public class AuthManager extends BasicManager{

	private static final CategoryLogger LOG = CategoryLoggerFactory.getLogger(AuthManager.class, Category.PROV);

	// business service
	private IDataManager dataManager;

	private final Action getAll;
	private final Action get;
	private final Action delete;
	private final Action create;
	private final Action update;

	public AuthManager() {
		getAll = newGetAllAction();
		get = newGetAction();
		delete = newDeleteAction();
		create = newCreateAction();
		update = newUpdateAction();
	}

	/**
	 * 
	 * @param dataManager
	 */
	public void setDataManager(IDataManager dataManager) {
		this.dataManager = dataManager;
	}

	/**
	 * This REST API is used to create an authentication element on E3 subsystem.
	 * 
	 * @param request the Auth structure to create
	 * @return Response the E3 response 
	 * 
	 * @inputWrapped com.alu.e3.prov.restapi.model.Auth
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.AuthResponse
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
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	@Description(value = "This REST API is used to create an authentication element on E3 subsystem.")
	public Response create(final Auth auth) {

		if(LOG.isDebugEnabled())
			LOG.debug("Create authentication ");

		return this.execute(create, auth);
	}

	/**
	 * This REST API is used to update the API authentication data.
	 * 
	 * @param request
	 * @param id
	 *            the authentication ID
	 * @return
	 * 
	 * @inputWrapped com.alu.e3.prov.restapi.model.AuthType
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.AuthResponse
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
	@Path("/{AUTH_ID}")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	@Description(value = "This REST API is used to update an authentication.")
	public Response update(final Auth auth, @PathParam("AUTH_ID") final String id) {

		if(LOG.isDebugEnabled())
			LOG.debug("Update Auth ID: {}", id);

		return this.execute(update, auth, id);
	}

	/**
	 * This REST API is used to delete an authentication from E3 subsystem.
	 * 
	 * @param id
	 *            the auth ID to delete
	 * @return
	 * 
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.AuthResponse
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
	@Path("/{AUTH_ID}")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "This REST API is used to delete an Auth.")
	public Response delete(@PathParam("AUTH_ID") final String id) {

		if(LOG.isDebugEnabled())
			LOG.debug("Delete Auth ID: {}", id);

		return this.execute(delete, id);

	}

	/**
	 * This REST API is used to get an authentication from E3 subsystem.
	 * 
	 * @param id
	 *            the auth ID to get
	 * @return
	 * 
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.AuthResponse
	 * 
	 * @HTTP 200 for success
	 * 
	 * @HTTP 500 for E3 internal errors
	 * 
	 * @RequestHeader N/A No specific request header needed
	 * 
	 * @ResponseHeader X-E3-Application-Error-Code 10x E3 Application Error Code
	 */


	@GET
	@Path("/{AUTH_ID}")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "This REST API is used to get an authentication by ID from E3 subsystem.")
	public Response get(@PathParam("AUTH_ID") final String id) {

		if(LOG.isDebugEnabled())
			LOG.debug("Get Auth ID: {}", id);

		return this.execute(get, id);
	}

	/**
	 * This REST API is used to get all authentications from E3 subsystem.
	 * 
	 * @return all the authentications from E3 subsystem
	 * 
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.AuthResponse
	 * 
	 * @HTTP 200 for success
	 * 
	 * @HTTP 500 for E3 internal errors
	 * 
	 * @RequestHeader N/A No specific request header needed
	 * 
	 * @ResponseHeader X-E3-Application-Error-Code 10x E3 Application Error Code
	 */

	@GET
	@Path("")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "This REST API is used to get all authentications from E3 subsystem.")
	public Response getAll() {

		if(LOG.isDebugEnabled())
			LOG.debug("GetAll Auths");

		return execute(getAll);
	}


	protected final Action newCreateAction() {
		return new  Action("create") {
			@Override
			protected Object doAction(Object... params) {

				Auth auth = (Auth) params[0];

				if ((auth.getId() == null) || (auth.getId().equals(""))) {
					auth.setId(UUID.randomUUID().toString());
				}

				com.alu.e3.prov.restapi.model.Error error = validate(auth);

				if(LOG.isDebugEnabled())
					LOG.debug("Creating Auth ID: {}", auth.getId());

				AuthResponse response = new AuthResponse(AuthResponse.SUCCESS);
				if(error == null){
					com.alu.e3.data.model.Auth authDataModel = BeanConverterUtil.toDataModel(auth);
					dataManager.addAuth(authDataModel);
					response.setId(auth.getId());
				}
				else{
					error.setErrorCode("400");
					response.setStatus(AuthResponse.FAILURE);
					response.setError(error);
				}

				return response;
			}
		};
	}

	protected final Action newUpdateAction() {
		return new  Action("update") {
			@Override
			protected Object doAction(Object... params) {
				Auth auth = (Auth) params[0];
				String id = (String) params[1];

				if(LOG.isDebugEnabled())
					LOG.debug("Updating Auth ID: {}", id);

				com.alu.e3.prov.restapi.model.Error error = validate(auth);

				if(auth.getId() == null || auth.getId().equals(""))
					auth.setId(id);
				else if(auth.getId().equals(id) == false)
					throw new InvalidParameterException("Auth ID mismatch");

				AuthResponse response = new AuthResponse(AuthResponse.SUCCESS);

				if(error == null){
					com.alu.e3.data.model.Auth authDataModel = BeanConverterUtil.toDataModel(auth);
					dataManager.updateAuth(authDataModel);
					response.setId(auth.getId());
				}
				else{
					error.setErrorCode("400");
					response.setStatus(AuthResponse.FAILURE);
					response.setError(error);
				}
				return response;
			}
		};
	}

	protected final Action newDeleteAction() {
		return new  Action("delete") {
			@Override
			protected Object doAction(Object... params) {
				String id = (String) params[0];

				if(LOG.isDebugEnabled())
					LOG.debug("Deleting Auth ID: {}", id);

				dataManager.removeAuth(id);

				AuthResponse response = new AuthResponse(AuthResponse.SUCCESS);
				return response;
			}
		};
	}

	protected final Action newGetAction() {
		return new  Action("get") {
			@Override
			protected Object doAction(Object... params) {
				String id = (String) params[0];

				if(LOG.isDebugEnabled())
					LOG.debug("Getting Auth ID: {}", id);

				com.alu.e3.data.model.Auth authDataModel = dataManager.getAuthById(id, true);
				Auth auth = BeanConverterUtil.fromDataModel(authDataModel);

				AuthResponse response = new AuthResponse(AuthResponse.SUCCESS);
				response.setAuth(auth);
				return response;

			}
		};
	}

	protected final Action newGetAllAction() {
		return new Action("getAll") {
			@Override
			protected Object doAction(Object... params) {
				if(LOG.isDebugEnabled())
					LOG.debug("Getting All Auths");

				AuthResponse response = new AuthResponse(AuthResponse.SUCCESS);

				Set<String> auths = dataManager.getAllAuthIds();
				response.getIds().addAll(auths);

				return response;
			}
		};
	}

	private Error validate(Auth auth) {
		com.alu.e3.prov.restapi.model.Error error = new Error();
		error.setErrorText("");
		boolean inError = false;

		// Do some validation
		AuthType authType = auth.getType();
		if(authType == null){
			inError = true;
			error.setErrorText("Could not determine auth-type from request.");			
		} else {
			if(authType.equals(AuthType.BASIC)){
				BasicAuth basicAuth = auth.getBasicAuth();
				if(basicAuth == null){
					inError = true;
					error.setErrorText("Request did not contain BasicAuth info.");
				} else {
					if(basicAuth.getUsername() == null || basicAuth.getUsername().isEmpty()){
						inError = true;
						error.setErrorText("Username must not be empty for Basic authentication type.");
					}
					if(basicAuth.getPassword() == null || basicAuth.getPassword().length <= 0){
						inError = true;
						error.setErrorText(error.getErrorText()+" Password must not be empty for Basic authentication type.");
					}
				}
			}
			else if(authType.equals(AuthType.WSSE)){
				WSSEAuth basicAuth = auth.getWsseAuth();
				if(basicAuth == null){
					inError = true;
					error.setErrorText("Request did not contain WsseAuth info.");
				} else {
					if(basicAuth.getUsername() == null || basicAuth.getUsername().isEmpty()){
						inError = true;
						error.setErrorText("Username must not be empty for WSSE authentication type.");
					}
					if(basicAuth.getPassword() == null || basicAuth.getPassword().length <= 0){
						inError = true;
						error.setErrorText(error.getErrorText()+" Passowrd must not be empty for WSSE authentication type.");
					}
				}
			}
			else if(authType.equals(AuthType.AUTHKEY)){
				AuthKeyAuth authKeyAuth = auth.getAuthKeyAuth();
				if(authKeyAuth == null) {
					inError = true;
					error.setErrorText("Request did not contain AuthKeyAuth info.");					
				} else {
					if(authKeyAuth.getKeyValue()== null || authKeyAuth.getKeyValue().isEmpty()){
						inError = true;
						error.setErrorText("authKey must not be empty for AuthKey authentication type.");	
					}
				}
			}
			else if(authType.equals(AuthType.IP_WHITE_LIST)){
				IpWhiteList ipWhiteListAuth = auth.getIpWhiteListAuth();
				if(ipWhiteListAuth == null) {
					inError = true;
					error.setErrorText("Request did not contain ipWhiteListAuth info.");							
				} else {
					// We don't check for null ipList here, but could ....
					List<String> ipList = ipWhiteListAuth.getIp();
					// Check for duplicate white-list ips by adding all members of list to a Set
					Set<String> testSet = new HashSet<String>();
					for (String ip : ipList) {
						if (testSet.contains(ip)) {
							if(LOG.isDebugEnabled())
								LOG.debug("Found duplicate whitelist ip: {}", ip);
							inError = true;
							error.setErrorText("Duplicate ip in white-list: " + ip);
							break;
						}
						testSet.add(ip);
					}
				}
			}

			for(Key key : auth.getProperties()){
				if(key.getName() == null || key.getName().isEmpty())
					throw new IllegalArgumentException("All properties must have a name");
			}
		}

		if(inError)
			return error;
		else
			return null;
	}



}
