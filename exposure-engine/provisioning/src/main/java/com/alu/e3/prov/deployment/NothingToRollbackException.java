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
package com.alu.e3.prov.deployment;

import com.alu.e3.prov.ProvisionException;

public class NothingToRollbackException extends ProvisionException {

    private static final long serialVersionUID = -7862928566449313500L;
    
    public NothingToRollbackException(int code, String message, Throwable cause) {
		super(code, message, cause);
	}

	public NothingToRollbackException(int code, String message) {
		super(code, message);
	}

	public NothingToRollbackException(int code, Throwable cause) {
		super(code, cause);
	}

}
