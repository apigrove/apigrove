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
package com.alu.e3.data.model.sub;

import java.io.Serializable;

import com.alu.e3.common.E3Constant;

import com.alu.e3.data.model.enumeration.ActionType;
import com.alu.e3.data.model.enumeration.StatusType;

public class Counter implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1931439800857840947L;

	private StatusType status;
	private ActionType action;
	private float warning;
	private long threshold;
	private transient Integer _REAL_warning;
	
	public Counter() {
		
	}
	
	public Counter(float warning, int threshold) {
		this.warning = warning;
		this.threshold = threshold;
		this.status = StatusType.ACTIVE;
		this.action = E3Constant.DEFAULT_ERROR_ACTION;
	}

	public StatusType getStatus() {
		return status;
	}

	public void setStatus(StatusType status) {
		this.status = status;
	}

	public ActionType getAction() {
		return action;
	}

	public void setAction(ActionType action) {
		this.action = action;
	}

	public float getWarning() {
		return warning;
	}

	public void setWarning(float warning) {
		this.warning = warning;
	}

	public long getThreshold() {
		return threshold;
	}

	public void setThreshold(long threshold) {
		this.threshold = threshold;
	}
	
	public Integer get_REAL_warning() {
		if (_REAL_warning==null) _REAL_warning = Math.round((warning/100.0f)*threshold);
		return _REAL_warning;
	}
}
