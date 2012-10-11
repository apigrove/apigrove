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
/**
 * 
 */
package com.alu.e3;

import com.alu.e3.common.logging.Category;
import com.alu.e3.common.logging.CategoryLogger;
import com.alu.e3.common.logging.CategoryLoggerFactory;
import com.alu.e3.common.osgi.api.IInstanceInfo;

public class SystemManagerInfo {
	private static final CategoryLogger logger = CategoryLoggerFactory.getLogger(SystemManagerInfo.class, Category.SYS);
	protected IInstanceInfo instanceInfo;

	public void setInstanceInfo(IInstanceInfo instanceInfo) {
		this.instanceInfo = instanceInfo;
	}
	
	public SystemManagerInfo() {}
	
	public void init() {
		if(logger.isWarnEnabled()) {
			logger.warn("Elected as manager");
		}
		instanceInfo.setManager(true);
	}
	
}
