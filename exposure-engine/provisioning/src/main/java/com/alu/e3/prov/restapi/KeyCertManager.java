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
import com.alu.e3.data.model.Certificate;
import com.alu.e3.data.wrapper.BeanConverterUtil;
import com.alu.e3.prov.restapi.model.SSLCert;
import com.alu.e3.prov.restapi.model.SSLCertResponse;
import com.alu.e3.prov.restapi.model.SSLKey;
import com.alu.e3.prov.restapi.model.SSLKeyResponse;

/**
 * REST API to provision SSLKeys and SSLCerts on E3 subsystem.
 */
@Controller
@Path("/keys")
@Description(value = " E3 REST API provision SSLKeys and SSLCerts on E3 subsystem")
public class KeyCertManager extends BasicManager{

	private static final CategoryLogger LOG = CategoryLoggerFactory.getLogger(KeyCertManager.class, Category.PROV);

	// business service
	private IDataManager dataManager;

	private final Action getAllKeys;
	private final Action getKey;
	private final Action deleteKey;
	private final Action createKey;
	private final Action updateKey;
	
	private final Action getAllCerts;
	private final Action getCert;
	private final Action deleteCert;
	private final Action createCert;
	private final Action updateCert;
	
	private final Action getCSR;
	private final Action getSelfSigned;
	
	public KeyCertManager() {
		getAllKeys = newGetAllKeysAction();
		getKey = newGetKeyAction();
		deleteKey = newDeleteKeyAction();
		createKey = newCreateKeyAction();
		updateKey = newUpdateKeyAction();
		
		getAllCerts = newGetAllCertsAction();
		getCert = newGetCertAction();
		deleteCert = newDeleteCertAction();
		createCert = newCreateCertAction();
		updateCert = newUpdateCertAction();
		
		getCSR = newGetCSRAction();
		getSelfSigned = newGetSelfSignedAction();
	}

	

	/**
	 * 
	 * @param dataManager
	 */
	public void setDataManager(IDataManager dataManager) {
		this.dataManager = dataManager;
	}
	
	/**
	 * This REST API is used to create a a SSLKey element on E3 subsystem.
	 * 
	 * @param request the SSLKey structure to create
	 * @return Response the E3 response 
	 * 
	 * @inputWrapped com.alu.e3.prov.restapi.model.SSLKey
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.SSLKeyResponse
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
	@Description(value = "This REST API is used to create an SSLKey element on E3 subsystem.")
	public Response create(final SSLKey key) {
		
		if(LOG.isDebugEnabled())
			LOG.debug("Create SSLKey ");

		return this.execute(createKey, key);
	}

	/**
	 * This REST API is used to update the SSLKey data.
	 * 
	 * @param request
	 * @param id
	 *            the SSLKey ID
	 * @return
	 * 
	 * @inputWrapped com.alu.e3.prov.restapi.model.SSLKey
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.SSLKeyResponse
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
	@Path("/{KEY_ID}")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	@Description(value = "This REST API is used to update an SSLKey.")
	public Response update(final SSLKey key, @PathParam("KEY_ID") final String id) {
		
		if(LOG.isDebugEnabled())
			LOG.debug("Update SSLKey ID: {}", id);

		return this.execute(updateKey, key, id);
	}

	/**
	 * This REST API is used to delete an SSLKey from E3 subsystem.
	 * 
	 * @param id
	 *            the SSLkey ID to delete
	 * @return
	 * 
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.SSLKeyResponse
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
	@Path("/{KEY_ID}")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "This REST API is used to delete an SSLKey.")
	public Response delete(@PathParam("KEY_ID") final String id) {
		
		if(LOG.isDebugEnabled())
			LOG.debug("Delete SSLKey ID: {}", id);

		return this.execute(deleteKey, id);

	}
	
	/**
	 * This REST API is used to get an SSLKey from E3 subsystem.
	 * 
	 * @param id
	 *            the SSLkey ID to get
	 * @return
	 * 
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.SSLKeyResponse
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
	@Path("/{KEY_ID}")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "This REST API is used to get an SSLKey by ID from E3 subsystem.")
	public Response get(@PathParam("KEY_ID") final String id) {
		
		if(LOG.isDebugEnabled())
			LOG.debug("Get SSLKey ID: {}", id);

		return this.execute(getKey, id);
	}
	
	/**
	 * This REST API is used to get all SSLKeys from E3 subsystem.
	 * 
	 * @return all the SSLKeys from E3 subsystem
	 * 
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.SSLKeyResponse
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
	@Description(value = "This REST API is used to get all SSLKeys from E3 subsystem.")
	public Response getAll() {
		
		if(LOG.isDebugEnabled())
			LOG.debug("GetAll SSLKeys");

		return execute(getAllKeys);
	}
	
	/**
	 * This REST API is used to create a a SSLCert element on E3 subsystem.
	 * 
	 * @param request the SSLCert structure to create
	 * @return Response the E3 response 
	 * 
	 * @inputWrapped com.alu.e3.prov.restapi.model.SSLCert
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.SSLCertResponse
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
	@Path("/{KEY_ID}/certs")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	@Description(value = "This REST API is used to create an SSLCert element on E3 subsystem.")
	public Response createCert(final SSLCert cert, @PathParam("KEY_ID") final String keyId) {
		
		if(LOG.isDebugEnabled())
			LOG.debug("Create SSLCert ");

		return this.execute(createCert, keyId, cert);
	}

	/**
	 * This REST API is used to update the SSLKey data.
	 * 
	 * @param request
	 * @param keyId
	 *            the SSLKey ID
	 * @param certId
	 * 			  the SSLCert ID to get
	 * @return
	 * 
	 * @inputWrapped com.alu.e3.prov.restapi.model.SSLCert
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.SSLCertResponse
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
	@Path("/{KEY_ID}/certs/{CERT_ID}")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	@Description(value = "This REST API is used to update an SSLKey.")
	public Response updateCert(final SSLCert cert, @PathParam("KEY_ID") final String keyId, @PathParam("CERT_ID") final String certId) {
		
		if(LOG.isDebugEnabled())
			LOG.debug("Update SSLCert ID: {}", certId);

		return this.execute(updateCert, cert, keyId, certId);
	}

	/**
	 * This REST API is used to delete an SSLCert from E3 subsystem.
	 * 
	 * @param certId
	 *            the SSLkey ID to delete
	 * @param certId
	 * 			  the SSLCert ID to delete
	 * @return
	 * 
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.SSLCertResponse
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
	@Path("/{KEY_ID}/certs/{CERT_ID}")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "This REST API is used to delete an SSLCert.")
	public Response deleteCert(@PathParam("KEY_ID") final String keyId, @PathParam("CERT_ID") final String certId) {
		
		if(LOG.isDebugEnabled())
			LOG.debug("Delete SSLCert ID: {}", certId);

		return this.execute(deleteCert, keyId, certId);

	}
	
	/**
	 * This REST API is used to get an SSLCert from E3 subsystem.
	 * 
	 * @param keyId
	 *            the SSLkey ID to get
	 * @param certId
	 * 			  the SSLCert ID to get
	 * @return
	 * 
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.SSLCertResponse
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
	@Path("/{KEY_ID}/certs/{CERT_ID}")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "This REST API is used to get an SSLKey by ID from E3 subsystem.")
	public Response getCert(@PathParam("KEY_ID") final String keyId, @PathParam("CERT_ID") final String certId) {
		
		if(LOG.isDebugEnabled())
			LOG.debug("Get SSLCert ID: {}", certId);

		return this.execute(getCert, keyId, certId);
	}
	
	/**
	 * This REST API is used to get all SSLCerts for a key
	 * 
	 * @return all the SSLCerts for a key
	 * 
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.SSLCertResponse
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
	@Path("/{KEY_ID}/certs")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "This REST API is used to get all SSLCerts for a key.")
	public Response getAllCerts(@PathParam("KEY_ID") final String keyId) {
		
		if(LOG.isDebugEnabled())
			LOG.debug("GetAll SSLKeys");

		return execute(getAllCerts, keyId);
	}
	
	/**
	 * This REST API is used to get a CSR for the given SSLKey ID from E3 subsystem.
	 * 
	 * @param id
	 *            the SSLkey ID to get
	 * @return
	 * 
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.SSLCertResponse
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
	@Path("/{KEY_ID}/csr")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "This REST API is used to get a CSR for the given SSLKey ID from E3 subsystem.")
	public Response getCSR(@PathParam("KEY_ID") final String id) {
		
		if(LOG.isDebugEnabled())
			LOG.debug("Get CSR for SSLKey ID: {}", id);

		return this.execute(getCSR, id);
	}
	
	/**
	 * This REST API is used to create a self-signed cert for an existing Key
	 * 
	 * @param id
	 *            the SSLkey ID to sign
	 * @return
	 * 
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.SSLCertResponse
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
	@Path("/{KEY_ID}/selfsign")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "This REST API is used to create a self-signed cert for an existing Key")
	public Response getSelfSigned(@PathParam("KEY_ID") final String id) {
		
		if(LOG.isDebugEnabled())
			LOG.debug("Create SelfSign for SSLKey ID: {}", id);

		return this.execute(getSelfSigned, id);
	}
	

	protected final Action newCreateKeyAction() {
		return new  Action("create") {
			protected Object doAction(Object... params) {
				SSLKey key = (SSLKey) params[0];
				
				if ((key.getId() == null) || (key.getId().equals(""))) {
					key.setId(UUID.randomUUID().toString());
				}
				
				if (key.getContent() == null || key.getContent().isEmpty()) {
					throw new IllegalArgumentException("Must include content when creating a key");
				}
				if(key.getDisplayName() == null || key.getDisplayName().isEmpty()) {
					throw new IllegalArgumentException("Must include a display name when creating a key");
				}
				
				if(LOG.isDebugEnabled())
					LOG.debug("Creating SSLKey ID:", key.getId());
				
				// Convert the provisioning object to the true model object
				com.alu.e3.data.model.Key modelKey = BeanConverterUtil.toDataModel(key);
				dataManager.addKey(modelKey);

				SSLKeyResponse response = new SSLKeyResponse(SSLKeyResponse.SUCCESS);
				
				response.setId(key.getId());
				return response;
			}
		};
	}
	
	protected final Action newUpdateKeyAction() {
		return new  Action("update") {
			protected Object doAction(Object... params) {
				SSLKey key = (SSLKey) params[0];
				String id = (String) params[1];
				
				if(LOG.isDebugEnabled())
					LOG.debug("Updating SSLKey ID: {}", id);
				
				if(key.getId() == null || key.getId().isEmpty())
					key.setId(id);
				else if(key.getId().equals(id) == false)
					throw new InvalidParameterException("SSLKey ID mismatch");
				
				com.alu.e3.data.model.Key modelKey = BeanConverterUtil.toDataModel(key);
				dataManager.updateKey(modelKey);

				SSLKeyResponse response = new SSLKeyResponse(SSLKeyResponse.SUCCESS);
				response.setId(key.getId());
				return response;
			}
		};
	}
	
	protected final Action newDeleteKeyAction() {
		return new  Action("delete") {
			protected Object doAction(Object... params) {
				String id = (String) params[0];
				
				if(LOG.isDebugEnabled())
					LOG.debug("Deleting Auth ID: {}", id);
				
				dataManager.removeKey(id);
				
				SSLKeyResponse response = new SSLKeyResponse(SSLKeyResponse.SUCCESS);
				return response;
			}
		};
	}
	
	protected final Action newGetKeyAction() {
		return new  Action("get") {
			protected Object doAction(Object... params) {
				String id = (String) params[0];
				
				if(LOG.isDebugEnabled())
					LOG.debug("Getting Auth ID: {}", id);
				
				com.alu.e3.data.model.Key modelKey = dataManager.getKeyById(id, true);
				SSLKey key = null;
				if(modelKey != null)
					key = BeanConverterUtil.fromDataModel(modelKey);
				
				
				SSLKeyResponse response = new SSLKeyResponse(SSLKeyResponse.SUCCESS);
				response.setKey(key);
				return response;
	
			}
		};
	}
		
	protected final Action newGetAllKeysAction() {
		return new Action("getAll") {
			protected Object doAction(Object... params) {
				if(LOG.isDebugEnabled())
					LOG.debug("Getting All Auths");
	
				SSLKeyResponse response = new SSLKeyResponse(SSLKeyResponse.SUCCESS);
				
				Set<String> keyIds = dataManager.getAllKeyIds();
				
				response.getIds().addAll(keyIds);
	
				return response;
			}
		};
	}
	
	protected final Action newCreateCertAction() {
		return new  Action("create") {
			protected Object doAction(Object... params) {
				String keyId = (String) params[0];
				SSLCert cert = (SSLCert) params[1];
				
				if (cert.getId() == null || cert.getId().isEmpty()) {
					cert.setId(UUID.randomUUID().toString());
				}

				if(cert.getDisplayName() == null || cert.getDisplayName().isEmpty()) {
					throw new IllegalArgumentException("Must include a display name when creating a cert");
				}
				
				if(LOG.isDebugEnabled())
					LOG.debug("Creating SSLCert ID:", cert.getId());
				
				cert.setKeyId(keyId);
				
				Certificate certificate = BeanConverterUtil.toDataModel(cert);
				dataManager.addCert(certificate);
			
				SSLCertResponse response = new SSLCertResponse(SSLCertResponse.SUCCESS);
				
				response.setId(cert.getId());
				return response;
			}
		};
	}
	
	protected final Action newUpdateCertAction() {
		return new  Action("update") {
			protected Object doAction(Object... params) {
				SSLCert cert = (SSLCert) params[0];
				String keyId = (String) params[1];
				String certId = (String) params[2];
				
				if(LOG.isDebugEnabled())
					LOG.debug("Updating SSLCert ID: {}", keyId);
				
				if(cert.getId() == null || cert.getId().equals(""))
					cert.setId(certId);
				else if(cert.getId().equals(certId) == false)
					throw new InvalidParameterException("SSLCert ID mismatch");

				cert.setKeyId(keyId);
				
				Certificate certificate = BeanConverterUtil.toDataModel(cert);
				dataManager.updateCert(certificate);
				
				SSLCertResponse response = new SSLCertResponse(SSLCertResponse.SUCCESS);
				response.setId(cert.getId());
				return response;
			}
		};
	}
	
	protected final Action newDeleteCertAction() {
		return new  Action("delete") {
			protected Object doAction(Object... params) {
				String keyId = (String) params[0];
				String certId = (String) params[1];
				
				if(LOG.isDebugEnabled())
					LOG.debug("Deleting Auth ID: {}", keyId);
				
				dataManager.removeCert(certId);
				
				SSLCertResponse response = new SSLCertResponse(SSLCertResponse.SUCCESS);
				return response;
			}
		};
	}
	
	protected final Action newGetCertAction() {
		return new  Action("get") {
			protected Object doAction(Object... params) {

				String id = (String) params[1];
				
				if(LOG.isDebugEnabled())
					LOG.debug("Getting Cert ID: {}", id);
				
				Certificate c = dataManager.getCertById(id, true);
				SSLCert cert = BeanConverterUtil.fromDataModel(c);
				
				SSLCertResponse response = new SSLCertResponse(SSLCertResponse.SUCCESS);
				response.setCert(cert);
				return response;
			}
		};
	}
		
	protected final Action newGetAllCertsAction() {
		return new Action("getAll") {
			protected Object doAction(Object... params) {
				String keyId = (String) params[0];
				
				if(LOG.isDebugEnabled())
					LOG.debug("Getting All Auths");
				
				SSLCertResponse response = new SSLCertResponse(SSLCertResponse.SUCCESS);
				
				
				Set<String> certIds = dataManager.getAllCertIdsForKeyId(keyId);
				response.getIds().addAll(certIds);
	
				return response;
			}
		};
	}
	
	protected final Action newGetCSRAction() {
		return new  Action("get") {
			protected Object doAction(Object... params) {
				String keyId = (String) params[0];
				
				if(LOG.isDebugEnabled())
					LOG.debug("Getting CSR for Key ID: {}", keyId);
				
				/**
				 * TODO: implement
				 */
				//dataManager.generateCSR(keyId);

				// Create a mock object to return until the above logic is implemented
				// If the Cert already exists then throw illegal argument exception.  This logic should go into the datamanager
				if(keyId == null || keyId.isEmpty()){
					throw new IllegalArgumentException("SSLKey with that id does not exist");
				}

				SSLCertResponse response = new SSLCertResponse(SSLCertResponse.FAILURE);
				com.alu.e3.prov.restapi.model.Error e = new com.alu.e3.prov.restapi.model.Error();
				e.setErrorText("Not Implemented");
				response.setError(e);
				return response;
			}
		};
	}
	protected final Action newGetSelfSignedAction() {
		return new  Action("get") {
			protected Object doAction(Object... params) {
				String keyId = (String) params[0];
				
				if(LOG.isDebugEnabled())
					LOG.debug("Getting CSR for Key ID: {}", keyId);
				
				/**
				 * TODO: implement
				 */
				//dataManager.generateCSR(keyId);

				// Create a mock object to return until the above logic is implemented
				// If the Cert already exists then throw illegal argument exception.  This logic should go into the datamanager
				if(keyId == null || keyId.isEmpty()){
					throw new IllegalArgumentException("SSLKey with that id does not exist");
				}

				SSLCertResponse response = new SSLCertResponse(SSLCertResponse.FAILURE);
				com.alu.e3.prov.restapi.model.Error e = new com.alu.e3.prov.restapi.model.Error();
				e.setErrorText("Not Implemented");
				response.setError(e);
				return response;
			}
		};
	}

}
