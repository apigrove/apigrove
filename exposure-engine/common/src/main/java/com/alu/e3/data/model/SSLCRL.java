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
package com.alu.e3.data.model;

import java.io.Serializable;

public class SSLCRL implements Serializable {
	/**
	 * USID - Unique Serial ID
	 */
	private static final long serialVersionUID = -987654244200870901L;

	/**
	 * The key ID. Could be provided.
	 */
    protected String id;
    
    /**
     * The crl content
     */
    protected String content;

    /**
     * CRL display name for administration purpose.
     * If not provided, will be set with CRL's Issuer.
     */
    protected String displayName;

	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getContent() {
		return content;
	}


	public void setContent(String certContent) {
		this.content = certContent;
	}
	
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}


}
