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

/**
 * Interface IAuthDataAccess
 */
public interface IAuthDataAccess {
	public AuthReport checkAllowed(String apiId);
	public AuthReport checkAllowed(String authKey, String apiId);
	public AuthReport checkAllowed(CanonicalizedIpAddress ip, String apiId);
	public AuthReport checkAllowed(String username, String password, String apiId);
	public AuthReport checkAllowed(String username, String passwordDigest, boolean isPasswordText, String nonce, String created, String apiId);
	public AuthReport checkOAuthAllowed(String clientId, String clientSecret, String apiId);
}
