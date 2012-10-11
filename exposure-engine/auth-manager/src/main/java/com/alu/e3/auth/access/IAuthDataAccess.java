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
package com.alu.e3.auth.access;

import com.alu.e3.common.camel.AuthReport;
import com.alu.e3.common.tools.CanonicalizedIpAddress;
import com.alu.e3.data.model.Api;

/**
 * Interface IAuthDataAccess
 */
public interface IAuthDataAccess {
	public AuthReport checkAllowed(Api api);
	public AuthReport checkAllowed(Api api, String authKey);
	public AuthReport checkAllowed(Api api, CanonicalizedIpAddress ip);
	public AuthReport checkAllowed(Api api, String username, String password);
	public AuthReport checkAllowed(Api api, String username, String passwordDigest, boolean isPasswordText, String nonce, String created);
	public AuthReport checkOAuthAllowed(Api api, String clientId, String clientSecret);
}
