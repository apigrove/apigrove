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

import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.data.wrapper.BeanConverterUtil;
import com.alu.e3.prov.restapi.model.SSLCRL;
import com.alu.e3.prov.restapi.model.SSLCRLResponse;
import com.alu.e3.prov.restapi.model.SSLCert;
import com.alu.e3.prov.restapi.model.SSLCertResponse;

/**
 * The web service to create, update or delete a policy.
 */
@Controller
@Path("/truststore")
@Description(value = " E3 REST API to add/update/delete trusted CAs and CRLs")
public class TrustStoreManager extends BasicManager{
	
	private static final CategoryLogger LOG = CategoryLoggerFactory.getLogger(TrustStoreManager.class, Category.PROV);
	
	private final Action getAllCA;
	private final Action createCA;
	private final Action getCA;
	private final Action updateCA;
	private final Action deleteCA;
	private final Action getAllCRL;
	private final Action createCRL;
	private final Action getCRL;
	private final Action updateCRL;
	private final Action deleteCRL;

	private IDataManager dataManager;
	
	public TrustStoreManager() {
		getAllCA = newGetAllCAAction();
		createCA = newCreateCAAction();
		getCA = newGetCAAction();
		updateCA = newUpdateCAAction();
		deleteCA = newDeleteCAAction();
		getAllCRL = newGetAllCRLAction();
		createCRL = newCreateCRLAction();
		getCRL = newGetCRLAction();
		updateCRL = newUpdateCRLAction();
		deleteCRL = newDeleteCRLAction();
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
	 * @returnWrapped com.alu.e3.prov.restapi.model.SSLCertResponse
	 */
	@GET
	@Path("/certs")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "This REST API is used to fetch all trusted certificates authorithies.")
	public Response getAllCAs() {
		return execute(getAllCA);
	}

	/**
	 * Add a new trusted Certificate Authority.
	 * 
	 * @inputWrapped com.alu.e3.prov.restapi.model.SSLCert
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.SSLCertResponse
	 */
	@POST
	@Path("/certs")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	@Description(value = "This REST API is used to add a trusted Certificate Authority.")
	public Response addCA(SSLCert request) {
		return execute(createCA, request);
	}
	
	/**
	 * Gets a trusted Certificate Authority.
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.SSLCertResponse
	 */
	@GET
	@Path("/certs/{CA_ID}")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "This REST API is used to fetch trusted Certificate Authority.")
	public Response getCA(@PathParam("CA_ID") String caID) {
		return execute(getCA, caID);
	}
	
	/**
	 * Updates a trusted Certificate Authority.
	 * 
	 * @inputWrapped com.alu.e3.prov.restapi.model.SSLCert
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.SSLCertResponse
	 */
	@PUT
	@Path("/certs/{CA_ID}")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	@Description(value = "This REST API is used to update a trusted Certificate Authority.")
	public Response updateCA(SSLCert request, @PathParam("CA_ID") String caID) {
		return execute(updateCA, request, caID);
	}
	
	/**
	 * Deletes a trusted Certificate Authority.
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.SSLCertResponse
	 */
	@DELETE
	@Path("/certs/{CA_ID}")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "This REST API is used to remove a trusted Certificate Authority.")
	public Response removeCA(@PathParam("CA_ID") String caID) {
		return execute(deleteCA, caID);
	}
	
	/**
	 * Gets all Certificate Revocation List for a Certificate Authority
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.SSLCRLResponse
	 */
	@GET
	@Path("/crls")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "This REST API is used to fetch trusted Certificate Authority.")
	public Response getAllCRLs() {
		return execute(getAllCRL);
	}
	
	/**
	 * Adds a Certificate Revocation List for this Certificate Authority
	 * 
	 * @inputWrapped com.alu.e3.prov.restapi.model.SSLCRL
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.SSLCRLResponse
	 */
	@POST
	@Path("/crls")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	@Description(value = "This REST API is used to add a Certificate Revocation List for this Certificate Authority.")
	public Response addCRL(SSLCRL request) {
		return execute(createCRL, request);
	}
	
	/**
	 * Gets a Certificate Revocation List.
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.SSLCRLResponse
	 */
	@GET
	@Path("/crls/{CRL_ID}")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "This REST API is used to fetch Certificate Revocation List.")
	public Response getCRL(@PathParam("CRL_ID") String crlID) {
		return execute(getCRL, crlID);
	}
	
	/**
	 * Updates a Certificate Revocation List.
	 * 
	 * @inputWrapped com.alu.e3.prov.restapi.model.SSLCRL
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.SSLCRLResponse
	 */
	@PUT
	@Path("/crls/{CRL_ID}")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	@Description(value = "This REST API is used to update a certificate revocation list.")
	public Response updateCRL(SSLCRL request, @PathParam("CRL_ID") String crlID) {
		return execute(updateCRL, request, crlID);
	}
	
	/**
	 * Deletes a  Certificate Revocation List from a Certificate Authority.
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.SSLCRLResponse
	 */
	@DELETE
	@Path("/crls/{CRL_ID}")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "This REST API is used to remove a certificate revocation list from a certificate authority.")
	public Response removeCRL(@PathParam("CRL_ID") String crlID) {
		return execute(deleteCRL, crlID);
	}
		
	protected final Action newGetAllCAAction() {
		return new Action() {

			protected Object doAction(Object... params) {

				LOG.debug("Get all CA");

				SSLCertResponse response = new SSLCertResponse(SSLCertResponse.SUCCESS);
				
				// Get all CA from the store
				response.getIds().addAll(dataManager.getAllCA());

				return response;
			}
		};
	}

	protected final Action newCreateCAAction() {
		return new Action() {

			protected Object doAction(Object... params) {
				SSLCert cert = (SSLCert) params[0];

				if ((cert.getId() == null) || (cert.getId().equals("")))
				{
					// create the id
					cert.setId(UUID.randomUUID().toString());
				}
				
				LOG.debug("Creating CA:", cert.getId());
				
				com.alu.e3.data.model.Certificate certDataModel = BeanConverterUtil.toDataModel(cert);
				dataManager.addCA(certDataModel);
				
				SSLCertResponse response = new SSLCertResponse(SSLCertResponse.SUCCESS);
				response.setId(cert.getId());

				return response;
			}
		};
	}
	
	protected final Action newGetCAAction() {
		return new Action() {

			protected Object doAction(Object... params) {
				String caId = (String) params[0];

				LOG.debug("Getting CA:", caId);
				
				com.alu.e3.data.model.Certificate caDataModel = dataManager.getCAById(caId);
				
				SSLCert cert = BeanConverterUtil.fromDataModel(caDataModel);

				SSLCertResponse response = new SSLCertResponse(SSLCertResponse.SUCCESS);
				response.setCert(cert);
				
				return response;
			}
		};
	}

	protected final Action newUpdateCAAction() {
		return new Action() {

			protected Object doAction(Object... params) {
				SSLCert cert = (SSLCert) params[0];
				String caId = (String) params[1];

				LOG.debug("Updating CA:", caId);

				if(cert.getId() == null || cert.getId().equals(""))
					cert.setId(caId);
				else if(cert.getId().equals(caId) == false)
					throw new InvalidParameterException("CA ID mismatch");

				com.alu.e3.data.model.Certificate certDataModel = BeanConverterUtil.toDataModel(cert);				
				dataManager.updateCA(certDataModel);

				return new SSLCertResponse(SSLCertResponse.SUCCESS);
			}
		};
	}

	protected final Action newDeleteCAAction() {
		return new Action() {

			protected Object doAction(Object... params) {
				String caId = (String) params[0];

				LOG.debug("Deleting CA:", caId);

				dataManager.removeCA(caId);

				return new SSLCertResponse(SSLCertResponse.SUCCESS);
			}
		};
	}

	protected final Action newGetAllCRLAction() {
		return new Action() {

			protected Object doAction(Object... params) {

				LOG.debug("Get all CRL");

				SSLCRLResponse response = new SSLCRLResponse(SSLCRLResponse.SUCCESS);
				
				// Get all CRL from the store
				response.getIds().addAll(dataManager.getAllCRL());

				return response;
			}
		};
	}

	protected final Action newCreateCRLAction() {
		return new Action() {

			protected Object doAction(Object... params) {
				SSLCRL crl = (SSLCRL) params[0];

				if ((crl.getId() == null) || (crl.getId().equals("")))
				{
					// create the id
					crl.setId(UUID.randomUUID().toString());
				}
				
				LOG.debug("Creating CLR:", crl.getId());
				
				com.alu.e3.data.model.SSLCRL clrDataModel = BeanConverterUtil.toDataModel(crl);
				dataManager.addCRL(clrDataModel);
				
				SSLCRLResponse response = new SSLCRLResponse(SSLCRLResponse.SUCCESS);
				response.setId(crl.getId());

				return response;
			}
		};
	}
	
	protected final Action newGetCRLAction() {
		return new Action() {

			protected Object doAction(Object... params) {
				String crlID = (String) params[0];

				LOG.debug("Getting CRL:", crlID);
				
				com.alu.e3.data.model.SSLCRL crlDataModel = dataManager.getCRLById(crlID);
				
				SSLCRL crl = BeanConverterUtil.fromDataModel(crlDataModel);

				SSLCRLResponse response = new SSLCRLResponse(SSLCRLResponse.SUCCESS);
				response.setCRL(crl);
				
				return response;
			}
		};
	}

	protected final Action newUpdateCRLAction() {
		return new Action() {

			protected Object doAction(Object... params) {
				SSLCRL crl = (SSLCRL) params[0];
				String crlID = (String) params[1];

				LOG.debug("Updating CRL:", crlID);

				if(crl.getId() == null || crl.getId().equals(""))
					crl.setId(crlID);
				else if(crl.getId().equals(crlID) == false)
					throw new InvalidParameterException("CRL ID mismatch");

				com.alu.e3.data.model.SSLCRL crlDataModel = BeanConverterUtil.toDataModel(crl);				
				dataManager.updateCRL(crlDataModel);

				return new SSLCRLResponse(SSLCRLResponse.SUCCESS);
			}
		};
	}

	protected final Action newDeleteCRLAction() {
		return new Action() {

			protected Object doAction(Object... params) {
				String crlId = (String) params[0];

				LOG.debug("Deleting CRL:", crlId);

				dataManager.removeCRL(crlId);

				return new SSLCRLResponse(SSLCRLResponse.SUCCESS);
			}
		};
	}
}

