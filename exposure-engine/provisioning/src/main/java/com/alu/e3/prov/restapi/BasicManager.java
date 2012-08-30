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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.alu.e3.prov.restapi.model.BasicResponse;

public class BasicManager {


	/**
	 * Generic method to handle REST response and status.
	 * 
	 * @param params
	 * @return
	 */
	protected final Response execute(Action action, final Object... params) {

		BasicResponse response = (BasicResponse) action.doAction(params);
		ResponseBuilder builder = Response.ok(response);
		if(response.getError() != null){
			builder.status(new Integer(response.getError().getErrorCode()));
		}

		return builder.build();
	}


	/**
	 * Utility method to re-factor some REST code and introduce a generic method
	 * doAction to implement business logic.
	 * 
	 */
	protected static class Action {
		String name;

		protected Action() {
		}

		protected Action(String name) {
			this.name = name;
		}

		protected String getName() {
			return this.name;
		}

		/**
		 * Business action to implement
		 * 
		 * @param params
		 * @return
		 */
		protected Object doAction(final Object... params) {
			return null;
		}	

	}


}
