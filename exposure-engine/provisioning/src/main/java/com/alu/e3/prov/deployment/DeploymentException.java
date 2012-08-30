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

import java.util.ArrayList;
import java.util.List;

import com.alu.e3.data.model.Instance;
import com.alu.e3.prov.ProvisionException;

public class DeploymentException extends ProvisionException {

	private static final long serialVersionUID = -6054594686753373061L;
	
	protected List<Instance> toRollBack;

	public DeploymentException(int code, String message, Throwable cause) {
		super(code, message, cause);
		toRollBack = new ArrayList<Instance>();
	}

	public DeploymentException(int code, String message) {
		super(code, message);
		toRollBack = new ArrayList<Instance>();

	}

	public DeploymentException(int code, Throwable cause) {
		super(code, cause);
		toRollBack = new ArrayList<Instance>();

	}

	public List<Instance> getToRollBack() {
		return toRollBack;
	}

	public void setToRollBack(List<Instance> toRollBack) {
		this.toRollBack = toRollBack;
	}

}
