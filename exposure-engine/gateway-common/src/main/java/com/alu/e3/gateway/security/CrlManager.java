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
package com.alu.e3.gateway.security;

import java.security.cert.CRL;
import java.security.cert.Certificate;
import java.util.HashMap;

public class CrlManager {
	
	private HashMap<String, CRL> crlMap = new HashMap<String, CRL>();
	
	public CrlManager() {}

	public void addCRL(String id, CRL crl) {
		crlMap.put(id, crl);
	}

	public void removeCRL(String id) {
		crlMap.remove(id);
	}

	public boolean isCertificateRevoked(Certificate certificate) {
		
		for (CRL crl : crlMap.values()) {
			if (crl.isRevoked(certificate)) {
				return true;
			}
		}
		
		return false;
	}
}
