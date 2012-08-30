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

package com.alu.e3.prov.restapi.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * The API  type, it can either be PassThrough or Composite 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "https", propOrder = {
    "enabled"
})
public class HTTPSType {

    @XmlValue
    protected boolean enabled;
    
    @XmlAttribute(name = "tlsMode")
    protected TLSMode tlsMode;

    
	public boolean isEnabled() {
		return enabled;
	}
	
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	

	public TLSMode getTlsMode() {
		return tlsMode;
	}


	public void setTlsMode(TLSMode tlsMode) {
		this.tlsMode = tlsMode;
	}

}
