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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.Description;
import org.springframework.stereotype.Controller;

import com.alu.e3.common.E3Constant;
import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.common.osgi.api.IDataManager;
import com.alu.e3.data.wrapper.BeanConverterUtil;
import com.alu.e3.prov.restapi.model.BasicResponse;
import com.alu.e3.prov.restapi.model.ForwardProxy;
import com.alu.e3.prov.restapi.model.ProxyResponse;

/**
 * This class exposes the REST API to set/get the global proxy settings on the provisioning system.
 */
@Controller
@Path("/proxy")
@Description(value = " E3 REST API to set and get the global proxy settings.")
public class ProxySettingsManager extends BasicManager{
	private static final CategoryLogger LOG = CategoryLoggerFactory.getLogger(ProxySettingsManager.class, Category.PROV);
	
	private IDataManager dataManager;
	
	Action set = newSetAction();
	Action get = newGetAction();
	Action delete = newDeleteAction();
	
	/**
	 * 
	 * @param dataManager
	 */
	public void setDataManager(IDataManager dataManager) {
		this.dataManager = dataManager;
	}
	
	public ProxySettingsManager() {

	}
		

	/**
	 * Sets the global proxy settings.
	 * 
	 * @inputWrapped com.alu.e3.prov.restapi.model.ForwardProxy
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.ProxyResponse
	 */
	@POST
	@Path("")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	@Description(value = "This REST API is used to set the global proxy settings.")
	public Response set(ForwardProxy request) {
		
		
		return this.execute(set, request);
	}

	/**
	 * Gets the global proxy settings.
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.ProxyResponse
	 */
	@GET
	@Path("")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "This REST API is used to fetch the global proxy settings.")
	public Response get() {
		return this.execute(get);
	}

	/**
	 * Gets the global proxy settings.
	 * 
	 * @returnWrapped com.alu.e3.prov.restapi.model.ProxyResponse
	 */
	@DELETE
	@Path("")
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.WILDCARD })
	@Description(value = "This REST API is used to delete the global proxy settings.")
	public Response delete() {
		return this.execute(delete);
	}

	protected final Action newSetAction() {
		return new Action() {

			@Override
			protected Object doAction(Object... params) {
				ForwardProxy proxy = (ForwardProxy) params[0];

				if(proxy == null) {
				}				
												
				dataManager.putSettingString(E3Constant.GLOBAL_PROXY_SETTINGS, BeanConverterUtil.toDataModel(proxy).serialize());

				BasicResponse response = new BasicResponse(ProxyResponse.SUCCESS);

				return response;
			}
		};
	}
	
	protected final Action newGetAction() {
		return new Action() {

			@Override
			protected Object doAction(Object... params) {				

				ProxyResponse response = new ProxyResponse(BasicResponse.SUCCESS);
				String serializedProxy = dataManager.getSettingString(E3Constant.GLOBAL_PROXY_SETTINGS);
				ForwardProxy proxy = null;
				if (serializedProxy != null) {
					proxy = BeanConverterUtil.fromDataModel(com.alu.e3.data.model.sub.ForwardProxy.deserialize(serializedProxy));
				} 
				if (proxy == null) {
					proxy = new ForwardProxy();
				}
				response.setProxy(proxy);
				return response;
			}
		};
	}
	
	protected final Action newDeleteAction() {
		return new Action() {

			@Override
			protected Object doAction(Object... params) {				

				dataManager.clearSettingString(E3Constant.GLOBAL_PROXY_SETTINGS);

				BasicResponse response = new BasicResponse(ProxyResponse.SUCCESS);

				return response;
			}
		};
	}
}
